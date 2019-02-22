package java.com.wwgroup.flow.bo;

import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.helper.JointSignType;

public class Flow {
	private long id;

	private long createTime;

	private String formNum;

	private com.wwgroup.flow.bo.PersonDetail createPerson;

	private com.wwgroup.flow.bo.PersonDetail actualPerson;

	private com.wwgroup.flow.bo.FlowContent content;

	private FlowStatus status = FlowStatus.INIT;

	private DescionMaker decionmaker;

	private JointSignType jointSignType;

	private String[] jointSignDeptIds;

	private String jointSignDeptName;

	private String[] copyDeptIds;

	private String copyDeptName;

	private String copyDemo;

	private String[] innerJointSignIds;

	private String innerJointSignName;

	private com.wwgroup.flow.bo.FlowAttachment[] flowAttachments;

	private String templateCreateId;

	private boolean isTempalte;

	private boolean selfConfirm;

	private com.wwgroup.flow.bo.FlowType flowType;

	private int shengheStep = 0;

	private String shengheEmployeeId;

	private String shengheDeptId;

	private String shenghePostCode;
	
	private String secondEmployeeId;
	private String secondDeptId;
	private String secondPostCode;

	private boolean isLocal;

	// 重新起案次数，重新起案过一次后，发起人就不能再重新起案了。
	private int renewTimes;

	// 用于显示
	private String leaderValue;
	private String joinTypeValue;
	private String confirmValue;
	private String huibanDeptIds;
	private String chaosongDeptIds;
	//是否呈核总裁
	private boolean submitBoss;
	
	// 区分改造前后的记录
	private String isNew = "1";
	
	private FlowStatus nextStep = FlowStatus.INIT;
	private String[] pendJointSignDeptIds;
	private String lastEmployeeId;
	private String lastDeptId;
	private String lastPostCode;
	
	private long endTime;
	
	// 是否呈核副总裁
	private boolean submitFBoss;
	
	private String fileSys;
	private boolean submitBOffice;
	
	private boolean chariman;

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isLocal() {
		return isLocal;
	}

	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getFormNum() {
		return formNum;
	}

	public void setFormNum(String formNum) {
		this.formNum = formNum;
	}

	public com.wwgroup.flow.bo.PersonDetail getCreatePerson() {
		return createPerson;
	}

	public void setCreatePerson(com.wwgroup.flow.bo.PersonDetail createPerson) {
		this.createPerson = createPerson;
	}

	public com.wwgroup.flow.bo.PersonDetail getActualPerson() {
		return actualPerson != null ? actualPerson : this.createPerson;
	}

	public void setActualPerson(com.wwgroup.flow.bo.PersonDetail actualPerson) {
		this.actualPerson = actualPerson;
	}

	public com.wwgroup.flow.bo.FlowContent getContent() {
		return content;
	}

	public void setContent(com.wwgroup.flow.bo.FlowContent content) {
		this.content = content;
	}

	public FlowStatus getStatus() {
		return status;
	}

	public void setStatus(FlowStatus status) {
		this.status = status;
	}

	public DescionMaker getDecionmaker() {
		return decionmaker;
	}

	public void setDecionmaker(DescionMaker decionmaker) {
		this.decionmaker = decionmaker;
	}

	public JointSignType getJointSignType() {
		return jointSignType;
	}

	public void setJointSignType(JointSignType jointSignType) {
		this.jointSignType = jointSignType;
	}

	public String[] getJointSignDeptIds() {
		return jointSignDeptIds;
	}

	public void setJointSignDeptIds(String[] jointSignDeptIds) {
		this.jointSignDeptIds = jointSignDeptIds;
	}

	public String getJointSignDeptName() {
		return jointSignDeptName;
	}

	public void setJointSignDeptName(String jointSignDeptName) {
		this.jointSignDeptName = jointSignDeptName;
	}

	public String[] getCopyDeptIds() {
		return copyDeptIds;
	}

	public void setCopyDeptIds(String[] copyDeptIds) {
		this.copyDeptIds = copyDeptIds;
	}

	public String getCopyDeptName() {
		return copyDeptName;
	}

	public void setCopyDeptName(String copyDeptName) {
		this.copyDeptName = copyDeptName;
	}

	public String getCopyDemo() {
		return copyDemo;
	}

	public void setCopyDemo(String copyDemo) {
		this.copyDemo = copyDemo;
	}

	public String[] getInnerJointSignIds() {
		return innerJointSignIds;
	}

	public void setInnerJointSignIds(String[] innerJointSignIds) {
		this.innerJointSignIds = innerJointSignIds;
	}

	public String getInnerJointSignName() {
		return innerJointSignName;
	}

	public void setInnerJointSignName(String innerJointSignName) {
		this.innerJointSignName = innerJointSignName;
	}

	public String getTemplateCreateId() {
		return templateCreateId;
	}

	public void setTemplateCreateId(String templateCreateId) {
		this.templateCreateId = templateCreateId;
	}

	public boolean isTempalte() {
		return isTempalte;
	}

