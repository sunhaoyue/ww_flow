<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/pages/taglibs.inc" %>
<html>
<head>
<title>grid</title>
<link href="${ctx }/css/csspage_01.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" href="${ctx }/css/jquery-ui-1.8.21.custom.css" />
<link rel="stylesheet" type="text/css" href="${ctx }/css/ui.jqgrid.css" />
<script type="text/javascript" src='${ctx }/js/jquery-1.7.1.min.js'></script>
<script type="text/javascript" src="${ctx }/js/jqgrid/grid.locale-cn.js"></script>
<script type="text/javascript" src="${ctx }/js/jqgrid/jquery.jqGrid.min.js"></script>
</head>
 
 <body style="padding: 20px;">
  	<div class="bgc01">
	<table id="list1"></table>
	</div>
 	<script type="text/javascript">
 		var position;
 		var mainheight = $(window).height();
		var mainwidth = $(window).width();
		var w = mainwidth - 10;
		var h = mainheight - 180;
 		//弹出框按钮操作
		var api = frameElement.api, W = api.opener;
		api.button({
			  id:'valueOk',
			  name:'确定',
			  callback:confirm
		},
		{
			  id:'value',
			  name:'取消'
		});
		
		function confirm(){
			parent.setPosition(position);
		}
		
		jQuery("#list1").jqGrid({ 
			 datatype: "local",
			 shrinkToFit:false,
	  		 width:340, 
			 height: 200, 
			 colNames:['单位名称','岗位名称', '岗位代码', '单位名称'], 
			 colModel:[ {name:'deptPath',index:'deptPath', width:180}, 
			 		{name:'selectedPostName',index:'selectedPostName', width:120},
			 		{name:'selectedPostCode',index:'selectedPostCode', hidden:true},
			 		{name:'selectedDeptName',index:'selectedDeptName', hidden:true}
			 ],
			 onSelectRow: function(id){	
				 	var rowdata = $("#list1").jqGrid("getRowData",id);
				 	position = rowdata;	
	      	 }
		}); 
		
		//加载主管岗位信息
		$.ajax({
			type: "POST",
			url: '${ctx}/agent/agentAction!loadOperatorInfo.action',
			dataType:'json',		    
			success: function (data) {
				if(data.length>0){
					for(var i=0;i<=data.length;i++){
					 	jQuery("#list1").jqGrid('addRowData',i+1,data[i]);
					}		        		
				}
			}
		 })
 	</script>
 </body>
 </html>