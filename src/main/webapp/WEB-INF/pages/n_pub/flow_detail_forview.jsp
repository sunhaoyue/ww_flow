<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<tr>
	<td valign="top">主&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;旨：&nbsp;</td>
	 <td colspan="6"><textarea name="title" cols="45" rows="5" class="textarea01-readOnly" id="title" readonly style="width:680px;height:70px"><s:property value="flow.content.title"/></textarea></td>
</tr>
<tr>
	<td valign="top">内&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;文：&nbsp;</td>
	<td colspan="6"><label>
		<textarea name="detail" cols="45" rows="5" id="detail"></textarea>
		<input name="detaildata" type="hidden" id="detaildata" value="<s:property value="flow.content.detail"/>"/>
	</label></td>
</tr>     
<tr>
	<td valign="top">核&nbsp;决&nbsp;主&nbsp;管：&nbsp;</td>
	<td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td><table width="100%" border="0" cellpadding="0" cellspacing="0" class="text03">
			<tr>
				<input name="descion" type="text" id="descionMaker" class="width01-readOnly" readonly/>
				<input name="leader" type="hidden" id="leader" value="<s:property value="#request.leader"/>"/>
			</tr>
		</table></td>
		<td align="right">
			<span id="office_span" style="display:none;">
			<input name="submitBOffice" type="checkbox" value="1" id="submitBOffice" disabled>呈核总裁办公室
			</span>
			<c:if test="${not empty param.flowType && param.flowType == 'qiancheng'}">
				<label><input name="submitFBoss" type="checkbox" value="1" id="submitFBoss" onclick="toXSubmitFBoss();" disabled/></label>呈核副总裁
				<label><input name="submitBoss" type="checkbox" value="1" id="submitBoss" onclick="toXSubmitBoss();" disabled/></label>呈核总裁
			</c:if>
		</td>
		<input name="confirm" type="hidden" value="<s:property value="#request.confirm"/>" id="confirm"/>
		<input name="submitBOfficeHid" type="hidden" value="<s:property value="#request.submitBOffice"/>" id="submitBOfficeHid"/>
		<input name="submitFBossHid" type="hidden" value="<s:property value="#request.submitFBoss"/>" id="submitFBossHid"/>
		<input name="submitBossHid" type="hidden" value="<s:property value="#request.submitBoss"/>" id="submitBossHid"/>
		<script type="text/javascript">
		var selfconfirmValue = $("#confirm").attr("value");
		if(selfconfirmValue=='1'){
			$("[name='selfconfirm']").attr("checked",'true');
		}
		var submitBossValue = $("#submitBossHid").val();
		if(submitBossValue == '1'){
			$("[name='submitBoss']").attr("checked",true);
		}
		function toXSubmitBoss(){
			var x = document.getElementById("submitBoss");
			if (x.checked){
				$("#submitBossHid").val("1");
			} else {
				$("#submitBossHid").val("0");
			}
		}
		var submitFBossValue = $("#submitFBossHid").val();
		if(submitFBossValue == '1'){
			$("[name='submitFBoss']").attr("checked",true);
		}
		function toXSubmitFBoss(){
			var x = document.getElementById("submitFBoss");
			if (x.checked){
				$("#submitFBossHid").val("1");
			} else {
				$("#submitFBossHid").val("0");
			}
		}
		var submitBOfficeValue = $("#submitBOfficeHid").val();
		if (submitBOfficeValue == '1'){
			$("[name='submitBOffice']").attr("checked",true);
		}
		</script>
	</tr>
	</table></td>
</tr>      
<tr>
	<td valign="top">会&nbsp;办&nbsp;单&nbsp;位：</td>
	<td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-bottom:2px;">
		<tr>
			<td><label>
				<textarea name="huibanDept" cols="45" rows="3" class="textarea01 appInput" id="huibanDept" readonly></textarea>
				<input name="huibanDeptIds" type="hidden" id="huibanDeptIds" value="<s:property value="#request.huibanDeptIds"/>"/>
				<input name="jointSignDeptName" type="hidden" id="jointSignDeptName" value="<s:property value="flow.jointSignDeptName"/>"/>
			</label></td>
		</tr>
	</table></td>
</tr>
<tr>
	<td valign="top">抄送 / 受文<br/>单&nbsp;&nbsp;&nbsp;&nbsp;位：</td>
	<td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-bottom:2px;">
	<tr>
		<td><label>
		<textarea name="chaosongDept" cols="45" rows="3" class="textarea01 appInput" id="chaosongDept" readonly></textarea>
		<input name="chaosongDeptIds" type="hidden" id="chaosongDeptIds" value="<s:property value="#request.chaosongDeptIds"/>"/>
		<input name="copyDeptName" type="hidden" id="copyDeptName" value="<s:property value="flow.copyDeptName"/>"/>
		</label></td>
	</tr>
	</table></td>
</tr>
<tr>
	<td valign="top">抄&nbsp;送&nbsp;备&nbsp;注：</td>
	<td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-bottom:2px;">
	<tr>
		<td><label>
		<textarea name="copyDemo" cols="45" rows="5" class="textarea01-readOnly" id="copyDemo" value="<s:property value="flow.copyDemo"/>" readonly></textarea>
		</label></td>
	</tr>
	</table></td>
</tr>
<tr>
	<td valign="top">内&nbsp;部&nbsp;会&nbsp;签：</td>
	<td colspan="6">
		<textarea cols="45" rows="5" class="textarea01-readOnly"><s:property value="flow.innerJointSignName"/></textarea>
	</td>
</tr>
<tr>
	<td valign="top">建&nbsp;议&nbsp;方&nbsp;案：</td>
	<td colspan="6">
		<textarea name="scheme" cols="45" rows="5" class="textarea01-readOnly" id="scheme" readonly></textarea>
	</td>
	<input name="schemehidden" type="hidden" id="schemehidden" value="<s:property value="flow.content.scheme"/>"/> 
</tr>