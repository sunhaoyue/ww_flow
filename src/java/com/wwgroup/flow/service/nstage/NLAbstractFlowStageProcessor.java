package com.wwgroup.flow.service.nstage;

import java.util.HashMap;
import java.util.Map;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.helper.JointSignType;
import com.wwgroup.flow.bo.work.JointSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.dao.FlowDao;
import com.wwgroup.flow.service.FlowStageProcessor;
import com.wwgroup.flow.service.NLFlowService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.user.service.UserService;


public abstract class NLAbstractFlowStageProcessor implements
		FlowStageProcessor {
	
	protected final Logger logger = Logger.getLogger(getClass());

	protected FlowStageProcessor nextFlowStageProcessor;

	protected PersonService personService;
	
	protected UserService userService;

	protected FlowDao flowDao;

	protected NLFlowService flowService;
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setFlowDao(FlowDao flowDao) {
		this.flowDao = flowDao;
	}

	public void setNextFlowStageProcessor(FlowStageProcessor nextFlowStageProcessor) {
		this.nextFlowStageProcessor = nextFlowStageProcessor;
	}

	public void setFlowService(NLFlowService flowService) {
		this.flowService = flowService;
	}

	protected String[] splitValue(String personId) {
		return StringUtils.split(personId, ",");
	}

	@Override
	public Map<String, PersonDetail> start(Flow flow) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);

		Flow qianChenFlow = (Flow) flow;
		if (startValidate(qianChenFlow)) {
			result.putAll(doStart(qianChenFlow));
		}
		else {
			if (this.nextFlowStageProcessor != null) {
				result.putAll(this.nextFlowStageProcessor.start(flow));
			}
		}
		return result;
	}

	@Override
	public Map<String, PersonDetail> startNextWork(Flow flow, PersonDetail personDetail, MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		Flow qianChenFlow = (Flow) flow;
		if (startNextValidate(qianChenFlow)) {
			result.putAll(doNext(qianChenFlow, personDetail, prevWork));
		}
		else {
			if (this.nextFlowStageProcessor != null) {
				result.putAll(this.nextFlowStageProcessor.startNextWork(flow, personDetail, prevWork));
			}
		}
		return result;
	}

	@Override
	public Map<String, PersonDetail> startNextInsideWork(Flow flow, PersonDetail personDetail, MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);

		Flow qianChenFlow = (Flow) flow;
		// if (startNextValidate(qianChenFlow)) {
		result.putAll(doNextInside(qianChenFlow, personDetail, prevWork));
		// }
		// else {
		// if (this.nextFlowStageProcessor != null) {
		// result.putAll(this.nextFlowStageProcessor.startNextWork(flow, personDetail, prevWork));
		// }
		// }
		return result;
	}

	@SuppressWarnings("unused")
	@Override
	public Map<String, PersonDetail> completeInsideWork(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 完成myWork然后判断 本身是不是joinSignStartId，如果是的话，那么就结束分支会办的工作了
		if (JointSignWork.class.isInstance(work)) {
			JointSignWork joinSignWork = (JointSignWork) work;
			work.setStatus(FlowStatus.AGREE);
			this.flowService.updateWorkStatus(joinSignWork);
			
			// 获取当前处理人信息
			PersonDetail tmpPerson = personService.loadWidePersonDetail(work.getEmployeeId());
			work.setEmployeenam(tmpPerson.getName());
			work.setTitlenam(tmpPerson.getTitname());
			this.flowDao.updateWorkOtherInfo(work);
			
			JointSignType jointSignType = flow.getJointSignType();

			if (!joinSignWork.getJoinSignStartId().equals(work.getEmployeeId())) {
				// 自动向其领导汇报, 因为指派的都是其下级，所以不会跳出这个最终startEmployeeId的
				PersonDetail personDetail =
						personService.loadWidePersonDetail(work.getDeptId(), work.getEmployeeId(), work.getPostCode());
				PersonDetail mgrPersonDetail = personService.getMgrPersonDetail(personDetail);

				result.put(mgrPersonDetail.getEmployeeId() + mgrPersonDetail.getPostCode(), personDetail);
				result.putAll(this.startNextInsideWork(flow, mgrPersonDetail, joinSignWork));
			}
			else {
				// 这里是结束会办分支，继续正常流传过程，等于说
				this.completeWork(flow, joinSignWork);
			}
		}
		return result;
	}

	private Map<? extends String, ? extends PersonDetail> doNextInside(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);

		MyWork work = personDetail.buildWork(flow.getFlowType(), WorkStage.JOINTSIGN, prevWork);
		result.put(personDetail.getEmployeeId() + personDetail.getPostCode(), personDetail);

		if (prevWork != null) {
			work.setWorknum(prevWork.getWorknum());
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			if (((JointSignWork) prevWork).getParentId() != 0) {
				((JointSignWork) work).setParentId(((JointSignWork) prevWork).getParentId());
			}
		}
		work.setFlowId(flow.getId());
		/*logger.info(flow.getFormNum() + " doNextInside指派：对创建的work对象进行保存操作，这里是insert. " + work.getEmployeeId() + "("
				+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
		flowDao.saveWork(work, flowService.getOrganDao());
		//logger.info(flow.getFormNum() + " doNextInside指派：对创建的work对象进行保存操作，这里是insert. End...");
		return result;
	}

	@Override
	public Map<String, PersonDetail> completeWork(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		Flow qianChenFlow = (Flow) flow;
		//logger.info(flow.getFormNum() + " 判断表单状态，确认执行的类.　当前步骤为：" + qianChenFlow.getStatus() + " Start...");
		if (completeValidate(qianChenFlow)) {
			result.putAll(doComplete(qianChenFlow, work));
		}
		else {
			if (this.nextFlowStageProcessor != null) {
				result.putAll(this.nextFlowStageProcessor.completeWork(flow, work));
			}
		}
		//logger.info(flow.getFormNum() + " 判断表单状态，确认执行的类. 当前步骤为：" + qianChenFlow.getStatus() + " End...");
		return result;
	}

	@Override
	public void cancel(MyWork myWork, Flow flow) {
		//if (cancelValidate(flow)) {
			doCancel(myWork, flow);
		//}
		//else {
		//	if (this.nextFlowStageProcessor != null) {
		//		this.nextFlowStageProcessor.cancel(flow);
		//	}
		//}
	}

	@Override
	public void reject(MyWork work) {
		Flow flow = this.flowService.getFlow(work);
		if (rejectValidate(flow, work)) {
			doReject(flow, work);
		}
		else {
			if (this.nextFlowStageProcessor != null) {
				this.nextFlowStageProcessor.reject(work);
			}
		}
	}

	protected void doReject(Flow flow, MyWork work) {
		// TODO 创建一个驳回的新任务给 实际申请人。
		flow.setStatus(FlowStatus.REJECT);
		flowDao.updateFlow(flow);

		work.setStatus(FlowStatus.REJECT);
		this.flowService.updateWorkStatus(work);

		PersonDetail personDetail = flow.getActualPerson();
		MyWork rejectWork = personDetail.buildWork(flow.getFlowType(), WorkStage.REJECT, null);
		rejectWork.setFlowId(flow.getId());
		
		// 获取当前处理人信息
		PersonDetail tmpPerson = personService.loadWidePersonDetail(rejectWork.getEmployeeId());
		rejectWork.setEmployeenam(tmpPerson.getName());
		rejectWork.setTitlenam(tmpPerson.getTitname());
		
		flowDao.saveWork(rejectWork, flowService.getOrganDao());
	}

	abstract boolean rejectValidate(Flow flow, MyWork work);

	// 撤销当前表单
	public void doCancel(MyWork myWork, Flow flow) {
		// 最高核决主管未核决前，只有申请人本人可撤销，其他人只能驳回。撤销是，邮件通知本人与上级主管。
		// (邮件发送通知，通知里面有表单号的链接，能够直接进入这个表单界面)
		// TODO: 后续再流程的各个点击步骤上加入撤销的验证，如果撤销的话，那么就不能执行正常流转工作了。
		myWork.setStatus(FlowStatus.CANCEL);
		this.flowService.updateWorkStatus(myWork);
		
		// 驳回后写入结案日期
		long endTime = System.currentTimeMillis();
		flow.setEndTime(endTime);
		flowDao.updateFlowEndTime(flow);
		
		/*flow.setStatus(FlowStatus.CANCEL);
		flowDao.updateFlow(flow);*/

		// 通知实际申请人 以及 其上级主管
