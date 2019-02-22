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
<title>助理维护界面</title>
<jsp:include page="/pages/common_agent.jsp"></jsp:include>
<script type="text/javascript" src="${ctx }/js/layout.js"></script>
<script type="text/javascript">
var returnUrl = "${ctx}/pages/assist/assist_query.jsp";

//!!!!2012-9-12  改回与代理一致
//query param & oragn_user.jsp(125)
//查询助理人员
function toOrganQuery(checkPosition){

	//9-20: 判断单位是否空白
	if (checkPosition && !$("#position").val()) {
		alert("请先选择单位。");
		return;
	}
	
	$('#deptQuery').dialog({
			id:'dlg1',
			height:300,
			width:400,
			title:'单位查询',
			content:'url:${ctx}/pages/assist/assist_user.jsp?query=assist'
		});
		
	$('#assistQuery').dialog({
			id:'dlg2',
			height:400,
			width:700,
			title:'按组织查询',
			content:'url:${ctx}/pages/organ_user.jsp?query=assist'
		});
		$.dialog.data('deptId',deptId);
	//$.dialog.data('deptId',personCompanyId);		
}

//单位发生变化后，重新塞值
function setAssistPosition(assistPosition){
	var positionName = assistPosition.selectedDeptName+"("+assistPosition.selectedPostName+")";
	$("#position").val(positionName);
	$("#selectedDeptId").val(assistPosition.selectedDeptId);
	$("#selectedDeptName").val(assistPosition.selectedDeptName);				
	$("#selectedPostCode").val(assistPosition.selectedPostCode);				
	$("#selectedPostName").val(assistPosition.selectedPostName);
	
	deptId = assistPosition.selectedDeptId;
	
	//清空助理人员信息
	$("#assistInfo").attr("value","");
	$("#assistEmployeeId").attr("value","");
	$("#assistEmployeeName").attr("value","");
	$("#assistPostCode").attr("value","");
	$("#assistPostName").attr("value","");	
}

//添加助理人员
function setAssist(personDetail){
	if(personDetail){
		$("#assistInfo").val(personDetail.name+"("+personDetail.employeeId+")"+personDetail.postName);
		$("#assistEmployeeId").val(personDetail.employeeId);
		$("#assistEmployeeName").val(personDetail.name);
		$("#assistPostCode").val(personDetail.postCode);
		$("#assistPostName").val(personDetail.postName);
	}
}

//取消
function cancel(){
	$("#position").val("");
	window.location.href=returnUrl;
}
</script>
</head>

<body onkeydown="keyDown()" oncontextmenu="event.returnValue=false">
 <div class="rightpage01">
  <div class="headbg01">助理人设置</div>
  <form id="applyForm" method="post" name="applyForm">
  <input name="selectedDeptId" type="hidden" id="selectedDeptId" value="<s:property value="#request.personDetail.deptId"/>"/>
  <input name="selectedDeptName" type="hidden" id="selectedDeptName" value="<s:property value="#request.personDetail.deptName"/>"/>
  <input name="selectedPostCode" type="hidden" id="selectedPostCode" value="<s:property value="#request.personDetail.postCode"/>"/>
  <input name="selectedPostName" type="hidden" id="selectedPostName" value="<s:property value="#request.personDetail.postName"/>"/>
  <input name="selectedAssistEmployeeId" type="hidden" id="assistEmployeeId"/>      
  <input name="selectedAssistEmployeeName" type="hidden" id="assistEmployeeName"/>      
  <input name="selectedAssistPostCode" type="hidden" id="assistPostCode"/>      
  <input name="selectedAssistPostName" type="hidden" id="assistPostName"/>
  <input name="personCompanyId" type="hidden" id="personCompanyId" value="<s:property value="#request.personCompanyId"/>"/>      
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td><table width="800" border="0" cellspacing="8" cellpadding="0">
        <tr>
          <td align="right">单位：</td>
          <td><table border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td><input type="text" name="position" id="position"  class="validate[required] appInput" 
                	style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px;width:260px" readonly/></td>
                <td><div class="button blue small" id="deptQuery" onclick="toOrganQuery()">查&nbsp;询</div></td>
              </tr>
            </table></td>
        </tr>
        <tr>
          <td align="right">允许查看：</td>
          <td style="margin:5px;padding:5px;">
            <div class="rule-single-checkbox">
          	<input type="checkbox" name="allowReceive" id="allowReceive" value="1"/>
          	</div>
          	&nbsp;（此处勾选后，助理人可以查看主管待审核的表单）
          </td>
        </tr>
        <tr>
          <td align="right">允许指派：</td>
          <td style="margin:5px;padding:5px;">
          <div class="rule-single-checkbox">
          <input type="checkbox" name="allowAssign" id="allowAssign" value="1"/>
          </div>
          &nbsp;（此处勾选后，助理人可以协助主管对待审核表单进行指派操作。勾选指派需先勾选“查看”）
          </td>
        </tr>
        <tr>
          <td align="right">助理：</td>
          <td><table border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td><input type="text" name="assistInfo" id="assistInfo"  class="validate[required] appInput" 
                	style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px;width:260px" readonly/></td>
                <td><div class="button blue small" id="assistQuery" onclick="toOrganQuery(true)">查&nbsp;询</div></td>
              </tr>
            </table></td>
        </tr>
        <tr>
          <td align="right">&nbsp;</td>
          <td align="left"><table width="200" border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td><div class="button04" id="applySubmit"></div></td>
              <td><div class="button05" onclick="cancel()"></div></td>
            </tr>
          </table></td>
        </tr>
      </table></td>
    </tr>
  </table>
  </form>
</div>
<script type="text/javascript">
	//根据登录人的主岗位初始化值
	var deptId = $("#selectedDeptId").attr("value");
	var posiCode = $("#selectedPostCode").attr("value");
	var posiName = $("#selectedPostName").attr("value");
	var deptName = $("#selectedDeptName").attr("value");
	var position = deptName + "(" + posiName + ")";
	//$("#position").val(position);
	
	//9-20: 单位默认空白
	$("#position").val("");
	
	var personCompanyId = $("#personCompanyId").attr("value");
	
	//提交助理信息
	var saveUrl="${ctx}/assist/assistAction!submitAssist.action";
	var successMsg = "助理添加成功!";
	sumbitAssist(saveUrl,successMsg,returnUrl);
</script>
</body>
</html>

