/** 
 * Copyright (c) 2003-2007 Wonders Information Co.,Ltd. All Rights Reserved.
 * 5-6/F, 20 Bldg, 481 Guiping RD. Shanghai 200233,PRC
 *
 * This software is the confidential and proprietary information of Wonders Group.
 * (Research & Development Center). You shall not disclose such
 * Confidential Information and shall use it only in accordance with 
 * the terms of the license agreement you entered into with Wonders Group. 
 *
 * Distributable under GNU LGPL license by gun.org
 */

package com.wwgroup.common.vo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

import com.wwgroup.common.Page;

/**
 * VO对象工具类
 * 
 * @author gaoshang
 * @version $Revision$ 2008-3-10
 * @author (lastest modification by $Author$)
 */
public final class VOUtils {

	/**
	 * 日志处理器
	 */
	private static final Log LOGGER = LogFactory.getLog(VOUtils.class);

	private VOUtils() {
	}

	/**
	 * JSON数据中的日期修正为{time:longValue}型
	 * 
	 * @param jsonObject
	 *            JSON对象
	 * @param dateProp
	 *            日期属性名称
	 * @param dateFormat
	 *            日期格式字符串
	 * @return
	 */
	private static void fixJSONObject(JSONObject jsonObject, String dateProp,
			String dateFormat) {
		try {
			if (jsonObject.get(dateProp).equals(null)
					|| jsonObject.get(dateProp).equals("")) {
				jsonObject.put(dateProp, new JSONObject(true));
			} else {
				Date date = new SimpleDateFormat(dateFormat).parse(jsonObject
						.get(dateProp).toString());
				jsonObject.put(dateProp, "{\"time\":"
						+ JSONObject.fromObject(date).get("time") + "}");
			}
		} catch (ParseException e) {
			LOGGER.error("parse json data error, lack of property: '"
					+ dateProp + "' in json string: " + jsonObject, e);
		}
	}

	/**
	 * 将包含BO的Page对象转换成JSON字符串，中间需转换成VO对象
	 * 
	 * @param page
	 *            Page对象
	 * @param voClass
	 *            VO类
	 * @return JSON字符串
	 */
	@SuppressWarnings("rawtypes")
	public static String getJsonDataFromPage(Page page, Class voClass) {
		if (voClass != null) {
			return getJsonData(transformPage(page, voClass)).toString();
		} else {
			return getJsonData(page);
		}
	}

	/**
	 * 将Page对象中的BO对象转换成VO对象
	 * 
	 * @param page
	 *            Page对象
	 * @param voClass
	 *            VO类
	 * @return 更新后的Page对象
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Page transformPage(Page page, Class voClass) {
		List boList = page.getResult();
		if (boList != null && !boList.isEmpty()) {
			List voList = new ArrayList(boList.size());
			Iterator iter = boList.iterator();
			while (iter.hasNext()) {
				try {
					Object voObject = voClass.newInstance();
					Object boObject = iter.next();
					BeanUtils.copyProperties(boObject, voObject);
					voList.add(voObject);
				} catch (InstantiationException e) {
					LOGGER.error("VO class cannot be instantiated: "
							+ voClass.getName(), e);
				} catch (IllegalAccessException e) {
					LOGGER.error("VO class instantiated error: "
							+ voClass.getName(), e);
				}

			}
			page.setResult(voList);
		}
		return page;
	}

	/**
	 * 获取Json环境上下文默认设置
	 * 
	 * @param dateFormat
	 *            Date类型的转换格式
	 * @return JsonConfig对象
	 */
	public static JsonConfig getJsonConfig(String dateFormat) {
		JsonDateValueProcessor beanProcessor = new JsonDateValueProcessor();
		if (dateFormat != null) {
			DateFormat df = new SimpleDateFormat(dateFormat);
			beanProcessor.setDateFormat(df);
		}

		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
		jsonConfig.registerJsonValueProcessor(java.util.Date.class,
				beanProcessor);
		return jsonConfig;
	}

	/**
	 * 将对象转换成JSON字符串
	 * 
	 * @param bean
	 *            BO/VO对象
	 * @return JSON字符串
	 */
	public static String getJsonData(Object bean) {
		return JSONObject.fromObject(bean, getJsonConfig(null)).toString();
	}

	/**
	 * 将集合类型转换成JSON字符串
	 * 
	 * @param collection
	 *            集合对象
	 * @return JSON字符串
	 */
	@SuppressWarnings("rawtypes")
	public static String getJsonDataFromCollection(Collection collection) {
		return JSONArray.fromObject(collection, getJsonConfig(null)).toString();
	}

	/**
	 * 转换含有非标准日期的JSON字符串，默认日期格式为yyyy-MM-dd HH:mm:ss
	 * 
	 * @param jsonData
	 *            JSON字符串
	 * @param dateProps
	 *            日期属性名称数组
	 * @return
	 */
	public static String formatJsonData(String jsonData, String[] dateProps) {
		return formatJsonData(jsonData, dateProps, "yyyy-MM-dd'T'HH:mm:ss");
	}

