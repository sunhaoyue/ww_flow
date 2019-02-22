package com.wwgroup.flow.service.stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.helper.JointSignType;
import com.wwgroup.flow.bo.work.JointSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRPosition;
import com.wwgroup.organ.dao.OrganDao;
import com.wwgroup.user.bo.EmployeePos;

@SuppressWarnings("unused")
public class JointSignFlowStageProcessor extends AbstractFlowStageProcessor {

	@Override
	Map<String, PersonDetail> doStart(Flow flow) {
		return new HashMap<String, PersonDetail>(2);
	}

	@Override
	boolean startValidate(Flow flow) {
		//System.out.println("Start: " + this.getClass().getName());
		return false;
	}

	@Override
	boolean startNextValidate(Flow qianChenFlow) {
		//System.out.println("StartNext: " + this.getClass().getName());
		return qianChenFlow.getStatus() == FlowStatus.JOINTSIGN_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);

		MyWork work = personDetail.buildWork(flow.getFlowType(),
				WorkStage.JOINTSIGN, prevWork);
		result.put(personDetail.getEmployeeId() + personDetail.getPostCode(),
				personDetail);

		if (prevWork != null) {
			((JointSignWork) work).setWorknum(((JointSignWork) prevWork)
					.getWorknum());
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			work.setJoinStartEmployeeId(prevWork.getJoinStartEmployeeId());
			work.setJoinCycle(prevWork.getJoinCycle());
			if (((JointSignWork) prevWork).getParentId() != 0) {
				((JointSignWork) work).setParentId(((JointSignWork) prevWork)
						.getParentId());
			}

			// add by Cao_Shengyong 2014-03-25
			// 设置会办呈核上一级信息
			((JointSignWork) work).setHb_ChengHe(((JointSignWork) prevWork)
					.getHb_ChengHe());
			((JointSignWork) work)
					.setHb_JoinSignStartId(((JointSignWork) prevWork)
							.getHb_JoinSignStartId());
			((JointSignWork) work)
					.setHb_JoinStartEmployeeId(((JointSignWork) prevWork)
							.getHb_JoinStartEmployeeId());
			((JointSignWork) work).setHb_ChengHeEnd(((JointSignWork) prevWork)
					.getHb_ChengHeEnd());
		}
		work.setFlowId(flow.getId());
		flowDao.saveWork(work, flowService.getOrganDao());
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//System.out.println("JoinSignFlowStage");
		return flow.getStatus() == FlowStatus.JOINTSIGN_START;
	}

	// 要体现 指派 以及 逐级上报的特性
	// 大体逻辑是这样的：完成工作，然后看work中的employeeId是否与work中startEmployId一致，如果一致，那么就结束分支
	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		// System.out.println("1111111111111111222");
		//System.out.println(work.getHb_ChengHe() + "aaaaaaaaaaaaaaaaaaaaa");
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 完成myWork然后判断 本身是不是joinSignStartId，如果是的话，那么就结束分支会办的工作了
		// System.out.println(20);
		if (JointSignWork.class.isInstance(work)) {
			// System.out.println(30);
			// JointSignWork joinSignWork = (JointSignWork) work;
			work.setStatus(FlowStatus.AGREE);
			this.flowService.updateWorkStatus(work);

			String joinSignStartId = super.getLatestJoinSignStartId(
					work.getJoinSignStartId(), "employeeId");
			/**
			 * 2014-03-20 暂时先把总部会办时需要中心主管签核注释掉 待确认需要此功能后再开放
			 * 但是该功能还没有时行完整测试，而且还没有解决签核历呈的显示问题。 以上是需要注意的。
			 */
			// 通过前端参数，可以确认是否是点击的“呈核上一级”按钮
			// 以此来判断是继续原来的逻辑还是汇报表上一级主管
			if (work.getHb_ChengHe().equals("1")
					|| work.getHb_ChengHe().equals("2")) {
				EmployeePos employeePos = personService.loadWideEmployeePos(
						work.getEmployeeId(), work.getPostCode());
				if (employeePos != null) {
					// 当前审批人本身就是单位主管，则直接结束会办分支
					/**
					 * 如果当前会办签核时不是主管，则不可以执行此处理逻辑 即页面上无法看到“呈核上一级”按钮
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
						result.putAll(this.delegateJointSignWork(flow, work));
					} else {
						// 如果审批人本身是中心主管，则需要将审核记录的呈核值设为单位主管
						// 表示需要呈核至单位主管才能结束
						if (work.getEmployeeId().equalsIgnoreCase(
								work.getHb_JoinStartEmployeeId()) && work.getHb_ChengHe().equals("2")) {
							// 在会办结束时，更新历史同一会办下的HB相关信息
							this.flowService.updateHbWorkJoin(work);
							// 获取同一会办下第一条会办记录。即时间最早的一条
							MyWork tmpWork = this.flowService.getFirstHBWork(work);
							// 更新会办分支所有记录的ORGPATH
							this.flowService.updateHbWorkOrgPath(work, tmpWork);
							result.putAll(this
									.delegateJointSignWork(flow, work));
						} else {
							if (employeePos.getMgA_DeptFlg() == 1) {
								work.setHb_ChengHeEnd(GroupType.CMPGROUP.name());
							} else {
								// 如果审批人本身是部门主管，则需要将审核记录的呈核值设为中心主管
								// 表示需要呈核至中心主管才结束
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
								// 因为指派的都是其下级，所以不会跳出这个最终startEmployeeId的
								PersonDetail personDetail = personService
										.loadWidePersonDetail(work.getDeptId(),
												work.getEmployeeId(),
												work.getPostCode());
								PersonDetail mgrPersonDetail = personService
										.getMgrPersonDetail(personDetail);
								result.put(mgrPersonDetail.getEmployeeId()
										+ mgrPersonDetail.getPostCode(),
										personDetail);
								result.putAll(this.flowService.startNextWork(
										flow, mgrPersonDetail, work));
							} else {
								System.out.println("无法查询到工号["
										+ work.getEmployeeId() + "]的岗位["
										+ work.getPostCode() + "]的上级["
										+ work.getHb_ChengHeEnd() + "]的主管信息!");
							}
						}
					}
				} else {
					// 如果通过工号和岗位信息无法查询到相应信息，则直接抛出错误
					System.out.println("无法查询到工号[" + work.getEmployeeId()
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
					result.putAll(this.delegateJointSignWork(flow, work));
				}
			}
			// }
		}
		return result;
	}

	// 增加参数看是否需要进行总部中心主管的判断
	private Map<String, PersonDetail> delegateJointSignWork(Flow flow,
			MyWork prevWork) {
		if (flow.getFlowType() == FlowType.QIANCHENG && !flow.isLocal()) {
			return this.delegateQianChengRemoteSignWork(flow, prevWork);
		} else {
			return this.delegateNotQianChengRemoteSignWork(flow, prevWork);
		}
	}

	// 原来的流程
	private Map<String, PersonDetail> delegateNotQianChengRemoteSignWork(
			Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 结束该会办分支，然后判断一下是否结束整个会办工作（这里需要区分：顺序还是同时的）
		boolean isJointBranchCycleFinished = work.getEmployeeId().equals(
				super.getLatestJoinSignStartId(work.getJoinSignStartId(),
						"employeeId"))
				&& work.getStatus() == FlowStatus.AGREE;
		boolean isJointWholeCycleFinished = isJointBranchCycleFinished
				& JSONArray.fromObject(work.getJoinSignStartId()).size() == 1;

		/**
		 * 重新增加了会办是否结束的判断
		 * 当HB_CHENGHE的值为2，即呈核上一级的时候，上级主管审批时的状态
		 * 此时不是用原来的方式去判断了，应该比对HB相关的属性
		 */

		if (work.getHb_ChengHe().equals("2")) {
			isJointBranchCycleFinished = work.getEmployeeId().equals(
					super.getLatestJoinSignStartId(
							work.getHb_JoinSignStartId(), "employeeId"))
					&& work.getStatus() == FlowStatus.AGREE;

			isJointWholeCycleFinished = isJointBranchCycleFinished
					& JSONArray.fromObject(work.getHb_JoinSignStartId()).size() == 1;

		}
		//System.out.println(isJointWholeCycleFinished);
		if (flow.getJointSignType() == JointSignType.SEQUENCE) {
			// 如果是sequence的情况，查看下work的worknum是jointDeptIds的最后一个号码吗？如果是，那么就结束会办阶段工作

			if (isJointWholeCycleFinished) {
				// 如果为true代表分支结束 ，如果为false代表逐级上报，而且需要将原来joinSignStart去除掉最后一个
				if (flow.getJointSignDeptIds().length - 1 == work.getWorknum()) {
					// 最后一个了，结束会办工作
					// TODO: 是否自动本人核对，还需与对方确认
					// 开启后续 核对工作
					// 如果会办分支全部结束了，那么跳转到下一个阶段
					if (flow.isSelfConfirm()) {
						flow.setStatus(FlowStatus.CONFIRM_START);
					} else {
						flow.setStatus(FlowStatus.FINAL_DECISION_START);
					}
					// 更新流程
					this.flowService.updateFlowStatus(flow);
					result.putAll(this.flowService.startNextWork(flow,
							flow.getActualPerson(), null));
				} else {
					// 如果不是最后一个，目前是啥事都不需要处理了，只是单纯的等待其它分支结束
					long finishedWorkNum = work.getWorknum();
					long newWorkNum = finishedWorkNum + 1;
					String newJointSignDeptId = flow.getJointSignDeptIds()[(int) newWorkNum];
					String jointSignDeptId = newJointSignDeptId;
					// TODO: 这边需要询问清楚情况
					// TODO: 通过传入的信息找到部门主管
					PersonDetail person = personService
							.loadWideMgrPersonDetail(jointSignDeptId);
					JointSignWork newJointSignWork = (JointSignWork) person
							.buildWork(flow.getFlowType(), WorkStage.JOINTSIGN,
									null);

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
				// TODO：需要改进下jointSignFInished这个API需要在程序级别判定了
				if (this.flowService.hasJointSignFinished(flow)) {
					// saveFlowStatus
					if (flow.isSelfConfirm()) {
						flow.setStatus(FlowStatus.CONFIRM_START);
					} else {
						if (flow.getSecondEmployeeId() != null){
							flow.setStatus(FlowStatus.SECONDFINAL_DECISION_START);
						} else {
							flow.setStatus(FlowStatus.FINAL_DECISION_START);
						}
					}
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

	/**
	 * 签呈 -> 地方到总部
	 * 
	 * @param flow
	 * @param prevWork
	 * @return
	 */
	private Map<String, PersonDetail> delegateQianChengRemoteSignWork(
			Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		PersonDetail actualPerson = this.personService
				.loadWidePersonDetail(flow.getActualPerson().getEmployeeId());

		boolean isJointBranchCycleFinished = work.getEmployeeId().equals(
				super.getLatestJoinSignStartId(work.getJoinSignStartId(),
						"employeeId"))
				&& work.getStatus() == FlowStatus.AGREE;
		boolean isJointWholeCycleFinished = isJointBranchCycleFinished
				& JSONArray.fromObject(work.getJoinSignStartId()).size() == 1;
		
		/**
		 * 重新增加了会办是否结束的判断
		 * 当HB_CHENGHE的值为2，即呈核上一级的时候，上级主管审批时的状态
		 * 此时不是用原来的方式去判断了，应该比对HB相关的属性
		 */

		if (work.getHb_ChengHe().equals("2")) {
			isJointBranchCycleFinished = work.getEmployeeId().equals(
					super.getLatestJoinSignStartId(
							work.getHb_JoinSignStartId(), "employeeId"))
					&& work.getStatus() == FlowStatus.AGREE;

			isJointWholeCycleFinished = isJointBranchCycleFinished
					& JSONArray.fromObject(work.getHb_JoinSignStartId()).size() == 1;

		}

		// 结束该会办分支，然后判断一下是否结束整个会办工作（这里需要区分：顺序还是同时的）
		if (flow.getJointSignType() == JointSignType.SEQUENCE) {
			// 如果是sequence的情况，查看下work的worknum是jointDeptIds的最后一个号码吗？如果是，那么就结束会办阶段工作

			if (isJointWholeCycleFinished) {
				// 如果为true代表分支结束 ，如果为false代表逐级上报，
				int cmpCodeJointSignDeptIdCount = 0;
				int totalCmpCodeJointSignDeptIdCount = 0;
				for (int i = 0; i < flow.getJointSignDeptIds().length; i++) {
					String nextJointSignDeptId = flow.getJointSignDeptIds()[i];
					PersonDetail person = personService
							.loadWideMgrPersonDetail(nextJointSignDeptId);
					if (!person.getCmpCode().equals(actualPerson.getCmpCode())) {
						// 判断该person在该流程里CMPCODEJOINTSIGN是否已经完成
						totalCmpCodeJointSignDeptIdCount++;
						boolean hasFinish = flowService
								.checkPersonHasFinishWork(person,
										work.getFlowId(), WorkStage.JOINTSIGN);
						if (hasFinish) {
							cmpCodeJointSignDeptIdCount++;
						}
					}
				}
				if (cmpCodeJointSignDeptIdCount == totalCmpCodeJointSignDeptIdCount) {
					// 最后一个了，结束会办工作
					// TODO: 是否自动本人核对，还需与对方确认
					// 开启后续 核对工作
					// 如果会办分支全部结束了，那么跳转到下一个阶段
					if (flow.isSelfConfirm()) {
						flow.setStatus(FlowStatus.CONFIRM_START);
					} else {
						flow.setStatus(FlowStatus.FINAL_DECISION_START);
					}
					// 更新流程
					this.flowService.updateFlowStatus(flow);
					result.putAll(this.flowService.startNextWork(flow,
							flow.getActualPerson(), null));
				} else {
					// 如果不是最后一个，目前是啥事都不需要处理了，只是单纯的等待其它分支结束
					long finishedWorkNum = work.getWorknum();
					long newWorkNum = finishedWorkNum + 1;

					PersonDetail person = null;
					String[] nextJointSignDeptIds = flow.getJointSignDeptIds();
					for (int i = 0; i < nextJointSignDeptIds.length; i++) {
						String nextJointSignDeptId = nextJointSignDeptIds[i];
						person = personService
								.loadWideMgrPersonDetail(nextJointSignDeptId);
						if (!person.getCmpCode().equals(
								actualPerson.getCmpCode())) {
							// 判断该person在该流程里CMPCODEJOINTSIGN是否已经完成
							boolean hasFinish = flowService
									.checkPersonHasFinishWork(person,
											work.getFlowId(),
											WorkStage.JOINTSIGN);
							if (hasFinish) {
								continue;
							}
							// 该人未执行任务
							break;
						}
					}

					// TODO: 这边需要询问清楚情况
					// TODO: 通过传入的信息找到部门主管
					JointSignWork newJointSignWork = (JointSignWork) person
							.buildWork(flow.getFlowType(), WorkStage.JOINTSIGN,
									null);

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
				// TODO：需要改进下jointSignFInished这个API需要在程序级别判定了
				if (this.flowService.hasJointSignFinished(flow)) {
					// saveFlowStatus
					if (flow.isSelfConfirm()) {
						flow.setStatus(FlowStatus.CONFIRM_START);
					} else {
						if (flow.getSecondEmployeeId() != null){
							flow.setStatus(FlowStatus.SECONDFINAL_DECISION_START);
						} else {
							flow.setStatus(FlowStatus.FINAL_DECISION_START);
						}
					}
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
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.JOINTSIGN_START;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return false;
	}

}
