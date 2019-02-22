package com.wwgroup.flow.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.common.Page;
import com.wwgroup.common.action.BaseAjaxAction;
import com.wwgroup.common.util.CommonUtil;
import com.wwgroup.common.util.excel.ExcelExporter;
import com.wwgroup.common.vo.SuperUserConstant;
import com.wwgroup.common.vo.VOUtils;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.dto.AdvancedSearchDTO;
import com.wwgroup.flow.dto.Branch;
import com.wwgroup.flow.dto.ConfidentialSearchDTO;
import com.wwgroup.flow.dto.ConfidentialSearchbossDTO;
import com.wwgroup.flow.dto.FormStatus;
import com.wwgroup.flow.dto.LogDTO;
import com.wwgroup.flow.dto.LogInfo;
import com.wwgroup.flow.dto.MyWorkDTO;
import com.wwgroup.flow.dto.Order;
import com.wwgroup.flow.service.FlowManagementService;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.flow.vo.AdvancedSearchVo;
import com.wwgroup.organ.bo.HRCompany;

@SuppressWarnings("serial")
public class FlowManageAction extends BaseAjaxAction {

	private AdvancedSearchVo searchVo = new AdvancedSearchVo();

	private FlowManagementService flowManagementService;

	private PersonService personService;

	public AdvancedSearchVo getSearchVo() {
		return searchVo;
	}

	public void setSearchVo(AdvancedSearchVo searchVo) {
		this.searchVo = searchVo;
	}

