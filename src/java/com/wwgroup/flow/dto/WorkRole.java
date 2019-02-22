/** 
* 
 * Copyright (c) 1995-2012 Wonders Information Co.,Ltd. 
 * 1518 Lianhang Rd,Shanghai 201112.P.R.C.
 * All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Wonders Group.
 * (Social Security Department). You shall not disclose such
 * Confidential Information and shall use it only in accordance with 
 * the terms of the license agreement you entered into with Wonders Group. 
 *
 * Distributable under GNU LGPL license by gnu.org
 */

package com.wwgroup.flow.dto;

/**<p>
 * Title: cuteinfo_[子系统统名]_[模块名]
 * </p>
 * <p>
 * Description: 承担这项工作的身份
 * </p>
 * 
 * @author Administrator
 * @version $Revision$ 2012-7-29
 * @author (lastest modification by $Author$)
 * @since 20100620
 */
public enum WorkRole {

	// 自己的工作，助理允许指派，助理不允许指派，代理
	MYSELF, ASSIST_ASSIGNED, ASSIST_NOT_ASSIGNED, AGENT
}
