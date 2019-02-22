<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ include file="taglibs.inc" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>签呈申请单</title>
<link href="${ctx }/css/2.0/style.css" rel="stylesheet" type="text/css" />
</head>
<body style="background:#f5f8fd;">
<div class="left-sidebar">
	<div id="sidebar-nav" class="sidebar-nav">
		<div class="list-box">
			<div class="headbg"><a href="javascript:void(0);" onclick="javascript:parent.location.href='/portal'">返回首页</a></div>
			<ul>
				<li>
					<a class="item" href="${ctx }/WEB-INF/pages/flowmanagement/advancedsearch.jsp" target="mainFrame">
						<div class="arrow"></div>
						<i class="icon-0"></i>
						<span>高级查询</span>
					</a>
				</li>
				<li>
					<a class="item <s:if test="#request.menu == null">selected</s:if>" href="${ctx }/WEB-INF/pages/flowmanagement/todoworks.jsp" target="mainFrame">
						<div class="arrow"></div>
						<i class="icon-1"></i>
						<span>待审核表单&nbsp;
						(<s:if test="#request.myToDoWorkCount!=null"><s:property value="#request.myToDoWorkCount"/></s:if><s:else>0</s:else>)
						</span>
					</a>
				</li>
				<li>
					<a class="item <s:if test="#request.menu != null">selected</s:if>" href="${ctx }/WEB-INF/pages/flowmanagement/flowtemplates.jsp" target="mainFrame">
						<div class="arrow"></div>
						<i class="icon-2"></i>
						<span>暂存表单&nbsp;
						(<s:if test="#request.flowTemplatesCount!=null"><s:property value="#request.flowTemplatesCount"/></s:if><s:else>0</s:else>)
						</span>
					</a>
				</li>
				<li>
					<a class="item" href="${ctx }/WEB-INF/pages/flowmanagement/myProcessFlows.jsp" target="mainFrame">
						<div class="arrow"></div>
						<i class="icon-3"></i>
						<span>已提交表单&nbsp;
						(<s:if test="#request.myProcessFlowCount!=null"><s:property value="#request.myProcessFlowCount"/></s:if><s:else>0</s:else>)
						</span>
					</a>
				</li>
				<li>
					<a class="item" href="${ctx }/WEB-INF/pages/flowmanagement/creatorFlows.jsp" target="mainFrame">
						<div class="arrow"></div>
						<i class="icon-4"></i>
						<span>已审核表单&nbsp;
						(<s:if test="#request.creatorFlowCount!=null"><s:property value="#request.creatorFlowCount"/></s:if><s:else>0</s:else>)
						</span>
					</a>
				</li>
				<li>
					<a class="item" href="${ctx }/WEB-INF/pages/flowmanagement/copyFlows.jsp" target="mainFrame">
						<div class="arrow"></div>
						<i class="icon-4"></i>
						<span>抄送表单&nbsp;
						(<s:if test="#request.copyFlowCount!=null"><s:property value="#request.copyFlowCount"/></s:if><s:else>0</s:else>)
						</span>
					</a>
				</li>
				<li id="qianchenglink_n">
					<a class="item" href="${ctx }/nqianchen/qianChenAction!loadQianChenDetail.action" target="mainFrame">
						<div class="arrow"></div>
						<i class="icon-5"></i><span>签呈申请</span>
					</a>
				</li>
				<li id="neilianlink_n">
					<a class="item" href="${ctx }/nneilian/neiLianAction!loadNeiLianDetail.action" target="mainFrame">
						<div class="arrow"></div>
						<i class="icon-6"></i><span>内联申请</span>
					</a>
				</li>
				<s:if test="#request.canAgent==1">
				<li>
					<a class="item" href="${ctx }/WEB-INF/pages/agent/agent_query.jsp" target="mainFrame">
						<div class="arrow"></div>
						<i class="icon-7"></i>
						<span>流程代理人管理</span>
					</a>
				</li>
				</s:if>
				<s:if test="#request.canAssist==1">
				<li>
					<a class="item" href="${ctx }/WEB-INF/pages/assist/assist_query.jsp" target="mainFrame">
						<div class="arrow"></div>
						<i class="icon-8"></i>
						<span>助理人管理</span>
					</a>
				</li>
				</s:if>
			</ul>
		</div>
	</div>
</div>

<script type="text/javascript" src="${ctx }/js/jquery-1.7.1.min.js"></script>

<script type="text/javascript">
// 处理左侧菜单选择时高亮
$(document).ready(function(){
	$("a").click(function(){
		var mainFrameHref = this.href;
		var mainFrameTarget = mainFrameHref.substring(mainFrameHref.lastIndexOf("/"), mainFrameHref.length);
		$("#sidebar-nav .list-box ul li a").removeClass("selected");
		$(this).addClass("selected");
	});
});

// 对管理员隐藏签呈内联申请
var cookies = document.cookie.split(";");
for(var i = 0; i < cookies.length; i++){
	if (cookies[i].indexOf("HOTOA.USERNAME") >= 0 && cookies[i].split("=")[1] == "admin"){
		$("#qianchenglink").hide();
		$("#neilianlink").hide();
	}
}
</script>
</body>
</html>
