package com.wwgroup.user.bo;

import java.util.Date;

/**
 * 人员信息
 */
public class SystemUsers {

	private int userID; // 用户ID

	private String userName; // 系统用户名

	private String fullName; // 用户中文姓名

	private String passwordHash; // 用户密码（hash值）

	private int status; // 用户状态：1（默认）正常，0 删除

	private String depcod; // 部门代码

	private String cmpcod;// 公司代码

	private int styleID; // 用户页面主题样式（默认为0）

	private int pageID; // 用户个性化门户页面ID号（默认为0）

	private int leaderOrder;// 用户序列号（用于排序，小号优先）

	private String cell;// 用户手机号码(有可能为空)

	private String email; // 用户e-mail

	private Date birthday; // 用户生日

	private String image; // 用户照片

	private String workPhone;// 工作电话

	private String titnam;// 一个员工的employeeId职务名称（一一对应的）

	private String userRealName; // 用户中文姓名（不包含工号）

	public String getTitnam() {
		return titnam;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getDepcod() {
		return depcod;
	}

	public void setDepcod(String depcod) {
		this.depcod = depcod;
	}

	public String getCmpcod() {
		return cmpcod;
	}

	public void setCmpcod(String cmpcod) {
		this.cmpcod = cmpcod;
	}

	public int getStyleID() {
		return styleID;
	}

	public void setStyleID(int styleID) {
		this.styleID = styleID;
	}

	public int getPageID() {
		return pageID;
	}

	public void setPageID(int pageID) {
		this.pageID = pageID;
	}

	public int getLeaderOrder() {
		return leaderOrder;
	}

	public void setLeaderOrder(int leaderOrder) {
		this.leaderOrder = leaderOrder;
	}

	public String getCell() {
		return cell;
	}

	public void setCell(String cell) {
		this.cell = cell;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getWorkPhone() {
		return workPhone;
	}

	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}

	public void setTitnam(String titnam) {
		this.titnam = titnam;
	}

	public String getUserRealName() {
		return userRealName;
	}

	public void setUserRealName(String userRealName) {
		this.userRealName = userRealName;
	}

}
