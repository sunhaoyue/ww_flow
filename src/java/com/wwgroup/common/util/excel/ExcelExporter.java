package com.wwgroup.common.util.excel;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExcelExporter {

	private String templatePath;
	@SuppressWarnings("rawtypes")
	private Map parametersMap;
	@SuppressWarnings("rawtypes")
	private List fieldsList;
	private String filename = "exportExcel.xls";
	
	/**
	 * 设置数据
	 * @param paramMap
	 * @param paramList
	 */
	@SuppressWarnings("rawtypes")
	public void setData(Map paramMap, List paramList){
		this.parametersMap = paramMap;
		this.fieldsList = paramList;
	}
	
	public void export(HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("application/vnd.ms-excel");
		filename = getFilename();
		response.setHeader("Content-Disposition", "attachment; filename="
				+ filename + ";");
		ExcelData excelData = new ExcelData(this.parametersMap, fieldsList);
		ExcelTemplate excelTemplate = new ExcelTemplate();
		excelTemplate.setTemplatePath(getTemplatePath());
		excelTemplate.parse(request);
		ExcelFiller excelFiller = new ExcelFiller(excelTemplate, excelData);
		ByteArrayOutputStream bos = excelFiller.fill(request);
		ServletOutputStream os = response.getOutputStream();
		os.write(bos.toByteArray());
		os.flush();
		os.close();
	}
	
	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	@SuppressWarnings("rawtypes")
	public Map getParametersMap() {
		return this.parametersMap;
	}

	@SuppressWarnings("rawtypes")
	public void setParametersDto(Map parametersMap) {
		this.parametersMap = parametersMap;
	}

	@SuppressWarnings("rawtypes")
	public List getFieldsList() {
		return fieldsList;
	}

	@SuppressWarnings("rawtypes")
	public void setFieldsList(List fieldsList) {
		this.fieldsList = fieldsList;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
