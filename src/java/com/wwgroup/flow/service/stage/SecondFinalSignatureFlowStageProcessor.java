package com.wwgroup.flow.service.stage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;

// 最高副主管处理
@SuppressWarnings("unused")
public class SecondFinalSignatureFlowStageProcessor extends
		AbstractFlowStageProcessor {

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
		//System.out.println("SecondFinalSignatureFlowStageProcessor");
		return flow.getStatus() == FlowStatus.SECONDFINAL_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (prevWork != null
				&& StringUtils.isNotEmpty(prevWork.getJoinSignStartId())) {
			MyWork work = personDetail.buildWork(flow.getFlowType(),
					WorkStage.DIVISION_SIGN, prevWork);
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			work.setJoinCycle(prevWork.getJoinCycle());
			work.setJoinStartEmployeeId(prevWork.getJoinStartEmployeeId());
			work.setFlowId(flow.getId());
			work.setParentId(prevWork.getParentId());
			flowDao.saveWork(work, flowService.getOrganDao());
			result.put(
					personDetail.getEmployeeId() + personDetail.getPostCode(),
					personDetail);
			return result;
		}

		// 先判断主记录中是否记录了副主管信息
		if (flow.getSecondEmployeeId() != null) {
			personDetail = super.personService.loadWidePersonDetailPlus(
					flow.getSecondDeptId(), flow.getSecondEmployeeId(),
					flow.getSecondPostCode());
		} else if (flow.getShengheEmployeeId() != null) {
			personDetail = super.personService.loadWidePersonDetailPlus(
					flow.getShengheEmployeeId(), flow.getShengheDeptId(),
					flow.getShenghePostCode());
		} else {
			PersonDetail actualPerson = flow.getActualPerson();
			personDetail = super.personService.getMgrPersonDetail(actualPerson);
		}
		// 判断当前用户是否是最终核决主管
		boolean isTopSelf = personService.quailifiedDecisionMaker(
				flow.getDecionmaker(), personDetail);
		if (isTopSelf) {
			flow.setShengheEmployeeId(personDetail.getEmployeeId());
			flow.setShengheDeptId(personDetail.getDeptId());
			flow.setShenghePostCode(personDetail.getPostCode());
			flow.setShengheStep(flow.getDecionmaker().ordinal());
			flowDao.saveFlowShengheProperties(flow.getId(),
					personDetail.getEmployeeId(), personDetail.getDeptId(),
					personDetail.getPostCode(), flow.getShengheStep());
			flow.setStatus(FlowStatus.FINAL_DECISION_START);
			super.flowDao.updateFlow(flow);
			result.putAll(this.flowService.startNextWork(flow, personDetail,
					null));
			return result;
		}
		PersonDetail mgrPerson = null;
		try {
			mgrPerson = personService.getMgrPersonDetail(personDetail);
		} catch (Exception e) {
			throw new RuntimeException("当前处理人或下一步处理人汇报关系维护有误，请联系系统管理员处理");
		}
		// 判断上级是否是最终核决主管
		boolean isTopMgr = personService.quailifiedDecisionMaker(
				flow.getDecionmaker(), mgrPerson);
		// 如果不是最终核决的主管
		PersonDetail tmpMgrPerson = mgrPerson;
		boolean tmpTopMgr = isTopMgr;
		while (!tmpTopMgr) {
			try {
				tmpMgrPerson = personService.getMgrPersonDetail(tmpMgrPerson);
			} catch (Exception e) {
				tmpMgrPerson = mgrPerson;
			}
			tmpTopMgr = personService.quailifiedDecisionMaker(
					flow.getDecionmaker(), tmpMgrPerson);
		}
		if (tmpTopMgr) {
			flow.setShengheEmployeeId(tmpMgrPerson.getEmployeeId());
			flow.setShengheDeptId(tmpMgrPerson.getDeptId());
			flow.setShenghePostCode(tmpMgrPerson.getPostCode());
			flow.setShengheStep(flow.getDecionmaker().ordinal());
			flowDao.saveFlowShengheProperties(flow.getId(),
					tmpMgrPerson.getEmployeeId(), tmpMgrPerson.getDeptId(),
					tmpMgrPerson.getPostCode(), flow.getShengheStep());
		}
		flow.setSecondEmployeeId(mgrPerson.getEmployeeId());
		flow.setSecondDeptId(mgrPerson.getDeptId());
		flow.setSecondPostCode(mgrPerson.getPostCode());
		flowDao.saveFlowSecondProperties(flow.getId(),
				mgrPerson.getEmployeeId(), mgrPerson.getDeptId(),
				mgrPerson.getPostCode());
		//super.flowDao.updateFlow(flow);
		MyWork work = personDetail.buildWork(flow.getFlowType(),
				WorkStage.DIVISION_SIGN, null);
		work.setFlowId(flow.getId());
		work.setJoinCycle(work.getJoinCycle());
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("employeeId", personDetail.getEmployeeId());
		jsonObj.put("deptId", personDetail.getDeptId());
		JSONArray jsonArray = new JSONArray();
		jsonArray.add(jsonObj);

		work.setJoinSignStartId(jsonArray.toString());
		work.setJoinStartEmployeeId(work.getEmployeeId());
		flowDao.saveWork(work, flowService.getOrganDao());
		
		result.put(personDetail.getEmployeeId() + personDetail.getPostCode(),
				personDetail);

		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//System.out.println("SecondFinalSignatureFlowStageProcessor");
		return flow.getStatus() == FlowStatus.SECONDFINAL_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (!StringUtils.isEmpty(work.getJoinStartEmployeeId()) 
				&& !work.getEmployeeId().equals(work.getJoinStartEmployeeId())) {
			work.setStatus(FlowStatus.AGREE);
			this.flowService.updateWorkStatus(work);
			PersonDetail personDetail = personService.loadWidePersonDetail(
					work.getDeptId(), work.getEmployeeId(), work.getPostCode());
			PersonDetail mgrPersonDetail = personService
					.getMgrPersonDetail(personDetail);
			result.putAll(this.flowService.startNextWork(flow, mgrPersonDetail,
					work));
			return result;
		}

		work.setStatus(FlowStatus.APPROVED);
		this.flowDao.updateWork(work);
		
		flow.setStatus(FlowStatus.NEXTFINAL_DECISION_START);
		this.flowDao.updateFlow(flow);
		
		PersonDetail personDetail = personService.loadWidePersonDetailPlus(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		return result;
	}

	@Override
	boolean cancelValidate(Flow qianChenFlow) {
		// 最终和核决阶段就不能被撤销
		return false;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.SECONDFINAL_DECISION_START;
	}

}
