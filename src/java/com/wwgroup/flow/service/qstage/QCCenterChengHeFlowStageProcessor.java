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
import com.wwgroup.flow.bo.work.CmpcodeJoinSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.user.bo.EmployeePos;

/**
 * 签呈中心签核
 * 
 * @author eleven
 * 
 */
public class QCCenterChengHeFlowStageProcessor extends
		QCAbstractFlowStageProcessor {

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + QCCenterChengHeFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 中心/单位主管　doComplete 操作 Start...");
		// 领导审核与传统的 会签等区分 所以这边用了 另外一个字段来表示
		//logger.info(flow.getFormNum() + " 中心/单位主管 work 完成后的状态为: " + FlowStatus.APPROVED + ". Start...");
		work.setStatus(FlowStatus.APPROVED);
		this.flowDao.updateWork(work);
		//logger.info(flow.getFormNum() + " 中心/单位主管 work 完成后的状态为: " + FlowStatus.APPROVED + ". End...");

		// 完成时，将当前审核用户信息写到主记录中
		//logger.info(flow.getFormNum() + " 将当前处理人信息更新至flow中的上一步处理人 Start...");
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		// 此处注意需增加一个方法，用于更新以上四项数据.
		this.flowDao.updateFlowLastPerson(flow);
		//logger.info(flow.getFormNum() + " 将当前处理人信息更新至flow中的上一步处理人 End...");

		//logger.info(flow.getFormNum() + " 根据work中的部门ID、工号、岗位获取人员信息 Start...");
		PersonDetail personDetail = personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		//logger.info(flow.getFormNum() + " 根据work中的部门ID、工号、岗位获取人员信息 End...");
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		//logger.info(flow.getFormNum() + " 中心/单位主管　doComplete 操作 End...");
		return result;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 中心/单位主管　doNext 操作 Start...");
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
			 * 体系内 正常流转应该是：中心主管核准后进入本单位会办
			 * 1、先判断下一步取节点代码和当前节点的的代码是否一致，如果一致，则进行正常是中心主管等判断，如果不一致，则直接进入本单位会办
			 * 2、如果一致，则需要判断上级是否是中心主管（副主管仍需要继续呈核至中心主管）
			 */
			//logger.info(flow.getFormNum() + " 体系内：");
			//logger.info(flow.getFormNum() + " 根据上一步处理人的工号和岗位获取EmployeePos对象信息 Start...");
			EmployeePos employee = userService.getEmployeePosByEmpId(lastPerson
					.getEmployeeId(), lastPerson.getPostCode());
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
			if (flow.getStatus() == flow.getNextStep()) {
				// 判断上级是否核决主管
				//logger.info(flow.getFormNum() + " 上级主管为：" + mgrPerson.getEmployeeId() + "(" + mgrPerson.getPostCode() + ")");
				//logger.info(flow.getFormNum() + " 判断上级主管是否是核决主管 Start...");
				boolean isApproval = personService.quailifiedDecisionMaker(
						flow.getDecionmaker(), mgrPerson);
				//logger.info(flow.getFormNum() + " 判断上级主管是否是核决主管 End...");
				// 判断上级是否核决副主管
				//logger.info(flow.getFormNum() + " 判断上级主管是否是核决副主管 Start...");
				boolean isApprovalF = personService
						.quailifiedDecisionMakerPlus(flow.getDecionmaker(),
								mgrPerson);
				//logger.info(flow.getFormNum() + " 判断上级主管是否是核决副主管 End...");
				/**
				 * 上级是核决副主管或核决主管，那么肯定是要先去进行会办
				 */
				if (isApproval || isApprovalF) {
					//logger.info(flow.getFormNum() + " 上级主管是核决主管或核决副主管 Start...");
					flow.setNextStep(FlowStatus.NEXTFINAL_DECISION_START);
					super.flowDao.updateFlowNextStep(flow);

					// 判断是否存在会办
					if (flow.getPendJointSignDeptIds() != null
							&& flow.getPendJointSignDeptIds().length > 0) {
						//logger.info(flow.getFormNum() + " A:有会办单位，进行本单位会办 Start...");
						flow.setStatus(FlowStatus.CMPCODEJOINTSIGN_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.delegateJointSignWork(flow,
								personDetail));
						//logger.info(flow.getFormNum() + " A:有会办单位，进行本单位会办 End...");
					} else {
						// 没有会办，直接去最高副主管节点。
						//logger.info(flow.getFormNum() + " A:没有会办单位,直接进行副主管审核节点 Start...");
						flow.setStatus(FlowStatus.NEXTFINAL_DECISION_START);
						super.flowDao.updateFlow(flow);
						//logger.info(flow.getFormNum() + " A:没有会办单位 End...");
						result.putAll(this.flowService.startNextWork(flow,
								lastPerson, null));
					}
					logger.info(flow.getFormNum() + " 上级主管是核决主管或核决副主管 End...");
				} else {
					/**
					 * 1、如果当前用户是中心主管，直接进入本单位会办
					 * 2、如果上级是最高副主管或最高主管，与当前用户是中心主管的情况是一样的，直接进入本单位会办
					 * 以上两种情况的下一步骤节点都是最高副主管节点。
					 */
					if (employee.isCentermgr() || mgrEmployee.isTopFmgr()
							|| mgrEmployee.isTopmgr()) {
						flow.setNextStep(FlowStatus.NEXTFINAL_DECISION_START);
						super.flowDao.updateFlowNextStep(flow);
						//logger.info(flow.getFormNum() + " 当前处理人为中心主管或上级是最高副主管/最高主管 Start...");
						// 判断是否存在会办
						if (flow.getPendJointSignDeptIds() != null
								&& flow.getPendJointSignDeptIds().length > 0) {
							//logger.info(flow.getFormNum() + " B:有会办单位，进行本单位会办 Start...");
							flow.setStatus(FlowStatus.CMPCODEJOINTSIGN_START);
							super.flowDao.updateFlow(flow);
							result.putAll(this.delegateJointSignWork(flow,
									personDetail));
							//logger.info(flow.getFormNum() + " B:有会办单位，进行本单位会办 End...");
						} else {
							// 没有会办，直接去最高副主管节点。
							//logger.info(flow.getFormNum() + " B:没有会办单位,直接进行副主管审核节点 Start...");
							flow.setStatus(FlowStatus.NEXTFINAL_DECISION_START);
							super.flowDao.updateFlow(flow);
							result.putAll(this.flowService.startNextWork(flow,
									lastPerson, null));
							//logger.info(flow.getFormNum() + " B:没有会办单位 End...");
						}
						//logger.info(flow.getFormNum() + " 当前处理人为中心主管或上级是最高副主管/最高主管 End...");
					} else {
						/**
						 * 这里剩下的就是当前用户不是中心主管，且上一级不是中心以上主管
						 * 目前肯定这种情况下，当前用户肯定就是中心副主管 所以需要继续向上呈核
						 */
						//logger.info(flow.getFormNum() + " 当前用户即不是中心主管且上级不是中心以上主管 Start...");
						flow.setNextStep(FlowStatus.CENTER_CHENGHE_START);
						super.flowDao.updateFlowNextStep(flow);
						MyWork work = mgrPerson.buildWork(flow.getFlowType(),
								WorkStage.CENTER_CHENGHE, null);
						work.setFlowId(flow.getId());
						
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
						result.put(
								mgrPerson.getEmployeeId()
										+ mgrPerson.getPostCode(), mgrPerson);
						logger.info(flow.getFormNum() + " 当前用户即不是中心主管且上级不是中心以上主管 End...");
					}
				}
			} else {
				// 不一致，直接进入本单位会办
				// 判断是否存在会办
				// 这里判断是否存在会办就不能用原来存储选择的列表，而是未完成的会办列表
				if (flow.getPendJointSignDeptIds() != null
						&& flow.getPendJointSignDeptIds().length > 0) {
					//logger.info(flow.getFormNum() + " C:有会办单位，进行本单位会办 Start...");
					flow.setStatus(FlowStatus.CMPCODEJOINTSIGN_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.delegateJointSignWork(flow,
							personDetail));
					//logger.info(flow.getFormNum() + " C:有会办单位，进行本单位会办 End...");
				} else {
					// 没有会办，直接进入相应的节点去审批。目前来说只有体系内才存在这种情况
					// ，当前审核节点和下一步审核节点不一致，下一步肯定是去到核决副主管节点。
					//logger.info(flow.getFormNum() + " C:没有会办单位,直接进行副主管审核节点 Start...");
					flow.setStatus(flow.getNextStep());
					super.flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow,
							lastPerson, null));
					//logger.info(flow.getFormNum() + " C:没有会办单位 End...");
				}
			}
		} else {
			/**
			 * 地方至总部
			 */
			//logger.info(flow.getFormNum() + " 地方至总部 Start...");
			//logger.info(flow.getFormNum() + " 根据上一步处理人的工号和岗位获取EmployeePos对象信息 Start...");
			EmployeePos employee = userService.getEmployeePosByEmpId(lastPerson
					.getEmployeeId(), lastPerson.getPostCode());
			//logger.info(flow.getFormNum() + " 根据上一步处理人的工号和岗位获取EmployeePos对象信息 End...");
			// 是中心主管
			if (employee.isCentermgr()) {
				flow.setNextStep(FlowStatus.NEXTCHENGHE_START);
				super.flowDao.updateFlowNextStep(flow);

				// 判断是否存在会办
				// 这里判断是否存在会办就不能用原来存储选择的列表，而且未完成的会办列表
				if (flow.getPendJointSignDeptIds() != null
						&& flow.getPendJointSignDeptIds().length > 0) {
					//logger.info(flow.getFormNum() + " D:有会办单位，进行本单位会办 Start...");
					flow.setStatus(FlowStatus.CMPCODEJOINTSIGN_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.delegateJointSignWork(flow,
							personDetail));
					logger.info(flow.getFormNum() + " D:有会办单位，进行本单位会办 End...");
				} else {
					// 没有会办，直接进入相应的节点去审批。目前来说只有体系内才存在这种情况
					// ，当前审核节点和下一步审核节点不一致，下一步肯定是去到核决副主管节点。
					//logger.info(flow.getFormNum() + " D:没有会办单位,直接进入地方单位最高主管节点 Start...");
					flow.setStatus(FlowStatus.NEXTCHENGHE_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow,
							lastPerson, null));
					//logger.info(flow.getFormNum() + " D:没有会办单位 End...");
				}

			} else {
				// 不是中心主管
				// 获取上级主管
				//logger.info(flow.getFormNum() + " 获取上级主管信息PersonDetail对象 Start...");
				PersonDetail mgrPerson = personService
						.getMgrPersonDetail(lastPerson);
				//logger.info(flow.getFormNum() + " 获取上级主管信息PersonDetail对象 End...");
				//logger.info(flow.getFormNum() + " 获取上级主管信息EmployeePos对象 Start...");
				EmployeePos mgrEmployee = userService.getEmployeePosByEmpId(
						mgrPerson.getEmployeeId(), mgrPerson.getPostCode());
				//logger.info(flow.getFormNum() + " 获取上级主管信息EmployeePos对象 End...");
				// 如果上级是单位最高副主管或单位最高主管，一样先进入本单位会办
				if (mgrEmployee.isTopFmgr() || mgrEmployee.isTopmgr()) {
					flow.setNextStep(FlowStatus.NEXTCHENGHE_START);
					super.flowDao.updateFlowNextStep(flow);

					// 判断是否存在会办
					// 这里判断是否存在会办就不能用原来存储选择的列表，而且未完成的会办列表
					if (flow.getPendJointSignDeptIds() != null
							&& flow.getPendJointSignDeptIds().length > 0) {
						//logger.info(flow.getFormNum() + " E:有会办单位，进行本单位会办 Start...");
						flow.setStatus(FlowStatus.CMPCODEJOINTSIGN_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.delegateJointSignWork(flow,
								personDetail));
						//logger.info(flow.getFormNum() + " E:有会办单位，进行本单位会办 End...");
					} else {
						// 没有会办，直接进入相应的节点去审批。目前来说只有体系内才存在这种情况
						// ，当前审核节点和下一步审核节点不一致，下一步肯定是去到核决副主管节点。
						//logger.info(flow.getFormNum() + " E:没有会办单位,直接进入地方单位最高主管节点 Start...");
						flow.setStatus(FlowStatus.NEXTCHENGHE_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.flowService.startNextWork(flow,
								lastPerson, null));
						//logger.info(flow.getFormNum() + " E:没有会办单位 End...");
					}
				} else {
					// 继续呈核至中心主管
					//logger.info(flow.getFormNum() + " 继续汇报至中心主管呈核节点 Start...");
					flow.setNextStep(FlowStatus.CENTER_CHENGHE_START);
					super.flowDao.updateFlowNextStep(flow);

					MyWork work = mgrPerson.buildWork(flow.getFlowType(),
							WorkStage.CENTER_CHENGHE, null);
					work.setFlowId(flow.getId());
					
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
					result.put(
							mgrPerson.getEmployeeId() + mgrPerson.getPostCode(),
							mgrPerson);
					//logger.info(flow.getFormNum() + " 继续汇报至中心主管呈核节点 End...");
				}
			}
			//logger.info(flow.getFormNum() + " 地方至总部 Start...");
		}
		//logger.info(flow.getFormNum() + " 中心/单位主管　doNext 操作 End...");
		return result;
	}

	@Override
	Map<? extends String, ? extends PersonDetail> doStart(Flow flow) {
		return new HashMap<String, PersonDetail>(2);
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + QCCenterChengHeFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.CENTER_CHENGHE_START;
	}

	@Override
	boolean startValidate(Flow flow) {
		return false;
	}

	/**
	 * 这里的会办要改造 从未完成的会办列表中抓取本单位的会办单位进行会办
	 * 
	 * @param flow
	 * @param personDetail
	 * @param prevWork
	 * @return
	 */
	private Map<String, PersonDetail> delegateJointSignWork(Flow flow,
			PersonDetail personDetail) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 进行本单位会办的判断及创建 Start...");
		if (flow.getPendJointSignDeptIds() != null
				&& flow.getPendJointSignDeptIds().length > 0) {
			// 获取申请人信息
			//logger.info(flow.getFormNum() + " 获取申请人PersonDetail对象信息 Start...");
			PersonDetail actualPerson = this.personService
					.loadWidePersonDetail(flow.getActualPerson()
							.getEmployeeId());
			//logger.info(flow.getFormNum() + " 获取申请人PersonDetail对象信息 End...");

			String tmpNoSignDeptIds = "";
			String[] jointSignDeptIds = flow.getPendJointSignDeptIds();
			int count = -1;
			for (int i = 0; i < jointSignDeptIds.length; i++) {
				String jointSignDeptId = jointSignDeptIds[i];
				//logger.info(flow.getFormNum() + " 判断会办单位与申请人是否同一公司 " + jointSignDeptId + " Start...");
				PersonDetail person = personService
						.loadWideMgrPersonDetail(jointSignDeptId);
				SystemGroups tmpGroups = flowService.loadGroupsById(Integer.valueOf(jointSignDeptId));
				/**
				 * 这里取到的是未完成会办的单位，如果进行过中心会办，那么这里肯定就不再存在了，所以仅需根据单位来判断即可。
				 */
				if (person.getCmpCode().equals(actualPerson.getCmpCode()) && !tmpGroups.getSystemFlg().equals("Y")) {
					//logger.info(flow.getFormNum() + " 创建对应的work对象，此时并不保存至数据库 Start...");
					CmpcodeJoinSignWork work = (CmpcodeJoinSignWork) person
							.buildWork(flow.getFlowType(),
									WorkStage.CMPCODEJOINTSIGN, null);
					
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("employeeId", person.getEmployeeId());
					jsonObj.put("deptId", person.getDeptId());
					JSONArray jsonArray = new JSONArray();
					jsonArray.add(jsonObj);

					work.setJoinSignStartId(jsonArray.toString());
					work.setWorknum(work.getId() + count);
					work.setFlowId(flow.getId());
					work.setJoinCycle(jsonArray.size());
					work.setJoinStartEmployeeId(person.getEmployeeId());
					this.flowService.startNextWork(flow, person, work);

					result.put(
							person.getEmployeeId() + person.getPostCode(),
							person);
					//logger.info(flow.getFormNum() + " 创建对应的work对象，此时并不保存至数据库 End...");
					count--;
				} else {
					if (StringUtils.isEmpty(tmpNoSignDeptIds)){
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
			
			if (count == -1){
				// 没有本单位会办
				//logger.info(flow.getFormNum() + " 这里容错，如果没有本公司的会办，则进入CHENHE_START节点，Start...");
				flow.setStatus(FlowStatus.NEXTCHENGHE_START);
				flowDao.updateFlow(flow);
				result.putAll(this.flowService.startNextWork(flow,
						personDetail, null));
				//logger.info(flow.getFormNum() + " 这里容错，如果没有本公司的会办，则进入CHENHE_START节点，End...");
			}
		}
		//logger.info(flow.getFormNum() + " 进行本单位会办的判断及创建 End...");
		return result;
	}
}
