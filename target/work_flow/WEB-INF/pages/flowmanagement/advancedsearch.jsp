<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
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
<div class="rightpage01" style="border: 1px solid #AAC2F0;">
<div class="headbg01">高级查询</div>
<form action="" name="advancedForm" id="advancedForm" method="post">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>
			<table class="pTable" width="96%" border="0" cellspacing="4" cellpadding="0">
				<tr>
					<td width="12%" colspan="6" align="right">表单名称：</td>
					<td width="36%" colspan="18">
						<div class="rule-single-select">
						<select name="flowType" id="flowType">
							<option value="QIANCHENG" selected>签呈申请单</option>
							<option value="NEILIAN">内联申请单</option>
						</select>
						</div>
					</td>
					<td width="12%" colspan="6" align="right">发文类别：</td>
					<td width="36%" colspan="18">
						<div class="rule-single-select">
						<select name="localType" id="localType">
							<option value="">全部</option>
							<option value="1">体系内部</option>
							<option value="0">地方至总部</option>
						</select>
					</div>
					</td>
				</tr>
				<tr>
					<td width="12%" colspan="6" align="right">查询类别：</td>
					<td width="36%" colspan="18">
						<div class="rule-single-select">
						<select name="branch" id="branch" onchange="selectBranch(this)">
							<s:if test="#session.employeeId == 'Administrator' || #session.employeeId == 'admin'">
							<option value="All" selected>--全部--</option></s:if>
							<s:else><option value="MySubmit" selected>我申请的</option></s:else>
							<option value="SubDeptSubmit">下辖部门提交的</option>
							<option value="MyApproved">我签核过的</option>
							<option value="FormNum">按编号</option>
							<option value="Title">按主旨</option>
							<option value="MyAccepted">我接收的</option>
							<option value="MyDlg">我代签核的</option>
						</select>
						</div>
					</td>
					<td width="12%" colspan="6" align="right">发文人：</td>
					<td width="36%" colspan="18">
						<input type="text" name="creator" id="creator" class="width01 appInput" style="width:90%" />
					</td>
				</tr>
				<tr>
					<td width="12%" colspan="6" align="right">提交日期：</td>
					<td width="84%" colspan="42">            
						<input name="startTime" type="text" class="Wdate" id="startTime" 
						onclick="WdatePicker({el:'startTime',format:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'endTime\')}'})" 
						style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px"/>
						&nbsp;&nbsp;至&nbsp;&nbsp;
						<input name="endTime" type="text" class="Wdate" id="endTime" 
						onclick="WdatePicker({el:'endTime',format:'yyyy-MM-dd',errDealMode:2,minDate:'#F{$dp.$D(\'startTime\')}'})" 
						style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px"/>
					</td>
				</tr>
				<script>
				//alert(preMonthDate);
				document.getElementById("startTime").value = preMonthDate; 
				document.getElementById("endTime").value = date;
				</script>
				<tr>
					<td width="12%" colspan="6" align="right">结案日期：</td>
					<td width="84%" colspan="42">            
						<input name="closeStartTime" type="text" class="Wdate" id="closeStartTime" 
						onclick="WdatePicker({el:'closeStartTime',format:'yyyy-MM-dd',maxDate:'#F{$dp.$D(\'closeEndTime\')}'})" 
						style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px"/>
						&nbsp;&nbsp;至&nbsp;&nbsp;
						<input name="closeEndTime" type="text" class="Wdate" id="closeEndTime" 
						onclick="WdatePicker({el:'closeEndTime',format:'yyyy-MM-dd',errDealMode:2,minDate:'#F{$dp.$D(\'closeStartTime\')}'})" 
						style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px"/>
					</td>
				</tr>
				<script>
				//document.getElementById("closeStartTime").value = preMonthDate; 
				//document.getElementById("closeEndTime").value = date;
				</script>
				<tr>
					<td width="12%" colspan="6" align="right">发文单位：</td>
					<td width="36%" colspan="18">
						<div class="rule-single-select">
						<select name="creatCmp" id="creatCmp">
							<option value="">全部</option>
							<s:iterator value="#request.cmpList" id="item">
							<option value="<s:property value="#item.cmpCode" />"><s:property value="#item.cmpName" /></option>
							</s:iterator>
						</select>
						</div>
					</td>
					<td width="12%" colspan="6" align="right">表单状态：</td>
					<td width="36%" colspan="18">
						<div class="rule-single-select">
						<select name="formStatus" id="formStatus">
							<option value="ALL" selected>全选</option>
							<option value="DOING">签核中</option>
							<option value="APPROVED">已核准</option>
							<option value="REJECTED">已驳回</option>
							<option value="CANCELED">已取消</option>
							<option value="RENEWED">重新起案</option>
						</select>
						</div>
					</td>
				<tr>
				<tr id="Title" style="display:none">
					<td width="12%" colspan="6" align="right">表单主旨：</td>
					<td width="84%" colspan="42"><input type="text" name="title" id="title" class="width01 appInput" style="width:98%"/></td>
				</tr>
				<tr id="FormNum" style="display:none">
					<td width="12%" colspan="6" align="right">表单编号：</td>
					<td width="84%" colspan="42"><input type="text" name="formNum" id="formNum" class="width01 appInput" style="width:98%"/></td>
				</tr>
      </table></td>
      <td style="padding-right: 10px; width: 80px;">
      	<a href="javascript:void(0);" id="confirm" class="button blue medium">查询</a><br/><br/>
      	<a href="javascript:void(0);" id="export" class="button blue medium">导出</a>
      </td>
    </tr>
  </table>
  </form>
  <table id="list1"></table>
  <div id="pager2"></div>
