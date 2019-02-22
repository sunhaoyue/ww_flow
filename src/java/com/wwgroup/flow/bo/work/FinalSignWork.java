package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class FinalSignWork extends MyDelegateWork {

	public FinalSignWork() {
		super(WorkStage.BOSS_SIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
