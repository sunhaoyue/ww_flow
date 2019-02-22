package com.wwgroup.flow.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRGroupMgr;
import com.wwgroup.organ.bo.HRPosition;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.service.OrganService;
import com.wwgroup.user.bo.EmployeePos;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

public class PersonServiceImpl implements PersonService {

	private UserService userService;

	private OrganService organService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setOrganService(OrganService organService) {
		this.organService = organService;
	}

	@Override
	public PersonDetail loadWidePersonDetail(String employeeId) {
		PersonDetail person = new PersonDetail();
		SystemUsers user = userService.getUsersByName(employeeId);
		// 获取的是主岗位的信息
		EmployeePos employeePos = userService.getEmployeePosByEmpId(employeeId);
		if (employeePos == null) {
			return null;
		}
		SystemGroups groups = new SystemGroups();
		// 部门代码
		String deptCode = employeePos.getDeptcode();
		// 中心代码
		String a_deptCode = employeePos.getA_deptcode();
		// 公司代码
		String cmpCode = employeePos.getCmpcod();
		if (user != null && employeePos != null) {
			if (employeePos.isDeptmgr()) {
				groups = organService.getGroupsByDeptCode(deptCode, a_deptCode,
						cmpCode, GroupType.DEPTGROUP);
			} else if (employeePos.isCentermgr()) {
				groups = organService.getGroupsByDeptCode(deptCode, a_deptCode,
						cmpCode, GroupType.CENTERGROUP);
			} else if (employeePos.isTopmgr()) {
				groups = organService.getGroupsByDeptCode(deptCode, a_deptCode,
						cmpCode, GroupType.CMPGROUP);
			} else {
				groups = organService.getGroupsByDeptCode(deptCode, a_deptCode,
						cmpCode, GroupType.DEPTGROUP);
			}
			HRPosition position = organService.getPositionByPostCode(
					employeePos.getPostcode(), deptCode, cmpCode);

			if (groups != null && position != null) {
				person = this.buildPersonDetail(person, groups, user,
						employeePos, position);
				person.setDeptCode(groups.getDepcod());
				person.setA_deptCode(groups.getA_depcod());
				person.setCompPhone(user.getWorkPhone());
			}
			person.setTitname(user.getTitnam());
		}
		return person;
	}

	@Override
	public PersonDetail loadWidePersonDetail(String deptId, String employeeId,
			String postcode) {
		PersonDetail person = new PersonDetail();
		SystemGroups groups = organService.loadGroupsById(Integer
				.valueOf(deptId));
		SystemUsers user = userService.getUsersByName(employeeId);

		EmployeePos employeePos = userService.getEmployeePosByEmpId(
				user.getDepcod(), user.getCmpcod(), employeeId, postcode);

		if (groups != null && user != null && employeePos != null) {
			HRPosition position = organService.getPositionByPostCode(postcode,
					user.getDepcod(), user.getCmpcod());

			person = this.buildPersonDetail(person, groups, user, employeePos,
					position);
			person.setDeptCode(user.getDepcod());
			person.setCmpCode(user.getCmpcod());
			person.setA_deptCode(groups.getA_depcod());
			// person.setTitname(position.getPositionName());
			person.setTitname(user.getTitnam());
		}
		return person;
	}

	@Override
	public PersonDetail loadWidePersonDetail(String employeeId, String postcode) {
		PersonDetail person = new PersonDetail();
		SystemUsers user = userService.getUsersByName(employeeId);
		EmployeePos employeePos = userService.getEmployeePosByEmpId(employeeId,
				postcode);
		SystemGroups groups = organService.getGroupsByDeptCode(
				employeePos.getDeptcode(), employeePos.getA_deptcode(),
				employeePos.getCmpcod(), GroupType.DEPTGROUP);
		if (user != null && employeePos != null) {
			HRPosition position = organService.getPositionByPostCode(postcode,
					employeePos.getDeptcode(), employeePos.getCmpcod());

			person = this.buildPersonDetail(person, groups, user, employeePos,
					position);
			person.setDeptCode(groups.getDepcod());
			person.setCmpCode(groups.getCmpcod());
			person.setA_deptCode(groups.getA_depcod());
			person.setTitname(position.getPositionName());
		}
		return person;
	}

