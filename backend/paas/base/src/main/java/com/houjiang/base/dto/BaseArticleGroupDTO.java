package com.houjiang.base.dto;

import lombok.Data;

/**
 * 文章分类
 * @author Amos
 */
@Data
public class BaseArticleGroupDTO {

    /**
     * token
     */
    private String token;

    /**
     * 自增主键
     */
    private Integer articleGroupId;

    /**
     * 标题
     */
    private String articleGroupTitle;

    /**
     * 排序
     */
    private Integer articleGroupSort;


}
