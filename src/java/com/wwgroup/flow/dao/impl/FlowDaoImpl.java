package com.wwgroup.flow.dao.impl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.incrementer.OracleSequenceMaxValueIncrementer;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.StringUtils;

import com.wwgroup.common.Page;
import com.wwgroup.common.ToolSet;
import com.wwgroup.common.dao.AbstractJdbcDaoImpl;
import com.wwgroup.common.util.CommonUtil;
import java.com.wwgroup.flow.bo.Flow;
import com.wwgroup.flow.bo.FlowAttachment;
import com.wwgroup.flow.bo.FlowContent;
import com.wwgroup.flow.bo.FlowType;
import com.wwgroup.flow.bo.PersonDetail;
import com.wwgroup.flow.bo.WorkStage;
import com.wwgroup.flow.bo.helper.DescionMaker;
import com.wwgroup.flow.bo.helper.FlowStatus;
import com.wwgroup.flow.bo.helper.JointSignType;
import com.wwgroup.flow.bo.helper.PersonTYPE;
import com.wwgroup.flow.bo.work.BusinessSignWork;
import com.wwgroup.flow.bo.work.CancelWork;
import com.wwgroup.flow.bo.work.CenterJoinSignWork;
import com.wwgroup.flow.bo.work.CenterStepByStepWork;
import com.wwgroup.flow.bo.work.ChairmanSignWork;
import com.wwgroup.flow.bo.work.SubmitFBossSignWork;
import com.wwgroup.flow.bo.work.CmpcodeJoinSignWork;
import com.wwgroup.flow.bo.work.ConfirmWork;
import com.wwgroup.flow.bo.work.CopyDeptWork;
import com.wwgroup.flow.bo.work.CreateWork;
import com.wwgroup.flow.bo.work.DeptStepByStepWork;
import com.wwgroup.flow.bo.work.FinalPlusSignWork;
import com.wwgroup.flow.bo.work.FinalSignWork;
import com.wwgroup.flow.bo.work.InnerJointSignWork;
import com.wwgroup.flow.bo.work.JointSignWork;
import com.wwgroup.flow.bo.work.MyDelegateWork;
import com.wwgroup.flow.bo.work.MyWork;
import com.wwgroup.flow.bo.work.NextBusinessSignWork;
import com.wwgroup.flow.bo.work.NextFinalSignWork;
import com.wwgroup.flow.bo.work.NextStepByStepWork;
import com.wwgroup.flow.bo.work.RejectWork;
import com.wwgroup.flow.bo.work.SecondFinalSignWork;
import com.wwgroup.flow.bo.work.StepByStepWork;
import com.wwgroup.flow.bo.work.SystemJoinSignWork;
import com.wwgroup.flow.bo.work.XZJointSignWork;
import com.wwgroup.flow.dao.FlowDao;
import com.wwgroup.flow.dto.AdvancedSearchDTO;
import com.wwgroup.flow.dto.Branch;
import com.wwgroup.flow.dto.ConfidentialSearchDTO;
import com.wwgroup.flow.dto.ConfidentialSearchbossDTO;
import com.wwgroup.flow.dto.FormStatus;
import com.wwgroup.flow.dto.LogDTO;
import com.wwgroup.flow.dto.LogInfo;
import com.wwgroup.flow.dto.MyWorkDTO;
import com.wwgroup.flow.dto.MyWorkHistory;
import com.wwgroup.flow.dto.WorkRole;
import com.wwgroup.organ.bo.SystemGroups;
import com.wwgroup.organ.dao.OrganDao;
import com.wwgroup.user.bo.EmployeePos;

