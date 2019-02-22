package com.wwgroup.flow.action;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.common.action.BaseAction;
import com.wwgroup.common.util.CommonUtil;
import com.wwgroup.flow.bo.FlowAttachment;
import com.wwgroup.flow.service.FlowService;

@SuppressWarnings("serial")
public class DownLoadAction extends BaseAction {
	
	// 文件名参数变量
	private String filename;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	private FlowAttachment flowattachment = new FlowAttachment();

	
	public FlowAttachment getFlowattachment() {
		return flowattachment;
	}

	public void setFlowattachment(FlowAttachment flowattachment) {
		this.flowattachment = flowattachment;
	}

	@Override
	public Object getModel() {
		return this.flowattachment;
	}
	
	private FlowService flowService;

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}
	

	protected String operate() {
		return SUCCESS;
	}

	public InputStream getDownloadFile() throws FileNotFoundException {
		String id = (String) this.servletRequest.getParameter("id");
		if(StringUtils.isNotEmpty(id));{
			flowattachment = flowService.loadFlowAttachment(Long.valueOf(id));
		}
		filename = flowattachment.getAttachmentName();
		
		InputStream is = CommonUtil.byteToInputStream(flowattachment.getData());
		
		return is;
	}

	// 如果下载文件名为中文，进行字符编码转换
	public String getDownloadChineseFileName() {
		String downloadChineseFileName = filename;

		try {
			downloadChineseFileName = new String(downloadChineseFileName
					.getBytes(), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return downloadChineseFileName;
	}
}
