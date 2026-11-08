package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.util.List;

/**
 * sku规格
 * @author Amos
 */
@Data
public class SpecDTO {

    /**
     * 规格名字
     */
    private String paramName;

    /**
     * 属性值
     */
    private List<String> paramNameValues;

    /**
     * 规格状态
     */
    private List<Boolean> status;
}