	public PersonDetail loadWidePersonDetailPlus(String deptId,
			String employeeId, String postcode) {
		PersonDetail person = new PersonDetail();
		SystemGroups groups = organService.loadGroupsById(Integer
				.valueOf(deptId));
		SystemUsers user = userService.getUsersByName(employeeId);

		EmployeePos employeePos = userService.getEmployeePosByEmpId(
				user.getDepcod(), user.getCmpcod(), employeeId, postcode);

		if (groups != null && user != null && employeePos != null) {
			HRPosition position = organService.getPositionByPostCode(postcode,
					user.getDepcod(), user.getCmpcod());

			person = this.buildPersonDetail(person, groups, user, employeePos,
					position);
			person.setDeptCode(employeePos.getDeptcode());
			person.setCmpCode(groups.getCmpcod());
			person.setA_deptCode(groups.getA_depcod());
			person.setTitname(position.getPositionName());
		}
		return person;
	}

	@Override
	public PersonDetail loadWideMgrPersonDetail(String deptId) {
		PersonDetail mgrPerson = new PersonDetail();
		SystemGroups groups = organService.loadGroupsById(Integer
				.valueOf(deptId));
		String deptCode = groups.getDepcod();
		String a_deptCode = groups.getA_depcod();
		String cmpCode = groups.getCmpcod();
		HRGroupMgr groupMgr = new HRGroupMgr();

		// 部门主管
		if (StringUtils.isNotEmpty(deptCode)
				&& StringUtils.isNotEmpty(a_deptCode)
				&& StringUtils.isNotEmpty(cmpCode)) {
			groupMgr = userService.getGroupMgrByDept(deptCode, a_deptCode,
					cmpCode, DescionMaker.DEPTLEADER);
			mgrPerson.setDeptCode(deptCode);
			mgrPerson.setA_deptCode(a_deptCode);
			mgrPerson.setCmpCode(cmpCode);
			if (groupMgr != null) {
				mgrPerson = this.buildMgr(mgrPerson, groupMgr, groups,
						GroupType.DEPTGROUP);
			}
		}
		// 中心主管
		if (StringUtils.isEmpty(deptCode) && StringUtils.isNotEmpty(a_deptCode)
				&& StringUtils.isNotEmpty(cmpCode)) {
			// 如果groupMgr不为空，说明其本身就是主管了
			groupMgr = userService.getGroupMgrByDept(deptCode, a_deptCode,
					cmpCode, DescionMaker.CENTRALLEADER);
			mgrPerson.setA_deptCode(a_deptCode);
			mgrPerson.setCmpCode(cmpCode);
			if (groupMgr != null) {
				mgrPerson = this.buildMgr(mgrPerson, groupMgr, groups,
						GroupType.CENTERGROUP);
			}
		}

		// 单位主管
		if (StringUtils.isEmpty(deptCode) && StringUtils.isEmpty(a_deptCode)
				&& StringUtils.isNotEmpty(cmpCode)) {
			groupMgr = userService.getGroupMgrByDept(deptCode, a_deptCode,
					cmpCode, DescionMaker.UNITLEADER);
			mgrPerson.setCmpCode(cmpCode);
			if (groupMgr != null) {
				mgrPerson = this.buildMgr(mgrPerson, groupMgr, groups,
						GroupType.CMPGROUP);
			}
		}
		return mgrPerson;
	}

