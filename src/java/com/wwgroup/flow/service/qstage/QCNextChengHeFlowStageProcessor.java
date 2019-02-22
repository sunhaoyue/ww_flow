package com.wwgroup.flow.service.qstage;

import java.util.HashMap;
import java.util.Map;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.user.bo.EmployeePos;

/**
 * 签呈地方单位最高副主管 这里和核决是有区别的 
 * 1、地方至总部：这是的副主管仅做签核操作，不用去判断会办 2、体系内：仅仅是一个过渡
 * 
 * @author eleven
 * 
 */
// 驳回逻辑：驳回动作只有在审核阶段才能够发送，
public class QCNextChengHeFlowStageProcessor extends QCAbstractFlowStageProcessor {

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
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " startNextValidate: " + QCNextChengHeFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.NEXTCHENGHE_START;
	}

	@Override
	Map<String, PersonDetail> doNext(Flow flow, PersonDetail personDetail,
			MyWork prevWork) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 地方单位最高副主管　doNext 操作 Start...");
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
			//logger.info(flow.getFormNum() + " 体系内（地方单位最高副主管，过渡节点）：");
			// 判断是否存在会办
			//logger.info(flow.getFormNum() + " 直接进入地方单位最高主管审核节点 Start...");
			flow.setStatus(FlowStatus.CHENGHE_START);
			super.flowDao.updateFlow(flow);
			result.putAll(this.flowService.startNextWork(flow, lastPerson,
					null));
			//logger.info(flow.getFormNum() + " 直接进入地方单位最高主管审核节点  End...");
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
			/**
			 * 地方至总部签呈流转至此处，只可能存在最高副主管和主管了。这里是不需要进行会办的判断，因为最高主管还没签
			 * 此处需判断上一步处理人信息
			 * 1、如果上一步本身就是最高主管了，那么此主就不需要审核，直接进入最高主管节点
			 * 2、如果上一步本身不是最高主管，却是最高副主管，则需要进行一步签核操作；
			 * 3、如果即不是最高主管，也不是最高副主管，那么就要在此继续往上呈核，直到它是最高副主管
			 */
			if (employee.isTopmgr() || employee.isTopFmgr() || mgrEmployee.isTopmgr()){
				// 如果上一步处理人本身就是最高主管或最高副主管了，则下一步主管应该是最高主管签核节点
				flow.setNextStep(FlowStatus.CHENGHE_START);
				super.flowDao.updateFlowNextStep(flow);
				
				flow.setStatus(FlowStatus.CHENGHE_START);
				super.flowDao.updateFlow(flow);
				
				result.putAll(this.flowService.startNextWork(flow, lastPerson, null));
			} else {
				// 如果上一步处理人即不是最高主管也不是最高副主管，那么就继续呈核上一级
				//logger.info(flow.getFormNum() + " 继续汇报至地方单位最高副主管呈核节点 Start...");
				flow.setNextStep(FlowStatus.NEXTCHENGHE_START);
				super.flowDao.updateFlowNextStep(flow);
				MyWork work = mgrPerson.buildWork(flow.getFlowType(), WorkStage.FCHENGHE, null);
				work.setFlowId(flow.getId());
				
				// 获取当前处理人信息
				PersonDetail tmpPerson = personService.loadWidePersonDetail(work.getDeptId(), work.getEmployeeId(), work.getPostCode());
				work.setEmployeenam(tmpPerson.getName());
				work.setTitlenam(tmpPerson.getTitname());
				/*logger.info(flow.getFormNum() + " 继续进行地方单位最高副主管签核,创建work对象并保存至数据库(insert.)"
						+ work.getEmployeeId() + "("
						+ work.getFlowId() + "、" + work.getDeptId() + ")" + "Start...");*/
				flowDao.saveWork(work, flowService.getOrganDao());
				//logger.info(flow.getFormNum() + " 继续进行地方单位最高副主管签核,创建work对象并保存至数据库. End...");
				result.put(
						mgrPerson.getEmployeeId() + mgrPerson.getPostCode(),
						mgrPerson);
				
				//logger.info(flow.getFormNum() + " 继续汇报至地方单位最高副主管呈核节点 End...");
			}
		}
		//logger.info(flow.getFormNum() + " 地方单位最高主管　doNext 操作 End...");
		return result;
	}

	@Override
	boolean completeValidate(Flow flow) {
		//logger.info(flow.getFormNum() + " 状态：" + flow.getStatus() + " completeValidate: " + QCNextChengHeFlowStageProcessor.class.getName());
		return flow.getStatus() == FlowStatus.NEXTCHENGHE_START;
	}

	@Override
	Map<String, PersonDetail> doComplete(Flow flow, MyWork work) {
		Map<String, PersonDetail> result = new HashMap<String, PersonDetail>(2);
		//logger.info(flow.getFormNum() + " 地方单位最高副主管　doComplete 操作 Start...");
		// 领导审核与传统的 会签等区分 所以这边用了 另外一个字段来表示
		//logger.info(flow.getFormNum() + " 地方单位最高副主管 work 完成后的状态为: " + FlowStatus.APPROVED + ". Start...");
		work.setStatus(FlowStatus.APPROVED);
		this.flowDao.updateWork(work);
		//logger.info(flow.getFormNum() + " 地方单位最高副主管 work 完成后的状态为: " + FlowStatus.APPROVED + ". End...");

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
		//logger.info(flow.getFormNum() + " 地方单位最高副主管　doComplete 操作 End...");
		return result;
	}

	@Override
	boolean cancelValidate(Flow flow) {
		return flow.getStatus() == FlowStatus.NEXTCHENGHE_START;
	}

	@Override
	boolean rejectValidate(Flow flow, MyWork work) {
		return flow.getStatus() == FlowStatus.NEXTCHENGHE_START;
	}

}
