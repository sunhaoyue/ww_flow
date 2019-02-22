package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class NextBusinessSignWork extends MyDelegateWork {

	public NextBusinessSignWork() {
		super(WorkStage.FBUSINESS_SIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
