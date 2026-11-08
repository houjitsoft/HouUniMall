package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * @ClassName CommunityArticleVO
 * @Description 文章信息类
 * @Author Amos
 * @Date 2023/6/9 13:44
 * @Version 1.0
 * @author Amos
 */
@Data
public class CommunityArticleVO {

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 用户ID
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
     * 文章标题
     */
    private String articleTitle;

    /**
     * 文章内容
     */
    private String articleContent;

    /**
     * 文章封面
     */
    private String articleCover;

    /**
     * 文章类型
     */
    private Integer articleType;

    /**
     * 阅读数
     */
    private Integer readNum;

    /**
     * 评论数
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
     * 审核状态
     */
    private Integer auditState;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 公共ID
     */
    private String commonId;

    /**
     * 标签ID
     */
    private Integer tagId;

    /**
     * 是否已关注
     */
    private Integer hasFollowed;

    /**
     * 关注数
     */
    private Integer followerNum;
}
