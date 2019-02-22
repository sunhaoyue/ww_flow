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
<script type="text/javascript">
//用于刷新左侧的导航菜单
window.parent.leftFrame.location.href = "${ctx }/management/findMyWorksCount.action";
</script>
<body>
<div class="rightpage01">
<table id="list1"></table>
<div id="pager2"></div>
<script type="text/javascript">
//渲染文件编号为链接
function makeURL(cellvalue, options, rowObject) {
	switch (rowObject.flowTypeName) {
	case 'QIANCHENG':
		if (rowObject.isNew == "1"){
			return "<a class='gridlink' href='${ctx }/nqianchen/qianChenEditAction!loadQianChenDetail.action?formNum="+cellvalue+"&workId="+rowObject.workId+"&deptId="+rowObject.deptId+"&role="+rowObject.role+"'>"+cellvalue+"</a>";
		} else {
			return "<a class='gridlink' href='${ctx }/qianchen/qianChenEditAction!loadQianChenDetail.action?formNum="+cellvalue+"&workId="+rowObject.workId+"&deptId="+rowObject.deptId+"&role="+rowObject.role+"'>"+cellvalue+"</a>";
		}
		break;
	case 'NEILIAN':
		if (rowObject.isNew == "1") {
			return "<a class='gridlink' href='${ctx }/nneilian/neiLianEditAction!loadNeiLianDetail.action?formNum=" + cellvalue + "&workId=" + rowObject.workId + "&deptId=" + rowObject.deptId + "&role=" + rowObject.role + "'>" + cellvalue + "</a>"
		} else {
			return "<a class='gridlink' href='${ctx }/neilian/neiLianEditAction!loadNeiLianDetail.action?formNum=" + cellvalue + "&workId=" + rowObject.workId + "&deptId=" + rowObject.deptId + "&role=" + rowObject.role + "'>" + cellvalue + "</a>"
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

//alert(groupid);
$(function() {
	var mainheight = $(window).height();
	var mainwidth = $(window).width();
	var w = mainwidth - 60;
	var h = mainheight - 120;
	$("#list1").jqGrid({
		caption: '待审核列表',//表格标题
		url: '${ctx }/management/findMyToDoWorks.action',//请求路径
		postData: {},//额外请求参数,json格式
		datatype: 'json',//后台返回数据类型
		mtype: 'post',//请求类型
		sortname: 'createTime',
		sortorder: 'desc',

		gridview: true,
		gridstate: 'hidden',
		hoverrows: true,//是否显示鼠标悬浮数据记录行效果			    
		sortable: true,//列是否可拖拽排列		        
		rownumbers: true,//是否显示序号列
		rownumWidth: 50,//序号列宽度
		//width:700,//宽度	
		autowidth: true,//根据父容器调整宽度,只在初始化阶段起作用
		shrinkToFit: false,
		height: h,//高度
		pager: '#pager2',//分页栏渲染目标
		//以下部分为分页部分
		page: 1,//初始显示第几页，默认1
		pagerpos: 'left',//分页栏位置,left、center、right,默认center
		rowNum: 20,//页面大小
		rowList: [10, 20, 30],//下拉选择页面大小
		pgbuttons: true,//是否显示分页按钮(上下页,首尾页)
		pginput: false,//是否显示跳转到第几页输入框
		pgtext: '{0}共{1}页',//默认,{0}表示当前第几页,{1}表示总页数
		//以下部分为分页总记录数部分
		viewrecords: true,//分页栏是否显示总记录数
		recordpos: 'right',//总记录数位置
		emptyrecords: '无记录',//后台无数据显示文本,配合viewrecords使用
		recordtext: '{0}-{1} 共{2}条',//{0}当前页记录开始索引,{1}当前页记录结束索引,{2}总记录条数
		colNames: ['文件编号', '类别', '主旨', '申请人', '提交人', '申请时间', '流程编号', '部门编号', '工作项角色'],//表格列名称
		colModel: [ //列模型 
			{name: 'formnum',index: 'formnum',width: w*0.25,formatter: makeURL},
			{name: 'flowTypeName',index: 'flowTypeName',width: w*0.06, align:'center',formatter: renderFlowType},
			{name: 'title',index: 'title',width: w*0.3},
			{name: 'creatorName',index: 'creatorName',width: w*0.12, align:'center'},
			{name: 'actualName',index: 'actualName',width: w*0.12, align:'center'},
			{name: 'createTime',index: 'createTime',width: w*0.1, align:'center'},
			{name: 'workId',index: 'workId',hidden: true},
			{name: 'deptId',index: 'deptId',hidden: true},
			{name: 'role',index: 'role',hidden: true}
		],

		jsonReader: { //数据解析器
			root: "result",//结果数据
			page: 'currentPageNo',//当前页
			records: 'totalSize',//共多少数据
			total: 'totalPageCount',//共多少页
			repeatitems: false,
			userData: 'userdata' //统计数据字段,默认
		},

		//以下配置传递后台参数名称
		prmNames: {
			page: "pageNo",// 表示请求页码的参数名称  
			rows: "pageSize",// 表示请求页面大小的参数名称  
			sort: "sidx",// 表示用于排序的列名的参数名称  
			order: "sord",// 表示采用的排序方式的参数名称  
			search: "_search",// 表示是否是搜索请求的参数名称  
			nd: "nd",// 表示已经发送请求的次数的参数名称  
			id: "id",// 表示当在编辑数据模块中发送数据时，使用的id的名称  
			oper: "oper",// operation参数名称（我暂时还没用到）  
			editoper: "edit",// 当在edit模式中提交数据时，操作的名称  
			addoper: "add",// 当在add模式中提交数据时，操作的名称  
			deloper: "del",// 当在delete模式中提交数据时，操作的名称  
			subgridid: "id",// 当点击以载入数据到子表时，传递的数据名称  
			npage: null,
			totalrows: "totalrows" // 表示需从Server得到总共多少行数据的参数名称，参见jqGrid选项中的rowTotal  
		}
	});
});
</script>
</div>
<div id="applySuccess">	
<input name="formNum" type="hidden" id="formNum" value="<s:property value="flow.formNum"/>"/>      
<input name="FlowStatus" type="hidden" id="FlowStatus" value="<s:property value="#request.FlowStatus"/>"/>
<input name="innerhuibanIds" type="hidden" id="innerhuibanIds" value="<s:property value="#request.innerhuibanIds"/>"/>
<input name="huibanpersons" type="hidden" id="huibanpersons" value="<s:property value="#request.huibanpersons"/>"/>
<input name="nextPerson" type="hidden" id="nextPerson" value="<s:property value="#request.nextPerson"/>"/>
<input name="joinType" type="hidden" id="joinType" value="<s:property value="#request.joinType"/>"/>
<input name="flowId" type="hidden" id="flowId" value="<s:property value="flow.id"/>"/>
<script type="text/javascript">
var formNum = $("#formNum").attr("value");
var FlowStatus = $("#FlowStatus").attr("value");
var innerhuibanIds = $("#innerhuibanIds").attr("value");
var huibanpersons = $("#huibanpersons").attr("value");
var nextPerson = $("#nextPerson").attr("value");
var joinType = $("#joinType").attr("value");
var flowId = $("#flowId").attr("value");
if(formNum != '' && FlowStatus != '') {
	var content = '单据号' + formNum + '提交成功!';
	var content2 = "";
	//判断流程状态
	switch(FlowStatus) {
		case 'INNERJOINTSIGN_START':
			content2 = '内部会办进行中，会办人员列表如下：';
			break;
		case 'CENTERJOINTSIGN_START':
			content2 = '本中心会办进行中，会办人员列表如下：';
			break;
		case 'CMPCODEJOINTSIGN_START':
			content2 = '本单位会办进行中，会办人员列表如下：';
			break;
		case 'SYSTEMJOINTSIGN_START':
			content2 = '本体系会办进行中，会办人员列表如下：';
			break;
		case 'CHENGHE':
			content= content+'<br/>'+'下一个审核人是'+nextPerson;
			break;
		case 'JOINTSIGN_START':
			content2 = '会办进行中，会办人员列表如下：';
			break;
		case 'ASSIGN':
			content= content+'<br/>'+'指派成功，下一个被指派人是'+nextPerson;
			break;
		case 'CENTER_JOINTSIGN_BRANCH_FINISH':
		case 'CMPCODE_JOINTSIGN_BRANCH_FINISH':
		case 'SYSTEM_JOINTSIGN_BRANCH_FINISH':
		case 'JOINTSIGN_BRANCH_FINISH':
			content= content+'该会办分支已完成！';
			break;
		case 'CONFIRM':
			content= content+'需要本人确认，'+'发起人是'+nextPerson;				
			break;
		case 'CANCEL':
			content= content+'该单据被撤销！';				
			break;
		case 'SECONDFINAL_DECISION_START':
			content= content+'<br/>'+'下一个审核人是'+nextPerson;
			break;
		case 'NEXTBUSINESS_DECISION_START':
		case 'BUSINESS_DECISION_START':
		case 'NEXTFINAL_DECISION_START':
			content= content+'<br/>'+'下一个审核人是'+nextPerson;
			break;
		case 'FINAL_DECISION_START':
		case 'FINALPLUS_DECISION_START':
			//content= content+'<br/>'+'开始最终核决，最终审核人是'+nextPerson;
			content= content+'<br/>'+'下一个审核人是'+nextPerson;
			break;
		case 'FINAL_DECISION_SIGN':
		case 'FINALPLUS_DECISION_SIGN':
			content= content+'<br/>'+'下一个审核人是'+nextPerson;
			break;
		case 'FINAL_DECISION_END':
			content= content+'该单据被核决！';
			break;
		case 'COPY_SEND':
			content= content+'该单据被核决！';
			break;
		case 'REJECT':
			content= content+'该单据被驳回！';
			break;
		default:
			break;
	}
	if(FlowStatus=='INNERJOINTSIGN_START'||FlowStatus=='JOINTSIGN_START'||FlowStatus=='CMPCODEJOINTSIGN_START'||FlowStatus=='CENTERJOINTSIGN_START'||FlowStatus=='SYSTEMJOINTSIGN_START'){
		$.dialog({
				title:'提示',
				 content:'url:${ctx }/pages/pub/nextTip.jsp'
			});
		$.dialog.data('content',content);
		$.dialog.data('content2',content2);
		$.dialog.data('innerhuibanIds',innerhuibanIds);
		$.dialog.data('huibanpersons',huibanpersons);  
		$.dialog.data('flowId',flowId);
	} else {
		$.dialog({
			title:'提示',
		 	content:content
		});
	}
}
</script>
</div>
</body>
</html>
