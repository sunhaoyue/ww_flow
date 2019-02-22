package com.wwgroup.flow.bo.helper;



import com.wwgroup.flow.dto.FormStatus;
import org.apache.commons.lang3.StringUtils;

public enum FlowStatus {

	INIT {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "发起" : this.displayName;
		}

	},
	RECREATE {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "重起案" : this.displayName;
		}
	},
	
	
	INNERJOINTSIGN_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "内联签程启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	INNERJOINTSIGN_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "内联签程结束" : this.displayName;
		}

	},
	CHENGHE_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "审核启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	CHENGHE_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "审核结束" : this.displayName;
		}

	},
	JOINTSIGN_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "会签启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	JOINTSIGN_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "会签结束" : this.displayName;
		}

	},
	CONFIRM_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "发起人确认启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	CONFIRM_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "发起人确认结束" : this.displayName;
		}
	},

	DOING {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "处理中" : this.displayName;
		}
	},
	WAITING {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "指派" : this.displayName;
		}
	},
	APPROVED {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "核准" : this.displayName;
		}
	},
	AGREE {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "核准" : this.displayName;
		}
	},
	VIEW {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "查阅" : this.displayName;
		}
	},
	DISAGREE {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "驳回" : this.displayName;
		}
	},
	CANCEL {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "撤销" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.CANCELED.getDisplayCNName();
		}
	},
	REJECT {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "驳回" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.REJECTED.getDisplayCNName();
		}
	},
	FINAL_DECISION_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "最终核决启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	FINAL_DECISION_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "最终核决结束" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.APPROVED.getDisplayCNName();
		}
	},
	COPY_SEND {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "执行" : this.displayName;
		}

	//	@Override
	//	public String getFlowDisplayStatusName() {
	//		return FormStatus.APPROVED.getDisplayCNName();
	//	}
	},
	TEMP {

		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "暂存" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.TEMP.getDisplayCNName();
		}

	},
	END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "结案" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.END.getDisplayCNName();
		}
	},
	/*
	 * 新增四个流程状态
	 * edited by zhangqiang at 2012-11-20 14:38
	 */
	CENTER_CHENGHE_START{
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "中心审核启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	CENTER_CHENGHE_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "中心审核结束" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	CMPCODEJOINTSIGN_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "本单位会签启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	CMPCODEJOINTSIGN_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "本单位会签结束" : this.displayName;
		}

	},
	SECONDFINAL_DECISION_START{
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "事业部主管审核启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	SECONDFINAL_DECISION_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "事业部主管审核结束" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.APPROVED.getDisplayCNName();
		}
	},
	NEXTFINAL_DECISION_START{
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "最高副主管审核启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	NEXTFINAL_DECISION_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "最高副主管审核结束" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.APPROVED.getDisplayCNName();
		}
	},
	/**
	 * 新增流程状态
	 * edit by Cao_Shengyong
	 */
	DEPT_CHENGHE_START{
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "部门审核启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	DEPT_CHENGHE_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "部门审核结束" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	CENTERJOINTSIGN_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "本中心会签启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	CENTERJOINTSIGN_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "本中心会签结束" : this.displayName;
		}

	},
	NEXTBUSINESS_DECISION_START{
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "事业部主管审核启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	NEXTBUSINESS_DECISION_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "事业部主管审核结束" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.APPROVED.getDisplayCNName();
		}
	},
	BUSINESS_DECISION_START{
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "事业部主管审核启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	BUSINESS_DECISION_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "事业部主管审核结束" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.APPROVED.getDisplayCNName();
		}
	},
	SYSTEMJOINTSIGN_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "本体系会办启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	SYSTEMJOINTSIGN_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "本体系会办结束" : this.displayName;
		}

	},
	NEXTCHENGHE_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "审核启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	NEXTCHENGHE_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "审核结束" : this.displayName;
		}

	},
	XZJOINTSIGN_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "职能单位会办启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	XZJOINTSIGN_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "职能单位会办结束" : this.displayName;
		}

	},
	CHAIRMANSIGN_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "董事长签核启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	CHAIRMANSIGN_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "董事长签核结束" : this.displayName;
		}

	},
	SubmitFBossSIGN_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "副总裁签核启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	SubmitFBossSIGN_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "副总裁签核结束" : this.displayName;
		}

	},
	FINALPLUS_DECISION_START {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "核决启动" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.DOING.getDisplayCNName();
		}
	},
	FINALPLUS_DECISION_END {
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "核决结束" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.APPROVED.getDisplayCNName();
		}
	},
	CC_End{
		@Override
		public String getDisplayName() {
			return StringUtils.isEmpty(this.displayName) ? "抄送" : this.displayName;
		}

		@Override
		public String getFlowDisplayStatusName() {
			return FormStatus.APPROVED.getDisplayCNName();
		}
	};

	protected String displayName;

	public abstract String getDisplayName();

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getFlowDisplayStatusName() {
		return null;
	}

	// public FlowStatus renameDelegate(boolean delegated, String delegateKeyWords) {
	// if (delegated) {
	// this.setDisplayName(delegateKeyWords + " " + this.getDisplayName());
	// }
	// return this;
	// }

}