	@Override
	public PersonDetail loadWideMgrPersonDetailPlus(String deptId,
			GroupType groupType) {
		PersonDetail mgrPerson = new PersonDetail();
		HRGroupMgr groupMgr = new HRGroupMgr();
		List<SystemGroups> listGroups = organService
				.getAllGroupsByParent(Integer.valueOf(deptId));
		if (listGroups != null && listGroups.size() > 0) {
			int i = 0;
			List<HRGroupMgr> groupMgrs = new ArrayList<HRGroupMgr>();
			for (i = 0; i < listGroups.size(); i++) {
				SystemGroups groups = listGroups.get(i);
				groupMgrs = null;
				String deptCode = groups.getDepcod();
				String a_deptCode = groups.getA_depcod();
				String cmpCode = groups.getCmpcod();
				if (groupType == GroupType.DEPTGROUP) {
					// 如果是要找部门主管，则找该GROUPID及子部门中的部门
					if (groups.getGroupType() == 5) {
						groupMgrs = userService.getGroupMgrByDeptPlus(deptCode,
								a_deptCode, cmpCode, DescionMaker.DEPTLEADER);
						if (groupMgrs != null) {
							groupMgr = groupMgrs.get(0);
							mgrPerson.setDeptCode(deptCode);
							mgrPerson.setA_deptCode(a_deptCode);
							mgrPerson.setCmpCode(cmpCode);
							mgrPerson = this.buildMgr(mgrPerson, groupMgr,
									groups, GroupType.DEPTGROUP);
							break;
						}
					}
				} else if (groupType == GroupType.CENTERGROUP) {
					if (groups.getGroupType() == 4) {
						groupMgrs = userService
								.getGroupMgrByDeptPlus(deptCode, a_deptCode,
										cmpCode, DescionMaker.CENTRALLEADER);
						if (groupMgrs != null) {
							groupMgr = groupMgrs.get(0);
							mgrPerson.setA_deptCode(a_deptCode);
							mgrPerson.setCmpCode(cmpCode);
							mgrPerson = this.buildMgr(mgrPerson, groupMgr,
									groups, GroupType.CENTERGROUP);
							break;
						}
					}
				} else if (groupType == GroupType.CMPGROUP) {
					if (groups.getGroupType() == 3) {
						groupMgrs = userService.getGroupMgrByDeptPlus(deptCode,
								a_deptCode, cmpCode, DescionMaker.UNITLEADER);
						if (groupMgrs != null) {
							groupMgr = groupMgrs.get(0);
							mgrPerson.setCmpCode(cmpCode);
							mgrPerson = this.buildMgr(mgrPerson, groupMgr,
									groups, GroupType.CMPGROUP);
							break;
						}
					}
				}
			}
		}
		/*
		 * SystemGroups groups =
		 * organService.loadGroupsById(Integer.valueOf(deptId)); String deptCode
		 * = groups.getDepcod(); String a_deptCode = groups.getA_depcod();
		 * String cmpCode = groups.getCmpcod();
		 * 
		 * if (groupType == GroupType.DEPTGROUP){ groupMgr =
		 * userService.getGroupMgrByDept(deptCode, a_deptCode, cmpCode,
		 * DescionMaker.DEPTLEADER); mgrPerson.setA_deptCode(a_deptCode);
		 * mgrPerson.setCmpCode(cmpCode); if (groupMgr != null){ mgrPerson =
		 * this.buildMgr(mgrPerson, groupMgr, groups, GroupType.DEPTGROUP); } }
		 * if (groupType == GroupType.CENTERGROUP){ groupMgr =
		 * userService.getGroupMgrByDept(deptCode, a_deptCode, cmpCode,
		 * DescionMaker.CENTRALLEADER); mgrPerson.setA_deptCode(a_deptCode);
		 * mgrPerson.setCmpCode(cmpCode); if (groupMgr != null) { mgrPerson =
		 * this.buildMgr(mgrPerson, groupMgr, groups, GroupType.CENTERGROUP); }
		 * } if (groupType == GroupType.CMPGROUP){ groupMgr =
		 * userService.getGroupMgrByDept(deptCode, a_deptCode, cmpCode,
		 * DescionMaker.UNITLEADER); mgrPerson.setCmpCode(cmpCode); if (groupMgr
		 * != null) { mgrPerson = this.buildMgr(mgrPerson, groupMgr, groups,
		 * GroupType.CMPGROUP); } }
		 */
		return mgrPerson;
	}

