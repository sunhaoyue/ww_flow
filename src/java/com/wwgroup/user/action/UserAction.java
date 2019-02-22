package com.wwgroup.user.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.wwgroup.common.Page;
import com.wwgroup.common.action.BaseAjaxAction;
import com.wwgroup.common.vo.VOUtils;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.service.FlowService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.service.OrganService;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

@SuppressWarnings("serial")
public class UserAction extends BaseAjaxAction {

	private SystemUsers users = new SystemUsers();

	private UserService userService;

	private OrganService organService;

	private PersonService personService;
	
	private FlowService flowService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setOrganService(OrganService organService) {
		this.organService = organService;
	}

	@Override
	public Object getModel() {
		return this.users;
	}

	public SystemUsers getUsers() {
		return users;
	}

	public void setUsers(SystemUsers users) {
		this.users = users;
	}

	public FlowService getFlowService() {
		return flowService;
	}

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}

	// 根据组织节点查询包含的用户列表
	@SuppressWarnings("unchecked")
	public String getUsersByGroup() {
		int pageNo = 1;
		int pageSize = 10;
		int start = 0;
		String json = "";
		String param = this.servletRequest.getParameter("pageNo");
		if (param != null) {
			pageNo = Integer.parseInt(param);
		}
		param = this.servletRequest.getParameter("pageSize");
		if (param != null) {
			pageSize = Integer.parseInt(param);
			start = (pageNo - 1) * pageSize + 1;
		}
		String groupId = this.servletRequest.getParameter("id");// 组织ID
		SystemGroups groups = organService.loadGroupsById(Integer.valueOf(groupId));

		List<PersonDetail> result = new ArrayList<PersonDetail>();

		if (StringUtils.isNotEmpty(groupId)) {
			Page page = userService.getUsersByGroupWithPage(Integer.valueOf(groupId), start, pageSize);
			List<PersonDetail> list = page.getResult();
			if (list != null) {
				for (PersonDetail personDetail : list) {
					PersonDetail person = new PersonDetail();
					BeanUtils.copyProperties(personDetail, person);
					String fullName = personDetail.getName();
					String name = fullName.substring(0, fullName.indexOf("("));
					person.setName(name);
					if (groups != null) {
						person.setDeptId(String.valueOf(groups.getGroupID()));
						person.setDeptName(groups.getGroupName());
						person.setDeptPath(groups.getOrgPath());
						this.splitDeptPath(person, groups.getOrgPath());
					}
					result.add(person);
				}
				page.setResult(result);
			}
			json = VOUtils.getJsonDataFromPage(page, PersonDetail.class);
		}
		this.createJSonData(json);
		return AJAX;
	}

	public String getQianChenDetailByUser() {
		String json = "";
		String employeeId = (String) this.servletRequest.getParameter("employeeId");
		if (StringUtils.isNotEmpty(employeeId)) {
			PersonDetail personDetail = this.personService.loadWidePersonDetail(employeeId);
			json = VOUtils.getJsonData(personDetail);
		}
		createJSonData(json);
		return AJAX;
	}

	// 获取内部会办/会办人员列表
	public String loadJoinUsers() {
		String json = "";
		List<PersonDetail> result = new ArrayList<PersonDetail>();
		String innerhuibanIds = (String) this.servletRequest.getParameter("innerhuibanIds");
		String huibanpersons = (String) this.servletRequest.getParameter("huibanpersons");
		String flowId = (String) this.servletRequest.getParameter("flowId");
		// 内部会办人员信息(如果有内部会办，先查询内部会办列表，会办人员信息先不管)
		if (StringUtils.isNotEmpty(innerhuibanIds)) {
			String[] mixedValues = StringUtils.split(innerhuibanIds, ";");
			if (mixedValues != null) {
				for (int i = 0; i < mixedValues.length; i++) {
					
					//如该人已会办，则不显示在列表中
					String employeeId = mixedValues[i].split(",")[1];
					if (flowService.finishInnerJointSign(Long.parseLong(flowId),employeeId)) {
						continue;
					}
					
					String[] mixedValue = StringUtils.split(mixedValues[i], ",");
					SystemUsers user = userService.getUsersByName(mixedValue[1]);
					PersonDetail person = new PersonDetail();
					String realName = user.getFullName().substring(0, user.getFullName().indexOf("("));
					person.setEmployeeId(user.getUserName());
					person.setName(realName);
					result.add(person);
				}
			}
			json = VOUtils.getJsonDataFromCollection(result);
			createJSonData(json);
			return AJAX;
		}
		// 会办人员信息
		if (StringUtils.isNotEmpty(huibanpersons)) {
			String[] mixedValues = StringUtils.split(huibanpersons, ";");
			if (mixedValues != null) {
				for (int i = 0; i < mixedValues.length; i++) {
					String[] mixedValue = StringUtils.split(mixedValues[i], ",");
					PersonDetail person = new PersonDetail();
					person.setEmployeeId(mixedValue[0]);
					person.setName(mixedValue[1]);
					result.add(person);
				}
			}
			json = VOUtils.getJsonDataFromCollection(result);
			createJSonData(json);
			return AJAX;
		}
		return AJAX;
	}

	/**
	 * 将组织拆分为单位+中心+部门，供前台显示
	 * 
	 * @param deptPath
	 */
	private void splitDeptPath(PersonDetail person, String deptPath) {
		if (deptPath != null) {
			String[] depts = deptPath.split("/");
			for (int i = 0; i < depts.length; i++) {
				String dept = depts[i];
				if (i == 1) {
					person.setDept1(dept);
				}
				if (i == 2) {
					person.setDept2(dept);
				}
				if (i == 3) {
					person.setDept3(dept);

				}
			}
		}
	}

	public static void main(String[] args) {
		String fullName = "王菲(90905001)";
		String userName = fullName.substring(0, fullName.indexOf("("));
		System.out.println(userName);
	}

}
