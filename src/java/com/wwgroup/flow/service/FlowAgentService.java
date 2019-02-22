package com.wwgroup.flow.service;

import java.util.List;
import java.util.Map;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.AgentPerson;
import com.wwgroup.flow.bo.AgentRelation;
import com.wwgroup.flow.bo.PersonDetail;

/**
 * <p>
 * Title: cuteinfo_[子系统统名]_[模块名]
 * </p>
 * <p>
 * Description: TODO：1.代理与流程整合：方式1：就是在代办工作项查询的时候，将该登陆人，被代理的那些人的代办工作项都获取到，
 * 然后以那些代办人的身份进行接下来的操作。这样其它步骤都跟原来保持一致了。这种方式对内部改动量最小 //这样需要整合的入口主要就在
 * 待处理表单，以及高级查询这两块地方了 隐患: 担心会出现正常业务过程当中需要显示代理人操作痕迹，所以需要与对方进一步确认
 * 2.华炎方流程整合。设计记录以及保存等情况
 * </p>
 * 
 * @author Administrator
 * @version $Revision$ 2012-7-22
 * @author (lastest modification by $Author$)
 * @since 20100620
 */
public interface FlowAgentService {

	void saveAgent(AgentRelation agentRelation);

	Page findAll(String employeeId, int start, int size);

	/**
	 * <p>
	 * Description:获得所代理人的所有被代理人信息
	 * </p>
	 * 
	 * @param agentPerson
	 * @return
	 */
	PersonDetail[] getAgentees(PersonDetail agentPerson);

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @return
	 */
	Map<String, String> extraGetFlowCategories();

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param flowId
	 * @return
	 */
	Map<String, String> extraGetFlowCategories(String flowId);

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param flowId
	 * @return
	 */
	Map<String, String> extraGetFlowEntries(String flowId);

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 */
	void deleteAgent(AgentRelation agentRelation);

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 */
	void updateAgent(AgentRelation agentRelation);

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param id
	 */
	AgentRelation loadAgent(long id);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 * @param agentPerson
	 */
	void addAgentPerson(AgentRelation agentRelation, AgentPerson agentPerson);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param employeeId
	 * @return
	 */
	AgentRelation loadAgent(String employeeId);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 * @return
	 */
	List<AgentPerson> loadAgentPersons(AgentRelation agentRelation);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * @param agentRelation 
	 * 
	 * @param agentPerson
	 */
	void deleteAgentPerson(AgentRelation agentRelation, AgentPerson agentPerson);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 * @param agentPerson
	 */
	void updateAgentPerson(AgentRelation agentRelation, AgentPerson agentPerson);

}
