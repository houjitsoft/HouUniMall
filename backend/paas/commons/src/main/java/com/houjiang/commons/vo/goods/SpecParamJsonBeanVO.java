package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName SpecParamJsonBeanVO
 * @Description SpecParamJsonBeanVO
 * @Author Amos
 * @Date 2023/5/25 9:47
 * @Version 1.0
 * @author Amos
 */
@Data
public class SpecParamJsonBeanVO {
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

    private List<Map<String,Object>> paramNameValuesActivity;

    public SpecParamJsonBeanVO(String paramName, Integer sort, List<String> paramNameValues) {
        this.paramName = paramName;
        this.sort = sort;
        this.paramNameValues = paramNameValues;
    }
    public SpecParamJsonBeanVO() {

    }
}