	@Override
	public PersonDetail getMgrPersonDetail(PersonDetail actualPerson) {
		PersonDetail mgrPerson = new PersonDetail();

		// 获得员工编号
		String employeeId = actualPerson.getEmployeeId();
		// 上级主管工号
		String mgremployeeid = actualPerson.getMgrEmployeeid();
		// 上级主管岗位代码
		String mgrpostCode = actualPerson.getMgrPostcode();
		// System.out.println(employeeId + "^#" + mgremployeeid + "#" +
		// mgrpostCode);
		if (mgremployeeid != null) {
			SystemUsers users = userService.getUsersByName(mgremployeeid);

			if (StringUtils.isNotEmpty(employeeId)
					&& StringUtils.isNotEmpty(mgremployeeid)
					&& StringUtils.isNotEmpty(mgrpostCode)) {

				// 获得上级主管的相关信息(不一定是部门、中心、单位最高主管)
				EmployeePos mgremployeePos = userService.getEmployeePosByEmpId(
						mgremployeeid, mgrpostCode);

				// 部门代码
				String deptCode = mgremployeePos.getDeptcode();
				// 中心代码
				String a_deptCode = mgremployeePos.getA_deptcode();
				// 公司代码
				String cmpCode = mgremployeePos.getCmpcod();
				SystemGroups mgrGroups = new SystemGroups();
				if (mgremployeePos.isDeptmgr()) {
					mgrGroups = organService.getGroupsByDeptCode(deptCode,
							a_deptCode, cmpCode, GroupType.DEPTGROUP);
				} else if (mgremployeePos.isCentermgr()) {
					mgrGroups = organService.getGroupsByDeptCode(deptCode,
							a_deptCode, cmpCode, GroupType.CENTERGROUP);
				} else if (mgremployeePos.isTopmgr()) {
					mgrGroups = organService.getGroupsByDeptCode(deptCode,
							a_deptCode, cmpCode, GroupType.CMPGROUP);
				} else {
					mgrGroups = organService.getGroupsByDeptCode(deptCode,
							a_deptCode, cmpCode, GroupType.DEPTGROUP);
				}
				HRPosition mgrPosition = organService.getPositionByPostCode(
						mgrpostCode, deptCode, cmpCode);

				mgrPerson = this.buildPersonDetail(mgrPerson, mgrGroups, users,
						mgremployeePos, mgrPosition);
				mgrPerson.setDeptCode(deptCode);
				mgrPerson.setA_deptCode(a_deptCode);
				mgrPerson.setCmpCode(cmpCode);
			}
		}
		return mgrPerson;
	}

	/**
	 * @see com.wwgroup.flow.service.PersonService#getOrganCodes(com.wwgroup.flow.bo.PersonDetail)
	 */
	@Override
	public String[] getOrganCodes(PersonDetail actualPerson) {
		String[] organCodes = new String[2];
		String deptId = actualPerson.getDeptId();
		if (StringUtils.isNotEmpty(deptId)) {
			// SystemGroups groups =
			// organService.loadGroupsById(Integer.valueOf(deptId));
			organCodes = new String[2];
			organCodes[0] = actualPerson.getCmpCode();
			organCodes[1] = actualPerson.getDeptCode();
		}
		return organCodes;
	}

	public static void main(String[] args) {
		String fullName = "崔翠翠(HA0812524)";
		String employeeId = fullName.substring(fullName.indexOf("(") + 1,
				fullName.indexOf(")"));
		String realName = fullName.substring(0, fullName.indexOf("("));
		System.out.println(employeeId);
		System.out.println(realName);
	}

