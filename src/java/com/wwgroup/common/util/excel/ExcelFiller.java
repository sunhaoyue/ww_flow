package com.wwgroup.common.util.excel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jxl.Cell;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


import com.wwgroup.common.metatype.BaseDomain;
import com.wwgroup.common.util.CommonUtil;

public class ExcelFiller {
	private static Logger logger = Logger.getLogger(ExcelFiller.class);

	private ExcelTemplate excelTemplate = null;
	private ExcelData excelData = null;
	
	/**
	 * Excel模板数据类型 number:数字类型
	 */
	private static final String ExcelTPL_DataType_Number = "number";

	/**
	 * Excel模板数据类型 number:文本类型
	 */
	private static final String ExcelTPL_DataType_Label = "label";

	public ExcelFiller() {
	}

	public ExcelFiller(ExcelTemplate excelTemplate, ExcelData excelData) {
		setExcelData(excelData);
		setExcelTemplate(excelTemplate);
	}

	public ByteArrayOutputStream fill(HttpServletRequest request) {
		WritableSheet wSheet = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			InputStream is = request.getSession().getServletContext()
					.getResourceAsStream(getExcelTemplate().getTemplatePath());
			Workbook wb = Workbook.getWorkbook(is);
			WritableWorkbook wwb = Workbook.createWorkbook(bos, wb);
			wSheet = wwb.getSheet(0);
			fillStatics(wSheet);
			fillParameters(wSheet);
			fillFields(wSheet);

			wwb.write();
			wwb.close();
			wb.close();
		} catch (Exception e) {
			logger.error("基于模板生成可写工作表出错了!", e);
		}
		return bos;
	}

	/**
	 * 写入静态对象
	 */
	@SuppressWarnings("rawtypes")
	private void fillStatics(WritableSheet wSheet) {
		List statics = getExcelTemplate().getStaticObject();
		for (int i = 0; i < statics.size(); i++) {
			Cell cell = (Cell) statics.get(i);
			WritableCellFormat cellFormat = new WritableCellFormat(cell.getCellFormat());
			Label label = new Label(cell.getColumn(), cell.getRow(),
					cell.getContents());
			label.setCellFormat(cellFormat);
			try {
				wSheet.addCell(label);
			} catch (Exception e) {
				logger.error("写入静态对象发生错误!", e);
			}
		}
	}

	/**
	 * 写入参数对象
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private void fillParameters(WritableSheet wSheet) {
		List parameters = getExcelTemplate().getParameterObjct();
		Map parameterDto = getExcelData().getParametersMap();
		for (int i = 0; i < parameters.size(); i++) {
			Cell cell = (Cell) parameters.get(i);
			WritableCellFormat cellFormat = new WritableCellFormat(cell.getCellFormat());
			String key = getKey(cell.getContents().trim());
			String type = getType(cell.getContents().trim());
			try {
				if (type.equalsIgnoreCase(ExcelTPL_DataType_Number)) {
					Number number = new Number(cell.getColumn(), cell.getRow(),
							(Double) parameterDto.get(key));
					number.setCellFormat(cellFormat);
					wSheet.addCell(number);
				} else {
					Label label = new Label(cell.getColumn(), cell.getRow(),
							(String) parameterDto.get(key));
					label.setCellFormat(cellFormat);
					wSheet.addCell(label);
				}
			} catch (Exception e) {
				logger.error("写入表格参数对象发生错误!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 写入表格字段对象
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void fillFields(WritableSheet wSheet) throws Exception {
		List fields = getExcelTemplate().getFieldObjct();
		List fieldList = getExcelData().getFieldsList();
		for (int j = 0; j < fieldList.size(); j++) {
			Map dataMap = new HashMap();
			Object object = fieldList.get(j);
			if (object instanceof BaseDomain) {
				BaseDomain domain = (BaseDomain) object;
				dataMap.putAll(domain.toMap());
			} else if (object instanceof Map) {
				Map esMap = (HashMap) object;
				dataMap.putAll(esMap);
			} else {
				CommonUtil.copyPropFromBean2Map(object, dataMap);
			}
			for (int i = 0; i < fields.size(); i++) {
				Cell cell = (Cell) fields.get(i);
				WritableCellFormat cellFormat = new WritableCellFormat(cell.getCellFormat());
				//System.out.println(cellFormat.getFont().getName() + " # " + cellFormat.getFont().getPointSize());
				String key = getKey(cell.getContents().trim());
				String type = getType(cell.getContents().trim());
				try {
					if (type.equalsIgnoreCase(ExcelTPL_DataType_Number)) {
						Number number = new Number(cell.getColumn(),
								cell.getRow() + j, (Double) dataMap.get(key));
						number.setCellFormat(cellFormat);
						wSheet.addCell(number);
					} else {
						Label label = new Label(cell.getColumn(), cell.getRow()
								+ j, (String) dataMap.get(key));
						label.setCellFormat(cellFormat);
						wSheet.addCell(label);
					}
				} catch (Exception e) {
					logger.error("写入表格字段对象发生错误!");
					e.printStackTrace();
				}
			}
		}
		int row = 0;
		row += fieldList.size();
		if (fieldList == null || fieldList.size() < 1) {
			if (fields != null && fields.size() > 0) {
				Cell cell = (Cell) fields.get(0);
				row = cell.getRow();
				wSheet.removeRow(row + 5);
				wSheet.removeRow(row + 4);
				wSheet.removeRow(row + 3);
				wSheet.removeRow(row + 2);
				wSheet.removeRow(row + 1);
				wSheet.removeRow(row);
			}
		} else {
			Cell cell = (Cell) fields.get(0);
			row += cell.getRow();
			fillVariables(wSheet, row);
		}
	}

	/**
	 * 写入变量对象
	 */
	@SuppressWarnings("rawtypes")
	private void fillVariables(WritableSheet wSheet, int row) {
		List variables = getExcelTemplate().getVariableObject();
		Map parameterDto = getExcelData().getParametersMap();
		for (int i = 0; i < variables.size(); i++) {
			Cell cell = (Cell) variables.get(i);
			WritableCellFormat cellFormat = new WritableCellFormat(cell.getCellFormat());
			String key = getKey(cell.getContents().trim());
			String type = getType(cell.getContents().trim());
			try {
				if (type.equalsIgnoreCase(ExcelTPL_DataType_Number)) {
					Number number = new Number(cell.getColumn(), row,
							(Double) parameterDto.get(key));
					number.setCellFormat(cellFormat);
					wSheet.addCell(number);
				} else {
					String content = (String) parameterDto.get(key);
					if (StringUtils.isEmpty(content)
							&& !key.equalsIgnoreCase("nbsp")) {
						content = key;
					}
					Label label = new Label(cell.getColumn(), row, content);
					label.setCellFormat(cellFormat);
					wSheet.addCell(label);
				}
			} catch (Exception e) {
				logger.error("写入表格变量对象发生错误!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取模板键名
	 * 
	 * @param pKey
	 *            模板元标记
	 * @return 键名
	 */
	private static String getKey(String pKey) {
		String key = null;
		int index = pKey.indexOf(":");
		if (index == -1) {
			key = pKey.substring(3, pKey.length() - 1);
		} else {
			key = pKey.substring(3, index - 1);
		}
		return key;
	}

	/**
	 * 获取模板单元格标记数据类型
	 * 
	 * @param pType
	 *            模板元标记
	 * @return 数据类型
	 */
	private static String getType(String pType) {
		String type = ExcelTPL_DataType_Label;
		if (pType.indexOf(":n") != -1 || pType.indexOf(":N") != -1) {
			type = ExcelTPL_DataType_Number;
		}
		return type;
	}

	public ExcelTemplate getExcelTemplate() {
		return excelTemplate;
	}

	public void setExcelTemplate(ExcelTemplate excelTemplate) {
		this.excelTemplate = excelTemplate;
	}

	public ExcelData getExcelData() {
		return excelData;
	}

	public void setExcelData(ExcelData excelData) {
		this.excelData = excelData;
	}
}
