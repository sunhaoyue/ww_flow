package com.wwgroup.flow.bo.work;

import com.wwgroup.flow.bo.WorkStage;
/**
 * 同CMPCODE
 * @creator     zhangqiang
 * @create-time Nov 20, 2012   8:07:17 AM
 * @version 0.1
 */
public class CmpcodeJoinSignWork extends MyDelegateWork{

	public CmpcodeJoinSignWork() {
		super(WorkStage.CMPCODEJOINTSIGN); 
	}

	@Override
	public boolean supportAssignFunction() {
		return true;
	}

}
