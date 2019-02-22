package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class CenterStepByStepWork extends MyDelegateWork{

	public CenterStepByStepWork() {
		super(WorkStage.CENTER_CHENGHE);
	}

	@Override
	public boolean supportAssignFunction() {
		// TODO Auto-generated method stub
		return false;
	}

}
