package com.wwgroup.flow.service.qstage;

import java.util.HashMap;
import java.util.Map;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;

/**
 * 签呈内部会办（指定至人）
 * @author eleven
 *
 */
public class QCInnerJointSignFlowStageProcessor extends QCAbstractFlowStageProcessor {

	@Override
	boolean startValidate(Flow flow) {
		// 1.基本信息保存
		// 在没有内部会办的情况下，进入上级核程阶段
		//logger.info(flow.getFormNum() + " startValidate: " + QCInnerJointSignFlowStageProcessor.class.getName());
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
		
		// 开始前将用户选择的会办列表赋值给未完成会办列表字段中
		// 存在会办才去做这个操作
		/*if (!StringUtils.isEmpty(flow.getJointSignDeptName())) {
			String tmpNoJointSignDeptId = "";
			String[] jointSignDeptIds = flow.getJointSignDeptIds();
			for(int i = 0; i < jointSignDeptIds.length; i++){
				String jointSignDeptId = jointSignDeptIds[i];
				if (StringUtils.isEmpty(tmpNoJointSignDeptId)){
					tmpNoJointSignDeptId = jointSignDeptId;
				} else {
					tmpNoJointSignDeptId += ";" + jointSignDeptId;
				}
			}
			flow.setPendJointSignDeptIds(jointSignDeptIds);
			flowDao.updateNoJointSignDeptIds(tmpNoJointSignDeptId, flow);
		}*/
		
		if (this.flowService.saveFlow(flow)) {
			// 添加发起人工作项(自动完成)
			PersonDetail actualPerson = flow.getActualPerson();
			
			// 开始前将申请人信息写入临时字段
			flow.setLastEmployeeId(actualPerson.getEmployeeId());
			flow.setLastDeptId(actualPerson.getDeptId());
			flow.setLastPostCode(actualPerson.getPostCode());
			flowDao.updateFlowLastPerson(flow);
			
			if (!isRecreate || (isPreTemplate && !hasWorkRecord)) {
				// 添加发起人工作项(自动完成)
				MyWork createWork = actualPerson.buildWork(flow.getFlowType(), WorkStage.CREATE, null);
				createWork.setFlowId(flow.getId());
				
				// 获取当前处理人信息
				PersonDetail tmpPerson = personService.loadWidePersonDetail(
						createWork.getDeptId(), createWork.getEmployeeId(),
						createWork.getPostCode());
				createWork.setEmployeenam(tmpPerson.getName());
				createWork.setTitlenam(tmpPerson.getTitname());
				
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
		
		// 获取当前处理人信息
		PersonDetail tmpPerson = personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(),
				work.getPostCode());
		work.setEmployeenam(tmpPerson.getName());
		work.setTitlenam(tmpPerson.getTitname());
		
		flowDao.saveWork(work, flowService.getOrganDao());
		
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		result.put(personDetail.getEmployeeId() + personDetail.getPostCode(), personDetail);
		return result;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + QCInnerJointSignFlowStageProcessor.class.getName());
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
			flow.setStatus(FlowStatus.DEPT_CHENGHE_START);
			
			super.flowDao.updateFlow(flow);

			result.putAll(this.flowService.startNextWork(this.flowService.getFlow(work), this.flowService.getFlow(work)
					.getActualPerson(), null));
		}
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + QCInnerJointSignFlowStageProcessor.class.getName());
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
