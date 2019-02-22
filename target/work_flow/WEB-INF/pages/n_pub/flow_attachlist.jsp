<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ include file="/pages/taglibs.inc" %>
<div class="contentstyle">
	<table width="100%" border="0" cellspacing="0" cellpadding="0">
		<tr>
			<td valign="top">附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</td>
			<td colspan="6"><table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td>
					<table id="attachmentlist"></table>
				</td>
					<script>
					function makeAttachURL(cellvalue, options, rowObject){
						var id = rowObject.id;
						if (rowObject.isNew == '1') {
							return cellvalue;
						} else {
							return "<a class='gridlink' href='javascript:void(0);' onclick='openAttach(\"" + id + "\")'>" + cellvalue + "</a>"
						}
					}
					function openAttach(id){
					//alert($("#formNum").attr("value"));
						var postData = {
							"aid": id,
							"formnum": $("#formNum").attr("value"),
							"stat": "A"
						};
						$.post("${ctx}/nqianchen/qianChenEditAction!insertLog.action", postData, function(data){
							var url = "${ctx }/common/download.action?id=" + id;
							window.location.href = url;
						});
					}
					jQuery("#attachmentlist").jqGrid({ 
						datatype: "local",
						autowidth:true,
						shrinkToFit:false,
						height: 100,
						//editurl: '${ctx}/common/upload.action', // this is dummy existing url
						loadonce: true,
						colNames:['附件名称','上传者工号', '上传者姓名', '上传时间', '文件编号', '新文件标示符'], 
						colModel:[ {name:'attachmentName',index:'attachmentName', width:200, formatter:makeAttachURL}, 
							{name:'employeeId',index:'employeeId', width:100}, 
							{name:'userName',index:'userName', width:100}, 
							{name:'createTime',index:'createTime', width:120},
							{name:'id',index:'id', hidden:true},
							{name:'isNew',index:'isNew', hidden:true}
						], 
						caption: "附件列表" }
					); 
					</script>
			</tr>
			</table></td>
		</tr>
	</table>
</div>