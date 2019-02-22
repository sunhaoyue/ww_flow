package com.wwgroup.flow.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.dao.FlowDao;
import com.wwgroup.flow.dto.AdvancedSearchDTO;
import com.wwgroup.flow.dto.ConfidentialSearchDTO;
import com.wwgroup.flow.dto.ConfidentialSearchbossDTO;
import com.wwgroup.flow.dto.LogDTO;
import com.wwgroup.flow.dto.LogInfo;
import com.wwgroup.flow.dto.MyWorkDTO;
import com.wwgroup.flow.service.FlowAgentService;
import com.wwgroup.flow.service.FlowAssistService;
import com.wwgroup.flow.service.FlowManagementService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.organ.bo.HRCompany;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.service.OrganService;

public class FlowManagementServiceImpl implements FlowManagementService {

	private FlowDao flowDao;

	private PersonService personService;

	private FlowAgentService flowAgentService;

	private FlowAssistService flowAssistService;
	
	private OrganService organService;

	public OrganService getOrganService() {
		return organService;
	}

	public void setOrganService(OrganService organService) {
		this.organService = organService;
	}

	public void setFlowDao(FlowDao flowDao) {
		this.flowDao = flowDao;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setFlowAgentService(FlowAgentService flowAgentService) {
		this.flowAgentService = flowAgentService;
	}

	public void setFlowAssistService(FlowAssistService flowAssistService) {
		this.flowAssistService = flowAssistService;
	}

	@Override
	public Page findFlowTemplatesByPage(PersonDetail actualPerson, int start, int size) {
		return this.flowDao.findFlowTemplatesByPage(actualPerson, start, size);
	}

	@Override
	public int getMyToDoWorkCount(PersonDetail actualPerson) {
		PersonDetail[] agentees = flowAgentService.getAgentees(actualPerson);
		PersonDetail[] assistedMgrs = flowAssistService.getAssistedMgrs(actualPerson);
		return this.flowDao.getMyToDoWorkCount(actualPerson, agentees, assistedMgrs);
	}

	@Override
	public Page findMyToDoWorkByPage(PersonDetail actualPerson, int start, int size) {
		// 再进入之前需要将 额外的被代里人的人员信息组都获取到，然后一并传入
		PersonDetail[] agentees = flowAgentService.getAgentees(actualPerson);
		// TODO: 助理入口 首先一点 需要确认 这个助理人是否可以能收某个个主管的单子，如果可以，额外读取出是否可以指派
		PersonDetail[] assistedMgrs = flowAssistService.getAssistedMgrs(actualPerson);
		return this.flowDao.findMyToDoWorkByPage(actualPerson, agentees, assistedMgrs, start, size);
	}

	@Override
	public Page findCreatorFlowByPage(PersonDetail actualPerson, int start, int size) {
		// 再进入之前需要将 额外的被代里人的人员信息组都获取到，然后一并传入
		PersonDetail[] agentees = flowAgentService.getAgentees(actualPerson);
		// TODO: 助理入口 首先一点 需要确认 这个助理人是否可以能收某个个主管的单子，如果可以，额外读取出是否可以指派
		PersonDetail[] assistedMgrs = flowAssistService.getAssistedMgrs(actualPerson);
		return this.flowDao.findCreatorFlowByPage(actualPerson, agentees,assistedMgrs, start, size);
	}

	@Override
	public Page findMyProcessFlowByPage(PersonDetail actualPerson, int start, int size) {
		return this.flowDao.findMyProcessFlowByPage(actualPerson, start, size);
	}

	@Override
	public int getCreatorFlowCount(PersonDetail actualPerson) {
		// 再进入之前需要将 额外的被代里人的人员信息组都获取到，然后一并传入
		PersonDetail[] agentees = flowAgentService.getAgentees(actualPerson);
		// TODO: 助理入口 首先一点 需要确认 这个助理人是否可以能收某个个主管的单子，如果可以，额外读取出是否可以指派
		PersonDetail[] assistedMgrs = flowAssistService.getAssistedMgrs(actualPerson);
		return this.flowDao.getCreatorFlowCount(actualPerson, agentees,assistedMgrs);
	}

	@Override
	public int getFlowTemplateCount(PersonDetail actualPerson) {
		return this.flowDao.getFlowTemplateCount(actualPerson);
	}

	@Override
	public int getMyProcessFlowCount(PersonDetail actualPerson) {
		return this.flowDao.getMyProcessFlowCount(actualPerson);
	}

	@Override
	public Page searchWorkDTOByPage(AdvancedSearchDTO oneSearch, int start, int size) {
		
		//所有岗位部门
		List<String> deptIds = new ArrayList<String>();
		List<SystemGroups> groups = this.organService.getUserGroup(oneSearch.getEmployeeId());
		for (SystemGroups s : groups) {
			deptIds.add(String.valueOf(s.getGroupID()));
		}
		oneSearch.setUserDeptIds(deptIds.toArray(new String[deptIds.size()]));
		//所有岗位部门的子部门
		List<String> subDeptIdsList = new ArrayList<String>();
		for (String deptId : deptIds) {
			String[] s = this.personService.getSubDeptIds(deptId);
			if (s != null) {
				subDeptIdsList.addAll(Arrays.asList(s));
			}
		}
		if (subDeptIdsList.size() != 0) {
			oneSearch.setSubDeptIds(subDeptIdsList.toArray(new String[subDeptIdsList.size()]));
		}
		return this.flowDao.advanceSearch(oneSearch, start, size);
	}
	
	@Override
	public void delTempForm(List<String> formnumbers) {
		flowDao.delTempForm(formnumbers);
	}
	
	public Page findCopyFlowByPage(PersonDetail actualPerson, int start, int size){
		// 再进入之前需要将 额外的被代里人的人员信息组都获取到，然后一并传入
		PersonDetail[] agentees = flowAgentService.getAgentees(actualPerson);
		// TODO: 助理入口 首先一点 需要确认 这个助理人是否可以能收某个个主管的单子，如果可以，额外读取出是否可以指派
		PersonDetail[] assistedMgrs = flowAssistService.getAssistedMgrs(actualPerson);
		return this.flowDao.findCopyFlowByPage(actualPerson, agentees, assistedMgrs, start, size);
	}
	
	public int getCopyFlowCount(PersonDetail actualPerson){
		// 再进入之前需要将 额外的被代里人的人员信息组都获取到，然后一并传入
		PersonDetail[] agentees = flowAgentService.getAgentees(actualPerson);
		// TODO: 助理入口 首先一点 需要确认 这个助理人是否可以能收某个个主管的单子，如果可以，额外读取出是否可以指派
		PersonDetail[] assistedMgrs = flowAssistService.getAssistedMgrs(actualPerson);
		return this.flowDao.getCopyFlowCount(actualPerson, agentees, assistedMgrs);
	}
	
	public int getSecurityFlowCount(){
		return this.flowDao.getSecurityFlowCount();
	}
	
	@Override
	public Page findSecurityFlowByPage(AdvancedSearchDTO onSearchDTO, int start, int size) {
		return this.flowDao.findSecurityFlowByPage(onSearchDTO, start, size);
	}
	
	@Override
	public List<HRCompany> getCompanyList(){
		return this.organService.getCompanyList();
	}
	
	@Override
	public List<MyWorkDTO> searchWorkDTOExport(AdvancedSearchDTO oneSearch) {
		//所有岗位部门
		List<String> deptIds = new ArrayList<String>();
		List<SystemGroups> groups = this.organService.getUserGroup(oneSearch.getEmployeeId());
		for (SystemGroups s : groups) {
			deptIds.add(String.valueOf(s.getGroupID()));
		}
		oneSearch.setUserDeptIds(deptIds.toArray(new String[deptIds.size()]));
		//所有岗位部门的子部门
		List<String> subDeptIdsList = new ArrayList<String>();
		for (String deptId : deptIds) {
			String[] s = this.personService.getSubDeptIds(deptId);
			if (s != null) {
				subDeptIdsList.addAll(Arrays.asList(s));
			}
		}
		if (subDeptIdsList.size() != 0) {
			oneSearch.setSubDeptIds(subDeptIdsList.toArray(new String[subDeptIdsList.size()]));
		}
		return this.flowDao.advanceSearchToExport(oneSearch);
	}
	
	@Override
	public Page findLogInfoByPage(String startDate, String logLevel, int start, int size){
		return this.flowDao.findLogInfoByPage(startDate, logLevel, start, size);
	}
	
	@Override
	public List<LogInfo> findLogInfoByExport(String startDate, String logLevel){
		return this.flowDao.findLogInfoByExport(startDate, logLevel);
	}
	
	@Override
	public Page findCreatorFlowByPageEx(PersonDetail actualPerson, int start, int size, String sidx, String sord, AdvancedSearchDTO searchDTO) {
		// 再进入之前需要将 额外的被代里人的人员信息组都获取到，然后一并传入
		PersonDetail[] agentees = flowAgentService.getAgentees(actualPerson);
		// TODO: 助理入口 首先一点 需要确认 这个助理人是否可以能收某个个主管的单子，如果可以，额外读取出是否可以指派
		PersonDetail[] assistedMgrs = flowAssistService.getAssistedMgrs(actualPerson);
		return this.flowDao.findCreatorFlowByPageEx(actualPerson, agentees,assistedMgrs, start, size, sidx, sord, searchDTO);
	}
	
	@Override
	public Page searchConfidByPage(ConfidentialSearchDTO oneSearch, int start, int size) {
		return this.flowDao.confSearch(oneSearch, start, size);
	}
	
	@Override
	public void saveLog(LogDTO logDto){
		this.flowDao.saveLog(logDto);
	}
	
	@Override
	public List<MyWorkDTO> searchConfidExport(ConfidentialSearchDTO searchDTO) {
		return this.flowDao.confSearchToExport(searchDTO);
	}
	
	@Override
	public Page searchConfidbossByPage(ConfidentialSearchbossDTO oneSearch, int start, int size) {
		return this.flowDao.confSearchboss(oneSearch, start, size);
	}
	
	@Override
	public List<MyWorkDTO> searchConfidbossExport(ConfidentialSearchbossDTO searchDTO) {
		return this.flowDao.confSearchbossToExport(searchDTO);
	}
	
}
