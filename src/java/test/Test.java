package test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.wwgroup.flow.bo.helper.FlowStatus;

@SuppressWarnings("unused")
public class Test {

	private static FlowStatus approved = FlowStatus.APPROVED;//核准
	private static FlowStatus cancel = FlowStatus.CANCEL;//撤回
	private static FlowStatus center_chenghe_start = FlowStatus.CENTER_CHENGHE_START;//中心审核启动
	private static FlowStatus center_chenghe_end = FlowStatus.CENTER_CHENGHE_END;//中心审核启动

	private void FlowStatus() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DecimalFormat df = new DecimalFormat("#0.00");
		double d = 39.89;
		double p = d * 0.1;
		//System.out.println(df.format(d));
		int len = 10;
		int ii = 1;
		for(ii = 1; ii <= len; ii++){
			System.out.println(ii + " : " + df.format(d + p) + " --> " + p);
			d = d + p;
			p = d * 0.1;
		}
		
		
		String s = "神旺控股/神旺控股总部/行政管理中心/资讯处/BI课";
		//System.out.println(s.indexOf("/"));
		//System.out.println(s.substring(s.indexOf("/") + 1));
		
		List l1 = new ArrayList();
		List l2 = new ArrayList();
		
		l1.add(1);
		l1.add(2);
		l1.add(3);
		l1.add(4);

		l2.add(2);
		l2.add(3);
		l2.add(5);
		l2.add(6);
		l2.add(51);
		l2.add(52);
		
		l1.removeAll(l2);
		//System.out.println(l1.toString());
		
	/*	FlowStatus[] ss = FlowStatus.values();
		for(int i = 0; i < ss.length; i++){
			System.out.println(ss[i].toString() + " # " + ss[i].ordinal() + " # " + ss[i].getDisplayName());
		} */
		
		/*Calendar calendar = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy年M月d日");
		String s = df.format(calendar.getTime());
		System.out.println(s);
		String[] weekDays = new String[]{"星期日","星期一","星期二","星期三","星期四","星期五","星期六"}; 
		int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0) w = 0;
		System.out.println(weekDays[w]);*/
		
	/*	WorkStage[] ss = WorkStage.values();
		for(int i = 0; i < ss.length; i++){
			System.out.println(ss[i].toString() + " # " + ss[i].ordinal());
		}*/
		/*
		String s = StringUtils.arrayToDelimitedString(ss, "^");
		System.out.println(s);*/
		/*DescionMaker[] ss = DescionMaker.values();
		for(int i = 0; i < ss.length; i++){
			System.out.println(ss[i].toString() + " # " + ss[i].ordinal() + " # " + ss[i].name());
		}
		System.out.println(DescionMaker.values()[4].toString());*/
		/*System.err.println(approved.getDisplayName() + "("
				+ approved.getFlowDisplayStatusName() + ")" + " : "
				+ approved.ordinal());

		System.err.println(cancel.getDisplayName() + "("
				+ cancel.getFlowDisplayStatusName() + ")" + " : "
				+ cancel.ordinal());
		
		System.err.println(center_chenghe_start.getDisplayName() + "("
				+ center_chenghe_start.getFlowDisplayStatusName() + ")" + " : "
				+ center_chenghe_start.ordinal());
		
		System.err.println(center_chenghe_end.getDisplayName() + "("
				+ center_chenghe_end.getFlowDisplayStatusName() + ")" + " : "
				+ center_chenghe_end.ordinal());
		
		System.out.println(FlowStatus.AGREE.getDisplayName() + "("
				+ FlowStatus.AGREE.getFlowDisplayStatusName() + ")" + " : "
				+ FlowStatus.AGREE.ordinal());*/
		/*System.out.println(GroupType.DEPTGROUP.name());
		System.out.println(GroupType.CENTERGROUP.ordinal());
		System.out.println(GroupType.CMPGROUP.ordinal());*/
		String joinSignStartId = "[{\"employeeId\":\"00001309\",\"deptId\":\"260\"}]";
		String key = "employeeId";
		JSONArray jsonArray = JSONArray.fromObject(joinSignStartId);
		//System.out.println(jsonArray.toString());
		//System.out.println(jsonArray.size());
		for(int i = 0; i < jsonArray.size(); i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			//System.out.println(jsonObject.toString());
		}
		/*JSONObject jsonObj = (JSONObject) jsonArray.get(jsonArray.size() - 1);
		System.out.println(jsonObj.get(key));
		JSONArray retArray = new JSONArray();
		JSONObject tObj = new JSONObject();
		tObj.put(key, "00000283");
		tObj.put("deptId", jsonObj.get("deptId"));
		retArray.add(tObj);
		System.out.println(retArray.toString());*/
	}

}
