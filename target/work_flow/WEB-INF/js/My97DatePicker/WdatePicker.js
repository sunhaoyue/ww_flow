/*
 * My97 DatePicker 4.72 Release
 * License: http://www.my97.net/dp/license.asp
 */
var $dp,WdatePicker;
(function(){	
	//初始化动态变量
	var Config = {
		//[静态属性]是否自动引入Wdate类 默认为是 设置为是时,可直接在引入WdatePicker.js的页面里使用 class="Wdate"
		$wdate: true,
		//[静态属性]是否显示指定程序包的绝对位置,一般情况下为空即可(程序自动创建),该属性是为防止极其少数的情况下程序创建出错而设置的
		//程序包的绝对位置 注意,要用/开头 用/结尾 
		//默认为空, 为空时,程序自动创建
		//例如:程序包在http中的地址为 http://localhost/proName/My97DatePicker/
		//则 $dpPath = '/proName/My97DatePicker/';
		$dpPath: '',
		//[静态属性]是否跨框架
		$crossFrame: true,
		
		//双月显示
		doubleCalendar: false,
		//键盘控制开关
		enableKeyboard: true,
		//是否启用输入掩码功能
		enableInputMask: true,
		
		//在修改年月日时分秒等元素时,自动更新到el,默认是关闭的(即:需要点击确定或点击日期才更新)
		autoUpdateOnChanged: null,
		//哪一天作为,一年的第一周
		//常见算法有三种
		//1. ISO8601:规定第一个星期四为第一周,默认值: 4
		//2. MSExcel:1月1日所在的周: 可以填写: 7
		//3. 每年的第一个星期X作为第一周,可以填写: X
		whichDayIsfirstWeek: 4,
		
		//------------------
		//以下参数可以在 WdatePicker 函数里面传入,如果未定义,则使用配置值
		//------------------
		//日期选择框显示位置
		//注意:坐标默认单位是px,是相对当前框架坐标(不受滚动条影响),left属性只接受数字,top属性除接受数字外还可以接受 'above' 上方显示, 'under' 下方显示, 'auto' 系统根据页面大小自动选择(默认)
		//如:
		//{left:100,top:50}表示固定坐标[100,50]
		//{top:50}表示横坐标自动生成,纵坐标指定为 50
		//{left:100}表示纵坐标自动生成,横坐标指定为 100
		//{top:'above'}表示上方显示
		//{top:'under'}表示下方显示
		position: {},
		//默认语言,当值为'auto'时自动根据客户端浏览器的语言自动选择语言 
		//语言的相关配置请参考config.js
		lang: 'auto',
		//皮肤名称 默认自带 default和whyGreen两个皮肤
		//另外如果你css够强的话,可以自己做皮肤,如果您愿意,我可以把您的皮肤集成到下载包里
		//皮肤的相关配置请参考config.js
		skin: 'default',
		
		//日期显示格式
		dateFmt: 'yyyy-MM-dd',
		//日期格式[一般情况下不需要修改](RealValue)	
		realDateFmt: 'yyyy-MM-dd',
		//时间格式[一般情况下不需要修改](RealValue)	
		realTimeFmt: 'HH:mm:ss',
		//时间日期格式[一般情况下不需要修改](RealValue)
		realFullFmt: '%Date %Time',
		//最小日期
		minDate: '1900-01-01 00:00:00',
		//最大日期
		maxDate: '2099-12-31 23:59:59',
		//起始日期,既点击日期框时显示的起始日期 
		startDate: '',
		//false时(默认值): 表示仅当文本框的值为空时才使用startDate 作为起始日期
		//true时: 总是使用 startDate 作为起始日期
		alwaysUseStartDate: false,
		//年份差量
		yearOffset: 1911,
		
		//------------------
		//显示选项
		//------------------
		//周的第一天 0表示星期日 1表示星期一
		firstDayOfWeek: 0,
		//是否显示周
		isShowWeek: false,
		//是否高亮显示 周六 周日
		highLineWeekDay: true,
		//是否显示清空按钮
		isShowClear: true,
		//是否显示今天按钮
		isShowToday: true,
		//是否显示OK按钮
		isShowOK: true,
		//第一行空白处显示上月的日期，末行空白处显示下月的日期
		isShowOthers: true,
		//是否只读
		readOnly: false,
		
		//纠错模式设置 可设置3中模式 0 - 提示 1 - 自动纠错 2 - 标记
		errDealMode: 0,
		//为false时 点日期的时候不自动输入,而是要通过确定才能输入
		//为true时 即点击日期即可返回日期值
		//为null时(推荐使用) 如果有时间置为false 否则置为true
		autoPickDate: null,
		//是否启用快速选择功能
		qsEnabled: true,
		//是否自动显示快速选择
		autoShowQS: false,
		
		//特殊日期,高亮显示
		specialDates: null,
		//特殊天 可以表示为 0,1,2,3,4,5,6 分别代表周日到周六
		specialDays: null,
		//无效日期
		disabledDates: null,
		//无效天 可以表示为 0,1,2,3,4,5,6 分别代表周日到周六
		disabledDays: null,
		//为true时 无效天和无效日期变成有效天和有效日期
		opposite: false,
		
		//------------------
		//事件
		//------------------
		onpicking: null,
		onpicked: null,
		onclearing: null,
		oncleared: null,
		
		ychanging: null,
		ychanged: null,
		Mchanging: null,
		Mchanged: null,		
		dchanging: null,
		dchanged: null,
		Hchanging: null,
		Hchanged: null,
		mchanging: null,
		mchanged: null,
		schanging: null,
		schanged: null,
		
		//--------------------------
		//以下代码请勿妄动
		//--------------------------
		
		//控件的容器,当eCont非空时,将以flat模式直接将日期框显示在日期上
		eCont: null,
		
		//存储真实值的element,必须有value属性
		vel: null,
			
		//错误消息
		errMsg: '',
		//快速选择的数据
		quickSel: [],
		//只是是否有时间的某部分对象
		has: {}
	};
	
	WdatePicker = main;
	var w = window, d = 'document', de = 'documentElement', tag = 'getElementsByTagName', dptop, jsPath, $IE, $FF, $OPERA;
	
	//初始化浏览器
	
	switch (navigator.appName) {
		case 'Microsoft Internet Explorer':
			$IE = true;
			break;
		case 'Opera':
			$OPERA = true;
			break;
		default://FF or Safari
			$FF = true;
			break;
	}
	//jsPath 库文件的路径
	jsPath = getJsPath();
	
	//引入默认的Css类    
	if (Config.$wdate) {
		//添加CSS Wdate 和 日期错误时的样式
		loadCSS(jsPath + 'skin/WdatePicker.css');
	}
	
	//dptop
	dptop = w;
	if (Config.$crossFrame) {
		//找到除FrameSet外的 最顶层的窗体
		try {
			while (dptop.parent && dptop.parent[d] != dptop[d] && dptop.parent[d][tag]('frameset').length == 0) {
				dptop = dptop.parent;
			}
		} 
		catch (e) {
			//发生了跨域访问,所以会错误,直接忽略,因为此时dptop取到了,不跨域情况下最顶层的window对象		
		}
	}
	
	//预载
	if (!dptop.$dp) {
		dptop.$dp = {
			ff: $FF,
			ie: $IE,
			opera: $OPERA,
			el: null,
			win: w,
			//0 unloaded
			//1 loading
			//2 loaded
			status: 0,
			defMinDate: Config.minDate,
			defMaxDate: Config.maxDate,
			//平面模式时,按照队列一个一个载入
			flatCfgs:[]
		};		
	}
	
	//载入$dp	
	initTopDP();
	
	if ($dp.status == 0) {
		callback(w, function(){
			main(null, true);
		});
	}
		
	//添加事件
	if (!w[d].docMD) {
		dpAttachEvent(w[d], 'onmousedown', disposeDP);
		w[d].docMD = true;
	}
	if (!dptop[d].docMD) {
		dpAttachEvent(dptop[d], 'onmousedown', disposeDP);
		dptop[d].docMD = true;
	}
	dpAttachEvent(w, 'onunload', function(){
		if ($dp.dd) {
			display($dp.dd, "none");
		}
	});
	//end 主函数
	
	//内部函数	
	function initTopDP(){
		dptop.$dp = dptop.$dp || {};
		
		obj = {			
			$: function(el){
				return (typeof el == 'string') ? w[d].getElementById(el) : el;
			},
			$D: function(id, arg){
				return this.$DV(this.$(id).value, arg);
			},
			$DV: function(v, arg){
				if (v != '') {
					this.dt = $dp.cal.splitDate(v, $dp.cal.dateFmt);
					if (arg) {
						for (var p in arg) {
							if (this.dt[p] === undefined) {
								this.errMsg = 'invalid property:' + p;
							}
							else {
								this.dt[p] += arg[p];
								if (p == 'M') {
									//获取本月的天数,并加到天属性中,已防止月份逢2月或大小月会有些误差
									var offset = arg['M'] > 0 ? 1 : 0;
									var tmpday = new Date(this.dt['y'], this.dt['M'], 0).getDate();
									this.dt['d'] = Math.min(tmpday + offset, this.dt['d']);
								}
							}
						}
					}
					if (this.dt.refresh()) {
						return this.dt;
					}
				}
				return '';
			},
			show: function(){
				// 获取最大的z-Index
				var divs = dptop[d].getElementsByTagName('div'), maxZIndex = 1e5;
				for (var i = 0; i < divs.length; i++) {
					var curZ = parseInt(divs[i].style.zIndex);
					if (curZ > maxZIndex) {
						maxZIndex = curZ;
					}
				}
				this.dd.style.zIndex = maxZIndex + 2;
				display(this.dd, "block");
			},
			hide: function(){
				display(this.dd, "none");
			},
			attachEvent: dpAttachEvent
		};
		for(var p in obj){
			dptop.$dp[p] = obj[p];
		}
		$dp = dptop.$dp;
		$dp.dd = dptop[d].getElementById('_my97DP');
	}
	
	//添加事件
	function dpAttachEvent(o, sType, fHandler){
		if ($IE) {
			o.attachEvent(sType, fHandler);
		}
		else if (fHandler) {			
			var shortTypeName = sType.replace(/on/, "");
			fHandler._ieEmuEventHandler = function(e){
				return fHandler(e);
			};
			o.addEventListener(shortTypeName, fHandler._ieEmuEventHandler, false);
		}
	}
	
	//创建jsPath
	function getJsPath(){
		var path, tmp, scripts = w[d][tag]("script");
		for (var i = 0; i < scripts.length; i++) {
			//截去wdatepicker.js以后的部分
			path = scripts[i].src.substring(0, scripts[i].src.toLowerCase().indexOf('wdatepicker.js'));
			//截去 最后一个 / 以后的部分,因为要支持类似cn_wdatepicker.js的配置
			var tmp = path.lastIndexOf("/");
			
			if (tmp > 0) 
				path = path.substring(0, tmp + 1);
			
			if (path) 
				break;
			
		}
		return path;
	}
	
	//得到库文件的路径 path = jsPath 在载入的时候就已经得到了
	function createDPPath(path){
		var a, b;
		if (path.substring(0, 1) != "/" && path.indexOf('://') == -1) {/* 说明是相对路径 */
			a = dptop.location.href;
			b = location.href;
			if (a.indexOf('?') > -1) 
				a = a.substring(0, a.indexOf('?'));
			
			if (b.indexOf('?') > -1) 
				b = b.substring(0, b.indexOf('?'));
			
			//alert('path:' + path + '\ndptop: ' + a + '\nwin: ' + b);
			var aa,bb,al = '', bl = '', bls = '', i, j, s = '';
			for (i = 0; i < Math.max(a.length, b.length); i++) {
				aa = a.charAt(i).toLowerCase();
				bb = b.charAt(i).toLowerCase();
				if (aa == bb) {
					if (aa == '/') 
						j = i;
				}
				else {
					al = a.substring(j + 1, a.length);
					al = al.substring(0, al.lastIndexOf('/'));
					bl = b.substring(j + 1, b.length);
					bl = bl.substring(0, bl.lastIndexOf('/'));
					//alert('al:'+al+'\nbl:'+bl)
					break;
				}
			}
			if (al != '') {
				for (i = 0; i < al.split('/').length; i++) {
					s += "../";
				}
			}
			if (bl != '') 
				s += bl + '/';
			
			path = a.substring(0, a.lastIndexOf('/') + 1) + s + path;
		}
		//alert(path);
		Config.$dpPath = path;
	}
	
	function loadCSS(path, title, charset){
		var head = w[d][tag]('HEAD').item(0),style = w[d].createElement('link');
		if (head) {
			style.href = path;
			style.rel = 'stylesheet';
			style.type = 'text/css';
			if (title) 
				style.title = title;
			
			if (charset) 
				style.charset = charset;
			
			head.appendChild(style);
		}
	}
	
	function callback(o, func){
		dpAttachEvent(o, 'onload', func);
	}
	
	function getAbsM(w){
		w = w || dptop;
		var lm = 0, tm = 0;
		while (w != dptop) {
			var ifs = w.parent[d][tag]('iframe');
			for (var i = 0; i < ifs.length; i++) {
				//在IE中子框架不能操作同级子框架的document,所以加上try 
				try {
					if (ifs[i].contentWindow == w) {
						var rc = getBound(ifs[i]);
						lm += rc.left;
						tm += rc.top;
						break;
					}
				} 
				catch (e) {
				}
			}
			w = w.parent;
		}
		return {
			'leftM': lm,
			'topM': tm
		};
	}
	
	//这是一个很好的函数
	//可以单独抠出来使用
	function getBound(o){
		if (o.getBoundingClientRect) {
			return o.getBoundingClientRect();
		}
		else {
			var patterns = {
				ROOT_TAG: /^body|html$/i, // body for quirks mode, html for standards,
				OP_SCROLL: /^(?:inline|table-row)$/i
			};
			
			var hssFixed = false, win = null, t = o.offsetTop, l = o.offsetLeft, r = o.offsetWidth, b = o.offsetHeight;
			
			var parentNode = o.offsetParent;
			if (parentNode != o) {
				while (parentNode) {
					l += parentNode.offsetLeft;
					t += parentNode.offsetTop;
					
					if(getStyle(parentNode,'position').toLowerCase() == 'fixed')
						hssFixed = true;
					else if (parentNode.tagName.toLowerCase() == "body") 
						win = parentNode.ownerDocument.defaultView;
					
					parentNode = parentNode.offsetParent;
				}
			}
			
			parentNode = o.parentNode;
			while (parentNode.tagName && !patterns.ROOT_TAG.test(parentNode.tagName)) {
				if (parentNode.scrollTop || parentNode.scrollLeft) {
					if (!patterns.OP_SCROLL.test(display(parentNode))) {
						if (!$OPERA || parentNode.style.overflow !== 'visible') { 
							l -= parentNode.scrollLeft;
							t -= parentNode.scrollTop;
						}
					}
				}
				parentNode = parentNode.parentNode;
			}
			
			if (!hssFixed) {
				var scr = getScroll(win);
				l -= scr.left;
				t -= scr.top;
			}
			r += l;
			b += t;
			
			return {
				'left': l,
				'top': t,
				'right': r,
				'bottom': b
			};
		}
	}
	
	//获得浏览器可用尺寸
	function getWH(w){
		w = w || dptop;
		var doc = w[d],
		    width = (w.innerWidth) ? w.innerWidth : (doc[de] && doc[de].clientWidth) ? doc[de].clientWidth : doc.body.offsetWidth,
            height = (w.innerHeight) ? w.innerHeight : (doc[de] && doc[de].clientHeight) ? doc[de].clientHeight : doc.body.offsetHeight; 
		//alert(doc.clientHeight);
		//alert('width:' + width + '\nheight:' + height);
		return {
			'width': width,
			'height': height
		};
	}
	
	//取得scrollTop 和 scrollLeft 兼容html xhtml1.0
	function getScroll(w){
		w = w || dptop;
		var doc = w[d], doce = doc[de], db = doc.body;
		//必须这样做,不可用nodeType的方法
		doc = (doce && doce.scrollTop != null && (doce.scrollTop > db.scrollTop || doce.scrollLeft > db.scrollLeft)) ? doce : db;
		//alert('Left:' + doc.scrollLeft + '\nTop:' + doc.scrollTop);
		return {
			'top': doc.scrollTop,
			'left': doc.scrollLeft
		};
	}
	
	//隐藏日期框
	function disposeDP(e){
		//在其他框架单击时 IE中会把event传入给e,ff则可以直接取得event对象
		var src = e ? (e.srcElement || e.target) : null;
		
		//单击在日期框时,不会响应本事件,因为是iframe
		try {
			if ($dp.cal && !$dp.eCont && $dp.dd && src != $dp.el && $dp.dd.style.display == 'block') {
				$dp.cal.close();
			}
		}catch(e){}
	}
	
	//DatePicker载入完毕时
	function dpLoaded(){
		$dp.status = 2;
		
		loadFlat();
	}
	
	function loadFlat(){		
		//开始载入平面模式的控件,载入完成后,callback事件会调用下一次载入
		if ($dp.flatCfgs.length > 0) {			
			var cfg = $dp.flatCfgs.shift();
			cfg.el = {
				innerHTML: ''
			};			
			cfg.autoPickDate = true;
			cfg.qsEnabled = false;
			showPicker(cfg);
		}
	}
	
	var isDptopReady,dptopInterval
	function main(cfg, preLoad){		
		//一定要放最前面,否则很多函数的操作对象都不是当前文档;
		$dp.win = w;
		
		initTopDP();
		
		cfg = cfg ||
		{};
		//第一次载入
		if (preLoad) {
			if (!dptopReady()) {
				dptopInterval = dptopInterval ||
				setInterval(function(){
					//loadTime+=50;
					if (dptop[d].readyState == 'complete') {
						clearInterval(dptopInterval);
					//alert('作者在调试中:\ntime:'+loadTime+'\nstatus:'+$dp.status+'\nurl:'+w.location.href);
					}
					
					main(null, true);
				}, 50);
				
				return;
			}
			
			//说明是第一次预载
			if ($dp.status == 0) {
				//状态置为 loading
				$dp.status = 1;
				//开始载入
				showPicker({
					el: {
						innerHTML: ''
					}
				}, true);
			}
			else {
				//正在预载或者预载已经成功
				return;
			}
		}
		else 
			if (cfg.eCont) {
				//平面模式	
				//平面模式下,肯定是在当前页面,所以要将当前窗口cfg.eCont,使用获取到真正的对象
				cfg.eCont = $dp.$(cfg.eCont);
				$dp.flatCfgs.push(cfg);
				if ($dp.status == 2) {
					loadFlat();
				}
			}
			else {
				if ($dp.status == 0) {
					//有时预载会失败,或在预载前触发了日期框事件,则手工预载
					main(null, true);
					return;
				}
				if ($dp.status != 2) {
					//当没有预载完毕时直接退出
					return;
				}
				
				//找到触发WdatePicker的对象
				var evt = SearchEvent();
				//$dp.srcEl表示触发WdatePicker的对象
				if (evt) {
					$dp.srcEl = evt.srcElement || evt.target;
					//cancelBubble后就不触发 disposeDP 事件了
					evt.cancelBubble = true;
				}
				
				$dp.el = cfg.el = $dp.$(cfg.el || $dp.srcEl);
				
				//属性disabled为true时 或 同一控件点击时  不弹出日期框				
				if (!$dp.el || $dp.el['My97Mark'] === true || $dp.el.disabled || ($dp.el == $dp.el && display($dp.dd) != 'none' && $dp.dd.style.left != '-1970px')) {
					//alert($dp.dd.style.cssText+'\ncfg.el && cfg.el.disabled :'+(cfg.el && cfg.el.disabled)+'\ncfg.el == $dp.el : '+(cfg.el == $dp.el)+'\n$dp.dd.style.display!=none :'+($dp.dd.style.display != 'none'))
					$dp.el['My97Mark'] = false;
					return;
				}
				showPicker(cfg);
				
				if (evt && $dp.el.nodeType == 1 && $dp.el['My97Mark'] === undefined) {				
					$dp.el['My97Mark'] = false;
					
					var evt1,evt2;
					if (evt.type == 'focus') {
						evt1 = 'onclick';
						evt2 = 'onfocus';
					}
					else {
						evt1 = 'onfocus';
						evt2 = 'onclick';
					}
						
					//根据事件类型添加
					dpAttachEvent($dp.el, evt1, $dp.el[evt2]);										
				}				
			}
			
		function dptopReady(){
			if ($IE && dptop != w && dptop[d].readyState != 'complete') 
				return false;
			return true;
		}
		
		//这是一个很好的函数
		//可以单独抠出来使用
		function SearchEvent(){
			if ($FF) {
				func = SearchEvent.caller;
				while (func != null) {
					var arg0 = func.arguments[0];
					if (arg0 && (arg0 + '').indexOf('Event') >= 0) {
						return arg0;
					}
					func = func.caller;
				}
				return null;
			}
			return event;
		}
	}
	
	//返回最终样式函数，兼容IE和DOM，设置参数：元素对象、样式特性 
	function getStyle(obj, attribute){
		return obj.currentStyle ? obj.currentStyle[attribute] : document.defaultView.getComputedStyle(obj, false)[attribute];
	}
		
	//获取或设置样式
	function display(obj, value){
		if (obj) {
			if (value != null) 
				obj.style.display = value;
			else 
				return getStyle(obj, 'display');
		}
	}
	
	function showPicker(cfg, preLoad){
		//从默认模板载入数据
		for (var p in Config) {
			if (p.substring(0, 1) != '$') {
				$dp[p] = Config[p];
			}
		}		
		//把cfg的值赋值给$dp
		for (var p in cfg) {
			if ($dp[p] !== undefined) 
				$dp[p] = cfg[p];
			//取消提示	$dp.errMsg = 'invalid property:' + p;
		}
		
		var nodeName = $dp.el ? $dp.el.nodeName : 'INPUT';		
		//设置显示属性,即显示返回值的属性, 当控件为input时,设置为value,否则使用innerHTML
		if(preLoad || $dp.eCont || new RegExp(/input|textarea|div|span|p|a/ig).test(nodeName)){
			$dp.elProp = nodeName == 'INPUT' ? 'value' : 'innerHTML';
		}
		else
			//不合法的el
			return;
				
		//根据语言设置选择语言
		if ($dp.lang == 'auto') {
			$dp.lang = $IE ? navigator.browserLanguage.toLowerCase() : navigator.language.toLowerCase();
		}
		
		//不存在$dp.dd 或者 平面模式 或者 语言与上次的不同而且改语言是能在语言列表中找到的
		if (!$dp.dd || $dp.eCont || ($dp.lang && $dp.realLang && $dp.realLang.name != $dp.lang && $dp.getLangIndex && $dp.getLangIndex($dp.lang)>=0)) {
			//如果原来存在这个节点,则先移除
			if ($dp.dd && !$dp.eCont) {
				dptop[d].body.removeChild($dp.dd);
			}
			
			//产生iframe的HTML
			//设置$dpPath 为空时,调用createDPPath自动创建
			if (Config.$dpPath == '') 
				createDPPath(jsPath);
			var ifrHTML = '<iframe style="width:1px;height:1px" src="' + Config.$dpPath + 'My97DatePicker.htm" frameborder="0" border="0" scrolling="no"></iframe>';
			
			if ($dp.eCont) {
				$dp.eCont.innerHTML = ifrHTML;
				//载入下一个flat模式的控件
				callback($dp.eCont.childNodes[0], dpLoaded);
			}
			else {
				$dp.dd = dptop[d].createElement("DIV");
				$dp.dd.id = '_my97DP';
				$dp.dd.style.cssText = 'position:absolute';
				$dp.dd.innerHTML = ifrHTML;
				
				//将对象添加到dptop
				dptop[d].body.appendChild($dp.dd);
				//dptop[d].body.insertBefore($dp.dd, dptop[d].body.firstChild);
				//});
				//在iframe load 完毕时 设置$dp.status
				callback($dp.dd.childNodes[0], dpLoaded);
				
				if (preLoad) {
					//说明是预载 如果不设置top 在ie中会加长页面
					$dp.dd.style.left = $dp.dd.style.top = '-1970px';
				}
				else {
					$dp.show();
					//多语言情况下
					setPos();
				}
			}
		}
		else if ($dp.cal) {
			//显示日期选择框,必须放init前面,否则不能取道offsetWidth和Top的值,对qsDivSel有影响
			$dp.show();
			//控件初始化
			$dp.cal.init();
			//设置位置
			if (!$dp.eCont) setPos();
		}
		
		//根据$dp.position设置位置
		function setPos(){
			var l = $dp.position.left, t = $dp.position.top, el = $dp.el;
			
			//设置参考对象
			//如果是指定el的,el与srcEl不一样,且el是hidden或者display:none的情况下,使用srcEl作为参考对象			
			if(el != $dp.srcEl && (display(el)=='none' || el.type == 'hidden'))
				el = $dp.srcEl;
			
			
			//设置显示位置
			var objxy = getBound(el),
			//alert('objxy.left:'+objxy.left+'\nobjxy.right:'+objxy.right+'\nobjxy.top:'+objxy.top+'\nobjxy.bottom'+objxy.bottom);
			//左边距和上边距
			mm = getAbsM(w),
			/* 获得winsize */
			currWH = getWH(dptop),
			//获得 scrollTop 和 scrollLeft			
			scr = getScroll(dptop), ddHeight = $dp.dd.offsetHeight, ddWidth = $dp.dd.offsetWidth;
			
			// top
			if (isNaN(t)) {
				if (t == 'above' || (t != 'under' && ((mm.topM + objxy.bottom + ddHeight > currWH.height) && (mm.topM + objxy.top - ddHeight > 0)))) {
					t = scr.top + mm.topM + objxy.top - ddHeight - 2;
				}
				else {
					t = scr.top + mm.topM + Math.min(objxy.bottom, currWH.height - ddHeight) + 2;
				}
			}
			else {
				t += scr.top + mm.topM;
			}
			
			//left
			if (isNaN(l)) {
				l = scr.left + Math.min(mm.leftM + objxy.left, currWH.width - ddWidth - 5) - ($IE ? 2 : 0);
			}
			else {
				l += scr.left + mm.leftM;
			}
						
			$dp.dd.style.top = t + 'px';
			$dp.dd.style.left = l + 'px';
		}
	}
})();
