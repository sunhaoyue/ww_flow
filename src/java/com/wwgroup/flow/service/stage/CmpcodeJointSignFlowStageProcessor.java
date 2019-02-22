package com.wwgroup.flow.service.stage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.helper.JointSignType;
import com.wwgroup.flow.bo.work.CmpcodeJoinSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.user.bo.EmployeePos;

/**
 * 同CMPCODE会办处理器
 * 
 * @creator zhangqiang
 * @create-time Nov 20, 2012 7:54:02 AM
 * @version 0.1
 */
public class CmpcodeJointSignFlowStageProcessor extends
		AbstractFlowStageProcessor {
	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 完成myWork然后判断 本身是不是joinSignStartId，如果是的话，那么就结束分支会办的工作了
		if (CmpcodeJoinSignWork.class.isInstance(work)) {
			work.setStatus(FlowStatus.AGREE);
			this.flowService.updateWorkStatus(work);
			// JointSignType jointSignType = flow.getJointSignType();

			String joinSignStartId = super.getLatestJoinSignStartId(
					work.getJoinSignStartId(), "employeeId");

			/**
			 * add by Cao_Shengyong 2014-03-26 用于处理本单位会办的呈核上一级逻辑
			 * 
			 * 通过前端参数，可以确认是否是点击的“呈核上一级”按钮 1:表示是点击的呈核上一级按钮 2:表示的是呈核后的主管签核
			 */
			if (work.getHb_ChengHe().equals("1")
					|| work.getHb_ChengHe().equals("2")) {
				EmployeePos employeePos = personService.loadWideEmployeePos(
						work.getEmployeeId(), work.getPostCode());
				if (employeePos != null) {
					/**
					 * 当前审批人本身就是单位主管，则直接结束会办分支
					 * 如果当前会办签核时不是主管，则不可以执行此处的处理逻辑，即页面上无法看到“呈核上一级”按钮
					 * 并且将会办呈核上一级后审核的相关主管的签核记录HB_CHENGHE设置为2
					 */
					if (work.getHb_ChengHe().equals("1")) {
						work.setHb_ChengHe("2");
					}
					if (employeePos.getMgCentFlg() == 1) {
						// 在会办结束时，更新历史同一会办下的HB相关信息
						this.flowService.updateHbWorkJoin(work);
						// 获取同一会办下第一条会办记录。即时间最早的一条
						MyWork tmpWork = this.flowService.getFirstHBWork(work);
						// 更新会办分支所有记录的ORGPATH
						this.flowService.updateHbWorkOrgPath(work, tmpWork);
						result.putAll(this.delegateDoComplete(flow, work));
					} else {
						/**
						 * 如果审批人本身是中心主管，则需要将审核记录的呈核值设为单位主管 表示需要呈核至单位主管才能结束
						 */
						if (work.getEmployeeId().equalsIgnoreCase(
								work.getHb_JoinStartEmployeeId())) {
							// 在会办结束时，更新历史同一会办下的HB相关信息
							this.flowService.updateHbWorkJoin(work);
							// 获取同一会办下第一条会办记录。即时间最早的一条
							MyWork tmpWork = this.flowService.getFirstHBWork(work);
							// 更新会办分支所有记录的ORGPATH
							this.flowService.updateHbWorkOrgPath(work, tmpWork);
							result.putAll(this.delegateDoComplete(flow, work));
						} else {
							if (employeePos.getMgA_DeptFlg() == 1) {
								work.setHb_ChengHeEnd(GroupType.CMPGROUP.name());
							} else {
								// 如果审批人本身是部门主管，则需呈核至中心主管才可以
								work.setHb_ChengHeEnd(GroupType.CENTERGROUP
										.name());
							}

							PersonDetail mgrPersonDetail_Tmp = personService
									.getMgrPersonByIdAndPostCode(
											work.getEmployeeId(),
											work.getPostCode(),
											work.getHb_ChengHeEnd());
							if (mgrPersonDetail_Tmp != null) {
								work.setHb_JoinStartEmployeeId(mgrPersonDetail_Tmp
										.getEmployeeId());
								String hbJoinStartId = super
										.setHbJoinSignStartId(work
												.getJoinSignStartId(),
												mgrPersonDetail_Tmp
														.getEmployeeId());
								work.setHb_JoinSignStartId(hbJoinStartId);
								
								// 否则，需继续向上汇报，直到签核人为主管
								// 自动向其领导汇报,
								PersonDetail personDetail = personService
										.loadWidePersonDetail(work.getDeptId(),
												work.getEmployeeId(), work.getPostCode());
								PersonDetail mgrPersonDetail = personService
										.getMgrPersonDetail(personDetail);

								result.put(mgrPersonDetail.getEmployeeId()
										+ mgrPersonDetail.getPostCode(), personDetail);
								result.putAll(this.flowService.startNextWork(flow,
										mgrPersonDetail, work));
							} else {
								new RuntimeException("无法查询到工号["
										+ work.getEmployeeId() + "]的岗位["
										+ work.getPostCode() + "]的上级["
										+ work.getHb_ChengHeEnd() + "]的主管信息!");
							}
						}
					}
				} else {
					// 如果通过工号和岗位信息无法查询到相应信息，则直接抛出错误
					new RuntimeException("无法查询到工号[" + work.getEmployeeId()
							+ "]的岗位[" + work.getPostCode()
							+ "]相关信息，可能是汇报关系维护错误，请与系统管理员联系！");
				}
			} else {
				if (!joinSignStartId.equals(work.getEmployeeId())) {
					// 自动向其领导汇报, 因为指派的都是其下级，所以不会跳出这个最终startEmployeeId的
					PersonDetail personDetail = personService
							.loadWidePersonDetail(work.getDeptId(),
									work.getEmployeeId(), work.getPostCode());
					PersonDetail mgrPersonDetail = personService
							.getMgrPersonDetail(personDetail);

					result.put(mgrPersonDetail.getEmployeeId()
							+ mgrPersonDetail.getPostCode(), personDetail);
					result.putAll(this.flowService.startNextWork(flow,
							mgrPersonDetail, work));
				} else {
					// 获取同一会办下第一条会办记录。即时间最早的一条
					MyWork tmpWork = this.flowService.getFirstHBWork(work);
					// 更新会办分支所有记录的ORGPATH
					this.flowService.updateHbWorkOrgPath(work, tmpWork);
					result.putAll(this.delegateDoComplete(flow, work));
				}
			}
		}
		return result;
	}

	/**
	 * add by Cao_Shengyong 2014-03-26 将原来doComplete中的处理提取出来，以免代码量过大
	 * 而且涉及到反复处理，所以新增一个方法来处理，只需调用这个方法并传入相应的参数就可以了。
	 * 
	 * @param flow
	 * @param work
	 * @return
	 */
	private Map<String, PersonDetail> delegateDoComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		PersonDetail actualPerson = personService.loadWidePersonDetail(flow
				.getActualPerson().getEmployeeId());
		// 结束该会办分支，然后判断一下是否结束整个会办工作（这里需要区分：顺序还是同时的）
		boolean isJointBranchCycleFinished = work.getEmployeeId().equals(
				super.getLatestJoinSignStartId(work.getJoinSignStartId(),
						"employeeId"))
				&& work.getStatus() == FlowStatus.AGREE;
		boolean isJointWholeCycleFinished = isJointBranchCycleFinished
				& JSONArray.fromObject(work.getJoinSignStartId()).size() == 1;

		/**
		 * 重新增加了会办是否结束的判断 当HB_CHENGHE的值为2，即会办单位的主管呈核的上一级主管审批时，上一级主管的审批状态
		 * 此时不是用原来的方式去判断了，应该对比HB相关的属性 其实此处仅仅是把判断用的字段名称换掉了。
		 */
		if (work.getHb_ChengHe().equals("2")) {
			isJointBranchCycleFinished = work.getEmployeeId().equals(
					super.getLatestJoinSignStartId(
							work.getHb_JoinSignStartId(), "employeeId"))
					&& work.getStatus() == FlowStatus.AGREE;
			isJointWholeCycleFinished = isJointBranchCycleFinished
					&& JSONArray.fromObject(work.getHb_JoinSignStartId())
							.size() == 1;
		}

		if (flow.getJointSignType() == JointSignType.SEQUENCE) {
			// TODO 后续开发补上
			// 如果是sequence的情况，查看下work的worknum是同cmpcode的jointDeptIds的最后一个号码吗？如果是，那么就结束会办阶段工作

			if (isJointWholeCycleFinished) {
				// 如果为true代表分支结束 ，如果为false代表逐级上报
				int cmpCodeJointSignDeptIdCount = 0;
				int totalCmpCodeJointSignDeptIdCount = 0;
				for (int i = 0; i < flow.getJointSignDeptIds().length; i++) {
					String nextJointSignDeptId = flow.getJointSignDeptIds()[i];
					PersonDetail person = personService
							.loadWideMgrPersonDetail(nextJointSignDeptId);
					if (person.getCmpCode().equals(actualPerson.getCmpCode())) {
						totalCmpCodeJointSignDeptIdCount++;
						// 判断该person在该流程里CMPCODEJOINTSIGN是否已经完成
						boolean hasFinish = flowService
								.checkPersonHasFinishWork(person,
										work.getFlowId(),
										WorkStage.CMPCODEJOINTSIGN);
						if (hasFinish) {
							cmpCodeJointSignDeptIdCount++;
						}
					}
				}

				if (cmpCodeJointSignDeptIdCount == totalCmpCodeJointSignDeptIdCount) {
					// 开启后续 审核工作
					// 如果会办分支全部结束了，那么跳转到下一个阶段
					flow.setStatus(FlowStatus.CHENGHE_START);
					// 更新流程
					this.flowService.updateFlowStatus(flow);
					result.putAll(this.flowService.startNextWork(flow,
							flow.getActualPerson(), null));
				} else {
					// 如果不是最后一个，目前是啥事都不需要处理了，只是单纯的等待其它分支结束
					long finishedWorkNum = work.getWorknum();
					long newWorkNum = finishedWorkNum - 1;
					PersonDetail person = null;
					String[] nextJointSignDeptIds = flow.getJointSignDeptIds();
					for (int i = 0; i < nextJointSignDeptIds.length; i++) {
						String nextJointSignDeptId = nextJointSignDeptIds[i];
						person = personService
								.loadWideMgrPersonDetail(nextJointSignDeptId);
						if (person.getCmpCode().equals(
								actualPerson.getCmpCode())) {
							// 判断该person在该流程里CMPCODEJOINTSIGN是否已经完成
							boolean hasFinish = flowService
									.checkPersonHasFinishWork(person,
											work.getFlowId(),
											WorkStage.CMPCODEJOINTSIGN);
							if (hasFinish) {
								continue;
							}
							// 该人未执行任务
							break;
						}
					}

					// TODO: 这边需要询问清楚情况
					// TODO: 通过传入的信息找到部门主管

					CmpcodeJoinSignWork newJointSignWork = (CmpcodeJoinSignWork) person
							.buildWork(flow.getFlowType(),
									WorkStage.CMPCODEJOINTSIGN, null);

					JSONObject jsonObj = new JSONObject();
					jsonObj.put("employeeId", person.getEmployeeId());
					jsonObj.put("deptId", person.getDeptId());
					JSONArray jsonArray = new JSONArray();
					jsonArray.add(jsonObj);

					newJointSignWork.setJoinSignStartId(jsonArray.toString());
					newJointSignWork.setWorknum(newWorkNum);
					newJointSignWork.setFlowId(flow.getId());
					newJointSignWork.setJoinCycle(jsonArray.size());
					newJointSignWork.setJoinStartEmployeeId(person
							.getEmployeeId());

					result.put(person.getEmployeeId() + person.getPostCode(),
							person);
					result.putAll(this.flowService.startNextWork(flow, person,
							newJointSignWork));
				}
			}

		} else if (flow.getJointSignType() == JointSignType.CONCURRENT) {
			// TODO: 这里就判断 该流程下 会办分支 的所有work employeeId 等于
			// startEmployeeId的那个work列表，状态都是
			// Agree就代表全部同意了，可以进入下个流程阶段了

			/*
			 * boolean isJointBranchCycleFinished = work.getEmployeeId().equals(
			 * super.getLatestJoinSignStartId(work.getJoinSignStartId(),
			 * "employeeId")) && work.getStatus() == FlowStatus.AGREE; boolean
			 * isJointWholeCycleFinished = isJointBranchCycleFinished &
			 * JSONArray.fromObject(work.getJoinSignStartId()).size() == 1;
			 */

			if (isJointWholeCycleFinished) {
				// 如果同CMPCODE下的人都完成了
				if (this.flowService.hasCmpcodeJointSignFinished(flow,
						actualPerson.getCmpCode())) {
					flow.setStatus(FlowStatus.CHENGHE_START);
					// 更新流程
					this.flowService.updateFlowStatus(flow);
					PersonDetail signPerson = flow.getActualPerson() != null ? flow
							.getActualPerson() : flow.getCreatePerson();
					result.putAll(this.flowService.startNextWork(flow,
							signPerson, null));
				}
			} else {
				// 说明会办分支没有完成 只是完成了内部一部分
				// ，所以这边继续逐级上报上去。记住jointSignStartId切除最后一个片段，塞给新的work对象
				PersonDetail personDetail = personService.loadWidePersonDetail(
						work.getDeptId(), work.getEmployeeId(),
						work.getPostCode());
				PersonDetail mgrPersonDetail = personService
						.getMgrPersonDetail(personDetail);

				JSONArray jsonArray = JSONArray.fromObject(work
						.getJoinSignStartId());
				jsonArray.remove(jsonArray.size() - 1);
				work.setJoinSignStartId(jsonArray.toString());
				work.setJoinCycle(jsonArray.size());

				result.put(
						mgrPersonDetail.getEmployeeId()
								+ mgrPersonDetail.getPostCode(), personDetail);
				result.putAll(this.flowService.startNextWork(flow,
						mgrPersonDetail, work));
			}
		}
		return result;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);

		MyWork work = personDetail.buildWork(flow.getFlowType(),
				WorkStage.CMPCODEJOINTSIGN, prevWork);
		result.put(personDetail.getEmployeeId() + personDetail.getPostCode(),
				personDetail);

		if (prevWork != null) {
			((CmpcodeJoinSignWork) work)
					.setWorknum(((CmpcodeJoinSignWork) prevWork).getWorknum());
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			work.setJoinStartEmployeeId(prevWork.getJoinStartEmployeeId());
			work.setJoinCycle(prevWork.getJoinCycle());
			if (((CmpcodeJoinSignWork) prevWork).getParentId() != 0) {
				((CmpcodeJoinSignWork) work)
						.setParentId(((CmpcodeJoinSignWork) prevWork)
								.getParentId());
			}

			// add by Cao_Shengyong 2014-03-25
			// 设置会办呈核上一级信息
			((CmpcodeJoinSignWork) work)
					.setHb_ChengHe(((CmpcodeJoinSignWork) prevWork)
							.getHb_ChengHe());
			((CmpcodeJoinSignWork) work)
					.setHb_JoinSignStartId(((CmpcodeJoinSignWork) prevWork)
							.getHb_JoinSignStartId());
			((CmpcodeJoinSignWork) work)
					.setHb_JoinStartEmployeeId(((CmpcodeJoinSignWork) prevWork)
							.getHb_JoinStartEmployeeId());
			((CmpcodeJoinSignWork) work)
					.setHb_ChengHeEnd(((CmpcodeJoinSignWork) prevWork)
							.getHb_ChengHeEnd());
		}
		work.setFlowId(flow.getId());
		flowDao.saveWork(work, flowService.getOrganDao());
		return result;
	}

	@Override
	Map<? extends String, ? extends PersonDetail> doStart(Flow flow) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);

		boolean isRecreate = flow.getId() > 0;
		boolean hasWorkRecord = false;
		if (isRecreate) {
			hasWorkRecord = this.flowDao.listWorks(flow).length > 0;
		}
		boolean isPreTemplate = flow.isTempalte();
		boolean isSuccess = true;

		flow.setStatus(FlowStatus.CMPCODEJOINTSIGN_START);
		// 没有内部会办，需要保存
		if (flow.getInnerJointSignIds() == null) {
			isSuccess = this.flowService.saveFlow(flow);
		}

		if (isSuccess) {
			// 添加发起人工作项(自动完成)
			PersonDetail actualPerson = flow.getActualPerson();
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

			// 从jointSignDeptIds列表中读取人员等标示 然后通过personService读取出人员信息，
			// 和当前发起人的CMPCODE进行比较，如果相同，则发给相关人员任务
			result.putAll(this.delegateJointSignWork(flow,
					actualPerson.getCmpCode()));
			// String[] jointSignDeptIds = flow.getJointSignDeptIds();
			// int count = 0;
			// for (int i = 0; i < jointSignDeptIds.length; i++) {
			// String jointSignDeptId = jointSignDeptIds[i];
			// // TODO: 这边需要询问清楚情况
			// // TODO: 通过传入的信息找到部门主管
			// PersonDetail person =
			// personService.loadWideMgrPersonDetail(jointSignDeptId);
			// if(person.getCmpCode().equals(actualPerson.getCmpCode())){
			// CmpcodeJoinSignWork work =
			// (CmpcodeJoinSignWork) person.buildWork(flow.getFlowType(),
			// WorkStage.CMPCODEJOINTSIGN, null);
			//
			// JSONObject jsonObj = new JSONObject();
			// jsonObj.put("employeeId", person.getEmployeeId());
			// jsonObj.put("deptId", person.getDeptId());
			// JSONArray jsonArray = new JSONArray();
			// jsonArray.add(jsonObj);
			//
			// work.setJoinSignStartId(jsonArray.toString());
			// work.setWorknum(work.getId() + count);
			// work.setFlowId(flow.getId());
			// work.setJoinCycle(jsonArray.size());
			// work.setJoinStartEmployeeId(person.getEmployeeId());
			// this.flowService.startNextWork(flow, person, work);
			//
			// result.put(person.getEmployeeId() + person.getPostCode(),
			// person);
			//
			// count++;
			// }
			//
			// }
		}
		return result;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return false;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CMPCODEJOINTSIGN_START;
	}

	@Override
	boolean startValidate(Flow flow) {
		//System.out.println("Start: " + this.getClass().getName());
		// 只有是签呈&&地方到总部，才启动该处理器
		return flow.getFlowType() == FlowType.QIANCHENG && !flow.isLocal();
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CMPCODEJOINTSIGN_START;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//System.out.println("CmpcodeJointSignFlowStageProcessor");
		return flow.getStatus() == FlowStatus.CMPCODEJOINTSIGN_START;
	}

	private Map<String, PersonDetail> delegateJointSignWork(Flow flow,
			String cmpCode) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 没有上一级领导了，那么就发起后续活动，可能是会签，或者其它的
		if (flow.getJointSignDeptIds() != null
				&& flow.getJointSignDeptIds().length > 0) {
			// TODO: 直接进入下一个阶段：会办阶段, 需要再明确下会办的情况
			// 首先获取到flow对于会办的顺序还是同步的选择，然后根据记录的会办部门等情况，发出会办的任务给各方
			String[] jointSignDeptIds = flow.getJointSignDeptIds();
			if (JointSignType.SEQUENCE == flow.getJointSignType()) {
				// TODO 顺序会办后续补上
				// 顺序会办
				// 开始会办，读取第一个同CMPCODE的会办元素，进行转换后，得到相应的人员信息，并交由他来办理工作
				PersonDetail person = null;
				for (int i = 0; i < jointSignDeptIds.length; i++) {
					String jointSignDeptId = jointSignDeptIds[i];
					person = personService
							.loadWideMgrPersonDetail(jointSignDeptId);
					if (person.getCmpCode().equals(cmpCode)) {
						break;
					}
				}

				CmpcodeJoinSignWork work = (CmpcodeJoinSignWork) person
						.buildWork(flow.getFlowType(),
								WorkStage.CMPCODEJOINTSIGN, null);

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("employeeId", person.getEmployeeId());
				jsonObj.put("deptId", person.getDeptId());
				JSONArray jsonArray = new JSONArray();
				jsonArray.add(jsonObj);

				work.setJoinSignStartId(jsonArray.toString());
				work.setWorknum(-1);
				work.setFlowId(flow.getId());
				work.setJoinCycle(jsonArray.size());
				work.setJoinStartEmployeeId(person.getEmployeeId());
				this.flowService.startNextWork(flow, person, work);

				result.put(person.getEmployeeId() + person.getPostCode(),
						person);
			} else if (JointSignType.CONCURRENT == flow.getJointSignType()) {
				// 同时会办
				int count = -1;
				for (int i = 0; i < jointSignDeptIds.length; i++) {
					String jointSignDeptId = jointSignDeptIds[i];
					// TODO: 这边需要询问清楚情况
					// TODO: 通过传入的信息找到部门主管
					PersonDetail person = personService
							.loadWideMgrPersonDetail(jointSignDeptId);
					if (person.getCmpCode().equals(cmpCode)) {
						CmpcodeJoinSignWork work = (CmpcodeJoinSignWork) person
								.buildWork(flow.getFlowType(),
										WorkStage.CMPCODEJOINTSIGN, null);

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

						count--;
					}
				}

			}
		}
		return result;
	}
}
