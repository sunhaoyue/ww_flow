package com.wwgroup.organ.dao;

import java.util.List;

import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRCompany;
import com.wwgroup.organ.bo.HRPosition;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.user.bo.EmployeePos;

public interface OrganDao {

	List<SystemGroups> getGroupsByParent(int parentID);

	SystemGroups getGroupsByDeptCode(String deptCode, String a_deptCode, String cmpCode, GroupType groupType);

	HRPosition getPositionByPostCode(String postCode, String deptCode, String cmpCode);

	SystemGroups loadGroupsById(int groupId);

	List<SystemGroups> getAllGroupsByParent(String parentId);
	List<SystemGroups> getAllGroupsByParent(String orgPath,EmployeePos parentEmployeePos);

	List<HRPosition> getAssistPositionsByUser(String employeeId, GroupType groupType);
	
	int filterToCompanyLevel(int id);
	
	public List<SystemGroups> getUserGroup(String employeeid);

	boolean filterMgrPositionByUser(String employeeId, GroupType groupType, String position);

	EmployeePos loadEmployeePos(String employeeid, String deptCode, String a_deptCode, String cmpCode);
	
	List<SystemGroups> getAllGroupsByCode(String cmpcod);

	List<HRCompany> getCompanyList();

	int getSizeByP(int groupid, int parentid);

	List<SystemGroups> getCanUpAttachOrg(String groupid);

	int getJYJurisdiction(String empid);

	List<SystemGroups> getGroupsByCmp(int parentID, String cmpcod);
}
