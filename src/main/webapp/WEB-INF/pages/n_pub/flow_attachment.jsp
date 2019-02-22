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
               <div id="fileQueue"></div>
				<table>
               		<tr>
					<td><input name="upload" type="file" id="uploadify"/></td>     		
			   		<td><input type="image" src="../images/uploadfile.png" onclick="jQuery('#uploadify').uploadifyUpload()" id="doUpload" style="margin: 0px 10px; position: relative; top: -1px;"/></td>
					</tr>
				</table>
				<input name="operatorId" type="hidden" id="operatorId" value="<s:property value="#request.operatorId"/>"/>       
				<input name="operatorName" type="hidden" id="operatorName" value="<s:property value="#request.operatorName"/>"/>  
               <script type="text/javascript">
               		var fileCount=0;
               		var attachmentIds;
					$(document).ready(function() {
						$("#uploadify").uploadify({
							'uploader'       : '${ctx}/js/upload/uploadify.swf',
							'script'         : '${ctx}/common/upload.action',
							'buttonImg'		 : '${ctx}/js/upload/browse.png',
							'width'			 : 78,
							'height'		 : 25,
							'cancelImg'      : '${ctx}/js/upload/cancel.png',
							'queueID'        : 'fileQueue',
							'auto'           : false,
							'multi'          : true,
							'buttonText'	 : 'BROWSE',
							'queueSizeLimit' : 3,
							'fileDesc'		 : '请选择...',
							'sizeLimit'		 :20971520,
							
							'onComplete' : function(event, ID, fileObj, response, data) {
								fileCount++;
								if(attachmentIds){
									attachmentIds += ";" + response;
								}
								else {
									attachmentIds = response;
								}
								$("#attachmentIds").val(attachmentIds);

								
								var employeeId = $('#operatorId').attr("value");
							    var userName = $('#operatorName').attr("value");
							    
							    var mydata = [
			 						{attachmentName:fileObj.name,
			 						 employeeId:employeeId,
			 						 userName:userName,
			 						// createTime:fileObj.creationDate
			 						 createTime:dateTime,
			 						 isNew: 1,
			 						 attachmentId:response,
			 						 id:response
			 						 }
			 					];
							    var rowIds = $("#attachmentlist").jqGrid('getDataIDs');
							    var index = rowIds.length;
							    
						 		for(var i=0;i<=mydata.length;i++){
						 			jQuery("#attachmentlist").jqGrid('addRowData',index+1,mydata[i]);
						 		}
													
				        	},
				        	'onError' : function(event, ID, fileObj, response, data) {
								alert(fileObj.name+'上传不成功，请检查是否超过了上传大小限制!默认为20M');
				        	}
			 			})
					});

					function removeOneRow(attachmentId) {
						var rowIds = $("#attachmentlist").jqGrid('getDataIDs');
						if(rowIds){
						    for(var i = 0, j = rowIds.length; i < j; i++) {
							    var curRowData = $("#attachmentlist").jqGrid('getRowData', rowIds[i]);
								if (curRowData['id'] == attachmentId) {
									// 后台Ajax后前台删除
									$.ajax({
									    type: "POST",
									    url: '${ctx}/common/uploadDeleteFile.action',
									    dataType:'json',
									    data:{'id':attachmentId},		    
									    success: function (data,status) {
										    // $("#attachmentlist").jqGrid("delRowData", data.id); //前台删除  rowIds[i]
									    	if(data.length>0){
										       	for(var k=0;k<=data.length;k++){
										       		$("#attachmentlist").jqGrid("delRowData", rowIds[i]);
										       		return;
										  		}		        		
									       }
								    	}
						 			})
						 			return;
								}
						    }   
						}  
					}
					//下载附件
					function downloadAttachmentFile(attachmentId){
						window.location.href='${ctx}/common/download.action?id='+attachmentId;
					}
					
					//渲染操作列
					function renderOperation(cellvalue, options, rowObject) {
						var btns = [];
						var id = rowObject.id;
						
						var template = '${param.template}';
						var renew = '${param.renew}';
						if(rowObject.isNew || template || renew){
							btns.push("<input type='button' class='button blue small' value='下载' onclick='downloadAttachmentFile("+rowObject.id+")'>");
							btns.push("<input type='button' class='button blue small' value='删除' onclick='removeOneRow("+rowObject.id+")'>");
						}
						return btns.join('');
					}
			   </script>
               <table id="attachmentlist"></table></td>
               <script>
			 		jQuery("#attachmentlist").jqGrid({ 
			 			datatype: "local",
			 			autowidth:true,
			 			shrinkToFit:false,
			 			height: 100,
			 			editurl: '${ctx}/common/upload.action', // this is dummy existing url
			 			loadonce: true,
			 			colNames:['附件名称','上传者工号', '上传者姓名', '上传时间', '文件编号', '新文件标示符', '操作'], 
			 			colModel:[ {name:'attachmentName',index:'attachmentName', width:200, formatter:makeURL}, 
			 				 {name:'employeeId',index:'employeeId', width:100}, 
			 				 {name:'userName',index:'userName', width:100}, 
			 				 {name:'createTime',index:'createTime', width:120},
			 				 {name:'id',index:'id', hidden:true},
			 				 {name:'isNew',index:'isNew', hidden:true},
			 				 {name:'operation', index:'operation',width:100, align:'center', formatter:renderOperation}
			 			], 
			 			caption: "附件列表" }); 
			 	</script>
             </tr>
           </table></td>
       </tr>
	</table>
   </div>

