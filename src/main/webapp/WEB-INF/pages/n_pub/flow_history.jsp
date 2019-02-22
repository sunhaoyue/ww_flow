<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="/pages/taglibs.inc" %>
    <table id="history"><tr ><td align="right" ></td></tr></table>
	<script>
	var flowStatus = $("#flowStatus").attr("value");
	var showEndFlag = false;

	function init_grid() {
	 		jQuery("#history").jqGrid({
	 			caption: '签核历程',//表格标题
	 			datatype: "local", 
				shrinkToFit:false,
				autowidth:true,//根据父容器调整宽度,只在初始化阶段起作用
				height:'auto',//高度    
	 			colNames:['部门','审批人', '时间', '状态','意见', '是否有子历程', '流程编号','',''], 
	 			colModel:[ 
	 			     {name:'deptName',index:'deptName', width:160, align:'center', formatter:renderDeptNameEx, 
		 			     cellattr: function(rowId, tv, rawObject, cm, rdata) {
		 			     	if (Number(rowId) == 1) {
			 			     	// ||flowStatus=='REJECT'
		 			     		if(showEndFlag){
		 			     			return ' colspan=5';
			 			     	}
							}
		 			     }
	 			     }, 
	 				 {name:'userRealName',index:'userRealName', width:120, 
		 			     cellattr: function(rowId, tv, rawObject, cm, rdata) {
		 			     	if (Number(rowId) == 1) {
			 			     	// ||flowStatus=='REJECT'
		 			     		if(showEndFlag){
		 			     			return ' style="display:none;"';
			 			     	}
							}
		 			     }
	 			     }, 
	 				 {name:'processTime',index:'processTime', width:120, 
		 			     cellattr: function(rowId, tv, rawObject, cm, rdata) {
		 			     	if (Number(rowId) == 1) {
			 			     	// ||flowStatus=='REJECT'
		 			     		if(showEndFlag){
		 			     			return ' style="display:none;"';
			 			     	}
							}
		 			     }
	 			     }, 
	 				 {name:'workStatus',index:'workStatus', width:80, align:'center', formatter:renderWorkStatus, 
		 			     cellattr: function(rowId, tv, rawObject, cm, rdata) {
		 			     	if (Number(rowId) == 1) {
			 			     	// ||flowStatus=='REJECT'
		 			     		if(showEndFlag){
		 			     			return ' style="display:none;"';
			 			     	}
							}
		 			     }
	 			     },
	 				 {name:'opinion',index:'opinion', width:260, 
		 			     cellattr: function(rowId, tv, rawObject, cm, rdata) {
		 			     	if (Number(rowId) == 1) {
			 			     	// ||flowStatus=='REJECT'
		 			     		if(showEndFlag){
		 			     			return ' style="display:none;"';
			 			     	}
							}
		 			     }
	 			     },
	 				 
	 				 {name:'haveChildren',index:'haveChildren', hidden:true},
	 				 {name:'workId',index:'workId', hidden:true},
	 				 {name:'employeeId',hidden:true},
	 				 {name:'deptId',hidden:true}
	 			]
 			});

	}
	
 			$.ajax({
			    type: "POST",
			    url: '${ctx}/nqianchen/listWorkHistory.action',
			    dataType:'json',
			    data:{'formNum':formNum},
			    success: function (data,status ) {

					//禁用编辑器
					if (flowStatus) {
						CKEDITOR.config.readOnly = true;
					//$("#detaildata").parent().hide();
					//$("#detaildata").parent().parent().append('<div style="height:300px;background:#fff;overflow:auto">' + $("#detaildata").val() + '</div>');
					}
					
			    	showEndFlag = (flowStatus=='CANCEL'||flowStatus=='FINAL_DECISION_END'||flowStatus=='COPY_SEND'||flowStatus=='CC_End'|| (flowStatus== 'REJECT' && data[0].processTime != ""));

			    	init_grid();
				    
					var rowNum = 1;
				    
			    	//var flowStatus = $("#flowStatus").attr("value");
			    	// 新增一行‘已结束’行 ||flowStatus=='REJECT'
			    	if(showEndFlag){
			    		var finishRow = {};
			    		finishRow.deptName = '已结束';
			    		jQuery("#history").jqGrid('addRowData',rowNum,finishRow);
			    		
			    		rowNum++;

			    		//禁用会办抄送
			    		//CKEDITOR.config.readOnly = true;
			    		$("#huibanDept").addClass("textarea02-readOnly");
			    		$("#chaosongDept").addClass("textarea02-readOnly");
			    		$("#opinion").addClass("textarea02-readOnly");
			    		$("#opinion").attr("disabled","disabled");

			    		//readonly bugfix for IE **cannot scroll
			    		//$("#cke_detail").css("position","relative");
						//$("#cke_detail").append("<div style='position: absolute; width:100%; height:100%; top:0; left:0; background:#000; filter:alpha(opacity=20);'></div>");

			    	}
			    	
			    	if(data.length>0){
			        	for(var i=0;i<=data.length;i++){
			 				jQuery("#history").jqGrid('addRowData',i+rowNum,data[i]);
			  			}		        
			  			
			  			if(!showEndFlag){
			  				$("#adminAssignTd").show();
			  			}
			  					
			        }
		    	}
 			})
	</script>
	

