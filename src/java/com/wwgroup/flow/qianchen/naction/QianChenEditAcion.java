package com.wwgroup.flow.qianchen.naction;

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
import com.wwgroup.common.util.CommonUtil;
import com.wwgroup.common.util.MailUtil;
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
import com.wwgroup.flow.dto.LogDTO;
import com.wwgroup.flow.dto.WorkRole;
import com.wwgroup.flow.service.FlowAgentService;
import com.wwgroup.flow.service.FlowAssistService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.flow.service.QCFlowService;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.dao.OrganDao;
import com.wwgroup.user.bo.EmployeePos;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

public class QianChenEditAcion extends BaseAjaxAction {

	private static final long serialVersionUID = -1584212247495945020L;

	private Flow flow = new Flow();

	private PersonService personService;

	private QCFlowService flowService;

	private UserService userService;

	private FlowAgentService flowAgentService;

	private FlowAssistService flowAssistService;

	/**
	 * @return the userService
	 */
	public UserService getUserService() {
		return userService;
	}

	/**
	 * @param userService
	 *            the userService to set
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setFlowService(QCFlowService flowService) {
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
	 * 根据审核人加载签呈信息
	 * 
	 * @return
	 */
	public String loadQianChenDetail() {
		try {
			String formNum = (String) this.servletRequest
					.getParameter("formNum");
			String workId = (String) this.servletRequest.getParameter("workId");
			String deptId = (String) this.servletRequest.getParameter("deptId");
			String role = (String) this.servletRequest.getParameter("role");

			this.addOperatorInfo();
			String tmpCurrentUserFullName = super.getLoginUser().getUserRealName() 
					+ "(" + super.getLoginUser().getUserName() + ")";
			logger.info(formNum + " 用户：" + tmpCurrentUserFullName + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 打开表单: " + formNum + " Start....");
			
			// 加载流程详情
			//logger.info(formNum + " 加载表单信息 Start...");
			flow = flowService.loadFlow(formNum);
			//logger.info(formNum + " 加载表单信息 End...");

			// 会办单位、抄送单位加载
			this.loadDeptInfo();

			MyWork myWork = null;
			if (StringUtils.isNotEmpty(workId)) {
				//logger.info(formNum + " 加载当前签核记录信息 Start...");
				myWork = flowService.loadWork(Long.valueOf(workId));
				//logger.info(formNum + " 加载当前签核记录信息 End...");
				// System.out.println(myWork.getHb_JoinSignStartId());
				if (myWork != null) {
					this.servletRequest.setAttribute("canUpAttach", 
							personService.canUpAttach(myWork.getDeptId()) ? "1" : "0");
					
					this.servletRequest.setAttribute("workStage", myWork
							.getWorkStage().name());
					boolean isFHeader = false;
					if (myWork.getWorkStage().equals(WorkStage.DIVISION_SIGN)) {
						// 如果走到这一步了，说明此人是地方至总部签呈的事业部主管、或体系内核决主管的副主管
						PersonDetail divisionPerson = personService
								.loadWidePersonDetailPlus(myWork.getDeptId(),
										myWork.getEmployeeId(),
										myWork.getPostCode());
						isFHeader = personService.quailifiedDecisionMakerPlus(flow.getDecionmaker(), divisionPerson);
					}
					this.servletRequest.setAttribute("isFHeader", isFHeader);
					this.servletRequest.setAttribute("opinion",
							myWork.getOpinion());

					// 检查当前操作人是否是本人或是代理人
					String curEmployeeId = (String) getSession().get(
							"employeeId");
					//logger.info(formNum + " 获取签核人的代理人设置 Start...");
					AgentRelation agentRelation = flowAgentService
							.loadAgent(myWork.getEmployeeId());
					//logger.info(formNum + " 获取签核人的代理人设置 End...");
					boolean agented = false;
					if (null != agentRelation) {
						//logger.info(formNum + " 获取签核人的代理人设置人员列表 Start...");
						List<AgentPerson> agentPersons = flowAgentService
								.loadAgentPersons(agentRelation);
						//logger.info(formNum + " 获取签核人的代理人设置人员列表 Start...");
						for (AgentPerson agentPerson : agentPersons) {
							if (agentPerson.getAgentEmployeeId().equals(
									curEmployeeId)) {
								agented = true;
								break;
							}
						}
					}
					logger.info(formNum + " 当前操作用户：" + tmpCurrentUserFullName + "是否代理人：" + agented);

					// 检查当前操作人是否是助理人
					//logger.info(formNum + " 获取签核人的助理人设置人员列表 Start...");
					List<AssistRelation> assistRelationList = flowAssistService
							.findAll(myWork.getEmployeeId());
					//logger.info(formNum + " 获取签核人的助理人设置人员列表 End...");
					boolean assist = false;
					for (AssistRelation assistRelation : assistRelationList) {
						if (curEmployeeId.equals(assistRelation
								.getSelectedAssistEmployeeId())) {
							assist = true;
							break;
						}
					}
					this.servletRequest.setAttribute("assist", assist);
					logger.info(formNum + " 当前操作用户：" + tmpCurrentUserFullName + "是否助理人：" + assist);
					
					//logger.info(formNum + " 根据签核人工号和岗位信息获取签核人 Start...");
					EmployeePos employeePos = personService
							.loadWideEmployeePos(myWork.getEmployeeId(),
									myWork.getPostCode());
					//logger.info(formNum + " 根据签核人工号和岗位信息获取签核人 End...");
					if (curEmployeeId.equals(myWork.getEmployeeId()) || agented) {
						this.servletRequest.setAttribute("curEmployee", true);
					} else {
						this.servletRequest.setAttribute("curEmployee", false);
					}
					if ((curEmployeeId.equals(myWork.getJoinStartEmployeeId()) && !agented)
							|| (agented && !myWork.getHb_ChengHe().equals("2"))) {

						if (employeePos != null) {
							boolean hbMgrUser = false;
							// 当前用户本身就是最高主管了
							if (employeePos.getMgCentFlg() == 1) {
								//this.servletRequest.setAttribute("hbMgrUser", false);
								hbMgrUser = false;
							} else {
								/**
								 * 在职能部门会办步骤
								 * 现在系统中会办职能部门时首先找的里该职能部门中的部门主管，由于日本事务室的特殊性，故要增加
								 * 这个判断，如果当前人本身就已经是单位主管，即中心主管，前端显示同意，不显示呈核上一级
								 */
								if (myWork.getWorkStage() == WorkStage.XZJOINTSIGN){
									if (employeePos.getMgA_DeptFlg() == 1){
										//this.servletRequest.setAttribute("hbMgrUser",false);
										hbMgrUser = false;
									} else {
										hbMgrUser = true;
									}
								} else {
									hbMgrUser = true;
								}
							}
							this.servletRequest.setAttribute("hbMgrUser", hbMgrUser);
						} else {
							this.servletRequest.setAttribute("hbMgrUser", true);
						}
					} else {
						this.servletRequest.setAttribute("hbMgrUser", false);
					}
					if (myWork.getHb_ChengHe().equals("2")) {
						if (myWork.getWorkStage() == WorkStage.XZJOINTSIGN){
							this.servletRequest.setAttribute("hbMgrCanSign", true);
						} else {
							this.servletRequest.setAttribute("hbMgrCanSign", false);
						}
					} else {
						this.servletRequest.setAttribute("hbMgrCanSign", true);
					}

					this.servletRequest.setAttribute("hbChengHe",
							myWork.getHb_ChengHe());
					
					boolean remoteMgr = true;
					// 增加地方单位最高主管指派的判断，主要是为了兼容历史表单
					if (myWork.getWorkStage().equals(WorkStage.CHENGHE)){
						if (org.apache.commons.lang3.StringUtils.isEmpty(myWork.getJoinSignStartId())){
							remoteMgr = false;
						}
					}
					this.servletRequest.setAttribute("remoteMgrSign", remoteMgr);

					if (flow.getStatus() != FlowStatus.INNERJOINTSIGN_START
							&& flow.getStatus() != FlowStatus.CENTERJOINTSIGN_START
							&& flow.getStatus() != FlowStatus.JOINTSIGN_START
							&& flow.getStatus() != FlowStatus.CMPCODEJOINTSIGN_START
							&& flow.getStatus() != FlowStatus.SYSTEMJOINTSIGN_START
							&& flow.getStatus() != FlowStatus.XZJOINTSIGN_START
							&& flow.getStatus() != FlowStatus.CONFIRM_START
							&& flow.getStatus() != FlowStatus.REJECT) {
						// 非会办，可以驳回
						if (flow.getStatus() == FlowStatus.FINAL_DECISION_START
								|| flow.getStatus() == FlowStatus.NEXTFINAL_DECISION_START
								|| flow.getStatus() == FlowStatus.BUSINESS_DECISION_START
								|| flow.getStatus() == FlowStatus.NEXTBUSINESS_DECISION_START
								|| flow.getStatus() == FlowStatus.CHAIRMANSIGN_START
								|| flow.getStatus() == FlowStatus.SubmitFBossSIGN_START
								|| flow.getStatus() == FlowStatus.FINALPLUS_DECISION_START) {
							/**
							 *  这里主要是指派后的记录是不可以驳回，原来的判断是当前审核记录的工号和指派发起人是否相同即判断是否回到了指派人
							 *  但这有一个新问题：即指派人兼职了多个中心的主管，在同时需要指派多个中心时就会产生问题
							 *  这里修改判断方式为当前审核记录的岗位代码和指派发起人是否相同来判断是否返回到了指派人
							 */
							/*if (myWork.getEmployeeId().equals(
									myWork.getJoinStartEmployeeId())) {*/
							if (this.flowService.isCompleteAssign(myWork)){
								this.servletRequest.setAttribute("canRefuse",
										true);
							} else {
								this.servletRequest.setAttribute("canRefuse",
										false);
							}
						} else {
							this.servletRequest.setAttribute("canRefuse", true);
						}
					} else {
						this.servletRequest.setAttribute("canRefuse", false);
					}

					// 获得指派需要过滤的部门id
					String joinSignStartId = myWork.getJoinSignStartId();
					if ((flow.getStatus().equals(FlowStatus.JOINTSIGN_START)
							|| flow
							.getStatus().equals(
									FlowStatus.CMPCODEJOINTSIGN_START)
							|| flow.getStatus().equals(FlowStatus.CENTERJOINTSIGN_START)
							|| flow.getStatus().equals(FlowStatus.SYSTEMJOINTSIGN_START)
							|| flow.getStatus().equals(FlowStatus.XZJOINTSIGN_START))
							&& StringUtils.isNotEmpty(joinSignStartId)) {
						JSONArray jsonArray = JSONArray
								.fromObject(joinSignStartId);
						if (jsonArray.size() > 0) {
							JSONObject jsonObj = jsonArray
									.getJSONObject(jsonArray.size() - 1);
							deptId = String.valueOf(jsonObj.get("deptId"));
						}
					}
					if (flow.getStatus().equals(FlowStatus.JOINTSIGN_START)
							|| flow.getStatus().equals(
									FlowStatus.INNERJOINTSIGN_START)
							|| flow.getStatus().equals(FlowStatus.CENTERJOINTSIGN_START)
							|| flow.getStatus().equals(FlowStatus.SYSTEMJOINTSIGN_START)
							|| flow.getStatus().equals(
									FlowStatus.CMPCODEJOINTSIGN_START)
							|| flow.getStatus().equals(FlowStatus.XZJOINTSIGN_START)) {
						this.servletRequest.setAttribute("deptName",
								myWork.getDeptName());
					}

					// if
					// (myWork.getWorkStage().name().equalsIgnoreCase(WorkStage.))
					String workStatus_str = myWork.getWorkStage().name();
					boolean disp_agree = false;
					// 如果是会办
					if (StringUtils.isNotEmpty(workStatus_str)) {
						if (workStatus_str.equals(WorkStage.JOINTSIGN.name())
								|| workStatus_str
										.equals(WorkStage.INNERJOINTSIGN.name())
								|| workStatus_str
										.equals(WorkStage.CMPCODEJOINTSIGN
												.name())
								|| workStatus_str.equals(WorkStage.CENTERJOINTSIGN.name())
								|| workStatus_str.equals(WorkStage.SYSTEMJOINTSIGN.name())
								|| flow.getStatus().equals(FlowStatus.XZJOINTSIGN_START)) {
							if ((curEmployeeId.equals(myWork
									.getJoinStartEmployeeId()) || agented)
									|| (myWork.getHb_ChengHe().equals("2"))) {
								disp_agree = true;
							}
						}
					}
					// System.out.println(workStatus_str);
					// System.out.println(disp_agree);
					this.servletRequest.setAttribute("dispAgree", disp_agree);

					if (flow.getStatus().equals(FlowStatus.JOINTSIGN_START)
							|| flow.getStatus().equals(
									FlowStatus.INNERJOINTSIGN_START)
							|| flow.getStatus().equals(FlowStatus.CENTERJOINTSIGN_START)
							|| flow.getStatus().equals(FlowStatus.SYSTEMJOINTSIGN_START)
							|| flow.getStatus().equals(FlowStatus.XZJOINTSIGN_START)
							|| flow.getStatus().equals(
									FlowStatus.CMPCODEJOINTSIGN_START)
							|| flow.getStatus().equals(
									FlowStatus.SECONDFINAL_DECISION_START)
							|| flow.getStatus().equals(FlowStatus.NEXTBUSINESS_DECISION_START)
							|| flow.getStatus().equals(FlowStatus.BUSINESS_DECISION_START)
							|| flow.getStatus().equals(FlowStatus.NEXTFINAL_DECISION_START)
							|| flow.getStatus().equals(
									FlowStatus.FINAL_DECISION_START)
							|| flow.getStatus().equals(FlowStatus.FINALPLUS_DECISION_START)
							|| flow.getStatus().equals(FlowStatus.CHENGHE_START)) { // 增加核决主管
						MyWork prevWork = flowService.getPrevWork(myWork);
						if (null != prevWork
								&& myWork.getParentId()==prevWork.getParentId()) {
							this.servletRequest.setAttribute("opinion", prevWork.getOpinion());
							this.servletRequest.setAttribute("Hb_Agree", prevWork.getHb_Agree());
						}
					}

				}
			}
			// 供页面判断流程单状态
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String createTime = sdf.format(new Date(flow.getCreateTime()));
			this.servletRequest.setAttribute("flowStatus", flow.getStatus()
					.name());
			this.servletRequest.setAttribute("createTime", createTime);
			// System.out.println(flow.getDecionmaker().toString());
			this.servletRequest.setAttribute("leader", flow.getDecionmaker()
					.toString());
			this.servletRequest.setAttribute("joinType", flow
					.getJointSignType().toString());
			this.servletRequest
					.setAttribute("renewTimes", flow.getRenewTimes());
			boolean selfconfirm = flow.isSelfConfirm();
			if (selfconfirm) {
				this.servletRequest.setAttribute("confirm", 1);
			} else {
				this.servletRequest.setAttribute("confirm", 0);
			}
			if (flow.isSubmitBoss()) {
				this.servletRequest.setAttribute("submitBoss", 1);
			} else {
				this.servletRequest.setAttribute("submitBoss", 0);
			}
			if (flow.isSubmitFBoss()){
				this.servletRequest.setAttribute("submitFBoss", 1);
			} else {
				this.servletRequest.setAttribute("submitFBoss", 0);
			}
			if (flow.isSubmitBOffice()){
				this.servletRequest.setAttribute("submitBOffice", 1);
			} else {
				this.servletRequest.setAttribute("submitBOffice", 0);
			}
			
			if (flow.isChariman()){
				this.servletRequest.setAttribute("chairman", 1);
			} else {
				this.servletRequest.setAttribute("chairman", 0);
			}
			
			this.servletRequest.setAttribute("workId", workId);
			this.servletRequest.setAttribute("deptId", deptId);
			this.servletRequest.setAttribute("role", role);

			String employeeId = super.getLoginUser().getUserName();
			String formEmployeeId = null;
			if (flow.getFlowType() == FlowType.QIANCHENG) {
				formEmployeeId = flow.getActualPerson().getEmployeeId();
			} else {
				formEmployeeId = flow.getCreatePerson().getEmployeeId();
			}
			boolean showCancelButton = (employeeId.equals(formEmployeeId) && StringUtils
					.isEmpty(workId));
			showCancelButton &= flow.getStatus() != FlowStatus.FINAL_DECISION_END;
			//showCancelButton &= flow.getStatus() != FlowStatus.FINALPLUS_DECISION_END;
			showCancelButton &= flow.getStatus() != FlowStatus.COPY_SEND;
			showCancelButton &= flow.getStatus() != FlowStatus.CANCEL;
			showCancelButton &= flow.getStatus() != FlowStatus.REJECT;
			showCancelButton &= flow.getStatus() != FlowStatus.RECREATE;
			showCancelButton &= flow.getStatus() != FlowStatus.END;
			// 增加本人确认时不可撤消,因为本人确认是在核决主管核决之后。
			//showCancelButton &= flow.getStatus() == FlowStatus.CONFIRM_START;
			if (FlowStatus.CONFIRM_START == flow.getStatus()) showCancelButton = true;
			this.servletRequest.setAttribute("showCancelButton",
					(showCancelButton) ? 1 : 0);

			boolean isHQTR = false;
			if (userService.getUsersByName(formEmployeeId).getCmpcod()
					.equalsIgnoreCase("HQTR")) {
				isHQTR = true;
			}
			
			this.servletRequest.setAttribute("isHQTR", isHQTR);
			this.servletRequest.setAttribute("sysFlg", flow.getFileSys());
						
			logger.info(formNum + " 用户：" + tmpCurrentUserFullName + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 打开表单: " + formNum + " End...");
			
		} catch (Exception e) {
			logger.error("加载签呈表单时候发生异常：" + e.getMessage());
			e.printStackTrace();
			addActionError("表单数据异常，请联系管理员核实");
			return ERROR;
		}

		// FlowAttachment[] attachments = qianChenFlow.getFlowAttachments();
		return "qianchenDetail";

	}

	/**
	 * 加载暂存表单信息
	 * 
	 * @return
	 */
	public String loadTemplateQianChenDetail() {
		try {
			String formNum = (String) this.servletRequest
					.getParameter("formNum");

			this.addOperatorInfo();

			// 加载流程详情
			flow = flowService.loadFlow(formNum);
			// 获取登录用户
			String employeeId = super.getLoginUser().getUserName();
			boolean isHQTR = false;
			PersonDetail personDetail = personService.loadWidePersonDetail(employeeId);
			if (personDetail.getCmpCode().equalsIgnoreCase("HQTR")) {
				isHQTR = true;
			}
			/*
			OrganDao organDao = this.flowService.getOrganDao();
			SystemGroups systemGroups = organDao.getGroupsByDeptCode(
					personDetail.getDeptCode(),
					personDetail.getA_deptCode(),
					personDetail.getCmpCode(), GroupType.DEPTGROUP);
			sysFlg = systemGroups.getSystemFlg();
			*/
			this.servletRequest.setAttribute("isHQTR", isHQTR);
			this.servletRequest.setAttribute("sysFlg", flow.getFileSys());

			// 内部会办单位加载
			String[] innerhuibans = flow.getInnerJointSignIds();
			String innerhuibanIds = "";
			if (innerhuibans != null && innerhuibans.length > 0) {
				innerhuibanIds = org.springframework.util.StringUtils
						.arrayToDelimitedString(innerhuibans, ";");
				this.servletRequest.setAttribute("innerhuibanIds",
						innerhuibanIds);
			}

			// 会办单位、抄送单位加载
			this.loadDeptInfo();

			// 供页面判断流程单状态
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String createTime = sdf.format(new Date(flow.getCreateTime()));
			this.servletRequest.setAttribute("createTime", createTime);
			this.servletRequest.setAttribute("leader", flow.getDecionmaker()
					.toString());
			this.servletRequest.setAttribute("joinType", flow
					.getJointSignType().toString());
			boolean selfconfirm = flow.isSelfConfirm();
			if (selfconfirm) {
				this.servletRequest.setAttribute("confirm", 1);
			} else {
				this.servletRequest.setAttribute("confirm", 0);
			}
			if (flow.isSubmitBoss()) {
				this.servletRequest.setAttribute("submitBoss", 1);
			} else {
				this.servletRequest.setAttribute("submitBoss", 0);
			}
			if (flow.isSubmitFBoss()){
				this.servletRequest.setAttribute("submitFBoss", 1);
			} else {
				this.servletRequest.setAttribute("submitFBoss", 0);
			}
			if (flow.isSubmitBOffice()){
				this.servletRequest.setAttribute("submitBOffice", 1);
			} else {
				this.servletRequest.setAttribute("submitBOffice", 0);
			}
			
			if (flow.isChariman()){
				this.servletRequest.setAttribute("chairman", 1);
			} else {
				this.servletRequest.setAttribute("chairman", 0);
			}

		} catch (Exception e) {
			logger.error("加载签呈表单时候发生异常：" + e.getMessage());
			e.printStackTrace();
			addActionError("表单数据异常，请联系管理员核实");
			return ERROR;
		}
		return "qianchenTemplate";

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
			String employeeId = super.getLoginUser().getUserName();
			
			String tmpCurrentUserFullName = super.getLoginUser().getUserRealName() 
					+ "(" + super.getLoginUser().getUserName() + ")";

			Map<String, PersonDetail> map = new HashMap<String, PersonDetail>();
			if (StringUtils.isNotEmpty(workId)) {

				// 当前工作项
				MyWork myWork = flowService.loadWork(Long.valueOf(workId));

				// 当前操作人信息
				// PersonDetail person =
				// personService.loadWidePersonDetail(myWork.getDeptId(),
				// myWork.getEmployeeId(), myWork.getPostCode());

				// 建议方案
				String opinion = (String) this.servletRequest
						.getParameter("opinion");
				myWork.setOpinion(opinion);
				// 是否需要呈核
				// add by Cao_Shengyong 2014-03-24
				String hbChengHe = (String) this.servletRequest
						.getParameter("hbChengHe");

				myWork.setHb_ChengHe(hbChengHe);

				// 写入会办主管是否同意状态
				String hbAgree = (String) this.servletRequest
						.getParameter("hb_isagree");
				if (StringUtils.isNotEmpty(hbAgree)) {
					myWork.setHb_Agree(hbAgree);
				}
				// System.out.println(myWork.getHbChengHe() + "@");
				// 判断是否需要更新流程信息（会办单位、抄送单位、附件）
				String updateFlag = (String) this.servletRequest
						.getParameter("updateFlag");
				flow = flowService.getFlow(myWork);
				logger.info(flow.getFormNum() + " 由用户：" + tmpCurrentUserFullName + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 执行表单：" + flow.getFormNum() + " 的提交/同意等操作 Start...");
				if (StringUtils.isNotEmpty(updateFlag)
						&& updateFlag.equals("1")) {
					//logger.info(flow.getFormNum() + " 在会办单位/抄送单位/附件发生变化时进行表单信息的更新操作 Start...");
					this.updateFlowInfo();
					//logger.info(flow.getFormNum() + " 在会办单位/抄送单位/附件发生变化时进行表单信息的更新操作 End...");
				}
				
				/**
				 * edit 2014-11-17
				 * 增加更新是否呈核总裁项，主要是增加了主线签核可修改是否呈核总裁
				 * 这里判断如果页面上的值和数据库中不一致时才去做更新操作
				 */
				String tmpSubmitBoss = this.servletRequest.getParameter("submitBossHid");
				boolean submitBoss = false;
				if (tmpSubmitBoss != null && tmpSubmitBoss.equals("1")) {
					submitBoss = true;
				}
				if (flow.isSubmitBoss() != submitBoss){
					flow.setSubmitBoss(submitBoss);
					//logger.info(flow.getFormNum() + " 在表单的呈核总载项发生变化时，对表单对应的值进行更新 Start...");
					flowService.updateFlowSubmitBoss(flow);
					//logger.info(flow.getFormNum() + " 在表单的呈核总载项发生变化时，对表单对应的值进行更新 End...");
				}
				String tmpSubmitFBoss = this.servletRequest.getParameter("submitFBossHid");
				boolean submitFBoss = false;
				if (tmpSubmitFBoss != null && tmpSubmitFBoss.equals("1")){
					submitFBoss = true;
				}
				if (flow.isSubmitFBoss() != submitFBoss){
					flow.setSubmitFBoss(submitFBoss);
					flowService.updateFlowSubmitFBoss(flow);
				}
				
				myWork.setClientIP(CommonUtil.getClientIP(servletRequest));
				// 提交流程(如果是代理或助理，则附加相关信息)
				if (StringUtils.isNotEmpty(role)
						&& !role.equals(WorkRole.MYSELF.toString())
						&& StringUtils.isNotEmpty(employeeId)) {
					// 代理或者助理并允许指派
					if (role.equals(WorkRole.AGENT.toString())
							|| role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
						MyDelegateWork delegateWork = this.setDelegateWorkInfo(
								employeeId, myWork);
						logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + " 代理/助理执行身份表单的完成 Start...");
						map = flowService.completeWork(delegateWork);
						logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + " 代理/助理身份执行表单的完成 End...");
					}
				} else {
					logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + " 自己执行表单的完成 Start...");
					map = flowService.completeWork(myWork);
					logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + " 自己执行表单的完成 End...");
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

				// 检查下一个处理人的邮箱
				StringBuffer nextMailMessages = new StringBuffer();
				for (String key : map.keySet()) {
					PersonDetail curNextPerson = map.get(key);
					//logger.info(flow.getFormNum() + " 表单工作项完成后，获取下一步处理人的信息，主要是邮件 Start...");
					SystemUsers systemUsers = userService
							.getUsersByName(curNextPerson.getEmployeeId());
					//logger.info(flow.getFormNum() + " 表单工作项完成后，获取下一步处理人的信息，主要是邮件 End...");
					if (StringUtils.isBlank(systemUsers.getEmail())) {
						// 下一个处理人的邮箱为空
						nextMailMessages.append("人力系统未配置["
								+ curNextPerson.getName() + "]的邮箱地址，["
								+ curNextPerson.getName() + "]可能无法收到邮件通知\\n");
					}
				}
				this.servletRequest.setAttribute("mailExceptionMessage",
						nextMailMessages.toString());

				if (nextPerson != null) {
					this.servletRequest.setAttribute(
							"nextPerson",
							nextPerson.getName() + "("
									+ nextPerson.getEmployeeId() + ")");
				}
				// 内部会办
				if (status.equals(FlowStatus.INNERJOINTSIGN_START)) {
					String[] personIds = flow.getInnerJointSignIds();
					String innerhuibanIds = org.springframework.util.StringUtils
							.arrayToDelimitedString(personIds, ";");
					// 显示内部会办人员列表
					this.servletRequest.setAttribute("innerhuibanIds",
							innerhuibanIds);
					this.servletRequest.setAttribute("FlowStatus",
							"INNERJOINTSIGN_START");
				}
				
				// 同中心下会办
				if (status.equals(FlowStatus.CENTERJOINTSIGN_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"CENTERJOINTSIGN_START");
					// id + name的组合
					List<String> persons = new ArrayList<String>();
					for (String key : map.keySet()) {
						nextPerson = map.get(key);
						persons.add(nextPerson.getEmployeeId() + ","
								+ nextPerson.getName());

						// 会办邮件发送
						if (nextPerson != null) {
							try {
								logger.info(flow.getFormNum() + " 进行中心会办的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
								MailUtil.mailToPerson(nextPerson, flow);
							} catch (MailPreparationException e) {
								this.servletRequest.setAttribute(
										"mailExceptionMessage", e.getMessage());
							}
						}
					}

					String[] arr = (String[]) persons
							.toArray(new String[persons.size()]);
					if (arr != null && arr.length > 0) {
						String huibanpersons = org.springframework.util.StringUtils
								.arrayToDelimitedString(arr, ";");
						this.servletRequest.setAttribute("huibanpersons",
								huibanpersons);
					} else {
						this.servletRequest.setAttribute("FlowStatus",
								"CENTER_JOINTSIGN_BRANCH_FINISH");
					}
				}

				// 同CMPCODE下会办
				if (status.equals(FlowStatus.CMPCODEJOINTSIGN_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"CMPCODEJOINTSIGN_START");
					// id+name的组合
					List<String> persons = new ArrayList<String>();
					for (String key : map.keySet()) {
						nextPerson = map.get(key);
						persons.add(nextPerson.getEmployeeId() + ","
								+ nextPerson.getName());

						// 会办邮件发送
						if (nextPerson != null) {
							try {
								logger.info(flow.getFormNum() + " 进行公司会办的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
								MailUtil.mailToPerson(nextPerson, flow);
							} catch (MailPreparationException e) {
								this.servletRequest.setAttribute(
										"mailExceptionMessage", e.getMessage());
							}
						}

					}
					String[] arr = (String[]) persons
							.toArray(new String[persons.size()]);
					if (arr != null && arr.length > 0) {
						String huibanpersons = org.springframework.util.StringUtils
								.arrayToDelimitedString(arr, ";");
						this.servletRequest.setAttribute("huibanpersons",
								huibanpersons);
					} else {
						// 如果会办人列表为空，说明会办分支已完成
						this.servletRequest.setAttribute("FlowStatus",
								"CMPCODE_JOINTSIGN_BRANCH_FINISH");
					}
				}
				
				// 同体系下会办
				if (status.equals(FlowStatus.SYSTEMJOINTSIGN_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"SYSTEMJOINTSIGN_START");
					// id+name的组合
					List<String> persons = new ArrayList<String>();
					for (String key : map.keySet()) {
						nextPerson = map.get(key);
						persons.add(nextPerson.getEmployeeId() + ","
								+ nextPerson.getName());

						// 会办邮件发送
						if (nextPerson != null) {
							try {
								logger.info(flow.getFormNum() + " 进行体系会办的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
								MailUtil.mailToPerson(nextPerson, flow);
							} catch (MailPreparationException e) {
								this.servletRequest.setAttribute(
										"mailExceptionMessage", e.getMessage());
							}
						}

					}
					String[] arr = (String[]) persons
							.toArray(new String[persons.size()]);
					if (arr != null && arr.length > 0) {
						String huibanpersons = org.springframework.util.StringUtils
								.arrayToDelimitedString(arr, ";");
						this.servletRequest.setAttribute("huibanpersons",
								huibanpersons);
					} else {
						// 如果会办人列表为空，说明会办分支已完成
						this.servletRequest.setAttribute("FlowStatus",
								"SYSTEM_JOINTSIGN_BRANCH_FINISH");
					}
				}
				
				// 职能部门会办
				if (status.equals(FlowStatus.XZJOINTSIGN_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"JOINTSIGN_START");
					// id+name的组合
					List<String> persons = new ArrayList<String>();
					for (String key : map.keySet()) {
						nextPerson = map.get(key);
						persons.add(nextPerson.getEmployeeId() + ","
								+ nextPerson.getName());

						// 会办邮件发送
						if (nextPerson != null) {
							try {
								logger.info(flow.getFormNum() + " 进行职能部门会办的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
								MailUtil.mailToPerson(nextPerson, flow);
							} catch (MailPreparationException e) {
								this.servletRequest.setAttribute(
										"mailExceptionMessage", e.getMessage());
							}
						}

					}
					String[] arr = (String[]) persons
							.toArray(new String[persons.size()]);
					if (arr != null && arr.length > 0) {
						String huibanpersons = org.springframework.util.StringUtils
								.arrayToDelimitedString(arr, ";");
						this.servletRequest.setAttribute("huibanpersons",
								huibanpersons);
					} else {
						// 如果会办人列表为空，说明会办分支已完成
						this.servletRequest.setAttribute("FlowStatus",
								"JOINTSIGN_BRANCH_FINISH");
					}
				}

				// 内部会办结束，开始逐级审核，由于事情还没做完，还没到核决主管，需要确定一下一个领导信息
				if (status.equals(FlowStatus.INNERJOINTSIGN_END)
						|| status.equals(FlowStatus.CHENGHE_START)
						|| status.equals(FlowStatus.NEXTCHENGHE_START)
						|| status.equals(FlowStatus.CENTER_CHENGHE_START)
						|| status.equals(FlowStatus.DEPT_CHENGHE_START)
						|| status.equals(FlowStatus.SubmitFBossSIGN_START)
						|| status.equals(FlowStatus.CHAIRMANSIGN_START)) {
					this.servletRequest.setAttribute("FlowStatus", "CHENGHE");
					try {
						logger.info(flow.getFormNum() + " 进行主线签核的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
						MailUtil.mailToPerson(nextPerson, flow);
					} catch (MailPreparationException e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}
				// 开始会办
				if (status.equals(FlowStatus.JOINTSIGN_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"JOINTSIGN_START");
					// id+name的组合
					List<String> persons = new ArrayList<String>();
					StringBuffer mailMessages = new StringBuffer();
					for (String key : map.keySet()) {
						nextPerson = map.get(key);
						persons.add(nextPerson.getEmployeeId() + ","
								+ nextPerson.getName());

						// 会办邮件发送
						if (nextPerson != null) {
							SystemUsers systemUsers = userService
									.getUsersByName(nextPerson.getEmployeeId());
							if (StringUtils.isBlank(systemUsers.getEmail())) {
								mailMessages.append("人力系统未配置["
										+ nextPerson.getName() + "]的邮箱地址，["
										+ nextPerson.getName()
										+ "]可能无法收到邮件通知\\n");
							} else {
								try {
									logger.info(flow.getFormNum() + " 进行其它会办的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
									MailUtil.mailToPerson(nextPerson, flow);
								} catch (MailPreparationException e) {
									this.servletRequest.setAttribute(
											"mailExceptionMessage",
											e.getMessage());
								}
							}
						}
					}
					if (this.servletRequest
							.getAttribute("mailExceptionMessage") == null) {
						this.servletRequest
								.setAttribute("mailExceptionMessage",
										mailMessages.toString());
					}

					String[] arr = (String[]) persons
							.toArray(new String[persons.size()]);
					if (arr != null && arr.length > 0) {
						String huibanpersons = org.springframework.util.StringUtils
								.arrayToDelimitedString(arr, ";");
						this.servletRequest.setAttribute("huibanpersons",
								huibanpersons);
					} else {
						// 如果会办人列表为空，说明会办分支已完成
						this.servletRequest.setAttribute("FlowStatus",
								"JOINTSIGN_BRANCH_FINISH");
					}
				}

				// 发起人确认
				if (status.equals(FlowStatus.CONFIRM_START)) {
					this.servletRequest.setAttribute("FlowStatus", "CONFIRM");
					try {
						logger.info(flow.getFormNum() + " 进行本人确认的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
						MailUtil.mailToPerson(nextPerson, flow);
					} catch (Exception e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}
				
				// 事业部副主管审核
				if (status.equals(FlowStatus.NEXTBUSINESS_DECISION_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"NEXTBUSINESS_DECISION_START");
					try {
						logger.info(flow.getFormNum() + " 进行事业部副主管的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
						MailUtil.mailToPerson(nextPerson, flow);
					} catch (Exception e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}

				// 事业部主管审核
				if (status.equals(FlowStatus.BUSINESS_DECISION_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"BUSINESS_DECISION_START");
					try {
						logger.info(flow.getFormNum() + " 进行事业部主管的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
						MailUtil.mailToPerson(nextPerson, flow);
					} catch (Exception e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}

				// (Old)事业部主管审核
				if (status.equals(FlowStatus.SECONDFINAL_DECISION_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"SECONDFINAL_DECISION_START");
					try {
						MailUtil.mailToPerson(nextPerson, flow);
					} catch (Exception e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}
				
				// 副主管审核
				if (status.equals(FlowStatus.NEXTFINAL_DECISION_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"NEXTFINAL_DECISION_START");
					try {
						logger.info(flow.getFormNum() + " 进行核决副主管的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
						MailUtil.mailToPerson(nextPerson, flow);
					} catch (Exception e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}

				// 最终核决阶段
				if (status.equals(FlowStatus.FINAL_DECISION_START)) {
					if (myWork.getWorkStage() == WorkStage.BOSS_SIGN
							&& StringUtils.isNotBlank(myWork
									.getJoinStartEmployeeId())
							/**
							 *　这里将原来的判断方式修改为判断当前审核记录的岗位代码和下一步处理人的岗位代码比较
							 *　用来判断是否回到了指派人
							 */
							&& !this.flowService.isCompleteAssign(myWork, nextPerson.getPostCode())
									) {
						// 指派
						this.servletRequest.setAttribute("FlowStatus",
								"FINAL_DECISION_SIGN");
					} else {
						this.servletRequest.setAttribute("FlowStatus",
								"FINAL_DECISION_START");
					}
					try {
						logger.info(flow.getFormNum() + " 进行核决主管的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
						MailUtil.mailToPerson(nextPerson, flow);
					} catch (MailPreparationException e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}
				
				// 核决二次审核阶段
				if (status.equals(FlowStatus.FINALPLUS_DECISION_START)) {
					if (myWork.getWorkStage() == WorkStage.BOSSPLUS_SIGN
							&& StringUtils.isNotBlank(myWork
									.getJoinStartEmployeeId())
							/**
							 *　这里将原来的判断方式修改为判断当前审核记录的岗位代码和下一步处理人的岗位代码比较
							 *　用来判断是否回到了指派人
							 */
							&& !this.flowService.isCompleteAssign(myWork, nextPerson.getPostCode())
									) {
						// 指派
						this.servletRequest.setAttribute("FlowStatus",
								"FINALPLUS_DECISION_SIGN");
					} else {
						this.servletRequest.setAttribute("FlowStatus",
								"FINALPLUS_DECISION_START");
					}
					try {
						logger.info(flow.getFormNum() + " 进行核决主管二次审核的邮件发送操作: " + nextPerson.getEmployeeId() + nextPerson.getName());
						MailUtil.mailToPerson(nextPerson, flow);
					} catch (MailPreparationException e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}

				// 核决主管审核结束
				if (status.equals(FlowStatus.FINAL_DECISION_END)) {
					this.servletRequest.setAttribute("FlowStatus",
							"FINAL_DECISION_END");

					// 发送邮件给本人和上级主管
					PersonDetail mgrPerson = personService
							.getMgrPersonDetail(flow.getActualPerson());
					
					PersonDetail tmpPersonx = new PersonDetail();
					tmpPersonx.setEmployeeId("00000818");
					try {
						logger.info(flow.getFormNum() + " 进行核决后的申请人的邮件发送操作: "
								+ flow.getActualPerson().getEmployeeId()
								+ flow.getActualPerson().getName());
						MailUtil.mailToPerson(flow.getActualPerson(), flow);
						logger.info(flow.getFormNum() + " 进行核决后的申请人主管的邮件发送操作: "
								+ mgrPerson.getEmployeeId()
								+ mgrPerson.getName());
						MailUtil.mailToPerson(mgrPerson, flow);

						
						if (flowService.isTopApprove(flow)){
							// 暂停神旺控股最高主管核决后的特殊邮件发送
							//MailUtil.mailToPerson(tmpPersonx, flow);
						}
					} catch (MailPreparationException e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}
				// 抄送给单位
				if (status.equals(FlowStatus.COPY_SEND)) {
					this.servletRequest.setAttribute("FlowStatus", "COPY_SEND");
					// 发送邮件给本人和上级主管
					PersonDetail mgrPerson = personService
							.getMgrPersonDetail(flow.getActualPerson());
					PersonDetail tmpPerson = new PersonDetail();
					tmpPerson.setEmployeeId("00000006");
					try {
						logger.info(flow.getFormNum() + " 进行核决抄送后的申请人的邮件发送操作: "
								+ flow.getActualPerson().getEmployeeId()
								+ flow.getActualPerson().getName());
						MailUtil.mailToPerson(flow.getActualPerson(), flow);
						logger.info(flow.getFormNum() + " 进行核决抄送后的申请人主管的邮件发送操作: "
								+ mgrPerson.getEmployeeId()
								+ mgrPerson.getName());
						MailUtil.mailToPerson(mgrPerson, flow);
						if (flowService.isTopApprove(flow)){
							// 暂停神旺控股最高主管核决后的特殊邮件发送
							//MailUtil.mailToPerson(tmpPerson, flow);
						}
					} catch (MailPreparationException e) {
						this.servletRequest.setAttribute(
								"mailExceptionMessage", e.getMessage());
					}
				}
				
				// 会办意见如果选择了不同意，则发邮件通知申请人
				if (myWork.getHb_Agree().equals("2")){
					logger.info(flow.getFormNum() + " 会办不同意提交后发送邮件通知给申请人: " + flow.getActualPerson().getEmployeeId() + flow.getActualPerson().getName());
					MailUtil.mailToPerson(flow.getActualPerson(), flow, "有会办单位不同意");
				}
				
				logger.info(flow.getFormNum() + " 由用户：" + tmpCurrentUserFullName + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 执行表单：" + flow.getFormNum() + " 的提交/同意等操作 End...");
			}
		} catch (UncategorizedDataAccessException e) {
			logger.error("完成签呈表单时候发生异常：" + e.getMessage());
			e.printStackTrace();
			addActionError("数据插入失败，请联系管理员");
			return ERROR;
		} catch (Exception e) {
			addActionError(e.getMessage());
			return ERROR;
		}
		return SUCCESS;
	}

	/**
	 * 会办指派
	 * 
	 * @return
	 */
	public String assignWork() {
		try{
			String workId = (String) this.servletRequest.getParameter("workId");
			String assignId = (String) this.servletRequest.getParameter("assignId");
			String role = (String) this.servletRequest.getParameter("role");
			String employeeId = super.getLoginUser().getUserName();
	
			String tmpCurrentUserFullName = super.getLoginUser().getUserRealName() 
					+ "(" + super.getLoginUser().getUserName() + ")";
			
			logger.info(flow.getFormNum() + " 由用户：" + tmpCurrentUserFullName + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 进行指派操作 Start...");
			
			Map<String, PersonDetail> map = new HashMap<String, PersonDetail>();
	
			// 建议方案
			String opinion = (String) this.servletRequest.getParameter("opinion");
			if (StringUtils.isNotEmpty(workId) && StringUtils.isNotEmpty(assignId)) {
				MyWork myWork = flowService.loadWork(Long.valueOf(workId));
				String[] mixedValue = StringUtils.split(assignId, ",");
	
				// 当前操作人信息
				PersonDetail person = personService.loadWidePersonDetail(
						mixedValue[0], mixedValue[1], mixedValue[2]);
	
				myWork.setOpinion(opinion);
	
				myWork.setClientIP(CommonUtil.getClientIP(servletRequest));
				// 提交流程(如果是代理或助理，则附加代理人信息)
				if (StringUtils.isNotEmpty(role)
						&& !role.equals(WorkRole.MYSELF.toString())
						&& StringUtils.isNotEmpty(employeeId)) {
					// 代理或者助理并允许指派
					if (role.equals(WorkRole.AGENT.toString())
							|| role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
						MyDelegateWork delegateWork = this.setDelegateWorkInfo(
								employeeId, myWork);
						// 助理指派
						if (role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
							logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + " 助理/代理身份进行二次指派的操作. Start...");
							map = flowService
									.assignInsideWork(person, delegateWork);
							logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + " 助理/代理身份进行二次指派的操作. End...");
						} else {// 正常指派
							logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + " 助理/代理身份进行指派的操作. Start...");
							map = flowService.assignWork(person, delegateWork);
							logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + " 助理/代理身份进行指派的操作. End...");
						}
					}
				} else {
					logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + " 自已进行指派的操作. Start...");
					map = flowService.assignWork(person, myWork);
					logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + " 自已进行指派的操作. End...");
				}
	
				// 获得更新后的工作项（主要是流程状态）
				flow = flowService.getFlow(myWork);
				logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 进行指派操作的表单为：" + flow.getFormNum());
				/**
				 * edit 2014-11-17
				 * 增加更新是否呈核总裁项，主要是增加了主线签核可修改是否呈核总裁
				 * 这里判断如果页面上的值和数据库中不一致时才去做更新操作
				 */
				String tmpSubmitBoss = this.servletRequest.getParameter("submitBossHid");
				boolean submitBoss = false;
				if (tmpSubmitBoss != null && tmpSubmitBoss.equals("1")) {
					submitBoss = true;
				}
				if (flow.isSubmitBoss() != submitBoss){
					flow.setSubmitBoss(submitBoss);
					flowService.updateFlowSubmitBoss(flow);
				}
				String tmpSubmitFBoss = this.servletRequest.getParameter("submitFBossHid");
				boolean submitFBoss = false;
				if (tmpSubmitFBoss != null && tmpSubmitFBoss.equals("1")){
					submitFBoss = true;
				}
				if (flow.isSubmitFBoss() != submitFBoss){
					flow.setSubmitFBoss(submitFBoss);
					flowService.updateFlowSubmitFBoss(flow);
				}
	
				try {
					// 指派发送邮件
					for (String key : map.keySet()) {
						logger.info(flow.getFormNum() + " 用户：" + tmpCurrentUserFullName + "执行指派操作后发送通知邮件给：" + map.get(key).getEmployeeId());
						MailUtil.mailToPerson(map.get(key), flow);
					}
				} catch (MailPreparationException e) {
					this.servletRequest.setAttribute("mailExceptionMessage",
							e.getMessage());
				}
	
				// 检查下一个处理人的邮箱
				StringBuffer mailMessages = new StringBuffer();
				for (String key : map.keySet()) {
					PersonDetail curNextPerson = map.get(key);
					SystemUsers systemUsers = userService
							.getUsersByName(curNextPerson.getEmployeeId());
					if (StringUtils.isBlank(systemUsers.getEmail())) {
						// 下一个处理人的邮箱为空
						mailMessages.append("人力系统未配置[" + curNextPerson.getName()
								+ "]的邮箱地址，[" + curNextPerson.getName()
								+ "]可能无法收到邮件通知\\n");
					}
				}
				this.servletRequest.setAttribute("mailExceptionMessage",
						mailMessages.toString());
	
				// 下一个工作人信息（格式为：姓名（工号））
				PersonDetail nextPerson = null;
				for (String key : map.keySet()) {
					nextPerson = map.get(key);
					break;
				}
	
				if (nextPerson != null) {
					this.servletRequest.setAttribute("nextPerson",
							nextPerson.getName() + "(" + nextPerson.getEmployeeId()
									+ ")");
				}
				// FlowStatus本身没有此状态，为了标识目前处于指派状态
				this.servletRequest.setAttribute("FlowStatus", "ASSIGN");
			}
			logger.info(flow.getFormNum() + " 由用户：" + tmpCurrentUserFullName + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 进行指派操作 End...");
		} catch (Exception e) {
			addActionError(e.getMessage());
			return ERROR;
		}
		return SUCCESS;

	}

	/**
	 * 管理员指派，覆盖当前执行人
	 * 
	 * @return
	 */
	public String adminAssignWork() {
		String workId = (String) this.servletRequest
				.getParameter("adminAssignWorkId");
		String assignId = (String) this.servletRequest.getParameter("assignId");
		String adminAssignOldEmployeeId = this.servletRequest
				.getParameter("adminAssignOldEmployeeId");
		Map<String, PersonDetail> map = new HashMap<String, PersonDetail>();

		// 建议方案
		String opinion = (String) this.servletRequest.getParameter("opinion");
		if (StringUtils.isNotEmpty(workId) && StringUtils.isNotEmpty(assignId)) {
			MyWork myWork = flowService.loadWork(Long.valueOf(workId));
			String[] mixedValue = StringUtils.split(assignId, ",");

			// 当前操作人信息
			PersonDetail person = personService.loadWidePersonDetail(
					mixedValue[0], mixedValue[1], mixedValue[2]);

			myWork.setOpinion(opinion);

			map = flowService.adminAssignWork(person, myWork,
					adminAssignOldEmployeeId);

			// 获得更新后的工作项（主要是流程状态）
			flow = flowService.getFlow(myWork);
			// 指派发送邮件
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
				this.servletRequest.setAttribute("nextPerson",
						nextPerson.getName() + "(" + nextPerson.getEmployeeId()
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
		try{
			String workId = (String) this.servletRequest.getParameter("workId");
			String role = (String) this.servletRequest.getParameter("role");
			String employeeId = super.getLoginUser().getUserName();
	
			if (StringUtils.isNotEmpty(workId)) {
				MyWork myWork = flowService.loadWork(Long.valueOf(workId));
				String opinion = (String) this.servletRequest
						.getParameter("opinion");
				myWork.setOpinion(opinion);
	
				myWork.setClientIP(CommonUtil.getClientIP(servletRequest));
				// 提交流程(如果是代理或助理，则附加代理人信息)
				if (StringUtils.isNotEmpty(role)
						&& !role.equals(WorkRole.MYSELF.toString())
						&& StringUtils.isNotEmpty(employeeId)) {
					// 代理或者助理并允许指派
					if (role.equals(WorkRole.AGENT.toString())
							|| role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
						MyDelegateWork delegateWork = this.setDelegateWorkInfo(
								employeeId, myWork);
						flowService.reject(delegateWork);
					}
				} else {
					flowService.reject(myWork);
				}
	
				flow = flowService.getFlow(myWork);
				FlowStatus status = flow.getStatus();
				if (status.equals(FlowStatus.REJECT)) {
					this.servletRequest.setAttribute("FlowStatus", "REJECT");
					// 发送邮件给本人和上级主管
					PersonDetail mgrPerson = personService.getMgrPersonDetail(flow
							.getActualPerson());
	
					// 检查邮箱是否为空
					StringBuffer mailMessages = new StringBuffer();
					SystemUsers systemUsers = userService.getUsersByName(flow
							.getActualPerson().getEmployeeId());
					if (StringUtils.isBlank(systemUsers.getEmail())) {
						// 下一个处理人的邮箱为空
						mailMessages.append("人力系统未配置["
								+ flow.getActualPerson().getName() + "]的邮箱地址，["
								+ flow.getActualPerson().getName()
								+ "]可能无法收到邮件通知\\n");
					}
					systemUsers = userService.getUsersByName(mgrPerson
							.getEmployeeId());
					if (StringUtils.isBlank(systemUsers.getEmail())) {
						// 下一个处理人的邮箱为空
						mailMessages.append("人力系统未配置[" + mgrPerson.getName()
								+ "]的邮箱地址，[" + mgrPerson.getName()
								+ "]可能无法收到邮件通知\\n");
					}
					this.servletRequest.setAttribute("mailExceptionMessage",
							mailMessages.toString());
	
					// 发送
					try {
						MailUtil.mailToPerson(flow.getActualPerson(), flow);
						MailUtil.mailToPerson(mgrPerson, flow);
					} catch (MailPreparationException e) {
						this.servletRequest.setAttribute("mailExceptionMessage",
								e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			addActionError(e.getMessage());
			return ERROR;
		}
		return SUCCESS;

	}

	public String endForm() {
		String formNum = (String) this.servletRequest.getParameter("formNum");
		String workId = (String) this.servletRequest.getParameter("workId");
		String role = (String) this.servletRequest.getParameter("role");
		String employeeId = super.getLoginUser().getUserName();
		// 加载流程详情
		flow = flowService.loadFlow(formNum);
		MyWork myWork = flowService.loadWork(Long.valueOf(workId));

		// 提交流程(如果是代理或助理，则附加代理人信息)
		if (StringUtils.isNotEmpty(role)
				&& !role.equals(WorkRole.MYSELF.toString())
				&& StringUtils.isNotEmpty(employeeId)) {
			// 代理或者助理并允许指派
			if (role.equals(WorkRole.AGENT.toString())
					|| role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
				MyDelegateWork delegateWork = this.setDelegateWorkInfo(
						employeeId, myWork);
				flow = flowService.endFlow(flow, delegateWork);
			}
		} else {
			flow = flowService.endFlow(flow, myWork);
		}
		return "success";
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
		MyWork myWork = flowService.loadWork(Long.valueOf(workId));

		// 提交流程(如果是代理或助理，则附加代理人信息)
		if (StringUtils.isNotEmpty(role)
				&& !role.equals(WorkRole.MYSELF.toString())
				&& StringUtils.isNotEmpty(employeeId)) {
			// 代理或者助理并允许指派
			if (role.equals(WorkRole.AGENT.toString())
					|| role.equals(WorkRole.ASSIST_ASSIGNED.toString())) {
				MyDelegateWork delegateWork = this.setDelegateWorkInfo(
						employeeId, myWork);
				flow = flowService.renew(flow, delegateWork);
			}
		} else {
			flow = flowService.renew(flow, myWork);
		}

		// 会办单位、抄送单位加载
		this.loadDeptInfo();

		this.servletRequest
				.setAttribute("leader", flow.getDecionmaker().name());
		this.servletRequest.setAttribute("joinType", flow.getJointSignType()
				.name());
		this.servletRequest.setAttribute("workId", workId);
		boolean selfconfirm = flow.isSelfConfirm();
		if (selfconfirm) {
			this.servletRequest.setAttribute("confirm", 1);
		} else {
			this.servletRequest.setAttribute("confirm", 0);
		}
		if (flow.isSubmitBoss()) {
			this.servletRequest.setAttribute("submitBoss", 1);
		} else {
			this.servletRequest.setAttribute("submitBoss", 0);
		}
		if (flow.isSubmitFBoss()) {
			this.servletRequest.setAttribute("submitFBoss", 1);
		} else {
			this.servletRequest.setAttribute("submitFBoss", 0);
		}
		if (flow.isSubmitBOffice()){
			this.servletRequest.setAttribute("submitBOffice", 1);
		} else {
			this.servletRequest.setAttribute("submitBOffice", 0);
		}

		boolean isHQTR = false;
		String sysFlg = ""; // 748
		PersonDetail personDetail = personService.loadWidePersonDetail(employeeId);
		if (personDetail.getCmpCode().equalsIgnoreCase("HQTR")) {
			isHQTR = true;
		}
		OrganDao organDao = this.flowService.getOrganDao();
		SystemGroups systemGroups = organDao.getGroupsByDeptCode(
				personDetail.getDeptCode(),
				personDetail.getA_deptCode(),
				personDetail.getCmpCode(), GroupType.DEPTGROUP);
		sysFlg = systemGroups.getSystemFlg();
		this.servletRequest.setAttribute("isHQTR", isHQTR);
		this.servletRequest.setAttribute("sysFlg", sysFlg);

		return "qianchenRenew";

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

		// 发送邮件给本人和上级主管
		// PersonDetail mgrPerson =
		// personService.getMgrPersonDetail(flow.getActualPerson());
		// mailToPerson(flow.getActualPerson(), flow);
		// mailToPerson(mgrPerson, flow);

		flow = flowService.getFlow(myWork);
		//if (status.equals(FlowStatus.CANCEL)) {
			this.servletRequest.setAttribute("FlowStatus", "CANCEL");
		//}
		return SUCCESS;
	}

	public String cancelForm() {
		String formNum = (String) this.servletRequest.getParameter("formNum");
		// 加载流程详情
		flow = flowService.loadFlow(formNum);
		// 登陆人自己新建一个撤销work 然后完成该work
		String employeeId = super.getLoginUser().getUserName();
		PersonDetail loginPerson = personService
				.loadWidePersonDetail(employeeId);
		MyWork myWork = loginPerson.buildWork(flow.getFlowType(),
				WorkStage.CANCEL, null);

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
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	public String checkMgrPerson() {
		JSONObject json = new JSONObject();
		// 判断下一步处理人
		String workId = this.servletRequest.getParameter("workId");
		MyWork myWork = flowService.loadWork(Long.valueOf(workId));

		PersonDetail loginPerson = this.personService.loadWidePersonDetail(
				myWork.getDeptId(), myWork.getEmployeeId(),
				myWork.getPostCode());
		/******** 不需要进行汇报关系判断代码 start **********/
		// 1.核决主管签核 不需要判断
		Flow flow = flowService.getFlow(myWork);
		//System.out.println(flow.getStatus());
		if (flow.getStatus() == FlowStatus.FINAL_DECISION_START || flow.getStatus() == FlowStatus.FINALPLUS_DECISION_START || flow.getStatus() == FlowStatus.CHAIRMANSIGN_START || flow.getStatus() == FlowStatus.SubmitFBossSIGN_START) {
			// 取出当前登录人
			String curEmployeeId = (String) getSession().get("employeeId");
			if (curEmployeeId.equals(myWork.getJoinStartEmployeeId())) {
				// 当前登录人为核决主管本人,不需要判断汇报关系
				json.put("status", "success");
				this.createJSonData(json.toString());
				return AJAX;
			} else {
				// 取出代理人，代理人可以提交
				AgentRelation agentRelation = flowAgentService.loadAgent(myWork
						.getJoinStartEmployeeId());
				if (null != agentRelation) {
					List<AgentPerson> agentPersonList = flowAgentService
							.loadAgentPersons(agentRelation);
					for (AgentPerson agentPerson : agentPersonList) {
						if (agentPerson.getAgentEmployeeId().equals(
								curEmployeeId)) {
							// 当前登录人为核决主管的代理人,不需要判断汇报关系
							json.put("status", "success");
							this.createJSonData(json.toString());
							return AJAX;
						}
					}
				}
			}
		}
		// 2.本人确认不需要判断
		if (flow.getStatus() == FlowStatus.CONFIRM_START) {
			json.put("status", "success");
			this.createJSonData(json.toString());
			return AJAX;
		}
		// 3.会办结束不需要判断
		if (flow.getStatus() == FlowStatus.CMPCODEJOINTSIGN_START
				|| flow.getStatus() == FlowStatus.JOINTSIGN_START
				|| flow.getStatus() == FlowStatus.SYSTEMJOINTSIGN_START
				|| flow.getStatus() == FlowStatus.CENTERJOINTSIGN_START
				|| flow.getStatus() == FlowStatus.XZJOINTSIGN_START) {
			// 取出当前登录人
			String curEmployeeId = (String) getSession().get("employeeId");
			// edit by Cao_Shengyong 2014-03-26
			if (myWork.getHb_ChengHe().equals("2")) {
				if (curEmployeeId.equals(myWork.getHb_JoinStartEmployeeId())) {
					json.put("status", "success");
					this.createJSonData(json.toString());
					return AJAX;
				} else {
					// 取出代理人，代理人可以提交
					AgentRelation agentRelation = flowAgentService
							.loadAgent(myWork.getHb_JoinStartEmployeeId());
					if (null != agentRelation) {
						List<AgentPerson> agentPersonList = flowAgentService
								.loadAgentPersons(agentRelation);
						for (AgentPerson agentPerson : agentPersonList) {
							if (agentPerson.getAgentEmployeeId().equals(
									curEmployeeId)) {
								// 当前登录人为会办人的代理人,不需要判断汇报关系
								json.put("status", "success");
								this.createJSonData(json.toString());
								return AJAX;
							}
						}
					}
				}
			} else {
				if (curEmployeeId.equals(myWork.getJoinStartEmployeeId())) {
					// 当前登录人为会办本人,不需要判断汇报关系
					json.put("status", "success");
					this.createJSonData(json.toString());
					return AJAX;
				} else {
					// 取出代理人，代理人可以提交
					AgentRelation agentRelation = flowAgentService
							.loadAgent(myWork.getJoinStartEmployeeId());
					if (null != agentRelation) {
						List<AgentPerson> agentPersonList = flowAgentService
								.loadAgentPersons(agentRelation);
						for (AgentPerson agentPerson : agentPersonList) {
							if (agentPerson.getAgentEmployeeId().equals(
									curEmployeeId)) {
								// 当前登录人为会办人的代理人,不需要判断汇报关系
								json.put("status", "success");
								this.createJSonData(json.toString());
								return AJAX;
							}
						}
					}
				}
			}
		}
		
		// 4.内部会办不需要判断
		if (flow.getStatus() == FlowStatus.INNERJOINTSIGN_START){
			json.put("status", "success");
			this.createJSonData(json.toString());
			return AJAX;
		}

		/******** 不需要进行汇报关系判断代码 end **********/

		PersonDetail mgrPerson = null;
		try {
			mgrPerson = personService.getMgrPersonDetail(loginPerson);
		} catch (Exception e) {
			json.put("status", "error");
			json.put("message", "当前处理人或下一步处理人汇报关系维护有误，请联系系统管理员处理");
			this.createJSonData(json.toString());
			return AJAX;
		}

		json.put("status", "success");
		this.createJSonData(json.toString());
		return AJAX;
	}

	/**
	 * 如果会办单位、抄送单位、文件发生变化则更新相关信息
	 */
	private void updateFlowInfo() {
		// 附件上传
		String attachmentIds = this.servletRequest
				.getParameter("attachmentIds");
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
		String huibandeptIds = (String) this.servletRequest
				.getParameter("huibanDeptIds");
		String jointSignDeptName = (String) this.servletRequest
				.getParameter("huibanDept");
		if (StringUtils.isNotEmpty(huibandeptIds)) {
			String[] jointSignDeptIds = StringUtils.split(huibandeptIds, ";");
			// 会办等信息
			flow.setJointSignDeptIds(jointSignDeptIds);
			flow.setJointSignDeptName(jointSignDeptName);
		}

		// 传入的是部门ID
		String chaosongdeptIds = (String) this.servletRequest
				.getParameter("chaosongDeptIds");
		String copyDeptName = (String) this.servletRequest
				.getParameter("chaosongDept");
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
			huibanDeptIds = org.springframework.util.StringUtils
					.arrayToDelimitedString(huibanDepts, ";");
			this.servletRequest.setAttribute("huibanDeptIds", huibanDeptIds);
		}

		// 抄送单位
		String[] chaosongDepts = flow.getCopyDeptIds();
		String chaosongdeptIds = "";
		if (chaosongDepts != null && chaosongDepts.length > 0) {
			chaosongdeptIds = org.springframework.util.StringUtils
					.arrayToDelimitedString(chaosongDepts, ";");
			this.servletRequest
					.setAttribute("chaosongDeptIds", chaosongdeptIds);
		}
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
	 * 将代理、助理的相关信息保存起来，供签核历程使用
	 * 
	 * @param employeeId
	 * @param myWork
	 * @return
	 */
	private MyDelegateWork setDelegateWorkInfo(String employeeId, MyWork myWork) {
		PersonDetail loginPerson = personService
				.loadWidePersonDetail(employeeId);
		MyDelegateWork delegateWork = (MyDelegateWork) myWork;
		delegateWork.setDlgEmployeeId(loginPerson.getEmployeeId());
		delegateWork.setDlgPostCode(loginPerson.getPostCode());
		delegateWork.setDlgDeptId(loginPerson.getDeptId());
		delegateWork.setDlgDeptCode(loginPerson.getDeptCode());
		delegateWork.setDlgADeptCode(loginPerson.getA_deptCode());
		delegateWork.setDlgCmpCode(loginPerson.getCmpCode());
		delegateWork.setDlgEmployeenam(loginPerson.getName());
		delegateWork.setDlgTitnam(loginPerson.getTitname());
		return delegateWork;
	}
	
	public String viewQCDetail() {
		try{
			String formNum = (String) this.servletRequest.getParameter("formNum");
			
			flow = flowService.loadFlow(formNum);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String createTime = sdf.format(new Date(flow.getCreateTime()));
			this.servletRequest.setAttribute("createTime", createTime);
			this.servletRequest.setAttribute("leader", flow.getDecionmaker()
					.toString());
			this.servletRequest.setAttribute("joinType", flow
					.getJointSignType().toString());
			boolean selfconfirm = flow.isSelfConfirm();
			if (selfconfirm) {
				this.servletRequest.setAttribute("confirm", 1);
			} else {
				this.servletRequest.setAttribute("confirm", 0);
			}
			if (flow.isSubmitBoss()) {
				this.servletRequest.setAttribute("submitBoss", 1);
			} else {
				this.servletRequest.setAttribute("submitBoss", 0);
			}
			if (flow.isSubmitFBoss()){
				this.servletRequest.setAttribute("submitFBoss", 1);
			} else {
				this.servletRequest.setAttribute("submitFBoss", 0);
			}
			if (flow.isSubmitBOffice()){
				this.servletRequest.setAttribute("submitBOffice", 1);
			} else {
				this.servletRequest.setAttribute("submitBOffice", 0);
			}
			
			String tmpCurrentUserFullName = super.getLoginUser().getUserRealName() 
					+ "(" + super.getLoginUser().getUserName() + ")";
			LogDTO logDto = new LogDTO();
			logDto.setIP(CommonUtil.getClientIP(servletRequest));
			logDto.setUser(tmpCurrentUserFullName);
			logDto.setControl("查阅");
			logDto.setMessages(flow.getContent().getTitle() + "(" + formNum + ")");
			flowService.saveLog(logDto);
			
		} catch (Exception e) {
			logger.error("加载签呈表单时候发生异常：" + e.getMessage());
			e.printStackTrace();
			addActionError("表单数据异常，请联系管理员核实");
			return ERROR;
		}
		return "qcView";
	}
	
	public String insertLog(){
		String stat = this.servletRequest.getParameter("stat");
		String formnum = this.servletRequest.getParameter("formnum");
		
		String tmpCurrentUserFullName = super.getLoginUser().getUserRealName() 
				+ "(" + super.getLoginUser().getUserName() + ")";
		LogDTO logDto = new LogDTO();
		logDto.setIP(CommonUtil.getClientIP(servletRequest));
		logDto.setUser(tmpCurrentUserFullName);
		
		flow = flowService.loadFlow(formnum);
		
		//String msg = "";
		String tmpStr = "";
		if (stat.equals("A")){
			logDto.setControl("附件");
			FlowAttachment[] attachments = flow.getFlowAttachments();
			if (attachments != null){
				String id = this.servletRequest.getParameter("aid");
				for(FlowAttachment flowAttachment : attachments){
					//if (flowAttachment.getId()
					if (id.equals(String.valueOf(flowAttachment.getId()))){
						tmpStr = flowAttachment.getAttachmentName();
						break;
					}
				}
			}
			logDto.setMessages("附件名称：" + tmpStr + " (" + formnum + ")");
		} else if (stat.equals("P")){
			logDto.setControl("打印");
			logDto.setMessages("打印表单：" + flow.getContent().getTitle() + " (" + formnum + ")");
		} else {
			logDto.setControl("未知");
		}
		
		//logDto.setControl("查阅");
		//logDto.setMessages(flow.getContent().getTitle() + "(" + formNum + ")");
		flowService.saveLog(logDto);
		
		JSONObject json = new JSONObject();
		json.put("status", "success");
		this.createJSonData(json.toString());
		
		return AJAX;
	}
}
