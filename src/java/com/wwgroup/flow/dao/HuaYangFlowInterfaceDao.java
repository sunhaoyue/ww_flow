package com.wwgroup.flow.dao;

import java.util.List;
import java.util.Map;

import com.wwgroup.flow.bo.AgentPerson;
import com.wwgroup.flow.bo.AgentRelation;

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
public interface HuaYangFlowInterfaceDao {

	Map<String, String> extraGetFlowCategories();

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param flowId
	 * @return
	 */
	Map<String, String> extraGetFlowCategories(String flowId);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param flowId
	 * @return
	 */
	Map<String, String> extraGetFlowEntries(String flowId);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 * @param agentPerson 
	 */
	void saveFlowAgent(AgentRelation agentRelation, AgentPerson agentPerson);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * @param agentRelation 
	 * 
	 * @param agentPerson
	 */
	void deleteFlowAgent(AgentRelation agentRelation, AgentPerson agentPerson);

	/** <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param agentRelation
	 * @param agentPerson 
	 * @param oldSelectedFlowMap
	 */
	void updateFlowAgent(AgentRelation agentRelation, AgentPerson agentPerson, Map<String, List<String>> oldSelectedFlowMap);

}
