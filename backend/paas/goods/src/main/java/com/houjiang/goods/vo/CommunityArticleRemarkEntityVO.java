package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName CommunityArticleRemarkEntity
 * @Description 心得评论实体类
 * @Author Amos
 * @Date 2022/2/10 14:37
 * @Version 1.0
 * @author Amos
 */
@Data
public class CommunityArticleRemarkEntityVO {
    private Integer remarkId;
    /**
     * 评论内容
     */
    private String remarkContent;
    /**
     * 心得id
     */
    private String articleId;
    /**
     * 被评论ID
     */
    private String remarkedId;

    private String remarkUserName;

    private String remarkUserAvatar;

    private String remarkUser;

    private String remarkedUserName;

    private String remarkedUserAvatar;

    private String remarkedUser;

    private String remarkTime;
    /**
     * 评论层级:1-一级;2-二级'
     */
    private Integer remarkType;

    /**
     * 评论信息
     */
    private List<CommunityArticleRemarkEntityVO> remarkList;

    /**
     * 是否是自己的评论:1-是,0-否
     */
    private Integer ownRemark;

    /**
     * 是否被赞:1-是；0-否
     */
    private Integer isLiked;
    /**
     * 点赞数
     */
    private Integer likeNum;
    /**
     * 点赞ID
     */
    private Integer likeId;
}
