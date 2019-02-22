package com.wwgroup.flow.service.nstage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.common.util.MailUtil;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.bo.work.XZJointSignWork;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.SystemGroups;

// 指派的逻辑(该逻辑在 地方到中心的 指派 是一致的): 在final work表里加入 jointStartEmployeeId字段，自己本身的work就填入这个值，然后可以指派
// 指派完成后，逐级上报，最终到了 jointStartEmployeeId == work内的employeeId. ，其实还是可以继续往下再进行指派的，
// 具体结束与否 也就是 确定 按钮 什么时候出现，视 jointStartEmployeeId == work内的employeeId决定。
public class NLFinalSignatureFlowStageProcessor extends
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
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + NLFinalSignatureFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.FINAL_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 核决主管 doNext 代码片断 Start...");
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (prevWork != null && StringUtils.isNotEmpty(prevWork.getJoinSignStartId())) {
			MyWork work = personDetail.buildWork(flow.getFlowType(), WorkStage.BOSS_SIGN, prevWork);
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
			
			/*logger.info(flow.getFormNum() + " 核决主管指派的流转过程,创建work对象并保存至数据库(insert.)"
					+ work.getEmployeeId() + "("
					+ work.getFlowId() + "、" + work.getDeptId() + ")" + "Start...");*/
			flowDao.saveWork(work, flowService.getOrganDao());
			//logger.info(flow.getFormNum() + " 核决主管指派的流转过程,创建work对象并保存至数据库. End...");
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

		// 获取上级主管
		PersonDetail mgrPerson = personService.getMgrPersonDetail(lastPerson);
		if (flow.getDecionmaker() == DescionMaker.DEPTLEADER && ! StringUtils.isEmpty(flow.getJointSignDeptName())){
			mgrPerson = lastPerson;
		}

		MyWork work = mgrPerson.buildWork(flow.getFlowType(),
				WorkStage.BOSS_SIGN, null);
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
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		work.setEmployeenam(tmpPerson.getName());
		work.setTitlenam(tmpPerson.getTitname());

		/*logger.info(flow.getFormNum() + " 核决主管：对创建的work对象进行保存操作，这里是insert. " + work.getEmployeeId() + "("
				+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
		flowDao.saveWork(work, flowService.getOrganDao());
		//logger.info(flow.getFormNum() + " 核决主管：对创建的work对象进行保存操作，这里是insert. End...");
		result.put(mgrPerson.getEmployeeId() + mgrPerson.getPostCode(),
				mgrPerson);
		//logger.info(flow.getFormNum() + " 核决主管 doNext 代码片断 End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + NLFinalSignatureFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.FINAL_DECISION_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 核决主管 doComplete 代码片断 Start...");
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
		work.setStatus(FlowStatus.AGREE);
		this.flowDao.updateWork(work);

		// 完成时，将当前审核用户信息写到主记录中
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		this.flowDao.updateFlowLastPerson(flow);
		
		//20170228
		PersonDetail tmpPersonx = new PersonDetail();
	     tmpPersonx.setEmployeeId("00000818");
	     
	     PersonDetail tmpPersony = new PersonDetail();
	     tmpPersony.setEmployeeId("00315767");
	     
	     PersonDetail tmpPersonz = new PersonDetail();
	     tmpPersonz.setEmployeeId("00001339");

	     if (((flow.getDecionmaker() == DescionMaker.UNITLEADER) || (flow.getDecionmaker() == DescionMaker.HEADLEADER) ) && (work.getEmployeeId().equals("91608004"))){
		    MailUtil.mailTotmpPerson(tmpPersonx, flow);
		    MailUtil.mailTotmpPerson(tmpPersony, flow);
			MailUtil.mailTotmpPerson(tmpPersonz, flow);
	     }
		//20170228
		// 完成时判断是否还存在会办，这里原则上剩余的都是总部行政职能部门
		if (flow.getPendJointSignDeptIds() != null && flow.getPendJointSignDeptIds().length > 0){
			flow.setStatus(FlowStatus.XZJOINTSIGN_START);
			super.flowDao.updateFlow(flow);
			PersonDetail personDetail = personService.loadWidePersonDetail(work.getDeptId(), work.getEmployeeId(), work.getPostCode());
			result.putAll(this.delegateJointSignWork(flow, personDetail));
			return result;
		} else {
			flow.setStatus(FlowStatus.CONFIRM_START);
			this.flowDao.updateFlow(flow);
		}

		PersonDetail personDetail = this.personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, work));
		//logger.info(flow.getFormNum() + " 核决主管 doComplete 代码片断 End...");
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
	
	private Map<String, PersonDetail> delegateJointSignWork(Flow flow, PersonDetail personDetail){
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		
		if (flow.getPendJointSignDeptIds() != null && flow.getPendJointSignDeptIds().length > 0){
			String[] jointSIgnDeptIds = flow.getPendJointSignDeptIds();
			int count = -1;
			String tmpNoSignDeptIds = "";
			int i = 0;
			for(i = 0; i < jointSIgnDeptIds.length; i++){
				String jointSignDeptId = jointSIgnDeptIds[i];
				SystemGroups tmpGroups = flowService.loadGroupsById(Integer.valueOf(jointSignDeptId));
				if (tmpGroups.getSystemFlg().equals("Y")){
					// 如果是行政职能部门，则去找对应的部门主管
					PersonDetail person = personService.loadWideMgrPersonDetailPlus(jointSignDeptId, GroupType.DEPTGROUP);
					// 会办主管为部门主管，但是签核记录中的组织信息还是用表单中所选择的会办单位
					person.setDeptName(tmpGroups.getGroupName());
					person.setDeptId(String.valueOf(tmpGroups.getGroupID()));
					person.setDeptCode(tmpGroups.getDepcod());
					person.setA_deptCode(tmpGroups.getA_depcod());
					person.setCmpCode(tmpGroups.getCmpcod());
					
					XZJointSignWork work = (XZJointSignWork) person.buildWork(flow.getFlowType(), WorkStage.XZJOINTSIGN, null);
					
					JSONObject json = new JSONObject();
					json.put("employeeId", person.getEmployeeId());
					json.put("deptId", person.getDeptId());
					JSONArray ja = new JSONArray();
					ja.add(json);
					
					work.setJoinSignStartId(ja.toString());
					work.setWorknum(work.getId() + i);
					work.setFlowId(flow.getId());
					work.setJoinCycle(ja.size());
					work.setJoinStartEmployeeId(person.getEmployeeId());
					
					this.flowService.startNextWork(flow, person, work);
					
					result.put(person.getEmployeeId() + person.getPostCode(), person);
					
					count--;
				} else {
					if (StringUtils.isEmpty(tmpNoSignDeptIds)){
						tmpNoSignDeptIds = jointSignDeptId;
					} else {
						tmpNoSignDeptIds += ";" + jointSignDeptId;
					}
				}
			}
			
			// 更新未会办单位列表字段
			super.flowDao.updateNoJointSignDeptIds(tmpNoSignDeptIds, flow);
			
			if (count == -1){
				flow.setStatus(FlowStatus.CONFIRM_START);
				flowDao.updateFlow(flow);
				result.putAll(this.flowService.startNextWork(flow, personDetail, null));
			}
		}
		
		return result;
	}
}
