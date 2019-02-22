<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ include file="/WEB-INF/pages/taglibs.inc" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
<jsp:include page="/WEB-INF/pages/common.jsp"></jsp:include>
</head>
<s:if test="#request.mailExceptionMessage != null && #request.mailExceptionMessage != '' && #request.mailExceptionMessage.length() > 0">
	<script type="text/javascript">alert("${requestScope.mailExceptionMessage}");</script>	
</s:if>
<body>

<div class="rightpage01">
	<div style="margin-bottom: 3px;">
		<input type="button" id="delTemp" class="button blue medium" value="删除" />
	</div>
	<table id="list1"></table>
	<div id="pager2"></div>
</div>
<script type="text/javascript">
$(document).ready(function() {
	$("#delTemp").click(function() {
		var ids;
		ids = jQuery("#list1").jqGrid('getGridParam','selarrrow');
		if(ids==""){
			alert("请先选择要删除的记录!");
			return;
		}

		var params = new Array();
		var curRowData
		for (var i = 0; i < ids.length; i++) {
			curRowData = jQuery('#list1').getRowData(ids[i]);
			params.push(curRowData.formnum);
		}

		if(confirm("您是否确认删除？")){ 
			$.ajax({
				type: "POST",
				url: '${ctx }/management/delTempForm.action',
				dataType:'json',
				data: "params="+params.join(), 	    
				success: function (data,status) {
					alert("删除成功!");
					window.location.href="${ctx }/pages/flowmanagement/flowtemplates.jsp";	
				}
	 		})	 
		}

						
	});

});

 	
//渲染文件编号为链接
function makeURL(cellvalue, options, rowObject){
	switch(rowObject.flowTypeName) {
		case 'QIANCHENG':
			if (rowObject.isNew == "1"){
				return "<a class='gridlink' href='${ctx }/nqianchen/qianChenEditAction!loadTemplateQianChenDetail.action?formNum="+cellvalue+"&workId="+rowObject.workId+"&deptId="+rowObject.deptId+"'>"+cellvalue+"</a>";
			} else {
				return "<a class='gridlink' href='${ctx }/qianchen/qianChenEditAction!loadTemplateQianChenDetail.action?formNum="+cellvalue+"&workId="+rowObject.workId+"&deptId="+rowObject.deptId+"'>"+cellvalue+"</a>";
			}
			break;
		case 'NEILIAN':
			if (rowObject.isNew == "1"){
				return "<a class='gridlink' href='${ctx }/nneilian/neiLianEditAction!loadTemplateNeiLianDetail.action?formNum="+cellvalue+"&workId="+rowObject.workId+"&deptId="+rowObject.deptId+"'>"+cellvalue+"</a>";
			} else {
				return "<a class='gridlink' href='${ctx }/neilian/neiLianEditAction!loadTemplateNeiLianDetail.action?formNum="+cellvalue+"&workId="+rowObject.workId+"&deptId="+rowObject.deptId+"'>"+cellvalue+"</a>";
			}			
			break;
		default:
			break;
	}
}

//渲染流程类型为中文
function renderFlowType(cellvalue, options, rowObject){
	switch(cellvalue) {
		case 'QIANCHENG':
			return "签呈" ;
			break;
		case 'NEILIAN':
			return "内联";
			break;
		default:
			break;
	}
}
 	
