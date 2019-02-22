<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page isELIgnored="false"%>

<html>

	<head>
		<title>组织树</title>
		<script type="text/javascript" src='<%=request.getContextPath()%>/js/jquery-1.7.1.min.js'></script>
		<script type="text/javascript" src='<%=request.getContextPath()%>/js/jquery-ui-1.8.18.custom.min.js'></script>
		<script type="text/javascript" src="<%=request.getContextPath()%>/js/ztree/jquery.ztree.all-3.2.min.js"></script>
		<script type="text/javascript" src='<%=request.getContextPath()%>/js/custom.js'></script>	
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/zTreeStyle/zTreeStyle.css" />
	</head>

	<body style="padding: 20px;">
	<ul id="organTree2" class="ztree"></ul> 
	<script type="text/javascript">	
		var type = '${param.query}';
		
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
		
		var deptId = W.$.dialog.data('deptId');
		var contentType = W.$.dialog.data('contentType');
		var leader = W.$.dialog.data('leader');
		var huibanDeptIds = W.$.dialog.data('huibanDeptIds');
		var chaosongDeptIds = W.$.dialog.data('chaosongDeptIds');
		var flowType = W.$.dialog.data('flowType');
		var deptIds = "";
		
		if(type=='huiban'){
			deptIds = huibanDeptIds;
		}
		if(type=='chaosong'){
			deptIds = chaosongDeptIds;
		}
		
		//节点选择后的id和name
		var sids = new Array();
		var snames = new Array();
		
		function ajaxDataFilter(treeId, parentNode, childNodes) {
			var nodes = new Array();
		    if (childNodes) {
		      for(var i =0; i < childNodes.length; i++) {
		      	if(!childNodes[i].hidden){
		      		nodes.push(childNodes[i]);
		      		if(childNodes[i].checked==true){
		      			sids.push(childNodes[i].id);
		      			snames.push(childNodes[i].orgPath);
		      		}
		      	}
		      }
		    }
		    return nodes;
		};
		
		//确认组织选择
		function confirm(){
			if(type=='huiban'){
				parent.setHuibanDept(sids,snames);
			}
			if(type=='chaosong'){
				parent.setChaosongDept(sids,snames);
			}
		}
		
		$(function(){
			function zTreeOnCheck(event, treeId, treeNode) {
				if(treeNode.checked==true){
					if(!treeNode.clicked){
					  alert('该节点不可选');
					  treeNode.checked=false;
					} else {
					  	sids.push(treeNode.id);
		      			snames.push(treeNode.orgPath);
					}
				}
				if(treeNode.checked==false){
					dx1 = sids.indexOf(treeNode.id);
					dx2 = snames.indexOf(treeNode.orgPath);
					sids.remove(dx1);
		      		snames.remove(dx2);
				}
			};
					
			function zTreeBeforeClick(treeId, treeNode, clickFlag) {
				if(treeNode.clicked){
					return true;
				}
				else{
					alert('当前节点不可用!');
					return false;
				}
			};	
			
			function setFontCss(treeId, treeNode) {
				return treeNode.clicked == false ? {color:"#ACA899"} : {};
			};
 			var setting={
 			 	view: {
					fontCss : setFontCss
				},
 				async:{
 					enable:true,
 					url:'<%=request.getContextPath()%>/organ/loadOrgansByParent.action',
 					dataType:'json',
 					autoParam:['id','name',"level=lv","checked"],
 					otherParam: {"type":type, "deptId":deptId, "contentType":contentType, "leader":leader, "deptIds":deptIds, "flowType":flowType}, 					
					dataFilter: ajaxDataFilter 					
 				},
 				check: {
					enable: true,	
					autoCheckTrigger: false,		
					//是否向上、向下继承
					chkboxType: { "Y": "", "N": "" }
				},
 				callback:{
 					onCheck: zTreeOnCheck
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
 			$.fn.zTree.init($("#organTree2"), setting, null);
		});
	</script> 
	</body>
</html>