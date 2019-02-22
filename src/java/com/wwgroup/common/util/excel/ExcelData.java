package com.wwgroup.common.util.excel;

import java.util.List;
import java.util.Map;

/**
 * Excel数据对象
 */
public class ExcelData {
	/**
	 * Excel参数数据对象
	 */
	private Map parametersMap;
	/**
	 * Excel集合对象
	 */
	@SuppressWarnings("rawtypes")
	private List fieldsList;

	/**
	 * 构造函数
	 * 
	 * @param paramMap
	 * @param paramList
	 */
	@SuppressWarnings("rawtypes")
	public ExcelData(Map paramMap, List paramList) {
		setParametersMap(paramMap);
		setFieldsList(paramList);
	}

	public Map getParametersMap() {
		return parametersMap;
	}

	public void setParametersMap(Map parametersMap) {
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
}
