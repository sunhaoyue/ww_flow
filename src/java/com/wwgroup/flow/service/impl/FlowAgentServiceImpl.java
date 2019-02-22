package com.wwgroup.flow.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.AgentPerson;
import com.wwgroup.flow.bo.AgentRelation;
import com.wwgroup.flow.bo.AgentType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.dao.FlowAgentDao;
import com.wwgroup.flow.dao.HuaYangFlowInterfaceDao;
import com.wwgroup.flow.service.FlowAgentService;

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
public class FlowAgentServiceImpl implements FlowAgentService {

	private FlowAgentDao flowAgentDao;

	private HuaYangFlowInterfaceDao huaYangFlowInterfaceDao;

	public void setFlowAgentDao(FlowAgentDao flowAgentDao) {
		this.flowAgentDao = flowAgentDao;
	}

	public void setHuaYangFlowInterfaceDao(HuaYangFlowInterfaceDao huaYangFlowInterfaceDao) {
		this.huaYangFlowInterfaceDao = huaYangFlowInterfaceDao;
	}

	@Override
	public void saveAgent(AgentRelation agentRelation) {
		// 如果为空那么就变成系统今天日期
		if (agentRelation.getStartDate() == null) {
			agentRelation.setStartDate(new Date(System.currentTimeMillis()));
		}
		if (agentRelation.getEndDate() == null) {
			// 当前startTime的下一周时间
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(agentRelation.getStartDate().getTime());
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar
							.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
			calendar.setFirstDayOfWeek(Calendar.MONDAY);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

			Date endDate = new Date(calendar.getTime().getTime() + (7 * 24 * 60 * 60 * 1000));
			agentRelation.setEndDate(endDate);
		}

		this.flowAgentDao.saveAgent(agentRelation);
	}

	@Override
	public Page findAll(String employeeId, int start, int size) {
		return this.flowAgentDao.findAll(employeeId, start, size);
	}

	@Override
	public PersonDetail[] getAgentees(PersonDetail agentPerson) {
		return this.flowAgentDao.getAgentees(agentPerson);
	}

	@Override
	public Map<String, String> extraGetFlowCategories() {
		return this.huaYangFlowInterfaceDao.extraGetFlowCategories();
	}

	@Override
	public Map<String, String> extraGetFlowCategories(String flowId) {
		return this.huaYangFlowInterfaceDao.extraGetFlowCategories(flowId);
	}

	@Override
	public Map<String, String> extraGetFlowEntries(String flowId) {
		return this.huaYangFlowInterfaceDao.extraGetFlowEntries(flowId);
	}

	@Override
	public void deleteAgent(AgentRelation agentRelation) {
		this.flowAgentDao.deleteAgent(agentRelation);

	}

	@Override
	public void updateAgent(AgentRelation agentRelation) {
		this.flowAgentDao.updateAgent(agentRelation);
	}

	@Override
	public AgentRelation loadAgent(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAgentPerson(AgentRelation agentRelation, AgentPerson agentPerson) {
		this.flowAgentDao.saveAgentPerson(agentRelation, agentPerson);
		if (agentPerson.getType() == AgentType.FlowAgent) {
			if (agentPerson.getSelectedFlow() != null) {
				this.huaYangFlowInterfaceDao.saveFlowAgent(agentRelation, agentPerson);
			}
		}
	}

	@Override
	public AgentRelation loadAgent(String employeeId) {
		return this.flowAgentDao.loadAgentRelation(employeeId);
	}

	@Override
	public List<AgentPerson> loadAgentPersons(AgentRelation agentRelation) {
		return this.flowAgentDao.loadAgentPersons(agentRelation);
	}

	@Override
	public void deleteAgentPerson(AgentRelation agentRelation, AgentPerson agentPerson) {
		this.flowAgentDao.deleteAgentPerson(agentPerson);
		if (agentPerson.getType() == AgentType.FlowAgent) {
			if (agentPerson.getSelectedFlow() != null) {
				this.huaYangFlowInterfaceDao.deleteFlowAgent(agentRelation, agentPerson);
			}
		}
	}

	@Override
	public void updateAgentPerson(AgentRelation agentRelation, AgentPerson agentPerson) {
		Map<String, List<String>> oldSelectedFlowMap = this.flowAgentDao.updateAgentPerson(agentPerson);
		if (agentPerson.getType() == AgentType.FlowAgent) {
			if (agentPerson.getSelectedFlow() != null) {
				this.huaYangFlowInterfaceDao.updateFlowAgent(agentRelation, agentPerson, oldSelectedFlowMap);
			}
		}
	}

}
