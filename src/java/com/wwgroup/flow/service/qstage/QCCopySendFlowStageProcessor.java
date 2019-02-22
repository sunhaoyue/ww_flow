package com.wwgroup.flow.service.qstage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.common.util.MailUtil;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.organ.bo.SystemGroups;

// 抄送人阶段，直接发抄送的work，可以附加对应的complete动作，与主业务流程没有直接影响
public class QCCopySendFlowStageProcessor extends QCAbstractFlowStageProcessor {

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
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + QCCopySendFlowStageProcessor.class.getName());
		//return flow.getStatus() == FlowStatus.COPY_SEND;
		return flow.getStatus() == FlowStatus.CC_End;
	}

	@SuppressWarnings("unused")
	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail, MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 以下两个List为方便在抄送时去除重复用户，避免同一用户收到多封邮件
		List<PersonDetail> sendMailDetails = new ArrayList<PersonDetail>();
		List<PersonDetail> tmpMailDetails = new ArrayList<PersonDetail>();
		String[] copyDeptIds = flow.getCopyDeptIds();
		List<MyWork> tmpWorks = new ArrayList<MyWork>();
		// TODO: 目前是发送给部门主管，具体还需和客户确认下
		if (copyDeptIds != null) {
			for (String copyDeptID : copyDeptIds) {
				SystemGroups tmpGroups = this.flowService.loadGroupsById(Integer.valueOf(copyDeptID));
				List<PersonDetail> mgrPersonDetails = null;
				if (tmpGroups.getSystemFlg().equals("Y")){
					mgrPersonDetails = super.personService.loadWideMgrPersonDetailsPlus(copyDeptID);
				} else { 
					mgrPersonDetails = super.personService.loadWideMgrPersonDetails(copyDeptID);
				}
				if (mgrPersonDetails != null && mgrPersonDetails.size() > 0){
					for (PersonDetail mgrPersonDetail : mgrPersonDetails) {
						// 作为已审核项目而不是待办工作项
						//MyWork work = mgrPersonDetail.buildWork(flow.getFlowType(), WorkStage.CHENGHE, prevWork);
						// 修改原来的呈核标识为抄送部门标识
						MyWork work = mgrPersonDetail.buildWork(flow.getFlowType(), WorkStage.COPYDEPT, prevWork);
						work.setStatus(FlowStatus.VIEW);
						
						// MyWork work = mgrPersonDetail.buildWork(flow.getFlowType(), WorkStage.COPYDEPT, null);
						work.setFlowId(flow.getId());
						
						// 获取当前处理人信息
						PersonDetail tmpPerson = personService.loadWidePersonDetail(
								work.getDeptId(), work.getEmployeeId(),
								work.getPostCode());
						work.setEmployeenam(tmpPerson.getName());
						work.setTitlenam(tmpPerson.getTitname());
						
						/**
						 * 保存前选判断是否已有同工号，同部门的记录，如果有，就不用再去保存了
						 */
						if (tmpWorks != null && tmpWorks.size() > 0){
							for(MyWork tmpWork : tmpWorks){
								if (tmpWork.getDeptId().equals(work.getDeptId()) && tmpWork.getEmployeeId().equals(work.getEmployeeId())){
								} else {
									flowDao.saveWork(work, flowService.getOrganDao());
									result.put(mgrPersonDetail.getEmployeeId() + mgrPersonDetail.getPostCode(), mgrPersonDetail);
									break;
								}
							}
						} else {
							flowDao.saveWork(work, flowService.getOrganDao());
							result.put(mgrPersonDetail.getEmployeeId() + mgrPersonDetail.getPostCode(), mgrPersonDetail);
						}
						tmpWorks.add(work);
						//send mail
						//MailUtil.mailToPerson(mgrPersonDetail, flow);
						sendMailDetails.add(mgrPersonDetail);
					}
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
			}
		}
		flow.setStatus(FlowStatus.FINAL_DECISION_END);
		this.flowDao.updateFlow(flow);
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + QCCopySendFlowStageProcessor.class.getName());
	//	return flow.getStatus() == FlowStatus.COPY_SEND;
		return flow.getStatus() == FlowStatus.CC_End;
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
	//	return flow.getStatus() == FlowStatus.COPY_SEND;
		return flow.getStatus() == FlowStatus.CC_End;
	}

}
