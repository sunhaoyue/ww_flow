package com.wwgroup.flow.service.impl;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.common.Page;
import com.wwgroup.common.exceptions.MailException;
import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowAttachment;
import com.wwgroup.flow.bo.FlowContent;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.helper.PersonTYPE;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.dao.FlowDao;
import com.wwgroup.flow.dto.MyWorkHistory;
import com.wwgroup.flow.service.FlowService;
import com.wwgroup.flow.service.FlowStageProcessor;
import com.wwgroup.flow.service.PersonService;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.dao.OrganDao;
import com.wwgroup.user.bo.SystemUsers;
import com.wwgroup.user.dao.UserDao;

@SuppressWarnings("unused")
public class FlowServiceImpl implements FlowService {

	private PersonService personService;

	private FlowStageProcessor flowStage;

	private FlowDao flowDao;

	private OrganDao organDao;

	private UserDao userDao;

	public void setOrganDao(OrganDao organDao) {
		this.organDao = organDao;
	}

	public OrganDao getOrganDao() {
		return organDao;
	}

	public void setFlowStage(FlowStageProcessor flowStage) {
		this.flowStage = flowStage;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setFlowDao(FlowDao flowDao) {
		this.flowDao = flowDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public String generateFormNum(PersonDetail actualPerson, FlowType flowType) {
		// (内联)NL+组织代码(4位)-中心代码(4位)+YYYYMM+0001，
		// (签呈)QC+组织代码(4位)-中心代码(4位)+YYYYMM+0001
		Calendar cal1 = Calendar.getInstance();
		StringBuffer sb = new StringBuffer();
		if (flowType == FlowType.QIANCHENG) {
			sb.append("QC");
		} else if (flowType == FlowType.NEILIAN) {
			sb.append("NL");
		}

		String[] codes = this.personService.getOrganCodes(actualPerson);
		sb.append(codes[0]);
		sb.append("-");
		// if (StringUtils.isNotEmpty(codes[1]) && codes[1].length() > 4) {
		// sb.append(codes[1].substring(0, 4));
		// }
		// else {
		sb.append(codes[1]);
		// }
		sb.append(cal1.get(Calendar.YEAR));

		String month = String.valueOf(cal1.get(Calendar.MONTH) + 1);
		if (2 - month.length() > 0) {
			int remainPosition = 2 - month.length();
			StringBuffer tmp = new StringBuffer();
			for (int i = 0; i < remainPosition; i++) {
				tmp.append("0");
			}
			tmp.append(month);
			month = tmp.toString();
		}
		sb.append(month);

		String sequence = flowDao.getFlowSequence(actualPerson);
		int remainLength = 4 - sequence.length();
		if (remainLength > 0) {
			int remainIndex = remainLength;
			StringBuffer tmp = new StringBuffer();
			for (int i = 0; i < remainIndex; i++) {
				tmp.append("0");
			}
			tmp.append(sequence);
			sequence = tmp.toString();
		}
		sb.append(StringUtils.substring(sequence, sequence.length() - 4,
				sequence.length()));
		return sb.toString();
	}

	@Override
	public long getFlowId(String formNum) {
		return Long.parseLong(formNum.substring(0, formNum.indexOf(";")));
	}

	@Override
	public Page getWorksWithPage(PersonDetail person, int start, int size) {
		// TODO do some validation work
		return flowDao.getWorksWithPage(person, start, size);
	}

	@Override
	public Flow getFlow(MyWork work) {
		// 根据formnum获取到flow对象，然后获取到createPerson , actualPerson , content,
		// 【attachment】
		Flow flow = flowDao.loadFlow(work.getFlowId(), work.getFlowType());
		this.loadFlowProperties(flow);
		return flow;
	}

	private void loadFlowProperties(Flow flow) {
		if (flow.getFlowType() == FlowType.QIANCHENG) {
			flow.setActualPerson(flowDao.loadPerson(flow.getId(),
					PersonTYPE.ACTUAL));
		}
		flow.setCreatePerson(flowDao.loadPerson(flow.getId(), PersonTYPE.CREATE));
		flow.setContent(flowDao.loadContent(flow.getId()));
		flow.setFlowAttachments(flowDao.loadAttachments(flow.getId()));
	}

	@Override
	public void saveFlowTemplate(Flow flow, MyWork rejectWork) {
		// 如果这个flow本来就是template那么只做普通的更新操作就可以了
		if (flow.getId() > 0) {
			Flow oldFlow = this.loadFlow(flow.getFormNum());
			flow.setTempalte(oldFlow.isTempalte());
			flow.setRenewTimes(oldFlow.getRenewTimes() > flow.getRenewTimes() ? oldFlow
					.getRenewTimes() : flow.getRenewTimes());
		}
		if (flow.isTempalte() && flow.getId() > 0) {
			this.updateFlowStatus(flow);
			flow.getActualPerson().setFlowId(flow.getId());
			this.updatePersonDetail(flow.getActualPerson(), PersonTYPE.ACTUAL);
			flow.getCreatePerson().setFlowId(flow.getId());
			this.updatePersonDetail(flow.getCreatePerson(), PersonTYPE.CREATE);
			flow.getContent().setFlowId(flow.getId());
			this.updateFormContent(flow.getContent());
			this.updateFlowAttachments(flow, flow.getFlowAttachments());
			return;
		}
		// 如果是renewTimes==1重新起案 那么需要new一个新的flow然后将原来flow的记录全部复制出来
		if (flow.getRenewTimes() == 1) {
			// 取消原来表单 然后清理下原来表单的内存数据
			long oldCancelWorkId = rejectWork.getId();
			this.cancel(rejectWork, flow);

			MyWork[] oldAllWorks = this.listWorks(flow);
			// 新生成一个flowId，并且让原来的关系数据都复制一份新的
			FlowAttachment[] oldAttachments = this.flowDao.loadAttachments(flow
					.getId());

			// 识别重新起案的单据类型
			FlowType ft = null;
			if (flow.getFormNum().startsWith("QC"))
				ft = FlowType.QIANCHENG;
			else if (flow.getFormNum().startsWith("NL"))
				ft = FlowType.NEILIAN;

			String formNum = this.generateFormNum(flow.getActualPerson(), ft);
			flow.setFormNum(formNum);
			flow.setCreateTime(Calendar.getInstance().getTimeInMillis());
			flow.setTemplateCreateId(flow.getCreatePerson().getEmployeeId());
			flow.setTempalte(true);
			flow.setStatus(FlowStatus.TEMP);
			// 这里不新增附件记录了
			this.doSaveFlow(flow, false);

			// 以下所有的work都要重新复制一份 （复制新的 赋予新的workId 以及绑定新的FlowId）
			long reCreatedWorkId = 0;
			for (MyWork newWork : oldAllWorks) {// short path: Create Sign
												// Reject 101 102 103
				boolean changedWorkId = false;
				if (newWork.getId() == oldCancelWorkId) {
					changedWorkId = true;
				}
				newWork.setFlowId(flow.getId());
				this.saveWork(newWork);
				if (changedWorkId) {
					reCreatedWorkId = newWork.getId();
				}
				this.updateWorkStatus(newWork);
			}
			if (reCreatedWorkId > 0) {// 106
				rejectWork.setId(reCreatedWorkId);
				rejectWork.setFlowId(flow.getId());
			}

			for (FlowAttachment newAttachment : oldAttachments) {
				newAttachment.setFlowId(flow.getId());
			}
			this.flowDao.saveAttachments(oldAttachments);
		} else {
			flow.setTemplateCreateId(flow.getCreatePerson().getEmployeeId());
			flow.setTempalte(true);
			flow.setStatus(FlowStatus.TEMP);
			// 这里不新增附件记录了
			this.doSaveFlow(flow, false);
			// 下面是添加附件记录的关联
			this.updateFlowAttachments(flow, flow.getFlowAttachments());
		}
		if (rejectWork != null) {
			rejectWork.setStatus(FlowStatus.RECREATE);
			this.updateWorkStatus(rejectWork);
		}
	}

	/**
	 * @see com.wwgroup.flow.service.FlowService#submitFlow(com.wwgroup.flow.bo.Flow,
	 *      com.wwgroup.flow.bo.work.MyWork)
	 */
	@Override
	public Map<String, PersonDetail> submitFlow(Flow flow, MyWork rejectWork)
			throws SQLException, MailException {
		if (flow.isTempalte() && flow.getId() > 0) {
			Flow oldFlow = this.loadFlow(flow.getFormNum());
			flow.setTempalte(oldFlow.isTempalte());
			flow.setRenewTimes(oldFlow.getRenewTimes());
		}
		// 如果flow.renewTimes == 1那么首先取消 cancel原来的表单，然后再新开表单记录。 其它情况==0的情况走正常流转过程
		if (!flow.isTempalte() && flow.getRenewTimes() == 1) {
			// 取消原来表单 然后清理下原来表单的内存数据
			long oldCancelWorkId = rejectWork.getId();
			this.cancel(rejectWork, flow);
			MyWork[] oldAllWorks = this.listWorks(flow);
			// 新生成一个flowId，并且让原来的关系数据都复制一份新的
			FlowAttachment[] oldAttachments = this.flowDao.loadAttachments(flow
					.getId());

			// 识别重新起案的单据类型
			FlowType ft = null;
			if (flow.getFormNum().startsWith("QC"))
				ft = FlowType.QIANCHENG;
			else if (flow.getFormNum().startsWith("NL"))
				ft = FlowType.NEILIAN;

			String formNum = this.generateFormNum(flow.getActualPerson(), ft);
			flow.setFormNum(formNum);
			flow.setCreateTime(Calendar.getInstance().getTimeInMillis());
			this.doSaveFlow(flow, false);
			// 下面是添加附件记录的关联
			// TODO: 原来的work也要复制过来
			// 以下所有的work都要重新复制一份 （复制新的 赋予新的workId 以及绑定新的FlowId）
			long reCreatedWorkId = 0;
			for (MyWork newWork : oldAllWorks) {// short path: Create Sign
												// Reject 101 102 103
				long oldFinishTime = newWork.getFinishTime();
				boolean changedWorkId = false;
				if (newWork.getId() == oldCancelWorkId) {
					changedWorkId = true;
				}
				newWork.setOldFlowId(newWork.getFlowId());
				newWork.setFlowId(flow.getId());
				this.saveWork(newWork);
				if (changedWorkId) {
					reCreatedWorkId = newWork.getId();
				}
				this.updateWorkStatus(newWork);
				newWork.setFinishTime(oldFinishTime);
				flowDao.saveWorkFinishTime(newWork); // 保存完成时间
			}
			if (reCreatedWorkId > 0) {// 106
				rejectWork.setId(reCreatedWorkId);
				rejectWork.setFlowId(flow.getId());
			}

			// TODO: 这里要注意的是原来表单的附件是否也可以复制过来(看测试结果)
			for (FlowAttachment newAttachment : oldAttachments) {
				newAttachment.setFlowId(flow.getId());
			}
			this.flowDao.saveAttachments(oldAttachments);
			// this.updateFlowAttachments(flow, flow.getFlowAttachments());
		}
		// 这个work应该是新的单据中的work 与旧的无关
		Map<String, PersonDetail> result = flowStage.start(flow);
		if (rejectWork != null) {
			rejectWork.setStatus(FlowStatus.RECREATE);
			this.updateWorkStatus(rejectWork);
		}

		for (String key : result.keySet()) {
			PersonDetail nextPerson = result.get(key);
			if (null == nextPerson
					|| StringUtils.isBlank(nextPerson.getEmployeeId())) {
				// 下一个处理人为空
				throw new RuntimeException("汇报关系维护有误，请联系管理员");
			}
		}

		return result;
	}

	private MyWork[] listWorks(Flow flow) {
		return this.flowDao.listWorks(flow);
	}

	/**
	 * @see com.wwgroup.flow.service.FlowService#completeWork(com.wwgroup.flow.bo.work.MyWork)
	 */
	@Override
	public Map<String, PersonDetail> completeWork(MyWork work)
			throws SQLException {
		// 完成本身工作，并且根据情况，发起下一步请求
		if (work.getStatus() != FlowStatus.DOING) {
			throw new RuntimeException(work.getId()
					+ " work is not at running stage");
		}
		// TODO: 使用event 替换if else 块，与上面的startNextWork一起重构
		Flow flow = this.getFlow(work);
		// 判断
		Map<String, PersonDetail> map = flowStage.completeWork(flow, work);
		if (flow.getStatus() != FlowStatus.FINAL_DECISION_END) {
			for (String key : map.keySet()) {
				PersonDetail nextPerson = map.get(key);
				if (null == nextPerson
						|| StringUtils.isBlank(nextPerson.getEmployeeId())) {
					// 下一个处理人为空
					throw new RuntimeException("汇报关系维护有误，请联系管理员");
				}

				// SystemUsers systemUsers =
				// userDao.getUsersByName(nextPerson.getEmployeeId());
				// if(StringUtils.isBlank(systemUsers.getEmail())){
				// //下一个处理人的邮箱为空
				// //throw new
				// RuntimeException("下一步处理人["+nextPerson.getName()+"]邮箱为空，请联系管理员");
				// }
			}

		}

		return map;
	}

	/**
	 * @see com.wwgroup.flow.service.FlowService#assignWork(com.wwgroup.flow.bo.PersonDetail,
	 *      com.wwgroup.flow.bo.work.MyWork)
	 */
	@Override
	public Map<String, PersonDetail> assignWork(PersonDetail employee,
			MyWork parentWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		Flow flow = this.getFlow(parentWork);
		// 只会在会办的指派中发送
		if (flow.getStatus() == FlowStatus.JOINTSIGN_START
				|| flow.getStatus() == FlowStatus.CMPCODEJOINTSIGN_START
				|| flow.getStatus() == FlowStatus.FINAL_DECISION_START
				|| flow.getStatus() == FlowStatus.SECONDFINAL_DECISION_START
				|| this.canAssignInStage(flow)) {
			// 旧的work状态变成待定waiting，启动新的work
			// 目前暂时还没有这个必要
			parentWork.setStatus(FlowStatus.WAITING);
			flowDao.updateWork(parentWork);

			// TODO: 遇到审核或者最终核决中的指派情况，可能jointStartId为空，说明其为做出核决的开始人，
			// 那么给这个parentWork本身的employeeId赋予startId即可，这样新生成的指派work内就有startId了。
			if (StringUtils.isEmpty(parentWork.getJoinSignStartId())) {
				parentWork.setJoinSignStartId(parentWork.getEmployeeId());
			}
			result = flowStage.startNextWork(this.getFlow(parentWork),
					employee, parentWork);
		}
		return result;
	}

	@Override
	public boolean canAssignInStage(Flow flow) {
		boolean result = false;
		// 只有在转中心的情况下 才能允许指派
		if ((flow.getStatus() == FlowStatus.CHENGHE_START || flow.getStatus() == FlowStatus.FINAL_DECISION_START)
				&& !flow.isLocal()) {
			if (!StringUtils.isEmpty(flow.getShengheEmployeeId())) {
				// 只要序列比他大就可以进行指派了
				result = flow.getShengheStep() > DescionMaker.UNITLEADER
						.ordinal();
			}
		}
		return result;
	}

	// 二次指派的情况 ：如果workStage不等于JointSign的都是走这个InsideWork进行指派
	// 非JointSign的同意也要走特殊流程
	@Override
	public Map<String, PersonDetail> assignInsideWork(PersonDetail employee,
			MyWork parentWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		Flow flow = this.getFlow(parentWork);
		// 只会在会办的指派中发送
		if (flow.getStatus() == FlowStatus.JOINTSIGN_START
				|| flow.getStatus() == FlowStatus.SECONDFINAL_DECISION_START
				|| flow.getStatus() == FlowStatus.FINAL_DECISION_START
				|| this.canAssignInStage(flow)) {
			// 旧的work状态变成待定waiting，启动新的work
			// 目前暂时还没有这个必要
			parentWork.setStatus(FlowStatus.WAITING);
			flowDao.updateWork(parentWork);

			// TODO: 遇到审核或者最终核决中的指派情况，可能jointStartId为空，说明其为做出核决的开始人，
			// 那么给这个parentWork本身的employeeId赋予startId即可，这样新生成的指派work内就有startId了。
			if (StringUtils.isEmpty(parentWork.getJoinSignStartId())) {
				parentWork.setJoinSignStartId(parentWork.getEmployeeId());
			}
			// 如果最新的 分支开始节点ID标示 与 这个 work本事的employeeId deptId标示一致那么就不需要再额外加了
			String startEmployeeId = this.getLatestJoinSignStartId(
					parentWork.getJoinSignStartId(), "employeeId");
			String startDeptId = this.getLatestJoinSignStartId(
					parentWork.getJoinSignStartId(), "deptId");
			if (startEmployeeId.equals(parentWork.getEmployeeId())
					&& startDeptId.equals(parentWork.getDeptId())) {
			} else {
				// JSONArray jsonArray = new
				// JSONArray();//JSONArray.fromObject(parentWork.getJoinSignStartId());
				// JSONObject jsonObj = new JSONObject();
				// jsonObj.put("employeeId", parentWork.getEmployeeId());
				// jsonObj.put("deptId", parentWork.getDeptId());
				// jsonArray.add(jsonObj);
				// parentWork.setJoinSignStartId(jsonArray.toString());
			}

			result = flowStage.startNextWork(this.getFlow(parentWork),
					employee, parentWork);
		}
		return result;
	}

	private String getLatestJoinSignStartId(String joinSignStartId, String key) {
		JSONArray jsonArray = JSONArray.fromObject(joinSignStartId);
		JSONObject jsonObj = (JSONObject) jsonArray.get(jsonArray.size() - 1);
		return String.valueOf(jsonObj.get(key));
	}

	@Override
	public Map<String, PersonDetail> completeInsideWork(MyWork work) {
		// 完成本身工作，并且根据情况，发起下一步请求
		if (work.getStatus() != FlowStatus.DOING) {
			throw new RuntimeException(work.getId()
					+ " work is not at running stage");
		}
		// TODO: 使用event 替换if else 块，与上面的startNextWork一起重构
		Flow flow = this.getFlow(work);
		return flowStage.completeInsideWork(flow, work);
	}

	@Override
	public Flow[] loadFlowTemplates(String userName, FlowType flowType) {
		return flowDao.loadFlowTemplate(userName, flowType);
	}

	@Override
	public boolean saveFlow(Flow flow) {
		if (flow.isTempalte()) {
			flowDao.clearFlowTemplate(flow);
		}
		// else {
		if (flow.getId() > 0) {// TODO: 这个后续修改更改表单内所有内容
			this.updateFlowStatus(flow);
			flow.getActualPerson().setFlowId(flow.getId());
			this.updatePersonDetail(flow.getActualPerson(), PersonTYPE.ACTUAL);
			flow.getCreatePerson().setFlowId(flow.getId());
			this.updatePersonDetail(flow.getCreatePerson(), PersonTYPE.CREATE);
			flow.getContent().setFlowId(flow.getId());
			this.updateFormContent(flow.getContent());
			this.updateFlowAttachments(flow, flow.getFlowAttachments());
		} else {
			this.doSaveFlow(flow, false);
			// 下面是添加附件记录的关联
			this.updateFlowAttachments(flow, flow.getFlowAttachments());
		}
		// }
		return true;
	}

	private void updateFlowAttachments(Flow flow,
			FlowAttachment[] flowAttachments) {
		if (flowAttachments != null && flowAttachments.length > 0) {
			// 这里只是把已有flow与这些attachement关联上即可。
			this.flowDao.linkFlowAttachment(flow, flowAttachments);
		}
	}

	private void updateFormContent(FlowContent content) {
		// 这里是需要判断content.id是否大于0 ,然后决定做 更新还是新增操作
		if (content.getId() > 0) {
			flowDao.updateContent(content);
		} else {
			// 全字段更新
			flowDao.saveContent(content);
		}
	}

	private void updatePersonDetail(PersonDetail person, PersonTYPE personTYPE) {
		// 这里是需要判断person.id是否大于0 ,然后决定做 更新还是新增操作
		if (person.getId() > 0) {
			flowDao.updatePerson(person, personTYPE);
		} else {
			// 全字段更新
			flowDao.savePerson(person, personTYPE);
		}
	}

	private void doSaveFlow(Flow flow, boolean saveAttachment) {
		// TODO: add some validation and return false later if some business
		// false occured.
		/*
		 * SystemGroups systemGroups = organDao.loadGroupsById(420);
		 * System.out.println("@@@@@@@@@@@@@@@@@@@" +
		 * systemGroups.getOrgPath());
		 */
		flowDao.saveFlow(flow);
		if (flow.getCreatePerson() != null) {
			flow.getCreatePerson().setFlowId(flow.getId());
			flowDao.savePerson(flow.getCreatePerson(), PersonTYPE.CREATE);
		}
		if (flow.getActualPerson() != null) {
			flow.getActualPerson().setFlowId(flow.getId());
			flowDao.savePerson(flow.getActualPerson(), PersonTYPE.ACTUAL);
		}
		if (flow.getContent() != null) {
			flow.getContent().setFlowId(flow.getId());
			flowDao.saveContent(flow.getContent());
		}
		if (saveAttachment) {
			if (flow.getFlowAttachments() != null) {
				for (int i = 0; i < flow.getFlowAttachments().length; i++) {
					flow.getFlowAttachments()[i].setFlowId(flow.getId());
				}
				flowDao.saveAttachments(flow.getFlowAttachments());
			}
		}
	}

	@Override
	public void saveFlowAttachment(FlowAttachment attachment) {
		flowDao.saveAttachments(new FlowAttachment[] { attachment });
	}

	@Override
	public void updateFormContentOnShenghe(Flow flow) {
		flowDao.updateFlow(flow);
		this.updateFlowAttachments(flow, flow.getFlowAttachments());
	}

	@Override
	public Map<String, PersonDetail> startNextWork(Flow flow,
			PersonDetail actualPerson, MyWork prevWork) {
		return this.flowStage.startNextWork(flow, actualPerson, prevWork);
	}

	@Override
	public void updateWorkStatus(MyWork work) {
		this.flowDao.updateWork(work);
	}

	@Override
	public void updateFlowStatus(Flow flow) {
		this.flowDao.updateFlow(flow);
	}

	@Override
	public MyWork loadWork(long assignorId) {
		return this.flowDao.loadWork(assignorId);
	}

	@Override
	public Flow[] loadFlowTemplate(String userName, FlowType flowType) {
		return flowDao.loadFlowTemplate(userName, flowType);
	}

	@Override
	public void saveWork(MyWork work) {
		this.flowDao.saveWork(work, organDao);
	}

	public boolean hasJointSignFinished(Flow flow) {
		return this.flowDao.hasJointSignFinished(flow);
	}

	public boolean hasCmpcodeJointSignFinished(Flow flow, String cmpcode) {
		return this.flowDao.hasCmpcodeJointSignFinished(flow, cmpcode);
	}

	@Override
	public Flow loadFlow(String formnum) {
		Flow flow = flowDao.loadFlow(formnum);
		this.loadFlowProperties(flow);
		return flow;
	}

	@Override
	public MyWorkHistory[] listWorkHistory(String formNum) {
		return flowDao.listWorkHistory(formNum);
	}

	@Override
	public MyWorkHistory[] listWorkHistory(String formNum, long parentWorkId) {
		return flowDao.listWorkHistory(formNum, parentWorkId);
	}

	@Override
	public void cancel(MyWork myWork, Flow flow) {
		this.flowStage.cancel(myWork, flow);
	}

	@Override
	public Flow endFlow(Flow flow, MyWork myWork) {
		return this.flowStage.endFlow(flow);
	}

	@Override
	public void reject(MyWork work) {
		// 审核主管上级可以驳回，然后给发起人，发起人看到的是只读的表单，这时候的表单界面右上方有“重新起案”的按钮，
		// 点击后就新生成表单，并且复制了原来驳回表单的内容，然后可以修改表单内容。重起案的表单，提交处理后，原表单自动结案，
		// 不可再做“重新起案”处理；暂存处理时，原表单状态不变，仍可重新起案。
		this.flowStage.reject(work);
	}

	// 目前 只是表单的重新案次数递增一位，难点在于需要保证之前的工作历程，所以目前只是改变状态，然后重新开始. 如果后续有针对性业务场景再考虑变更这块。
	@Override
	public Flow renew(Flow flow, MyWork rejectWork) {
		// 重新起案 rejectWork不能为空，而且状态为REJECT，原表单的重新起案次数只能等于0次
		// 发起人点击后，就重新生成一张新的表单 内容复制原表单内容，然后新表单的重新起案次数递增一位。原表单变成已结案表单。
		if (rejectWork != null && rejectWork.getWorkStage() == WorkStage.REJECT
				&& flow.getRenewTimes() == 0) {
			// Flow newFlow = new Flow();
			// // 复制所有属性，然后保存
			// BeanUtils.copyProperties(flow, newFlow, new String[] {
			// "createPerson", "actualPerson", "content",
			// "status",
			// "decionmaker", "jointSignType", "jointSignDeptIds",
			// "copyDeptIds", "innerJointSignIds",
			// "flowAttachments", "flowType" });
			// newFlow.setCreatePerson(flow.getCreatePerson());
			// newFlow.setActualPerson(flow.getActualPerson());
			// newFlow.setContent(flow.getContent());
			// newFlow.setStatus(flow.getStatus());
			// newFlow.setDecionmaker(flow.getDecionmaker());
			// newFlow.setJointSignType(flow.getJointSignType());
			// newFlow.setJointSignDeptIds(flow.getJointSignDeptIds());
			// newFlow.setCopyDeptIds(flow.getCopyDeptIds());
			// newFlow.setInnerJointSignIds(flow.getInnerJointSignIds());
			// newFlow.setFlowAttachments(flow.getFlowAttachments());
			// newFlow.setFlowType(flow.getFlowType());
			//
			// newFlow.setCreatePerson(flow.getCreatePerson());
			// newFlow.setActualPerson(flow.getActualPerson());
			// newFlow.setContent(flow.getContent());
			// newFlow.setFlowAttachments(flow.getFlowAttachments());

			// newFlow.setFormNum(this.generateFormNum(flow.getActualPerson(),
			// flow.getFlowType()));
			// newFlow.setRenewTimes(1);

			// this.updateWorkStatus(rejectWork);

			flow.setRenewTimes(1);
			// this.flowStage.start(flow);
			return flow;
		}
		return null;
	}

	@Override
	public FlowAttachment loadFlowAttachment(long attachmentId) {
		return flowDao.loadAttachment(attachmentId);
	}

	/**
	 * @see com.wwgroup.flow.service.FlowService#deleteFlowAttachment(long)
	 */
	@Override
	public void deleteFlowAttachment(long attahmentId) {
		flowDao.deleteFlowAttachment(attahmentId);
	}

	@Override
	public void clearFlow(Flow flow) {
		flowDao.clearFlow(flow);
	}

	@Override
	public boolean finishInnerJointSign(long flowId, String employeeId) {
		return flowDao.finishInnerJointSign(flowId, employeeId);
	}

	@Override
	public boolean hasCmpcodeSignt(Flow flow, String cmpCode) {
		if (null == flow.getJointSignDeptIds()
				|| flow.getJointSignDeptIds().length == 0) {
			return false;
		}

		for (int i = 0; i < flow.getJointSignDeptIds().length; i++) {
			String jointSignDeptId = flow.getJointSignDeptIds()[i];
			SystemGroups dept = organDao.loadGroupsById(Integer
					.parseInt(jointSignDeptId));
			if (null != dept && dept.getCmpcod().equals(cmpCode)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean checkPersonHasFinishWork(PersonDetail person, long flowId,
			WorkStage workStage) {
		return flowDao.checkPersonHasFinishWork(person, flowId, workStage);
	}

	@Override
	public MyWork getBossSignFirstWork(long flowId, WorkStage workStage,
			String joinSignStartId) {
		return flowDao.getBossSignFirstWork(flowId, workStage, joinSignStartId);
	}

	@Override
	public Map<String, PersonDetail> adminAssignWork(PersonDetail person,
			MyWork myWork, String adminAssignOldEmployeeId) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		// 修改myWork的当前操作人为person
		myWork.setDeptId(person.getDeptId());
		myWork.setDeptName(person.getDeptName());
		myWork.setEmployeeId(person.getEmployeeId());
		myWork.setPostCode(person.getPostCode());
		myWork.setDeptCode(person.getDeptCode());
		myWork.setA_deptCode(person.getA_deptCode());
		myWork.setCmpCode(person.getCmpCode());

		if (StringUtils.isNotBlank(myWork.getJoinStartEmployeeId())
				&& adminAssignOldEmployeeId.equals(myWork
						.getJoinStartEmployeeId())) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("employeeId", person.getEmployeeId());
			jsonObj.put("deptId", person.getDeptId());
			JSONArray jsonArray = new JSONArray();
			jsonArray.add(jsonObj);

			myWork.setJoinSignStartId(jsonArray.toString());
			myWork.setJoinStartEmployeeId(person.getEmployeeId());
		}

		flowDao.updateAdminAssignWork(myWork);

		result.put(person.getEmployeeId() + person.getPostCode(), person);

		return result;
	}

	@Override
	public MyWork getPrevWork(MyWork myWork) {
		return flowDao.getPrevWork(myWork);
	}

	@Override
	public void updateHbWorkJoin(MyWork work) {
		this.flowDao.updateHbWorkJoin(work);
	}

	public MyWork getFirstHBWork(MyWork myWork) {
		return flowDao.getFirstHBWork(myWork);
	}

	public void updateHbWorkOrgPath(MyWork myWork, MyWork firstWork) {
		this.flowDao.updateHbWorkOrgPath(myWork, firstWork);
	}
	
	public boolean isCompleteAssign(MyWork myWork){
		MyWork parentWork = this.flowDao.getWorkByParentId(myWork);
		if (parentWork != null){
			if (myWork.getPostCode().equals(parentWork.getPostCode())){
				return true;
			}
		}
		return false;
	}
	
	public boolean isCompleteAssign(MyWork myWork, String postCode){
		MyWork parentWork = this.flowDao.getWorkByParentId(myWork);
		if (parentWork != null){
			if (postCode.equals(parentWork.getPostCode())){
				return true;
			}
		}
		return false;
	}
	
	public boolean isTopApprove(Flow flow){
		return this.flowDao.hasTopApprove(flow);
	}
}
