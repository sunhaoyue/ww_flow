package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;
/**
 * 同体系会办
 */
public class SystemJoinSignWork extends MyDelegateWork{

	public SystemJoinSignWork() {
		super(WorkStage.SYSTEMJOINTSIGN); 
	}

	@Override
	public boolean supportAssignFunction() {
		return true;
	}

}
