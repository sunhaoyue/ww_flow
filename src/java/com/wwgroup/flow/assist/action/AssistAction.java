package com.wwgroup.flow.assist.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.common.Page;
import com.wwgroup.common.action.BaseAjaxAction;
import com.wwgroup.common.vo.VOUtils;
import com.wwgroup.flow.bo.AssistRelation;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.service.FlowAssistService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRPosition;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.service.OrganService;

@SuppressWarnings("serial")
public class AssistAction extends BaseAjaxAction {

	private AssistRelation assist = new AssistRelation();

	private FlowAssistService assistService;

	private OrganService organService;

	private PersonService personService;

	public void setAssistService(FlowAssistService assistService) {
		this.assistService = assistService;
	}

	public void setOrganService(OrganService organService) {
		this.organService = organService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public AssistRelation getAssist() {
		return assist;
	}

	public void setAssist(AssistRelation assist) {
		this.assist = assist;
	}

	@Override
	public Object getModel() {
		return this.assist;
	}

	/**
	 * 查询所有助理记录
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String findAllAssists() {

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

		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();

		List<AssistRelation> result = new ArrayList<AssistRelation>();
		Page page = assistService.findAll(employeeId, start, pageSize);
		List<AssistRelation> list = page.getResult();
		if (list != null) {
			for (AssistRelation assistRelation : list) {
				if (assistRelation.isAllowReceiveMail()) {
					assistRelation.setAllowReceive(1);
				}
				if (assistRelation.isAllowAssignPerson()) {
					assistRelation.setAllowAssign(1);
				}
				result.add(assistRelation);
			}
			page.setResult(result);
		}
		json = VOUtils.getJsonDataFromPage(page, AssistRelation.class);
		this.createJSonData(json);
		return AJAX;
	}

	/**
	 * 新增助理人
	 * 
	 * @return
	 */
	public String addAssist() {
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();

		// 加载当前用户的相关信息
		PersonDetail personDetail = personService.loadWidePersonDetail(employeeId);
		this.servletRequest.setAttribute("personDetail", personDetail);
		this.servletRequest.setAttribute("personCompanyId", organService.filterToCompanyLevel(Integer.parseInt(personDetail.getDeptId())));
		return "addAssist";
	}

	/**
	 * 提交助理人信息
	 * 
	 * @return
	 */
	public String submitAssist() {
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();

		assist.setEmployeeId(employeeId);

		// 是否允许查看
		if (this.assist.getAllowReceive() == 1) {
			assist.setAllowReceiveMail(true);
		}
		// 是否允许指派
		if (this.assist.getAllowAssign() == 1) {
			assist.setAllowAssignPerson(true);
		}

		assistService.saveAssist(assist);
		return AJAX;
	}

	/**
	 * 删除代理人信息
	 * 
	 * @return
	 */
	public String deleteAssist() {
		String assistIds = this.servletRequest.getParameter("ids");
		if (StringUtils.isNotEmpty(assistIds)) {
			String[] ids = StringUtils.split(assistIds, ",");
			for (String id : ids) {
				assistService.removeAssist(Long.valueOf(id));
			}
		}
		return AJAX;
	}

	/**
	 * 加载代理人信息
	 * 
	 * @return
	 */
	public String loadAssist() {
		String assistId = this.servletRequest.getParameter("assistId");
		if (StringUtils.isNotEmpty(assistId)) {
			assist = assistService.loadAssist(Long.valueOf(assistId));
		}
		this.servletRequest.setAttribute("personCompanyId", organService.filterToCompanyLevel((int)assist.getSelectedDeptId()));
		return "editAssist";
	}

	/**
	 * 加载代理人信息
	 * 
	 * @return
	 */
	public String updateAssist() {
		// 是否允许查看
		if (this.assist.getAllowReceive() == 1) {
			assist.setAllowReceiveMail(true);
		} else {
			assist.setAllowReceiveMail(false);
		}
		// 是否允许指派
		if (this.assist.getAllowAssign() == 1) {
			assist.setAllowAssignPerson(true);
		} else {
			assist.setAllowAssignPerson(false);
		}
		assistService.updateAssist(assist);
		return AJAX;
	}

	/**
	 * 获得当前操作人的信息
	 */
	/*public String loadOperatorInfo() {
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		List<AssistRelation> result = new ArrayList<AssistRelation>();
		List<String> posCodList = new ArrayList<String>();
		
		// 单位主管的职位列表
		List<HRPosition> list2 = organService.getAssistPositionsByUser(employeeId, GroupType.CMPGROUP);
		if (list2 != null && list2.size() > 0) {
			for (HRPosition position2 : list2) {
				AssistRelation assist2 = new AssistRelation();
				assist2.setSelectedPostCode(position2.getPosicod());
				assist2.setSelectedPostName(position2.getPositionName());
				SystemGroups group = organService.getGroupsByDeptCode(null, null, position2.getCmpcod(),
						GroupType.CMPGROUP);
				if (group != null) {
					assist2.setSelectedDeptId(group.getGroupID());
					assist2.setSelectedDeptName(group.getGroupName());
					String orgPath = group.getOrgPath();
					assist2.setDeptPath(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				if (!posCodList.contains(assist2.getSelectedPostCode())){
					posCodList.add(assist2.getSelectedPostCode());
					result.add(assist2);
				}
			}
		}
		
		// 中心主管的职位列表
		List<HRPosition> list1 = organService.getAssistPositionsByUser(employeeId, GroupType.CENTERGROUP);
		if (list1 != null && list1.size() > 0) {
			for (HRPosition position1 : list1) {
				AssistRelation assist1 = new AssistRelation();
				assist1.setSelectedPostCode(position1.getPosicod());
				assist1.setSelectedPostName(position1.getPositionName());
				SystemGroups group = organService.getGroupsByDeptCode(null, position1.getA_depcode(), position1
						.getCmpcod(), GroupType.CENTERGROUP);
				if (group != null) {
					assist1.setSelectedDeptId(group.getGroupID());
					assist1.setSelectedDeptName(group.getGroupName());
					String orgPath = group.getOrgPath();
					assist1.setDeptPath(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				if (!posCodList.contains(assist1.getSelectedPostCode())){
					posCodList.add(assist1.getSelectedPostCode());
					result.add(assist1);
				}
			}
		}
		
		String json = VOUtils.getJsonDataFromCollection(result);
		this.createJSonData(json);
		return AJAX;
	}*/
	public String loadOperatorInfo() {
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		List<AssistRelation> result = new ArrayList<AssistRelation>();
		// 中心主管的职位列表
		List<HRPosition> list1 = organService.getAssistPositionsByUser(employeeId, GroupType.CENTERGROUP);
		if (list1 != null && list1.size() > 0) {
			for (HRPosition position1 : list1) {
				AssistRelation assist1 = new AssistRelation();
				assist1.setSelectedPostCode(position1.getPosicod());
				assist1.setSelectedPostName(position1.getPositionName());
				SystemGroups group = organService.getGroupsByDeptCode(null, position1.getA_depcode(), position1
						.getCmpcod(), GroupType.CENTERGROUP);
				if (group != null) {
					assist1.setSelectedDeptId(group.getGroupID());
					assist1.setSelectedDeptName(group.getGroupName());
					String orgPath = group.getOrgPath();
					assist1.setDeptPath(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				result.add(assist1);
			}
		}
		// 单位主管的职位列表
		List<HRPosition> list2 = organService.getAssistPositionsByUser(employeeId, GroupType.CMPGROUP);
		if (list2 != null && list2.size() > 0) {
			for (HRPosition position2 : list2) {
				AssistRelation assist2 = new AssistRelation();
				assist2.setSelectedPostCode(position2.getPosicod());
				assist2.setSelectedPostName(position2.getPositionName());
				SystemGroups group = organService.getGroupsByDeptCode(null, null, position2.getCmpcod(),
						GroupType.CMPGROUP);
				if (group != null) {
					assist2.setSelectedDeptId(group.getGroupID());
					assist2.setSelectedDeptName(group.getGroupName());
					String orgPath = group.getOrgPath();
					assist2.setDeptPath(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				result.add(assist2);
			}
		}
		String json = VOUtils.getJsonDataFromCollection(result);
		this.createJSonData(json);
		return AJAX;
	}
}
