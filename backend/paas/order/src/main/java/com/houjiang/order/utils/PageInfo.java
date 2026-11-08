package com.houjiang.order.utils;

import com.houjiang.commons.util.querypage.PageUtils;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 *
 */
@Data
public class PageInfo {

	/**
	 * 列表数据
	 */
	private PageUtils page;

	/**
	 * 会员等级名称
	 */
	private List<String> gradeNameList;

	/**
	 * 会员折扣
	 */
	private List<Integer> hyZheKouList;


}
