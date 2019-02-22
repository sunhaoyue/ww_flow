<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@page isELIgnored="false"%>
<%@ include file="/WEB-INF/pages/taglibs.inc" %>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=8">
<title>组织树</title>
<link id="skin" rel="stylesheet" type="text/css" href="${ctx }/css/jquery-ui-1.8.21.custom.css" />
<script type="text/javascript" src='${ctx }/js/jquery-1.7.1.min.js'></script>
<script type="text/javascript" src='${ctx }/js/jquery-ui-1.8.18.custom.min.js'></script>
<script type="text/javascript" src="${ctx }/js/ztree/jquery.ztree.core-3.2.js"></script>
<link rel="stylesheet" type="text/css" href="${ctx }/css/zTreeStyle/zTreeStyle.css" />
</head>
<body style="padding: 20px;">
<table width="100%" height="340" cellpadding="0" cellspacing="0">
	<tr>
		<td width="210" height="340">
			<div id="accordion" style="width: 200px; height:340px">
				<h3><a href="javascript:void(0);" style="outline-style: none;">组织机构</a></h3>
				<div style="padding:0px;margin: 0px;">
					<ul id="organTree" class="ztree"></ul>
				</div>					
			</div>
		
		</td>
		<td height="340">
			<iframe id="content" width="100%" height="100%" frameborder="0" style="border:1px solid #9DD1F4;" >
			</iframe>
		</td>
	</tr>
</table>
<script type="text/javascript">
	var employeeId;
	var personDetail;
	var type = '${param.query}';
	var adminAssign = '${param.adminAssign}';
	var bossAssign = '${param.bossAssign}';
	var divisionAssign = '${param.divisionAssign}';
	var localAssign = '${param.localAssign}';
	
	var treeUrl = '${ctx }/organ/loadOrgansByParent.action';
	if(bossAssign){
		treeUrl = '${ctx }/organ/loadBossOrgans.action';
	}
	if (divisionAssign){
		treeUrl = '${ctx }/organ/loadDivisionOrgans.action';
	}
	if (localAssign){
		treeUrl = '${ctx }/organ/loadLocalOrgans.action';
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
	
	var deptId = W.$.dialog.data('deptId');
	var assignDeptId = W.$.dialog.data('assignDeptId');
	var curWorkId = W.$.dialog.data('curWorkId');
	
	$( "#accordion" ).accordion({fillSpace:true});
	
	function ajaxDataFilter(treeId, parentNode, childNodes) {
		var nodes = new Array();
	    if (childNodes) {
	      for(var i =0; i < childNodes.length; i++) {
	      	if(!childNodes[i].hidden){
	      		nodes.push(childNodes[i]);
	      	}
	      }
	    }
	    return nodes;
	};
		
	$(function(){
		function zTreeOnClick(event, treeId, treeNode) {
			var ifm=document.getElementById("content");
			ifm.src='${ctx }/pages/user.jsp?groupid='+treeNode.id;

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
					url:treeUrl,
					dataType:'json',
					autoParam:['id','name',"level=lv"],
					otherParam: {"type":type, "deptId":deptId, "assignDeptId":assignDeptId,"curWorkId":curWorkId},
				dataFilter: ajaxDataFilter 					
				},
				callback:{
					onClick: zTreeOnClick,
					beforeClick : zTreeBeforeClick,
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
	
	function confirm(){
		//内部会办选择
		if(type=='innerhuiban'){
			parent.selectInnerhuiban(personDetail);
		}
		else if(type=='assign'){
			if(adminAssign){
				parent.adminAssignWork(personDetail);
			}else{
				return parent.assignWork(personDetail);	
			}
			
		}
		else if(type=='agent'){
			if (parent.setAssist)
				parent.setAssist(personDetail);
			if (parent.setAgent)
				parent.setAgent(personDetail);
		}
		else if(type=='assist'){
			parent.setAssist(personDetail);
		}
		else{
			  //申请人选择
		  parent.setFawenDept(personDetail);
		}
	}
</script>
</body>
</html>