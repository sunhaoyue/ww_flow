package com.wwgroup.common.metatype;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.wwgroup.common.util.CommonUtil;

@SuppressWarnings("serial")
public class BaseDomain implements Serializable {

	public Map toMap(){
		Map esMap = new HashMap();
		CommonUtil.copyPropFromBean2Map(this, esMap);
		return esMap;
	}
}
