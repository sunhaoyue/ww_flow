package com.wwgroup.flow.dto;

import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.helper.DescionMaker;

public class MyWorkDTO {
	// w.id, w.flowid, f.formnum, f.createtime, content.scheme, creator.name,
	// actual.name
	private long workId;

	private long flowId;

	private long deptId;

	private String formnum;

	private String createTime;

	private String title;

	private String creatorName;

	private String actualName;

	private FlowType flowType;

	private String flowTypeName;

	private boolean delegated;

	private String flowDisplayStatusName;

	// 4种情况： 自己的工作，助理允许指派，助理不允许指派，代理
	private WorkRole workRole = WorkRole.MYSELF;

	private String role;
	
	private String isNew = "0";
	
	private String endTime;
	
	private String finishTime;
	
	private DescionMaker decionMaker;
	private String decionMakerName;
	private String markDispName;
	private String localType;
	private String deptPath;

	public String getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}

	public WorkRole getWorkRole() {
		return workRole;
	}

	public void setWorkRole(WorkRole workRole) {
		this.workRole = workRole;
	}

	public FlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}

	public long getWorkId() {
		return workId;
	}

	public void setWorkId(long workId) {
		this.workId = workId;
	}

	public long getFlowId() {
		return flowId;
	}

	public long getDeptId() {
		return deptId;
	}

	public void setDeptId(long deptId) {
		this.deptId = deptId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public String getFormnum() {
		return formnum;
	}

	public void setFormnum(String formnum) {
		this.formnum = formnum;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public String getActualName() {
		return actualName;
	}

	public void setActualName(String actualName) {
		this.actualName = actualName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFlowTypeName() {
		return flowTypeName;
	}

	public void setFlowTypeName(String flowTypeName) {
		this.flowTypeName = flowTypeName;
	}

	// 加了委派的字段
	// 如果这个字段为true的情况下，说明是被委派的任务，那么需要前台将实际操作人的组织人员等信息设置到delegateWork的dlg_相关字段中
	public boolean isDelegated() {
		return delegated;
	}

	public void setDelegated(boolean delegated) {
		this.delegated = delegated;
	}

	public String getFlowDisplayStatusName() {
		return flowDisplayStatusName;
	}

	public void setFlowDisplayStatusName(String flowDisplayStatusName) {
		this.flowDisplayStatusName = flowDisplayStatusName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getIsNew() {
		return isNew;
	}

	public void setIsNew(String isNew) {
		this.isNew = isNew;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public DescionMaker getDecionMaker() {
		return decionMaker;
	}

	public void setDecionMaker(DescionMaker decionMaker) {
		this.decionMaker = decionMaker;
	}

	public String getDecionMakerName() {
		return decionMakerName;
	}

	public void setDecionMakerName(String decionMakerName) {
		this.decionMakerName = decionMakerName;
	}

	public String getMarkDispName() {
		return markDispName;
	}

	public void setMarkDispName(String markDispName) {
		this.markDispName = markDispName;
	}

	public String getLocalType() {
		return localType;
	}

	public void setLocalType(String localType) {
		this.localType = localType;
	}

	public String getDeptPath() {
		return deptPath;
	}

	public void setDeptPath(String deptPath) {
		this.deptPath = deptPath;
	}

}
