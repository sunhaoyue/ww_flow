package com.wwgroup.flow.service.stage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.helper.JointSignType;
import com.wwgroup.flow.bo.work.JointSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.user.bo.EmployeePos;

// 驳回逻辑：驳回动作只有在审核阶段才能够发送，
public class ChengHeFlowStageProcessor extends AbstractFlowStageProcessor {

	@Override
	boolean startValidate(Flow flow) {
		//System.out.println("Start: " + this.getClass().getName());
		return true;
	}

	@Override
	Map<String, PersonDetail> doStart(Flow flow) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);

		boolean isRecreate = flow.getId() > 0;
		boolean hasWorkRecord = false;
		if (isRecreate) {
			hasWorkRecord = this.flowDao.listWorks(flow).length > 0;
		}
		boolean isPreTemplate = flow.isTempalte();
		boolean isSuccess = true;

		/*
		 * flow.setStatus(FlowStatus.CHENGHE_START); if
		 * (flow.getInnerJointSignIds() == null) { isSuccess =
		 * this.flowService.saveFlow(flow); }
		 */
		/*
		 * 修改代码，既没有内部会办，也没有同CMPCODE会办 edited by zhangqiang at 2012-11-20 15:49
		 */
		if (flow.getInnerJointSignIds() == null) {
			isSuccess = this.flowService.saveFlow(flow);
		}
		flow.setStatus(FlowStatus.CHENGHE_START);

		if (isSuccess) {
			PersonDetail actualPerson = flow.getActualPerson();
			// 新的表单 或者 是 暂存表单 而且没有 work记录 的 都可以进入
			if (!isRecreate || (isPreTemplate && !hasWorkRecord)) {
				// 添加发起人工作项(自动完成)
				MyWork createWork = actualPerson.buildWork(flow.getFlowType(),
						WorkStage.CREATE, null);
				createWork.setFlowId(flow.getId());
				flowDao.saveWork(createWork, flowService.getOrganDao());
				createWork
						.setStatus(flow.getRenewTimes() > 0 ? FlowStatus.RECREATE
								: FlowStatus.INIT);
				this.flowService.updateWorkStatus(createWork);

				// result.put(actualPerson.getEmployeeId() +
				// actualPerson.getPostCode(), actualPerson);
			}
			// 跟据目前情况产生新的work然后发送给调用方
			// TODO: 后续会改进接口, （内部会办），先上报，然后会办，最后核准，（抄送单位），派发任务
			// 开始启动工作，首先是启动发文部分，其次是会办部分，申请人验收（可选）,主管核准
			// 修改流程状态进入 发文进行阶段，然后找到最后一个完成的发文work，发给其上级

			// 根据这个人以及所属岗位找到其上级领导, 如果没有那么就结束发文阶段，直接进入会办阶段
			result.putAll(this.flowService.startNextWork(flow, actualPerson,
					null));
		}
		return result;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CHENGHE_START;
	}

	@SuppressWarnings("unused")
	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (prevWork != null
				&& StringUtils.isEmpty(prevWork.getJoinSignStartId())) {
			MyWork work = personDetail.buildWork(flow.getFlowType(),
					WorkStage.CHENGHE, prevWork);
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			work.setFlowId(flow.getId());
			work.setJoinCycle(work.getJoinCycle());
			flowDao.saveWork(work, flowService.getOrganDao());
			result.put(
					personDetail.getEmployeeId() + personDetail.getPostCode(),
					personDetail);
			return result;
		}

		// 核呈有用 decsionMaker 0-4级别
		// int finishedShengheStep = flow.getShengheStep();
		// 最终逻辑确定：这里就是找上级，然后判断找到的上级是否是核决主管，如果到了就先停止，先执行后面的步骤
		// 如果是flow为local的话就是调用personService找到其上一级负责人，如果负责人的身份是选择的核决主管，那么就停下来进入核决阶段了
		if (flow.isLocal()) {
			PersonDetail mgrPerson = null;
			try {
				mgrPerson = personService.getMgrPersonDetail(personDetail);
			} catch (Exception e) {
				throw new RuntimeException("当前处理人或下一步处理人汇报关系维护有误，请联系系统管理员处理");
			}

			/**
			 * 用于判断主线中当前审核人的上一级是否是最终核决的主管
			 * 例如：当前表单选择的核决主管为神旺控股最高主管，则判断当前审核人的上一级是否就是神旺控股的最高主管 2014-06-24
			 * 目前需要修改为如果最终核决的是神旺控股最高主管，则判断当前审核的人上一级是否是神旺控股最高主管或副主管
			 */
			// 如果上级主管对应岗位是最高副主管或核决最高主管，如果是就先停止，先执行后面的步骤
			// 上级是否是最高副主管
			//boolean isTopFHead = personService.isCenterFHead(
			//		mgrPerson.getEmployeeId(), mgrPerson.getPostCode());
			// 上级是否是最终核决主管的副主管
			boolean isTopFHead = personService.quailifiedDecisionMakerPlus(flow.getDecionmaker(), mgrPerson);
			// 上级是否是最终核决主管
			boolean isTopMgr = personService.quailifiedDecisionMaker(
					flow.getDecionmaker(), mgrPerson);
			if (isTopFHead || isTopMgr) {
				// 如果不是最终核决的主管
				PersonDetail tmpMgrPerson = mgrPerson;
				while (!isTopMgr) {
					try {
						tmpMgrPerson = personService
								.getMgrPersonDetail(mgrPerson);
					} catch (Exception e) {
						tmpMgrPerson = mgrPerson;
					}
					isTopMgr = personService.quailifiedDecisionMaker(
							flow.getDecionmaker(), tmpMgrPerson);
				}
				if (isTopMgr) {
					flow.setShengheEmployeeId(tmpMgrPerson.getEmployeeId());
					flow.setShengheDeptId(tmpMgrPerson.getDeptId());
					flow.setShenghePostCode(tmpMgrPerson.getPostCode());
					flow.setShengheStep(flow.getDecionmaker().ordinal());
					flowDao.saveFlowShengheProperties(flow.getId(),
							tmpMgrPerson.getEmployeeId(),
							tmpMgrPerson.getDeptId(),
							tmpMgrPerson.getPostCode(), flow.getShengheStep());
				}
				// if
				// (personService.quailifiedDecisionMaker(flow.getDecionmaker(),
				// mgrPerson)) {
				// 将副主管工号、部门ID、岗位代码写入FLOW中
				if (isTopFHead) {
					flow.setSecondEmployeeId(mgrPerson.getEmployeeId());
					flow.setSecondDeptId(mgrPerson.getDeptId());
					flow.setSecondPostCode(mgrPerson.getPostCode());
					flowDao.saveFlowSecondProperties(flow.getId(),
							mgrPerson.getEmployeeId(), mgrPerson.getDeptId(),
							mgrPerson.getPostCode());
				}
				/*
				 * if (isTopMgr) {
				 * flow.setShengheEmployeeId(mgrPerson.getEmployeeId());
				 * flow.setShengheDeptId(mgrPerson.getDeptId());
				 * flow.setShenghePostCode(mgrPerson.getPostCode());
				 * flow.setShengheStep(flow.getDecionmaker().ordinal());
				 * flowDao.saveFlowShengheProperties(flow.getId(),
				 * mgrPerson.getEmployeeId(), mgrPerson.getDeptId(),
				 * mgrPerson.getPostCode(), flow.getShengheStep()); }
				 */
				// 进入本人确认 然后进入核决阶段
				if (!StringUtils.isEmpty(flow.getJointSignDeptName())) {
					flow.setStatus(FlowStatus.JOINTSIGN_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this
							.delegateJointSignWork(flow, personDetail));
				} else {
					if (flow.isSelfConfirm()) {
						flow.setStatus(FlowStatus.CONFIRM_START);
						super.flowDao.updateFlow(flow);
						// 我这里传入的是实际的申请人，而不是代申请人
						result.putAll(this.flowService.startNextWork(flow,
								flow.getActualPerson(), null));
					} else {
						if (isTopFHead) {
							flow.setStatus(FlowStatus.SECONDFINAL_DECISION_START);
						} else {
							flow.setStatus(FlowStatus.FINAL_DECISION_START);
						}
						super.flowDao.updateFlow(flow);
						result.putAll(this.flowService.startNextWork(flow,
								mgrPerson, null));
					}
				}
			} else {
				MyWork work = mgrPerson.buildWork(flow.getFlowType(),
						WorkStage.CHENGHE, null);
				work.setFlowId(flow.getId());
				flowDao.saveWork(work, flowService.getOrganDao());
				super.flowDao.updateFlow(flow);
				result.put(mgrPerson.getEmployeeId() + mgrPerson.getPostCode(),
						mgrPerson);
			}
		}
		// 如果flow为remote的话， 先判断shengheEmployeeId字段中是否有记录，
		// 在没有值的情况下, 查找地方的最高主管，
		// 直到最高主管了，然后往shengheEmployeeId里面填值。
		// 在有值得情况下 也是查找其员工上面一级 并且decisionMaker序列 + 1 ：employeeId
		// 记录到shengheEmployeeId字段中，
		// 如果其记录的shengheEmployeeId中的decsionMaker序列大于或者等于Flow规定的decsionMaker序列，就跳转到核决阶段。
		else if (!flow.isLocal()) {
			if (StringUtils.isEmpty(flow.getShengheEmployeeId())) {
				// 查找本单位的地方主管
				EmployeePos actualPerson = userService
						.getEmployeePosByEmpId(flow.getActualPerson()
								.getEmployeeId());
				EmployeePos mgrEmployee = userService
						.getEmployeePosByEmpId(actualPerson.getMgremployeeid());
				PersonDetail mgrPerson = null;
				// System.out.println("查找本单位主管：" + mgrEmployee.getEmployeeid() +
				// "#" + mgrEmployee.getPostcode() + "#" +
				// mgrEmployee.isTopmgr());
				
				// 下一步处理人
				PersonDetail nextPerson = null;
				boolean isTop = false;
				
				// 查找本单位是否有最高副主管，如果没有，则查找出最高主管
				PersonDetail topFMgrPerson = null;
				topFMgrPerson = personService.getCenterFEmployeeByCmpCode(actualPerson.getCmpcod());
				// 不为NULL，则表示申请人所属单位存在单位最高副主管
				if (topFMgrPerson != null){
					// 如果副主管和当前传入的处理人是同一个人，则说明副主管已审核，则直接转至他的上一级
					if (topFMgrPerson.getEmployeeId().equals(personDetail.getEmployeeId())){
						nextPerson = personService.getMgrPersonDetail(topFMgrPerson);
						isTop = true;
					} else {
						EmployeePos tmpEmployeePos = userService.getEmployeePosByEmpId(personDetail.getEmployeeId(),
								personDetail.getPostCode());
						if (tmpEmployeePos.isTopmgr()){
							nextPerson = personService.loadWidePersonDetail(tmpEmployeePos.getEmployeeid());
							isTop = true;
						} else {
							nextPerson = topFMgrPerson;
						}
					}
				} else {
					if (mgrEmployee.isTopmgr()) {
						SystemGroups groups = this.flowService.getOrganDao()
								.getGroupsByDeptCode(mgrEmployee.getDeptcode(),
										mgrEmployee.getA_deptcode(),
										mgrEmployee.getCmpcod(),
										GroupType.DEPTGROUP);
						nextPerson = personService.loadWidePersonDetail(String.valueOf(groups.getGroupID()),
								mgrEmployee.getEmployeeid(), mgrEmployee.getPostcode());
					} else {
						nextPerson = personService
								.getCenterEmployeeByCmpCode(actualPerson
										.getCmpcod());
					}
					isTop = true;
				}
				
				/*boolean isTop = false;
				if (mgrEmployee.isTopFmgr()){
					mgrPerson = personService.loadWidePersonDetail(mgrEmployee
							.getEmployeeid());
				} else {
					if (mgrEmployee.isTopmgr()) {
						mgrPerson = personService.loadWidePersonDetail(mgrEmployee
								.getEmployeeid());
					} else {
						mgrPerson = personService
								.getCenterEmployeeByCmpCode(actualPerson
										.getCmpcod());
					}
					isTop = true;
				}*/
				if (isTop){
				flow.setShengheEmployeeId(nextPerson.getEmployeeId());
				flow.setShengheStep(DescionMaker.UNITLEADER.ordinal());
				flow.setShengheDeptId(nextPerson.getDeptId());
				flow.setShenghePostCode(nextPerson.getPostCode());
				flowDao.saveFlowShengheProperties(flow.getId(),
						nextPerson.getEmployeeId(), nextPerson.getDeptId(),
						nextPerson.getPostCode(), flow.getShengheStep());
				}
				// 记录下该状态后，还是继续发请求给这个主管.
				MyWork work = nextPerson.buildWork(flow.getFlowType(),
						WorkStage.CHENGHE, null);
				work.setFlowId(flow.getId());

				flowDao.saveWork(work, flowService.getOrganDao());

				result.put(nextPerson.getEmployeeId() + nextPerson.getPostCode(),
						nextPerson);
			} else {
				int shengheStep = flow.getShengheStep();
				PersonDetail mgrPerson = personService
						.getMgrPersonDetail(personDetail);

				// 判断该人是否是最终核决主管 如果是 跳过该人的审核
				boolean cont = false;
				if (flow.getDecionmaker() == DescionMaker.HEADLEADER
						&& mgrPerson.getMgrEmployeeid().equals(
								mgrPerson.getEmployeeId())) {
					cont = true;
				}

				// 判断该人是否是地方最高主管
				// 通过当前人员的工号、岗位代码判断是否是单位最高主管
				boolean isTopLocal = personService.isTopMgr(personDetail);

				// 如果上级主管对应岗位是最高副主管或核决最高主管，如果是就先停止，先执行后面的步骤
				// 上级是否是最高副主管
				boolean isTopFHead = personService.isCenterFHead(
						mgrPerson.getEmployeeId(), mgrPerson.getPostCode());
				// 上级是否是最终核决主管
				/*
				 * boolean isTopMgr = personService.quailifiedDecisionMaker(
				 * flow.getDecionmaker(), mgrPerson);
				 */
				if (isTopLocal) {
					//if (flow.getDecionmaker().equals(DescionMaker.HEADLEADER)){
						flow.setSecondEmployeeId(mgrPerson.getEmployeeId());
						flow.setSecondDeptId(mgrPerson.getDeptId());
						flow.setSecondPostCode(mgrPerson.getPostCode());
						flowDao.saveFlowSecondProperties(flow.getId(),
								mgrPerson.getEmployeeId(), mgrPerson.getDeptId(),
								mgrPerson.getPostCode());
					//}

					flow.setShengheEmployeeId(mgrPerson.getEmployeeId());
					flow.setShengheStep(shengheStep + 1);
					flow.setShengheDeptId(mgrPerson.getDeptId());
					flow.setShenghePostCode(mgrPerson.getPostCode());
					flowDao.saveFlowShengheProperties(flow.getId(),
							mgrPerson.getEmployeeId(), mgrPerson.getDeptId(),
							mgrPerson.getPostCode(), flow.getShengheStep());
				}
				// 如果不是最终核决的主管
				PersonDetail tmpMgrPerson = mgrPerson;

				if (shengheStep + 1 < flow.getDecionmaker().ordinal() && !cont && !isTopLocal) {
					MyWork work = mgrPerson.buildWork(flow.getFlowType(),
							WorkStage.CHENGHE, null);
					work.setFlowId(flow.getId());
					flowDao.saveWork(work, flowService.getOrganDao());

					result.put(
							mgrPerson.getEmployeeId() + mgrPerson.getPostCode(),
							mgrPerson);
				} else {
					// 进入下一阶段
					if (!StringUtils.isEmpty(flow.getJointSignDeptName())) {
						flow.setStatus(FlowStatus.JOINTSIGN_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.delegateJointSignWork(flow,
								personDetail));
					} else {
						if (flow.isSelfConfirm()) {
							flow.setStatus(FlowStatus.CONFIRM_START);
							super.flowDao.updateFlow(flow);
							// 我这里传入的是实际的申请人，而不是代申请人
							result.putAll(this.flowService.startNextWork(flow,
									flow.getActualPerson(), null));
						} else {
							// 如果上级是神旺副主管或当前人是单位最高主管
							if (isTopLocal && flow.getDecionmaker().equals(DescionMaker.HEADLEADER)) {
								flow.setStatus(FlowStatus.SECONDFINAL_DECISION_START);
							} else {
								flow.setStatus(FlowStatus.FINAL_DECISION_START);
							}
							super.flowDao.updateFlow(flow);
							result.putAll(this.flowService.startNextWork(flow,
									mgrPerson, null));
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//System.out.println("ChengHeFlowStageProcessor");
		return flow.getStatus() == FlowStatus.CHENGHE_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		// System.out.println("ChengHe");
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (!StringUtils.isEmpty(work.getJoinSignStartId())) {
			work.setStatus(FlowStatus.AGREE);
			this.flowService.updateWorkStatus(work);
			PersonDetail personDetail = personService.loadWidePersonDetail(
					work.getDeptId(), work.getEmployeeId(), work.getPostCode());
			PersonDetail mgrPersonDetail = personService
					.getMgrPersonDetail(personDetail);
			result.putAll(this.flowService.startNextWork(flow, mgrPersonDetail,
					work));
			return result;
		}

		// 领导审核与传统的 会签等区分 所以这边用了 另外一个字段来表示
		work.setStatus(FlowStatus.APPROVED);
		this.flowDao.updateWork(work);
		PersonDetail personDetail = personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		return result;
	}

	private Map<String, PersonDetail> delegateJointSignWork(Flow flow,
			PersonDetail personDetail) {
		if (flow.getFlowType() == FlowType.QIANCHENG && !flow.isLocal()) {
			return this.delegateQianChengRemoteSignWork(flow, personDetail);
		} else {
			return this.delegateNotQianChengRemoteSignWork(flow);
		}
	}

	private Map<String, PersonDetail> delegateNotQianChengRemoteSignWork(
			Flow flow) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 没有上一级领导了，那么就发起后续活动，可能是会签，或者其它的
		if (flow.getJointSignDeptIds() != null
				&& flow.getJointSignDeptIds().length > 0) {
			// TODO: 直接进入下一个阶段：会办阶段, 需要再明确下会办的情况
			// 首先获取到flow对于会办的顺序还是同步的选择，然后根据记录的会办部门等情况，发出会办的任务给各方
			String[] jointSignDeptIds = flow.getJointSignDeptIds();
			if (JointSignType.SEQUENCE == flow.getJointSignType()) {
				// 顺序会办
				// 开始会办，读取第一个会办元素，进行转换后，得到相应的人员信息，并交由他来办理工作
				String jointSignDeptId = jointSignDeptIds[0];
				// TODO: 这边需要询问清楚情况
				// TODO: 通过传入的信息找到部门主管
				PersonDetail person = personService
						.loadWideMgrPersonDetail(jointSignDeptId);
				JointSignWork work = (JointSignWork) person.buildWork(
						flow.getFlowType(), WorkStage.JOINTSIGN, null);

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("employeeId", person.getEmployeeId());
				jsonObj.put("deptId", person.getDeptId());
				JSONArray jsonArray = new JSONArray();
				jsonArray.add(jsonObj);

				work.setJoinSignStartId(jsonArray.toString());
				work.setWorknum(work.getId());
				work.setFlowId(flow.getId());
				work.setJoinCycle(jsonArray.size());
				work.setJoinStartEmployeeId(person.getEmployeeId());
				this.flowService.startNextWork(flow, person, work);

				result.put(person.getEmployeeId() + person.getPostCode(),
						person);
			} else if (JointSignType.CONCURRENT == flow.getJointSignType()) {
				// 同时会办
				for (int i = 0; i < jointSignDeptIds.length; i++) {
					String jointSignDeptId = jointSignDeptIds[i];
					// TODO: 这边需要询问清楚情况
					// TODO: 通过传入的信息找到部门主管
					PersonDetail person = personService
							.loadWideMgrPersonDetail(jointSignDeptId);
					JointSignWork work = (JointSignWork) person.buildWork(
							flow.getFlowType(), WorkStage.JOINTSIGN, null);

					JSONObject jsonObj = new JSONObject();
					jsonObj.put("employeeId", person.getEmployeeId());
					jsonObj.put("deptId", person.getDeptId());
					JSONArray jsonArray = new JSONArray();
					jsonArray.add(jsonObj);

					work.setJoinSignStartId(jsonArray.toString());
					work.setWorknum(work.getId() + i);
					work.setFlowId(flow.getId());
					work.setJoinCycle(jsonArray.size());
					work.setJoinStartEmployeeId(person.getEmployeeId());
					this.flowService.startNextWork(flow, person, work);

					result.put(person.getEmployeeId() + person.getPostCode(),
							person);
				}

			}
		}
		return result;
	}

	/**
	 * 签呈 -> 地方到总部
	 * 
	 * @param flow
	 * @param prevWork
	 * @return
	 */
	private Map<String, PersonDetail> delegateQianChengRemoteSignWork(
			Flow flow, PersonDetail personDetail) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 没有上一级领导了，那么就发起后续活动，可能是会签，或者其它的
		if (flow.getJointSignDeptIds() != null
				&& flow.getJointSignDeptIds().length > 0) {
			PersonDetail actualPerson = this.personService
					.loadWidePersonDetail(flow.getActualPerson()
							.getEmployeeId());
			// TODO: 直接进入下一个阶段：会办阶段, 需要再明确下会办的情况
			// 首先获取到flow对于会办的顺序还是同步的选择，然后根据记录的会办部门等情况，发出会办的任务给各方
			String[] jointSignDeptIds = flow.getJointSignDeptIds();
			if (JointSignType.SEQUENCE == flow.getJointSignType()) {
				// 顺序会办
				// 开始会办，读取第一个不是同CMPCODE会办元素，进行转换后，得到相应的人员信息，并交由他来办理工作
				PersonDetail person = null;
				for (int i = 0; i < jointSignDeptIds.length; i++) {
					String jointSignDeptId = jointSignDeptIds[i];
					person = personService
							.loadWideMgrPersonDetail(jointSignDeptId);
					if (!person.getCmpCode().equals(actualPerson.getCmpCode())) {
						break;
					}
				}

				if (null == person) {
					if (flow.isSelfConfirm()) {
						flow.setStatus(FlowStatus.CONFIRM_START);
						super.flowDao.updateFlow(flow);
						// 我这里传入的是实际的申请人，而不是代申请人
						result.putAll(this.flowService.startNextWork(flow,
								flow.getActualPerson(), null));
					} else {
						PersonDetail mgrPerson = personService
								.getMgrPersonDetail(personDetail);
						flow.setStatus(FlowStatus.FINAL_DECISION_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.flowService.startNextWork(flow,
								mgrPerson, null));
					}

				} else {
					// TODO: 这边需要询问清楚情况
					// TODO: 通过传入的信息找到部门主管
					JointSignWork work = (JointSignWork) person.buildWork(
							flow.getFlowType(), WorkStage.JOINTSIGN, null);

					JSONObject jsonObj = new JSONObject();
					jsonObj.put("employeeId", person.getEmployeeId());
					jsonObj.put("deptId", person.getDeptId());
					JSONArray jsonArray = new JSONArray();
					jsonArray.add(jsonObj);

					work.setJoinSignStartId(jsonArray.toString());
					work.setWorknum(work.getId());
					work.setFlowId(flow.getId());
					work.setJoinCycle(jsonArray.size());
					work.setJoinStartEmployeeId(person.getEmployeeId());
					this.flowService.startNextWork(flow, person, work);

					result.put(person.getEmployeeId() + person.getPostCode(),
							person);
				}
			} else if (JointSignType.CONCURRENT == flow.getJointSignType()) {
				/*
				 * 签呈，同时会办，取不同的CMPCODE进行会办
				 */

				// 同时会办
				int count = 0;
				for (int i = 0; i < jointSignDeptIds.length; i++) {
					String jointSignDeptId = jointSignDeptIds[i];
					// TODO: 这边需要询问清楚情况
					// TODO: 通过传入的信息找到部门主管
					PersonDetail person = personService
							.loadWideMgrPersonDetail(jointSignDeptId);
					if (!person.getCmpCode().equals(actualPerson.getCmpCode())) {
						JointSignWork work = (JointSignWork) person.buildWork(
								flow.getFlowType(), WorkStage.JOINTSIGN, null);

						JSONObject jsonObj = new JSONObject();
						jsonObj.put("employeeId", person.getEmployeeId());
						jsonObj.put("deptId", person.getDeptId());
						JSONArray jsonArray = new JSONArray();
						jsonArray.add(jsonObj);

						work.setJoinSignStartId(jsonArray.toString());
						work.setWorknum(work.getId() + count);
						work.setFlowId(flow.getId());
						work.setJoinCycle(jsonArray.size());
						work.setJoinStartEmployeeId(person.getEmployeeId());
						this.flowService.startNextWork(flow, person, work);

						result.put(
								person.getEmployeeId() + person.getPostCode(),
								person);
						count++;
					}
				}

				if (count == 0) {
					if (flow.isSelfConfirm()) {
						flow.setStatus(FlowStatus.CONFIRM_START);
						super.flowDao.updateFlow(flow);
						// 我这里传入的是实际的申请人，而不是代申请人
						result.putAll(this.flowService.startNextWork(flow,
								flow.getActualPerson(), null));
					} else {
						PersonDetail mgrPerson = personService
								.getMgrPersonDetail(personDetail);
						if (flow.getSecondEmployeeId() != null) {
							flow.setStatus(FlowStatus.SECONDFINAL_DECISION_START);
						} else {
							flow.setStatus(FlowStatus.FINAL_DECISION_START);
						}
						// flow.setStatus(FlowStatus.FINAL_DECISION_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.flowService.startNextWork(flow,
								mgrPerson, null));
					}
				}

			}
		}
		return result;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CHENGHE_START;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.CHENGHE_START;
	}

}
