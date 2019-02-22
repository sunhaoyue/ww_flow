package com.wwgroup.organ.vo;

public class OrganVo {

	private String id;// 节点ID

	private String name;// 节点名称

	private boolean isParent;// 是否为父节点

	private String pId;// 父节点的id

	private boolean halfCheck;// 半选状态

	private boolean checked;// 是否被选中

	private String orgPath; // 组全路径

	private boolean hidden = false; // 节点是否隐藏，默认为不隐藏

	private boolean clicked = true; // 节点是否可选，默认为可选
	
	private boolean parent;
	
	private String cmpcod;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getIsParent() {
		return isParent;
	}

	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	public String getPId() {
		return pId;
	}

	public void setPId(String pId) {
		this.pId = pId;
	}

	public boolean isHalfCheck() {
		return halfCheck;
	}

	public void setHalfCheck(boolean halfCheck) {
		this.halfCheck = halfCheck;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public String getOrgPath() {
		return orgPath;
	}

	public void setOrgPath(String orgPath) {
		this.orgPath = orgPath;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isClicked() {
		return clicked;
	}

	public void setClicked(boolean clicked) {
		this.clicked = clicked;
	}

	public boolean isParent() {
		return parent;
	}

	public String getCmpcod() {
		return cmpcod;
	}

	public void setCmpcod(String cmpcod) {
		this.cmpcod = cmpcod;
	}

}
