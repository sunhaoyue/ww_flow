package com.wwgroup.flow.service.nstage;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;

public class NLConfirmFlowStageProcessor extends NLAbstractFlowStageProcessor {

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
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + NLConfirmFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CONFIRM_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 本人确认 doNext 代码片断 Start...");
		// 获取申请人信息
		PersonDetail actualPerson = flow.getActualPerson();
		
		MyWork work = actualPerson.buildWork(flow.getFlowType(),
				WorkStage.CONFIRM, null);
		work.setFlowId(flow.getId());
		
		// 获取当前处理人信息
		PersonDetail tmpPerson = personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(),
				work.getPostCode());
		work.setEmployeenam(tmpPerson.getName());
		work.setTitlenam(tmpPerson.getTitname());
		/*logger.info(flow.getFormNum() + " 本人确认：对创建的work对象进行保存操作，这里是insert. " + work.getEmployeeId() + "("
				+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
		flowDao.saveWork(work, flowService.getOrganDao());
		//logger.info(flow.getFormNum() + " 本人确认：对创建的work对象进行保存操作，这里是insert. End...");
		result.put(actualPerson.getEmployeeId() + actualPerson.getPostCode(),
				actualPerson);
		//logger.info(flow.getFormNum() + " 本人确认 doNext 代码片断 End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + NLConfirmFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CONFIRM_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 本人确认 doComplete 代码片断 Start...");
		// 参数通过work得到
		//work.setStatus(FlowStatus.AGREE);2016-10-8
		work.setStatus(FlowStatus.COPY_SEND);
		this.flowDao.updateWork(work);

		// 完成时，将当前审核用户信息写到主记录中
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		// 此处注意需增加一个方法，用于更新以上数据.
		this.flowDao.updateFlowLastPerson(flow);
		
		if (StringUtils.isEmpty(flow.getCopyDeptName())){
			flow.setStatus(FlowStatus.FINAL_DECISION_END);
		} else {
			flow.setStatus(FlowStatus.CC_End);
		}
		
		this.flowDao.updateFlow(flow);
		
		long endTime = System.currentTimeMillis();
		flow.setEndTime(endTime);
		this.flowDao.updateFlowEndTime(flow);

		// 参数通过work得到.
		// 传入的是部门ID + 人员ID
		// 这个传入person其实关系不大
		PersonDetail personDetail = this.personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		//logger.info(flow.getFormNum() + " 本人确认 doComplete 代码片断 End...");
		return result;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		//return flow.getStatus() == FlowStatus.CONFIRM_START;
		// 由于修改后的本人确人为最后一步，即核决主管已核准且会办完成，所以不可以撤消
		return false;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return false;
	}

}
