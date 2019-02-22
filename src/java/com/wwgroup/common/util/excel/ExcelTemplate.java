package com.wwgroup.common.util.excel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class ExcelTemplate {
	private static Logger logger = Logger.getLogger(ExcelTemplate.class);

	@SuppressWarnings("rawtypes")
	private List staticObject = null;
	@SuppressWarnings("rawtypes")
	private List parameterObjct = null;
	@SuppressWarnings("rawtypes")
	private List fieldObjct = null;
	@SuppressWarnings("rawtypes")
	private List variableObject = null;
	private String templatePath = null;

	public ExcelTemplate(String templatePath) {
		this.templatePath = templatePath;
	}

	public ExcelTemplate() {
	}

	/**
	 * 解析Excel模板
	 * 
	 * @param request
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void parse(HttpServletRequest request) {
		this.staticObject = new ArrayList();
		this.parameterObjct = new ArrayList();
		this.fieldObjct = new ArrayList();
		this.variableObject = new ArrayList();
		if (StringUtils.isEmpty(this.templatePath)) {
			logger.error("Excel模板路径不能为空!");
			return;
		}
		InputStream is = request.getSession().getServletContext()
				.getResourceAsStream(this.templatePath);
		if (is == null) {
			logger.error("未找到模板文件，请确认模板文件路径是否正确["
					+ this.templatePath + "]");
			return;
		}
		Workbook workbook = null;
		try {
			workbook = Workbook.getWorkbook(is);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		Sheet sheet = workbook.getSheet(0);
		if (sheet != null) {
			int rows = sheet.getRows();
			for (int k = 0; k < rows; k++) {
				Cell[] cells = sheet.getRow(k);
				for (int j = 0; j < cells.length; j++) {
					String cellContent = cells[j].getContents().trim();
					if (!StringUtils.isEmpty(cellContent)) {
						if (cellContent.indexOf("$P") != -1
								|| cellContent.indexOf("$p") != -1) {
							this.parameterObjct.add(cells[j]);
						} else if (cellContent.indexOf("$F") != -1
								|| cellContent.indexOf("$f") != -1) {
							this.fieldObjct.add(cells[j]);
						} else if (cellContent.indexOf("$V") != -1
								|| cellContent.indexOf("$v") != -1) {
							this.staticObject.add(cells[j]);
						}
					}
				}
			}
		} else {
			logger.error("模板工作表对象不能为空!");
		}
	}

	/**
	 * 增加一个静态文本对象
	 */
	@SuppressWarnings("unchecked")
	public void addStaticObject(Cell cell) {
		this.staticObject.add(cell);
	}

	/**
	 * 增加一个参数对象
	 */
	@SuppressWarnings("unchecked")
	public void addParameterObjct(Cell cell) {
		this.parameterObjct.add(cell);
	}

	/**
	 * 增加一个字段对象
	 */
	@SuppressWarnings("unchecked")
	public void addFieldObjct(Cell cell) {
		this.fieldObjct.add(cell);
	}

	@SuppressWarnings("rawtypes")
	public List getStaticObject() {
		return this.staticObject;
	}

	@SuppressWarnings("rawtypes")
	public List getParameterObjct() {
		return this.parameterObjct;
	}

	@SuppressWarnings("rawtypes")
	public List getFieldObjct() {
		return this.fieldObjct;
	}

	public String getTemplatePath() {
		return this.templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	@SuppressWarnings("rawtypes")
	public List getVariableObject() {
		return this.variableObject;
	}
}
