<%@ page contentType="text/html; charset=utf-8"%>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td width="100">代理人姓名：<span class="text03">*</span></td>
		<td width="145">
			<input name="agentUserName2" type="text" class="validate[required] appInput"
				id="agentUserName2" style="height:16px;line-height:14px;width:120px" readonly="readonly" />
		</td>
		<td width="60">职称：<span class="text03">*</span></td>
		<td width="239">
			<input name="agentPostName2" type="text" class="validate[required] appInput"
				id="agentPostName2" style="height:16px;line-height:14px;width:200px" readonly="readonly" />
		</td>
		<input name="agentUserId2" type="hidden" id="agentUserId2"/>		
		<input name="agentEmployeeId2" type="hidden" id="agentEmployeeId2"/>
		<input name="agentPostCode2" type="hidden" id="agentPostCode2"/>
		<input name="agentDeptId2" type="hidden" id="agentDeptId2"/>   
		<input name="agentfdept" type="hidden" id="agentfdept"/>    
		<td width="230">
			<table border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td>
						<div id="query3" class="button blue small" onclick="toOrganQuery()">按组织查询</div>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td>代理人部门：<span class="text03">*</span></td>
		<td colspan="4">
			<span class="width04"><input name="agentfdept1" type="text"
					class="width02 appInput" id="agentfdept1" readonly="readonly" />
			</span>
			<span class="width04"><input name="agentfdept2" type="text"
					class="width02 appInput" id="agentfdept2" readonly="readonly" />
			</span>
			<span class="width04"><input name="agentfdept3" type="text"
					class="width02 appInput" id="agentfdept3" readonly="readonly" />
			</span>
		</td>
	</tr>
	<tr>
		<td colspan="5">
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td>工作流表单（可多选）</td>
					<td>
						<div id="flowQuery" class="button blue small" onclick="toOrganQuery()">表单选择</div>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td colspan="5">
			<textarea name="flowNames" cols="45" rows="5" class="textarea01"
				id="flowNames" readonly="readonly"></textarea>
			<input name="flows" type="hidden" id="flows"/>		

			<input name="savedFlowNames" type="hidden" id="savedFlowNames"/>

		</td>
	</tr>
	<tr>
		<td colspan="5" align="center">
			<table width="200" border="0" cellspacing="10" cellpadding="0">
				<tr>
					<td>
						<div class="button blue small" onclick="addAgentPerson2()">新 增</div>
					</td>
					<td>
						<div class="button blue small" onclick="resetAgentPerson2()">重 置</div>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<table id="agentPersonlist2"></table>
	</tr>
