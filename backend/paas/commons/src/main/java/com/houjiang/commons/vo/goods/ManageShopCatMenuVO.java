package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName ManageShopCatMenuVO
 * @Description 店铺商品类目
 * @Author Amos
 * @Date 2023/6/21 9:42
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageShopCatMenuVO {

    private Integer catId;

    /**
     *  分类名称
     */
    private String catName;

    /**
     * 父类
     */
    private Integer catParentId;

    /**
     * 类型id
     */
    private Integer typeId;

    /**
     * 分佣比例
     */
    private Float catCommission;

    /**
     * 分类级别
     */
    private Integer catLevel;

    private List<ManageShopCatMenuVO> childMenu;

    /**
     * 商品类型子集集合
     */
    private List<ManageShopCatMenuVO> childGoodsCatList;

    /**
     * 分类图片
     */
    private String catPic;

    /**
     *退款金额
     */
    private BigDecimal refundRatio;

    /**
     *退款比例
     */
    private Integer refundProportion;

    /**
     * 是否允许虚拟0:否
     */
    private Integer catIsVirtual; 

    /**
     * 
     */
    private Integer catDisplayorder;

    /**
     *是否在前台显示1显示0不显示
     */
    private Integer catShowStatus;

    /**
     *类型名称
     */
    private String typeName;

    /**
     *是否为虚拟商品 0是1否
     */
    private Integer catIsDummy;

    /**
     *是否是积分商品 分类
     */
    private Integer isIntegralGoods;

    /**
     * 商品属性组ID
     */
    private Integer groupId;


}
