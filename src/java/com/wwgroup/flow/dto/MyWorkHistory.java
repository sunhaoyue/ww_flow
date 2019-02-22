package com.wwgroup.flow.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wwgroup.flow.bo.helper.FlowStatus;

public class MyWorkHistory {

	private long workId;

	private String deptName;

	private String processManName;

	private String processTime;

	private Date calculatedProcessTime;

	private String workStatus;

	private String opinion;

	private boolean haveChildren;

	private String userRealName;

	private int workNum;

	private FlowStatus status;

	private int stage;
	
	private long oldFlowId;
	
	private String orgpath;
	
	private String employeeNam;
	private String titnam;
	private String dlgEmployeeNam;
	private String dlgTitnam;
	
	public String getEmployeeNam() {
		return employeeNam;
	}

	public void setEmployeeNam(String employeeNam) {
		this.employeeNam = employeeNam;
	}

	public String getTitnam() {
		return titnam;
	}

	public void setTitnam(String titnam) {
		this.titnam = titnam;
	}

	public String getDlgEmployeeNam() {
		return dlgEmployeeNam;
	}

	public void setDlgEmployeeNam(String dlgEmployeeNam) {
		this.dlgEmployeeNam = dlgEmployeeNam;
	}

	public String getDlgTitnam() {
		return dlgTitnam;
	}

	public void setDlgTitnam(String dlgTitnam) {
		this.dlgTitnam = dlgTitnam;
	}

	public String getHbAgree() {
		return hbAgree;
	}

	public void setHbAgree(String hbAgree) {
		this.hbAgree = hbAgree;
	}

	private String hbAgree;

	public long getWorkId() {
		return workId;
	}

	public void setWorkId(long workId) {
		this.workId = workId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getProcessManName() {
		return processManName;
	}

	public void setProcessManName(String processManName) {
		this.processManName = processManName;
	}

	SimpleDateFormat displaySdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	private String dlg_cmpcode;

	private String dlg_a_deptcode;

	private String dlg_deptcode;

	private String dlg_postcode;

	private String dlg_employeeid;

	private String postCode;

	private String cmpCode;

	private String deptCode;

	private String deptId;

	private String dlgDeptId;

	public Object getWorkStatus;

	public String getProcessTime() {
		return processTime;
	}

	public void setProcessTime(String processTime) {
		this.processTime = processTime;
	}

	public void setCalculatedProcessTime(Date calculatedProcessTime) {
		this.calculatedProcessTime = calculatedProcessTime;
		this.processTime = displaySdf.format(this.calculatedProcessTime);
	}

	public Date getCalculateProcessTime() {
		return this.calculatedProcessTime;
	}

	public String getWorkStatus() {
		return workStatus;
	}

	public void setWorkStatus(String workStatus) {
		this.workStatus = workStatus;
	}

	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}

	public boolean isHaveChildren() {
		return haveChildren;
	}

	public void setHaveChildren(boolean haveChildren) {
		this.haveChildren = haveChildren;
	}

	public String getUserRealName() {
		return userRealName;
	}

	public void setUserRealName(String userRealName) {
		this.userRealName = userRealName;
	}

	public void setWorkNum(int workNum) {
		this.workNum = workNum;
	}

	public int getWorkNum() {
		return workNum;
	}

	public void setStatus(FlowStatus status) {
		this.status = status;
	}

	public FlowStatus getStatus() {
		return status;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	public int getStage() {
		return stage;
	}

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param postCode
	 */
	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param dlg_employeeid
	 */
	public void setDlgEmployeeId(String dlg_employeeid) {
		this.dlg_employeeid = dlg_employeeid;
	}

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param dlg_postcode
	 */
	public void setDlgPostCode(String dlg_postcode) {
		this.dlg_postcode = dlg_postcode;
	}

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param dlg_deptcode
	 */
	public void setDlgDeptCode(String dlg_deptcode) {
		this.dlg_deptcode = dlg_deptcode;	
	}

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param dlg_a_deptcode
	 */
	public void setDlgADeptCode(String dlg_a_deptcode) {
		this.dlg_a_deptcode = dlg_a_deptcode;
	}

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param dlg_cmpcode
	 */
	public void setDlgCmpCode(String dlg_cmpcode) {
		this.dlg_cmpcode = dlg_cmpcode;
	}

	public String getDlg_cmpcode() {
		return dlg_cmpcode;
	}

	public String getDlg_a_deptcode() {
		return dlg_a_deptcode;
	}

	public String getDlg_deptcode() {
		return dlg_deptcode;
	}

	public String getDlg_postcode() {
		return dlg_postcode;
	}

	public String getDlg_employeeid() {
		return dlg_employeeid;
	}

	public String getPostCode() {
		return postCode;
	}

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param string
	 */
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param string
	 */
	public void setCmpCode(String cmpCode) {
		this.cmpCode = cmpCode;
	}

	public String getCmpCode() {
		return cmpCode;
	}

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public String getDeptId() {
		return deptId;
	}
	
	public void setDlgDeptId(String dlgDeptId) {
		this.dlgDeptId = dlgDeptId;
	}

	public String getDlgDeptId() {
		return dlgDeptId;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
		    return true;
		}
		if (obj instanceof MyWorkHistory) {
			MyWorkHistory item = (MyWorkHistory) obj;
		    return item.getWorkId() == this.getWorkId();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) (this.getWorkId() * 37);
	}
}
