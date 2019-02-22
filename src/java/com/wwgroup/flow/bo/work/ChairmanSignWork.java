package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class ChairmanSignWork extends MyDelegateWork {

	public ChairmanSignWork() {
		super(WorkStage.CHAIRMAN_SIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
