package com.wwgroup.user.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.support.incrementer.OracleSequenceMaxValueIncrementer;
import org.springframework.util.CollectionUtils;

import com.wwgroup.common.Page;
import com.wwgroup.common.dao.AbstractJdbcDaoImpl;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRGroupMgr;
import com.wwgroup.user.bo.EmployeePos;
import com.wwgroup.user.bo.HbMgrUser;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.dao.UserDao;

public class UserDaoImpl extends AbstractJdbcDaoImpl implements UserDao {

	private OracleSequenceMaxValueIncrementer persondetail_incr;

	protected class UserRowMapper implements ParameterizedRowMapper<SystemUsers> {
		@SuppressWarnings("finally")
		public SystemUsers mapRow(ResultSet rs, int rowNum) {
			SystemUsers users = new SystemUsers();
			try {
				// chenweijie: 这边与华炎的代理数据库对接需要用到这个字段，所以放出来了，需要确定下对前期的影响
				users.setUserID(rs.getInt("USERID"));
				users.setUserName(rs.getString("USERNAME"));
				users.setCmpcod(rs.getString("CMPCOD"));
				users.setDepcod(rs.getString("DEPCOD"));
				users.setFullName(rs.getString("FULLNAME"));
				users.setCell(rs.getString("CELL"));
				users.setEmail(rs.getString("EMAIL"));
				users.setWorkPhone(rs.getString("WORKPHONE"));
				users.setTitnam(rs.getString("titnam"));

			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return users;
			}
		}
	}

	protected class EmployeePosRowMapper implements ParameterizedRowMapper<EmployeePos> {
		@SuppressWarnings("finally")
		public EmployeePos mapRow(ResultSet rs, int rowNum) {
			EmployeePos employeePos = new EmployeePos();
			try {
				employeePos.setCmpcod(rs.getString("CMPCOD"));
				employeePos.setEmployeeid(rs.getString("EMPLOYEEID"));
				employeePos.setDeptcode(rs.getString("POSDEPCOD"));
				employeePos.setPostcode(rs.getString("POSITCOD1"));
				employeePos.setA_deptcode(rs.getString("POSA_DEPCOD"));
				employeePos.setMgremployeeid(rs.getString("MGEMPLOYEEID1"));
				employeePos.setMgrpostcode(rs.getString("MGPOSITCOD1"));
				employeePos.setMgDeptFlg(rs.getInt("MGDEPFLG1"));
				employeePos.setMgA_DeptFlg(rs.getInt("MGA_DEPFLG1"));
				employeePos.setMgA_DeptFFlg(rs.getInt("MGA_DEPFFLG1"));
				employeePos.setMgCentFlg(rs.getInt("MGCENTFLG1"));
				employeePos.setMgCentFFlg(rs.getInt("MGCENTFFLG1"));
				if (rs.getInt("MGDEPFLG1") == 1) {
					employeePos.setDeptmgr(true);
				}
				if (rs.getInt("MGA_DEPFLG1") == 1) {
					employeePos.setCentermgr(true);
				}
				if (rs.getInt("MGCENTFLG1") == 1) {
					employeePos.setTopmgr(true);
				}
				if (employeePos.getMgCentFFlg() == 1){
					employeePos.setTopFmgr(true);
				}
				if (employeePos.getMgA_DeptFFlg() == 1){
					employeePos.setCenterFmgr(true);
				}

			} catch (SQLException ex) {
				//System.out.println("####" + ex.getMessage());
				ex.printStackTrace();
			} finally {
				return employeePos;
			}
		}
	}

	protected class GroupMgrRowMapper implements ParameterizedRowMapper<HRGroupMgr> {
		@SuppressWarnings("finally")
		public HRGroupMgr mapRow(ResultSet rs, int rowNum) {
			HRGroupMgr groupMgr = new HRGroupMgr();
			try {
				groupMgr.setCmpcod(rs.getString("CMPCOD"));
				groupMgr.setDepcod(rs.getString("DEPCOD"));
				groupMgr.setEmployeeid(rs.getString("EMPLOYEEID"));
				groupMgr.setEmployeenam(rs.getString("EMPLOYEENAM"));
				groupMgr.setType(rs.getString("TYPE"));

			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return groupMgr;
			}
		}
	}

