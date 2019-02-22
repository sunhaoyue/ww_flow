package com.wwgroup.flow.service;

import java.util.Map;

import com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.work.MyWork;

public interface FlowStageProcessor {

	Map<String, PersonDetail> start(Flow flow);

	Map<String, PersonDetail> startNextWork(Flow flow, PersonDetail personDetail, MyWork prevWork);

	Map<String, PersonDetail> completeWork(Flow flow, MyWork work);

	void cancel(MyWork myWork, Flow flow);

	void reject(MyWork work);

	Map<String, PersonDetail> startNextInsideWork(Flow flow, PersonDetail employee, MyWork parentWork);

	Map<String, PersonDetail> completeInsideWork(Flow flow, MyWork work);

	Flow endFlow(Flow flow);

}
