package com.wwgroup.organ.service.impl;

import java.util.List;

import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRCompany;
import com.wwgroup.organ.bo.HRPosition;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.dao.OrganDao;
import com.wwgroup.organ.service.OrganService;
import com.wwgroup.user.bo.EmployeePos;

public class OrganServiceImpl implements OrganService {

	private OrganDao organDao;

	public void setOrganDao(OrganDao organDao) {
		this.organDao = organDao;
	}

	@Override
	public List<SystemGroups> getGroupsByParent(int parentID) {
		return organDao.getGroupsByParent(parentID);
	}

	@Override
	public SystemGroups getGroupsByDeptCode(String deptCode, String a_deptCode, String cmpCode, GroupType groupType) {
		return organDao.getGroupsByDeptCode(deptCode, a_deptCode, cmpCode, groupType);
	}

	@Override
	public HRPosition getPositionByPostCode(String postCode, String deptCode, String cmpCode) {
		return organDao.getPositionByPostCode(postCode, deptCode, cmpCode);
	}

	@Override
	public SystemGroups loadGroupsById(int groupId) {
		return organDao.loadGroupsById(groupId);
	}

	@Override
	public List<SystemGroups> getAllGroupsByParent(int parentID) {
		//SystemGroups groups = organDao.loadGroupsById(parentID);
		//return organDao.getAllGroupsByParent(String.valueOf(groups.getGroupID()));
		return organDao.getAllGroupsByParent(String.valueOf(parentID));
	}

	@Override
	public List<HRPosition> getAssistPositionsByUser(String employeeId, GroupType groupType) {
		return organDao.getAssistPositionsByUser(employeeId, groupType);
	}
	
	@Override
	public int filterToCompanyLevel(int id) {
		return organDao.filterToCompanyLevel(id);
	}
	
	@Override
	public List<SystemGroups> getUserGroup(String employeeid) {
		return organDao.getUserGroup(employeeid);
	}

	@Override
	public List<SystemGroups> getAllGroupsByParent(int parentID,EmployeePos parentEmployeePos) {
		SystemGroups groups = organDao.loadGroupsById(parentID);
		return organDao.getAllGroupsByParent(groups.getOrgPath(),parentEmployeePos);
	}

	public List<SystemGroups> getAllGroupsByCode(String cmpcod){
		return organDao.getAllGroupsByCode(cmpcod);
	}
	
	@Override
	public List<HRCompany> getCompanyList(){
		return organDao.getCompanyList();
	}
	
	@Override
	public int getSizeByP(int groupid, int parentid){
		return organDao.getSizeByP(groupid, parentid);
	}
	
	@Override
	public List<SystemGroups> getCanUpAttachOrg(String groupid){
		return organDao.getCanUpAttachOrg(groupid);
	}
	
	@Override
	public int getJYJurisdiction(String empid){
		return organDao.getJYJurisdiction(empid);
	}
	
	@Override
	public List<SystemGroups> getGroupsByCmp(int parentID, String cmpcod) {
		return organDao.getGroupsByCmp(parentID, cmpcod);
	}
}
