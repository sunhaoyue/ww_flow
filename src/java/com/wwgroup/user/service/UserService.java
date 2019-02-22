package com.wwgroup.user.service;

import java.util.List;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRGroupMgr;
import com.wwgroup.user.bo.EmployeePos;
import com.wwgroup.user.bo.HbMgrUser;
import com.wwgroup.user.bo.SystemUsers;

public interface UserService {

	/**
	 * 查找某个用户组内的所有用户
	 * 
	 * @param groupID
	 * @return
	 */
	List<PersonDetail> getUsersByGroup(int groupID);

	/**
	 * 分页查找某个用户组内的所有用户
	 * 
	 * @param groupID
	 * @param start
	 * @param size
	 * @return
	 */
	Page getUsersByGroupWithPage(int groupID, int start, int size);

	/**
	 * 根据人员编号查询用户信息
	 * 
	 * @param Name
	 * @return
	 */
	SystemUsers getUsersByName(String name);

	/**
	 * 根据员工编号获取人员主岗位的关系信息（主岗位）
	 * 
	 * @param userId
	 * @return
	 */
	EmployeePos getEmployeePosByEmpId(String employeeid);
	
	/**
	 * 根所员工编号及单位代码，获取人员兼职单位的信息
	 */
	List<EmployeePos> getEmployeePosListPTJob(String employeeid, String cmpcod);

	/**
	 * 根据员工编号、职位代码获取人员岗位的关系信息
	 * 
	 * @param employeeid
	 * @param deptCode
	 * @param comCode
	 * @return
	 */
	EmployeePos getEmployeePosByEmpId(String deptCode, String comCode, String employeeid, String postCode);

	/**
	 * 根据员工编号、职位代码获取人员岗位的关系信息
	 * 
	 * @param employeeid
	 * @param deptCode
	 * @param comCode
	 * @return
	 */
	EmployeePos getEmployeePosByEmpId(String employeeid, String postCode);

	/**
	 * 获取某个部门\中心\单位主管的人员岗位的关系信息
	 * 
	 * @param employeeid
	 * @param deptCode
	 * @param comCode
	 * @param forceAssign
	 * @return
	 */
	@Deprecated
	EmployeePos getMgrEmployeePos(String deptCode, String comCode, String employeeid, boolean forceAssign);

	/**
	 * 获取某个部门\中心\单位主管的人员岗位的关系信息
	 * 
	 * @param employeeid
	 * @param deptCode
	 * @param comCode
	 * @param forceAssign
	 * @return
	 */
	EmployeePos getMgrEmployeePos(String deptCode, String a_deptCode, String comCode, String employeeid,
			GroupType groupType);

	/**
	 * 查询某人是否为部门/中心/单位主管，不是返回null
	 * 
	 * @param deptId
	 * @return
	 */
	HRGroupMgr getGroupMgrByDept(String deptCode, String a_deptCode, String comCode, String employeeid,
			DescionMaker descionMaker);
	
	/**
	 * 查询某人是否为部门/中心/单位副主管，不是返回null
	 * 
	 * @param deptId
	 * @return
	 */
	HRGroupMgr getGroupMgrByDeptPlus(String deptCode, String a_deptCode, String comCode, String employeeid,
			DescionMaker descionMaker);

	/**
	 * 查询部门/中心/单位主管(出现一个部门有两个主管的情况，获取 第一个主管)
	 * 
	 * @param deptId
	 * @return
	 */
	HRGroupMgr getGroupMgrByDept(String deptCode, String a_deptCode, String comCode, DescionMaker descionMaker);

	/**
	 * 查询部门/中心/单位主管(出现一个部门有两个主管的情况，获取 所有主管)
	 * 
	 * @param deptId
	 * @return
	 */
	List<HRGroupMgr> getGroupMgrsByDept(String deptCode, String a_deptCode, String comCode, DescionMaker descionMaker);

	/**
	 * 根据用户查询所有主管明细
	 * 
	 * @param employeeId
	 * @return
	 */
	List<HRGroupMgr> getGroupMgrByUserName(String employeeId, String type);

	EmployeePos getCenterEmployeeByCmpCode(String cmpCode);
	
	EmployeePos getCenterFEmployeeByCmpCode(String cmpCode);

	List<HRGroupMgr> getGroupMgrByDeptPlus(String deptCode, String a_deptCode,
			String cmpCode, DescionMaker descionMaker);

	HbMgrUser getHbMgrUser(String deptId);

	EmployeePos getChairmanEmployeePos();
	
	EmployeePos getSubmitFBossEmployeePos();

	/**
	 * 这是一个特殊方法，用于获取职能部门GroupID及下属部门中所有的部门和中心主管
	 * @param deptId
	 * @return
	 */
	List<HRGroupMgr> getGroupMgrByDeptEx(String deptId);
}
