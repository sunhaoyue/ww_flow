package com.wwgroup.flow.service.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wwgroup.common.util.MailUtil;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;

// 抄送人阶段，直接发抄送的work，可以附加对应的complete动作，与主业务流程没有直接影响
public class CopySendFlowStageProcessor extends AbstractFlowStageProcessor {

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
		//System.out.println("StartNext: " + this.getClass().getName());
		return flow.getStatus() == FlowStatus.COPY_SEND;
	}

	@SuppressWarnings("unused")
	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail, MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 以下两个List为方便在抄送时去除重复用户，避免同一用户收到多封邮件
		List<PersonDetail> sendMailDetails = new ArrayList<PersonDetail>();
		List<PersonDetail> tmpMailDetails = new ArrayList<PersonDetail>();
		String[] copyDeptIds = flow.getCopyDeptIds();
		// TODO: 目前是发送给部门主管，具体还需和客户确认下
		if (copyDeptIds != null) {
			for (String copyDeptID : copyDeptIds) {
				List<PersonDetail> mgrPersonDetails = super.personService.loadWideMgrPersonDetails(copyDeptID);
				for (PersonDetail mgrPersonDetail : mgrPersonDetails) {
					// 作为已审核项目而不是待办工作项
					MyWork work = mgrPersonDetail.buildWork(flow.getFlowType(), WorkStage.CHENGHE, prevWork);
					work.setStatus(FlowStatus.AGREE);
					
					// MyWork work = mgrPersonDetail.buildWork(flow.getFlowType(), WorkStage.COPYDEPT, null);
					work.setFlowId(flow.getId());
					flowDao.saveWork(work, flowService.getOrganDao());
					result.put(mgrPersonDetail.getEmployeeId() + mgrPersonDetail.getPostCode(), personDetail);
										
					//send mail
					//MailUtil.mailToPerson(mgrPersonDetail, flow);
					sendMailDetails.add(mgrPersonDetail);
				}
			}
			// add by Cao_Shengyong 2014-04-11
			// 用于剔除待发送用户中存在的重复项
			if (sendMailDetails != null && sendMailDetails.size() > 0){
				for(int i = 0; i < sendMailDetails.size(); i++){
					for(int j = sendMailDetails.size() - 1; j > i; j--){
						if (sendMailDetails.get(j).getEmployeeId().equalsIgnoreCase(sendMailDetails.get(i).getEmployeeId())){
							sendMailDetails.remove(j);
						}
					}
				}
				if (sendMailDetails != null && sendMailDetails.size() > 0){
					for(PersonDetail sendMailPersonDetail : sendMailDetails){
						// send mail
						MailUtil.mailToPerson(sendMailPersonDetail, flow);
					}
				}
				/*for(PersonDetail tmpPersonDetail : sendMailDetails){
					if (!tmpMailDetails.contains(tmpPersonDetail)){
						tmpMailDetails.add(tmpPersonDetail);
					}
				}
				if (tmpMailDetails != null && tmpMailDetails.size() > 0){
					for(PersonDetail sendMailPersonDetail : tmpMailDetails){
						// send mail
						MailUtil.mailToPerson(sendMailPersonDetail, flow);
					}
				}*/
			}
		}
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		System.out.println("completeValidate: " + this.getClass().getName());
		return flow.getStatus() == FlowStatus.COPY_SEND;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		work.setStatus(FlowStatus.VIEW);
		this.flowService.updateWorkStatus(work);
		return new HashMap<String, PersonDetail>(2);
	}

	@Override
	boolean cancelValidate(Flow qianChenFlow) {
		return false;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.COPY_SEND;
	}

}
