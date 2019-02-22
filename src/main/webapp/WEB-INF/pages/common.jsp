<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="taglibs.inc" %>

<link href="${ctx }/css/csspage_01.css" rel="stylesheet" type="text/css" />
<link href="${ctx }/css/csspage_03.css" rel="stylesheet" type="text/css" />
<link id="skin" rel="stylesheet" type="text/css" href="${ctx }/css/jquery-ui-1.8.21.custom.css" />
<link rel="stylesheet" type="text/css" href="${ctx }/css/ui.jqgrid.css" />
<link rel="stylesheet" href="${ctx }/css/validationEngine.jquery.css" type="text/css"/>
<link rel="stylesheet" href="${ctx }/css/template.css" type="text/css"/>
<link rel="stylesheet" href="${ctx }/js/upload/uploadify.css" type="text/css"/>

<%-- jquery base  --%>
<script type="text/javascript" src='${ctx }/js/jquery-1.7.1.min.js'></script>
<script type="text/javascript" src='${ctx }/js/jquery.form.js'></script>

<%-- jquery validationEngine  --%>
<script src="${ctx }/js/validationEngine/languages/jquery.validationEngine-zh_CN.js" type="text/javascript" charset="utf-8"></script>
<script src="${ctx }/js/validationEngine/jquery.validationEngine.js" type="text/javascript" charset="utf-8"></script>
<script src="${ctx }/js/validationEngine/contrib/other-validations.js" type="text/javascript" charset="utf-8"></script>
<%-- grid--%>
<script type="text/javascript" src="${ctx }/js/jqgrid/grid.locale-cn.js"></script>
<script type="text/javascript" src="${ctx }/js/jqgrid/jquery.jqGrid.min.js"></script>
<%-- dialog--%>
<script type="text/javascript" src="${ctx }/js/lhgdialog/lhgdialog.js?skin=iblue"></script>
<%-- date--%>
<script src="${ctx }/js/My97DatePicker/WdatePicker.js" type="text/javascript"></script>
<%-- ckeditor--%>
<script charset="utf-8" type="text/javascript" src="${ctx }/js/ckeditor/ckeditor.js"></script>
<script charset="utf-8" type="text/javascript" src="${ctx }/js/ckeditor/adapters/jquery.js"></script>
<%-- upload--%>
<script type="text/javascript" src='${ctx }/js/upload/swfobject.js'></script>
<script type="text/javascript" src='${ctx }/js/upload/jquery.uploadify.v2.1.4.js'></script>
<%-- custom--%>
<script type="text/javascript" src='${ctx }/js/custom.js'></script>

<link href="${ctx }/css/2.0/ui.jqgrid.Ex.css" rel="stylesheet" type="text/css" />
<link href="${ctx }/css/2.0/style.css" rel="stylesheet" type="text/css" />

<script type="text/javascript">			
var date = new Date().format('yyyy-MM-dd');
var dateTime = new Date().format('yyyy-MM-dd hh:mm:ss');

//内文非空校验
function editorcheck() {
	if (CKEDITOR.instances.detail.getData() == "") {
		alert("内文不能为空！");
		return false;
	}
	return true;
}

//组织查询弹出设置
function toOrganQuery(type) {
	//会办选择前检查核决主管是否存在
	if (type == 'huiban') {}
	$('#query1').dialog({
		id: 'dlg1',
		height: 410,
		width: 700,
		title: '按组织查询',
		content: 'url:${ctx }/pages/organ_user.jsp'
	});
	$('#query3').dialog({
		id: 'dlg3',
		height: 300,
		width: 250,
		title: '查询组织',
		content: 'url:${ctx }/pages/organ2.jsp?query=huiban'
	});
	$('#query4').dialog({
		id: 'dlg4',
		height: 300,
		width: 250,
		title: '查询组织',
		content: 'url:${ctx }/pages/organ2.jsp?query=chaosong'
	});
	$('#query5').dialog({
		id: 'dlg5',
		height: 410,
		width: 700,
		title: '按组织查询',
		content: 'url:${ctx }/pages/organ_user.jsp?query=innerhuiban'
	});

	leader = $('input:radio[name="leader"]:checked').val() || $("#leader").val();
	$.dialog.data('deptId', fawenDeptId);
	if (typeof(contentType) != 'undefined') $.dialog.data('contentType', contentType);
	else $.dialog.data('contentType', 1);
	$.dialog.data('leader', leader);
	$.dialog.data('huibanDeptIds', huibanDeptIds);
	$.dialog.data('chaosongDeptIds', chaosongDeptIds);
	$.dialog.data('flowType', $("#flowType").val());
}

