package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;

public class CopyDeptWork extends MyDelegateWork {

	public CopyDeptWork() {
		super(WorkStage.COPYDEPT);
	}

	@Override
	public boolean supportAssignFunction() {
		return false;
	}

}
