package com.wwgroup.flow.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.lob.LobHandler;

import com.wwgroup.common.dao.AbstractJdbcDaoImpl;
import com.wwgroup.flow.bo.AgentPerson;
import com.wwgroup.flow.bo.AgentRelation;
import com.wwgroup.flow.dao.HuaYangFlowInterfaceDao;

/**
 * <p>
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
public class HuaYangFlowInterfaceDaoImpl extends AbstractJdbcDaoImpl implements InitializingBean,
		HuaYangFlowInterfaceDao {
	@SuppressWarnings("unused")
	private LobHandler lobHandler;

	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, String> extraGetFlowCategories() {
		final Map<String, String> result = new HashMap<String, String>(2);
		String sql = "select CategoryID,CategoryName from workflowcategorylist where parentId=0 or parentId=-1";
		getJdbcTemplate().query(sql, new ParameterizedRowMapper() {
			@Override
			public Object mapRow(ResultSet arg0, int arg1) throws SQLException {
				result.put(arg0.getString("CategoryID"), arg0.getString("CategoryName"));
				return 1;
			}
		});
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, String> extraGetFlowCategories(String flowId) {
		final Map<String, String> result = new HashMap<String, String>(2);
		String sql = "select CategoryID,CategoryName from workflowcategorylist where parentId=?";
		getJdbcTemplate().query(sql, new ParameterizedRowMapper() {
			@Override
			public Object mapRow(ResultSet arg0, int arg1) throws SQLException {
				result.put(arg0.getString("CategoryID"), arg0.getString("CategoryName"));
				return 1;
			}
		}, flowId);
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, String> extraGetFlowEntries(String flowId) {
		final Map<String, String> result = new HashMap<String, String>(2);
		String sql =
				"select flowId,flowName from workflow flow where isEnabled=1 and Categoryid=? and flow.EffectEndDate <= TO_DATE('9999-12-31','YYYY-MM-DD') and flow.EffectEndDate >= SYSDATE and flow.EffectStartDate <= SYSDATE and flow.STATUS>0 order by flow.flowName asc";
		getJdbcTemplate().query(sql, new ParameterizedRowMapper() {
			@Override
			public Object mapRow(ResultSet arg0, int arg1) throws SQLException {
				result.put(arg0.getString("flowId"), arg0.getString("flowName"));
				return 1;
			}
		}, flowId);
		return result;
	}

	@SuppressWarnings("unused")
	@Override
	public void saveFlowAgent(AgentRelation agentRelation, AgentPerson agentPerson) {
		
		SimpleDateFormat smf = new SimpleDateFormat("YYYY-mm-dd");
		
		List<Object[]> args = new ArrayList<Object[]>();
		Iterator<List<String>> iter = agentPerson.getSelectedFlow().values().iterator();
		while (iter.hasNext()) {
			List<String> flowIds = iter.next();
			for (String flowId : flowIds) {
				Object[] arg = new Object[] { agentRelation.getActualUserId(), agentPerson.getAgentUserId(), flowId };
				args.add(arg);
			}
		}
		
		StringBuffer sql =
			new StringBuffer("INSERT into FLOWAGENT(CLIENTMAN,AGENTMAN,AGENTSTART,AGENTEND,AGENTEFFECT,FLOWID) "
					+ "VALUES (?,?,SYSDATE, TO_DATE('9999-12-31','YYYY-MM-DD'),1,?)");
		getJdbcTemplate().batchUpdate(sql.toString(), args);
	}

	@Override
	public void deleteFlowAgent(AgentRelation agentRelation, AgentPerson agentPerson) {
		// UPDATE FLOWAGENT SET AGENTEFFECT=0,GENTEND =SYSDATE WHERE FLOWID = {要撤销的流程ID} AND CLIENTMAN={当前用户ID}
		List<Object[]> args = new ArrayList<Object[]>();
		Iterator<List<String>> iter = agentPerson.getSelectedFlow().values().iterator();
		while (iter.hasNext()) {
			List<String> flowIds = iter.next();
			for (String flowId : flowIds) {
				Object[] arg = new Object[] { flowId, agentRelation.getActualUserId() };
				args.add(arg);
			}
		}
		StringBuffer sql =
				new StringBuffer(
						"UPDATE FLOWAGENT SET AGENTEFFECT=0,AGENTEND =SYSDATE WHERE FLOWID = ? AND CLIENTMAN=?");
		getJdbcTemplate().batchUpdate(sql.toString(), args);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateFlowAgent(AgentRelation agentRelation, AgentPerson agentPerson, Map<String, List<String>> oldSelectedFlowMap) {
		List<String> newSelectedFlows = new ArrayList<String>(2);
		List<String> oldSelectedFlows = new ArrayList<String>(2);
		Iterator<List<String>> iter = agentPerson.getSelectedFlow().values().iterator();
		while (iter.hasNext()) {
			List<String> flowIds = iter.next();
			newSelectedFlows.addAll(flowIds);
		}
		iter = oldSelectedFlowMap.values().iterator();
		while (iter.hasNext()) {
			List<String> flowIds = iter.next();
			oldSelectedFlows.addAll(flowIds);
		}
		Map<String, List<String>> newSelectedFlowMap = agentPerson.getSelectedFlow();

		// 先去共有的值 (这是不变得部分)
		List<String> sharedFlowIds = (List<String>) CollectionUtils.intersection(newSelectedFlows, oldSelectedFlows);
		// 共有的 与旧的比较得到 不同的 （这是需要撤销的）
		// UPDATE FLOWAGENT SET AGENTEFFECT=0,GENTEND ={当前时间} WHERE FLOWID = {要撤销的流程ID} AND CLIENTMAN={当前用户ID}
		List<String> deletedFlowIds = (List<String>) CollectionUtils.subtract(oldSelectedFlows, sharedFlowIds);
		// 共有的 与新的比较得到 不同的 （这是需要新增的）
		List<String> newFlowIds = (List<String>) CollectionUtils.subtract(newSelectedFlows, sharedFlowIds);

		Map<String, List<String>> tmpFlowMap = new HashMap<String, List<String>>();
		tmpFlowMap.put("deleted", deletedFlowIds);
		agentPerson.setSelectedFlow(tmpFlowMap);
		this.deleteFlowAgent(agentRelation, agentPerson);

		tmpFlowMap.clear();
		tmpFlowMap.put("add", newFlowIds);
		agentPerson.setSelectedFlow(tmpFlowMap);
		this.saveFlowAgent(agentRelation, agentPerson);

		agentPerson.setSelectedFlow(newSelectedFlowMap);
	}
}
