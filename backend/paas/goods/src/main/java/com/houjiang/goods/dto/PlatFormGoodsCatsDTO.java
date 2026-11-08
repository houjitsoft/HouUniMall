package com.houjiang.goods.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.goods.entity.GoodsAttributeEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分类
 * @author Amos
 */
@Data
public class PlatFormGoodsCatsDTO extends UserTokenBeanDTO {

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 分类父id
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
     * 大宝占类目佣金的比例
     */
    private BigDecimal babyCommission;
    /**
     * 是否设置为虚拟商品分类0是1否
     */
    private Integer catIsVirtual;

    /**
     * 排序
     */
    private Integer catDisplayorder;

    /**
     * 1 2 3  对应分类的一 二 三 级
     */
    private Integer catLevel;

    /**
     * 是否在前台显示1显示0不显示
     */
    private Integer catShowStatus;

    /**
     * 是否是积分分类
     */
    private Integer isIntegralGoods;

    /**
     * 品牌id
     */
    private List<Integer> brandIdList;

    /**
     *商品属性
     */
    private List<GoodsAttributeEntity> goodsAttriParam;

    /**
     * 质保金额度
     */
    private BigDecimal refundRatio;

    /**
     * 退款费用比例
     */
    private Integer refundProportion;

    /**
     * 商品属性组ID
     */
    private Integer groupId;
}


