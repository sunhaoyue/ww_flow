package com.wwgroup.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 页对象，用于包含数据及分页信息的对象，Page类实现了用于显示分页信息的基本方法，结果数据的类型是List
 * 
 * 
 */

@SuppressWarnings("serial")
public class Page implements Serializable {
	/**
	 * 空页对象
	 */
	public static final Page EMPTY_PAGE = new Page();

	/**
	 * 默认页容量
	 */
	public static final int DEFAULT_PAGE_SIZE = 10;

	/**
	 * 最大页容量
	 */
	public static final int MAX_PAGE_SIZE = 100;

	/**
	 * 每页的记录数
	 */
	private int pageSize = DEFAULT_PAGE_SIZE;

	/**
	 * 当前页第一条数据在数据库中的位置
	 */
	private int start;

	/**
	 * 当前页包含的记录数，currentPageSize <= pageSize
	 */
	private int currentPageSize;

	/**
	 * 总记录数
	 */
	private int totalSize;

	/**
	 * 当前页中存放的返回结果列表
	 */
	@SuppressWarnings("rawtypes")
	private List result;

	/**
	 * 当前页码
	 */
	private int currentPageNo;

	/**
	 * 总页数
	 */
	private int totalPageCount;

	/**
	 * 构造函数，构造出空页面
	 */
	@SuppressWarnings("rawtypes")
	public Page() {
		this(0, 0, 0, DEFAULT_PAGE_SIZE, new ArrayList());
	}

	/**
	 * 构造函数，指定开始记录
	 * 
	 * @param start
	 *            本页数据在数据库中的起始位置
	 */
	@SuppressWarnings("rawtypes")
	public Page(int start) {
		this(start, DEFAULT_PAGE_SIZE, -1, DEFAULT_PAGE_SIZE, new ArrayList());
	}

	/**
	 * 构造函数
	 * 
	 * @param start
	 *            本页数据在数据库中的起始位置
	 * @param currentPageSize
	 *            本页包含的数据条数
	 * @param totalSize
	 *            数据库中总记录条数
	 * @param pageSize
	 *            本页容量
	 * @param data
	 *            本页包含的数据
	 */
	@SuppressWarnings("rawtypes")
	public Page(int start, int currentPageSize, int totalSize, int pageSize,
			List data) {

		this.currentPageSize = currentPageSize;
		this.pageSize = pageSize;
		this.start = start;
		this.totalSize = totalSize;
		this.result = data;

		this.currentPageNo = (start - 1) / pageSize + 1;
		this.totalPageCount = (totalSize + pageSize - 1) / pageSize;

		if (totalSize == 0 && currentPageSize == 0) {
			this.currentPageNo = 1;
			this.totalPageCount = 1;
		}
	}

	/**
	 * 当前页中的记录
	 */
	@SuppressWarnings("rawtypes")
	public List getResult() {
		return this.result;
	}

	/**
	 * 取每页数据容量
	 */
	public int getPageSize() {
		return this.pageSize;
	}

	/**
	 * 是否有下一页
	 */
	public boolean hasNextPage() {
		return this.getCurrentPageNo() < this.getTotalPageCount();
	}

	/**
	 * 是否有前一页
	 */
	public boolean hasPreviousPage() {
		return this.getCurrentPageNo() > 1;
	}

	/**
	 * 获得当前页第一条数据在数据库中的位置
	 */
	public int getStart() {
		return start;
	}

	/**
	 * 获得当前页最后一条数据在数据库中的位置
	 */
	public int getEnd() {
		int end = this.getStart() + this.getCurrentPageSize() - 1;
		if (end < 0) {
			end = 0;
		}
		return end;
	}

	/**
	 * 获取上一页第一条数据在数据库中的位置
	 */
	public int getStartOfPreviousPage() {
		return Math.max(start - pageSize, 1);
	}

	/**
	 * 获取下一页第一条数据在数据库中的位置
	 */
	public int getStartOfNextPage() {
		return start + currentPageSize;
	}

	/**
	 * 获取任一页第一条数据在数据库中的位置，每页条数使用默认值
	 * 
	 * @param pageNo
	 *            页号
	 */
	public static int getStartOfAnyPage(int pageNo) {
		return getStartOfAnyPage(pageNo, DEFAULT_PAGE_SIZE);
	}

	/**
	 * 获取任一页第一条数据在数据库中的位置
	 * 
	 * @param pageNo
	 *            页号
	 * @param pageSize
	 *            页面容量
	 */
	public static int getStartOfAnyPage(int pageNo, int pageSize) {
		int startIndex = (pageNo - 1) * pageSize + 1;
		if (startIndex < 1) {
			startIndex = 1;
		}
		return startIndex;
	}

	/**
	 * 得到相邻一组页的页号
	 * 
	 * @param size
	 *            左右半径范围
	 * @return 相邻一组页的页号
	 */
	/*public int[] getNeighbouringPage(int size) {
		int left = this.currentPageNo - size;
		int right = this.currentPageNo + size;
		int begin = ((left > 0) ? left : 1);
		int end = ((right < this.totalPageCount) ? right : totalPageCount);
		int[] num = new int[end - begin + 1];
		for (int i = 0; i < num.length; i++) {
			num[i] = begin + i;
		}
		return num;
	}*/

	/**
	 * 获得当前页包含的记录数
	 */
	public int getCurrentPageSize() {
		return currentPageSize;
	}

	/**
	 * 取这次查询符合条件的总记录数
	 */
	public int getTotalSize() {
		return this.totalSize;
	}

	/**
	 * 获得当前页码
	 */
	public int getCurrentPageNo() {
		return this.currentPageNo;
	}

	/**
	 * 获得下页页码
	 */
	public int getNextPageNo() {
		return this.currentPageNo + 1;
	}

	/**
	 * 获取上页代码
	 * 
	 * @return
	 */
	public int getPrevPageNo() {
		return hasPreviousPage() ? getCurrentPageNo() - 1 : getCurrentPageNo();
	}

	/**
	 * 取总页码
	 */
	public int getTotalPageCount() {
		return this.totalPageCount;
	}

	@SuppressWarnings("rawtypes")
	public void setResult(List result) {
		this.result = result;
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
		if (this.pageSize > 0) {
			this.totalPageCount = (totalSize + pageSize - 1) / pageSize;
		}
	}

	public void setCurrentPageNo(int currentPageNo) {
		this.currentPageNo = currentPageNo;
	}

	public void setCurrentPageSize(int currentPageSize) {
		this.currentPageSize = currentPageSize;
	}
}
