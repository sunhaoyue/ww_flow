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
<title>代理管理界面</title>
<jsp:include page="/pages/common_agent.jsp"></jsp:include>

<script type="text/javascript">
var returnUrl = "${ctx}/pages/agent/agent_query.jsp";

//组织查询弹出设置
function toOrganQuery(){
		$('#query2').dialog({
			id:'dlg2',
			height:400,
			width:700,
			title:'按组织查询',
			content:'url:${ctx}/pages/organ_user.jsp?query=agent'
		});
		
		$.dialog.data('deptId',deptId);
}

//通过组织查询添加代理
function setAgent(personDetail){
	$("#agentUserName").val(personDetail.name);
	$("#agentEmployeeId").val(personDetail.employeeId);
	$("#agentPostName").val(personDetail.postName);
	$("#agentPostCode").val(personDetail.postCode);
	$("#agentDeptId").val(personDetail.deptId);		
	
	$("#agentdept1").val(personDetail.dept1);			
	$("#agentdept2").val(personDetail.dept2);			
	$("#agentdept3").val(personDetail.dept3);			

}

//代理类别
function agentTypeSelect(){
	var agentType =$('input:radio[name="agentType"]:checked').val();
	if(agentType=='PersonAgent'){
		document.getElementById('lab').style.display="block";
		document.getElementById('listbox').style.display="block";												
	}	
}

//取消
function cancel(){
	window.location.href=returnUrl;
}	
</script>
</head>

