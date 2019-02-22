<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ taglib uri="/struts-tags" prefix="s"%>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>内联申请单</title>
<jsp:include page="/pages/common.jsp"></jsp:include>

	<script type="text/javascript">
			jQuery(document).ready(function(){
		       jQuery("#applyForm").validationEngine();
		    });
		    //htmleditor编辑器初始化
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
			
			//会办指派选择
			function assignWork(personDetail){
				var assignId = personDetail.deptId+","+ personDetail.employeeId+","+personDetail.postCode;		
				$("#assignId").val(assignId);
				document.getElementById('applyForm').action="<%=request.getContextPath()%>/neilian/neiLianEditAction!assignWork.action";
				document.getElementById('applyForm').submit();
			}
			//管理员指派选择
			function adminAssignWork(personDetail){
				var assignId = personDetail.deptId+","+ personDetail.employeeId+","+personDetail.postCode;		
				$("#assignId").val(assignId);
				document.getElementById('applyForm').action="<%=request.getContextPath()%>/neilian/neiLianEditAction!adminAssignWork.action";
				document.getElementById('applyForm').submit();
			}
			
			//驳回
			function reject(){
				if($("#applyForm").validationEngine('validate')==false){
					return;
				};
				if(editorcheck()==false){
					return;
				};
				document.getElementById('applyForm').action="<%=request.getContextPath()%>/neilian/neiLianEditAction!rejectWork.action";
				document.getElementById('applyForm').submit();				
			}
			
			//重新起案
			function renew(){
				var workId = document.getElementById('workId').value;
				document.getElementById('applyForm').action="<%=request.getContextPath()%>/neilian/neiLianEditAction!renewWork.action?type=renew&workId="+workId;
				document.getElementById('applyForm').submit();				
			}
		    
			//表单提交(同意)
			function applySubmit(){
				var attachmentIds = $("#attachmentIds").attr("value");
				if(attachmentIds){
					$("#updateFlag").val('1');
				}
				
				//检查上级主管
				var postData = {
					"workId":$("#workId").val()
				};
				$.post("<%=request.getContextPath()%>/neilian/neiLianEditAction!checkMgrPerson.action",postData,function(data){
					if(data.status == "error"){
						alert(data.message);
						return;
					}
					document.getElementById("applyForm").action="<%=request.getContextPath()%>/neilian/neiLianEditAction!confirmWork.action";				
					document.getElementById('applyForm').submit();
				},"json");
			}
			
			//表单提交(同意)
			function applySubmit_Ex(){
				var attachmentIds = $("#attachmentIds").attr("value");
				if(attachmentIds){
					$("#updateFlag").val('1');
				}
				
				$("#hbChengHe").val('1');
				
				//检查上级主管
				var postData = {
					"workId":$("#workId").val()
				};
				$.post("<%=request.getContextPath()%>/neilian/neiLianEditAction!checkMgrPerson.action",postData,function(data){
					if(data.status == "error"){
						alert(data.message);
						return;
					}
					document.getElementById("applyForm").action="<%=request.getContextPath()%>/neilian/neiLianEditAction!confirmWork.action";				
					document.getElementById('applyForm').submit();
				},"json");
				
			}
			
			//取消
			function cancel(){
				document.getElementById("applyForm").action="<%=request.getContextPath()%>/qianchen/qianChenEditAction!cancelWork.action";				
				document.getElementById('applyForm').submit();
			}

			//取消
			function cancelForm(){
				document.getElementById("applyForm").action="<%=request.getContextPath()%>/qianchen/qianChenEditAction!cancelForm.action";				
				document.getElementById('applyForm').submit();
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
 <form id="applyForm" method="post" name="applyForm">
 <input  type="hidden" name="id" id="flowId" value="<s:property value="flow.id"/>"/>
 <input  type="hidden" name="workId" id="workId" value="<s:property value="#request.workId"/>"/>
 <input type="hidden" name="hbChengHe" id="hbChengHe" value="<s:property value='#request.hbChengHe'/>" />
 <input  type="hidden" name="assignDeptId" id="assignDeptId" value="<s:property value="#request.deptId"/>"/>
 <input name="attachmentIds" type="hidden" id="attachmentIds"/>
 <input name="assignId" type="hidden" id="assignId"/> 
 <input  type="hidden" name="updateFlag" id="updateFlag" value="0"/>
 <input name="flowStatus" type="hidden" id="flowStatus" value="<s:property value="#request.flowStatus"/>"/>
 <input name="workStage" type="hidden" id="workStage" value="<s:property value="#request.workStage"/>"/>
 <input name="showCancelButton" type="hidden" id="showCancelButton" value="<s:property value="#request.showCancelButton"/>"/>
 <input name="role" type="hidden" id="role" value="<s:property value="#request.role"/>"/>  
 <input name="renewTimes" type="hidden" id="renewTimes" value="<s:property value="#request.renewTimes"/>"/>
 <input type="hidden" name="adminAssignDeptId" id="adminAssignDeptId" />
 <input type="hidden" name="adminAssignWorkId" id="adminAssignWorkId" />
 <input type="hidden" name="adminAssignOldEmployeeId" id="adminAssignOldEmployeeId" />
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
         <td align="left" valign="middle" class="text02">申请人基本资料</td>
<%--
         <s:if test="#request.workStage=='REJECT'">
         	<td align="right" valign="middle"><input type="button" value="重新起案" onclick="renew()"></td>
         </s:if>
--%>
        </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80"><div align="justify">申请人姓名：</div></td>
         <td align="left"><input name="loginRealName" type="text" class="width01-readOnly" readonly id="loginRealName" value="<s:property value="flow.createPerson.name"/>"/></td>
         <td align="right">工号：</td>
         <td align="left"><input name="loginEmployeeId" type="text" class="width01-readOnly" readonly id="loginEmployeeId" value="<s:property value="flow.createPerson.employeeId"/>"/></td>
         <td align="right">职称：</td>
         <td align="left"><input name="loginPostName" type="text" class="width02-readOnly" readonly id="loginPostName" value="<s:property value="flow.createPerson.postName"/>"/></td>
         <td align="right">分机：</td>
         <td align="left"><input name="loginPhone" type="text" class="width03-readOnly" readonly id="loginPhone" value="<s:property value="flow.createPerson.compPhone"/>"/></td>
		 <input name="createPerson.postCode" type="hidden" id="actualPostCode" value="<s:property value="#request.personDetail.postCode"/>"/>     
       </tr>
       <tr>
         <td>申&nbsp;请&nbsp; 单&nbsp;位：</td>
         <td colspan="7"><label class="width04">
         <input name="textfield3" type="text" class="width02-readOnly" id="textfield3" value="<s:property value="#request.dept1"/>"/>
         <input name="textfield4" type="text" class="width02-readOnly" id="textfield4" value="<s:property value="#request.dept2"/>"/>
         <input name="textfield5" type="text" class="width02-readOnly" id="textfield5" value="<s:property value="#request.dept3"/>"/>
         </label></td>
       </tr>
     </table>
   </div>
   <div class="contentstyle">
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="24" height="24" align="left" valign="top"><img src="<%=request.getContextPath()%>/images/ico02.gif" width="18" height="18" /></td>
         <td align="left" valign="middle" class="text02">内联明细</td>
         <td valign="middle" class="text02">&nbsp;</td>
       </tr>
     </table>
     <table width="100%" border="0" cellspacing="0" cellpadding="0">
       <tr>
         <td width="80" valign="top">发&nbsp;文&nbsp;单&nbsp;位：&nbsp;&nbsp;</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><span class="width04">
               <input name="fawenDept" type="text" class="width0-readOnly" id="fawenDept" readonly value="<s:property value="flow.createPerson.deptPath"/>" style="width:99%"/>
               <input name="content.deptId" type="hidden" id="fawenDeptId" value="<s:property value="flow.createPerson.deptId"/>"/>
             </span></td>
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top">机&nbsp;&nbsp;&nbsp;密&nbsp;&nbsp;&nbsp;&nbsp;性：&nbsp;&nbsp;<span class="text03">*</span></td>
         <td width="257"><label>
           </label>
           <table width="140" border="0" cellpadding="0" cellspacing="0">
               <td><input name="secretLevelName" type="text" id="secretLevelName" class="width01-readOnly" readonly/>
				   <input name="flow.content.secretLevel" type="hidden" id="secretLevel" value="<s:property value="flow.content.secretLevel"/>"/>
               </td>
           </table>
           <label></label></td>
         <td width="80">时&nbsp;&nbsp;&nbsp;效&nbsp;&nbsp;&nbsp;&nbsp;性：&nbsp;<span class="text03">*</span></td>
         <td width="337"><table width="140" border="0" cellpadding="0" cellspacing="0">
           <tr>
             <td><input name="exireLevelName" type="text" id="exireLevelName" class="width01-readOnly" readonly/>
             	 <input name="flow.content.exireLevel" type="hidden" id="exireLevel" value="<s:property value="flow.content.exireLevel"/>"/>
             </td>
           </tr>
         </table></td>
        </tr>
        <!-- 公用明细 -->
	   <jsp:include page="/pages/pub/flow_detail_foredit.jsp">
	   	<jsp:param name="flowType" value="neilian"/>
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
	<jsp:include page="/pages/pub/flow_attachment.jsp"></jsp:include>
	<!-- 签核历程 -->
	<jsp:include page="/pages/pub/flow_history.jsp"></jsp:include>
    <table width="300" border="0" cellpadding="0" cellspacing="0" class="center">
	  	<tr>
	  		<s:if test="#request.workStage!=null&&#request.role!='ASSIST_ASSIGNED'&&#request.role!='ASSIST_NOT_ASSIGNED'">
		  	    <s:if test="#request.workStage!='COPYDEPT'&&#request.workStage!='REJECT'">
		  	       <s:if test="#request.workStage=='CONFIRM' && !#request.assist">
				   	<td><input type="button" value="本人确认" onclick="applySubmit()"/></td>
				  	<td><input type="button" value="撤销" onclick="cancel()"/></td>	  		
				   </s:if>
				   <s:elseif test="#request.workStage=='JOINTSIGN'||#request.workStage=='INNERJOINTSIGN'||#request.workStage=='CMPCODEJOINTSIGN'">
					<s:if test="#request.deptName!=null">
                    <td><font color="red">（<s:property value="#request.deptName"/>）</font></td>
					</s:if>
					<s:if test="#request.curEmployee">
					<td><input type="button" value="提交" onclick="applySubmit()"/></td>
					</s:if>
					<s:if test="#request.hbMgrUser">
					<td><input type="button" value="呈核上一级" onclick="applySubmit_Ex()"/></td>
					</s:if>
				   </s:elseif>
				   <s:elseif test="!#request.assist">
				   	<td><input type="button" value="同意" onclick="applySubmit()"/></td>
				   </s:elseif>
				</s:if>
				<s:if test="#request.canRefuse && !#request.assist">
		       		    <td><input type="button" value="驳回" onclick="reject()"></td>
		    	</s:if>
			</s:if>
			<s:if test="#request.workStage=='JOINTSIGN'&&#request.role!='ASSIST_NOT_ASSIGNED'&&#request.hbMgrCanSign">
		    	<td><input type="button" value="指派" onclick="toAssign()" id="assign"></td>   		
		    </s:if>
			<s:if test="#request.workStage=='REJECT' && !#request.assist">
	         		<td><input type="button" value="本人确认" onclick="cancel()"/></td>

					<%--
	         		<td><input type="button" value="撤销" onclick="cancel()"/></td><!-- 暂时不需要 endFormSubmit -->
					--%>

			         <s:if test="#request.workStage=='REJECT'">
			         	<td align="right" valign="middle"><input id="renewButton" type="button" value="重新起案" onclick="renew()"></td>
			         </s:if>

				<!-- 	<td><input type="button" value="取消" onclick="cancel()"/></td>  -->
	        </s:if>

			<s:if test="#request.showCancelButton==1 && !#request.assist">
		    	<td><input type="button" value="撤销" onclick="cancelForm()" id="cancelForm"></td>   		
		    </s:if>
			<s:if test="#session.employeeId == 'Administrator' || #session.employeeId == 'admin'">
				<td id="adminAssignTd" style="display:none;"><input type="button" value="管理员指派" onclick="toAdminAssign()" id="adminAssign" /></td>
			</s:if>
			<td><input type="button" value="打印" id="printForm" onclick="printForm('<s:property value="flow.formNum"/>', 'nl')" /></td>

	  	</tr>
	</table>
  </div>
</div>
	<script>
		loadAttachments();
 		
 		//已选会办单位信息加载
 		var huibanDeptIds = $("#huibanDeptIds").attr("value");
 		var huibanDepts = $("#huibanDept").attr("value");
 		var oldhuibanDeptIds = huibanDeptIds;
 		
 		//已选抄送单位信息加载
 		var chaosongDeptIds = $("#chaosongDeptIds").attr("value");
 		var chaosongDept = $("#chaosongDept").attr("value");
 		var oldchaosongDeptIds = chaosongDeptIds;
 		
 		//用于会办组织过滤
 		var fawenDeptId = $("#fawenDeptId").attr("value");
 		var contentType = "1";
		var leader = $("#leader").attr("value");
		
		//用于指派过滤
		var assignDeptId = $("#assignDeptId").attr("value");
		
		//如果是查看的话，设置为只读
		var workStage = $("#workStage").attr("value");
		if(!workStage){
			document.getElementById("query3").disabled = "disabled";
			document.getElementById("query4").disabled = "disabled";
			document.getElementById("doUpload").disabled = "disabled";					
		}else{
			//除了呈核阶段，其他阶段不允许更改会办和抄送单位
			if(workStage=='CMPCODEJOINTSIGN' || workStage == 'JOINTSIGN' || workStage == 'BOSS_SIGN' || workStage == "CONFIRM"){
				document.getElementById("query3").disabled = "disabled";
			}
			//最终审核之前都可以附件上传
			if(workStage=='BOSS_SIGN'){
				document.getElementById("doUpload").disabled = "disabled";
				document.getElementById("query4").disabled = "disabled";	
			}
		}

		var renewTimes = $("#renewTimes").attr("value");
		if(renewTimes > 0) {
			if(document.getElementById("renewButton"))
			document.getElementById("renewButton").disabled = "disabled";
		}
	</script>
</body>
</html>
