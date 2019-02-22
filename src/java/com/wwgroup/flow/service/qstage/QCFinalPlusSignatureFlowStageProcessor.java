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
import com.wwgroup.flow.bo.work.MyWork;

// 这里是神旺控股执行总经理的第二次签核，主要是会办的职能部门中如果都是同意，则需回到执行总经理这里再次签核确认
public class QCFinalPlusSignatureFlowStageProcessor extends QCAbstractFlowStageProcessor {

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
		return flow.getStatus() == FlowStatus.FINALPLUS_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail, MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 核决主管　doNext 操作 Start...");
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (prevWork != null && StringUtils.isNotEmpty(prevWork.getJoinSignStartId())) {
			MyWork work = personDetail.buildWork(flow.getFlowType(), WorkStage.BOSSPLUS_SIGN, prevWork);
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			work.setJoinCycle(prevWork.getJoinCycle());
			work.setJoinStartEmployeeId(prevWork.getJoinStartEmployeeId());
			work.setFlowId(flow.getId());
			work.setParentId(prevWork.getParentId());
			
			// 获取当前处理人信息
			PersonDetail tmpPerson = personService.loadWidePersonDetail(
					work.getDeptId(), work.getEmployeeId(),
					work.getPostCode());
			work.setEmployeenam(tmpPerson.getName());
			work.setTitlenam(tmpPerson.getTitname());
			
			flowDao.saveWork(work, flowService.getOrganDao());
			result.put(personDetail.getEmployeeId() + personDetail.getPostCode(), personDetail);
			return result;
		}
		
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
		 * 这里就不用去再获取上一次签核人的主管了，能走到这一步，上一步肯定是核决管
		 * 1、执行总经理
		 * 2、电解水、职能部门的单位最高主管
		 */
		
		MyWork work = lastPerson.buildWork(flow.getFlowType(), WorkStage.BOSSPLUS_SIGN, null);
		work.setFlowId(flow.getId());
		work.setJoinCycle(work.getJoinCycle());
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("employeeId", lastPerson.getEmployeeId());
		jsonObj.put("deptId", lastPerson.getDeptId());
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
		flowDao.saveWork(work, flowService.getOrganDao());
		result.put(lastPerson.getEmployeeId() + lastPerson.getPostCode(), lastPerson);
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.FINALPLUS_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (!StringUtils.isEmpty(work.getJoinStartEmployeeId()) 
				// 修改指派结束判断
				// 原来是判断当前工号和指派发起人工号是否一致，如果一个工号兼职了多个中心主管，则可能产生问题
				// 所以修改为判断当前签核记录的岗位代码和父ID的岗位代码是否一致
				//&& !work.getEmployeeId().equals(work.getJoinStartEmployeeId())) {
				&& !this.flowService.isCompleteAssign(work)) {
			work.setStatus(FlowStatus.AGREE);
			this.flowService.updateWorkStatus(work);
			
			PersonDetail personDetail =
					personService.loadWidePersonDetail(work.getDeptId(), work.getEmployeeId(), work.getPostCode());
			PersonDetail mgrPersonDetail = personService.getMgrPersonDetail(personDetail);
			result.putAll(this.flowService.startNextWork(flow, mgrPersonDetail, work));
			return result;
		}
		// 参数通过work得到
		work.setStatus(FlowStatus.AGREE);
		this.flowDao.updateWork(work);
		
		// 完成时，将当前审核用户信息写到主记录中
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		// 此处注意需增加一个方法，用于更新以上数据.
		this.flowDao.updateFlowLastPerson(flow);

		/**
		 * 这里是否需要董事长签核的判断条件是：
		 * 1、表单中勾选了神旺控股董事长
		 * 2、表单中勾选了副总裁或总裁
		 * 3、会办中有职能部门，且职能部门中有不同意项
		 */
		boolean toChairman = false;
		/*if (flow.getPendJointSignDeptIds() != null && flow.getPendJointSignDeptIds().length > 0){
			String[] jointSIgnDeptIds = flow.getPendJointSignDeptIds();
			int i = 0;
			for(i = 0; i < jointSIgnDeptIds.length; i++){
				String jointSignDeptId = jointSIgnDeptIds[i];
				SystemGroups tmpGroups = flowService.loadGroupsById(Integer.valueOf(jointSignDeptId));
				if (tmpGroups.getSystemFlg().equals("Y")){
					toChairman = true;
					break;
				}
			}
		}
		if (!toChairman){
			if (flow.getDecionmaker() == DescionMaker.CHAIRMAN || flow.isSubmitBoss() || flow.isSubmitFBoss()){
				toChairman = true;
			}
		}*/
		if (!toChairman){
			if (flow.isChariman() || flow.isSubmitBoss() || flow.isSubmitFBoss()){
				toChairman = true;
			}
		}
		
		// 如果需要董事长签核，则去董事长签核步骤。否则直接去本人确认
		if (toChairman){
			flow.setStatus(FlowStatus.CHAIRMANSIGN_START);
		} else {
			flow.setStatus(FlowStatus.CONFIRM_START);
		}
		super.flowDao.updateFlow(flow);

		// 参数通过work得到.
		// 传入的是部门ID + 人员ID
		PersonDetail personDetail =
				this.personService.loadWidePersonDetail(work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(this.flowService.getFlow(work), personDetail, work));
		//logger.info(flow.getFormNum() + " 核决主管　doComplete 操作 End...");
		return result;
	}

	@Override
	boolean cancelValidate(Flow qianChenFlow) {
		// 最终和核决阶段就不能被撤销
		return false;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.FINAL_DECISION_START;
	}
}
