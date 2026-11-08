package com.houjiang.base.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName LikeArticleVO
 * @Description
 * @Author Amos
 * @Date 2023/6/16 16:00
 * @Version 1.0
 * @author Amos
 */
@Data
public class LikeArticleVO {

    /**
     * 标签信息
     */
    private List<HoneycombLabelVO> labelList;

    /**
     * 文章id
     */
    private Integer articleId;
}
