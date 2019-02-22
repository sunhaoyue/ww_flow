package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class FinalPlusSignWork extends MyDelegateWork {

	public FinalPlusSignWork() {
		super(WorkStage.BOSSPLUS_SIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
