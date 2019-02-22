<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page isErrorPage="true" %>
<%@page import="java.text.SimpleDateFormat"%>
<%@ page import="java.io.*"%>
<%@ page isErrorPage="true"%>
<%
response.setStatus(HttpServletResponse.SC_OK);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" >
<head>
<title>500 Internal Server Error</title>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery-1.7.1.min.js"></script>
<style type="text/css">
body{
	font-size:12px;color:#4d4b4b;
}
</style>
<script>
function showErrorMessage(){
	$("#divexception").toggle();
}
//$(document).ready(showErrorMessage);
</script>
</head>
<body>
<table width="90%" border="1" cellpadding="0" cellspacing="0" align="center">
	<tr>
		<td valign="top" bgcolor="#D6E2F2">
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td height="25">
					异常信息
					<a href="javascript:;" onclick="showErrorMessage();">查看详情</a>
					</td>
				</tr>
				<tr>
					<td height="80" bgcolor="#FFFFFF">
						<table width="100%" border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td width="20%" height="24" align="center">
									系统异常，请联系管理员。
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td align="left">
						<div style="display:none;hight:60%" id="divexception">
							<pre>
							<%
							try{
								ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
								PrintStream printStream = new PrintStream(byteArrayOutputStream);
								
								printStream.println();
								printStream.println("出错用户：" + request.getSession().getAttribute("employeeId"));
								printStream.println("访问的路径: " + request.getAttribute("javax.servlet.forward.request_uri"));
								printStream.println("异常信息：" + exception.getClass() + ":" + exception.getMessage());
								printStream.println();
								
								Enumeration<String> e = request.getParameterNames();
								if (e.hasMoreElements()) {
									printStream.println("请求中的Parameter包括：");
									while (e.hasMoreElements()) {
										String key = e.nextElement();
										if (!key.contains("detail") && !key.contains("hidden")) {
											printStream.println(key + " = " + request.getParameter(key));
										}
									}
									printStream.println();
								}
								printStream.println("堆栈信息：");
								exception.printStackTrace(printStream);
								printStream.println();
								out.print(byteArrayOutputStream);
								
								File dir = new File(request.getRealPath("/errorLog"));
								if (!dir.exists()) {
									dir.mkdir();
								}
								String timeStamp = new SimpleDateFormat("yyyyMMdd-hhmmssS").format(new Date());
								FileOutputStream fileOutputStream = new FileOutputStream(new File(dir.getAbsolutePath() + File.separatorChar + "error-" + timeStamp + "-" + request.getSession().getAttribute("employeeId") + ".txt"));
								new PrintStream(fileOutputStream).print(byteArrayOutputStream);
								fileOutputStream.close();
								
							} catch (Exception ex) {
							    ex.printStackTrace();
							}
							%>
							</pre>
						</div>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</body>
</html>