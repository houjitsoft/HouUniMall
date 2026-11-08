package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class GoodSpecValuesJsonBeanVO {
    /**
     * 规格名字
     */
    private String paramName;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 规格值
     */
    private List<String> paramNameValues;

}
