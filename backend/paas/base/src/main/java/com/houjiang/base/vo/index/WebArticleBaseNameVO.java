package com.houjiang.base.vo.index;

import lombok.Data;
/**
 * @ClassName WebArticleBaseNameVO
 * @Description 网站初始化内容设置
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebArticleBaseNameVO {

	/**
	 *ID
	 */
	private Integer articleId;

	/**
	 *标题
	 */
	/**
	 * 心得标题
	 */
	private String articleTitle;

	/**
	 *组ID
	 */
	private Integer articleGroupId;

	/**
	 *调用网址-url
	 */
	private String articleUrl;

	/**
	 *描述
	 */
	private String articleDesc;

	/**
	 *状态 1:启用  2:关闭
	 */
	private Integer articleStatus;
}