	public void setTempalte(boolean isTempalte) {
		this.isTempalte = isTempalte;
	}

	public com.wwgroup.flow.bo.FlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(com.wwgroup.flow.bo.FlowType flowType) {
		this.flowType = flowType;
	}

	public com.wwgroup.flow.bo.FlowAttachment[] getFlowAttachments() {
		return flowAttachments;
	}

	public void setFlowAttachments(com.wwgroup.flow.bo.FlowAttachment[] flowAttachments) {
		this.flowAttachments = flowAttachments;
	}

	public int getShengheStep() {
		return shengheStep;
	}

	public void setShengheStep(int shengheStep) {
		this.shengheStep = shengheStep;
	}

	public String getShengheEmployeeId() {
		return shengheEmployeeId;
	}

	public void setShengheEmployeeId(String shengheEmployeeId) {
		this.shengheEmployeeId = shengheEmployeeId;
	}

	public boolean isSelfConfirm() {
		return selfConfirm;
	}

	public void setSelfConfirm(boolean selfConfirm) {
		this.selfConfirm = selfConfirm;
	}

	public int getRenewTimes() {
		return renewTimes;
	}

	public void setRenewTimes(int renewTimes) {
		this.renewTimes = renewTimes;
	}

	public String getShengheDeptId() {
		return shengheDeptId;
	}

	public void setShengheDeptId(String shengheDeptId) {
		this.shengheDeptId = shengheDeptId;
	}

	public String getShenghePostCode() {
		return shenghePostCode;
	}

	public void setShenghePostCode(String shenghePostCode) {
		this.shenghePostCode = shenghePostCode;
	}

	public String getLeaderValue() {
		return leaderValue;
	}

	public void setLeaderValue(String leaderValue) {
		this.leaderValue = leaderValue;
	}

	public String getJoinTypeValue() {
		return joinTypeValue;
	}

	public void setJoinTypeValue(String joinTypeValue) {
		this.joinTypeValue = joinTypeValue;
	}

	public String getConfirmValue() {
		return confirmValue;
	}

	public void setConfirmValue(String confirmValue) {
		this.confirmValue = confirmValue;
	}

	public String getHuibanDeptIds() {
		return huibanDeptIds;
	}

	public void setHuibanDeptIds(String huibanDeptIds) {
		this.huibanDeptIds = huibanDeptIds;
	}

	public String getChaosongDeptIds() {
		return chaosongDeptIds;
	}

	public void setChaosongDeptIds(String chaosongDeptIds) {
		this.chaosongDeptIds = chaosongDeptIds;
	}

	public boolean isSubmitBoss() {
		return submitBoss;
	}

	public void setSubmitBoss(boolean submitBoss) {
		this.submitBoss = submitBoss;
	}

	public String getSecondEmployeeId() {
		return secondEmployeeId;
	}

	public void setSecondEmployeeId(String secondEmployeeId) {
		this.secondEmployeeId = secondEmployeeId;
	}

	public String getSecondDeptId() {
		return secondDeptId;
	}

	public void setSecondDeptId(String secondDeptId) {
		this.secondDeptId = secondDeptId;
	}

	public String getSecondPostCode() {
		return secondPostCode;
	}

	public void setSecondPostCode(String secondPostCode) {
		this.secondPostCode = secondPostCode;
	}

	public String getIsNew() {
		return isNew;
	}

	public void setIsNew(String isNew) {
		this.isNew = isNew;
	}

	public FlowStatus getNextStep() {
		return nextStep;
	}

	public void setNextStep(FlowStatus nextStep) {
		this.nextStep = nextStep;
	}

	public String[] getPendJointSignDeptIds() {
		return pendJointSignDeptIds;
	}

	public void setPendJointSignDeptIds(String[] pendJointSignDeptIds) {
		this.pendJointSignDeptIds = pendJointSignDeptIds;
	}

	public String getLastEmployeeId() {
		return lastEmployeeId;
	}

	public void setLastEmployeeId(String lastEmployeeId) {
		this.lastEmployeeId = lastEmployeeId;
	}

	public String getLastDeptId() {
		return lastDeptId;
	}

	public void setLastDeptId(String lastDeptId) {
		this.lastDeptId = lastDeptId;
	}

	public String getLastPostCode() {
		return lastPostCode;
	}

	public void setLastPostCode(String lastPostCode) {
		this.lastPostCode = lastPostCode;
	}

	public boolean isSubmitFBoss() {
		return submitFBoss;
	}

	public void setSubmitFBoss(boolean submitFBoss) {
		this.submitFBoss = submitFBoss;
	}

	public String getFileSys() {
		return fileSys;
	}

	public void setFileSys(String fileSys) {
		this.fileSys = fileSys;
	}

	public boolean isSubmitBOffice() {
		return submitBOffice;
	}

	public void setSubmitBOffice(boolean submitBOffice) {
		this.submitBOffice = submitBOffice;
	}

	public boolean isChariman() {
		return chariman;
	}

	public void setChariman(boolean chariman) {
		this.chariman = chariman;
	}
}
