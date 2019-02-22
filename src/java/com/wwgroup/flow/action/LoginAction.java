package com.wwgroup.flow.action;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.Cookie;

import org.apache.struts2.ServletActionContext;

import com.wwgroup.common.action.BaseAction;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.service.UserService;

public class LoginAction extends BaseAction {
	private static final long serialVersionUID = 9090067914892408108L;
	private UserService userService;
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String operate() {

		// 这里一般是工号
//		String userName = (String) this.servletRequest
//				.getParameter("loginName");
//
//		SystemUsers user = userService.getUsersByName(userName);
//		Enumeration enu = this.servletRequest.getSession().getAttributeNames();
//		while(enu.hasMoreElements()){
//			String key = enu.nextElement().toString();
//			String value = this.servletRequest.getSession().getAttribute(key).toString();
//			System.out.println("session里面的内容,key:"+key+",value:"+value);
//		}
//		String loginName = this.servletRequest.getSession().getAttribute("hotlong.cas.user").toString();
//		System.out.println("登录名:"+loginName);
//		logger.info("登录名："+loginName);
		String loginName = null;
		try {
			// TODO 真实环境中应该直接读取获取cookie为 HOTDATA_USERNAME的字段
			// 设置Cookie
			
			Cookie[] cookies = null;
			
			if (this.servletRequest != null) {
				cookies = this.servletRequest.getCookies();
			} else {
				cookies = ServletActionContext.getRequest().getCookies();
			}
			Cookie readcookie = null;
			for (int i = 0; i < cookies.length; i++) {
				readcookie = cookies[i];
				if (readcookie.getName().equals("HOTOA.USERNAME")) {
//				if (readcookie.getName().equals("loginName")) {
					loginName = URLDecoder.decode(readcookie.getValue(),
							"utf-8");
//					System.out.println("cookie= " + loginName);
//					readcookie.setMaxAge(0);
//					readcookie.setPath("/");
//					this.servletResponse.addCookie(readcookie);
				}
			}
//			String value = URLEncoder.encode(userName, "utf-8");
//			Cookie cookie = new Cookie("userName", value);
//			cookie.setMaxAge(60 * 60 * 24 * 6);
//			this.servletResponse.addCookie(cookie);

		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SystemUsers user = userService.getUsersByName(loginName);

		String userRealName = user.getFullName().substring(0,
				user.getFullName().indexOf("("));

		if (user != null) {
			this.getSession().put("employeeId", loginName);
			this.getSession().put("name", userRealName);
		}
		return SUCCESS;
	}
}