//签核子历程弹出设置
function toChildHistory(workId) {
	$('#childHistory' + workId).dialog({
		id: 'dlg' + workId,
		height: 350,
		width: 700,
		title: '签核历程',
		content: 'url:${ctx }/pages/pub/childhistory.jsp?workId=' + workId + '&formNum=' + formNum
	});
}

//签核子历程弹出设置
function toChildHistoryEx(workId) {
	$('#childHistory' + workId).dialog({
		id: 'dlg' + workId,
		height: 350,
		width: 700,
		title: '签核历程',
		content: 'url:${ctx }/pages/n_pub/childhistory.jsp?workId=' + workId + '&formNum=' + formNum
	});
}

//配置会办单位
function configHuibanDept(sids, snames) {
	huibanDepts = "";
	huibanDeptIds = "";
	$("#huibanDept").attr("value", "");
	$("#huibanDeptIds").attr("value", "");
	for (i = 0; i < sids.length; i++) {
		var deptName = snames[i];
		var deptId = sids[i]
		if (huibanDepts) {
			huibanDepts += ";" + deptName;
			huibanDeptIds += ";" + deptId;
		} else {
			huibanDepts = deptName;
			huibanDeptIds = deptId;
		}
	}
	$("#huibanDept").val(huibanDepts);
	$("#huibanDeptIds").val(huibanDeptIds);
}
//配置会办单位
function configEditHuibanDept(sids, snames) {
	huibanDepts = $("#huibanDept").val();
	huibanDeptIds = $("#huibanDeptIds").val();
	for (i = 0; i < sids.length; i++) {
		var deptName = snames[i];
		var deptId = sids[i]
		if (huibanDepts) {
			var oldDeptIds = huibanDeptIds.split(";");
			var found = false;
			for (var j = 0; j < oldDeptIds.length; j++) {
				var curOldDeptId = oldDeptIds[j];
				if (parseInt(curOldDeptId) == parseInt(deptId)) {
					found = true;
					break;
				}
			}
			if (!found) {
				huibanDepts += ";" + deptName;
				huibanDeptIds += ";" + deptId;
			}
		} else {
			huibanDepts = deptName;
			huibanDeptIds = deptId;
		}
	}
	$("#huibanDept").val(huibanDepts);
	$("#huibanDeptIds").val(huibanDeptIds);
}

//配置抄送单位
function configChaosongDept(sids, snames) {
	chaosongDepts = "";
	chaosongDeptIds = "";
	$("#chaosongDept").attr("value", "");
	$("#chaosongDeptIds").attr("value", "");
	for (i = 0; i < sids.length; i++) {
		var deptName = snames[i];
		var deptId = sids[i];
		if (chaosongDepts) {
			chaosongDepts += ";" + deptName;
			chaosongDeptIds += ";" + deptId;
		} else {
			chaosongDepts = deptName;
			chaosongDeptIds = deptId;
		}
	}
	$("#chaosongDept").val(chaosongDepts);
	$("#chaosongDeptIds").val(chaosongDeptIds);
}
//配置抄送单位
function configEditChaosongDept(sids, snames) {
	chaosongDepts = $("#chaosongDept").val();
	chaosongDeptIds = $("#chaosongDeptIds").val();
	for (i = 0; i < sids.length; i++) {
		var deptName = snames[i];
		var deptId = sids[i]
		if (chaosongDepts) {
			var oldDeptIds = chaosongDeptIds.split(";");
			var found = false;
			for (var j = 0; j < oldDeptIds.length; j++) {
				var curOldDeptId = oldDeptIds[j];
				if (parseInt(curOldDeptId) == parseInt(deptId)) {
					found = true;
					break;
				}
			}
			if (!found) {
				chaosongDepts += ";" + deptName;
				chaosongDeptIds += ";" + deptId;
			}
		} else {
			chaosongDepts = deptName;
			chaosongDeptIds = deptId;
		}
	}
	$("#chaosongDept").val(chaosongDepts);
	$("#chaosongDeptIds").val(chaosongDeptIds);
}

