package com.houjiang.commons.vo.goods;

import lombok.Data;
import lombok.ToString;

/**
 * 规格跟着商品走，存储在 goods_base中spec_value 字段
 * @author Amos
 */
@ToString
@Data
public class GoodBaseSpecValuesVO {

    /**
     * 规格名字
     */
    private String paramName;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 是否是公用属性
     */
    private Boolean isCommon;

    /**
     * 公用属性id
     */
    private Integer refSpecId;

    /**
     * 规格值
     */
    private String paramValue;

    private String skuId;

}