	/**
	 * 转换含有非标准日期的JSON字符串
	 * 
	 * @param jsonData
	 *            JSON字符串
	 * @param dateProps
	 *            日期属性名称数组
	 * @param dateFormat
	 *            日期格式字符串
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String formatJsonData(String jsonData, String[] dateProps,
			String dateFormat) {
		if (dateProps == null) {
			return jsonData;
		}
		JSONObject jsonObject = JSONObject.fromObject(jsonData);
		for (Iterator iter = Arrays.asList(dateProps).iterator(); iter
				.hasNext();) {
			String dateProp = (String) iter.next();
			if (jsonObject.has(dateProp)) {
				fixJSONObject(jsonObject, dateProp, dateFormat);
			}
		}
		return jsonObject.toString();
	}

	/**
	 * 转换含有非标准日期的JSON数组数据
	 * 
	 * @param jsonData
	 *            JSON数组数据
	 * @param dateProps
	 *            日期属性名称数组
	 * @param dateFormat
	 *            日期格式字符串
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String formatJsonDataArray(String jsonData,
			String[] dateProps, String dateFormat) {
		if (dateProps == null) {
			return jsonData;
		}
		JSONArray jsonArray = JSONArray.fromObject(jsonData);
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			for (Iterator iter = Arrays.asList(dateProps).iterator(); iter
					.hasNext();) {
				String dateProp = (String) iter.next();
				if (jsonObject.has(dateProp)) {
					fixJSONObject(jsonObject, dateProp, dateFormat);
				}
			}
		}
		return jsonArray.toString();
	}

	/**
	 * 将JSON数据转换为Java Bean对象
	 * 
	 * @param data
	 *            JSON字符串
	 * @param beanClass
	 *            待转换的Bean类型
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Object getBeanFromJsonData(String data, Class beanClass) {
		JSONObject jsonObject = JSONObject.fromObject(data);
		return JSONObject.toBean(jsonObject, beanClass);
	}

	/**
	 * 将JSON数据转换为Java Bean对象，含有待转换的日期
	 * 
	 * @param data
	 *            JSON字符串
	 * @param dateProps
	 *            日期属性名称数组
	 * @param dateFormat
	 *            JSON字符串中含有的日期格式
	 * @param beanClass
	 *            待转换的Bean类型
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Object getBeanFromJsonData(String data, String[] dateProps,
			String dateFormat, Class beanClass) {
		return getBeanFromJsonData(formatJsonData(data, dateProps, dateFormat),
				beanClass);
	}

	/**
	 * 将JSON数据转换为Java Bean对象，含有待转换的日期，格式为yyyy-MM-dd HH:mm:ss
	 * 
	 * @param data
	 *            JSON字符串
	 * @param dateProps
	 *            日期属性名称数组
	 * @param beanClass
	 *            待转换的Bean类型
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Object getBeanFromJsonData(String data, String[] dateProps,
			Class beanClass) {
		return getBeanFromJsonData(data, dateProps, "yyyy-MM-dd'T'HH:mm:ss",
				beanClass);
	}

	/**
	 * 将JSON数组数据转换为Java Bean对象列表
	 * 
	 * @param data
	 *            JSON数组数据
	 * @param beanClass
	 *            待转换的Bean类型
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List getBeanListFromJsonData(String data, Class beanClass) {
		JSONArray jsonArray = JSONArray.fromObject(data);
		List list = new ArrayList(jsonArray.size());
		for (Iterator iter = jsonArray.iterator(); iter.hasNext();) {
			JSONObject jsonObject = (JSONObject) iter.next();
			list.add(JSONObject.toBean(jsonObject, beanClass));
		}
		return list;
	}

	/**
	 * 将JSON数组数据转换为Java Bean对象列表，含有待转换的日期
	 * 
	 * @param data
	 *            JSON数组数据
	 * @param dateProps
	 *            日期属性名称数组
	 * @param dateFormat
	 *            JSON字符串中含有的日期格式
	 * @param beanClass
	 *            待转换的Bean类型
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List getBeanListFromJsonData(String data, String[] dateProps,
			String dateFormat, Class beanClass) {
		return getBeanListFromJsonData(formatJsonDataArray(data, dateProps,
				dateFormat), beanClass);
	}

	/**
	 * 将JSON数组数据转换为Java Bean对象列表，含有待转换的日期，格式为yyyy-MM-dd HH:mm:ss
	 * 
	 * @param data
	 *            JSON数组数据
	 * @param dateProps
	 *            日期属性名称数组
	 * @param beanClass
	 *            待转换的Bean类型
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List getBeanListFromJsonData(String data, String[] dateProps,
			Class beanClass) {
		return getBeanListFromJsonData(data, dateProps,
				"yyyy-MM-dd'T'HH:mm:ss", beanClass);
	}

}
