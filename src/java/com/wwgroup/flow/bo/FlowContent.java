package com.wwgroup.flow.bo;

public class FlowContent {

	private long id;
	private long flowId;
	private String deptName;
	private String deptId;
	private int secretLevel;
	private int exireLevel;
	private String cash;
	private int type;
	private String title;
	private String detail;
	private String scheme;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public int getSecretLevel() {
		return secretLevel;
	}

	public void setSecretLevel(int secretLevel) {
		this.secretLevel = secretLevel;
	}

	public int getExireLevel() {
		return exireLevel;
	}

	public void setExireLevel(int exireLevel) {
		this.exireLevel = exireLevel;
	}

	public String getCash() {
		return cash;
	}

	public void setCash(String cash) {
		this.cash = cash;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

}
