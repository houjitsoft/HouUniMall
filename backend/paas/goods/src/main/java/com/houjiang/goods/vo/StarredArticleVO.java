package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName StarredArticle
 * @Description 收藏心得返回实体类
 * @Author Amos
 * @Date 2022/6/10 14:04
 * @Version 1.0
 * @author Amos
 */
@Data
public class StarredArticleVO {
    /**
     * 心得id
     */
    private String articleId;
    /**
     * 心得标题
     */
    private String articleTitle;
    /**
     *心得封面
     */
    private String articleCover;
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

    private String userId;
    /**
     *心得类型:1-图片,2-视频
     */
    private Integer articleType;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;


}
