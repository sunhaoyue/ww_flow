<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ include file="/WEB-INF/pages/taglibs.inc" %>
<%
request.setCharacterEncoding("UTF-8");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>代理管理界面</title>
<jsp:include page="/WEB-INF/pages/common_agent.jsp"></jsp:include>
<script type="text/javascript" src='${ctx }/js/layout.js'></script>
<script type="text/javascript">
var returnUrl = "${ctx }/agent/agentAction!addAgent.action";

$(function(){
	$("#content_work").css("display","none");
	$("#a_proxy").click(function(){
		$(this).removeClass().addClass("titlebg03");
		$("#a_work").removeClass().addClass("titlebg04");
		$("#content_proxy").css("display","block");
		$("#content_work").css("display","none");
		//设置代理类型（岗位代理）
		$("#agentType").val("PositionAgent");
		agentType = $("#agentType").attr("value");	
	});
	$("#a_work").click(function(){
		$(this).removeClass().addClass("titlebg03");
		$("#a_proxy").removeClass().addClass("titlebg04");
		$("#content_work").css("display","block");
		$("#content_proxy").css("display","none");
		//设置代理类型（流程代理）
		$("#agentType").val("FlowAgent");
		agentType = $("#agentType").attr("value");				
	});
});

//组织查询弹出设置
function toOrganQuery(){
		$('#query2').dialog({
			id:'dlg1',
			height:400,
			width:700,
			title:'按组织查询',
			content:'url:${ctx}/pages/organ_user.jsp?query=agent'
		});
		
		$('#query3').dialog({
			id:'dlg2',
			height:400,
			width:700,
			title:'按组织查询',
			content:'url:${ctx}/pages/organ_user.jsp?query=agent'
		});
		
		$('#deptQuery').dialog({
			id:'dlg3',
			height:300,
			width:400,
			title:'单位查询',
			content:'url:${ctx}/pages/agent/position_select.jsp?query=agent'
		});
		
		$('#flowQuery').dialog({
			id:'dlg4',
			height:350,
			width:500,
			title:'工作流查询',
			content:'url:${ctx}/pages/agent/agent_tree.jsp'
		});	
		$.dialog.data('agentType',agentType);
		//用于组织查询的单位过滤
		$.dialog.data('deptId',personCompanyId);
}

//通过组织查询添加代理
function setAgent(personDetail){
	if(agentType=='PositionAgent'){
		$("#agentUserName1").val(personDetail.name+"("+personDetail.employeeId+")");
		$("#agentEmployeeId1").val(personDetail.employeeId);			
		$("#agentPostName1").val(personDetail.postName);
		$("#agentDeptId1").val(personDetail.deptId);
		$("#agentUserId1").val(personDetail.userID);
		
		$("#agentpdept").val(personDetail.deptPath);				
		$("#agentpdept1").val(personDetail.dept1);			
		$("#agentpdept2").val(personDetail.dept2);			
		$("#agentpdept3").val(personDetail.dept3);	
	}
	if(agentType=='FlowAgent'){
		$("#agentUserName2").val(personDetail.name+"("+personDetail.employeeId+")");
		$("#agentEmployeeId2").val(personDetail.employeeId);
		$("#agentPostName2").val(personDetail.postName);
		$("#agentDeptId2").val(personDetail.deptId);
		$("#agentUserId2").val(personDetail.userID);						
		
		$("#agentfdept").val(personDetail.deptPath);						
		$("#agentfdept1").val(personDetail.dept1);			
		$("#agentfdept2").val(personDetail.dept2);			
		$("#agentfdept3").val(personDetail.dept3);
	}			
}

//单位发生变化后，重新塞值(只对岗位代理有效)
function setPosition(position){
	if(agentType=='PositionAgent'){
		$("#position").val(position.selectedPostName+"("+position.selectedDeptName+")");		
		$("#selectedPostCode").val(position.selectedPostCode);				
		$("#selectedPostName").val(position.selectedPostName);
		$("#selectedDeptPath").val(position.deptPath);			
	}
}

