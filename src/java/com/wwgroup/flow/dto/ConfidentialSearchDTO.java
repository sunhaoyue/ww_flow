package com.wwgroup.flow.dto;

import com.wwgroup.flow.bo.FlowType;

public class ConfidentialSearchDTO {

	private FlowType flowType;
	private String localType;
	private String title;
	private String formNum;
	private String creator;
	private String startTime;
	private String endTime;
	private FormStatus formStatus;
	private String creatCmp;
	private String leader;
	private String rePath;
	private String orderBy = " asc ";
	private String sorder;
	private String employeeId;
	
	public FlowType getFlowType() {
		return flowType;
	}
	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}
	public String getLocalType() {
		return localType;
	}
	public void setLocalType(String localType) {
		this.localType = localType;
	}
	public void setFormNum(String formNum) {
		this.formNum = formNum;
	}
	public String getFormNum() {
		return formNum;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
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
	public String getCreatCmp() {
		return creatCmp;
	}
	public void setCreatCmp(String creatCmp) {
		this.creatCmp = creatCmp;
	}
	public String getLeader() {
		return leader;
	}
	public void setLeader(String leader) {
		this.leader = leader;
	}
	public String getRePath() {
		return rePath;
	}
	public void setRePath(String rePath) {
		this.rePath = rePath;
	}
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public String getSorder() {
		return sorder;
	}
	public void setSorder(String sorder) {
		this.sorder = sorder;
	}
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public FormStatus getFormStatus() {
		return formStatus;
	}
	public void setFormStatus(FormStatus formStatus) {
		this.formStatus = formStatus;
	}
}