//机密性、时效性文字渲染
function renderLevel() {
	//机密性
	switch ($("#secretLevel").attr("value")) {　　
	case '1':
		　　$("#secretLevelName").val("一般");　　
		break;　　
	case '2':
		　　$("#secretLevelName").val("密件");　　
		break;　　
	default:
		　　
		break;　　
	}
	//时效性
	switch ($("#exireLevel").attr("value")) {　　
	case '1':
		　　$("#exireLevelName").val("一般");　　
		break;　　
	case '2':
		　　$("#exireLevelName").val("速件");　　
		break;　　
	default:
		　　
		break;　　
	}
}

//类别文字渲染
function renderContentType() {
	//类别
	switch ($("#type").attr("value")) {　　
	case '1':
		　　$("#contentType").val("体系内部");　　
		break;　　
	case '2':
		　　$("#contentType").val("地方到总部");　　
		break;　　
	default:
		　　
		break;　　
	}
}

//核决主管文字渲染
function renderDescionMaker() {
	var isHQTR = false;
	var tmp = $("#isHQTR").attr("value");
	if (tmp == "true") {
		isHQTR = true;
	}
	var dispStr = "";
	//核决主管
	switch ($("#leader").attr("value")) {
	case 'DEPTLEADER':
		if (isHQTR) {
			dispStr = "单位/部门主管";
		} else {
			dispStr = "部门主管";
		}
		break;
	case 'CENTRALLEADER':
		if (isHQTR) {
			dispStr = "单位最高主管";
		} else {
			dispStr = "中心主管";
		}
		break;
	case 'UNITLEADER':
		if (isHQTR) {
			dispStr = "神旺控股执行总经理";
		} else {
			dispStr = "单位最高主管";
		}
		break;
	case 'REGINLEADER':
		dispStr = "事业部最高主管";
		break;
	case 'HEADLEADER':
		dispStr = "神旺控股执行总经理";
		break;
	default:
		dispStr = "";
		break;　　
	}
	$("#descionMaker").val(dispStr);
}

//会办顺序文字渲染	
function renderJoinType() {
	//会办顺序
	switch ($("#joinType").attr("value")) {　　
	case 'SEQUENCE':
		　　$("#jointSignType").val("顺序会办");　　
		break;　　
	case 'CONCURRENT':
		　　$("#jointSignType").val("同时会办");　　
		break;　　
	default:
		　　
		break;　　
	}
}

//核决主管过滤(防呆处理)
function judgeDecisionMaker() {
	$.ajax({
		type:
		"POST",
		url: '${ctx }/qianchen/judgeDecisionMaker.action',
		dataType: 'json',
		data: {
			'deptId': fawenDeptId,
			'employeeId': actualEmployeeId,
			postCode: actualPostCode
		},
		success: function(data, status) {
			if (data) {
				//部门主管/中心副主管，可选为中心主管、单位最高主管
				if (data.mgDeptFlg == '1' || data.mgA_DeptFFlg == '1') {
					$("#deptleader input").attr("disabled", "disabled");
				}
				//中心主管，可选为单位最高主管
				if (data.mgA_DeptFlg == '1' || data.mgCentFFlg == '1') {
					$("#deptleader input").attr("disabled", "disabled");
					$("#centalleader input").attr("disabled", "disabled");
				}
				//单位主管，都不可选			    		
				if (data.mgCentFlg == '1') {
					$("#deptleader input").attr("disabled", "disabled");
					$("#centalleader input").attr("disabled", "disabled");
					$("#unitleader input").attr("disabled", "disabled");
				}
			}
		}
	})
}

//类别选择
function typeSelect() {
	$(':radio[name=leader]').attr('checked', false);
	if (document.getElementById('select').value == 2) {
		document.getElementById('deptleader').style.display = "none";
		document.getElementById('unitleader').style.display = "none";
		document.getElementById('centalleader').style.display = "none";
		document.getElementById('reginleader').style.display = "block";
		document.getElementById('headleader').style.display = "block";
	}
	//体系内
	if (document.getElementById('select').value == 1) {
		document.getElementById('deptleader').style.display = "block";
		document.getElementById('unitleader').style.display = "block";
		document.getElementById('centalleader').style.display = "block";
		document.getElementById('reginleader').style.display = "none";
		document.getElementById('headleader').style.display = "none";
	}
	contentType = $("#select").attr("value");
	try{
		hiddenHeader();
	}catch(e){}
}

