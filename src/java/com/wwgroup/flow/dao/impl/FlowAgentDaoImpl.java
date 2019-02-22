package com.wwgroup.flow.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.incrementer.OracleSequenceMaxValueIncrementer;
import org.springframework.jdbc.support.lob.LobHandler;

import com.wwgroup.common.Page;
import com.wwgroup.common.dao.AbstractJdbcDaoImpl;
import com.wwgroup.flow.bo.AgentPerson;
import com.wwgroup.flow.bo.AgentReason;
import com.wwgroup.flow.bo.AgentRelation;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.dao.FlowAgentDao;

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
public class FlowAgentDaoImpl extends AbstractJdbcDaoImpl implements InitializingBean, FlowAgentDao {

	@SuppressWarnings("unused")
	private LobHandler lobHandler;

	private OracleSequenceMaxValueIncrementer flowagent_incr;

	private SimpleJdbcInsert flowagent_jdbcInsert;

	private OracleSequenceMaxValueIncrementer flowagentperson_incr;

	private SimpleJdbcInsert flowagentperson_jdbcInsert;

	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	@Override
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		JdbcTemplate template = super.createJdbcTemplate(dataSource);
		this.flowagent_incr = new OracleSequenceMaxValueIncrementer(dataSource, "SEQ_WW_FLOWAGENT");
		this.flowagent_jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("WW_FLOWAGENT");

