$(function(){
	$(".rule-single-checkbox").ruleSingleCheckbox();
	$(".rule-multi-checkbox").ruleMultiCheckbox();
	$(".rule-multi-radio").ruleMultiRadio();
	$(".rule-single-select").ruleSingleSelect();
});

//复选框
$.fn.ruleSingleCheckbox = function () {
    var singleCheckbox = function (parentObj) {
        //查找复选框
        var checkObj = parentObj.children('input:checkbox').eq(0);
        parentObj.children().hide();
        //添加元素及样式
        var newObj = $('<a href="javascript:;">'
		+ '<i class="off">否</i>'
		+ '<i class="on">是</i>'
		+ '</a>').prependTo(parentObj);
        parentObj.addClass("single-checkbox");
        //判断是否选中
        if (checkObj.prop("checked") == true) {
            newObj.addClass("selected");
        }
		//检查控件是否启用
		if(checkObj.prop("disabled") == true){
			newObj.css("cursor","default");
			return;
		}
        //绑定事件
        $(newObj).click(function () {
            if ($(this).hasClass("selected")) {
                $(this).removeClass("selected");
            } else {
                $(this).addClass("selected");
            }
            checkObj.trigger("click"); //触发对应的checkbox的click事件
        });
    };
    return $(this).each(function () {
        singleCheckbox($(this));
    });
};

//多项复选框
$.fn.ruleMultiCheckbox = function() {
	var multiCheckbox = function(parentObj){
		parentObj.addClass("multi-checkbox"); //添加样式
		parentObj.children().hide(); //隐藏内容
		var divObj = $('<div class="boxwrap"></div>').prependTo(parentObj); //前插入一个DIV
		parentObj.find(":checkbox").each(function(){
			var indexNum = parentObj.find(":checkbox").index(this); //当前索引
			var newObj = $('<a href="javascript:;">' + parentObj.find('label').eq(indexNum).text() + '</a>').appendTo(divObj); //查找对应Label创建选项
			if($(this).prop("checked") == true){
				newObj.addClass("selected"); //默认选中
			}
			//检查控件是否启用
			if($(this).prop("disabled") == true){
				newObj.css("cursor","default");
				return;
			}
			//绑定事件
			$(newObj).click(function(){
				if($(this).hasClass("selected")){
					$(this).removeClass("selected");
				}else{
					$(this).addClass("selected");
				}
				parentObj.find(':checkbox').eq(indexNum).trigger("click"); //触发对应的checkbox的click事件
			});
		});
	};
	return $(this).each(function() {
		multiCheckbox($(this));
	});
}

//多项单选
$.fn.ruleMultiRadio = function() {
	var multiRadio = function(parentObj){
		parentObj.addClass("multi-radio"); //添加样式
		parentObj.children().hide(); //隐藏内容
		var divObj = $('<div class="boxwrap"></div>').prependTo(parentObj); //前插入一个DIV
		parentObj.find('input[type="radio"]').each(function(){
			var indexNum = parentObj.find('input[type="radio"]').index(this); //当前索引
			var newObj = $('<a href="javascript:;">' + parentObj.find('label').eq(indexNum).text() + '</a>').appendTo(divObj); //查找对应Label创建选项
			if($(this).prop("checked") == true){
				newObj.addClass("selected"); //默认选中
			}
			//检查控件是否启用
			if($(this).prop("disabled") == true){
				newObj.css("cursor","default");
				return;
			}
			//绑定事件
			$(newObj).click(function(){
				$(this).siblings().removeClass("selected");
				$(this).addClass("selected");
				parentObj.find('input[type="radio"]').prop("checked",false);
				parentObj.find('input[type="radio"]').eq(indexNum).prop("checked",true);
				parentObj.find('input[type="radio"]').eq(indexNum).trigger("click"); //触发对应的radio的click事件
			});
		});
	};
	return $(this).each(function() {
		multiRadio($(this));
	});
}

//单选下拉框
$.fn.ruleSingleSelect = function () {
    var singleSelect = function (parentObj) {
        parentObj.addClass("single-select"); //添加样式
        parentObj.children().hide(); //隐藏内容
        var divObj = $('<div class="boxwrap"></div>').prependTo(parentObj); //前插入一个DIV
        //创建元素
        var titObj = $('<a class="select-tit" href="javascript:;"><span></span><i></i></a>').appendTo(divObj);
        var itemObj = $('<div class="select-items"><ul></ul></div>').appendTo(divObj);
        var arrowObj = $('<i class="arrow"></i>').appendTo(divObj);
        var selectObj = parentObj.find("select").eq(0); //取得select对象
        //遍历option选项
        selectObj.find("option").each(function (i) {
            var indexNum = selectObj.find("option").index(this); //当前索引
            var liObj = $('<li>' + $(this).text() + '</li>').appendTo(itemObj.find("ul")); //创建LI
            if ($(this).prop("selected") == true) {
                liObj.addClass("selected");
                titObj.find("span").text($(this).text());
            }
            //检查控件是否启用
            if ($(this).prop("disabled") == true) {
                liObj.css("cursor", "default");
                return;
            }
            //绑定事件
            liObj.click(function () {
                $(this).siblings().removeClass("selected");
                $(this).addClass("selected"); //添加选中样式
                selectObj.find("option").prop("selected", false);
                selectObj.find("option").eq(indexNum).prop("selected", true); //赋值给对应的option
                titObj.find("span").text($(this).text()); //赋值选中值
                arrowObj.hide();
                itemObj.hide(); //隐藏下拉框
                selectObj.trigger("change"); //触发select的onchange事件
            });
        });
        //检查控件是否启用
        if (selectObj.prop("disabled") == true) {
            titObj.css("cursor", "default");
            return;
        }
        //绑定单击事件
        titObj.click(function (e) {
            e.stopPropagation();
            if (itemObj.is(":hidden")) {
                //隐藏其它的下位框菜单
                $(".single-select .select-items").hide();
                $(".single-select .arrow").hide();
                //位于其它无素的上面
                arrowObj.css("z-index", "1");
                itemObj.css("z-index", "1");
                //显示下拉框
                arrowObj.show();
                itemObj.show();
            } else {
                //位于其它无素的上面
                arrowObj.css("z-index", "-1");
                itemObj.css("z-index", "-1");
                //隐藏下拉框
                arrowObj.hide();
                itemObj.hide();
            }
        });
        //绑定页面点击事件
        $(document).click(function (e) {
            selectObj.trigger("blur"); //触发select的onblure事件
            arrowObj.hide();
            itemObj.hide(); //隐藏下拉框
        });
    };
    return $(this).each(function () {
        singleSelect($(this));
    });
}