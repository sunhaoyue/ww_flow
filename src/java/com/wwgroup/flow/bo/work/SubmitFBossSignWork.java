package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class SubmitFBossSignWork extends MyDelegateWork {

	public SubmitFBossSignWork() {
		super(WorkStage.SubmitFBoss_SIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
