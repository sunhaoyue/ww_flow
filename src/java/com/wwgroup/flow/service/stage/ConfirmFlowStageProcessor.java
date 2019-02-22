package com.wwgroup.flow.service.stage;

import java.util.HashMap;
import java.util.Map;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;

public class ConfirmFlowStageProcessor extends AbstractFlowStageProcessor {

	@Override
	boolean startValidate(Flow flow) {
		//System.out.println("Start: " + this.getClass().getName());
		return false;
	}

	@Override
	Map<String, PersonDetail> doStart(Flow flow) {
		return new HashMap<String, PersonDetail>(2);
	}

	@Override
	boolean startNextValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CONFIRM_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);

		MyWork work = personDetail.buildWork(flow.getFlowType(),
				WorkStage.CONFIRM, null);
		work.setFlowId(flow.getId());
		flowDao.saveWork(work, flowService.getOrganDao());

		result.put(personDetail.getEmployeeId() + personDetail.getPostCode(),
				personDetail);
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//System.out.println("ConfirmFlowStageProcessor");
		return flow.getStatus() == FlowStatus.CONFIRM_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 参数通过work得到
		work.setStatus(FlowStatus.AGREE);
		this.flowDao.updateWork(work);

		//flow.setStatus(FlowStatus.FINAL_DECISION_START);
		if (flow.getSecondEmployeeId() != null){
			flow.setStatus(FlowStatus.SECONDFINAL_DECISION_START);
		} else {
			flow.setStatus(FlowStatus.FINAL_DECISION_START);
		}
		this.flowDao.updateFlow(flow);

		// 参数通过work得到.
		// 传入的是部门ID + 人员ID
		// 这个传入person其实关系不大
		PersonDetail personDetail = this.personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		return result;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CONFIRM_START;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return false;
	}

}