	/**
	 * @see com.wwgroup.flow.service.PersonService#quailifiedDecisionMaker(com.wwgroup.flow.bo.helper.DescionMaker,
	 *      com.wwgroup.flow.bo.PersonDetail)
	 */
	@Override
	public boolean quailifiedDecisionMaker(DescionMaker decionmaker,
			PersonDetail mgrPerson) {
		HRGroupMgr groupMgr = userService.getGroupMgrByDept(
				mgrPerson.getDeptCode(), mgrPerson.getA_deptCode(),
				mgrPerson.getCmpCode(), mgrPerson.getEmployeeId(), decionmaker);
		if (groupMgr != null) {
			return true;
		}
		return false;
	}

	/**
	 * @see com.wwgroup.flow.service.PersonService#quailifiedDecisionMakerPlus(com.wwgroup.flow.bo.helper.DescionMaker,
	 *      com.wwgroup.flow.bo.PersonDetail)
	 */
	@Override
	public boolean quailifiedDecisionMakerPlus(DescionMaker decionmaker,
			PersonDetail mgrPerson) {
		HRGroupMgr groupMgr = userService.getGroupMgrByDeptPlus(
				mgrPerson.getDeptCode(), mgrPerson.getA_deptCode(),
				mgrPerson.getCmpCode(), mgrPerson.getEmployeeId(), decionmaker);
		if (groupMgr != null) {
			return true;
		}
		return false;
	}

	/**
	 * @see com.wwgroup.flow.service.PersonService#getSubDeptIds(java.lang.String)
	 */
	@Override
	public String[] getSubDeptIds(String deptId) {
		String[] subDeptIds = null;
		// TODO 目前没有查询所有节点的方法
		List<SystemGroups> groups = organService.getGroupsByParent(Integer
				.valueOf(deptId));

		if (groups.size() == 0)
			return null;

		if (groups != null && groups.size() > 0) {
			subDeptIds = new String[groups.size()];
			for (int i = 0; i < groups.size(); i++) {
				subDeptIds[i] = String.valueOf(groups.get(i).getGroupID());
			}
		}

		// 递归找出所有下辖部门节点
		for (String groupid : subDeptIds) {
			String[] child = getSubDeptIds(groupid);
			if (child != null) {
				List<String> a = Arrays.asList(child);
				List<String> b = Arrays.asList(subDeptIds);
				List<String> c = new ArrayList<String>();
				c.addAll(a);
				c.addAll(b);
				subDeptIds = c.toArray(new String[a.size() + b.size()]);
			}
		}

		return subDeptIds;
	}

	@Override
	public Map<String, String> getMgrPostMap(PersonDetail mgrPerson) {
		// 推荐直接过滤出来这个人的全部岗位，然后再筛选是否为主管的
		return new HashMap<String, String>();
	}

	/**
	 * 构造人员信息
	 */
	private PersonDetail buildPersonDetail(PersonDetail person,
			SystemGroups group, SystemUsers user, EmployeePos empos,
			HRPosition position) {

		// user
		String realName = user.getFullName().substring(0,
				user.getFullName().indexOf("("));
		person.setEmployeeId(user.getUserName());
		person.setName(realName);
		person.setCompPhone(user.getWorkPhone());
		person.setUserID(user.getUserID());

		person.setPostId(String.valueOf(position.getPositionId()));
		person.setPostName(position.getPositionName());
		person.setPostCode(position.getPosicod());

		// group
		person.setDeptId(String.valueOf(group.getGroupID()));
		person.setDeptName(group.getGroupName());
		person.setDeptPath(group.getOrgPath());
		// person.setDeptCode(group.getDepcod());
		// person.setA_deptCode(group.getA_depcod());
		person.setCmpCode(user.getCmpcod());

		// mgr
		if (!empos.getMgremployeeid().equals('0')) {
			person.setMgrEmployeeid(empos.getMgremployeeid());
			person.setMgrPostcode(empos.getMgrpostcode());
		}
		return person;

	}

