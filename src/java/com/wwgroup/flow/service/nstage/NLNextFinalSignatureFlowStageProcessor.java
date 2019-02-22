package com.wwgroup.flow.service.nstage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.wwgroup.common.util.MailUtil;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;

// 核决副主管核准后的呈核上一级
public class NLNextFinalSignatureFlowStageProcessor extends
		NLAbstractFlowStageProcessor {

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
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + NLNextFinalSignatureFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.NEXTFINAL_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 核决副主管 doNext 代码片断 Start...");
		// 获取上一步主线的签核人
		PersonDetail lastPerson = null;
		if (flow.getLastEmployeeId() != null) {
			lastPerson = super.personService.loadWidePersonDetail(
					flow.getLastDeptId(), flow.getLastEmployeeId(),
					flow.getLastPostCode());
		}
		if (lastPerson == null) {
			throw new RuntimeException("当前处理人或下一步处理人汇报关系维护有误，请联系系统管理员处理");
		}
		// 获取上级主管
		PersonDetail mgrPerson = personService
				.getMgrPersonDetail(lastPerson);
		// 判断上级是否是最终核决主管
		boolean isApproval = personService.quailifiedDecisionMaker(flow.getDecionmaker(), mgrPerson);
		
		//20170228
		PersonDetail tmpPersonx = new PersonDetail();
	     tmpPersonx.setEmployeeId("00000818");
	     
	     PersonDetail tmpPersony = new PersonDetail();
	     tmpPersony.setEmployeeId("00315767");
	     
	     PersonDetail tmpPersonz = new PersonDetail();
	     tmpPersonz.setEmployeeId("00001339");


		if ((isApproval) &&((flow.getDecionmaker() == DescionMaker.UNITLEADER) || (flow.getDecionmaker() == DescionMaker.HEADLEADER) ) && mgrPerson.getEmployeeId().equals("91608004") ){
			MailUtil.mailTotmpPerson(tmpPersonx, flow);
			MailUtil.mailTotmpPerson(tmpPersony, flow);
			MailUtil.mailTotmpPerson(tmpPersonz, flow);
		}
		//20170228
		
		if (isApproval || (flow.getDecionmaker() == DescionMaker.DEPTLEADER)){
			// 如果上级是最终核决主管，则直接进入核决节点
			flow.setNextStep(FlowStatus.FINAL_DECISION_START);
			super.flowDao.updateFlowNextStep(flow);
			
			flow.setStatus(FlowStatus.FINAL_DECISION_START);
			super.flowDao.updateFlow(flow);
			result.putAll(this.flowService.startNextWork(flow,
					lastPerson, null));
		} else {
			MyWork work = mgrPerson.buildWork(flow.getFlowType(),
					WorkStage.FBOSS_SIGN, null);
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
			/*logger.info(flow.getFormNum() + " 核决副主管：对创建的work对象进行保存操作，这里是insert. " + work.getEmployeeId() + "("
					+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
			flowDao.saveWork(work, flowService.getOrganDao());
			//logger.info(flow.getFormNum() + " 核决副主管：对创建的work对象进行保存操作，这里是insert. End...");
			result.put(
					mgrPerson.getEmployeeId() + mgrPerson.getPostCode(),
					mgrPerson);
		}
		//logger.info(flow.getFormNum() + " 核决副主管 doNext 代码片断 End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + NLNextFinalSignatureFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.NEXTFINAL_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 核决副主管 doComplete 代码片断 Start...");
		work.setStatus(FlowStatus.APPROVED);
		this.flowDao.updateWork(work);
		
		// 完成时，将当前审核用户信息写到主记录中
		// flow.setNextStep(flow.getNextStep());
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		// 此处注意需增加一个方法，用于更新以上数据.
		this.flowDao.updateFlowLastPerson(flow);
		
		PersonDetail personDetail = personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		//logger.info(flow.getFormNum() + " 核决副主管 doComplete 代码片断 End...");
		return result;
	}

	@Override
	boolean cancelValidate(Flow qianChenFlow) {
		// 最终和核决阶段就不能被撤销
		return false;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.NEXTFINAL_DECISION_START;
	}

}
