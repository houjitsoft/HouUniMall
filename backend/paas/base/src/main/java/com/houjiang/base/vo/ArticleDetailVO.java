package com.houjiang.base.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName ArticleDetailVO
 * @Description 文章详情
 * @Author Amos
 * @Date 2023/6/16 15:49
 * @Version 1.0
 * @author Amos
 */
@Data
public class ArticleDetailVO {

    /**
     * 文章ID
     */
    private Integer articleId;

    /**
     * 内容标题
     */
    private String extendTitle;

    /**
     * 内容点赞数
     */
    private Integer extendCollect;

    /**
     * 推广的内容
     */
    private String extendContent;

    /**
     * 相关商品,以英文逗号分割,eg:124,456
     */
    private String extendGoodsList;

    /**
     * 商品主图集合，以英文逗号分割
     */
    private String extendGoodsImageList;

    /**
     * 商品集合
     */
    private List<WapCommonBaseUserFootVO> goodsList;

    /**
     * 图片集合
     */
    private List<String> goodsImageList;

    /**
     * 1已点2未点
     */
    private Integer extendIsLiked = 2;

    /**
     * 上传类型1:图片2视频
     */
    private Integer extendType;

    /**
     * 文章发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date extendPublishTime;

    /**
     * 编辑者id
     */
    private Integer extendPublisherId;

    /**
     * 发布者
     */
    private String extendPublisherName;

    /**
     * 发布者头像
     */
    private String extendPublisherLogo;

    /**
     * 点赞记录id
     */
    private Integer collectId;

    /**
     * 查询标签
     */
    private List<HoneycombLabelVO> labelList;

    /**
     * 查询相似文章
     */
    private List<AriticleInformatioListVO> likeArticles;

    /**
     * 审核状态-1：审核成功 2:审核中 3:审核失败 0:关闭
     */
    private Integer extendVerifyStatus;

    /**
     * 封面
     */
    private String extendCover;
}
