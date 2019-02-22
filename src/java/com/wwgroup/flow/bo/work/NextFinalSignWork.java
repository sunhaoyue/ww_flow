package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class NextFinalSignWork extends MyDelegateWork {

	public NextFinalSignWork() {
		super(WorkStage.FBOSS_SIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
