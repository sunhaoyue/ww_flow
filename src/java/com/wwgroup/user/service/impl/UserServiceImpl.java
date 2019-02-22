package com.wwgroup.user.service.impl;

import java.util.List;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRGroupMgr;
import com.wwgroup.user.bo.EmployeePos;
import com.wwgroup.user.bo.HbMgrUser;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.dao.UserDao;
import com.wwgroup.user.service.UserService;

public class UserServiceImpl implements UserService {

	private UserDao userDao;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public List<PersonDetail> getUsersByGroup(int groupID) {
		return userDao.getUsersByGroup(groupID);
	}

	@Override
	public EmployeePos getEmployeePosByEmpId(String employeeid) {
		return userDao.getEmployeePosByEmpId(employeeid);
	}
	
	@Override
	public List<EmployeePos> getEmployeePosListPTJob(String employeeid, String cmpcod){
		return userDao.getEmployeePosListPTJob(employeeid, cmpcod);
	}

	@Override
	public SystemUsers getUsersByName(String name) {
		return userDao.getUsersByName(name);
	}

	@Override
	public EmployeePos getEmployeePosByEmpId(String deptCode, String comCode, String employeeid, String postCode) {
		return userDao.getEmployeePosByEmpId(deptCode, comCode, employeeid, postCode);
	}

	@Override
	public HRGroupMgr getGroupMgrByDept(String deptCode, String a_deptCode, String comCode, String employeeid,
			DescionMaker descionMaker) {
		return userDao.getGroupMgrByDept(deptCode, a_deptCode, comCode, employeeid, descionMaker);
	}
	
	@Override
	public HRGroupMgr getGroupMgrByDeptPlus(String deptCode, String a_deptCode, String comCode, String employeeid,
			DescionMaker descionMaker) {
		return userDao.getGroupMgrByDeptPlus(deptCode, a_deptCode, comCode, employeeid, descionMaker);
	}

	@Override
	public Page getUsersByGroupWithPage(int groupID, int start, int size) {
		return userDao.getUsersByGroupWithPage(groupID, start, size);
	}

	@Override
	public HRGroupMgr getGroupMgrByDept(String deptCode, String a_deptCode, String comCode, DescionMaker descionMaker) {
		List<HRGroupMgr> mgr = userDao.getGroupMgrByDept(deptCode, a_deptCode, comCode, descionMaker);
		return mgr != null && mgr.size() > 0 ? mgr.get(0) : null;
	}
	
	@Override
	public List<HRGroupMgr> getGroupMgrByDeptPlus(String deptCode, String a_deptCode, String cmpCode, DescionMaker descionMaker) {
		List<HRGroupMgr> mgr = userDao.getGroupMgrByDeptPlus(deptCode, a_deptCode, cmpCode, descionMaker);
		return mgr != null && mgr.size() > 0 ? mgr : null;
	}

	@Override
	public EmployeePos getMgrEmployeePos(String deptCode, String comCode, String employeeid, boolean forceAssign) {
		return userDao.getMgrEmployeePos(deptCode, comCode, employeeid, forceAssign);
	}

	@Override
	public EmployeePos getEmployeePosByEmpId(String employeeid, String postCode) {
		return userDao.getEmployeePosByEmpId(employeeid, postCode);
	}

	@Override
	public List<HRGroupMgr> getGroupMgrsByDept(String deptCode, String a_deptCode, String comCode,
			DescionMaker descionMaker) {
		List<HRGroupMgr> mgr = userDao.getGroupMgrByDept(deptCode, a_deptCode, comCode, descionMaker);
		return mgr != null && mgr.size() > 0 ? mgr : null;
	}
	
	@Override
	public List<HRGroupMgr> getGroupMgrByDeptEx(String deptId){
		List<HRGroupMgr> mgr = userDao.getGroupMgrByDeptEx(deptId);
		return mgr != null && mgr.size() > 0 ? mgr : null;
	}

	@Override
	public List<HRGroupMgr> getGroupMgrByUserName(String employeeId, String type) {
		List<HRGroupMgr> mgr = userDao.getGroupMgrByUserName(employeeId, type);
		return mgr != null && mgr.size() > 0 ? mgr : null;
	}

	@Override
	public EmployeePos getMgrEmployeePos(String deptCode, String a_deptCode, String comCode, String employeeid,
			GroupType groupType) {
		return userDao.getMgrEmployeePos(deptCode, a_deptCode, comCode, employeeid, groupType);
	}

	@Override
	public EmployeePos getCenterEmployeeByCmpCode(String cmpCode) {
		return userDao.getCenterEmployeeByCmpCode(cmpCode);
	}
	
	public EmployeePos getCenterFEmployeeByCmpCode(String cmpCode) {
		return userDao.getCenterFEmployeeByCmpCode(cmpCode);
	}
	
	@Override
	public HbMgrUser getHbMgrUser(String deptId){
		return userDao.getHbMgrUser(deptId);
	}
	
	@Override
	public EmployeePos getChairmanEmployeePos(){
		return userDao.getChairmanEmployeePos();
	}
	
	@Override
	public EmployeePos getSubmitFBossEmployeePos(){
		return userDao.getSubmitFBossEmployeePos();
	}
}
