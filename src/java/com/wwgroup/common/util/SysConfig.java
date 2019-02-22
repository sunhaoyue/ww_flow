package com.wwgroup.common.util;

import java.io.InputStream;
import java.util.Properties;

import com.wwgroup.common.mail.constants.MailConstants;

public class SysConfig {
	private static final String FILE_PATH = "sysConfig.properties";

	private static Properties properties = new Properties();
	private static SysConfig instance = new SysConfig();

	private SysConfig() {
		loadConfig();
	}

	public static SysConfig getInstance() {
		return instance;
	}

	private void loadConfig() {
		try {
			InputStream in = SysConfig.class.getClassLoader()
					.getResourceAsStream(FILE_PATH);
			properties.load(in);
		} catch (Exception e) {
			System.out.println("loading [sysConfig.properties] error:"
					+ e.getMessage());
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 * 取得String类型的指定系统参数值
	 * 
	 * @param key
	 *            系统参数名
	 * @return
	 */
	public String getString(String key) {
		return properties.getProperty(key) != null ? String.valueOf(properties
				.getProperty(key)) : null;
	}

	/**
	 * 取得String类型的指定系统参数值，如果未定义则使用默认值
	 * 
	 * @param key
	 *            系统参数名
	 * @param defaultValue
	 *            系统参数默认值
	 * @return
	 */
	public String getString(String key, String defaultValue) {
		try {
			return properties.getProperty(key) != null ? getString(key)
					: defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 取得Boolean类型的指定系统参数值
	 * 
	 * @param key
	 *            系统参数名
	 * @return
	 */
	public Boolean getBoolean(String key) {
		return properties.getProperty(key) != null ? Boolean
				.valueOf(getString(key)) : null;
	}

	/**
	 * 取得Boolean类型的指定系统参数值，如果未定义则使用默认值
	 * 
	 * @param key
	 *            系统参数名
	 * @param defaultValue
	 *            系统参数默认值
	 * @return
	 */
	public Boolean getBoolean(String key, Boolean defaultValue) {
		try {
			return properties.getProperty(key) != null ? getBoolean(key)
					: defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 取得Short类型的指定系统参数值
	 * 
	 * @param key
	 *            系统参数名
	 * @return
	 */
	public Short getShort(String key) {
		return properties.getProperty(key) != null ? Short
				.valueOf(getString(key)) : null;
	}

	/**
	 * 取得Short类型的指定系统参数值，如果未定义则使用默认值
	 * 
	 * @param key
	 *            系统参数名
	 * @param defaultValue
	 *            系统参数默认值
	 * @return
	 */
	public Short getShort(String key, Short defaultValue) {
		try {
			return properties.getProperty(key) != null ? getShort(key)
					: defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 取得Integer类型的指定系统参数值
	 * 
	 * @param key
	 *            系统参数名
	 * @return
	 */
	public Integer getInt(String key) {
		return properties.getProperty(key) != null ? Integer
				.valueOf(getString(key)) : null;
	}

	/**
	 * 取得Integer类型的指定系统参数值，如果未定义则使用默认值
	 * 
	 * @param key
	 *            系统参数名
	 * @param defaultValue
	 *            系统参数默认值
	 * @return
	 */
	public Integer getInt(String key, Integer defaultValue) {
		try {
			return properties.getProperty(key) != null ? getInt(key)
					: defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 取得Long类型的指定系统参数值
	 * 
	 * @param key
	 *            系统参数名
	 * @return
	 */
	public Long getLong(String key) {
		return properties.getProperty(key) != null ? Long
				.valueOf(getString(key)) : null;
	}

	/**
	 * 取得Long类型的指定系统参数值，如果未定义则使用默认值
	 * 
	 * @param key
	 *            系统参数名
	 * @param defaultValue
	 *            系统参数默认值
	 * @return
	 */
	public Long getLong(String key, Long defaultValue) {
		try {
			return properties.getProperty(key) != null ? getLong(key)
					: defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 取得Float类型的指定系统参数值
	 * 
	 * @param key
	 *            系统参数名
	 * @return
	 */
	public Float getFloat(String key) {
		return properties.getProperty(key) != null ? Float
				.valueOf(getString(key)) : null;
	}

	/**
	 * 取得Float类型的指定系统参数值，如果未定义则使用默认值
	 * 
	 * @param key
	 *            系统参数名
	 * @param defaultValue
	 *            系统参数默认值
	 * @return
	 */
	public Float getFloat(String key, Float defaultValue) {
		try {
			return properties.getProperty(key) != null ? getFloat(key)
					: defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 取得Double类型的指定系统参数值
	 * 
	 * @param key
	 *            系统参数名
	 * @return
	 */
	public Double getDouble(String key) {
		return properties.getProperty(key) != null ? Double
				.valueOf(getString(key)) : null;
	}

	/**
	 * 取得Double类型的指定系统参数值，如果未定义则使用默认值
	 * 
	 * @param key
	 *            系统参数名
	 * @param defaultValue
	 *            系统参数默认值
	 * @return
	 */
	public Double getDouble(String key, Double defaultValue) {
		try {
			return properties.getProperty(key) != null ? getDouble(key)
					: defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static void main(String[] agrs) {
		String mailHost = SysConfig.getInstance().getProperty(
				MailConstants.MAIL_HOST);
		System.out.println(mailHost);
	}
}
