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

package java.com.wwgroup.flow.bo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title: cuteinfo_[子系统统名]_[模块名]
 * </p>
 * <p>
 * Description: [描述该类概要功能介绍]
 * </p>
 * 
 * @author Administrator
 * @version $Revision$ 2012-8-19
 * @author (lastest modification by $Author$)
 * @since 20100620
 */
public class AgentPerson {

	private long id;

	private long agentRelationId;

	private String agentEmployeeId;

	private String agentPostCode;

	private Map<String, List<String>> selectedFlow;

	private com.wwgroup.flow.bo.AgentType type = com.wwgroup.flow.bo.AgentType.PositionAgent;

	private String agentDeptId;

	private String agentUserId;

	// 供显示用
	private String actualUserName;

	private String agentUserName;

	private String agentType;

	private String agentReason;

	private String agentDept;

	private String agentPostName;

	private String flows;

	private String flowNames;
	
	//流程数
	private int flowCount;

	public void setType(AgentType type) {
		this.type = type;
	}

	public AgentType getType() {
		return type;
	}

	public void setAgentEmployeeId(String employeeId) {
		this.agentEmployeeId = employeeId;
	}

	public String getAgentEmployeeId() {
		return agentEmployeeId;
	}

	public void setSelectedFlow(Map<String, List<String>> selectedFlowMap) {
		this.selectedFlow = selectedFlowMap;
	}

	public Map<String, List<String>> getSelectedFlow() {
		return selectedFlow;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getAgentRelationId() {
		return agentRelationId;
	}

	public void setAgentRelationId(long agentRelationId) {
		this.agentRelationId = agentRelationId;
	}

	public void setAgentDeptId(String agentDeptId) {
		this.agentDeptId = agentDeptId;
	}

	public String getAgentDeptId() {
		return agentDeptId;
	}

	public String getAgentPostCode() {
		return agentPostCode;
	}

	public void setAgentPostCode(String agentPostCode) {
		this.agentPostCode = agentPostCode;
	}

	public String getAgentUserId() {
		return agentUserId;
	}

	public void setAgentUserId(String agentUserId) {
		this.agentUserId = agentUserId;
	}

	public String getActualUserName() {
		return actualUserName;
	}

	public void setActualUserName(String actualUserName) {
		this.actualUserName = actualUserName;
	}

	public String getAgentUserName() {
		return agentUserName;
	}

	public void setAgentUserName(String agentUserName) {
		this.agentUserName = agentUserName;
	}

	public String getAgentType() {
		return agentType;
	}

	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}

	public String getAgentReason() {
		return agentReason;
	}

	public void setAgentReason(String agentReason) {
		this.agentReason = agentReason;
	}

	public String getAgentDept() {
		return agentDept;
	}

	public void setAgentDept(String agentDept) {
		this.agentDept = agentDept;
	}

	public String getAgentPostName() {
		return agentPostName;
	}

	public void setAgentPostName(String agentPostName) {
		this.agentPostName = agentPostName;
	}

	public String getFlows() {
		return flows;
	}

	public void setFlows(String flows) {
		this.flows = flows;
	}

	public String getFlowNames() {
		return flowNames;
	}

	public void setFlowNames(String flowNames) {
		this.flowNames = flowNames;
	}

	public int getFlowCount() {
		return flowCount;
	}

	public void setFlowCount(int flowCount) {
		this.flowCount = flowCount;
	}
	
	private String operation;
	private String isNew;

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getIsNew() {
		return isNew;
	}

	public void setIsNew(String isNew) {
		this.isNew = isNew;
	}
}