</div>
<script type="text/javascript">
//渲染文件编号为链接
function makeURL(cellvalue, options, rowObject){
	switch(rowObject.flowTypeName) {
		case 'QIANCHENG':
			if (rowObject.isNew == "1"){
				return "<a class='gridlink' href='${ctx}/nqianchen/qianChenEditAction!loadQianChenDetail.action?formNum="+cellvalue+"'>"+cellvalue+"</a>";
			} else {
				return "<a class='gridlink' href='${ctx}/qianchen/qianChenEditAction!loadQianChenDetail.action?formNum="+cellvalue+"'>"+cellvalue+"</a>";
			}
			break;
		case 'NEILIAN':
			if (rowObject.isNew == "1"){
				return "<a class='gridlink' href='${ctx}/nneilian/neiLianEditAction!loadNeiLianDetail.action?formNum="+cellvalue+"'>"+cellvalue+"</a>";
			} else {
				return "<a class='gridlink' href='${ctx}/neilian/neiLianEditAction!loadNeiLianDetail.action?formNum="+cellvalue+"'>"+cellvalue+"</a>";
			}
			break;
		default:
			break;
	}
}

//获取页面初始值
var flowType = $("#flowType").attr("value");
var branch = $("#branch").attr("value");
var formStatus = $("#formStatus").attr("value");
var startTime = $("#startTime").attr("value");
var endTime = $("#endTime").attr("value");
var title = $("#title").attr("value");
var formNum = $("#formNum").attr("value");

var closeStartTime = $("#closeStartTime").attr("value");
var closeEndTime = $("#closeEndTime").attr("value");

var localType = $("#localType").attr("value");
var creator = $("#creator").attr("value");
var creatCmp = $("#creatCmp").attr("value");
		
var postData = {
		flowType : flowType,
		branch: branch,
		formStatus : formStatus,
		startTime : startTime,
		endTime : endTime,
		closeStartTime: closeStartTime,
		closeEndTime: closeEndTime
};
		
function selectBranch(obj){
	document.getElementById("FormNum").style.display = "none"
	document.getElementById("Title").style.display = "none"
	if(obj.value=="FormNum"){
		document.getElementById("FormNum").style.display = ""
	}
	if(obj.value=="Title"){
		document.getElementById("Title").style.display = ""
	}
}
		
//渲染流程类型为中文
function renderFlowType(cellvalue, options, rowObject){
	switch(cellvalue)
	  	{
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
		 	
	$(function(){ 
	  $("#list1").jqGrid({
	  			  
	    caption: '查询表单列表',//表格标题
	    url:'${ctx}/management/searchMyWorks.action',//请求路径
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
	    
	  	autowidth:true,//根据父容器调整宽度,只在初始化阶段起作用
	  	height:200,//高度    
	  	
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
	    
			colNames:['文件编号', '类别', '主旨', '申请人', '提交人', '申请日期', '结案日期', '流程编号'],//表格列名称
			colModel :[ //列模型 
	      {name:'formnum', index:'formnum', width:180, formatter:makeURL}, 
	      {name:'flowTypeName', index:'flowTypeName',width:60, align:'center',formatter:renderFlowType,sortable:false},
	      {name:'title', index:'title',width:120},			      
	      {name:'creatorName', index:'creatorName',width:90, align:'center',sortable:false},
	      {name:'actualName', index:'actualName', width:90, align:'center',sortable:false}, 
	      {name:'createTime', index:'createTime',width:70, align:'center'},
	      {name:'endTime', index:'endTime',width:70, align:'center',sortable:false},
	      {name:'workId', index:'workId',width:90, hidden:true}
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
		        totalrows:"totalrows", // 表示需从Server得到总共多少行数据的参数名称，参见jqGrid选项中的rowTotal
		        flowType : flowType  
		 },
		 postData : postData
	  }); 
});
jQuery("#confirm").click( function() {
	//获取页面查询条件变更后的值
	flowType = $("#flowType").attr("value");
	branch = $("#branch").attr("value");
	formStatus = $("#formStatus").attr("value");
	startTime = $("#startTime").attr("value");
	endTime = $("#endTime").attr("value");
	
	closeStartTime = $("#closeStartTime").attr("value");
	closeEndTime = $("#closeEndTime").attr("value");
	
	/* localType = $("#localType").attr("value");
	creator = $("#creator").attr("value");
	creatCmp = $("#creatCmp").attr("value"); */
	
	//重新赋值
	postData.flowType = flowType;
	postData.branch = branch;
	postData.formStatus = formStatus;
	postData.startTime = startTime;
	postData.endTime = endTime;
	postData.formNum = $("#formNum").attr("value");
	postData.title = $("#title").attr("value");
	
	postData.closeStartTime = closeStartTime;
	postData.closeEndTime = closeEndTime;
	
	postData.localType = $("#localType").attr("value");
	postData.creator = $("#creator").attr("value");
	postData.creatCmp = $("#creatCmp").attr("value");

	jQuery("#list1").jqGrid('setGridParam',{postData:postData}).trigger("reloadGrid")
});

jQuery("#export").click(function(){
	//${ctx }/export!toExcel.action
	document.getElementById("advancedForm").action="${ctx }/export!toExcel.action";	
	document.getElementById('advancedForm').submit();
});
</script>
</body>
</html>
