package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分类
 * @author Amos
 */
@Data
public class PlatFormGoodsCatsVO {

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 分类图片
     */
    private String catPic;

    /**
     * 类型
     */
    private Integer typeId;

    /**
     * 分类父id
     */
    private Integer catParentId;

    /**
     * 分佣比例
     */
    private Float catCommission;

    /**
     * 是否允许虚拟0:否
     */
    private Integer catIsVirtual;

    /**
     * 排序
     */
    private Integer catDisplayorder;

    /**
     * 是否在前台显示1显示0不显示
     */
    private Integer catShowStatus;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 是否为虚拟商品 0是1否
     */
    private Integer catIsDummy;

    /**
     * 1 2 3  对应分类的一 二 三 级
     */
    private Integer catLevel;

    /**
     * 商品类型子集集合
     */
    private List<PlatFormGoodsCatsVO> childGoodsCatList;

    /**
     * 是否是积分商品 分类
     */
    private Integer isIntegralGoods;



    private BigDecimal refundRatio;

    private Integer refundProportion;






}
