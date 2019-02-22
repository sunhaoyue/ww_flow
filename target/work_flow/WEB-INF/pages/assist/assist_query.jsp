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
<title>代理查询界面</title>
<jsp:include page="/pages/common_min.jsp"></jsp:include>
<script type="text/javascript">
//渲染流程类型为中文renderOperation
function renderAssist(cellvalue, options, rowObject){
	switch(cellvalue) {
		case 1:
			return "是";
			break;
		case 0:
			return "否";
			break;
		default:
			break;
	}
}
		
//渲染单位
function renderDept(cellvalue, options, rowObject){
	var selectedPostName = rowObject.selectedPostName;
	return cellvalue + "(" + selectedPostName + ")";		
}
		
//渲染助理人
function renderAssistEmployee(cellvalue, options, rowObject){
	var selectedAssistEmployeeId = rowObject.selectedAssistEmployeeId;
	return cellvalue + "(" + selectedAssistEmployeeId + ")";		
}
		
//渲染操作列
function renderOperation(cellvalue, options, rowObject){
	var id = rowObject.id;
	return "<a class='button blue small' href='${ctx}/assist/assistAction!loadAssist.action?assistId="+id+"'>编辑</a>";			
}
		
//删除助理人
function deleteAssist(){
	var ids;
	ids = jQuery("#assistList").jqGrid('getGridParam','selarrrow');
	if(ids==""){
		alert("请先选择要删除的记录!");
		return;
	}
	if(confirm("您是否确认删除？")){ 
		$.ajax({
			type: "POST",
			url: '${ctx}/assist/deleteAssist.action',
			dataType:'json',
			data: "ids="+ids, 	    
			success: function (data,status) {
				alert("删除成功!");
				window.location.href="${ctx}/pages/assist/assist_query.jsp";	
			}
 		})	 
	}
}
</script>
</head>
<body>
<div class="toolbar-wrap">
	<div id="floatHead" class="toolbar">
		<div class="l-list">
			<ul class="icon-list">
				<li><a class="button blue medium add" href="${ctx }/assist/assistAction!addAssist.action"><i></i><span>添加</span></a></li>
				<li><a class="button blue medium del" href="javascript:void(0);" onclick="deleteAssist()"><i></i><span>删除</span></a></li>
			</ul>
		</div>
	</div>
	<table id="assistList"></table>
	<div id="assistPager"></div>
</div>

<script type="text/javascript">
var groupid = '${param.groupid}';
$(function(){
	var mainheight = $(window).height();
	var mainwidth = $(window).width();
	var w = mainwidth - 80;
	var h = mainheight - 180; 
	$("#assistList").jqGrid({
		caption: '助理人列表',//表格标题
		url:'${ctx}/assist/findAllAssists.action',//请求路径
	    postData:{},//额外请求参数,json格式
	    datatype: 'json',//后台返回数据类型
	    mtype:'post',//请求类型

		multiselect: true,
	    gridview: true,
	    gridstate:'hidden',
	    hoverrows:true,//是否显示鼠标悬浮数据记录行效果			    
	    sortable: true,//列是否可拖拽排列		        
	    rownumbers: true,//是否显示序号列
	    rownumWidth:40,//序号列宽度
	    
	  	//width:700,//宽度	
	  	autowidth:true,//根据父容器调整宽度,只在初始化阶段起作用
	  	shrinkToFit:false,
	  	height:h,//高度    
	  	
	  	pager: '#assistPager',//分页栏渲染目标
	  	//以下部分为分页部分
	  	page:1,//初始显示第几页，默认1
	  	pagerpos:'left',//分页栏位置,left、center、right,默认center
	  	rowNum:10,//页面大小
	    rowList:[10,20,30],//下拉选择页面大小
	  	pgbuttons:true,//是否显示分页按钮(上下页,首尾页)
	  	pginput:false,//是否显示跳转到第几页输入框
	  	pgtext:'{0}共{1}页',//默认,{0}表示当前第几页,{1}表示总页数
	  	
	  	//以下部分为分页总记录数部分
	  	viewrecords:true,//分页栏是否显示总记录数
	  	recordpos:'right',//总记录数位置
	  	emptyrecords:'无记录',//后台无数据显示文本,配合viewrecords使用
	  	recordtext:'{0}-{1} 共{2}条',//{0}当前页记录开始索引,{1}当前页记录结束索引,{2}总记录条数
	    
		colNames:['单位', '职位名称', '允许查看', '允许指派', '助理人', '助理人工号', '操作', '助理编号'],//表格列名称
		colModel :[ //列模型 
			{name:'selectedDeptName', index:'selectedDeptName',width:w*0.4, formatter:renderDept,sortable:false},
			{name:'selectedPostName', index:'selectedPostName',width:w*0.1,hidden:true},		      
			{name:'allowReceive', index:'allowReceive', width:w*0.1, align:'center', formatter:renderAssist,sortable:false}, 
			{name:'allowAssign', index:'allowAssign',width:w*0.1, align:'center', formatter:renderAssist,sortable:false},
			{name:'selectedAssistEmployeeName', index:'selectedAssistEmployeeName',width:w*0.2, formatter:renderAssistEmployee,sortable:false},
			{name:'selectedAssistEmployeeId',width:10, index:'selectedAssistEmployeeId',hidden:true},
			{name:'operation', index:'operation',width:w*0.1, align:'center', formatter:renderOperation,sortable:false},			      
			{name:'id', index:'id',width:10, hidden:true}
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
</body>
</html>