//		PersonDetail personDetail = flow.getActualPerson();
//		MyWork cancelWork = personDetail.buildWork(flow.getFlowType(), WorkStage.CANCEL, null);
//		cancelWork.setFlowId(flow.getId());
//		flowDao.saveWork(cancelWork);
//
//		PersonDetail mgrPersonDetail = this.personService.getMgrPersonDetail(flow.getActualPerson());
//		cancelWork = mgrPersonDetail.buildWork(flow.getFlowType(), WorkStage.CANCEL, null);
//		cancelWork.setFlowId(flow.getId());
//		flowDao.saveWork(cancelWork);
	}

	// 结案当前表单
	public Flow endFlow(Flow flow) {
		flow.setStatus(FlowStatus.END);
		flowDao.updateFlow(flow);
		return flow;
	}

	// 取消工作是否可以接
	abstract boolean cancelValidate(Flow flow);

	// 完成当前工作
	abstract Map<String, PersonDetail> doComplete(Flow flow, MyWork work);

	// 完成工作是否可以接
	abstract boolean completeValidate(Flow flow);

	// 下一步做什么
	abstract Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail, MyWork prevWork);

	// 下一步工作是否可以接
	abstract boolean startNextValidate(Flow flow);

	// 开始阶段工作
	abstract Map<? extends String, ? extends PersonDetail> doStart(Flow flow);

	// 下一阶段工作是否可以接
	abstract boolean startValidate(Flow flow);

	public String getLatestJoinSignStartId(String joinSignStartId, String key) {
		JSONArray jsonArray = JSONArray.fromObject(joinSignStartId);
		JSONObject jsonObj = (JSONObject) jsonArray.get(jsonArray.size() - 1);
		return String.valueOf(jsonObj.get(key));
	}
	
	/**
	 * add by Cao_Shengyong 2014-03-25
	 * 
	 * @param joinSignStartId
	 * @return
	 */
	public String setHbJoinSignStartId(String joinSignStartId, String mgrEmployeeId){
		JSONArray jsonArray = JSONArray.fromObject(joinSignStartId);
		JSONObject jsonObj = jsonArray.getJSONObject(jsonArray.size() - 1);
		JSONObject retObj = new JSONObject();
		retObj.put("employeeId", mgrEmployeeId);
		retObj.put("deptId", jsonObj.getString("deptId"));
		JSONArray retArray = new JSONArray();
		retArray.add(retObj);
		return retArray.toString();
	}

}
