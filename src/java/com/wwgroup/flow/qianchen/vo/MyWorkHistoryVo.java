package com.wwgroup.flow.qianchen.vo;


public class MyWorkHistoryVo {

	private long workId;
	
	private String deptId;

	private String deptName;

	private String processManName;

	private String processTime;

	private String workStatus;

	private String opinion;

	private boolean haveChildren;

	private String userRealName;

	private int workNum;

	private int stage;
	
	private String employeeId;
	
	private String orgpath;
	
	private String hbAgree;
	
	public long getWorkId() {
		return workId;
	}

	public void setWorkId(long workId) {
		this.workId = workId;
	}
	
	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getProcessManName() {
		return processManName;
	}

	public void setProcessManName(String processManName) {
		this.processManName = processManName;
	}

	public String getProcessTime() {
		return processTime;
	}

	public void setProcessTime(String processTime) {
		this.processTime = processTime;
	}

	public String getWorkStatus() {
		return workStatus;
	}

	public void setWorkStatus(String workStatus) {
		this.workStatus = workStatus;
	}

	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}

	public boolean isHaveChildren() {
		return haveChildren;
	}

	public void setHaveChildren(boolean haveChildren) {
		this.haveChildren = haveChildren;
	}

	public String getUserRealName() {
		return userRealName;
	}

	public void setUserRealName(String userRealName) {
		this.userRealName = userRealName;
	}

	public void setWorkNum(int workNum) {
		this.workNum = workNum;
	}

	public int getWorkNum() {
		return workNum;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	public int getStage() {
		return stage;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getOrgpath() {
		return orgpath;
	}

	public void setOrgpath(String orgpath) {
		this.orgpath = orgpath;
	}

	public String getHbAgree() {
		return hbAgree;
	}

	public void setHbAgree(String hbAgree) {
		this.hbAgree = hbAgree;
	}

}
