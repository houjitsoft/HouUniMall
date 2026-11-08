package com.houjiang.goods.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName CommunityArticleTagEntity
 * @Description 心得点赞实体类
 * @Author Amos
 * @Date 2022/2/10 14:37
 * @Version 1.0
 * @author Amos
 */
@Data
public class CommunityLikeEntityVO {
    /**
     * 点赞ID
     */
    private Integer likeId;
    /**
     * 心得id
     */
    private String articleId;
    /**
     *心得封面
     */
    private String articleCover;
    /**
     * 被点赞ID
     */
    private String likedId;
    /**
     * 点赞用户
     */
    private String likeUser;
    /**
     * 被点赞用户
     */
    private String likedUser;
    /**
     * 点赞类型:1-心得;2-评论
     */
    private Integer likeType;
    /**
     * 点赞用户名
     */
    private String likeUserName;
    /**
     * 点赞用户头像
     */
    private String likeUserAvatar;
    /**
     * 点赞时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date likeTime;
    /**
     * 心得标题
     */
    private String articleTitle;
    /**
     * 评论内容
     */
    private String remarkContent;
}


