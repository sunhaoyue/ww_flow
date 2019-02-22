package com.wwgroup.common.vo;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * Date类型Json转换处理器 将对象的Date类型转换成符合规格的日期时间格式
 * 
 * 
 */
public class JsonDateValueProcessor implements JsonValueProcessor {
	/**
	 * 默认的日期时间处理格式
	 */
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * 处理数组中的Date对象 {@inheritDoc}
	 * 
	 * @see net.sf.json.processors.JsonValueProcessor#processArrayValue(Object,
	 *      JsonConfig)
	 */
	public Object processArrayValue(Object value, JsonConfig jsonConfig) {
		return process(value, jsonConfig);
	}

	/**
	 * 处理单个Date对象 {@inheritDoc}
	 * 
	 * @see net.sf.json.processors.JsonValueProcessor#processArrayValue(Object,
	 *      JsonConfig)
	 */
	public Object processObjectValue(String key, Object value,
			JsonConfig jsonConfig) {
		return process(value, jsonConfig);
	}

	/**
	 * 转换Date对象值
	 * 
	 * @param value
	 *            Date对象
	 * @param jsonConfig
	 *            Json上下文配置对象
	 * @return 规格化日期时间字符串
	 */
	private Object process(Object value, JsonConfig jsonConfig) {
		Object dateValue = value;
		if (dateValue instanceof Date) {
			dateValue = new java.util.Date(((Date) dateValue).getTime());
		}
		if (dateValue instanceof java.util.Date) {
			return this.dateFormat.format(dateValue);
		}
		return dateValue;
	}

}
