package com.wwgroup.common.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

@SuppressWarnings({ "unused", "serial", "rawtypes" })
public abstract class AbstractAction extends ActionSupport implements ModelDriven, SessionAware, ParameterAware,
		ServletRequestAware, ServletResponseAware {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	protected Map session;

	protected Map parameters;

	protected HttpServletRequest servletRequest;

	protected HttpServletResponse servletResponse;

	protected abstract String operate();
	
	/**
	 * @return the session
	 */
	public Map getSession() {
		return session;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.apache.struts2.interceptor.SessionAware#setSession(java.util.Map)
	 */
	public void setSession(Map session) {
		this.session = session;
	}

	/**
	 * @return the parameters
	 */
	public Map getParameters() {
		return parameters;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.apache.struts2.interceptor.ParameterAware#setParameters(java.util.Map)
	 */
	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return servletRequest
	 */
	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	/**
	 * @return servletResponse
	 */
	public HttpServletResponse getServletResponse() {
		return servletResponse;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.opensymphony.webwork.interceptor.ServletRequestAware#setServletRequest(javax.servlet.http.HttpServletRequest)
	 */
	public void setServletRequest(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.opensymphony.webwork.interceptor.ServletResponseAware#setServletResponse(javax.servlet.http.HttpServletResponse)
	 */
	public void setServletResponse(HttpServletResponse servletResponse) {
		this.servletResponse = servletResponse;
	}

	/**
	 * 获取登录用户
	 * 
	 * @return
	 */
	public SystemUsers getLoginUser() {
		// 真实环境中应该直接读取获取cookie为 HOTDATA_USERNAME的字段
		String employeeId = (String) this.session.get("employeeId");
		String userRealName = (String) this.session.get("name");
		SystemUsers user = new SystemUsers();
		user.setUserName(employeeId);
		user.setUserRealName(userRealName);
		return user;
	}

	/**
	 * 将组织拆分为单位+中心+部门，供前台显示
	 * 
	 * @param deptPath
	 */
	public void splitDeptPath(String deptPath) {
		if (deptPath != null) {
			String[] depts = deptPath.split("/");
			for (int i = 0; i < depts.length; i++) {
				String dept = depts[i];
				this.servletRequest.setAttribute("dept" + i, dept);
			}
		}
	}
}
