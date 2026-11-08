package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName GdbAttributeAssionParamDTO
 * @Author Amos
 * @Date 2023/6/26 15:11
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbAttributeAssionParamDTO {

    private Integer attributeId;

    /**
     * 属性id
     */
    private Integer attributeContentId;

    /**
     * 商品属性id查
     */
    private String attributeIds;

    /**
     * 属性内容
     */
    private String attributeContentIds;
}