//取消
function cancel(){
	window.location.href=returnUrl;
}

//added by hzp 9.1
//代理原因中“其它原因”输入框根据需要显示”
$(document).ready(function() {
	function checkOtherReasonRadio(radio) {
		if(radio.value == "Other") $("#otherReason, #otherReason~td").css("visibility", "visible");
		else $("#otherReason, #otherReason~td").css("visibility", "hidden");
	}
	//init
	checkOtherReasonRadio($("input[name='agentReason']:checked")[0]);
	//click
	$("input[name='agentReason']").click(function(){
		checkOtherReasonRadio(this);
	});
});


</script>
</head>
<body>
<div class="container">
	<div class="header">
		<div class="titlebg01">工作代理人设定</div>
	</div>
	<!-- <div class="titlebg02"></div> -->
	<form id="applyForm" method="post" name="applyForm">
	<input name="agentId" type="hidden" id="agentId" value="<s:property value="agent.id"/>"/>		
	<input name="agentType" type="hidden" id="agentType" value="PositionAgent"/>
	<input name="actualUserId" type="hidden" id="actualUserId" value="<s:property value="#request.personDetail.userID"/>"/>
	<input name="selectedPostCode" type="hidden" id="selectedPostCode"/>
	<input name="selectedPostName" type="hidden" id="selectedPostName"/>
	<input name="selectedDeptPath" type="hidden" id="selectedDeptPath"/>	
	<input name="agentReasonValue" type="hidden" id="agentReasonValue" value="<s:property value="agent.agentReason"/>"/>   
	<input name="deptId" type="hidden" id="deptId" value="<s:property value="#request.personDetail.deptId"/>"/>
	<input name="personCompanyId" type="hidden" id="personCompanyId" value="<s:property value="#request.personCompanyId"/>"/>					
	<div class="bgc01">
		<div class="contentstyle">
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td width="24" height="24" align="left" valign="top">
						<img src="${ctx }/images/ico01.gif" width="18" height="18" />
					</td>
					<td align="left" valign="middle" class="text02">申请人基本资料</td>
				</tr>
			</table>
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td width="80">申请人姓名：</td>
					<td align="left">
						<input name="actualRealName" type="text" class="width01 appInput" id="actualRealName" value="<s:property value="#request.personDetail.name"/>" readonly="readonly" />
					</td>
					<td align="right">工号：</td>
					<td align="left">
						<input name="actualEmployeeId" type="text" class="width01 appInput" id="actualEmployeeId" value="<s:property value="#request.personDetail.employeeId"/>" readonly="readonly" />
					</td>
					<td align="right">职称：</td>
					<td align="left">
						<input name="actualPostName" type="text" class="width02 appInput" id="actualPostName" value="<s:property value="#request.personDetail.postName"/>" readonly="readonly" />
					</td>
					<td align="right">分机：</td>
					<td align="left">
						<input name="actualPhone" type="text" class="width03 appInput" id="actualPhone" value="<s:property value="#request.personDetail.compPhone"/>" readonly="readonly" />
					</td>
				</tr>
				<tr>
					<td>
						部&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;门：
					</td>
					<td colspan="7">
						<label class="width04">
							<input name="actualdept1" type="text" class="width02 appInput" id="actualdept1" value="<s:property value="#request.dept1"/>" readonly="readonly" />
							<input name="actualdept2" type="text" class="width02 appInput" id="actualdept2" value="<s:property value="#request.dept2"/>" readonly="readonly" />
							<input name="actualdept3" type="text" class="width02 appInput" id="actualdept3" value="<s:property value="#request.dept3"/>" readonly="readonly" />
						</label>
					</td>
				</tr>
			</table>
		</div>
		<div class="contentstyle">
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td width="24" height="24" align="left" valign="top">
						<img src="${ctx }/images/ico02.gif" width="18" height="18" />
					</td>
					<td align="left" valign="middle" class="text02">代理原因</td>
					<td valign="middle" class="text02">&nbsp;</td>
				</tr>
			</table>
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td width="100" align="right">代理起始日期：</td>
					<td width="220">
						<input name="startDate" type="text" class="Wdate" id="startDate" 
				           onclick="WdatePicker({el:'startDate',format:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'endDate\')}'})"
				           style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px" value="<s:property value="#request.startDate"/>"/>
					</td>
					<td width="100" align="right">代理结束日期：<span class="text03">*</span></td>
					<td>
						<input name="endDate" type="text" class="Wdate" id="endDate" 
				           onclick="WdatePicker({el:'endDate',format:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'startDate\')}'})"
				           style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px" value="<s:property value="#request.endDate"/>"/>
					</td>
				</tr>
				<tr>
					<td align="right">代 理 原 因：</td>
					<td>
						<table width="240" border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td>
									<div class="rule-multi-radio">
									<input name="agentReason" type="radio" id="agentReasonRadio1" value="Public" checked="checked" class="validate[required]"/>
									<label>公出</label>
									<input name="agentReason" type="radio" id="agentReasonRadio2" value="Business_Trip" class="validate[required]"/>
									<label>出差</label>
									<input name="agentReason" type="radio" id="agentReasonRadio3" value="Leave" class="validate[required]"/>
									<label>请假</label>
									<input name="agentReason" type="radio" id="agentReasonRadio4" value="Other" class="validate[required]"/>
									<label>其它</label>
									</div>
								</td>
							</tr>
						</table>
					</td>
					<td align="right" id="otherReason">其它原因：</td>
					<td>
						<input name="realName" type="text" id="realName" class="width03 appInput" style="width:330px" value="<s:property value="agent.otherReason"/>"/>
					</td>
				</tr>
			</table>
		</div>
		<div class="contentstyle">
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td width="24" height="24" align="left" valign="top">
						<img src="${ctx }/images/ico02.gif" width="18" height="18" />
					</td>
					<td align="left" valign="middle" class="text02">代理明细</td>
					<td valign="middle" class="text02">&nbsp;</td>
				</tr>
			</table>
			<div class="titlebg_line01">
				<table width="100" border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td><span id="a_proxy" class="titlebg03">签呈/内联</span></td>
					</tr>
				</table>
			</div>
			<div id="content_proxy" class="titlebg_line02">
				 <jsp:include page="/WEB-INF/pages/agent/agent_person.jsp"></jsp:include>
			</div>
			<div id="content_work" class="titlebg_line02">
				 <jsp:include page="/WEB-INF/pages/agent/agent_flow.jsp"></jsp:include>
			</div>
		</div>
		<div>
			<table width="201" border="0" cellpadding="0" cellspacing="0"
				class="center">
				<tr>
				    <td><div class="button04" id="applySubmit"></div></td>
				    <td><div class="button05" onclick="cancel()"></div></td>
				</tr>
			</table>
		</div>
	</div>
	</form>
