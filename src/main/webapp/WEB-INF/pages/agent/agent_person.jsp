<%@ page contentType="text/html; charset=utf-8"%>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td width="100">代理人姓名：<span class="text03">*</span></td>
		<td width="145">
			<input name="agentUserName1" type="text" class="validate[required] appInput"
				id="agentUserName1" style="height:16px;line-height:14px;width:120px"  readonly="readonly" />
		</td>
		<td width="60">职称：<span class="text03">*</span></td>
		<td width="239">
			<input name="agentPostName1" type="text" class="validate[required] appInput"
				id="agentPostName1" style="height:16px;line-height:14px;width:200px" readonly="readonly" />
		</td>
		<input name="agentUserId1" type="hidden" id="agentUserId1"/>		
		<input name="agentEmployeeId1" type="hidden" id="agentEmployeeId1"/>
		<input name="agentPostCode1" type="hidden" id="agentPostCode1"/>
		<input name="agentDeptId1" type="hidden" id="agentDeptId1"/>   
		<input name="agentpdept" type="hidden" id="agentpdept"/>     
		<td width="230">
			<table border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td>
						<div id="query2" class="button blue small" onclick="toOrganQuery()">按组织查询</div>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td>代理人部门：<span class="text03">*</span></td>
		<td colspan="4">
			<span class="width04">
			<input name="agentpdept1" type="text" class="width02 appInput" id="agentpdept1" readonly="readonly"  />
			</span>
			<span class="width04">
			<input name="agentpdept2" type="text" class="width02 appInput" id="agentpdept2" readonly="readonly" />
			</span>
			<span class="width04">
			<input name="agentpdept3" type="text" class="width02 appInput" id="agentpdept3" readonly="readonly" />
			</span>
		</td>
	</tr>
	<tr>
		<td>所需代理岗位：<span class="text03">*</span>
		<input name="savedPostNames" type="hidden" id="savedPostNames"/>
		</td>
        <td colspan="3"><input type="text" name="position" id="position" class="validate[required] appInput"
             style="margin-top:-1px;margin-bottom:-1px;height:16px;line-height:14px;width:250px" readonly/>
        <div class="button blue small" id="deptQuery" onclick="toOrganQuery()">查&nbsp;询</div></td>
	</tr>
	<tr>
		<td colspan="5" align="center">
			<table width="200" border="0" cellspacing="10" cellpadding="0">
				<tr>
					<td>
						<div class="button blue small" onclick="addAgentPerson1()">新 增</div>
					</td>
					<td>
						<div class="button blue small" onclick="resetAgentPerson1()">重 置</div>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<table id="agentPersonlist1"></table>
	</tr>
