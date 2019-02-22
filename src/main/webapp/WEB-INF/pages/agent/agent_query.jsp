<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ include file="/pages/taglibs.inc" %>
<%
request.setCharacterEncoding("UTF-8");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>代理查询界面</title>
<jsp:include page="/pages/common_min.jsp"></jsp:include>
<script type="text/javascript">
//渲染流程类型为中文
function renderAgentType(cellvalue, options, rowObject){
	switch(cellvalue)
	  	{
	　　   case 'PositionAgent':
	 　　    return "岗位代理"; 
	 　　  break;
	　　   case 'FlowAgent':
	　　     return "流程代理";
	　　     break;
	　　   default:
	　　     break;
	　　 }
}

//渲染流程类型为中文
function renderAgentReason(cellvalue, options, rowObject){
	switch(cellvalue)
	  	{
	　　   case 'Public':
	 　　    return "公出"; 
	 　　    break;
	　　   case 'Business_Trip':
	　　     return "出差";
	　　     break;
		  case 'Leave':
	　　     return "请假";
	　　     break;
		  case 'Other':
	　　     return "其他";
	　　     break;
	　　   default:
	　　     break;
	　　 }
}

//渲染操作列
function renderOperation(cellvalue, options, rowObject){
	var id = rowObject.id;
	return "<a href='${ctx}/agent/agentAction!loadAgent.action?agentId="+id+"'>编辑</a>"			
}	

function deleteAgent(){
	var ids;
	ids = jQuery("#agentList").jqGrid('getGridParam','selarrrow');
	if(ids==""){
		alert("请先选择要删除的记录!");
		return;
	}

	var params = new Array();
	var curRowData
	for (var i = 0; i < ids.length; i++) {
		curRowData = jQuery('#agentList').getRowData(ids[i]);
		params.push(curRowData.agentRelationId + "|" + curRowData.id);
	}

	if(confirm("您是否确认删除？")){ 
		$.ajax({
			type: "POST",
			url: '${ctx}/agent/deleteAgent.action',
			dataType:'json',
			data: "params="+params.join(), 	    
			success: function (data,status) {
				alert("删除成功!");
				window.location.href="${ctx}/pages/agent/agent_query.jsp";	
			}
 		})	 
	}
}
</script>

</head>
<body>
<div class="toolbar-wrap">
	<div id="floatHead" class="toolbar">
		<div class="l-list">
			<ul class="icon-list">
				<li><a class="button blue medium add" href="${ctx }/agent/agentAction!addAgent.action"><i></i><span>设定</span></a></li>
				<li><a class="button blue medium del" href="javascript:void(0);" onclick="deleteAgent()"><i></i><span>删除</span></a></li>
			</ul>
			<span id="overdue" style="color:red;margin-left:50px;font-weight:bold;display:none;">代理人设定已过期</span>
		</div>
	</div>
	<table id="agentList"></table>
	<div id="agentPager"></div>
</div>
<script type="text/javascript">
$(function(){
	var mainheight = $(window).height();
	var mainwidth = $(window).width();
	var w = mainwidth - 10;
	var h = mainheight - 180;
	$("#agentList").jqGrid({
		caption: '代理人列表',
		datatype: "local",
		autowidth:true,//根据父容器调整宽度,只在初始化阶段起作用
		shrinkToFit:false,
	  	height:200,//高度   
	  	multiselect: true,
	
		rownumbers: true,//是否显示序号列
	    rownumWidth:50,//序号列宽度
        
		colNames:['代理人', '代理类别', '受代理人', '原因', '代理编号', 'agentRelationId'],//表格列名称
		colModel :[ //列模型 
	      {name:'agentUserName', index:'agentUserName',width:w*0.3},
	      {name:'agentType', index:'agentType', width:w*0.1, align:'center', formatter:renderAgentType}, 
	      {name:'actualUserName', index:'actualUserName',width:w*0.3},			      
	      {name:'agentReason', index:'agentReason',width:w*0.1, align:'center', formatter:renderAgentReason},			      
	      {name:'id', index:'id', hidden:true},
	      {name:'agentRelationId', index:'agentRelationId', hidden:true}
		]
	}); 
  
	$.ajax({
		type: "POST",
		url: '${ctx}/agent/findAllAgents.action',
		dataType:'json',
		success: function (data) {
			if(data.length>0){
				for(var i=0;i<=data.length;i++){
					jQuery("#agentList").jqGrid('addRowData',i+1,data[i]);
				}		        		
			}else {
				var hasAgents = false;
			}
		}
	})
}); 

var hasAgents = true;
$(document).ready(function() {
	var overdue = false;
	$.get('${ctx}/agent/agentOverdue.action', function(result) {
		if (result == 1) {
			overdue = true;
		}
	});

	if (hasAgents && overdue) {
		$("#overdue").show();
	}
});
</script>
</body>
</html>