</div>
<script type="text/javascript">
var agentType = $("#agentType").attr("value");
var id = $("#agentId").attr("value");
var deptId = $("#deptId").attr("value");
var personCompanyId = $("#personCompanyId").attr("value");

//为代理日期设置默认值
var startDate = $("#startDate").attr("value");
var endDate = $("#endDate").attr("value");
if(!startDate||!endDate){
  $("#startDate").val(date);
  $("#endDate").val(nextWeekDate);			  
}

//代理原因初始化
//var agentReasonRadios = document.applyForm.agentReasonRadio;
   var agentReasonRadios = $("input[name='agentReason']");
   var agentReasonValue = $("#agentReasonValue").attr("value");
for(var i=0;i<agentReasonRadios.length;i++ ){
	if(agentReasonRadios[i].value==agentReasonValue){
		agentReasonRadios[i].checked=true;
	}
}

//加载代理人列表
if(id>0){	
	$.ajax({
	    type: "POST",
	    url: '${ctx}/agent/agentAction!loadAgentPerson.action',
	    dataType:'json',
	    data:{'id':id},		    
	    success: function (data) {
	    	if(data&&data.length>0){
	        	for(var i=0;i<data.length;i++){
	        		if(data[i].agentType=='PositionAgent'){
	        			var rowIds1 = $("#agentPersonlist1").jqGrid('getDataIDs');
						var index1 = rowIds1.length;
	        			jQuery("#agentPersonlist1").jqGrid('addRowData',index1+1,data[i]);			        		
	        		}
	        		if(data[i].agentType=='FlowAgent'){
	        			var rowIds2 = $("#agentPersonlist2").jqGrid('getDataIDs');
						var index2 = rowIds2.length;
	        			jQuery("#agentPersonlist2").jqGrid('addRowData',index2+1,data[i]);	
	        		}
	  			}		        		
	        }
    	}
		})
	}

