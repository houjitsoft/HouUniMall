package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName GoodsCatEntity
 * @Description 商品分类表
 * @Author Amos
 * @Date 2023/6/2 15:34
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_cat")
@Data
public class GoodsCatEntity {

    @TableId
    private Integer catId;
    /**
     *  分类名称
     */
    private String  catName;
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
    private Float catCommission;
    /**
     * 是否允许虚拟
     */
    private Integer catIsVirtual;
    /**
     * 排序
     */
    private Integer catDisplayorder;
    /**
     * 分类级别
     */
    private Integer catLevel;
    /**
     * 是否在前台显示
     */
    private Integer catShowStatus;
    /**
     * 大宝占类目佣金的比例
     */
    private BigDecimal babyCommission;
    private BigDecimal bigbabyCommission;
    private Integer catFristParentId;
    /**
     * 标注该分类是否是 积分商品的分类 1 是 0 否
     */
    private Integer isIntegralGoods;
}
