package com.wwgroup.organ.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRCompany;
import com.wwgroup.organ.bo.HRPosition;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.dao.OrganDao;
import com.wwgroup.user.bo.EmployeePos;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class OrganDaoImpl extends JdbcDaoSupport implements OrganDao {

	protected class OrganRowMapper implements ParameterizedRowMapper<SystemGroups> {
		@SuppressWarnings("finally")
		public SystemGroups mapRow(ResultSet rs, int rowNum) {
			SystemGroups groups = new SystemGroups();
			try {
				groups.setGroupID(rs.getInt("GROUPID"));
				groups.setGroupName(rs.getString("GROUPNAME"));
				groups.setParentID(rs.getInt("PARENTID"));
				groups.setDepcod(rs.getString("DEPCOD"));
				groups.setOrgPath(rs.getString("ORGPATH"));
				groups.setA_depcod(rs.getString("A_DEPCOD"));
				groups.setCmpcod(rs.getString("CMPCOD"));
				groups.setGroupType(rs.getInt("GROUPTYPE"));
				groups.setSystemFlg(rs.getString("SYSTEMFLG"));
			}
			catch (SQLException ex) {
				ex.printStackTrace();
			}
			finally {
				return groups;
			}
		}
	}

	protected class PositionRowMapper implements ParameterizedRowMapper<HRPosition> {
		@SuppressWarnings("finally")
		public HRPosition mapRow(ResultSet rs, int rowNum) {
			HRPosition position = new HRPosition();
			try {
				position.setPositionName(rs.getString("POSITIONNAM"));
				position.setPositionId(rs.getInt("POSITIONID"));
				position.setPosicod(rs.getString("POSITCOD"));
				position.setCmpcod(rs.getString("CMPCOD"));
				position.setDepcod(rs.getString("DEPCOD"));
				position.setA_depcode(rs.getString("POSA_DEPCOD"));

			}
			catch (SQLException ex) {
				ex.printStackTrace();
			}
			finally {
				return position;
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
				if (rs.getInt("MGPOSFLG") == 1){
					employeePos.setMajorpost(true);
				} else {
					employeePos.setMajorpost(false);
				}
				employeePos.setMgDeptFlg(rs.getInt("MGDEPFLG1"));
				employeePos.setMgA_DeptFlg(rs.getInt("MGA_DEPFLG1"));
				employeePos.setMgCentFlg(rs.getInt("MGCENTFLG1"));
				if (rs.getInt("MGDEPFLG1") == 1) {
					employeePos.setDeptmgr(true);
				}
				if (rs.getInt("MGA_DEPFLG1") == 1) {
					employeePos.setCentermgr(true);
				}
				if (rs.getInt("MGCENTFLG1") == 1) {
					employeePos.setTopmgr(true);
				}

			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return employeePos;
			}
		}
	}
	
	protected class HRCompanyRowMapper implements ParameterizedRowMapper<HRCompany>{
		public HRCompany mapRow(ResultSet rs, int rowNum){
			HRCompany company = new HRCompany();
			try {
				company.setCmpCode(rs.getString("cmpcod"));
				company.setCmpName(rs.getString("cmpnamsht"));
			} catch (SQLException e) {
				e.printStackTrace();
				company = null;
			}
			return company;
		}
	}

	@Override
	public List<SystemGroups> getGroupsByParent(int parentID) {
		String sql = "select * from SystemGroups sg where sg.grouptype >= 3 and parentid =:parentID";
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("parentID", parentID);
		ParameterizedRowMapper<SystemGroups> mapper = new OrganRowMapper();
		return getJdbcTemplate().query(sql, mapper, map);
	}

	@Override
	public SystemGroups getGroupsByDeptCode(String deptCode, String a_deptCode, String cmpCode, GroupType groupType) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("select * from SystemGroups sg where sg.grouptype >= 3");
		Map<String, String> map = new HashMap<String, String>();

		if (groupType.equals(GroupType.DEPTGROUP)) {
			buffer.append(" and sg.depcod = :depcod and sg.a_depcod = :a_depcod and sg.cmpcod = :cmpcod");
			map.put("depcod", deptCode);
			map.put("a_depcod", a_deptCode);
			map.put("cmpcod", cmpCode);
		}
		else if (groupType.equals(GroupType.CENTERGROUP)) {
			buffer.append(" and sg.depcod is null and sg.a_depcod = :a_depcod and sg.cmpcod = :cmpcod");
			map.put("a_depcod", a_deptCode);
			map.put("cmpcod", cmpCode);
		}
		else if (groupType.equals(GroupType.CMPGROUP)) {
			buffer.append(" and sg.depcod is null and sg.a_depcod is null and sg.cmpcod = :cmpcod");
			map.put("cmpcod", cmpCode);
		}
		else {
			buffer.append(" and sg.depcod = :depcod and sg.a_depcod = :a_depcod and sg.cmpcod = :cmpcod");
			map.put("depcod", deptCode);
			map.put("a_depcod", a_deptCode);
			map.put("cmpcod", cmpCode);
		}

		ParameterizedRowMapper<SystemGroups> mapper = new OrganRowMapper();
		return getJdbcTemplate().queryForObject(buffer.toString(), mapper, map);
	}

	@Override
	public HRPosition getPositionByPostCode(String postCode, String deptCode, String cmpCode) {
		//String sql = "select he.POSITIONNAM, he.POSITIONID, he.POSITCOD, he.CMPCOD, he.DEPCOD, 1 as POSA_DEPCOD from hr_position he where he.positcod=:positcod and he.depcod=:deptCode and he.cmpcod =:cmpCode";
		//String sql = "select he.POSITIONNAM, he.POSITIONID, he.POSITCOD, he.CMPCOD, he.DEPCOD, 1 as POSA_DEPCOD from hr_position he where he.positcod=:positcod and he.cmpcod =:cmpCode";
		String sql = "select he.POSITIONNAM, he.POSITIONID, he.POSITCOD, he.CMPCOD, he.DEPCOD, 1 as POSA_DEPCOD from hr_position he where he.positcod=:positcod";
		Map<String, String> map = new HashMap<String, String>();
		map.put("positcod", postCode);
		//map.put("deptCode", deptCode);
		map.put("cmpCode", cmpCode);
		ParameterizedRowMapper<HRPosition> mapper = new PositionRowMapper();
		return getJdbcTemplate().queryForObject(sql, mapper, map);
	}

	@Override
	public SystemGroups loadGroupsById(int groupId) {
		String sql = "select * from SystemGroups sg where sg.groupid =:groupId";
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("groupId", groupId);
		ParameterizedRowMapper<SystemGroups> mapper = new OrganRowMapper();
		return getJdbcTemplate().queryForObject(sql, mapper, map);
	}

	@Override
	public List<SystemGroups> getAllGroupsByParent(String parentId) {
		//String sql = "select * from SystemGroups sg where sg.grouptype >= 3 and sg.orgpath like :orgPath";
		String sql = "select * from systemgroups start with groupid=:groupid connect by parentid= prior groupid";
		Map<String, String> map = new HashMap<String, String>();
		map.put("groupid", parentId);
		ParameterizedRowMapper<SystemGroups> mapper = new OrganRowMapper();
		return getJdbcTemplate().query(sql, mapper, map);
	}
	@Override
	public List<SystemGroups> getAllGroupsByParent(String orgPath,EmployeePos parentEmployeePos) {
		Map<String, String> map = new HashMap<String, String>();
		String sql = null;
		if(parentEmployeePos.isTopmgr()){
			//地方主管
			sql = "select * from SystemGroups sg where sg.cmpcod = :cmpcod";
			map.put("cmpcod", parentEmployeePos.getCmpcod());
		}else if(parentEmployeePos.isCentermgr()){
			//中心主管
			sql = "select * from SystemGroups sg where sg.a_depcod = :a_depcod";
			map.put("a_depcod", parentEmployeePos.getA_deptcode());
		}else if(parentEmployeePos.isDeptmgr()){
			//部门主管
			sql = "select * from SystemGroups sg where sg.depcod = :depcod";
			map.put("depcod", parentEmployeePos.getDeptcode());
		}else{
			sql = "select * from SystemGroups sg where sg.grouptype >= 3 and sg.orgpath like :orgPath";
			map.put("orgPath", orgPath + "%");
		}
		
		ParameterizedRowMapper<SystemGroups> mapper = new OrganRowMapper();
		List<SystemGroups> list =  getJdbcTemplate().query(sql, mapper, map);
		List<SystemGroups> allList = new ArrayList<SystemGroups>();
		allList.addAll(list);
		map.clear();
		for(SystemGroups curGroup : list){
			if(curGroup.getGroupType() == 4){
				String innerSql = "select * from SystemGroups sg where sg.orgpath like :orgPath ";
				map.put("orgPath", curGroup.getOrgPath() + "%");
				List<SystemGroups> children = getJdbcTemplate().query(innerSql, mapper, map);
				allList.addAll(children);
			}
		}
		
		return allList;
		
	}

	@Override
	public List<HRPosition> getAssistPositionsByUser(String employeeId, GroupType groupType) {
		StringBuffer buffer = new StringBuffer();
		buffer
				.append("select p.POSITIONNAM, p.POSITIONID, p.POSITCOD, p.CMPCOD, p.DEPCOD, ep.POSA_DEPCOD from hr_position p join hr_emppos ep on p.POSITCOD = ep.POSITCOD1 "
						+ " where ep.EMPLOYEEID = ?");
		if (GroupType.DEPTGROUP.equals(groupType)) {
			buffer.append(" and ep.MGDEPFLG1 = 1");
		}
		if (GroupType.CENTERGROUP.equals(groupType)) {
			buffer.append(" and ep.MGA_DEPFLG1 = 1");
		}
		if (GroupType.CMPGROUP.equals(groupType)) {
			buffer.append(" and ep.MGCENTFLG1 = 1");
		}
		if (groupType == null) {
			buffer.append(" and ep.MGDEPFLG1 = 0 and ep.MGCENTFLG1 = 0 and ep.MGCENTFLG1 = 0");
		}
		ParameterizedRowMapper<HRPosition> mapper = new PositionRowMapper();
		return getJdbcTemplate().query(buffer.toString(), mapper, employeeId);
	}
	
	@Override
	public int filterToCompanyLevel(int id) {
		
		if (!isCompany(id)) {
			String sql = "select * from SystemGroups sg where sg.groupid = ?";
			ParameterizedRowMapper<SystemGroups> mapper = new OrganRowMapper();
			return getJdbcTemplate().query(sql, mapper, id).get(0).getParentID();
		}
		
		return id;
	}
	
	private boolean isCompany(int id) {
		String countSQL = "select count(1) from SystemGroups sg where sg.groupid = ? and sg.parentid not in (select groupid from SystemGroups where parentid = '701' or parentid = '0')";
		int result = getJdbcTemplate().queryForInt(countSQL, id);
		return result == 0;
	}
	
	
	/**
	 * 获得人员的所有岗位的部门
	 * 
	 * 依据GroupType确定groupid
	 * 
	 */
	@Override
	public List<SystemGroups> getUserGroup(String employeeid) {
		
		List<SystemGroups> result = new ArrayList<SystemGroups>();
		
		for (HRPosition h : getAssistPositionsByUser(employeeid, GroupType.DEPTGROUP)) {
			result.add(getGroupsByDeptCode(h.getDepcod(), h.getA_depcode(), h.getCmpcod(), GroupType.DEPTGROUP));
		}
		
		for (HRPosition h : getAssistPositionsByUser(employeeid, GroupType.CMPGROUP)) {
			result.add(getGroupsByDeptCode(h.getDepcod(), h.getA_depcode(), h.getCmpcod(), GroupType.CMPGROUP));
		}
		
		for (HRPosition h : getAssistPositionsByUser(employeeid, GroupType.CENTERGROUP)) {
			result.add(getGroupsByDeptCode(h.getDepcod(), h.getA_depcode(), h.getCmpcod(), GroupType.CENTERGROUP));
		}
		
		for (HRPosition h : getAssistPositionsByUser(employeeid, null)) {
			result.add(getGroupsByDeptCode(h.getDepcod(), h.getA_depcode(), h.getCmpcod(), GroupType.DEPTGROUP));
		}
		
		return result;
		
	}
	
	// add by Cao_Shengyong 2014-03-19
	// 用于判断某用户的某岗位是否是对应的主管
	public boolean filterMgrPositionByUser(String employeeid, GroupType groupType, String position){
		int bRet = 0;
		StringBuffer buffer = new StringBuffer();
		buffer
				.append("select count(1) from hr_position p join hr_emppos ep on p.POSITCOD = ep.POSITCOD1 "
						+ " where ep.EMPLOYEEID = ? and p.positcod= ? ");
		if (GroupType.DEPTGROUP.equals(groupType)) {
			buffer.append(" and ep.MGDEPFLG1 = 1");
		}
		if (GroupType.CENTERGROUP.equals(groupType)) {
			buffer.append(" and ep.MGA_DEPFLG1 = 1");
		}
		if (GroupType.CMPGROUP.equals(groupType)) {
			buffer.append(" and ep.MGCENTFLG1 = 1");
		}
		if (groupType == null) {
			buffer.append(" and ep.MGDEPFLG1 = 0 and ep.MGCENTFLG1 = 0 and ep.MGCENTFLG1 = 0");
		}
		//ParameterizedRowMapper<HRPosition> mapper = new PositionRowMapper();
		
		bRet = getJdbcTemplate().queryForInt(buffer.toString(), employeeid, position);
		return bRet != 0;
	}
	
	public EmployeePos loadEmployeePos(String employeeid, String deptCode, String a_deptCode, String cmpCode){
		EmployeePos employeePos = null;
		Map<String, String> map = new HashMap<String, String>();
		StringBuffer buf = new StringBuffer();
		buf.append("select * from hr_emppos where 1=1 and employeeid = '" + employeeid + "' ");
		//map.put("employeeid", employeeid);
		if (StringUtils.isNotEmpty(cmpCode)){
			buf.append(" and cmpcod ='" + cmpCode + "' ");
			//map.put("cmpCode", cmpCode);
		}
		/*if (StringUtils.isNotEmpty(deptCode)){
			buf.append(" and posdepcod = '" + deptCode + "' ");
		} else {
			buf.append(" and posa_depcod = '" + a_deptCode + "' ");
		}*/
		//System.out.println(map);
		buf.append(" order by mgposflg");
		//System.out.println(buf.toString());
		ParameterizedRowMapper<EmployeePos> mapper = new EmployeePosRowMapper();
		List<EmployeePos> result = getJdbcTemplate().query(buf.toString(), mapper, map);
		boolean hasMajorpost = false;
		// System.out.println(result.size());
		//int retIndex = 0;
		if (result != null && result.size() > 0){
			SystemGroups systemGroups = new SystemGroups();
			if (StringUtils.isNotEmpty(deptCode)){
				systemGroups = getGroupsByDeptCode(deptCode, a_deptCode, cmpCode, GroupType.DEPTGROUP);
			} else {
				if (StringUtils.isNotEmpty(a_deptCode)){
					systemGroups = getGroupsByDeptCode(deptCode, a_deptCode, cmpCode, GroupType.CENTERGROUP);
				} else {
					systemGroups = getGroupsByDeptCode(deptCode, a_deptCode, cmpCode, GroupType.CENTERGROUP);
				}
			}
			for(int i = 0; i < result.size(); i++){
				employeePos = result.get(i);
				// 如果传入的部门代码不为空,说明会办的是部门主管
				if (StringUtils.isNotEmpty(deptCode)){
					// 找到当前部门代码的记录,并且是部门主管
					if (employeePos.getDeptcode().equalsIgnoreCase(deptCode) && (employeePos.getMgDeptFlg() == 1)){
						hasMajorpost = true;
						/*if (employeePos.isMajorpost()){
							hasMajorpost = true;
						} else {
							hasMajorpost = true;
						}*/
					}
				} else {
					if (StringUtils.isNotEmpty(a_deptCode)){
						// 部门代码为空,说明会办的是中心主管,则去判断中心代码是否为空
						if (employeePos.getA_deptcode().equalsIgnoreCase(a_deptCode)) {
							//  && (employeePos.getMgA_DeptFlg() == 1)
							if (systemGroups.getCmpcod().equals("HQTR")){
								if (systemGroups.getParentID() == 705){
									if (employeePos.getMgA_DeptFlg() == 1){
										hasMajorpost = true;
									}
								} else {
									if (employeePos.getMgDeptFlg() == 1){
										hasMajorpost = true;
									}
								}
							} else {
								if (employeePos.getMgA_DeptFlg() == 1){
									hasMajorpost = true;
								}
							}
						}
					} else {
						// 如果部门和中心代码都为空,则说明会办的是单位,则获取该单位代码的记录
						// 一般单位主管肯定是主岗位,所以此处仅获取主岗位的信息
						if (employeePos.isMajorpost()){
							hasMajorpost = true;
						} else {
							// 如果没有主岗位信息,则只能是去随机获取一条记录
							hasMajorpost = true;
						}
					}
				}
				if (hasMajorpost){
					break;
				}
				/*if (employeePos.isMajorpost()){
					hasMajorpost = true;
					break;
				}*/
			}
			if (!hasMajorpost){
				employeePos = result.get(0);
			}
		}
		return employeePos;
	}
	
	public List<SystemGroups> getAllGroupsByCode(String cmpcod){
		StringBuffer buffer = new StringBuffer();
		buffer.append("select * from SystemGroups sg where sg.grouptype >= 3");
		buffer.append(" and sg.cmpcod = :cmpcod");
		Map<String, String> map = new HashMap<String, String>();
		map.put("cmpcod", cmpcod);
		
		ParameterizedRowMapper<SystemGroups> mapper = new OrganRowMapper();
		return getJdbcTemplate().query(buffer.toString(), mapper, map);
	}
	
	@Override
	public List<HRCompany> getCompanyList(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("select cmpcod,cmpnamsht from hr_a_company order by cmpnamsht");
		ParameterizedRowMapper<HRCompany> mapper = new HRCompanyRowMapper();
		return getJdbcTemplate().query(buffer.toString(), mapper);
	}
	
	@Override
	public int getSizeByP(int groupid, int parentid){
		StringBuffer buffer = new StringBuffer();
		buffer.append("select groupid from systemgroups where groupid=? start with parentid=? connect by parentid=prior groupid");
		int result = 0;
		try{
			result = getJdbcTemplate().queryForInt(buffer.toString(), groupid, parentid);
		} catch (Exception e) {
			result = 0;
		}
		return result;
	}
	
	@Override
	public List<SystemGroups> getCanUpAttachOrg(String groupid){
		String sql = "select * from systemgroups where groupid=:groupid start with groupid in (select groupid from ww_flowattachset) connect by parentid=prior groupid";
		Map<String, String> map = new HashMap<String, String>();
		map.put("groupid", groupid);
		ParameterizedRowMapper<SystemGroups> mapper = new OrganRowMapper();
		return getJdbcTemplate().query(sql, mapper, map);
	}
	
	@Override
	public int getJYJurisdiction(String empid){
		int result = 0;
		StringBuffer sb = new StringBuffer();
		sb.append("select count(1) from WW_FLOWSEARCHSET where empid=? and status=?");
		try {
			result = getJdbcTemplate().queryForInt(sb.toString(), empid, 1);
		} catch (Exception e) {
			result = 0;
		}
		return result;
	}
	
	@Override
	public List<SystemGroups> getGroupsByCmp(int parentID, String cmpcod) {
		List<Object> params = new ArrayList<Object>();
		//String sql = "select * from SystemGroups sg where sg.grouptype >= 3 and parentid =:parentID";
		StringBuffer buffer = new StringBuffer();
		buffer.append("select * from systemgroups ");
		buffer.append(" where grouptype in (3,4) and parentid=? ");
		params.add(parentID);
		if (StringUtils.isNotEmpty(cmpcod)){
			buffer.append(" and cmpcod=? ");
			params.add(cmpcod);
		}
		ParameterizedRowMapper<SystemGroups> mapper = new OrganRowMapper();
		return getJdbcTemplate().query(buffer.toString(), mapper, params.toArray());
	}
}
