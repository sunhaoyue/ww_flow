package com.wwgroup.flow.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.struts2.ServletActionContext;

import com.wwgroup.common.action.BaseAction;
import com.wwgroup.flow.bo.FlowAttachment;
import com.wwgroup.flow.service.FlowService;
import com.wwgroup.flow.service.PersonService;

@SuppressWarnings("serial")
public class UploadAction extends BaseAction {

	@SuppressWarnings("unused")
	private PersonService personService;

	private FlowService flowService;

	/** 文件对象 */
	private File Filedata;

	/** 文件名 */
	private String FiledataFileName;

	/** 文件内容类型 */
	private String FiledataContentType;

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}

	public File getFiledata() {
		return Filedata;
	}

	public void setFiledata(File filedata) {
		Filedata = filedata;
	}

	public String getFiledataFileName() {
		return FiledataFileName;
	}

	public void setFiledataFileName(String filedataFileName) {
		FiledataFileName = filedataFileName;
	}

	public String getFiledataContentType() {
		return FiledataContentType;
	}

	public void setFiledataContentType(String filedataContentType) {
		FiledataContentType = filedataContentType;
	}

	/**
	 * 附件上传
	 * 
	 * @return
	 * @throws Exception
	 */
	public String operate() {
		// 获取登录用户
		String employeeId = super.getLoginUser().getUserName();
		String employeeName = super.getLoginUser().getUserRealName();

		FlowAttachment attachment = null;

		try {
			attachment = new FlowAttachment();
			InputStream is = new FileInputStream(Filedata);
			attachment.setEmployeeId(employeeId);
			attachment.setEmployeeName(employeeName);
			attachment.setAttachmentName(FiledataFileName);
			attachment.setAttachment(is);
			this.flowService.saveFlowAttachment(attachment);
			is.close();
			ServletActionContext.getResponse().getWriter().write(String.valueOf(attachment.getId()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return NONE;
	}

}
