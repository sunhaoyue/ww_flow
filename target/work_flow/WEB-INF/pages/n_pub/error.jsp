<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>系统出错</title>
<script type="text/javascript" src='<%=request.getContextPath()%>/js/jquery-1.7.1.min.js'></script>
</head>

<body>
	<s:if test="errorMessages.size() > 0 || fieldErrors.size() > 0"><s:if test="errorMessages.size() > 0"><s:iterator value="errorMessages" var="curErrorMessage">${curErrorMessage}<br/></s:iterator></s:if><s:else><s:iterator value="fieldErrors" var="curFieldError">${curFieldError.value}<br/></s:iterator></s:else></s:if>
	<br/>
	<a href="javascript:void(0);" title="返回" onclick="window.history.back();return false;">返回</a>
</body>
</html>
