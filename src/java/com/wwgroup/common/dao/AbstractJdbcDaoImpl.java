package com.wwgroup.common.dao;

import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.dialect.Dialect;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import com.wwgroup.common.Page;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Jdbc方式对于DAO方法的抽象实现，一般的DAO实现方法可以直接继承这个抽象类
 * 
 */

@SuppressWarnings({ "unchecked", "unused" })
public abstract class AbstractJdbcDaoImpl extends JdbcDaoSupport
		implements ApplicationContextAware {

	protected final Logger logger = Logger.getLogger(this.getClass());

	private Dialect dialect;

	private ApplicationContext applicationContext;

	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	/**
	 * 获取数据库dialect
	 */
	public synchronized Dialect getDialect() {
		if (dialect == null) {
			dialect = (Dialect) applicationContext.getBean("defaultDialect");
		}
		return dialect;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * 默认构造器，调用此方法初始化，需要调用setDataSource设置数据源
	 */
	public void AbstractJdbcDAOImpl() {
	}

	/**
	 * 构造器，调用此方法初始化数据源
	 */
	public void AbstractJdbcDAOImpl(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@SuppressWarnings("rawtypes")
	public Page queryWithPage(Page page, String sql,
			ParameterizedRowMapper mapper, Object... args) {
		String countSQL = "select count(*) from ("
				+ sql + " ) ";
		page.setTotalSize(getJdbcTemplate().queryForObject(countSQL, args,Integer.class));
		String querySQL = getDialect().getLimitString(sql,
				page.getCurrentPageSize(), page.getPageSize());
		// 构造rowNum (rownum_ <= ? and rownum_< ?)
		Object[] obj = new Object[args.length + 2];
		for (int i = 0; i < args.length; i++) {
			obj[i] = args[i];
		}
		// 起始行
		obj[args.length] = page.getCurrentPageNo() * page.getPageSize();
		// 结束行
		obj[args.length + 1] = page.getStart() - 1;
		List result = getJdbcTemplate().query(querySQL, mapper, obj);
		page.setCurrentPageSize(result.size());
		page.setResult(result);
		return page;
	}
}
