package com.wwgroup.flow.vo;

import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.dto.Branch;
import com.wwgroup.flow.dto.FormStatus;
import com.wwgroup.flow.dto.Order;

public class AdvancedSearchVo {

	private String startTime;
	private String endTime;

	private FormStatus formStatus;
	private Branch branch = Branch.MySubmit;
	private Order order;
	private PersonDetail user;

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public FormStatus getFormStatus() {
		return formStatus;
	}

	public void setFormStatus(FormStatus formStatus) {
		this.formStatus = formStatus;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public PersonDetail getUser() {
		return user;
	}

	public void setUser(PersonDetail user) {
		this.user = user;
	}

}
