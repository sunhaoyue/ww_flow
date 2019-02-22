package com.wwgroup.common.action;

import com.opensymphony.xwork2.ActionContext;
import com.wwgroup.common.Page;
import com.wwgroup.common.view.AjaxProvider;

@SuppressWarnings("serial")
public class BaseAjaxAction extends AbstractAction implements AjaxProvider {

	private String responseData;

	private String errorResultLocation;

	private boolean ajaxSuccess = false;

	public String getErrorResultLocation() {
		return errorResultLocation;
	}

	public void setErrorResultLocation(String errorResultLocation) {
		this.errorResultLocation = errorResultLocation;
	}

	public String getResponseData() {
		return responseData;
	}

	/**
	 * @param responseData
	 *            the responseData to set
	 */
	public void setResponseData(String responseData) {
		this.responseData = responseData;
	}

	public boolean isAjaxSuccess() {
		return ajaxSuccess;
	}

	public void setAjaxSuccess(boolean ajaxSuccess) {
		this.ajaxSuccess = ajaxSuccess;
	}

	public boolean hasAjaxErrors() {
		String object = (String) ActionContext.getContext().get(
				AJAX_ERRORS_FLAG);
		return Boolean.parseBoolean(object);
	}

	/**
	 * @see com.opensymphony.xwork2.ActionSupport#execute()
	 */
	@Override
	public String execute() throws Exception {
		String result = operate();
		createJSonData(result);
		return AJAX;
	}

	@Override
	protected String operate() {
		return NONE;
	}

	/**
	 * 设置Ajax响应数据，JSON格式
	 * 
	 * @param jsonData
	 *            json数据
	 */
	protected void createJSonData(String jsonData) {
		this.setResponseData(jsonData);
	}

	/**
	 * 每页的记录数
	 */
	protected Integer getPageSize() {
		String limit = servletRequest.getParameter("limit");
		if (limit != null) {
			return Integer.parseInt(limit);
		} else {
			return Page.DEFAULT_PAGE_SIZE;
		}
	}

	/**
	 * 当前页第一条数据在数据库中的位置
	 * 
	 * @return
	 */
	protected Integer getPageStart() {
		String start = servletRequest.getParameter("start");
		if (start != null) {
			return Integer.parseInt(start);
		} else {
			return 0;
		}
	}

	@Override
	public Object getModel() {
		// TODO Auto-generated method stub
		return null;
	}

}