function removePositionRow(agentId, agentpersonId, rowId) {
	if (!confirm("确认删除？"))
		return;
	
	var curRowData = jQuery('#agentPersonlist1').getRowData(rowId);
	removePostNameFromHidden(curRowData);
	for ( var i = 0, j = 1; i < j; i++) {
	    if (agentId>0&&agentpersonId>0&&curRowData.id == agentpersonId) {
	    	// 后台Ajax后前台删除
			$.ajax({
				type: "POST",
				url: '${ctx}/agent/agentAction!deleteAgentPerson.action',
				dataType:'json',
				data:{'agentId':agentId, 'agentpersonId':agentpersonId},		    
				success: function (data) {
	    			$("#agentPersonlist1").delRowData(rowId);
	    			alert('代理人删除成功！');
				}
			})
	    }
	    else{
	    	$("#agentPersonlist1").delRowData(rowId);
	    	alert('代理人删除成功！');
	    }
	}  
	

}

//从缓存删除岗位  允许再次选择
function removePostNameFromHidden(rowData) {
	var savedPostNames = $("#savedPostNames").val().split(",");
	var tobeDelPostNames = rowData.agentPostName.split(",");

	for(var i = 0; i < tobeDelPostNames.length; i++) {
		var j = savedPostNames.indexOf(tobeDelPostNames[i]);
		if (j >= 0)
			savedPostNames.remove(j);
	}

	$("#savedPostNames").val(savedPostNames.join(","));
}

function removeFlowRow(agentId, agentpersonId, rowId) {

	if (!confirm("确认删除？"))
		return;
	
	var curRowData = jQuery('#agentPersonlist2').getRowData(rowId);
	removeFromHidden(curRowData);
	for ( var i = 0, j = 1; i < j; i++) {
	    if (agentId>0&&agentpersonId>0&&curRowData.id == agentpersonId) {
	    	// 后台Ajax后前台删除
			$.ajax({
				type: "POST",
				url: '${ctx}/agent/agentAction!deleteAgentPerson.action',
				dataType:'json',
				data:{'agentId':agentId, 'agentpersonId':agentpersonId},		    
				success: function (data) {
	    			$("#agentPersonlist2").delRowData(rowId);
	    			alert('代理人删除成功！');
				}
			})
	    }
	    else{
	    	$("#agentPersonlist2").delRowData(rowId);
	    	alert('代理人删除成功!');
	    }
	}
}
//从缓存区删除name
function removeFromHidden(rowData) {
	var savedFlowNames = $("#savedFlowNames").val().split(",");
	var tobeDelFlowNames = rowData.flowNames.split(",");

	for(var i = 0; i < tobeDelFlowNames.length; i++) {
		var j = savedFlowNames.indexOf(tobeDelFlowNames[i]);
		if (j >= 0)
			savedFlowNames.remove(j);
	}

	$("#savedFlowNames").val(savedFlowNames.join(","));
}

//提交助理信息
var saveUrl="${ctx}/agent/agentAction!submitAgent.action";
var successMsg = "代理设定成功!";
sumbitAgent(saveUrl,successMsg,returnUrl);
</script>   
</body>
</html>
