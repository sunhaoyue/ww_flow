<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@page isELIgnored="false"%>
 <html>
 
 <head>
 	<title>grid</title>
 	<link href="<%=request.getContextPath()%>/css/csspage_01.css" rel="stylesheet" type="text/css" />
	<link id="skin" rel="stylesheet" type="text/css"
				href="<%=request.getContextPath()%>/css/jquery-ui-1.8.21.custom.css" />
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/ui.jqgrid.css" />
	<script type="text/javascript" src='<%=request.getContextPath()%>/js/jquery-1.7.1.min.js'></script>
 	<script type="text/javascript" src="<%=request.getContextPath()%>/js/jqgrid/grid.locale-cn.js"></script>
 	<script type="text/javascript" src="<%=request.getContextPath()%>/js/jqgrid/jquery.jqGrid.min.js"></script>
 </head>
 
 <body style="padding: 20px;">
 <div class="bgc01" style="width:650px;">
	<table id="childhistory"></table>
 </div>
	<script>
	function statusCellAttr(rowId, val, rowObject, cm, rdata){
		if (rowObject.hbAgree == "2"){
			return "style='color:red;'";
		}
	}
			var workId = '${param.workId}';
			var formNum = '${param.formNum}';

	 		jQuery("#childhistory").jqGrid({
	 			datatype: "local",
				autowidth:true,//根据父容器调整宽度,只在初始化阶段起作用
				height: 270,
				width: 370,
	 			colNames:['部门','审批人', '时间', '状态','意见', '是否有子历程', '流程编号'], 
	 			colModel:[ {name:'deptName',index:'deptName', width:100}, 
	 				 {name:'userRealName',index:'userRealName', width:60}, 
	 				 {name:'processTime',index:'processTime', width:60}, 
	 				 {name:'workStatus',index:'workStatus', width:40, align:'center',cellattr:statusCellAttr},
	 				 {name:'opinion',index:'opinion', width:110},
	 				 {name:'haveChildren',index:'haveChildren', width:0, hidden:true},
	 				 {name:'workId',index:'workId', width:0, hidden:true}	   
	 			]
 			});
 			
 			$.ajax({
			    type: "POST",
			    url: '<%=request.getContextPath()%>/qianchen/listWorkHistory.action',
			    dataType:'json',
			   	data:{'workId':workId,'formNum':formNum},
			    success: function (data,status ) {
			    	if(data.length>0){
			        	for(var i=0;i<=data.length;i++){
			 				jQuery("#childhistory").jqGrid('addRowData',i+1,data[i]);
			  			}		        		
			        }
		    	}
 			});
 			
	</script>
 </body>
 </html>