package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;
/**
 * 同中心会办
 */
public class CenterJoinSignWork extends MyDelegateWork{

	public CenterJoinSignWork() {
		super(WorkStage.CENTERJOINTSIGN); 
	}

	@Override
	public boolean supportAssignFunction() {
		return true;
	}

}
