package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SpecParamJsonBeanDTO
 * @Description 规格：需求 规格跟着商品走 ，新增加字段
 * @Author Amos
 * @Date 2023/6/26 15:13
 * @Version 1.0
 * @author Amos
 */
@Data
public class SpecParamJsonBeanDTO {
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
     * 属性值
     */
    private List<String> paramNameValues;
    /**
     * 是否下架
     */
    private List<Integer> isOffShelf;
}
