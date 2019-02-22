<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
         <td valign="top">主&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;旨：&nbsp;<span class="text03">*</span></td>            
         <td colspan="6"><textarea name="content.title" cols="45" rows="5" class="textarea01 appInput" id="title" style="width:680px;height:120px"></textarea></td>
       </tr>
       <tr>
         <td valign="top">内&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;文：&nbsp;<span class="text03">*</span></td>
         <td colspan="6"><label>
           <textarea name="content.detail" cols="45" rows="5" class="validate[required]" id="detail"></textarea>
           <input name="detailValue" type="hidden" id="detailValue"/>
         </label></td>
       </tr>
       <tr>
		<td valign="top" class="text03">核&nbsp;决&nbsp;主&nbsp;管：&nbsp;*</td>
		<td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
		  <tr>
		    <td><table width="100%" border="0" cellpadding="0" cellspacing="0" class="text03" id="leaderTable">
		      <tr>
		        <td id="deptleader">
					<label>
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
				<td id="unitleader" style='display:block;'><label>
				  <input name="leader" type="radio" value="UNITLEADER" class="validate[required]" id="leaderRadio" onclick="leaderClick()"/>
				  </label>
				  	<s:if test="#request.isHQTR">
						神旺控股执行总经理
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
				神旺控股执行总经理</td>
		     </tr>
		     <input name="leaderValue" type="hidden" id="leader"/>
             </table></td>
             <td align="right">
				<span style="display:none;"><input name="selfconfirm" type="checkbox" value="1" id="selfconfirm" checked/>本人确认</span>
				<span id="office_span" style="display:none;">
				<input name="submitBOffice" type="checkbox" value="1" id="submitBOffice">呈核总裁办公室
				</span>
             	<c:if test="${not empty param.flowType && param.flowType == 'qiancheng'}">
             		<label><input name="chairman" type="checkbox" value="1" id="chairman"/></label>神旺控股董事长
             		<label><input name="submitFBoss" type="checkbox" value="1" id="submitFBoss"/></label>呈核副总裁
             		<label><input name="submitBoss" type="checkbox" value="1" id="submitBoss"/></label>呈核总裁
             	</c:if>
             </td>
             <td align="right" style="display:none;"><table border="0" cellspacing="0" cellpadding="0">
               <tr>
                 <td>会&nbsp;办&nbsp;顺&nbsp;序：<span class="text03">*</span></td>
                 <td><table width="180" border="0" cellpadding="0" cellspacing="0">
                     <tr>
                       <td><label>
                         <input name="joinType" type="radio" id="joinType1" value="CONCURRENT" checked="checked" />
                         </label>
                         同时会办</td>
                       <td><label>
                         <input name="joinType" type="radio" id="joinType2" value="SEQUENCE" />
                         </label>
                         循序会办</td>
                     </tr>
                 </table></td>
               </tr>
             </table></td>
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top">会&nbsp;办&nbsp;单&nbsp;位：</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-bottom:2px">
           <tr>
             <td><label>
               <textarea name="huibanDept" cols="45" rows="3" class="textarea02 appInput" id="huibanDept" readonly></textarea>
               <input name="huibanDeptIds" type="hidden" id="huibanDeptIds"/>
             </label></td>
             <td><div id="query3" class="button blue small" onclick="toOrganQuery('huiban')">查询组织</div></td>
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top">抄送 / 受文<br/>单&nbsp;&nbsp;&nbsp;&nbsp;位：</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-bottom:2px">
           <tr>
             <td><label>
               <textarea name="chaosongDept" cols="45" rows="3" class="textarea02 appInput" id="chaosongDept" readonly></textarea>
               <input name="chaosongDeptIds" type="hidden" id="chaosongDeptIds"/>     
             </label></td>
             <td><div id="query4" class="button blue small" onclick="toOrganQuery()">查询组织</div></td>
           </tr>
         </table></td>
       </tr>
       <tr>
         <td valign="top">抄&nbsp;送&nbsp;备&nbsp;注：</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-bottom:2px">
           <tr>
             <td><label>
               <textarea name="copyDemo" cols="45" rows="3" class="textarea02 appInput" id="copyDemo"></textarea>
             </label></td>
             <td>&nbsp;</td>
           </tr>
         </table></td>
       </tr>
       <tr id="inner">
         <td valign="top">内&nbsp;部&nbsp;会&nbsp;签：</td>
         <td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-bottom:2px">
           <tr>
             <td><span class="width04">
               <input name="innerhuiban" type="text" class="width0 appInput" id="innerhuiban" readonly/>
               <input name="innerhuibanIds" type="hidden" id="innerhuibanIds"/>        
             </span></td>
             <td width="155">&nbsp;
             <div class="button blue small" onclick="addInnerhuiban()">添&nbsp;加</div>
             <div id="query5" class="button blue small" onclick="toOrganQuery()">查询组织</div>
             </td>         
           </tr>
         </table></td>
       </tr>
       <tr id="innerNames">
		<td valign="top"></td>	
         <td colspan="6" valign="top"><textarea name="innerhuibanNames" cols="45" rows="5" class="textarea01 appInput" id="innerhuibanNames" style="width:580px" readonly></textarea>
         	<div class="button blue small" onclick="removeInnerhuiban()">清&nbsp;除</div>
         </td>
         
       </tr>
       <tr>
         <td valign="top">建&nbsp;议&nbsp;方&nbsp;案：</td>
         <td colspan="6"><textarea name="content.scheme" cols="45" rows="5" class="textarea01 appInput" id="scheme" style="width:580px;height:120px"></textarea></td>
       </tr>

