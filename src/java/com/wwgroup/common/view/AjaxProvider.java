package com.wwgroup.common.view;

/**
 * AJAX Action特性
 * 
 * 
 */
public interface AjaxProvider {

	/**
	 * AJAX SUCCESS RESULT NAME
	 */
	static final String AJAX = "ajax";

	/**
	 * AJAX FAILURE RESULT NAME
	 */
	static final String AJAX_ERROR = "ajaxError";

	/**
	 * AJAX FAILURE FLAG
	 */
	static final String AJAX_ERRORS_FLAG = "com.WWGroup.web.ajax.errors.flag";

	/**
	 * @return
	 */
	String getResponseData();

	/**
	 * @param responseData
	 */
	void setResponseData(String responseData);

	/**
	 * @return
	 */
	boolean hasAjaxErrors();

	/**
	 * @return
	 */
	String getErrorResultLocation();

	/**
	 * @param errorResultLocation
	 */
	void setErrorResultLocation(String errorResultLocation);

	/**
	 * @return
	 */
	boolean isAjaxSuccess();

	/**
	 * @param ajaxSuccess
	 */
	void setAjaxSuccess(boolean ajaxSuccess);

}