var groupid = '${param.groupid}';
//alert(groupid);
$(function(){
	var mainheight = $(window).height();
	var mainwidth = $(window).width();
	var w = mainwidth - 70;
	var h = mainheight - 180;
	$("#list1").jqGrid({
		caption: '暂存表单列表',//表格标题
		url:'${ctx }/management/findFlowTemplates.action',//请求路径
		postData:{},//额外请求参数,json格式
		datatype: 'json',//后台返回数据类型
		mtype:'post',//请求类型
		
		sortname: 'createTime',
		sortorder: 'desc',
		multiselect: true,
		
		gridview: true,
		gridstate:'hidden',
		hoverrows:true,//是否显示鼠标悬浮数据记录行效果			    
		sortable: true,//列是否可拖拽排列		        
		rownumbers: true,//是否显示序号列
		rownumWidth:50,//序号列宽度
		
		//width:700,//宽度	
		autowidth:true,//根据父容器调整宽度,只在初始化阶段起作用
		shrinkToFit:false,
		height:h,//高度    
		
		pager: '#pager2',//分页栏渲染目标
		//以下部分为分页部分
		page:1,//初始显示第几页，默认1
		pagerpos:'left',//分页栏位置,left、center、right,默认center
		rowNum:20,//页面大小
		rowList:[10,20,30],//下拉选择页面大小
		pgbuttons:true,//是否显示分页按钮(上下页,首尾页)
		pginput:false,//是否显示跳转到第几页输入框
		pgtext:'{0}共{1}页',//默认,{0}表示当前第几页,{1}表示总页数
		
		//以下部分为分页总记录数部分
		viewrecords:true,//分页栏是否显示总记录数
		recordpos:'right',//总记录数位置
		emptyrecords:'无记录',//后台无数据显示文本,配合viewrecords使用
		recordtext:'{0}-{1} 共{2}条',//{0}当前页记录开始索引,{1}当前页记录结束索引,{2}总记录条数

		colNames:['文件编号', '类别', '主旨', '申请人', '提交人', '申请时间', '表单编号'],//表格列名称
		colModel :[ //列模型 
			{name:'formnum', index:'formnum', width:w*0.25, formatter:makeURL}, 
			{name:'flowTypeName', index:'flowTypeName',width:w*0.06, align:'center',formatter:renderFlowType},
			{name:'title', index:'title',width:w*0.3},			      
			{name:'creatorName', index:'creatorName',width:w*0.1, align:'center'},
			{name:'actualName', index:'actualName', width:w*0.1, align:'center'}, 
			{name:'createTime', index:'createTime',width:w*0.1, align:'center'},
			{name:'formnum', index:'formnumorg', hidden:true},
		],

		jsonReader:{//数据解析器
			root:"result",//结果数据
			page:'currentPageNo',//当前页
			records:'totalSize',//共多少数据
			total:'totalPageCount',//共多少页
			repeatitems: false,
			userData:'userdata'//统计数据字段,默认
		},

		//以下配置传递后台参数名称
		prmNames : {  
			page:"pageNo",    // 表示请求页码的参数名称  
			rows:"pageSize",    // 表示请求页面大小的参数名称  
			sort: "sidx", // 表示用于排序的列名的参数名称  
			order: "sord", // 表示采用的排序方式的参数名称  
			search:"_search", // 表示是否是搜索请求的参数名称  
			nd:"nd", // 表示已经发送请求的次数的参数名称  
			id:"id", // 表示当在编辑数据模块中发送数据时，使用的id的名称  
			oper:"oper",    // operation参数名称（我暂时还没用到）  
			editoper:"edit", // 当在edit模式中提交数据时，操作的名称  
			addoper:"add", // 当在add模式中提交数据时，操作的名称  
			deloper:"del", // 当在delete模式中提交数据时，操作的名称  
			subgridid:"id", // 当点击以载入数据到子表时，传递的数据名称  
			npage: null,   
			totalrows:"totalrows" // 表示需从Server得到总共多少行数据的参数名称，参见jqGrid选项中的rowTotal  
		}
	});
}); 
</script>
<input name="formNum" type="hidden" id="formNum" value="<s:property value="flow.formNum"/>"/>      
<input name="FlowStatus" type="hidden" id="FlowStatus" value="<s:property value="#request.FlowStatus"/>"/>
<input name="innerhuibanIds" type="hidden" id="innerhuibanIds" value="<s:property value="#request.innerhuibanIds"/>"/>
<input name="huibanpersons" type="hidden" id="huibanpersons" value="<s:property value="#request.huibanpersons"/>"/>
<input name="nextPerson" type="hidden" id="nextPerson" value="<s:property value="#request.nextPerson"/>"/>
<input name="joinType" type="hidden" id="joinType" value="<s:property value="#request.joinType"/>"/>
<input name="flowId" type="hidden" id="flowId" value="<s:property value="flow.id"/>"/>
<script type="text/javascript">
//用于刷新左侧的导航菜单
window.parent.leftFrame.location.href = "${ctx }/management/findMyWorksCount.action?menu=flowtemplates";
</script>
</body>
</html>
