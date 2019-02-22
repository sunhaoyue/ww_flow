package com.wwgroup.flow.service.stage;

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

// 指派的逻辑(该逻辑在 地方到中心的 指派 是一致的): 在final work表里加入 jointStartEmployeeId字段，自己本身的work就填入这个值，然后可以指派
// 指派完成后，逐级上报，最终到了 jointStartEmployeeId == work内的employeeId. ，其实还是可以继续往下再进行指派的，
// 具体结束与否 也就是 确定 按钮 什么时候出现，视 jointStartEmployeeId == work内的employeeId决定。
public class FinalSignatureFlowStageProcessor extends AbstractFlowStageProcessor {

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
	boolean startNextValidate(Flow flow) {
		//System.out.println("StartNext: " + this.getClass().getName());
		return flow.getStatus() == FlowStatus.FINAL_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail, MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (prevWork != null && StringUtils.isNotEmpty(prevWork.getJoinSignStartId())) {
			MyWork work = personDetail.buildWork(flow.getFlowType(), WorkStage.BOSS_SIGN, prevWork);
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			work.setJoinCycle(prevWork.getJoinCycle());
			work.setJoinStartEmployeeId(prevWork.getJoinStartEmployeeId());
			work.setFlowId(flow.getId());
			work.setParentId(prevWork.getParentId());
			flowDao.saveWork(work, flowService.getOrganDao());
			
			result.put(personDetail.getEmployeeId() + personDetail.getPostCode(), personDetail);
			return result;
		}

		if (flow.getShengheEmployeeId() != null) {
			personDetail =
				super.personService.loadWidePersonDetail(flow.getShengheDeptId(), flow.getShengheEmployeeId(), flow
						.getShenghePostCode());
			// personDetail = super.personService.getMgrPersonDetail(personDetail);
		}
		else {
			PersonDetail actualPerson = flow.getActualPerson();
			personDetail = super.personService.getMgrPersonDetail(actualPerson);
		}
		MyWork work = personDetail.buildWork(flow.getFlowType(), WorkStage.BOSS_SIGN, null);
		work.setFlowId(flow.getId());
		work.setJoinCycle(work.getJoinCycle());
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("employeeId", personDetail.getEmployeeId());
		jsonObj.put("deptId", personDetail.getDeptId());
		JSONArray jsonArray = new JSONArray();
		jsonArray.add(jsonObj);

		work.setJoinSignStartId(jsonArray.toString());
		
		//work.setJoinSignStartId(work.getEmployeeId());
		work.setJoinStartEmployeeId(work.getEmployeeId());
		result.put(personDetail.getEmployeeId() + personDetail.getPostCode(), personDetail);
		
		flowDao.saveWork(work, flowService.getOrganDao());
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//System.out.println("completeValidate: " + this.getClass().getName());
		return flow.getStatus() == FlowStatus.FINAL_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (!StringUtils.isEmpty(work.getJoinStartEmployeeId()) 
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
		
		if (StringUtils.isEmpty(flow.getCopyDeptName())) {
			flow.setStatus(FlowStatus.FINAL_DECISION_END);
		}
		else {
			// 需要再确认一下关于抄送的细节
			flow.setStatus(FlowStatus.COPY_SEND);
		}
		this.flowDao.updateFlow(flow);

		// 参数通过work得到.
		// 传入的是部门ID + 人员ID
		PersonDetail personDetail =
				this.personService.loadWidePersonDetail(work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(this.flowService.getFlow(work), personDetail, work));
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
