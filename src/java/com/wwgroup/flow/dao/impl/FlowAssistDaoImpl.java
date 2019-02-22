package com.wwgroup.flow.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.incrementer.OracleSequenceMaxValueIncrementer;
import org.springframework.jdbc.support.lob.LobHandler;

import com.wwgroup.common.Page;
import com.wwgroup.common.dao.AbstractJdbcDaoImpl;
import com.wwgroup.flow.bo.AssistRelation;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.dao.FlowAssistDao;

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
public class FlowAssistDaoImpl extends AbstractJdbcDaoImpl implements InitializingBean, FlowAssistDao {

	@SuppressWarnings("unused")
	private LobHandler lobHandler;

	private OracleSequenceMaxValueIncrementer flowassit_incr;

	private SimpleJdbcInsert flowassist_jdbcInsert;

	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	@Override
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		JdbcTemplate template = super.createJdbcTemplate(dataSource);
		this.flowassit_incr = new OracleSequenceMaxValueIncrementer(dataSource, "SEQ_WW_FLOWASSIST");
		this.flowassist_jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("WW_FLOWASSIST");
		return template;
	}

	protected class MyFlowAssistRowMapper implements ParameterizedRowMapper<AssistRelation> {
		@SuppressWarnings("finally")
		public AssistRelation mapRow(ResultSet rs, int rowNum) {
			AssistRelation flow = new AssistRelation();
			try {
				flow.setId(rs.getInt("ID"));
				flow.setEmployeeId(rs.getString("EMPLOYEEID"));
				flow.setSelectedDeptId(rs.getInt("SELECTEDDEPTID"));
				flow.setSelectedDeptName(rs.getString("SELECTEDDEPTNAME"));
				flow.setSelectedPostCode(rs.getString("SELECTEDPOSTCODE"));
				flow.setSelectedPostName(rs.getString("SELECTEDPOSTNAME"));
				flow.setAllowReceiveMail(rs.getInt("ALLOWRECEIVEMAIL") > 0 ? true : false);
				flow.setAllowAssignPerson(rs.getInt("ALLOWASSIGNPERSON") > 0 ? true : false);
				flow.setSelectedAssistEmployeeId(rs.getString("SELECTEDASSISTEMPLOYEEID"));
				flow.setSelectedAssistEmployeeName(rs.getString("SELECTEDASSISTEMPLOYEENAME"));
				flow.setSelectedAssistPostCode(rs.getString("SELECTEDASSISTPOSTCODE"));
				flow.setSelectedAssistPostName(rs.getString("SELECTEDASSISTPOSTNAME"));
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return flow;
			}
		}
	}

	@Override
	public Page findAll(String employeeId, int start, int size) {
		String sql = "select * from WW_FLOWASSIST f where f.EMPLOYEEID = ?";
		ParameterizedRowMapper<AssistRelation> mapper = new MyFlowAssistRowMapper();
		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, sql, mapper, employeeId);
		return page;
	}
	
	@Override
	public List<AssistRelation> findAll(String employeeId) {
		String sql = "select * from WW_FLOWASSIST f where f.EMPLOYEEID = ?";
		ParameterizedRowMapper<AssistRelation> mapper = new MyFlowAssistRowMapper();
		return getJdbcTemplate().query(sql, mapper, employeeId);
	}

	@Override
	public void removeAssist(final long id) {
		String updateSQL = "delete from ww_flowassist ast where ast.id = :id";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, id);
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public void saveAssist(AssistRelation assistRelation) {
		assistRelation.setId(flowassit_incr.nextIntValue());
		Map<String, Object> parameters = new HashMap<String, Object>(16);
		parameters.put("ID", assistRelation.getId());
		parameters.put("EMPLOYEEID", assistRelation.getEmployeeId());
		parameters.put("SELECTEDDEPTID", assistRelation.getSelectedDeptId());
		parameters.put("SELECTEDDEPTNAME", assistRelation.getSelectedDeptName());
		parameters.put("SELECTEDPOSTCODE", assistRelation.getSelectedPostCode());
		parameters.put("SELECTEDPOSTNAME", assistRelation.getSelectedPostName());
		parameters.put("ALLOWRECEIVEMAIL", assistRelation.isAllowReceiveMail());
		parameters.put("ALLOWASSIGNPERSON", assistRelation.isAllowAssignPerson());
		parameters.put("SELECTEDASSISTEMPLOYEEID", assistRelation.getSelectedAssistEmployeeId());
		parameters.put("SELECTEDASSISTEMPLOYEENAME", assistRelation.getSelectedAssistEmployeeName());
		parameters.put("SELECTEDASSISTPOSTCODE", assistRelation.getSelectedAssistPostCode());
		parameters.put("SELECTEDASSISTPOSTNAME", assistRelation.getSelectedAssistPostName());
		flowassist_jdbcInsert.execute(parameters);
	}

	@Override
	public void updateAssist(final AssistRelation assistRelation) {
		String updateSQL = "update WW_FLOWASSIST set EMPLOYEEID=:EMPLOYEEID,SELECTEDPOSTCODE=:SELECTEDPOSTCODE,SELECTEDPOSTNAME=:SELECTEDPOSTNAME, "
				+ "ALLOWRECEIVEMAIL=:ALLOWRECEIVEMAIL,ALLOWASSIGNPERSON=:ALLOWASSIGNPERSON,SELECTEDASSISTEMPLOYEEID=:SELECTEDASSISTEMPLOYEEID, "
				+ "SELECTEDASSISTEMPLOYEENAME=:SELECTEDASSISTEMPLOYEENAME,SELECTEDASSISTPOSTCODE=:SELECTEDASSISTPOSTCODE,SELECTEDASSISTPOSTNAME=:SELECTEDASSISTPOSTNAME, "
				+ "SELECTEDDEPTID=:SELECTEDDEPTID,SELECTEDDEPTNAME=:SELECTEDDEPTNAME" + " where ID=:ID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, assistRelation.getEmployeeId());
				state.setObject(2, assistRelation.getSelectedPostCode());
				state.setObject(3, assistRelation.getSelectedPostName());
				state.setObject(4, assistRelation.isAllowReceiveMail());
				state.setObject(5, assistRelation.isAllowAssignPerson());
				state.setObject(6, assistRelation.getSelectedAssistEmployeeId());
				state.setObject(7, assistRelation.getSelectedAssistEmployeeName());
				state.setObject(8, assistRelation.getSelectedAssistPostCode());
				state.setObject(9, assistRelation.getSelectedAssistPostName());
				state.setObject(10, assistRelation.getSelectedDeptId());
				state.setObject(11, assistRelation.getSelectedDeptName());
				state.setObject(12, assistRelation.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	protected class PersonDetailRowMapper implements ParameterizedRowMapper<PersonDetail> {
		@SuppressWarnings("finally")
		public PersonDetail mapRow(ResultSet rs, int rowNum) {
			PersonDetail flow = new PersonDetail();
			try {
				flow.setEmployeeId(rs.getString("employeeid"));
				flow.setPostCode(rs.getString("selectedpostcode"));
				flow.setAllowAssignAction(rs.getInt("allowassignperson") > 0);
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return flow;
			}
		}
	}

	@Override
	public PersonDetail[] getAssistedMgrs(PersonDetail loginPerson) {
		String sql = "select * from ww_flowassist ast where ast.selectedassistemployeeid  =:id and ast.allowreceivemail > 0";//  and ast.selectedassistpostcode =:code
		ParameterizedRowMapper<PersonDetail> mapper = new PersonDetailRowMapper();
		// 构造分页信息
		List<PersonDetail> result = getJdbcTemplate().query(sql, mapper, loginPerson.getEmployeeId());//, loginPerson.getPostCode()
		return result.toArray(new PersonDetail[result.size()]);
	}

	@Override
	public AssistRelation loadAssist(long id) {
		String sql = "select * from ww_flowassist ast where ast.id  =:id";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		ParameterizedRowMapper<AssistRelation> mapper = new MyFlowAssistRowMapper();
		return getJdbcTemplate().queryForObject(sql, mapper, map);
	}

}
