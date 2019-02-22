<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
	<script type="text/javascript">
 		function leaderClick(){
			leader =$('input:radio[name="leader"]:checked').val();
 		}
	</script>
	<c:if test="${not empty param.flowType && param.flowType == 'qiancheng'}">
		<input type="hidden" id="flowType" value="1"/>
	</c:if>
	<c:if test="${not empty param.flowType && param.flowType == 'neilian'}">
		<input type="hidden" id="flowType" value="2"/>
	</c:if>
       <tr>
         <td valign="top">主&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;旨：&nbsp;&nbsp;<span class="text03">*</span></td>
         <td colspan="6"><textarea name="title" cols="45" rows="5" class="textarea01 appInput" id="title" style="width:680px;height:70px"><s:property value="flow.content.title"/></textarea></td>
       </tr>
       <tr>
         <td valign="top">内&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;文：&nbsp;&nbsp;<span class="text03">*</span></td>
         <td colspan="6"><label>
           <textarea name="content.detail" cols="45" rows="5" class="validate[required]" id="detail"></textarea>
           <input name="detaildata" type="hidden" id="detaildata" value="<s:property value="flow.content.detail"/>"/>                            
         </label></td>
       </tr>
       <tr>
         <td valign="top" class="text03">核&nbsp;决&nbsp;主&nbsp;管：&nbsp;&nbsp;*</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><table width="100%" border="0" cellpadding="0" cellspacing="0" class="text03" id="leaderTable">
               <tr>
                 <td id="deptleader"><label>
                   <input name="leader" type="radio" value="DEPTLEADER" class="validate[required]" id="leaderRadio" onclick="leaderClick()"/>
                   </label>
                   	<s:if test="#request.isHQTR">
						单位/部门主管
					</s:if>
					<s:elseif test="!#request.isHQTR">
						部门主管
					</s:elseif>
                   </td>
                 <td id="centalleader"><label>
                   <input name="leader" type="radio" value="CENTRALLEADER" class="validate[required]" id="leaderRadio" onclick="leaderClick()"/>
                   </label>
                   	<s:if test="#request.isHQTR">
						单位最高主管
					</s:if>
					<s:elseif test="!#request.isHQTR">
						中心主管
					</s:elseif>
                   </td>
                 <td id="unitleader"><label>
                   <input name="leader" type="radio" value="UNITLEADER" class="validate[required]" id="leaderRadio" onclick="leaderClick()"/>
                   </label>
                   		<s:if test="#request.isHQTR">
						神旺控股最高主管
					</s:if>
					<s:elseif test="!#request.isHQTR">
						单位最高主管
					</s:elseif>
                   </td>
                 <td id="reginleader" style="display:none"><label>
                   <input name="leader" type="radio" value="REGINLEADER" class="validate[required]" id="leaderRadio" onclick="leaderClick()"/>
                   </label>                   
                   事业部最高主管</td>
                 <td id="headleader" style="display:none"><label>
                   <input name="leader" type="radio" value="HEADLEADER" class="validate[required]" id="leaderRadio" onclick="leaderClick()"/>
                 </label>
				神旺控股最高主管</td>
               </tr>
             </table></td>
             <input name="leaderValue" type="hidden" id="leader" value="<s:property value="#request.leader"/>"/>                           	           
	           <script type="text/javascript">
	           $(document).ready(function () {
	               var leaderRadios = document.applyForm.leaderRadio;
	               var leaderValue = $("#leader").attr("value");
	               if(leaderValue){
					   for(var i=0;i<leaderRadios.length;i++ ){
							if(leaderRadios[i].value==leaderValue){
								leaderRadios[i].checked=true;
								if (leaderValue.toLowerCase() == "unitleader"){
									$("#submitBoss").attr("disabled",false);
								}
							}
					   }
				   }
	           });
	           </script> 
	         <td align="right">
	         	<label><input name="selfconfirm" type="checkbox" value="1" id="selfconfirm"/></label>本人确认
	         	<c:if test="${not empty param.flowType && param.flowType == 'qiancheng'}">
             		<label><input name="submitBoss" type="checkbox" value="1" id="submitBoss" /></label>呈核总裁
             	</c:if>
	         </td>
	         
             <input name="confirm" type="hidden" value="<s:property value="#request.confirm"/>" id="confirm"/>
             <input name="submitBossHid" type="hidden" value="<s:property value="#request.submitBoss"/>" id="submitBossHid"/>
             <script type="text/javascript">
             $(document).ready(function () {
             	 var selfconfirmValue = $("#confirm").attr("value");
             	 if(selfconfirmValue=='1'){
             	 	  $("[name='selfconfirm']").attr("checked",'true');    
             	 }
             	 var submitBossValue = $("#submitBossHid").val();
             	 if(submitBossValue == '1'){
             	 	$("[name='submitBoss']").attr("checked",true);
             	 }
             });
             </script>
             <td align="right" style="display:none;"><table border="0" cellspacing="0" cellpadding="0">
               <tr>
                 <td>会&nbsp;办&nbsp;顺&nbsp;序：<span class="text03">*</span></td>
                 <td><table width="180" border="0" cellpadding="0" cellspacing="0">
                     <tr>
                       <td><label>
                         <input name="joinType" type="radio" id="joinTypeRadio" value="CONCURRENT" />
                         </label>
                         同时会办</td>
                       <td><label>
                         <input name="joinType" type="radio" id="joinTypeRadio" value="SEQUENCE" />
                         </label>
                         循序会办</td>
                     </tr>
                 </table></td>
               </tr>
             </table></td>
             <input name="joinTypeValue" type="hidden" id="joinType" value="<s:property value="#request.joinType"/>"/>             
	           <script type="text/javascript">
	           $(document).ready(function () {
	               var joinTypeRadios = document.applyForm.joinTypeRadio;
	               var joinTypeValue = $("#joinType").attr("value");
				   for(var i=0;i<joinTypeRadios.length;i++ ){
						if(joinTypeRadios[i].value==joinTypeValue){
							joinTypeRadios[i].checked=true;
						}
				   }
	           });
	           </script>                   
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top">会&nbsp;办&nbsp;单&nbsp;位：</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><label>
               <textarea name="huibanDept" cols="45" rows="3" class="textarea02" id="huibanDept" readonly></textarea>
               <input name="huibanDeptIds" type="hidden" id="huibanDeptIds" value="<s:property value="#request.huibanDeptIds"/>"/>
               <input name="jointSignDeptName" type="hidden" id="jointSignDeptName" value="<s:property value="flow.jointSignDeptName"/>"/>             
             </label></td>
             <td><div id="query3" class="button01" onclick="toOrganQuery()">查询组织</div></td>
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top">抄&nbsp;送&nbsp;单&nbsp;位：</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><label>
               <textarea name="chaosongDept" cols="45" rows="3" class="textarea02" id="chaosongDept" readonly></textarea>
               <input name="chaosongDeptIds" type="hidden" id="chaosongDeptIds" value="<s:property value="#request.chaosongDeptIds"/>"/>
               <input name="copyDeptName" type="hidden" id="copyDeptName" value="<s:property value="flow.copyDeptName"/>"/>                       
             </label></td>
             <td><div id="query4" class="button01" onclick="toOrganQuery()">查询组织</div></td>
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top">抄&nbsp;送&nbsp;备&nbsp;注：</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><label>
               <textarea name="copyDemo" cols="45" rows="3" class="textarea02" id="copyDemo" value="<s:property value="flow.copyDemo"/>"></textarea>
               <input name="copyDemohidden" type="hidden" id="copyDemohidden" value="<s:property value="flow.copyDemo"/>"/>     
             </label></td>
             <td>&nbsp;</td>
           </tr>
         </table></td>
       </tr>
       <tr id="inner">
         <td valign="top">内&nbsp;部&nbsp;会&nbsp;办：</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><span class="width04">
               <input name="innerhuiban" type="text" class="width0" id="innerhuiban" readonly/>
               <input name="innerhuibanIds" type="hidden" id="innerhuibanIds" value="<s:property value="#request.innerhuibanIds"/>"/> 
               <input name="innerJointSignName" type="hidden" id="innerJointSignName" value="<s:property value="flow.innerJointSignName"/>"/>                       
             </span></td>
             <td width="140"><div class="button02" onclick="addInnerhuiban()">添&nbsp;加</div>
             <div id="query5" class="button01" onclick="toOrganQuery()">查询组织</div>             
           </tr>
         </table></td>
       </tr>
       <tr id="innerNames">
         <td colspan="7" valign="top"><textarea name="innerhuibanNames" cols="45" rows="5" class="textarea01" id="innerhuibanNames" readonly></textarea>
         	<div class="button02" onclick="removeInnerhuiban()">清&nbsp;除</div>
         </td>
        </tr>
       <tr>
         <td valign="top">建&nbsp;议&nbsp;方&nbsp;案：</td>
         <td colspan="6"><textarea name="content.scheme" cols="45" rows="3" class="textarea01" id="scheme"></textarea></td>
         <input name="schemehidden" type="hidden" id="schemehidden" value="<s:property value="flow.content.scheme"/>"/>          
       </tr>
