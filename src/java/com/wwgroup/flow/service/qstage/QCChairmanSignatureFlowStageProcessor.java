package com.wwgroup.flow.service.qstage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.user.bo.EmployeePos;

/**
 * 董事长签核
 * 
 * 暂时不考虑董事长指派，因为目前组织架构中部分岗位的上级主管并不是董事长，而指派后的返回是按照汇报关系
 * 
 * @author eleven
 *
 */
public class QCChairmanSignatureFlowStageProcessor extends
		QCAbstractFlowStageProcessor {

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.CHAIRMANSIGN_START;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return false;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		
		work.setStatus(FlowStatus.AGREE);
		this.flowDao.updateWork(work);
		
		// 完成时，将当前审核用户信息写到主记录中
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		// 此处注意需增加一个方法，用于更新以上数据.
		this.flowDao.updateFlowLastPerson(flow);
		
		boolean toSubmitFBoss = false;
		if (!toSubmitFBoss){
			if (flow.isSubmitBoss() || flow.isSubmitFBoss()){
				toSubmitFBoss = true;
			}
		}
		// 董事长签核后直接至本人确认
		if (toSubmitFBoss){
			flow.setStatus(FlowStatus.SubmitFBossSIGN_START);
		} else {
		    flow.setStatus(FlowStatus.CONFIRM_START);
		}
		//flow.setStatus(FlowStatus.CONFIRM_START);
		this.flowDao.updateFlow(flow);
		
		// 完成时判断是否存在会办，这里原则上剩余的都是总部行政职能部门
		/*if (flow.getPendJointSignDeptIds() != null && flow.getPendJointSignDeptIds().length > 0){
			flow.setStatus(FlowStatus.XZJOINTSIGN_START);
			super.flowDao.updateFlow(flow);
			PersonDetail personDetail = personService.loadWidePersonDetail(work.getDeptId(), work.getEmployeeId(), work.getPostCode());
			result.putAll(this.delegateJointSignWork(flow, personDetail));
			return result;
		} else {
			flow.setStatus(FlowStatus.CONFIRM_START);
			this.flowDao.updateFlow(flow);
		}*/
		
		PersonDetail personDetail =
				this.personService.loadWidePersonDetail(work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(this.flowService.getFlow(work), personDetail, work));
		
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CHAIRMANSIGN_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		EmployeePos chairman = null;
		chairman = userService.getChairmanEmployeePos();
		if (chairman == null){
			throw new RuntimeException("董事长信息维护有误，请与人力资源处或系统管理员联系！");
		}
		PersonDetail person = null;
		person = personService.loadWidePersonDetail(chairman.getEmployeeid(), chairman.getPostcode());
		if (person == null){
			throw new RuntimeException("董事长信息维护有误，请与人力资源处或系统管理员联系！");
		}
		
		MyWork work = person.buildWork(flow.getFlowType(), WorkStage.CHAIRMAN_SIGN, null);
		work.setFlowId(flow.getId());
		work.setJoinCycle(work.getJoinCycle());
		
		JSONObject json = new JSONObject();
		json.put("employeeId", person.getEmployeeId());
		json.put("deptId", person.getDeptId());
		JSONArray ja = new JSONArray();
		ja.add(json);
		
		work.setJoinSignStartId(ja.toString());
		work.setJoinStartEmployeeId(work.getEmployeeId());
		
		work.setEmployeenam(person.getName());
		work.setTitlenam(person.getTitname());
		flowDao.saveWork(work, flowService.getOrganDao());
		
		result.put(person.getEmployeeId() + person.getPostCode(), person);
		
		return result;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CHAIRMANSIGN_START;
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
