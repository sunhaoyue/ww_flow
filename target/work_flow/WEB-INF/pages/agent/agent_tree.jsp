<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@page isELIgnored="false"%>
<%@ include file="/pages/taglibs.inc" %>
<html>
<head>
<title>代理表单树</title>
<link href="${ctx }/css/csspage_01.css" rel="stylesheet" type="text/css" />
<link href="${ctx }/css/csspage_03.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" href="${ctx }/css/jquery-ui-1.8.21.custom.css" />
<%-- jquery base  --%>
<script type="text/javascript" src='${ctx }/js/jquery-1.7.1.min.js'></script>
<script type="text/javascript" src='${ctx }/js/jquery.form.js'></script>

<%-- tree  --%>
<script type="text/javascript" src="${ctx }/js/ztree/jquery.ztree.all-3.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="${ctx }/css/zTreeStyle/zTreeStyle.css" />
	
<%-- custom--%>
<script type="text/javascript" src='${ctx }/js/custom.js'></script>	
<style type="text/css">
ul.ztree {margin-top: 10px;border: 1px solid #617775;background: #f0f6e4;width:180px;height:200px;overflow-y:scroll;overflow-x:auto;}
</style>
</head>

	<body style="padding: 20px;">
		<table width="100%" cellpadding="0" cellspacing="0">
			<tr>
				<td style="padding-top: 30px; width: 40%;">
					<span class="targetspanstyle">可选表单</span>
					<div class="divborderstyle">
						<ul id="agentTree" class="ztree"></ul>
					</div>
					<div id="lifeEventToolTips" style="">
					</div>
				</td>
				<td style="padding-top: 60px; width: 30%;">
		            <div style="width:60px;float:center; padding-top:20px;padding-left:20px;">
					<input type="button" id="add" value="&nbsp;&nbsp;>&nbsp;&nbsp;" width="60" onclick="addNodes()"/><br />
		           	<input type="button" id="remove" width="60" value="&nbsp;&nbsp;<&nbsp;&nbsp;" onclick="removeNodes()"/>
		            </div>
				</td>
				<td style="padding-top: 30px; width: 40%;">
					<span class="targetspanstyle">已选表单</span>
					<div class="divborderstyle">
						<ul id="agentTree2" class="ztree"></ul>
					</div>
				</td>
			</tr>
		</table>
		<script type="text/javascript">		
		var nodes = new Array();
		
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
			function zTreeOnCheck(event, treeId, treeNode) {
				if(treeNode.checked==true){
					//父节点是不让选的
					if(treeNode.isParent==false){
						nodes.push(treeNode);	
					}
				}else{
					//父节点是不让选的
					if(treeNode.isParent==false){
						for(var i =0; i < nodes.length; i++) {
							if(nodes[i].id == treeNode.id) {
								var dx = nodes.indexOf(nodes[i]);
								if(dx>=0){
									nodes.remove(dx);
								}
								//nodes = nodes.slice(i + 1);
							}// nodes.push(treeNode);
						}							
					}
				}
			};

			//added by hzp 8.31
			//异步加载树后检查是否已被选择
	 		function checkSelectedItems() {
	 			var sourceTreeObj = $.fn.zTree.getZTreeObj("agentTree");
				if (flowIdArray) {
					for(var i = 0; i < flowIdArray.length; i++) {
						var sourceNode = sourceTreeObj.getNodeByParam("id", flowIdArray[i], null);
						if (sourceNode) {
							sourceTreeObj.checkNode(sourceNode, true, true);
							sourceTreeObj.setChkDisabled(sourceNode, true);
						}
					}
				}

				//根据name检查已保存的项
				if (savedFlowNames) {
					for (var i = 0; i < savedFlowNames.length; i++) {
						var sourceNode = sourceTreeObj.getNodeByParam("name", savedFlowNames[i], null);
						if (sourceNode) {
							sourceTreeObj.checkNode(sourceNode, true, true);
							sourceTreeObj.setChkDisabled(sourceNode, true);
						}
					}
				}
	 		}
			
 			var setting={
 				async:{
 					enable:true,
 					url:'${ctx }/agent/getExtraFlows.action',
 					dataType:'json',
 					autoParam:['id','name',"level=lv","checked"]				
 				},
 				check: {
					enable: true,	
					autoCheckTrigger: true
				},
 				callback:{
 					onCheck: zTreeOnCheck,
 					onExpand: checkSelectedItems
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
 			var setting2={
 				 	check: {
						enable: true,	
						autoCheckTrigger: true
					},
	 				data:{
	 					key:{
		 					name:'name'
		 				},	
	 					simpleData:{
		 					enable:true,//default false
		 					idKey:'id',//default id
							pIdKey: "PId",
							rootPId: 0
		 				}
	 				}
	 		};
 			$.fn.zTree.init($("#agentTree"), setting, null);
 			$.fn.zTree.init($("#agentTree2"), setting2, null);


 			//added by hzp 8.31
 			//初始化已选择的数据
			var flow = parent.getFlow();

			if (flow && flow.savedFlowNames) {
				savedFlowNames = flow.savedFlowNames.split(",");
			}
			
			if (flow && flow.flowIds && flow.flowNames) {
				var splitedIds = flow.flowIds.split(",");
				var splitedNames = flow.flowNames.split(",");
		
				var targetTreeObj = $.fn.zTree.getZTreeObj("agentTree2");
				var currentNodes = new Array();
				var splitedIds = flow.flowIds.split(",");
				var splitedNames = flow.flowNames.split(",");
				
				for(var i = 0; i < splitedIds.length; i++) {
					currentNodes.push({id: splitedIds[i], name: splitedNames[i]});
					flowIdArray.push(splitedIds[i]);
					flowNamesArray.push(splitedNames[i]);
				}
				targetTreeObj.addNodes(null, currentNodes);
			}
			
			
 			
		});
		
		//数据初始化
		var flowIds;
		var flowNames
		var flowIdArray = new Array();
		var flowNamesArray = new Array();
		var savedFlowNames;
		
		//添加节点
 		function addNodes(){
				var sourceTreeObj = $.fn.zTree.getZTreeObj("agentTree");			
			 	var targetTreeObj = $.fn.zTree.getZTreeObj("agentTree2");
	 			
			 	//缓存添加成功的节点
	 			for(var i =0; i < nodes.length; i++) {
	 				var dx=-1;	
	 				for(var j =0; j < flowIdArray.length; j++){
	 					dx = flowIdArray.indexOf(nodes[i].id);
	 					// 应该是若不存在 那么跳出循环， 若存在那么继续
	 					if(dx>=0){
	 						continue;
	 					}else{
							break;
		 				}
	 				}
	 				// alert('dx.....' + dx);
	 				if(dx<0){
		 				// alert(nodes[i].id + '---' + nodes[i].name);
	 					targetTreeObj.addNodes(null, nodes[i]);
	 					flowIdArray.push(nodes[i].id);
	 					flowNamesArray.push(nodes[i].name);
	 					dx=-1;
	 					//added by hzp 8.31
	 					//set disable
	 					sourceTreeObj.setChkDisabled(nodes[i], true);
						//uncheck the to-right column items
	 					var targetNode = targetTreeObj.getNodeByParam("id", nodes[i].id, null);
	 					targetTreeObj.checkNode(targetNode, false, true);
	 				}	
	 			}

	 			
	 			//将节点添加到隐藏域
	 			/*
	 			flowIds="";
	 			flowNames="";
	 			for(var k =0; k < flowIdArray.length; k++) {
	 				if(flowIds){
						flowIds = flowIds+","+flowIdArray[k];
						flowNames = flowNames+","+flowNamesArray[k];			
					}
					else{
						flowIds=flowIdArray[k];
						flowNames = flowNamesArray[k];
					}
	 			}
	 			*/
 		}
		
		//删除节点
 		function removeNodes(){
			var sourceTreeObj = $.fn.zTree.getZTreeObj("agentTree");			
	 		var targetTreeObj = $.fn.zTree.getZTreeObj("agentTree2");
	 		var selectedNodes = targetTreeObj.getCheckedNodes(true);
	 		for(var i =0; i < selectedNodes.length; i++) {
	 			var node = selectedNodes[i];
	 			var dx=-1;
	 			targetTreeObj.removeNode(node); 
	 			for(var j =0; j < flowIdArray.length; j++) {
	 				dx = flowIdArray.indexOf(node.id);
	 				if(dx>=0){
	 					flowIdArray.remove(dx);
	 					flowNamesArray.remove(dx);
	 				}
	 			}
	 			
	 			//added by hzp 8.31
	 			var sourceNode = sourceTreeObj.getNodeByParam("id", node.id, null);
	 			if (sourceNode) {
		 			sourceTreeObj.setChkDisabled(sourceNode, false);
		 			sourceTreeObj.checkNode(sourceNode, false, true);

		 			//remove from cwj's cache
		 			var dx = nodes.indexOf(sourceNode);
		 			if (dx >= 0) nodes.remove(dx);
	 			}
	 			
	 		}
	 		
	 		//将节点添加到隐藏域(重新做一遍)
	 		/*
	 		flowIds="";
	 		flowNames="";
	 		for(var k =0; k < flowIdArray.length; k++) {
	 			if(flowIds){
					flowIds = flowIds+","+flowIdArray[k];
					flowNames = flowNames+","+flowNamesArray[k];			
				}
				else{
					flowIds=flowIdArray[k];;
					flowNames = flowNamesArray[k];
				}
	 		}
	 		*/
 		}
 		
 		function confirm(){
 	 		
 			//将节点添加到隐藏域
	 		flowIds="";
	 		flowNames="";
	 		for(var k =0; k < flowIdArray.length; k++) {
	 			if(flowIds){
					flowIds = flowIds+","+flowIdArray[k];
					flowNames = flowNames+","+flowNamesArray[k];			
				}
				else{
					flowIds=flowIdArray[k];;
					flowNames = flowNamesArray[k];
				}
	 		}
 			var flowCount = 0;
 			if(flowNamesArray){
 				flowCount = flowNamesArray.length;
 			}
 			parent.setFlow(flowIds, flowNames, flowCount);
 		}

 		

	</script>
	</body>
</html>