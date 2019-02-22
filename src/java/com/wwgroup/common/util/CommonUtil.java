package com.wwgroup.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * 常用工具类
 * 
 * @creator zhangqiang
 * @create-time Jan 16, 2013 10:21:20 AM
 * @version 0.1
 */
public class CommonUtil {

	/**
	 * Inputstream 转成byte数组
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static byte[] inputStreamToByte(InputStream is) throws IOException {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		int ch;
		while ((ch = is.read()) != -1) {
			bytestream.write(ch);
		}
		byte imgdata[] = bytestream.toByteArray();
		bytestream.close();
		return imgdata;
	}

	/**
	 * byte数组转成InputStream
	 * 
	 * @param data
	 * @return
	 */
	public static InputStream byteToInputStream(byte[] data) {
		InputStream is = new ByteArrayInputStream(data);

		return is;
	}

	public static String getCurrentTime() {
		String retval = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		retval = sdf.format(date);
		return retval;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void copyPropFromBean2Map(Object fromObject, Map toMap) {
		if (toMap != null) {
			try {
				toMap.putAll(BeanUtils.describe(fromObject));
				toMap.remove("class");
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static String getServerIP() {
		Enumeration netInterfaces = null;
		String localIP = null;
		String netIP = null;

		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			boolean finded = false;
			do {
				NetworkInterface ni = (NetworkInterface) netInterfaces
						.nextElement();
				Enumeration address = ni.getInetAddresses();
				while (address.hasMoreElements()) {
					ip = (InetAddress) address.nextElement();
					if ((!ip.isSiteLocalAddress()) && (!ip.isLoopbackAddress())
							&& (ip.getHostAddress().indexOf(":") == -1)) {
						netIP = ip.getHostAddress();
						finded = true;
						break;
					}
					if ((!ip.isSiteLocalAddress()) || (ip.isLoopbackAddress())
							|| (ip.getHostAddress().indexOf(":") != -1))
						continue;
					localIP = ip.getHostAddress();
				}
				if (!netInterfaces.hasMoreElements()) break;
			} while (!finded);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if ((netIP != null) && (!"".equals(netIP))) {
			return netIP;
		}
		return localIP;
	}
	
	public static String getClientIP(HttpServletRequest request){
		String ip = request.getHeader("x-forwarded-for");
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
			ip = request.getRemoteAddr();
		}
		if (StringUtils.isEmpty(ip)) {
			ip = request.getRemoteAddr();
		}
		String[] arr = ip.split(".");
		for (int i = 0; i < arr.length; i++) {
			String strTmp = arr[i];
			if (!"unknown".equalsIgnoreCase(strTmp)) {
				ip = strTmp;
				break;
			}
		}
		return ip;
	}
}
