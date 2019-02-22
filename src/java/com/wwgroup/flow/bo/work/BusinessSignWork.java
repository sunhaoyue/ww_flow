package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class BusinessSignWork extends MyDelegateWork {

	public BusinessSignWork() {
		super(WorkStage.BUSINESS_SIGN);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
