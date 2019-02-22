package com.wwgroup.flow.bo;

import java.util.Date;

/**
 * <p>
 * Title: cuteinfo_[子系统统名]_[模块名]
 * </p>
 * <p>
 * Description: [描述该类概要功能介绍]
 * </p>
 * 
 * @author Administrator
 * @version $Revision$ 2012-7-22
 * @author (lastest modification by $Author$)
 * @since 20100620
 */
public class AgentRelation {

	private long id;

	private String actualEmployeeId;

	// ActualUserID;
	private String actualUserId;

	private Date startDate;

	private Date endDate;

	private AgentReason reason;

	// 供显示用
	private String agentReason;

	private String otherReason;

	// 供选择所需岗位用

	private String selectedPostCode;

	private String selectedPostName;

	private String selectedDeptName;

	private String deptPath;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setActualEmployeeId(String employeeId) {
		this.actualEmployeeId = employeeId;
	}

	public String getActualUserId() {
		return actualUserId;
	}

	public void setActualUserId(String actualUserId) {
		this.actualUserId = actualUserId;
	}

	public String getActualEmployeeId() {
		return actualEmployeeId;
	}

	public void setStartDate(Date date) {
		this.startDate = date;
	}

	public void setEndDate(Date date) {
		this.endDate = date;
	}

	public void setReason(AgentReason reason) {
		this.reason = reason;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public AgentReason getReason() {
		return reason;
	}

	public String getAgentReason() {
		return agentReason;
	}

	public void setAgentReason(String agentReason) {
		this.agentReason = agentReason;
	}

	public String getOtherReason() {
		return otherReason;
	}

	public void setOtherReason(String otherReason) {
		this.otherReason = otherReason;
	}

	public String getSelectedPostCode() {
		return selectedPostCode;
	}

	public void setSelectedPostCode(String selectedPostCode) {
		this.selectedPostCode = selectedPostCode;
	}

	public String getSelectedPostName() {
		return selectedPostName;
	}

	public void setSelectedPostName(String selectedPostName) {
		this.selectedPostName = selectedPostName;
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
