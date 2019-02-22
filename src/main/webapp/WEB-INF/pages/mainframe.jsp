<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="taglibs.inc" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>公文处理 - 神旺企业协同办公系统</title>
<script type="text/javascript">
window.moveTo(0,0);
window.resizeTo(screen.availWidth,screen.availHeight);
window.outerWidth = screen.availWidth;
window.outerHeight = screen.availHeight;
</script>
</head>
<frameset rows="42,*" frameborder="no" border="0" framespacing="0">
	<frame src="${ctx }/management/top.action" name="topFrame" scrolling="no" id="topFrame" marginwidth="0" marginheight="0" noresize />
	<frameset cols="181,*" frameborder="no" border="0" framespacing="0">
	  <frame src="${ctx }/WEB-INF/pages/left_menu_load.jsp" name="leftFrame" scrolling="No" noresize="noresize" id="leftFrame" title="leftFrame" />
	  <frame src="${ctx }/WEB-INF/pages/flowmanagement/todoworks.jsp" name="mainFrame" id="mainFrame" title="mainFrame" />
	</frameset>
</frameset>
<noframes><body>
</body>
</noframes></html>
