package com.houjiang.base.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class HoneyCombArticleListVO {

	private Integer articleId;
	/** 内容标题 */
	private String extendTitle;
	/** 封面 */
	private String extendCover;

	/** 推广的内容 */
	private String extendContent;
	/** 相关商品,以英文逗号分割,eg:124,456 */
	private String extendGoodsList;
	/** 商品主图集合，以英文逗号分割 */
	private String extendGoodsImageList;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	/** 文章发布时间 */
	private Date extendPublishTime;
	/** 编辑者id */
	private Integer extendPublisherId;
	/** 发布者 */
	private String extendPublisherName;
	/** 上传类型1:图片2视频 */
	private Integer extendType;
	/** 内容点赞数 */
	private Integer extendCollect;

	/** 商品集合 */
	private List<WapCommonBaseUserFootVO> goodsList;
	/** 图片集合 */
	private List<String> goodsImageList;

	/** 审核状态-1：审核中 2:审核成功 3:审核失败 0:关闭 */
	private Integer extendVerifyStatus;
	/** 审核信息备注 */
	private String extendVerifyReason;

	/** 权重 */
	private Integer displayOrder;

	/** 标签列表 */
	private List<String> labelList;

}
