package com.wwgroup.flow.service.qstage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.JointSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.user.bo.EmployeePos;

public class QCJointSignFlowStageProcessor extends QCAbstractFlowStageProcessor {

	@Override
	Map<String, PersonDetail> doStart(Flow flow) {
		return new HashMap<String, PersonDetail>(2);
	}

	@Override
	boolean startValidate(Flow flow) {
		return false;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		/*logger.info(flow.getFormNum() + " 状态：" + flow.getStatus()
				+ " startNextValidate: "
				+ QCJointSignFlowStageProcessor.class.getName());*/
		return flow.getStatus() == FlowStatus.JOINTSIGN_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 会办 doNext 代码片断：");
		//logger.info(flow.getFormNum() + " 根据传入的 personDetail 创建 work 对象,此时还没有保存至数据库...");
		MyWork work = personDetail.buildWork(flow.getFlowType(),
				WorkStage.JOINTSIGN, prevWork);
		result.put(personDetail.getEmployeeId() + personDetail.getPostCode(),
				personDetail);

		if (prevWork != null) {
			//logger.info(flow.getFormNum() + " 如果传入的 pervWork 不为空，说明是会办分支内的操作，包含会办的创建、指派、呈核上一级、指派后的回流...");
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

		// 获取当前处理人信息
		//logger.info(flow.getFormNum() + " 根据 work 中的部门ID、工号、岗位信息获取人员信息 Start...");
		PersonDetail tmpPerson = personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		//logger.info(flow.getFormNum() + " 根据 work 中的部门ID、工号、岗位信息获取人员信息 End...");
		work.setEmployeenam(tmpPerson.getName());
		work.setTitlenam(tmpPerson.getTitname());
		/*logger.info(flow.getFormNum() + " 对创建的work对象进行保存操作，这里是insert. " + work.getEmployeeId() + "("
				+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
		flowDao.saveWork(work, flowService.getOrganDao());
		//logger.info(flow.getFormNum() + " 对创建的work对象进行保存操作，这里是insert. End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		/*logger.info(flow.getFormNum() + " 状态：" + flow.getStatus()
				+ " completeValidate: "
				+ QCJointSignFlowStageProcessor.class.getName());*/
		return flow.getStatus() == FlowStatus.JOINTSIGN_START;
	}

	// 要体现 指派 以及 逐级上报的特性
	// 大体逻辑是这样的：完成工作，然后看work中的employeeId是否与work中startEmployId一致，如果一致，那么就结束分支
	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 完成myWork然后判断 本身是不是joinSignStartId，如果是的话，那么就结束分支会办的工作了
		if (JointSignWork.class.isInstance(work)) {
			//logger.info(flow.getFormNum() + " 表单会办 doComplete 代码执行 Start...");
			work.setStatus(FlowStatus.AGREE);
			//logger.info(flow.getFormNum() + " 更新 work 状态为" + FlowStatus.AGREE + " Start...");
			this.flowService.updateWorkStatus(work);
			//logger.info(flow.getFormNum() + " 更新 work 状态为" + FlowStatus.AGREE + " End...");

			String joinSignStartId = super.getLatestJoinSignStartId(
					work.getJoinSignStartId(), "employeeId");
			// 通过前端参数，可以确认是否是点击的“呈核上一级”按钮
			// 以此来判断是继续原来的逻辑还是汇报表上一级主管
			if (work.getHb_ChengHe().equals("1")
					|| work.getHb_ChengHe().equals("2")) {
				//logger.info(flow.getFormNum() + " 根据 work 中的工号和岗位信息获取人员完整信息 Start...");
				EmployeePos employeePos = personService.loadWideEmployeePos(
						work.getEmployeeId(), work.getPostCode());
				//logger.info(flow.getFormNum() + " 根据 work 中的工号和岗位信息获取人员完整信息 End...");
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
						//logger.info(flow.getFormNum() + " A:更新 work 中 HB_开头的相关字段信息 Start...");
						this.flowService.updateHbWorkJoin(work);
						//logger.info(flow.getFormNum() + " A:更新 work 中 HB_开头的相关字段信息 End...");
						// 获取同一会办下第一条会办记录。即时间最早的一条
						//logger.info(flow.getFormNum() + " A:获取同一会办下的第一条会办记录，即开始　Start...");
						MyWork tmpWork = this.flowService.getFirstHBWork(work);
						//logger.info(flow.getFormNum() + " A:获取同一会办分支下的第一条会办记录，即开始　End...");
						// 更新会办分支所有记录的ORGPATH
						//logger.info(flow.getFormNum() + " A:更新同一会办分支签核记录的ORGPATH Start...");
						this.flowService.updateHbWorkOrgPath(work, tmpWork);
						//logger.info(flow.getFormNum() + " A:更新同一会办分支签核记录的ORGPATH End...");
						result.putAll(this.delegateJointSignWork(flow, work));
					} else {
						// 如果审批人本身是中心主管，则需要将审核记录的呈核值设为单位主管
						// 表示需要呈核至单位主管才能结束
						if (work.getEmployeeId().equalsIgnoreCase(
								work.getHb_JoinStartEmployeeId())
								&& work.getHb_ChengHe().equals("2")) {
							// 在会办结束时，更新历史同一会办下的HB相关信息
							//logger.info(flow.getFormNum() + " B:更新 work 中 HB_开头的相关字段信息 Start...");
							this.flowService.updateHbWorkJoin(work);
							//logger.info(flow.getFormNum() + " B:更新 work 中 HB_开头的相关字段信息 End...");
							// 获取同一会办下第一条会办记录。即时间最早的一条
							//logger.info(flow.getFormNum() + " B:获取同一会办下的第一条会办记录，即开始　Start...");
							MyWork tmpWork = this.flowService
									.getFirstHBWork(work);
							//logger.info(flow.getFormNum() + " B:获取同一会办分支下的第一条会办记录，即开始　End...");
							// 更新会办分支所有记录的ORGPATH
							//logger.info(flow.getFormNum() + " B:更新同一会办分支签核记录的ORGPATH Start...");
							this.flowService.updateHbWorkOrgPath(work, tmpWork);
							//logger.info(flow.getFormNum() + " B:更新同一会办分支签核记录的ORGPATH End...");
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

							//logger.info(flow.getFormNum() + " 查找呈核上一级对应的主管　Start...");
							PersonDetail mgrPersonDetail_Tmp = personService
									.getMgrPersonByIdAndPostCode(
											work.getEmployeeId(),
											work.getPostCode(),
											work.getHb_ChengHeEnd());
							//logger.info(flow.getFormNum() + " 查找呈核上一级对应的主管　End...");
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
								//logger.info(flow.getFormNum() + " 根据 work 中的部门ID、工号、岗位代码获取人员信息 Start...");
								PersonDetail personDetail = personService
										.loadWidePersonDetail(work.getDeptId(),
												work.getEmployeeId(),
												work.getPostCode());
								//logger.info(flow.getFormNum() + " 根据 work 中的部门ID、工号、岗位代码获取人员信息 End...");
								//logger.info(flow.getFormNum() + " 根据人员信息获取其上级主管 Start...");
								PersonDetail mgrPersonDetail = personService
										.getMgrPersonDetail(personDetail);
								//logger.info(flow.getFormNum() + " 根据人员信息获取其上级主管 End...");
								result.put(mgrPersonDetail.getEmployeeId()
										+ mgrPersonDetail.getPostCode(),
										personDetail);
								result.putAll(this.flowService.startNextWork(
										flow, mgrPersonDetail, work));
							} else {
								System.out.println(flow.getFormNum() + " 无法查询到工号["
										+ work.getEmployeeId() + "]的岗位["
										+ work.getPostCode() + "]的上级["
										+ work.getHb_ChengHeEnd() + "]的主管信息!");
							}
						}
					}
				} else {
					// 如果通过工号和岗位信息无法查询到相应信息，则直接抛出错误
					System.out.println(flow.getFormNum() + " 无法查询到工号[" + work.getEmployeeId()
							+ "]的岗位[" + work.getPostCode()
							+ "]相关信息，可能是汇报关系维护错误，请与系统管理员联系！");
				}
			} else {
				if (!joinSignStartId.equals(work.getEmployeeId())) {
					// 自动向其领导汇报, 因为指派的都是其下级，所以不会跳出这个最终startEmployeeId的
					//logger.info(flow.getFormNum() + " 根据 work 中的部门ID、工号、岗位代码获取人员信息 Start...");
					PersonDetail personDetail = personService
							.loadWidePersonDetail(work.getDeptId(),
									work.getEmployeeId(), work.getPostCode());
					//logger.info(flow.getFormNum() + " 根据 work 中的部门ID、工号、岗位代码获取人员信息 End...");
					//logger.info(flow.getFormNum() + " 根据人员信息获取其上级主管 Start...");
					PersonDetail mgrPersonDetail = personService
							.getMgrPersonDetail(personDetail);
					//logger.info(flow.getFormNum() + " 根据人员信息获取其上级主管 End...");

					result.put(mgrPersonDetail.getEmployeeId()
							+ mgrPersonDetail.getPostCode(), personDetail);
					result.putAll(this.flowService.startNextWork(flow,
							mgrPersonDetail, work));
				} else {
					// 获取同一会办下第一条会办记录。即时间最早的一条
					//logger.info(flow.getFormNum() + " C:获取同一会办下的第一条会办记录，即开始　Start...");
					MyWork tmpWork = this.flowService.getFirstHBWork(work);
					//logger.info(flow.getFormNum() + " C:获取同一会办下的第一条会办记录，即开始　End...");
					// 更新会办分支所有记录的ORGPATH
					//logger.info(flow.getFormNum() + " C:更新同一会办分支签核记录的ORGPATH Start...");
					this.flowService.updateHbWorkOrgPath(work, tmpWork);
					//logger.info(flow.getFormNum() + " C:更新同一会办分支签核记录的ORGPATH End...");
					result.putAll(this.delegateJointSignWork(flow, work));
				}
			}
			// }
			//logger.info(flow.getFormNum() + " 表单会办 doComplete 代码执行End...");
		}
		return result;
	}

	// 增加参数看是否需要进行总部中心主管的判断
	private Map<String, PersonDetail> delegateJointSignWork(Flow flow,
			MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 结束该会办分支，然后判断一下是否结束整个会办工作（这里需要区分：顺序还是同时的）
		boolean isJointBranchCycleFinished = work.getEmployeeId().equals(
				super.getLatestJoinSignStartId(work.getJoinSignStartId(),
						"employeeId"))
				&& work.getStatus() == FlowStatus.AGREE;
		boolean isJointWholeCycleFinished = isJointBranchCycleFinished
				& JSONArray.fromObject(work.getJoinSignStartId()).size() == 1;

		/**
		 * 重新增加了会办是否结束的判断 当HB_CHENGHE的值为2，即呈核上一级的时候，上级主管审批时的状态
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

		if (isJointWholeCycleFinished) {
			// TODO：需要改进下jointSignFInished这个API需要在程序级别判定了
			//logger.info(flow.getFormNum() + " 判断会办是否完成  Start...");
			if (this.flowService.hasJointSignFinished(flow)) {
				//logger.info(flow.getFormNum() + " 会办完成后，设置FlowStatus为下一步处理的状态...");
				flow.setStatus(FlowStatus.NEXTFINAL_DECISION_START);
				// 更新流程
				/*logger.info(flow.getFormNum() + " 会办完成后，更新表单 " + flow.getFormNum()
						+ " 流程的状态 Start...");*/
				this.flowService.updateFlowStatus(flow);
				//logger.info(flow.getFormNum() + " 会办完成后，更新表单 " + flow.getFormNum() + " 流程的状态 End...");
				PersonDetail signPerson = flow.getActualPerson() != null ? flow
						.getActualPerson() : flow.getCreatePerson();
				//logger.info(flow.getFormNum() + " 流转至一下步处理...");
				result.putAll(this.flowService.startNextWork(flow, signPerson,
						null));
			}
			//logger.info(flow.getFormNum() + " 判断会办会支是否完成  End...");
		} else {
			// 说明会办分支没有完成 只是完成了内部一部分
			// ，所以这边继续逐级上报上去。记住jointSignStartId切除最后一个片段，塞给新的work对象
			//logger.info(flow.getFormNum() + " 这里是会办没有完成的情况：");
			//logger.info(flow.getFormNum() + " 根据 work 中的部门ID、工号、岗位代码获取人员信息 Start...");
			PersonDetail personDetail = personService.loadWidePersonDetail(
					work.getDeptId(), work.getEmployeeId(), work.getPostCode());
			//logger.info(flow.getFormNum() + " 根据 work 中的部门ID、工号、岗位代码获取人员信息 End...");
			//logger.info(flow.getFormNum() + " 根据人员信息获取其上级主管 Start...");
			PersonDetail mgrPersonDetail = personService
					.getMgrPersonDetail(personDetail);
			//logger.info(flow.getFormNum() + " 根据人员信息获取其上级主管 End...");

			JSONArray jsonArray = JSONArray.fromObject(work
					.getJoinSignStartId());
			jsonArray.remove(jsonArray.size() - 1);
			work.setJoinSignStartId(jsonArray.toString());
			work.setJoinCycle(jsonArray.size());

			result.put(
					mgrPersonDetail.getEmployeeId()
							+ mgrPersonDetail.getPostCode(), personDetail);
			result.putAll(this.flowService.startNextWork(flow, mgrPersonDetail,
					work));
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
