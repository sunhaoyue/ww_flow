<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/pages/taglibs.inc" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=8">
<title></title>
<jsp:include page="/WEB-INF/pages/common_min.jsp"></jsp:include>
<script type="text/javascript" src="${ctx }/js/layout.js"></script>
</head>
<body>
<div class="rightpage01">
	<form action="" name="logInfoForm" id="logInfoForm" method="post">
	<table align="center" style="border: 0px solid;width: 100%;margin-bottom: 5px;">
		<tr>
			<td width="6%" colspan="3" align="right">
				日&nbsp;&nbsp;&nbsp;&nbsp;期：
			</td>
			<td width="24%" colspan="12">
				<input name="startTime" type="text" class="Wdate" id="startTime" 
						onclick="WdatePicker({el:'startTime',format:'yyyy-MM-dd'})" 
						style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px"/>
			</td>
			<script>
			document.getElementById("startTime").value = date;
			</script>
			<td width="6%" colspan="3" align="right">
				日志级别：
			</td>
			<td width="24%" colspan="12">
				<div class="rule-single-select">
				<select name="logLevel" id="logLevel">
					<option value="ALL" selected>全选</option>
					<option value="DEBUG">DEBUG</option>
					<option value="INFO">INFO</option>
					<option value="WARN">WARN</option>
					<option value="ERROR">ERROR</option>
				</select>
				</div>
			</td>
			<td width="40%" colspan="20">
				<input type="button" class="button blue medium" value="查询" onclick="toSearch();" />
				&nbsp;&nbsp;&nbsp;&nbsp;
				<input type="button" class="button blue medium" value="导出" onclick="toExport();" />
			</td>
		</tr>
	</table>
	</form>
	<table id="list1"></table>
	<div id="pager2"></div>
</div>
<script type="text/javascript">
var startTime = $("#startTime").attr("value");
var logLevel = $("#logLevel").attr("value");
var postData = {
	logLevel: logLevel,
	startTime: startTime
};
$(function(){
	var mainheight = $(window).height();
	var mainwidth = $(window).width();
	var w = mainwidth - 600;
	var h = mainheight - 160;
	$("#list1").jqGrid({
		caption: '系统日志',//表格标题
		url:'${ctx}/management/findLogInfoByPage.action',//请求路径
		postData:{},//额外请求参数,json格式
		datatype: 'json',//后台返回数据类型
		mtype:'post',//请求类型

		sortname: 'stamp',
		sortorder: 'desc',
	    
		gridview: true,
		gridstate:'hidden',
		hoverrows:true,//是否显示鼠标悬浮数据记录行效果			    
		sortable: true,//列是否可拖拽排列		        
		rownumbers: true,//是否显示序号列
		rownumWidth:50,//序号列宽度

		//width:700,//宽度	
		autowidth:true,//根据父容器调整宽度,只在初始化阶段起作用
		shrinkToFit:true,
		height:h,//高度    
		
		pager: '#pager2',//分页栏渲染目标
		//以下部分为分页部分
		page:1,//初始显示第几页，默认1
		pagerpos:'left',//分页栏位置,left、center、right,默认center
		rowNum:50,//页面大小
		rowList:[20,30,50],//下拉选择页面大小
		pgbuttons:true,//是否显示分页按钮(上下页,首尾页)
		pginput:true,//是否显示跳转到第几页输入框
		pgtext:'{0}共{1}页',//默认,{0}表示当前第几页,{1}表示总页数
		
		//以下部分为分页总记录数部分
		viewrecords:true,//分页栏是否显示总记录数
		recordpos:'right',//总记录数位置
		emptyrecords:'无记录',//后台无数据显示文本,配合viewrecords使用
		recordtext:'{0}-{1} 共{2}条',//{0}当前页记录开始索引,{1}当前页记录结束索引,{2}总记录条数
		
		colNames:['时间', '线程名', '类型', '类目录', '类名', '行', '信息'],//表格列名称
		colModel :[ //列模型 
			{name:'stamp', index:'stamp', width:100,sortable:false}, 
			{name:'thread', index:'thread',width:60, align:'center',sortable:false},
			{name:'infolevel', index:'infolevel',width:40, align:'center',sortable:false},			      
			{name:'classdir', index:'classdir',width:200,sortable:false},
			{name:'classfile', index:'classfile', width:100, align:'center',sortable:false, hidden:true}, 
			{name:'classline', index:'classline',width:40, align:'center',sortable:false},
			{name:'messages', index:'messages',width:w,sortable:false}
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
		},
		postData : postData
	}); 
});
function toSearch(){
	startTime = $("#startTime").attr("value");
	logLevel = $("#logLevel").attr("value");
	postData.startTime = startTime;
	postData.logLevel = logLevel;
	
	jQuery("#list1").jqGrid('setGridParam',{postData:postData}).trigger("reloadGrid");
}

function toExport(){
	document.getElementById("logInfoForm").action="${ctx }/export!exportLogInfo.action";				
	document.getElementById('logInfoForm').submit();
}
</script>
</body>
</html>
