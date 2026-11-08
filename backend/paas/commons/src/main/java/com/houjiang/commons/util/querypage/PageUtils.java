/**
 * 厚匠科技版权所有
 */

package com.houjiang.commons.util.querypage;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.Serializable;
import java.util.List;

/**
 * 分页工具类
 *
 * @author Amos
 */
public class PageUtils<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 总记录数
	 */
	private int totalCount;
	/**
	 * 每页记录数
	 */
	private int pageSize;
	/**
	 * 总页数
	 */
	private int totalPage;
	/**
	 * 当前页数
	 */
	private int currPage;
	/**
	 * 列表数据
	 */
	private List<T> list;

	/**
	 * 分页
	 * @param list        列表数据
	 * @param totalCount  总记录数
	 * @param pageSize    每页记录数
	 * @param currPage    当前页数
	 */
	public PageUtils(List<T> list, int totalCount, int pageSize, int currPage) {
		this.list = list;
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.currPage = currPage;
		this.totalPage = (int)Math.ceil((double)totalCount/pageSize);
	}
	/**
     * 分页
     */
	public PageUtils(List<T> list, Integer page, Integer limit) {
		this.pageSize = limit;
		this.currPage = page;
		if(page == 0 && limit == -1){
			this.list = list;
		}else {
			// 计算起始索引
			int start = (page - 1) * limit;
			// 截取列表
			this.list = list.subList(start, Math.min(start + limit, list.size()));
		}
		this.totalCount = list.size();
		// 计算总页数
		this.totalPage = (int) Math.ceil((double) list.size() / limit);
	}

	/**
	 * 分页
	 */
	public PageUtils(IPage<T> page) {
		this.list = page.getRecords();
		this.totalCount = (int)page.getTotal();
		this.pageSize = (int)page.getSize();
		this.currPage = (int)page.getCurrent();
		this.totalPage = (int)page.getPages();
	}

	/**
	 * 分页
	 */
	public  static <T> PageUtils<T> getPage(IPage<T> page) {
		return new PageUtils<>(page);
	}
	/**
	 * 分页
	 */
	public static <T> PageUtils<T> getPage(List<T> list, Integer page, Integer limit) {
		return new PageUtils<>(list, page, limit);
	}
	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getCurrPage() {
		return currPage;
	}

	public void setCurrPage(int currPage) {
		this.currPage = currPage;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

}
