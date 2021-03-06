package com.wwgroup.flow.neilian.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.mail.MailPreparationException;

import com.wwgroup.common.action.BaseAjaxAction;
import com.wwgroup.common.util.MailUtil;
import com.wwgroup.common.vo.VOUtils;
import com.wwgroup.flow.bo.AgentPerson;
import com.wwgroup.flow.bo.AgentRelation;
import com.wwgroup.flow.bo.AssistRelation;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowAttachment;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyDelegateWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.dto.WorkRole;
import com.wwgroup.flow.service.FlowAgentService;
import com.wwgroup.flow.service.FlowAssistService;
import com.wwgroup.flow.service.FlowService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.user.bo.EmployeePos;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

public class NeiLianEditAcion extends BaseAjaxAction {

	private static final long serialVersionUID = -5061533437038476980L;

	private Flow flow = new Flow();

	private PersonService personService;

	private FlowService flowService;
	
	private UserService userService;
	
	private FlowAgentService flowAgentService;
	
	private FlowAssistService flowAssistService;


	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}
	
	public void setFlowAgentService(FlowAgentService flowAgentService) {
		this.flowAgentService = flowAgentService;
	}
	
	public void setFlowAssistService(FlowAssistService flowAssistService) {
		this.flowAssistService = flowAssistService;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	@Override
	public Object getModel() {
		return this.flow;
	}

	/**
	 * 根据审核人加载内联信息
	 * 
	 * @return
	 */
	public String loadNeiLianDetail() {
		
		String formNum = (String) this.servletRequest.getParameter("formNum");
		String workId = (String) this.servletRequest.getParameter("workId");
		String deptId = (String) this.servletRequest.getParameter("deptId");
		String role = (String) this.servletRequest.getParameter("role");
		
		this.addOperatorInfo();

		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		String name = super.getLoginUser().getUserRealName();
		this.servletRequest.setAttribute("loginId", employeeId);
		this.servletRequest.setAttribute("loginName", name);

		// 加载流程详情
		flow = flowService.loadFlow(formNum);
		MyWork myWork = null;
		if (StringUtils.isNotEmpty(workId)) {
			myWork = flowService.loadWork(Long.valueOf(workId));
			if (myWork != null) {
				this.servletRequest.setAttribute("workStage", myWork.getWorkStage().name());
				boolean isFHeader = false;
				if (myWork.getWorkStage().equals(WorkStage.DIVISION_SIGN)){
					isFHeader = personService.isCenterFHead(myWork.getEmployeeId(), myWork.getPostCode());
				}
				this.servletRequest.setAttribute("isFHeader", isFHeader);
				this.servletRequest.setAttribute("opinion", myWork.getOpinion());
				
				//检查当前操作人是否是本人或是代理人
				String curEmployeeId = (String) getSession().get("employeeId");
				
				AgentRelation agentRelation = flowAgentService.loadAgent(myWork.getEmployeeId());
				boolean agented = false;
				if(null != agentRelation){
					List<AgentPerson> agentPersons = flowAgentService.loadAgentPersons(agentRelation);
					for(AgentPerson agentPerson : agentPersons){
						if(agentPerson.getAgentEmployeeId().equals(curEmployeeId)){
							agented = true;
							break;
						}
					}
				}
				
				//检查当前操作人是否是助理人
				List<AssistRelation> assistRelationList = flowAssistService.findAll(myWork.getEmployeeId());
				boolean assist = false;
				for(AssistRelation assistRelation : assistRelationList){
					if(curEmployeeId.equals(assistRelation.getSelectedAssistEmployeeId())){
						assist = true;
						break;
					}
				}
				this.servletRequest.setAttribute("assist", assist);
				
				if(curEmployeeId.equals(myWork.getEmployeeId()) || agented){
					this.servletRequest.setAttribute("curEmployee", true);
				}else{
					this.servletRequest.setAttribute("curEmployee", false);
				}
				EmployeePos employeePos = personService.loadWideEmployeePos(myWork.getEmployeeId(), myWork.getPostCode());
				if ((curEmployeeId.equals(myWork.getJoinStartEmployeeId()) && !agented)
						|| (agented && !myWork.getHb_ChengHe().equals("2"))) {
					if (employeePos != null){
						// 当前用户本身就是最高主管了
						if (employeePos.getMgCentFlg() == 1){
							this.servletRequest.setAttribute("hbMgrUser", false);
						} else {
							this.servletRequest.setAttribute("hbMgrUser", true);
						}
					} else {
						this.servletRequest.setAttribute("hbMgrUser", true);
					}
				} else {
					this.servletRequest.setAttribute("hbMgrUser", false);
				}
				//System.out.println(myWork.getEmployeeId() + "#" + myWork.getHb_ChengHe());
				if (myWork.getHb_ChengHe().equals("2")){
					this.servletRequest.setAttribute("hbMgrCanSign", false);
				} else {
					this.servletRequest.setAttribute("hbMgrCanSign", true);
				}
				
				this.servletRequest.setAttribute("hbChengHe", myWork.getHb_ChengHe());
				
				if(flow.getStatus() != FlowStatus.INNERJOINTSIGN_START 
						&& flow.getStatus() != FlowStatus.JOINTSIGN_START
						&& flow.getStatus() != FlowStatus.CMPCODEJOINTSIGN_START
						&& flow.getStatus() != FlowStatus.CONFIRM_START
						&& flow.getStatus() != FlowStatus.REJECT
						){
					//非会办，可以驳回
					if(flow.getStatus() == FlowStatus.FINAL_DECISION_START){
						if(myWork.getEmployeeId().equals(myWork.getJoinStartEmployeeId())){
							this.servletRequest.setAttribute("canRefuse", true);
						}else{
							this.servletRequest.setAttribute("canRefuse", false);
						}
					}else{
						this.servletRequest.setAttribute("canRefuse", true);
					}
				}else{
					this.servletRequest.setAttribute("canRefuse", false);
				}
				
				// 获得指派需要过滤的部门id
				String joinSignStartId = myWork.getJoinSignStartId();
				if (flow.getStatus().equals(FlowStatus.JOINTSIGN_START) && StringUtils.isNotEmpty(joinSignStartId)) {
					JSONArray jsonArray = JSONArray.fromObject(joinSignStartId);
					if (jsonArray.size() > 0) {
						JSONObject jsonObj = jsonArray.getJSONObject(jsonArray.size() - 1);
						deptId = String.valueOf(jsonObj.get("deptId"));
					}
				}
				
				String workStatus_str = myWork.getWorkStage().name();
				boolean disp_agree = false;
				// 如果是会办
				if (StringUtils.isNotEmpty(workStatus_str)) {
					//System.out.println("@@" + workStatus_str.equals(WorkStage.JOINTSIGN.name()));
					if (workStatus_str.equals(WorkStage.JOINTSIGN.name())
							|| workStatus_str
									.equals(WorkStage.INNERJOINTSIGN.name())
							|| workStatus_str
									.equals(WorkStage.CMPCODEJOINTSIGN.name())) {
						//System.out.println("##");
						//System.out.println(curEmployeeId + " # " + myWork.getJoinStartEmployeeId());
						if ((curEmployeeId.equals(myWork.getJoinStartEmployeeId()) || agented)
								|| (myWork.getHb_ChengHe().equals("2"))) {
							disp_agree = true;
						}
					}
				}
				//System.out.println(agented);
				//System.out.println(disp_agree);
				this.servletRequest.setAttribute("dispAgree", disp_agree);
				
				if (flow.getStatus().equals(FlowStatus.JOINTSIGN_START) 
						|| flow.getStatus().equals(FlowStatus.INNERJOINTSIGN_START)
						|| flow.getStatus().equals(FlowStatus.SECONDFINAL_DECISION_START)  
						|| flow.getStatus().equals(FlowStatus.CMPCODEJOINTSIGN_START)) {
					this.servletRequest.setAttribute("deptName", myWork.getDeptName());
					
					MyWork prevWork = flowService.getPrevWork(myWork);
					if(null != prevWork 
							&& !myWork.getEmployeeId().equals(prevWork.getEmployeeId())){
						this.servletRequest.setAttribute("opinion", prevWork.getOpinion());
					}
				}
				
//				if (flow.getStatus().equals(FlowStatus.JOINTSIGN_START) || flow.getStatus().equals(FlowStatus.INNERJOINTSIGN_START)){
//					this.servletRequest.setAttribute("deptName", myWork.getDeptName());
//				}
			}
		}
		String deptPath = flow.getCreatePerson().getDeptPath();
		splitDeptPath(deptPath);

		// 会办单位、抄送单位加载
		this.loadDeptInfo();

		// 供页面判断流程单状态
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String createTime = sdf.format(new Date(flow.getCreateTime()));
		this.servletRequest.setAttribute("createTime", createTime);
		this.servletRequest.setAttribute("flowStatus", flow.getStatus().name());
		this.servletRequest.setAttribute("leader", flow.getDecionmaker().toString());
		this.servletRequest.setAttribute("joinType", flow.getJointSignType().toString());
		this.servletRequest.setAttribute("renewTimes", flow.getRenewTimes());
		boolean selfconfirm = flow.isSelfConfirm();
		if (selfconfirm) {
			this.servletRequest.setAttribute("confirm", 1);
		}
		else {
			this.servletRequest.setAttribute("confirm", 0);
		}
		this.servletRequest.setAttribute("workId", workId);
		this.servletRequest.setAttribute("deptId", deptId);
		this.servletRequest.setAttribute("role", role);

		String formEmployeeId = null;
		if (flow.getFlowType() == FlowType.QIANCHENG) {
			formEmployeeId = flow.getActualPerson().getEmployeeId();
		}
		else {
			formEmployeeId = flow.getCreatePerson().getEmployeeId();
		}
		boolean showCancelButton = (employeeId.equals(formEmployeeId) && StringUtils.isEmpty(workId));
		showCancelButton &= flow.getStatus() != FlowStatus.FINAL_DECISION_END;
		showCancelButton &= flow.getStatus() != FlowStatus.COPY_SEND;
		showCancelButton &= flow.getStatus() != FlowStatus.CANCEL;
		showCancelButton &= flow.getStatus() != FlowStatus.REJECT;
		showCancelButton &= flow.getStatus() != FlowStatus.RECREATE;
		showCancelButton &= flow.getStatus() != FlowStatus.END;
		this.servletRequest.setAttribute("showCancelButton", (showCancelButton) ? 1 : 0);
		boolean isHQTR = false;
		if (userService.getUsersByName(formEmployeeId).getCmpcod().equalsIgnoreCase("HQTR")){
			isHQTR = true;
		}
		this.servletRequest.setAttribute("isHQTR", isHQTR);

		return "neilianDetail";

	}

	/**
	 * 加载暂存表单信息
	 * 
	 * @return
	 */
	public String loadTemplateNeiLianDetail() {
		String formNum = (String) this.servletRequest.getParameter("formNum");

		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		String name = super.getLoginUser().getUserRealName();
		this.servletRequest.setAttribute("loginId", employeeId);
		this.servletRequest.setAttribute("loginName", name);

		// 加载流程详情
		flow = flowService.loadFlow(formNum);

		String deptPath = flow.getCreatePerson().getDeptPath();
		splitDeptPath(deptPath);
		
		boolean isHQTR = false;
		if (userService.getUsersByName(employeeId).getCmpcod().equalsIgnoreCase("HQTR")){
			isHQTR = true;
		}
		this.servletRequest.setAttribute("isHQTR", isHQTR);

		// 会办单位、抄送单位加载
		this.loadDeptInfo();

		// 供页面判断流程单状态
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String createTime = sdf.format(new Date(flow.getCreateTime()));
		this.servletRequest.setAttribute("createTime", createTime);
		this.servletRequest.setAttribute("leader", flow.getDecionmaker().toString());
		this.servletRequest.setAttribute("joinType", flow.getJointSignType().toString());
		boolean selfconfirm = flow.isSelfConfirm();
		if (selfconfirm) {
			this.servletRequest.setAttribute("confirm", 1);
		}
		else {
			this.servletRequest.setAttribute("confirm", 0);
		}

		return "neilianTemplate";

	}

	/**
	 * 同意审核/会办/内部会办
	 * 
	 * @return
	 */
	public String confirmWork() {
		try {
			String workId = (String) this.servletRequest.getParameter("workId");
			String role = (String) this.servletRequest.getParameter("role");
			// 获取登录用户
			String employeeId = super.getLoginUser().getUserName();

			if (StringUtils.isNotEmpty(workId)) {
				// 当前工作项
				MyWork myWork = flowService.loadWork(Long.valueOf(workId));

				// 当前操作人信息
				// PersonDetail person = personService.loadWidePersonDetail(myWork.getDeptId(), myWork.getEmployeeId(), myWork.getPostCode());

				// 建议方案
				String opinion = (String) this.servletRequest.getParameter("opinion");
				myWork.setOpinion(opinion);
				// 是否需要呈核
				// add by Cao_Shengyong 2014-03-24
				String hbChengHe = (String)this.servletRequest.getParameter("hbChengHe");
				myWork.setHb_ChengHe(hbChengHe);
				//System.out.println(myWork.getHb_ChengHe());
				// 写入会办主管是否同意状态
				String hbAgree = (String) this.servletRequest
						.getParameter("hb_isagree");
				if (StringUtils.isNotEmpty(hbAgree)) {
					myWork.setHb_Agree(hbAgree);
				}

				// 判断是否需要更新流程信息（会办单位、抄送单位、附件）
				String updateFlag = (String) this.servletRequest.getParameter("updateFlag");
				flow = flowService.getFlow(myWork);
				if (StringUtils.isNotEmpty(updateFlag) && updateFlag.equals("1")) {
					this.updateFlowInfo();
				}
				Map<String, PersonDetail> map = new HashMap<String, PersonDetail>();
				// 提交流程(如果是代理或助理，则附加代理人信息)
				if (StringUtils.isNotEmpty(role) && !role.equals(WorkRole.MYSELF.toString())
						&& StringUtils.isNotEmpty(employeeId)) {
					// 代理或者助理并允许指派
					if (role.equals(WorkRole.AGENT.toString()) || role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
						MyDelegateWork delegateWork = this.setDelegateWorkInfo(employeeId, myWork);
						map = flowService.completeWork(delegateWork);
					}
				}
				else {
					map = flowService.completeWork(myWork);
				}
				// 获得更新后的工作项（主要是流程状态）
				flow = flowService.getFlow(myWork);
				// 流程状态
				FlowStatus status = flow.getStatus();

				// 下一个工作人信息（格式为：姓名（工号））
				PersonDetail nextPerson = null;
				for (String key : map.keySet()) {
					nextPerson = map.get(key);
					break;
				}
				
				//检查下一个处理人的邮箱
				StringBuffer mailMessages = new StringBuffer();
				for (String key : map.keySet()) {
					PersonDetail curNextPerson = map.get(key);
					SystemUsers systemUsers = userService.getUsersByName(curNextPerson.getEmployeeId());
					if(StringUtils.isBlank(systemUsers.getEmail())){
						//下一个处理人的邮箱为空
						mailMessages.append("人力系统未配置["+curNextPerson.getName()+"]的邮箱地址，["+curNextPerson.getName()+"]可能无法收到邮件通知\\n");
					}
				}
				this.servletRequest.setAttribute("mailExceptionMessage",mailMessages.toString());
				
				
				if (nextPerson != null) {
					this.servletRequest.setAttribute("nextPerson", nextPerson.getName() + "(" + nextPerson.getEmployeeId()
							+ ")");
				}

				// 开始逐级审核，由于事情还没做完，还没到核决主管，需要确定一下一个领导信息
				if (status.equals(FlowStatus.CHENGHE_START)) {
					this.servletRequest.setAttribute("FlowStatus", "CHENGHE");
					
					try {
						MailUtil.mailToPerson(nextPerson, flow);
					} catch(MailPreparationException e){
						this.servletRequest.setAttribute("mailExceptionMessage", e.getMessage());
					}
					

				}
				// 开始会办
				if (status.equals(FlowStatus.JOINTSIGN_START)) {
					this.servletRequest.setAttribute("FlowStatus", "JOINTSIGN_START");
					// id+name的组合
					List<String> persons = new ArrayList<String>();
					for (String key : map.keySet()) {
						nextPerson = map.get(key);
						persons.add(nextPerson.getEmployeeId() + "," + nextPerson.getName());
						//会办邮件发送
						if (nextPerson != null){
							try {
								MailUtil.mailToPerson(nextPerson, flow);
							} catch(MailPreparationException e){
								this.servletRequest.setAttribute("mailExceptionMessage", e.getMessage());
							}
						}
					}
					String[] arr = (String[]) persons.toArray(new String[persons.size()]);
					if (arr != null && arr.length > 0) {
						String huibanpersons = org.springframework.util.StringUtils.arrayToDelimitedString(arr, ";");
						this.servletRequest.setAttribute("huibanpersons", huibanpersons);
					}
					else {
						// 如果会办人列表为空，说明会办分支已完成
						this.servletRequest.setAttribute("FlowStatus", "JOINTSIGN_BRANCH_FINISH");
					}
				}

				// 发起人确认
				if (status.equals(FlowStatus.CONFIRM_START)) {
					this.servletRequest.setAttribute("FlowStatus", "CONFIRM");
				}
				
				// 事业部主管审核
				if (status.equals(FlowStatus.SECONDFINAL_DECISION_START)){
					this.servletRequest.setAttribute("FlowStatus", "SECONDFINAL_DECISION_START");
					try{
						MailUtil.mailToPerson(nextPerson, flow);
					}catch (Exception e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}
				
				// 副主管审核
				if (status.equals(FlowStatus.NEXTFINAL_DECISION_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"NEXTFINAL_DECISION_START");
					try {
						MailUtil.mailToPerson(nextPerson, flow);
					} catch (Exception e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}

				// 最终核决阶段
				if (status.equals(FlowStatus.FINAL_DECISION_START)) {
					this.servletRequest.setAttribute("FlowStatus", "FINAL_DECISION_START");
					try {
						MailUtil.mailToPerson(nextPerson, flow);
					} catch(MailPreparationException e){
						this.servletRequest.setAttribute("mailExceptionMessage", e.getMessage());
					}
				}
				// 核决主管审核结束
				if (status.equals(FlowStatus.FINAL_DECISION_END)) {
					this.servletRequest.setAttribute("FlowStatus", "FINAL_DECISION_END");
					
					// 发送邮件给本人和上级主管
					PersonDetail mgrPerson = personService.getMgrPersonDetail(flow.getActualPerson());
					PersonDetail tmpPerson = new PersonDetail();
					tmpPerson.setEmployeeId("00000006");
					try {
						MailUtil.mailToPerson(flow.getActualPerson(), flow);
						MailUtil.mailToPerson(mgrPerson, flow);
						if (flowService.isTopApprove(flow)){
							MailUtil.mailToPerson(tmpPerson, flow);
						}
					} catch(MailPreparationException e){
						this.servletRequest.setAttribute("mailExceptionMessage", e.getMessage());
					}
				}
				// 抄送给单位
				if (status.equals(FlowStatus.COPY_SEND)) {
					this.servletRequest.setAttribute("FlowStatus", "COPY_SEND");
					
					// 发送邮件给本人和上级主管
					PersonDetail mgrPerson = personService.getMgrPersonDetail(flow.getActualPerson());
					PersonDetail tmpPerson = new PersonDetail();
					tmpPerson.setEmployeeId("00000006");
					try {
						MailUtil.mailToPerson(flow.getActualPerson(), flow);
						MailUtil.mailToPerson(mgrPerson, flow);
						if (flowService.isTopApprove(flow)){
							MailUtil.mailToPerson(tmpPerson, flow);
						}
					} catch(MailPreparationException e){
						this.servletRequest.setAttribute("mailExceptionMessage", e.getMessage());
					}
				}
			}
		}catch(UncategorizedDataAccessException e){
			logger.error("完成内联表单时候发生异常："+e.getMessage());
			e.printStackTrace();
			addActionError("数据插入失败，请联系管理员");
			return ERROR;
		}catch (Exception e) {
			addActionError(e.getMessage());
			return ERROR;
		}
		return SUCCESS;
	}


	/**
	 * 设置当前操作人信息
	 */
	private void addOperatorInfo() {
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		String name = super.getLoginUser().getUserRealName();
		// 主要给附件上传信息显示使用
		this.servletRequest.setAttribute("operatorId", employeeId);
		this.servletRequest.setAttribute("operatorName", name);
	}
	
	/**
	 * 会办指派
	 * 
	 * @return
	 */
	public String assignWork() {
		String workId = (String) this.servletRequest.getParameter("workId");
		String assignId = (String) this.servletRequest.getParameter("assignId");
		String role = (String) this.servletRequest.getParameter("role");
		String employeeId = super.getLoginUser().getUserName();

		Map<String, PersonDetail> map = new HashMap<String, PersonDetail>();

		// 建议方案
		String opinion = (String) this.servletRequest.getParameter("opinion");
		
		if (StringUtils.isNotEmpty(workId) && StringUtils.isNotEmpty(assignId)) {
			MyWork myWork = flowService.loadWork(Long.valueOf(workId));
			String[] mixedValue = StringUtils.split(assignId, ",");

			// 当前操作人信息   黄文志好的 [251, 00006101, Z3001387]   [251, 00006101, Z3001888]
			PersonDetail person = personService.loadWidePersonDetail(mixedValue[0], mixedValue[1], mixedValue[2]);
			myWork.setOpinion(opinion);
			// 提交流程(如果是代理或助理，则附加代理人信息)
			if (StringUtils.isNotEmpty(role) && !role.equals(WorkRole.MYSELF.toString())
					&& StringUtils.isNotEmpty(employeeId)) {
				// 代理或者助理并允许指派
				if (role.equals(WorkRole.AGENT.toString()) || role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
					MyDelegateWork delegateWork = this.setDelegateWorkInfo(employeeId, myWork);
					// 助理指派
					if (role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
						map = flowService.assignInsideWork(person, delegateWork);
					}
					else {// 正常指派
						map = flowService.assignWork(person, delegateWork);
					}
				}
			}
			else {
				map = flowService.assignWork(person, myWork);
			}

			// 获得更新后的工作项（主要是流程状态）
			flow = flowService.getFlow(myWork);
			
			try {
				//指派发送邮件
				for (String key : map.keySet()) {
					MailUtil.mailToPerson(map.get(key), flow);
				}
			} catch(MailPreparationException e){
				this.servletRequest.setAttribute("mailExceptionMessage", e.getMessage());
			}
			
			//检查下一个处理人的邮箱
			StringBuffer mailMessages = new StringBuffer();
			for (String key : map.keySet()) {
				PersonDetail curNextPerson = map.get(key);
				SystemUsers systemUsers = userService.getUsersByName(curNextPerson.getEmployeeId());
				if(StringUtils.isBlank(systemUsers.getEmail())){
					//下一个处理人的邮箱为空
					mailMessages.append("人力系统未配置["+curNextPerson.getName()+"]的邮箱地址，["+curNextPerson.getName()+"]可能无法收到邮件通知\\n");
				}
			}
			this.servletRequest.setAttribute("mailExceptionMessage",mailMessages.toString());

			// 下一个工作人信息（格式为：姓名（工号））
			PersonDetail nextPerson = null;
			for (String key : map.keySet()) {
				nextPerson = map.get(key);
				break;
			}

			if (nextPerson != null) {
				this.servletRequest.setAttribute("nextPerson", nextPerson.getName() + "(" + nextPerson.getEmployeeId()
						+ ")");
			}
			// FlowStatus本身没有此状态，为了标识目前处于指派状态
			this.servletRequest.setAttribute("FlowStatus", "ASSIGN");
		}
		return SUCCESS;

	}
	
	/**
	 * 管理员指派，覆盖当前执行人
	 * @return
	 */
	public String adminAssignWork(){
		String workId = (String) this.servletRequest.getParameter("adminAssignWorkId");
		String assignId = (String) this.servletRequest.getParameter("assignId");
		String adminAssignOldEmployeeId = this.servletRequest.getParameter("adminAssignOldEmployeeId");
		Map<String, PersonDetail> map = new HashMap<String, PersonDetail>();

		// 建议方案
		String opinion = (String) this.servletRequest.getParameter("opinion");
		if (StringUtils.isNotEmpty(workId) && StringUtils.isNotEmpty(assignId)) {
			MyWork myWork = flowService.loadWork(Long.valueOf(workId));
			String[] mixedValue = StringUtils.split(assignId, ",");

			// 当前操作人信息
			PersonDetail person = personService.loadWidePersonDetail(mixedValue[0], mixedValue[1], mixedValue[2]);

			myWork.setOpinion(opinion);
			
			map = flowService.adminAssignWork(person, myWork,adminAssignOldEmployeeId);

			// 获得更新后的工作项（主要是流程状态）
			flow = flowService.getFlow(myWork);
			//指派发送邮件
			for (String key : map.keySet()) {
				MailUtil.mailToPerson(map.get(key), flow);
			}

			// 下一个工作人信息（格式为：姓名（工号））
			PersonDetail nextPerson = null;
			for (String key : map.keySet()) {
				nextPerson = map.get(key);
				break;
			}

			if (nextPerson != null) {
				this.servletRequest.setAttribute("nextPerson", nextPerson.getName() + "(" + nextPerson.getEmployeeId()
						+ ")");
			}
			// FlowStatus本身没有此状态，为了标识目前处于指派状态
			this.servletRequest.setAttribute("FlowStatus", "ADMIN_ASSIGN");
		}
		return SUCCESS;

	}

	/**
	 * 驳回
	 * 
	 * @return
	 */
	public String rejectWork() {
		String workId = (String) this.servletRequest.getParameter("workId");
		String role = (String) this.servletRequest.getParameter("role");
		String employeeId = super.getLoginUser().getUserName();

		if (StringUtils.isNotEmpty(workId)) {
			MyWork myWork = flowService.loadWork(Long.valueOf(workId));
			String opinion = (String) this.servletRequest.getParameter("opinion");
			myWork.setOpinion(opinion);

			// 提交流程(如果是代理或助理，则附加代理人信息)
			if (StringUtils.isNotEmpty(role) && !role.equals(WorkRole.MYSELF.toString())
					&& StringUtils.isNotEmpty(employeeId)) {
				// 代理或者助理并允许指派
				if (role.equals(WorkRole.AGENT.toString()) || role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
					MyDelegateWork delegateWork = this.setDelegateWorkInfo(employeeId, myWork);
					flowService.reject(delegateWork);
				}
			}
			else {
				flowService.reject(myWork);
			}

			flow = flowService.getFlow(myWork);
			FlowStatus status = flow.getStatus();
			if (status.equals(FlowStatus.REJECT)) {
				this.servletRequest.setAttribute("FlowStatus", "REJECT");
				// 发送邮件给本人和上级主管
				PersonDetail mgrPerson = personService.getMgrPersonDetail(flow.getActualPerson());
				
				//检查邮箱是否为空
				StringBuffer mailMessages = new StringBuffer();
				SystemUsers systemUsers = userService.getUsersByName(flow.getActualPerson().getEmployeeId());
				if(StringUtils.isBlank(systemUsers.getEmail())){
					//下一个处理人的邮箱为空
					mailMessages.append("人力系统未配置["+flow.getActualPerson().getName()+"]的邮箱地址，["+flow.getActualPerson().getName()+"]可能无法收到邮件通知\\n");
				}
				systemUsers = userService.getUsersByName(mgrPerson.getEmployeeId());
				if(StringUtils.isBlank(systemUsers.getEmail())){
					//下一个处理人的邮箱为空
					mailMessages.append("人力系统未配置["+mgrPerson.getName()+"]的邮箱地址，["+mgrPerson.getName()+"]可能无法收到邮件通知\\n");
				}
				this.servletRequest.setAttribute("mailExceptionMessage", mailMessages.toString());
				
				//发送
				try {
					MailUtil.mailToPerson(flow.getActualPerson(), flow);
					MailUtil.mailToPerson(mgrPerson, flow);
				} catch (MailPreparationException e) {
					this.servletRequest.setAttribute("mailExceptionMessage", e.getMessage());
				}
			}
		}
		return SUCCESS;

	}

	/**
	 * 重新起案
	 * 
	 * @return
	 */
	public String renewWork() {
		String formNum = (String) this.servletRequest.getParameter("formNum");
		String workId = (String) this.servletRequest.getParameter("workId");
		String role = (String) this.servletRequest.getParameter("role");
		String employeeId = super.getLoginUser().getUserName();

		// 加载流程详情
		flow = flowService.loadFlow(formNum);
		String deptPath = flow.getCreatePerson().getDeptPath();
		splitDeptPath(deptPath);
		//System.out.println(flow.getCreatePerson().getEmployeeId());
		MyWork myWork = flowService.loadWork(Long.valueOf(workId));
		
		// 提交流程(如果是代理或助理，则附加代理人信息)
		if (StringUtils.isNotEmpty(role) && !role.equals(WorkRole.MYSELF.toString())
				&& StringUtils.isNotEmpty(employeeId)) {
			// 代理或者助理并允许指派
			if (role.equals(WorkRole.AGENT.toString()) || role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
				MyDelegateWork delegateWork = this.setDelegateWorkInfo(employeeId, myWork);
				flow = flowService.renew(flow, delegateWork);
			}
		}
		else {
			flow = flowService.renew(flow, myWork);
		}

		// 会办单位、抄送单位加载
		this.loadDeptInfo();

		
		boolean isHQTR = false;
		String createUserId = flow.getCreatePerson().getEmployeeId();
		if (userService.getUsersByName(createUserId).getCmpcod().equalsIgnoreCase("HQTR")){
			isHQTR = true;
		}
		this.servletRequest.setAttribute("isHQTR", isHQTR);
		
		this.servletRequest.setAttribute("leader", flow.getDecionmaker().name());
		this.servletRequest.setAttribute("joinType", flow.getJointSignType().name());
		this.servletRequest.setAttribute("workId", workId);
		boolean selfconfirm = flow.isSelfConfirm();
		if (selfconfirm) {
			this.servletRequest.setAttribute("confirm", 1);
		}
		else {
			this.servletRequest.setAttribute("confirm", 0);
		}

		return "neilianRenew";

	}

	/**
	 * 撤销
	 * 
	 * @return
	 */
	public String cancelWork() {
		String formNum = (String) this.servletRequest.getParameter("formNum");
		String workId = (String) this.servletRequest.getParameter("workId");
		// 加载流程详情
		flow = flowService.loadFlow(formNum);
		MyWork myWork = flowService.loadWork(Long.valueOf(workId));

		// 撤销工作项
		flowService.cancel(myWork, flow);

//		// 发送邮件给本人和上级主管
//		PersonDetail mgrPerson = personService.getMgrPersonDetail(flow.getActualPerson());
//		mailToPerson(flow.getActualPerson(), flow);
//		mailToPerson(mgrPerson, flow);

		flow = flowService.getFlow(myWork);
		FlowStatus status = flow.getStatus();
		if (status.equals(FlowStatus.CANCEL)) {
			this.servletRequest.setAttribute("FlowStatus", "CANCEL");
		}
		return SUCCESS;
	}

	public String cancelForm() {
		String formNum = (String) this.servletRequest.getParameter("formNum");
		// 加载流程详情
		flow = flowService.loadFlow(formNum);
		// 登陆人自己新建一个撤销work 然后完成该work
		String employeeId = super.getLoginUser().getUserName();
		PersonDetail loginPerson = personService.loadWidePersonDetail(employeeId);
		MyWork myWork = loginPerson.buildWork(flow.getFlowType(), WorkStage.CANCEL, null);
		
		// 建议
		String opinion = (String) this.servletRequest.getParameter("opinion");
		myWork.setOpinion(opinion);
		
		myWork.setStatus(FlowStatus.CANCEL);
		myWork.setFlowId(flow.getId());
		this.flowService.saveWork(myWork);
		this.flowService.updateWorkStatus(myWork);

		flow.setStatus(FlowStatus.CANCEL);
		this.flowService.updateFlowStatus(flow);

		// 删除所有未完成工作项，也就是待办工作项
		this.flowService.clearFlow(flow);
		return SUCCESS;
	}
	
	/**
	 * 检查上级主管
	 * @return
	 */
	@SuppressWarnings("unused")
	public String checkMgrPerson(){
		JSONObject json = new JSONObject();
		//判断下一步处理人
		String workId = this.servletRequest.getParameter("workId");
		MyWork myWork = flowService.loadWork(Long.valueOf(workId));
		
		/******** 不需要进行汇报关系判断代码 start **********/
		//1.核决主管签核 不需要判断
		Flow flow = flowService.getFlow(myWork);
		if(flow.getStatus() == FlowStatus.FINAL_DECISION_START){
			//取出当前登录人
			String curEmployeeId = (String) getSession().get("employeeId");
			if(curEmployeeId.equals(myWork.getJoinStartEmployeeId())){
				//当前登录人为核决主管本人,不需要判断汇报关系
				json.put("status","success");
				this.createJSonData(json.toString());
				return AJAX;
			}else{
				//取出代理人，代理人可以提交
				AgentRelation agentRelation = flowAgentService.loadAgent(myWork.getJoinStartEmployeeId());
				if(null != agentRelation){
					List<AgentPerson> agentPersonList = flowAgentService.loadAgentPersons(agentRelation);
					for(AgentPerson agentPerson : agentPersonList){
						if(agentPerson.getAgentEmployeeId().equals(curEmployeeId)){
							//当前登录人为核决主管的代理人,不需要判断汇报关系
							json.put("status","success");
							this.createJSonData(json.toString());
							return AJAX;
						}
					}
				}
			}
		}
		//2.本人确认不需要判断
		if(flow.getStatus() == FlowStatus.CONFIRM_START){
			json.put("status","success");
			this.createJSonData(json.toString());
			return AJAX;
		}
		//3.会办结束不需要判断
		if(flow.getStatus() == FlowStatus.CMPCODEJOINTSIGN_START
				||flow.getStatus() == FlowStatus.JOINTSIGN_START){
			//取出当前登录人
			String curEmployeeId = (String) getSession().get("employeeId");
			// edit by Cao_Shengyong 2014-03-26
			if (myWork.getHb_ChengHe().equals("2")){
				if (curEmployeeId.equals(myWork.getHb_JoinStartEmployeeId())){
					json.put("status","success");
					this.createJSonData(json.toString());
					return AJAX;
				} else {
					//取出代理人，代理人可以提交
					AgentRelation agentRelation = flowAgentService.loadAgent(myWork.getHb_JoinStartEmployeeId());
					if(null != agentRelation){
						List<AgentPerson> agentPersonList = flowAgentService.loadAgentPersons(agentRelation);
						for(AgentPerson agentPerson : agentPersonList){
							if(agentPerson.getAgentEmployeeId().equals(curEmployeeId)){
								//当前登录人为会办人的代理人,不需要判断汇报关系
								json.put("status","success");
								this.createJSonData(json.toString());
								return AJAX;
							}
						}
					}
				}
			} else {
				if(curEmployeeId.equals(myWork.getJoinStartEmployeeId())){
					//当前登录人为会办本人,不需要判断汇报关系
					json.put("status","success");
					this.createJSonData(json.toString());
					return AJAX;
				}else{
					//取出代理人，代理人可以提交
					AgentRelation agentRelation = flowAgentService.loadAgent(myWork.getJoinStartEmployeeId());
					if(null != agentRelation){
						List<AgentPerson> agentPersonList = flowAgentService.loadAgentPersons(agentRelation);
						for(AgentPerson agentPerson : agentPersonList){
							if(agentPerson.getAgentEmployeeId().equals(curEmployeeId)){
								//当前登录人为会办人的代理人,不需要判断汇报关系
								json.put("status","success");
								this.createJSonData(json.toString());
								return AJAX;
							}
						}
					}
				}
			}
		}
		
		/******** 不需要进行汇报关系判断代码 end **********/
		
		PersonDetail loginPerson = this.personService.loadWidePersonDetail(myWork.getDeptId(), myWork.getEmployeeId(), myWork.getPostCode());
		PersonDetail mgrPerson = null;
		try {
			mgrPerson = personService.getMgrPersonDetail(loginPerson);
		} catch (Exception e) {
			json.put("status","error");
			json.put("message","当前处理人或下一步处理人汇报关系维护有误，请联系系统管理员处理");
			this.createJSonData(json.toString());
			return AJAX;
		}
		
		json.put("status","success");
		this.createJSonData(json.toString());
		return AJAX;
	}

//	private void mailToPerson(PersonDetail nextPerson, Flow flow) {
//		if (nextPerson != null) {
//			MailContent mailContent = new MailContent();
//			mailContent.setFormNum(flow.getFormNum());
//			mailContent.setUserName(nextPerson.getName());
//			mailContent.setEmail("1165528086@qq.com");
//			// MailUtil.sendMail(mailContent, this.servletRequest);
//		}
//	}

	/**
	 * 如果会办单位、抄送单位、文件发生变化则更新相关信息
	 */
	private void updateFlowInfo() {

		// 附件上传
		String attachmentIds = this.servletRequest.getParameter("attachmentIds");
		if (StringUtils.isNotEmpty(attachmentIds)) {
			String[] attachids = attachmentIds.split(";");
			FlowAttachment[] attachments = new FlowAttachment[attachids.length];
			for (int i = 0; i < attachids.length; i++) {
				FlowAttachment attachment = new FlowAttachment();
				attachment.setId(Long.valueOf(attachids[i]));
				attachments[i] = attachment;
			}
			flow.setFlowAttachments(attachments);
		}

		// 传入的是部门ID 会办
		String huibandeptIds = (String) this.servletRequest.getParameter("huibanDeptIds");
		String jointSignDeptName = (String) this.servletRequest.getParameter("huibanDept");
		if (StringUtils.isNotEmpty(huibandeptIds)) {
			String[] jointSignDeptIds = StringUtils.split(huibandeptIds, ";");
			// 会办等信息
			flow.setJointSignDeptIds(jointSignDeptIds);
			flow.setJointSignDeptName(jointSignDeptName);
		}

		// 传入的是部门ID
		String chaosongdeptIds = (String) this.servletRequest.getParameter("chaosongDeptIds");
		String copyDeptName = (String) this.servletRequest.getParameter("chaosongDept");
		if (StringUtils.isNotEmpty(chaosongdeptIds)) {
			String[] copyDeptIds = StringUtils.split(chaosongdeptIds, ";");
			flow.setCopyDeptIds(copyDeptIds);
			flow.setCopyDeptName(copyDeptName);
		}
		flowService.updateFormContentOnShenghe(flow);
	}

	/**
	 * 转换会办单位、抄送单位为可显示的格式
	 */
	private void loadDeptInfo() {
		// 会办单位
		String[] huibanDepts = flow.getJointSignDeptIds();
		String huibanDeptIds = "";
		if (huibanDepts != null && huibanDepts.length > 0) {
			huibanDeptIds = org.springframework.util.StringUtils.arrayToDelimitedString(huibanDepts, ";");
			this.servletRequest.setAttribute("huibanDeptIds", huibanDeptIds);
		}

		// 抄送单位
		String[] chaosongDepts = flow.getCopyDeptIds();
		String chaosongdeptIds = "";
		if (chaosongDepts != null && chaosongDepts.length > 0) {
			chaosongdeptIds = org.springframework.util.StringUtils.arrayToDelimitedString(chaosongDepts, ";");
			this.servletRequest.setAttribute("chaosongDeptIds", chaosongdeptIds);
		}
	}

	/**
	 * 将代理、助理的相关信息保存起来，供签核历程使用
	 * 
	 * @param employeeId
	 * @param myWork
	 * @return
	 */
	private MyDelegateWork setDelegateWorkInfo(String employeeId, MyWork myWork) {
		PersonDetail loginPerson = personService.loadWidePersonDetail(employeeId);
		MyDelegateWork delegateWork = (MyDelegateWork) myWork;
		delegateWork.setDlgEmployeeId(loginPerson.getEmployeeId());
		delegateWork.setDlgPostCode(loginPerson.getPostCode());
		delegateWork.setDlgDeptId(loginPerson.getDeptId());
		delegateWork.setDlgDeptCode(loginPerson.getDeptCode());
		delegateWork.setDlgADeptCode(loginPerson.getA_deptCode());
		delegateWork.setDlgCmpCode(loginPerson.getCmpCode());
		return delegateWork;
	}

	/**
	 * 加载参考内联信息
	 * 
	 * @return
	 */
	public String loadReferNeiLianDetail() {
		String formNum = (String) this.servletRequest.getParameter("formNum");

		// 加载流程详情
		flow = flowService.loadFlow(formNum);
		String deptPath = flow.getCreatePerson().getDeptPath();
		splitDeptPath(deptPath);

		// 会办单位、抄送单位加载
		// 会办单位
		String[] huibanDepts = flow.getJointSignDeptIds();
		String huibanDeptIds = "";
		if (huibanDepts != null && huibanDepts.length > 0) {
			huibanDeptIds = org.springframework.util.StringUtils.arrayToDelimitedString(huibanDepts, ";");
			flow.setHuibanDeptIds(huibanDeptIds);
		}

		// 抄送单位
		String[] chaosongDepts = flow.getCopyDeptIds();
		String chaosongdeptIds = "";
		if (chaosongDepts != null && chaosongDepts.length > 0) {
			chaosongdeptIds = org.springframework.util.StringUtils.arrayToDelimitedString(chaosongDepts, ";");
			flow.setChaosongDeptIds(chaosongdeptIds);
		}

		flow.setLeaderValue(flow.getDecionmaker().name());
		flow.setJoinTypeValue(flow.getJointSignType().name());
		boolean selfconfirm = flow.isSelfConfirm();
		if (selfconfirm) {
			flow.setConfirmValue("1");
		}
		else {
			flow.setConfirmValue("0");
		}

		flow.setStatus(null);
		flow.setFlowAttachments(null);
		flow.setNextStep(null);
		String json = VOUtils.getJsonData(flow);
		createJSonData(json);
		return AJAX;
	}
}