<body onkeydown="keyDown()" oncontextmenu="event.returnValue=false">
<div class="container">
	<div class="header">
		<div class="titlel01"></div>
		<div class="titlebg01">代理人设置</div>
		<div class="titler01"></div>
	</div>
 <form id="applyForm" method="post" name="applyForm">
 <input name="id" type="hidden" id="id" value="<s:property value="agent.id"/>"/>
 <input name="actualPostCode" type="hidden" id="actualPostCode" value="<s:property value="agent.actualPostCode"/>"/>
 <input name="actualDeptId" type="hidden" id="actualDeptId" value="<s:property value="agent.actualDeptId"/>"/>
 <input name="agentEmployeeId" type="hidden" id="agentEmployeeId" value="<s:property value="agent.agentEmployeeId"/>"/>
 <input name="agentPostCode" type="hidden" id="agentPostCode" value="<s:property value="agent.agentPostCode"/>"/>
 <input name="agentDeptId" type="hidden" id="agentDeptId" value="<s:property value="agent.agentDeptId"/>"/>        	
 <input name="agentReasonValue" type="hidden" id="agentReasonValue" value="<s:property value="agent.agentReason"/>"/>   
 <input type="hidden" name="effectOnSystem" id="effectOnSystem" value="<s:property value="agent.effectOnSystem"/>"/>
      	    	
 <div class="bgc01">
   <div class="contentstyle">
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="24" height="24" align="left" valign="top"><img src="${ctx }/images/ico01.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">申请人基本资料</td>
       </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80"><div align="justify">申请人姓名：<span class="text03">*</span></div></td>
         <td align="left"><input name="actualUserName" type="text" class="width01" id="actualRealName" value="<s:property value="agent.actualUserName"/>"/></td>
         <td align="right">工号：</td>
         <td align="left"><input name="actualEmployeeId" type="text" class="width01" id="actualEmployeeId" value="<s:property value="agent.actualEmployeeId"/>"/></td>
         <td align="right">职称：</td>
         <td align="left"><input name="actualPostName" type="text" class="width02" id="actualPostName" value="<s:property value="agent.actualPostName"/>"/></td>
         <td align="right">分机：</td>
         <td align="left"><input name="actualPhone" type="text" class="width03" id="actualPhone" value="<s:property value="agent.actualUserPhone"/>"/></td>
       </tr>
       <tr>
         <td>部&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;门：</td>
         <td colspan="7"><label class="width04">
         <input name="actualdept1" type="text" class="width02" id="actualdept1" value="<s:property value="#request.dept1"/>"/>
         </label>
           <span class="width04">
           <input name="actualdept2" type="text" class="width02" id="actualdept2" value="<s:property value="#request.dept2"/>"/>
          </span><span class="width04">
          <input name="actualdept3" type="text" class="width02" id="actualdept3" value="<s:property value="#request.dept3"/>"/>
          </span></td>
       </tr>
     </table>
   </div>
   <div class="contentstyle">
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="24" height="24" align="left" valign="top"><img src="${ctx }/images/ico02.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">代理原因</td>
         <td valign="middle" class="text02">&nbsp;</td>
       </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="100">代理开始日期：<span class="text03">*</span></td>
         <td width="150"><input name="startDate" type="text" class="Wdate" id="startDate" 
           onclick="WdatePicker({el:'startDate',format:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'endDate\')}'})"
           style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px" value="<s:property value="#request.startDate"/>"/>
         <td width="100">代理结束日期：<span class="text03">*</span></td>
         <td><input name="endDate" type="text" class="Wdate" id="endDate" 
           onclick="WdatePicker({el:'endDate',format:'yyyy-MM-dd',minDate:'#F{$dp.$D(\'startDate\')}'})"
           style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px" value="<s:property value="#request.endDate"/>"/>
       </tr>
       <tr>
         <td>代 理 原 因：&nbsp;<span class="text03">*</span></td>
         <td><table width="160" border="0" cellpadding="0" cellspacing="0">
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
           </table></td>
         <td align="right">其它原因：&nbsp;</td>
         <td><input name="realName" type="text" class="width01" id="realName" value="<s:property value="agent.otherReason"/>"/></td>
       </tr>
     </table>
    </div>
   <div class="contentstyle">
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="24" height="24" align="left" valign="top"><img src="<%=request.getContextPath()%>/images/ico02.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">代理明细</td>
         <td valign="middle" class="text02">&nbsp;</td>
       </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80">代理人姓名：<span class="text03">*</span></td>
         <td width="145"><input name="agentUserName" type="text" class="width01" id="agentUserName" value="<s:property value="agent.agentUserName"/>"/></td>
         <td width="60">职称：<span class="text03">*</span></td>
         <td width="239"><input name="agentPostName" type="text" class="width02" id="agentPostName" value="<s:property value="agent.agentPostName"/>"/></td> 
         <td width="230"><table border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><div id="query2" class="button01" onclick="toOrganQuery()">按组织查询</div></td>
           </tr>
         </table></td>
        </tr>
       <tr>
         <td>代理人部门：<span class="text03">*</span></td>
         <td colspan="4"><span class="width04">
         <input name="agentdept1" type="text" class="width02" id="agentdept1" value="<s:property value="#request.agentDept1"/>"/>
         </span><span class="width04">
         <input name="agentdept2" type="text" class="width02" id="agentdept2" value="<s:property value="#request.agentDept2"/>"/>
         </span><span class="width04">
         <input name="agentdept3" type="text" class="width02" id="agentdept3" value="<s:property value="#request.agentDept3"/>"/>
         </span></td>
        </tr>
        <tr>
          <td width="120">签呈内联有效：</td>
          <td><input type="checkbox" name="effectSystem" id="effectSystem" value="1"/></td>
        </tr>
       <tr id="lab">
         <td>代签核表单：<span class="text03">*</span></td>
         <td colspan="4" id="listbox">
         	<jsp:include page="/pages/agent/agent_tree.jsp"></jsp:include>
         </td>
       </tr>
     </table>
   </div>
   <div><table width="200" border="0" cellpadding="0" cellspacing="0" class="center">
	  <tr>
	    <td><div class="button04" id="applySubmit"></div></td>
	    <td><div class="button05" onclick="cancel()"></div></td>
	  </tr>
   </table></div>
 </div>
 </form>
</div>
<script type="text/javascript">
	//用于代理人过滤
	var deptId = $("#actualDeptId").attr("value");
	
	//代理原因初始化
	var agentReasonRadios = document.applyForm.agentReasonRadio;
    var agentReasonValue = $("#agentReasonValue").attr("value");
	for(var i=0;i<agentReasonRadios.length;i++ ){
		if(agentReasonRadios[i].value==agentReasonValue){
			agentReasonRadios[i].checked=true;
		}
	}
	
	//签呈内联有效性初始化
	var effectOnSystem = $("#effectOnSystem").attr("value");
	if(effectOnSystem=="true"){
		$("[name='effectSystem']").attr("checked",'true');
	}
	
	//提交助理信息
	var saveUrl="${ctx}/agent/agentAction!updateAgent.action";
	var successMsg = "代理修改成功!";
	sumbitApply(saveUrl,successMsg,returnUrl);
</script>   
</body>
</html>
