<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/pages/taglibs.inc" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=8">
<title></title>
<jsp:include page="/pages/common_min.jsp"></jsp:include>
<script type="text/javascript" src='${ctx }/js/layout.js'></script>
</head>

<body>
 <div class="rightpage01">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td><table width="400" border="0" cellspacing="8" cellpadding="0">
        <tr>
          <td width="100" align="right">查询类别：</td>
          <td colspan="3">
           <div class="rule-single-select">
            <select name="branch" id="branch" onchange="selectBranch(this)">
              <option value="MySubmit" selected>我申请的</option>
              <option value="ThisUnitSubmit">本单位发文的</option>
              <option value="SubDeptSubmit">下辖部门提交的</option>
              <option value="MyApproved">我签核过的</option>
              <option value="FormNum">按编号</option>
              <option value="Title">按主旨</option> 
              <option value="MyAccepted">我接收的</option>                          
            </select>
            </div>
          </td>
        <tr>
          <td align="right">提交日期：</td>
          <td>            
          	<input name="startTime" type="text" class="Wdate" id="startTime" onclick="WdatePicker({el:'startTime',format:'yyyy-MM-dd'})"/>
          </td>
          <td>           
            &nbsp;&nbsp;至&nbsp;&nbsp;
          </td> 
          <td>
            <input name="endTime" type="text" class="Wdate" id="endTime" onclick="WdatePicker({el:'endTime',format:'yyyy-MM-dd'})"/>
          </td>
        </tr>
        <script>  
			   document.getElementById("startTime").value = preMonthDate; 
			   document.getElementById("endTime").value = date; 
		</script>
        <tr>
          <td width="100" align="right">表单状态：</td>
          <td colspan="3">
           <div class="rule-single-select">
            <select name="formStatus" id="formStatus">
              <option value="ALL" selected>全选</option>
              <option value="TEMP">暂存</option>                          
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
          <td align="right">表单主旨：</td>
          <td colspan="3"><input type="text" name="title" id="title" class="width01 appInput"/></td>
        </tr>
        <tr id="FormNum" style="display:none">
          <td align="right">表单编号：</td>
          <td colspan="3"><input type="text" name="formNum" id="formNum" class="width01 appInput"/></td>
        </tr>
      </table></td>
      <td style="padding-left: 50px;"><a href="#" id="confirm" class="button blue bigrounded">查询</a></td>
    </tr>
  </table>
  <table id="list1"></table>
  <div id="pager2"></div>
</div>
<script type="text/javascript">
		//获取页面初始值
		var branch = $("#branch").attr("value");
		var formStatus = $("#formStatus").attr("value");
		var startTime = $("#startTime").attr("value");
		var endTime = $("#endTime").attr("value");
		var title = $("#title").attr("value");
		var formNum = $("#formNum").attr("value");
		var neiLianReferInfo;
		
		var postData = {
				flowType : "NEILIAN",
				branch: branch,
				formStatus : formStatus,
				startTime : startTime,
				endTime : endTime
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
			 　　    return "签呈" ;
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
			  			  
			    caption: '参考内联列表',//表格标题
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
			    
			    //autowidth:true,//根据父容器调整宽度,只在初始化阶段起作用
			    width: 780,
			    //shrinkToFit:true,
			  	height:150,//高度  
			  	
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
			    
 				colNames:['文件编号', '类别', '主旨', '申请人', '提交人', '申请时间', '流程编号'],//表格列名称
 				colModel :[ //列模型 
			      {name:'formnum', index:'formnum', width:140}, 
			      {name:'flowTypeName', index:'flowTypeName',width:40,align:'center',formatter:renderFlowType},
			      {name:'title', index:'title',width:120},			      
			      {name:'creatorName', index:'creatorName',width:90,align:'center'},
			      {name:'actualName', index:'actualName', width:90,align:'center'}, 
			      {name:'createTime', index:'createTime',width:90,align:'center'},
			      {name:'workId', index:'workId', hidden:true}
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
				 postData : postData,
				 
				 onSelectRow: function(id){	
				 	var rowdata = $("#list1").jqGrid("getRowData",id);
				 	neiLianReferInfo = rowdata;
	      		 }
			  }); 
		});
		
		jQuery("#confirm").click( function() {
			//获取页面查询条件变更后的值
			branch = $("#branch").attr("value");
			formStatus = $("#formStatus").attr("value");
			startTime = $("#startTime").attr("value");
			endTime = $("#endTime").attr("value");
			
			//重新赋值
			postData.branch = branch;
			postData.formStatus = formStatus;
			postData.startTime = startTime;
			postData.endTime = endTime;
			postData.formNum = $("#formNum").attr("value");
			postData.title = $("#title").attr("value");
	
			jQuery("#list1").jqGrid('setGridParam',{postData:postData}).trigger("reloadGrid")
		});	
		
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
		
		function confirm(){
			parent.setNeiLianRefer(neiLianReferInfo);
		}
</script>
</body>
</html>
