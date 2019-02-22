package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class SecondFinalSignWork extends MyDelegateWork {

	public SecondFinalSignWork() {
		super(WorkStage.DIVISION_SIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
