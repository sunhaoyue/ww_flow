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
hiddenHeader();
      jQuery("#applyForm").validationEngine();
      $("#submitBoss").attr("checked",false).attr("disabled",true);
      $("#submitFBoss").attr("checked",false).attr("disabled",true);
      $("#chairman").attr("checked",false).attr("disabled",true);
      //呈核总裁
      $("#leaderTable").find("input:radio").each(function(){
      	$(this).live("click",function(){
      		var radioVal = $(this).parent().parent().attr("id");
      		var contentType = $("select[name='content.type'] option:selected").attr("value");
      		if(contentType == "1"){
      			//体系内部，单位最高主管，"呈核总裁" 可用
      			if(radioVal == "unitleader" && $("#hid_HQFlg").val() == "true"){
	   				$("#submitBoss").attr("disabled",false);
	   				$("#submitFBoss").attr("disabled",false);
	   				$("#chairman").attr("disabled",true);
	   			}else{
	   				if ($("#hid_sysFlg").val() == "X" && radioVal == "centalleader"){
	      				$("#submitBoss").attr("disabled",false);
	      				$("#submitFBoss").attr("disabled",false);
	      				$("#chairman").attr("disabled",false);
	      			} else {
		   				if ($("#hid_HQFlg").val() == "true" && $("#hid_sysFlg").val() == "F" && radioVal == "centalleader"){
		      				$("#submitBoss").attr("disabled",false);
		     				$("#submitFBoss").attr("disabled",false);
		     				$("#chairman").attr("disabled",true);
		      			} else {
		     				$("#submitBoss").attr("checked",false).attr("disabled",true);
		     				$("#submitFBoss").attr("checked",false).attr("disabled",true);
		     				$("#chairman").attr("checked",false).attr("disabled",true);
		     			}
	     			}
	   			}
      		}else{
      			//地方到总部，转投资最高主管，"呈核总裁" 可用
      			if(radioVal == "headleader"){
      				$("#submitBoss").attr("disabled",false);
      				$("#submitFBoss").attr("disabled",false);
      			}else{
	   				if ($("#hid_sysFlg").val() == "F" && radioVal == "reginleader"){
	     				$("#submitBoss").attr("disabled",false);
	     				$("#submitFBoss").attr("disabled",false);
	     			} else {
	     				$("#submitBoss").attr("checked",false).attr("disabled",true);
	     				$("#submitFBoss").attr("checked",false).attr("disabled",true);
	     			}
	   			}
      		}
      	});
      });
  /*    $("#submitBOffice").click(function(){
		if (document.getElementById("submitBOffice").checked){
			$("#submitBoss").attr("disabled",false);
			$("#submitFBoss").attr("disabled",false);
			$("#chairman").attr("checked",false);
		} else {
			$("#submitBoss").attr("checked",false).attr("disabled",true);
			$("#submitFBoss").attr("checked",false).attr("disabled",true);
			$("#chairman").attr("checked",false).attr("disabled",true);
		}
	}); */
   });
   
   //htmleditor编辑器初始化
   window.onload = function() {  
	CKEDITOR.replace( 'detail',
	{
		skin : 'v2'
	});
	CKEDITOR.instances.detail.setData($("#detaildata").attr("value")); 
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

//通过组织查询添加发文单位
function setFawenDept(personDetail){
	$("#actualRealName").val(personDetail.name);
	$("#actualEmployeeId").val(personDetail.employeeId);
	$("#actualPostName").val(personDetail.postName);
	$("#actualPostCode").val(personDetail.postCode);			
	$("#actualDept").val(personDetail.deptPath);			
	$("#fawenDept").val(personDetail.deptPath);	
	$("#fawenDeptName").val(personDetail.deptName);
	$("#fawenDeptId").val(personDetail.deptId);
	
	//设置全局变量，供组织过滤使用
	fawenDeptId = personDetail.deptId;
	actualEmployeeId = personDetail.employeeId;
		actualPostCode = personDetail.postCode;
	
	document.getElementById("deptleader").disabled=false;
	document.getElementById("centalleader").disabled=false;
	document.getElementById("unitleader").disabled=false;				
	
	//核决主管过滤
	judgeDecisionMaker();
	//总部员工不允许选“地方到总部”
	judgeDftoZb();
}

//通过组织查询添加会办单位
function setHuibanDept(sids,snames){
	configHuibanDept(sids,snames);
}

//通过组织查询添加抄送单位
function setChaosongDept(sids,snames){
	configChaosongDept(sids,snames);			
}

//内部会办选择
var innerhuiban;
var innerhuibanNames;
var innerhuibanId;
var innerhuibanIds;
function selectInnerhuiban(personDetail){
	innerhuiban = personDetail.deptPath+"  "+personDetail.name+"  "+personDetail.postName;
	innerhuibanId = personDetail.deptId+","+ personDetail.employeeId+","+personDetail.postCode;		
	$("#innerhuiban").val(innerhuiban);
}

function addInnerhuiban(personDetail){
	if (neibuhuibanCheck(innerhuibanNames, innerhuiban)) {
		//添加后置空
		innerhuiban = ""; innerhuibanId = "";
		$("#innerhuiban").val("");
		return;
	}
	neibuhuibanCheck(innerhuibanNames, innerhuiban);
	if(innerhuibanNames){
		innerhuibanNames = innerhuibanNames + "\r\n"+  innerhuiban;
		innerhuibanIds = innerhuibanIds+";"+innerhuibanId;					
	}
	else{
		innerhuibanNames = innerhuiban;
		innerhuibanIds=innerhuibanId;
	}
	$("#innerhuibanNames").val(innerhuibanNames);
	$("#innerhuibanIds").val(innerhuibanIds);
	
	//添加后置空
	innerhuiban = ""; innerhuibanId = "";
	$("#innerhuiban").val("");
}

function removeInnerhuiban(){
	if(window.confirm("确认要清除内部会办人员吗?")){
		$("#innerhuibanNames").val("");
		$("#innerhuibanIds").val("");
		//置空
		innerhuiban = "";innerhuibanNames="";innerhuibanId="";innerhuibanIds="";
	}
	
}

//表单提交
function applySubmit(){
	if($("#applyForm").validationEngine('validate')==false){
		return;
	};
	if(editorcheck()==false){
		return;
	};
	
	//检查会办单位
	var postData = {
		"huibanDeptIds":$("#huibanDeptIds").val(),
		"huibanDept":$("#huibanDept").val(),
		"flow.actualPerson.employeeId":$("#loginEmployeeId").val(),
		"flow.actualPerson.postCode":$("#actualPostCode").val(),
		"flow.content.deptId":$("#fawenDeptId").val()
	};
	$.post("${ctx}/nqianchen/qianChenAction!confirmJoinDeptMgrPerson.action",postData,function(data){
		if(data.status == "error"){
			alert(data.message);
			return;
		}
		if(data.status == "success" && data.noMgrPersonDepts.length > 0){
			alert("以下会办单位因未设置相应主管，所以无法提交发文："+data.noMgrPersonDepts);
			return;	
		}
		$.dialog.tips('数据正在提交中，请稍候...', 6000, 'tips.gif');
		document.getElementById("applyForm").action="${ctx}/nqianchen/qianChenAction!submitQianChenApply.action";
		document.getElementById('applyForm').submit();
	},"json");
}
function hiddenHeader(){
	var sysFlg_hid = $("#hid_sysFlg").val();
	var HQFlg_hid = $("#hid_HQFlg").val();
	var isLocal = $('#select').val();
	var control = true;
	if (control){
		if (isLocal == "1"){
			if (HQFlg_hid == "true" && sysFlg_hid == 'F'){
				$("#unitleader").hide();
			//	$("#office_span").show();
			} else {
				$("#unitleader").show();
			//	$("#office_span").hide();
			}
			if (sysFlg_hid == 'X'){
				$("#unitleader").hide();
			}
		} else if (isLocal == "2") {
			if (sysFlg_hid == 'F'){
				$("#headleader").hide();
			//	$("#office_span").show();
			} else {
				$("#headleader").show();
			//	$("#office_span").hide();
			}
		}
	}
}
</script>	
</head>
<body onkeydown="keyDown()" oncontextmenu="event.returnValue=false">

<div class="container">
 <div class="header">
  <div class="titlel01"></div>
  <div class="titlebg01">签呈申请单</div>
  <div class="titler01"></div>
 </div>
 <form id="applyForm" method="post" name="applyForm">
 <input  type="hidden" name="id" id="flowId" value="<s:property value="flow.id"/>"/>
 <input name="content.id" type="hidden" id="content.id" value="<s:property value="flow.content.id"/>"/>
 <input  type="hidden" name="renewTimes" id="renewTimes" value="<s:property value="flow.renewTimes"/>"/>
 <input  type="hidden" name="workId" id="workId" value="<s:property value="#request.workId"/>"/>
 <input  type="hidden" name="type" id="type" value="renew"/>  
 <input name="attachmentIds" type="hidden" id="attachmentIds"/>
 <input type="hidden" name="hid_sysFlg" id="hid_sysFlg" value="<s:property value='#request.sysFlg'/>" />
 <input type="hidden" name="hid_HQFlg" id="hid_HQFlg" value="<s:property value='#request.isHQTR'/>" />
 <div class="titlebg02">
   <table width="98%" border="0" cellpadding="0" cellspacing="0" class="center">
     <tr>
       <td align="left"><table border="0" cellspacing="0" cellpadding="0">
         <tr>
           <td width="80" height="24" class="text01">申请日期：</td>
           <td><label>
             <input name="applyDate" type="text" class="width01 appInput" id="applyDate" onclick="WdatePicker({el:'applyDate',format:'yyyy-MM-dd'})"/>
             <script>  
			   document.getElementById("applyDate").value = date; 
			</script>
           </label></td>
         </tr>
       </table></td>
       <td align="right"><table border="0" cellspacing="0" cellpadding="0">
         <tr>
           <td width="110" class="text01">表单编号：</td>
           <td><label>
             <input name="formNum" type="text" class="text02 appInput" id="formNum" value="<s:property value="flow.formNum"/>"/>
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
         <td width="24" height="24" align="left" valign="top"><img src="${ctx }/images/ico01.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">代申请人基本资料</td>
       </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80">代申请人：</td>
         <td align="left"><input name="createPerson.name" type="text" class="width01 appInput" id="loginRealName" value="<s:property value="flow.createPerson.name"/>" readonly="readonly" /></td>
         <td align="right">工号：</td>
         <td align="left"><input name="createPerson.employeeId" type="text" class="width01 appInput" id="loginEmployeeId" value="<s:property value="flow.createPerson.employeeId"/>" readonly="readonly" /></td>
         <td align="right">职称：</td>
         <td align="left"><input name="createPerson.postName" type="text" class="width02 appInput" id="loginPostName" value="<s:property value="flow.createPerson.postName"/>" readonly="readonly" /></td>
         <td align="right">分机：</td>
         <td align="left"><input name="createPerson.compPhone" type="text" class="width03 appInput" id="loginPhone" value="<s:property value="flow.createPerson.compPhone"/>" readonly="readonly" /></td>
       	 <input name="createPerson.id" type="hidden" id="createPersonId" value="<s:property value="flow.createPerson.id"/>"/>		       
       </tr>
       <tr>
         <td>部&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;门：</td>
         <td colspan="7"><label class="width04">
           <input name="textfield7" type="text" class="width0 appInput" id="textfield7" value="<s:property value="flow.createPerson.deptPath"/>" readonly="readonly" />
         </label></td>
        </tr>
     </table>
   </div>
   <div class="contentstyle">
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="24" height="24" align="left" valign="top"><img src="${ctx }/images/ico01.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">申请人基本资料</td>
         <td valign="middle" class="text02"><div id="query1" class="button blue small" style="float: right;margin-right: 5px;width: 62px;" onclick="toOrganQuery()">按组织查询</div></td>
       </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80"><div align="justify">申&nbsp;&nbsp;请&nbsp;&nbsp;人：<span class="text03">*</span></div></td>
         <td align="left"><input name="actualPerson.name" type="text" class="width01 appInput" id="actualRealName" value="<s:property value="flow.actualPerson.name"/>" readonly="readonly" /></td>
         <td align="right">工号：</td>
         <td align="left"><input name="actualPerson.employeeId" type="text" class="width01 appInput" id="actualEmployeeId" value="<s:property value="flow.actualPerson.employeeId"/>" readonly="readonly" /></td>
         <td align="right">职称：</td>
         <td align="left"><input name="actualPerson.postName" type="text" class="width02 appInput" id="actualPostName" value="<s:property value="flow.actualPerson.postName"/>" readonly="readonly" /></td>
         <td align="right">分机：</td>
         <td align="left"><input name="actualPerson.compPhone" type="text" class="width03 appInput" id="actualPhone" value="<s:property value="flow.actualPerson.compPhone"/>" readonly="readonly" /></td>
		 <input name="actualPerson.id" type="hidden" id="actualPersonId" value="<s:property value="flow.actualPerson.id"/>"/>		 		 
		 <input name="actualPerson.postCode" type="hidden" id="actualPostCode" value="<s:property value="flow.actualPerson.postCode"/>"/>
       </tr>
       <tr>
         <td>部&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;门：</td>
         <td colspan="7"><label class="width04">
           <input name="actualDept" type="text" class="width0 appInput" id="actualDept" value="<s:property value="flow.actualPerson.deptPath"/>" readonly="readonly" />
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
         <td width="80" valign="top">发文单位：&nbsp;<span class="text03">*</span></td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><span class="width04">
               <input name="fawenDept" type="text" class="validate[required] appInput" id="fawenDept" 
               style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px;width:98%;" value="<s:property value="flow.actualPerson.deptPath"/>"  readonly="readonly" />
               <input name="content.deptName" type="hidden" id="fawenDeptName" value="<s:property value="flow.actualPerson.deptName"/>"/>
               <input name="content.deptId" type="hidden" id="fawenDeptId" value="<s:property value="flow.actualPerson.deptId"/>"/>
             </span></td>
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top">机&nbsp;&nbsp;&nbsp;密&nbsp;&nbsp;&nbsp;&nbsp;性：&nbsp;<span class="text03">*</span></td>
         <td><label>
           </label>
           <table width="140" border="0" cellpadding="0" cellspacing="0">
             <tr>
               <td>
               <div class="rule-multi-radio">
                 <input name="content.secretLevel" type="radio" id="secretRadio" value="1" class="validate[required]"/>
				<label>一般</label>
                 <input type="radio" name="content.secretLevel" id="secretRadio" value="2" class="validate[required]"/>
				<label>密件</label>
				</div>
               </td>
             </tr>
           </table></td>
           <input name="secretLevelValue" type="hidden" id="secretLevel" value="<s:property value="flow.content.secretLevel"/>"/>
           <script type="text/javascript">
               var secretRadios = document.applyForm.secretRadio;
               var secretLevelValue = $("#secretLevel").attr("value");
			   for(var i=0;i<secretRadios.length;i++ ){
					if(secretRadios[i].value==secretLevelValue){
						secretRadios[i].checked=true;
					}
			   }
           </script>           
         <td width="80">时&nbsp;&nbsp;&nbsp;效&nbsp;&nbsp;&nbsp;&nbsp;性：&nbsp;<span class="text03">*</span></td>
         <td><table width="140" border="0" cellpadding="0" cellspacing="0">
           <tr>
             <td>
             <div class="rule-multi-radio">
               <input name="content.exireLevel" type="radio" id="exireLevelRadio" value="1" class="validate[required]"/>
               <label>一般</label>
               <input name="content.exireLevel" type="radio" id="exireLevelRadio" value="2" class="validate[required]"/>
               <label>速件</label>
               </div>
             </td>
           </tr>
         </table></td>
           <input name="exireLevelValue" type="hidden" id="exireLevel" value="<s:property value="flow.content.exireLevel"/>"/>
           <script type="text/javascript">
               var exireRadios = document.applyForm.exireLevelRadio;
               var exireLevelValue = $("#exireLevel").attr("value");
			   for(var i=0;i<exireRadios.length;i++ ){
					if(exireRadios[i].value==exireLevelValue){
						exireRadios[i].checked=true;
					}
			   }
           </script>   
         <td width="80">预&nbsp;估&nbsp;金&nbsp;额：&nbsp;<span class="text03">*</span></td>
         <td><label>
           <input name="content.cash" type="text" id="textfield14" class="validate[required,custom[number]]" 
           style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px" value="<s:property value="flow.content.cash"/>"/>（元）</label></td>
         <td>&nbsp;</td>
       </tr>
       <tr>
         <td valign="top">类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;别：&nbsp;<span class="text03">*</span></td>
         <td colspan="6"><label>
           <select name="content.type" id="select" onchange="typeSelect()">
             <s:if test="flow.content.type==2">
	             <option value="1">体系内部</option>
	             <option value="2"  id="dftozb" selected>地方到总部</option>
             </s:if>
             <s:else>
	             <option value="1" selected>体系内部</option>
	             <option value="2" id="dftozb" >地方到总部</option>
	         </s:else>
           </select>
         </label></td>
       </tr>
       <!-- 公用明细 -->
		<script>$(document).ready(function () {typeSelect();});</script>
   	   <jsp:include page="/pages/n_pub/flow_detail_forapply2.jsp">
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
    <jsp:include page="/pages/n_pub/flow_attachment.jsp">
    	<jsp:param name="renew" value="true"/>
    </jsp:include>
	<!-- 签核历程 -->
	<jsp:include page="/pages/n_pub/flow_history.jsp"></jsp:include>
	<table width="200" border="0" cellpadding="0" cellspacing="0" class="center">
  	<tr>
    	<td><div class="button03" id="save"></div></td>
		<td><div class="button04" onclick="applySubmit()"></div></td>
  	</tr>
	</table>
 </div>
</div>
<script>
var formNum = document.getElementById('formNum').value;	
loadAttachments();
	
//已选会办单位信息加载
var huibanDeptIds = $("#huibanDeptIds").attr("value");
var huibanDepts = $("#huibanDept").attr("value");
var oldhuibanDeptIds = huibanDeptIds;

//已选抄送单位信息加载
var chaosongDeptIds = $("#chaosongDeptIds").attr("value");
var chaosongDept = $("#chaosongDept").attr("value");
var oldchaosongDeptIds = chaosongDeptIds;

//用于内部会办组织过滤
var fawenDeptId = $("#fawenDeptId").attr("value");
//用于会办组织过滤
var contentType = $("#select").attr("value");
leader =$('input:radio[name="leader"]:checked').val();
function leaderClick(){
	leader =$('input:radio[name="leader"]:checked').val();
}

//核决主管的过滤
var actualEmployeeId = $("#actualEmployeeId").attr("value");
var actualPostCode = $("#actualPostCode").attr("value");
	
//核决主管过滤
judgeDecisionMaker();

//暂存表单
var saveUrl = "${ctx}/nqianchen/saveQianChenApply.action"
saveApply(saveUrl); 			
</script>
</body>
</html>
