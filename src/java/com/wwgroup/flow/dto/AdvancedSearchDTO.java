package com.wwgroup.flow.dto;

import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;

public class AdvancedSearchDTO {

	private FlowType flowType;
	private String startTime;
	private String endTime;
	
	private com.wwgroup.flow.dto.FormStatus formStatus;
	private com.wwgroup.flow.dto.Branch branch = com.wwgroup.flow.dto.Branch.MySubmit;
	private com.wwgroup.flow.dto.Order order;
	private PersonDetail user;
	private String[] subDeptIds;
	private String formNum;
	private String title;
	
	private String orderBy = " asc ";
	private String sorder;
	
	/** 所有岗位 */
	private String[] userDeptIds;
	
	/**
	 * 员工号
	 * added by zhangqiang at 2012-11-20 12:53
	 */
	private String employeeId;


	private String localType;
	private String creator;
	private String creatCmp;

	private String closeStartTime;
	private String closeEndTime;

	/**
	 * 部门名称
	 * added by 孙浩月 at 2019-02-21 14:58
	 * 作为模糊查询的参数获取
	 *
	 */
	private String departmentName;



	public String getCloseStartTime() {
		return closeStartTime;
	}

	public void setCloseStartTime(String closeStartTime) {
		this.closeStartTime = closeStartTime;
	}

	public String getCloseEndTime() {
		return closeEndTime;
	}

	public void setCloseEndTime(String closeEndTime) {
		this.closeEndTime = closeEndTime;
	}

	public String getLocalType() {
		return localType;
	}

	public void setLocalType(String localType) {
		this.localType = localType;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreatCmp() {
		return creatCmp;
	}

	public void setCreatCmp(String creatCmp) {
		this.creatCmp = creatCmp;
	}

	public void setBranch(com.wwgroup.flow.dto.Branch branch) {
		this.branch = branch;
	}
	
	public com.wwgroup.flow.dto.Branch getBranch() {
		return branch;
	}

	public void setOrder(com.wwgroup.flow.dto.Order order) {
		this.order = order;
	}

	public com.wwgroup.flow.dto.Order getOrder() {
		return order;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}

	public FlowType getFlowType() {
		return flowType;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setFormStatus(com.wwgroup.flow.dto.FormStatus formStatus) {
		this.formStatus = formStatus;
	}

	public com.wwgroup.flow.dto.FormStatus getFormStatus() {
		return formStatus;
	}

	public void setUser(PersonDetail user) {
		this.user = user;
	}

	public PersonDetail getUser() {
		return user;
	}

	public void setSubDeptIds(String[] subDeptIds) {
		this.subDeptIds = subDeptIds;
	}

	public String[] getSubDeptIds() {
		return subDeptIds;
	}

	public void setFormNum(String formNum) {
		this.formNum = formNum;
	}

	public String getFormNum() {
		return formNum;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public String[] getUserDeptIds() {
		return userDeptIds;
	}

	public void setUserDeptIds(String[] userDeptIds) {
		this.userDeptIds = userDeptIds;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
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


	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
}