public class FlowDaoImpl extends AbstractJdbcDaoImpl implements
		InitializingBean, FlowDao {

	private OracleSequenceMaxValueIncrementer flow_incr;

	private SimpleJdbcInsert flow_jdbcInsert;

	private OracleSequenceMaxValueIncrementer persondetail_incr;

	private SimpleJdbcInsert persondetail_jdbcInsert;

	private OracleSequenceMaxValueIncrementer flowwork_incr;

	private SimpleJdbcInsert flowwork_jdbcInsert;

	private OracleSequenceMaxValueIncrementer flowcontent_incr;

	private SimpleJdbcInsert flowcontent_jdbcInsert;

	private OracleSequenceMaxValueIncrementer flowattachment_incr;

	// private SimpleJdbcInsert QIANCHENFLOWATTACHMENT_jdbcInsert;
	
	private SimpleJdbcInsert log_jdbcInsert;

	private LobHandler lobHandler;

	private OracleSequenceMaxValueIncrementer deptcount_incr;

	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	protected class QianChenFlowRowMapper implements
			ParameterizedRowMapper<Flow> {
		@SuppressWarnings("finally")
		public Flow mapRow(ResultSet rs, int rowNum) {
			Flow flow = new Flow();
			try {
				flow.setId(rs.getInt("ID"));
				flow.setFlowType(FlowType.values()[rs.getInt("FLOWTYPE")]);
				flow.setCreateTime(rs.getTimestamp("CREATETIME").getTime());
				flow.setFormNum(rs.getString("FORMNUM"));
				flow.setStatus(FlowStatus.values()[rs.getInt("STATUS")]);
				flow.setDecionmaker(DescionMaker.values()[rs
						.getInt("DECIONMAKER")]);
				flow.setJointSignType(JointSignType.values()[rs
						.getInt("JOINTSIGNTYPE")]);
				String jointSignDeptId = rs.getString("JOINTSIGNDEPTIDS");
				if (!org.apache.commons.lang3.StringUtils
						.isEmpty(jointSignDeptId)) {
					String[] jointSignDeptIds = new String[] {};
					if (jointSignDeptId.indexOf(";") != -1) {
						jointSignDeptIds = jointSignDeptId.split(";");
					} else {
						jointSignDeptIds = new String[] { jointSignDeptId };
					}
					flow.setJointSignDeptIds(jointSignDeptIds);
				}
				flow.setJointSignDeptName(rs.getString("JOINTSIGNDEPTNAME"));
				flow.setCopyDeptIds(org.apache.commons.lang3.StringUtils.split(
						rs.getString("COPYDEPTIDS"), ";"));
				flow.setCopyDeptName(rs.getString("COPYDEPTNAME"));
				flow.setCopyDemo(rs.getString("COPYDEMO"));
				flow.setInnerJointSignIds(org.apache.commons.lang3.StringUtils
						.split(rs.getString("INNERJOINTSIGNIDS"), ";"));
				flow.setInnerJointSignName(rs.getString("INNERJOINTSIGNNAME"));

				flow.setShengheStep(rs.getInt("SHENGHESTEP"));
				flow.setShengheEmployeeId(rs.getString("SHENGHEEMPLOYEEID"));
				flow.setShengheDeptId(rs.getString("SHENGHEDEPTID"));
				flow.setShenghePostCode(rs.getString("SHENGHEPOSTCODE"));

				// add by Cao_Shengyong 2014-06-25
				flow.setSecondEmployeeId(rs.getString("SECONDEMPLOYEEID"));
				flow.setSecondDeptId(rs.getString("SECONDDEPTID"));
				flow.setSecondPostCode(rs.getString("SECONDPOSTCODE"));

				flow.setTempalte(rs.getInt("ISTEMPLATE") > 0);
				flow.setTemplateCreateId(rs.getString("TEMPLATECREATEID"));

				flow.setSelfConfirm(rs.getInt("SELFCONFIRM") > 0);
				flow.setLocal(rs.getInt("ISLOCAL") > 0);

				flow.setRenewTimes(rs.getInt("RENEWTIMES"));

				flow.setSubmitBoss(rs.getInt("SUBMITBOSS") > 0);
				
				flow.setSubmitFBoss(rs.getInt("SUBMITFBOSS") > 0);

				flow.setIsNew(rs.getString("ISNEW"));

				flow.setNextStep(FlowStatus.values()[rs.getInt("TEMPNEXTSTEP")]);
				String pendJointSignDeptId = rs
						.getString("TEMPJOINTSIGNDEPTIDS");
				if (!org.apache.commons.lang3.StringUtils
						.isEmpty(pendJointSignDeptId)) {
					String[] pendJointSignDeptIds = new String[] {};
					if (pendJointSignDeptId.indexOf(";") != -1) {
						pendJointSignDeptIds = pendJointSignDeptId.split(";");
					} else {
						pendJointSignDeptIds = new String[] { pendJointSignDeptId };
					}
					flow.setPendJointSignDeptIds(pendJointSignDeptIds);
				}
				flow.setLastEmployeeId(rs.getString("TEMPLASTEMPLOYEEID"));
				flow.setLastDeptId(rs.getString("TEMPLASTDEPTID"));
				flow.setLastPostCode(rs.getString("TEMPLASTPOSTCODE"));
				
				flow.setSubmitBOffice(rs.getInt("SUBMITBOFFICE") > 0);
				flow.setFileSys(rs.getString("FSYS"));
				
				flow.setChariman(rs.getInt("CHAIRMAN") > 0);
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return flow;
			}
		}
	}

	protected class QianChenFlowContentRowMapper implements
			ParameterizedRowMapper<FlowContent> {
		@SuppressWarnings("finally")
		public FlowContent mapRow(ResultSet rs, int rowNum) {
			FlowContent flow = new FlowContent();
			try {
				flow.setId(rs.getInt("ID"));
				flow.setFlowId(rs.getInt("flowId"));
				flow.setDeptName(rs.getString("DEPTNAME"));
				flow.setDeptId(rs.getString("DEPTID"));
				flow.setSecretLevel(rs.getInt("SECRETLEVEL"));
				flow.setExireLevel(rs.getInt("EXPIRELEVEL"));
				NumberFormat nf = NumberFormat.getInstance();
				nf.setGroupingUsed(false);
				BigDecimal bd = rs.getBigDecimal("CASH");
				if (bd != null){
					flow.setCash(nf.format(bd));
				} else {
					flow.setCash("0");
				}
				flow.setType(rs.getInt("TYPE"));
				flow.setTitle(rs.getString("TITLE"));
				flow.setDetail(lobHandler.getClobAsString(rs, "DETAIL"));
				flow.setScheme(rs.getString("SCHEME"));
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return flow;
			}
		}
	}

	protected class QianChenPersonDetailRowMapper implements
			ParameterizedRowMapper<PersonDetail> {
		@SuppressWarnings("finally")
		public PersonDetail mapRow(ResultSet rs, int rowNum) {
			PersonDetail flow = new PersonDetail();
			try {
				flow.setId(rs.getInt("ID"));
				flow.setFlowId(rs.getInt("flowId"));
				flow.setDeptName(rs.getString("DEPTNAME"));
				flow.setDeptId(rs.getString("DEPTID"));
				flow.setName(rs.getString("NAME"));
				flow.setEmployeeId(rs.getString("EMPNUMBER"));
				flow.setPostName(rs.getString("POSTNAME"));
				flow.setPostId(rs.getString("POSTID"));
				flow.setCompPhone(rs.getString("COMPPHONE"));
				flow.setDeptPath(rs.getString("DEPTPATH"));

				flow.setPostId(rs.getString("POSTID"));
				flow.setPostName(rs.getString("POSTNAME"));
				flow.setPostCode(rs.getString("POSTCODE"));
				flow.setMgrEmployeeid(rs.getString("MGREMPLOYEEID"));
				flow.setMgrPostcode(rs.getString("MGRPOSTCODE"));
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return flow;
			}
		}
	}

	protected class QianChenFlowAttachmentRowMapper implements
			ParameterizedRowMapper<FlowAttachment> {

		@SuppressWarnings("finally")
		public FlowAttachment mapRow(ResultSet rs, int rowNum) {
			FlowAttachment flow = new FlowAttachment();
			try {
				flow.setId(rs.getInt("ID"));
				flow.setFlowId(rs.getInt("flowId"));
				flow.setCreateTime(rs.getTimestamp("CREATETIME").getTime());
				flow.setAttachmentName(rs.getString("ATTACHMENTNAME"));
				flow.setAttachment(lobHandler.getBlobAsBinaryStream(rs,
						"ATTACHMENT"));
				flow.setEmployeeId(rs.getString("CREATEEMPLOYEEID"));
				flow.setEmployeeName(rs.getString("CREATEEMPLOYEENAME"));
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return flow;
			}
		}
	}

	protected class WorkRowMapper implements ParameterizedRowMapper<MyWork> {
		public MyWork mapRow(ResultSet rs, int rowNum) {
			try {
				MyWork work = null;
				int workstage = rs.getInt("WORKSTAGE");
				if (workstage == WorkStage.CREATE.ordinal()) {
					work = new CreateWork();
				} else if (workstage == WorkStage.INNERJOINTSIGN.ordinal()) {
					work = new InnerJointSignWork();
				} else if (workstage == WorkStage.DEPT_CHENGHE.ordinal()) {
					work = new DeptStepByStepWork();
				} else if (workstage == WorkStage.CENTER_CHENGHE.ordinal()) {
					work = new CenterStepByStepWork();
				} else if (workstage == WorkStage.FCHENGHE.ordinal()){
					work = new NextStepByStepWork();
				} else if (workstage == WorkStage.CHENGHE.ordinal()) {
					work = new StepByStepWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));
					((StepByStepWork) work).setWorknum(rs
							.getInt("WORKNUM"));
					((StepByStepWork) work).setParentId(rs
							.getInt("parentId"));
				} else if (workstage == WorkStage.CANCEL.ordinal()) {
					work = new CancelWork();
				} else if (workstage == WorkStage.REJECT.ordinal()) {
					work = new RejectWork();
				} else if (workstage == WorkStage.COPYDEPT.ordinal()) {
					work = new CopyDeptWork();
				} else if (workstage == WorkStage.CONFIRM.ordinal()) {
					work = new ConfirmWork();
				} else if (workstage == WorkStage.FBUSINESS_SIGN.ordinal()) {
					work = new NextBusinessSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					((NextBusinessSignWork) work).setWorknum(rs
							.getInt("WORKNUM"));
					((NextBusinessSignWork) work).setParentId(rs
							.getInt("parentId"));
				} else if (workstage == WorkStage.BUSINESS_SIGN.ordinal()) {
					work = new BusinessSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					((BusinessSignWork) work).setWorknum(rs.getInt("WORKNUM"));
					((BusinessSignWork) work)
							.setParentId(rs.getInt("parentId"));
				} else if (workstage == WorkStage.DIVISION_SIGN.ordinal()) {
					work = new SecondFinalSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					((SecondFinalSignWork) work).setWorknum(rs
							.getInt("WORKNUM"));
					((SecondFinalSignWork) work).setParentId(rs
							.getInt("parentId"));
				} else if (workstage == WorkStage.FBOSS_SIGN.ordinal()) {
					work = new NextFinalSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					((NextFinalSignWork) work).setWorknum(rs.getInt("WORKNUM"));
					((NextFinalSignWork) work).setParentId(rs
							.getInt("parentId"));
				} else if (workstage == WorkStage.BOSS_SIGN.ordinal()) {
					work = new FinalSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					((FinalSignWork) work).setWorknum(rs.getInt("WORKNUM"));
					((FinalSignWork) work).setParentId(rs.getInt("parentId"));
				} else if (workstage == WorkStage.BOSSPLUS_SIGN.ordinal()){
					work = new FinalPlusSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					((FinalPlusSignWork) work).setWorknum(rs.getInt("WORKNUM"));
					((FinalPlusSignWork) work).setParentId(rs.getInt("parentId"));
				} else if (workstage == WorkStage.JOINTSIGN.ordinal()) {
					work = new JointSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					work.setHb_Agree(rs.getString("HB_AGREE"));
					work.setHb_ChengHe(rs.getString("HB_CHENGHE"));
					work.setHb_ChengHeEnd(rs.getString("HB_CHENGHEEND"));
					work.setHb_JoinSignStartId(rs
							.getString("HB_JOINSIGNSTARTID"));
					work.setHb_JoinStartEmployeeId(rs
							.getString("HB_JOINSTARTEMPLOYEEID"));

					((JointSignWork) work).setWorknum(rs.getInt("WORKNUM"));
					((JointSignWork) work).setParentId(rs.getInt("parentId"));
				} else if (workstage == WorkStage.CENTERJOINTSIGN.ordinal()) {
					work = new CenterJoinSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					work.setHb_Agree(rs.getString("HB_AGREE"));
					work.setHb_ChengHe(rs.getString("HB_CHENGHE"));
					work.setHb_ChengHeEnd(rs.getString("HB_CHENGHEEND"));
					work.setHb_JoinSignStartId(rs
							.getString("HB_JOINSIGNSTARTID"));
					work.setHb_JoinStartEmployeeId(rs
							.getString("HB_JOINSTARTEMPLOYEEID"));

					((CenterJoinSignWork) work)
							.setWorknum(rs.getInt("WORKNUM"));
					((CenterJoinSignWork) work).setParentId(rs
							.getInt("parentId"));
				} else if (workstage == WorkStage.CMPCODEJOINTSIGN.ordinal()) {
					work = new CmpcodeJoinSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					work.setHb_Agree(rs.getString("HB_AGREE"));
					work.setHb_ChengHe(rs.getString("HB_CHENGHE"));
					work.setHb_ChengHeEnd(rs.getString("HB_CHENGHEEND"));
					work.setHb_JoinSignStartId(rs
							.getString("HB_JOINSIGNSTARTID"));
					work.setHb_JoinStartEmployeeId(rs
							.getString("HB_JOINSTARTEMPLOYEEID"));

					((CmpcodeJoinSignWork) work).setWorknum(rs
							.getInt("WORKNUM"));
					((CmpcodeJoinSignWork) work).setParentId(rs
							.getInt("parentId"));
				} else if (workstage == WorkStage.SYSTEMJOINTSIGN.ordinal()) {
					work = new SystemJoinSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					work.setHb_Agree(rs.getString("HB_AGREE"));
					work.setHb_ChengHe(rs.getString("HB_CHENGHE"));
					work.setHb_ChengHeEnd(rs.getString("HB_CHENGHEEND"));
					work.setHb_JoinSignStartId(rs
							.getString("HB_JOINSIGNSTARTID"));
					work.setHb_JoinStartEmployeeId(rs
							.getString("HB_JOINSTARTEMPLOYEEID"));

					((SystemJoinSignWork) work)
							.setWorknum(rs.getInt("WORKNUM"));
					((SystemJoinSignWork) work).setParentId(rs
							.getInt("parentId"));
				} else if (workstage == WorkStage.XZJOINTSIGN.ordinal()){
					work = new XZJointSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					work.setHb_Agree(rs.getString("HB_AGREE"));
					work.setHb_ChengHe(rs.getString("HB_CHENGHE"));
					work.setHb_ChengHeEnd(rs.getString("HB_CHENGHEEND"));
					work.setHb_JoinSignStartId(rs
							.getString("HB_JOINSIGNSTARTID"));
					work.setHb_JoinStartEmployeeId(rs
							.getString("HB_JOINSTARTEMPLOYEEID"));

					((XZJointSignWork) work)
							.setWorknum(rs.getInt("WORKNUM"));
					((XZJointSignWork) work).setParentId(rs
							.getInt("parentId"));
				} else if (workstage == WorkStage.CHAIRMAN_SIGN.ordinal()) {
					work = new ChairmanSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					((ChairmanSignWork) work).setWorknum(rs.getInt("WORKNUM"));
					((ChairmanSignWork) work).setParentId(rs.getInt("parentId"));
				} else if (workstage == WorkStage.SubmitFBoss_SIGN.ordinal()) {
					work = new SubmitFBossSignWork();
					work.setJoinSignStartId(rs.getString("JOINSIGNSTARTID"));
					work.setJoinStartEmployeeId(rs
							.getString("joinStartEmployeeId"));
					work.setJoinCycle(rs.getInt("JOINCYCLE"));

					((SubmitFBossSignWork) work).setWorknum(rs.getInt("WORKNUM"));
					((SubmitFBossSignWork) work).setParentId(rs.getInt("parentId"));
				}
				if (work != null) {
					int flowType = rs.getInt("FLOWTYPE");
					if (flowType == FlowType.QIANCHENG.ordinal()) {
						work.setFlowType(FlowType.QIANCHENG);
					}
					if (flowType == FlowType.NEILIAN.ordinal()) {
						work.setFlowType(FlowType.NEILIAN);
					}
					work.setId(rs.getLong("ID"));
					work.setFlowId(rs.getLong("FLOWID"));
					work.setDeptName(rs.getString("DEPTNAME"));
					work.setDeptId(rs.getString("DEPTID"));
					work.setCreateTime(rs.getTimestamp(7).getTime());
					work.setEmployeeId(rs.getString("EMPLOYEEID"));

					Timestamp finishTime = rs.getTimestamp(7);
					if (finishTime != null) {
						work.setFinishTime(finishTime.getTime());
					}
					work.setOpinion(rs.getString("OPINION"));
					work.setStatus(FlowStatus.values()[rs.getInt("STATUS")]);
					// POSTCODE, DEPTCODE, A_DEPTCODE, CMPCODE
					work.setPostCode(rs.getString("POSTCODE"));
					work.setDeptCode(rs.getString("DEPTCODE"));
					work.setA_deptCode(rs.getString("A_DEPTCODE"));
					work.setCmpCode(rs.getString("CMPCODE"));
					work.setOldFlowId(rs.getLong("OLDFLOWID"));
					work.setOrgpath(rs.getString("ORGPATH"));
					return work;
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public Flow loadFlow(long flowId, FlowType flowType) {
		String sql = "select * from WW_FLOW flow where flow.id=:flowId and flow.flowtype=:flowType";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flowId", flowId);
		map.put("flowType", flowType.ordinal());
		ParameterizedRowMapper<Flow> mapper = new QianChenFlowRowMapper();
		return getJdbcTemplate().queryForObject(sql, mapper, map);
	}

	@Override
	// 是否已完成会办
	public boolean finishInnerJointSign(long flowId, String employeeId) {
		String sql = "select count(*) from ww_work t where oldflowid=:flowId and employeeid = :employeeid and finishtime is not null";
		return getJdbcTemplate().queryForInt(sql, flowId, employeeId) > 0 ? true
				: false;
	}

	@Override
	public Flow loadFlow(String formnum) {
		String sql = "select * from WW_FLOW flow where flow.formnum=:formnum";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formnum", formnum);
		ParameterizedRowMapper<Flow> mapper = new QianChenFlowRowMapper();
		return getJdbcTemplate().queryForObject(sql, mapper, map);
	}

	@Override
	public FlowContent loadContent(long flowId) {
		String sql = "select * from WW_FLOWCONTENT flow where flow.flowId=:flowId";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flowId", flowId);
		ParameterizedRowMapper<FlowContent> mapper = new QianChenFlowContentRowMapper();
		return getJdbcTemplate().queryForObject(sql, mapper, map);
	}

	@Override
	public PersonDetail loadPerson(long flowId, PersonTYPE personTYPE) {
		String sql = "select * from WW_PERSONDETAIL flow where flow.flowId=:flowId and type =:type";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flowId", flowId);
		map.put("type", personTYPE.ordinal());
		ParameterizedRowMapper<PersonDetail> mapper = new QianChenPersonDetailRowMapper();
		return getJdbcTemplate().queryForObject(sql, mapper, map);
	}

	@Override
	public MyWork loadWork(long workId) {
		String sql = "select FLOWTYPE, WORKSTAGE, ID, FLOWID, DEPTNAME, DEPTID, CREATETIME, FINISHTIME, OPINION, STATUS, JOINSIGNSTARTID, JOINCYCLE, joinStartEmployeeId, WORKNUM, EMPLOYEEID, "
				+ "POSTCODE, DEPTCODE, A_DEPTCODE, CMPCODE, PARENTID,OLDFLOWID,ORGPATH,HB_CHENGHE,HB_JOINSIGNSTARTID,HB_JOINSTARTEMPLOYEEID,HB_CHENGHEEND,HB_AGREE from WW_WORK flow where id =:id";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", workId);
		ParameterizedRowMapper<MyWork> mapper = new WorkRowMapper();
		return getJdbcTemplate().queryForObject(sql, mapper, map);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public FlowAttachment[] loadAttachments(long flowId) {
		String sql = "select * from WW_FLOWATTACHMENT flow where flow.flowId=:flowId order by flow.createtime";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flowId", flowId);
		ParameterizedRowMapper<FlowAttachment> mapper = new QianChenFlowAttachmentRowMapper();
		List attachments = getJdbcTemplate().query(sql, mapper, map);
		return (FlowAttachment[]) attachments
				.toArray(new FlowAttachment[attachments.size()]);
	}

	@Override
	public void saveFlow(Flow flow) {
		//logger.info(flow.getFormNum() + " Start 开始进行写入数据库的操作....");
		flow.setId(flow_incr.nextIntValue());

		Map<String, Object> parameters = new HashMap<String, Object>(16);
		parameters.put("ID", flow.getId());
		parameters.put("CREATETIME", new Timestamp(flow.getCreateTime()));
		parameters.put("FORMNUM", flow.getFormNum());
		parameters.put("CREATEPERSONID", flow.getCreatePerson().getId());
		parameters.put("ACTUALPERSONID", flow.getActualPerson().getId());
		parameters.put("CONTENTID", flow.getContent().getId());
		parameters.put("STATUS", flow.getStatus().ordinal());
		if (flow.getDecionmaker() != null) {
			parameters.put("DECIONMAKER", flow.getDecionmaker().ordinal());
		}
		parameters.put("JOINTSIGNTYPE", flow.getJointSignType().ordinal());
		parameters.put("JOINTSIGNDEPTIDS", StringUtils.arrayToDelimitedString(
				flow.getJointSignDeptIds(), ";"));
		parameters.put("JOINTSIGNDEPTNAME", flow.getJointSignDeptName());
		parameters.put("COPYDEPTIDS",
				StringUtils.arrayToDelimitedString(flow.getCopyDeptIds(), ";"));
		parameters.put("COPYDEPTNAME", flow.getCopyDeptName());
		parameters.put("COPYDEMO", flow.getCopyDemo());
		parameters.put("INNERJOINTSIGNIDS", StringUtils.arrayToDelimitedString(
				flow.getInnerJointSignIds(), ";"));
		parameters.put("INNERJOINTSIGNNAME", flow.getInnerJointSignName());
		parameters.put("FLOWTYPE", flow.getFlowType().ordinal());
		if (flow.isTempalte()) {
			parameters.put("TEMPLATECREATEID", flow.getTemplateCreateId());
			parameters.put("ISTEMPLATE", 1);
		}
		parameters.put("SELFCONFIRM", flow.isSelfConfirm());
		parameters.put("ISLOCAL", flow.isLocal());
		parameters.put("RENEWTIMES", flow.getRenewTimes());
		parameters.put("SUBMITBOSS", flow.isSubmitBoss());
		parameters.put("SUBMITFBOSS", flow.isSubmitFBoss());
		parameters.put("ISNEW", flow.getIsNew());
		parameters.put("SUBMITBOFFICE", flow.isSubmitBOffice());
		parameters.put("FSYS", flow.getFileSys());
		parameters.put("chairman", flow.isChariman());
		//logger.info(flow.getFormNum() + " End 开始进行写入数据库的操作....");
		flow_jdbcInsert.execute(parameters);
	}

	@Override
	public void savePerson(PersonDetail createPerson, PersonTYPE personTYPE) {
		Map<String, Object> parameters = new HashMap<String, Object>(9);
		createPerson.setId(persondetail_incr.nextIntValue());
		parameters.put("ID", createPerson.getId());
		parameters.put("FLOWID", createPerson.getFlowId());
		parameters.put("NAME", createPerson.getName());
		parameters.put("EMPNUMBER", createPerson.getEmployeeId());
		parameters.put("POSTNAME", createPerson.getPostName());
		parameters.put("POSTID", createPerson.getPostId());
		parameters.put("COMPPHONE", createPerson.getCompPhone());
		parameters.put("DEPTNAME", createPerson.getDeptName());
		parameters.put("DEPTID", createPerson.getDeptId());
		parameters.put("TYPE", personTYPE.ordinal());
		parameters.put("DEPTPATH", createPerson.getDeptPath());

		parameters.put("POSTID", createPerson.getPostId());
		parameters.put("POSTNAME", createPerson.getPostName());
		parameters.put("POSTCODE", createPerson.getPostCode());
		parameters.put("MGREMPLOYEEID", createPerson.getMgrEmployeeid());
		parameters.put("MGRPOSTCODE", createPerson.getMgrPostcode());
		persondetail_jdbcInsert.execute(parameters);
	}

	@Override
	public void saveContent(FlowContent content) {
		Map<String, Object> parameters = new HashMap<String, Object>(11);
		content.setId(flowcontent_incr.nextIntValue());
		parameters.put("ID", content.getId());
		parameters.put("FLOWID", content.getFlowId());
		parameters.put("DEPTNAME", content.getDeptName());
		parameters.put("DEPTID", content.getDeptId());
		parameters.put("SECRETLEVEL", content.getSecretLevel());
		parameters.put("EXPIRELEVEL", content.getExireLevel());
		parameters.put("CASH", content.getCash());
		parameters.put("TYPE", content.getType());
		parameters.put("TITLE", content.getTitle());
		parameters.put("DETAIL", content.getDetail());
		parameters.put("SCHEME", content.getScheme());
		flowcontent_jdbcInsert.execute(parameters);
	}

	@Override
	public void saveAttachments(FlowAttachment[] qianChenFlowAttachments) {
		for (int i = 0; i < qianChenFlowAttachments.length; i++) {
			final FlowAttachment attachment = qianChenFlowAttachments[i];
			attachment.setId(flowattachment_incr.nextIntValue());
			String sql = " INSERT INTO WW_FLOWATTACHMENT(ID,FLOWID,CREATEEMPLOYEEID,CREATEEMPLOYEENAME,CREATETIME,ATTACHMENTNAME,ATTACHMENT)"
					+ " VALUES(?,?,?,?,?,?,?)";
			getJdbcTemplate().execute(
					sql,
					new AbstractLobCreatingPreparedStatementCallback(
							this.lobHandler) {
						@Override
						protected void setValues(PreparedStatement ps,
								LobCreator lobCreator) throws SQLException,
								DataAccessException {
							ps.setLong(1, attachment.getId());
							ps.setLong(2, attachment.getFlowId());
							ps.setString(3, attachment.getEmployeeId());
							ps.setString(4, attachment.getEmployeeName());

							if (attachment.getCreateTime() == 0) {
								attachment.setCreateTime(System
										.currentTimeMillis());
							}

							ps.setTimestamp(5,
									new Timestamp(attachment.getCreateTime()));
							ps.setString(6, attachment.getAttachmentName());
							if (attachment.getAttachment() != null) {
								lobCreator.setBlobAsBytes(ps, 7, ToolSet
										.InputStreamToByte(attachment
												.getAttachment()));
							} else {
								lobCreator.setBlobAsBytes(ps, 7, new byte[0]);
							}
						}
					});
		}
	}

	@Override
	public void saveWork(MyWork work, OrganDao organDao) {
		Map<String, Object> parameters = new HashMap<String, Object>(11);
		work.setId(flowwork_incr.nextIntValue());
		parameters.put("ID", work.getId());
		parameters.put("FLOWID", work.getFlowId());
		parameters.put("DEPTNAME", work.getDeptName());
		parameters.put("EmployeeId", work.getEmployeeId());
		parameters.put("DEPTID", work.getDeptId());
		parameters.put("CREATETIME", new Timestamp(work.getCreateTime()));
		parameters.put("OPINION", work.getOpinion());
		parameters.put("STATUS", work.getStatus().ordinal());
		parameters.put("FLOWTYPE", work.getFlowType().ordinal());
		parameters.put("WORKSTAGE", work.getWorkStage().ordinal());
		if (!org.apache.commons.lang3.StringUtils.isEmpty(work
				.getJoinSignStartId())) {
			parameters.put("joinSignStartId", work.getJoinSignStartId());
		}
		if (!org.apache.commons.lang3.StringUtils.isEmpty(work
				.getJoinStartEmployeeId())) {
			parameters
					.put("joinStartEmployeeId", work.getJoinStartEmployeeId());
		}
		parameters.put("JOINCYCLE", work.getJoinCycle());
		/*
		 * 新增一种work判断
		 */
		if (JointSignWork.class.isInstance(work)
				|| CenterJoinSignWork.class.isInstance(work)
				|| CmpcodeJoinSignWork.class.isInstance(work)
				|| SystemJoinSignWork.class.isInstance(work)
				|| FinalSignWork.class.isInstance(work)
				|| NextFinalSignWork.class.isInstance(work)
				|| SecondFinalSignWork.class.isInstance(work)
				|| BusinessSignWork.class.isInstance(work)
				|| NextBusinessSignWork.class.isInstance(work)
				|| StepByStepWork.class.isInstance(work)
				|| XZJointSignWork.class.isInstance(work)
				|| ChairmanSignWork.class.isInstance(work)
				|| SubmitFBossSignWork.class.isInstance(work)
				|| FinalPlusSignWork.class.isInstance(work)) {
			parameters.put("WORKNUM", work.getWorknum());
			if (work.getParentId() == 0) {
				work.setParentId(work.getId());
			}
			parameters.put("PARENTID", work.getParentId());
		}
		// POSTCODE, DEPTCODE, A_DEPTCODE, CMPCODE
		// edit by Cao_Shengyong 2014-03-20
		// 如果当前审核人签核历程岗位代码为空，则查询到该用户在部门/中心/单位中的主岗位
		// 如果同一部门/中心/单位中有多个兼职岗位，则取第一个
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(work.getPostCode())) {
			parameters.put("POSTCODE", work.getPostCode());
		} else {
			EmployeePos employeePos = organDao.loadEmployeePos(
					work.getEmployeeId(), work.getDeptCode(),
					work.getA_deptCode(), work.getCmpCode());
			if (employeePos != null) {
				parameters.put("POSTCODE", employeePos.getPostcode());
			}
		}
		parameters.put("DEPTCODE", work.getDeptCode());
		parameters.put("A_DEPTCODE", work.getA_deptCode());
		parameters.put("CMPCODE", work.getCmpCode());
		parameters.put("OLDFLOWID", work.getOldFlowId() == 0 ? work.getFlowId()
				: work.getOldFlowId());
		// add by Cao_Shengyong 2013-12-24 增加将组织全路径记录至签核记录中
		if (organDao != null) {
			SystemGroups systemGroups = organDao.loadGroupsById(Integer
					.valueOf(work.getDeptId()));
			parameters.put("ORGPATH", systemGroups.getOrgPath());
		}
		parameters.put("HB_CHENGHE", work.getHb_ChengHe());
		parameters.put("HB_CHENGHEEND", work.getHb_ChengHeEnd());
		parameters.put("HB_JOINSIGNSTARTID", work.getHb_JoinSignStartId());
		parameters.put("HB_JOINSTARTEMPLOYEEID",
				work.getHb_JoinStartEmployeeId());
		parameters.put("HB_AGREE", work.getHb_Agree());
		
		parameters.put("EMPLOYEENAM", work.getEmployeenam());
		parameters.put("TITNAM", work.getTitlenam());
		
		//parameters.put("SERVERIP", CommonUtil.getServerIP());

		flowwork_jdbcInsert.execute(parameters);
	}

	@Override
	public String getFlowSequence(PersonDetail actualPerson) {
		// added by hzp 2012.9.24
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;

		String sql = "select DEPTCOUNT from WW_DEPTCOUNT d where d.cmpcode=? and d.deptcode=? and d.year=? and d.month=?";
		String insertSql = "insert into WW_DEPTCOUNT(ID, CMPCODE, DEPTCODE, YEAR, MONTH, DEPTCOUNT) values(?,?,?,?,?,1)";
		String updateSql = "update WW_DEPTCOUNT d set d.DEPTCOUNT=? where d.cmpcode=? and d.deptcode=? and d.year=? and d.month=?";

		List<Integer> result = getJdbcTemplate().query(sql,
				new ParameterizedRowMapper<Integer>() {
					public Integer mapRow(ResultSet rs, int rowNum) {
						int count = 0;
						try {
							count = rs.getInt("DEPTCOUNT");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						return count;
					}

				}, actualPerson.getCmpCode(), actualPerson.getDeptCode(), year,
				month);

		// 如果该部门此年月没有记录，则插入初始count为1的记录
		// 存在记录则更新count
		if (result.size() == 0) {
			getJdbcTemplate().update(insertSql,
					deptcount_incr.nextIntValue(), actualPerson.getCmpCode(),
					actualPerson.getDeptCode(), year, month);
			return "1";
		} else {
			getJdbcTemplate().update(updateSql, result.get(0) + 1,
					actualPerson.getCmpCode(), actualPerson.getDeptCode(),
					year, month);
			return String.valueOf(result.get(0) + 1);
		}

	}

	@Override
	public Page getWorksWithPage(PersonDetail person, int start, int size) {
		String sql = "select FLOWTYPE, WORKSTAGE, ID, FLOWID, DEPTNAME, DEPTID, CREATETIME, FINISHTIME, OPINION, STATUS, joinSignStartId, joinStartEmployeeId, JOINCYCLE, WORKNUM, "
				+ "POSTCODE, DEPTCODE, A_DEPTCODE, CMPCODE, PARENTID,OLDFLOWID,ORGPATH,HB_CHENGHE,HB_JOINSIGNSTARTID,HB_JOINSTARTEMPLOYEEID,HB_CHENGHEEND,HB_AGREE  from WW_WORK qcw where qcw.deptid =? and qcw.status =? ";
		ParameterizedRowMapper<MyWork> mapper = new WorkRowMapper();

		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, sql, mapper, person.getDeptId(),
				FlowStatus.DOING.ordinal());
		return page;
	}

	@Override
	public Page findFlowTemplatesByPage(PersonDetail actualPerson, int start,
			int size) {
		String sql = "select f.status as status, 1 as employeeId, 1 as postCode, 1 as workId, f.id as flowId, 1 as deptId, f.formnum as formnum, f.flowtype as flowType, f.isnew, f.createtime as createtime, "
				+ "content.title as title, creator.name as creatorName, actual.name as actualName "
				+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
				+ "left join ww_persondetail actual on f.id = actual.flowid "
				+ "left join ww_flowcontent content on f.id = content.flowid "
				+ "where f.TEMPLATECREATEID=? and f.ISTEMPLATE=1 and creator.type = 0 and actual.type = 1 order by f.createtime desc";
		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapper();
		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, sql, mapper, actualPerson.getEmployeeId());
		return page;
	}

	protected class MyWorkDTORowMapper implements
			ParameterizedRowMapper<MyWorkDTO> {

		// private String actualEmployeeId;

		private Map<String, WorkRole> workRoleMap;

		public MyWorkDTORowMapper() {
			// this.actualEmployeeId = actualPerson.getEmployeeId();
		}

		public MyWorkDTORowMapper(Map<String, WorkRole> workRoleMap) {
			this.workRoleMap = workRoleMap;
		}

		@SuppressWarnings("finally")
		public MyWorkDTO mapRow(ResultSet rs, int rowNum) {
			MyWorkDTO myWorkDTO = new MyWorkDTO();
			try {

				FlowStatus status = FlowStatus.values()[rs.getInt("status")];
				myWorkDTO.setFlowDisplayStatusName(status
						.getFlowDisplayStatusName());
				myWorkDTO.setWorkId(rs.getInt("workId"));
				myWorkDTO.setFlowId(rs.getInt("flowId"));
				myWorkDTO.setDeptId(rs.getInt("deptId"));
				myWorkDTO.setFormnum(rs.getString("formnum"));
				myWorkDTO
						.setCreateTime(String.valueOf(rs.getDate("createtime")));
				myWorkDTO.setTitle(rs.getString("title"));
				myWorkDTO.setCreatorName(rs.getString("creatorName"));
				myWorkDTO.setActualName(rs.getString("actualName"));
				myWorkDTO.setFlowType(FlowType.values()[rs.getInt("flowType")]);
				myWorkDTO.setIsNew(rs.getString("isnew"));

				String employeeId = rs.getString("employeeId");
				String postCode = rs.getString("postCode");
				String key = employeeId + postCode;
				if (workRoleMap.containsKey(key)) {
					myWorkDTO.setWorkRole(workRoleMap.get(key));
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return myWorkDTO;
			}
		}
	}
	
	// 增加isNew,用于判断当前表单是否新版本产生
	protected class MyWorkDTORowMapperPlus implements ParameterizedRowMapper<MyWorkDTO>{
		private Map<String, WorkRole> workRoleMap;
		public MyWorkDTORowMapperPlus(){}
		public MyWorkDTORowMapperPlus(Map<String, WorkRole> workRoleMap){
			this.workRoleMap = workRoleMap;
		}
		@SuppressWarnings("finally")
		public MyWorkDTO mapRow(ResultSet rs, int rowNum){
			MyWorkDTO myWorkDTO = new MyWorkDTO();
			try {
				FlowStatus status = FlowStatus.values()[rs.getInt("status")];
				myWorkDTO.setFlowDisplayStatusName(status.getFlowDisplayStatusName());
				myWorkDTO.setWorkId(rs.getInt("workId"));
				myWorkDTO.setFlowId(rs.getInt("flowId"));
				myWorkDTO.setDeptId(rs.getInt("deptId"));
				myWorkDTO.setFormnum(rs.getString("formnum"));
				myWorkDTO
						.setCreateTime(String.valueOf(rs.getDate("createtime")));
				myWorkDTO.setTitle(rs.getString("title"));
				myWorkDTO.setCreatorName(rs.getString("creatorName"));
				myWorkDTO.setActualName(rs.getString("actualName"));
				myWorkDTO.setFlowType(FlowType.values()[rs.getInt("flowType")]);
				myWorkDTO.setIsNew(rs.getString("isnew"));
				if (rs.getDate("endtime") != null) {
					myWorkDTO.setEndTime(String.valueOf(rs.getDate("endtime")));
				}

				String employeeId = rs.getString("employeeId");
				String postCode = rs.getString("postCode");
				String key = employeeId + postCode;
				if (workRoleMap != null){
					if (workRoleMap.containsKey(key)) {
						myWorkDTO.setWorkRole(workRoleMap.get(key));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				return myWorkDTO;
			}
		}
	}
	
	// 增加finishtime，已审核列表专用
	protected class MyWorkDTORowMapperPlusEx implements ParameterizedRowMapper<MyWorkDTO>{
		private Map<String, WorkRole> workRoleMap;
		public MyWorkDTORowMapperPlusEx(){}
		public MyWorkDTORowMapperPlusEx(Map<String, WorkRole> workRoleMap){
			this.workRoleMap = workRoleMap;
		}
		@SuppressWarnings("finally")
		public MyWorkDTO mapRow(ResultSet rs, int rowNum){
			MyWorkDTO myWorkDTO = new MyWorkDTO();
			try {
				FlowStatus status = FlowStatus.values()[rs.getInt("status")];
				myWorkDTO.setFlowDisplayStatusName(status.getFlowDisplayStatusName());
				myWorkDTO.setWorkId(rs.getInt("workId"));
				myWorkDTO.setFlowId(rs.getInt("flowId"));
				myWorkDTO.setDeptId(rs.getInt("deptId"));
				myWorkDTO.setFormnum(rs.getString("formnum"));
				myWorkDTO
						.setCreateTime(String.valueOf(rs.getDate("createtime")));
				myWorkDTO.setTitle(rs.getString("title"));
				myWorkDTO.setCreatorName(rs.getString("creatorName"));
				myWorkDTO.setActualName(rs.getString("actualName"));
				myWorkDTO.setFlowType(FlowType.values()[rs.getInt("flowType")]);
				myWorkDTO.setIsNew(rs.getString("isnew"));
				if (rs.getDate("endtime") != null) {
					myWorkDTO.setEndTime(String.valueOf(rs.getDate("endtime")));
				}
				if (rs.getDate("finishtime") != null) {
					myWorkDTO.setFinishTime(String.valueOf(rs.getDate("finishtime")));
				}
				String employeeId = rs.getString("employeeId");
				String postCode = rs.getString("postCode");
				String key = employeeId + postCode;
				if (workRoleMap != null){
					if (workRoleMap.containsKey(key)) {
						myWorkDTO.setWorkRole(workRoleMap.get(key));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				return myWorkDTO;
			}
		}
	}

	@Override
	public int getMyToDoWorkCount(PersonDetail actualPerson,
			PersonDetail[] agentees, PersonDetail[] assistedMgrs) {
		Map<String, WorkRole> workRoleMap = new HashMap<String, WorkRole>(8);
		List<String> assistEmployeeIdPostCodes = new ArrayList<String>(8);
		List<String> agentEmployeeIdPostCodes = new ArrayList<String>(8);
		for (PersonDetail assistPerson : assistedMgrs) {
			String key = assistPerson.getEmployeeId()
					+ assistPerson.getPostCode();
			// dlgMgrs.put(key, assistPerson);
			workRoleMap
					.put(key,
							assistPerson.isAllowAssignAction() ? WorkRole.ASSIST_ASSIGNED
									: WorkRole.ASSIST_NOT_ASSIGNED);
			assistEmployeeIdPostCodes.add(assistPerson.getEmployeeId()
					+ assistPerson.getPostCode());
		}
		for (PersonDetail agentPerson : agentees) {
			String key = agentPerson.getEmployeeId()
					+ agentPerson.getPostCode();
			if (!workRoleMap.containsKey(key)) {
				// dlgMgrs.put(key, agentPerson);
				workRoleMap.put(key, WorkRole.AGENT);
				agentEmployeeIdPostCodes.add(agentPerson.getEmployeeId()
						+ agentPerson.getPostCode());
			}
		}
		String key = actualPerson.getEmployeeId() + actualPerson.getPostCode();
		// dlgMgrs.put(key, actualPerson);
		workRoleMap.put(key, WorkRole.MYSELF);
		// employeeIdPostCodes.add(actualPerson.getEmployeeId() +
		// actualPerson.getPostCode());

		StringBuffer sql = new StringBuffer(
				"select f.status as status, w.EMPLOYEEID as employeeId, w.POSTCODE as postCode , w.id as workId, w.flowid as flowId, w.deptId as deptId, f.formnum as formnum, f.flowtype as flowType,"
						+ " f.createtime as createtime, content.title as title, creator.name as creatorName, actual.name as actualName "
						+ "from ww_work w left join ww_flow f on w.flowid = f.id "
						+ "left join ww_persondetail creator on f.id = creator.flowid "
						+ "left join ww_persondetail actual on f.id = actual.flowid "
						+ "left join ww_flowcontent content on f.id = content.flowid "
						+ "where w.status = ? and f.status != ?");
		// sql.append(" and (concat(concat(w.employeeid,w.POSTCODE),w.WORKSTAGE) in ( ");
		sql.append(" and (concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		// int i = 0;
		for (String employeeIdPostCode : assistEmployeeIdPostCodes) {
			// if (i != 0) {
			sql.append(" , ");
			// }
			// sql.append(" '" + employeeIdPostCode +
			// WorkStage.JOINTSIGN.ordinal() + "' ");
			sql.append(" '" + employeeIdPostCode + "' ");
			// i++;
		}
		sql.append(") ");

		sql.append(" or concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		// int i = 0;
		for (String employeeIdPostCode : agentEmployeeIdPostCodes) {
			// if (i != 0) {
			sql.append(" , ");
			// }
			sql.append(" '" + employeeIdPostCode + "' ");
			// i++;
		}
		sql.append(") ");

		sql.append(" or w.employeeid = '"
				+ actualPerson.getEmployeeId()
				+ "') and creator.type = 0 and actual.type = 1 order by f.createtime asc");

		String countSQL = "select count(*) from ("
				+ sql.toString() + " ) ";
		return getJdbcTemplate().queryForInt(
				countSQL,
				new Object[] { FlowStatus.DOING.ordinal(),
						FlowStatus.CANCEL.ordinal() });// ,
		// actualPerson.getEmployeeId()
	}

	@Override
	public Page findMyToDoWorkByPage(PersonDetail actualPerson,
			PersonDetail[] agentees, PersonDetail[] assistedMgrs, int start,
			int size) {
		// 加入了助理功能后 ，首先需要将助理和代理进行合并，如果有重复的出现以助理优先级为高，也就是以助理指派优先
		// 读取出计入后将work的employeeId与postCode和
		// 助理和代理的进行比较,比较对的情况下，首先给myWorkDTO.delegated(true)，然后还要将助理+是否允许指派动作的属性设置上，供前台判断
		// 这里的key值为 employeeId + postCode
		// Map<String, PersonDetail> dlgMgrs = new HashMap<String,
		// PersonDetail>(8);
		Map<String, WorkRole> workRoleMap = new HashMap<String, WorkRole>(8);
		List<String> assistEmployeeIdPostCodes = new ArrayList<String>(8);

		List<String> agentEmployeeIdPostCodes = new ArrayList<String>(8);

		for (PersonDetail assistPerson : assistedMgrs) {
			String key = assistPerson.getEmployeeId()
					+ assistPerson.getPostCode();
			// dlgMgrs.put(key, assistPerson);
			workRoleMap
					.put(key,
							assistPerson.isAllowAssignAction() ? WorkRole.ASSIST_ASSIGNED
									: WorkRole.ASSIST_NOT_ASSIGNED);
			assistEmployeeIdPostCodes.add(assistPerson.getEmployeeId()
					+ assistPerson.getPostCode());
		}
		for (PersonDetail agentPerson : agentees) {
			String key = agentPerson.getEmployeeId()
					+ agentPerson.getPostCode();
			if (!workRoleMap.containsKey(key)) {
				// dlgMgrs.put(key, agentPerson);
				workRoleMap.put(key, WorkRole.AGENT);
				agentEmployeeIdPostCodes.add(agentPerson.getEmployeeId()
						+ agentPerson.getPostCode());
			}
		}
		String key = actualPerson.getEmployeeId() + actualPerson.getPostCode();
		// dlgMgrs.put(key, actualPerson);
		workRoleMap.put(key, WorkRole.MYSELF);
		// employeeIdPostCodes.add(actualPerson.getEmployeeId() +
		// actualPerson.getPostCode());

		StringBuffer sql = new StringBuffer(
				"select f.status as status, w.EMPLOYEEID as employeeId, w.POSTCODE as postCode , w.id as workId, w.flowid as flowId, w.deptId as deptId, f.formnum as formnum, f.flowtype as flowType,"
						+ " f.createtime as createtime, f.isnew as isnew, content.title as title, creator.name as creatorName, actual.name as actualName "
						+ "from ww_work w left join ww_flow f on w.flowid = f.id "
						+ "left join ww_persondetail creator on f.id = creator.flowid "
						+ "left join ww_persondetail actual on f.id = actual.flowid "
						+ "left join ww_flowcontent content on f.id = content.flowid "
						+ "where w.status = ? and f.status != ?");

		// hzp mod: 取消WorkStage.JOINTSIGN限制（743 & 751）
		// sql.append(" and (concat(concat(w.employeeid,w.POSTCODE),w.WORKSTAGE) in ( ");
		sql.append(" and (concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		// int i = 0;
		for (String employeeIdPostCode : assistEmployeeIdPostCodes) {
			// if (i != 0) {
			sql.append(" , ");
			// }
			// sql.append(" '" + employeeIdPostCode +
			// WorkStage.JOINTSIGN.ordinal() + "' ");
			sql.append(" '" + employeeIdPostCode + "' ");
			// i++;
		}
		sql.append(") ");

		sql.append(" or concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		// int i = 0;
		// System.out.println(agentEmployeeIdPostCodes);
		for (String employeeIdPostCode : agentEmployeeIdPostCodes) {
			// if (i != 0) {
			sql.append(" , ");
			// }
			sql.append(" '" + employeeIdPostCode + "' ");
			// i++;
		}
		sql.append(") ");

		sql.append(" or w.employeeid = '"
				+ actualPerson.getEmployeeId()
				+ "') and creator.type = 0 and actual.type = 1 order by f.createtime DESC");
		// System.out.println(sql.toString());
		// System.out.println(FlowStatus.DOING.ordinal() + " # " +
		// FlowStatus.CANCEL.ordinal());
		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapper(
				workRoleMap);
		// 构造分页信息
		//System.out.println(sql.toString());
		//System.out.println(FlowStatus.DOING.ordinal() + " # " + FlowStatus.CANCEL.ordinal());
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, sql.toString(), mapper,
				FlowStatus.DOING.ordinal(), FlowStatus.CANCEL.ordinal());// ,
		// actualPerson.getEmployeeId()
		return page;
	}

	@Override
	public Page advanceSearch(AdvancedSearchDTO oneSearch, int start, int size) {
		StringBuffer whereClause = new StringBuffer();
		StringBuffer selectClause = new StringBuffer();
		// distinct work.id as workId, work.flowid as flowId, ww_work work left
		// join on work.flowid = flow.id
		selectClause
				.append("select distinct flow.formnum as formnum, flow.status as status, 1 as employeeId, 1 as postCode, 1 as workId, flow.id as flowId, 1 as deptId, flow.flowtype as flowType,flow.isnew, flow.createtime as createtime, "
						+ "content.title as title, creator.name as creatorName, actual.name as actualName "
						/*+ " ,case when flow.status=20 or flow.status=19 or flow.status=17 then "
						+ " (select max(w.finishtime) from ww_work w where w.flowid=flow.id and w.finishtime is not null) "
						+ "  end endtime "*/
						+ " , flow.endtime "
						+ " from ww_flow flow "
						+ " left join ww_persondetail creator on flow.id = creator.flowid "
						+ " left join ww_persondetail actual on flow.id = actual.flowid "
						+ " left join ww_flowcontent content on flow.id = content.flowid "
						+ " left join ww_work work on flow.id = work.flowid ");

		List<Object> whereClauseParams = new ArrayList<Object>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		// 表单区分
		whereClause
				.append(" where flow.FLOWTYPE = ? and flow.templatecreateid is null and creator.type = 0 and actual.type = 1 ");
		whereClauseParams.add(oneSearch.getFlowType().ordinal());
		
		//System.out.println(org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getLocalType()) + "  ########### ");
		
		try {
			long startTime = 0L, endTime = 0L;
			// 如果为空那么就变成系统今天日期
			if (!org.apache.commons.lang3.StringUtils.isEmpty(oneSearch
					.getEndTime())) {
				endTime = sdf.parse(oneSearch.getEndTime()).getTime() + 24 * 60
						* 60 * 1000;
			} else {
				endTime = System.currentTimeMillis();
			}
			whereClauseParams.add(new Timestamp(endTime));
			whereClause.append(" and flow.createTime <= ?");

			// 组织时间参数
			if (!org.apache.commons.lang3.StringUtils.isEmpty(oneSearch
					.getStartTime())) {
				startTime = sdf.parse(oneSearch.getStartTime()).getTime();
			} else {
				// 当前endTime的上一个月时间
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(endTime);
				calendar.add(Calendar.MONTH, -1);
				startTime = calendar.getTimeInMillis();
			}
			whereClauseParams.add(new Timestamp(startTime));
			whereClause.append(" and flow.createTime >= ?");
		} catch (ParseException ex) {
			ex.printStackTrace();
		}

		try {
			long closeStartTime = 0L, closeEndTime = 0L;
			if (!org.apache.commons.lang3.StringUtils.isEmpty(oneSearch.getCloseEndTime())) {
				closeEndTime = sdf.parse(oneSearch.getCloseEndTime()).getTime() + 24 * 60
						* 60 * 1000;
				whereClauseParams.add(new Timestamp(closeEndTime));
				whereClause.append(" and flow.endTime <= ?");
			}
			if (!org.apache.commons.lang3.StringUtils.isEmpty(oneSearch.getCloseStartTime())) {
				closeStartTime = sdf.parse(oneSearch.getCloseStartTime()).getTime();
				whereClauseParams.add(new Timestamp(closeStartTime));
				whereClause.append(" and flow.endTime >= ?");
			}
			
		} catch (ParseException ex) {
			ex.printStackTrace();
		}

		// 处理 流程流转状态
		FormStatus formStatus = oneSearch.getFormStatus();

		// --注意“重起案”可为过程中状态，而非flow最后状态，所以从work表中的status判断
		if (formStatus == FormStatus.RENEWED) {
			whereClause.append(" and work.status = "
					+ formStatus.transform()[0].ordinal());
		}

		if (formStatus != null && formStatus != FormStatus.ALL
				&& formStatus != FormStatus.RENEWED) {
			whereClause.append(" and (");
			// 语句格式类似 ：status = 1 or status = 2 ...
			int i = 0;
			FlowStatus[] flowStatus = formStatus.transform();
			for (FlowStatus status : flowStatus) {
				if (i != 0) {
					whereClause.append(" or ");
				}
				whereClause.append(" flow.status = " + status.ordinal() + " ");
				i++;
			}
			whereClause.append(" )");
		}

		/**
		 * add by Cao_Shengyong 2014-03-26 优化查询条件 新增一个临时变量，用于设置SQL语句中增加的条件
		 * 该条件是表示在签核记录中存在当前登录人的工号，包括代理人
		 */
		String tmpWhere = "";
		if (oneSearch.getUser() != null) {
			tmpWhere = " or flow.id in (select distinct (w.flowid) from ww_work w where w.employeeid='"
					+ oneSearch.getUser().getEmployeeId()
					+ "' or (concat(w.employeeid, w.postcode) in ('"
					+ oneSearch.getUser().getEmployeeId()
					+ "') or w.dlg_employeeid='"
					+ oneSearch.getUser().getEmployeeId()
					+ "') or concat(w.employeeid, w.postcode) in ('"
					+ oneSearch.getUser().getEmployeeId() + "'))";
		}

		// 处理流程分支条件
		if (oneSearch.getBranch() == Branch.MySubmit) {
			// 我申请的（FlowType）表单, 也就是实际创建人是登陆的人
			// whereClause.append(" and actual.empnumber = ? and actual.postid = ? ");
			whereClause.append(" and actual.empnumber = ? ");
			whereClauseParams.add(oneSearch.getUser().getEmployeeId());
			// whereClauseParams.add(oneSearch.getUser().getPostId());
		} else if (oneSearch.getBranch() == Branch.ThisUnitSubmit) {
			// 创建人表中的单位 与登陆人的单位

			if (oneSearch.getUserDeptIds() != null) {
				whereClause.append(" and actual.deptid in ( ");
				int i = 0;
				for (String deptId : oneSearch.getUserDeptIds()) {
					if (i != 0) {
						whereClause.append(" , ");
					}
					whereClause.append(" '" + deptId + "' ");
					i++;
				}
				whereClause.append(") ");
			}

			whereClause
					.append(" and ? in (select hg.employeeid from hr_groupmgr hg)");
			whereClauseParams.add(oneSearch.getUser().getEmployeeId());
		} else if (oneSearch.getBranch() == Branch.SubDeptSubmit) {
			// 关于下下部门的定义：周德佶给予回复 20130122 16:12:56
			// 部门、中心主管、单位最高主管下属的员工提交的单。仅为涉及提交的表单
			whereClause.append(" and (actual.mgremployeeid = ? ");
			if (oneSearch.getSubDeptIds() != null) {
				whereClause.append(" or (actual.deptid in ( ");
				int i = 0;
				for (String deptId : oneSearch.getSubDeptIds()) {
					if (i != 0) {
						whereClause.append(" , ");
					}
					whereClause.append(" '" + deptId + "' ");
					i++;
				}
				whereClause.append("))  ) ");
			} else {
				// 对于没有下辖部门的情况，就给一个不存在的deptid
				whereClause.append(" or actual.deptid in (9999999) ) ");
			}
			//whereClause.append(")");
			whereClauseParams.add(oneSearch.getEmployeeId());
		} else if (oneSearch.getBranch() == Branch.MyApproved) {
			// 最终是由登陆人，做核决的表单
			whereClause
			// .append(" and exists(select work.flowid from ww_work work where work.flowid = flow.id and (work.status=? or work.status=?) and work.employeeid = ? and work.postcode = ?)");
					.append(" and exists(select work.flowid from ww_work work where work.flowid = flow.id and (work.status=? or work.status=?) and work.employeeid = ?)");
			whereClauseParams.add(FlowStatus.APPROVED.ordinal());
			whereClauseParams.add(FlowStatus.AGREE.ordinal());
			// whereClauseParams.add(WorkStage.BOSS_SIGN);
			whereClauseParams.add(oneSearch.getUser().getEmployeeId());
			// whereClauseParams.add(oneSearch.getUser().getPostCode());
		} else if (oneSearch.getBranch() == Branch.MyAccepted) {
			// 还未处理的
			whereClause
					.append(" and exists(select work.flowid from ww_work work where work.flowid = flow.id and (work.status = ? or work.status = ? or work.status = ? or work.status = ?) and work.employeeid = ?)");
			whereClauseParams.add(FlowStatus.DOING.ordinal());
			whereClauseParams.add(FlowStatus.AGREE.ordinal());
			whereClauseParams.add(FlowStatus.APPROVED.ordinal());
			whereClauseParams.add(FlowStatus.REJECT.ordinal());
			whereClauseParams.add(oneSearch.getUser().getEmployeeId());
		}

		// 根据表单编号与主旨查询需限制在权限范围内，即 我申请的&本单位发文的&下辖部门提交的
		// 复制上面代码

		else if (oneSearch.getBranch() == Branch.FormNum) {
			if (null != oneSearch.getUser()) {
				// Branch.MySubmit
				whereClause
						.append(" and ((actual.empnumber = ? and actual.postid = ?) ");
				whereClauseParams.add(oneSearch.getUser().getEmployeeId());
				whereClauseParams.add(oneSearch.getUser().getPostId());

				// Branch.ThisUnitSubmit
				if (oneSearch.getUserDeptIds() != null) {
					whereClause.append(" or (actual.deptid in ( ");
					int i = 0;
					for (String deptId : oneSearch.getUserDeptIds()) {
						if (i != 0) {
							whereClause.append(" , ");
						}
						whereClause.append(" '" + deptId + "' ");
						i++;
					}
					whereClause.append(") ");
				}
				whereClause
						.append(" and ? in (select hg.employeeid from hr_groupmgr hg))");
				whereClauseParams.add(oneSearch.getUser().getEmployeeId());

				// Branch.SubDeptSubmit
				if (oneSearch.getSubDeptIds() != null) {
					whereClause.append(" or (actual.deptid in ( ");
					int i = 0;
					for (String deptId : oneSearch.getSubDeptIds()) {
						if (i != 0) {
							whereClause.append(" , ");
						}
						whereClause.append(" '" + deptId + "' ");
						i++;
					}
					whereClause.append(")) " + tmpWhere + " ) ");
				} else {
					// 对于没有下辖部门的情况，就给一个不存在的deptid
					whereClause.append(" or actual.deptid in (9999999) "
							+ tmpWhere + " ) ");
				}
			}
			// 创建人表中的单位 与登陆人的单位
			if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getFormNum())){
				whereClause.append(" and flow.formnum like '%"
						+ oneSearch.getFormNum() + "%' ");
			}
		} else if (oneSearch.getBranch() == Branch.Title) {
			if (null != oneSearch.getUser()) {
				// System.out.println(oneSearch.getUser().getEmployeeId() + " ^ " + oneSearch.getUser().getPostId());
				// Branch.MySubmit
				whereClause
						.append(" and ((actual.empnumber = ? and actual.postid = ?) ");
				whereClauseParams.add(oneSearch.getUser().getEmployeeId());
				whereClauseParams.add(oneSearch.getUser().getPostId());

				// Branch.ThisUnitSubmit
				if (oneSearch.getUserDeptIds() != null) {
					whereClause.append(" or (actual.deptid in ( ");
					int i = 0;
					for (String deptId : oneSearch.getUserDeptIds()) {
						if (i != 0) {
							whereClause.append(" , ");
						}
						whereClause.append(" '" + deptId + "' ");
						i++;
					}
					whereClause.append(") ");
				}
				whereClause
						.append(" and ? in (select hg.employeeid from hr_groupmgr hg))");
				whereClauseParams.add(oneSearch.getUser().getEmployeeId());

				// Branch.SubDeptSubmit
				if (oneSearch.getSubDeptIds() != null) {
					whereClause.append(" or (actual.deptid in ( ");
					int i = 0;
					for (String deptId : oneSearch.getSubDeptIds()) {
						if (i != 0) {
							whereClause.append(" , ");
						}
						whereClause.append(" '" + deptId + "' ");
						i++;
					}
					whereClause.append(")) " + tmpWhere + " ) ");
				} else {
					// 对于没有下辖部门的情况，就给一个不存在的deptid
					whereClause.append(" or actual.deptid in (9999999) "
							+ tmpWhere + " ) ");
				}
			}
			// 创建人表中的单位 与登陆人的单位
			if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getTitle())){
				whereClause.append(" and content.title like '%"
						+ oneSearch.getTitle() + "%' ");
			}
		} else if (oneSearch.getBranch() == Branch.MyDlg) {
			whereClause.append(" and work.dlg_employeeid = '"
					+ oneSearch.getUser().getEmployeeId() + "' ");
		}

		if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getLocalType())){
			whereClause.append(" and flow.islocal=? ");
			whereClauseParams.add(oneSearch.getLocalType());
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getCreator())){
			whereClause.append(" and actual.empnumber=? ");
			whereClauseParams.add(oneSearch.getCreator());
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getCreatCmp())){
			whereClause.append(" and flow.formnum like '%" + oneSearch.getCreatCmp() + "%'");
		}
		//20190121  sunhaoyue add

		if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getDepartmentName())){
			whereClause.append(" and flow.depname like '%" + oneSearch.getDepartmentName() + "%'");
		}
		// 标签排序处理
		whereClause.append(" order by " + oneSearch.getOrderBy() + " "
				+ oneSearch.getSorder() + " ");
		// Order order = oneSearch.getOrder();
		// System.out.println(order);
		// if (order == Order.CreateTime) {
		// whereClause.append(" order by flow.createTime desc");
		// }

		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapperPlus();
		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);

		String querySQL = selectClause.append(whereClause).toString();
		// System.out.println(querySQL);
		page = queryWithPage(page, querySQL, mapper,
				whereClauseParams.toArray());

		return page;
	}

	@Override
	// 在 已经审核 的栏目列出的是 我已经审核的表单。并且在显示的表格栏中需要添加 “状态” 信息栏。
	public Page findCreatorFlowByPage(PersonDetail actualPerson,
			PersonDetail[] agentees, PersonDetail[] assistedMgrs, int start,
			int size) {

		String tmpWhereStr = "";
		List<String> assistEmployeeIdPostCodes = new ArrayList<String>(8);

		for (PersonDetail assistPerson : assistedMgrs) {
			assistEmployeeIdPostCodes.add(assistPerson.getEmployeeId()
					+ assistPerson.getPostCode());
		}

		List<String> agentEmployeeIdPostCodes = new ArrayList<String>(8);
		// 判断是否由代理人经办过的表单
		for (PersonDetail agentPerson : agentees) {
			agentEmployeeIdPostCodes.add(agentPerson.getEmployeeId()
					+ agentPerson.getPostCode());
		}
		
		tmpWhereStr = " w.employeeid='" + actualPerson.getEmployeeId() + "' ";
		tmpWhereStr += " or (concat(w.employeeid,w.POSTCODE) in ( ";
		tmpWhereStr += "'" + actualPerson.getEmployeeId() + "'";
		for (String employeeIdPostCode : agentEmployeeIdPostCodes) {
			tmpWhereStr += " , ";
			tmpWhereStr += " '" + employeeIdPostCode + "' ";
		}
		tmpWhereStr += " ) and w.DLG_EMPLOYEEID = '" + actualPerson.getEmployeeId() + "') ";
		tmpWhereStr += " or concat(w.employeeid,w.POSTCODE) in ( ";
		tmpWhereStr += "'" + actualPerson.getEmployeeId() + "'";
		for (String employeeIdPostCode : assistEmployeeIdPostCodes) {
			tmpWhereStr += " , ";
			tmpWhereStr += " '" + employeeIdPostCode + "' ";
		}
		tmpWhereStr += " ) ";

		/**
		 * 优化查询SQL语句，主要是将原来的w.status=16的过滤方式，原来的是用not in ，这样会造成签核记录表查询了多次 且not
		 * in 查询效率低，现改为select * from (原SQL语句) where status!=16
		 * 就是先全部查出来，然后整体过滤status=16的记录，即撤消的申请单不显示
		 */
		StringBuffer sql = new StringBuffer();
		sql.append("select * from (");

		sql.append("select f.status as status, 1 as employeeId, 1 as postCode, 1 as workId, f.id as flowId, 1 as deptId, f.formnum as formnum, f.flowtype as flowType,f.isnew, f.createtime as createtime, content.title as title, creator.name as creatorName, actual.name as actualName "
				+ "  , f.endtime "
				//+ " ,(select max(finishtime) from ww_work w where w.flowid=f.id and (" + tmpWhereStr + ")) finishtime "
				+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
				+ "left join ww_persondetail actual on f.id = actual.flowid "
				+ "left join ww_flowcontent content on f.id = content.flowid "
				+ "where f.id in (select distinct(w.flowId) from ww_work w where (w.status = ? or w.status = ? or w.status = ? or w.status = ? or w.status = ? or w.status = ?) and "
				+ " (w.employeeid = ? "
				);

		sql.append(" or (concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		for (String employeeIdPostCode : agentEmployeeIdPostCodes) {
			sql.append(" , ");
			sql.append(" '" + employeeIdPostCode + "' ");
		}
		sql.append(") and w.DLG_EMPLOYEEID = ?) ");

		sql.append(" or concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		for (String employeeIdPostCode : assistEmployeeIdPostCodes) {
			sql.append(" , ");
			sql.append(" '" + employeeIdPostCode + "' ");
		}
		sql.append(") ");
		//sql.append(" (").append(tmpWhereStr);

		sql.append(" )) and (f.istemplate is null or f.istemplate = 0) and creator.type = 0 and actual.type = 1 ");

		sql.append(" order by f.createtime desc ) where status!=? ");
		
		//sql.append(") where status!=? order by finishtime desc ");

		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapperPlus();

		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, sql.toString(), mapper,
				FlowStatus.AGREE.ordinal(), FlowStatus.APPROVED.ordinal(),
				FlowStatus.CANCEL.ordinal(), FlowStatus.REJECT.ordinal(),
				//FlowStatus.DOING.ordinal(), FlowStatus.WAITING.ordinal(),
				FlowStatus.AGREE.ordinal(), FlowStatus.WAITING.ordinal(),
				actualPerson.getEmployeeId(), actualPerson.getEmployeeId(),
				FlowStatus.CANCEL.ordinal());
		return page;
	}

	@Override
	public Page findMyProcessFlowByPage(PersonDetail actualPerson, int start,
			int size) {
		String sql = "select f.status as status, 1 as employeeId, 1 as postCode, 1 as workId, f.id as flowId, 1 as deptId, f.formnum as formnum, f.flowtype as flowType,f.isnew, f.createtime as createtime, content.title as title, creator.name as creatorName, actual.name as actualName "
				+ " , f.endtime "
				+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
				+ "left join ww_persondetail actual on f.id = actual.flowid "
				+ "left join ww_flowcontent content on f.id = content.flowid "
				+ "where f.id in (select distinct(w.flowId) from ww_work w where w.status=? and w.employeeid = ?) and (f.istemplate is null or f.istemplate = 0) "
				+ "and creator.type = 0 and actual.type = 1 order by f.createtime DESC";
		// QianChenFlowRowMapper
		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapperPlus();
		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, sql, mapper, FlowStatus.INIT.ordinal(),
				actualPerson.getEmployeeId());
		return page;
	}

	@Override
	public int getCreatorFlowCount(PersonDetail actualPerson,
			PersonDetail[] agentees, PersonDetail[] assistedMgrs) {
		List<String> agentEmployeeIdPostCodes = new ArrayList<String>(8);

		StringBuffer sql = new StringBuffer(
				"select * from (select f.status as status, 1 as employeeId, 1 as postCode, 1 as workId, f.id as flowId, 1 as deptId, f.formnum as formnum, f.flowtype as flowType, f.createtime as createtime, content.title as title, creator.name as creatorName, actual.name as actualName "
						+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
						+ "left join ww_persondetail actual on f.id = actual.flowid "
						+ "left join ww_flowcontent content on f.id = content.flowid "
						+ "where f.id in (select distinct(w.flowId) from ww_work w where (w.status = ? or w.status = ? or w.status = ? or w.status = ? or w.status = ? or w.status = ? ) and w.finishtime is not null and (w.employeeid = ?");
		// 判断是否由代理人经办过的表单
		for (PersonDetail agentPerson : agentees) {
			agentEmployeeIdPostCodes.add(agentPerson.getEmployeeId()
					+ agentPerson.getPostCode());
		}

		// 助理
		List<String> assistEmployeeIdPostCodes = new ArrayList<String>(8);
		for (PersonDetail assistPerson : assistedMgrs) {
			assistEmployeeIdPostCodes.add(assistPerson.getEmployeeId()
					+ assistPerson.getPostCode());
		}

		sql.append(" or (concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		for (String employeeIdPostCode : agentEmployeeIdPostCodes) {
			sql.append(" , ");
			sql.append(" '" + employeeIdPostCode + "' ");
		}

		sql.append(") and w.DLG_EMPLOYEEID = ?) ");

		sql.append(" or concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		for (String employeeIdPostCode : assistEmployeeIdPostCodes) {
			sql.append(" , ");
			sql.append(" '" + employeeIdPostCode + "' ");
		}
		sql.append(") ");

		sql.append(" )) and (f.istemplate is null or f.istemplate = 0) and creator.type = 0 and actual.type = 1 order by f.createtime DESC");

		sql.append(") where status!=?");

		String countSQL = "select count(*) "
				+ sql.toString().substring(
						sql.toString().toLowerCase().indexOf(" from ") + 1);
		return getJdbcTemplate().queryForInt(
				countSQL,
				new Object[] { FlowStatus.AGREE.ordinal(),
						FlowStatus.APPROVED.ordinal(),
						FlowStatus.CANCEL.ordinal(),
						FlowStatus.REJECT.ordinal(),
						//FlowStatus.DOING.ordinal(),
						FlowStatus.APPROVED.ordinal(),
						FlowStatus.WAITING.ordinal(),
						actualPerson.getEmployeeId(),
						actualPerson.getEmployeeId(),
						FlowStatus.CANCEL.ordinal() });
	}

	@Override
	public int getFlowTemplateCount(PersonDetail actualPerson) {
		String sql = "select 1 as workId, f.id as flowId, f.formnum as formnum, f.flowtype as flowType, f.createtime as createtime, "
				+ "content.scheme as scheme, creator.name as creatorName, actual.name as actualName "
				+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
				+ "left join ww_persondetail actual on f.id = actual.flowid "
				+ "left join ww_flowcontent content on f.id = content.flowid "
				+ "where f.TEMPLATECREATEID=? and f.ISTEMPLATE=1 and creator.type = 0 and actual.type = 1 order by f.createtime";
		String countSQL = "select count(*) "
				+ sql.substring(sql.toLowerCase().indexOf(" from ") + 1);
		return getJdbcTemplate().queryForInt(countSQL,
				new Object[] { actualPerson.getEmployeeId() });
	}

	@Override
	public int getMyProcessFlowCount(PersonDetail actualPerson) {
		String sql = "select 1 as workId, f.id as flowId, f.formnum as formnum, f.flowtype as flowType, f.createtime as createtime, content.scheme as scheme, creator.name as creatorName, actual.name as actualName "
				+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
				+ "left join ww_persondetail actual on f.id = actual.flowid "
				+ "left join ww_flowcontent content on f.id = content.flowid "
				+ "where f.id in (select distinct(w.flowId) from ww_work w where w.status=? and w.employeeid = ?) "
				+ " and (f.istemplate is null or f.istemplate = 0) and creator.type = 0 and actual.type = 1 order by f.createtime";
		String countSQL = "select count(*) "
				+ sql.substring(sql.toLowerCase().indexOf(" from ") + 1);
		return getJdbcTemplate().queryForInt(
				countSQL,
				new Object[] { FlowStatus.INIT.ordinal(),
						actualPerson.getEmployeeId() });
	}

	protected class MyWorkHistoryRowMapper implements
			ParameterizedRowMapper<MyWorkHistory> {
		@SuppressWarnings("finally")
		public MyWorkHistory mapRow(ResultSet rs, int rowNum) {
			MyWorkHistory flow = new MyWorkHistory();
			// DLG_EMPLOYEEID VARCHAR2(255),
			// DLG_POSTCODE VARCHAR2(255),
			// DLG_DEPTCODE VARCHAR2(255),
			// DLG_A_DEPTCODE VARCHAR2(255),
			// DLG_CMPCODE VARCHAR2(255)
			// DEPTCODE VARCHAR2(255), CMPCODE VARCHAR2(255),
			// 整合代理和助理的现实风格,判断work的dlg字段与实际的字段是否一致，如果不一致就是委托了，需要重新风格上调整
			// 风格改为
			// 审核人(ProcessManName)：张三(员工号)职位(代)
			// 状态(Status)：(代) 审核
			// 意见(Opinion)：(代)李四(员工号)职位: 正文
			try {
				flow.setWorkId(rs.getLong("workId"));
				flow.setDeptName(rs.getString("deptName"));
				String actProcessManName = rs.getString("processManName");
				flow.setProcessManName(actProcessManName);
				Timestamp ts = rs.getTimestamp("processTime");
				if (ts != null) {
					flow.setCalculatedProcessTime(new Date(rs.getTimestamp(
							"processTime").getTime()));
				}
				String tmpAgree = rs.getString("hbAgree");
				if (tmpAgree.equals("2")) {
					flow.setWorkStatus("不同意");
				} else {
					flow.setWorkStatus(FlowStatus.values()[rs
							.getInt("workStatus")].getDisplayName());
				}
				flow.setStatus(FlowStatus.values()[rs.getInt("workStatus")]);
				flow.setOpinion(rs.getString("opinion"));
				flow.setWorkNum(rs.getInt("worknum"));
				flow.setStage(rs.getInt("stage"));

				// String employeeId = rs.getString("employeeId");
				String postCode = rs.getString("POSTCODE");
				String DLG_EMPLOYEEID = rs.getString("DLG_EMPLOYEEID");
				String DLG_POSTCODE = rs.getString("DLG_POSTCODE");
				String DLG_DEPTCODE = rs.getString("DLG_DEPTCODE");
				String DLG_A_DEPTCODE = rs.getString("DLG_A_DEPTCODE");
				String DLG_CMPCODE = rs.getString("DLG_CMPCODE");

				flow.setDeptCode(rs.getString("DEPTCODE"));
				flow.setDeptId(rs.getString("DEPTID"));
				flow.setCmpCode(rs.getString("CMPCODE"));
				flow.setPostCode(postCode);
				flow.setDlgEmployeeId(DLG_EMPLOYEEID);
				flow.setDlgPostCode(DLG_POSTCODE);
				flow.setDlgDeptCode(DLG_DEPTCODE);
				flow.setDlgDeptId(rs.getString("DLG_DEPTID"));
				flow.setDlgADeptCode(DLG_A_DEPTCODE);
				flow.setDlgCmpCode(DLG_CMPCODE);
				flow.setOrgpath(rs.getString("orgpath"));
				flow.setHbAgree(rs.getString("hbAgree"));
				flow.setOldFlowId(rs.getLong("oldFlowId"));
				
				flow.setEmployeeNam(rs.getString("employeenam"));
				flow.setTitnam(rs.getString("titnam"));
				flow.setDlgEmployeeNam(rs.getString("dlg_employeenam"));
				flow.setDlgTitnam(rs.getString("dlg_titnam"));
				
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				return flow;
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public MyWorkHistory[] listWorkHistory(String formNum) {
		// 额外添加待办工作项的并且放到首上条目上面
		// 将原来的现实顺序按照倒叙方式排序
		// 整合代理和助理的现实风格,判断work的dlg字段与实际的字段是否一致，如果不一致就是委托了，需要重新风格上调整

		List result = new ArrayList();
		// w.finishtime is not null 表示已经被处理过的了
		// 首层无会办的情况 w.joinsignstartid == null DEPTCODE VARCHAR2(255), CMPCODE
		// VARCHAR2(255),
		String sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE,"
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID, "
				+ " w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.finishtime is not null and w.joinsignstartid is null and w.WORKSTAGE != "
				+ WorkStage.BOSS_SIGN.ordinal()
				+ " and w.WORKSTAGE != "
				+ WorkStage.DIVISION_SIGN.ordinal()
				+ " and w.WORKSTAGE != "
				+ WorkStage.FBOSS_SIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formnum", formNum);
		ParameterizedRowMapper<MyWorkHistory> mapper = new MyWorkHistoryRowMapper();
		List userTemplates = getJdbcTemplate().query(sql, mapper, map);
		result.addAll(userTemplates);

		/**
		 * 新增事业部主管核准指派
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.DIVISION_SIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		Iterator divisionSignJointSignWorkIterator = getJdbcTemplate()
				.query(sql, mapper, map).iterator();
		Map<Long, Map<Integer, MyWorkHistory>> divisionJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (divisionSignJointSignWorkIterator.hasNext()) {
			MyWorkHistory divisionWorkHistory = (MyWorkHistory) divisionSignJointSignWorkIterator
					.next();
			// 因为会办所以都会有子元素
			divisionWorkHistory.setHaveChildren(true);

			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示签核历程按钮
			if (divisionWorkHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> divisionOldHisMap = (Map<Integer, MyWorkHistory>) divisionJoinSignWorkHistoryMap
						.get(divisionWorkHistory.getOldFlowId());
				if (null == divisionOldHisMap) {
					divisionOldHisMap = new HashMap<Integer, MyWorkHistory>();
					divisionJoinSignWorkHistoryMap.put(
							divisionWorkHistory.getOldFlowId(),
							divisionOldHisMap);
				}
				MyWorkHistory divisonOldHis = divisionOldHisMap
						.get(divisionWorkHistory.getWorkNum());
				if (divisonOldHis != null) {
					if (divisonOldHis.getStatus().ordinal() < divisionWorkHistory
							.getStatus().ordinal()) {
						long divisionWorkId = divisonOldHis.getWorkId();
						divisionWorkHistory.setWorkId(divisionWorkId);
						divisionOldHisMap.put(divisionWorkHistory.getWorkNum(),
								divisionWorkHistory);
					}
				} else {
					divisionOldHisMap.put(divisionWorkHistory.getWorkNum(),
							divisionWorkHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> divisionMapIter = divisionJoinSignWorkHistoryMap
				.values().iterator();
		while (divisionMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> divisionCurMap = divisionMapIter.next();
			result.addAll(divisionCurMap.values());
		}

		/**
		 * 新增最高副主管核准
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE,"
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID, "
				+ " w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.finishtime is not null "
				// + " and w.joinsignstartid is null "
				+ " and w.WORKSTAGE = "
				+ WorkStage.FBOSS_SIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		Map<String, Object> fbossMap = new HashMap<String, Object>();
		fbossMap.put("formnum", formNum);
		ParameterizedRowMapper<MyWorkHistory> fbossMapper = new MyWorkHistoryRowMapper();
		List fbossTemplates = getJdbcTemplate().query(sql, fbossMapper,
				fbossMap);
		result.addAll(fbossTemplates);

		/*
		 * 新增最终核准指派
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum "
				//+ " and w.joinStartEmployeeId = w.employeeid "
				+ " and w.WORKSTAGE = "
				+ WorkStage.BOSS_SIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		Iterator bossSignJointSignWorkIterator = getJdbcTemplate().query(
				sql, mapper, map).iterator();
		Map<Long, Map<Integer, MyWorkHistory>> bossJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (bossSignJointSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) bossSignJointSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) bossJoinSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					bossJoinSignWorkHistoryMap.put(workHistory.getOldFlowId(),
							oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> bossMapIter = bossJoinSignWorkHistoryMap
				.values().iterator();
		while (bossMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = bossMapIter.next();
			result.addAll(curMap.values());
		}

		/*
		 * 新增同CMPCODE会办
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.HB_joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.CMPCODEJOINTSIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";

		Iterator cmpcodeJointSignWorkIterator = getJdbcTemplate().query(
				sql, mapper, map).iterator();
		Map<Long, Map<Integer, MyWorkHistory>> cmpcodeJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (cmpcodeJointSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) cmpcodeJointSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) cmpcodeJoinSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					cmpcodeJoinSignWorkHistoryMap.put(
							workHistory.getOldFlowId(), oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> cmpcodeMapIter = cmpcodeJoinSignWorkHistoryMap
				.values().iterator();
		while (cmpcodeMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = cmpcodeMapIter.next();
			result.addAll(curMap.values());
		}

		// 然后获取 会办的workStage.JoinSign, 以及jointSignStartId 与employeeId一致的记录
		// 以woknum加以区分分支情况,最终处理出来返回给首层的会办进程信息
		// and w.finishtime is not null
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.HB_joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.JOINTSIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		// and w.joinStartEmployeeId = w.employeeid
		// and w.joinsignstartid like '%\"'||w.joinStartEmployeeId||'\"%'
		Iterator jointSignWorkIterator = getJdbcTemplate().query(sql,
				mapper, map).iterator();

		Map<Long, Map<Integer, MyWorkHistory>> joinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (jointSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) jointSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				// 取得最新状态即可
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) joinSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					joinSignWorkHistoryMap.put(workHistory.getOldFlowId(),
							oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> mapIter = joinSignWorkHistoryMap
				.values().iterator();
		while (mapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = mapIter.next();
			result.addAll(curMap.values());
		}
		
		// 新增行政职能会办分支
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.HB_joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.XZJOINTSIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		//logger.info(formNum + " 加载签核记录中的其它会办及指派记录 Start...");
		Iterator jointSignWorkIterator2 = getJdbcTemplate().query(sql,
				mapper, map).iterator();
		//logger.info(formNum + " 加载签核记录中的其它会办及指派记录 End...");
		Map<Long, Map<Integer, MyWorkHistory>> joinSignworkHistory2Map2 = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (jointSignWorkIterator2.hasNext()) {
			MyWorkHistory workHistory2 = (MyWorkHistory) jointSignWorkIterator2
					.next();
			// 因为是会办所以都回有子元素
			workHistory2.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory2.getStatus() != FlowStatus.DOING) {
				// 取得最新状态即可
				Map<Integer, MyWorkHistory> oldHistory2Map2 = (Map<Integer, MyWorkHistory>) joinSignworkHistory2Map2
						.get(workHistory2.getOldFlowId());
				if (null == oldHistory2Map2) {
					oldHistory2Map2 = new HashMap<Integer, MyWorkHistory>();
					joinSignworkHistory2Map2.put(workHistory2.getOldFlowId(),
							oldHistory2Map2);
				}
				MyWorkHistory oldHistory2 = oldHistory2Map2.get(workHistory2
						.getWorkNum());
				if (oldHistory2 != null) {
					if (oldHistory2.getStatus().ordinal() < workHistory2
							.getStatus().ordinal()) {
						long workId = oldHistory2.getWorkId();
						workHistory2.setWorkId(workId);
						oldHistory2Map2
								.put(workHistory2.getWorkNum(), workHistory2);
					}
				} else {
					oldHistory2Map2.put(workHistory2.getWorkNum(), workHistory2);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> mapIter2 = joinSignworkHistory2Map2
				.values().iterator();
		while (mapIter2.hasNext()) {
			Map<Integer, MyWorkHistory> curMap2 = mapIter.next();
			result.addAll(curMap2.values());
		}

		Collections.sort(result, new Comparator() {
			@Override
			public int compare(Object entryA, Object entryB) {
				MyWorkHistory workHistoryA = (MyWorkHistory) entryA;
				MyWorkHistory workHistoryB = (MyWorkHistory) entryB;
				Date dateA = workHistoryA.getCalculateProcessTime();// .getProcessTime()
				Date dateB = workHistoryB.getCalculateProcessTime();
				int comparedResult = dateA.compareTo(dateB);
				if (comparedResult == 0) {
					comparedResult = Integer
							.valueOf(workHistoryA.getStage())
							.compareTo(Integer.valueOf(workHistoryB.getStage()));
				}
				// 已有历程倒叙显示
				return -comparedResult;
			}
		});

		// 在首上加上该流程的代办工作项
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree   "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.status = :status"
				+ " order by w.workstage";
		map.put("status", FlowStatus.DOING.ordinal());
		List todoWorks = getJdbcTemplate().query(sql, mapper, map);
		List overall = new ArrayList();
		overall.addAll(todoWorks);
		overall.addAll(result);
		return (MyWorkHistory[]) overall.toArray(new MyWorkHistory[overall
				.size()]);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public MyWorkHistory[] listWorkHistory(String formNum, long parentId) {
		String sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID, "
				+ "w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.finishtime is not null and w.parentid = :parentid"
				+ " order by w.workstage, w.finishtime";

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formnum", formNum);
		map.put("parentid", parentId);
		ParameterizedRowMapper<MyWorkHistory> mapper = new MyWorkHistoryRowMapper();
		List userTemplates = getJdbcTemplate().query(sql, mapper, map);
		return (MyWorkHistory[]) userTemplates
				.toArray(new MyWorkHistory[userTemplates.size()]);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Flow[] loadFlowTemplate(String userName, FlowType flowType) {
		String sql = "select * from WW_FLOW flow where flow.TEMPLATECREATEID=:templateId and flow.ISTEMPLATE=1 and flow.FlowType=:flowtype";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("templateId", userName);
		map.put("flowtype", flowType.ordinal());
		ParameterizedRowMapper<Flow> mapper = new QianChenFlowRowMapper();
		List userTemplates = getJdbcTemplate().query(sql, mapper, map);
		return (Flow[]) userTemplates.toArray(new Flow[userTemplates.size()]);
	}

	@Override
	public void updateWork(final MyWork mywork) {
		// 这边的mywork基本都是mydelegateWork不需要判断了
		final MyDelegateWork work = (MyDelegateWork) mywork;
		work.setFinishTime(System.currentTimeMillis());
		// FLOWTYPE, ID, FORMNUM, DEPTNAME, DEPTID, CREATETIME, FINISHTIME,
		// OPINION, STATUS, ASSIGNORID, WORKNUM
		// DLG_EMPLOYEEID,DLG_POSTCODE,DLG_DEPTCODE,DLG_A_DEPTCODE,DLG_CMPCODE
		String updateSQL = "update WW_WORK set OPINION=:opinion,STATUS=:status,FINISHTIME=:finishTime,"
				+ "DLG_EMPLOYEEID=:DLG_EMPLOYEEID,DLG_POSTCODE=:DLG_POSTCODE,DLG_DEPTCODE=:DLG_DEPTCODE,DLG_A_DEPTCODE=:DLG_A_DEPTCODE,DLG_CMPCODE=:DLG_CMPCODE,DLG_DEPTID=:DLG_DEPTID"
				+ ",JOINSIGNSTARTID=:JOINSIGNSTARTID,joinStartEmployeeId =:joinStartEmployeeId, JOINCYCLE=:JOINCYCLE, WORKNUM=:WORKNUM,PARENTID=:PARENTID"
				+ ",HB_CHENGHE=:HB_CHENGHE,HB_JOINSIGNSTARTID=:HB_JOINSIGNSTARTID,HB_JOINSTARTEMPLOYEEID=:HB_JOINSTARTEMPLOYEEID,HB_AGREE=:HB_AGREE "
				+ ",DLG_EMPLOYEENAM=:DLG_EMPLOYEENAM,DLG_TITNAM=:DLG_TITNAM,SERVERIP=:SERVERIP,CLIENTIP=:CLIENTIP"
				+ " where FLOWID=:FLOWID and ID=:workID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, work.getOpinion());
				state.setObject(2, work.getStatus().ordinal());
				state.setObject(3, new Timestamp(work.getFinishTime()));

				state.setObject(4, work.getDlgEmployeeId());
				state.setObject(5, work.getDlgPostCode());
				state.setObject(6, work.getDlgDeptCode());
				state.setObject(7, work.getDlgADeptCode());
				state.setObject(8, work.getDlgCmpCode());
				state.setObject(9, work.getDlgDeptId());

				state.setObject(10, work.getJoinSignStartId());
				state.setObject(11, work.getJoinStartEmployeeId());
				state.setObject(12, work.getJoinCycle());

				state.setObject(13, work.getWorknum());
				state.setObject(14, work.getParentId());
				state.setObject(15, work.getHb_ChengHe());
				if (org.apache.commons.lang3.StringUtils.isNotEmpty(work
						.getHb_JoinStartEmployeeId())) {
					state.setObject(16, work.getHb_JoinSignStartId());
					state.setObject(17, work.getHb_JoinStartEmployeeId());
				} else {
					state.setObject(16, work.getJoinSignStartId());
					state.setObject(17, work.getJoinStartEmployeeId());
				}
				state.setObject(18, work.getHb_Agree());
				state.setObject(19, work.getDlgEmployeenam());
				state.setObject(20, work.getDlgTitnam());
				state.setObject(21, CommonUtil.getServerIP());
				state.setObject(22, work.getClientIP());
				state.setObject(23, work.getFlowId());
				state.setObject(24, work.getId());
			}
		};
		getJdbcTemplate().update(updateSQL,param);
	}

	public void updateAdminAssignWork(final MyWork mywork) {
		final MyDelegateWork work = (MyDelegateWork) mywork;
		String updateSQL = "update WW_WORK set DEPTID=:deptId,DEPTNAME=:deptName,EMPLOYEEID=:employeeId,"
				+ "POSTCODE=:postCode,DEPTCODE=:deptCode,A_DEPTCODE=:A_deptCode,CMPCODE=:cmpCode,"
				+ "JOINSIGNSTARTID=:JOINSIGNSTARTID,joinStartEmployeeId =:joinStartEmployeeId "
				+ ",EMPLOYEENAM=:employeeNam,TITNAM=:titnam where ID=:workID ";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, work.getDeptId());
				state.setObject(2, work.getDeptName());
				state.setObject(3, work.getEmployeeId());

				state.setObject(4, work.getPostCode());
				state.setObject(5, work.getDeptCode());
				state.setObject(6, work.getA_deptCode());
				state.setObject(7, work.getCmpCode());
				state.setObject(8, work.getJoinSignStartId());
				state.setObject(9, work.getJoinStartEmployeeId());
				state.setObject(10, work.getEmployeenam());
				state.setObject(11, work.getTitlenam());
				state.setObject(12, work.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public int getWorkCount(Flow qianChenFlow, WorkStage innerjointsign,
			FlowStatus agree, FlowType flowType) {
		String updateSQL = "select count(id) from WW_WORK where OLDFLOWID=? and STATUS=? and WORKSTAGE=? and FLOWTYPE=?";
		Object[] param = new Object[] { qianChenFlow.getId(), agree.ordinal(),
				innerjointsign.ordinal(), flowType.ordinal() };
		return getJdbcTemplate().queryForInt(
				updateSQL, param);
	}

	@Override
	public void updateFlow(final Flow flow) {
		String updateSQL = "update WW_FLOW f set f.STATUS=:status, f.jointsigndeptids=:jointIds, f.jointsigndeptname=:jointName, "
				+ "f.copydeptids=:copyIds, f.copydeptname =:copyName, f.RENEWTIMES =:RENEWTIMES, "
				+ "f.CREATETIME=:createTime, f.FORMNUM=:FORMNUM, f.DECIONMAKER=:DECIONMAKER, "
				+ "f.JOINTSIGNTYPE=:JOINTSIGNTYPE, f.COPYDEMO=:COPYDEMO, f.INNERJOINTSIGNIDS=:INNERJOINTSIGNIDS, "
				+ "f.INNERJOINTSIGNNAME=:INNERJOINTSIGNNAME, f.FLOWTYPE=:FLOWTYPE, f.SELFCONFIRM=:SELFCONFIRM,"
				+ "f.ISLOCAL=:ISLOCAL,f.SUBMITBOSS=:SUBMITBOSS,f.SUBMITFBOSS=:SUBMITFBOSS,f.CHAIRMAN=:CHAIRMAN "
				+ " where ID=:FLOWID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, flow.getStatus().ordinal());
				if (flow.getJointSignDeptIds() != null
						&& flow.getJointSignDeptIds().length > 0) {
					state.setObject(
							2,
							StringUtils.arrayToDelimitedString(
									flow.getJointSignDeptIds(), ";"));
				} else {
					state.setObject(2, null);
				}
				state.setObject(3, flow.getJointSignDeptName());
				if (flow.getCopyDeptIds() != null
						&& flow.getCopyDeptIds().length > 0) {
					state.setObject(
							4,
							StringUtils.arrayToDelimitedString(
									flow.getCopyDeptIds(), ";"));
				} else {
					state.setObject(4, null);
				}
				state.setObject(5, flow.getCopyDeptName());
				state.setObject(6, flow.getRenewTimes());

				state.setObject(7, new Timestamp(flow.getCreateTime()));
				state.setObject(8, flow.getFormNum());
				state.setObject(9, flow.getDecionmaker().ordinal());
				state.setObject(10, flow.getJointSignType().ordinal());
				state.setObject(11, flow.getCopyDemo());
				if (flow.getInnerJointSignIds() != null
						&& flow.getInnerJointSignIds().length > 0) {
					state.setObject(
							12,
							StringUtils.arrayToDelimitedString(
									flow.getInnerJointSignIds(), ";"));
				} else {
					state.setObject(12, null);
				}
				state.setObject(13, flow.getInnerJointSignName());
				state.setObject(14, flow.getFlowType().ordinal());
				state.setObject(15, flow.isSelfConfirm());
				state.setObject(16, flow.isLocal());
				// 2014-03-11 增加暂存后再次暂存是更新呈核总裁字段的值 by Cao_Shengyong
				state.setObject(17, flow.isSubmitBoss());
				state.setObject(18, flow.isSubmitFBoss());
				state.setObject(19, flow.isChariman());
				state.setObject(20, flow.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		JdbcTemplate template = super.createJdbcTemplate(dataSource);
		this.flow_incr = new OracleSequenceMaxValueIncrementer(dataSource,
				"SEQ_WW_FLOW");
		new OracleSequenceMaxValueIncrementer(dataSource,
				"SEQ_WW_FLOW_FORM");
		this.flow_jdbcInsert = new SimpleJdbcInsert(dataSource)
				.withTableName("WW_FLOW");

		this.persondetail_incr = new OracleSequenceMaxValueIncrementer(
				dataSource, "SEQ_WW_PERSONDETAIL");
		this.persondetail_jdbcInsert = new SimpleJdbcInsert(dataSource)
				.withTableName("WW_PERSONDETAIL");

		this.flowattachment_incr = new OracleSequenceMaxValueIncrementer(
				dataSource, "SEQ_WW_FLOWATTACHMENT");
		// this.QIANCHENFLOWATTACHMENT_jdbcInsert = new SimpleJdbcInsert(
		// dataSource).withTableName("WW_QIANCHENFLOWATTACHMENT");

		this.flowcontent_incr = new OracleSequenceMaxValueIncrementer(
				dataSource, "SEQ_WW_FLOWCONTENT");
		this.flowcontent_jdbcInsert = new SimpleJdbcInsert(dataSource)
				.withTableName("WW_FLOWCONTENT");

		this.flowwork_incr = new OracleSequenceMaxValueIncrementer(dataSource,
				"SEQ_WW_FLOWWORK");
		this.flowwork_jdbcInsert = new SimpleJdbcInsert(dataSource)
				.withTableName("WW_WORK");

		this.deptcount_incr = new OracleSequenceMaxValueIncrementer(dataSource,
				"SEQ_WW_DEPTCOUNT");
		
		this.log_jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("WW_FLOW_LOG");
		
		return template;
	}

	@Override
	public void clearFlowTemplate(final Flow flow) {
		String updateSQL = "update ww_flow set templatecreateid=:createId,istemplate=0 where ID=:FLOWID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, "");
				state.setObject(2, flow.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public void saveFlowShengheProperties(final long id,
			final String employeeId, final String deptId,
			final String postCode, final int finishedShengheStep) {
		String updateSQL = "update ww_flow f set f.shenghestep=:step,f.shengheemployeeid=:employId,f.SHENGHEDEPTID=:deptId,f.SHENGHEPOSTCODE=:postCode where ID=:FLOWID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, finishedShengheStep);
				state.setObject(2, employeeId);
				state.setObject(3, deptId);
				state.setObject(4, postCode);
				state.setObject(5, id);
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public void saveFlowSecondProperties(final long id,
			final String employeeId, final String deptId, final String postCode) {
		String updateSQL = "update ww_flow f set f.secondemployeeid=:employeeId,f.seconddeptid=:deptId,f.secondpostcode=:postCode where ID=:FLOWID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, employeeId);
				state.setObject(2, deptId);
				state.setObject(3, postCode);
				state.setObject(4, id);
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public boolean hasJointSignFinished(final Flow flow) {
		// 判断 该流程下 会办分支 的所有work employeeId 等于 startEmployeeId的那个work列表，状态都是
		String searchSQL = "select w.joinsignstartid,count(w.id) as count_work from ww_work w where w.flowid = :id and w.oldflowid = :oldflowid and w.joinsignstartid is not null and w.status in ("
				+ FlowStatus.AGREE.ordinal()
				+ ","
				+ FlowStatus.WAITING.ordinal()
				+ ","
				+ FlowStatus.APPROVED.ordinal()
				+ ") group by w.joinsignstartid ";

		String allSQL = "select w.joinsignstartid,count(w.id) as count_work from ww_work w where w.flowid = :id and w.oldflowid = :oldflowid and w.joinsignstartid is not null group by w.joinsignstartid ";
		List<Map<String, Object>> searchList = getJdbcTemplate()
				.queryForList(searchSQL, flow.getId(), flow.getId());
		List<Map<String, Object>> allList = getJdbcTemplate()
				.queryForList(allSQL, flow.getId(), flow.getId());

		int same = 0;
		for (Map<String, Object> searchItem : searchList) {
			String joinsignstartid = searchItem.get("joinsignstartid")
					.toString();
			int searchCount = Integer.parseInt(searchItem.get("count_work")
					.toString());

			for (Map<String, Object> allItem : allList) {
				String allJoinsignstartid = allItem.get("joinsignstartid")
						.toString();
				int allCount = Integer.parseInt(allItem.get("count_work")
						.toString());

				if (joinsignstartid.equals(allJoinsignstartid)) {
					if (searchCount == allCount) {
						same++;
					}
					break;
				}
			}
		}

		return same == allList.size();

	}

	public boolean hasCmpcodeJointSignFinished(Flow flow, String cmpcode) {
		if (flow.getRenewTimes() > 0) {
			String updateSQL = "select count(*) from ww_work w where w.joinstartemployeeid is not null and w.flowid = :id and w.cmpcode = :cmpcode and w.status in ("
					+ FlowStatus.AGREE.ordinal()
					+ ","
					+ FlowStatus.WAITING.ordinal()
					+ ") and w.createtime>(select createtime from ww_work where flowid= :id and status= :status)";
			int finished = getJdbcTemplate().queryForInt(updateSQL,
					flow.getId(), cmpcode, flow.getId(),
					FlowStatus.RECREATE.ordinal());

			String updateSQL2 = "select count(*) from ww_work w where w.joinstartemployeeid is not null and w.flowid = :id and w.cmpcode = :cmpcode "
					+ " and w.createtime>(select createtime from ww_work where flowid= :id and status= :status)";
			int overall = getJdbcTemplate().queryForInt(updateSQL2,
					flow.getId(), cmpcode, flow.getId(),
					FlowStatus.RECREATE.ordinal());
			return finished == overall;
		} else {
			String updateSQL = "select count(*) from ww_work w where w.joinstartemployeeid is not null and w.flowid = :id and w.cmpcode = :cmpcode and w.status in ("
					+ FlowStatus.AGREE.ordinal()
					+ ","
					+ FlowStatus.WAITING.ordinal() + ")";
			int finished = getJdbcTemplate().queryForInt(updateSQL,
					flow.getId(), cmpcode);

			String updateSQL2 = "select count(*) from ww_work w where w.joinstartemployeeid is not null and w.flowid = :id and w.cmpcode = :cmpcode";
			int overall = getJdbcTemplate().queryForInt(updateSQL2,
					flow.getId(), cmpcode);

			return finished == overall;
		}
	}

	@Override
	public void linkFlowAttachment(final Flow flow,
			FlowAttachment[] flowAttachments) {
		StringBuffer ids = new StringBuffer("(");
		int i = 0;
		for (FlowAttachment attachment : flowAttachments) {
			attachment.setFlowId(flow.getId());
			ids.append(attachment.getId());
			if (i != flowAttachments.length - 1) {
				ids.append(",");
			}
			i++;
		}
		ids.append(")");
		String updateSQL = "update ww_flowattachment set flowid = :flowId where id in "
				+ ids.toString();
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, flow.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public void updateContent(final FlowContent content) {
		String updateSQL = "update ww_flowcontent f set f.DEPTNAME=:DEPTNAME,f.DEPTID=:DEPTID,f.SECRETLEVEL=:SECRETLEVEL,f.EXPIRELEVEL=:EXPIRELEVEL"
				+ ",f.CASH=:CASH,f.TYPE=:TYPE,f.TITLE=:TITLE,f.DETAIL=:DETAIL,f.SCHEME=:SCHEME, f.FLOWID=:FLOWID where ID=:ID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, content.getDeptName());
				state.setObject(2, content.getDeptId());
				state.setObject(3, content.getSecretLevel());
				state.setObject(4, content.getExireLevel());
				state.setObject(5, content.getCash());
				state.setObject(6, content.getType());
				state.setObject(7, content.getTitle());
				state.setObject(8, content.getDetail());
				state.setObject(9, content.getScheme());
				state.setObject(10, content.getFlowId());
				state.setObject(11, content.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public void updatePerson(final PersonDetail person,
			final PersonTYPE personTYPE) {
		String updateSQL = "update ww_persondetail f set f.FLOWID=:FLOWID,f.NAME=:NAME,f.EMPNUMBER=:EMPNUMBER"
				+ ",f.COMPPHONE=:COMPPHONE,f.DEPTNAME=:DEPTNAME,f.DEPTID=:DEPTID,f.TYPE=:TYPE"
				+ ",f.DEPTPATH=:DEPTPATH,f.POSTID=:POSTID,f.POSTNAME=:POSTNAME,f.POSTCODE=:POSTCODE"
				+ ",f.MGREMPLOYEEID=:MGREMPLOYEEID,f.MGRPOSTCODE=:MGRPOSTCODE where ID=:ID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, person.getFlowId());
				state.setObject(2, person.getName());
				state.setObject(3, person.getEmployeeId());
				state.setObject(4, person.getCompPhone());
				state.setObject(5, person.getDeptName());
				state.setObject(6, person.getDeptId());
				state.setObject(7, personTYPE.ordinal());
				state.setObject(8, person.getDeptPath());
				state.setObject(9, person.getPostId());
				state.setObject(10, person.getPostName());
				state.setObject(11, person.getPostCode());
				state.setObject(12, person.getMgrEmployeeid());
				state.setObject(13, person.getMgrPostcode());
				state.setObject(14, person.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public FlowAttachment loadAttachment(long attachmentId) {
		String sql = "select * from WW_FLOWATTACHMENT fa where fa.id=:id";
		ParameterizedRowMapper<FlowAttachment> mapper = new QianChenFlowAttachmentRowMapper();
		FlowAttachment item = getJdbcTemplate().queryForObject(sql,
				mapper, attachmentId);
		try {
			byte[] data = CommonUtil.inputStreamToByte(item.getAttachment());
			item.setData(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return item;
	}

	@Override
	public PersonDetail loadPerson(long personId) {
		String sql = "select * from WW_PERSONDETAIL person where person.id=:id";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", personId);
		ParameterizedRowMapper<PersonDetail> mapper = new QianChenPersonDetailRowMapper();
		return getJdbcTemplate().queryForObject(sql, mapper, map);
	}

	@Override
	public void deleteFlowAttachment(final long attahmentId) {
		String updateSQL = "delete from WW_FLOWATTACHMENT agt where agt.id = :id";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, attahmentId);
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public MyWork[] listWorks(Flow flow) {
		String sql = "select FLOWTYPE, WORKSTAGE, ID, FLOWID, DEPTNAME, DEPTID, CREATETIME, FINISHTIME, OPINION, STATUS, JOINSIGNSTARTID, JOINCYCLE, joinStartEmployeeId, WORKNUM, EMPLOYEEID, "
				+ "POSTCODE, DEPTCODE, A_DEPTCODE, CMPCODE, PARENTID,OLDFLOWID,ORGPATH,HB_CHENGHE,HB_JOINSIGNSTARTID,HB_JOINSTARTEMPLOYEEID,HB_CHENGHEEND,HB_AGREE from WW_WORK flow where flow.flowid =:id";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", flow.getId());
		ParameterizedRowMapper<MyWork> mapper = new WorkRowMapper();

		List userTemplates = getJdbcTemplate().query(sql, mapper, map);
		return (MyWork[]) userTemplates
				.toArray(new MyWork[userTemplates.size()]);
	}

	@Override
	public void clearFlow(final Flow flow) {
		String updateSQL = "delete from ww_work w where w.flowId = :id and w.status = "
				+ FlowStatus.DOING.ordinal();
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, flow.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public void delTempForm(List<String> formnumbers) {
		for (String formnum : formnumbers) {
			delTempForm(formnum);
		}
	}

	private void delTempForm(final String formnum) {
		String updateSQL = "delete from ww_flow w where w.formnum = :formnum";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, formnum);
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@Override
	public boolean checkPersonHasFinishWork(PersonDetail person, long flowId,
			WorkStage workStage) {
		String querySQL = "select count(1) from ww_work w where w.employeeId = :employeeId and w.flowId = :flowId and w.workStage = :workStage ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("employeeId", person.getEmployeeId());
		map.put("flowId", flowId);
		map.put("workStage", workStage.ordinal());
		int count = getJdbcTemplate().queryForInt(querySQL, map);
		return count > 0;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public MyWork getBossSignFirstWork(long flowId, WorkStage workStage,
			String joinSignStartId) {
		String sql = "select FLOWTYPE, WORKSTAGE, ID, FLOWID, DEPTNAME, DEPTID, CREATETIME, FINISHTIME, OPINION, STATUS, JOINSIGNSTARTID, JOINCYCLE, joinStartEmployeeId, WORKNUM, EMPLOYEEID, "
				+ "POSTCODE, DEPTCODE, A_DEPTCODE, CMPCODE, PARENTID,ORGPATH,HB_CHENGHE,HB_JOINSIGNSTARTID,HB_JOINSTARTEMPLOYEEID,HB_CHENGHEEND,HB_AGREE from WW_WORK flow where flow.flowid =:flowid and flow.WORKSTAGE=:workStage order by createtime ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flowid", flowId);
		map.put("workStage", workStage.ordinal());
		ParameterizedRowMapper<MyWork> mapper = new WorkRowMapper();

		List userTemplates = getJdbcTemplate().query(sql, mapper, map);
		if (null != userTemplates && userTemplates.size() > 0) {
			return (MyWork) userTemplates.get(0);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public MyWork getPrevWork(MyWork myWork) {
		StringBuffer sql = new StringBuffer();
		sql.append("select FLOWTYPE, WORKSTAGE, ID, FLOWID, DEPTNAME, DEPTID, CREATETIME, FINISHTIME, OPINION, STATUS, JOINSIGNSTARTID, JOINCYCLE, joinStartEmployeeId, WORKNUM, EMPLOYEEID, ");
		sql.append("POSTCODE, DEPTCODE, A_DEPTCODE, CMPCODE, PARENTID,OLDFLOWID,ORGPATH,HB_CHENGHE,HB_JOINSIGNSTARTID,HB_JOINSTARTEMPLOYEEID,HB_CHENGHEEND,HB_AGREE from WW_WORK flow ");
		sql.append("where flow.flowid =:flowid ");
		sql.append("and joinstartemployeeid = :joinstartemployeeid ");
		sql.append("and id <> :id ");
		sql.append("and flow.OLDFLOWID = :oldFlowId ");
		sql.append("and flow.finishtime is not null ");
		sql.append("and parentid in (select distinct parentid from ww_work f where f.id = :id) ");
		sql.append("and flow.WORKSTAGE=:workStage and flow.createtime < :createTime order by createtime desc ");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flowid", myWork.getFlowId());
		map.put("joinstartemployeeid", myWork.getJoinStartEmployeeId());
		map.put("id", myWork.getId());
		map.put("oldFlowId", myWork.getOldFlowId());
		map.put("workStage", myWork.getWorkStage().ordinal());
		map.put("createTime", new Timestamp(myWork.getCreateTime()));
		ParameterizedRowMapper<MyWork> mapper = new WorkRowMapper();

		List userTemplates = getJdbcTemplate().query(sql.toString(),
				mapper, map);
		if (null != userTemplates && userTemplates.size() > 0) {
			return (MyWork) userTemplates.get(0);
		}
		return null;
	}

	@Override
	public void saveWorkFinishTime(final MyWork work) {
		String updateSQL = "update WW_WORK set FINISHTIME=:finishTime where ID=:workID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, new Timestamp(work.getFinishTime()));
				state.setObject(2, work.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);

	}

	public void updateHbWorkJoin(final MyWork myWork) {
		String updateSQL = "update ww_work set hb_joinsignstartid=:hb_joinsignstartid,hb_joinstartemployeeid=:hb_joinstartemployeeid ";
		updateSQL += " where flowid=:flowid and workstage=:workstage and worknum=:worknum and joinstartemployeeid=:joinstartemployeeid ";
		updateSQL += " and hb_joinstartemployeeid!=:hb_joinstartemployeeid2";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			public void setValues(PreparedStatement state) throws SQLException {
				// System.out.println(myWork.getWorkStage() + "#" +
				// myWork.getWorknum());
				// TODO Auto-generated method stub
				state.setObject(1, myWork.getHb_JoinSignStartId());
				state.setObject(2, myWork.getHb_JoinStartEmployeeId());
				state.setObject(3, myWork.getFlowId());
				state.setObject(4, myWork.getWorkStage().ordinal());
				state.setObject(5, myWork.getWorknum());
				state.setObject(6, myWork.getJoinStartEmployeeId());
				state.setObject(7, myWork.getHb_JoinStartEmployeeId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@SuppressWarnings("rawtypes")
	public MyWork getFirstHBWork(MyWork myWork) {
		String selectSQL = "select FLOWTYPE, WORKSTAGE, ID, FLOWID, DEPTNAME, DEPTID, CREATETIME, FINISHTIME, OPINION, STATUS, JOINSIGNSTARTID, JOINCYCLE, joinStartEmployeeId, WORKNUM, EMPLOYEEID, "
				+ " POSTCODE, DEPTCODE, A_DEPTCODE, CMPCODE, PARENTID,OLDFLOWID,ORGPATH,HB_CHENGHE,HB_JOINSIGNSTARTID,HB_JOINSTARTEMPLOYEEID,HB_CHENGHEEND,HB_AGREE "
				+ " from ww_work where flowid=:flowid "
				+ " and workstage=:workstage and worknum=:worknum and joincycle=:joincycle "
				+ " and joinstartemployeeid=:joinstartemployeeid order by createtime";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flowid", myWork.getFlowId());
		map.put("workstage", myWork.getWorkStage().ordinal());
		map.put("worknum", myWork.getWorknum());
		map.put("joincycle", myWork.getJoinCycle());
		map.put("joinstartemployeeid", myWork.getJoinStartEmployeeId());
		ParameterizedRowMapper<MyWork> mapper = new WorkRowMapper();
		List userTemplates = getJdbcTemplate().query(selectSQL, mapper,
				map);
		if (userTemplates != null && userTemplates.size() > 0) {
			return (MyWork) userTemplates.get(0);
		}
		return myWork;
	}

	public void updateHbWorkOrgPath(final MyWork myWork, MyWork firstWork) {
		final String tmpOrgPath = firstWork.getOrgpath();
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(tmpOrgPath)) {
			String updateSQL = "update ww_work set orgpath=:orgpath "
					+ " where flowid=:flowid and worknum=:worknum and joincycle=:joincycle "
					+ " and workstage=:workstage and joinstartemployeeid=:joinstartemployeeid";
			PreparedStatementSetter param = new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement state)
						throws SQLException {
					// TODO Auto-generated method stub
					state.setObject(1, tmpOrgPath);
					state.setObject(2, myWork.getFlowId());
					state.setObject(3, myWork.getWorknum());
					state.setObject(4, myWork.getJoinCycle());
					state.setObject(5, myWork.getWorkStage().ordinal());
					state.setObject(6, myWork.getJoinStartEmployeeId());
				}
			};
			getJdbcTemplate().update(updateSQL, param);
		}
	}

	public void updateFlowNextStep(final Flow flow) {
		String updateSQL = "update ww_flow set TEMPNEXTSTEP=:nextstep where id=:flowid";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, flow.getNextStep().ordinal());
				state.setObject(2, flow.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	public void updateFlowLastPerson(final Flow flow) {
		String updateSQL = "update ww_flow set TEMPLASTEMPLOYEEID=:employeeId,TEMPLASTDEPTID=:deptId,TEMPLASTPOSTCODE=:postCode"
				+ " where id=:flowid";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, flow.getLastEmployeeId());
				state.setObject(2, flow.getLastDeptId());
				state.setObject(3, flow.getLastPostCode());
				state.setObject(4, flow.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	public void updateNoJointSignDeptIds(final String deptIds, final Flow flow) {
		String updateSQL = "update ww_flow set TEMPJOINTSIGNDEPTIDS=:nojointSignDeptids where id=:flowid";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, deptIds);
				state.setObject(2, flow.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public MyWorkHistory[] listWorkHistoryEx(String formNum) {
		// 额外添加待办工作项的并且放到首上条目上面
		// 将原来的现实顺序按照倒叙方式排序
		// 整合代理和助理的现实风格,判断work的dlg字段与实际的字段是否一致，如果不一致就是委托了，需要重新风格上调整

		List result = new ArrayList();
		// w.finishtime is not null 表示已经被处理过的了
		// 首层无会办的情况 w.joinsignstartid == null DEPTCODE VARCHAR2(255), CMPCODE
		// VARCHAR2(255),
		String sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE,"
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID, "
				+ " w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.finishtime is not null and w.joinsignstartid is null and w.WORKSTAGE != "
				+ WorkStage.BOSS_SIGN.ordinal()
				+ " and w.WORKSTAGE != "
				+ WorkStage.DIVISION_SIGN.ordinal()
				+ " and w.WORKSTAGE != "
				+ WorkStage.FBOSS_SIGN.ordinal()
				+ " and w.WORKSTAGE != "
				+ WorkStage.CHENGHE.ordinal()
				+ " and w.WORKSTAGE != "
				+ WorkStage.BOSSPLUS_SIGN.ordinal()
				+ " and w.WORKSTAGE != "
				+ WorkStage.SubmitFBoss_SIGN.ordinal()
				+ " and w.WORKSTAGE != "
				+ WorkStage.CHAIRMAN_SIGN.ordinal()
				+ " order by w.finishtime, w.workstage";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formnum", formNum);
		// System.out.println(sql);
		ParameterizedRowMapper<MyWorkHistory> mapper = new MyWorkHistoryRowMapper();
		//logger.info(formNum + " 加载签核记录中的无会办记录 Start...");
		List userTemplates = getJdbcTemplate().query(sql, mapper, map);
		//logger.info(formNum + " 加载签核记录中的无会办记录 End...");
		result.addAll(userTemplates);
		
		/**
		 * 新增地方单位最高主管核准指派
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				//+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.WORKSTAGE = "
				+ WorkStage.CHENGHE.ordinal()
				+ " order by w.finishtime, w.workstage";
		Iterator localJoinSignWorkIterator = getJdbcTemplate().query(sql, mapper, map).iterator();
		Map<Long, Map<Integer, MyWorkHistory>> localJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer,MyWorkHistory>>(2);
		while(localJoinSignWorkIterator.hasNext()){
			MyWorkHistory localWorkHistory = (MyWorkHistory) localJoinSignWorkIterator.next();
			// 因为指派都会有子元素
			localWorkHistory.setHaveChildren(true);
			
			// 指派中，如果最终显示DOING状态，那么界面上不显示签核历程按钮
			if (localWorkHistory.getStatus() != FlowStatus.DOING){
				Map<Integer, MyWorkHistory> localOldHisMap = localJoinSignWorkHistoryMap.get(localWorkHistory.getOldFlowId());
				if (localOldHisMap == null){
					localOldHisMap = new HashMap<Integer, MyWorkHistory>();
					localJoinSignWorkHistoryMap.put(localWorkHistory.getOldFlowId(), localOldHisMap);
				}
				MyWorkHistory localOldHis = localOldHisMap.get(localWorkHistory.getWorkNum());
				if (localOldHis != null){
					if (localOldHis.getStatus().ordinal() < localWorkHistory.getStatus().ordinal()){
						long localWorkId = localOldHis.getWorkId();
						localWorkHistory.setWorkId(localWorkId);
						localOldHisMap.put(localWorkHistory.getWorkNum(), localWorkHistory);
					}
				} else {
					localOldHisMap.put(localWorkHistory.getWorkNum(), localWorkHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> localMapIterator = localJoinSignWorkHistoryMap.values().iterator();
		while(localMapIterator.hasNext()){
			Map<Integer, MyWorkHistory> localCurMap = localMapIterator.next();
			result.addAll(localCurMap.values());
		}

		/**
		 * 新增事业部主管核准指派
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.BUSINESS_SIGN.ordinal()
				+ " order by w.finishtime, w.workstage";
		//logger.info(formNum + " 加载签核记录中的事业部及指派记录 Start...");
		Iterator divisionSignJointSignWorkIterator = getJdbcTemplate()
				.query(sql, mapper, map).iterator();
		//logger.info(formNum + " 加载签核记录中的事业部及指派记录 End...");
		Map<Long, Map<Integer, MyWorkHistory>> divisionJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (divisionSignJointSignWorkIterator.hasNext()) {
			MyWorkHistory divisionWorkHistory = (MyWorkHistory) divisionSignJointSignWorkIterator
					.next();
			// 因为会办所以都会有子元素
			divisionWorkHistory.setHaveChildren(true);

			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示签核历程按钮
			if (divisionWorkHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> divisionOldHisMap = (Map<Integer, MyWorkHistory>) divisionJoinSignWorkHistoryMap
						.get(divisionWorkHistory.getOldFlowId());
				if (null == divisionOldHisMap) {
					divisionOldHisMap = new HashMap<Integer, MyWorkHistory>();
					divisionJoinSignWorkHistoryMap.put(
							divisionWorkHistory.getOldFlowId(),
							divisionOldHisMap);
				}
				MyWorkHistory divisonOldHis = divisionOldHisMap
						.get(divisionWorkHistory.getWorkNum());
				if (divisonOldHis != null) {
					if (divisonOldHis.getStatus().ordinal() < divisionWorkHistory
							.getStatus().ordinal()) {
						long divisionWorkId = divisonOldHis.getWorkId();
						divisionWorkHistory.setWorkId(divisionWorkId);
						divisionOldHisMap.put(divisionWorkHistory.getWorkNum(),
								divisionWorkHistory);
					}
				} else {
					divisionOldHisMap.put(divisionWorkHistory.getWorkNum(),
							divisionWorkHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> divisionMapIter = divisionJoinSignWorkHistoryMap
				.values().iterator();
		while (divisionMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> divisionCurMap = divisionMapIter.next();
			result.addAll(divisionCurMap.values());
		}

		/**
		 * 新增最高副主管核准指派
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.FBOSS_SIGN.ordinal()
				+ " order by w.finishtime, w.workstage";
		//logger.info(formNum + " 加载签核记录中的核决副主管及指派记录 Start...");
		Iterator fbossSignJointSignWorkIterator = getJdbcTemplate()
				.query(sql, mapper, map).iterator();
		//logger.info(formNum + " 加载签核记录中的核决副主管及指派记录 End...");
		Map<Long, Map<Integer, MyWorkHistory>> fbossJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (fbossSignJointSignWorkIterator.hasNext()) {
			MyWorkHistory fbossWorkHistory = (MyWorkHistory) fbossSignJointSignWorkIterator
					.next();
			// 指派都有子元素
			fbossWorkHistory.setHaveChildren(true);
			// 注意：指派中如果是最终显示DOING状态，那么界面上不显示签核历程按钮
			if (fbossWorkHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) fbossJoinSignWorkHistoryMap
						.get(fbossWorkHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					fbossJoinSignWorkHistoryMap.put(
							fbossWorkHistory.getOldFlowId(), oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(fbossWorkHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < fbossWorkHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						fbossWorkHistory.setWorkId(workId);
						oldHistoryMap.put(fbossWorkHistory.getWorkNum(),
								fbossWorkHistory);
					}
				} else {
					oldHistoryMap.put(fbossWorkHistory.getWorkNum(),
							fbossWorkHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> fbossMapIter = fbossJoinSignWorkHistoryMap
				.values().iterator();
		while (fbossMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = fbossMapIter.next();
			result.addAll(curMap.values());
		}

		/*
		 * 新增最终核准指派
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum "
				//+ " and w.joinStartEmployeeId = w.employeeid "
				+ " and w.WORKSTAGE = "
				+ WorkStage.BOSS_SIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		//logger.info(formNum + " 加载签核记录中的核决主管及指派记录 Start...");
		Iterator bossSignJointSignWorkIterator = getJdbcTemplate().query(
				sql, mapper, map).iterator();
		//logger.info(formNum + " 加载签核记录中的核决主管及指派记录 End...");
		Map<Long, Map<Integer, MyWorkHistory>> bossJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (bossSignJointSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) bossSignJointSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) bossJoinSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					bossJoinSignWorkHistoryMap.put(workHistory.getOldFlowId(),
							oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> bossMapIter = bossJoinSignWorkHistoryMap
				.values().iterator();
		while (bossMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = bossMapIter.next();
			result.addAll(curMap.values());
		}
		
		/*
		 * 新增最终核准二次审核
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum "
				//+ " and w.joinStartEmployeeId = w.employeeid "
				+ " and w.WORKSTAGE = "
				+ WorkStage.BOSSPLUS_SIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		Iterator bossPlusSignJointSignWorkIterator = getJdbcTemplate().query(
				sql, mapper, map).iterator();
		Map<Long, Map<Integer, MyWorkHistory>> bossPlusJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (bossPlusSignJointSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) bossPlusSignJointSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) bossPlusJoinSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					bossPlusJoinSignWorkHistoryMap.put(workHistory.getOldFlowId(),
							oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> bossPlusMapIter = bossPlusJoinSignWorkHistoryMap
				.values().iterator();
		while (bossPlusMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = bossPlusMapIter.next();
			result.addAll(curMap.values());
		}
		
		/*
		 * 新增董事长核准指派
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum "
				//+ " and w.joinStartEmployeeId = w.employeeid "
				+ " and w.WORKSTAGE = "
				+ WorkStage.CHAIRMAN_SIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		Iterator chairManSignWorkIterator = getJdbcTemplate().query(
				sql, mapper, map).iterator();
		Map<Long, Map<Integer, MyWorkHistory>> chairManSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (chairManSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) chairManSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) chairManSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					chairManSignWorkHistoryMap.put(workHistory.getOldFlowId(),
							oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> chairManMapIter = chairManSignWorkHistoryMap
				.values().iterator();
		while (chairManMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = chairManMapIter.next();
			result.addAll(curMap.values());
		}

		/*
		 * 新增副总裁核准指派
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum "
				//+ " and w.joinStartEmployeeId = w.employeeid "
				+ " and w.WORKSTAGE = "
				+ WorkStage.SubmitFBoss_SIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		Iterator SubmitFBossSignWorkIterator = getJdbcTemplate().query(
				sql, mapper, map).iterator();
		Map<Long, Map<Integer, MyWorkHistory>> SubmitFBossSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (SubmitFBossSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) SubmitFBossSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) SubmitFBossSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					SubmitFBossSignWorkHistoryMap.put(workHistory.getOldFlowId(),
							oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> SubmitFBossMapIter = SubmitFBossSignWorkHistoryMap
				.values().iterator();
		while (SubmitFBossMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = SubmitFBossMapIter.next();
			result.addAll(curMap.values());
		}
		
		
		/*
		 * 新增同中心会办
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.HB_joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.CENTERJOINTSIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		//logger.info(formNum + " 加载签核记录中的同中心会办及指派记录 Start...");
		Iterator centerJointSignWorkIterator = getJdbcTemplate().query(
				sql, mapper, map).iterator();
		//logger.info(formNum + " 加载签核记录中的同中心会办及指派记录 End...");
		Map<Long, Map<Integer, MyWorkHistory>> centerJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (centerJointSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) centerJointSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) centerJoinSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					centerJoinSignWorkHistoryMap.put(
							workHistory.getOldFlowId(), oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> centerMapIter = centerJoinSignWorkHistoryMap
				.values().iterator();
		while (centerMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = centerMapIter.next();
			result.addAll(curMap.values());
		}

		/*
		 * 新增同CMPCODE会办
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.HB_joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.CMPCODEJOINTSIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		//logger.info(formNum + " 加载签核记录中的同公司会办及指派记录 Start...");
		Iterator cmpcodeJointSignWorkIterator = getJdbcTemplate().query(
				sql, mapper, map).iterator();
		//logger.info(formNum + " 加载签核记录中的同公司会办及指派记录 End...");
		// Map<Integer, MyWorkHistory> cmpcodeJoinSignWorkHistoryMap = new
		// HashMap<Integer, MyWorkHistory>(2);
		Map<Long, Map<Integer, MyWorkHistory>> cmpcodeJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (cmpcodeJointSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) cmpcodeJointSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) cmpcodeJoinSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					cmpcodeJoinSignWorkHistoryMap.put(
							workHistory.getOldFlowId(), oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> cmpcodeMapIter = cmpcodeJoinSignWorkHistoryMap
				.values().iterator();
		while (cmpcodeMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = cmpcodeMapIter.next();
			result.addAll(curMap.values());
		}

		/*
		 * 新增同体系会办
		 */
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.HB_joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.SYSTEMJOINTSIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		//logger.info(formNum + " 加载签核记录中的同体系会办及指派记录 Start...");
		Iterator systemJointSignWorkIterator = getJdbcTemplate().query(
				sql, mapper, map).iterator();
		//logger.info(formNum + " 加载签核记录中的同体系会办及指派记录 End...");
		Map<Long, Map<Integer, MyWorkHistory>> systemJoinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (systemJointSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) systemJointSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) systemJoinSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					systemJoinSignWorkHistoryMap.put(
							workHistory.getOldFlowId(), oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> systemMapIter = systemJoinSignWorkHistoryMap
				.values().iterator();
		while (systemMapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = systemMapIter.next();
			result.addAll(curMap.values());
		}

		// 然后获取 会办的workStage.JoinSign, 以及jointSignStartId 与employeeId一致的记录
		// 以woknum加以区分分支情况,最终处理出来返回给首层的会办进程信息
		// and w.finishtime is not null
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.HB_joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.JOINTSIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		//logger.info(formNum + " 加载签核记录中的其它会办及指派记录 Start...");
		Iterator jointSignWorkIterator = getJdbcTemplate().query(sql,
				mapper, map).iterator();
		//logger.info(formNum + " 加载签核记录中的其它会办及指派记录 End...");
		Map<Long, Map<Integer, MyWorkHistory>> joinSignWorkHistoryMap = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (jointSignWorkIterator.hasNext()) {
			MyWorkHistory workHistory = (MyWorkHistory) jointSignWorkIterator
					.next();
			// 因为是会办所以都回有子元素
			workHistory.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory.getStatus() != FlowStatus.DOING) {
				// 取得最新状态即可
				Map<Integer, MyWorkHistory> oldHistoryMap = (Map<Integer, MyWorkHistory>) joinSignWorkHistoryMap
						.get(workHistory.getOldFlowId());
				if (null == oldHistoryMap) {
					oldHistoryMap = new HashMap<Integer, MyWorkHistory>();
					joinSignWorkHistoryMap.put(workHistory.getOldFlowId(),
							oldHistoryMap);
				}
				MyWorkHistory oldHistory = oldHistoryMap.get(workHistory
						.getWorkNum());
				if (oldHistory != null) {
					if (oldHistory.getStatus().ordinal() < workHistory
							.getStatus().ordinal()) {
						long workId = oldHistory.getWorkId();
						workHistory.setWorkId(workId);
						oldHistoryMap
								.put(workHistory.getWorkNum(), workHistory);
					}
				} else {
					oldHistoryMap.put(workHistory.getWorkNum(), workHistory);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> mapIter = joinSignWorkHistoryMap
				.values().iterator();
		while (mapIter.hasNext()) {
			Map<Integer, MyWorkHistory> curMap = mapIter.next();
			result.addAll(curMap.values());
		}
		
		// 新增行政职能会办分支
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree  "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.HB_joinStartEmployeeId = w.employeeid and w.WORKSTAGE = "
				+ WorkStage.XZJOINTSIGN.ordinal()
				+ " order by w.finishtime desc, w.workstage";
		//logger.info(formNum + " 加载签核记录中的其它会办及指派记录 Start...");
		Iterator jointSignWorkIterator2 = getJdbcTemplate().query(sql,
				mapper, map).iterator();
		//logger.info(formNum + " 加载签核记录中的其它会办及指派记录 End...");
		Map<Long, Map<Integer, MyWorkHistory>> joinSignworkHistory2Map2 = new HashMap<Long, Map<Integer, MyWorkHistory>>(
				2);
		while (jointSignWorkIterator2.hasNext()) {
			MyWorkHistory workHistory2 = (MyWorkHistory) jointSignWorkIterator2
					.next();
			// 因为是会办所以都回有子元素
			workHistory2.setHaveChildren(true);
			// 注意：会办中如果是最终显示DOING状态，那么界面上不显示 签核历程 按钮
			// else
			if (workHistory2.getStatus() != FlowStatus.DOING) {
				// 取得最新状态即可
				Map<Integer, MyWorkHistory> oldHistory2Map2 = (Map<Integer, MyWorkHistory>) joinSignworkHistory2Map2
						.get(workHistory2.getOldFlowId());
				if (null == oldHistory2Map2) {
					oldHistory2Map2 = new HashMap<Integer, MyWorkHistory>();
					joinSignworkHistory2Map2.put(workHistory2.getOldFlowId(),
							oldHistory2Map2);
				}
				MyWorkHistory oldHistory2 = oldHistory2Map2.get(workHistory2
						.getWorkNum());
				if (oldHistory2 != null) {
					if (oldHistory2.getStatus().ordinal() < workHistory2
							.getStatus().ordinal()) {
						long workId = oldHistory2.getWorkId();
						workHistory2.setWorkId(workId);
						oldHistory2Map2
								.put(workHistory2.getWorkNum(), workHistory2);
					}
				} else {
					oldHistory2Map2.put(workHistory2.getWorkNum(), workHistory2);
				}
			}
		}
		Iterator<Map<Integer, MyWorkHistory>> mapIter2 = joinSignworkHistory2Map2
				.values().iterator();
		while (mapIter2.hasNext()) {
			Map<Integer, MyWorkHistory> curMap2 = mapIter2.next();
			result.addAll(curMap2.values());
		}

		Collections.sort(result, new Comparator() {
			@Override
			public int compare(Object entryA, Object entryB) {
				MyWorkHistory workHistoryA = (MyWorkHistory) entryA;
				MyWorkHistory workHistoryB = (MyWorkHistory) entryB;
				Date dateA = workHistoryA.getCalculateProcessTime();// .getProcessTime()
				Date dateB = workHistoryB.getCalculateProcessTime();
				int comparedResult = dateA.compareTo(dateB);
				if (comparedResult == 0) {
					comparedResult = Integer
							.valueOf(workHistoryA.getStage())
							.compareTo(Integer.valueOf(workHistoryB.getStage()));
				}
				// 已有历程倒叙显示
				return -comparedResult;
			}
		});

		// 在首上加上该流程的代办工作项
		sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE, w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID,"
				+ " w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree   "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w left join ww_flow f on w.flowid = f.id where f.formnum = :formnum and w.status = :status"
				+ " order by w.workstage";
		map.put("status", FlowStatus.DOING.ordinal());
		//logger.info(formNum + " 加载签核记录中的待处理记录 Start...");
		List todoWorks = getJdbcTemplate().query(sql, mapper, map);
		//logger.info(formNum + " 加载签核记录中的待处理记录 End...");
		List overall = new ArrayList();
		overall.addAll(todoWorks);
		overall.addAll(result);
		return (MyWorkHistory[]) overall.toArray(new MyWorkHistory[overall
				.size()]);
	}

	@Override
	public boolean hasJointSignFinishedEx(final Flow flow) {
		// 判断 该流程下 会办分支 的所有work employeeId 等于 startEmployeeId的那个work列表，状态都是
		// 且workstage状态是会办的状态
		String searchSQL = "select w.joinsignstartid,count(w.id) as count_work from ww_work w where w.flowid = :id and w.oldflowid = :oldflowid and w.joinsignstartid is not null and w.status in ("
				+ FlowStatus.AGREE.ordinal()
				+ ","
				+ FlowStatus.WAITING.ordinal()
				+ ")"
				+ " and w.workstage in ("
				+ WorkStage.CENTERJOINTSIGN.ordinal()
				+ ","
				+ WorkStage.CMPCODEJOINTSIGN.ordinal()
				+ ","
				+ WorkStage.SYSTEMJOINTSIGN.ordinal()
				+ ","
				+ WorkStage.JOINTSIGN.ordinal()
				+ ","
				+ WorkStage.XZJOINTSIGN.ordinal()
				+ ") group by w.joinsignstartid ";

		String allSQL = "select w.joinsignstartid,count(w.id) as count_work from ww_work w where w.flowid = :id and w.oldflowid = :oldflowid "
				+ " and w.joinsignstartid is not null "
				+ " and w.workstage in ("
				+ WorkStage.CENTERJOINTSIGN.ordinal()
				+ ","
				+ WorkStage.CMPCODEJOINTSIGN.ordinal()
				+ ","
				+ WorkStage.SYSTEMJOINTSIGN.ordinal()
				+ ","
				+ WorkStage.JOINTSIGN.ordinal()
				+ ","
				+ WorkStage.XZJOINTSIGN.ordinal()
				+ ") group by w.joinsignstartid ";
		List<Map<String, Object>> searchList = getJdbcTemplate()
				.queryForList(searchSQL, flow.getId(), flow.getId());
		List<Map<String, Object>> allList = getJdbcTemplate()
				.queryForList(allSQL, flow.getId(), flow.getId());

		int same = 0;
		for (Map<String, Object> searchItem : searchList) {
			String joinsignstartid = searchItem.get("joinsignstartid")
					.toString();
			int searchCount = Integer.parseInt(searchItem.get("count_work")
					.toString());

			for (Map<String, Object> allItem : allList) {
				String allJoinsignstartid = allItem.get("joinsignstartid")
						.toString();
				int allCount = Integer.parseInt(allItem.get("count_work")
						.toString());

				if (joinsignstartid.equals(allJoinsignstartid)) {
					if (searchCount == allCount) {
						same++;
					}
					break;
				}
			}
		}

		return same == allList.size();

	}

	public int getCopyFlowCount(PersonDetail actualPerson,
			PersonDetail[] agentees, PersonDetail[] assistedMgrs) {
		List<String> agentEmployeeIdPostCodes = new ArrayList<String>(8);

		StringBuffer sql = new StringBuffer(
				"select f.status as status, 1 as employeeId, 1 as postCode, 1 as workId, f.id as flowId, 1 as deptId, "
						+ " f.formnum as formnum, f.flowtype as flowType, f.createtime as createtime,"
						+ " content.title as title, creator.name as creatorName, actual.name as actualName "
						+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
						+ "left join ww_persondetail actual on f.id = actual.flowid "
						+ "left join ww_flowcontent content on f.id = content.flowid "
						+ "where f.id in (select distinct(w.flowId) from ww_work w "
						+ " where (w.status = ? or (w.status=? and w.finishtime is null)) and (w.employeeid = ?");
		// 判断是否由代理人经办过的表单
		for (PersonDetail agentPerson : agentees) {
			agentEmployeeIdPostCodes.add(agentPerson.getEmployeeId()
					+ agentPerson.getPostCode());
		}

		// 助理
		List<String> assistEmployeeIdPostCodes = new ArrayList<String>(8);
		for (PersonDetail assistPerson : assistedMgrs) {
			assistEmployeeIdPostCodes.add(assistPerson.getEmployeeId()
					+ assistPerson.getPostCode());
		}

		sql.append(" or (concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		for (String employeeIdPostCode : agentEmployeeIdPostCodes) {
			sql.append(" , ");
			sql.append(" '" + employeeIdPostCode + "' ");
		}

		sql.append(")) ");

		sql.append(" or concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		for (String employeeIdPostCode : assistEmployeeIdPostCodes) {
			sql.append(" , ");
			sql.append(" '" + employeeIdPostCode + "' ");
		}
		sql.append(") ");

		sql.append(" )) and (f.istemplate is null or f.istemplate = 0) and creator.type = 0 and actual.type = 1 "
				+ " order by f.createtime DESC");


		String countSQL = "select count(*) "
				+ sql.toString().substring(
						sql.toString().toLowerCase().indexOf(" from ") + 1);
		return getJdbcTemplate().queryForInt(
				countSQL,
				new Object[] { FlowStatus.VIEW.ordinal(), FlowStatus.AGREE.ordinal(),
						actualPerson.getEmployeeId()});
	}

	public Page findCopyFlowByPage(PersonDetail actualPerson,
			PersonDetail[] agentees, PersonDetail[] assistedMgrs, int start,
			int size) {
		List<String> assistEmployeeIdPostCodes = new ArrayList<String>(8);

		for (PersonDetail assistPerson : assistedMgrs) {
			assistEmployeeIdPostCodes.add(assistPerson.getEmployeeId()
					+ assistPerson.getPostCode());
		}

		List<String> agentEmployeeIdPostCodes = new ArrayList<String>(8);

		StringBuffer sql = new StringBuffer();
		sql.append("select f.status as status, 1 as employeeId, 1 as postCode, 1 as workId, f.id as flowId,"
				+ " 1 as deptId, f.formnum as formnum, f.flowtype as flowType,f.isnew, f.createtime as createtime,"
				+ " content.title as title, creator.name as creatorName, actual.name as actualName, f.endTime "
				+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
				+ "left join ww_persondetail actual on f.id = actual.flowid "
				+ "left join ww_flowcontent content on f.id = content.flowid "
				+ "where f.id in (select distinct(w.flowId) from ww_work w "
				+ " where (w.status=? or (w.status=? and w.finishtime is null)) and (w.employeeid=? ");
		// 判断是否由代理人经办过的表单
		for (PersonDetail agentPerson : agentees) {
			agentEmployeeIdPostCodes.add(agentPerson.getEmployeeId()
					+ agentPerson.getPostCode());
		}

		sql.append(" or (concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		for (String employeeIdPostCode : agentEmployeeIdPostCodes) {
			sql.append(" , ");
			sql.append(" '" + employeeIdPostCode + "' ");
		}
		sql.append(")) ");

		sql.append(" or concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		for (String employeeIdPostCode : assistEmployeeIdPostCodes) {
			sql.append(" , ");
			sql.append(" '" + employeeIdPostCode + "' ");
		}
		sql.append(") ");

		sql.append(" )) and (f.istemplate is null or f.istemplate=0) and creator.type=0 and actual.type=1 "
				+ " order by f.createtime DESC");
/*System.out.println(sql.toString());
System.out.println(FlowStatus.VIEW.ordinal() + " ^" + FlowStatus.AGREE.ordinal() + " ^" + actualPerson.getEmployeeId());*/
		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapperPlus();
		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, sql.toString(), mapper,
				FlowStatus.VIEW.ordinal(), FlowStatus.AGREE.ordinal(), actualPerson.getEmployeeId());

		return page;
	}
	
	@SuppressWarnings("rawtypes")
	public MyWork getWorkByParentId(MyWork myWork){
		StringBuffer sql = new StringBuffer();
		sql.append("select FLOWTYPE, WORKSTAGE, ID, FLOWID, DEPTNAME, DEPTID, CREATETIME, FINISHTIME, OPINION, STATUS, JOINSIGNSTARTID, JOINCYCLE, joinStartEmployeeId, WORKNUM, EMPLOYEEID, ");
		sql.append("POSTCODE, DEPTCODE, A_DEPTCODE, CMPCODE, PARENTID,OLDFLOWID,ORGPATH,HB_CHENGHE,HB_JOINSIGNSTARTID,HB_JOINSTARTEMPLOYEEID,HB_CHENGHEEND,HB_AGREE from WW_WORK ");
		sql.append(" where id=:id");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", myWork.getParentId());
		ParameterizedRowMapper<MyWork> mapper = new WorkRowMapper();
		List tempWorks = getJdbcTemplate().query(sql.toString(), mapper, map);
		if (null != tempWorks && tempWorks.size() > 0){
			return (MyWork) tempWorks.get(0);
		}
		return null;
	}
	
	public void updateWorkOtherInfo(final MyWork myWork){
		StringBuffer sql = new StringBuffer();
		sql.append("update ww_work set employeenam=:employeenam,titnam=:titnam where id=:id");
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, myWork.getEmployeenam());
				state.setObject(2, myWork.getTitlenam());
				state.setObject(3, myWork.getId());
			}
		};
		getJdbcTemplate().update(sql.toString(), param);
	}
	
	public int getSecurityFlowCount() {
		StringBuffer sql = new StringBuffer(
				"select * from (select f.status as status, 1 as employeeId, 1 as postCode, 1 as workId, f.id as flowId, 1 as deptId, f.formnum as formnum, f.flowtype as flowType, f.createtime as createtime, content.title as title, creator.name as creatorName, actual.name as actualName "
						+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
						+ "left join ww_persondetail actual on f.id = actual.flowid "
						+ "left join ww_flowcontent content on f.id = content.flowid "
						+ "where f.id in (select distinct(w.flowId) from ww_work w where 1=1 and (w.postcode=?");

		sql.append(" )) and (f.istemplate is null or f.istemplate = 0) and creator.type = 0 and actual.type = 1 order by f.createtime DESC");
		sql.append(") where status!=?");
		String countSQL = "select count(*) "
				+ sql.toString().substring(
						sql.toString().toLowerCase().indexOf(" from ") + 1);
		return getJdbcTemplate().queryForInt(countSQL,
				new Object[] { "Q0010008", FlowStatus.CANCEL.ordinal() });
	}
	
	@Override
	// 在 已经审核 的栏目列出的是 我已经审核的表单。并且在显示的表格栏中需要添加 “状态” 信息栏。
	public Page findSecurityFlowByPage(AdvancedSearchDTO oneSearch, int start, int size) {
		StringBuffer whereClause = new StringBuffer();
		/**
		 * 优化查询SQL语句，主要是将原来的w.status=16的过滤方式，原来的是用not in ，这样会造成签核记录表查询了多次 且not
		 * in 查询效率低，现改为select * from (原SQL语句) where status!=16
		 * 就是先全部查出来，然后整体过滤status=16的记录，即撤消的申请单不显示
		 */
		StringBuffer sql = new StringBuffer();
		sql.append("select * from ( ");

		sql.append("select f.status as status, 1 as employeeId, 1 as postCode, 1 as workId, f.id as flowId, 1 as deptId, f.formnum as formnum, f.flowtype as flowType,f.isnew, f.createtime as createtime, content.title as title, creator.name as creatorName, actual.name as actualName "
				+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
				+ "left join ww_persondetail actual on f.id = actual.flowid "
				+ "left join ww_flowcontent content on f.id = content.flowid "
				+ "where f.id in (select distinct(w.flowId) from ww_work w where 1=1 ");

		FormStatus formStatus = oneSearch.getFormStatus();
		if (formStatus != null && formStatus != FormStatus.ALL) {
			whereClause.append(" and ( ");
			int i = 0;
			FlowStatus[] flowStatus = formStatus.transform();
			for (FlowStatus status : flowStatus) {
				if (i != 0) {
					whereClause.append(" or ");
				}
				whereClause.append(" f.status=" + status.ordinal() + " ");
				i++;
			}
			whereClause.append(" ) ");
		}

		sql.append(" and (w.postcode=? ");

		// +
		// " (w.status=? or w.status=? or w.status=? or w.status=?) and (w.postcode=? ");

		sql.append(" )) ");
		sql.append(whereClause)
				.append(" and content.title like '%" + oneSearch.getTitle() + "%' ")
				.append(" and (f.istemplate is null or f.istemplate=0) and creator.type=0 and actual.type=1 order by f.createtime DESC");

		sql.append(") where status!=?");
		
		// System.out.println(sql.toString());
		// System.out.println(FlowStatus.CANCEL.ordinal());
		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapper();
		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, sql.toString(), mapper, "Q0010008",
				FlowStatus.CANCEL.ordinal());
		return page;
	}
	
	public boolean hasTopApprove(Flow flow){
		String sql = "select count(*) from ww_work where flowid=? and postcode=?";
		int i = getJdbcTemplate().queryForInt(sql,
				new Object[] {flow.getId(), "Q0010008"});
		return i > 0;
	}
	
	@Override
	public void updateFlowEndTime(final Flow flow){
		String sql = "update ww_flow set endTime=:endTime where id=:id";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement stat) throws SQLException {
				stat.setObject(1, new Timestamp(flow.getEndTime()));
				stat.setObject(2, flow.getId());
			}
		};
		getJdbcTemplate().update(sql, param);
	}
	
	@Override
	public List<MyWorkDTO> advanceSearchToExport(AdvancedSearchDTO oneSearch) {
		StringBuffer whereClause = new StringBuffer();
		StringBuffer selectClause = new StringBuffer();
		selectClause
				.append("select distinct flow.formnum as formnum, flow.status as status, 1 as employeeId, 1 as postCode, 1 as workId, flow.id as flowId, 1 as deptId, flow.flowtype as flowType,flow.isnew, flow.createtime as createtime, "
						+ "content.title as title, creator.name as creatorName, actual.name as actualName "
						+ " , flow.endtime "
						+ " from ww_flow flow "
						+ " left join ww_persondetail creator on flow.id = creator.flowid "
						+ " left join ww_persondetail actual on flow.id = actual.flowid "
						+ " left join ww_flowcontent content on flow.id = content.flowid "
						+ " left join ww_work work on flow.id = work.flowid ");

		List<Object> whereClauseParams = new ArrayList<Object>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		// 表单区分
		whereClause
				.append(" where flow.FLOWTYPE = ? and flow.templatecreateid is null and creator.type = 0 and actual.type = 1 ");
		whereClauseParams.add(oneSearch.getFlowType().ordinal());
		try {
			long startTime = 0L, endTime = 0L;
			// 如果为空那么就变成系统今天日期
			if (!org.apache.commons.lang3.StringUtils.isEmpty(oneSearch
					.getEndTime())) {
				endTime = sdf.parse(oneSearch.getEndTime()).getTime() + 24 * 60
						* 60 * 1000;
			} else {
				endTime = System.currentTimeMillis();
			}
			whereClauseParams.add(new Timestamp(endTime));
			whereClause.append(" and flow.createTime <= ?");

			// 组织时间参数
			if (!org.apache.commons.lang3.StringUtils.isEmpty(oneSearch
					.getStartTime())) {
				startTime = sdf.parse(oneSearch.getStartTime()).getTime();
			} else {
				// 当前endTime的上一个月时间
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(endTime);
				calendar.add(Calendar.MONTH, -1);
				startTime = calendar.getTimeInMillis();
			}
			whereClauseParams.add(new Timestamp(startTime));
			whereClause.append(" and flow.createTime >= ?");
		} catch (ParseException ex) {
			ex.printStackTrace();
		}

		try {
			long closeStartTime = 0L, closeEndTime = 0L;
			if (!org.apache.commons.lang3.StringUtils.isEmpty(oneSearch.getCloseEndTime())) {
				closeEndTime = sdf.parse(oneSearch.getCloseEndTime()).getTime() + 24 * 60
						* 60 * 1000;
				whereClauseParams.add(new Timestamp(closeEndTime));
				whereClause.append(" and flow.endTime <= ?");
			}
			if (!org.apache.commons.lang3.StringUtils.isEmpty(oneSearch.getCloseStartTime())) {
				closeStartTime = sdf.parse(oneSearch.getCloseStartTime()).getTime();
				whereClauseParams.add(new Timestamp(closeStartTime));
				whereClause.append(" and flow.endTime >= ?");
			}
			
		} catch (ParseException ex) {
			ex.printStackTrace();
		}

		// 处理 流程流转状态
		FormStatus formStatus = oneSearch.getFormStatus();

		// --注意“重起案”可为过程中状态，而非flow最后状态，所以从work表中的status判断
		if (formStatus == FormStatus.RENEWED) {
			whereClause.append(" and work.status = "
					+ formStatus.transform()[0].ordinal());
		}

		if (formStatus != null && formStatus != FormStatus.ALL
				&& formStatus != FormStatus.RENEWED) {
			whereClause.append(" and (");
			// 语句格式类似 ：status = 1 or status = 2 ...
			int i = 0;
			FlowStatus[] flowStatus = formStatus.transform();
			for (FlowStatus status : flowStatus) {
				if (i != 0) {
					whereClause.append(" or ");
				}
				whereClause.append(" flow.status = " + status.ordinal() + " ");
				i++;
			}
			whereClause.append(" )");
		}

		/**
		 * add by Cao_Shengyong 2014-03-26 优化查询条件 新增一个临时变量，用于设置SQL语句中增加的条件
		 * 该条件是表示在签核记录中存在当前登录人的工号，包括代理人
		 */
		String tmpWhere = "";
		if (oneSearch.getUser() != null) {
			tmpWhere = " or flow.id in (select distinct (w.flowid) from ww_work w where w.employeeid='"
					+ oneSearch.getUser().getEmployeeId()
					+ "' or (concat(w.employeeid, w.postcode) in ('"
					+ oneSearch.getUser().getEmployeeId()
					+ "') or w.dlg_employeeid='"
					+ oneSearch.getUser().getEmployeeId()
					+ "') or concat(w.employeeid, w.postcode) in ('"
					+ oneSearch.getUser().getEmployeeId() + "'))";
		}

		// 处理流程分支条件
		if (oneSearch.getBranch() == Branch.MySubmit) {
			// 我申请的（FlowType）表单, 也就是实际创建人是登陆的人
			// whereClause.append(" and actual.empnumber = ? and actual.postid = ? ");
			whereClause.append(" and actual.empnumber = ? ");
			whereClauseParams.add(oneSearch.getUser().getEmployeeId());
			// whereClauseParams.add(oneSearch.getUser().getPostId());
		} else if (oneSearch.getBranch() == Branch.ThisUnitSubmit) {
			// 创建人表中的单位 与登陆人的单位

			if (oneSearch.getUserDeptIds() != null) {
				whereClause.append(" and actual.deptid in ( ");
				int i = 0;
				for (String deptId : oneSearch.getUserDeptIds()) {
					if (i != 0) {
						whereClause.append(" , ");
					}
					whereClause.append(" '" + deptId + "' ");
					i++;
				}
				whereClause.append(") ");
			}

			whereClause
					.append(" and ? in (select hg.employeeid from hr_groupmgr hg)");
			whereClauseParams.add(oneSearch.getUser().getEmployeeId());
		} else if (oneSearch.getBranch() == Branch.SubDeptSubmit) {
			// 关于下下部门的定义：周德佶给予回复 20130122 16:12:56
			// 部门、中心主管、单位最高主管下属的员工提交的单。仅为涉及提交的表单
			whereClause.append(" and (actual.mgremployeeid = ? ");
			if (oneSearch.getSubDeptIds() != null) {
				whereClause.append(" or (actual.deptid in ( ");
				int i = 0;
				for (String deptId : oneSearch.getSubDeptIds()) {
					if (i != 0) {
						whereClause.append(" , ");
					}
					whereClause.append(" '" + deptId + "' ");
					i++;
				}
				whereClause.append("))  ) ");
			} else {
				// 对于没有下辖部门的情况，就给一个不存在的deptid
				whereClause.append(" or actual.deptid in (9999999) ) ");
			}
			//whereClause.append(")");
			whereClauseParams.add(oneSearch.getEmployeeId());
		} else if (oneSearch.getBranch() == Branch.MyApproved) {
			// 最终是由登陆人，做核决的表单
			whereClause
					.append(" and exists(select work.flowid from ww_work work where work.flowid = flow.id and (work.status=? or work.status=?) and work.employeeid = ?)");
			whereClauseParams.add(FlowStatus.APPROVED.ordinal());
			whereClauseParams.add(FlowStatus.AGREE.ordinal());
			whereClauseParams.add(oneSearch.getUser().getEmployeeId());
		} else if (oneSearch.getBranch() == Branch.MyAccepted) {
			// 还未处理的
			whereClause
					.append(" and exists(select work.flowid from ww_work work where work.flowid = flow.id and (work.status = ? or work.status = ? or work.status = ? or work.status = ?) and work.employeeid = ?)");
			whereClauseParams.add(FlowStatus.DOING.ordinal());
			whereClauseParams.add(FlowStatus.AGREE.ordinal());
			whereClauseParams.add(FlowStatus.APPROVED.ordinal());
			whereClauseParams.add(FlowStatus.REJECT.ordinal());
			whereClauseParams.add(oneSearch.getUser().getEmployeeId());
		}

		// 根据表单编号与主旨查询需限制在权限范围内，即 我申请的&本单位发文的&下辖部门提交的
		// 复制上面代码

		else if (oneSearch.getBranch() == Branch.FormNum) {
			if (null != oneSearch.getUser()) {
				// Branch.MySubmit
				whereClause
						.append(" and ((actual.empnumber = ? and actual.postid = ?) ");
				whereClauseParams.add(oneSearch.getUser().getEmployeeId());
				whereClauseParams.add(oneSearch.getUser().getPostId());

				// Branch.ThisUnitSubmit
				if (oneSearch.getUserDeptIds() != null) {
					whereClause.append(" or (actual.deptid in ( ");
					int i = 0;
					for (String deptId : oneSearch.getUserDeptIds()) {
						if (i != 0) {
							whereClause.append(" , ");
						}
						whereClause.append(" '" + deptId + "' ");
						i++;
					}
					whereClause.append(") ");
				}
				whereClause
						.append(" and ? in (select hg.employeeid from hr_groupmgr hg))");
				whereClauseParams.add(oneSearch.getUser().getEmployeeId());

				// Branch.SubDeptSubmit
				if (oneSearch.getSubDeptIds() != null) {
					whereClause.append(" or (actual.deptid in ( ");
					int i = 0;
					for (String deptId : oneSearch.getSubDeptIds()) {
						if (i != 0) {
							whereClause.append(" , ");
						}
						whereClause.append(" '" + deptId + "' ");
						i++;
					}
					whereClause.append(")) " + tmpWhere + " ) ");
				} else {
					// 对于没有下辖部门的情况，就给一个不存在的deptid
					whereClause.append(" or actual.deptid in (9999999) "
							+ tmpWhere + " ) ");
				}
			}
			// 创建人表中的单位 与登陆人的单位
			if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getFormNum())){
				whereClause.append(" and flow.formnum like '%"
						+ oneSearch.getFormNum() + "%' ");
			}
		} else if (oneSearch.getBranch() == Branch.Title) {
			if (null != oneSearch.getUser()) {
				// Branch.MySubmit
				whereClause
						.append(" and ((actual.empnumber = ? and actual.postid = ?) ");
				whereClauseParams.add(oneSearch.getUser().getEmployeeId());
				whereClauseParams.add(oneSearch.getUser().getPostId());

				// Branch.ThisUnitSubmit
				if (oneSearch.getUserDeptIds() != null) {
					whereClause.append(" or (actual.deptid in ( ");
					int i = 0;
					for (String deptId : oneSearch.getUserDeptIds()) {
						if (i != 0) {
							whereClause.append(" , ");
						}
						whereClause.append(" '" + deptId + "' ");
						i++;
					}
					whereClause.append(") ");
				}
				whereClause
						.append(" and ? in (select hg.employeeid from hr_groupmgr hg))");
				whereClauseParams.add(oneSearch.getUser().getEmployeeId());

				// Branch.SubDeptSubmit
				if (oneSearch.getSubDeptIds() != null) {
					whereClause.append(" or (actual.deptid in ( ");
					int i = 0;
					for (String deptId : oneSearch.getSubDeptIds()) {
						if (i != 0) {
							whereClause.append(" , ");
						}
						whereClause.append(" '" + deptId + "' ");
						i++;
					}
					whereClause.append(")) " + tmpWhere + " ) ");
				} else {
					// 对于没有下辖部门的情况，就给一个不存在的deptid
					whereClause.append(" or actual.deptid in (9999999) "
							+ tmpWhere + " ) ");
				}
			}
			// 创建人表中的单位 与登陆人的单位
			if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getTitle())){
				whereClause.append(" and content.title like '%"
						+ oneSearch.getTitle() + "%' ");
			}
		} else if (oneSearch.getBranch() == Branch.MyDlg) {
			whereClause.append(" and work.dlg_employeeid = "
					+ oneSearch.getUser().getEmployeeId());
		}
		
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getLocalType())){
			whereClause.append(" and flow.islocal=? ");
			whereClauseParams.add(oneSearch.getLocalType());
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getCreator())){
			whereClause.append(" and actual.empnumber=? ");
			whereClauseParams.add(oneSearch.getCreator());
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(oneSearch.getCreatCmp())){
			whereClause.append(" and flow.formnum like '%" + oneSearch.getCreatCmp() + "%'");
		}

		// 标签排序处理
		whereClause.append(" order by createTime desc ");

		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapperPlus();
		String querySQL = selectClause.append(whereClause).toString();

		return getJdbcTemplate().query(querySQL, mapper, whereClauseParams.toArray());
	}
	
	protected class LogInfoRowMapper implements ParameterizedRowMapper<LogInfo>{
		public LogInfo mapRow(ResultSet rs, int rowNum){
			LogInfo logInfo = new LogInfo();
			try {
				logInfo.setStamp(rs.getString("stamp"));
				logInfo.setThread(rs.getString("thread"));
				logInfo.setInfolevel(rs.getString("infolevel"));
				logInfo.setClassdir(rs.getString("classdir"));
				logInfo.setClassfile(rs.getString("classfile"));
				logInfo.setClassline(rs.getString("classline"));
				logInfo.setMessages(rs.getString("messages"));
				logInfo.setPdate(rs.getString("pdate"));
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
			return logInfo;
		}
	}
	
	@Override
	public Page findLogInfoByPage(String startDate, String logLevel, int start, int size){
		StringBuffer buffer = new StringBuffer();
		List<Object> whereParams = new ArrayList<Object>();
		buffer.append("select stamp,thread,infolevel,classdir,classfile,classline,messages,pdate ");
		buffer.append(" from ww_flow_log4 where 1=1 ");
		if (org.apache.commons.lang3.StringUtils.isEmpty(startDate)){
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			startDate = df.format(new Date());
		}
		buffer.append(" and pdate=? ");
		whereParams.add(startDate);
		if (!logLevel.equalsIgnoreCase("ALL")){
			buffer.append(" and infolevel=? ");
			whereParams.add(logLevel);
		}
		buffer.append(" order by stamp desc ");
		ParameterizedRowMapper<LogInfo> mapper = new LogInfoRowMapper();
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, buffer.toString(), mapper, whereParams.toArray());
		return page;
	}
	
	@Override
	public List<LogInfo> findLogInfoByExport(String startDate, String logLevel){
		StringBuffer buffer = new StringBuffer();
		List<Object> whereParams = new ArrayList<Object>();
		buffer.append("select stamp,thread,infolevel,classdir,classfile,classline,messages,pdate ");
		buffer.append(" from ww_flow_log4 where 1=1 ");
		if (org.apache.commons.lang3.StringUtils.isEmpty(startDate)){
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			startDate = df.format(new Date());
		}
		buffer.append(" and pdate=? ");
		whereParams.add(startDate);
		if (!logLevel.equalsIgnoreCase("ALL")){
			buffer.append(" and infolevel=? ");
			whereParams.add(logLevel);
		}
		buffer.append(" order by stamp desc ");
		ParameterizedRowMapper<LogInfo> mapper = new LogInfoRowMapper();
		return getJdbcTemplate().query(buffer.toString(), mapper, whereParams.toArray());
	}
	
	@Override
	public void updateFlowSubmitBoss(final Flow flow){
		String updateSQL = "update ww_flow set SUBMITBOSS=:SUBMITBOSS where ID=:FLOWID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, flow.isSubmitBoss());
				state.setObject(2, flow.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}
	
	@Override
	public void updateFlowSubmitFBoss(final Flow flow){
		String updateSQL = "update ww_flow set SUBMITFBOSS=:SUBMITFBOSS where ID=:FLOWID";
		PreparedStatementSetter param = new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement state) throws SQLException {
				state.setObject(1, flow.isSubmitFBoss());
				state.setObject(2, flow.getId());
			}
		};
		getJdbcTemplate().update(updateSQL, param);
	}
	
	@Override
	// 在 已经审核 的栏目列出的是 我已经审核的表单。并且在显示的表格栏中需要添加 “状态” 信息栏。
	public Page findCreatorFlowByPageEx(PersonDetail actualPerson,
			PersonDetail[] agentees, PersonDetail[] assistedMgrs, int start,
			int size, String sidx, String sord, AdvancedSearchDTO searchDTO) {

		String tmpWhereStr = "";
		List<String> assistEmployeeIdPostCodes = new ArrayList<String>(8);

		for (PersonDetail assistPerson : assistedMgrs) {
			assistEmployeeIdPostCodes.add(assistPerson.getEmployeeId()
					+ assistPerson.getPostCode());
		}

		List<String> agentEmployeeIdPostCodes = new ArrayList<String>(8);
		// 判断是否由代理人经办过的表单
		for (PersonDetail agentPerson : agentees) {
			agentEmployeeIdPostCodes.add(agentPerson.getEmployeeId()
					+ agentPerson.getPostCode());
		}
		
		tmpWhereStr = " w.employeeid='" + actualPerson.getEmployeeId() + "' ";
		tmpWhereStr += " or (concat(w.employeeid,w.POSTCODE) in ( ";
		tmpWhereStr += "'" + actualPerson.getEmployeeId() + "'";
		for (String employeeIdPostCode : agentEmployeeIdPostCodes) {
			tmpWhereStr += " , ";
			tmpWhereStr += " '" + employeeIdPostCode + "' ";
		}
		tmpWhereStr += " ) and w.DLG_EMPLOYEEID = '" + actualPerson.getEmployeeId() + "') ";
		tmpWhereStr += " or concat(w.employeeid,w.POSTCODE) in ( ";
		tmpWhereStr += "'" + actualPerson.getEmployeeId() + "'";
		for (String employeeIdPostCode : assistEmployeeIdPostCodes) {
			tmpWhereStr += " , ";
			tmpWhereStr += " '" + employeeIdPostCode + "' ";
		}
		tmpWhereStr += " ) ";

		/**
		 * 优化查询SQL语句，主要是将原来的w.status=16的过滤方式，原来的是用not in ，这样会造成签核记录表查询了多次 且not
		 * in 查询效率低，现改为select * from (原SQL语句) where status!=16
		 * 就是先全部查出来，然后整体过滤status=16的记录，即撤消的申请单不显示
		 */
		StringBuffer sql = new StringBuffer();
		sql.append("select * from (");

		sql.append("select f.status as status, 1 as employeeId, 1 as postCode, 1 as workId, f.id as flowId, 1 as deptId, f.formnum as formnum, f.flowtype as flowType,f.isnew, f.createtime as createtime, content.title as title, creator.name as creatorName, actual.name as actualName "
				+ "  , f.endtime "
				+ " , s.finishtime "
				//+ " ,(select max(finishtime) from ww_work w where w.flowid=f.id and (" + tmpWhereStr + ")) finishtime "
				+ "from ww_flow f left join ww_persondetail creator on f.id = creator.flowid "
				+ "left join ww_persondetail actual on f.id = actual.flowid "
				+ "left join ww_flowcontent content on f.id = content.flowid "
				+ "left join (select max(finishtime) finishtime,flowid from ww_work w,ww_flow f where f.id=w.flowid and (" + tmpWhereStr + ") group by w.flowid) s "
				+ " on s.flowid=f.id "
				+ "where f.id in (select distinct(w.flowId) from ww_work w where (w.status = ? or w.status = ? or w.status = ? or w.status = ? or w.status = ? or w.status = ?) and w.finishtime is not null and "
				//+ " (w.employeeid = ? "
				);

		/*sql.append(" or (concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		for (String employeeIdPostCode : agentEmployeeIdPostCodes) {
			sql.append(" , ");
			sql.append(" '" + employeeIdPostCode + "' ");
		}
		sql.append(") and w.DLG_EMPLOYEEID = ?) ");

		sql.append(" or concat(w.employeeid,w.POSTCODE) in ( ");
		sql.append("'" + actualPerson.getEmployeeId() + "'");
		for (String employeeIdPostCode : assistEmployeeIdPostCodes) {
			sql.append(" , ");
			sql.append(" '" + employeeIdPostCode + "' ");
		}
		sql.append(") ");*/
		sql.append(" (").append(tmpWhereStr);

		sql.append(" )) and (f.istemplate is null or f.istemplate = 0) and creator.type = 0 and actual.type = 1 ");

		//sql.append(" order by createtime desc ) where status!=? ");
		sql.append(" )  where status!=? ");
		
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getTitle())){
			sql.append(" and title like '%" + searchDTO.getTitle() + "%'");
		}
		
		String idx = "createtime";
		String ord = "desc";
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(sidx)){
			idx = sidx;
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(sord)){
			ord = sord;
		}
		
		sql.append(" order by " + idx + " " + ord);
		
		//sql.append(") where status!=? order by finishtime desc ");
