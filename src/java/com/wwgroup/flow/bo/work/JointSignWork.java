package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class JointSignWork extends MyDelegateWork {

	public JointSignWork() {
		super(WorkStage.JOINTSIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return true;
	}

}
