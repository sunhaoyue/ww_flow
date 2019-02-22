package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class NextStepByStepWork extends MyDelegateWork {

	public NextStepByStepWork() {
		super(WorkStage.FCHENGHE);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
