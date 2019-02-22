<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ include file="taglibs.inc" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>签呈申请单</title>
<link href="${ctx }/css/2.0/leftmenu.css" rel="stylesheet" type="text/css" />
</head>
<body>
<div class="left-sidebar">
	<div id="sidebar-nav" class="sidebar-nav">
		<div class="list-box">
			<div class="headbg"><a href="javascript:void(0);" onclick="javascript:parent.location.href='/portal'">返回首页</a></div>
			<ul>
				<li class="" onclick="openURL('${ctx }/management/.action', 'mainFrame', this)">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/search.png" width="24" alt="" />
						</i>
						<span>高级查询</span>
				</li>
				<li class="<s:if test="#request.menu == null">selected</s:if>" onclick="openURL('${ctx }/pages/flowmanagement/todoworks.jsp', 'mainFrame', this)">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/comment.png" width="24" alt="" />
						</i>
						<span>待审核表单&nbsp;
						(<s:if test="#request.myToDoWorkCount!=null"><s:property value="#request.myToDoWorkCount"/></s:if><s:else>0</s:else>)
						</span>
				</li>
				<li class="<s:if test="#request.menu != null">selected</s:if>" onclick="openURL('${ctx }/pages/flowmanagement/flowtemplates.jsp', 'mainFrame', this)">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/delicious.png" width="24" alt="" />
						</i>
						<span>暂存表单&nbsp;
						(<s:if test="#request.flowTemplatesCount!=null"><s:property value="#request.flowTemplatesCount"/></s:if><s:else>0</s:else>)
						</span>
				</li>
				<li class="" onclick="openURL('${ctx }/pages/flowmanagement/myProcessFlows.jsp', 'mainFrame', this)">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/collaboration.png" width="24" alt="" />
						</i>
						<span>已提交表单&nbsp;
						(<s:if test="#request.myProcessFlowCount!=null"><s:property value="#request.myProcessFlowCount"/></s:if><s:else>0</s:else>)
						</span>
				</li>
				<li class="" onclick="openURL('${ctx }/pages/flowmanagement/creatorFlows.jsp', 'mainFrame', this)">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/document_library.png" width="24" alt="" />
						</i>
						<span>已审核表单&nbsp;
						(<s:if test="#request.creatorFlowCount!=null"><s:property value="#request.creatorFlowCount"/></s:if><s:else>0</s:else>)
						</span>
				</li>
				<li class="" onclick="openURL('${ctx }/pages/flowmanagement/copyFlows.jsp', 'mainFrame', this)">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/upcoming_work.png" width="24" alt="" />
						</i>
						<span>抄送表单&nbsp;
						(<s:if test="#request.copyFlowCount!=null"><s:property value="#request.copyFlowCount"/></s:if><s:else>0</s:else>)
						</span>
				</li>
				<li id="qianchenglink" class="" onclick="openURL('${ctx }/nqianchen/qianChenAction!loadQianChenDetail.action', 'mainFrame', this)">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/plus.png" width="24" alt="" />
						</i>
						<span>签呈申请</span>
				</li>
				<li id="neilianlink" class="" onclick="openURL('${ctx }/nneilian/neiLianAction!loadNeiLianDetail.action', 'mainFrame', this)">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/plus.png" width="24" alt="" />
						</i>
						<span>内联申请</span>
				</li>
				<s:if test="#request.canAgent==1">
				<li class="" onclick="openURL('${ctx }/agent/agentAction!addAgent.action', 'mainFrame', this)">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/customers.png" width="24" alt="" />
						</i>
						<span>代理人管理</span>
				</li>
				</s:if>
				<s:if test="#request.canAssist==1">
				<li class="" onclick="openURL('${ctx }/pages/assist/assist_query.jsp', 'mainFrame', this)">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/user.png" width="24" alt="" />
						</i>
						<span>助理人管理</span>
				</li>
				</s:if>
				<s:if test="#request.canSecurity==1">
				<li class="" onclick="openURL('${ctx }/pages/flowmanagement/securityFlows.jsp', 'mainFrame', this)">
					<div class="arrow"></div>
					<i class="icon">
						<img src="${ctx }/images/2.0/networking.png" width="24" alt="" />
					</i>
					<span>机密表单&nbsp;
					(<s:if test="#request.securityFlowCount!=null"><s:property value="#request.securityFlowCount"/></s:if><s:else>0</s:else>)
					</span>
				</li>
				</s:if>
				<s:if test="#request.jySearch==1">
				<li class="" onclick="openURL('${ctx }/management/confidentialSearch.action', 'mainFrame', this)">
					<div class="arrow"></div>
					<i class="icon">
						<img src="${ctx }/images/2.0/search.png" width="24" alt="" />
					</i>
					<span>机要查询&nbsp;</span>
				</li>
				</s:if>
				<s:if test="#session.employeeId == 'Administrator' || #session.employeeId == 'admin' || #session.employeeId == '91011001'">
				<li class="" onclick="openURL('${ctx}/pages/flowmanagement/logList.jsp','mainFrame',this)">
					<div class="arrow"></div>
					<i class="icon">
						<img src="${ctx }/images/2.0/networking.png" width="24" alt="" />
					</i>
					<span>系统日志</span>
				</li>
				</s:if>
				<s:if test="#session.employeeId == 'Administrator' || #session.employeeId == 'admin' || #session.employeeId == '00000818' || #session.employeeId == '00315767' || #session.employeeId == '00001339' ">
				<li class="" onclick="openURL('${ctx}/management/confidentialSearchboss.action','mainFrame',this)">
					<div class="arrow"></div>
					<i class="icon">
						<img src="${ctx }/images/2.0/search.png" width="24" alt="" />
					</i>
					<span>副董事长查询</span>
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
		/* var mainFrameHref = this.href;
		var mainFrameTarget = mainFrameHref.substring(mainFrameHref.lastIndexOf("/"), mainFrameHref.length);
		$("#sidebar-nav .list-box ul li").removeClass("selected");
		$(this).parent().addClass("selected"); */
	});
});

function openURL(url, target, obj){
	window.open(url, target);
	$("#sidebar-nav .list-box ul li").removeClass("selected");
	$(obj).addClass("selected");
}

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
