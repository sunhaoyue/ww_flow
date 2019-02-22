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

package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

/**
 * <p>
 * Title: cuteinfo_[子系统统名]_[模块名]
 * </p>
 * <p>
 * Description: 就是供外层填入，历程显示使用
 * </p>
 * 
 * @author Administrator
 * @version $Revision$ 2012-7-28
 * @author (lastest modification by $Author$)
 * @since 20100620
 */
public abstract class MyDelegateWork extends MyWork {

	private String dlgDeptId, dlgEmployeeId, dlgPostCode, dlgDeptCode, dlgADeptCode, dlgCmpCode;
	
	private String dlgEmployeenam;
	private String dlgTitnam;

	public MyDelegateWork(WorkStage workStage) {
		super(workStage);
	}

	public String getDlgDeptId() {
		return dlgDeptId;
	}

	public void setDlgDeptId(String dlgDeptId) {
		this.dlgDeptId = dlgDeptId;
	}

	public String getDlgEmployeeId() {
		return dlgEmployeeId;
	}

	public void setDlgEmployeeId(String dlgEmployeeId) {
		this.dlgEmployeeId = dlgEmployeeId;
	}

	public String getDlgPostCode() {
		return dlgPostCode;
	}

	public void setDlgPostCode(String dlgPostCode) {
		this.dlgPostCode = dlgPostCode;
	}

	public String getDlgDeptCode() {
		return dlgDeptCode;
	}

	public void setDlgDeptCode(String dlgDeptCode) {
		this.dlgDeptCode = dlgDeptCode;
	}

	public String getDlgADeptCode() {
		return dlgADeptCode;
	}

	public void setDlgADeptCode(String dlgADeptCode) {
		this.dlgADeptCode = dlgADeptCode;
	}

	public String getDlgCmpCode() {
		return dlgCmpCode;
	}

	public void setDlgCmpCode(String dlgCmpCode) {
		this.dlgCmpCode = dlgCmpCode;
	}

	public String getDlgEmployeenam() {
		return dlgEmployeenam;
	}

	public void setDlgEmployeenam(String dlgEmployeenam) {
		this.dlgEmployeenam = dlgEmployeenam;
	}

	public String getDlgTitnam() {
		return dlgTitnam;
	}

	public void setDlgTitnam(String dlgTitnam) {
		this.dlgTitnam = dlgTitnam;
	}

}
