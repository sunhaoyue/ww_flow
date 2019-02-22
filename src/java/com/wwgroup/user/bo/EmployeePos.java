package com.wwgroup.user.bo;

/**
 * 人员岗位关系
 * 
 * 
 */
public class EmployeePos {

	private String cmpcod; // 公司代码
	private String employeeid; // 员工编号（对应系统用户名）
	private String deptcode; // 部门代码
	private String postcode; // 岗位代码
	private String mgremployeeid; // 上级主管工号
	private boolean isDeptmgr; // 是否是部门主管
	private boolean isCentermgr;// 是否是中心主管
	private boolean isCenterFmgr = false; // 是否是中心副主管
	private boolean isTopFmgr = false; // 是否是最高副主管
	private boolean isTopmgr; // 是否是最高主管
	private String mgrpostcode; // 上级主管岗位代码
	private boolean isMajorpost; // 是否是主岗位
	private String a_deptcode;// 中心代码
	private int mgDeptFlg;
	private int mgA_DeptFlg;
	private int mgCentFlg;
	private int mgCentFFlg;
	private int mgA_DeptFFlg;

	public String getCmpcod() {
		return cmpcod;
	}

	public void setCmpcod(String cmpcod) {
		this.cmpcod = cmpcod;
	}

	public String getEmployeeid() {
		return employeeid;
	}

	public void setEmployeeid(String employeeid) {
		this.employeeid = employeeid;
	}

	public String getDeptcode() {
		return deptcode;
	}

	public void setDeptcode(String deptcode) {
		this.deptcode = deptcode;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getMgremployeeid() {
		return mgremployeeid;
	}

	public void setMgremployeeid(String mgremployeeid) {
		this.mgremployeeid = mgremployeeid;
	}

	public boolean isDeptmgr() {
		return isDeptmgr;
	}

	public void setDeptmgr(boolean isDeptmgr) {
		this.isDeptmgr = isDeptmgr;
	}

	public boolean isCentermgr() {
		return isCentermgr;
	}

	public void setCentermgr(boolean isCentermgr) {
		this.isCentermgr = isCentermgr;
	}

	public boolean isTopmgr() {
		return isTopmgr;
	}

	public void setTopmgr(boolean isTopmgr) {
		this.isTopmgr = isTopmgr;
	}

	public String getMgrpostcode() {
		return mgrpostcode;
	}

	public void setMgrpostcode(String mgrpostcode) {
		this.mgrpostcode = mgrpostcode;
	}

	public boolean isMajorpost() {
		return isMajorpost;
	}

	public void setMajorpost(boolean isMajorpost) {
		this.isMajorpost = isMajorpost;
	}

	public String getA_deptcode() {
		return a_deptcode;
	}

	public void setA_deptcode(String a_deptcode) {
		this.a_deptcode = a_deptcode;
	}

	public int getMgDeptFlg() {
		return mgDeptFlg;
	}

	public void setMgDeptFlg(int mgDeptFlg) {
		this.mgDeptFlg = mgDeptFlg;
	}

	public int getMgA_DeptFlg() {
		return mgA_DeptFlg;
	}

	public void setMgA_DeptFlg(int mgA_DeptFlg) {
		this.mgA_DeptFlg = mgA_DeptFlg;
	}

	public int getMgCentFlg() {
		return mgCentFlg;
	}

	public void setMgCentFlg(int mgCentFlg) {
		this.mgCentFlg = mgCentFlg;
	}

	public boolean isTopFmgr() {
		return isTopFmgr;
	}

	public void setTopFmgr(boolean isTopFmgr) {
		this.isTopFmgr = isTopFmgr;
	}

	public int getMgCentFFlg() {
		return mgCentFFlg;
	}

	public void setMgCentFFlg(int mgCentFFlg) {
		this.mgCentFFlg = mgCentFFlg;
	}

	public boolean isCenterFmgr() {
		return isCenterFmgr;
	}

	public void setCenterFmgr(boolean isCenterFmgr) {
		this.isCenterFmgr = isCenterFmgr;
	}

	public int getMgA_DeptFFlg() {
		return mgA_DeptFFlg;
	}

	public void setMgA_DeptFFlg(int mgA_DeptFFlg) {
		this.mgA_DeptFFlg = mgA_DeptFFlg;
	}

}
