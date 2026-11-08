package com.houjiang.order.dto;

import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName PlaceCirculateWithinDTO
 * @Description 下单内循环参数
 * @Author Amos
 * @Date 2023/7/4 10:04
 * @Version 1.0
 * @author Amos
 */
@Data
public class PlaceCirculateWithinDTO {

    /**
     * 获取运费总额
     */
    private BigDecimal totalGoodsShippingFee;

    /**
     * 活动状态
     */
    private Integer activityStatus;

    /**
     * 订单使用
     */
    private ActivityGoodsDetailDTO agd;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;

    /**
     * 记录每次红包拆分金额
     */
    private BigDecimal couponSumMoney;

    /**
     * 活动ID
     */
    private Integer promotionId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品ID sku
     */
    private Integer commonId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 货物装载
     */
    private BigDecimal goodsAmount;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 货物运费
     */
    private BigDecimal goodsShippingFee;

    /**
     * 商品类型
     */
    private Integer s;

    /**
     * 订单状态
     */
    private Integer orderGoodsStatus;

    /**
     * 评价状态
     */
    private Integer orderGoodsEvaluationStatus;

    /**
     * 退款状态
     */
    private Integer goodsReturnStatus;

    /**
     * 退货状态
     */
    private Integer goodsRefundStatus;

    /**
     * 订单货物ID交货
     */
    private Integer orderGoodsIdDeliver;

    /**
     * goods表红包抵扣金额
     */
    private BigDecimal goodsRtpPrice;

    /**
     * 获取满减优惠金额
     */
    private BigDecimal manjianDiscount;

    /**
     * 获取满折优惠金额
     */
    private BigDecimal manzheDiscount;

    /**
     * 商品价格
     */
    private BigDecimal rebatePrice;

    /**
     * 版本id
     */
    private Integer verId;

}
