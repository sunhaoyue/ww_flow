package com.wwgroup.flow.dto;

public enum Branch {
// 结合flowType
//分别代表：
//  全部(管理员)	 我申请           本单位发文                  下辖部门提交          我签核的           我接收		 编号	  主旨         我代签核的
	All,MySubmit, ThisUnitSubmit, SubDeptSubmit, MyApproved, MyAccepted, FormNum, Title, MyDlg
	
	// 目前不清楚的分支是 ： 按内联编号， 内联主旨

}
