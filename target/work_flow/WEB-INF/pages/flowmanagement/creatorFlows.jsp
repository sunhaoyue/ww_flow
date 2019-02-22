<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/pages/taglibs.inc" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
<jsp:include page="/WEB-INF/pages/common.jsp"></jsp:include>
</head>
<body>
<div class="rightpage01">
	<table align="center" style="border: 0px solid; width: 100%; margin-bottom: 5px;">
		<tr>
			<td width="6%" colspan="3" align="right">主&nbsp;&nbsp;&nbsp;&nbsp;旨：</td>
			<td width="54%" colspan="18">
				<input type="text" class="appInput" size="80" value="" style="height: 20px;" id="searchSub" name="searchSub" />
			</td>
			<td width="40%" colspan="20">
				<input type="button" class="button blue medium" value="查询" onclick="toSearch();" />&nbsp;&nbsp;
				<input type="button" class="button blue medium" value="重置" onclick="toReset();" />
			</td>
		</tr>
	</table>
	<table id="list1"></table>
	<div id="pager2"></div>
</div>
<script type="text/javascript">
//渲染文件编号为链接
function makeURL(cellvalue, options, rowObject){
	switch(rowObject.flowTypeName) {
		case 'QIANCHENG':
			if (rowObject.isNew == "1"){
				return "<a class='gridlink' href='${ctx }/nqianchen/qianChenEditAction!loadQianChenDetail.action?formNum="+cellvalue+"'>"+cellvalue+"</a>";
			} else {
				return "<a class='gridlink' href='${ctx }/qianchen/qianChenEditAction!loadQianChenDetail.action?formNum="+cellvalue+"'>"+cellvalue+"</a>";
			}
		break;
		case 'NEILIAN':
			if (rowObject.isNew == "1"){
				return "<a class='gridlink' href='${ctx }/nneilian/neiLianEditAction!loadNeiLianDetail.action?formNum="+cellvalue+"'>"+cellvalue+"</a>";
			} else {
				return "<a class='gridlink' href='${ctx }/neilian/neiLianEditAction!loadNeiLianDetail.action?formNum="+cellvalue+"'>"+cellvalue+"</a>";
			}
			break;
		default:
			break;
	}
}

//渲染流程类型为中文
function renderFlowType(cellvalue, options, rowObject) {
	switch (cellvalue) {　　
		case 'QIANCHENG':
			return "签呈";
			break;
		case 'NEILIAN':
			return "内联";
			break;
		default:
			break;
	}
}
 	
var groupid = '${param.groupid}';
var searchSub = $('#searchSub').attr('value');
var postData = {searchSub: searchSub};
$(function(){
	var mainheight = $(window).height();
	var mainwidth = $(window).width();
	var w = mainwidth - 60;
	var h = mainheight - 160;
	$("#list1").jqGrid({
		caption: '已审核表单列表',//表格标题
		url:'${ctx}/management/findCreatorFlow.action',//请求路径
		postData:{},//额外请求参数,json格式
		datatype: 'json',//后台返回数据类型
		mtype:'post',//请求类型

		sortname: 'createTime',
		sortorder: 'desc',
	    
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
		
		colNames:['文件编号', '类别', '主旨', '申请人', '提交人', '申请日期', "审核日期", '状态', '结案日期'],//表格列名称
		colModel :[ //列模型 
			{name:'formnum', index:'formnum', width:w*0.24, formatter:makeURL,sortable:false}, 
			{name:'flowTypeName', index:'flowTypeName',width:w*0.06, align:'center',formatter:renderFlowType,sortable:false},
			{name:'title', index:'title',width:w*0.2,sortable:false},			      
			{name:'creatorName', index:'creatorName',width:w*0.1, align:'center',sortable:false},
			{name:'actualName', index:'actualName', width:w*0.1, align:'center', hidden:true,sortable:false}, 
			{name:'createTime', index:'createTime',width:w*0.08, align:'center'},
			{name:'finishTime', index:'finishTime',width:w*0.08, align:'center'},
			{name:'flowDisplayStatusName', index:'flowDisplayStatusName',width:w*0.06, align:'center',sortable:false},
			{name:'endTime', index:'endTime',width:w*0.08, align:'center',sortable:false}
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
function toSearch(){
	searchSub = $("#searchSub").attr("value");
	postData.searchSub = searchSub;
	jQuery("#list1").jqGrid('setGridParam',{postData:postData}).trigger("reloadGrid");
}
function toReset(){
	$("#searchSub").attr("value", "");
	searchSub = $("#searchSub").attr("value");
	postData.searchSub = searchSub;
	jQuery("#list1").jqGrid('setGridParam',{postData:postData}).trigger("reloadGrid");
}
</script>
</body>
</html>