	public void setFlowManagementService(FlowManagementService flowManagementService) {
		this.flowManagementService = flowManagementService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public Object getModel() {
		return this.searchVo;
	}

	// 查询待审核表单
	public String findMyWorksCount() {
		try {
			// 获取登录用户
			String employeeId = super.getLoginUser().getUserName();
			
			String tmpCurrentUser = super.getLoginUser().getUserRealName() + "(" + employeeId + ")";
			//logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 加载左侧菜单统计数　Start...");
			PersonDetail actualPerson = new PersonDetail();
			actualPerson.setEmployeeId(employeeId);

			PersonDetail person = this.personService.loadWidePersonDetail(employeeId);
			if (person != null) {
				actualPerson.setPostCode(person.getPostCode());
			}
			// 查询代审核单数目
			int myToDoWorkCount = flowManagementService.getMyToDoWorkCount(actualPerson);
			int flowTemplatesCount = flowManagementService.getFlowTemplateCount(actualPerson);
			int myProcessFlowCount = flowManagementService.getMyProcessFlowCount(actualPerson);
			int creatorFlowCount = flowManagementService.getCreatorFlowCount(actualPerson);
			int copyFlowCount = flowManagementService.getCopyFlowCount(actualPerson);
			int securityFlowCont = flowManagementService.getSecurityFlowCount();
			this.servletRequest.setAttribute("myToDoWorkCount", myToDoWorkCount);
			this.servletRequest.setAttribute("flowTemplatesCount", flowTemplatesCount);
			this.servletRequest.setAttribute("myProcessFlowCount", myProcessFlowCount);
			this.servletRequest.setAttribute("creatorFlowCount", creatorFlowCount);
			this.servletRequest.setAttribute("copyFlowCount", copyFlowCount);
			this.servletRequest.setAttribute("securityFlowCount", securityFlowCont);

			// 代理功能过滤
			boolean canAgent = personService.qualifiedGroupMgr(employeeId, "AGENT");
			if (canAgent) {
				this.servletRequest.setAttribute("canAgent", 1);
			}
			// 助理功能过滤
			boolean canAssist = personService.qualifiedGroupMgr(employeeId, "ASSIST");
			if (canAssist) {
				this.servletRequest.setAttribute("canAssist", 1);
			}
			if (employeeId.equals("00000006")){
				this.servletRequest.setAttribute("canSecurity", 1);
			}
			
			if (personService.getJYJurisdiction(employeeId)){
				this.servletRequest.setAttribute("jySearch", 1);
			}
			
			this.servletRequest.setAttribute("menu", this.servletRequest.getParameter("menu"));
			//logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 加载左侧菜单统计数　End...");
		} catch (Exception e) {
			logger.error("查看我的任务列表数量时发生异常："+e.getMessage());
			addActionError("表单数据或岗位信息存在异常，请联系系统管理员进行处理。");
			return ERROR;
		}
		return SUCCESS;
	}

	// 查询待审核表单
	@SuppressWarnings("unchecked")
	public String findMyToDoWorks() {
		try {
			int pageNo = 1;
			int pageSize = 10;
			int start = 0;
			// 获取登录用户
			String employeeId = super.getLoginUser().getUserName();
			
			String tmpCurrentUser = super.getLoginUser().getUserRealName() + "(" + employeeId + ")";
			logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 加载待审核列表　Start...");
			
			PersonDetail actualPerson = new PersonDetail();
			actualPerson.setEmployeeId(employeeId);
			// 这里暂时先把所有关于employeeId的代理人全部找到，忽略postCode信息.后面找客户确认.actualPerson.setPostCode(this.personService.loadWidePersonDetail(employeeId).getPostCode());
			// 查找助理信息时，需要postCode
			PersonDetail person = this.personService.loadWidePersonDetail(employeeId);
			if (person != null) {
				actualPerson.setPostCode(person.getPostCode());
			}
			String param = this.servletRequest.getParameter("pageNo");
			if (param != null) {
				pageNo = Integer.parseInt(param);
			}
			param = this.servletRequest.getParameter("pageSize");
			if (param != null) {
				pageSize = Integer.parseInt(param);
				start = (pageNo - 1) * pageSize + 1;
			}

			if (actualPerson != null) {
				Page page = flowManagementService.findMyToDoWorkByPage(actualPerson, start, pageSize);
				List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
				List<MyWorkDTO> list = page.getResult();
				if (list != null) {
					for (MyWorkDTO myWorkDTO : list) {
						if (myWorkDTO.getFlowType() != null) {
							myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
						}
						// 将工作项角色塞值供前台传递参数
						if (myWorkDTO.getWorkRole() != null) {
							myWorkDTO.setRole(myWorkDTO.getWorkRole().name());
						}
						result.add(myWorkDTO);
					}
					page.setResult(result);
				}
				String json = VOUtils.getJsonDataFromPage(page, MyWorkDTO.class);
				this.createJSonData(json);
			}
			logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 加载待审核列表　End...");
		} catch (Exception e) {
			logger.error("查看我的任务列表时发生异常："+e.getMessage());
			addActionError("表单数据或岗位信息存在异常，请联系系统管理员进行处理。");
			return ERROR;
		}
		return AJAX;
	}

	// 查询暂存表单
	@SuppressWarnings("unchecked")
	public String findFlowTemplates() {
		int pageNo = 1;
		int pageSize = 10;
		int start = 0;
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		String tmpCurrentUser = super.getLoginUser().getUserRealName() + "(" + employeeId + ")";
		logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 加载暂存表单列表　Start...");

		PersonDetail actualPerson = new PersonDetail();
		actualPerson.setEmployeeId(employeeId);

		String param = this.servletRequest.getParameter("pageNo");
		if (param != null) {
			pageNo = Integer.parseInt(param);
		}
		param = this.servletRequest.getParameter("pageSize");
		if (param != null) {
			pageSize = Integer.parseInt(param);
			start = (pageNo - 1) * pageSize + 1;
		}

		if (actualPerson != null) {
			Page page = flowManagementService.findFlowTemplatesByPage(actualPerson, start, pageSize);
			List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
			List<MyWorkDTO> list = page.getResult();
			if (list != null) {
				for (MyWorkDTO myWorkDTO : list) {
					if (myWorkDTO.getFlowType() != null) {
						myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
					}
					result.add(myWorkDTO);
				}
				page.setResult(result);
			}
			String json = VOUtils.getJsonDataFromPage(page, MyWorkDTO.class);
			this.createJSonData(json);
		}
		logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 加载暂存表单列表　End...");
		return AJAX;
	}

	// 查询已提交表单
	@SuppressWarnings("unchecked")
	public String findMyProcessFlow() {
		int pageNo = 1;
		int pageSize = 10;
		int start = 0;
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		String tmpCurrentUser = super.getLoginUser().getUserRealName() + "(" + employeeId + ")";
		logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 加载已提交表单列表　Start...");

		PersonDetail actualPerson = new PersonDetail();
		actualPerson.setEmployeeId(employeeId);

		String param = this.servletRequest.getParameter("pageNo");
		if (param != null) {
			pageNo = Integer.parseInt(param);
		}
		param = this.servletRequest.getParameter("pageSize");
		if (param != null) {
			pageSize = Integer.parseInt(param);
			start = (pageNo - 1) * pageSize + 1;
		}

		if (actualPerson != null) {
			Page page = flowManagementService.findMyProcessFlowByPage(actualPerson, start, pageSize);
			List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
			List<MyWorkDTO> list = page.getResult();
			if (list != null) {
				for (MyWorkDTO myWorkDTO : list) {
					if (myWorkDTO.getFlowType() != null) {
						myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
					}
					result.add(myWorkDTO);
				}
				page.setResult(result);
			}
			String json = VOUtils.getJsonDataFromPage(page, MyWorkDTO.class);
			this.createJSonData(json);
		}
		logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 加载已提交表单列表　End...");
		return AJAX;
	}

	// 查询已审核表单
	@SuppressWarnings("unchecked")
	public String findCreatorFlow() {
		int pageNo = 1;
		int pageSize = 10;
		int start = 0;
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		String tmpCurrentUser = super.getLoginUser().getUserRealName() + "(" + employeeId + ")";
		logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 加载已审核表单列表　Start...");

		PersonDetail actualPerson = new PersonDetail();
		actualPerson.setEmployeeId(employeeId);

		String param = this.servletRequest.getParameter("pageNo");
		if (param != null) {
			pageNo = Integer.parseInt(param);
		}
		param = this.servletRequest.getParameter("pageSize");
		if (param != null) {
			pageSize = Integer.parseInt(param);
			start = (pageNo - 1) * pageSize + 1;
		}
		
		String sidx = this.servletRequest.getParameter("sidx");
		String sord = this.servletRequest.getParameter("sord");
		
		AdvancedSearchDTO searchDTO = new AdvancedSearchDTO();
		String searchSub = this.servletRequest.getParameter("searchSub");
		searchDTO.setTitle(searchSub);
		
		if (actualPerson != null) {
			Page page = flowManagementService.findCreatorFlowByPageEx(actualPerson, start, pageSize, sidx, sord, searchDTO);
			List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
			List<MyWorkDTO> list = page.getResult();
			if (list != null) {
				for (MyWorkDTO myWorkDTO : list) {
					if (myWorkDTO.getFlowType() != null) {
						myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
					}
					result.add(myWorkDTO);
				}
				page.setResult(result);
			}
			String json = VOUtils.getJsonDataFromPage(page, MyWorkDTO.class);
			this.createJSonData(json);
		}
		logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 加载已审核表单列表　End...");
		return AJAX;
	}

	// 高级查询
	@SuppressWarnings("unchecked")
	public String searchMyWorks() {
		int pageNo = 1;
		int pageSize = 10;
		int start = 0;

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
		String tmpCurrentUser = super.getLoginUser().getUserRealName() + "(" + employeeId + ")";
		logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 执行高级查询操作　Start...");
		
		AdvancedSearchDTO searchDTO = new AdvancedSearchDTO();
		
		if(!employeeId.equalsIgnoreCase(SuperUserConstant.Administrator.toString())
				&& !employeeId.equalsIgnoreCase(SuperUserConstant.Admin.toString())
						){
			PersonDetail personDetail = personService.loadWidePersonDetail(employeeId);
			searchDTO.setUser(personDetail);
		}
		
		/*
		 * 设置员工号
		 * added by zhangqiang at 2012-11-20 12:53
		 */
		searchDTO.setEmployeeId(employeeId);

		String flowType = this.servletRequest.getParameter("flowType");
		String branch = this.servletRequest.getParameter("branch");
		String formStatus = this.servletRequest.getParameter("formStatus");
		String startTime = this.servletRequest.getParameter("startTime");
		String endTime = this.servletRequest.getParameter("endTime");
		String formNum = this.servletRequest.getParameter("formNum");
		String title = this.servletRequest.getParameter("title");
		String sidx = this.servletRequest.getParameter("sidx");
		String sord = this.servletRequest.getParameter("sord");
		
		String localType = this.servletRequest.getParameter("localType");
		String creator = this.servletRequest.getParameter("creator");
		String creatCmp = this.servletRequest.getParameter("creatCmp");
		
		String closeStartTime = this.servletRequest.getParameter("closeStartTime");
		String closeEndTime = this.servletRequest.getParameter("closeEndTime");


		String departmentName = this.servletRequest.getParameter("departmentName");

		//System.out.println(Order.CreateTime.name());
		if (StringUtils.isNotEmpty(sidx)){
			searchDTO.setOrderBy(sidx);
		} else {
			searchDTO.setOrderBy(Order.CreateTime.name());
		}
		if (StringUtils.isNotEmpty(sord)){
			searchDTO.setSorder(sord);
		} else {
			searchDTO.setSorder(" asc ");
		}

		searchDTO.setFlowType(FlowType.valueOf(flowType));
		searchDTO.setBranch(Branch.valueOf(branch));
		searchDTO.setFormStatus(FormStatus.valueOf(formStatus));
		
		searchDTO.setStartTime(startTime);
		searchDTO.setEndTime(endTime);

		if (StringUtils.isNotEmpty(formNum)) {
			searchDTO.setFormNum(formNum);
		}
		if (StringUtils.isNotEmpty(title)) {
			searchDTO.setTitle(title);
		}
		
		if (StringUtils.isNotEmpty(localType)){
			searchDTO.setLocalType(localType);
		}
		if (StringUtils.isNotEmpty(creator)){
			searchDTO.setCreator(creator);
		}
		if (StringUtils.isNotEmpty(creatCmp)){
			searchDTO.setCreatCmp(creatCmp);
		}
		if (StringUtils.isNotEmpty(departmentName)) {
			searchDTO.setDepartmentName(departmentName);
		}



		searchDTO.setCloseStartTime(closeStartTime);
		searchDTO.setCloseEndTime(closeEndTime);

		// searchDTO.setStartTime(startTime);
		// searchDTO.setEndTime(endTime);
		Page page = flowManagementService.searchWorkDTOByPage(searchDTO, start, pageSize);
		List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
		List<MyWorkDTO> list = page.getResult();
		if (list != null) {
			for (MyWorkDTO myWorkDTO : list) {
				if (myWorkDTO.getFlowType() != null) {
					myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
				}
				result.add(myWorkDTO);
			}
			page.setResult(result);
		}
		String json = VOUtils.getJsonDataFromPage(page, MyWorkDTO.class);
		this.createJSonData(json);
		logger.info("用户：" + tmpCurrentUser + "(IP:" + CommonUtil.getClientIP(servletRequest) + ") 执行高级查询操作　End...");
		return AJAX;
	}
	
	
	// 查询暂存表单
	public String delTempForm() {
		String params = this.servletRequest.getParameter("params");
		flowManagementService.delTempForm(Arrays.asList(params.split(",")));
		return AJAX;
	}
	
	public String top(){
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		PersonDetail curPerson = this.personService.loadWidePersonDetail(employeeId);
		String nowtime = CommonUtil.getCurrentTime();
		this.servletRequest.setAttribute("curPerson", curPerson);
		this.servletRequest.setAttribute("nowtime", nowtime);
		return SUCCESS;
	}
	
	// 查询抄送表单
	@SuppressWarnings("unchecked")
	public String findCopyFlow() {
		int pageNo = 1;
		int pageSize = 10;
		int start = 0;
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();

		PersonDetail actualPerson = new PersonDetail();
		actualPerson.setEmployeeId(employeeId);

		String param = this.servletRequest.getParameter("pageNo");
		if (param != null) {
			pageNo = Integer.parseInt(param);
		}
		param = this.servletRequest.getParameter("pageSize");
		if (param != null) {
			pageSize = Integer.parseInt(param);
			start = (pageNo - 1) * pageSize + 1;
		}

		if (actualPerson != null) {
			Page page = flowManagementService.findCopyFlowByPage(actualPerson, start, pageSize);
			List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
			List<MyWorkDTO> list = page.getResult();
			if (list != null) {
				for (MyWorkDTO myWorkDTO : list) {
					if (myWorkDTO.getFlowType() != null) {
						myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
					}
					result.add(myWorkDTO);
				}
				page.setResult(result);
			}
			String json = VOUtils.getJsonDataFromPage(page, MyWorkDTO.class);
			this.createJSonData(json);
		}
		return AJAX;
	}
	
	@SuppressWarnings("unchecked")
	public String findSecurityFlow() {
		int pageNo = 1;
		int pageSize = 10;
		int start = 0;
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();

		PersonDetail actualPerson = new PersonDetail();
		actualPerson.setEmployeeId(employeeId);

		String param = this.servletRequest.getParameter("pageNo");
		if (param != null) {
			pageNo = Integer.parseInt(param);
		}
		param = this.servletRequest.getParameter("pageSize");
		if (param != null) {
			pageSize = Integer.parseInt(param);
			start = (pageNo - 1) * pageSize + 1;
		}
		
		AdvancedSearchDTO searchDTO = new AdvancedSearchDTO();
		String formStatus = this.servletRequest.getParameter("formStatus");
		String searchSub = this.servletRequest.getParameter("searchSub");
		searchDTO.setFormStatus(FormStatus.valueOf(formStatus));
		searchDTO.setTitle(searchSub);

		if (actualPerson != null) {
			Page page = flowManagementService.findSecurityFlowByPage(searchDTO, start, pageSize);
			List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
			List<MyWorkDTO> list = page.getResult();
			if (list != null) {
				for (MyWorkDTO myWorkDTO : list) {
					if (myWorkDTO.getFlowType() != null) {
						myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
					}
					result.add(myWorkDTO);
				}
				page.setResult(result);
			}
			String json = VOUtils.getJsonDataFromPage(page, MyWorkDTO.class);
			this.createJSonData(json);
		}
		return AJAX;
	}
	
	public String advancedSearch(){
		List<HRCompany> list = flowManagementService.getCompanyList();
		this.servletRequest.setAttribute("cmpList", list);
		return SUCCESS;
	}
	
	public String confidentialSearch(){
		List<HRCompany> list = flowManagementService.getCompanyList();
		this.servletRequest.setAttribute("cmpList", list);
		return SUCCESS;
	}
	
	public String confidentialSearchboss(){
		List<HRCompany> list = flowManagementService.getCompanyList();
		this.servletRequest.setAttribute("cmpList", list);
		return SUCCESS;
	}
	
	public String findLogInfoByPage(){
		int pageNo = 1;
		int pageSize = 10;
		int start = 0;

		String param = this.servletRequest.getParameter("pageNo");
		if (param != null) {
			pageNo = Integer.parseInt(param);
		}
		param = this.servletRequest.getParameter("pageSize");
		if (param != null) {
			pageSize = Integer.parseInt(param);
			start = (pageNo - 1) * pageSize + 1;
		}
		
		String startDate = this.servletRequest.getParameter("startTime");
		String logLevel = this.servletRequest.getParameter("logLevel");
		Page page = flowManagementService.findLogInfoByPage(startDate, logLevel, start, pageSize);
		String json = VOUtils.getJsonDataFromPage(page, LogInfo.class);
		this.createJSonData(json);
		return AJAX;
	}
	
	@SuppressWarnings("unchecked")
	public String confSearchWorks(){
		int pageNo = 1;
		int pageSize = 10;
		int start = 0;
		
		String param = this.servletRequest.getParameter("pageNo");
		if (param != null){
			pageNo = Integer.parseInt(param);
		}
		param = this.servletRequest.getParameter("pageSize");
		if (param != null) {
			pageSize = Integer.parseInt(param);
			start = (pageNo - 1) * pageSize + 1;
		}
		String employeeId = super.getLoginUser().getUserName();
		String tmpCurrentUser = super.getLoginUser().getUserRealName() + "(" + employeeId + ")";
		
		ConfidentialSearchDTO searchDTO = new ConfidentialSearchDTO();
		searchDTO.setEmployeeId(employeeId);
		
		String flowType = this.servletRequest.getParameter("flowType");
		String localType = this.servletRequest.getParameter("localType");
		String formNum = this.servletRequest.getParameter("formNum");
		String title = this.servletRequest.getParameter("title");
		String creator = this.servletRequest.getParameter("creator");
		String startTime = this.servletRequest.getParameter("startTime");
		String endTime = this.servletRequest.getParameter("endTime");
		String formStatus = this.servletRequest.getParameter("formStatus");
		String creatCmp = this.servletRequest.getParameter("creatCmp");
		String leader = this.servletRequest.getParameter("leader");
		String rePath = this.servletRequest.getParameter("rePath");
		
		String sidx = this.servletRequest.getParameter("sidx");
		String sord = this.servletRequest.getParameter("sord");
		
		if (StringUtils.isNotEmpty(sidx)){
			searchDTO.setOrderBy(sidx);
		} else {
			searchDTO.setOrderBy(Order.CreateTime.name());
		}
		if (StringUtils.isNotEmpty(sord)){
			searchDTO.setSorder(sord);
		} else {
			searchDTO.setSorder(" asc ");
		}
		searchDTO.setFlowType(FlowType.valueOf(flowType));
		if (StringUtils.isNotEmpty(localType)){
			searchDTO.setLocalType(localType);
		}
		if (StringUtils.isNotEmpty(formNum)) {
			searchDTO.setFormNum(formNum);
		}
		if (StringUtils.isNotEmpty(title)) {
			searchDTO.setTitle(title);
		}
		if (StringUtils.isNotEmpty(creator)){
			searchDTO.setCreator(creator);
		}
		searchDTO.setStartTime(startTime);
		searchDTO.setEndTime(endTime);
		searchDTO.setFormStatus(FormStatus.valueOf(formStatus));
		if (StringUtils.isNotEmpty(creatCmp)){
			searchDTO.setCreatCmp(creatCmp);
		}
		if (StringUtils.isNotEmpty(leader)){
			searchDTO.setLeader(leader);
		}
		if (StringUtils.isNotEmpty(rePath)){
			searchDTO.setRePath(rePath);
		}
		
		// 此处需要插入日志，必需记录用户、IP在什么时间，执行了什么操作
		LogDTO logdDto = new LogDTO();
		logdDto.setIP(CommonUtil.getClientIP(servletRequest));
		logdDto.setUser(tmpCurrentUser);
		logdDto.setControl("查询");
		String scond = "查询条件：";
		String tmpcond = flowType.equals("QIANCHENG") ? "签呈" : "内联";
		scond += "表单类型(" + tmpcond + ")";
		tmpcond = StringUtils.isEmpty(localType) ? "全部" : (localType.equals("1") ? "体系内部" : "地方至总部");
		scond += "; 发文类别(" + tmpcond + ")";
		scond += "; 主旨(" + title + ")";
		scond += "; 发文人(" + creator + ")";
		scond += "; 提交日期(" + startTime + " 至 " + endTime + ")";
		tmpcond = formStatus.equalsIgnoreCase("ALL") ? "全部" : (localType.equals("DOING") ? "签核中" : "已核准");
		scond += "; 表单状态(" + tmpcond + ")";
		tmpcond = StringUtils.isEmpty(creatCmp) ? "全部" : creatCmp;
		scond += "; 发文单位(" + tmpcond + " : " + rePath + ")";
		tmpcond = StringUtils.isEmpty(leader) ? "全部" : leader;
		scond += "; 核决主管(" + tmpcond + ")";
		scond += "; 排序字段(" + searchDTO.getOrderBy() + ")";
		scond += "; 排序方式(" + searchDTO.getSorder() + ")";
		scond += "; 当前访问页(" + pageNo + ")";
		logdDto.setMessages(scond);
		flowManagementService.saveLog(logdDto);
		
		Page page = flowManagementService.searchConfidByPage(searchDTO, start, pageSize);
		List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
		List<MyWorkDTO> list = page.getResult();
		if (list != null){
			for(MyWorkDTO myWorkDTO : list){
				if (myWorkDTO.getFlowType() != null){
					myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
				}
				if (myWorkDTO.getDecionMaker() != null){
					myWorkDTO.setDecionMakerName(myWorkDTO.getDecionMaker().toString());
				}
				result.add(myWorkDTO);
			}
			page.setResult(result);
		}
		String json = VOUtils.getJsonDataFromPage(page, MyWorkDTO.class);
		this.createJSonData(json);
		
		return AJAX;
	}

	public String confExportWorks(){	
		String employeeId = super.getLoginUser().getUserName();
		String tmpCurrentUser = super.getLoginUser().getUserRealName() + "(" + employeeId + ")";
		
		ConfidentialSearchDTO searchDTO = new ConfidentialSearchDTO();
		searchDTO.setEmployeeId(employeeId);
		
		String flowType = this.servletRequest.getParameter("flowType");
		String localType = this.servletRequest.getParameter("localType");
		String formNum = this.servletRequest.getParameter("formNum");
		String title = this.servletRequest.getParameter("title");
		String creator = this.servletRequest.getParameter("creator");
		String startTime = this.servletRequest.getParameter("startTime");
		String endTime = this.servletRequest.getParameter("endTime");
		String formStatus = this.servletRequest.getParameter("formStatus");
		String creatCmp = this.servletRequest.getParameter("creatCmp");
		String leader = this.servletRequest.getParameter("leader");
		String rePath = this.servletRequest.getParameter("rePath");
		System.out.println(rePath);
		String sidx = this.servletRequest.getParameter("sidx");
		String sord = this.servletRequest.getParameter("sord");
		
		if (StringUtils.isNotEmpty(sidx)){
			searchDTO.setOrderBy(sidx);
		} else {
			searchDTO.setOrderBy(Order.CreateTime.name());
		}
		if (StringUtils.isNotEmpty(sord)){
			searchDTO.setSorder(sord);
		} else {
			searchDTO.setSorder(" asc ");
		}
		searchDTO.setFlowType(FlowType.valueOf(flowType));
		if (StringUtils.isNotEmpty(localType)){
			searchDTO.setLocalType(localType);
		}
		if (StringUtils.isNotEmpty(formNum)) {
			searchDTO.setFormNum(formNum);
		}
		if (StringUtils.isNotEmpty(title)) {
			searchDTO.setTitle(title);
		}
		if (StringUtils.isNotEmpty(creator)){
			searchDTO.setCreator(creator);
		}
		searchDTO.setStartTime(startTime);
		searchDTO.setEndTime(endTime);
		searchDTO.setFormStatus(FormStatus.valueOf(formStatus));
		if (StringUtils.isNotEmpty(creatCmp)){
			searchDTO.setCreatCmp(creatCmp);
		}
		if (StringUtils.isNotEmpty(leader)){
			searchDTO.setLeader(leader);
		}
		if (StringUtils.isNotEmpty(rePath)){
			searchDTO.setRePath(rePath);
		}
		
		// 此处需要插入日志，必需记录用户、IP在什么时间，执行了什么操作
		LogDTO logdDto = new LogDTO();
		logdDto.setIP(CommonUtil.getClientIP(servletRequest));
		logdDto.setUser(tmpCurrentUser);
		logdDto.setControl("导出");
		String scond = "导出条件：";
		String tmpcond = flowType.equals("QIANCHENG") ? "签呈" : "内联";
		scond += "表单类型(" + tmpcond + ")";
		tmpcond = StringUtils.isEmpty(localType) ? "全部" : (localType.equals("1") ? "体系内部" : "地方至总部");
		scond += "; 发文类别(" + tmpcond + ")";
		scond += "; 主旨(" + title + ")";
		scond += "; 发文人(" + creator + ")";
		scond += "; 提交日期(" + startTime + " 至 " + endTime + ")";
		tmpcond = formStatus.equalsIgnoreCase("ALL") ? "全部" : (localType.equals("DOING") ? "签核中" : "已核准");
		scond += "; 表单状态(" + tmpcond + ")";
		tmpcond = StringUtils.isEmpty(creatCmp) ? "全部" : creatCmp;
		scond += "; 发文单位(" + tmpcond + " : " + rePath + ")";
		tmpcond = StringUtils.isEmpty(leader) ? "全部" : leader;
		scond += "; 核决主管(" + tmpcond + ")";
		//scond += "; 排序字段(" + searchDTO.getOrderBy() + ")";
		//scond += "; 排序方式(" + searchDTO.getSorder() + ")";
		logdDto.setMessages(scond);
		flowManagementService.saveLog(logdDto);

		List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
		List<MyWorkDTO> list = flowManagementService.searchConfidExport(searchDTO);
		if (list != null){
			for(MyWorkDTO myWorkDTO : list){
				/*
				if (myWorkDTO.getFlowType() != null){
					myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
				}
				if (myWorkDTO.getDecionMaker() != null){
					myWorkDTO.setDecionMakerName(myWorkDTO.getDecionMaker().toString());
				}
				*/
				if (myWorkDTO.getFlowType().toString().equalsIgnoreCase("QIANCHENG")){
					myWorkDTO.setFlowTypeName("签呈");
				} else if (myWorkDTO.getFlowType().toString().equalsIgnoreCase("NEILIAN")){
					myWorkDTO.setFlowTypeName("内联");
				} else {
					myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
				}
				if (myWorkDTO.getEndTime() == null){
					myWorkDTO.setEndTime(" ");
				}
				boolean b = myWorkDTO.getFormnum().indexOf("HQTR") >= 0;
				String dispMarkName = "";
				switch (myWorkDTO.getDecionMaker()) {
					case DEPTLEADER:
						if (b) {
							dispMarkName = "单位/部门主管";
						} else {
							dispMarkName = "部门主管";
						}
						break;
					case CENTRALLEADER:
						if (b) {
							dispMarkName = "单位最高主管";
						} else {
							dispMarkName = "中心主管";
						}
						break;
					case UNITLEADER:
						if (b) {
							dispMarkName = "神旺控股执行总经理";
						} else {
							dispMarkName = "单位最高主管";
						}
						break;
					case REGINLEADER:
						dispMarkName = "事业部最高主管";
						break;
					case HEADLEADER:
						dispMarkName = "神旺控股执行总经理";
						break;
					default:
						dispMarkName = myWorkDTO.getDecionMaker().toString();
						break;
				}
				myWorkDTO.setMarkDispName(dispMarkName);
				String oldPath = myWorkDTO.getDeptPath();
				myWorkDTO.setDeptPath(oldPath.substring(oldPath.indexOf("/") + 1));
				result.add(myWorkDTO);
			}
		}
		ExcelExporter excelExporter = new ExcelExporter();
		excelExporter.setTemplatePath("/report/confidEport.xls");
		excelExporter.setData(null, result);
		try {
			excelExporter.export(servletRequest, servletResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return NONE;
	}	
	
	
	@SuppressWarnings("unchecked")
	public String confSearchbossWorks(){
		int pageNo = 1;
		int pageSize = 10;
		int start = 0;
		
		String param = this.servletRequest.getParameter("pageNo");
		if (param != null){
			pageNo = Integer.parseInt(param);
		}
		param = this.servletRequest.getParameter("pageSize");
		if (param != null) {
			pageSize = Integer.parseInt(param);
			start = (pageNo - 1) * pageSize + 1;
		}
		String employeeId = super.getLoginUser().getUserName();
		String tmpCurrentUser = super.getLoginUser().getUserRealName() + "(" + employeeId + ")";
		
		ConfidentialSearchbossDTO searchDTO = new ConfidentialSearchbossDTO();
		searchDTO.setEmployeeId(employeeId);
		
		String flowType = this.servletRequest.getParameter("flowType");
		String localType = this.servletRequest.getParameter("localType");
		String formNum = this.servletRequest.getParameter("formNum");
		String title = this.servletRequest.getParameter("title");
		String creator = this.servletRequest.getParameter("creator");
		String startTime = this.servletRequest.getParameter("startTime");
		String endTime = this.servletRequest.getParameter("endTime");
		String formStatus = this.servletRequest.getParameter("formStatus");
		String creatCmp = this.servletRequest.getParameter("creatCmp");
		String leader = this.servletRequest.getParameter("leader");
		String rePath = this.servletRequest.getParameter("rePath");
		
		String sidx = this.servletRequest.getParameter("sidx");
		String sord = this.servletRequest.getParameter("sord");
		
		if (StringUtils.isNotEmpty(sidx)){
			searchDTO.setOrderBy(sidx);
		} else {
			searchDTO.setOrderBy(Order.CreateTime.name());
		}
		if (StringUtils.isNotEmpty(sord)){
			searchDTO.setSorder(sord);
		} else {
			searchDTO.setSorder(" asc ");
		}
		searchDTO.setFlowType(FlowType.valueOf(flowType));
		if (StringUtils.isNotEmpty(localType)){
			searchDTO.setLocalType(localType);
		}
		if (StringUtils.isNotEmpty(formNum)) {
			searchDTO.setFormNum(formNum);
		}
		if (StringUtils.isNotEmpty(title)) {
			searchDTO.setTitle(title);
		}
		if (StringUtils.isNotEmpty(creator)){
			searchDTO.setCreator(creator);
		}
		searchDTO.setStartTime(startTime);
		searchDTO.setEndTime(endTime);
		searchDTO.setFormStatus(FormStatus.valueOf(formStatus));
		if (StringUtils.isNotEmpty(creatCmp)){
			searchDTO.setCreatCmp(creatCmp);
		}
		if (StringUtils.isNotEmpty(leader)){
			searchDTO.setLeader(leader);
		}
		if (StringUtils.isNotEmpty(rePath)){
			searchDTO.setRePath(rePath);
		}
		
		// 此处需要插入日志，必需记录用户、IP在什么时间，执行了什么操作
	/*	LogDTO logdDto = new LogDTO();
		logdDto.setIP(CommonUtil.getClientIP(servletRequest));
		logdDto.setUser(tmpCurrentUser);
		logdDto.setControl("查询");
		String scond = "查询条件：";
		String tmpcond = flowType.equals("QIANCHENG") ? "签呈" : "内联";
		scond += "表单类型(" + tmpcond + ")";
		tmpcond = StringUtils.isEmpty(localType) ? "全部" : (localType.equals("1") ? "体系内部" : "地方至总部");
		scond += "; 发文类别(" + tmpcond + ")";
		scond += "; 主旨(" + title + ")";
		scond += "; 发文人(" + creator + ")";
		scond += "; 提交日期(" + startTime + " 至 " + endTime + ")";
		tmpcond = formStatus.equalsIgnoreCase("ALL") ? "全部" : (localType.equals("DOING") ? "签核中" : "已核准");
		scond += "; 表单状态(" + tmpcond + ")";
		tmpcond = StringUtils.isEmpty(creatCmp) ? "全部" : creatCmp;
		scond += "; 发文单位(" + tmpcond + " : " + rePath + ")";
		tmpcond = StringUtils.isEmpty(leader) ? "全部" : leader;
		scond += "; 核决主管(" + tmpcond + ")";
		scond += "; 排序字段(" + searchDTO.getOrderBy() + ")";
		scond += "; 排序方式(" + searchDTO.getSorder() + ")";
		scond += "; 当前访问页(" + pageNo + ")";
		logdDto.setMessages(scond);
		flowManagementService.saveLog(logdDto);
		*/
		Page page = flowManagementService.searchConfidbossByPage(searchDTO, start, pageSize);
		List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
		List<MyWorkDTO> list = page.getResult();
		if (list != null){
			for(MyWorkDTO myWorkDTO : list){
				if (myWorkDTO.getFlowType() != null){
					myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
				}
				if (myWorkDTO.getDecionMaker() != null){
					myWorkDTO.setDecionMakerName(myWorkDTO.getDecionMaker().toString());
				}
				result.add(myWorkDTO);
			}
			page.setResult(result);
		}
		String json = VOUtils.getJsonDataFromPage(page, MyWorkDTO.class);
		this.createJSonData(json);
		
		return AJAX;
	}

	public String confbossExportWorks(){	
		String employeeId = super.getLoginUser().getUserName();
		String tmpCurrentUser = super.getLoginUser().getUserRealName() + "(" + employeeId + ")";
		
		ConfidentialSearchbossDTO searchDTO = new ConfidentialSearchbossDTO();
		searchDTO.setEmployeeId(employeeId);
		
		String flowType = this.servletRequest.getParameter("flowType");
		String localType = this.servletRequest.getParameter("localType");
		String formNum = this.servletRequest.getParameter("formNum");
		String title = this.servletRequest.getParameter("title");
		String creator = this.servletRequest.getParameter("creator");
		String startTime = this.servletRequest.getParameter("startTime");
		String endTime = this.servletRequest.getParameter("endTime");
		String formStatus = this.servletRequest.getParameter("formStatus");
		String creatCmp = this.servletRequest.getParameter("creatCmp");
		String leader = this.servletRequest.getParameter("leader");
		String rePath = this.servletRequest.getParameter("rePath");
		System.out.println(rePath);
		String sidx = this.servletRequest.getParameter("sidx");
		String sord = this.servletRequest.getParameter("sord");
		
		if (StringUtils.isNotEmpty(sidx)){
			searchDTO.setOrderBy(sidx);
		} else {
			searchDTO.setOrderBy(Order.CreateTime.name());
		}
		if (StringUtils.isNotEmpty(sord)){
			searchDTO.setSorder(sord);
		} else {
			searchDTO.setSorder(" asc ");
		}
		searchDTO.setFlowType(FlowType.valueOf(flowType));
		if (StringUtils.isNotEmpty(localType)){
			searchDTO.setLocalType(localType);
		}
		if (StringUtils.isNotEmpty(formNum)) {
			searchDTO.setFormNum(formNum);
		}
		if (StringUtils.isNotEmpty(title)) {
			searchDTO.setTitle(title);
		}
		if (StringUtils.isNotEmpty(creator)){
			searchDTO.setCreator(creator);
		}
		searchDTO.setStartTime(startTime);
		searchDTO.setEndTime(endTime);
		searchDTO.setFormStatus(FormStatus.valueOf(formStatus));
		if (StringUtils.isNotEmpty(creatCmp)){
			searchDTO.setCreatCmp(creatCmp);
		}
		if (StringUtils.isNotEmpty(leader)){
			searchDTO.setLeader(leader);
		}
		if (StringUtils.isNotEmpty(rePath)){
			searchDTO.setRePath(rePath);
		}
		
		// 此处需要插入日志，必需记录用户、IP在什么时间，执行了什么操作
	/*	LogDTO logdDto = new LogDTO();
		logdDto.setIP(CommonUtil.getClientIP(servletRequest));
		logdDto.setUser(tmpCurrentUser);
		logdDto.setControl("导出");
		String scond = "导出条件：";
		String tmpcond = flowType.equals("QIANCHENG") ? "签呈" : "内联";
		scond += "表单类型(" + tmpcond + ")";
		tmpcond = StringUtils.isEmpty(localType) ? "全部" : (localType.equals("1") ? "体系内部" : "地方至总部");
		scond += "; 发文类别(" + tmpcond + ")";
		scond += "; 主旨(" + title + ")";
		scond += "; 发文人(" + creator + ")";
		scond += "; 提交日期(" + startTime + " 至 " + endTime + ")";
		tmpcond = formStatus.equalsIgnoreCase("ALL") ? "全部" : (localType.equals("DOING") ? "签核中" : "已核准");
		scond += "; 表单状态(" + tmpcond + ")";
		tmpcond = StringUtils.isEmpty(creatCmp) ? "全部" : creatCmp;
		scond += "; 发文单位(" + tmpcond + " : " + rePath + ")";
		tmpcond = StringUtils.isEmpty(leader) ? "全部" : leader;
		scond += "; 核决主管(" + tmpcond + ")";
		//scond += "; 排序字段(" + searchDTO.getOrderBy() + ")";
		//scond += "; 排序方式(" + searchDTO.getSorder() + ")";
		logdDto.setMessages(scond);
		flowManagementService.saveLog(logdDto);
      */
		List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
		List<MyWorkDTO> list = flowManagementService.searchConfidbossExport(searchDTO);
		if (list != null){
			for(MyWorkDTO myWorkDTO : list){
				/*
				if (myWorkDTO.getFlowType() != null){
					myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
				}
				if (myWorkDTO.getDecionMaker() != null){
					myWorkDTO.setDecionMakerName(myWorkDTO.getDecionMaker().toString());
				}
				*/
				if (myWorkDTO.getFlowType().toString().equalsIgnoreCase("QIANCHENG")){
					myWorkDTO.setFlowTypeName("签呈");
				} else if (myWorkDTO.getFlowType().toString().equalsIgnoreCase("NEILIAN")){
					myWorkDTO.setFlowTypeName("内联");
				} else {
					myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
				}
				if (myWorkDTO.getEndTime() == null){
					myWorkDTO.setEndTime(" ");
				}
				boolean b = myWorkDTO.getFormnum().indexOf("HQTR") >= 0;
				String dispMarkName = "";
				switch (myWorkDTO.getDecionMaker()) {
					case DEPTLEADER:
						if (b) {
							dispMarkName = "单位/部门主管";
						} else {
							dispMarkName = "部门主管";
						}
						break;
					case CENTRALLEADER:
						if (b) {
							dispMarkName = "单位最高主管";
						} else {
							dispMarkName = "中心主管";
						}
						break;
					case UNITLEADER:
						if (b) {
							dispMarkName = "神旺控股执行总经理";
						} else {
							dispMarkName = "单位最高主管";
						}
						break;
					case REGINLEADER:
						dispMarkName = "事业部最高主管";
						break;
					case HEADLEADER:
						dispMarkName = "神旺控股执行总经理";
						break;
					default:
						dispMarkName = myWorkDTO.getDecionMaker().toString();
						break;
				}
				myWorkDTO.setMarkDispName(dispMarkName);
				String oldPath = myWorkDTO.getDeptPath();
				myWorkDTO.setDeptPath(oldPath.substring(oldPath.indexOf("/") + 1));
				result.add(myWorkDTO);
			}
		}
		ExcelExporter excelExporter = new ExcelExporter();
		excelExporter.setTemplatePath("/report/confidEport.xls");
		excelExporter.setData(null, result);
		try {
			excelExporter.export(servletRequest, servletResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return NONE;
	}	
	
}
