package com.wwgroup.common.mail;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import sun.misc.BASE64Encoder;

public class MimeMail extends AbstractMail {

	private boolean html;

	private Map<String, Object> attachment = new HashMap<String, Object>();

	private Map<String, Object> inline = new HashMap<String, Object>();

	public boolean isHtml() {
		return html;
	}

	public void setHtml(boolean html) {
		this.html = html;
	}

	public Map<String, Object> getAttachment() {
		return attachment;
	}

	public void setAttachment(Map<String, Object> attachment) {
		this.attachment = attachment;
	}

	public Map<String, Object> getInline() {
		return inline;
	}

	public void setInline(Map<String, Object> inline) {
		this.inline = inline;
	}

	public void addAttachment(String name, String path) {
		attachment.put(name, path);
	}

	public void addAttachment(String name, File file) {
		attachment.put(name, file);
	}

	public void addAttachment(String name, InputStream is) {
		attachment.put(name, is);
	}

	public void addInline(String id, String path) {
		inline.put(id, path);
	}

	public void addInline(String id, File file) {
		inline.put(id, file);
	}

	public void addInline(String id, InputStream is) {
		inline.put(id, is);
	}

	/**
	 * 使用Spring的JavaMailSender发送复杂邮件
	 * 
	 * @return
	 */
	public void send() throws MailException {
		/* 邮件发送器 */
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		/* 设置邮件发送器 */
		sender.setHost(this.getHostname());
		sender.setPort(this.getPort());
		sender.setProtocol(this.getProtocol());
		sender.setUsername(this.getUsername());
		sender.setPassword(this.getPassword());
		sender.setJavaMailProperties(this.getProperties());
		sender.setDefaultEncoding(this.getDefaultEncoding());
		/* 邮件信息 */
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper;
		/* 设置邮件信息 */
		try {
			helper = new MimeMessageHelper(message, true);
			helper.setFrom(this.getFrom());
			if (!this.getTo().isEmpty()) {
				helper.setTo(this.getTo().toArray(
						new String[this.getTo().size()]));
			}
			if (!this.getCc().isEmpty()) {
				helper.setCc(this.getCc().toArray(
						new String[this.getCc().size()]));
			}
			if (!this.getBcc().isEmpty()) {
				helper.setBcc(this.getBcc().toArray(
						new String[this.getBcc().size()]));
			}
			helper.setSubject(this.getSubject());
			helper.setText(this.getText(), this.isHtml());
			if (this.getSentDate() != null) {
				helper.setSentDate(this.getSentDate());
			} else {
				helper.setSentDate(new Date());
			}
			/* 设置邮件附件 */
			Iterator<String> it = this.getAttachment().keySet().iterator();
			while (it.hasNext()) {
				String name = it.next();
				Object object = this.getAttachment().get(name);
				/* BASE64编码附件名，以防止乱码 */
				BASE64Encoder enc = new BASE64Encoder();
				name = new StringBuffer("=?").append(this.getDefaultEncoding())
						.append("?B?").append(
								enc.encode(name.getBytes(this
										.getDefaultEncoding()))).append("?=")
						.toString();
				if (object instanceof String) {
					helper.addAttachment(name, new FileSystemResource(
							(String) object));
				} else if (object instanceof File) {
					helper.addAttachment(name, new FileSystemResource(
							(File) object));
				} else if (object instanceof InputStream) {
					helper.addAttachment(name, new InputStreamResource(
							(InputStream) object));
				}
			}
			/* 设置邮件内部资源 */
			it = this.getInline().keySet().iterator();
			while (it.hasNext()) {
				String id = it.next();
				Object object = this.getInline().get(id);
				if (object instanceof String) {
					helper.addInline(id,
							new FileSystemResource((String) object));
				} else if (object instanceof File) {
					helper.addInline(id, new FileSystemResource((File) object));
				} else if (object instanceof InputStream) {
					helper.addInline(id, new InputStreamResource(
							(InputStream) object));
				}
			}
		} catch (Exception ex) {
			throw new MailPreparationException("");
		}

		/* 发送邮件 */
		sender.send(message);
	}
}
