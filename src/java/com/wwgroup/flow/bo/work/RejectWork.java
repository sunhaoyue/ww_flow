package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class RejectWork extends MyDelegateWork {

	public RejectWork() {
		super(WorkStage.REJECT);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
