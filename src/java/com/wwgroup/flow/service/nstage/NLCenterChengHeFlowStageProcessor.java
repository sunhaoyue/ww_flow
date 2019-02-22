package com.wwgroup.flow.service.nstage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.JointSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.user.bo.EmployeePos;

// 驳回逻辑：驳回动作只有在审核阶段才能够发送，
public class NLCenterChengHeFlowStageProcessor extends NLAbstractFlowStageProcessor {

	@Override
	boolean startValidate(Flow flow) {
		return false;
	}

	@Override
	Map<String, PersonDetail> doStart(Flow flow) {
		return new HashMap<String, PersonDetail>(2);
	}

	@Override
	boolean startNextValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + NLCenterChengHeFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 中心主管　doNext 操作 Start...");
		PersonDetail lastPerson = null;
		if (flow.getLastEmployeeId() != null) {
			lastPerson = super.personService.loadWidePersonDetail(
					flow.getLastDeptId(), flow.getLastEmployeeId(),
					flow.getLastPostCode());
		}
		if (lastPerson == null) {
			throw new RuntimeException("当前处理人或下一步处理人汇报关系维护有误，请联系系统管理员处理");
		}
		
		/**
		 * 正常流转应该是：中心主管核准后进入本单位会办
		 * 1、先判断下一步取节点代码和当前节点的的代码是否一致，如果一致，则进行正常是中心主管等判断，如果不一致，则直接进入本单位会办
		 * 2、如果一致，则需要判断上级是否是中心主管（副主管仍需要继续呈核至中心主管）
		 */
		EmployeePos employee = userService.getEmployeePosByEmpId(lastPerson
				.getEmployeeId(), lastPerson.getPostCode());
		// 获取上级主管
		PersonDetail mgrPerson = personService
				.getMgrPersonDetail(lastPerson);
		EmployeePos mgrEmployee = userService.getEmployeePosByEmpId(
				mgrPerson.getEmployeeId(), mgrPerson.getPostCode());
		if (flow.getStatus() == flow.getNextStep()) {
			// 判断上级是否核决主管
			boolean isApproval = personService.quailifiedDecisionMaker(
					flow.getDecionmaker(), mgrPerson);
			// 判断上级是否核决副主管
			boolean isApprovalF = personService
					.quailifiedDecisionMakerPlus(flow.getDecionmaker(),
							mgrPerson);
			/**
			 * 上级是核决副主管或核决主管，那么肯定是要先去进行会办
			 */
			if (isApproval || isApprovalF) {
				flow.setNextStep(FlowStatus.NEXTFINAL_DECISION_START);
				super.flowDao.updateFlowNextStep(flow);

				// 判断是否存在会办
				if (flow.getPendJointSignDeptIds() != null
						&& flow.getPendJointSignDeptIds().length > 0) {
					flow.setStatus(FlowStatus.JOINTSIGN_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.delegateJointSignWork(flow,
							personDetail));
				} else {
					// 没有会办，直接去最高副主管节点。
					flow.setStatus(FlowStatus.NEXTFINAL_DECISION_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow,
							lastPerson, null));
				}
			} else {
				/**
				 * 1、如果当前用户是中心主管，直接进入本单位会办
				 * 2、如果上级是最高副主管或最高主管，与当前用户是中心主管的情况是一样的，直接进入本单位会办
				 * 以上两种情况的下一步骤节点都是最高副主管节点。
				 */
				if (employee.isCentermgr() || mgrEmployee.isTopFmgr()
						|| mgrEmployee.isTopmgr()) {
					flow.setNextStep(FlowStatus.NEXTFINAL_DECISION_START);
					super.flowDao.updateFlowNextStep(flow);

					// 判断是否存在会办
					if (flow.getPendJointSignDeptIds() != null
							&& flow.getPendJointSignDeptIds().length > 0) {
						flow.setStatus(FlowStatus.JOINTSIGN_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.delegateJointSignWork(flow,
								personDetail));
					} else {
						// 没有会办，直接去最高副主管节点。
						flow.setStatus(FlowStatus.NEXTFINAL_DECISION_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.flowService.startNextWork(flow,
								lastPerson, null));
					}
				} else {
					/**
					 * 这里剩下的就是当前用户不是中心主管，且上一级不是中心以上主管
					 * 目前肯定这种情况下，当前用户肯定就是中心副主管 所以需要继续向上呈核
					 */
					flow.setNextStep(FlowStatus.CENTER_CHENGHE_START);
					super.flowDao.updateFlowNextStep(flow);

					MyWork work = mgrPerson.buildWork(flow.getFlowType(),
							WorkStage.CENTER_CHENGHE, null);
					work.setFlowId(flow.getId());
					
					// 获取当前处理人信息
					PersonDetail tmpPerson = personService.loadWidePersonDetail(
							work.getDeptId(), work.getEmployeeId(),
							work.getPostCode());
					work.setEmployeenam(tmpPerson.getName());
					work.setTitlenam(tmpPerson.getTitname());
					/*logger.info(flow.getFormNum() + " 中心主管节点继续汇报中心主管:对创建的work对象进行保存操作，这里是insert. " + work.getEmployeeId() + "("
							+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
					flowDao.saveWork(work, flowService.getOrganDao());
					//logger.info(flow.getFormNum() + " 中心主管节点继续汇报中心主管:对创建的work对象进行保存操作，这里是insert. End...");
					result.put(
							mgrPerson.getEmployeeId()
									+ mgrPerson.getPostCode(), mgrPerson);
				}
			}
		} else {
			// 不一致，直接进入本单位会办
			// 判断是否存在会办
			// 这里判断是否存在会办就不能用原来存储选择的列表，而是未完成的会办列表
			if (flow.getPendJointSignDeptIds() != null
					&& flow.getPendJointSignDeptIds().length > 0) {
				flow.setStatus(FlowStatus.JOINTSIGN_START);
				super.flowDao.updateFlow(flow);
				result.putAll(this.delegateJointSignWork(flow,
						personDetail));
			} else {
				// 没有会办，直接进入相应的节点去审批。目前来说只有体系内才存在这种情况
				// ，当前审核节点和下一步审核节点不一致，下一步肯定是去到核决副主管节点。
				flow.setStatus(flow.getNextStep());
				super.flowDao.updateFlow(flow);
				result.putAll(this.flowService.startNextWork(flow,
						lastPerson, null));
			}
		}
		//logger.info(flow.getFormNum() + " 中心主管　doNext 操作 End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + NLCenterChengHeFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 中心主管　doComplete 操作 Start...");
		// 领导审核与传统的 会签等区分 所以这边用了 另外一个字段来表示
		work.setStatus(FlowStatus.APPROVED);
		this.flowDao.updateWork(work);

		// 完成时，将当前审核用户信息写到主记录中
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		// 此处注意需增加一个方法，用于更新以上四项数据.
		this.flowDao.updateFlowLastPerson(flow);

		PersonDetail personDetail = personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		//logger.info(flow.getFormNum() + " 中心主管　doComplete 操作 End...");
		return result;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}
	
	/**
	 * 这里的会办要改造 从未完成的会办列表中抓取本单位的会办单位进行会办
	 * 
	 * @param flow
	 * @param personDetail
	 * @param prevWork
	 * @return
	 */
	private Map<String, PersonDetail> delegateJointSignWork(Flow flow,
			PersonDetail personDetail) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 所有剩余会办都在此处完成
		if (flow.getPendJointSignDeptIds() != null
				&& flow.getPendJointSignDeptIds().length > 0) {
			String[] jointSignDeptIds = flow.getPendJointSignDeptIds();
			String tmpNoSignDeptIds = "";
			int count = -1;
			for (int i = 0; i < jointSignDeptIds.length; i++) {
				String jointSignDeptId = jointSignDeptIds[i];
				SystemGroups tmpGroups = flowService.loadGroupsById(Integer.valueOf(jointSignDeptId));
				if (!tmpGroups.getSystemFlg().equals("Y")){
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
					
					count--;
				} else {
					if (StringUtils.isEmpty(tmpNoSignDeptIds)){
						tmpNoSignDeptIds = jointSignDeptId;
					} else {
						tmpNoSignDeptIds += ";" + jointSignDeptId;
					}
				}
			}
			
			// 更新未会办的单位列表
			super.flowDao.updateNoJointSignDeptIds(tmpNoSignDeptIds, flow);
			
			if (count == -1){
				flow.setStatus(FlowStatus.NEXTFINAL_DECISION_START);
				flowDao.updateFlow(flow);
				result.putAll(this.flowService.startNextWork(flow, personDetail, null));
			}
		}
		return result;
	}

}
