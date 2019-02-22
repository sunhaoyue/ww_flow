package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class DeptStepByStepWork extends MyDelegateWork{

	public DeptStepByStepWork() {
		super(WorkStage.DEPT_CHENGHE);
	}

	@Override
	public boolean supportAssignFunction() {
		// TODO Auto-generated method stub
		return false;
	}

}
