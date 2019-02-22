package com.wwgroup.flow.neilian.naction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
import com.wwgroup.flow.service.NLFlowService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.dao.OrganDao;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

public class NeiLianAction extends BaseAjaxAction {
	private static final long serialVersionUID = 6471924915418431782L;

	private Flow flow = new Flow();

	private PersonService personService;

	private NLFlowService flowService;

	private UserService userService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setFlowService(NLFlowService flowService) {
		this.flowService = flowService;
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
	 * 加载内联信息(主要是申请人)
	 * 
	 * @return
	 */
	public String loadNeiLianDetail() {
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		String name = super.getLoginUser().getUserRealName();
		// 主要给附件上传信息显示使用
		this.servletRequest.setAttribute("operatorId", employeeId);
		this.servletRequest.setAttribute("operatorName", name);

		boolean isHQTR = false;
		String sysFlg = "";
		if (StringUtils.isNotEmpty(employeeId)) {
			PersonDetail personDetail = personService
					.loadWidePersonDetail(employeeId);
			String deptPath = personDetail.getDeptPath();
			splitDeptPath(deptPath);
			this.servletRequest.setAttribute("personDetail", personDetail);

			if (personDetail.getCmpCode().trim().equalsIgnoreCase("HQTR")) {
				isHQTR = true;
			}
			OrganDao organDao = this.flowService.getOrganDao();
			SystemGroups systemGroups = organDao.getGroupsByDeptCode(
					personDetail.getDeptCode(), personDetail.getA_deptCode(),
					personDetail.getCmpCode(), GroupType.DEPTGROUP);
			sysFlg = systemGroups.getSystemFlg();
		}
		this.servletRequest.setAttribute("isHQTR", isHQTR);
		this.servletRequest.setAttribute("sysFlg", sysFlg);
		return "toApply";
	}

	/**
	 * 提交内联申请单
	 * 
	 * @return
	 */
	public String submitNeiLianApply() {

		try {
			this.saveBaseFlowInfo();

			// 判断是否为暂存表单
			String template = (String) this.servletRequest
					.getParameter("template");
			if (template != null && template.equals("1")) {
				flow.setTempalte(true);
			}

			// 判断是否是重新起案后的签呈单
			String type = (String) this.servletRequest.getParameter("type");
			String workId = (String) this.servletRequest.getParameter("workId");
			Map<String, PersonDetail> map = new HashMap<String, PersonDetail>();
			if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(workId)
					&& type.equals("renew")) {
				MyWork myWork = flowService.loadWork(Long.valueOf(workId));
				// 重启案
				map = flowService.submitFlow(flow, myWork);
			} else {
				// 正常提交
				map = flowService.submitFlow(flow, null);
			}

			// 发送邮件
			try {
				for (String key : map.keySet()) {
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

			// 如果有内部会办，则显示会办人列表
			if (flow.getInnerJointSignIds() != null) {
				this.servletRequest.setAttribute("FlowStatus",
						"INNERJOINTSIGN_START");
			} else if (flow.getStatus() == FlowStatus.CENTERJOINTSIGN_START) {
				this.servletRequest.setAttribute("FlowStatus",
						"CENTERJOINTSIGN_START");
				configJoinSignPersons(map, nextPerson);
			} else if (flow.getStatus() == FlowStatus.JOINTSIGN_START) {
				this.servletRequest.setAttribute("FlowStatus",
						"JOINTSIGN_START");
				configJoinSignPersons(map, nextPerson);
			} else {
				this.servletRequest.setAttribute("FlowStatus", "CHENGHE");
				// 判断是否有会办单位
				if (flow.getJointSignDeptIds() != null) {
					this.servletRequest.setAttribute("joinType", flow
							.getJointSignType().name());
					// 判断审核人是否是最终核决主管，如果不是，则继续逐级审核；如果是，则先会办
					PersonDetail mgrPerson = personService.getMgrPersonDetail(flow.getActualPerson());
					boolean ret = personService.quailifiedDecisionMaker(flow.getDecionmaker(), mgrPerson);
					ret = ret & !(flow.getDecionmaker() == DescionMaker.DEPTLEADER);
					if (ret) {
						this.servletRequest.setAttribute("FlowStatus",
								"JOINTSIGN_START");
						configJoinSignPersons(map, nextPerson);
					} else {
						this.servletRequest.setAttribute("FlowStatus",
								"CHENGHE");
					}
					// 发起人确认
				} else if (flow.getStatus().equals(FlowStatus.CONFIRM_START)) {
					this.servletRequest.setAttribute("FlowStatus", "CONFIRM");
				} else if (flow.getStatus().equals(
						FlowStatus.FINAL_DECISION_START)) {
					this.servletRequest.setAttribute("FlowStatus",
							"FINAL_DECISION_START");
				} else {
					this.servletRequest.setAttribute("FlowStatus", "CHENGHE");
				}
			}
			/*
			 * if (nextPerson != null) { // 判断是否有会办单位 if
			 * (flow.getJointSignDeptIds() != null) {
			 * this.servletRequest.setAttribute("joinType",
			 * flow.getJointSignType().name()); //
			 * 判断是下级审核人是否是最终核决主管，如果不是，则继续逐级审核；如果是，则先会办 PersonDetail mgrPerson =
			 * personService.getMgrPersonDetail(flow.getActualPerson()); if
			 * (personService.quailifiedDecisionMaker(flow.getDecionmaker(),
			 * mgrPerson)) { this.servletRequest.setAttribute("FlowStatus",
			 * "JOINTSIGN_START"); configJoinSignPersons(map, nextPerson); }
			 * else { this.servletRequest.setAttribute("FlowStatus", "CHENGHE");
			 * } // 发起人确认 } else if
			 * (flow.getStatus().equals(FlowStatus.CONFIRM_START)) {
			 * this.servletRequest.setAttribute("FlowStatus", "CONFIRM"); //
			 * 最终核决 } else if
			 * (flow.getStatus().equals(FlowStatus.FINAL_DECISION_START)) {
			 * this.servletRequest.setAttribute("FlowStatus",
			 * "FINAL_DECISION_START"); } else {
			 * this.servletRequest.setAttribute("FlowStatus", "CHENGHE"); } }
			 */
		} catch (UncategorizedDataAccessException e) {
			logger.error("提交内联表单时候发生异常：" + e.getMessage());
			e.printStackTrace();
			addActionError("数据插入失败，请联系管理员");
			return ERROR;
		} catch (Exception e) {
			logger.error("提交内联表单时候发生异常：" + e.getMessage());
			e.printStackTrace();
			addActionError(e.getMessage());
			return ERROR;
		}

		return SUCCESS;
	}

	/**
	 * 暂存内联申请单
	 * 
	 * @return
	 */
	public String saveNeiLianApply() {
		try {
			this.saveBaseFlowInfo();

			String type = (String) this.servletRequest.getParameter("type");
			String workId = (String) this.servletRequest.getParameter("workId");
			MyWork myWork = null;
			if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(workId)
					&& type.equals("renew")) {
				// 重启案
				myWork = flowService.loadWork(Long.valueOf(workId));
			}
			// 执行保存方法
			this.flowService.saveFlowTemplate(flow, myWork);

			this.createJSonData("{\"success\":true}");
		} catch (Exception e) {
			logger.error("暂存内联表单时候发生异常：" + e.getMessage());
			e.printStackTrace();
			addActionError(e.getMessage());
			return ERROR;
		}
		return AJAX;
	}

	/**
	 * 检查会办的单位是否有主管
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	public String confirmJoinDeptMgrPerson() {
		JSONObject json = new JSONObject();

		// 判断下一步处理人
		String actualEmployeeId = flow.getActualPerson().getEmployeeId();
		String postcode = flow.getActualPerson().getPostCode();
		String depId = flow.getContent().getDeptId();
		PersonDetail loginPerson = this.personService.loadWidePersonDetail(
				depId, actualEmployeeId, postcode);
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
		StringBuffer sb = new StringBuffer();
		String huibandeptIds = (String) this.servletRequest
				.getParameter("huibanDeptIds");
		String huibandeptNames = (String) this.servletRequest
				.getParameter("huibanDept");
		PersonDetail person = null;
		if (StringUtils.isNotBlank(huibandeptIds)) {
			String[] jointSignDeptIds = StringUtils.split(huibandeptIds, ";");
			String[] jointSignDeptNames = StringUtils.split(huibandeptNames,
					";");
			for (int i = 0; i < jointSignDeptIds.length; i++) {
				String jointSignDeptId = jointSignDeptIds[i];
				String jointSignDeptName = jointSignDeptNames[i];
				SystemGroups tmpGroups = flowService.loadGroupsById(Integer.valueOf(jointSignDeptId));
				if (tmpGroups.getSystemFlg().equals("Y")){
					person = personService.loadWideMgrPersonDetailPlus(jointSignDeptId, GroupType.DEPTGROUP);
				} else {
					person = personService.loadWideMgrPersonDetail(jointSignDeptId);
				}
				if (null == person
						|| StringUtils.isBlank(person.getEmployeeId())) {
					// 部门主管不存在
					if (sb.length() > 0) {
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
	 * 保存基本的流程信息
	 */
	private void saveBaseFlowInfo() {

		long createTime = System.currentTimeMillis();

		long loginPersonId = flow.getCreatePerson().getId();
		// String loginEmployeeId = flow.getCreatePerson().getEmployeeId();

		// 申请人信息(对于内联，loginPerson就是actualPerson)
		// PersonDetail loginPerson =
		// this.personService.loadWidePersonDetail(loginEmployeeId);
		String actualEmployeeId = flow.getActualPerson().getEmployeeId();
		String postcode = flow.getActualPerson().getPostCode();
		String depId = flow.getContent().getDeptId();
		PersonDetail loginPerson = this.personService.loadWidePersonDetail(
				depId, actualEmployeeId, postcode);

		if (loginPersonId > 0) {
			loginPerson.setId(loginPersonId);
		}

		// 生成表单编号
		if (StringUtils.isEmpty(flow.getFormNum())) {
			String formNum = flowService.generateFormNum(loginPerson,
					FlowType.NEILIAN);
			flow.setFormNum(formNum);
		}

		// 只有体系内
		flow.setLocal(true);

		// 设置为改造后的记录标识
		flow.setIsNew("1");

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

		// 会办基本信息
		// flow.setFormNum(formNum);
		flow.setCreateTime(createTime);
		flow.setActualPerson(loginPerson);

		PersonDetail createPerson = new PersonDetail();
		BeanUtils.copyProperties(loginPerson, createPerson);
		flow.setCreatePerson(createPerson);

		String leader = this.servletRequest.getParameter("leader");
		flow.setDecionmaker(DescionMaker.valueOf(leader));

		String joinType = this.servletRequest.getParameter("joinType");
		flow.setJointSignType(JointSignType.valueOf(joinType));

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

		// 传入的是部门ID + 人员ID
		String innerhuibanIds = (String) this.servletRequest
				.getParameter("innerhuibanIds");
		String innerJointSignName = (String) this.servletRequest
				.getParameter("innerhuibanNames");
		if (StringUtils.isNotEmpty(innerhuibanIds)) {
			String[] innerJointSignIds = StringUtils.split(innerhuibanIds, ";");
			flow.setInnerJointSignIds(innerJointSignIds);
			flow.setInnerJointSignName(innerJointSignName);
			this.servletRequest.setAttribute("workStage", "INNERJOINTSIGN");
			this.servletRequest.setAttribute("innerhuibanIds", innerhuibanIds);
		}

		// 是否需要本人确认
		String selfconfirm = (String) this.servletRequest
				.getParameter("selfconfirm");
		if (selfconfirm != null && selfconfirm.equals("1")) {
			flow.setSelfConfirm(true);
		}

		flow.setFlowType(FlowType.NEILIAN);

		String submitBOffice = (String)this.servletRequest.getParameter("submitBOffice");
		if (submitBOffice != null && submitBOffice.equals("1")){
			flow.setSubmitBOffice(true);
		}
		String fsys = (String)this.servletRequest.getParameter("hid_sysFlg");
		if (!StringUtils.isEmpty(fsys)){
			flow.setFileSys(fsys);
		}
	}

	/**
	 * 构造会办人列表信息
	 * 
	 * @param map
	 * @param nextPerson
	 */
	private void configJoinSignPersons(Map<String, PersonDetail> map,
			PersonDetail nextPerson) {
		// 显示会办人列表，id+name的组合
		List<String> persons = new ArrayList<String>();
		for (String key : map.keySet()) {
			nextPerson = map.get(key);
			persons.add(nextPerson.getEmployeeId() + "," + nextPerson.getName());
		}
		String[] arr = (String[]) persons.toArray(new String[persons.size()]);
		if (arr != null && arr.length > 0) {
			String huibanpersons = org.springframework.util.StringUtils
					.arrayToDelimitedString(arr, ";");
			this.servletRequest.setAttribute("huibanpersons", huibanpersons);
		}
	}

}
