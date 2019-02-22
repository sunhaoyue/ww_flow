package com.wwgroup.flow.dao;

import java.util.List;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowAttachment;
import com.wwgroup.flow.bo.FlowContent;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.helper.PersonTYPE;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.dto.AdvancedSearchDTO;
import com.wwgroup.flow.dto.ConfidentialSearchDTO;
import com.wwgroup.flow.dto.ConfidentialSearchbossDTO;
import com.wwgroup.flow.dto.LogDTO;
import com.wwgroup.flow.dto.LogInfo;
import com.wwgroup.flow.dto.MyWorkDTO;
import com.wwgroup.flow.dto.MyWorkHistory;
import com.wwgroup.organ.dao.OrganDao;

public interface FlowDao {

	void saveFlow(Flow flow);

	void savePerson(PersonDetail createPerson, PersonTYPE personTYPE);

	void saveContent(FlowContent content);

	void saveAttachments(FlowAttachment[] flowAttachments);

	String getFlowSequence(PersonDetail actualPerson);

	void saveWork(MyWork work, OrganDao organDao);

	Page getWorksWithPage(PersonDetail person, int start, int size);

	void updateWork(MyWork work);
	void updateAdminAssignWork(final MyWork mywork);

	Flow loadFlow(long flowId, FlowType flowType);

	PersonDetail loadPerson(long flowId, PersonTYPE personTYPE);
	
	PersonDetail loadPerson(long personId);

	FlowContent loadContent(long flowId);

	MyWork loadWork(long assignorId);

	FlowAttachment[] loadAttachments(long flowId);
	
	int getWorkCount(Flow flow, WorkStage innerjointsign, FlowStatus agree, FlowType flowType);

	void updateFlow(Flow flow);

	Flow[] loadFlowTemplate(String userName, FlowType flowType);

	void clearFlowTemplate(Flow flow);

	Flow loadFlow(String formnum);

	Page findFlowTemplatesByPage(PersonDetail actualPerson, int start, int size);

	Page findMyToDoWorkByPage(PersonDetail actualPerson, PersonDetail[] agentees, PersonDetail[] assistedMgrs, int start, int size);

	Page findCreatorFlowByPage(PersonDetail actualPerson, PersonDetail[] agentees, PersonDetail[] assistedMgrs,int start, int size);

	Page findMyProcessFlowByPage(PersonDetail actualPerson, int start, int size);

	int getCreatorFlowCount(PersonDetail actualPerson, PersonDetail[] agentees,PersonDetail[] assistedMgrs);

	int getFlowTemplateCount(PersonDetail actualPerson);

	int getMyProcessFlowCount(PersonDetail actualPerson);

	int getMyToDoWorkCount(PersonDetail actualPerson, PersonDetail[] agentees, PersonDetail[] assistedMgrs);

	MyWorkHistory[] listWorkHistory(String formNum);

	MyWorkHistory[] listWorkHistory(String formNum, long parentWorkId);

	// 将审核主管人的信息记录到flow当中去
	void saveFlowShengheProperties(long id, String employeeId, String deptId, String postCode, int finishedShengheStep);
	
	// 将审核副主管人的信息记录到flow当中去
	void saveFlowSecondProperties(long id, String employeeId, String deptId, String postCode);

	// 该流程下 会办分支 的所有work employeeId 等于 startEmployeeId的那个work列表，状态都是
	boolean hasJointSignFinished(Flow flow);
	
	boolean hasCmpcodeJointSignFinished(Flow flow,String cmpcode);

	// 复杂查询
	Page advanceSearch(AdvancedSearchDTO oneSearch, int start, int size);

	void linkFlowAttachment(Flow flow, FlowAttachment[] flowAttachments);

	void updateContent(FlowContent content);

	void updatePerson(PersonDetail person, PersonTYPE personTYPE);
	
	FlowAttachment loadAttachment(long attachmentId);

