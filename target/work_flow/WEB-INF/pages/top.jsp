<%@page import="java.util.Date"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%
String nowtime = (String)request.getAttribute("nowtime");
String root = request.getContextPath();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>签呈申请单</title>
<link href="<%=root%>/css/2.0/style.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=root%>/js/jquery-1.11.0.min.js" charset="utf-8"></script>
<script type="text/javascript" src="<%=root%>/js/itExtJquery.js" charset="utf-8"></script>
<script type="text/javascript">
$(function(){
	var nowtime = "<%=nowtime%>";
	var myDate= new Date(Date.parse(nowtime.replace(/-/g, "/")));
	var timer = $('#clock');
	var i = 0;
	interval = function(){
		var nowtime = $.date.add(myDate, "s", i * 1 + 1);
		timer.text($.date.toLongDateTimeString(nowtime));
		i++;
	};
	interval();
	window.setInterval(interval, 1000);
});
</script>
</head>
<body>
<div class="topHeader">
	<div class="header-box">
		<h1 class="logo"></h1>
		<ul id="nav" class="nav"></ul>
		<div class="nav-right">
			<div class="info">
			<div style="position:absolute;right:10px;top:5px;font-size:12px;">
				<i class="icon-0"></i><span id="clock"></span>
			</div>
			<div style="position:absolute;right:10px;top:25px;">
				<i class="icon-1"></i><span>您好，<s:property value="#request.curPerson.name" /><s:property value="#request.curPerson.titname" /></span>
			</div>
			</div>
		</div>
	</div>
</div>
<iframe width="0" height="0" src="<%=root%>/pages/sessiontimer.jsp"></iframe>
</body>
</html>
