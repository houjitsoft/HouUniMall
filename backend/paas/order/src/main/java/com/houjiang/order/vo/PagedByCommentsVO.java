package com.houjiang.order.vo;

import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.commons.util.querypage.PageUtils;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class PagedByCommentsVO extends PageHelper {
	/**
	 * 全部评论数
	 */
	private Integer allOrderNum;
	/**
	 * 好评数
	 */
	private Integer goodTotalCount;
	/**
	 * 中评数
	 */
	private Integer neutralTotalCount;
	/**
	 * 差评数
	 */
	private Integer badTotalCount;
	/**
	 * 晒单数
	 */
	private Integer imageTotalCount;
	/**
	 * 好评率
	 */
	private String rate;
	/**
	 * 每页的显示的数据
	 */
	private List<?> list;
	/**
	 * 每页的显示的数据
	 */
	private PageUtils records;
}
