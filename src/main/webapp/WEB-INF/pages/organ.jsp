<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page isELIgnored="false"%>

<html>

	<head>
		<title>组织树</title>
		<script type="text/javascript" src='<%=request.getContextPath()%>/js/jquery-1.7.1.min.js'></script>
		<script type="text/javascript" src='<%=request.getContextPath()%>/js/jquery-ui-1.8.18.custom.min.js'></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/js/ztree/jquery.ztree.core-3.2.js"></script>
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/zTreeStyle/zTreeStyle.css" />
	</head>

	<body style="padding: 20px;">
	<ul id="organTree" class="ztree" width="210px;"></ul> 
	<script type="text/javascript">
		var type = '${param.query}';
		var nodeObj;
		function fliterData(treeId,parentNode,childNodes){
			if(childNodes){
				if(childNodes.msg){
					alert(childNodes.msg);
					return childNodes.result||{};
				}
			}
			return childNodes;
 		}
 		
 		//确认组织选择
		function confirm(){
			parent.setFawenDept(nodeObj);
		}
 		
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
		
 		
		$(function(){
			function zTreeOnClick(event, treeId, treeNode) {
				nodeObj = treeNode;
			}			
 			var setting={
 				async:{
 					enable:true,
 					url:'<%=request.getContextPath()%>/organ/loadOrgansByParent.action',
 					dataType:'json',
 					autoParam:['id','name',"level=lv"],
 					dataFilter:fliterData
 				},
 				callback:{
 					onClick: zTreeOnClick,
 					onAsyncSuccess:function(event,treeId,treeNode,msg){
 					}
 				},
 				data: {
					simpleData: {
						enable: true,
						idKey: "id",
						pIdKey: "PId",
						rootPId: 0
					}
				}
 			};
 			$.fn.zTree.init($("#organTree"), setting, null);
		});
	</script> 
	</body>
</html>