	/**
	 * 构造部门主管信息
	 * 
	 * @param mgrPerson
	 * @param groupMgr
	 * @param group
	 * @return
	 */
	@SuppressWarnings("unused")
	private PersonDetail buildMgr(PersonDetail mgrPerson, HRGroupMgr groupMgr,
			SystemGroups group, GroupType groupType) {
		// 人员信息
		String employeeid = groupMgr.getEmployeeid();
		mgrPerson.setEmployeeId(employeeid);
		mgrPerson.setName(groupMgr.getEmployeenam());

		SystemUsers user = userService.getUsersByName(employeeid);
		//System.out.println(" ***************** ");
		//System.out.println(group.getDepcod() + " $ " + group.getA_depcod() + " $ " + group.getCmpcod() + " $ " + employeeid + " $ " + groupType);
		EmployeePos employeePos = userService.getMgrEmployeePos(
				group.getDepcod(), group.getA_depcod(), group.getCmpcod(),
				employeeid, groupType);

		//System.out.println(employeePos);
		// 岗位信息
		if (employeePos != null) {
			mgrPerson.setPostCode(employeePos.getPostcode());
		}

		// 组织信息
		mgrPerson.setDeptName(group.getGroupName());
		mgrPerson.setDeptPath(group.getOrgPath());
		mgrPerson.setDeptId(String.valueOf(group.getGroupID()));

		return mgrPerson;

	}

	@Override
	public List<PersonDetail> loadWideMgrPersonDetails(String deptId) {
		List<PersonDetail> mgrPersons = new ArrayList<PersonDetail>();
		List<HRGroupMgr> groupMgrs = new ArrayList<HRGroupMgr>();
		SystemGroups groups = organService.loadGroupsById(Integer
				.valueOf(deptId));
		String deptCode = groups.getDepcod();
		String a_deptCode = groups.getA_depcod();
		String cmpCode = groups.getCmpcod();
		// 部门主管
		if (StringUtils.isNotEmpty(deptCode)
				&& StringUtils.isNotEmpty(a_deptCode)
				&& StringUtils.isNotEmpty(cmpCode)) {
			groupMgrs = userService.getGroupMgrsByDept(deptCode, a_deptCode,
					cmpCode, DescionMaker.DEPTLEADER);
			if (groupMgrs != null && groupMgrs.size() > 0) {
				for (HRGroupMgr groupMgr : groupMgrs) {
					PersonDetail mgrPerson = new PersonDetail();
					this.buildMgr(mgrPerson, groupMgr, groups,
							GroupType.DEPTGROUP);
					mgrPerson.setDeptCode(deptCode);
					mgrPerson.setA_deptCode(a_deptCode);
					mgrPerson.setCmpCode(cmpCode);
					mgrPersons.add(mgrPerson);
				}
			}
		}
		// 中心主管
		if (StringUtils.isEmpty(deptCode) && StringUtils.isNotEmpty(a_deptCode)
				&& StringUtils.isNotEmpty(cmpCode)) {
			// 如果groupMgr不为空，说明其本身就是主管了
			groupMgrs = userService.getGroupMgrsByDept(deptCode, a_deptCode,
					cmpCode, DescionMaker.CENTRALLEADER);
			if (groupMgrs != null && groupMgrs.size() > 0) {
				for (HRGroupMgr groupMgr : groupMgrs) {
					PersonDetail mgrPerson = new PersonDetail();
					this.buildMgr(mgrPerson, groupMgr, groups,
							GroupType.CENTERGROUP);
					mgrPerson.setA_deptCode(a_deptCode);
					mgrPerson.setCmpCode(cmpCode);
					mgrPersons.add(mgrPerson);
				}
			}
		}

		// 单位主管
		if (StringUtils.isEmpty(deptCode) && StringUtils.isEmpty(a_deptCode)
				&& StringUtils.isNotEmpty(cmpCode)) {
			groupMgrs = userService.getGroupMgrsByDept(deptCode, a_deptCode,
					cmpCode, DescionMaker.UNITLEADER);
			if (groupMgrs != null && groupMgrs.size() > 0) {
				for (HRGroupMgr groupMgr : groupMgrs) {
					PersonDetail mgrPerson = new PersonDetail();
					this.buildMgr(mgrPerson, groupMgr, groups,
							GroupType.CMPGROUP);
					mgrPerson.setCmpCode(cmpCode);
					mgrPersons.add(mgrPerson);
				}
			}
		}
		return mgrPersons;
	}
	
