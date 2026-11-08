package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class GoodsAttributeVO {

    /**
     * 属性id
     */
    private Integer id;

    /**
     * 分类
     */
    private Integer catId;

    /**
     * 商品属性内容
     */
    private String commodityAttribute;

    /**
     *属性状态
     */
    private Integer attributeStatus;

    /**
     *属性内容关联id
     */
    private Integer attributeContentId;

    /**
     *属性内容
     */
    private List<String> attributeContent;

    /**
     *属性内容
     */
    private String attriContent;



}
