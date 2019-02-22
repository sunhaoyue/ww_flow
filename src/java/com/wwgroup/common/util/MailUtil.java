package com.wwgroup.common.util;

import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wwgroup.common.mail.MailContent;
import com.wwgroup.common.mail.MimeMail;
import com.wwgroup.common.mail.constants.MailConstants;
import com.wwgroup.common.mail.factory.MailFactory;
import com.wwgroup.flow.bo.AgentPerson;
import com.wwgroup.flow.bo.AgentRelation;
import com.wwgroup.flow.bo.AssistRelation;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.dao.FlowDao;
import com.wwgroup.flow.service.FlowAgentService;
import com.wwgroup.flow.service.FlowAssistService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

public class MailUtil {
	
	private static Log logger = LogFactory.getLog(MailUtil.class);
	
	private static FlowDao flowDao;
	
	private static UserService userService;
	
	private static FlowAgentService flowAgentService;
	
	private static FlowAssistService flowAssistService;
	
	private static PersonService personService;
	
	private static boolean sendMail = SysConfig.getInstance().getBoolean("send_mail", false);
	
	public static void sendMail(MailContent mailContent, Flow flow) {
		//System.out.println(mailContent.getEmail());

		//判断配置文件中邮件发送开关是否开启,如果该人没有邮箱，也不发送邮件
		if (!sendMail || StringUtils.isEmpty(mailContent.getEmail())) return;

		MimeMail mail = MailFactory.createMimeMail();
		mail.addTo(mailContent.getEmail());
		mail.setCc(mailContent.getCc());
		try {
			mail.setSubject(SysConfig.getInstance().getString(
					MailConstants.SYSTEM_ANNOUNCEMENT));
			
			mailContent.getUserName();
			mailContent.getTitle();
			
			//抄送
			if (flow.getStatus().equals(FlowStatus.COPY_SEND)) {
				mail.setSubject("【" + mailContent.getActualPersonName() + "】" + "的" + "【" + mailContent.getTitle() + "】" + "核准通知");
			
			//驳回
			} else if (flow.getStatus().equals(FlowStatus.REJECT)) {
				mail.setSubject("【" + mailContent.getActualPersonName() + "】" + "的" + "【" + mailContent.getTitle() + "】" + "驳回通知");
				
			//核准
			} else if (flow.getStatus().equals(FlowStatus.FINAL_DECISION_END)) {
				mail.setSubject("【" + mailContent.getActualPersonName() + "】" + "的" + "【" + mailContent.getTitle() + "】" + "核准通知");
				
			//待办
			} else {
				mail.setSubject("【" + mailContent.getActualPersonName() + "】" + "的" + "【" + mailContent.getTitle() + "】" + "需要您审核");
				
			}
			
			if (flow.getFlowType().equals(FlowType.QIANCHENG))
				mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/qianchenapply.html"));
			if (flow.getFlowType().equals(FlowType.NEILIAN))
				mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/neilianapply.html"));
			
			String text = mail.getText();
			BeanMap map = new BeanMap(mailContent);
			for (Object key : map.keySet()) {
				text = text.replaceAll(MailConstants.TEMPLATE_OCCUPY
						+ String.valueOf(key) + MailConstants.TEMPLATE_OCCUPY,
						String.valueOf(map.get(key)));
			}
			mail.setText(text);
			mail.setHtml(true);
			//System.out.println("Send Mail DEBUG: " + flow.getFormNum() + " # " + mailContent.getEmail());
			logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " sendMail to: " + mailContent.getEmail());
			mail.send();
		} catch (Exception ex) {
			logger.info(flow.getFormNum() + "　EORROR sendMail to: " + mailContent.getEmail());
			String msg = "无法连接邮件服务器或邮件地址存在错误，通知邮件发送失败！请将此问题反馈给系统管理员";
			logger.error(msg);
			//throw new MailPreparationException(msg);
		}
	}
	
	
	
	public static void mailToPerson(PersonDetail nextPerson, Flow flow) {
		
		//判断配置文件中邮件发送开关是否开启
		if (!sendMail) return;
		
		if(null == nextPerson) return;
		
		//判断是否有代理人且岗位符合
		AgentRelation agentRelation = flowAgentService.loadAgent(nextPerson.getEmployeeId());
		Date now = new Date();
		List<AgentPerson> agentPersons = null;
		if (agentRelation != null 
				&& agentRelation.getStartDate() != null
				&& agentRelation.getEndDate() != null
				&& agentRelation.getStartDate().before(now)	//当前日期大于开始日期 
				&& agentRelation.getEndDate().after(now)) {	//当前日期小于结束日期
			agentPersons = flowAgentService.loadAgentPersons(agentRelation);
			for (AgentPerson agentPeson : agentPersons) {
				if (agentPeson.getAgentPostCode().equals(nextPerson.getPostCode())) {
					nextPerson = personService.loadWidePersonDetail(agentPeson.getAgentEmployeeId());
					break;
				}
			}
		}
		
		MailContent mailContent = new MailContent();
		mailContent.setFormNum(flow.getFormNum());
		mailContent.setUserName(nextPerson.getName());
		mailContent.setActualPersonName(flow.getActualPerson().getName());
		mailContent.setTitle(flowDao.loadContent(flow.getId()).getTitle());
		
		//判断是否有助理人
		List<AssistRelation> assistRelationList = flowAssistService.findAll(nextPerson.getEmployeeId());
		for(AssistRelation curAssistRelation : assistRelationList){
			SystemUsers systemUser = userService.getUsersByName(curAssistRelation.getSelectedAssistEmployeeId());
			if(null != systemUser 
					&& StringUtils.isNotBlank(systemUser.getEmail())
					&& curAssistRelation.getSelectedPostCode().equals(nextPerson.getPostCode())){//发信给当前签核主管对应岗位的助理人
				mailContent.getCc().add(systemUser.getEmail());
			}
		}
				
		SystemUsers systemUsers = userService.getUsersByName(nextPerson.getEmployeeId());
		if(null == systemUsers){
			return;
		}
		//System.out.println("邮件发送: " + systemUsers.getFullName() + " $ " + systemUsers.getEmail());
		String email = systemUsers.getEmail();
		if(StringUtils.isBlank(email)){
			return;
		}
		mailContent.setEmail(email);
		
		MailUtil.sendMail(mailContent, flow);
	}
	
	public static void mailToPerson(PersonDetail nextPerson, Flow flow,
			String param) {

		// 判断配置文件中邮件发送开关是否开启
		if (!sendMail)
			return;

		if (null == nextPerson)
			return;

		// 判断是否有代理人且岗位符合
		AgentRelation agentRelation = flowAgentService.loadAgent(nextPerson
				.getEmployeeId());
		Date now = new Date();
		List<AgentPerson> agentPersons = null;
		if (agentRelation != null && agentRelation.getStartDate() != null
				&& agentRelation.getEndDate() != null
				&& agentRelation.getStartDate().before(now) // 当前日期大于开始日期
				&& agentRelation.getEndDate().after(now)) { // 当前日期小于结束日期
			agentPersons = flowAgentService.loadAgentPersons(agentRelation);
			for (AgentPerson agentPeson : agentPersons) {
				if (agentPeson.getAgentPostCode().equals(
						nextPerson.getPostCode())) {
					nextPerson = personService.loadWidePersonDetail(agentPeson
							.getAgentEmployeeId());
					break;
				}
			}
		}

		MailContent mailContent = new MailContent();
		mailContent.setFormNum(flow.getFormNum());
		mailContent.setUserName(nextPerson.getName());
		mailContent.setActualPersonName(flow.getActualPerson().getName());
		mailContent.setTitle(flowDao.loadContent(flow.getId()).getTitle());

		// 判断是否有助理人
		List<AssistRelation> assistRelationList = flowAssistService
				.findAll(nextPerson.getEmployeeId());
		for (AssistRelation curAssistRelation : assistRelationList) {
			SystemUsers systemUser = userService
					.getUsersByName(curAssistRelation
							.getSelectedAssistEmployeeId());
			if (null != systemUser
					&& StringUtils.isNotBlank(systemUser.getEmail())
					&& curAssistRelation.getSelectedPostCode().equals(
							nextPerson.getPostCode())) {// 发信给当前签核主管对应岗位的助理人
				mailContent.getCc().add(systemUser.getEmail());
			}
		}

		SystemUsers systemUsers = userService.getUsersByName(nextPerson
				.getEmployeeId());
		if (null == systemUsers) {
			return;
		}
		// System.out.println("邮件发送: " + systemUsers.getFullName() + " $ " +
		// systemUsers.getEmail());
		String email = systemUsers.getEmail();
		if (StringUtils.isBlank(email)) {
			return;
		}
		mailContent.setEmail(email);

		MailUtil.sendMail(mailContent, flow, param);
	}
	
	public static void sendMail(MailContent mailContent, Flow flow, String param) {
		// System.out.println(mailContent.getEmail());

		// 判断配置文件中邮件发送开关是否开启,如果该人没有邮箱，也不发送邮件
		if (!sendMail || StringUtils.isEmpty(mailContent.getEmail()))
			return;

		MimeMail mail = MailFactory.createMimeMail();
		mail.addTo(mailContent.getEmail());
		mail.setCc(mailContent.getCc());
		try {
			mail.setSubject(SysConfig.getInstance().getString(
					MailConstants.SYSTEM_ANNOUNCEMENT));

			mailContent.getUserName();
			mailContent.getTitle();
			
			if (StringUtils.isEmpty(param)){
				if (flow.getStatus().equals(FlowStatus.REJECT)) {
					mail.setSubject("【" + mailContent.getActualPersonName() + "】"
							+ "的" + "【" + mailContent.getTitle() + "】" + "驳回通知");
					// 核准
				} else if (flow.getStatus().equals(FlowStatus.END)) {
					mail.setSubject("【" + mailContent.getActualPersonName() + "】"
							+ "的" + "【" + mailContent.getTitle() + "】" + "核准通知");
					// 待办
				} else {
					mail.setSubject("【" + mailContent.getActualPersonName() + "】"
							+ "的" + "【" + mailContent.getTitle() + "】" + "需要您审核");
				}
			} else {
				mail.setSubject("【" + mailContent.getActualPersonName() + "】"
							+ "的" + "【" + mailContent.getTitle() + "】" + " " + param);
			}

			if (flow.getFlowType().equals(FlowType.QIANCHENG))
				mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/qianchenapply.html"));
			if (flow.getFlowType().equals(FlowType.NEILIAN))
				mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/neilianapply.html"));

			String text = mail.getText();
			BeanMap map = new BeanMap(mailContent);
			for (Object key : map.keySet()) {
				text = text.replaceAll(
						MailConstants.TEMPLATE_OCCUPY + String.valueOf(key)
								+ MailConstants.TEMPLATE_OCCUPY,
						String.valueOf(map.get(key)));
			}
			mail.setText(text);
			mail.setHtml(true);
			// System.out.println("Send Mail DEBUG: " + flow.getFormNum() +
			// " # " + mailContent.getEmail());
			logger.info(flow.getFormNum() + " 状态：" + flow.getStatus()
					+ " sendMail to: " + mailContent.getEmail());
			mail.send();
		} catch (Exception ex) {
			logger.info(flow.getFormNum() + "　EORROR sendMail to: "
					+ mailContent.getEmail());
			String msg = "无法连接邮件服务器或邮件地址存在错误，通知邮件发送失败！请将此问题反馈给系统管理员";
			logger.error(msg);
			// throw new MailPreparationException(msg);
		}
	}

	public static void sendMailx(MailContent mailContent, Flow flow) {
		//System.out.println(mailContent.getEmail());

		//判断配置文件中邮件发送开关是否开启,如果该人没有邮箱，也不发送邮件
		if (!sendMail || StringUtils.isEmpty(mailContent.getEmail())) return;

		MimeMail mail = MailFactory.createMimeMail();
		mail.addTo(mailContent.getEmail());
		mail.setCc(mailContent.getCc());
		try {
			mail.setSubject(SysConfig.getInstance().getString(
					MailConstants.SYSTEM_ANNOUNCEMENT));
			
			mailContent.getUserName();
			mailContent.getTitle();
			
			//待审核
			if (flow.getStatus().equals(FlowStatus.NEXTFINAL_DECISION_START)) {
				mail.setSubject("【" + mailContent.getActualPersonName() + "】" + "的" + "【" + mailContent.getTitle() + "】" + "请您查阅");
				if (flow.getFlowType().equals(FlowType.QIANCHENG))
					mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/qianchenapplyboss.html"));
				if (flow.getFlowType().equals(FlowType.NEILIAN))
					mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/neilianapplyboss.html"));
			//核准
			} else if (flow.getStatus().equals(FlowStatus.FINAL_DECISION_START)) {
				mail.setSubject("【" + mailContent.getActualPersonName() + "】" + "的" + "【" + mailContent.getTitle() + "】" + "请您查阅");
				if (flow.getFlowType().equals(FlowType.QIANCHENG))
					mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/qianchenapplybossa.html"));
				if (flow.getFlowType().equals(FlowType.NEILIAN))
					mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/neilianapplybossa.html"));
			//待办
			} else {
				mail.setSubject("【" + mailContent.getActualPersonName() + "】" + "的" + "【" + mailContent.getTitle() + "】" + "请您查阅");
				if (flow.getFlowType().equals(FlowType.QIANCHENG))
					mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/qianchenapplybossb.html"));
				if (flow.getFlowType().equals(FlowType.NEILIAN))
					mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/neilianapplyboss.html"));
			}
			
		/*	if (flow.getFlowType().equals(FlowType.QIANCHENG))
				mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/qianchenapplyboss.html"));
			if (flow.getFlowType().equals(FlowType.NEILIAN))
				mail.setTemplate(MailUtil.class.getResourceAsStream("/mailTemplate/neilianapplyboss.html"));
			*/
			String text = mail.getText();
			BeanMap map = new BeanMap(mailContent);
			for (Object key : map.keySet()) {
				text = text.replaceAll(MailConstants.TEMPLATE_OCCUPY
						+ String.valueOf(key) + MailConstants.TEMPLATE_OCCUPY,
						String.valueOf(map.get(key)));
			}
			mail.setText(text);
			mail.setHtml(true);
			//System.out.println("Send Mail DEBUG: " + flow.getFormNum() + " # " + mailContent.getEmail());
			logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " sendMail to: " + mailContent.getEmail());
			mail.send();
		} catch (Exception ex) {
			logger.info(flow.getFormNum() + "　EORROR sendMail to: " + mailContent.getEmail());
			String msg = "无法连接邮件服务器或邮件地址存在错误，通知邮件发送失败！请将此问题反馈给系统管理员";
			logger.error(msg);
			//throw new MailPreparationException(msg);
		}
	}
	
	
	
	public static void mailTotmpPerson(PersonDetail nextPerson, Flow flow) {
		
		//判断配置文件中邮件发送开关是否开启
		if (!sendMail) return;
		
		if(null == nextPerson) return;
		
		//判断是否有代理人且岗位符合
		AgentRelation agentRelation = flowAgentService.loadAgent(nextPerson.getEmployeeId());
		Date now = new Date();
		List<AgentPerson> agentPersons = null;
		if (agentRelation != null 
				&& agentRelation.getStartDate() != null
				&& agentRelation.getEndDate() != null
				&& agentRelation.getStartDate().before(now)	//当前日期大于开始日期 
				&& agentRelation.getEndDate().after(now)) {	//当前日期小于结束日期
			agentPersons = flowAgentService.loadAgentPersons(agentRelation);
			for (AgentPerson agentPeson : agentPersons) {
				if (agentPeson.getAgentPostCode().equals(nextPerson.getPostCode())) {
					nextPerson = personService.loadWidePersonDetail(agentPeson.getAgentEmployeeId());
					break;
				}
			}
		}
		
		MailContent mailContent = new MailContent();
		mailContent.setFormNum(flow.getFormNum());
		mailContent.setUserName(nextPerson.getName());
		mailContent.setActualPersonName(flow.getActualPerson().getName());
		mailContent.setTitle(flowDao.loadContent(flow.getId()).getTitle());
		
		//判断是否有助理人
		List<AssistRelation> assistRelationList = flowAssistService.findAll(nextPerson.getEmployeeId());
		for(AssistRelation curAssistRelation : assistRelationList){
			SystemUsers systemUser = userService.getUsersByName(curAssistRelation.getSelectedAssistEmployeeId());
			if(null != systemUser 
					&& StringUtils.isNotBlank(systemUser.getEmail())
					&& curAssistRelation.getSelectedPostCode().equals(nextPerson.getPostCode())){//发信给当前签核主管对应岗位的助理人
				mailContent.getCc().add(systemUser.getEmail());
			}
		}
				
		SystemUsers systemUsers = userService.getUsersByName(nextPerson.getEmployeeId());
		if(null == systemUsers){
			return;
		}
		//System.out.println("邮件发送: " + systemUsers.getFullName() + " $ " + systemUsers.getEmail());
		String email = systemUsers.getEmail();
		if(StringUtils.isBlank(email)){
			return;
		}
		mailContent.setEmail(email);
		
		MailUtil.sendMailx(mailContent, flow);
	}
	
	public FlowDao getFlowDao() {
		return flowDao;
	}



	public void setFlowDao(FlowDao flowDao) {
		MailUtil.flowDao = flowDao;
	}



	public UserService getUserService() {
		return userService;
	}



	public void setUserService(UserService userService) {
		MailUtil.userService = userService;
	}



	public FlowAgentService getFlowAgentService() {
		return flowAgentService;
	}



	public void setFlowAgentService(FlowAgentService flowAgentService) {
		MailUtil.flowAgentService = flowAgentService;
	}



	public PersonService getPersonService() {
		return personService;
	}



	public void setPersonService(PersonService personService) {
		MailUtil.personService = personService;
	}



	public FlowAssistService getFlowAssistService() {
		return flowAssistService;
	}



	public void setFlowAssistService(FlowAssistService flowAssistService) {
		MailUtil.flowAssistService = flowAssistService;
	}

}
