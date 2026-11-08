package com.houjiang.commons.vo.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GdbMerchantGoodsCatVO {
    /**
     * 分类ID
     */
    private Integer catId;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 父分类ID
     */
    private Integer catParentId;

    /**
     * 分类图片
     */
    private String catPic;

    /**
     * 类型ID
     */
    private Integer typeId;

    /**
     * 分类佣金
     */
    private Float catCommission;

    /**
     * 是否为虚拟分类
     */
    private Boolean catIsVirtual;

    /**
     * 分类显示顺序
     */
    private Short catDisplayorder;

    /**
     * 分类层级
     */
    private Boolean catLevel;

    /**
     * 分类显示状态
     */
    private Boolean catShowStatus;



}