	@Override
	public List<PersonDetail> loadWideMgrPersonDetailsPlus(String deptId){
		List<PersonDetail> mgrPersons = new ArrayList<PersonDetail>();
		List<HRGroupMgr> groupMgrs = new ArrayList<HRGroupMgr>();
		SystemGroups groups = organService.loadGroupsById(Integer.valueOf(deptId));
		String deptCode = groups.getDepcod();
		String a_deptCode = groups.getA_depcod();
		String cmpCode = groups.getCmpcod();
		groupMgrs = userService.getGroupMgrByDeptEx(deptId);
		if (groupMgrs != null && groupMgrs.size() > 0){
			for(HRGroupMgr groupMgr : groupMgrs){
				PersonDetail mgrPerson = new PersonDetail();
				this.buildMgr(mgrPerson, groupMgr, groups, GroupType.DEPTGROUP);
				mgrPerson.setDeptCode(deptCode);
				mgrPerson.setA_deptCode(a_deptCode);
				mgrPerson.setCmpCode(cmpCode);
				mgrPersons.add(mgrPerson);
			}
		}
		return mgrPersons;
	}

	@Override
	public boolean qualifiedGroupMgr(String employeeId, String type) {
		List<HRGroupMgr> mgrs = userService.getGroupMgrByUserName(employeeId,
				type);
		if (mgrs != null && mgrs.size() > 0) {
			return true;
		}
		return false;
	}

	@Override
	public PersonDetail getCenterEmployeeByCmpCode(String cmpCode) {
		PersonDetail person = new PersonDetail();
		// 获取的是主岗位的信息
		EmployeePos employeePos = userService
				.getCenterEmployeeByCmpCode(cmpCode);
		if (employeePos == null) {
			return null;
		}
		SystemUsers user = userService.getUsersByName(employeePos
				.getEmployeeid());
		SystemGroups groups = new SystemGroups();
		// 部门代码
		String deptCode = employeePos.getDeptcode();
		// 中心代码
		String a_deptCode = employeePos.getA_deptcode();

		if (user != null && employeePos != null) {
			if (!StringUtils.isEmpty(deptCode)) {
				groups = organService.getGroupsByDeptCode(deptCode, a_deptCode,
						cmpCode, GroupType.DEPTGROUP);
			} else {
				if (!StringUtils.isEmpty(a_deptCode)) {
					groups = organService.getGroupsByDeptCode(deptCode,
							a_deptCode, cmpCode, GroupType.CENTERGROUP);
				} else {
					groups = organService.getGroupsByDeptCode(deptCode,
							a_deptCode, cmpCode, GroupType.CMPGROUP);
				}
			}

			HRPosition position = organService.getPositionByPostCode(
					employeePos.getPostcode(), deptCode, cmpCode);

			if (groups != null && position != null) {
				person = this.buildPersonDetail(person, groups, user,
						employeePos, position);
				person.setDeptCode(groups.getDepcod());
				person.setA_deptCode(groups.getA_depcod());
				person.setCompPhone(user.getWorkPhone());
			}
		}
		return person;
	}

