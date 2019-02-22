package com.wwgroup.flow.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.wwgroup.flow.service.IExcelService;

public class ExcelServiceImpl implements IExcelService {
	
	@Override
	public InputStream getExcelInputStream(){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pubData2Stream(out);
		return new ByteArrayInputStream(out.toByteArray());
	}

	private void pubData2Stream(OutputStream os){
		Label label;
		WritableWorkbook workbook;
		try {
			workbook = Workbook.createWorkbook(os);
			WritableSheet sheet = workbook.createSheet("Sheet1", 0);
			
			label = new Label(0, 0, "struts2导出excel");
			sheet.addCell(label);
			
			workbook.write();
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
