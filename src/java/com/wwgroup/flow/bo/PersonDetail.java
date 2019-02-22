package com.wwgroup.flow.bo;

import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.work.BusinessSignWork;
import com.wwgroup.flow.bo.work.CancelWork;
import com.wwgroup.flow.bo.work.CenterJoinSignWork;
import com.wwgroup.flow.bo.work.CenterStepByStepWork;
import com.wwgroup.flow.bo.work.ChairmanSignWork;
import com.wwgroup.flow.bo.work.SubmitFBossSignWork;
import com.wwgroup.flow.bo.work.CmpcodeJoinSignWork;
import com.wwgroup.flow.bo.work.ConfirmWork;
import com.wwgroup.flow.bo.work.CopyDeptWork;
import com.wwgroup.flow.bo.work.CreateWork;
import com.wwgroup.flow.bo.work.DeptStepByStepWork;
import com.wwgroup.flow.bo.work.FinalPlusSignWork;
import com.wwgroup.flow.bo.work.FinalSignWork;
import com.wwgroup.flow.bo.work.InnerJointSignWork;
import com.wwgroup.flow.bo.work.JointSignWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.bo.work.NextBusinessSignWork;
import com.wwgroup.flow.bo.work.NextFinalSignWork;
import com.wwgroup.flow.bo.work.NextStepByStepWork;
import com.wwgroup.flow.bo.work.RejectWork;
import com.wwgroup.flow.bo.work.SecondFinalSignWork;
import com.wwgroup.flow.bo.work.StepByStepWork;
import com.wwgroup.flow.bo.work.SystemJoinSignWork;
import com.wwgroup.flow.bo.work.XZJointSignWork;

public class PersonDetail {

	private long id;

	private long flowId;

	private String name;

	private String postName;

	private String postId;

	private String postCode;

	private String compPhone;

	private String deptName;

	private String deptId;

	private String employeeId;

	private String deptPath;

	private String deptCode;

	private String a_deptCode;

	private String email;

	private String cmpCode;

	private String mgrEmployeeid;

	private String mgrPostcode;

	private int userID;

	private boolean allowAssignAction;

	// 将organ拆分为3个单位，纯粹供显示用
	private String dept1;

	private String dept2;

	private String dept3;
	
