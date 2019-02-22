package com.wwgroup.organ.service;

import java.util.List;

import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRCompany;
import com.wwgroup.organ.bo.HRPosition;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.user.bo.EmployeePos;

public interface OrganService {

	/**
	 * 根据父节点查找子节点列表
	 */
	List<SystemGroups> getGroupsByParent(int parentID);

	/**
	 * 根据代码查询所属组织（若三个code都为空，则查询转投资总部）
	 * 
	 * @param deptCode
	 * @return
	 */
	SystemGroups getGroupsByDeptCode(String deptCode, String a_deptCode, String cmpCode, GroupType groupType);

	/**
	 * 根据岗位代码查询岗位信息
	 * 
	 * @param postCode
	 * @return
	 */
	HRPosition getPositionByPostCode(String postCode, String deptCode, String cmpCode);

	/**
	 * 根据部门ID查询部门信息
	 * 
	 * @param groupId
	 * @return
	 */
	SystemGroups loadGroupsById(int groupId);

	/**
	 * 根据父节点查找所有子节点列表
	 * 
	 * @param parentID
	 * @return
	 */
	List<SystemGroups> getAllGroupsByParent(int parentID);
	List<SystemGroups> getAllGroupsByParent(int parentID,EmployeePos parentEmployeePos);

	/**
	 * 根据用户查询所有单位、中心的职位列表
	 * 
	 * @param employeeId
	 * @return
	 */
	List<HRPosition> getAssistPositionsByUser(String employeeIdgroupType, GroupType groupType);
	
	/**
	 * 过滤到公司层级（流程代理人设定选择）
	 * @param id
	 * @return
	 */
	int filterToCompanyLevel(int id);
	
	/**
	 * 获得人员的所有岗位的部门
	 * 
	 * 依据GroupType确定groupid
	 * 
	 */
	public List<SystemGroups> getUserGroup(String employeeid);
	
	/**
	 * 根据单位代码获取所有部门
	 */
	public List<SystemGroups> getAllGroupsByCode(String cmpcod);

	List<HRCompany> getCompanyList();

	int getSizeByP(int groupid, int parentid);

	List<SystemGroups> getCanUpAttachOrg(String groupid);

	int getJYJurisdiction(String empid);

	List<SystemGroups> getGroupsByCmp(int parentID, String cmpcod);
}
