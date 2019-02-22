package com.wwgroup.flow.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.wwgroup.common.Page;
import com.wwgroup.common.exceptions.MailException;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowAttachment;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.dto.LogDTO;
import com.wwgroup.flow.dto.MyWorkHistory;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.dao.OrganDao;

public interface NLFlowService {

	// 生成唯一的单据Id
	// TODO: 传入部门，人员等组织信息，结合数据库的sequenceId来产生，具体找旺旺再确定下显示规则。
	String generateFormNum(PersonDetail actualPerson, FlowType flowType);

	// 通过formNum的组合字段，获取当中的flowId -->数据库sequenceId
	long getFlowId(String formNum);

	// 提交签成表单
	Map<String, PersonDetail> submitFlow(Flow flow, MyWork rejectWork) throws SQLException,MailException;

	// 分页显示works信息
	Page getWorksWithPage(PersonDetail person, int start, int size);

	// 获取流程概况
	Flow getFlow(MyWork work);

	// 完成工作项,返回接下来马上要接任务的人员清单 Map.key值为employeeID+postCode保持唯一性，一般反馈信息拿personDetail就可以了
	Map<String, PersonDetail> completeWork(MyWork work) throws SQLException;

	// 撤消工作项(在最终核决前，发起人都可以撤销该表单)
	void cancel(MyWork myWork, Flow flow);

	// 驳回
	void reject(MyWork work);

	// 重新起案
	Flow renew(Flow flow, MyWork rejectWork);

	// 指派给某人做事情
	Map<String, PersonDetail> assignWork(PersonDetail employee, MyWork work);

	// 助理指派使用单独接口
	Map<String, PersonDetail> assignInsideWork(PersonDetail employee, MyWork work);
	// 与之上的接口是对应的
	Map<String, PersonDetail> completeInsideWork(MyWork work);
	
	// 是否可以在审核阶段进行指派
	boolean canAssignInStage(Flow flow);

	void saveFlowTemplate(Flow flow, MyWork myWork);

	Flow[] loadFlowTemplates(String userName, FlowType flowType);

	boolean saveFlow(Flow flow);

	Map<String, PersonDetail> startNextWork(Flow flow, PersonDetail actualPerson, MyWork prevWork);

	void updateWorkStatus(MyWork work);

	void updateFlowStatus(Flow flow);

	MyWork loadWork(long assignorId);

	Flow[] loadFlowTemplate(String userName, FlowType flowType);

	void saveWork(MyWork work);

	boolean hasJointSignFinished(Flow flow);
	
	boolean hasCmpcodeJointSignFinished(Flow flow,String cmpcode);

	Flow loadFlow(String formnum);

	// 显示签呈历史信息 就是首层的
	MyWorkHistory[] listWorkHistory(String formNum);
	// 显示第二层历史信息
	MyWorkHistory[] listWorkHistory(String formNum, long parentWorkId);
	
	// 说明经手这个工作的人肯定是主管级别，所以需要更新了，会办与抄送是去替换掉， 附件的就是直接增加即可
	void updateFormContentOnShenghe(Flow flow);

	void saveFlowAttachment(FlowAttachment attachment);
	
	FlowAttachment loadFlowAttachment(long attachmentId);

	void deleteFlowAttachment(long attahmentId);

	Flow endFlow(Flow flow, MyWork myWork);

	void clearFlow(Flow flow);
	
	//是否已完成内部会办
	boolean finishInnerJointSign(long flowId, String employeeId);

	//检查流程中会办里是否有相同的cmpcode会办
	boolean hasCmpcodeSignt(Flow flow, String cmpCode);

	boolean checkPersonHasFinishWork(PersonDetail person, long flowId,WorkStage workStage);

	MyWork getBossSignFirstWork(long flowId, WorkStage workStage,String joinSignStartId);

	Map<String, PersonDetail> adminAssignWork(PersonDetail person, MyWork myWork,String adminAssignOldEmployeeId);

	/**
	 * 获取同workstage的mywork之前的work
	 * @param flowStatus
	 * @param myWork
	 * @return
	 */
	MyWork getPrevWork(MyWork myWork);
	
	OrganDao getOrganDao();
	
	/**
	 * add by Cao_Shengyong 2014-03-26
	 * 用于在呈核上一级时，主管核准完成会办时更新相关信息
	 * @param myWork
	 */
	void updateHbWorkJoin(MyWork myWork); 

	
	/**
	 * add by Cao_Shengyong 2014-03-27
	 * 用于获取同一会办下时间最早的一条记录
	 */
	MyWork getFirstHBWork(MyWork myWork);
	/**
	 * 用于更新ORGPATH
	 * @param myWork
	 */
	void updateHbWorkOrgPath(MyWork myWork, MyWork firstWork);
	
	boolean isTopApprove(Flow flow);
	
	List<SystemGroups> getAllGroupsByParent(int parentID);
	
	SystemGroups getRealCenterGroupsById(int groupId);

	boolean isCompleteAssign(MyWork myWork, String postCode);

	boolean isCompleteAssign(MyWork myWork);

	void saveLog(LogDTO logDto);

	SystemGroups loadGroupsById(int groupId);
}
