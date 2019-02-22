<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/pages/taglibs.inc" %>
<html>
<head>
<title>grid</title>
<link href="${ctx }/css/csspage_01.css" rel="stylesheet" type="text/css" />
<link id="skin" rel="stylesheet" type="text/css" href="${ctx }/css/jquery-ui-1.8.21.custom.css" />
<link rel="stylesheet" type="text/css" href="${ctx }/css/ui.jqgrid.css" />
<script type="text/javascript" src='${ctx }/js/jquery-1.7.1.min.js'></script>
<script type="text/javascript" src="${ctx }/js/jqgrid/grid.locale-cn.js"></script>
<script type="text/javascript" src="${ctx }/js/jqgrid/jquery.jqGrid.min.js"></script>
<link href="${ctx }/css/2.0/ui.jqgrid.Ex.css" rel="stylesheet" type="text/css" />
<link href="${ctx }/css/2.0/style.css" rel="stylesheet" type="text/css" />
</head>
 
 <body style="padding: 20px;">
 	<div class="bgc01">
	<table id="list1"></table>
	<div id="pager2"></div> 
	</div>
 	<script type="text/javascript">
 		var groupid = '${param.groupid}';
 		var mainheight = $(window).height();
		var mainwidth = $(window).width();
		var w = mainwidth - 10;
		var h = mainheight - 180;
 		$(function(){ 
			  $("#list1").jqGrid({
			  
			    caption: '用户列表',//表格标题
			    url:'${ctx}/user/getUsersByGroup.action?id='+groupid,//请求路径
			    postData:{},//额外请求参数,json格式
			    datatype: 'json',//后台返回数据类型
			    mtype:'post',//请求类型
			    			   
			    gridstate:'hidden',
			    rownumbers: true,//是否显示序号列
			    
			  	width:390,//根据父容器调整宽度,只在初始化阶段起作用
			  	shrinkToFit:false,
			  	height:180,//高度
			  	
			  	pager: '#pager2',//分页栏渲染目标
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
			    
 				colNames:['姓名', '工号', '岗位名称', '部门路径', '部门ID', '职位代码', '所在单位', '所在中心', '所在部门', '用户ID'],//表格列名称
 				colModel :[ //列模型 
			      {name:'name', index:'name', width:100}, 
			      {name:'employeeId', index:'employeeId',width:100},
			      {name:'postName', index:'postName',width:120},
			      {name:'deptPath', index:'deptPath',hidden:true},
			      {name:'deptId', index:'deptId',hidden:true},		      
			      {name:'postCode', index:'postCode',hidden:true},
			      {name:'dept1', index:'dept1',hidden:true},			      			      			      
			      {name:'dept2', index:'dept2',hidden:true},			      			      			      
			      {name:'dept3', index:'dept3',hidden:true},
			      {name:'userID', index:'dept3',hidden:true}
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
				 			  
				 onSelectRow: function(id){	
				 	var rowdata = $("#list1").jqGrid("getRowData",id);
				 	parent.personDetail = rowdata;
	      		 }
			  }); 
		}); 
 	</script>
 </body>
 </html>