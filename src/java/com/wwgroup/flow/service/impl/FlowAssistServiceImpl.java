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

package com.wwgroup.flow.service.impl;

import java.util.List;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.AssistRelation;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.dao.FlowAssistDao;
import com.wwgroup.flow.service.FlowAssistService;

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
public class FlowAssistServiceImpl implements FlowAssistService {

	private FlowAssistDao flowAssistDao;

	public void setFlowAssistDao(FlowAssistDao flowAssistDao) {
		this.flowAssistDao = flowAssistDao;
	}

	@Override
	public Page findAll(String employeeId, int start, int size) {
		return flowAssistDao.findAll(employeeId, start, size);
	}

	@Override
	public void removeAssist(long id) {
		flowAssistDao.removeAssist(id);
	}

	@Override
	public void saveAssist(AssistRelation assistRelation) {
		flowAssistDao.saveAssist(assistRelation);
	}

	@Override
	public void updateAssist(AssistRelation assistRelation) {
		flowAssistDao.updateAssist(assistRelation);
	}

	@Override
	public PersonDetail[] getAssistedMgrs(PersonDetail loginPerson) {
		return flowAssistDao.getAssistedMgrs(loginPerson);
	}

	@Override
	public AssistRelation loadAssist(long id) {
		return flowAssistDao.loadAssist(id);
	}

	@Override
	public List<AssistRelation> findAll(String employeeId) {
		return flowAssistDao.findAll(employeeId);
	}
}