	private String titname;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public String getName() {
		return name;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPostName() {
		return postName;
	}

	public void setPostName(String postName) {
		this.postName = postName;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getCompPhone() {
		return compPhone;
	}

	public void setCompPhone(String compPhone) {
		this.compPhone = compPhone;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public String getDeptPath() {
		return deptPath;
	}

	public void setDeptPath(String deptPath) {
		this.deptPath = deptPath;
	}

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public String getA_deptCode() {
		return a_deptCode;
	}

	public void setA_deptCode(String code) {
		a_deptCode = code;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCmpCode() {
		return cmpCode;
	}

	public void setCmpCode(String cmpCode) {
		this.cmpCode = cmpCode;
	}

	public String getMgrEmployeeid() {
		return mgrEmployeeid;
	}

	public void setMgrEmployeeid(String mgrEmployeeid) {
		this.mgrEmployeeid = mgrEmployeeid;
	}

	public String getMgrPostcode() {
		return mgrPostcode;
	}

	public void setMgrPostcode(String mgrPostcode) {
		this.mgrPostcode = mgrPostcode;
	}

	public MyWork buildWork(FlowType flowType, WorkStage workstage, MyWork prevWork) {
		MyWork work = this.buildWork(workstage, prevWork);
		work.setFlowType(flowType);
		return work;
	}

	private MyWork buildWork(WorkStage workstage, MyWork prevWork) {
		MyWork work = null;
		// 启动
		if (WorkStage.CREATE == workstage) {
			work = new CreateWork();
		// 内部会签
		} else if (WorkStage.INNERJOINTSIGN == workstage) {
			work = new InnerJointSignWork();
		// 部门主管审核
		} else if (WorkStage.DEPT_CHENGHE == workstage){
			work = new DeptStepByStepWork();
		// 中心主管审核
		}else if(WorkStage.CENTER_CHENGHE == workstage){
			work = new CenterStepByStepWork();
		// 中心内会签
		} else if (WorkStage.CENTERJOINTSIGN == workstage){
			work = new CenterJoinSignWork();
			if (prevWork != null && prevWork.getId() > 0){
				((CenterJoinSignWork) work).setJoinSignStartId(((CenterJoinSignWork) prevWork).getJoinSignStartId());
				((CenterJoinSignWork) work).setJoinCycle(((CenterJoinSignWork) prevWork).getJoinCycle());
				((CenterJoinSignWork) work).setJoinStartEmployeeId(((CenterJoinSignWork) prevWork).getJoinStartEmployeeId());

				// 设置会办呈核上一级信息
				((CenterJoinSignWork) work).setHb_ChengHe(((CenterJoinSignWork) prevWork).getHb_ChengHe());
				((CenterJoinSignWork) work).setHb_JoinSignStartId(((CenterJoinSignWork) prevWork).getHb_JoinSignStartId());
				((CenterJoinSignWork) work).setHb_JoinStartEmployeeId(((CenterJoinSignWork) prevWork).getHb_JoinStartEmployeeId());
				((CenterJoinSignWork) work).setHb_ChengHeEnd(((CenterJoinSignWork) prevWork).getHb_ChengHeEnd());
			}
		// 单位内会办
		}else if(WorkStage.CMPCODEJOINTSIGN == workstage){
			work = new CmpcodeJoinSignWork();
			// TODO: 复制属性给这个work对象
			if (prevWork != null && prevWork.getId() > 0) {
				((CmpcodeJoinSignWork) work).setJoinSignStartId(((CmpcodeJoinSignWork) prevWork).getJoinSignStartId());
				((CmpcodeJoinSignWork) work).setJoinCycle(((CmpcodeJoinSignWork) prevWork).getJoinCycle());
				((CmpcodeJoinSignWork) work).setJoinStartEmployeeId(((CmpcodeJoinSignWork) prevWork).getJoinStartEmployeeId());
				
				((CmpcodeJoinSignWork) work).setHb_ChengHe(((CmpcodeJoinSignWork) prevWork).getHb_ChengHe());
				((CmpcodeJoinSignWork) work).setHb_JoinSignStartId(((CmpcodeJoinSignWork) prevWork).getHb_JoinSignStartId());
				((CmpcodeJoinSignWork) work).setHb_JoinStartEmployeeId(((CmpcodeJoinSignWork) prevWork).getHb_JoinStartEmployeeId());
				((CmpcodeJoinSignWork) work).setHb_ChengHeEnd(((CmpcodeJoinSignWork) prevWork).getHb_ChengHeEnd());
			}
		// 单位主管审核
		}else if (WorkStage.CHENGHE == workstage) {
			work = new StepByStepWork();
		} else if (WorkStage.FCHENGHE == workstage){
			work = new NextStepByStepWork();
		// 体系内会办
		} else if(WorkStage.SYSTEMJOINTSIGN == workstage) {
			work = new SystemJoinSignWork();
			// TODO: 复制属性给这个work对象
			if (prevWork != null && prevWork.getId() > 0) {
				((SystemJoinSignWork) work).setJoinSignStartId(((SystemJoinSignWork) prevWork).getJoinSignStartId());
				((SystemJoinSignWork) work).setJoinCycle(((SystemJoinSignWork) prevWork).getJoinCycle());
				((SystemJoinSignWork) work).setJoinStartEmployeeId(((SystemJoinSignWork) prevWork).getJoinStartEmployeeId());
				
				// 设置会办呈核上一级信息
				((SystemJoinSignWork) work).setHb_ChengHe(((SystemJoinSignWork) prevWork).getHb_ChengHe());
				((SystemJoinSignWork) work).setHb_JoinSignStartId(((SystemJoinSignWork) prevWork).getHb_JoinSignStartId());
				((SystemJoinSignWork) work).setHb_JoinStartEmployeeId(((SystemJoinSignWork) prevWork).getHb_JoinStartEmployeeId());
				((SystemJoinSignWork) work).setHb_ChengHeEnd(((SystemJoinSignWork) prevWork).getHb_ChengHeEnd());
			}
		// 其他单位会办
		} else if (WorkStage.JOINTSIGN == workstage) {
			work = new JointSignWork();
			// TODO: 复制属性给这个work对象
			if (prevWork != null && prevWork.getId() > 0) {
				((JointSignWork) work).setJoinSignStartId(((JointSignWork) prevWork).getJoinSignStartId());
				((JointSignWork) work).setJoinCycle(((JointSignWork) prevWork).getJoinCycle());
				((JointSignWork) work).setJoinStartEmployeeId(((JointSignWork) prevWork).getJoinStartEmployeeId());
				// add by Cao_Shengyong 2014-03-25
				// 设置会办呈核上一级信息
				((JointSignWork) work).setHb_ChengHe(((JointSignWork) prevWork).getHb_ChengHe());
				((JointSignWork) work).setHb_JoinSignStartId(((JointSignWork) prevWork).getHb_JoinSignStartId());
				((JointSignWork) work).setHb_JoinStartEmployeeId(((JointSignWork) prevWork).getHb_JoinStartEmployeeId());
				((JointSignWork) work).setHb_ChengHeEnd(((JointSignWork) prevWork).getHb_ChengHeEnd());
			}
		} else if (WorkStage.XZJOINTSIGN == workstage){
			work = new XZJointSignWork();
			// TODO: 复制属性给这个work对象
			if (prevWork != null && prevWork.getId() > 0) {
				((XZJointSignWork) work).setJoinSignStartId(((XZJointSignWork) prevWork).getJoinSignStartId());
				((XZJointSignWork) work).setJoinCycle(((XZJointSignWork) prevWork).getJoinCycle());
				((XZJointSignWork) work).setJoinStartEmployeeId(((XZJointSignWork) prevWork).getJoinStartEmployeeId());
				// add by Cao_Shengyong 2014-03-25
				// 设置会办呈核上一级信息
				((XZJointSignWork) work).setHb_ChengHe(((XZJointSignWork) prevWork).getHb_ChengHe());
				((XZJointSignWork) work).setHb_JoinSignStartId(((XZJointSignWork) prevWork).getHb_JoinSignStartId());
				((XZJointSignWork) work).setHb_JoinStartEmployeeId(((XZJointSignWork) prevWork).getHb_JoinStartEmployeeId());
				((XZJointSignWork) work).setHb_ChengHeEnd(((XZJointSignWork) prevWork).getHb_ChengHeEnd());
			}
		}
		// 本人确认
		else if (WorkStage.CONFIRM == workstage) {
			work = new ConfirmWork();
			// TODO: 复制属性给这个work对象
		}
		// 撤消
		else if (WorkStage.CANCEL == workstage) {
			work = new CancelWork();
			// TODO: 复制属性给这个work对象
		}
		// 驳回
		else if (WorkStage.REJECT == workstage) {
			work = new RejectWork();
			// TODO: 复制属性给这个work对象
		}
		// 核决主管审核
		else if (WorkStage.BOSS_SIGN == workstage) {
			work = new FinalSignWork();
			// TODO: 复制属性给这个work对象
		}
		// 核决主管二次审核
		else if (WorkStage.BOSSPLUS_SIGN == workstage){
			work = new FinalPlusSignWork();
		}
		else if (WorkStage.DIVISION_SIGN == workstage){
			work = new SecondFinalSignWork();
		}
		// 核决副主管审核
		else if (WorkStage.FBOSS_SIGN == workstage){
			work = new NextFinalSignWork();
		}
		// 抄送
		else if (WorkStage.COPYDEPT == workstage) {
			work = new CopyDeptWork();
			// TODO: 复制属性给这个work对象
		// 事业部主管审核
		} else if (WorkStage.BUSINESS_SIGN == workstage){
			work = new BusinessSignWork();
		// 事业部副主管审核
		} else if (WorkStage.FBUSINESS_SIGN == workstage){
			work = new NextBusinessSignWork();
		} else if (WorkStage.CHAIRMAN_SIGN == workstage){
			work = new ChairmanSignWork();
		} else if (WorkStage.SubmitFBoss_SIGN == workstage){
			work = new SubmitFBossSignWork();	
		}
		
		if(workstage == WorkStage.CMPCODEJOINTSIGN || workstage == WorkStage.JOINTSIGN
				|| workstage == WorkStage.CENTERJOINTSIGN || workstage == WorkStage.SYSTEMJOINTSIGN
				|| workstage == WorkStage.XZJOINTSIGN){
			if(null != prevWork){
				work.setDeptName(prevWork.getDeptName());
			}else{
				work.setDeptName(this.getDeptName());
			}
		}else{
			work.setDeptName(this.getDeptName());
		}
		
		work.setDeptId(this.getDeptId());
		work.setCreateTime(System.currentTimeMillis());
		work.setStatus(FlowStatus.DOING);
		work.setEmployeeId(this.getEmployeeId());

		work.setPostCode(this.getPostCode());
		work.setDeptCode(this.getDeptCode());
		work.setA_deptCode(this.getA_deptCode());
		work.setCmpCode(this.getCmpCode());
		return work;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	/**
	 * <p>
	 * Description:临时字段 不必持久化 就是告诉前台这个主管 如果助理可以访问的话，是允许指派操作的
	 * </p>
	 * 
	 * @param allowAssignAction
	 */
	public void setAllowAssignAction(boolean allowAssignAction) {
		this.allowAssignAction = allowAssignAction;
	}

	public boolean isAllowAssignAction() {
		return allowAssignAction;
	}

	public String getDept1() {
		return dept1;
	}

	public void setDept1(String dept1) {
		this.dept1 = dept1;
	}

	public String getDept2() {
		return dept2;
	}

	public void setDept2(String dept2) {
		this.dept2 = dept2;
	}

	public String getDept3() {
		return dept3;
	}

	public void setDept3(String dept3) {
		this.dept3 = dept3;
	}

	public String getTitname() {
		return titname;
	}

	public void setTitname(String titname) {
		this.titname = titname;
	}

}
