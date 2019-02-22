<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ include file="/pages/taglibs.inc" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>

<link rel="stylesheet" type="text/css" href="${ctx }/css/jquery-ui-1.8.21.custom.css" />
<link rel="stylesheet" type="text/css" href="${ctx }/css/ui.jqgrid.css" />
<link rel="stylesheet" type="text/css" href="${ctx }/css/jquery-ui-1.8.21.custom.css" />

<script type="text/javascript" src='${ctx }/js/jquery-1.7.1.min.js'></script>
<script type="text/javascript" src='${ctx }/js/jquery-ui-1.8.18.custom.min.js'></script>
<script type="text/javascript" src="${ctx }/js/jqgrid/grid.locale-cn.js"></script>
<script type="text/javascript" src="${ctx }/js/jqgrid/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="${ctx }/js/lhgdialog/lhgdialog.min.js"></script>



<style type="text/css">
* { font-size:12px; line-height:24px; }
</style>

</head>

<body>
	<ul>
	 	<li><span id="content"></span></li>
	 	<li><span id="content2"></span></li>
	</ul> 
	<ul><table id="userList"></table></td></ul> 
     <script>
     	var api = frameElement.api, W = api.opener;
		var content = W.$.dialog.data('content');
		var content2 = W.$.dialog.data('content2');
		var innerhuibanIds = W.$.dialog.data('innerhuibanIds');
		var huibanpersons = W.$.dialog.data('huibanpersons');
		var flowId = W.$.dialog.data('flowId');
		
		$("#content").text(content);
		$("#content2").text(content2);
		
		jQuery("#userList").jqGrid({ 
			datatype: "local", 
			height: 100, 
			colNames:['会办人工号', '会办人姓名'], 
			colModel:[
				{name:'employeeId',index:'employeeId', width:120}, 
			 	{name:'name',index:'name', width:120}
			 	], 
			 caption: ""}); 
 		
 		$.ajax({
			type: "POST",
			url: '${ctx }/user/loadJoinUsers.action',
			dataType:'json',
			data:{'innerhuibanIds':innerhuibanIds,'huibanpersons':huibanpersons,'flowId':flowId},		    
			success: function (data,status) {
				if(data.length>0){
					for(var i=0;i<=data.length;i++){
						jQuery("#userList").jqGrid('addRowData',i+1,data[i]);
					}		        		
				}
			}
 		})		
	</script>
 
</body>
</html>
