package com.wwgroup.flow.service.nstage;

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

public class NLCenterJointSignFlowStageProcessor extends
		NLAbstractFlowStageProcessor {

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
		//logger.info(flow.getFormNum() + " 中心会办　doComplete 操作 Start...");
		// 完成myWork然后判断 本身是不是joinSignStartId，如果是的话，那么就结束分支会办的工作了
		if (CenterJoinSignWork.class.isInstance(work)) {
			work.setStatus(FlowStatus.AGREE);
			this.flowService.updateWorkStatus(work);

			String joinSignStartId = super.getLatestJoinSignStartId(
					work.getJoinSignStartId(), "employeeId");

			// 通过前端参数，判断点击的是否“呈核上一级”(1：呈核上一级；2：呈核后的主管签核)
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
							MyWork tmpWork = this.flowService
									.getFirstHBWork(work);
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
		//logger.info(flow.getFormNum() + " 中心会办　doComplete 操作 End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + NLCenterJointSignFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CENTERJOINTSIGN_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 中心会办　doNext 操作 Start...");
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
		/*logger.info(flow.getFormNum() + " 中心会办:对创建的work对象进行保存操作，这里是insert. " + work.getEmployeeId() + "("
				+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
		flowDao.saveWork(work, flowService.getOrganDao());
		//logger.info(flow.getFormNum() + " 中心会办:对创建的work对象进行保存操作，这里是insert. End...");
		//logger.info(flow.getFormNum() + " 中心会办　doNext 操作 End...");
		return result;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + NLCenterJointSignFlowStageProcessor.class.getName());
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
			// 如果同CMPCODE下的人都完成了
			if (this.flowService.hasJointSignFinished(flow)) {
				flow.setStatus(FlowStatus.CENTER_CHENGHE_START);
				this.flowService.updateFlowStatus(flow);
				PersonDetail signPerson = flow.getActualPerson() != null ? flow
						.getActualPerson() : flow.getCreatePerson();
				result.putAll(this.flowService.startNextWork(flow, signPerson,
						null));
			}
		} else {
			/**
			 * 说明会办分支没有完成，只是完成了内部一部分，
			 * 所以这边继续逐级上报。记住jointSignStartId切除最后一个片段，塞给新的work对象
			 */
			PersonDetail personDetail = personService.loadWidePersonDetail(
					work.getDeptId(), work.getEmployeeId(), work.getPostCode());
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
			result.putAll(this.flowService.startNextWork(flow, mgrPersonDetail,
					work));
		}

		return result;
	}
}
