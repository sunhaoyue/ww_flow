package com.wwgroup.common.mail.factory;

import java.util.Properties;

import com.wwgroup.common.mail.MimeMail;
import com.wwgroup.common.mail.SimpleMail;
import com.wwgroup.common.mail.constants.MailConstants;
import com.wwgroup.common.util.SysConfig;

/**
 * 邮件工厂
 * 
 * 
 */

public class MailFactory {

	/**
	 * 创建简单邮件
	 * 
	 * @return
	 */
	static public SimpleMail createSimpleMail() {
		SimpleMail mail = new SimpleMail();
		mail.setHostname(SysConfig.getInstance().getString(
				MailConstants.MAIL_HOST));
		mail.setPort(SysConfig.getInstance().getInt((MailConstants.MAIL_PORT)));
		mail.setProtocol(SysConfig.getInstance().getString(
				MailConstants.MAIL_PROTOCOL));
		mail.setUsername(SysConfig.getInstance().getString(
				MailConstants.MAIL_USERNAME));
		mail.setPassword(SysConfig.getInstance().getString(
				MailConstants.MAIL_PASSWORD));
		mail
				.setFrom(SysConfig.getInstance().getString(
						MailConstants.MAIL_FROM));
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", SysConfig.getInstance().getString(
				MailConstants.MAIL_SMTP_AUTH));
		properties.put("mail.smtp.timeout", SysConfig.getInstance().getString(
				MailConstants.MAIL_SMTP_TIMEOUT));
		mail.setProperties(properties);
		mail.setDefaultEncoding(SysConfig.getInstance().getString(
				MailConstants.MAIL_DEFAULT_ENCODING));
		return mail;
	}

	/**
	 * 创建复杂邮件
	 * 
	 * @return
	 */
	static public MimeMail createMimeMail() {
		MimeMail mail = new MimeMail();
		mail.setHostname(SysConfig.getInstance().getString(
				MailConstants.MAIL_HOST));
		mail.setPort(SysConfig.getInstance().getInt(MailConstants.MAIL_PORT));
		mail.setProtocol(SysConfig.getInstance().getString(
				MailConstants.MAIL_PROTOCOL));
		mail.setUsername(SysConfig.getInstance().getString(
				MailConstants.MAIL_USERNAME));
		mail.setPassword(SysConfig.getInstance().getString(
				MailConstants.MAIL_PASSWORD));
		mail
				.setFrom(SysConfig.getInstance().getString(
						MailConstants.MAIL_FROM));
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", SysConfig.getInstance().getString(
				MailConstants.MAIL_SMTP_AUTH));
		properties.put("mail.smtp.timeout", SysConfig.getInstance().getString(
				MailConstants.MAIL_SMTP_TIMEOUT));
		mail.setProperties(properties);
		mail.setDefaultEncoding(SysConfig.getInstance().getString(
				MailConstants.MAIL_DEFAULT_ENCODING));
		return mail;
	}
	
}
