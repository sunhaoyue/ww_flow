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
import com.wwgroup.user.bo.EmployeePos;

/**
 * 事业部副主管签核
 * 由于事业部副主管和主管都可以进行指派操作，所以分拆为两个节点。
 * @author eleven
 *
 */
public class QCNextBusinessFlowStageProcessor extends
		QCAbstractFlowStageProcessor {

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.NEXTBUSINESS_DECISION_START;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.NEXTBUSINESS_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 事业部副主管　doComplete 操作 Start...");
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
		
		// 完成时，将当前审核用户信息写到主记录中
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		// 此处注意需增加一个方法，用于更新以上数据.
		this.flowDao.updateFlowLastPerson(flow);
		
		PersonDetail personDetail = personService.loadWidePersonDetailPlus(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		//logger.info(flow.getFormNum() + " 事业部副主管　doComplete 操作 End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + QCNextBusinessFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.NEXTBUSINESS_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 事业部副主管　doNext 操作 Start...");
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (prevWork != null
				&& StringUtils.isNotEmpty(prevWork.getJoinSignStartId())) {
			MyWork work = personDetail.buildWork(flow.getFlowType(),
					WorkStage.FBUSINESS_SIGN, prevWork);
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
			/*logger.info(flow.getFormNum() + " 指派：对创建的work对象进行保存操作，这里是insert. " + work.getEmployeeId() + "("
					+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
			flowDao.saveWork(work, flowService.getOrganDao());
			//logger.info(flow.getFormNum() + " 指派：对创建的work对象进行保存操作，这里是insert. End...");
			result.put(
					personDetail.getEmployeeId() + personDetail.getPostCode(),
					personDetail);
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
		
		if (flow.isLocal()){
			/**
			 * 体系内
			 * 如果是体系内，本节点仅作为过渡，直接去下一个节点
			 */
			flow.setNextStep(FlowStatus.NEXTFINAL_DECISION_START);
			super.flowDao.updateFlowNextStep(flow);
			
			// 没有会办，直接去事业部主管节点。
			flow.setStatus(FlowStatus.BUSINESS_DECISION_START);
			super.flowDao.updateFlow(flow);
			result.putAll(this.flowService.startNextWork(flow,
					lastPerson, null));
		} else {
			/**
			 * 地方至总部
			 * 事业部的主管肯定是中心主管，所以判断上一步处理的人上级（上一级处理人肯定是地方的最高主管）
			 * 所以要先看这个上一级是否是最终的核决主管或副主管
			 * 表单中如果选择的核决主管是事业部主管，那么这里对应的上级是否是中心主管
			 * 表单中如果选择的核决主管是神旺控股最高主管，那么这里对应的上级是否是单位最高主管
			 */
			// 获取上级主管
			PersonDetail mgrPerson = personService
					.getMgrPersonDetail(lastPerson);
			EmployeePos mgrEmployee = userService.getEmployeePosByEmpId(
					mgrPerson.getEmployeeId(), mgrPerson.getPostCode());
			// 上级是否是核决主管
			boolean isApproval = personService.quailifiedDecisionMaker(flow.getDecionmaker(), mgrPerson);
			// 上级是否是核决副主管
			boolean isApprovalF = personService.quailifiedDecisionMakerPlus(flow.getDecionmaker(), mgrPerson);
			if (isApproval || isApprovalF || mgrEmployee.isTopFmgr() || mgrEmployee.isTopmgr()){
				// 记录下一步处理节点为核决副主管，并转至下一节点
				flow.setNextStep(FlowStatus.NEXTFINAL_DECISION_START);
				super.flowDao.updateFlowNextStep(flow);
				
				flow.setStatus(FlowStatus.BUSINESS_DECISION_START);
				super.flowDao.updateFlow(flow);
				result.putAll(this.flowService.startNextWork(flow,
						lastPerson, null));
			} else {
				// 判断上级是否是中心主管，即事业部主管
				if (mgrEmployee.isCentermgr()){
					flow.setNextStep(FlowStatus.BUSINESS_DECISION_START);
					super.flowDao.updateFlowNextStep(flow);
					
					// 直接转至事业部主管节点
					flow.setStatus(FlowStatus.BUSINESS_DECISION_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow,
							lastPerson, null));
				} else {
					// 继续呈核至中心副主管
					flow.setNextStep(FlowStatus.NEXTBUSINESS_DECISION_START);
					super.flowDao.updateFlowNextStep(flow);

					MyWork work = mgrPerson.buildWork(flow.getFlowType(),
							WorkStage.FBUSINESS_SIGN, null);
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
					/*logger.info(flow.getFormNum() + " 对创建的work对象进行保存操作，这里是insert. " + work.getEmployeeId() + "("
							+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
					flowDao.saveWork(work, flowService.getOrganDao());
					//logger.info(flow.getFormNum() + " 对创建的work对象进行保存操作，这里是insert. End...");
					result.put(
							mgrPerson.getEmployeeId() + mgrPerson.getPostCode(),
							mgrPerson);
				}
			}
		}
		//logger.info(flow.getFormNum() + " 事业部副主管　doNext 操作 End...");
		return result;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + QCNextBusinessFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.NEXTBUSINESS_DECISION_START;
	}

	@Override
	Map<? extends String, ? extends PersonDetail> doStart(Flow flow) {
		return new HashMap<String, PersonDetail>(2);
	}

	@Override
	boolean startValidate(Flow flow) {
		return false;
	}

}
