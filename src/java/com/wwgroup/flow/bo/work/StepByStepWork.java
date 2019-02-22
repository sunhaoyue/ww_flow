package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class StepByStepWork extends MyDelegateWork {

	public StepByStepWork() {
		super(WorkStage.CHENGHE);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
