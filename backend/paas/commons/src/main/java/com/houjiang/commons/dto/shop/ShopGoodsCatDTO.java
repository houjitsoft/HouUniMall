package com.houjiang.commons.dto.shop;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * goods_cat
 *
 * @author Amos
 */
@Data
public class ShopGoodsCatDTO implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 父类
     */
    private Integer catParentId;

    /**
     * 分类图片
     */
    private String catPic;

    /**
     * 类型id
     */
    private Integer typeId;

    /**
     * 分佣比例
     */
    private BigDecimal catCommission;

    /**
     * 是否允许虚拟
     */
    private Boolean catIsVirtual;

    /**
     * 排序
     */
    private Short catDisplayorder;

    /**
     * 分类级别
     */
    private Integer catLevel;

    /**
     * 是否在前台显示
     */
    private Boolean catShowStatus;

    /**
     * 第一分类父id
     */
    private Integer catFristParentId;

    /**
     * 标注该分类是否是 积分商品的分类 1 是 2 否
     */
    private Integer isIntegralGoods;

    /**
     * 分类别名
     */
    private String catAlias;

    /**
     * 分类id
     */
    private Integer catId;
}
