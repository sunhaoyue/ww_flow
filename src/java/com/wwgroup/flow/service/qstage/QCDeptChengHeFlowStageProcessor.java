package com.wwgroup.flow.service.qstage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.CenterJoinSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.user.bo.EmployeePos;

/**
 * 签呈的部门审核
 * 
 * @author eleven
 * 
 */
public class QCDeptChengHeFlowStageProcessor extends
		QCAbstractFlowStageProcessor {

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.DEPT_CHENGHE_START;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.DEPT_CHENGHE_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 部门主管　doComplete 操作 Start...");
		work.setStatus(FlowStatus.APPROVED);
		this.flowDao.updateWork(work);
		
		// 完成时，将当前审核用户信息写到主记录中
		//flow.setNextStep(flow.getNextStep());
		flow.setLastEmployeeId(work.getEmployeeId());
		flow.setLastDeptId(work.getDeptId());
		flow.setLastPostCode(work.getPostCode());
		// 此处注意需增加一个方法，用于更新以上四项数据.
		this.flowDao.updateFlowLastPerson(flow);
		
		PersonDetail personDetail = personService.loadWidePersonDetail(
				work.getDeptId(), work.getEmployeeId(), work.getPostCode());
		result.putAll(this.flowService.startNextWork(
				this.flowService.getFlow(work), personDetail, null));
		//logger.info(flow.getFormNum() + " 部门主管　doComplete 操作 End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + QCDeptChengHeFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.DEPT_CHENGHE_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 部门主管　doNext 操作 Start...");
		// 部门主管呈核阶段是不可能有指派的，所以原来代码中的指派流转过程的判断就不加了
		/**
		 * 这里需要判断是体系内还是地方至总部的签呈 1、体系内签呈 需要判断传入用户（第一次传入的是申请人）是否是核决主管，
		 * 如果不是核决主管，则判断本身是否是单位最高副主管或中心主管，因为他们的核决主管肯定就是单位最高主管
		 * 如果不是单位最高副主管或中心主管，则判断是否是中心副主管或部门主管
		 * 
		 */
		EmployeePos employee = userService.getEmployeePosByEmpId(personDetail
				.getEmployeeId(), personDetail.getPostCode());
		if (flow.isLocal()) {
			/**
			 * 体系内：核决主管可能是部门主管/中心主管/单位最高主管 此处目前来说是难点。
			 */
			// 获取上级主管
			PersonDetail mgrPerson = personService
					.getMgrPersonDetail(personDetail);
			//logger.info(flow.getFormNum() + " 当前传入人：" + personDetail.getEmployeeId() + personDetail.getName() + "(" + personDetail.getPostCode() + ")");
			//logger.info(flow.getFormNum() + " 当前传入人的上级：" + mgrPerson.getEmployeeId() + mgrPerson.getName() + "(" + mgrPerson.getPostCode() + ")");
			EmployeePos mgrEmployee = userService.getEmployeePosByEmpId(
					mgrPerson.getEmployeeId(), mgrPerson.getPostCode());
			// 判断上一级是否核决主管
			boolean isApproval = personService.quailifiedDecisionMaker(
					flow.getDecionmaker(), mgrPerson);
			// 判断上一级是否核决副主管
			boolean isApprovalF = personService.quailifiedDecisionMakerPlus(
					flow.getDecionmaker(), mgrPerson);
//System.out.println(flow.getDecionmaker() + " # " + DescionMaker.DEPTLEADER);
//System.out.println(flow.getDecionmaker().equals(DescionMaker.DEPTLEADER));
			/**
			 * 如果核决主管是部门主管，则部门主管要先签，然后进行会办。
			 * 会办完成后，直接进入本人确认
			 * 这里有个特殊情况，如果核决主管是部门主管，有会办则核决主管要签两次，会办前一次，会完完成后核决一次
			 * 没有会办，则直接去核决
			 */
			if (flow.getDecionmaker().equals(DescionMaker.DEPTLEADER)){
				flow.setNextStep(FlowStatus.FINAL_DECISION_START);
				super.flowDao.updateFlowNextStep(flow);
				
				// 判断是否存在会办，如果存在，主管先签
				if (!StringUtils.isEmpty(flow.getJointSignDeptName())){
					if (employee.isDeptmgr()){
						// 如果有会办，则将会办单位的值写入未完成会办单位字段中
						this.setPendJointSignDeptIds(flow);
						
						flow.setStatus(FlowStatus.CENTERJOINTSIGN_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.delegateJointSignWork(flow,
								personDetail));
					} else {
						flow.setNextStep(FlowStatus.DEPT_CHENGHE_START);
						super.flowDao.updateFlowNextStep(flow);
						
						MyWork work = mgrPerson.buildWork(flow.getFlowType(), WorkStage.DEPT_CHENGHE, null);
						work.setFlowId(flow.getId());
						
						// 获取当前处理人信息
						PersonDetail tmpPerson = personService.loadWidePersonDetail(
								work.getDeptId(), work.getEmployeeId(),
								work.getPostCode());
						work.setEmployeenam(tmpPerson.getName());
						work.setTitlenam(tmpPerson.getTitname());
						/*logger.info(flow.getFormNum() + " 继续进行部门主管签核,创建work对象并保存至数据库(insert.)"
								+ work.getEmployeeId() + "("
								+ work.getFlowId() + "、" + work.getDeptId() + ")" + "Start...");*/
						flowDao.saveWork(work, flowService.getOrganDao());
						//logger.info(flow.getFormNum() + " 继续进行部门主管签核,创建work对象并保存至数据库. End...");
						result.put(mgrPerson.getEmployeeId() + mgrPerson.getPostCode(), mgrPerson);
					}
				} else {
					if (isApproval){
						flow.setStatus(FlowStatus.FINAL_DECISION_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.flowService.startNextWork(flow,
								personDetail, null));
					} else {
						flow.setNextStep(FlowStatus.DEPT_CHENGHE_START);
						super.flowDao.updateFlowNextStep(flow);
						
						MyWork work = mgrPerson.buildWork(flow.getFlowType(), WorkStage.DEPT_CHENGHE, null);
						work.setFlowId(flow.getId());
						
						// 获取当前处理人信息
						PersonDetail tmpPerson = personService.loadWidePersonDetail(
								work.getDeptId(), work.getEmployeeId(),
								work.getPostCode());
						work.setEmployeenam(tmpPerson.getName());
						work.setTitlenam(tmpPerson.getTitname());
						/*logger.info(flow.getFormNum() + " 继续进行部门主管签核,创建work对象并保存至数据库(insert.)"
								+ work.getEmployeeId() + "("
								+ work.getFlowId() + "、" + work.getDeptId() + ")" + "Start...");*/
						flowDao.saveWork(work, flowService.getOrganDao());
						//logger.info(flow.getFormNum() + " 继续进行部门主管签核,创建work对象并保存至数据库. End...");
						result.put(mgrPerson.getEmployeeId() + mgrPerson.getPostCode(), mgrPerson);
					}
				}
			} else {
			/**
			 * 1、如果上级是核决的副主管或核决主管了，那么肯定是要先去进行相关的会办。
			 * 2、如果上级是单位最高副主管或单位最高主管了，那么说明最终的核决主管肯定是单位最高主管，所以和上级是核决主管的情况一样。
			 * 以上两种情况的下一步骤节点都是最高副主管节点（即核决副主管）
			 */
			if ((isApprovalF || isApproval || mgrEmployee.isTopFmgr()
					|| mgrEmployee.isTopmgr())) {
				flow.setNextStep(FlowStatus.NEXTFINAL_DECISION_START);
				super.flowDao.updateFlowNextStep(flow);
				
				// 判断是否存在会办，如果存在，先去进行本中心的会办
				if (!StringUtils.isEmpty(flow.getJointSignDeptName())) {
					// 如果有会办，则将会办单位的值写入未完成会办单位字段中
					this.setPendJointSignDeptIds(flow);
					
					flow.setStatus(FlowStatus.CENTERJOINTSIGN_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.delegateJointSignWork(flow,
							personDetail));
				} else {
					// 如果没有会办，则直接去最高副主管节点
					flow.setStatus(FlowStatus.NEXTFINAL_DECISION_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow,
							personDetail, null));
				}
			} else {
				/**
				 * 1、如果当前用户本身就是部门主管，直接进入中心会签
				 * 2、如果上级是中心副主管或中心主管，与当前用户本身是部门管的情况则是一样的，直接进入中心会签
				 * 以上两种情况的下一步骤节点都是中心呈核节点
				 */
				if (employee.isDeptmgr() || mgrEmployee.isCenterFmgr() || mgrEmployee.isCentermgr()){
					flow.setNextStep(FlowStatus.CENTER_CHENGHE_START);
					super.flowDao.updateFlowNextStep(flow);

					// 判断是否存在会办，如果存在，先去进行本中心的会办
					if (!StringUtils.isEmpty(flow.getJointSignDeptName())) {
						// 如果有会办，则将会办单位的值写入未完成会办单位字段中
						this.setPendJointSignDeptIds(flow);
						
						flow.setStatus(FlowStatus.CENTERJOINTSIGN_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.delegateJointSignWork(flow,
								personDetail));
					} else {
						// 如果没有会办，则直接去中心呈核节点
						flow.setStatus(FlowStatus.CENTER_CHENGHE_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.flowService.startNextWork(flow,
								personDetail, null));
					}
				} else {
					/**
					 * 这里剩下的就是当前用户不是部门主管，且上一级不是部门主管以上主管和核决主管（含核决副主管）,
					 * 这里需要继续向上呈核，汇报至部门主管
					 */
					flow.setNextStep(FlowStatus.DEPT_CHENGHE_START);
					super.flowDao.updateFlowNextStep(flow);
					
					MyWork work = mgrPerson.buildWork(flow.getFlowType(), WorkStage.DEPT_CHENGHE, null);
					work.setFlowId(flow.getId());
					
					// 获取当前处理人信息
					PersonDetail tmpPerson = personService.loadWidePersonDetail(
							work.getDeptId(), work.getEmployeeId(),
							work.getPostCode());
					work.setEmployeenam(tmpPerson.getName());
					work.setTitlenam(tmpPerson.getTitname());
					/*logger.info(flow.getFormNum() + " 继续进行部门主管签核,创建work对象并保存至数据库(insert.)"
							+ work.getEmployeeId() + "("
							+ work.getFlowId() + "、" + work.getDeptId() + ")" + "Start...");*/
					flowDao.saveWork(work, flowService.getOrganDao());
					//logger.info(flow.getFormNum() + " 继续进行部门主管签核,创建work对象并保存至数据库. End...");
					result.put(mgrPerson.getEmployeeId() + mgrPerson.getPostCode(), mgrPerson);
				}
			}
			}
		} else {
			/**
			 * 地方至总部，核决主管必定是事业部最高主管或神旺控股最高主管，
			 * 所以在未呈核至事业部主管前，呈核过程肯定是：申请人－内部会签－部门审核－中心会签－中心主管－本单位会办－单位最高主管－事业部
			 * 1、如果申请人是部门主管，则先直接进入中心会签，待中心会签完成后，转至中心审核步骤。
			 * 2、如果申请人不是部门主管，则先判断其上一级是否是中心副主管以上
			 * （虽然地方单位不存在中心副主管）,如果不是，则向上汇报直接找到部门主管
			 * 或其上一级为中心副主管以上。如果是，则根据需要看是否进行中心会签，然后进入中心主管审核步骤。
			 */
			// 如果是部门主管，直接进入下一流程
			if (employee.isDeptmgr()) {
				flow.setNextStep(FlowStatus.CENTER_CHENGHE_START);
				super.flowDao.updateFlowNextStep(flow);
				
				if (!StringUtils.isEmpty(flow.getJointSignDeptName())) {
					// 如果有会办，则将会办单位的值写入未完成会办单位字段中
					this.setPendJointSignDeptIds(flow);
					
					flow.setStatus(FlowStatus.CENTERJOINTSIGN_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.delegateJointSignWork(flow,
							personDetail));
				} else {
					flow.setStatus(FlowStatus.CENTER_CHENGHE_START);
					super.flowDao.updateFlow(flow);
					result.putAll(this.flowService.startNextWork(flow,
							personDetail, null));
				}
			} else {
				// 如果不是部门主管
				// 获取上级主管
				PersonDetail mgrPerson = personService
						.getMgrPersonDetail(personDetail);
				EmployeePos mgrEmployee = userService.getEmployeePosByEmpId(
						mgrPerson.getEmployeeId(), mgrPerson.getPostCode());
				// 如果上级是中心副主管/中心主管/最高副主管/最高主管
				if (mgrEmployee.isCentermgr() || mgrEmployee.isCenterFmgr()
						|| mgrEmployee.isTopFmgr() || mgrEmployee.isTopmgr()) {
					
					flow.setNextStep(FlowStatus.CENTER_CHENGHE_START);
					super.flowDao.updateFlowNextStep(flow);
					
					// 虽然自己不是部门主管，但上级是中心副主管/中心主管/最高副主管/最高主管，则一样先进入申请人所属中心会办
					if (!StringUtils.isEmpty(flow.getJointSignDeptName())) {
						// 如果有会办，则将会办单位的值写入未完成会办单位字段中
						this.setPendJointSignDeptIds(flow);
						
						flow.setStatus(FlowStatus.CENTERJOINTSIGN_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.delegateJointSignWork(flow,
								personDetail));
					} else {
						flow.setStatus(FlowStatus.CENTER_CHENGHE_START);
						super.flowDao.updateFlow(flow);
						result.putAll(this.flowService.startNextWork(flow,
								personDetail, null));
					}
				} else {
					flow.setNextStep(FlowStatus.DEPT_CHENGHE_START);
					super.flowDao.updateFlowNextStep(flow);
					
					MyWork work = mgrPerson.buildWork(flow.getFlowType(),
							WorkStage.DEPT_CHENGHE, null);
					work.setFlowId(flow.getId());
					
					// 获取当前处理人信息
					PersonDetail tmpPerson = personService.loadWidePersonDetail(
							work.getDeptId(), work.getEmployeeId(),
							work.getPostCode());
					work.setEmployeenam(tmpPerson.getName());
					work.setTitlenam(tmpPerson.getTitname());
					/*logger.info(flow.getFormNum() + " 继续进行部门主管签核,创建work对象并保存至数据库(insert.)"
							+ work.getEmployeeId() + "("
							+ work.getFlowId() + "、" + work.getDeptId() + ")" + "Start...");*/
					flowDao.saveWork(work, flowService.getOrganDao());
					//logger.info(flow.getFormNum() + " 继续进行部门主管签核,创建work对象并保存至数据库. End...");
					result.put(
							mgrPerson.getEmployeeId() + mgrPerson.getPostCode(),
							mgrPerson);
				}
			}
		}
		//logger.info(flow.getFormNum() + " 部门主管　doNext 操作 End...");
		return result;
	}

	@Override
	boolean startNextValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + QCDeptChengHeFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.DEPT_CHENGHE_START;
	}
	
	private void setPendJointSignDeptIds(Flow flow){
		/**
		 * 2014-11-17 修改测试
		 *  用于将会办单位字段值写入未完成会办单位字段中
		 */
		String[] jointSignDeptIds = flow.getJointSignDeptIds();
		if (jointSignDeptIds != null && jointSignDeptIds.length > 0){
			String tmpNoJointSignDeptId = "";
			for(int i = 0; i < jointSignDeptIds.length; i++){
				String jointSignDeptId = jointSignDeptIds[i];
				if (StringUtils.isEmpty(tmpNoJointSignDeptId)){
					tmpNoJointSignDeptId = jointSignDeptId;
				} else {
					tmpNoJointSignDeptId += ";" + jointSignDeptId;
				}
			}
			flow.setPendJointSignDeptIds(jointSignDeptIds);
			flowDao.updateNoJointSignDeptIds(tmpNoJointSignDeptId, flow);
		}
	}

	@Override
	Map<? extends String, ? extends PersonDetail> doStart(Flow flow) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);

		boolean isRecreate = flow.getId() > 0;
		boolean hasWorkRecord = false;
		if (isRecreate) {
			hasWorkRecord = this.flowDao.listWorks(flow).length > 0;
		}
		boolean isPreTemplate = flow.isTempalte();
		boolean isSuccess = true;

		flow.setStatus(FlowStatus.DEPT_CHENGHE_START);
		
		if (flow.getInnerJointSignIds() == null) {
			isSuccess = this.flowService.saveFlow(flow);
		}
		
		// 开始前将用户选择的会办列表赋值给未完成会办列表字段中
		// 存在会办才去做这个操作
		/*if (!StringUtils.isEmpty(flow.getJointSignDeptName())) {
			String tmpNoJointSignDeptId = "";
			String[] jointSignDeptIds = flow.getJointSignDeptIds();
			for(int i = 0; i < jointSignDeptIds.length; i++){
				String jointSignDeptId = jointSignDeptIds[i];
				if (StringUtils.isEmpty(tmpNoJointSignDeptId)){
					tmpNoJointSignDeptId = jointSignDeptId;
				} else {
					tmpNoJointSignDeptId += ";" + jointSignDeptId;
				}
			}
			flow.setPendJointSignDeptIds(jointSignDeptIds);
			flowDao.updateNoJointSignDeptIds(tmpNoJointSignDeptId, flow);
		}*/

		if (isSuccess) {
			PersonDetail actualPerson = flow.getActualPerson();
			// 开始前将申请人信息写入临时字段
			flow.setLastEmployeeId(actualPerson.getEmployeeId());
			flow.setLastDeptId(actualPerson.getDeptId());
			flow.setLastPostCode(actualPerson.getPostCode());
			flowDao.updateFlowLastPerson(flow);
			
			// 新的表单或暂存的表单，而且没有work记录的都可以进入
			if (!isRecreate || (isPreTemplate && !hasWorkRecord)) {
				// 添加发起人工作项（自动完成）
				MyWork createWork = actualPerson.buildWork(flow.getFlowType(),
						WorkStage.CREATE, null);
				createWork.setFlowId(flow.getId());
				
				// 获取当前处理人信息
				PersonDetail tmpPerson = personService.loadWidePersonDetail(
						createWork.getDeptId(), createWork.getEmployeeId(),
						createWork.getPostCode());
				createWork.setEmployeenam(tmpPerson.getName());
				createWork.setTitlenam(tmpPerson.getTitname());
				
				flowDao.saveWork(createWork, flowService.getOrganDao());
				createWork
						.setStatus(flow.getRenewTimes() > 0 ? FlowStatus.RECREATE
								: FlowStatus.INIT);
				this.flowService.updateWorkStatus(createWork);
			}
			// 执行doNext
			result.putAll(this.flowService.startNextWork(flow, actualPerson,
					null));
		}

		return result;
	}

	@Override
	boolean startValidate(Flow flow) {
		return true;
	}

	private Map<String, PersonDetail> delegateJointSignWork(Flow flow,
			PersonDetail personDetail) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		if (flow.getJointSignDeptIds() != null
				&& flow.getJointSignDeptIds().length > 0) {
			// 获取申请人信息。因为需要先去会办与申请人同一个中心的单会
			PersonDetail actualPerson = this.personService
					.loadWidePersonDetail(flow.getActualPerson()
							.getEmployeeId());
			// 获取申请人所属中心的组织信息
			SystemGroups actualGroups = this.flowService
					.getRealCenterGroupsById(Integer.valueOf(actualPerson
							.getDeptId()));
			// 获取申请人所属中心下的所有组织
			List<SystemGroups> childGroupList = this.flowService
					.getAllGroupsByParent(actualGroups.getGroupID());
			List<String> childList = new ArrayList<String>();
			for (int i = 0; i < childGroupList.size(); i++) {
				SystemGroups tmpGroups = childGroupList.get(i);
				String tmpGroupId = String.valueOf(tmpGroups.getGroupID());
				if (!childList.contains(tmpGroupId) && !tmpGroups.getSystemFlg().equals("Y")) {
					childList.add(tmpGroupId);
				}
			}
			String tmpNoSignDeptIds = "";
			String[] jointSignDeptIds = flow.getJointSignDeptIds();
			int count = -1;
			for (int i = 0; i < jointSignDeptIds.length; i++) {
				String jointSignDeptId = jointSignDeptIds[i];
				if (childList.contains(jointSignDeptId)) {
					// 通过传入的信息找到部门主管
					PersonDetail person = personService
							.loadWideMgrPersonDetail(jointSignDeptId);
					CenterJoinSignWork work = (CenterJoinSignWork) person
							.buildWork(flow.getFlowType(),
									WorkStage.CENTERJOINTSIGN, null);

					JSONObject jsonObject = new JSONObject();
					jsonObject.put("employeeId", person.getEmployeeId());
					jsonObject.put("deptId", person.getDeptId());
					JSONArray jsonArray = new JSONArray();
					jsonArray.add(jsonObject);

					work.setJoinSignStartId(jsonArray.toString());
					work.setWorknum(work.getId() + count);
					work.setFlowId(flow.getId());
					work.setJoinCycle(jsonArray.size());
					work.setJoinStartEmployeeId(person.getEmployeeId());
					this.flowService.startNextWork(flow, person, work);

					result.put(person.getEmployeeId() + person.getPostCode(),
							person);

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
			super.flowDao.updateNoJointSignDeptIds(tmpNoSignDeptIds, flow);

			if (count == -1) {
				// 没有本中心会办
				flow.setStatus(FlowStatus.CENTER_CHENGHE_START);
				flowDao.updateFlow(flow);
				result.putAll(this.flowService.startNextWork(flow,
						personDetail, null));
			}
		}
		return result;
	}
}
