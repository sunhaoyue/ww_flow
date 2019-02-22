package com.wwgroup.flow.service.qstage;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.bo.work.SystemJoinSignWork;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.user.bo.EmployeePos;

/**
 * 签呈最高主管 这里和核决是有区别的 1、地方至总部：这里需要进行相关的逻辑判断 2、体系内：仅仅是一个过渡
 * 
 * @author eleven
 * 
 */
// 驳回逻辑：驳回动作只有在审核阶段才能够发送，
public class QCChengHeFlowStageProcessor extends QCAbstractFlowStageProcessor {

	@Override
	boolean startValidate(Flow flow) {
		return false;
	}

	@Override
	Map<String, PersonDetail> doStart(Flow flow) {
		return new HashMap<String, PersonDetail>(2);
	}

	@Override
	boolean startNextValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + QCChengHeFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CHENGHE_START;
	}

	@SuppressWarnings("unused")
	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 地方单位最高主管　doNext 操作 Start...");
		
		// 如果prevWork中的startEmployeeId不为空，代表是指派的流转过程
		if (prevWork != null && StringUtils.isNotEmpty(prevWork.getJoinSignStartId())){
			logger.info(flow.getFormNum() + " 地方单位最高主管指派代码片段 Start...");
			MyWork work = personDetail.buildWork(flow.getFlowType(), WorkStage.CHENGHE, prevWork);
			work.setJoinSignStartId(prevWork.getJoinSignStartId());
			work.setJoinCycle(prevWork.getJoinCycle());
			work.setJoinStartEmployeeId(prevWork.getJoinStartEmployeeId());
			work.setFlowId(flow.getId());
			work.setParentId(prevWork.getParentId());
			
			// 获取当前处理人信息
			PersonDetail tmpPerson = personService.loadWidePersonDetail(
					work.getDeptId(), work.getEmployeeId(),
					work.getPostCode());
			work.setEmployeenam(tmpPerson.getName());
			work.setTitlenam(tmpPerson.getTitname());
			/*logger.info(flow.getFormNum() + " 保存指派的work对象，这里是insert. " + work.getEmployeeId() + "("
					+ work.getFlowId() + "、" + work.getDeptId() + ")" + " Start...");*/
			flowDao.saveWork(work, flowService.getOrganDao());
			//logger.info(flow.getFormNum() + " 保存指派的work对象，这里是insert. End...");
			result.put(
					personDetail.getEmployeeId() + personDetail.getPostCode(),
					personDetail);
			
			//logger.info(flow.getFormNum() + " 地方单位最高主管指派代码片段 End...");
			return result;
		}
		
		PersonDetail lastPerson = null;
		if (flow.getLastEmployeeId() != null) {
			//logger.info(flow.getFormNum() + " 获取上一步的处理人信息 Start...");
			lastPerson = super.personService.loadWidePersonDetail(
					flow.getLastDeptId(), flow.getLastEmployeeId(),
					flow.getLastPostCode());
			//logger.info(flow.getFormNum() + " 获取上一步的处理人信息 End...");
		}
		if (lastPerson == null) {
			throw new RuntimeException("当前处理人或下一步处理人汇报关系维护有误，请联系系统管理员处理");
		}

		if (flow.isLocal()) {
			/**
			 * 体系内 1、判断下一步节点代码和当前节点代码是否一致 体系内走到这一步，仅仅是进行一下过渡去做下一步的会办操作。
			 * 因为体系内如果走中心主管签核完后，虽然是最高副主管或最高主管，但他们肯定就是核决人了。
			 * 所以这里直接判断是否存在会办，这里的会办暂时不考虑体系内外之分
			 */
			flow.setNextStep(FlowStatus.NEXTFINAL_DECISION_START);
			super.flowDao.updateFlowNextStep(flow);
			//logger.info(flow.getFormNum() + " 体系内：");
			// 判断是否存在会办
			if (flow.getPendJointSignDeptIds() != null
					&& flow.getPendJointSignDeptIds().length > 0) {
				//logger.info(flow.getFormNum() + " A:有会办单位，进行体系单位会办 Start...");
				flow.setStatus(FlowStatus.SYSTEMJOINTSIGN_START);
				super.flowDao.updateFlow(flow);
				result.putAll(this.delegateJointSignWork(flow, personDetail));
				//logger.info(flow.getFormNum() + " A:有会办单位，进行体系单位会办 End...");
			} else {
				// 没有会办，去事业部副主管节点(仅过渡)。
				//logger.info(flow.getFormNum() + " A:没有会办单位,直接进行事业部副主管审核节点 Start...");
				flow.setStatus(FlowStatus.NEXTBUSINESS_DECISION_START);
				super.flowDao.updateFlow(flow);
				result.putAll(this.flowService.startNextWork(flow, lastPerson,
						null));
				//logger.info(flow.getFormNum() + " A:没有会办单位,直接进行事业部副主管审核节点 End...");
			}
		} else {
			/**
			 * 地方至总部
			 */
			//logger.info(flow.getFormNum() + " 地方至总部 Start...");
			// DescionMaker
			//logger.info(flow.getFormNum() + " 根据上一步处理人的工号和岗位获取EmployeePos对象信息 Start...");
			EmployeePos employee = userService.getEmployeePosByEmpId(
					lastPerson.getEmployeeId(), lastPerson.getPostCode());
			//logger.info(flow.getFormNum() + " 根据上一步处理人的工号和岗位获取EmployeePos对象信息 End...");
			// 获取上级主管
			//logger.info(flow.getFormNum() + " 获取上一步处理人的上级主管信息PersonDetail对象 Start...");
			PersonDetail mgrPerson = personService
					.getMgrPersonDetail(lastPerson);
			//logger.info(flow.getFormNum() + " 获取上一步处理人的上级主管信息PersonDetail对象 End...");
			//logger.info(flow.getFormNum() + " 获取上级主管EmployeePos对象信息(通过工号和岗位代码) Start...");
			EmployeePos mgrEmployee = userService.getEmployeePosByEmpId(
					mgrPerson.getEmployeeId(), mgrPerson.getPostCode());
			//logger.info(flow.getFormNum() + " 获取上级主管EmployeePos对象信息 End...");
			if (employee.isTopmgr()) {
				// 如果上一步的审核人是最高主管了，那么下一步主线就是事业部
				flow.setNextStep(FlowStatus.NEXTBUSINESS_DECISION_START);
				super.flowDao.updateFlowNextStep(flow);

				// 判断是否存在会办
				// 这里判断是否存在会办就不能用原来存储选择的列表，而且未完成的会办列表
				if (flow.getPendJointSignDeptIds() != null
						&& flow.getPendJointSignDeptIds().length > 0) {
					//logger.info(flow.getFormNum() + " B:有会办单位，进行体系单位会办 Start...");
					flow.setStatus(FlowStatus.SYSTEMJOINTSIGN_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this
							.delegateJointSignWork(flow, personDetail));
					//logger.info(flow.getFormNum() + " B:有会办单位，进行体系单位会办 End...");
				} else {
					// 没有会办，直接进入相应的节点去审批。目前来说只有体系内才存在这种情况
					// ，当前审核节点和下一步审核节点不一致，下一步肯定是去到核决副主管节点。
					//logger.info(flow.getFormNum() + " B:没有会办单位,直接进行事业部副主管审核节点 Start...");
					flow.setStatus(FlowStatus.NEXTBUSINESS_DECISION_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow,
							lastPerson, null));
					//logger.info(flow.getFormNum() + " B:没有会办单位,直接进行事业部副主管审核节点 End...");
				}
			} else {
				// 继续呈核至地方单位最高主管
				//logger.info(flow.getFormNum() + " 继续汇报至地方单位最高主管呈核节点 Start...");
				flow.setNextStep(FlowStatus.CHENGHE_START);
				super.flowDao.updateFlowNextStep(flow);

				MyWork work = mgrPerson.buildWork(flow.getFlowType(),
						WorkStage.CHENGHE, null);
				work.setFlowId(flow.getId());
				
				work.setJoinCycle(work.getJoinCycle());
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("employeeId", mgrPerson.getEmployeeId());
				jsonObj.put("deptId", mgrPerson.getDeptId());
				JSONArray jsonArray = new JSONArray();
				jsonArray.add(jsonObj);

				work.setJoinSignStartId(jsonArray.toString());
				work.setJoinStartEmployeeId(work.getEmployeeId());

				// 获取当前处理人信息
				PersonDetail tmpPerson = personService.loadWidePersonDetail(
						work.getDeptId(), work.getEmployeeId(),
						work.getPostCode());
				work.setEmployeenam(tmpPerson.getName());
				work.setTitlenam(tmpPerson.getTitname());
				/*logger.info(flow.getFormNum() + " 继续进行中心主管签核,创建work对象并保存至数据库(insert.)"
						+ work.getEmployeeId() + "("
						+ work.getFlowId() + "、" + work.getDeptId() + ")" + "Start...");*/
				flowDao.saveWork(work, flowService.getOrganDao());
				//logger.info(flow.getFormNum() + " 继续进行中心主管签核,创建work对象并保存至数据库. End...");
				result.put(mgrPerson.getEmployeeId() + mgrPerson.getPostCode(),
						mgrPerson);
				//logger.info(flow.getFormNum() + " 继续汇报至中心主管呈核节点 End...");
			}
		}
		//logger.info(flow.getFormNum() + " 地方单位最高主管　doNext 操作 End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + QCChengHeFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CHENGHE_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		// System.out.println("ChengHe");
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 地方单位最高主管　doComplete 操作 Start...");
		if (!StringUtils.isEmpty(work.getJoinStartEmployeeId()) 
				
				//&& !work.getEmployeeId().equals(work.getJoinStartEmployeeId())) {
				&& !this.flowService.isCompleteAssign(work)){
			//logger.info(flow.getFormNum() + " 地方单位最高主管指派代码片段 Start...");
			work.setStatus(FlowStatus.AGREE);
			this.flowService.updateWorkStatus(work);
			PersonDetail personDetail = personService.loadWidePersonDetail(
					work.getDeptId(), work.getEmployeeId(), work.getPostCode());
			PersonDetail mgrPersonDetail = personService
					.getMgrPersonDetail(personDetail);
			result.putAll(this.flowService.startNextWork(flow, mgrPersonDetail,
					work));
			//logger.info(flow.getFormNum() + " 地方单位最高主管指派代码片段 End...");
			return result;
		}
		
		
		// 领导审核与传统的 会签等区分 所以这边用了 另外一个字段来表示
		//logger.info(flow.getFormNum() + " 地方单位最高主管 work 完成后的状态为: " + FlowStatus.APPROVED + ". Start...");
		work.setStatus(FlowStatus.APPROVED);
		this.flowDao.updateWork(work);
		//logger.info(flow.getFormNum() + " 地方单位最高主管 work 完成后的状态为: " + FlowStatus.APPROVED + ". End...");

		// 完成时，将当前审核用户信息写到主记录中
		//logger.info(flow.getFormNum() + " 将当前处理人信息更新至flow中的上一步处理人 Start...");
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		// 此处注意需增加一个方法，用于更新以上数据.
		this.flowDao.updateFlowLastPerson(flow);
		//logger.info(flow.getFormNum() + " 将当前处理人信息更新至flow中的上一步处理人 End...");
		
		PersonDetail personDetail = personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		//logger.info(flow.getFormNum() + " 地方单位最高主管　doComplete 操作 End...");
		return result;
	}

	private Map<String, PersonDetail> delegateJointSignWork(Flow flow,
			PersonDetail personDetail) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 进行体系内会办的判断及创建 Start...");
		// 这里暂时不考虑体系内外之分，所有剩余会办都在此处完成
		if (flow.getPendJointSignDeptIds() != null
				&& flow.getPendJointSignDeptIds().length > 0) {

			// 获取申请人组织信息
			//logger.info(flow.getFormNum() + " 获取申请人PersonDetail对象信息 Start...");
			SystemGroups actualUserG = this.flowService.loadGroupsById(Integer
					.valueOf(flow.getActualPerson().getDeptId()));
			//logger.info(flow.getFormNum() + " 获取申请人PersonDetail对象信息 End...");

			String tmpNoSignDeptIds = "";
			String[] jointSignDeptIds = flow.getPendJointSignDeptIds();
			int count = -1;
			for (int i = 0; i < jointSignDeptIds.length; i++) {
				String jointSignDeptId = jointSignDeptIds[i];
				// 循环获取未完成会办列表的组织信息
				//logger.info(flow.getFormNum() + " 根据部门ID(" + jointSignDeptId + ")获取SystemGroups对象 Start...");
				SystemGroups tmpGroups = this.flowService
						.loadGroupsById(Integer.valueOf(jointSignDeptId));
				//logger.info(flow.getFormNum() + " 根据部门ID(" + jointSignDeptId + ")获取SystemGroups对象 End...");
				// 这里去判断会办单位与申请人是否同一体系，这里还没有，所以用一个假的判断
				// 如果会办部门所属体系与申请人所属体系相同，则进入会办
				if (tmpGroups.getSystemFlg() != null
						&& tmpGroups.getSystemFlg().equals(
								actualUserG.getSystemFlg()) && !tmpGroups.getSystemFlg().equals("Y")) {
					//logger.info(flow.getFormNum() + " 根据部门ID(" + jointSignDeptId + ")获取主管PersonDetail对象信息 Start...");
					PersonDetail person = personService
							.loadWideMgrPersonDetail(jointSignDeptId);
					//logger.info(flow.getFormNum() + " 根据部门ID(" + jointSignDeptId + ")获取主管PersonDetail对象信息 End...");
					SystemJoinSignWork work = (SystemJoinSignWork) person
							.buildWork(flow.getFlowType(),
									WorkStage.SYSTEMJOINTSIGN, null);

					JSONObject jsonObj = new JSONObject();
					jsonObj.put("employeeId", person.getEmployeeId());
					jsonObj.put("deptId", person.getDeptId());
					JSONArray jsonArray = new JSONArray();
					jsonArray.add(jsonObj);

					work.setJoinSignStartId(jsonArray.toString());
					work.setWorknum(work.getId() + i);
					work.setFlowId(flow.getId());
					work.setJoinCycle(jsonArray.size());
					work.setJoinStartEmployeeId(person.getEmployeeId());
					this.flowService.startNextWork(flow, person, work);

					result.put(person.getEmployeeId() + person.getPostCode(),
							person);

					count--;
				} else {
					if (StringUtils.isEmpty(tmpNoSignDeptIds)) {
						tmpNoSignDeptIds = jointSignDeptId;
					} else {
						tmpNoSignDeptIds += ";" + jointSignDeptId;
					}
				}
			}
			// 更新未会办的单位列表
			//logger.info(flow.getFormNum() + " 更新剩余的会办单位字段信息 Start...");
			super.flowDao.updateNoJointSignDeptIds(tmpNoSignDeptIds, flow);
			//logger.info(flow.getFormNum() + " 更新剩余的会办单位字段信息 End...");
			
			if (count == -1) {
				// 没有本单位会办
				//logger.info(flow.getFormNum() + " 这里容错，如果没有体系内的会办，则进入NEXTBUSINESS_DECISION_START节点，Start...");
				flow.setStatus(FlowStatus.NEXTBUSINESS_DECISION_START);
				flowDao.updateFlow(flow);
				result.putAll(this.flowService.startNextWork(flow,
						personDetail, null));
				//logger.info(flow.getFormNum() + " 这里容错，如果没有体系内的会办，则进入NEXTBUSINESS_DECISION_START节点，End...");
			}
		}
		//logger.info(flow.getFormNum() + " 进行体系内会办的判断及创建 End...");
		return result;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CHENGHE_START;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.CHENGHE_START;
	}

}
