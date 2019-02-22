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
<title>签呈申请单</title>
<jsp:include page="/pages/common.jsp"></jsp:include>
<script type="text/javascript" src='${ctx }/js/layout.js'></script>
<script type="text/javascript">
jQuery(document).ready(function(){
      jQuery("#applyForm").validationEngine();
      hiddenHeader();
});			
	
   //表单内容初始化
   window.onload = function() {
	var editor = CKEDITOR.replace( 'detail',
	{
		skin : 'v2'
	});
	CKEDITOR.instances.detail.setData($("#detaildata").attr("value")); 
	CKEDITOR.on("instanceReady", function(ev){
		editor = ev.editor;
		editor.setReadOnly(true);
	});

	//将部分明细渲染成中文
	renderLevel();
	renderContentType();
	renderDescionMaker();
	renderJoinType();

	//会办单位
   	$("#huibanDept").val($("#jointSignDeptName").attr("value"));
   	//抄送单位
   	$("#chaosongDept").val($("#copyDeptName").attr("value"));
   	//抄送备注
   	$("#copyDemo").val($("#copyDemohidden").attr("value"));				
	//建议方案
   	$("#scheme").val($("#schemehidden").attr("value"));
   	//意见
   	$("#opinion").val($("#opinionhidden").attr("value"));	    	
   };

//通过组织查询添加会办单位
function setHuibanDept(sids,snames){
	configEditHuibanDept(sids,snames);
	if(huibanDeptIds!=oldhuibanDeptIds){
		$("#updateFlag").val('1');
	}
}
//通过组织查询添加抄送单位
function setChaosongDept(sids,snames){
	configEditChaosongDept(sids,snames);			
	if(chaosongDeptIds!=oldchaosongDeptIds){
		$("#updateFlag").val('1');
	}
}

function editSubmitBoss(flag){
	$("#submitBoss").attr("disabled",true);
	$("#submitFBoss").attr("disabled",true);
	var leader = $("#leader").attr("value");
	var contentType = $("#type").attr("value");
	var bRet = false;
	if (flag){
		if (contentType == "1" && $('#hid_HQFlg').val() == 'true'){
			// 体系内部
			if (leader == "UNITLEADER"){
				bRet = true;
			}
		} else {
			// 地方至总部
			if (leader == "HEADLEADER"){
				bRet = true;
			}
		}
/*		try{
			if ($('#submitBOfficeHid').val() == '1'){
				bRet = true;
			}
		}catch(e){} */
	}
/*	if (bRet){
		$("#submitBoss").attr("disabled",false);
		$("#submitFBoss").attr("disabled",false);
		$("#chairman").attr("checked",false);
	}*/
}
function hiddenHeader(){
	var sysFlg_hid = $("#hid_sysFlg").val();
	var HQFlg_hid = $("#hid_HQFlg").val();
	var isLocal = $('#type').val();
/*	var control = true;
	if (control){
		if (isLocal == "1"){
		  if (HQFlg_hid == "true" && sysFlg_hid == 'F'){
				$("#office_span").show();
			} else {
				$("#office_span").hide();
			}
		} else if (isLocal == "2") {
		  	if (sysFlg_hid == 'F'){
				$("#office_span").show();
			} else {
				$("#office_span").hide();
			}
		}
	}*/
}
</script>	
</head>
<body onkeydown="keyDown()" oncontextmenu="event.returnValue=false">
<div class="container" style="margin-top: 3px;">
 <div class="header">
  <div class="titlel01"></div>
  <div class="titlebg01">签呈申请单</div>
  <div class="titler01"></div>
 </div>
 <form id="applyForm" method="post" name="applyForm">
 <input type="hidden" name="hid_sysFlg" id="hid_sysFlg" value="<s:property value='#request.sysFlg'/>" />
 <input type="hidden" name="hid_HQFlg" id="hid_HQFlg" value="<s:property value='#request.isHQTR'/>" />  
 <input type="hidden" id="currentEmployeeId" value='<s:property value="#session.employeeId"/>'/>
 <input type="hidden" name="id" id="flowId" value="<s:property value="flow.id"/>"/>
 <input type="hidden" name="workId" id="workId" value="<s:property value="#request.workId"/>"/>
 <input type="hidden" name="hbChengHe" id="hbChengHe" value="<s:property value='#request.hbChengHe'/>" />
 <input type="hidden" name="assignDeptId" id="assignDeptId" value="<s:property value="#request.deptId"/>"/>
 <input name="attachmentIds" type="hidden" id="attachmentIds"/>
 <input name="assignId" type="hidden" id="assignId"/> 
 <input type="hidden" name="updateFlag" id="updateFlag" value="0"/> 
 <input name="flowStatus" type="hidden" id="flowStatus" value="<s:property value="#request.flowStatus"/>"/>
 <input name="workStage" type="hidden" id="workStage" value="<s:property value="#request.workStage"/>"/>
 <input name="showCancelButton" type="hidden" id="showCancelButton" value="<s:property value="#request.showCancelButton"/>"/>
 <input name="role" type="hidden" id="role" value="<s:property value="#request.role"/>"/>
