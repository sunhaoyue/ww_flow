package com.wwgroup.flow.service;

import java.util.List;
import java.util.Map;

import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.organ.bo.GroupType;
import com.wwgroup.user.bo.EmployeePos;

public interface PersonService {
	/**
	 * 查询签呈人员详细信息(主岗位)
	 * 
	 * @param userName
	 *            一般传入的参数为登录保存的人员姓名，如崔翠翠(HA0812524)
	 * @return
	 */
	PersonDetail loadWidePersonDetail(String employeeId);
	
	/**
	 * 获取地方主管
	 * @param cmpCode
	 * @return
	 */
	PersonDetail getCenterEmployeeByCmpCode(String cmpCode);
	
	/**
	 * 获取地方副主管
	 */
	PersonDetail getCenterFEmployeeByCmpCode(String cmpCode);

	/**
	 * 获得这个部门下面的人员的详细信息
	 * 
	 * @param deptId
	 *            部门Id
	 * @param employeeId
	 *            用户编号
	 * @param postcode
	 *            职位代码
	 * @return
	 */
	PersonDetail loadWidePersonDetail(String deptId, String employeeId, String postcode);
	
	PersonDetail loadWidePersonDetailPlus(String deptId, String employeeId, String postcode);

	/**
	 * 查询签呈人员上级主管的详细信息
	 * 
	 * @param person
	 * @return
	 */
	PersonDetail getMgrPersonDetail(PersonDetail person);

	/**
	 * <p>
	 * 获得当前部门领导信息
	 * </p>
	 * 
	 * @param jointSignDeptId
	 * @return
	 */
	PersonDetail loadWideMgrPersonDetail(String deptId);

	/**
	 * <p>
	 * Description:获得实际申请者的 组织代码(4位)-中心代码(4位) 数组
	 * </p>
	 * 
	 * @param actualPerson
	 * @return
	 */
	String[] getOrganCodes(PersonDetail actualPerson);

	/**
	 * <p>
	 * Description:判断该主管是否与设置的核诀主管匹配
	 * </p>
	 * 
	 * @param decionmaker
	 * @param mgrPerson
	 * @return
	 */
	boolean quailifiedDecisionMaker(DescionMaker decionmaker, PersonDetail mgrPerson);
	
	/**
	 * Description: 判断该主管是否为设置的核决主管的副主管
	 */
	boolean quailifiedDecisionMakerPlus(DescionMaker decionmaker, PersonDetail mgrPerson);

	/**
	 * <p>
	 * Description:通过部门id获得其所属所有子部门id
	 * </p>
	 * 
	 * @param deptId
	 * @return
	 */
	String[] getSubDeptIds(String deptId);

	/**
	 * <p>
	 * Description:查阅出这个主管所有（管理）岗位的列表清单（key postCode, value
	 * postName）。这个是给助理维护界面使用的
	 * </p>
	 * 
	 * @param mgrPerson
	 * @return
	 */
	Map<String, String> getMgrPostMap(PersonDetail mgrPerson);

	/**
	 * <p>
	 * 获得当前部门领导信息（有多个领导）
	 * </p>
	 * 
	 * @param jointSignDeptId
	 * @return
	 */
	List<PersonDetail> loadWideMgrPersonDetails(String deptId);

	/**
	 * 根据工号判断该用户是否为单位、中心、部门主管
	 * 
	 * @param employeeId
	 * @return
	 */
	boolean qualifiedGroupMgr(String employeeId, String type);

	/**
	 * add by Cao_Shengyong 2014-03-25
	 * 用于获取用户对应岗位上的相关信息
	 */
	public EmployeePos loadWideEmployeePos(String employeeId, String postcode);
	
	/**
	 * add by Cao_Shengyong 2014-03-25
	 * 根据用户ID和岗位，查找到对应级别的主管相关信息
	 * employeeId:　用户工号
	 * postcode:	用户岗位
	 * grouptype:	需要查找到的级别。可能是：部门、中心、单位
	 * 				最高只会找到单位主管
	 * 				对应的参数值可能是与GroupTypes中的对应，即：DEPTGROUP, CENTERGROUP, CMPGROUP
	 * 该方法是一直寻找，直到能找到对应级别的人员信息
	 */
	public PersonDetail getMgrPersonByIdAndPostCode(String employeeId, String postcode, String groupType);
	
	/**
	 *　add by Cao_Shengyong 2014-06-24
	 *　根据用户ID和岗位，判断该用户是否是单位副主管
	 */
	public boolean isCenterFHead(String employeeid, String postcode);
	
	/**
	 * add by Cao_Shengyong 2014-07-10
	 * 判断用户是否单位最高主管
	 */
	public boolean isTopMgr(PersonDetail personDetail);

	boolean canUpAttach(String groupid);

	boolean getJYJurisdiction(String empid);

	PersonDetail loadWideMgrPersonDetailPlus(String deptId, GroupType groupType);

	/**
	 * 根据岗位代码、工号查找到对应的用户信息
	 * @param employeeId
	 * @param postcode
	 * @return
	 */
	PersonDetail loadWidePersonDetail(String employeeId, String postcode);

	/**
	 * 这是一个特殊处理方法，主要是用于行政职能部门的抄送
	 * 在抄送给职能部门时，即要发送给中心主管，也要发送给部门主管
	 * 这里就是根据传入的GroupID去查找出下属所有的中心主管和部门主管
	 * 由于前端已经控制了职能部门只能选到中心级别，所以这里传过来的职能部门ID肯定是中心
	 * @param deptId
	 * @return
	 */
	List<PersonDetail> loadWideMgrPersonDetailsPlus(String deptId);
	
}