/*System.out.println(sql.toString());
System.out.println(FlowStatus.AGREE.ordinal() + " ^ " + FlowStatus.APPROVED.ordinal() + " ^ " + 
		FlowStatus.CANCEL.ordinal() + " ^ " + FlowStatus.REJECT.ordinal() + " ^ " + 
		FlowStatus.AGREE.ordinal() + " ^ " + FlowStatus.WAITING.ordinal() + " ^ " + 
		FlowStatus.CANCEL.ordinal());*/
		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapperPlusEx();
		// 构造分页信息
		Page page = new Page(start, size, 0, size, null);
		page = queryWithPage(page, sql.toString(), mapper,
				FlowStatus.AGREE.ordinal(), FlowStatus.APPROVED.ordinal(),
				FlowStatus.CANCEL.ordinal(), FlowStatus.REJECT.ordinal(),
				//FlowStatus.DOING.ordinal(), FlowStatus.WAITING.ordinal(),
				FlowStatus.AGREE.ordinal(), FlowStatus.WAITING.ordinal(),
				//actualPerson.getEmployeeId(), actualPerson.getEmployeeId(),
				FlowStatus.CANCEL.ordinal());
		return page;
	}
	
	@Override
	public MyWorkHistory getHisWorkByID(MyWork work){
		String sql = "select w.DLG_EMPLOYEEID as DLG_EMPLOYEEID, w.DLG_POSTCODE as DLG_POSTCODE, w.DLG_DEPTCODE as DLG_DEPTCODE, w.DLG_A_DEPTCODE as DLG_A_DEPTCODE, w.DLG_CMPCODE as DLG_CMPCODE,"
				+ "w.employeeId as employeeId, w.POSTCODE as POSTCODE, w.DEPTCODE as DEPTCODE, w.CMPCODE as CMPCODE, w.DEPTID as DEPTID, w.DLG_DEPTID as DLG_DEPTID, "
				+ " w.id as workId, w.deptname as deptName, w.employeeid as processManName, "
				+ "w.finishtime as processTime, w.status as workStatus, w.opinion as opinion, w.worknum as worknum, w.workstage stage,w.oldflowid oldFlowId,w.orgpath orgpath,nvl(w.hb_agree,'0') hbAgree "
				+ ",w.employeenam,w.titnam,w.dlg_employeenam,w.dlg_titnam "
				+ "from ww_work w where w.id=:id";
		ParameterizedRowMapper<MyWorkHistory> mapper = new MyWorkHistoryRowMapper();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", work.getId());
		return getJdbcTemplate().query(sql, mapper, map).get(0);
	}
	
	protected class MyWorkDTORowMapperPlusX implements ParameterizedRowMapper<MyWorkDTO>{
		private Map<String, WorkRole> workRoleMap;
		public MyWorkDTORowMapperPlusX(){}
		public MyWorkDTORowMapperPlusX(Map<String, WorkRole> workRoleMap){
			this.workRoleMap = workRoleMap;
		}
		@SuppressWarnings("finally")
		public MyWorkDTO mapRow(ResultSet rs, int rowNum){
			MyWorkDTO myWorkDTO = new MyWorkDTO();
			try {
				FlowStatus status = FlowStatus.values()[rs.getInt("status")];
				myWorkDTO.setFlowDisplayStatusName(status.getFlowDisplayStatusName());
				myWorkDTO.setWorkId(rs.getInt("workId"));
				myWorkDTO.setFlowId(rs.getInt("flowId"));
				myWorkDTO.setDeptId(rs.getInt("deptId"));
				myWorkDTO.setFormnum(rs.getString("formnum"));
				myWorkDTO
						.setCreateTime(String.valueOf(rs.getDate("createtime")));
				myWorkDTO.setTitle(rs.getString("title"));
				myWorkDTO.setCreatorName(rs.getString("creatorName"));
				myWorkDTO.setActualName(rs.getString("actualName"));
				myWorkDTO.setFlowType(FlowType.values()[rs.getInt("flowType")]);
				myWorkDTO.setIsNew(rs.getString("isnew"));
				if (rs.getDate("endtime") != null) {
					myWorkDTO.setEndTime(String.valueOf(rs.getDate("endtime")));
				}
				
				myWorkDTO.setDecionMaker(DescionMaker.values()[rs.getInt("decionmaker")]);
				myWorkDTO.setLocalType(rs.getString("islocal"));
				myWorkDTO.setDeptPath(rs.getString("deptpath"));

				String employeeId = rs.getString("employeeId");
				String postCode = rs.getString("postCode");
				String key = employeeId + postCode;
				if (workRoleMap != null){
					if (workRoleMap.containsKey(key)) {
						myWorkDTO.setWorkRole(workRoleMap.get(key));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				return myWorkDTO;
			}
		}
	}
	
	@Override
	public Page confSearch(ConfidentialSearchDTO searchDTO, int start, int size){
		
		StringBuffer selectClause = new StringBuffer();
		StringBuffer whereClause = new StringBuffer();
		
		selectClause.append("select distinct flow.formnum as formnum, flow.status as status, flow.id as flowId, ");
		selectClause.append(" 1 as employeeId, 1 as postCode, 1 as workId, 1 as deptId, 1 as actualName, ");
		selectClause.append(" flow.flowtype as flowType, flow.isnew, flow.createtime as createtime, ");
		selectClause.append(" flow.decionmaker,flow.islocal, ");
		selectClause.append(" content.title as title, creator.name as creatorName, creator.deptpath, flow.endtime ");
		selectClause.append(" from ww_flow flow ");
		selectClause.append(" left join ww_persondetail creator on flow.id=creator.flowid ");
		selectClause.append(" left join ww_flowcontent content on flow.id=content.flowid ");


		List<Object> whereClauseParams = new ArrayList<Object>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		whereClause.append(" where flow.flowtype=? and flow.templatecreateid is null and creator.type=0 ");
		whereClauseParams.add(searchDTO.getFlowType().ordinal());
		
		try {
			long startTime = 0L, endTime = 0L;
			if (!org.apache.commons.lang3.StringUtils.isEmpty(searchDTO.getEndTime())){
				endTime = sdf.parse(searchDTO.getEndTime()).getTime() + 24 * 60 * 60 * 1000;
			} else {
				endTime = System.currentTimeMillis();
			}
			whereClauseParams.add(new Timestamp(endTime));
			whereClause.append(" and flow.createTime<=? ");
			
			if (!org.apache.commons.lang3.StringUtils.isEmpty(searchDTO.getStartTime())){
				startTime = sdf.parse(searchDTO.getStartTime()).getTime();
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(endTime);
				calendar.add(Calendar.MONDAY, -1);
				startTime = calendar.getTimeInMillis();
			}
			whereClauseParams.add(new Timestamp(startTime));
			whereClause.append(" and flow.createTime>=? ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		FormStatus formStatus = searchDTO.getFormStatus();
		
		if (formStatus != null && formStatus != FormStatus.ALL){
			whereClause.append(" and (");
			int i = 0;
			FlowStatus[] flowStatus = formStatus.transform();
			for(FlowStatus status : flowStatus){
				if (i != 0){
					whereClause.append(" or ");
				}
				whereClause.append(" flow.status=" + status.ordinal() + " ");
				i++;
			}
			whereClause.append(" ) ");
		}
		
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getTitle())){
			whereClause.append(" and content.title like ? ");
			whereClauseParams.add("%" + searchDTO.getTitle() + "%");
		}	
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getFormNum())){
			whereClause.append(" and flow.formnum like ? ");
			whereClauseParams.add("%" + searchDTO.getFormNum() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getLocalType())){
			whereClause.append(" and flow.islocal=? ");
			whereClauseParams.add(searchDTO.getLocalType());
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getCreator())){
			whereClause.append(" and (creator.empnumber=? or creator.name like ? ) ");
			whereClauseParams.add(searchDTO.getCreator());
			whereClauseParams.add("%" + searchDTO.getCreator() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getCreatCmp())){
			whereClause.append(" and flow.formnum like ? ");
			whereClauseParams.add("%" + searchDTO.getCreatCmp() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getLeader())){
			DescionMaker descionMaker = DescionMaker.valueOf(searchDTO.getLeader());
			whereClause.append(" and flow.decionmaker=? ");
			whereClauseParams.add(descionMaker.ordinal());
		}  
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getRePath())){
			whereClause.append(" and creator.deptpath like ? ");
			whereClauseParams.add("%" + searchDTO.getRePath() + "%");
		} 
		
		whereClause.append(" order by " + searchDTO.getOrderBy() + " " + searchDTO.getSorder() + " ");
		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapperPlusX();
		
		Page page = new Page(start, size, 0, size, null);
		String querySQL = selectClause.append(whereClause).toString();

		page = queryWithPage(page, querySQL, mapper, whereClauseParams.toArray());

		return page;
	}
	
	@Override
	public void saveLog(LogDTO logDto){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("STAMP", new Timestamp(System.currentTimeMillis()));
		params.put("IP", logDto.getIP());
		params.put("FUSER", logDto.getUser());
		params.put("MESSAGES", logDto.getMessages());
		params.put("CONTR", logDto.getControl());
		log_jdbcInsert.execute(params);
	}
	
	@Override
	public List<MyWorkDTO> confSearchToExport(ConfidentialSearchDTO searchDTO){
		
		StringBuffer selectClause = new StringBuffer();
		StringBuffer whereClause = new StringBuffer();
		
		selectClause.append("select distinct flow.formnum as formnum, flow.status as status, flow.id as flowId, ");
		selectClause.append(" 1 as employeeId, 1 as postCode, 1 as workId, 1 as deptId, 1 as actualName, ");
		selectClause.append(" flow.flowtype as flowType, flow.isnew, flow.createtime as createtime, ");
		selectClause.append(" flow.decionmaker,flow.islocal, ");
		selectClause.append(" content.title as title, creator.name as creatorName, creator.deptpath, flow.endtime ");
		selectClause.append(" from ww_flow flow ");
		selectClause.append(" left join ww_persondetail creator on flow.id=creator.flowid ");
		selectClause.append(" left join ww_flowcontent content on flow.id=content.flowid ");
		
		List<Object> whereClauseParams = new ArrayList<Object>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		whereClause.append(" where flow.flowtype=? and flow.templatecreateid is null and creator.type=0");
		whereClauseParams.add(searchDTO.getFlowType().ordinal());
		
		try {
			long startTime = 0L, endTime = 0L;
			if (!org.apache.commons.lang3.StringUtils.isEmpty(searchDTO.getEndTime())){
				endTime = sdf.parse(searchDTO.getEndTime()).getTime() + 24 * 60 * 60 * 1000;
			} else {
				endTime = System.currentTimeMillis();
			}
			whereClauseParams.add(new Timestamp(endTime));
			whereClause.append(" and flow.createTime<=? ");
			
			if (!org.apache.commons.lang3.StringUtils.isEmpty(searchDTO.getStartTime())){
				startTime = sdf.parse(searchDTO.getStartTime()).getTime();
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(endTime);
				calendar.add(Calendar.MONDAY, -1);
				startTime = calendar.getTimeInMillis();
			}
			whereClauseParams.add(new Timestamp(startTime));
			whereClause.append(" and flow.createTime>=? ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		FormStatus formStatus = searchDTO.getFormStatus();
		
		if (formStatus != null && formStatus != FormStatus.ALL){
			whereClause.append(" and (");
			int i = 0;
			FlowStatus[] flowStatus = formStatus.transform();
			for(FlowStatus status : flowStatus){
				if (i != 0){
					whereClause.append(" or ");
				}
				whereClause.append(" flow.status=" + status.ordinal() + " ");
				i++;
			}
			whereClause.append(" ) ");
		}
		
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getTitle())){
			whereClause.append(" and content.title like ? ");
			whereClauseParams.add("%" + searchDTO.getTitle() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getFormNum())){
			whereClause.append(" and flow.formnum like ? ");
			whereClauseParams.add("%" + searchDTO.getFormNum() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getLocalType())){
			whereClause.append(" and flow.islocal=? ");
			whereClauseParams.add(searchDTO.getLocalType());

		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getCreator())){
			whereClause.append(" and (creator.empnumber=? or creator.name like ? ) ");
			whereClauseParams.add(searchDTO.getCreator());
			whereClauseParams.add("%" + searchDTO.getCreator() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getCreatCmp())){
			whereClause.append(" and flow.formnum like ? ");
			whereClauseParams.add("%" + searchDTO.getCreatCmp() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getLeader())){
			DescionMaker descionMaker = DescionMaker.valueOf(searchDTO.getLeader());
	    	whereClause.append(" and flow.decionmaker=? ");
    		whereClauseParams.add(descionMaker.ordinal());
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getRePath())){
			whereClause.append(" and creator.deptpath like ? ");
			whereClauseParams.add("%" + searchDTO.getRePath() + "%");
		}
		
		whereClause.append(" order by createtime desc ");
		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapperPlusX();

		String querySQL = selectClause.append(whereClause).toString();

		return getJdbcTemplate().query(querySQL, mapper, whereClauseParams.toArray());
	}
	
	@Override
	public List<MyWork> getJointWorks(Flow flow, WorkStage workStage){
		String sql = "select FLOWTYPE, WORKSTAGE, ID, FLOWID, DEPTNAME, DEPTID, a.CREATETIME, FINISHTIME, OPINION, STATUS, JOINSIGNSTARTID, JOINCYCLE, joinStartEmployeeId, WORKNUM, EMPLOYEEID, "
				+ "POSTCODE, DEPTCODE, A_DEPTCODE, CMPCODE, PARENTID,OLDFLOWID,ORGPATH,HB_CHENGHE,HB_JOINSIGNSTARTID,HB_JOINSTARTEMPLOYEEID,HB_CHENGHEEND,HB_AGREE from ww_work a inner join ( "
				+ " select max(createtime) createtime from WW_WORK t where flowid=:id and workstage=:workstage group by t.joinsignstartid "
				+ " ) b on a.createtime=b.createtime ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", flow.getId());
		map.put("workstage", workStage.ordinal());
		ParameterizedRowMapper<MyWork> mapper = new WorkRowMapper();
		//System.out.println(sql);
		//System.out.println(map);
		//List userTemplates = getJdbcTemplate().query(sql, mapper, map);
		List<MyWork> result = getJdbcTemplate().query(sql, mapper, map);
		if (result != null && result.size() > 0)
			return result;
		else
			return null;
	}
	
	@Override
	public Page confSearchboss(ConfidentialSearchbossDTO searchDTO, int start, int size){
		
		StringBuffer selectClause = new StringBuffer();
		StringBuffer whereClause = new StringBuffer();
		
		selectClause.append("select distinct flow.formnum as formnum, flow.status as status, flow.id as flowId, ");
		selectClause.append(" 1 as employeeId, 1 as postCode, 1 as workId, 1 as deptId, 1 as actualName, ");
		selectClause.append(" flow.flowtype as flowType, flow.isnew, flow.createtime as createtime, ");
		selectClause.append(" flow.decionmaker,flow.islocal, ");
		selectClause.append(" content.title as title, creator.name as creatorName, creator.deptpath, flow.endtime ");
		selectClause.append(" from ww_flow flow ");
		selectClause.append(" left join ww_persondetail creator on flow.id=creator.flowid ");
		selectClause.append(" left join ww_flowcontent content on flow.id=content.flowid ");
		selectClause.append(" left join ww_work work on flow.id=work.flowid ");

		List<Object> whereClauseParams = new ArrayList<Object>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		whereClause.append(" where flow.flowtype=? and flow.templatecreateid is null and creator.type=0  and work.employeeid='91608004' and flow.decionmaker in ('2','4') ");
		whereClauseParams.add(searchDTO.getFlowType().ordinal());
		
		try {
			long startTime = 0L, endTime = 0L;
			if (!org.apache.commons.lang3.StringUtils.isEmpty(searchDTO.getEndTime())){
				endTime = sdf.parse(searchDTO.getEndTime()).getTime() + 24 * 60 * 60 * 1000;
			} else {
				endTime = System.currentTimeMillis();
			}
			whereClauseParams.add(new Timestamp(endTime));
			whereClause.append(" and flow.createTime<=? ");
			
			if (!org.apache.commons.lang3.StringUtils.isEmpty(searchDTO.getStartTime())){
				startTime = sdf.parse(searchDTO.getStartTime()).getTime();
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(endTime);
				calendar.add(Calendar.MONDAY, -1);
				startTime = calendar.getTimeInMillis();
			}
			whereClauseParams.add(new Timestamp(startTime));
			whereClause.append(" and flow.createTime>=? ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		FormStatus formStatus = searchDTO.getFormStatus();
		
		if (formStatus != null && formStatus != FormStatus.ALL){
			whereClause.append(" and (");
			int i = 0;
			FlowStatus[] flowStatus = formStatus.transform();
			for(FlowStatus status : flowStatus){
				if (i != 0){
					whereClause.append(" or ");
				}
				whereClause.append(" flow.status=" + status.ordinal() + " ");
				i++;
			}
			whereClause.append(" ) ");
		}
		
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getTitle())){
			whereClause.append(" and content.title like ? ");
			whereClauseParams.add("%" + searchDTO.getTitle() + "%");
		}	
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getFormNum())){
			whereClause.append(" and flow.formnum like ? ");
			whereClauseParams.add("%" + searchDTO.getFormNum() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getLocalType())){
			whereClause.append(" and flow.islocal=? ");
			whereClauseParams.add(searchDTO.getLocalType());
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getCreator())){
			whereClause.append(" and (creator.empnumber=? or creator.name like ? ) ");
			whereClauseParams.add(searchDTO.getCreator());
			whereClauseParams.add("%" + searchDTO.getCreator() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getCreatCmp())){
			whereClause.append(" and flow.formnum like ? ");
			whereClauseParams.add("%" + searchDTO.getCreatCmp() + "%");
		}
	/*	if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getLeader())){
			DescionMaker descionMaker = DescionMaker.valueOf(searchDTO.getLeader());
			whereClause.append(" and flow.decionmaker=? ");
			whereClauseParams.add(descionMaker.ordinal());
		}  
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getRePath())){
			whereClause.append(" and creator.deptpath like ? ");
			whereClauseParams.add("%" + searchDTO.getRePath() + "%");
		}  */
		
		whereClause.append(" order by " + searchDTO.getOrderBy() + " " + searchDTO.getSorder() + " ");
		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapperPlusX();
		
		Page page = new Page(start, size, 0, size, null);
		String querySQL = selectClause.append(whereClause).toString();

		page = queryWithPage(page, querySQL, mapper, whereClauseParams.toArray());

		return page;
	}
	
	
	@Override
	public List<MyWorkDTO> confSearchbossToExport(ConfidentialSearchbossDTO searchDTO){
		
		StringBuffer selectClause = new StringBuffer();
		StringBuffer whereClause = new StringBuffer();
		
		selectClause.append("select distinct flow.formnum as formnum, flow.status as status, flow.id as flowId, ");
		selectClause.append(" 1 as employeeId, 1 as postCode, 1 as workId, 1 as deptId, 1 as actualName, ");
		selectClause.append(" flow.flowtype as flowType, flow.isnew, flow.createtime as createtime, ");
		selectClause.append(" flow.decionmaker,flow.islocal, ");
		selectClause.append(" content.title as title, creator.name as creatorName, creator.deptpath, flow.endtime ");
		selectClause.append(" from ww_flow flow ");
		selectClause.append(" left join ww_persondetail creator on flow.id=creator.flowid ");
		selectClause.append(" left join ww_flowcontent content on flow.id=content.flowid ");
		selectClause.append(" left join ww_work work on flow.id=work.flowid ");
		
		List<Object> whereClauseParams = new ArrayList<Object>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		whereClause.append(" where flow.flowtype=? and flow.templatecreateid is null and creator.type=0 and work.employeeid='91608004' and flow.decionmaker in ('2','4') ");
		whereClauseParams.add(searchDTO.getFlowType().ordinal());
		
		try {
			long startTime = 0L, endTime = 0L;
			if (!org.apache.commons.lang3.StringUtils.isEmpty(searchDTO.getEndTime())){
				endTime = sdf.parse(searchDTO.getEndTime()).getTime() + 24 * 60 * 60 * 1000;
			} else {
				endTime = System.currentTimeMillis();
			}
			whereClauseParams.add(new Timestamp(endTime));
			whereClause.append(" and flow.createTime<=? ");
			
			if (!org.apache.commons.lang3.StringUtils.isEmpty(searchDTO.getStartTime())){
				startTime = sdf.parse(searchDTO.getStartTime()).getTime();
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(endTime);
				calendar.add(Calendar.MONDAY, -1);
				startTime = calendar.getTimeInMillis();
			}
			whereClauseParams.add(new Timestamp(startTime));
			whereClause.append(" and flow.createTime>=? ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		FormStatus formStatus = searchDTO.getFormStatus();
		
		if (formStatus != null && formStatus != FormStatus.ALL){
			whereClause.append(" and (");
			int i = 0;
			FlowStatus[] flowStatus = formStatus.transform();
			for(FlowStatus status : flowStatus){
				if (i != 0){
					whereClause.append(" or ");
				}
				whereClause.append(" flow.status=" + status.ordinal() + " ");
				i++;
			}
			whereClause.append(" ) ");
		}
		
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getTitle())){
			whereClause.append(" and content.title like ? ");
			whereClauseParams.add("%" + searchDTO.getTitle() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getFormNum())){
			whereClause.append(" and flow.formnum like ? ");
			whereClauseParams.add("%" + searchDTO.getFormNum() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getLocalType())){
			whereClause.append(" and flow.islocal=? ");
			whereClauseParams.add(searchDTO.getLocalType());

		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getCreator())){
			whereClause.append(" and (creator.empnumber=? or creator.name like ? ) ");
			whereClauseParams.add(searchDTO.getCreator());
			whereClauseParams.add("%" + searchDTO.getCreator() + "%");
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getCreatCmp())){
			whereClause.append(" and flow.formnum like ? ");
			whereClauseParams.add("%" + searchDTO.getCreatCmp() + "%");
		}
	/*	if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getLeader())){
			DescionMaker descionMaker = DescionMaker.valueOf(searchDTO.getLeader());
	    	whereClause.append(" and flow.decionmaker=? ");
    		whereClauseParams.add(descionMaker.ordinal());
		}
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(searchDTO.getRePath())){
			whereClause.append(" and creator.deptpath like ? ");
			whereClauseParams.add("%" + searchDTO.getRePath() + "%");
		} */
		
		whereClause.append(" order by createtime desc ");
		ParameterizedRowMapper<MyWorkDTO> mapper = new MyWorkDTORowMapperPlusX();

		String querySQL = selectClause.append(whereClause).toString();

		return getJdbcTemplate().query(querySQL, mapper, whereClauseParams.toArray());
	}
	
}
