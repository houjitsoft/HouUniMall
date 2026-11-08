package com.houjiang.order.dto;

import lombok.Data;

import java.util.List;

/**
 * 添加评论所需参数
 *
 * @author Amos
 */
@Data
public class CommentsDTO {

    /**
     * 商品公共表id
     */
    private Integer commonId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 敏感词过滤
     */
    private String feedback;

    /**
     * 分数
     */
    private Integer scores;

    /**
     * 是否匿名评价 0 不匿名 1  匿名
     */
    private Integer isanonymous;

    /**
     * 图片集合
     */
    private List<String> imageList;


}
