package com.houjiang.base.vo.index;


import lombok.Data;

import java.util.List;
/**
 * @ClassName WebIndexArticleGroupVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebIndexArticleGroupVO {

	/**
	 * 自增主键
	 */
	private Integer articleGroupId;

	/**
	 * 标题
	 */
	private String articleGroupTitle;

	/**
	 * 排序
	 */
	private Integer articleGroupSort;

	private List<WebArticleBaseNameVO> articletitle;

	private List<WebArticleBaseNameVO> articletitle2;


}
