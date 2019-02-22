package com.wwgroup.common.view;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;

/**
 * <!-- START SNIPPET: description --> Renders a view using the Freemarker
 * template engine.
 * <p>
 * The FreemarkarManager class configures the template loaders so that the
 * template location can be either
 * </p>
 * <ul>
 * <li>relative to the web root folder. eg <code>/WEB-INF/views/home.ftl</code></li>
 * <li>a classpath resuorce. eg <code>com/company/web/views/home.ftl</code></li>
 * </ul>
 * <!-- END SNIPPET: description --> <b>This result type takes the following
 * parameters:</b> <!-- START SNIPPET: params -->
 * <ul>
 * <li><b>location (default)</b> - the location of the template to process.</li>
 * <li><b>parse</b> - true by default. If set to false, the location param
 * will not be parsed for Ognl expressions.</li>
 * <li><b>contentType</b> - defaults to "text/html" unless specified.</li>
 * </ul>
 * <!-- END SNIPPET: params --> <b>Example:</b>
 * 
 * <pre>
 *      &lt;!-- START SNIPPET: example --&gt;
 *     
 *      &lt;result name=&quot;success&quot; type=&quot;ajax&quot;&gt;foo.jsp&lt;/result&gt;
 *     
 *      &lt;!-- END SNIPPET: example --&gt;
 * </pre> *
 * 
 * @since 1.0
 */
@SuppressWarnings("serial")
public class AjaxResult extends StrutsResultSupport {

	private static final String AJAX_SUCCESS = "{\"success\":true}";

	private static final String SUCCESS_PERFIX = "{\"success\":true,result:[";

	private static final String FAILURE_PERFIX = "{\"success\":false,result:[],";

	private static final String SUFFIX = "]}";

	private Writer writer;

	private String defaultEncoding = "UTF-8";

	@Inject(StrutsConstants.STRUTS_I18N_ENCODING)
	public void setDefaultEncoding(String encoding) {
		defaultEncoding = encoding;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.apache.struts2.dispatcher.StrutsResultSupport#doExecute(java.lang.String,
	 *      com.opensymphony.xwork2.ActionInvocation)
	 */
	@Override
	protected void doExecute(String finalLocation, ActionInvocation invocation)
			throws Exception {
		Object action = invocation.getAction();
		String responseData = "";
		if (action instanceof AjaxProvider) {
			AjaxProvider ajaxAction = (AjaxProvider) action;
			HttpServletResponse response = ServletActionContext.getResponse();
			String encoding = getEncoding(finalLocation);
			String contentType = getContentType(finalLocation);

			String successData = ajaxAction.getResponseData();

			if (encoding != null) {
				// 异步文件上传，IE下contentType非application/json
				HttpServletRequest request = ServletActionContext.getRequest();

				if (request.getContentType() != null
						&& request.getContentType().indexOf(
								"multipart/form-data;") != -1
						&& !"XMLHttpRequest".equals(request
								.getHeader("x-requested-with"))) {
					response.setCharacterEncoding(encoding);
					successData = "<textarea>" + successData + "</textarea>";
				} else {
					// normal
					contentType = contentType + ";charset=" + encoding;
					response.setContentType(contentType);
				}
			}

			if (successData != null) {
				// 成功标记，没有数据返回
				if (Action.SUCCESS.equals(successData)) {
					responseData = AJAX_SUCCESS;
				}
				// 返回数据是否已经加了成功标志(后续页面会调用js的success()方法)
				else if (ajaxAction.isAjaxSuccess()) {
					responseData = SUCCESS_PERFIX + successData + SUFFIX;
				} else {
					responseData = successData;
				}
			}
			// 业务调用中存在异常，异常拦截器设置了异常标志
			else if (ajaxAction.hasAjaxErrors()) {
				// 获取异常信息、跳转位置
				String errorResultLocation = ajaxAction
						.getErrorResultLocation();
				String exceptionMessage = invocation.getStack().findString(
						"exception.message");
				exceptionMessage = exceptionMessage.replaceAll("\r", " ");
				exceptionMessage = exceptionMessage.replaceAll("\n", " ");
				exceptionMessage = exceptionMessage.replaceAll("\t", " ");
				responseData = getFailureData(errorResultLocation,
						exceptionMessage);
			}
			getWriter().write(responseData);
		}
	}

	/**
	 * @param errorResultLocation
	 * @return
	 */
	private String getFailureData(String errorResultLocation,
			String exceptionMessage) {
		String errors = "errors:[{msg:\"" + exceptionMessage + "\"}]";
		if (StringUtils.isNotBlank(errorResultLocation)) {
			String target = ",\"target\":\"" + errorResultLocation;
			return FAILURE_PERFIX + errors + target + "\"}";
		} else {
			return FAILURE_PERFIX + errors + "}";
		}
	}

	/**
	 * @param writer
	 */
	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	/**
	 * The default writer writes directly to the response writer.
	 */
	protected Writer getWriter() throws IOException {
		if (writer != null) {
			return writer;
		}
		return ServletActionContext.getResponse().getWriter();
	}

	/**
	 * Retrieve the content type for this template. <p/> People can override
	 * this method if they want to provide specific content types for specific
	 * templates (eg text/xml).
	 * 
	 * @return The content type associated with this template (default
	 *         "application/json")
	 */
	protected String getContentType(String templateLocation) {
		return "application/json";
	}

	/**
	 * Retrieve the encoding for this template. <p/> People can override this
	 * method if they want to provide specific encodings for specific templates.
	 * 
	 * @return The encoding associated with this template (defaults to the value
	 *         of 'struts.i18n.encoding' property)
	 */
	protected String getEncoding(String templateLocation) {
		String encoding = defaultEncoding;
		if (encoding == null) {
			encoding = System.getProperty("file.encoding");
		}
		if (encoding == null) {
			encoding = "UTF-8";
		}
		return encoding;
	}
}
