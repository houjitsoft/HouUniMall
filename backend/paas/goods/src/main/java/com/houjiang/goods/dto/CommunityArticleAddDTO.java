package com.houjiang.goods.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class CommunityArticleAddDTO {
	/**
	 *心得 视频地址
	 */
	private String resourceUrl;

	/**
	 * 图片
	 */
	private List<String> imageUrl;
	/**
	 * 心得标题
	 */
	private String articleTitle;
	/**
	 * 心得内容
	 */
	private String articleContent;
	/**
	 *心得封面
	 */
	private String articleCover;

	private Integer commonId;
	/**
	 * 会员标签id
	 */
	private Integer tagId;
	/**
	 * 心得id
	 */
	private String articleId;

	private String userId;

	private Integer resourceType;

	private Integer goodsId;

	private String token;

	private Integer catId;
	/**
	 *心得类型:1-图片,2-视频
	 */
	private Integer articleType;


}
