package com.wwgroup.common.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public abstract class AbstractMail {

	private String hostname;

	private int port = 25;

	private String protocol = "smtp";

	private String username;

	private String password;

	private String from;

	private List<String> to = new ArrayList<String>();

	private List<String> cc = new ArrayList<String>();

	private List<String> bcc = new ArrayList<String>();

	private String defaultEncoding;

	private String subject;

	private String text;

	private Date sentDate;

	private Properties properties = new Properties();

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getTo() {
		return to;
	}

	public void setTo(List<String> to) {
		this.to = to;
	}

	public List<String> getCc() {
		return cc;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	public List<String> getBcc() {
		return bcc;
	}

	public void setBcc(List<String> bcc) {
		this.bcc = bcc;
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setHost(String hostname, int port, String protocal) {
		this.hostname = hostname;
		this.port = port;
		this.protocol = protocal;
	}

	public void setAuthentication(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void setTo(String to) {
		this.to.clear();
		this.to.add(to);
	}

	public void setTo(String[] to) {
		this.to.clear();
		this.to.addAll(Arrays.asList(to));
	}

	public void addTo(String to) {
		this.to.add(to);
	}

	public void setCc(String cc) {
		this.cc.clear();
		this.cc.add(cc);
	}

	public void setCc(String[] cc) {
		this.cc.clear();
		this.cc.addAll(Arrays.asList(cc));
	}

	public void addCc(String cc) {
		this.cc.add(cc);
	}

	public void setBcc(String bcc) {
		this.bcc.clear();
		this.bcc.add(bcc);
	}

	public void setBcc(String[] bcc) {
		this.bcc.clear();
		this.bcc.addAll(Arrays.asList(bcc));
	}

	public void addBcc(String bcc) {
		this.bcc.add(bcc);
	}

	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	/**
	 * 根据模板绝对路径设置邮件内容
	 * 
	 * @param path
	 *            模板绝对路径
	 * @return
	 */
	public void setTemplate(String path) throws IOException {
		InputStream is = new FileInputStream(path);
		setTemplate(is);
		is.close();
	}

	/**
	 * 根据模板文件设置邮件内容
	 * 
	 * @param file
	 *            模板文件
	 * @return
	 */
	public void setTemplate(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		setTemplate(is);
		is.close();
	}

	/**
	 * 根据模板输入流设置邮件内容
	 * 
	 * @param is
	 *            模板输入流
	 * @return
	 */
	public void setTemplate(InputStream is) throws IOException {
		StringBuffer sb = new StringBuffer();
		String str;
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				this.getDefaultEncoding()));
		while ((str = reader.readLine()) != null) {
			sb.append(str);
		}
		setText(sb.toString());
	}

	abstract public void send();
}
