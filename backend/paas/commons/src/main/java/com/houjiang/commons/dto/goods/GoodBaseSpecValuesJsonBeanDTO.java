package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName GoodBaseSpecValuesJsonBeanDTO
 * @Description 规格跟着商品走，存储在 goods_base中spec_value 字段
 * @Author Amos
 * @Date 2023/6/26 15:04
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodBaseSpecValuesJsonBeanDTO {

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
}
