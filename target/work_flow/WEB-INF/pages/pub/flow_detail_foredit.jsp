<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
       <tr>
         <td valign="top">主&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;旨：&nbsp;&nbsp;</td>
         <td colspan="6"><textarea name="title" cols="45" rows="5" class="textarea01-readOnly" id="title" readonly style="width:680px;height:70px"><s:property value="flow.content.title"/></textarea></td>
       </tr>
       <tr>
         <td valign="top">内&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;文：&nbsp;&nbsp;</td>
         <td colspan="6"><label>
           <textarea name="detail" cols="45" rows="5" id="detail"></textarea>
           <input name="detaildata" type="hidden" id="detaildata" value="<s:property value="flow.content.detail"/>"/>                 
         </label></td>
       </tr>
       <tr>
         <td valign="top" class="text03">核&nbsp;决&nbsp;主&nbsp;管：&nbsp;&nbsp;</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
           <tr>
             <td><table width="100%" border="0" cellpadding="0" cellspacing="0" class="text03">
               <tr>
               	<input name="descion" type="text" id="descionMaker" class="width01-readOnly" readonly/>
               	<input name="leader" type="hidden" id="leader" value="<s:property value="#request.leader"/>"/>
               	<input name="isHQTR" type="hidden" id="isHQTR" value="<s:property value="#request.isHQTR"/>"/>                           	           
               </tr>
             </table></td>
             <td align="right" style="display:none;"><table border="0" cellspacing="0" cellpadding="0">
               <tr>
                 <td>会&nbsp;办&nbsp;顺&nbsp;序：</td>
                 <td><table width="180" border="0" cellpadding="0" cellspacing="0">
                     <tr>
                        <input name="order" type="text" id="jointSignType" class="width01-readOnly" readonly/>             
               			<input name="joinType" type="hidden" id="joinType" value="<s:property value="#request.joinType"/>"/>             
                     </tr>
                 </table></td>
               </tr>
             </table></td>
             <td align="right">
             	<label><input name="selfconfirm" type="checkbox" value="1" id="selfconfirm" disabled/></label>本人确认
             	<c:if test="${not empty param.flowType && param.flowType == 'qiancheng'}">
             		<label><input name="submitBossValue" type="checkbox" value="1" id="submitBossValue" disabled/></label>呈核总裁
             	</c:if>
             </td>
             <input name="confirm" type="hidden" value="<s:property value="#request.confirm"/>" id="confirm"/>
             <input name="submitBossHid" type="hidden" value="<s:property value="#request.submitBoss"/>" id="submitBossHid"/>
             <script type="text/javascript">
             	 var selfconfirmValue = $("#confirm").attr("value");
             	 if(selfconfirmValue=='1'){
             	 	  $("[name='selfconfirm']").attr("checked",'true');    
             	 }
             	 var submitBossValue = $("#submitBossHid").val();
             	 if(submitBossValue == '1'){
             	 	$("[name='submitBossValue']").attr("checked",true);
             	 }
             	 
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
               <td>
               	<div id="query3" class="button01" onclick="toOrganQuery()">查询组织</div>
               </td>           
             </label></td>
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
               <textarea name="copyDemo" cols="45" rows="3" class="textarea02-readOnly" id="copyDemo" value="<s:property value="flow.copyDemo"/>" readonly></textarea>
               <input name="copyDemohidden" type="hidden" id="copyDemohidden" value="<s:property value="flow.copyDemo"/>"/>     
             </label></td>
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top">建&nbsp;议&nbsp;方&nbsp;案：</td>
         <td colspan="6"><textarea name="scheme" cols="45" rows="3" class="textarea01-readOnly" id="scheme" readonly></textarea></td>
         <input name="schemehidden" type="hidden" id="schemehidden" value="<s:property value="flow.content.scheme"/>"/> 
       </tr>
       
       	<s:if test="#request.dispAgree!=null&&#request.dispAgree">
   		<tr>
   			<td valign="top" rowspan="2">&nbsp;意&nbsp;见&nbsp;：</td>
   			<td colspan="6">
   			<input type="radio" id="hb_isagree" name="hb_isagree" value="1" checked="checked" />同意
   			<input type="radio" id="hb_isagree" name="hb_isagree" value="2" />不同意
   			</td>
     	</tr>
     	<tr>
     		<td colspan="6"><textarea name="opinion" cols="45" rows="4" class="validate[required] text-input" id="opinion" style="width:99%;"></textarea></td>
     		<input name="opinionhidden" type="hidden" id="opinionhidden" value="<s:property value="#request.opinion"/>"/>
     	</tr>
       	</s:if>
       	<s:else>
       	<tr>
         <td valign="top">&nbsp;意&nbsp;见&nbsp;：</td>
         <td colspan="6"><textarea name="opinion" cols="45" rows="4" class="validate[required] text-input" id="opinion" style="width:99%;"></textarea></td>
         <input name="opinionhidden" type="hidden" id="opinionhidden" value="<s:property value="#request.opinion"/>"/>
        </tr>
        </s:else> 
       </tr>
