package com.wwgroup.organ.bo;

import com.wwgroup.common.vo.TreeModel;

/**
 * 
 * 部门对象
 * 
 */
public class SystemGroups extends TreeModel {

	// 组ID
	private int groupID;
	// 组名称
	private String groupName;
	// 组序列号
	private int leaderOrder;
	// 父级组ID
	private int parentID;
	// 公司代码
	private String cmpcod;
	// 组类型
	private int groupType;
	// 组全路径
	private String orgPath;
	// 部门代码
	private String depcod;
	// 中心代码
	private String a_depcod;
	// 体系代码
	private String systemFlg;

	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getLeaderOrder() {
		return leaderOrder;
	}

	public void setLeaderOrder(int leaderOrder) {
		this.leaderOrder = leaderOrder;
	}

	public int getParentID() {
		return parentID;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public String getCmpcod() {
		return cmpcod;
	}

	public void setCmpcod(String cmpcod) {
		this.cmpcod = cmpcod;
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	public String getOrgPath() {
		return orgPath;
	}

	public void setOrgPath(String orgPath) {
		this.orgPath = orgPath;
	}

	public String getDepcod() {
		return depcod;
	}

	public void setDepcod(String depcod) {
		this.depcod = depcod;
	}

	public String getA_depcod() {
		return a_depcod;
	}

	public void setA_depcod(String a_depcod) {
		this.a_depcod = a_depcod;
	}

	public String getSystemFlg() {
		return systemFlg;
	}

	public void setSystemFlg(String systemFlg) {
		this.systemFlg = systemFlg;
	}
}
