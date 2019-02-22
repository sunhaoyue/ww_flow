<%@ page contentType="text/html; charset=utf-8"%>
<%@ include file="taglibs.inc" %>

<link href="${ctx }/css/csspage_01.css" rel="stylesheet" type="text/css" />
<link href="${ctx }/css/csspage_03.css" rel="stylesheet" type="text/css" />

<link id="skin" rel="stylesheet" type="text/css" href="${ctx }/css/jquery-ui-1.8.21.custom.css" />
<link rel="stylesheet" type="text/css" href="${ctx }/css/ui.jqgrid.css" />

<%-- jquery base  --%>
<script type="text/javascript" src='${ctx }/js/jquery-1.7.1.min.js'></script>
<script type="text/javascript" src='${ctx }/js/jquery.form.js'></script>

<%-- grid--%>
<script type="text/javascript" src="${ctx }/js/jqgrid/grid.locale-cn.js"></script>
<script type="text/javascript" src="${ctx }/js/jqgrid/jquery.jqGrid.min.js"></script>

<%-- date--%>
<script type="text/javascript" src="${ctx }/js/My97DatePicker/WdatePicker.js"></script>

<%-- custom--%>
<script type="text/javascript" src='${ctx }/js/custom.js'></script>
	
<link href="${ctx }/css/2.0/ui.jqgrid.Ex.css" rel="stylesheet" type="text/css" />
<link href="${ctx }/css/2.0/style.css" rel="stylesheet" type="text/css" />

<script type="text/javascript">
//当年
var year = new Date().getFullYear();
//当月
var month = new Date().getMonth() + 1;
//下一个月
var nextMonth = month + 1;
//当天
var day = new Date().getDate();
//首先判断是否超过了12个月，如果超过则年自动+1
if (nextMonth > 12) {
	nextMonth -= 12; //月份减
	year++; //年份增
}

//判断是否超过下一个月的最大天数，如果超过则扣除最大天数，月份+1
var new_date = new Date(year, nextMonth, 1); //取当年下一个月中的第一天
var lastday = (new Date(new_date.getTime() - 1000 * 60 * 60 * 24)).getDate(); //取当年下一个月最后一天日期 
if (day > lastday) {
	day = day - lastday;
	nextMonth++;
}

//再次判断是否超过了12个月，如果超过则年自动+1
if (nextMonth > 12) {
	nextMonth -= 12; //月份减
	year++; //年份增
}

//当前时间
var date = new Date().format('yyyy-MM-dd');
//当前时间后一个月
var nextMonthDate = new Date().format('yyyy-MM-dd', year, nextMonth, day);

var nowDate = new Date();
var preMonth = new Date(nowDate.setMonth(nowDate.getMonth() - 1));
var preMonthDate = preMonth.format('yyyy-MM-dd');

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