package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class CreateWork extends MyDelegateWork {

	public CreateWork() {
		super(WorkStage.CREATE);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
