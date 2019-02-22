package com.wwgroup.flow.dto;

import com.wwgroup.flow.bo.helper.FlowStatus;

public enum FormStatus {
	// 分别对应内容为：
	// 暂存，签核中，已核准，已驳回，已取消，重新起案, 全选, 已结案(新增)
	TEMP {
		@Override
		public FlowStatus[] transform() {
			return new FlowStatus[] { FlowStatus.TEMP };
		}

		@Override
		public String getDisplayCNName() {
			return "暂存";
		}
	},
	DOING {
		@Override
		public FlowStatus[] transform() {
			return new FlowStatus[] { FlowStatus.INNERJOINTSIGN_START,
					FlowStatus.CHENGHE_START, FlowStatus.JOINTSIGN_START,
					FlowStatus.CONFIRM_START, FlowStatus.FINAL_DECISION_START,
					FlowStatus.CENTER_CHENGHE_START,
					FlowStatus.CMPCODEJOINTSIGN_START,
					FlowStatus.SECONDFINAL_DECISION_START,
					FlowStatus.NEXTFINAL_DECISION_START,
					FlowStatus.DEPT_CHENGHE_START,
					FlowStatus.CENTERJOINTSIGN_START,
					FlowStatus.NEXTBUSINESS_DECISION_START,
					FlowStatus.BUSINESS_DECISION_START,
					FlowStatus.SubmitFBossSIGN_START ,
					FlowStatus.SYSTEMJOINTSIGN_START };
		}

		@Override
		public String getDisplayCNName() {
			return "签核中";
		}
	},
	APPROVED {
		@Override
		public FlowStatus[] transform() {
			return new FlowStatus[] {FlowStatus.FINAL_DECISION_END };
		}

		@Override
		public String getDisplayCNName() {
			return "已核准";
		}
	},
	REJECTED {
		@Override
		public FlowStatus[] transform() {
			return new FlowStatus[] { FlowStatus.REJECT };
		}

		@Override
		public String getDisplayCNName() {
			return "已驳回";
		}
	},
	CANCELED {
		@Override
		public FlowStatus[] transform() {
			return new FlowStatus[] { FlowStatus.CANCEL };
		}

		@Override
		public String getDisplayCNName() {
			return "已撤销";
		}
	},
	RENEWED {
		@Override
		public FlowStatus[] transform() {
			return new FlowStatus[] { FlowStatus.RECREATE };
		}

		@Override
		public String getDisplayCNName() {
			return "重新起案";
		}
	},
	END {
		@Override
		public FlowStatus[] transform() {
			return new FlowStatus[] {FlowStatus.END};
		}

		@Override
		public String getDisplayCNName() {
			return "已结案";
		}
	},
    COPY_SEND {
		@Override
		public FlowStatus[] transform() {
			return new FlowStatus[] {FlowStatus.COPY_SEND};
		}

		@Override
		public String getDisplayCNName() {
			return "已核准";
		}
	},
	CC_End {
		@Override
		public FlowStatus[] transform() {
			return new FlowStatus[] {FlowStatus.CC_End};
		}

		@Override
		public String getDisplayCNName() {
			return "已核准";
		}
	},	
	ALL {
		@Override
		public FlowStatus[] transform() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDisplayCNName() {
			return "全选";
		}
	};

	abstract public FlowStatus[] transform();

	abstract public String getDisplayCNName();

}
