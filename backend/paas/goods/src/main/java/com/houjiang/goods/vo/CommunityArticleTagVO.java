package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName CommunityArticleTagEntity
 * @Description 心得话题实体类
 * @Author Amos
 * @Date 2022/2/10 14:37
 * @Version 1.0
 * @author Amos
 */
@Data
public class CommunityArticleTagVO {
    /**
     * 会员标签id
     */
    private Integer tagId;

    private Integer classId;
    /**
     *话题内容
     */
    private String tagContent;
    /**
     *话题副标题
     */
    private String tagSubtitle;
    /**
     *话题封面
     */
    private String tagCover;
}
