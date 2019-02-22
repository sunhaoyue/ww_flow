<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="taglibs.inc" %>
<link href="${ctx }/css/2.0/style.css" rel="stylesheet" type="text/css" />
<link href="${ctx }/css/csspage_01.css" rel="stylesheet" type="text/css" />
<link href="${ctx }/css/csspage_03.css" rel="stylesheet" type="text/css" />

<link id="skin" rel="stylesheet" type="text/css" href="${ctx }/css/jquery-ui-1.8.21.custom.css" />
<link rel="stylesheet" type="text/css" href="${ctx }/css/ui.jqgrid.css" />
<link rel="stylesheet" href="${ctx }/css/validationEngine.jquery.css" type="text/css"/>

<%-- jquery base  --%>
<script type="text/javascript" src='${ctx }/js/jquery-1.7.1.min.js'></script>
<script type="text/javascript" src='${ctx }/js/jquery.form.js'></script>

<script type="text/javascript" src='${ctx }/js/json2.js'></script>

<%-- jquery validationEngine  --%>
<script src="${ctx }/js/validationEngine/languages/jquery.validationEngine-zh_CN.js" type="text/javascript" charset="utf-8"></script>
<script src="${ctx }/js/validationEngine/jquery.validationEngine.js" type="text/javascript" charset="utf-8"></script>

<%-- grid--%>
<script type="text/javascript" src="${ctx }/js/jqgrid/grid.locale-cn.js"></script>
<script type="text/javascript" src="${ctx }/js/jqgrid/jquery.jqGrid.min.js"></script>

<%-- dialog--%>
<script type="text/javascript" src="${ctx }/js/lhgdialog/lhgdialog.min.js?skin=iblue"></script>
<%-- date--%>
<script src="${ctx }/js/My97DatePicker/WdatePicker.js" type="text/javascript"></script>
<%-- custom--%>
<script type="text/javascript" src='${ctx }/js/custom.js'></script>
<link href="${ctx }/css/2.0/ui.jqgrid.Ex.css" rel="stylesheet" type="text/css" />

<script type="text/javascript">
//当年
var year = new Date().getFullYear();
//当月   			   
var month = new Date().getMonth() + 1;
//当天
var day = new Date().getDate();
//当天的后一周
var nextDay = day + 7;

//判断是否超过下一个月的最大天数，如果超过则扣除最大天数，月份+1
var new_date = new Date(year, month, 1); //取当年下一个月中的第一天       
var lastday = (new Date(new_date.getTime() - 1000 * 60 * 60 * 24)).getDate(); //取当年下一个月最后一天日期 
if (nextDay > lastday) {
	nextDay = nextDay - lastday;
	month++;
}

//判断是否超过了12个月，如果超过则年自动+1
if (month > 12) {
	month -= 12; //月份减        
	year++; //年份增  
}
//当前时间
var date = new Date().format('yyyy-MM-dd');

//当前时间后一个周
var nextWeekDate = new Date().format('yyyy-MM-dd', year, month, nextDay);

//提交助理表单
function sumbitAssist(saveUrl, successMsg, returnUrl) {
	$("#applySubmit").click(function() {
		var form = $("#applyForm");
		form.ajaxSubmit({
			url: saveUrl,
			type: 'post',
			dataType: "json",
			beforeSubmit: function(array, form, option) {
				if (form.validationEngine('validate') == false) {
					return false;
				};
				/* 提交遮罩 */
				form.mask ? form.mask() : null;
			},
			success: function(data) {
				alert(successMsg);
				window.location.href = returnUrl;
			}
		});
	});
}

//提交代理表单
function sumbitAgent(saveUrl, successMsg, returnUrl) {
	$("#applySubmit").click(function() {
		var form = $("#applyForm");
		form.ajaxSubmit({
			url: saveUrl,
			type: 'post',
			dataType: "json",
			beforeSubmit: function(array, form, option) {
				var reason = $('input:radio[name="agentReason"]:checked').val();
				//代理原因判断
				if (reason == "Other") {
					var realName = $("#realName").attr("value");
					if (!realName) {
						alert("请输入其他原因!");
						$("#realName").focus();
						return false;
					}
				}
				//代理人判断
				if (agentType == "PositionAgent") {
					var records = jQuery('#agentPersonlist1').jqGrid('getGridParam', 'records');
					if (records == 0) {
						alert('请先添加代理人！');
						return false;
					}
				}
				if (agentType == "FlowAgent") {
					var records = jQuery('#agentPersonlist2').jqGrid('getGridParam', 'records');
					if (records == 0) {
						alert('请先添加代理人！');
						return false;
					}
				}
				/* 提交遮罩 */
				form.mask ? form.mask() : null;
			},
			success: function(data) {
				var params = {};
				params.id = data.id;
				var rowIds1 = $("#agentPersonlist1").jqGrid('getDataIDs');
				var rowIds2 = $("#agentPersonlist2").jqGrid('getDataIDs');

				//岗位代理人信息
				if (rowIds1) {
					for (var i = 0,
					j = rowIds1.length; i < j; i++) {
						var curRowData1 = $("#agentPersonlist1").jqGrid('getRowData', rowIds1[i]);
						if (curRowData1.isNew == 1) {
							mydata1.push(curRowData1);
						}
					}
				}
				//流程代理人信息
				if (rowIds2) {
					for (var k = 0,
					l = rowIds2.length; k < l; k++) {
						var curRowData2 = $("#agentPersonlist2").jqGrid('getRowData', rowIds2[k]);
						if (curRowData2.isNew == 1) {
							mydata2.push(curRowData2);
						}
					}
				}
				params.modifiedData1 = JSON.stringify(mydata1);
				params.modifiedData2 = JSON.stringify(mydata2);

				$.ajax({
					type: "POST",
					url: '${ctx }/agent/agentAction!addAgentPerson.action',
					dataType: 'json',
					data: params,
					success: function(data, status) {
						alert(successMsg);
						window.location.href = returnUrl;
					}
				})
			}
		});
	});
}

function keyDown() {
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
</script>