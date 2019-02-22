package com.wwgroup.flow.service.qstage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.CenterJoinSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.user.bo.EmployeePos;

public class QCCenterJointSignFlowStageProcessor extends
		QCAbstractFlowStageProcessor {

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return false;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CENTERJOINTSIGN_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 完成myWork然后判断 本身是不是joinSignStartId，如果是的话，那么就结束分支会办的工作了
		if (CenterJoinSignWork.class.isInstance(work)) {
			//logger.info(flow.getFormNum() + " 表单会办 doComplete 代码执行 Start...");
			//logger.info(flow.getFormNum() + " 更新 work 状态为" + FlowStatus.AGREE + " Start...");
			work.setStatus(FlowStatus.AGREE);
			this.flowService.updateWorkStatus(work);
			//logger.info(flow.getFormNum() + " 更新 work 状态为" + FlowStatus.AGREE + " End...");

			String joinSignStartId = super.getLatestJoinSignStartId(
					work.getJoinSignStartId(), "employeeId");

			// 通过前端参数，判断点击的是否“呈核上一级”(1：呈核上一级；2：呈核后的主管签核)
			if (work.getHb_ChengHe().equals("1")
					|| work.getHb_ChengHe().equals("2")) {
				//logger.info(flow.getFormNum() + " 根据 work 中的工号和岗位信息获取EmployeePos信息 Start...");
				EmployeePos employeePos = personService.loadWideEmployeePos(
						work.getEmployeeId(), work.getPostCode());
				//logger.info(flow.getFormNum() + " 根据 work 中的工号和岗位信息获取EmployeePos信息 End...");
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
						//logger.info(flow.getFormNum() + " A:更新 work 中 HB_开头的相关字段信息 Start...");
						this.flowService.updateHbWorkJoin(work);
						//logger.info(flow.getFormNum() + " A:更新 work 中 HB_开头的相关字段信息 End...");
						// 获取同一会办下第一条会办记录。即时间最早的一条
						//logger.info(flow.getFormNum() + " A:获取同一会办下的第一条会办记录，即开始　Start...");
						MyWork tmpWork = this.flowService.getFirstHBWork(work);
						//logger.info(flow.getFormNum() + " A:获取同一会办下的第一条会办记录，即开始　End...");
						// 更新会办分支所有记录的ORGPATH
						//logger.info(flow.getFormNum() + " A:更新同一会办分支签核记录的ORGPATH Start...");
						this.flowService.updateHbWorkOrgPath(work, tmpWork);
						//logger.info(flow.getFormNum() + " A:更新同一会办分支签核记录的ORGPATH End...");
						result.putAll(this.delegateDoComplete(flow, work));
					} else {
						/**
						 * 如果审批人本身是中心主管，则需要将审核记录的呈核值设为单位主管 表示需要呈核至单位主管才能结束
						 */
						if (work.getEmployeeId().equalsIgnoreCase(
								work.getHb_JoinStartEmployeeId())) {
							// 在会办结束时，更新历史同一会办下的HB相关信息
							//logger.info(flow.getFormNum() + " B:更新 work 中 HB_开头的相关字段信息 Start...");
							this.flowService.updateHbWorkJoin(work);
							//logger.info(flow.getFormNum() + " B:更新 work 中 HB_开头的相关字段信息 End...");
							// 获取同一会办下第一条会办记录。即时间最早的一条
							//logger.info(flow.getFormNum() + " B:获取同一会办下的第一条会办记录，即开始　Start...");
							MyWork tmpWork = this.flowService
									.getFirstHBWork(work);
							//logger.info(flow.getFormNum() + " B:获取同一会办下的第一条会办记录，即开始　End...");
							// 更新会办分支所有记录的ORGPATH
							//logger.info(flow.getFormNum() + " B:更新同一会办分支签核记录的ORGPATH Start...");
							this.flowService.updateHbWorkOrgPath(work, tmpWork);
							//logger.info(flow.getFormNum() + " B:更新同一会办分支签核记录的ORGPATH End...");
							result.putAll(this.delegateDoComplete(flow, work));
						} else {
							if (employeePos.getMgA_DeptFlg() == 1) {
								work.setHb_ChengHeEnd(GroupType.CMPGROUP.name());
							} else {
								// 如果审批人本身是部门主管，则需呈核至中心主管才可以
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
								//logger.info(flow.getFormNum() + " 根据 work 中的部门ID、工号、岗位代码获取PersonDetail信息 Start...");
								PersonDetail personDetail = personService
										.loadWidePersonDetail(work.getDeptId(),
												work.getEmployeeId(),
												work.getPostCode());
								//logger.info(flow.getFormNum() + " 根据 work 中的部门ID、工号、岗位代码获取PersonDetail信息 End...");
								//logger.info(flow.getFormNum() + " 根据人员信息获取其上级主管PersonDetail Start...");
								PersonDetail mgrPersonDetail = personService
										.getMgrPersonDetail(personDetail);
								//logger.info(flow.getFormNum() + " 根据人员信息获取其上级主管PersonDetail End...");
								result.put(mgrPersonDetail.getEmployeeId()
										+ mgrPersonDetail.getPostCode(),
										personDetail);
								result.putAll(this.flowService.startNextWork(
										flow, mgrPersonDetail, work));
							} else {
								new RuntimeException(flow.getFormNum() + " 无法查询到工号["
										+ work.getEmployeeId() + "]的岗位["
										+ work.getPostCode() + "]的上级["
										+ work.getHb_ChengHeEnd() + "]的主管信息!");
							}
						}
					}
				} else {
					// 如果通过工号和岗位信息无法查询到相应信息，则直接抛出错误
					new RuntimeException(flow.getFormNum() + " 无法查询到工号[" + work.getEmployeeId()
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
					result.putAll(this.delegateDoComplete(flow, work));
				}
			}
		}
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + QCCenterJointSignFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CENTERJOINTSIGN_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 会办 doNext 代码片断 Start...");
		MyWork work = personDetail.buildWork(flow.getFlowType(),
				WorkStage.CENTERJOINTSIGN, prevWork);
		result.put(personDetail.getEmployeeId() + personDetail.getPostCode(),
				personDetail);

		if (prevWork != null) {
			((CenterJoinSignWork) work)
					.setWorknum(((CenterJoinSignWork) prevWork).getWorknum());
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			work.setJoinStartEmployeeId(prevWork.getJoinStartEmployeeId());
			work.setJoinCycle(prevWork.getJoinCycle());
			if (((CenterJoinSignWork) prevWork).getParentId() != 0) {
				((CenterJoinSignWork) work)
						.setParentId(((CenterJoinSignWork) prevWork)
								.getParentId());
			}

			// add by Cao_Shengyong 2014-03-25
			// 设置会办呈核上一级信息
			((CenterJoinSignWork) work)
					.setHb_ChengHe(((CenterJoinSignWork) prevWork)
							.getHb_ChengHe());
			((CenterJoinSignWork) work)
					.setHb_JoinSignStartId(((CenterJoinSignWork) prevWork)
							.getHb_JoinSignStartId());
			((CenterJoinSignWork) work)
					.setHb_JoinStartEmployeeId(((CenterJoinSignWork) prevWork)
							.getHb_JoinStartEmployeeId());
			((CenterJoinSignWork) work)
					.setHb_ChengHeEnd(((CenterJoinSignWork) prevWork)
							.getHb_ChengHeEnd());
		}
		work.setFlowId(flow.getId());
		
		// 获取当前处理人信息
		PersonDetail tmpPerson = personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(),
				work.getPostCode());
		work.setEmployeenam(tmpPerson.getName());
		work.setTitlenam(tmpPerson.getTitname());
		/*logger.info(flow.getFormNum() + " 对创建的work对象进行保存操作，这里是insert. " + work.getEmployeeId() + "("
				+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
		flowDao.saveWork(work, flowService.getOrganDao());
		//logger.info(flow.getFormNum() + " 对创建的work对象进行保存操作，这里是insert. End...");
		//logger.info(flow.getFormNum() + " 会办 doNext 代码片断 End...");
		return result;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + QCCenterJointSignFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CENTERJOINTSIGN_START;
	}

	@Override
	Map<? extends String, ? extends PersonDetail> doStart(Flow flow) {
		return null;
	}

	@Override
	boolean startValidate(Flow flow) {
		return false;
	}

	// 用于判断会办是否结束并进入下一审批流程
	@SuppressWarnings("unused")
	private Map<String, PersonDetail> delegateDoComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		PersonDetail actualPerson = personService.loadWidePersonDetail(flow
				.getActualPerson().getEmployeeId());
		// 结束该会办分支，然后判断一下是否结束整个会办工作
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

		if (isJointWholeCycleFinished) {
			//logger.info(flow.getFormNum() + " 判断会办是否完成  Start...");
			// 如果同CMPCODE下的人都完成了
			if (this.flowService.hasJointSignFinished(flow)) {
				//logger.info(flow.getFormNum() + " 会办完成后，更新表单 " + flow.getFormNum() + " 流程的状态 Start...");
				flow.setStatus(FlowStatus.CENTER_CHENGHE_START);
				this.flowService.updateFlowStatus(flow);
				//logger.info(flow.getFormNum() + " 会办完成后，更新表单 " + flow.getFormNum() + " 流程的状态 End...");
				PersonDetail signPerson = flow.getActualPerson() != null ? flow
						.getActualPerson() : flow.getCreatePerson();
				result.putAll(this.flowService.startNextWork(flow, signPerson,
						null));
			}
			logger.info(flow.getFormNum() + " 判断会办会支是否完成  End...");
		} else {
			/**
			 * 说明会办分支没有完成，只是完成了内部一部分，
			 * 所以这边继续逐级上报。记住jointSignStartId切除最后一个片段，塞给新的work对象
			 */
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

}
