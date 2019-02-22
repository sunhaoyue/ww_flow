package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class XZJointSignWork extends MyDelegateWork {

	public XZJointSignWork() {
		super(WorkStage.XZJOINTSIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return true;
	}

}
