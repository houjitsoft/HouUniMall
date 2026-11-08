package com.houjiang.goods.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class CommunityArticleDetailVO {
	/**
	 * 心得id
	 */
	private String articleId;
	/**
	 * 心得内容
	 */
	private String articleContent;

	/**
	 * 点赞ID
	 */
	private Integer articleIsLiked;

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 用户头像
	 */
	private String userAvatar;
	/**
	 * 阅读量
	 */
	private Integer readNum;
	/**
	 * 评论量
	 */
	private Integer remarkNum;
	/**
	 * 点赞数
	 */
	private Integer likeNum;
	/**
	 * 收藏数
	 */
	private Integer starNum;
	/**
	 * 会员标签id
	 */
	private Integer tagId;
	/**
	 *话题内容
	 */
	private String tagContent;

	/**
	 * 商品id
	 */
	private Integer commonId;
	/**
	 * 商品id
	 */
	private Integer goodsId;
	/**
	 * 商品图片
	 */
	private String commonImage;
	/**
	 * 商品价格
	 */
	private BigDecimal commonPrice;

	/**
	 * 商品名
	 */
	private String commonName;
	/**
	 *心得类型:1-图片,2-视频
	 */
	private Integer articleType;

	/**
	 * 图片
	 */
	private List<String> imageUrls;
	/**
	 *心得 视频地址
	 */
	private String resourceUrl;

	/**
	 * 创建时间
	 */
	private String createTime;

	private List<CommunityArticleRemarkEntityVO> mainRemarks;
	/**
	 * 1-代表该心得是自己的；0-则否
	 */
	private Integer isMine;
	/**
	 * 1-代表已经关注该心得作者;0-则否
	 */
	private Integer isFollowed;
	/**
	 * 1-已收藏；0-未收藏
	 */
	private Integer hasStarred;
	/**
	 * 点赞ID
	 */
	private Integer likeId;


	/**
	 * 积分比例
	 */
	private Double pointsRate;


	/**
	 * 现金金额
	 */
	private BigDecimal cashPrice;



}
