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

package com.wwgroup.flow.bo;

/**<p>
 * Title: cuteinfo_[子系统统名]_[模块名]
 * </p>
 * <p>
 * Description: [描述该类概要功能介绍]
 * </p>
 * 
 * @author Administrator
 * @version $Revision$ 2012-7-22
 * @author (lastest modification by $Author$)
 * @since 20100620
 */
public enum AgentType {
	PositionAgent, FlowAgent;

	private String selectedPostCode;

	public AgentType selectedPostCode(String postCode) {
		AgentType postAgent = AgentType.PositionAgent;
		postAgent.selectedPostCode = postCode;
		return postAgent;
	}

	public String getSelectedPostCode() {
		return selectedPostCode;
	}

	public void setSelectedPostCode(String selectedPostCode) {
		this.selectedPostCode = selectedPostCode;
	}
}
