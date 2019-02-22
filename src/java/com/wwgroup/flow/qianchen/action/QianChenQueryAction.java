package com.wwgroup.flow.qianchen.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.wwgroup.common.action.BaseAjaxAction;
import com.wwgroup.common.vo.VOUtils;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowAttachment;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.dto.MyWorkHistory;
import com.wwgroup.flow.qianchen.vo.FlowAttachmentVo;
import com.wwgroup.flow.qianchen.vo.MyWorkHistoryVo;
import com.wwgroup.flow.service.FlowService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.service.OrganService;
import com.wwgroup.user.bo.EmployeePos;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

@SuppressWarnings({"serial", "unused"})
public class QianChenQueryAction extends BaseAjaxAction {

	private Flow flow = new Flow();

	private FlowService flowService;

	private UserService userService;

	private OrganService organService;

	private PersonService personService;

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setOrganService(OrganService organService) {
		this.organService = organService;
	}

	private static final String prefix = "{\"success\":true,result:";

	private static final String suffex = "}";

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}

	public Flow getQianChenFlow() {
		return flow;
	}

	public void setQianChenFlow(Flow qianChenFlow) {
		this.flow = qianChenFlow;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public Object getModel() {
		return this.flow;
	}

	// 获取流程对应的附件列表
	public String loadAttachments() {
		String formNum = (String) this.servletRequest.getParameter("formNum");
		flow = flowService.loadFlow(formNum);
		FlowAttachment[] attachments = flow.getFlowAttachments();
		List<FlowAttachmentVo> result = new ArrayList<FlowAttachmentVo>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (attachments != null) {
			for (FlowAttachment flowAttachment : attachments) {
				FlowAttachmentVo attachmentVo = new FlowAttachmentVo();
				attachmentVo.setId(flowAttachment.getId());
				attachmentVo.setAttachmentName(flowAttachment.getAttachmentName());
				attachmentVo.setEmployeeId(flowAttachment.getEmployeeId());
				attachmentVo.setUserName(flowAttachment.getEmployeeName());
				attachmentVo.setCreateTime(sdf.format(new Date(flowAttachment.getCreateTime())));
				result.add(attachmentVo);
			}
		}
		String json = VOUtils.getJsonDataFromCollection(result);
		createJSonData(json);
		return AJAX;
	}

	// 获取流程对应的附件列表
	public String listWorkHistory() {
		String formNum = (String) this.servletRequest.getParameter("formNum");
		String workId = (String) this.servletRequest.getParameter("workId");
		//根据workId取parentId
		
		MyWorkHistory[] workHistories;
		List<MyWorkHistoryVo> result = new ArrayList<MyWorkHistoryVo>();
		if (StringUtils.isNotEmpty(workId)) {
			long actWorkId = Long.parseLong(workId);
			MyWork myWork = flowService.loadWork(actWorkId);
			if(myWork.getParentId() != 0 && StringUtils.isNotBlank(myWork.getJoinStartEmployeeId())){
				actWorkId = myWork.getParentId();
			}
			workHistories = flowService.listWorkHistory(formNum, actWorkId);
		} else {
			workHistories = flowService.listWorkHistory(formNum);
		}
		if (workHistories != null) {
			for (int i = 0; i < workHistories.length; i++) {
				MyWorkHistoryVo historyVo = new MyWorkHistoryVo();
				BeanUtils.copyProperties(workHistories[i], historyVo);
				String employeeId = workHistories[i].getProcessManName();
				String deptId = workHistories[i].getDeptId();
				SystemUsers user = userService.getUsersByName(employeeId);
				String orgPath = workHistories[i].getOrgpath();
				// 默认签核记录中的组织全路径为空,则实时根据部门ID获取对应信息
				if (StringUtils.isEmpty(orgPath)){
					if (StringUtils.isNotEmpty(deptId)) {
						try{
							SystemGroups group = organService.loadGroupsById(Integer.valueOf(deptId));
							if (group != null) {
								orgPath = group.getOrgPath();
							}
						}catch(Exception e){
						
						}
					}
				}
				//System.out.println(orgPath);
				if (StringUtils.isNotEmpty(orgPath)){
					historyVo.setDeptName(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				//historyVo.setDeptName(orgPath);
				historyVo.setDeptId(deptId);
				historyVo.setEmployeeId(employeeId);
				String fullName = user.getFullName();

				String realName = "";
				if (StringUtils.isNotEmpty(fullName)) {
					realName = fullName.substring(0, fullName.indexOf("("));
				}

				String delegateKeyWords = "(代)";
				if (!StringUtils.isEmpty(workHistories[i].getDlg_employeeid())
						&& !employeeId.equals(workHistories[i].getDlg_employeeid())
						&& !workHistories[i].getPostCode().equals(workHistories[i].getDlg_postcode())) {
					// 这里的work里面还要记录代理人的姓名
					SystemUsers dlgUser = userService.getUsersByName(workHistories[i].getDlg_employeeid());// workHistories[i].getDlg_employeeid()

					String dlgRealName = "";
					if (StringUtils.isNotEmpty(dlgUser.getFullName())) {
						dlgRealName = dlgUser.getFullName().substring(0, dlgUser.getFullName().indexOf("("));
					}
//					String postName =
//							personService.loadWidePersonDetail(workHistories[i].getDeptId(), employeeId,
//									workHistories[i].getPostCode()).getPostName();
//					String dlgPostName =
//							personService.loadWidePersonDetail(workHistories[i].getDlgDeptId(),
//									workHistories[i].getDlg_employeeid(), workHistories[i].getDlg_postcode())
//									.getPostName();
					// 改成职务名称了
					String postName = userService.getUsersByName(employeeId).getTitnam();
					String dlgPostName = userService.getUsersByName(workHistories[i].getDlg_employeeid()).getTitnam();
					
					StringBuffer delegetProcessManName = new StringBuffer(dlgRealName != null ? dlgRealName : "");
					delegetProcessManName.append("(");
					delegetProcessManName.append(workHistories[i].getDlg_employeeid() != null ? workHistories[i]
							.getDlg_employeeid() : "");
					delegetProcessManName.append(")");

					delegetProcessManName.append(dlgPostName != null ? dlgPostName : "");
					delegetProcessManName.append(delegateKeyWords);

					// 审核人(ProcessManName)：张三(员工号)职位(代)
					historyVo.setUserRealName(delegetProcessManName.toString());

					// (代) 审核
					if (workHistories[i].getHbAgree().equals("2")){
						historyVo.setWorkStatus(delegateKeyWords + " 不同意");
					} else {
						historyVo.setWorkStatus(delegateKeyWords + " " + workHistories[i].getStatus().getDisplayName());
					}

					StringBuffer dlg_opionin = new StringBuffer(delegateKeyWords);
					dlg_opionin.append(realName);
					dlg_opionin.append("(");
					dlg_opionin.append(employeeId);
					dlg_opionin.append(")");
					dlg_opionin.append(postName != null ? postName : "");
					dlg_opionin.append(": ");
					// (代)李四(员工号)职位: 正文
					historyVo.setOpinion(dlg_opionin.toString()
							+ (workHistories[i].getOpinion() != null ? workHistories[i].getOpinion() : ""));
				} else {
					if (StringUtils.isNotEmpty(fullName)) {
						// realName =
						// fullName.substring(0,fullName.indexOf("("));
						historyVo.setUserRealName(fullName + user.getTitnam());
					}
				}
				result.add(historyVo);
			}
		}
		String json = VOUtils.getJsonDataFromCollection(result);
		createJSonData(json);
		return AJAX;
	}

	public String judgeDecisionMaker() {
		String deptId = this.servletRequest.getParameter("deptId");
		String employeeId = this.servletRequest.getParameter("employeeId");
		String postCode = this.servletRequest.getParameter("postCode");
		if (StringUtils.isNotEmpty(employeeId) && StringUtils.isNotEmpty(postCode)) {

			// 获取人员关系
			EmployeePos pos = userService.getEmployeePosByEmpId(employeeId, postCode);
			// 部门主管
			if (pos.isDeptmgr()) {
				this.servletRequest.setAttribute("deptmgr", 1);
			}
			//
			if (pos.isCentermgr()) {
				this.servletRequest.setAttribute("centermgr", 1);
			}
			if (pos.isTopmgr()) {
				this.servletRequest.setAttribute("topmgr", 1);
			}
			String json = VOUtils.getJsonData(pos);
			createJSonData(json);
		}
		return AJAX;
	}
}
