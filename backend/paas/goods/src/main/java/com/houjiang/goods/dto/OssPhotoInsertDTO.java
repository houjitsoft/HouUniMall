package com.houjiang.goods.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class OssPhotoInsertDTO extends UserTokenBeanDTO{

    /**
     * 商品id
     */
    private int  goodsId;
    /**
     * 类型id
     */
    private int typeId;
    /**
     * 类型名
     */
    private String typeName;
    /**
     * 报告内容l
     */
    private String reportContent;

    /**
     * 图片集合
     */
    private List<photoUrlDTO>ossparam;


}
