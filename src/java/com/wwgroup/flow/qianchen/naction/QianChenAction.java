package com.wwgroup.flow.qianchen.naction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.mail.MailPreparationException;

import com.wwgroup.common.action.BaseAjaxAction;
import com.wwgroup.common.util.MailUtil;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowAttachment;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.helper.JointSignType;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.flow.service.QCFlowService;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.dao.OrganDao;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

@SuppressWarnings("unused")
public class QianChenAction extends BaseAjaxAction {
	private static final long serialVersionUID = -142445296883587922L;
	
	//private static final Logger logger = Logger.getLogger(QianChenAction.class);

	private Flow flow = new Flow();

	private PersonService personService;

	private QCFlowService flowService;
	
	private UserService userService;

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setFlowService(QCFlowService flowService) {
		this.flowService = flowService;
	}
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public Object getModel() {
		return this.flow;
	}

	/**
	 * 加载签呈信息(主要是申请人)
	 * 
	 * @return
	 */
	public String loadQianChenDetail() {
		try {
			// 获取登录用户
			String employeeId = super.getLoginUser().getUserName();
			String name = super.getLoginUser().getUserRealName();
			// 主要给附件上传信息显示使用
			this.servletRequest.setAttribute("operatorId", employeeId);
			this.servletRequest.setAttribute("operatorName", name);
			
			//System.out.println(this.servletRequest.getLocalAddr());
			//System.out.println(this.servletRequest.getRemoteAddr());

			boolean isHQTR = false;
			String sysFlg = ""; // 748
			if (StringUtils.isNotEmpty(employeeId)) {
				PersonDetail personDetail = personService.loadWidePersonDetail(employeeId);
				this.servletRequest.setAttribute("personDetail", personDetail);
				if (personDetail.getCmpCode().trim().equalsIgnoreCase("HQTR")){
					isHQTR = true;
				}
				OrganDao organDao = this.flowService.getOrganDao();
				SystemGroups systemGroups = organDao.getGroupsByDeptCode(
						personDetail.getDeptCode(),
						personDetail.getA_deptCode(),
						personDetail.getCmpCode(), GroupType.DEPTGROUP);
				sysFlg = systemGroups.getSystemFlg();
				//System.out.println(systemGroups.getSystemFlg() + " ## ");
			}
			this.servletRequest.setAttribute("isHQTR", isHQTR);
			this.servletRequest.setAttribute("sysFlg", sysFlg);
		} catch (Exception e) {
			logger.error("加载签呈表单时候发生异常："+e.getMessage());
			e.printStackTrace();
			addActionError("表单数据异常，请联系管理员核实");
			return ERROR;
		}
		return "toApply";
	}

