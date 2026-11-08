package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName CommunityArticleSmallEntity
 * @Description 用于发现好友中的心得数据封装
 * @Author Amos
 * @Date 2022/6/22 09:28
 * @Version 1.0
 * @author Amos
 */
@Data
public class CommunityArticleSmallEntityVO {
    /**
     * 心得id
     */
    private String articleId;
    /**
     * 心得内容
     */
    private String articleContent;
    /**
     *心得封面
     */
    private String articleCover;
    /**
     *心得类型:1-图片,2-视频
     */
    private Integer articleType;
}