<input name="renewTimes" type="hidden" id="renewTimes" value="<s:property value="#request.renewTimes"/>"/>
<input type="hidden" name="adminAssignDeptId" id="adminAssignDeptId" />
<input type="hidden" name="adminAssignWorkId" id="adminAssignWorkId" />
<input type="hidden" name="adminAssignOldEmployeeId" id="adminAssignOldEmployeeId" />
<input type="hidden" id="flowType" value="1"/>
<input type="hidden" id="canUpAttach" name="canUpAttach" value="<s:property value="#request.canUpAttach" />"/>
 <div class="titlebg02">
   <table width="98%" border="0" cellpadding="0" cellspacing="0" class="center">
     <tr>
       <td align="left"><table border="0" cellspacing="0" cellpadding="0">
         <tr>
           <td width="80" height="24" class="text01">申请日期：</td>
           <td><label>
             <input name="applyDate" type="text" class="width01-readOnly" readonly id="applyDate" value="<s:property value="#request.createTime"/>"/>
           </label></td>
         </tr>
       </table></td>
       <td align="right"><table border="0" cellspacing="0" cellpadding="0" width="100%">
         <tr>
           <td width="110" class="text01">表单编号：</td>
           <td><label>
             <input name="formNum" type="text" class="text02-readOnly" readonly id="formNum" value="<s:property value="flow.formNum"/>"/>
           </label></td>
         </tr>
       </table></td>
     </tr>
   </table>
 </div>
 <div class="bgc01">
   <div class="contentstyle">                   
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="24" height="24" align="left" valign="top"><img src="<%=request.getContextPath()%>/images/ico01.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">代申请人基本资料</td>
       </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80">代申请人：</td>
         <td align="left"><input name="loginRealName" type="text" class="width01-readOnly" readonly id="loginRealName" value="<s:property value="flow.createPerson.name"/>"/></td>
         <td align="right">工号：</td>
         <td align="left"><input name="loginEmployeeId" type="text" class="width01-readOnly" readonly id="loginEmployeeId" value="<s:property value="flow.createPerson.employeeId"/>"/></td>
         <td align="right">职称：</td>
         <td align="left"><input name="loginPostName" type="text" class="width02-readOnly" readonly id="loginPostName" value="<s:property value="flow.createPerson.postName"/>"/></td>
         <td align="right">分机：</td>
         <td align="left"><input name="loginPhone" type="text" class="width03-readOnly" readonly id="loginPhone" value="<s:property value="flow.createPerson.compPhone"/>"/></td>
       </tr>
       <tr>
         <td>部&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;门：</td>
         <td colspan="7"><label class="width04">
           <input name="textfield7" type="text" class="width0-readOnly" readonly id="textfield7" value="<s:property value="flow.createPerson.deptPath"/>"/>
         </label></td>
        </tr>
     </table>
   </div>
   <div class="contentstyle">
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="24" height="24" align="left" valign="top"><img src="${ctx }/images/ico01.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">申请人基本资料</td>
       </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80"><div align="justify">申&nbsp;&nbsp;请&nbsp;&nbsp;人：</div></td>
         <td align="left"><input name="actualRealName" type="text" class="width01-readOnly" readonly id="actualRealName" value="<s:property value="flow.actualPerson.name"/>"/></td>
         <td align="right">工号：</td>
         <td align="left"><input name="actualEmployeeId" type="text" class="width01-readOnly" readonly id="actualEmployeeId" value="<s:property value="flow.actualPerson.employeeId"/>"/></td>
         <td align="right">职称：</td>
         <td align="left"><input name="actualPostName" type="text" class="width02-readOnly" readonly id="actualPostName" value="<s:property value="flow.actualPerson.postName"/>"/></td>
         <td align="right">分机：</td>
         <td align="left"><input name="actualPhone" type="text" class="width03-readOnly" readonly id="actualPhone" value="<s:property value="flow.actualPerson.compPhone"/>"/></td>
		 <input name="actualPerson.postCode" type="hidden" id="actualPostCode" value="<s:property value="#request.personDetail.postCode"/>"/>       
       </tr>
       <tr>
         <td>部&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;门：</td>
         <td colspan="7"><label class="width04">
           <input name="actualDept" type="text" class="width0-readOnly" readonly id="actualDept" value="<s:property value="flow.actualPerson.deptPath"/>"/>
         </label></td>
       </tr>
     </table>
   </div>
   <div class="contentstyle">
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="24" height="24" align="left" valign="top"><img src="${ctx }/images/ico02.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">签呈明细</td>
         <td valign="middle" class="text02">&nbsp;</td>
       </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80" valign="top">发&nbsp;文&nbsp;单&nbsp;位：&nbsp;</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><span class="width04">
               <input name="fawenDept" type="text" class="width0-readOnly" id="fawenDept" readonly value="<s:property value="flow.actualPerson.deptPath"/>" style="width:98%"/>
               <input name="content.deptId" type="hidden" id="fawenDeptId" value="<s:property value="flow.actualPerson.deptId"/>"/>
             </span></td>
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top">机&nbsp;&nbsp;&nbsp;密&nbsp;&nbsp;&nbsp;性：&nbsp;</td>
         <td><label>
           </label>
           <table width="140" border="0" cellpadding="0" cellspacing="0">
             <tr>
               <td><input name="secretLevelName" type="text" id="secretLevelName" class="width01-readOnly" readonly/>
				   <input name="flow.content.secretLevel" type="hidden" id="secretLevel" value="<s:property value="flow.content.secretLevel"/>"/>
               </td>
             </tr>
           </table>
           <label>           </label></td>
         <td width="80">时&nbsp;&nbsp;&nbsp;效&nbsp;&nbsp;&nbsp;性：&nbsp;</td>
         <td><table width="140" border="0" cellpadding="0" cellspacing="0">
           <tr>
             <td><input name="exireLevelName" type="text" id="exireLevelName" class="width01-readOnly" readonly/>
             	 <input name="flow.content.exireLevel" type="hidden" id="exireLevel" value="<s:property value="flow.content.exireLevel"/>"/>
             </td>
           </tr>
         </table></td>
         <td width="80">预&nbsp;估&nbsp;金&nbsp;额：&nbsp;</td>
         <td><label>
           <input name="cash" type="text" id="textfield14" class="width01-readOnly" readonly value="<s:property value="flow.content.cash"/>"/>（元）</label></td>
         <td>&nbsp;</td>
       </tr>
       <tr>
         <td valign="top">类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;别：&nbsp;</td>
             <td><input name="contentTypeName" type="text" id="contentType" class="width01-readOnly" readonly/>
             	 <input name="flow.content.type" type="hidden" id="type" value="<s:property value="flow.content.type"/>"/>
             </td>
       </tr>
        <!-- 公用明细 -->
	   <jsp:include page="/pages/n_pub/flow_detail_forview.jsp">
	   	<jsp:param name="flowType" value="qiancheng"/>
	   </jsp:include>
     </table>
    </div>
  </div>
  </form>
  <div class="bgc01">
  	  <script>
  	  	 var formNum = $("#formNum").attr("value");
  	  </script>
	  <!-- 附件列表 -->
	  <jsp:include page="/pages/n_pub/flow_attachlist.jsp"></jsp:include>
	  <!-- 签核历程 -->
	  <jsp:include page="/pages/n_pub/flow_history.jsp"></jsp:include>
	  <div class="line10"></div>
	  <table width="500" border="0" cellpadding="0" cellspacing="0" class="center">
	  	<tr>
			<td><input type="button" class="button blue medium" value="打印" id="printForm" onclick="printFormPlus('<s:property value="flow.formNum"/>', 'qc')" /></td>
	  	</tr>
	  </table>
	</div>
</div>
<div class="line15"></div>
<script> 
loadAttachments();

function printFormPlus(formid, type){
	var postData = {
		"formnum": $("#formNum").attr("value"),
		"stat": "P"
	};
	$.post("${ctx}/nqianchen/qianChenEditAction!insertLog.action", postData, function(data){
		
	});
	printForm(formid, type);
}
</script>
</body>
</html>