	/**
	 * 提交签呈申请单
	 * 
	 * @return
	 */
	public String submitQianChenApply() {

		try {
			this.saveBaseFlowInfo();

			// 判断是否为暂存表单
			String template = (String) this.servletRequest.getParameter("template");
			if (template != null && template.equals("1")) {
				flow.setTempalte(true);
			}
			// 判断是否是重新起案后的签呈单
			String type = (String) this.servletRequest.getParameter("type");
			String workId = (String) this.servletRequest.getParameter("workId");
			Map<String, PersonDetail> map = new HashMap<String, PersonDetail>();
			if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(workId) && type.equals("renew")) {
				MyWork myWork = flowService.loadWork(Long.valueOf(workId));
				// 重启案
				map = flowService.submitFlow(flow, myWork);
			} else {
				// 正常提交
				map = flowService.submitFlow(flow, null);
			}
			try {
				//发送邮件
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
			
			// 如果有内部会办则显示会办人列表
			if (flow.getInnerJointSignIds() != null) {
				// 开始内部会办
				this.servletRequest.setAttribute("FlowStatus", "INNERJOINTSIGN_START");
			} else if (flow.getStatus() == FlowStatus.CENTERJOINTSIGN_START){
				this.servletRequest.setAttribute("FlowStatus", "CENTERJOINTSIGN_START");
				configJoinSignPersons(map, nextPerson);
			}else if(flow.getStatus() == FlowStatus.CMPCODEJOINTSIGN_START){
				// 开始同CMPCODE会办
				this.servletRequest.setAttribute("FlowStatus", "CMPCODEJOINTSIGN_START");
				configJoinSignPersons(map, nextPerson);
			} else if (flow.getStatus() == FlowStatus.SYSTEMJOINTSIGN_START){
				this.servletRequest.setAttribute("FlowStatus", "SYSTEMJOINTSIGN_START");
				configJoinSignPersons(map, nextPerson);
			} else if (flow.getStatus() == FlowStatus.JOINTSIGN_START){
				this.servletRequest.setAttribute("FlowStatus", "JOINTSIGN_START");
				configJoinSignPersons(map, nextPerson);
			}else {
				// 呈核开始
				this.servletRequest.setAttribute("FlowStatus", "CHENGHE");
				if (nextPerson != null) {
					// 判断是否有会办单位
					if (flow.getJointSignDeptIds() != null) {
						this.servletRequest.setAttribute("joinType", flow.getJointSignType().name());
						// 判断是下级审核人是否是最终核决主管，如果不是，则继续逐级审核；如果是，则先会办
						if (flow.isLocal()) {
							PersonDetail mgrPerson = personService.getMgrPersonDetail(flow.getActualPerson());
							// if (personService.quailifiedDecisionMaker(flow.getDecionmaker(), nextPerson)) {
							boolean ret = personService.quailifiedDecisionMaker(flow.getDecionmaker(), mgrPerson);
							// 体系内部门主管有会办的情况下是先签再会办
							ret = ret & !(flow.getDecionmaker() == DescionMaker.DEPTLEADER);
							if (ret) {
								this.servletRequest.setAttribute("FlowStatus", "JOINTSIGN_START");
								configJoinSignPersons(map, nextPerson);
							} else {
								this.servletRequest.setAttribute("FlowStatus", "CHENGHE");
							}
						} else {
							// 在转中心的情况下：如果发起单据的这个人本身是单位主管，并且他选择的是"事业部最高主管"，
							// 那么就直接进入会办了，其它情况都是走审核阶段
							boolean result = personService.quailifiedDecisionMaker(DescionMaker.UNITLEADER, flow
									.getActualPerson());
							result = result & flow.getDecionmaker() == DescionMaker.REGINLEADER;
							if (result) {
								this.servletRequest.setAttribute("FlowStatus", "JOINTSIGN_START");
								configJoinSignPersons(map, nextPerson);
							} else {
								this.servletRequest.setAttribute("FlowStatus", "CHENGHE");
							}
						}
						// 发起人确认
					} else if (flow.getStatus().equals(FlowStatus.CONFIRM_START)) {
						this.servletRequest.setAttribute("FlowStatus", "CONFIRM");
					} // 最终核决
					else if (flow.getStatus().equals(FlowStatus.FINAL_DECISION_START)) {
						this.servletRequest.setAttribute("FlowStatus", "FINAL_DECISION_START");
					} else {
						this.servletRequest.setAttribute("FlowStatus", "CHENGHE");
					}
				}
			}
		}catch(UncategorizedDataAccessException e){
			logger.error("提交签呈表单时候发生异常："+e.getMessage());
			e.printStackTrace();
			addActionError("数据插入失败，请联系管理员");
			return ERROR;
		}catch (Exception e) {
			logger.error("提交签呈表单时候发生异常："+e.getMessage());
			e.printStackTrace();
			addActionError(e.getMessage());
			return ERROR;
		}
		return SUCCESS;
	}

