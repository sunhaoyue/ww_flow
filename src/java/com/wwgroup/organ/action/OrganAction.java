package com.wwgroup.organ.action;

import java.util.ArrayList;
import java.util.List;



import com.wwgroup.common.action.BaseAjaxAction;
import com.wwgroup.common.vo.VOUtils;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.service.FlowService;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.service.OrganService;
import com.wwgroup.organ.vo.OrganVo;
import com.wwgroup.user.bo.EmployeePos;
import com.wwgroup.user.service.UserService;
import org.apache.commons.lang3.StringUtils;


@SuppressWarnings({ "serial", "unused" })
public class OrganAction extends BaseAjaxAction {

	private OrganService organService;

	private UserService userService;

	private FlowService flowService;

	private final static int ROOT_GROUPID = 701;

	public void setOrganService(OrganService organService) {
		this.organService = organService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}

	private OrganVo organVo = new OrganVo();

	@Override
	public Object getModel() {
		return this.organVo;
	}

	public OrganVo getOrganVo() {
		return organVo;
	}

	public void setOrganVo(OrganVo organVo) {
		this.organVo = organVo;
	}

	public String loadOrgansByParent() {

		String id = this.servletRequest.getParameter("id");// 父节点的ID
		String type = this.servletRequest.getParameter("type");
		String deptId = this.servletRequest.getParameter("deptId");
		String contentType = this.servletRequest.getParameter("contentType"); // 类别，1：体系内，2：地方到总部
		String leader = this.servletRequest.getParameter("leader"); // 核决准管
		String assignDeptId = this.servletRequest.getParameter("assignDeptId");
		String flowType = this.servletRequest.getParameter("flowType"); // 1：签呈
																		// 2：内联

		// 获取当前登录人
		String employeeId = (String) this.getSession().get("employeeId");
		EmployeePos employeePos = userService.getEmployeePosByEmpId(employeeId);
		SystemGroups curSystemGroups = null;
		if (null != employeePos) {
			curSystemGroups = organService.getGroupsByDeptCode(
					employeePos.getDeptcode(), employeePos.getA_deptcode(),
					employeePos.getCmpcod(), GroupType.CMPGROUP);
		}

		// 会办单位或者抄送单位
		String deptIds = this.servletRequest.getParameter("deptIds");

		List<Integer> validGroupId = new ArrayList<Integer>();
		// 当前节点
		SystemGroups group = null;
		// 上级节点1
		SystemGroups pGroup1 = null;
		// 上级节点2
		SystemGroups pGroup2 = null;
		// 上级节点3
		SystemGroups pGroup3 = null;

		// 所有子节点id列表
		List<SystemGroups> allchildGroups = new ArrayList<SystemGroups>();

		int groupId = 0;
		//System.out.println(deptId + " # " + deptIds);
		if (StringUtils.isNotEmpty(deptId)) {
			groupId = Integer.valueOf(deptId);
			if (groupId > 0) {
				// 构造当前节点的路径id列表
				validGroupId.add(groupId);
				group = organService.loadGroupsById(groupId);
				// 判断父节点是否为根节点，如果不是，则把id放到有效节点路径中
				int pid1 = group.getParentID();
				if (group != null && pid1 != 0) {
					validGroupId.add(pid1);
					pGroup1 = organService.loadGroupsById(pid1);
					int pid2 = pGroup1.getParentID();
					if (pGroup1 != null && pid2 != 0) {
						pGroup2 = organService.loadGroupsById(pid2);
						validGroupId.add(pid2);
						int pid3 = pGroup2.getParentID();
						if (null != pGroup2 && pid3 != 0) {
							pGroup3 = organService.loadGroupsById(pid3);
							validGroupId.add(pid3);
						}
					}
				}
			}
		}

		// add by Cao_Shengyong 2014-08-13
		// 此处暂时仅针对代理人设置中的按组织查询，处理测试阶段
		// 考虑到同一工号兼职了不同单位的岗位，在设置代理人或助理人时，只能选择到主岗位所在单位人员
		// 例如：王天华主岗位在总部，但是还兼成都神旺总经理，而且使用的是同一工号，所以需要将它非主岗位单位的信息取出
		// 然后生成一个列表
		List<SystemGroups> groupList = new ArrayList<SystemGroups>();
		List<Integer> pGroupList1 = new ArrayList<Integer>();
		List<Integer> pGroupList2 = new ArrayList<Integer>();
		List<Integer> pGroupList3 = new ArrayList<Integer>();
		if (StringUtils.isNotEmpty(type) && (type.equals("agent"))){
			try{
				List<String> tmpCmpList = new ArrayList<String>();
				List<EmployeePos> empPTJobList = userService.getEmployeePosListPTJob(employeeId, employeePos.getCmpcod());
				if (empPTJobList != null && empPTJobList.size() > 0){
					for(EmployeePos tmpEmployeePos : empPTJobList){
						if (!tmpCmpList.contains(tmpEmployeePos.getCmpcod())){
							tmpCmpList.add(tmpEmployeePos.getCmpcod());
							SystemGroups tmpGroup = null;
							SystemGroups tmpPGroup1 = null;
							SystemGroups tmpPGroup2 = null;
							SystemGroups tmpPGroup3 = null;
							tmpGroup = organService.getGroupsByDeptCode(tmpEmployeePos.getDeptcode(),
									tmpEmployeePos.getA_deptcode(), tmpEmployeePos.getCmpcod(),
									GroupType.CENTERGROUP);
							groupList.add(tmpGroup);
							validGroupId.add(tmpGroup.getGroupID());
							int tmpPid1 = tmpGroup.getParentID();
							if (tmpGroup != null && tmpPid1 != 0){
								validGroupId.add(tmpPid1);
								pGroupList1.add(tmpPid1);
								tmpPGroup1 = organService.loadGroupsById(tmpPid1);
								int tmpPid2 = tmpPGroup1.getParentID();
								if (tmpPGroup1 != null && tmpPid2 != 0) {
									tmpPGroup2 = organService.loadGroupsById(tmpPid2);
									validGroupId.add(tmpPid2);
									pGroupList2.add(tmpPid2);
									int tmpPid3 = tmpPGroup2.getParentID();
									if (null != tmpPGroup2 && tmpPid3 != 0) {
										tmpPGroup3 = organService.loadGroupsById(tmpPid3);
										validGroupId.add(tmpPid3);
										pGroupList3.add(tmpPid3);
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println("获取兼职单位信息出错，请调试。" + this.getClass().getName());
			}
		}
		

		String[] dIds = null;
		if (StringUtils.isNotEmpty(deptIds)) {
			dIds = deptIds.split(";");
		}

		// 根据点击的父节点逐层加载子节点
		List<SystemGroups> groups = new ArrayList<SystemGroups>();
		List<OrganVo> result = new ArrayList<OrganVo>();
		if (StringUtils.isNotEmpty(id) && Integer.valueOf(id) != 0) {
			groups = organService.getGroupsByParent(Integer.valueOf(id));
		} else {
			groups = organService.getGroupsByParent(0);
		}

		// 循环子节点列表，将一些属性放入到OrganVo用于前台的树显示，并进行一些节点过滤
		for (SystemGroups systemGroups : groups) {
			OrganVo organVo = new OrganVo();
			organVo.setId(String.valueOf(systemGroups.getGroupID()));
			organVo.setName(systemGroups.getGroupName());
			organVo.setPId(String.valueOf(systemGroups.getParentID()));
			organVo.setOrgPath(systemGroups.getOrgPath());
			// 如果是部门节点，则无子节点
			if (systemGroups.getGroupType() != 5) {
				organVo.setParent(true);
			}
			// 判断是否节点被选中，如果被选中自动打钩
			if (dIds != null) {
				for (int i = 0; i < dIds.length; i++) {
					if (dIds[i]
							.equals(String.valueOf(systemGroups.getGroupID()))) {
						organVo.setChecked(true);
					}
				}
			}

			// 以下过滤判断中，setHidden代表节点是否显示、setClicked代表节点是否可用
			// 内部会办过滤
			if (StringUtils.isNotEmpty(type) && type.equals("innerhuiban")
					&& groupId > 0) {
				// 如果是部门、或者中心节点，则同当前组织平级的内部人员
				if (pGroup1 != null && pGroup2 != null && pGroup3 != null) {
					allchildGroups = organService.getAllGroupsByParent(pGroup1
							.getGroupID());
					allchildGroups.add(pGroup2);
					allchildGroups.add(pGroup3);
					validGroupId = new ArrayList<Integer>();
					for (int i = 0; i < allchildGroups.size(); i++) {
						validGroupId.add(allchildGroups.get(i).getGroupID());
					}
					if (validGroupId.contains(systemGroups.getGroupID())
							|| systemGroups.getGroupID() == ROOT_GROUPID) {
						organVo.setHidden(false);
						// 父节点和根节点不可用
						if (systemGroups.getGroupID() == ROOT_GROUPID
								|| systemGroups.getGroupID() == pGroup1
										.getGroupID()
								|| systemGroups.getGroupID() == pGroup2
										.getGroupID()
								|| systemGroups.getGroupID() == pGroup3
										.getGroupID()) {
							organVo.setClicked(false);
						}
					} else {
						organVo.setHidden(true);
					}
				} else {
					if (systemGroups.getGroupID() == ROOT_GROUPID) {
						organVo.setClicked(false);
					}
				}
			}
			// 会办过滤
			if (StringUtils.isNotEmpty(type) && type.equals("huiban")
					&& groupId > 0 && StringUtils.isNotEmpty(contentType)) {
				if (StringUtils.isNotBlank(flowType)) {
					if ("2".equals(flowType)) {
						// 内联 开放所有单位分支
						organVo.setHidden(false);
					} else {
						// 签呈
						if (contentType.equals("1")) {
							// 体系内
							if (null != curSystemGroups
									&& curSystemGroups.getCmpcod().equals(
											"HQTR")) {
								// 总部人员 开放所有单位分支
								organVo.setHidden(false);
							} else {
								// 地方人员 开放提单人所在单位（CMPCOD）下所有分支
								if (null != curSystemGroups) {
									organVo.setHidden(false);
								} else {
									organVo.setHidden(true);
								}
							}
						} else {
							// 地方到总部
							organVo.setHidden(false);

							if (null != employeePos
									&& employeePos.isTopmgr()
									&& null != systemGroups.getCmpcod()
									&& null != employeePos.getCmpcod()
									&& systemGroups.getCmpcod().equals(
											employeePos.getCmpcod())) {
								// 不能添加自己单位的人员
								organVo.setHidden(true);
							}

						}
					}
				} else {
					organVo.setHidden(false);
				}
				/**
				 * 在会办选择时增加判断，如果是总部的职能体系单位，仅显示中心，即父节点是705
				 * 不是705就要隐藏
				 */
				if (systemGroups.getSystemFlg().equals("G")){
					if (systemGroups.getParentID() != 705){
						organVo.setHidden(true);
					}
				}
			}
			
			// 抄送过滤
			if (StringUtils.isNotEmpty(type) && type.equals("chaosong")
					&& groupId > 0 && StringUtils.isNotEmpty(contentType)) {
				if (systemGroups.getSystemFlg().equals("G")){
					if (systemGroups.getParentID() != 705){
						organVo.setHidden(true);
					}
				}
			}
			
			// 指派过滤
			if (StringUtils.isNotEmpty(type) && type.equals("assign")
					&& StringUtils.isNotEmpty(assignDeptId)) {
				List<Integer> children = new ArrayList<Integer>();
				int assignDepId = Integer.valueOf(assignDeptId);
				List<Integer> validAssignDepId = new ArrayList<Integer>();
				if (assignDepId > 0) {
					// 构造指派节点的路径id列表
					validAssignDepId.add(assignDepId);
					SystemGroups assignGroup = organService
							.loadGroupsById(assignDepId);
					
					/**
					 * 考虑到职能部门的呈核上级后的二次指派，这里需要往上提一级
					 * 这里主要是传入的是部门ID的情况，如果传入的本身就是中心就不用
					 */
					if (assignGroup.getSystemFlg().equals("Y") && assignGroup.getGroupType() == 5){
						assignDepId = assignGroup.getParentID();
					}
					// 查询所有下级节点，包括本节点
					allchildGroups = organService
							.getAllGroupsByParent(assignDepId);
					for (int i = 0; i < allchildGroups.size(); i++) {
						children.add(allchildGroups.get(i).getGroupID());
					}

					// 判断父节点是否为根节点，如果不是，则把id放到有效节点路径中
					int assignPid1 = assignGroup.getParentID();
					if (assignGroup != null && assignPid1 != 0) {
						validAssignDepId.add(assignPid1);
						SystemGroups assignPGroup1 = organService
								.loadGroupsById(assignPid1);
						int assignPid2 = assignPGroup1.getParentID();
						if (assignPGroup1 != null && assignPid2 != 0) {
							validAssignDepId.add(assignPid2);
							SystemGroups assignPGroup2 = organService
									.loadGroupsById(assignPid2);
							int assignPid3 = assignPGroup2.getParentID();
							if (assignPGroup2 != null && assignPid3 != 0) {
								validAssignDepId.add(assignPid3);
							}
						}
					}
				}
				// 除了需要显示的节点，其他节点都过滤
				if (validAssignDepId.contains(systemGroups.getGroupID())
						|| children.contains(systemGroups.getGroupID())
						|| systemGroups.getGroupID() == ROOT_GROUPID) {
					organVo.setHidden(false);
					if (systemGroups.getGroupID() != assignDepId
							&& !children.contains(systemGroups.getGroupID())) {
						organVo.setClicked(false);
					}
				} else {
					organVo.setHidden(true);
				}
			}
			
			// 代理过滤 reversed with agent
			if (StringUtils.isNotEmpty(type) && type.equals("assist")
					&& groupId > 0) {
				// 这里不管是部门/中心/单位节点，均展示
				/*if (systemGroups.getGroupID() == ROOT_GROUPID
						|| systemGroups.getCmpcod().equals(group.getCmpcod())){
					organVo.setHidden(false);
					if (systemGroups.getGroupType() < 5){
						organVo.setClicked(false);
					}
				} else {
					organVo.setHidden(true);
				}*/
				// 如果是部门、或者中心节点
				if (pGroup1 != null && pGroup2 != null) {
					// 查询所有下级节点，包括本节点
					//allchildGroups = organService.getAllGroupsByParent(groupId);
					if (group.getParentID() == 705 || !group.getCmpcod().equals("HQTR")){
						allchildGroups = organService.getAllGroupsByParent(groupId);
					} else {
						allchildGroups = organService.getAllGroupsByParent(group.getParentID());
					}
					for (int i = 0; i < allchildGroups.size(); i++) {
						validGroupId.add(allchildGroups.get(i).getGroupID());
					}
					if (validGroupId.contains(systemGroups.getGroupID())
							|| systemGroups.getGroupID() == ROOT_GROUPID) {
						organVo.setHidden(false);
						// 父节点和根节点不可用
						if (systemGroups.getGroupID() == ROOT_GROUPID
								|| systemGroups.getGroupID() == pGroup1
										.getGroupID()
								|| systemGroups.getGroupID() == pGroup2
										.getGroupID()) {
							organVo.setClicked(false);
						}
					} else {
						organVo.setHidden(true);
					}
				} else {
					// 查询所有下级节点，包括本节点
					allchildGroups = organService.getAllGroupsByParent(groupId);
					for (int i = 0; i < allchildGroups.size(); i++) {
						validGroupId.add(allchildGroups.get(i).getGroupID());
					}
					if (validGroupId.contains(systemGroups.getGroupID())
							|| systemGroups.getGroupID() == ROOT_GROUPID) {
						organVo.setHidden(false);
						// 父节点和本节点不可用
						if (systemGroups.getGroupID() == ROOT_GROUPID
								|| systemGroups.getGroupID() == groupId) {
							organVo.setClicked(false);
						}
					} else {
						organVo.setHidden(true);
					}
				}
			}
			// 助理过滤（只有中心、单位主管可用）
			if (StringUtils.isNotEmpty(type) && type.equals("agent")
					&& groupId > 0) {
				// 如果是中心节点
				if (pGroup1 != null && pGroup2 != null) {
					// 查询所有下级节点，包括本节点
					allchildGroups = organService.getAllGroupsByParent(group
							.getParentID());
					for (int i = 0; i < allchildGroups.size(); i++) {
						validGroupId.add(allchildGroups.get(i).getGroupID());
					}
					if (validGroupId.contains(systemGroups.getGroupID())
							|| systemGroups.getGroupID() == ROOT_GROUPID) {
						organVo.setHidden(false);
						// 父节点和根节点不可用
						if (systemGroups.getGroupID() == ROOT_GROUPID
								|| systemGroups.getGroupID() == pGroup1
										.getGroupID()
								|| systemGroups.getGroupID() == pGroup2
										.getGroupID()) {
							organVo.setClicked(false);
						}
					} else {
						organVo.setHidden(true);
					}
				}
				// 如果是单位节点
				else {
					// 查询所有下级节点，包括本节点
					allchildGroups = organService.getAllGroupsByParent(groupId);
					for (int i = 0; i < allchildGroups.size(); i++) {
						validGroupId.add(allchildGroups.get(i).getGroupID());
					}
					if (validGroupId.contains(systemGroups.getGroupID())
							|| systemGroups.getGroupID() == ROOT_GROUPID) {
						organVo.setHidden(false);
						// 父节点和本节点不可用
						if (systemGroups.getGroupID() == ROOT_GROUPID
								|| systemGroups.getGroupID() == groupId) {
							organVo.setClicked(false);
						}
					} else {
						organVo.setHidden(true);
					}
				}
				
				// 上面是原来的，暂时不去修改，另外再增加一部分去判断兼职单位的选择
				if (pGroupList1 != null && pGroupList1.size() > 0 && pGroupList2 != null && pGroupList2.size() > 0){
					if (groupList != null && groupList.size() > 0){
						for(SystemGroups tmpGroups : groupList){
							allchildGroups = organService.getAllGroupsByParent(tmpGroups.getParentID());
							for(int i = 0; i < allchildGroups.size(); i++){
								if (!validGroupId.contains(allchildGroups.get(i).getGroupID())){
									validGroupId.add(allchildGroups.get(i).getGroupID());
								}
							}
							if (validGroupId.contains(systemGroups.getGroupID())
									|| systemGroups.getGroupID() == ROOT_GROUPID){
								organVo.setHidden(false);
								if (systemGroups.getGroupID() == ROOT_GROUPID
										|| pGroupList1.contains(systemGroups.getGroupID())
										|| pGroupList2.contains(systemGroups.getGroupID())){
									organVo.setClicked(false);
								}
							} else {
								organVo.setHidden(true);
							}
						}
					}
				} else {
					if (groupList != null && groupList.size() > 0){
						for(SystemGroups tmpGroups : groupList){
							allchildGroups = organService.getAllGroupsByParent(tmpGroups.getGroupID());
							for(int i = 0; i < allchildGroups.size(); i++){
								if (!validGroupId.contains(allchildGroups.get(i).getGroupID())){
									validGroupId.add(allchildGroups.get(i).getGroupID());
								}
							}
							if (validGroupId.contains(systemGroups.getGroupID())
									|| systemGroups.getGroupID() == ROOT_GROUPID){
								organVo.setHidden(false);
								if (systemGroups.getGroupID() == ROOT_GROUPID
										|| systemGroups.getGroupID() == tmpGroups.getGroupID()){
									organVo.setClicked(false);
								}
							} else {
								organVo.setHidden(false);
							}
						}
					}
				}
			}
			result.add(organVo);
		}

		this.createJSonData(VOUtils.getJsonDataFromCollection(result));
		return AJAX;
	}

	/**
	 * 最终核决主管指派
	 * 
	 * @return
	 */
	public String loadBossOrgans() {
		String id = this.servletRequest.getParameter("id");// 父节点的ID
		String type = this.servletRequest.getParameter("type");
		String deptId = this.servletRequest.getParameter("deptId");
		String contentType = this.servletRequest.getParameter("contentType"); // 类别，1：体系内，2：地方到总部
		String leader = this.servletRequest.getParameter("leader"); // 核决准管
		String assignDeptId = this.servletRequest.getParameter("assignDeptId");
		String curWorkId = this.servletRequest.getParameter("curWorkId"); // 当前的work主键
		MyWork curWork = flowService.loadWork(Long.parseLong(curWorkId));

		// 获取当前登录人
		String employeeId = (String) this.getSession().get("employeeId");
		if (!curWork.getEmployeeId().equals(employeeId)) {
			// 不是当前登录人的单子，可能是助理人或是代理人的
			employeeId = curWork.getEmployeeId();
		}

		EmployeePos employeePos = userService.getEmployeePosByEmpId(employeeId,
				curWork.getPostCode());
		if (null == employeePos) {
			employeePos = userService.getEmployeePosByEmpId(employeeId);
		}
		
		SystemGroups curWorkGroups = organService.loadGroupsById(Integer.valueOf(curWork.getDeptId()));
		
		// 会办单位或者抄送单位
		String deptIds = this.servletRequest.getParameter("deptIds");

		List<Integer> validGroupId = new ArrayList<Integer>();
		// 当前节点
		SystemGroups group = null;
		// 上级节点1
		SystemGroups pGroup1 = null;
		// 上级节点2
		SystemGroups pGroup2 = null;

		// 所有子节点id列表
		List<SystemGroups> allchildGroups = new ArrayList<SystemGroups>();

		int groupId = 0;
		if (StringUtils.isNotEmpty(deptId)) {
			groupId = Integer.valueOf(deptId);
			if (groupId > 0) {
				// 构造当前节点的路径id列表
				validGroupId.add(groupId);
				group = organService.loadGroupsById(groupId);
				// 判断父节点是否为根节点，如果不是，则把id放到有效节点路径中
				int pid1 = group.getParentID();
				if (group != null && pid1 != 0) {
					validGroupId.add(pid1);
					pGroup1 = organService.loadGroupsById(pid1);
					int pid2 = pGroup1.getParentID();
					if (pGroup1 != null && pid2 != 0) {
						pGroup2 = organService.loadGroupsById(pid2);
						validGroupId.add(pid2);
					}
				}
			}
		}

		String[] dIds = null;
		if (StringUtils.isNotEmpty(deptIds)) {
			dIds = deptIds.split(";");
		}

		// 根据点击的父节点逐层加载子节点
		List<SystemGroups> groups = new ArrayList<SystemGroups>();
		List<OrganVo> result = new ArrayList<OrganVo>();
		if (StringUtils.isNotEmpty(id) && Integer.valueOf(id) != 0) {
			groups = organService.getGroupsByParent(Integer.valueOf(id));
		} else {
			groups = organService.getGroupsByParent(0);
		}
		String curWorkSysFlg = curWorkGroups.getSystemFlg();
		boolean isFilter = true;
		if (curWorkSysFlg.equals("Y")){
			isFilter = false;
		}
		int assignParentId = Integer.valueOf(assignDeptId);
		// 循环子节点列表，将一些属性放入到OrganVo用于前台的树显示，并进行一些节点过滤
		for (SystemGroups systemGroups : groups) {
			boolean isGo = true;
			if (isFilter && systemGroups.getSystemFlg().equals("Y")){
				isGo = false;
			}
			if (isGo){
				OrganVo organVo = new OrganVo();
				organVo.setId(String.valueOf(systemGroups.getGroupID()));
				organVo.setName(systemGroups.getGroupName());
				organVo.setPId(String.valueOf(systemGroups.getParentID()));
				organVo.setOrgPath(systemGroups.getOrgPath());
				// 如果是部门节点，则无子节点
				if (systemGroups.getGroupType() != 5) {
					organVo.setParent(true);
				}
				// 判断是否节点被选中，如果被选中自动打钩
				if (dIds != null) {
					for (int i = 0; i < dIds.length; i++) {
						if (dIds[i]
								.equals(String.valueOf(systemGroups.getGroupID()))) {
							organVo.setChecked(true);
						}
					}
				}
	
				List<Integer> children = new ArrayList<Integer>();
				int assignDepId = Integer.valueOf(assignDeptId);
				List<Integer> validAssignDepId = new ArrayList<Integer>();
				if (assignDepId > 0) {
					// 构造指派节点的路径id列表
					validAssignDepId.add(assignDepId);
					SystemGroups assignGroup = organService
							.loadGroupsById(assignDepId);
					//　edit by Cao_Shengyong 
					// 核决主管/副主管核决指派时需要考虑到总部多层架构的情况，所以这里往上提一级(即被指派的人可以选择所属中心)
					// 如果是职能部门，则不提
					if (!assignGroup.getSystemFlg().equals("Y")){
						assignParentId = assignGroup.getParentID();
					}
					if (assignGroup.getGroupType() == 5){
						assignParentId = assignGroup.getParentID();
					}
	
					// 查询所有下级节点，包括本节点
					allchildGroups = organService.getAllGroupsByParent(assignDepId,
							employeePos);
					for (int i = 0; i < allchildGroups.size(); i++) {
						children.add(allchildGroups.get(i).getGroupID());
					}
	
					// 判断父节点是否为根节点，如果不是，则把id放到有效节点路径中
					int assignPid1 = assignGroup.getParentID();
					if (assignGroup != null && assignPid1 != 0) {
						validAssignDepId.add(assignPid1);
						SystemGroups assignPGroup1 = organService
								.loadGroupsById(assignPid1);
						int assignPid2 = assignPGroup1.getParentID();
						if (assignPGroup1 != null && assignPid2 != 0) {
							validAssignDepId.add(assignPid2);
							SystemGroups assignPGroup2 = organService
									.loadGroupsById(assignPid2);
							int assignPid3 = assignPGroup2.getParentID();
							if (null != assignPGroup2 && assignPid3 != 0) {
								validAssignDepId.add(assignPid3);
							}
						}
					}
				}
				if (systemGroups.getGroupType() < 5) {
					organVo.setClicked(false);
				}
				
				int cint = organService.getSizeByP(systemGroups.getGroupID(), assignParentId);
				// 除了需要显示的节点，其他节点都过滤
				if (validAssignDepId.contains(systemGroups.getGroupID())
						|| children.contains(systemGroups.getGroupID())
						//|| systemGroups.getParentID() == assignParentId
						|| cint > 0
						|| systemGroups.getGroupID() == ROOT_GROUPID) {
					organVo.setHidden(false);
					if (systemGroups.getGroupID() != assignDepId
							&& !children.contains(systemGroups.getGroupID())) {
						organVo.setClicked(false);
					}
					//if (systemGroups.getParentID() == assignParentId){
					if (cint > 0) {
						organVo.setClicked(true);
					}
				} else {
					organVo.setHidden(true);
				}
	
				result.add(organVo);
			}
		}

		this.createJSonData(VOUtils.getJsonDataFromCollection(result));
		return AJAX;
	}

	/**
	 * 事业部主管指派
	 */
	public String loadDivisionOrgans() {
		String id = this.servletRequest.getParameter("id"); // 父节点的ID
		String type = this.servletRequest.getParameter("type");
		String deptId = this.servletRequest.getParameter("deptId");
		String contentType = this.servletRequest.getParameter("contentType"); // 类别　1：体系内，　2：地方到总部
		String leader = this.servletRequest.getParameter("leader");
		String assignDeptId = this.servletRequest.getParameter("assignDeptId");
		String curWorkId = this.servletRequest.getParameter("curWorkId"); // 当前的work主键
		MyWork curWork = flowService.loadWork(Long.parseLong(curWorkId));

		// 获取当前登录人
		String employeeId = (String) this.getSession().get("employeeId");
		if (!curWork.getEmployeeId().equals(employeeId)) {
			// 不是当前登录人，可能是助理人或是代理人
			employeeId = curWork.getEmployeeId();
		}

		// 获取当前审批记录的人员信息(工号 + 岗位)
		EmployeePos employeePos = userService.getEmployeePosByEmpId(employeeId,
				curWork.getPostCode());
		if (null == employeePos) {
			employeePos = userService.getEmployeePosByEmpId(employeeId);
		}

		// 会办单位或抄送单位
		String deptIds = this.servletRequest.getParameter("deptIds");

		List<Integer> validGroupId = new ArrayList<Integer>();
		// 当前节点
		SystemGroups group = null;
		// 上级节点1
		SystemGroups pGroup1 = null;
		// 上级节点2
		SystemGroups pGroup2 = null;

		// 所有子节点id列表
		List<SystemGroups> allchildGroups = new ArrayList<SystemGroups>();

		int groupId = 0;
		if (StringUtils.isNotEmpty(deptIds)) {
			groupId = Integer.valueOf(deptId);
			if (groupId > 0) {
				// 构造当前节点的路径id列表
				validGroupId.add(groupId);
				group = organService.loadGroupsById(groupId);
				// 判断父节点是否为根节点，如果不是，则把id放到有效节点路径中
				int pid1 = group.getParentID();
				if (group != null && pid1 != 0) {
					validGroupId.add(pid1);
					pGroup1 = organService.loadGroupsById(pid1);
					int pid2 = pGroup1.getParentID();
					if (pGroup1 != null && pid2 != 0) {
						pGroup2 = organService.loadGroupsById(pid2);
						validGroupId.add(pid2);
					}
				}
			}
		}

		String[] dIds = null;
		if (StringUtils.isNotEmpty(deptIds)) {
			dIds = deptIds.split(";");
		}

		// 根据点击的父节点逐层加载子节点
		List<SystemGroups> groups = new ArrayList<SystemGroups>();
		List<OrganVo> result = new ArrayList<OrganVo>();
		if (StringUtils.isNotEmpty(id) && Integer.valueOf(id) != 0) {
			groups = organService.getGroupsByParent(Integer.valueOf(id));
		} else {
			groups = organService.getGroupsByParent(0);
		}
		
		int assignParentId = Integer.valueOf(assignDeptId);

		// 循环子节点列表，将一些属性放入到OrganVo用于前台的树显示，并进行一些节点过滤
		for (SystemGroups systemGroups : groups) {
			OrganVo organVo = new OrganVo();
			organVo.setId(String.valueOf(systemGroups.getGroupID()));
			organVo.setName(systemGroups.getGroupName());
			organVo.setPId(String.valueOf(systemGroups.getParentID()));
			organVo.setOrgPath(systemGroups.getOrgPath());
			// 如果是部门节点，则无子节点
			if (systemGroups.getGroupType() != 5) {
				organVo.setParent(true);
			}
			// 判断是否节点被选中，如果被选中自动打钩
			if (dIds != null) {
				for (int i = 0; i < dIds.length; i++) {
					if (dIds[i]
							.equals(String.valueOf(systemGroups.getGroupID()))) {
						organVo.setChecked(true);
					}
				}
			}

			List<Integer> children = new ArrayList<Integer>();
			int assignDepId = Integer.valueOf(assignDeptId);
			List<Integer> validAssignDepId = new ArrayList<Integer>();
			if (assignDepId > 0) {
				// 构造指派节点的路径id列表
				validAssignDepId.add(assignDepId);
				
				SystemGroups assignGroup = organService
						.loadGroupsById(assignDepId);
				
				assignParentId = assignGroup.getParentID();

				// 查询所有下级节点，包括本节点
				allchildGroups = organService.getAllGroupsByParent(assignDepId,
						employeePos);
				for (int i = 0; i < allchildGroups.size(); i++) {
					children.add(allchildGroups.get(i).getGroupID());
				}

				// 判断父节点是否为根节点，如果不是，则把id放到有效节点路径中
				int assignPid1 = assignGroup.getParentID();
				if (assignGroup != null && assignPid1 != 0) {
					validAssignDepId.add(assignPid1);
					SystemGroups assignPGroup1 = organService
							.loadGroupsById(assignPid1);
					int assignPid2 = assignPGroup1.getParentID();
					if (assignPGroup1 != null && assignPid2 != 0) {
						validAssignDepId.add(assignPid2);
						SystemGroups assignPGroup2 = organService
								.loadGroupsById(assignPid2);
						int assignPid3 = assignPGroup2.getParentID();
						if (null != assignPGroup2 && assignPid3 != 0) {
							validAssignDepId.add(assignPid3);
						}
					}
				}
			}
			if (systemGroups.getGroupType() < 5) {
				organVo.setClicked(false);
			}
			//System.out.println(systemGroups.getGroupID() +  " ^ " + assignParentId);
			int cint = organService.getSizeByP(systemGroups.getGroupID(), assignParentId);
			//System.out.println(cint);
			// 除了需要显示的节点，其他节点都过滤
			if (validAssignDepId.contains(systemGroups.getGroupID())
					|| children.contains(systemGroups.getGroupID())
					|| systemGroups.getParentID() == assignParentId
					//|| cint > 0
					|| cint > 0
					|| systemGroups.getGroupID() == ROOT_GROUPID) {
				organVo.setHidden(false);
				if (systemGroups.getGroupID() != assignDepId
						&& !children.contains(systemGroups.getGroupID())) {
					organVo.setClicked(false);
				}
				//if (systemGroups.getParentID() == assignParentId){
				if (cint > 0){
					organVo.setClicked(true);
				}
			} else {
				organVo.setHidden(true);
			}

			result.add(organVo);
		}
		this.createJSonData(VOUtils.getJsonDataFromCollection(result));
		return AJAX;
	}
	
	// 地方单位最高主管指派
	public String loadLocalOrgans(){
		String id = this.servletRequest.getParameter("id"); // 父节点的ID
		String type = this.servletRequest.getParameter("type");
		String deptId = this.servletRequest.getParameter("deptId");
		String contentType = this.servletRequest.getParameter("contentType"); // 类别　1：体系内，　2：地方到总部
		String leader = this.servletRequest.getParameter("leader");
		String assignDeptId = this.servletRequest.getParameter("assignDeptId");
		String curWorkId = this.servletRequest.getParameter("curWorkId"); // 当前的work主键
		MyWork curWork = flowService.loadWork(Long.parseLong(curWorkId));
		
		// 获取当前登录人
		String employeeId = (String) this.getSession().get("employeeId");
		if (!curWork.getEmployeeId().equals(employeeId)) {
			// 不是当前登录人，可能是助理人或是代理人
			employeeId = curWork.getEmployeeId();
		}

		// 获取当前审批记录的人员信息(工号 + 岗位)
		EmployeePos employeePos = userService.getEmployeePosByEmpId(employeeId,
				curWork.getPostCode());
		if (null == employeePos) {
			employeePos = userService.getEmployeePosByEmpId(employeeId);
		}
		
		// 会办单位或抄送单位
		String deptIds = this.servletRequest.getParameter("deptIds");

		List<Integer> validGroupId = new ArrayList<Integer>();
		// 当前节点
		SystemGroups group = null;
		// 上级节点1
		SystemGroups pGroup1 = null;
		// 上级节点2
		SystemGroups pGroup2 = null;

		// 所有子节点id列表
		List<SystemGroups> allchildGroups = new ArrayList<SystemGroups>();

		int groupId = 0;
		if (StringUtils.isNotEmpty(deptIds)) {
			groupId = Integer.valueOf(deptId);
			if (groupId > 0) {
				// 构造当前节点的路径id列表
				validGroupId.add(groupId);
				group = organService.loadGroupsById(groupId);
				// 判断父节点是否为根节点，如果不是，则把id放到有效节点路径中
				int pid1 = group.getParentID();
				if (group != null && pid1 != 0) {
					validGroupId.add(pid1);
					pGroup1 = organService.loadGroupsById(pid1);
					int pid2 = pGroup1.getParentID();
					if (pGroup1 != null && pid2 != 0) {
						pGroup2 = organService.loadGroupsById(pid2);
						validGroupId.add(pid2);
					}
				}
			}
		}

		String[] dIds = null;
		if (StringUtils.isNotEmpty(deptIds)) {
			dIds = deptIds.split(";");
		}

		// 根据点击的父节点逐层加载子节点
		List<SystemGroups> groups = new ArrayList<SystemGroups>();
		List<OrganVo> result = new ArrayList<OrganVo>();
		if (StringUtils.isNotEmpty(id) && Integer.valueOf(id) != 0) {
			groups = organService.getGroupsByParent(Integer.valueOf(id));
		} else {
			groups = organService.getGroupsByParent(0);
		}

		// 循环子节点列表，将一些属性放入到OrganVo用于前台的树显示，并进行一些节点过滤
		for (SystemGroups systemGroups : groups) {
			OrganVo organVo = new OrganVo();
			organVo.setId(String.valueOf(systemGroups.getGroupID()));
			organVo.setName(systemGroups.getGroupName());
			organVo.setPId(String.valueOf(systemGroups.getParentID()));
			organVo.setOrgPath(systemGroups.getOrgPath());
			// 如果是部门节点，则无子节点
			if (systemGroups.getGroupType() != 5) {
				organVo.setParent(true);
			}
			// 判断是否节点被选中，如果被选中自动打钩
			if (dIds != null) {
				for (int i = 0; i < dIds.length; i++) {
					if (dIds[i]
							.equals(String.valueOf(systemGroups.getGroupID()))) {
						organVo.setChecked(true);
					}
				}
			}

			List<Integer> children = new ArrayList<Integer>();
			int assignDepId = Integer.valueOf(assignDeptId);
			List<Integer> validAssignDepId = new ArrayList<Integer>();
			if (assignDepId > 0) {
				// 构造指派节点的路径id列表
				validAssignDepId.add(assignDepId);
				SystemGroups assignGroup = organService
						.loadGroupsById(assignDepId);

				// 查询所有下级节点，包括本节点
				allchildGroups = organService.getAllGroupsByParent(assignDepId,
						employeePos);
				for (int i = 0; i < allchildGroups.size(); i++) {
					children.add(allchildGroups.get(i).getGroupID());
				}

				// 判断父节点是否为根节点，如果不是，则把id放到有效节点路径中
				int assignPid1 = assignGroup.getParentID();
				if (assignGroup != null && assignPid1 != 0) {
					validAssignDepId.add(assignPid1);
					SystemGroups assignPGroup1 = organService
							.loadGroupsById(assignPid1);
					int assignPid2 = assignPGroup1.getParentID();
					if (assignPGroup1 != null && assignPid2 != 0) {
						validAssignDepId.add(assignPid2);
						SystemGroups assignPGroup2 = organService
								.loadGroupsById(assignPid2);
						int assignPid3 = assignPGroup2.getParentID();
						if (null != assignPGroup2 && assignPid3 != 0) {
							validAssignDepId.add(assignPid3);
						}
					}
				}
			}
			if (systemGroups.getGroupType() < 5) {
				organVo.setClicked(false);
			}
			// 除了需要显示的节点，其他节点都过滤
			if (validAssignDepId.contains(systemGroups.getGroupID())
					|| children.contains(systemGroups.getGroupID())
					|| systemGroups.getGroupID() == ROOT_GROUPID) {
				organVo.setHidden(false);
				if (systemGroups.getGroupID() != assignDepId
						&& !children.contains(systemGroups.getGroupID())) {
					organVo.setClicked(false);
				}
			} else {
				organVo.setHidden(true);
			}

			result.add(organVo);
		}
		this.createJSonData(VOUtils.getJsonDataFromCollection(result));
		
		return AJAX;
	}
	
	public String loadOrgsByCmp(){
		String id = this.servletRequest.getParameter("id");
		String cmpcod = this.servletRequest.getParameter("cmpcod");
		
		if (org.apache.commons.lang3.StringUtils.isEmpty(id)){
			id = "701";
		}
		int gid = Integer.valueOf(id);
		
		List<SystemGroups> groups = new ArrayList<SystemGroups>();
		List<OrganVo> result = new ArrayList<OrganVo>();
		//if (StringUtils.isEmpty(cmpcod)){
		//	gid = 701;
		//}
		groups = organService.getGroupsByCmp(gid, cmpcod);
		for(SystemGroups systemGroups : groups){
			OrganVo organVo = new OrganVo();
			organVo.setId(String.valueOf(systemGroups.getGroupID()));
			organVo.setName(systemGroups.getGroupName());
			organVo.setPId(String.valueOf(systemGroups.getParentID()));
			organVo.setOrgPath(systemGroups.getOrgPath());
			organVo.setCmpcod(systemGroups.getCmpcod());
			if (systemGroups.getGroupType() != 5){
				organVo.setParent(true);
			}
			if (systemGroups.getGroupType() == 5){
				organVo.setHidden(true);
			}
			
			result.add(organVo);
		}
		this.createJSonData(VOUtils.getJsonDataFromCollection(result));
		return AJAX;
	}
}
