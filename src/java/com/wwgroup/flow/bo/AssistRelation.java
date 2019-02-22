/** 
 * 
 * Copyright (c) 1995-2012 Wonders Information Co.,Ltd. 
 * 1518 Lianhang Rd,Shanghai 201112.P.R.C.
 * All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Wonders Group.
 * (Social Security Department). You shall not disclose such
 * Confidential Information and shall use it only in accordance with 
 * the terms of the license agreement you entered into with Wonders Group. 
 *
 * Distributable under GNU LGPL license by gnu.org
 */

package com.wwgroup.flow.bo;

/**
 * <p>
 * Title: cuteinfo_[子系统统名]_[模块名]
 * </p>
 * <p>
 * Description: [描述该类概要功能介绍]
 * </p>
 * 
 * @author Administrator
 * @version $Revision$ 2012-7-29
 * @author (lastest modification by $Author$)
 * @since 20100620
 */
public class AssistRelation {
	private long id;
	private String selectedPostCode;
	private String selectedPostName;
	private boolean allowReceiveMail;
	private boolean allowAssignPerson;
	private String selectedAssistEmployeeId;
	private String selectedAssistEmployeeName;
	private String selectedAssistPostCode;
	private String selectedAssistPostName;
	private String employeeId;

	private int allowReceive = 0;
	private int allowAssign = 0;
	private long selectedDeptId;
	private String selectedDeptName;
	private String deptPath;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param selectedPostCode
	 */
	public void setSelectedPostCode(String selectedPostCode) {
		this.selectedPostCode = selectedPostCode;
	}

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param selectedPostName
	 */
	public void setSelectedPostName(String selectedPostName) {
		this.selectedPostName = selectedPostName;
	}

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param b
	 */
	public void setAllowReceiveMail(boolean allowReceiveMail) {
		this.allowReceiveMail = allowReceiveMail;
	}

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param b
	 */
	public void setAllowAssignPerson(boolean allowAssignPerson) {
		this.allowAssignPerson = allowAssignPerson;
	}

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param string
	 */
	public void setSelectedAssistEmployeeId(String selectedAssistEmployeeId) {
		this.selectedAssistEmployeeId = selectedAssistEmployeeId;
	}

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param string
	 */
	public void setSelectedAssistEmployeeName(String selectedAssistEmployeeName) {
		this.selectedAssistEmployeeName = selectedAssistEmployeeName;
	}

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param postCode
	 */
	public void setSelectedAssistPostCode(String selectedAssistPostCode) {
		this.selectedAssistPostCode = selectedAssistPostCode;
	}

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param postName
	 */
	public void setSelectedAssistPostName(String selectedAssistPostName) {
		this.selectedAssistPostName = selectedAssistPostName;
	}

	public String getSelectedPostCode() {
		return selectedPostCode;
	}

	public String getSelectedPostName() {
		return selectedPostName;
	}

	public boolean isAllowReceiveMail() {
		return allowReceiveMail;
	}

	public boolean isAllowAssignPerson() {
		return allowAssignPerson;
	}

	public String getSelectedAssistEmployeeId() {
		return selectedAssistEmployeeId;
	}

	public String getSelectedAssistEmployeeName() {
		return selectedAssistEmployeeName;
	}

	public String getSelectedAssistPostCode() {
		return selectedAssistPostCode;
	}

	public String getSelectedAssistPostName() {
		return selectedAssistPostName;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public int getAllowReceive() {
		return allowReceive;
	}

	public void setAllowReceive(int allowReceive) {
		this.allowReceive = allowReceive;
	}

	public int getAllowAssign() {
		return allowAssign;
	}

	public void setAllowAssign(int allowAssign) {
		this.allowAssign = allowAssign;
	}

	public long getSelectedDeptId() {
		return selectedDeptId;
	}

	public void setSelectedDeptId(long selectedDeptId) {
		this.selectedDeptId = selectedDeptId;
	}

	public String getSelectedDeptName() {
		return selectedDeptName;
	}

	public void setSelectedDeptName(String selectedDeptName) {
		this.selectedDeptName = selectedDeptName;
	}

	public String getDeptPath() {
		return deptPath;
	}

	public void setDeptPath(String deptPath) {
		this.deptPath = deptPath;
	}

}