	protected class PersonDetailRowMapper implements ParameterizedRowMapper<PersonDetail> {
		@SuppressWarnings("finally")
		public PersonDetail mapRow(ResultSet rs, int rowNum) {
			PersonDetail personDetail = new PersonDetail();
			try {
				// 用户信息
				personDetail.setId(persondetail_incr.nextIntValue());
				personDetail.setName(rs.getString("name"));
				personDetail.setEmployeeId(rs.getString("employeeid"));
				personDetail.setEmail(rs.getString("email"));
				personDetail.setCompPhone(rs.getString("workphone"));
				personDetail.setUserID(rs.getInt("userid"));

				// 岗位信息
				personDetail.setCmpCode(rs.getString("cmpcod"));
				personDetail.setDeptCode(rs.getString("depcod"));
				personDetail.setPostName(rs.getString("postnam"));
				personDetail.setPostId(rs.getString("postid"));
				personDetail.setPostCode(rs.getString("postcod"));

			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return personDetail;
			}
		}
	}
	
	protected class HbMgrRowMapper implements ParameterizedRowMapper<HbMgrUser>{
		public HbMgrUser mapRow(ResultSet rs, int rowNum){
			HbMgrUser hbMgrUser = new HbMgrUser();
			try {
				hbMgrUser.setOrgPath(rs.getString("orgpath"));
				hbMgrUser.setGroupId(rs.getString("groupid"));
				hbMgrUser.setUserId(rs.getString("userid"));
				hbMgrUser.setUserName(rs.getString("username"));
				hbMgrUser.setEmployeeId(rs.getString("employeeid"));
				hbMgrUser.setPosName(rs.getString("postname"));
				hbMgrUser.setPosCode(rs.getString("postcode"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return hbMgrUser;
		}
	}

	@Override
	public List<PersonDetail> getUsersByGroup(int groupID) {
		String sql = "select * from SystemUsers su join SystemGroupMembership sgms on su.userid = sgms.userid where sgms.groupid = :groupid";
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("groupid", groupID);

		ParameterizedRowMapper<PersonDetail> mapper = new PersonDetailRowMapper();
		return getJdbcTemplate().query(sql, mapper, map);
	}

	@Override
	public EmployeePos getEmployeePosByEmpId(String employeeid) {
		String sql = "select * from HR_EMPPOS he where he.EMPLOYEEID = :employeeid and he.mgposflg <> 2";
		Map<String, String> map = new HashMap<String, String>();
		map.put("employeeid", employeeid);
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		List<EmployeePos> result = getJdbcTemplate().query(sql, mapper, map);
		if (result != null && result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public List<EmployeePos> getEmployeePosListPTJob(String employeeid, String cmpcod){
		String sql = "select * from hr_emppos he where he.employeeid=:employeeid and he.cmpcod!=:cmpcod";
		Map<String, String> map = new HashMap<String, String>();
		map.put("employeeid", employeeid);
		map.put("cmpcod", cmpcod);
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		List<EmployeePos> result = getJdbcTemplate().query(sql, mapper, map);
		if (result != null && result.size() > 0){
			return result;
		} else {
			return null;
		}
	}

	@Override
	public SystemUsers getUsersByName(String name) {
		List<SystemUsers> list = new ArrayList<SystemUsers>();
		String sql = "select su.*, ee.titnam from SystemUsers su left join hr_employee ee on su.username = ee.employeeid where su.username = :name";
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", name);
		ParameterizedRowMapper<SystemUsers> mapper = new UserRowMapper();
		list = getJdbcTemplate().query(sql, mapper, map);
		return list.get(0);
	}

	@Override
	public EmployeePos getEmployeePosByEmpId(String deptCode, String comCode, String employeeid, String postCode) {
		//String sql = "select * from HR_EMPPOS he where he.POSDEPCOD =:deptCode and he.CMPCOD = :comCode and he.EMPLOYEEID = :employeeid and he.POSITCOD1 =:postCode";
		//String sql = "select * from HR_EMPPOS he where he.CMPCOD = :comCode and he.EMPLOYEEID = :employeeid and he.POSITCOD1 =:postCode";
		String sql = "select * from HR_EMPPOS he where he.EMPLOYEEID = :employeeid and he.POSITCOD1 =:postCode";
		Map<String, String> map = new HashMap<String, String>();
		//map.put("deptCode", deptCode);
		map.put("comCode", comCode);
		map.put("employeeid", employeeid);
		map.put("postCode", postCode);
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		
		List<EmployeePos> list = getJdbcTemplate().query(sql, mapper, map);
		if(!CollectionUtils.isEmpty(list)){
			return list.get(0);
		}
		
		return null;
	}

	@Override
	public HRGroupMgr getGroupMgrByDept(String deptCode, String a_deptCode, String comCode, String employeeid,
			DescionMaker descionMaker) {
		StringBuffer sqlBuff = new StringBuffer();
		Map<String, String> map = new HashMap<String, String>();

		sqlBuff.append("select * from hr_groupmgr he where 1=1");
		if (descionMaker.equals(DescionMaker.DEPTLEADER)) {
			sqlBuff
					.append(" and he.DEPCOD = :deptCode and he.CMPCOD = :comCode and he.EMPLOYEEID = :employeeid and he.type = -1");

			map.put("deptCode", deptCode);
			map.put("comCode", comCode);
			map.put("employeeid", employeeid);

		}
		if (descionMaker.equals(DescionMaker.CENTRALLEADER) || descionMaker.equals(DescionMaker.REGINLEADER)) {
			sqlBuff
					.append(" and he.DEPCOD = :deptCode and he.CMPCOD = :comCode and he.EMPLOYEEID = :employeeid and he.type = -2");
			map.put("deptCode", deptCode);
			map.put("comCode", comCode);
			map.put("employeeid", employeeid);
		}
		if (descionMaker.equals(DescionMaker.UNITLEADER) || descionMaker.equals(DescionMaker.HEADLEADER)) {
			sqlBuff
					.append(" and he.DEPCOD = :deptCode and he.CMPCOD = :comCode and he.EMPLOYEEID = :employeeid and he.type = -3");
			map.put("deptCode", deptCode);
			map.put("comCode", comCode);
			map.put("employeeid", employeeid);
		}
		ParameterizedRowMapper<HRGroupMgr> mapper = new GroupMgrRowMapper();
		try {
			return getJdbcTemplate().queryForObject(sqlBuff.toString(), mapper, map);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public HRGroupMgr getGroupMgrByDeptPlus(String deptCode, String a_deptCode, String comCode, String employeeid,
			DescionMaker descionMaker) {
		StringBuffer sqlBuff = new StringBuffer();
		Map<String, String> map = new HashMap<String, String>();

		sqlBuff.append("select * from hr_groupmgr he where 1=1");
		if (descionMaker.equals(DescionMaker.DEPTLEADER)) {
			sqlBuff
					.append(" and he.DEPCOD = :deptCode and he.CMPCOD = :comCode and he.EMPLOYEEID = :employeeid and he.type = -11");

			map.put("deptCode", deptCode);
			map.put("comCode", comCode);
			map.put("employeeid", employeeid);

		}
		if (descionMaker.equals(DescionMaker.CENTRALLEADER) || descionMaker.equals(DescionMaker.REGINLEADER)) {
			sqlBuff
					.append(" and he.DEPCOD = :a_deptCode and he.CMPCOD = :comCode and he.EMPLOYEEID = :employeeid and he.type = -21");
			map.put("a_deptCode", a_deptCode);
			map.put("comCode", comCode);
			map.put("employeeid", employeeid);
		}
		if (descionMaker.equals(DescionMaker.UNITLEADER) || descionMaker.equals(DescionMaker.HEADLEADER)) {
			sqlBuff
					.append(" and he.DEPCOD = :deptCode and he.CMPCOD = :comCode and he.EMPLOYEEID = :employeeid and he.type = -31");
			map.put("deptCode", deptCode);
			map.put("comCode", comCode);
			map.put("employeeid", employeeid);
		}
		ParameterizedRowMapper<HRGroupMgr> mapper = new GroupMgrRowMapper();
		try {
			return getJdbcTemplate().queryForObject(sqlBuff.toString(), mapper, map);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public Page getUsersByGroupWithPage(int groupID, int start, int size) {
		StringBuffer sb = new StringBuffer();
		sb.append("select su.userid as userid, su.fullName as name, su.workphone as workphone, su.email as email, ep.employeeid as employeeid, su.cmpcod as cmpcod, su.depcod as depcod,pos.positcod as postcod, pos.positionnam postnam, pos.positionid as postid ");
		sb.append(" from hr_emppos ep, SystemUsers su, hr_position pos, systemgroups sgs,hr_employee epe ");
		sb.append(" where ep.employeeid = su.username ");
		sb.append(" and ep.cmpcod = pos.cmpcod ");
		sb.append(" and ep.posdepcod = pos.depcod ");
		sb.append(" and ep.positcod1 = pos.positcod ");
		sb.append(" and sgs.depcod = pos.depcod ");
		sb.append(" and sgs.cmpcod = pos.cmpcod ");
		sb.append(" and epe.employeeid=ep.employeeid ");
		sb.append(" and ep.posdepcod in ");
		sb.append(" (select depcod from systemgroups where groupid = ?) ");
		sb.append(" order by ep.mgcmpflg desc,ep.mgcentflg1 desc,ep.mga_depflg1 desc,to_number(epe.dencod),ep.employeeid");

		ParameterizedRowMapper<PersonDetail> mapper = new PersonDetailRowMapper();

		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);
		// Page page = new Page(start, 0, 0, size, null);
		page = queryWithPage(page, sb.toString(), mapper, groupID);
		return page;
	}

	@Override
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		JdbcTemplate template = super.createJdbcTemplate(dataSource);

		this.persondetail_incr = new OracleSequenceMaxValueIncrementer(dataSource, "SEQ_WW_PERSONDETAIL");
		return template;
	}

	@Override
	public List<HRGroupMgr> getGroupMgrByDept(String deptCode, String a_deptCode, String comCode,
			DescionMaker descionMaker) {
		StringBuffer sqlBuff = new StringBuffer();
		Map<String, String> map = new HashMap<String, String>();

		sqlBuff.append("select * from hr_groupmgr he where 1=1");
		if (descionMaker.equals(DescionMaker.DEPTLEADER)) {
			sqlBuff.append(" and he.DEPCOD = :deptCode and he.CMPCOD = :comCode and he.type = -1");
			map.put("deptCode", deptCode);
			map.put("comCode", comCode);

		}
		if (descionMaker.equals(DescionMaker.CENTRALLEADER)) {
			sqlBuff.append(" and he.DEPCOD = :a_deptCode and he.CMPCOD = :comCode and he.type = -2");
			map.put("a_deptCode", a_deptCode);
			map.put("comCode", comCode);
		}
		if (descionMaker.equals(DescionMaker.UNITLEADER)) {
			//sqlBuff.append(" and he.DEPCOD = :deptCode and he.CMPCOD = :comCode and he.type = -3");
			//hr_groupmgr中不存在CMPCOD为空的数据
			sqlBuff.append(" and he.CMPCOD = :comCode and he.type = -3");
			//map.put("deptCode", deptCode);
			map.put("comCode", comCode);
		}
		//System.out.println(sqlBuff.toString());
		//System.out.println(map.toString());
		ParameterizedRowMapper<HRGroupMgr> mapper = new GroupMgrRowMapper();
		List<HRGroupMgr> list = getJdbcTemplate().query(sqlBuff.toString(), mapper, map);
		if(CollectionUtils.isEmpty(list)){
			map.clear();
			sqlBuff.delete(0, sqlBuff.length());
			sqlBuff.append("select * from hr_groupmgr where depcod in (select depcod from systemgroups where a_depcod=:a_deptCode and depcod is not null) order by type ");
			map.put("a_deptCode", a_deptCode);
			list = getJdbcTemplate().query(sqlBuff.toString(), mapper, map);
		}
		
		return list;
	}
	
	@Override
	public List<HRGroupMgr> getGroupMgrByDeptPlus(String deptCode, String a_deptCode, String cmpCode, DescionMaker descionMaker){
		StringBuffer buffer = new StringBuffer();
		Map<String, String> map = new HashMap<String, String>();
		buffer.append("select * from hr_groupmgr where 1=1 ");
		if (descionMaker.equals(DescionMaker.DEPTLEADER)){
			buffer.append(" and depcod=:deptCode and cmpcod=:cmpCode and type=-1 ");
			map.put("deptCode", deptCode);
			map.put("cmpCode", cmpCode);
		}
		if (descionMaker.equals(DescionMaker.CENTRALLEADER)) {
			buffer.append(" and he.DEPCOD = :a_deptCode and he.CMPCOD = :cmpCode and he.type = -2");
			map.put("a_deptCode", a_deptCode);
			map.put("cmpCode", cmpCode);
		}
		if (descionMaker.equals(DescionMaker.UNITLEADER)) {
			buffer.append(" and he.CMPCOD = :cmpCode and he.type = -3");
			//map.put("deptCode", deptCode);
			map.put("cmpCode", cmpCode);
		}
		ParameterizedRowMapper<HRGroupMgr> mapper = new GroupMgrRowMapper();
		List<HRGroupMgr> list = getJdbcTemplate().query(buffer.toString(), mapper, map);
		return list;
	}
	
	@Override
	public List<HRGroupMgr> getGroupMgrByDeptEx(String deptId){
		String sql = "select * from hr_groupmgr where (type=-1 or type=-2) and depcod in (select depcod from systemgroups start with groupid=? connect by parentid= prior groupid)";
		ParameterizedRowMapper<HRGroupMgr> mapper = new GroupMgrRowMapper();
		List<HRGroupMgr> list = getJdbcTemplate().query(sql, mapper, deptId);
		return list;
	}

	@Override
	public EmployeePos getMgrEmployeePos(String deptCode, String comCode, String employeeid, boolean forceAssign) {
		EmployeePos employeePos = new EmployeePos();
		List<EmployeePos> list = new ArrayList<EmployeePos>();
		String sql = "select * from HR_EMPPOS he where he.POSDEPCOD =:deptCode and he.CMPCOD = :comCode and he.EMPLOYEEID = :employeeid";
		Map<String, String> map = new HashMap<String, String>();
		map.put("deptCode", deptCode);
		map.put("comCode", comCode);
		map.put("employeeid", employeeid);
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		list = getJdbcTemplate().query(sql, mapper, map);
		if (list != null && list.size() > 0) {
			for (EmployeePos epos : list) {
				boolean isMgr = epos.isDeptmgr() || epos.isCentermgr() || epos.isTopmgr();
				if (forceAssign | isMgr) {
					employeePos = epos;
				}
			}
		}
		return employeePos;
	}

	@Override
	public EmployeePos getEmployeePosByEmpId(String employeeid, String postCode) {
		String sql = "select * from HR_EMPPOS he where he.EMPLOYEEID = :employeeid and he.POSITCOD1 =:postCode";
		Map<String, String> map = new HashMap<String, String>();
		map.put("employeeid", employeeid);
		map.put("postCode", postCode);
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		return getJdbcTemplate().queryForObject(sql, mapper, map);
	}

	@Override
	public List<HRGroupMgr> getGroupMgrByUserName(String employeeId, String type) {
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("select * from hr_groupmgr mgr where mgr.EMPLOYEEID=?");
		// 如果是代理查询所有，如果是助理查询单位中心
		if (type.equals("ASSIST")) {
			sqlBuff.append(" and type <> -1 and type <> -21");
		}
		ParameterizedRowMapper<HRGroupMgr> mapper = new GroupMgrRowMapper();
		return getJdbcTemplate().query(sqlBuff.toString(), mapper, employeeId);
	}

	@Override
	public EmployeePos getMgrEmployeePos(String deptCode, String a_deptCode, String comCode, String employeeid,
			GroupType groupType) {
		List<EmployeePos> list = new ArrayList<EmployeePos>();
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("select * from HR_EMPPOS he where he.CMPCOD = :comCode and he.EMPLOYEEID = :employeeid");
		Map<String, String> map = new HashMap<String, String>();
		map.put("comCode", comCode);
		map.put("employeeid", employeeid);
		// 部门主管
		if (groupType.equals(GroupType.DEPTGROUP)) {
			sqlBuff.append(" and he.POSDEPCOD =:deptCode and he.POSA_DEPCOD =:a_deptCode and MGDEPFLG1 = 1");
			map.put("a_deptCode", a_deptCode);
			map.put("deptCode", deptCode);
		}
		// 中心主管
		if (groupType.equals(GroupType.CENTERGROUP)) {
			sqlBuff.append(" and he.POSA_DEPCOD =:a_deptCode and MGA_DEPFLG1 = 1");
			map.put("a_deptCode", a_deptCode);
		}
		if (groupType.equals(GroupType.CMPGROUP)) {
			sqlBuff.append(" and MGCENTFLG1 = 1");
		}
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		list = getJdbcTemplate().query(sqlBuff.toString(), mapper, map);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	@Override
	public EmployeePos getCenterEmployeeByCmpCode(String cmpCode) {
		//String sql = "select * from HR_EMPPOS he where he.CMPCOD=:cmpCode and he.MGCENTFLG1 = 1 and he.mgposflg<>2";
		/**
		 * 由于成都神旺最高主管是王天华，为兼职岗位，原有的查询主岗位则无法获取人员
		 * 现改为通过是否主岗位来排序，并获取第一条记录。一般主岗位是1，兼职为2，所以取第一条肯定为主岗位，
		 * 如果没有，则就是兼职中的任意一条记录
		 */
		String sql = "select * from (select tb.*,rownum rn from (select * from hr_emppos he where he.cmpcod=:cmpCode and he.mgcentflg1=1 order by he.mgposflg) tb)";
		Map<String, String> map = new HashMap<String, String>();
		map.put("cmpCode", cmpCode);
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		List<EmployeePos> result = getJdbcTemplate().query(sql, mapper, map);
		if (result != null && result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public EmployeePos getCenterFEmployeeByCmpCode(String cmpCode) {
		//String sql = "select * from HR_EMPPOS he where he.CMPCOD=:cmpCode and he.MGCENTFLG1 = 1 and he.mgposflg<>2";
		/**
		 * 通过是否主岗位来排序，并获取第一条记录。一般主岗位是1，兼职为2，所以取第一条肯定为主岗位，
		 * 如果没有，则就是兼职中的任意一条记录
		 */
		String sql = "select * from (select tb.*,rownum rn from (select * from hr_emppos he where he.cmpcod=:cmpCode and he.mgcentfflg1=1 order by he.mgposflg) tb)";
		Map<String, String> map = new HashMap<String, String>();
		map.put("cmpCode", cmpCode);
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		List<EmployeePos> result = getJdbcTemplate().query(sql, mapper, map);
		if (result != null && result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public HbMgrUser getHbMgrUser(String deptId){
		StringBuffer buffer = new StringBuffer();
		buffer.append("select * from ww_flowhbmgr where groupid in ( ");
		buffer.append("select groupid from systemgroups where start with groupid=? connect by parentid= prior groupid) order by employeeid");
		ParameterizedRowMapper<HbMgrUser> mapper = new HbMgrRowMapper();
		List<HbMgrUser> result = getJdbcTemplate().query(buffer.toString(), mapper, deptId);
		if (result != null && result.size() > 0){
			return result.get(0);
		}
		return null;
	}
	
	@Override
	public EmployeePos getChairmanEmployeePos(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(" select * from hr_emppos where mgcmpflg=? ");
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		List<EmployeePos> result = getJdbcTemplate().query(buffer.toString(), mapper, "1");
		if (result != null && result.size() > 0){
			return result.get(0);
		}
		return null;
	}
	
	@Override
	public EmployeePos getSubmitFBossEmployeePos(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(" select * from hr_emppos where FFBossflg=? ");
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		List<EmployeePos> result = getJdbcTemplate().query(buffer.toString(), mapper, "1");
		if (result != null && result.size() > 0){
			return result.get(0);
		}
		return null;
	}
}
