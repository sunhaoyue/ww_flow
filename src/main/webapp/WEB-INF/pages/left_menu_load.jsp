<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ include file="taglibs.inc" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>签呈申请单</title>
<link href="${ctx }/css/2.0/leftmenu.css" rel="stylesheet" type="text/css" />
</head>
<body>
<div class="left-sidebar">
	<div id="sidebar-nav" class="sidebar-nav">
		<div class="list-box">
			<div class="headbg"><a href="javascript:void(0);" onclick="javascript:parent.location.href='/portal'">返回首页</a></div>
			<ul>
				<li class="">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/search.png" width="24" alt="" />
						</i>
						<span>高级查询</span>
				</li>
				<li class="">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/comment.png" width="24" alt="" />
						</i>
						<span>待审核表单&nbsp;(<img src="${ctx }/images/loading_b.gif" width="16" alt="" />)</span>
				</li>
				<li class="">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/delicious.png" width="24" alt="" />
						</i>
						<span>暂存表单&nbsp;(<img src="${ctx }/images/loading_b.gif" width="16" alt="" />)</span>
				</li>
				<li class="">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/collaboration.png" width="24" alt="" />
						</i>
						<span>已提交表单&nbsp;(<img src="${ctx }/images/loading_b.gif" width="16" alt="" />)</span>
				</li>
				<li class="">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/document_library.png" width="24" alt="" />
						</i>
						<span>已审核表单&nbsp;(<img src="${ctx }/images/loading_b.gif" width="16" alt="" />)</span>
				</li>
				<li class="">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/upcoming_work.png" width="24" alt="" />
						</i>
						<span>抄送表单&nbsp;(<img src="${ctx }/images/loading_b.gif" width="16" alt="" />)</span>
				</li>
				<li id="qianchenglink" class="">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/plus.png" width="24" alt="" />
						</i>
						<span>签呈申请</span>
				</li>
				<li id="neilianlink" class="">
						<div class="arrow"></div>
						<i class="icon">
							<img src="${ctx }/images/2.0/plus.png" width="24" alt="" />
						</i>
						<span>内联申请</span>
				</li>
			</ul>
		</div>
	</div>
</div>
</body>
</html>
