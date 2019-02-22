package com.wwgroup.user.dao;

import java.util.List;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRGroupMgr;
import com.wwgroup.user.bo.EmployeePos;
import com.wwgroup.user.bo.HbMgrUser;
import com.wwgroup.user.bo.SystemUsers;

public interface UserDao {

	List<PersonDetail> getUsersByGroup(int groupID);

	EmployeePos getEmployeePosByEmpId(String employeeid);
	
	List<EmployeePos> getEmployeePosListPTJob(String employeeid, String cmpcod);

	SystemUsers getUsersByName(String name);

	EmployeePos getEmployeePosByEmpId(String deptCode, String comCode, String employeeid, String postCode);

	HRGroupMgr getGroupMgrByDept(String deptCode, String a_deptCode, String comCode, String employeeid,
			DescionMaker descionMaker);
	
	HRGroupMgr getGroupMgrByDeptPlus(String deptCode, String a_deptCode, String comCode, String employeeid,
			DescionMaker descionMaker);

	Page getUsersByGroupWithPage(int groupID, int start, int size);

	List<HRGroupMgr> getGroupMgrByDept(String deptCode, String a_deptCode, String comCode, DescionMaker descionMaker);

	EmployeePos getMgrEmployeePos(String deptCode, String comCode, String employeeid, boolean forceAssign);

	EmployeePos getEmployeePosByEmpId(String employeeid, String postCode);

	List<HRGroupMgr> getGroupMgrByUserName(String employeeId, String type);

	EmployeePos getMgrEmployeePos(String deptCode, String a_deptCode, String comCode, String employeeid,
			GroupType groupType);

	EmployeePos getCenterEmployeeByCmpCode(String cmpCode);

	EmployeePos getCenterFEmployeeByCmpCode(String cmpCode);

	List<HRGroupMgr> getGroupMgrByDeptPlus(String deptCode, String a_deptCode,
			String cmpCode, DescionMaker descionMaker);

	HbMgrUser getHbMgrUser(String deptId);

	/**
	 * 通过HR_EMPPOS查找是董事长岗位的第一条记录
	 * MGCMPFLG='1'
	 * @return
	 */
	EmployeePos getChairmanEmployeePos();
	
	/**
	 * 通过HR_EMPPOS查找是副总裁的第一条记录
	 * FFBossFlg='1'
	 * @return
	 */
	EmployeePos getSubmitFBossEmployeePos();

	/**
	 * 这是一个特殊方法，用于获取职能部门GroupID及下属部门中所有的部门和中心主管
	 * @param deptId
	 * @return
	 */
	List<HRGroupMgr> getGroupMgrByDeptEx(String deptId);
}
