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
<title>内联申请单</title>
<jsp:include page="/pages/common.jsp"></jsp:include>
<script type="text/javascript" src='${ctx }/js/layout.js'></script>
<script type="text/javascript">
jQuery(document).ready(function(){
	hiddenHeader();
    jQuery("#applyForm").validationEngine();
    $("#submitBOffice").attr("checked",false).attr("disabled",true);
    $("#leaderTable").find("input:radio").each(function(){
    	$(this).live("click",function(){
    		var radioVal = $(this).parent().parent().attr("id");
    	/*	if ($("#hid_HQFlg").val() == "true" &&  $("#hid_sysFlg").val() == "F" && radioVal == "centalleader"){
    			$("#submitBOffice").attr("disabled",false);
    		} else {
    			$("#submitBOffice").attr("checked",false).attr("disabled",true);
    		} */
    	});
    });
});
  //htmleditor编辑器初始化
  window.onload = function() {  
CKEDITOR.replace( 'detail',
{
	skin : 'v2'
});
  };

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
	$.post("${ctx}/nneilian/neiLianAction!confirmJoinDeptMgrPerson.action",postData,function(data){
		if(data.status == "error"){
			alert(data.message);
			return;
		}
		if(data.status == "success" && data.noMgrPersonDepts.length > 0){
			alert("以下会办单位因未设置相应主管，所以无法提交发文："+data.noMgrPersonDepts);
			return;	
		}
		$.dialog.tips('数据正在提交中，请稍候...', 6000, 'tips.gif');
		document.getElementById("applyForm").action="${ctx}/nneilian/neiLianAction!submitNeiLianApply.action";				
		document.getElementById('applyForm').submit();
	},"json");
}

//参考内联
function referenceNeiLian(){
	$('#reference').dialog({
			id:'reference',
			height:400,
			width:800,
			title:'参考内联',
			content:'url:${ctx}/pages/n_neilian/neilian_refer.jsp'
		});
}

//设置参考内联(设置内容包括机密性、时效性、主旨、内文)
function setNeiLianRefer(neiLianReferInfo){
//alert(neiLianReferInfo);
	$.ajax({
	    type: "POST",
	    url: '${ctx}/nneilian/loadReferNeiLianDetail.action',
	    dataType:'json',
	   	data:{'workId':neiLianReferInfo.workId,'formNum':neiLianReferInfo.formnum},
	    success: function (data,status) {
	    	//设置核决主管
	    	$("input:[name=leader]:radio").each(function(){ 
	    		if (this.value == data.leaderValue){ 
	                this.checked=true; 
	            } 
	        })
	        //会办顺序
	    	$("input:[name=joinType]:radio").each(function(){ 
	    		if (this.value == data.joinTypeValue){ 
	                this.checked=true; 
	            } 
	        })
	        $("#title").val(data.content.title);
	        CKEDITOR.instances.detail.setData(data.content.detail); 
	        
	        //会办单位
	    	$("#huibanDept").val(data.jointSignDeptName);
	    	$("#huibanDeptIds").val(data.huibanDeptIds);
	    	
	    	//抄送单位
	    	$("#chaosongDept").val(data.copyDeptName);
	    	$("#chaosongDeptIds").val(data.chaosongDeptIds);
	    					    	
	    	//抄送备注
	    	$("#copyDemo").val(data.copyDemo);				
			//建议方案
	    	$("#scheme").val(data.content.scheme);
    	}
		})
}
function hiddenHeader(){
	var sysFlg_hid = $("#hid_sysFlg").val();
	var HQFlg_hid = $("#hid_HQFlg").val();
	var control = true;
	if (control){
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
	}
}		
</script>
</head>

