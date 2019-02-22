package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class ConfirmWork extends MyDelegateWork {

	public ConfirmWork() {
		super(WorkStage.CONFIRM);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