	/**
	 * 暂存签呈申请单
	 * 
	 * @return
	 */
	public String saveQianChenApply() {

		this.saveBaseFlowInfo();

		String type = (String) this.servletRequest.getParameter("type");
		String workId = (String) this.servletRequest.getParameter("workId");
		MyWork myWork = null;
		if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(workId) && type.equals("renew")) {
			// 重启案
			myWork = flowService.loadWork(Long.valueOf(workId));
		}
		// 执行保存方法
		this.flowService.saveFlowTemplate(flow, myWork);
		this.createJSonData("{\"success\":true}");
		return AJAX;
	}

	/**
	 * 保存基本的流程信息
	 */
	private void saveBaseFlowInfo() {

		long createTime = System.currentTimeMillis();

		long loginPersonId = flow.getCreatePerson().getId();
		long actualPersonId = flow.getActualPerson().getId();

		String loginEmployeeId = flow.getCreatePerson().getEmployeeId();
		String actualEmployeeId = flow.getActualPerson().getEmployeeId();
		String postcode = flow.getActualPerson().getPostCode();
		String depId = flow.getContent().getDeptId();

		// 代申请人信息
		PersonDetail loginPerson = this.personService.loadWidePersonDetail(loginEmployeeId);
		PersonDetail actualPerson = this.personService.loadWidePersonDetail(depId, actualEmployeeId, postcode);

		if (loginPersonId > 0) {
			loginPerson.setId(loginPersonId);
		}
		if (actualPersonId > 0) {
			actualPerson.setId(actualPersonId);
		}

		// 生成表单编号
		if(StringUtils.isEmpty(flow.getFormNum())){
			String formNum = flowService.generateFormNum(actualPerson, FlowType.QIANCHENG);
			flow.setFormNum(formNum);
		}
		
		// 判断是体系内部or地方到总部
		if (flow.getContent().getType() == 1) {
			flow.setLocal(true);
		}
		flow.setIsNew("1");

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

		// 签呈基本信息
		// flow.setFormNum(formNum);
		flow.setCreateTime(createTime);
		flow.setActualPerson(actualPerson);
		flow.setCreatePerson(loginPerson);

		String leader = this.servletRequest.getParameter("leader");
		flow.setDecionmaker(DescionMaker.valueOf(leader));

		String joinType = this.servletRequest.getParameter("joinType");
		flow.setJointSignType(JointSignType.valueOf(joinType));

		String fsys = (String)this.servletRequest.getParameter("hid_sysFlg");
		if (!StringUtils.isEmpty(fsys)){
			flow.setFileSys(fsys);
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
		
		/**
		 * 这里增加判断，如果会办部门中有总部行政职能部门，则自动修正核决主管
		 * 1、总部：
		 * 		A、职能部门、电解水则修正为单位最高主管 CENTRALLEADER
		 * 		B、其他核决主管为神旺控股执行总经理 UNITLEADER
		 * 2、地方：
		 * 		A、电解水体系修改正为事业部最高主管 REGINLEADER
		 * 		B、其他修正为神旺控股执行总经理 HEADLEADER
		 */
		boolean needEdit = false;
		String[] jointSignDeptIds = flow.getJointSignDeptIds();
		if (jointSignDeptIds != null && jointSignDeptIds.length > 0){
			for(int i = 0; i < jointSignDeptIds.length; i++){
				String jointSignDeptId = jointSignDeptIds[i];
				SystemGroups tmpGroups = flowService.getOrganDao().loadGroupsById(Integer.valueOf(jointSignDeptId));
				if (tmpGroups.getSystemFlg().equals("X")){
					needEdit = true;
					break;
				}
			}
		}
		if (needEdit){
			// 这里获取申请人所属的体系，即表单所属体系 flow.getFileSys()
			// 申请人所属单位
			String cmpcod = actualPerson.getCmpCode();
			String fileSys = flow.getFileSys();
			if (flow.isLocal()){
				// 如果是职能体系
				if (fileSys.equals("X")){
					flow.setDecionmaker(DescionMaker.CENTRALLEADER);
				} else  if (fileSys.equals("F")) {
					// 电解水这里判断不一样，如果是体系内，但是电解水的发文，有可能是地方的体系内，这里也要直接修改为事业部主管
					if (cmpcod.equals("HQTR")){
						flow.setDecionmaker(DescionMaker.CENTRALLEADER);
					} else {
						flow.setLocal(false);
						flow.getContent().setType(2);
						flow.setDecionmaker(DescionMaker.REGINLEADER);
					}
				} else {
					// 其他发文同样要判断是否是地方单位发文
					if (cmpcod.equals("HQTR")){
						flow.setDecionmaker(DescionMaker.UNITLEADER);
					} else {
						flow.setLocal(false);
						flow.getContent().setType(2);
						flow.setDecionmaker(DescionMaker.HEADLEADER);
					}
				}
			} else {
				// 判断是否电解水体系
				if (fileSys.equals("F")){
					flow.setDecionmaker(DescionMaker.REGINLEADER);
				} else {
					flow.setDecionmaker(DescionMaker.HEADLEADER);
				}
				flow.setLocal(false);
				flow.getContent().setType(2);
			}
		}
		/*=======================自动修改核决主管.结束==========================*/
		

		// 传入的是部门ID
		String chaosongdeptIds = (String) this.servletRequest.getParameter("chaosongDeptIds");
		String copyDeptName = (String) this.servletRequest.getParameter("chaosongDept");
		if (StringUtils.isNotEmpty(chaosongdeptIds)) {
			String[] copyDeptIds = StringUtils.split(chaosongdeptIds, ";");
			flow.setCopyDeptIds(copyDeptIds);
			flow.setCopyDeptName(copyDeptName);
		}
		// 传入的是部门ID + 人员ID
		String innerhuibanIds = (String) this.servletRequest.getParameter("innerhuibanIds");
		String innerJointSignName = (String) this.servletRequest.getParameter("innerhuibanNames");
		if (StringUtils.isNotEmpty(innerhuibanIds)) {
			String[] innerJointSignIds = StringUtils.split(innerhuibanIds, ";");
			flow.setInnerJointSignIds(innerJointSignIds);
			flow.setInnerJointSignName(innerJointSignName);
			this.servletRequest.setAttribute("workStage", "INNERJOINTSIGN");
			this.servletRequest.setAttribute("innerhuibanIds", innerhuibanIds);
		}

		// 发起人确认
		String selfconfirm = (String) this.servletRequest.getParameter("selfconfirm");
		if (selfconfirm != null && selfconfirm.equals("1")) {
			flow.setSelfConfirm(true);
		}
		
		//是否呈核总裁
		String submitBoss = (String)this.servletRequest.getParameter("submitBoss");
		if (submitBoss != null && submitBoss.equals("1")) {
			flow.setSubmitBoss(true);
		}
		flow.setFlowType(FlowType.QIANCHENG);
		// 是否呈核副总裁
		String submitFBoss = (String)this.servletRequest.getParameter("submitFBoss");
		if (submitFBoss != null && submitFBoss.equals("1")){
			flow.setSubmitFBoss(true);
		}
		
		String submitBOffice = (String)this.servletRequest.getParameter("submitBOffice");
		if (submitBOffice != null && submitBOffice.equals("1")){
			flow.setSubmitBOffice(true);
		}
		
		String chairman = (String)this.servletRequest.getParameter("chairman");
		if (chairman != null && chairman.equals("1")){
			flow.setChariman(true);
		}
	}
	
	/**
	 * 检查会办的单位是否有主管
	 * @return
	 */
	public String confirmJoinDeptMgrPerson(){
		JSONObject json = new JSONObject();
		//判断下一步处理人
		String actualEmployeeId = flow.getActualPerson().getEmployeeId();
		String postcode = flow.getActualPerson().getPostCode();
		String depId = flow.getContent().getDeptId();
		PersonDetail loginPerson = this.personService.loadWidePersonDetail(depId, actualEmployeeId, postcode);
		PersonDetail mgrPerson = null;
		try {
			mgrPerson = personService.getMgrPersonDetail(loginPerson);
		} catch (Exception e) {
			json.put("status","error");
			json.put("message","当前处理人或下一步处理人汇报关系维护有误，请联系系统管理员处理");
			this.createJSonData(json.toString());
			return AJAX;
		}
		
		
		
		json.put("status", "success");
		StringBuffer sb = new StringBuffer();
		String huibandeptIds = (String) this.servletRequest.getParameter("huibanDeptIds");
		String huibandeptNames = (String) this.servletRequest.getParameter("huibanDept");
		PersonDetail person = null;
		if(StringUtils.isNotBlank(huibandeptIds)){
			String[] jointSignDeptIds = StringUtils.split(huibandeptIds, ";");
			String[] jointSignDeptNames = StringUtils.split(huibandeptNames, ";");
			for(int i=0;i<jointSignDeptIds.length;i++){
				person = null;
				String jointSignDeptId = jointSignDeptIds[i];
				String jointSignDeptName = jointSignDeptNames[i];
				SystemGroups tmpGroups = flowService.loadGroupsById(Integer.valueOf(jointSignDeptId));
				if (tmpGroups.getSystemFlg().equals("Y")){
					person = personService.loadWideMgrPersonDetailPlus(jointSignDeptId, GroupType.DEPTGROUP);
				} else {
					person = personService.loadWideMgrPersonDetail(jointSignDeptId);
				}
				if(null == person || StringUtils.isBlank(person.getEmployeeId())){
					//部门主管不存在
					if(sb.length()>0){
						sb.append(",");
					}
					sb.append(jointSignDeptName);
				}
			}
		}
		json.put("noMgrPersonDepts", sb.toString());
		this.createJSonData(json.toString());
		return AJAX;
	}

	/**
	 * 构造会办人列表信息
	 * 
	 * @param map
	 * @param nextPerson
	 */
	private void configJoinSignPersons(Map<String, PersonDetail> map, PersonDetail nextPerson) {
		// 显示会办人列表，id+name的组合
		List<String> persons = new ArrayList<String>();
		for (String key : map.keySet()) {
			nextPerson = map.get(key);
			persons.add(nextPerson.getEmployeeId() + "," + nextPerson.getName());
		}
		String[] arr = (String[]) persons.toArray(new String[persons.size()]);
		if (arr != null && arr.length > 0) {
			String huibanpersons = org.springframework.util.StringUtils.arrayToDelimitedString(arr, ";");
			this.servletRequest.setAttribute("huibanpersons", huibanpersons);
		}
	}
}