//渲染签呈历程
function renderDeptName(cellvalue, options, rowObject) {
	var workId = rowObject.workId;
	var buttonId = 'childHistory' + workId;
	if (rowObject.haveChildren == true) {
		return cellvalue + '&nbsp;&nbsp<input type="button" value="签核历程" class="button blue small" onclick="toChildHistory(' + workId + ')" id="' + buttonId + '">';
	} else {
		return cellvalue;
	}
}

//渲染签呈历程
function renderDeptNameEx(cellvalue, options, rowObject) {
	var workId = rowObject.workId;
	var buttonId = 'childHistory' + workId;
	if (rowObject.haveChildren == true) {
		return cellvalue + '&nbsp;&nbsp<input type="button" value="签核历程" class="button blue small" onclick="toChildHistoryEx(' + workId + ')" id="' + buttonId + '">';
	} else {
		return cellvalue;
	}
}

// 渲染状态
function renderWorkStatus(cellvalue, options, rowObject) {
	if (rowObject.hbAgree == "2") {
		return "<font color='red'>" + cellvalue + "</font>";
	} else {
		return cellvalue;
	}
}

//渲染文件编号为链接，供下载
function makeURL(cellvalue, options, rowObject) {
	var id = rowObject.id;
	if (rowObject.isNew == '1') {
		return cellvalue;
	} else {
		return "<a href='${ctx }/common/download.action?id=" + id + "'>" + cellvalue + "</a>"
	}
}

//弹出指派窗口
function toAssign() {
	$('#assign').dialog({
		id: 'dlg5',
		height: 400,
		width: 700,
		title: '按组织查询',
		content: 'url:${ctx }/pages/organ_user.jsp?query=assign'
	});
	$.dialog.data('assignDeptId', assignDeptId);
}
// 地方单位最高主管指派窗口
function toLocalAssign(){
	$('#localAssign').dialog({
		id: 'localAssign',
		height: 400,
		width: 700,
		title: '按组织查询',
		content: 'url:${ctx }/pages/organ_user.jsp?query=assign&localAssign=true'
	});
	$.dialog.data('assignDeptId', assignDeptId);
	$.dialog.data('curWorkId', $("#workId").val());
}
// 事业部主管弹出指派窗口
function toDivisionAssign() {
	$('#divisionAssign').dialog({
		id: 'dlg5',
		height: 400,
		width: 700,
		title: '按组织查询',
		content: 'url:${ctx }/pages/organ_user.jsp?query=assign&divisionAssign=true'
	});
	$.dialog.data('assignDeptId', assignDeptId);
	$.dialog.data('curWorkId', $("#workId").val());
}
//最终核决主管弹出指派窗口
function toBossAssign() {
	$('#bossAssign').dialog({
		id: 'dlg5',
		height: 400,
		width: 700,
		title: '按组织查询',
		content: 'url:${ctx }/pages/organ_user.jsp?query=assign&bossAssign=true'
	});
	$.dialog.data('assignDeptId', assignDeptId);
	$.dialog.data('curWorkId', $("#workId").val());
}
//弹出管理员指派窗口
function toAdminAssign() {
	//判断选中行
	var selectedRowId = jQuery("#history").jqGrid("getGridParam", "selrow");
	if (!selectedRowId || selectedRowId.length == 0) {
		alert("请选择要进行分派的记录");
		return;
	}
	var selectedRow = jQuery("#history").jqGrid("getRowData", selectedRowId);
	if (selectedRow.workStatus != '处理中') {
		alert("只有处理中的任务才能进行分派");
		return;
	}
	$("#adminAssignDeptId").val(selectedRow.deptId);
	$("#adminAssignWorkId").val(selectedRow.workId);
	$("#adminAssignOldEmployeeId").val(selectedRow.employeeId);

	$('#adminAssign').dialog({
		id: 'dlg5',
		height: 400,
		width: 700,
		title: '按组织查询',
		content: 'url:${ctx }/pages/organ_user.jsp?query=assign&adminAssign=true'
	});
	$.dialog.data('assignDeptId', $("#adminAssignDeptId").val());
}

//暂存表单
function saveApply(saveUrl) {
	$("#save").click(function() {
		var form = $("#applyForm");
		form.ajaxSubmit({
			url: saveUrl,
			type: 'post',
			dataType: "json",
			beforeSubmit: function(array, form, option) {
				if (form.validationEngine('validate') == false) {
					return false;
				};
				if (editorcheck() == false) {
					return false;
				};
				/* 提交遮罩 */
				//form.mask ? form.mask() : null;
				$.dialog.tips('数据正在提交中，请稍候...', 6000, 'tips.gif');
			},
			beforeSerialize: function($Form, options) {
				/* 解决内文无法提交到后台的问题 */
				for (instance in CKEDITOR.instances) {
					CKEDITOR.instances[instance].updateElement();
				}
				return true;
			},
			success: function(data) {
				$.dialog.tips('暂存表单成功！', 5000, 'tips.gif');
				//alert('暂存表单成功！');
				window.location.href = '${ctx }/pages/flowmanagement/flowtemplates.jsp';
			}
		});
	});
}