</table>
<script>
	 		jQuery("#agentPersonlist2").jqGrid({
	 			caption: '代理人列表',//表格标题
	 			datatype: "local", 
				shrinkToFit:true,
				autowidth:true,//根据父容器调整宽度,只在初始化阶段起作用
				height:150,//高度
				rownumbers: true,//是否显示序号列
			    rownumWidth:50,//序号列宽度,  
	 			colNames:['被代理人', '代理人', '表单数量', '可代理的表单', '操作', '代理人部门', '代理人id', '代理人工号', '代理人所属部门Id', '代理类别', '代理流程编号', '代理编号', '是否为新增代理人'], 
	 			colModel:[
	 			 	 {name:'actualUserName',index:'actualUserName', width:150},
	 			 	 {name:'agentUserName',index:'agentUserName', width:150, align:'center'},
	 			 	 {name:'flowCount',index:'flowCount', width:100, align:'center'},
	 				 {name:'flowNames',index:'flowNames', width:200, align:'center', formatter:cacheNames},
	 				 {name:'operation',index:'operation', width:100, align:'center', formatter:renderWork},	 				 
	 				 
	 				 {name:'agentDept',index:'agentDept', width:120,hidden:true},
	 				 
	 				 {name:'agentUserId',index:'agentUserId', width:120, hidden:true},
	 				 {name:'agentEmployeeId',index:'agentEmployeeId', width:120, hidden:true},   		 	 
	 				 {name:'agentDeptId',index:'agentDeptId', width:120, hidden:true},
	 				 {name:'agentType',index:'agentType', width:120, hidden:true},
	 				 {name:'flows',index:'flows', width:120, hidden:true}, 				 
	 				 {name:'id',index:'id', width:120, hidden:true},		 
	 				 {name:'isNew',index:'isNew', width:50, hidden:true}	 				 		 	 				 	 				 		  	 				 		 	 				 	 				 		 	 				 		 	 				 	 				 		  	 				 		 	 				 	 				 		 
	 			]
 			});
 			
 			var mydata2 = new Array();
 			//流程数
 			var flowCount = 0;
 			function addAgentPerson2(){

 				if($("#applyForm").validationEngine('validate')==false){
					return false;
				};

				if ($("#flowNames").val() == "") {
					alert("请选择工作流表单。");
					return false;
				}
				
 				var actualUserName = $("#actualRealName").attr("value");
 				var agentUserName = $("#agentUserName2").attr("value");
 				var agentDept = $("#agentfdept").attr("value");
 				
 				//隐藏字段
 				var agentUserId = $("#agentUserId2").attr("value");
 				var agentEmployeeId = $("#agentEmployeeId2").attr("value");
 				var agentDeptId = $("#agentDeptId2").attr("value");
 				var agentType = "FlowAgent";
 				
 				var flowNames = $("#flowNames").attr("value");
 				var flows = $("#flows").attr("value");
 				
				var rowdata ={actualUserName:actualUserName,
			 		flowNames:flowNames,
			 		flowCount:flowCount,
			 		agentUserName:agentUserName,
			 		agentDept:agentDept,
			 		agentUserId:agentUserId,
			 		agentEmployeeId:agentEmployeeId,
			 		agentDeptId:agentDeptId,
			 		agentType:agentType,
			 		flows:flows,
			 		isNew: 1
			 	}
			 	//mydata2.push(rowdata);
			 	var rowIds = $("#agentPersonlist2").jqGrid('getDataIDs');
				var index = rowIds.length;
				jQuery("#agentPersonlist2").jqGrid('addRowData',index+1,rowdata);
				resetAgentPerson2();
 			}
 			
 			//重置
 			function resetAgentPerson2(){
 				//显示的重置
 				$("#agentUserName2").attr("value","");
 				$("#agentPostName2").attr("value","");
 				$("#flowNames").attr("value","");
 				
 				$("#agentfdept1").attr("value","");
 				$("#agentfdept2").attr("value","");
  				$("#agentfdept3").attr("value","");
 				
 				//隐藏域重置
 				$("#agentUserId2").attr("value","");
 				$("#agentEmployeeId2").attr("value","");
 				$("#agentDeptId2").attr("value","");
 				$("#agentfdept").attr("value","");
 				$("#flows").attr("value","");	
 			}
 			
 			 //渲染操作列
			function renderWork(cellvalue, options, rowObject){
 				var agentId;
 				if(!id){
 					agentId = 0;
 				}
 				else{
 					agentId = id;
 				}
				var agentpersonid = rowObject.id;
				return "<input type='button' value='删除' onclick='removeFlowRow("+agentId+","+agentpersonid+ "," + options.rowId + ")'>";			
			}

 			 //缓存已保存的name，在tree打开时检查并禁用已保存项
			function cacheNames(cellvalue, options, rowObject) {
				savedFlowNames = "";
				if ($("#savedFlowNames").val() == "")
					savedFlowNames = cellvalue;
				else
					savedFlowNames = $("#savedFlowNames").val() + "," + cellvalue;
				$("#savedFlowNames").val(savedFlowNames);
				return cellvalue;
			}
			
			function setFlow(flowIds, flowNames, count){
				$("#flowNames").val(flowNames);
				$("#flows").val(flowIds);
				flowCount = count;
			}

			function getFlow(){
				if ($("#flowNames").val() == "" && $("#flows").val() == "" && $("#savedFlowNames").val() == "")
					return null;
				return {flowNames: $("#flowNames").val(), flowIds: $("#flows").val(), savedFlowNames: $("#savedFlowNames").val()}
			}

</script>
