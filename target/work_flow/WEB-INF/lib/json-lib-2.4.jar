<!DOCTYPE html>
<html>
<head>
<meta id="viewport" name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>
<body>
<script type="text/javascript">
//获取url参数
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i"),
        r = decodeURIComponent(window.location.search).substr(1).match(reg);
    return r!=null ? decodeURI(r[2]) : null;
}

(function(){
	var ratio = window.devicePixelRatio || 1;
	var UA = String(navigator.userAgent).toLowerCase();
	//alert(navigator.userAgent)
	//var hasTouch = document.documentElement.ontouchstart !== undefined;
	var isMobile = UA.indexOf('mobile') > -1;
	//var isAndroid = UA.indexOf('android') > -1;
	var isIPad = UA.indexOf('ipad') > -1;
	//var isIPhone = UA.indexOf('iphone') > -1;
	//此方法误判很严重
	//var isBigScreen = Math.min(screen.height/ratio,screen.width/ratio) > 600;
	//针对点一点扫一扫，跳转结果界面
	var g_type = getUrlParam("type");
	var g_tips = getUrlParam("tips");
	var tabs = getUrlParam("tabs");
	var sms = getUrlParam("sms");
	
	//检测平台
	var p = String(navigator.platform.toLowerCase()),
		isWin = p.indexOf("win") == 0,  
		isMac = p.indexOf("mac") == 0, 
		isX11 = (p == "x11") || (p.indexOf("linux") == 0);  
		
	var template = getUrlParam("template") || "default";
	var $href = '';
	//1、not mobile //pc
	//2、mobile but ipad
	//3、big screen //android pad
	//4、检测平台
	if((isMobile && !isIPad)||!(isWin||isMac||isX11||isIPad)){
		$href = "mobile.html";
		if(tabs == "wx"){
			if (sms != 1){
				if(g_type=="logout") {
					location.href = template+"/result.html?type=success";
					return;
				} else if(g_type=="authfailed") {
					location.href = template+"/result.html?type=failure&g_tips="+g_tips;
					return;
				} else if(g_type=="authalready") {
					location.href = template+"/result.html?type=warn&g_tips="+g_tips;
					return;
				}
			}
			else
			{
				$href = "mobile_sms.html";
				location.href = template+"/"+$href+location.search;
				return;
			}
		}		
	}else{
		$href = "pc.html";
	}	
	location.href = template+"/"+$href+location.search;
})();
</script>
</body>
</html>