		this.flowagentperson_incr = new OracleSequenceMaxValueIncrementer(dataSource, "SEQ_WW_FLOWAGENTPERSON");
		this.flowagentperson_jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("WW_FLOWAGENTPERSON");
		return template;
	}

	protected class PersonDetailRowMapper implements ParameterizedRowMapper<PersonDetail> {
		@SuppressWarnings("finally")
		public PersonDetail mapRow(ResultSet rs, int rowNum) {
			PersonDetail flow = new PersonDetail();
			try {
				flow.setEmployeeId(rs.getString("id"));
				flow.setPostCode(rs.getString("postcode"));
			}
			catch (SQLException ex) {
				ex.printStackTrace();
			}
			finally {
				return flow;
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	private Map<String, List<String>> translateIntoMap(String selectedFlow) {
		JSONObject jsonObject = JSONObject.fromObject(selectedFlow);
		Iterator iter = jsonObject.keys();
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			JSONArray value = (JSONArray) jsonObject.get(key);
			List values = JSONArray.toList(value);
			result.put(key, values);
		}
		// {"flowType1":["flow1","flow2"],"flowType2":["flow1","flow2"]}
		return result;
	}

	private JSONObject translateIntoJSON(Map<String, List<String>> selectedFlow) {
		return JSONObject.fromObject(selectedFlow);
	}

	@Override
	public Page findAll(String employeeId, int start, int size) {
		String sql = "select * from WW_FLOWAGENT f where f.ACTUALEMPLOYEEID = ?";
		ParameterizedRowMapper<AgentRelation> mapper = new MyFlowAgentRowMapper();
		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, sql, mapper, employeeId);
		return page;
	}

	@Override
	public PersonDetail[] getAgentees(PersonDetail agentPerson) {
		// TODO：需要添加额外的有效性验证来筛选代理人信息
		// 1首先根据agentEmployeeId type=posioinAgent获得agentPerson对象
		// 2根据relationId获得
		String sql =
				"select agt.actualemployeeid as id, person.agentpostcode as postcode from ww_flowagent agt "
						+ "left join ww_flowagentperson person on agt.id = person.agentrelationid "
						+ "where person.agenttype = 0 and person.agentemployeeid = ? and (agt.STARTDATE <= ? and agt.ENDDATE >= ?)";

		java.sql.Timestamp currentDate = new java.sql.Timestamp(System.currentTimeMillis());
		//System.out.println(sql);
		//System.out.println(agentPerson.getEmployeeId() + "#" + currentDate);
		// String sql =
		// "select f.ACTUALEMPLOYEEID as id, f.ACTUALPOSTCODE as postcode from WW_FLOWAGENT f where f.AGENTEMPLOYEEID = ?  and f.EFFECTONSYSTEM > 0 and (f.STARTDATE <= ? and f.ENDDATE >= ?)";
		ParameterizedRowMapper<PersonDetail> mapper = new PersonDetailRowMapper();
		// 构造分页信息
		List<PersonDetail> result =
				getJdbcTemplate().query(sql, mapper, agentPerson.getEmployeeId(), currentDate, currentDate);// ,

		Map<String, PersonDetail> resultMap = new HashMap<String, PersonDetail>(2);
		for (PersonDetail person : result) {
			resultMap.put(person.getEmployeeId() + person.getPostCode(), person);
		}
		return resultMap.values().toArray(new PersonDetail[resultMap.size()]);
	}

	@Override
	public void updateAgent(final AgentRelation agentRelation) {
		String updateSQL =
				"update WW_FLOWAGENT set STARTDATE=:STARTDATE,ENDDATE=:ENDDATE,AGENTREASON=:AGENTREASON,AGENTREASON_REALNAME=:AGENTREASON_REALNAME where ID=:ID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, new Timestamp(agentRelation.getStartDate().getTime()));
				state.setObject(2, new Timestamp(agentRelation.getEndDate().getTime()));
				state.setObject(3, agentRelation.getReason().ordinal());
				state.setObject(4, agentRelation.getReason().getRealName());
				state.setObject(5, agentRelation.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public void saveAgent(AgentRelation agentRelation) {
		agentRelation.setId(flowagent_incr.nextIntValue());
		Map<String, Object> parameters = new HashMap<String, Object>(16);
		parameters.put("ID", agentRelation.getId());
		parameters.put("ACTUALEMPLOYEEID", agentRelation.getActualEmployeeId());
		parameters.put("ActualUserID", agentRelation.getActualUserId());
		parameters.put("STARTDATE", new Timestamp(agentRelation.getStartDate().getTime()));
		parameters.put("ENDDATE", new Timestamp(agentRelation.getEndDate().getTime()));
		parameters.put("AGENTREASON", agentRelation.getReason().ordinal());
		if (!org.apache.commons.lang3.StringUtils.isEmpty(agentRelation.getReason().getRealName())) {
			parameters.put("AGENTREASON_REALNAME", agentRelation.getReason().getRealName());
		}
		flowagent_jdbcInsert.execute(parameters);
	}

	@Override
	public void saveAgentPerson(AgentRelation agentRelation, AgentPerson agentPerson) {
		agentPerson.setId(flowagentperson_incr.nextIntValue());
		Map<String, Object> parameters = new HashMap<String, Object>(16);
		parameters.put("ID", agentPerson.getId());
		parameters.put("AGENTRELATIONID", agentRelation.getId());

		parameters.put("agentUserId", agentPerson.getAgentUserId());

		parameters.put("AGENTEMPLOYEEID", agentPerson.getAgentEmployeeId());
		parameters.put("AGENTDEPTID", agentPerson.getAgentDeptId());
		// 将selectFlow组织成json格式的数据放入数据库，读取出来的时候再反向解析完成
		if (agentPerson.getSelectedFlow() != null) {
			parameters.put("SELECTFLOW", this.translateIntoJSON(agentPerson.getSelectedFlow()).toString());
		}
		parameters.put("AGENTTYPE", agentPerson.getType().ordinal());
		if (!org.apache.commons.lang3.StringUtils.isEmpty(agentPerson.getType().getSelectedPostCode())) {
			parameters.put("AGENTPOSTCODE", agentPerson.getType().getSelectedPostCode());
		}
		parameters.put("AGENTPOSTNAME", agentPerson.getAgentPostName());
		parameters.put("SELECTFLOWNAME", agentPerson.getFlowNames());
		flowagentperson_jdbcInsert.execute(parameters);
	}

	@Override
	public AgentRelation loadAgentRelation(String employeeId) {
		String sql = "select * from WW_FLOWAGENT fa where fa.ACTUALEMPLOYEEID=:id";
		ParameterizedRowMapper<AgentRelation> mapper = new MyFlowAgentRowMapper();
		List<AgentRelation> result = getJdbcTemplate().query(sql, mapper, employeeId);
		return result != null && result.size() > 0 ? result.get(0) : null;
	}

	@Override
	public List<AgentPerson> loadAgentPersons(AgentRelation agentRelation) {
		String sql = "select * from WW_FLOWAGENTPERSON fa where fa.AGENTRELATIONID=:id";
		ParameterizedRowMapper<AgentPerson> mapper = new MyFlowAgentPersonRowMapper();
		return getJdbcTemplate().query(sql, mapper, agentRelation.getId());
	}

	protected class MyFlowAgentPersonRowMapper implements ParameterizedRowMapper<AgentPerson> {
		@SuppressWarnings("finally")
		public AgentPerson mapRow(ResultSet rs, int rowNum) {
			AgentPerson flow = new AgentPerson();
			try {
				flow.setId(rs.getInt("ID"));
				flow.setAgentRelationId(rs.getInt("AGENTRELATIONID"));
				flow.setAgentUserId(rs.getString("agentUserId"));
				flow.setAgentEmployeeId(rs.getString("AGENTEMPLOYEEID"));
				if (org.apache.commons.lang3.StringUtils.isNotEmpty(rs.getString("SELECTFLOW"))
						&& !rs.getString("SELECTFLOW").equals("null")) {
					flow.setSelectedFlow(translateIntoMap(rs.getString("SELECTFLOW")));
				}
				flow.setType(com.wwgroup.flow.bo.AgentType.values()[rs.getInt("AGENTTYPE")]);
				String agentPostCode = rs.getString("AGENTPOSTCODE");
				if (!org.apache.commons.lang3.StringUtils.isEmpty(agentPostCode)) {
					flow.setType(com.wwgroup.flow.bo.AgentType.PositionAgent.selectedPostCode(agentPostCode));
				}
				flow.setAgentPostCode(agentPostCode);
				flow.setAgentDeptId(rs.getString("AGENTDEPTID"));
				flow.setAgentPostName(rs.getString("AGENTPOSTNAME"));
				flow.setFlowNames(rs.getString("SELECTFLOWNAME"));
			}
			catch (SQLException ex) {
				ex.printStackTrace();
			}
			finally {
				return flow;
			}
		}
	}

	protected class MyFlowAgentRowMapper implements ParameterizedRowMapper<AgentRelation> {
		@SuppressWarnings("finally")
		public AgentRelation mapRow(ResultSet rs, int rowNum) {
			AgentRelation flow = new AgentRelation();
			try {
				flow.setId(rs.getInt("ID"));
				flow.setActualEmployeeId(rs.getString("ACTUALEMPLOYEEID"));
				flow.setActualUserId(rs.getString("ActualUserID"));
				flow.setStartDate(new java.util.Date(rs.getTimestamp("STARTDATE").getTime()));
				flow.setEndDate(new java.util.Date(rs.getTimestamp("ENDDATE").getTime()));
				flow.setReason(AgentReason.values()[rs.getInt("AGENTREASON")]);
				String agent_realName = rs.getString("AGENTREASON_REALNAME");
				if (!org.apache.commons.lang3.StringUtils.isEmpty(agent_realName)) {
					flow.setReason(AgentReason.Other.realName(agent_realName));
				}
			}
			catch (SQLException ex) {
				ex.printStackTrace();
			}
			finally {
				return flow;
			}
		}
	}

	@Override
	public void deleteAgent(final AgentRelation agentRelation) {
		String updateSQL = "delete from ww_flowagent agt where agt.id = :id";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, agentRelation.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public void deleteAgentPerson(final AgentPerson agentPerson) {
		String updateSQL = "delete from ww_flowagentperson agt where agt.id = :id";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, agentPerson.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public Map<String, List<String>> updateAgentPerson(final AgentPerson agentPerson) {
		String sql = "select * from WW_FLOWAGENTPERSON f where f.ID = ?";
		ParameterizedRowMapper<AgentPerson> mapper = new MyFlowAgentPersonRowMapper();
		List<AgentPerson> result = getJdbcTemplate().query(sql, mapper, agentPerson.getId());
		if (result.size() == 1) {
			AgentPerson oldAgent = result.get(0);
			String updateSQL =
					"update WW_FLOWAGENTPERSON set agentUserId=:agentUserId ,AGENTEMPLOYEEID=:AGENTEMPLOYEEID,AGENTPOSTCODE=:AGENTPOSTCODE, "
							+ "SELECTFLOW=:SELECTFLOW,AGENTTYPE=:AGENTTYPE,AGENTDEPTID=:AGENTDEPTID where ID=:ID";
			PreparedStatementSetter param = new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement state) throws SQLException {
					state.setObject(1, agentPerson.getAgentUserId());
					state.setObject(2, agentPerson.getAgentEmployeeId());
					state.setObject(3, agentPerson.getAgentPostCode());
					state.setObject(4, translateIntoJSON(agentPerson.getSelectedFlow()).toString());
					state.setObject(5, agentPerson.getType().ordinal());
					state.setObject(6, agentPerson.getAgentDeptId());
					state.setObject(7, agentPerson.getId());
				}
			};
			getJdbcTemplate().update(updateSQL, param);
			return oldAgent.getSelectedFlow();
		}
		return null;
	}

}