	@Override
	public PersonDetail getCenterFEmployeeByCmpCode(String cmpCode) {
		PersonDetail person = new PersonDetail();
		// 获取的是主岗位的信息
		EmployeePos employeePos = userService
				.getCenterFEmployeeByCmpCode(cmpCode);
		if (employeePos == null) {
			return null;
		}
		SystemUsers user = userService.getUsersByName(employeePos
				.getEmployeeid());
		SystemGroups groups = new SystemGroups();
		// 部门代码
		String deptCode = employeePos.getDeptcode();
		// 中心代码
		String a_deptCode = employeePos.getA_deptcode();

		if (user != null && employeePos != null) {
			if (!StringUtils.isEmpty(deptCode)) {
				groups = organService.getGroupsByDeptCode(deptCode, a_deptCode,
						cmpCode, GroupType.DEPTGROUP);
			} else {
				if (!StringUtils.isEmpty(a_deptCode)) {
					groups = organService.getGroupsByDeptCode(deptCode,
							a_deptCode, cmpCode, GroupType.CENTERGROUP);
				} else {
					groups = organService.getGroupsByDeptCode(deptCode,
							a_deptCode, cmpCode, GroupType.CMPGROUP);
				}
			}

			HRPosition position = organService.getPositionByPostCode(
					employeePos.getPostcode(), deptCode, cmpCode);

			if (groups != null && position != null) {
				person = this.buildPersonDetail(person, groups, user,
						employeePos, position);
				person.setDeptCode(groups.getDepcod());
				person.setA_deptCode(groups.getA_depcod());
				person.setCompPhone(user.getWorkPhone());
			}
		}
		return person;
	}

	/**
	 * add by Cao_Shengyong 2014-03-25 用于获取用户对应岗位上的相关信息
	 */
	public EmployeePos loadWideEmployeePos(String employeeId, String postcode) {
		// PersonDetail person = new PersonDetail();
		// SystemGroups groups =
		// organService.loadGroupsById(Integer.valueOf(deptId));
		SystemUsers user = userService.getUsersByName(employeeId);

		EmployeePos employeePos = userService.getEmployeePosByEmpId(
				user.getDepcod(), user.getCmpcod(), employeeId, postcode);

		return employeePos;
	}

	public PersonDetail getMgrPersonByIdAndPostCode(String employeeId,
			String postcode, String groupType) {
		PersonDetail person = new PersonDetail();
		EmployeePos employeePos = loadWideEmployeePos(employeeId, postcode);
		if (employeePos == null) {
			return null;
		}
		boolean goon = true;
		if (groupType.equalsIgnoreCase(GroupType.CENTERGROUP.name())) {
			if (employeePos.getMgA_DeptFlg() == 1) {
				goon = false;
			}
		} else if (groupType.equalsIgnoreCase(GroupType.CMPGROUP.name())) {
			if (employeePos.getMgCentFlg() == 1) {
				goon = false;
			}
		} else if (groupType.equalsIgnoreCase(GroupType.DEPTGROUP.name())) {
			if (employeePos.getMgDeptFlg() == 1) {
				goon = false;
			}
		}
		if (goon) {
			person = getMgrPersonByIdAndPostCode(
					employeePos.getMgremployeeid(),
					employeePos.getMgrpostcode(), groupType);
		} else {
			person.setEmployeeId(employeePos.getEmployeeid());
		}
		return person;
	}

	public boolean isCenterFHead(String employeeid, String postcode) {
		boolean bRet = false;
		EmployeePos employeePos = userService.getEmployeePosByEmpId(employeeid,
				postcode);
		if (employeePos.isTopFmgr())
			bRet = true;
		return bRet;
	}

	public boolean isTopMgr(PersonDetail personDetail) {
		boolean bRet = false;
		EmployeePos employeePos = userService.getEmployeePosByEmpId(
				personDetail.getEmployeeId(), personDetail.getPostCode());
		if (employeePos.isTopmgr())
			bRet = true;
		return bRet;
	}

	@Override
	public boolean canUpAttach(String groupid) {
		List<SystemGroups> list = organService.getCanUpAttachOrg(groupid);
		if (list != null && list.size() > 0)
			return true;
		else {
			return false;
		}
	}

	@Override
	public boolean getJYJurisdiction(String empid) {
		return organService.getJYJurisdiction(empid) > 0 ? true : false;
	}
}
