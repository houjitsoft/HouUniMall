package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * @ClassName GoodBaseSpecValuesJsonBeanVO
 * @Description GoodBaseSpecValuesJsonBeanVO
 * @Author Amos
 * @Date 2023/5/25 9:47
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodBaseSpecValuesJsonBeanVO {
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
