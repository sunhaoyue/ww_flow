package com.wwgroup.flow.dao;

import java.util.List;
import java.util.Map;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.AgentPerson;
import com.wwgroup.flow.bo.AgentRelation;
import com.wwgroup.flow.bo.PersonDetail;

/**<p>
 * Title: cuteinfo_[子系统统名]_[模块名]
 * </p>
 * <p>
 * Description: [描述该类概要功能介绍]
 * </p>
 * 
 * @author Administrator
 * @version $Revision$ 2012-7-22
 * @author (lastest modification by $Author$)
 * @since 20100620
 */
public interface FlowAgentDao {

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 */
	void saveAgent(AgentRelation agentRelation);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * @param employeeId 
	 * 
	 * @param start
	 * @param size
	 * @return
	 */
	Page findAll(String employeeId, int start, int size);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentPerson
	 * @return
	 */
	PersonDetail[] getAgentees(PersonDetail agentPerson);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 */
	void deleteAgent(AgentRelation agentRelation);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 * @return
	 */
	void updateAgent(AgentRelation agentRelation);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 * @param agentPerson
	 */
	void saveAgentPerson(AgentRelation agentRelation, AgentPerson agentPerson);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param employeeId
	 * @return
	 */
	AgentRelation loadAgentRelation(String employeeId);

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
	 * 
	 * @param agentPerson
	 */
	void deleteAgentPerson(AgentPerson agentPerson);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentPerson
	 * @return
	 */
	Map<String, List<String>> updateAgentPerson(AgentPerson agentPerson);

}
