package com.wwgroup.flow.service.stage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.helper.JointSignType;
import com.wwgroup.flow.bo.work.CmpcodeJoinSignWork;
import com.wwgroup.flow.bo.work.MyWork;

@SuppressWarnings("unused")
public class InnerJointSignFlowStageProcessor extends AbstractFlowStageProcessor {

	@Override
	boolean startValidate(Flow flow) {
		//System.out.println("Start: " + this.getClass().getName());
		// 1.基本信息保存
		// 在没有内部会办的情况下，进入上级核程阶段
		return flow.getInnerJointSignIds() != null;
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
		
		flow.setStatus(FlowStatus.INNERJOINTSIGN_START);
		if (this.flowService.saveFlow(flow)) {
			// 添加发起人工作项(自动完成)
			PersonDetail actualPerson = flow.getActualPerson();
			if (!isRecreate || (isPreTemplate && !hasWorkRecord)) {
				// 添加发起人工作项(自动完成)
				MyWork createWork = actualPerson.buildWork(flow.getFlowType(), WorkStage.CREATE, null);
				createWork.setFlowId(flow.getId());
				flowDao.saveWork(createWork, flowService.getOrganDao());
				createWork.setStatus(flow.getRenewTimes() > 0 ? FlowStatus.RECREATE : FlowStatus.INIT);
				this.flowService.updateWorkStatus(createWork);
				
				// result.put(actualPerson.getEmployeeId() + actualPerson.getPostCode(), actualPerson);
			}

			// 跟据目前情况产生新的work然后发送给调用方
			// TODO: 后续会改进接口, （内部会办），先上报，然后会办，最后核准，（抄送单位），派发任务

			// 从innerJointDept列表中读取人员等标示 然后通过personService读取出人员信息，并且发给相关人员任务
			String[] personIds = flow.getInnerJointSignIds();
			for (int i = 0; i < personIds.length; i++) {
				String[] mixedValue = this.splitValue(personIds[i]);
				// TO DO
				PersonDetail personDetail =
						super.personService.loadWidePersonDetail(mixedValue[0], mixedValue[1], mixedValue[2]);
				result.putAll(this.flowService.startNextWork(flow, personDetail, null));
			}
		}
		return result;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail, MyWork prevWork) {
		// 跟一般的成核类似，只是普通的添加一个InnerJointWork而且这里没有 向上成核的过程，一步即可
		MyWork work = personDetail.buildWork(flow.getFlowType(), WorkStage.INNERJOINTSIGN, null);
		work.setFlowId(flow.getId());
		flowDao.saveWork(work, flowService.getOrganDao());
		
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		result.put(personDetail.getEmployeeId() + personDetail.getPostCode(), personDetail);
		return result;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.INNERJOINTSIGN_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		work.setStatus(FlowStatus.AGREE);
		this.flowDao.updateWork(work);

		// 决定流程是否变成 结束内部会签状态
		// 就是查阅 完成状态Aggree 内部会签的工作记录数，是否和记录的内部会签数组一致
		int workCount =
				super.flowDao.getWorkCount(flow, WorkStage.INNERJOINTSIGN, FlowStatus.AGREE, flow.getFlowType());
		if (flow.getInnerJointSignIds().length == workCount) {
			// 完成 内部会签
			// 暂时不需要InnerJointSignEnd状态使用的情况，无需判断
			/*
			 * 判断是否有同CMPCODE下的会办
			 * added by zhangqiang at 2012-11-20 16:30
			 */
			if(flow.getFlowType() == FlowType.QIANCHENG && !flow.isLocal()){//签呈&&地方到总部
				//中心审核开始
				flow.setStatus(FlowStatus.CENTER_CHENGHE_START);
			}else{
				flow.setStatus(FlowStatus.CHENGHE_START);
			}
			
			
			super.flowDao.updateFlow(flow);

			result.putAll(this.flowService.startNextWork(this.flowService.getFlow(work), this.flowService.getFlow(work)
					.getActualPerson(), null));
		}
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//System.out.println("InnerJointSignFlowStage");
		return flow.getStatus() == FlowStatus.INNERJOINTSIGN_START;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.INNERJOINTSIGN_START;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return false;
	}

}
