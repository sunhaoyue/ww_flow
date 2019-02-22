package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class CancelWork extends MyDelegateWork {

	public CancelWork() {
		super(WorkStage.CANCEL);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
