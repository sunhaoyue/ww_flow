package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;

// TODO:
// 在其上次加入delegateWork，所有之前的子类继承delegetWork，
// 其中会加入到实际操作人以及操作人组织信息等字段，
// 该字段信息需要在操作表单时候讲登陆人的信息带进来即可。
// 代理人的话，可以进行任何操作
// 而助理的话，只能进行指派操作 TODO
// 然后再显示历程的时候，直接在delegateWork里面进行实际二次操作完成功能
public abstract class MyWork {

	private long id;

	private long flowId;

	private String deptId;

	private String deptName;

	private String postCode;

	private String deptCode;

	private String a_deptCode;

	private String cmpCode;

	private String employeeId;

	private long createTime;

	private long finishTime;

	private String opinion;

	private FlowStatus status = FlowStatus.INIT;

	private WorkStage workStage;

	private FlowType flowType;

	// 会办分支主管（起始职员ID），这样的话，如果指派后，
	// 同意的上层主管的职位ID与该人一致，那么就是同一个，如果他再选择同意，那么这个会办分支就算完成了
	// 由于在地方到总部的 事业部最高主管以及 转投资最高主管 都可以，指派，而且逻辑几乎与会办分支是一致的，所以将该字段从jointSignWork改放到基类中来设置。
	// 如果使用了委派动作，直接将该人的employeeId作为jointSignStartId存入
	private String joinSignStartId;

	private long worknum = 0;

	private long parentId;

	private int joinCycle;

	private String joinStartEmployeeId;
	
	//flowId,用在重启案的时候
	private long oldFlowId;
	
	// 用于记录签核人所属组织全路径
	private String orgpath;
	
	/**
	 *  add by Cao_Shengyong 2014-03-24
	 *  用于记录会办部门核准后需呈核上一级的相关信息
	 *  默认HbChengHe = '0'
	 *  	HbJoinSignStartId = joinSignStartId
	 *  	hbJoinStartEmployeeId = joinStartEmployeeId
	 */
	private String Hb_ChengHe = "0";
	private String Hb_JoinSignStartId;
	private String Hb_JoinStartEmployeeId;
	private String Hb_ChengHeEnd;
	
	private String Hb_Agree = "0";
	
	private String employeenam;
	private String titlenam;
	
	private String serverIP;
	private String clientIP;

	public void setJoinStartEmployeeId(String joinStartEmployeeId) {
		this.joinStartEmployeeId = joinStartEmployeeId;	
	}

	public String getJoinStartEmployeeId() {
		return joinStartEmployeeId;
	}

	public void setJoinCycle(int joinCycle) {
		this.joinCycle = joinCycle;
	}
	
	public int getJoinCycle() {
		return joinCycle;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public MyWork(WorkStage workStage) {
		this.workStage = workStage;
	}

	public long getWorknum() {
		return worknum;
	}

	public void setWorknum(long worknum) {
		this.worknum = worknum;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
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

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}

	public FlowStatus getStatus() {
		return status;
	}

	public void setStatus(FlowStatus status) {
		this.status = status;
	}

	public WorkStage getWorkStage() {
		return workStage;
	}

	public FlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}

	public String getJoinSignStartId() {
		return joinSignStartId;
	}

	public void setJoinSignStartId(String joinSignStartId) {
		this.joinSignStartId = joinSignStartId;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public String getA_deptCode() {
		return a_deptCode;
	}

	public void setA_deptCode(String code) {
		a_deptCode = code;
	}

	public String getCmpCode() {
		return cmpCode;
	}

	public void setCmpCode(String cmpCode) {
		this.cmpCode = cmpCode;
	}
	
	public long getOldFlowId() {
		return oldFlowId;
	}

	public void setOldFlowId(long oldFlowId) {
		this.oldFlowId = oldFlowId;
	}

	public String getOrgpath() {
		return orgpath;
	}

	public void setOrgpath(String orgpath) {
		this.orgpath = orgpath;
	}

	public String getHb_ChengHe() {
		return Hb_ChengHe;
	}

	public void setHb_ChengHe(String hbChengHe) {
		Hb_ChengHe = hbChengHe;
	}

	public String getHb_JoinSignStartId() {
		return Hb_JoinSignStartId;
	}

	public void setHb_JoinSignStartId(String hbJoinSignStartId) {
		Hb_JoinSignStartId = hbJoinSignStartId;
	}

	public String getHb_JoinStartEmployeeId() {
		return Hb_JoinStartEmployeeId;
	}

	public void setHb_JoinStartEmployeeId(String hbJoinStartEmployeeId) {
		Hb_JoinStartEmployeeId = hbJoinStartEmployeeId;
	}

	public String getHb_ChengHeEnd() {
		return Hb_ChengHeEnd;
	}

	public void setHb_ChengHeEnd(String hbChengHeEnd) {
		Hb_ChengHeEnd = hbChengHeEnd;
	}

	public String getHb_Agree() {
		return Hb_Agree;
	}

	public void setHb_Agree(String hb_Agree) {
		Hb_Agree = hb_Agree;
	}

	abstract public boolean supportAssignFunction();

	public String getEmployeenam() {
		return employeenam;
	}

	public void setEmployeenam(String employeenam) {
		this.employeenam = employeenam;
	}

	public String getTitlenam() {
		return titlenam;
	}

	public void setTitlenam(String titlenam) {
		this.titlenam = titlenam;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public void setWorkStage(WorkStage workStage) {
		this.workStage = workStage;
	}

}
