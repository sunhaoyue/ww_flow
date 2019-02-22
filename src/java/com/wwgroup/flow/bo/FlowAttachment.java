package com.wwgroup.flow.bo;

import java.io.InputStream;

public class FlowAttachment {

	private long id;
	private long flowId;
	private long createTime;
	private String attachmentName;
	private InputStream attachment;
	private String employeeId;
	private String employeeName;
	
	private byte[] data;	//临时存储数据

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public InputStream getAttachment() {
		return attachment;
	}

	public void setAttachment(InputStream attachment) {
		this.attachment = attachment;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
