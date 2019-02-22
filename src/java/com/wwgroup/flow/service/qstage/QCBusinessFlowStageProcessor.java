package com.wwgroup.flow.service.qstage;

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

/**
 * 事业部主管签核
 * 由于事业部副主管和主管都可以进行指派操作，所以分拆为两个节点。
 * @author eleven
 *
 */
public class QCBusinessFlowStageProcessor extends
		QCAbstractFlowStageProcessor {

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.BUSINESS_DECISION_START;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.BUSINESS_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 事业部主管　doComplete 操作 Start...");
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (!StringUtils.isEmpty(work.getJoinStartEmployeeId()) 
				&& !work.getEmployeeId().equals(work.getJoinStartEmployeeId())) {
			//logger.info(flow.getFormNum() + " 事业部主管指派代码片段 Start...");
			work.setStatus(FlowStatus.AGREE);
			//logger.info(flow.getFormNum() + " 更新 work 完成后的状态为: " + FlowStatus.AGREE + ". Start...");
			this.flowService.updateWorkStatus(work);
			//logger.info(flow.getFormNum() + " 更新 work 完成后的状态为: " + FlowStatus.AGREE + ". End...");
			
			//logger.info(flow.getFormNum() + " 指派流转根据work中的部门ID、工号、岗位获取人员信息 Start...");
			PersonDetail personDetail = personService.loadWidePersonDetail(
					work.getDeptId(), work.getEmployeeId(), work.getPostCode());
			//logger.info(flow.getFormNum() + " 指派流转根据work中的部门ID、工号、岗位获取人员信息 End...");
			//logger.info(flow.getFormNum() + " 根据人员信息获取上传主管信息 Start...");
			PersonDetail mgrPersonDetail = personService
					.getMgrPersonDetail(personDetail);
			//logger.info(flow.getFormNum() + " 根据人员信息获取上传主管信息 End...");
			result.putAll(this.flowService.startNextWork(flow, mgrPersonDetail,
					work));
			//logger.info(flow.getFormNum() + " 事业部主管指派代码片段 End...");
			return result;
		}
		
		//logger.info(flow.getFormNum() + " 更新事业部主管 work 完成后的状态为: " + FlowStatus.APPROVED + ". Start...");
		work.setStatus(FlowStatus.APPROVED);
		this.flowDao.updateWork(work);
		//logger.info(flow.getFormNum() + " 更新事业部主管 work 完成后的状态为: " + FlowStatus.APPROVED + ". End...");
		
		// 完成时，将当前审核用户信息写到主记录中
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		// 此处注意需增加一个方法，用于更新以上数据.
		///logger.info(flow.getFormNum() + " 将当前处理人信息更新至flow中的上一步处理人 Start...");
		this.flowDao.updateFlowLastPerson(flow);
		//logger.info(flow.getFormNum() + " 将当前处理人信息更新至flow中的上一步处理人 End...");
		
		//logger.info(flow.getFormNum() + " 根据work中的部门ID、工号、岗位获取人员信息 Start...");
		PersonDetail personDetail = personService.loadWidePersonDetailPlus(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		//logger.info(flow.getFormNum() + " 根据work中的部门ID、工号、岗位获取人员信息 End...");
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		//logger.info(flow.getFormNum() + " 事业部主管　doComplete 操作 End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + QCBusinessFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.BUSINESS_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 事业部主管　doNext 操作 Start...");
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (prevWork != null
				&& StringUtils.isNotEmpty(prevWork.getJoinSignStartId())) {
			//logger.info(flow.getFormNum() + " 事业部主管指派代码片段 Start...");
			//logger.info(flow.getFormNum() + " 根据传入的personDetail创建work对象，此时并未保存至数据库...");
			MyWork work = personDetail.buildWork(flow.getFlowType(),
					WorkStage.BUSINESS_SIGN, prevWork);
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			work.setJoinCycle(prevWork.getJoinCycle());
			work.setJoinStartEmployeeId(prevWork.getJoinStartEmployeeId());
			work.setFlowId(flow.getId());
			work.setParentId(prevWork.getParentId());
			
			// 获取当前处理人信息
			//logger.info(flow.getFormNum() + " 指派流转根据work中的部门ID、工号、岗位获取人员信息 Start...");
			PersonDetail tmpPerson = personService.loadWidePersonDetail(
					work.getDeptId(), work.getEmployeeId(),
					work.getPostCode());
			//logger.info(flow.getFormNum() + " 指派流转根据work中的部门ID、工号、岗位获取人员信息 End...");
			work.setEmployeenam(tmpPerson.getName());
			work.setTitlenam(tmpPerson.getTitname());
			/*logger.info(flow.getFormNum() + " 保存指派的work对象，这里是insert. " + work.getEmployeeId() + "("
					+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
			flowDao.saveWork(work, flowService.getOrganDao());
			//logger.info(flow.getFormNum() + " 保存指派的work对象，这里是insert. End...");
			result.put(
					personDetail.getEmployeeId() + personDetail.getPostCode(),
					personDetail);
			//logger.info(flow.getFormNum() + " 事业部主管指派代码片段 End...");
			return result;
		}
		PersonDetail lastPerson = null;
		if (flow.getLastEmployeeId() != null) {
			//logger.info(flow.getFormNum() + " 获取上一步的处理人信息 Start...");
			lastPerson = super.personService.loadWidePersonDetail(
					flow.getLastDeptId(), flow.getLastEmployeeId(),
					flow.getLastPostCode());
			//logger.info(flow.getFormNum() + " 获取上一步的处理人信息 End...");
		}
		if (lastPerson == null) {
			throw new RuntimeException("当前处理人或下一步处理人汇报关系维护有误，请联系系统管理员处理");
		}
		
		if (flow.isLocal()){
			/**
			 * 体系内
			 * 如果是体系内，本节点仅作为过渡，直接去下一个节点
			 */
			//logger.info(flow.getFormNum() + " 体系内：");
			flow.setNextStep(FlowStatus.NEXTFINAL_DECISION_START);
			//logger.info(flow.getFormNum() + " 更新流程下一个处理步骤 " + FlowStatus.NEXTFINAL_DECISION_START + " Start...");
			super.flowDao.updateFlowNextStep(flow);
			//logger.info(flow.getFormNum() + " 更新流程下一个处理步骤 " + FlowStatus.NEXTFINAL_DECISION_START + " End...");
			// 判断是否存在会办
			// 这里判断是否存在会办就不能用原来存储选择的列表，而且未完成的会办列表
			if (flow.getPendJointSignDeptIds() != null
					&& flow.getPendJointSignDeptIds().length > 0) {
				//logger.info(flow.getFormNum() + "　A:进入其它单位会办　Start...");
				flow.setStatus(FlowStatus.JOINTSIGN_START);
				super.flowDao.updateFlow(flow);
				result.putAll(this.delegateJointSignWork(flow,
						personDetail));
				//logger.info(flow.getFormNum() + "　A:进入其它单位会办　End...");
			} else {
				// 没有会办，直接去最高副主管节点。
				//logger.info(flow.getFormNum() + " A:没有会办，进入核决副主管节点 Start...");
				flow.setStatus(FlowStatus.NEXTFINAL_DECISION_START);
				super.flowDao.updateFlow(flow);
				result.putAll(this.flowService.startNextWork(flow,
						lastPerson, null));
				//logger.info(flow.getFormNum() + " A:没有会办，进入核决副主管节点 End...");
			}
		} else {
			/**
			 * 地方至总部
			 * 事业部的主管肯定是中心主管，所以判断上一步处理的人上级（上一级处理人肯定是地方的最高主管）
			 * 所以要先看这个上一级是否是最终的核决主管或副主管
			 * 表单中如果选择的核决主管是事业部主管，那么这里对应的上级是否是中心主管
			 * 表单中如果选择的核决主管是神旺控股最高主管，那么这里对应的上级是否是单位最高主管
			 */
			//logger.info(flow.getFormNum() + " 地方至总部 Start...");
			// 获取上级主管
			//logger.info(flow.getFormNum() + " 获取上级主管PersonDetail对象信息 Start...");
			PersonDetail mgrPerson = personService
					.getMgrPersonDetail(lastPerson);
			//logger.info(flow.getFormNum() + " 获取上级主管PersonDetail对象信息 End...");
			//logger.info(flow.getFormNum() + " 根据工号和岗位获取上级主管EmployeePos对象信息 Start...");
			EmployeePos mgrEmployee = userService.getEmployeePosByEmpId(
					mgrPerson.getEmployeeId(), mgrPerson.getPostCode());
			//logger.info(flow.getFormNum() + " 根据工号和岗位获取上级主管EmployeePos对象信息 End...");
			// 上级是否是核决主管
			//logger.info(flow.getFormNum() + " 判断上级主管是否为核决主管　Start...");
			boolean isApproval = personService.quailifiedDecisionMaker(flow.getDecionmaker(), mgrPerson);
			//logger.info(flow.getFormNum() + " 判断上级主管是否为核决主管(" + isApproval + ")　End...");
			// 上级是否是核决副主管
			//logger.info(flow.getFormNum() + " 判断上级主管是否为核决副主管　Start...");
			boolean isApprovalF = personService.quailifiedDecisionMakerPlus(flow.getDecionmaker(), mgrPerson);
			//logger.info(flow.getFormNum() + " 判断上级主管是否为核决副主管(" + isApprovalF + ")　End...");
			if (isApproval || isApprovalF || mgrEmployee.isTopFmgr() || mgrEmployee.isTopmgr()){
				// 记录下一步处理节点为核决副主管，并转至下一节点
				flow.setNextStep(FlowStatus.NEXTFINAL_DECISION_START);
				super.flowDao.updateFlowNextStep(flow);
				
				// 判断是否存在会办
				// 这里判断是否存在会办就不能用原来存储选择的列表，而且未完成的会办列表
				if (flow.getPendJointSignDeptIds() != null
						&& flow.getPendJointSignDeptIds().length > 0) {
					//logger.info(flow.getFormNum() + " B:有会办单位，进行其它单位会办 Start...");
					flow.setStatus(FlowStatus.JOINTSIGN_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.delegateJointSignWork(flow,
							personDetail));
					//logger.info(flow.getFormNum() + " B:有会办单位，进行其它单位会办 End...");
				} else {
					//logger.info(flow.getFormNum() + " B:没有会办单位,直接进入核决副主管节点 Start...");
					flow.setStatus(FlowStatus.NEXTFINAL_DECISION_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow,
							lastPerson, null));
					//logger.info(flow.getFormNum() + " B:没有会办单位,直接进入核决副主管节点 End...");
				}
			} else {
				// 继续呈核至中心主管
				//logger.info(flow.getFormNum() + " 继续汇报至事业部主管呈核节点 Start...");
				flow.setNextStep(FlowStatus.BUSINESS_DECISION_START);
				super.flowDao.updateFlowNextStep(flow);

				MyWork work = mgrPerson.buildWork(flow.getFlowType(),
						WorkStage.BUSINESS_SIGN, null);
				work.setFlowId(flow.getId());
				work.setJoinCycle(work.getJoinCycle());
				
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("employeeId", mgrPerson.getEmployeeId());
				jsonObj.put("deptId", mgrPerson.getDeptId());
				JSONArray jsonArray = new JSONArray();
				jsonArray.add(jsonObj);

				work.setJoinSignStartId(jsonArray.toString());
				work.setJoinStartEmployeeId(work.getEmployeeId());
				
				// 获取当前处理人信息
				PersonDetail tmpPerson = personService.loadWidePersonDetail(
						work.getDeptId(), work.getEmployeeId(),
						work.getPostCode());
				work.setEmployeenam(tmpPerson.getName());
				work.setTitlenam(tmpPerson.getTitname());
				/*logger.info(flow.getFormNum() + " 继续进行事业部主管签核,创建work对象并保存至数据库(insert.)"
						+ work.getEmployeeId() + "("
						+ work.getFlowId() + "、" + work.getDeptId() + ")" + "Start...");*/
				flowDao.saveWork(work, flowService.getOrganDao());
				//logger.info(flow.getFormNum() + " 继续进行事业部主管签核,创建work对象并保存至数据库. End...");
				result.put(
						mgrPerson.getEmployeeId() + mgrPerson.getPostCode(),
						mgrPerson);
				//logger.info(flow.getFormNum() + " 继续汇报至事业部主管呈核节点 End...");
			}
			//logger.info(flow.getFormNum() + " 地方至总部 End...");
		}
		//logger.info(flow.getFormNum() + " 事业部主管　doNext 操作 End...");
		return result;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + QCBusinessFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.BUSINESS_DECISION_START;
	}

	@Override
	Map<? extends String, ? extends PersonDetail> doStart(Flow flow) {
		return new HashMap<String, PersonDetail>(2);
	}

	@Override
	boolean startValidate(Flow flow) {
		return false;
	}

	private Map<String, PersonDetail> delegateJointSignWork(Flow flow,
			PersonDetail personDetail) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 所有剩余会办都在此处完成
		//logger.info(flow.getFormNum() + " 进行其它会单位work的创建 Start...");
		if (flow.getPendJointSignDeptIds() != null
				&& flow.getPendJointSignDeptIds().length > 0) {
			String[] jointSignDeptIds = flow.getPendJointSignDeptIds();
			int count = -1;
			String tmpNoSignDeptIds = "";
			for (int i = 0; i < jointSignDeptIds.length; i++) {
				String jointSignDeptId = jointSignDeptIds[i];
				//logger.info(flow.getFormNum() + " 根据会办单位的ID获取对应的主管 Start...");
				PersonDetail person = personService
						.loadWideMgrPersonDetail(jointSignDeptId);
				SystemGroups tmpGroups = flowService.loadGroupsById(Integer.valueOf(jointSignDeptId));
				//logger.info(flow.getFormNum() + " 根据会办单位的ID获取对应的主管 End...");
				//logger.info(flow.getFormNum() + " 创建对应的work对象，此时并不保存至数据库 Start...");
				if (!tmpGroups.getSystemFlg().equals("Y")){
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
					//logger.info(flow.getFormNum() + " 创建对应的work对象，此时并不保存至数据库 End...");
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
		//logger.info(flow.getFormNum() + " 进行其它会单位work的创建 Start...");
		return result;
	}
}
