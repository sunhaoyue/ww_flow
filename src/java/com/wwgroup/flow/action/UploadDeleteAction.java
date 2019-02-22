package com.wwgroup.flow.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.common.action.BaseAjaxAction;
import com.wwgroup.common.vo.VOUtils;
import com.wwgroup.flow.bo.FlowAttachment;
import com.wwgroup.flow.service.FlowService;
import com.wwgroup.flow.service.PersonService;

@SuppressWarnings("serial")
public class UploadDeleteAction extends BaseAjaxAction {

	private FlowAttachment flow = new FlowAttachment();

	@SuppressWarnings("unused")
	private PersonService personService;

	private FlowService flowService;

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}

	@Override
	public Object getModel() {
		return this.flow;
	}

	/**
	 * 附件删除
	 * 
	 * @return
	 * @throws Exception
	 */
	public String uploadDeleteFile() {
		String id = (String) this.servletRequest.getParameter("id");
		if (StringUtils.isNotEmpty(id)) {
			this.flowService.deleteFlowAttachment(Long.valueOf(id));
		}
		FlowAttachment result = new FlowAttachment();
		result.setId(Long.valueOf(id));
//		JsonConfig config = new JsonConfig();
//		config.setExcludes(new String[] { "attachment" });
//		String json = JSONObject.fromObject(result, config).toString();
//		createJSonData(json);
		List<FlowAttachment> results = new ArrayList<FlowAttachment>();
		results.add(result);
		String json = VOUtils.getJsonDataFromCollection(results);
		createJSonData(json);
		return AJAX;
	}

}