</table>
<script>
	 		jQuery("#agentPersonlist1").jqGrid({
	 			caption: '代理人列表',//表格标题
	 			datatype: "local", 
				shrinkToFit:false,
				autowidth:true,//根据父容器调整宽度,只在初始化阶段起作用
				height:150,//高度
				rownumbers: true,//是否显示序号列
			    rownumWidth:50,//序号列宽度,  
	 			colNames:['被代理人', '代理岗位', '代理人', '代理人部门', '操作', '代理人id', '代理人工号', '代理人岗位代码', '代理人所属部门Id', '代理类别', '代理编号', '是否为新增代理人'], 
	 			colModel:[{name:'actualUserName',index:'actualUserName', width:80}, 
	 				 {name:'agentPostName',index:'agentPostName', width:150, formatter:cachePostNames }, 
	 				 {name:'agentUserName',index:'agentUserName', width:120, align:'center'},
	 				 {name:'agentDept',index:'agentDept', width:240},
	 				 {name:'operation',index:'operation', width:80, align:'center', formatter:renderProxy},
	 				 {name:'agentUserId',index:'agentUserId', hidden:true},
	 				 {name:'agentEmployeeId',index:'agentEmployeeId', hidden:true},   		 	 
	 				 {name:'agentPostCode',index:'agentPostCode', hidden:true},   		 
	 				 {name:'agentDeptId',index:'agentDeptId', hidden:true},
	 				 {name:'agentType',index:'agentType', hidden:true},
	 				 {name:'id',index:'id', hidden:true},		 
	 				 {name:'isNew',index:'isNew', hidden:true}	 				 		 	 				 	 				 		  	 				 		 	 				 	 				 		 	 				 		 	 				 	 				 		  	 				 		 	 				 	 				 		 
	 			]
 			});
 			
 			var mydata1 = new Array();			
 			
 			function addAgentPerson1(){
 				var agentPostName = $("#selectedPostName").attr("value")+"("+$("#selectedDeptPath").attr("value")+")";
 				if($("#applyForm").validationEngine('validate')==false){
					return false;
				};


				//岗位重复判断
				if ($("#savedPostNames").val().split(",").indexOf(agentPostName) >= 0) {
					alert("该岗位已被代理，请重新选择。");
					return;
				}

 				var actualUserName = $("#actualRealName").attr("value");
 				//代理岗位
 				//var agentPostName = $("#selectedPostName").attr("value")+"("+$("#selectedDeptPath").attr("value")+")";
 				var agentUserName = $("#agentUserName1").attr("value");
 				var agentDept = $("#agentpdept").attr("value");

				if (agentUserName.indexOf(actualUserName) >= 0) {
					alert("代理人不能为自己，请重新选择。");
					return;
				}
 				
 				//隐藏字段
 				var agentUserId = $("#agentUserId1").attr("value");
 				var agentEmployeeId = $("#agentEmployeeId1").attr("value");
 				var agentPostCode = $("#selectedPostCode").attr("value");
 				var agentDeptId = $("#agentDeptId1").attr("value"); 
 				var agentType = "PositionAgent";
				
				var rowdata ={actualUserName:actualUserName,
			 		agentPostName:agentPostName,
			 		agentUserName:agentUserName,
			 		agentDept:agentDept,
			 		agentUserId:agentUserId,
			 		agentEmployeeId:agentEmployeeId,
			 		agentPostCode:agentPostCode,
			 		agentDeptId:agentDeptId,
			 		agentType:agentType,
			 		id:0,
			 		isNew: 1
			 	}
			 	//mydata1.push(rowdata);
			 	var rowIds = $("#agentPersonlist1").jqGrid('getDataIDs');
				var index = rowIds.length;
				jQuery("#agentPersonlist1").jqGrid('addRowData',index+1,rowdata);
				resetAgentPerson1();
 			}
 			
 			//重置
 			function resetAgentPerson1(){
 				//显示的重置
 				$("#agentUserName1").attr("value","");
 				$("#agentPostName1").attr("value","");
 				$("#position").attr("value","");
 				
 				$("#agentpdept1").attr("value","");
 				$("#agentpdept2").attr("value","");
  				$("#agentpdept3").attr("value","");
 				
 				//隐藏域重置
 				$("#agentUserId1").attr("value","");
 				$("#agentEmployeeId1").attr("value","");
 				$("#agentDeptId1").attr("value","");
 				$("#agentpdept").attr("value","");
 			}
 			
 			 //渲染操作列
			function renderProxy(cellvalue, options, rowObject){
 				var agentId;
 				if(!id){
 					agentId = 0;
 				}
 				else{
 					agentId = id;
 				}
				var agentpersonid = rowObject.id;
				return "<input type='button' class='button blue small' value='删除' onclick='removePositionRow("+agentId+","+agentpersonid+ "," + options.rowId + ")'>";			
			}

			 //缓存已保存的岗位name
			function cachePostNames(cellvalue, options, rowObject) {
				savedPostNames = "";
				if ($("#savedPostNames").val() == "")
					savedPostNames = cellvalue;
				else
					savedPostNames = $("#savedPostNames").val() + "," + cellvalue;
				$("#savedPostNames").val(savedPostNames);
				return cellvalue;
			}
</script>
