package com.wwgroup.common.vo;

public class TreeModel {

	private String id;// 节点ID

	private String name;// 节点名称

	private boolean isParent;// 是否为父节点

	private String pId;// 父节点的id

	private boolean halfCheck;// 半选状态

	private boolean checked;// 是否被选中

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

	public void setIsParent(boolean isParent) {
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
}
