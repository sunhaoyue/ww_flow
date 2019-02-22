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

package com.wwgroup.flow.dao;

import java.util.List;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.AssistRelation;
import com.wwgroup.flow.bo.PersonDetail;

/**
 * <p>
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
public interface FlowAssistDao {

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param employeeId
	 * @param start
	 * @param size
	 * @return
	 */
	Page findAll(String employeeId, int start, int size);
	List<AssistRelation> findAll(String employeeId);

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param id
	 */
	void removeAssist(long id);

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param assistRelation
	 */
	void saveAssist(AssistRelation assistRelation);

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param assistRelation
	 */
	void updateAssist(AssistRelation assistRelation);

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param loginPerson
	 * @return
	 */
	PersonDetail[] getAssistedMgrs(PersonDetail loginPerson);

	/**
	 * <p>
	 * Description:[方法功能中文描述]
	 * </p>
	 * 
	 * @param id
	 */
	AssistRelation loadAssist(long id);
}