//加载已上传附件
function loadAttachments() {
	$.ajax({
		type: "POST",
		url: '${ctx }/qianchen/loadAttachments.action',
		dataType: 'json',
		data: {
			'formNum': formNum
		},
		success: function(data, status) {
			if (data.length > 0) {
				for (var i = 0; i <= data.length; i++) {
					jQuery("#attachmentlist").jqGrid('addRowData', i + 1, data[i]);
				}
			}
		}
	})
}

function keyDown(e) {
	/*
	// 禁止使用backspace键
	if(window.event.keyCode == 8){
		// alert("不能使用backspace键");
		event.returnValue=false;
	}
	// 后面还可以禁止其它键，照着上面的方法写就行了
	// 比如：if(event.shiftKey&&event.keyCode == 121) // 屏蔽shift+F10
	*/

}

//更换核决主管时，清空会办单位
$(document).ready(function() {
	//$("input[name=leader]").click(function() {
	//		$("#huibanDept").val("");
	// });
});

//内部会办判断重复
function neibuhuibanCheck(innerhuibanNames, innerhuiban) {
	if (innerhuibanNames && innerhuibanNames.indexOf(innerhuiban) >= 0) {
		if (innerhuiban == "") alert("请选择人员后添加。")
		else alert("请勿重复添加。");
		return true;
	}
	return false;
}

function leaderClick() {
	leader = $('input:radio[name="leader"]:checked').val();
}

//总部员工不允许选“地方到总部”
function judgeDftoZb() {
	//$("#dftozb").parent().val("");
	if ($("#actualDept").val()) {
		if ($("#actualDept").val().indexOf("神旺控股总部") < 0) {
			$("#dftozb").removeAttr("disabled");
			//$("#dftozb").prev().attr("selected", "selected");
		} else {
			$("#dftozb").attr("disabled", "disabled");
			$("#dftozb").prev().attr("selected", "selected");
		}
	}
}

$(document).ready(function() {
	judgeDftoZb();
});

/**
 * 　屏蔽退格键返回上一页
 */
// 处理键盘事件 禁止后退键（Backspace）密码或单行、多行文本框除外
function forbidBackSpace(e) {
	var ev = e || window.event; // 获取event对象
	var obj = ev.target || ev.srcElement; // 获取事件源
	var t = obj.type || obj.getAttribute('type'); // 获取事件源类型
	// 获取作为判断条件的事件类型
	var vReadOnly = obj.readOnly;
	var vDisabled = obj.disabled;
	// 处理undefined值情况
	vReadOnly = (vReadOnly == undefined) ? false : vReadOnly;
	vDisabled = (vDisabled == undefined) ? true : vDisabled;
	// 当敲Backspace键时，事件源类型为密码或单行、多行文本的，
	// 并且readOnly属性为true或disabled属性为true的，则退格键失效
	var flag1 = ev.keyCode == 8 && (t == "password" || t == "text" || t == "textarea") && (vReadOnly == true || vDisabled == true);
	// 当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效
	var flag2 = ev.keyCode == 8 && t != "password" && t != "text" && t != "textarea";
	// 判断
	if (flag2 || flag1) return false;
}
// 禁止后退键 作用于Firefox、Opera
document.onkeypress = forbidBackSpace;
// 禁止后退键 作用于IE、Chrome
document.onkeydown = forbidBackSpace;

function printForm(formid, type) {
	var s = "";
	try{
		var sysFlg_hid = $("#hid_sysFlg").val();
		if (sysFlg_hid == "F"){
			s = "D";
		}
	}catch(e){}
	var su = "http://oatemp.sanwant.com.cn/report/jsp/HR_Print/";
	var x = "";
	if (type == "qc"){
		x = "qiancheng" + s + ".jsp"; 
	} else if (type == "nl"){
		x = "neilian" + s + ".jsp";
	} else {
		return;
	}
	window.open(su + x + "?fileid=" + formid);
}
</script>