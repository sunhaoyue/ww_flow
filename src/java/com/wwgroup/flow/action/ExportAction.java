package com.wwgroup.flow.action;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import com.wwgroup.common.action.BaseAction;
import com.wwgroup.common.util.excel.ExcelExporter;
import com.wwgroup.common.vo.SuperUserConstant;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.dto.AdvancedSearchDTO;
import com.wwgroup.flow.dto.Branch;
import com.wwgroup.flow.dto.FormStatus;
import com.wwgroup.flow.dto.LogInfo;
import com.wwgroup.flow.dto.MyWorkDTO;
import com.wwgroup.flow.dto.Order;
import com.wwgroup.flow.service.FlowManagementService;
import com.wwgroup.flow.service.PersonService;

@SuppressWarnings("serial")
public class ExportAction extends BaseAction {

	private FlowManagementService flowManagementService;

	private PersonService personService;
	

	public FlowManagementService getFlowManagementService() {
		return flowManagementService;
	}

	public void setFlowManagementService(FlowManagementService flowManagementService) {
		this.flowManagementService = flowManagementService;
	}

	public PersonService getPersonService() {
		return personService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	protected String operate(){
		/*InputStream excelstStream;
		IExcelService es = new ExcelServiceImpl();
		excelstStream = es.getExcelInputStream();
		return SUCCESS;*/
		
		return SUCCESS;
	}
	
  public String toExcel(){
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
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
		searchDTO.setCloseStartTime(closeStartTime);
		searchDTO.setCloseEndTime(closeEndTime);
		
		List<MyWorkDTO> result = new ArrayList<MyWorkDTO>();
		List<MyWorkDTO> list = flowManagementService.searchWorkDTOExport(searchDTO);
		if (list != null) {
			for (MyWorkDTO myWorkDTO : list) {
				if (myWorkDTO.getFlowType() != null) {
					if (myWorkDTO.getFlowType().toString().equalsIgnoreCase("QIANCHENG")){
						myWorkDTO.setFlowTypeName("签呈");
					} else if (myWorkDTO.getFlowType().toString().equalsIgnoreCase("NEILIAN")){
						myWorkDTO.setFlowTypeName("内联");
					} else {
						myWorkDTO.setFlowTypeName(myWorkDTO.getFlowType().toString());
					}
				}
				if (myWorkDTO.getEndTime() == null){
					myWorkDTO.setEndTime(" ");
				}
				result.add(myWorkDTO);
			}
		}
		
		ExcelExporter excelExporter = new ExcelExporter();
		excelExporter.setTemplatePath("/report/wwflowEport.xls");
		excelExporter.setData(null, result);
		try {
			excelExporter.export(servletRequest, servletResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return NONE;
	} 
	

	public String exportLogInfo(){
		String startDate = this.servletRequest.getParameter("startTime");
		String logLevel = this.servletRequest.getParameter("logLevel");
		List<LogInfo> list = new ArrayList<LogInfo>();
		list = flowManagementService.findLogInfoByExport(startDate, logLevel);
		ExcelExporter excelExporter = new ExcelExporter();
		excelExporter.setTemplatePath("/report/LogInfoExport.xls");
		excelExporter.setData(null, list);
		try {
			excelExporter.export(servletRequest, servletResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return NONE;
	}

}
