package com.houjiang.base.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class HoneycombArticleQueryDTO extends PageHelper {

	/** 文章id */
	private Integer articleId;

	/** 文章标题 */
	private String extendTitle;
	/**
	 * 用户id
	 */
	private String userId;

	private Integer totalCount;

}
