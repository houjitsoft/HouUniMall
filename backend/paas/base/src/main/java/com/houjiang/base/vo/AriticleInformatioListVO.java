package com.houjiang.base.vo;

import lombok.Data;

/**
 * @ClassName AriticleInformatioListVO
 * @Description 文章信息
 * @Author Amos
 * @Date 2023/6/16 15:57
 * @Version 1.0
 * @author Amos
 */
@Data
public class AriticleInformatioListVO {

    /**
     * 文章id
     */
    private Integer articleId;

    /**
     * 文章标题
     */
    private String extendTitle;

    /**
     * 文章封面
     */
    private String extendCover;

    /**
     * 内容点赞数
     */
    private Integer extendCollect;

    /**
     * 发布者logo
     */
    private String extendPublisherLogo;

    /**
     * 发布者
     */
    private String extendPublisherName;

    /**
     * 发布者id
     */
    private Integer extendPublisherId;

    /**
     * 用户是否点赞1:已点2:未点
     */
    private Integer extendIsLiked = 2;

    /**
     * 文章比例
     */
    private String articleScale;

    /**
     * 上传类型1:图片2视频
     */
    private Integer extendType;
}
