<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Calendar"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ include file="/pages/taglibs.inc" %>
<%
Calendar calendar = Calendar.getInstance();
SimpleDateFormat df = new SimpleDateFormat("yyyy年M月d日");
String date = df.format(calendar.getTime());
String[] weekDays = new String[]{"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};
int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
if (w < 0) w = 0;
String week = weekDays[w];
String disp = "今天是" + date + "&nbsp;&nbsp;" + week;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<html>
<head>
<title>登录</title>
<link href="${ctx }/css/2.0/login.css" type="text/css" rel="stylesheet" />
<script type="text/javascript" src='${ctx }/js/jquery-1.7.1.min.js'></script>
<script type="text/javascript">
var context = "${ctx}";
var tdheight;

function addCookie(objName,objValue){//添加cookie
	var str = objName + "=" + escape(objValue);
	document.cookie = str;
	//alert("添加cookie成功");
}

function getCookie(name){
	var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
	if (arr = document.cookie.match(reg))
		return unescape(arr[2]);
	else
		return null;
}
	
	//提交表单
function submitForm() {
	var cookie_name = "HOTOA.USERNAME";
	var cookie_value = document.getElementById('loginName').value;
	//alert(cookie_value);
	addCookie(cookie_name,cookie_value);
       loginForm.submit();
}
   
function setLoginName(){
   	alert("111");
}

function getTdHeight() {
    tdheight = $(window).height();
    $("#toptd").css('height', tdheight * 0.15);
    $("#tableheight").css('height', tdheight);
}
$(document).ready(function() {
	//alert(getCookie("HOTOA.USERNAME"));
    getTdHeight();
    var cookieUser = getCookie("HOTOA.USERNAME");
    if (cookieUser != null && cookieUser != ""){
    	$('#loginName').val(cookieUser);
    }
});

window.document.onkeydown = function(e){
	var evt = e ? e : (window.event ? window.event : null); // 此方法为了在firefox中的兼容
	var keycode = evt.keyCode ? evt.keyCode : evt.which ? evt.whick : evt.charCode;
	if (keycode == 13){
		//if (document)
		submitForm();
	}
}
</script>
</head>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" class="bg_1">
<form name="loginForm" action="${ctx }/login.action" method="post">
<table width="100%" border="0" cellspacing="0" cellpadding="0" id="tableheight" style="height: 609px;">
	<tr>
		<td id="toptd" style="height: 121px;"></td>
	</tr>
	<tr>
		<td id="middletd">
			<div class="bg_middle">
				<table align="center">
					<tr>
						<td>
							<div class="login">
								<div class="login_L">
									<div class="login_1">
										<ul>
											<li>
												<em class="time"><%=disp %></em>
											</li>
											<li>
												<span>选择颜色</span>
												<p class="l_y_y_1">
												<img src="${ctx }/images/login/login_color_selected_blue.gif" alt="" />
												</p>
											</li>
											<li class="l_sty_1">
												<span>语言选择</span>
												<p id="l_y_y_1">
													<img id="selectedLanguageSrc" src="${ctx }/images/login/login_y_y_CN.png" style="display: block;">
													<em id="selectedLanguage" style="display: block;">简体中文</em>
												</p>
											</li>
										</ul>
									</div>
									<div class="login_2">
										<strong>温馨提示：</strong>
										<p>输入有效工号即可登录。</p>
									</div>
								</div>
								<div class="login_R">
									<img class="l_logo" src="${ctx }/images/login/login_ww.png" /><h1>公文管理系统</h1>
									<ul>
										<li>
											<span>用户名</span>
											<input size="20" class="TextField" id="loginName" type="text" value="91011001" name="loginName" />
										</li>
										<li>
											<span>密码</span>
											<input size="20" class="TextField" id="password" type="password" name="password" />
										</li>
										<li></li>
										<li>
											<span>&nbsp;</span>
											<img src="${ctx }/images/login/userLogin_button.png" alt="" onclick="submitForm();" border="0" style="cursor:pointer" />
										</li>
									</ul>
								</div>
							</div>
						</td>
					</tr>
				</table>
			</div>
		</td>
	</tr>
	<tr>
		<td class="bg_foot" align="center">
			<div class="login_3">
				神旺控股版权所有	2014&copy;
			</div>
		</td>
	</tr>
</table>
</form>
</body>
</html>