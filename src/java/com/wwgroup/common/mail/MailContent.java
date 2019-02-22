package com.wwgroup.common.mail;

import java.util.ArrayList;
import java.util.List;

public class MailContent {

	String formNum;

	String userName;

	String email;
	
	String title;
	
	String actualPersonName;
	
	private List<String> cc = new ArrayList<String>();
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFormNum() {
		return formNum;
	}

	public void setFormNum(String formNum) {
		this.formNum = formNum;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getActualPersonName() {
		return actualPersonName;
	}

	public void setActualPersonName(String actualPersonName) {
		this.actualPersonName = actualPersonName;
	}

	public List<String> getCc() {
		return cc;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}
}
