package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ManageSpecValueResultVO
 * @Author Amos
 * @Date 2023/6/27 15:53
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageSpecValueResultVO {
    /**
     * 规格名称值id
     */
    private String specValueId;
    /**
     * 规格值名称
     */
    private String specValueName;
    /**
     *分类id
     */
    private String catId;
    /**
     * 规格id
     */
    private String  specId;
    /**
     * 规格名称
     */
    private  String specName;
    /**
     *商品id
     */
    private String goodsId;
    /**
     * 规格值名字集合
     */
    private List specValueNames;
}
