package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class InnerJointSignWork extends MyDelegateWork {

	public InnerJointSignWork() {
		super(WorkStage.INNERJOINTSIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