<body onkeydown="keyDown()" oncontextmenu="event.returnValue=false">
<div class="container">
 <div class="header">
  <div class="titlel01"></div>
  <div class="titlebg01">内联申请单</div>
  <div class="titler01"></div>
 </div>                             	           
 <div class="titlebg02">
   <table width="98%" border="0" cellpadding="0" cellspacing="0" class="center">
     <tr>
       <td align="left"><table border="0" cellspacing="0" cellpadding="0">
         <tr>
           <td width="80" height="24" class="text01">申请日期：</td>
           <td><label>
             <input name="applyDate" type="text" class="width01 appInput" id="applyDate" onclick="WdatePicker({el:'applyDate',format:'yyyy-MM-dd'})" value="<s:property value="#request.createTime"/>"/>
             <script>  
			   document.getElementById("applyDate").value = date; 
			</script>
		   </label></td>
         </tr>
       </table></td>
       <td align="right"><table border="0" cellspacing="0" cellpadding="0"  width="100%">
         <tr>
           <td width="110" class="text01">表单编号：</td>
           <td><label>
             <input name="textfield2" type="text" class="width02 appInput" id="textfield2" value="<s:property value="flow.formNum"/>"/>
           </label></td>
         </tr>
       </table></td>
     </tr>
   </table>
 </div>
 <div class="bgc01">   
   <form id="applyForm" method="post" name="applyForm">
   <input name="attachmentIds" type="hidden" id="attachmentIds"/>   
   <input type="hidden" name="hid_sysFlg" id="hid_sysFlg" value="<s:property value='#request.sysFlg'/>" />
   <input type="hidden" name="hid_HQFlg" id="hid_HQFlg" value="<s:property value='#request.isHQTR'/>" />
   <div class="contentstyle">
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="24" height="24" align="left" valign="top"><img src="<%=request.getContextPath()%>/images/ico01.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">申请人基本资料</td>
        </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80"><div align="justify">申请人姓名：</td>
         <td align="left"><input name="createPerson.name" type="text" class="width01 appInput" id="loginRealName" value="<s:property value="#request.personDetail.name"/>" readonly="readonly" /></td>
         <td align="right">工号：</td>
         <td align="left"><input name="createPerson.employeeId" type="text" class="width01 appInput" id="loginEmployeeId" value="<s:property value="#request.personDetail.employeeId"/>" readonly="readonly" /></td>
         <td align="right">职称：</td>
         <td align="left"><input name="createPerson.postName" type="text" class="width02 appInput" id="loginPostName" value="<s:property value="#request.personDetail.postName"/>" readonly="readonly" /></td>
         <td align="right">分机：</td>
         <td align="left"><input name="createPerson.compPhone" type="text" class="width03 appInput" id="loginPhone" value="<s:property value="#request.personDetail.compPhone"/>" readonly="readonly" /></td>
		 <input name="createPerson.postCode" type="hidden" id="actualPostCode" value="<s:property value="#request.personDetail.postCode"/>"/>       
       </tr>
       <tr>
         <td>申&nbsp;请&nbsp;单&nbsp;位：</td>
         <td colspan="7"><label class="width04">
         <input name="textfield3" type="text" class="width02 appInput" id="textfield3" value="<s:property value="#request.dept1"/>" readonly="readonly" />
         <input name="textfield4" type="text" class="width02 appInput" id="textfield4" value="<s:property value="#request.dept2"/>" readonly="readonly" />
         <input name="textfield5" type="text" class="width02 appInput" id="textfield5" value="<s:property value="#request.dept3"/>" readonly="readonly" />
         </label></td>
       </tr>
     </table>
   </div>
   <div class="contentstyle">
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="24" height="24" align="left" valign="top"><img src="${ctx }/images/ico02.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">内联明细</td>
         <td valign="middle" class="text02">&nbsp;</td>
       </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80" valign="top">参&nbsp;考&nbsp;内&nbsp;联：&nbsp;</td>
         <td colspan="3"><table border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><input name="textfield6" type="text" class="width01 appInput" id="textfield6" readonly /></td>
             <td><div class="button blue small" id="reference" onclick="referenceNeiLian()">查&nbsp;找</div></td>
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top"> 发&nbsp;文&nbsp;单&nbsp;位：&nbsp;<span class="text03">*</span></td>
         <td colspan="3"><table width="100%" border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><span class="width04">
               <input name="fawenDept" type="text" class="validate[required] appInput" id="fawenDept" readonly="readonly" 
               style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px;width:98%;" value="<s:property value="#request.personDetail.deptPath"/>"/>
               <input name="content.deptName" type="hidden" id="fawenDeptName" value="<s:property value="#request.personDetail.deptName"/>"/>
               <input name="content.deptId" type="hidden" id="fawenDeptId" value="<s:property value="#request.personDetail.deptId"/>"/>             
             </span></td>
           </tr>
         </table></td>
        </tr>
       <tr>
         <td valign="top">机&nbsp;&nbsp;&nbsp;密&nbsp;&nbsp;&nbsp;性：&nbsp;<span class="text03">*</span></td>
         <td width="257"><label>
           </label>
           <table width="140" border="0" cellpadding="0" cellspacing="0">
             <tr>
               <td>
               <div class="rule-multi-radio">
                 <input name="content.secretLevel" type="radio" id="radio" value="1" checked="checked" class="validate[required]"/>
                 <label>一般</label>
                 <input type="radio" name="content.secretLevel" id="radio2" value="2" class="validate[required]"/>
                 <label>密件</label>
               </div>
               </td>
             </tr>
           </table>
           <label></label></td>
         <td width="80">时&nbsp;&nbsp;&nbsp;效&nbsp;&nbsp;&nbsp;性：&nbsp;<span class="text03">*</span></td>
         <td width="337"><table width="140" border="0" cellpadding="0" cellspacing="0">
           <tr>
             <td>
             <div class="rule-multi-radio">
               <input name="content.exireLevel" type="radio" id="radio3" value="1" checked="checked" class="validate[required]"/>
              	<label> 一般</label>
               <input name="content.exireLevel" type="radio" id="radio4" value="2" class="validate[required]"/>
              	<label>速件</label>
              	</div>
             </td>
           </tr>
         </table></td>
        </tr>
        <!-- 公用明细 -->
	    <jsp:include page="/pages/n_pub/flow_detail_forapply1.jsp">
	    	<jsp:param name="flowType" value="neilian"/>
	    </jsp:include>
	    <script type="text/javascript">
       		//document.getElementById('inner').style.display="none";
       		//document.getElementById('innerNames').style.display="none";
        </script>
     </table>
   </div>
   </form>
   <!-- 附件列表 -->
   <jsp:include page="/pages/n_pub/flow_attachment.jsp"></jsp:include>
   <div><table width="200" border="0" cellpadding="0" cellspacing="0" class="center">
	  <tr>
	    <td><div class="button03" id="save"></div></td>
	    <td><div class="button04" onclick="applySubmit()"></div></td>
	  </tr>
   </table></div>
 </div>
</div>
<script>
//用于会办组织过滤
var fawenDeptId = $("#fawenDeptId").attr("value");
var contentType = "1";
leader =$('input:radio[name="leader"]:checked').val();
	
var huibanDeptIds = "";
var huibanDepts = "";
var chaosongDeptIds = "";
var chaosongDepts = "";

//核决主管的过滤
var actualEmployeeId = $("#loginEmployeeId").attr("value");
var actualPostCode = $("#actualPostCode").attr("value");

//核决主管过滤
judgeDecisionMaker();
	
//暂存表单
var saveUrl = "${ctx}/nneilian/saveNeiLianApply.action"
saveApply(saveUrl);
</script>
</body>
</html>