	void deleteFlowAttachment(long attahmentId);

	MyWork[] listWorks(Flow flow);

	void clearFlow(Flow flow);
	
	//是否已完成内部会办
	boolean finishInnerJointSign(long flowId, String employeeId);
	
	void delTempForm(List<String> formnumbers);

	boolean checkPersonHasFinishWork(PersonDetail person, long flowId,WorkStage workStage);

	MyWork getBossSignFirstWork(long flowId, WorkStage workStage,String joinSignStartId);

	MyWork getPrevWork(MyWork myWork);

	void saveWorkFinishTime(MyWork newWork);

	/**
	 * add by Cao_Shengyong 2014-03-26
	 * 用于在呈核上一级时，主管核准完成会办时更新相关信息
	 * @param myWork
	 */
	void updateHbWorkJoin(MyWork myWork);
	
	/**
	 * add by Cao_Shengyong 2014-03-27
	 * 用于在呈核上一级主管后，在结束会办分支时查询出该分支下时间最早的一条记录
	 */
	MyWork getFirstHBWork(MyWork myWork);
	/**
	 * 用于更新ORGPATH
	 * @param myWork
	 */
	void updateHbWorkOrgPath(MyWork myWork, MyWork firstWork);
	
	/**
	 * 更新下一步骤节点
	 * @param flow
	 */
	void updateFlowNextStep(Flow flow);
	
	/**
	 * 更新当前步骤审核人
	 */
	void updateFlowLastPerson(Flow flow);
	
	/**
	 * 更新未完成的会办单位列表
	 * @param deptIds
	 */
	void updateNoJointSignDeptIds(String deptIds, Flow flow);
	
	MyWorkHistory[] listWorkHistoryEx(String formNum);
	
	// 该流程下 会办分支 的所有work employeeId 等于 startEmployeeId的那个work列表，状态都是
	// 且workstage状态是会办的状态
	boolean hasJointSignFinishedEx(Flow flow);
	
	/**
	 * 获取抄送表单数
	 */
	int getCopyFlowCount(PersonDetail actualPerson, PersonDetail[] agentees,PersonDetail[] assistedMgrs);
	
	/**
	 * 获取抄送表单列表
	 */
	Page findCopyFlowByPage(PersonDetail actualPerson, PersonDetail[] agentees, PersonDetail[] assistedMgrs,int start, int size);
	
	MyWork getWorkByParentId(MyWork myWork);
	
	void updateWorkOtherInfo(MyWork myWork);
	
	int getSecurityFlowCount();
	
	Page findSecurityFlowByPage(AdvancedSearchDTO onSearchDTO, int start, int size);
	
	boolean hasTopApprove(Flow flow);

	void updateFlowEndTime(Flow flow);

	List<MyWorkDTO> advanceSearchToExport(AdvancedSearchDTO oneSearch);

	Page findLogInfoByPage(String startDate, String logLevel, int start, int size);

	List<LogInfo> findLogInfoByExport(String startDate, String logLevel);

	void updateFlowSubmitBoss(Flow flow);

	Page findCreatorFlowByPageEx(PersonDetail actualPerson,
			PersonDetail[] agentees, PersonDetail[] assistedMgrs, int start,
			int size, String sidx, String sord, AdvancedSearchDTO searchDTO);

	void updateFlowSubmitFBoss(Flow flow);

	MyWorkHistory getHisWorkByID(MyWork work);

	Page confSearch(ConfidentialSearchDTO searchDTO, int start, int size);

	void saveLog(LogDTO logDto);

	List<MyWorkDTO> confSearchToExport(ConfidentialSearchDTO searchDTO);

	List<MyWork> getJointWorks(Flow flow, WorkStage workStage);
	
	Page confSearchboss(ConfidentialSearchbossDTO searchDTO, int start, int size);
	
	List<MyWorkDTO> confSearchbossToExport(ConfidentialSearchbossDTO searchDTO);
}
