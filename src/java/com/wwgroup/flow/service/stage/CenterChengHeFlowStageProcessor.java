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
import com.wwgroup.flow.bo.helper.JointSignType;
import com.wwgroup.flow.bo.work.CmpcodeJoinSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.user.bo.EmployeePos;

public class CenterChengHeFlowStageProcessor extends AbstractFlowStageProcessor{
	
	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//System.out.println("completeValidate: CenterChengHeFlowStageProcessor");
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		//System.out.println("Center");
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (!StringUtils.isEmpty(work.getJoinSignStartId())) {
			work.setStatus(FlowStatus.AGREE);
			this.flowService.updateWorkStatus(work);
			PersonDetail personDetail =
					personService.loadWidePersonDetail(work.getDeptId(), work.getEmployeeId(), work.getPostCode());
			PersonDetail mgrPersonDetail = personService.getMgrPersonDetail(personDetail);
			result.putAll(this.flowService.startNextWork(flow, mgrPersonDetail, work));
			return result;
		}
		// 领导审核与传统的 会签等区分 所以这边用了 另外一个字段来表示
		work.setStatus(FlowStatus.APPROVED);
		this.flowDao.updateWork(work);
		//System.out.println(work.getDeptId());
		PersonDetail personDetail =
				personService.loadWidePersonDetail(work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(this.flowService.getFlow(work), personDetail, null));
		return result;
	}

	@SuppressWarnings("unused")
	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (prevWork != null && StringUtils.isEmpty(prevWork.getJoinSignStartId())) {
			MyWork work = personDetail.buildWork(flow.getFlowType(), WorkStage.CENTER_CHENGHE, prevWork);
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			work.setFlowId(flow.getId());
			work.setJoinCycle(work.getJoinCycle());
			flowDao.saveWork(work, flowService.getOrganDao());
			result.put(personDetail.getEmployeeId() + personDetail.getPostCode(), personDetail);
			return result;
		}
		//System.out.println(personDetail.getPostCode() + " # " + personDetail.getEmployeeId());
		//检查当前这个人是否是中心主管
		EmployeePos employee = userService.getEmployeePosByEmpId(personDetail.getEmployeeId());
		//System.out.println(1 + " ###################### " + employee.isCentermgr() + "##" + employee.getEmployeeid());
		if(employee.isCentermgr()){
			//是中心主管，直接进入下一个流程
			// 进入下一阶段
			if (!StringUtils.isEmpty(flow.getJointSignDeptName())) {
				flow.setStatus(FlowStatus.CMPCODEJOINTSIGN_START);
				super.flowDao.updateFlow(flow);
				result.putAll(this.delegateJointSignWork(flow,personDetail,prevWork));
			}
			else {
				PersonDetail mgrPerson = personService.getMgrPersonDetail(personDetail);
				flow.setStatus(FlowStatus.CHENGHE_START);
				super.flowDao.updateFlow(flow);
				//result.putAll(this.flowService.startNextWork(flow, mgrPerson, null));
				// 尝试修改传入的人员为当前人员，那么到呈核节点时可以判断通过上一级去判断是否副主管或主管
				// edit by Cao_Shengyong 2014-08-07
				result.putAll(this.flowService.startNextWork(flow, personDetail, null));
			}
		}else{
			//还是部门主管，再找中心主管
			PersonDetail mgrPerson = personService.getMgrPersonDetail(personDetail);
			EmployeePos mgrEmployee = userService.getEmployeePosByEmpId(mgrPerson.getEmployeeId());
			// 增加判断上一级是地方最高主管或最高副主管，则先会办
			// edit by Cao_Shengyong 2014-08-07
			if(mgrEmployee.isTopmgr() || mgrEmployee.isTopFmgr()){
				//此人是地方最高主管，先不让此人审核，进入本单位会办
				if (!StringUtils.isEmpty(flow.getJointSignDeptName())) {
					flow.setStatus(FlowStatus.CMPCODEJOINTSIGN_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.delegateJointSignWork(flow,personDetail,prevWork));
				}
				else {
					flow.setStatus(FlowStatus.CHENGHE_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow, personDetail, null));
				}
			}else{
				// 记录下该状态后，还是继续发请求给这个主管.
				MyWork work = mgrPerson.buildWork(flow.getFlowType(), WorkStage.CENTER_CHENGHE, null);
				work.setFlowId(flow.getId());
				flowDao.saveWork(work, flowService.getOrganDao());

				result.put(mgrPerson.getEmployeeId() + mgrPerson.getPostCode(), mgrPerson);
			}
			
		}

		return result;
	}

	@Override
	Map<? extends String, ? extends PersonDetail> doStart(Flow flow) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);

		boolean isRecreate = flow.getId() > 0;
		boolean hasWorkRecord = false;
		if (isRecreate) {
			hasWorkRecord = this.flowDao.listWorks(flow).length > 0;
		}
		boolean isPreTemplate = flow.isTempalte();
		boolean isSuccess = true;
		
		flow.setStatus(FlowStatus.CENTER_CHENGHE_START);
		if(flow.getInnerJointSignIds() == null){
			isSuccess = this.flowService.saveFlow(flow);
		}
		
		
		if (isSuccess) {
			PersonDetail actualPerson = flow.getActualPerson();
			// 新的表单 或者 是 暂存表单 而且没有 work记录 的 都可以进入
			if (!isRecreate || (isPreTemplate && !hasWorkRecord)) {
				// 添加发起人工作项(自动完成)
				MyWork createWork = actualPerson.buildWork(flow.getFlowType(), WorkStage.CREATE, null);
				createWork.setFlowId(flow.getId());
				flowDao.saveWork(createWork, flowService.getOrganDao());
				createWork.setStatus(flow.getRenewTimes() > 0 ? FlowStatus.RECREATE : FlowStatus.INIT);
				this.flowService.updateWorkStatus(createWork);

			}
			//执行doNext
			result.putAll(this.flowService.startNextWork(flow, actualPerson, null));
		}
		return result;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	boolean startValidate(Flow flow) {
		//System.out.println("Start: " + this.getClass().getName());
		return flow.getFlowType() == FlowType.QIANCHENG && !flow.isLocal();
	}

	private Map<String, PersonDetail> delegateJointSignWork(Flow flow,PersonDetail personDetail,MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 没有上一级领导了，那么就发起后续活动，可能是会签，或者其它的
		if (flow.getJointSignDeptIds() != null && flow.getJointSignDeptIds().length > 0) {
			// TODO: 直接进入下一个阶段：会办阶段, 需要再明确下会办的情况
			// 首先获取到flow对于会办的顺序还是同步的选择，然后根据记录的会办部门等情况，发出会办的任务给各方
			PersonDetail actualPerson = this.personService.loadWidePersonDetail(flow.getActualPerson().getEmployeeId());
			String[] jointSignDeptIds = flow.getJointSignDeptIds();
			if (JointSignType.SEQUENCE == flow.getJointSignType()) {
				//TODO 顺序会办后续补上
				// 顺序会办
				// 开始会办，读取第一个同CMPCODE的会办元素，进行转换后，得到相应的人员信息，并交由他来办理工作
				PersonDetail person = null;
				boolean found = false;
				for(int i=0;i<jointSignDeptIds.length;i++){
					String jointSignDeptId = jointSignDeptIds[i];
					person = personService.loadWideMgrPersonDetail(jointSignDeptId);
					if(person.getCmpCode().equals(actualPerson.getCmpCode())){
						found = true;
						break;
					}
				}
				if(!found){
					flow.setStatus(FlowStatus.CHENGHE_START);
					flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow, personDetail, null));
					return result;
				}
				
				CmpcodeJoinSignWork work = (CmpcodeJoinSignWork) person.buildWork(flow.getFlowType(), WorkStage.CMPCODEJOINTSIGN, null);

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("employeeId", person.getEmployeeId());
				jsonObj.put("deptId", person.getDeptId());
				JSONArray jsonArray = new JSONArray();
				jsonArray.add(jsonObj);

				work.setJoinSignStartId(jsonArray.toString());
				work.setWorknum(-1);
				work.setFlowId(flow.getId());
				work.setJoinCycle(jsonArray.size());
				work.setJoinStartEmployeeId(person.getEmployeeId());
				this.flowService.startNextWork(flow, person, work);

				result.put(person.getEmployeeId() + person.getPostCode(), person);
			}
			else if (JointSignType.CONCURRENT == flow.getJointSignType()) {
				// 同时会办
				int count = -1;
				for (int i = 0; i < jointSignDeptIds.length; i++) {
					String jointSignDeptId = jointSignDeptIds[i];
					// TODO: 这边需要询问清楚情况
					// TODO: 通过传入的信息找到部门主管
					PersonDetail person = personService.loadWideMgrPersonDetail(jointSignDeptId);
					if(person.getCmpCode().equals(actualPerson.getCmpCode())){
						CmpcodeJoinSignWork work =
							(CmpcodeJoinSignWork) person.buildWork(flow.getFlowType(), WorkStage.CMPCODEJOINTSIGN, null);

						JSONObject jsonObj = new JSONObject();
						jsonObj.put("employeeId", person.getEmployeeId());
						jsonObj.put("deptId", person.getDeptId());
						JSONArray jsonArray = new JSONArray();
						jsonArray.add(jsonObj);
		
						work.setJoinSignStartId(jsonArray.toString());
						work.setWorknum(work.getId() + count);
						work.setFlowId(flow.getId());
						work.setJoinCycle(jsonArray.size());
						work.setJoinStartEmployeeId(person.getEmployeeId());
						this.flowService.startNextWork(flow, person, work);
		
						result.put(person.getEmployeeId() + person.getPostCode(), person);
						
						count--;
					}
				}
				
				if(count == -1){
					//没有本单位会办
					flow.setStatus(FlowStatus.CHENGHE_START);
					flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow, personDetail, null));
				}

			}
		}
		return result;
	}
}
