package com.wwgroup.common.mail;

import java.util.Date;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 简单邮件
 * 
 */

public class SimpleMail extends AbstractMail {

	/**
	 * 使用Spring的JavaMailSender发送简单邮件
	 * 
	 * @return
	 */
	public void send() throws MailException {
		/* 邮件发送器 */
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		/* 邮件信息 */
		SimpleMailMessage message = new SimpleMailMessage();

		/* 设置邮件发送器 */
		sender.setHost(this.getHostname());
		sender.setPort(this.getPort());
		sender.setProtocol(this.getProtocol());
		sender.setUsername(this.getUsername());
		sender.setPassword(this.getPassword());
		sender.setJavaMailProperties(this.getProperties());
		sender.setDefaultEncoding(this.getDefaultEncoding());

		/* 设置邮件信息 */
		message.setFrom(this.getFrom());
		if (!this.getTo().isEmpty()) {
			message
					.setTo(this.getTo()
							.toArray(new String[this.getTo().size()]));
		}
		if (!this.getCc().isEmpty()) {
			message
					.setCc(this.getCc()
							.toArray(new String[this.getCc().size()]));
		}
		if (!this.getBcc().isEmpty()) {
			message.setBcc(this.getBcc().toArray(
					new String[this.getBcc().size()]));
		}
		message.setSubject(this.getSubject());
		message.setText(this.getText());
		if (this.getSentDate() != null) {
			message.setSentDate(this.getSentDate());
		} else {
			message.setSentDate(new Date());
		}

		/* 发送邮件 */
		sender.send(message);
	}
}
