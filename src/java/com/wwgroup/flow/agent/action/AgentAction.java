package com.wwgroup.flow.agent.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.common.action.BaseAjaxAction;
import com.wwgroup.common.vo.VOUtils;
import com.wwgroup.flow.bo.AgentPerson;
import com.wwgroup.flow.bo.AgentReason;
import com.wwgroup.flow.bo.AgentRelation;
import com.wwgroup.flow.bo.AgentType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.service.FlowAgentService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.HRPosition;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.service.OrganService;
import com.wwgroup.organ.vo.OrganVo;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

@SuppressWarnings("serial")
public class AgentAction extends BaseAjaxAction {

	private AgentRelation agent = new AgentRelation();

	private FlowAgentService agentService;

	private PersonService personService;

	private UserService userService;

	private OrganService organService;

	public void setAgentService(FlowAgentService agentService) {
		this.agentService = agentService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setOrganService(OrganService organService) {
		this.organService = organService;
	}

	public AgentRelation getAgent() {
		return agent;
	}

	public void setAgent(AgentRelation agent) {
		this.agent = agent;
	}

	@Override
	public Object getModel() {
		return this.agent;
	}

	/**
	 * 查询所有代理记录
	 * 
	 * @return
	 */
	public String findAllAgents() {
		String json = "";

		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();

		AgentRelation agentRelation = agentService.loadAgent(employeeId);
		List<AgentPerson> agentPersonList = agentService.loadAgentPersons(agentRelation);
		if (agentPersonList != null) {
			for (AgentPerson agentPerson : agentPersonList) {
				// 申请人姓名
				SystemUsers actualUser = userService.getUsersByName(employeeId);
				if (actualUser != null) {
					agentPerson.setActualUserName(actualUser.getFullName());
				}
				SystemUsers agentUser = userService.getUsersByName(agentPerson.getAgentEmployeeId());
				if (agentUser != null) {
					agentPerson.setAgentUserName(agentUser.getFullName());
				}
				if (agentPerson.getType() != null) {
					agentPerson.setAgentType(agentPerson.getType().name());
				}
				if (agentRelation.getReason() != null) {
					agentPerson.setAgentReason(agentRelation.getReason().name());
				}
			}
		}
		json = VOUtils.getJsonDataFromCollection(agentPersonList);
		this.createJSonData(json);
		int overdue = Calendar.getInstance().getTimeInMillis() > agentRelation.getEndDate().getTime() + 24*60*60*1000 ? 1 : 0;
		this.servletRequest.setAttribute("overdue", overdue);
		return AJAX;
	}
	
	/**
	 * 判断代理人是否过期
	 * 
	 * @return
	 */
	public String overdue() {

		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		AgentRelation agentRelation = agentService.loadAgent(employeeId);
		int overdue = Calendar.getInstance().getTimeInMillis() > agentRelation.getEndDate().getTime() + 24*60*60*1000 ? 1 : 0;
		this.createJSonData(String.valueOf(overdue));
		return AJAX;
	}

	/**
	 * 新增代理人
	 * 
	 * @return
	 */
	public String addAgent() {
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		if (StringUtils.isNotEmpty(employeeId)) {

			PersonDetail personDetail = personService.loadWidePersonDetail(employeeId);
			String deptPath = personDetail.getDeptPath();
			splitDeptPath(deptPath);
			this.servletRequest.setAttribute("personDetail", personDetail);
			this.servletRequest.setAttribute("personCompanyId", organService.filterToCompanyLevel(Integer.parseInt(personDetail.getDeptId())));
		}
		agent = agentService.loadAgent(employeeId);
		if (agent != null && agent.getId() > 0) {
			agent.setAgentReason(agent.getReason().name());
			agent.setOtherReason(agent.getReason().getRealName());

			// 日期回显
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String startDate = sdf.format(agent.getStartDate());
			String endDate = sdf.format(new Date(agent.getEndDate().getTime() - 24*60*60*1000));
			this.servletRequest.setAttribute("startDate", startDate);
			this.servletRequest.setAttribute("endDate", endDate);
		}
		return "addAgent";
	}

	/**
	 * 提交代理信息
	 * 
	 * @return
	 */
	public String submitAgent() {
		
		agent.setEndDate(new Date(agent.getEndDate().getTime() + 24*60*60*1000));

		// 代理原因
		String agentReason = this.servletRequest.getParameter("agentReason");
		String realName = this.servletRequest.getParameter("realName");
		String id = this.servletRequest.getParameter("agentId");
		if (StringUtils.isNotEmpty(realName)) {
			agent.setReason(AgentReason.Other.realName(realName));
		}
		else {
			agent.setReason(AgentReason.valueOf(agentReason));
		}

		AgentRelation agentRelation = agentService.loadAgent(agent.getActualEmployeeId());
		if (agentRelation == null) {
			agentService.saveAgent(agent);
		}
		else {
			agent.setId(Long.valueOf(id));
			agentService.updateAgent(agent);
		}

		String json = VOUtils.getJsonData(agent);
		createJSonData(json);
		return AJAX;

	}

	/**
	 * 添加代理人
	 */
	@SuppressWarnings("unchecked")
	public String addAgentPerson() {
		String id = this.servletRequest.getParameter("id");
		String data1 = this.servletRequest.getParameter("modifiedData1");
		String data2 = this.servletRequest.getParameter("modifiedData2");
		String actualUserName = super.getLoginUser().getUserName();
		List<AgentPerson> list1 = null;
		List<AgentPerson> list2 = null;
		// 岗位代理人列表
		if (StringUtils.isNotEmpty(data1)) {
			list1 = VOUtils.getBeanListFromJsonData(data1, AgentPerson.class);
		}
		// 流程代理人列表
		if (StringUtils.isNotEmpty(data2)) {
			list2 = VOUtils.getBeanListFromJsonData(data2, AgentPerson.class);
		}
		AgentRelation agentRelation = new AgentRelation();
		agentRelation.setId(Long.valueOf(id));
		agentRelation.setActualUserId(String.valueOf(userService.getUsersByName(actualUserName).getUserID()));
		//agentRelation.setStartDate(agent.getStartDate());
		//agentRelation.setEndDate(agent.getEndDate());
		
		if (list1 != null && list1.size() > 0) {
			for (AgentPerson agentPerson : list1) {
				if (agentPerson.getAgentType().equals(AgentType.PositionAgent.name())) {
					agentPerson.getType().selectedPostCode(agentPerson.getAgentPostCode());
				}
				agentService.addAgentPerson(agentRelation, agentPerson);
			}
		}
		if (list2 != null && list2.size() > 0) {
			for (AgentPerson agentPerson : list2) {
				if (agentPerson.getAgentType().equals(AgentType.FlowAgent.name())) {
					agentPerson.setType(AgentType.FlowAgent);
					// 设置代理表单
					String selectedFlow = agentPerson.getFlows();
					Map<String, List<String>> selectedFlowMap = new HashMap<String, List<String>>();
					if (StringUtils.isNotEmpty(selectedFlow)) {
						List<String> lists = new ArrayList<String>();
						String[] selectedFlows = StringUtils.split(selectedFlow, ";");
						if (selectedFlows != null) {
							for (String flow : selectedFlows) {
								String[] flows = StringUtils.split(flow, ",");
								if (flows != null) {
									for (int i = 0; i < flows.length; i++) {
										lists.add(flows[i]);
									}
								}
							}
							selectedFlowMap.put("type1 ", lists);
						}
					}
					agentPerson.setSelectedFlow(selectedFlowMap);
				}
				agentService.addAgentPerson(agentRelation, agentPerson);
			}
		}
		return AJAX;
	}

	/**
	 * 加载代理人信息
	 * 
	 * @return
	 */
	public String loadAgentPerson() {
		String actualUserName = super.getLoginUser().getUserRealName();

		List<AgentPerson> list = agentService.loadAgentPersons(agent);
		if (list != null && list.size() > 0) {
			for (AgentPerson agentPerson : list) {
				agentPerson.setAgentType(agentPerson.getType().name());

				agentPerson.setActualUserName(actualUserName);
				SystemUsers agentUser = userService.getUsersByName(agentPerson.getAgentEmployeeId());
				if (agentUser != null) {
					agentPerson.setAgentUserName(agentUser.getFullName());
				}
				String deptId = agentPerson.getAgentDeptId();
				if (StringUtils.isNotEmpty(deptId)) {
					SystemGroups group = organService.loadGroupsById(Integer.valueOf(deptId));
					if (group != null) {
						agentPerson.setAgentDept(group.getOrgPath());
					}
				}
				//计算流程数
				if (agentPerson.getSelectedFlow()!=null){
					List<String> flowCount = agentPerson.getSelectedFlow().get("type1 ");
					if(flowCount != null && flowCount.size() > 0){
						agentPerson.setFlowCount(flowCount.size());
					}
				}
			}
			String json = VOUtils.getJsonDataFromCollection(list);
			createJSonData(json);
		}
		return AJAX;
	}

	/**
	 * 删除代理人信息
	 * 
	 * @return
	 */
	public String deleteAgentPerson() {
		String agentId = this.servletRequest.getParameter("agentId");
		String agentpersonId = this.servletRequest.getParameter("agentpersonId");
		AgentRelation agentRelation = new AgentRelation();
		agentRelation.setId(Long.valueOf(agentId));
		AgentPerson agentPerson = new AgentPerson();
		agentPerson.setId(Long.valueOf(agentpersonId));
		agentService.deleteAgentPerson(agentRelation, agentPerson);
		return AJAX;
	}
	
	/**
	 * 批量删除代理人信息
	 * 
	 * @return
	 */
	public String deleteAgent() {
		String params = this.servletRequest.getParameter("params");
		if (StringUtils.isNotEmpty(params)) {
			String[] records = StringUtils.split(params, ",");
			for (String record : records) {
				String[] p = record.split("[|]");
				String agentId = p[0];
				String agentpersonId = p[1];
				AgentRelation agentRelation = new AgentRelation();
				agentRelation.setId(Long.valueOf(agentId));
				AgentPerson agentPerson = new AgentPerson();
				agentPerson.setId(Long.valueOf(agentpersonId));
				agentService.deleteAgentPerson(agentRelation, agentPerson);
			}
		}
		return AJAX;
	}

	/**
	 * 更新代理人信息
	 * 
	 * @return
	 */
	public String updateAgent() {
		// 代理原因
		String agentReason = this.servletRequest.getParameter("agentReason");
		String realName = this.servletRequest.getParameter("realName");
		if (StringUtils.isNotEmpty(realName)) {
			agent.setReason(AgentReason.Other.realName(realName));
		}
		else {
			agent.setReason(AgentReason.valueOf(agentReason));
		}

		agentService.updateAgent(agent);
		return AJAX;
	}

	/**
	 * 流程展现（和华炎对接）
	 * 
	 * @return
	 */
	public String getExtraFlows() {
		String id = this.servletRequest.getParameter("id");// 父节点的ID
		Map<String, String> extraFlowCategories = new HashMap<String, String>();
		Map<String, String> extraFlowEntries = new HashMap<String, String>();
		List<OrganVo> result = new ArrayList<OrganVo>();
		if (StringUtils.isNotEmpty(id) && Integer.valueOf(id) != 0) {
			extraFlowCategories = agentService.extraGetFlowCategories(id);
			extraFlowEntries = agentService.extraGetFlowEntries(id);
		}
		else {
			extraFlowCategories = agentService.extraGetFlowCategories();
		}
		if (extraFlowCategories.keySet().size() > 0) {
			for (String key : extraFlowCategories.keySet()) {
				String name = extraFlowCategories.get(key);
				OrganVo organVo = new OrganVo();
				organVo.setParent(true);
				organVo.setId(key);
				organVo.setName(name);
				result.add(organVo);
			}
		}
		if (extraFlowEntries.keySet().size() > 0) {
			for (String key : extraFlowEntries.keySet()) {
				String name = extraFlowEntries.get(key);
				OrganVo organVo = new OrganVo();
				organVo.setParent(false);
				organVo.setId(key);
				organVo.setName(name);
				result.add(organVo);
			}
		}
		this.createJSonData(VOUtils.getJsonDataFromCollection(result));
		return AJAX;
	}

	/**
	 * 获得当前操作人的信息
	 */
	// edit by Cao_Shengyong 2014-08-14
	// 主要修改获取顺序，改为单位－－中心－－部门
	// 且增加判断：如果已获取的信息中的岗位已存在，则不用再往列表中增加
	/*public String loadOperatorInfo(){
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		List<AgentRelation> result = new ArrayList<AgentRelation>();
		List<String> posCodList = new ArrayList<String>();
		// 单位主管的职位列表
		List<HRPosition> list3 = organService.getAssistPositionsByUser(employeeId, GroupType.CMPGROUP);
		if (list3 != null && list3.size() > 0) {
			for (HRPosition position : list3) {
				AgentRelation agentRelation = new AgentRelation();
				agentRelation.setSelectedPostCode(position.getPosicod());
				agentRelation.setSelectedPostName(position.getPositionName());
				SystemGroups group = organService.getGroupsByDeptCode(null, null, position.getCmpcod(),
						GroupType.CMPGROUP);
				if (group != null) {
					agentRelation.setSelectedDeptName(group.getGroupName());
					String orgPath = group.getOrgPath();
					agentRelation.setDeptPath(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				posCodList.add(agentRelation.getSelectedPostCode());
				result.add(agentRelation);
			}
		}
		// 中心主管的职位列表
		List<HRPosition> list2 = organService.getAssistPositionsByUser(employeeId, GroupType.CENTERGROUP);
		if (list2 != null && list2.size() > 0) {
			for (HRPosition position : list2) {
				AgentRelation agentRelation = new AgentRelation();
				agentRelation.setSelectedPostCode(position.getPosicod());
				agentRelation.setSelectedPostName(position.getPositionName());
				SystemGroups group = organService.getGroupsByDeptCode(null, position.getA_depcode(), position
						.getCmpcod(), GroupType.CENTERGROUP);
				if (group != null) {
					agentRelation.setSelectedDeptName(group.getGroupName());
					String orgPath = group.getOrgPath();
					agentRelation.setDeptPath(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				if (!posCodList.contains(agentRelation.getSelectedPostCode())){
					posCodList.add(agentRelation.getSelectedPostCode());
					result.add(agentRelation);
				}
			}
		}
		// 部门主管的职位列表
		List<HRPosition> list1 = organService.getAssistPositionsByUser(employeeId, GroupType.DEPTGROUP);
		if (list1 != null && list1.size() > 0) {
			for (HRPosition position : list1) {
				AgentRelation agentRelation = new AgentRelation();
				agentRelation.setSelectedPostCode(position.getPosicod());
				agentRelation.setSelectedPostName(position.getPositionName());
				SystemGroups group = organService.getGroupsByDeptCode(position.getDepcod(), position.getA_depcode(),
						position.getCmpcod(), GroupType.DEPTGROUP);
				if (group != null) {
					agentRelation.setSelectedDeptName(group.getGroupName());
					String orgPath = group.getOrgPath();
					agentRelation.setDeptPath(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				if (!posCodList.contains(agentRelation.getSelectedPostCode())){
					posCodList.add(agentRelation.getSelectedPostCode());
					result.add(agentRelation);
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
		List<AgentRelation> result = new ArrayList<AgentRelation>();

		// 部门主管的职位列表
		List<HRPosition> list1 = organService.getAssistPositionsByUser(employeeId, GroupType.DEPTGROUP);
		if (list1 != null && list1.size() > 0) {
			for (HRPosition position : list1) {
				AgentRelation agentRelation = new AgentRelation();
				agentRelation.setSelectedPostCode(position.getPosicod());
				agentRelation.setSelectedPostName(position.getPositionName());
				SystemGroups group = organService.getGroupsByDeptCode(position.getDepcod(), position.getA_depcode(),
						position.getCmpcod(), GroupType.DEPTGROUP);
				if (group != null) {
					agentRelation.setSelectedDeptName(group.getGroupName());
					String orgPath = group.getOrgPath();
					agentRelation.setDeptPath(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				result.add(agentRelation);
			}
		}
		// 中心主管的职位列表
		List<HRPosition> list2 = organService.getAssistPositionsByUser(employeeId, GroupType.CENTERGROUP);
		if (list2 != null && list2.size() > 0) {
			for (HRPosition position : list2) {
				AgentRelation agentRelation = new AgentRelation();
				agentRelation.setSelectedPostCode(position.getPosicod());
				agentRelation.setSelectedPostName(position.getPositionName());
				SystemGroups group = organService.getGroupsByDeptCode(null, position.getA_depcode(), position
						.getCmpcod(), GroupType.CENTERGROUP);
				if (group != null) {
					agentRelation.setSelectedDeptName(group.getGroupName());
					String orgPath = group.getOrgPath();
					agentRelation.setDeptPath(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				result.add(agentRelation);
			}
		}
		// 单位主管的职位列表
		List<HRPosition> list3 = organService.getAssistPositionsByUser(employeeId, GroupType.CMPGROUP);
		if (list3 != null && list3.size() > 0) {
			for (HRPosition position : list3) {
				AgentRelation agentRelation = new AgentRelation();
				agentRelation.setSelectedPostCode(position.getPosicod());
				agentRelation.setSelectedPostName(position.getPositionName());
				SystemGroups group = organService.getGroupsByDeptCode(null, null, position.getCmpcod(),
						GroupType.CMPGROUP);
				if (group != null) {
					agentRelation.setSelectedDeptName(group.getGroupName());
					String orgPath = group.getOrgPath();
					agentRelation.setDeptPath(orgPath.substring(orgPath.indexOf("/") + 1));
				}
				result.add(agentRelation);
			}
		}
		String json = VOUtils.getJsonDataFromCollection(result);
		this.createJSonData(json);
		return AJAX;
	}
	
}
