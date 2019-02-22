
package com.wwgroup.flow.service;

import java.util.List;

import com.wwgroup.common.Page;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.dto.AdvancedSearchDTO;
import com.wwgroup.flow.dto.ConfidentialSearchDTO;
import com.wwgroup.flow.dto.ConfidentialSearchbossDTO;
import com.wwgroup.flow.dto.LogDTO;
import com.wwgroup.flow.dto.LogInfo;
import com.wwgroup.flow.dto.MyWorkDTO;
import com.wwgroup.organ.bo.HRCompany;

/**<p>
 * Title: cuteinfo_[子系统统名]_[模块名]
 * </p>
 * <p>
 * Description: [描述该类概要功能介绍]
 * </p>
 * 
 * @author Administrator
 * @version $Revision$ 2012-6-6
 * @author (lastest modification by $Author$)
 * @since 20100620
 */
public interface FlowManagementService {

	Page findFlowTemplatesByPage(PersonDetail actualPerson, int start, int size);

	Page findMyToDoWorkByPage(PersonDetail actualPerson, int start, int size);

	Page findCreatorFlowByPage(PersonDetail actualPerson, int start, int size);

	Page findMyProcessFlowByPage(PersonDetail actualPerson, int start, int size);

	int getFlowTemplateCount(PersonDetail actualPerson);

	int getMyToDoWorkCount(PersonDetail actualPerson);

	int getCreatorFlowCount(PersonDetail actualPerson);

	int getMyProcessFlowCount(PersonDetail actualPerson);

	/** <p>
	 * Description:页面高级查询
	 * </p>
	 * 
	 * @param oneSearch
	 * @param start
	 * @param size
	 * @return
	 */
	Page searchWorkDTOByPage(AdvancedSearchDTO oneSearch, int start, int size);
	
	//删除暂存表单
	void delTempForm(List<String> formnumbers);
	
	/**
	 * 获取抄送表单列表
	 */
	Page findCopyFlowByPage(PersonDetail actualPerson, int start, int size);
	
	/**
	 * 获取抄送表单数
	 */
	int getCopyFlowCount(PersonDetail actualPerson);
	
	int getSecurityFlowCount();
	
	Page findSecurityFlowByPage(AdvancedSearchDTO onSearchDTO, int start, int size);

	List<HRCompany> getCompanyList();

	List<MyWorkDTO> searchWorkDTOExport(AdvancedSearchDTO oneSearch);

	Page findLogInfoByPage(String startDate, String logLevel, int start,
			int size);

	List<LogInfo> findLogInfoByExport(String startDate, String logLevel);

	Page findCreatorFlowByPageEx(PersonDetail actualPerson, int start,
			int size, String sidx, String sord, AdvancedSearchDTO searchDTO);

	Page searchConfidByPage(ConfidentialSearchDTO oneSearch, int start, int size);

	void saveLog(LogDTO logDto);

	List<MyWorkDTO> searchConfidExport(ConfidentialSearchDTO searchDTO);
	
	Page searchConfidbossByPage(ConfidentialSearchbossDTO oneSearch, int start, int size);
	
	List<MyWorkDTO> searchConfidbossExport(ConfidentialSearchbossDTO searchDTO);

}
