/*******************************************************************************
 * \u516c\u7528\u7684\u5de5\u5177\u65b9\u6cd5
 ******************************************************************************/
Array.prototype.remove = function(dx) {
	if (isNaN(dx) || dx > this.length) {
		return false;
	}
	for (var i = 0, n = 0; i < this.length; i++) {
		if (this[i] != this[dx]) {
			this[n++] = this[i]
		}
	}
	this.length -= 1
}

Array.prototype.indexOf = function(value) {
	var index = -1;
	for (var i = 0; i < this.length; i++) {
		if (this[i] == value) {
			index = i;
			break;
		}
	}
	return index;
}

Date.prototype.format = function(format, year, month, day) {
	var o = {
		"M+" : this.getMonth() + 1, // month
		"d+" : this.getDate(), // day
		"h+" : this.getHours(), // hour
		"m+" : this.getMinutes(), // minute
		"s+" : this.getSeconds(), // second
		"q+" : Math.floor((this.getMonth() + 3) / 3), // quarter
		"S" : this.getMilliseconds() // millisecond
	}
	if(month){
		o['M+'] = month;
	}
	if(day){
		o['d+'] = day;
	}
	var fullyear = this.getFullYear();
	if(year){
		fullyear = year;
	}
	if (/(y+)/.test(format))
		format = format.replace(RegExp.$1, (fullyear + "").substr(4
				- RegExp.$1.length));
	for (var k in o)
		if (new RegExp("(" + k + ")").test(format))
			format = format.replace(RegExp.$1, RegExp.$1.length == 1
					? o[k]
					: ("00" + o[k]).substr(("" + o[k]).length));
	return format;
}


