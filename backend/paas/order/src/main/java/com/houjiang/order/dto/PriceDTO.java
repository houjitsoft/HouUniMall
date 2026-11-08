package com.houjiang.order.dto;

import com.houjiang.commons.vo.shop.ShippingListVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PriceDTO
 * @Description
 * @Author Amos
 * @Date 2023/9/18 9:26
 * @Version 1.0
 * @author Amos
 */
@Data
public class PriceDTO {
    /**
     * 红包装ID
     */
    String redpacketId;

    /**
     * 平台优惠码
     */
    String platformVoucherCode;

    /**
     * 平台优惠价格
     */
    BigDecimal platformVoucherPrice;

    /**
     * 可用的商品列表，每个元素表示商品ID
     */
    List<Integer> avaliableGoods;

    /**
     * 总价
     */
    BigDecimal totalPrice;

    /**
     * 优惠券ID列表
     */
    List<String> voucherIdList;

    /**
     * 红包价格
     */
    BigDecimal redPacketPrice;

    /**
     * 订单折扣类型
     */
    Integer orderDiscountType;

    /**
     * 优惠券总价
     */
    BigDecimal couponTotalPrice;

    /**
     * 发货清单，每个元素表示一个ShippingListVO对象，包含了发货的相关信息
     */
    List<ShippingListVO> shippingLists;

    /**
     * 满减优惠基数：满XXX元减YYY元（manjianDiscountBase为满减的阈值，manzheDiscountBase为满折的阈值）
     */
    BigDecimal manjianDiscountBase, manzheDiscountBase;

    /**
     * 总抽奖列表，每个元素是一个Map，包含了各种抽奖信息（奖品、数量等）
     */
    List<Map<String, String>> totalGiveawayList;

    /**
     * 店铺的抽奖信息
     */
    List<Map<String,String>> shopGiveawayList;

    /**
     * 抽奖信息
     */
    String giveawayInfo;

    /**
     * 红包状态
     */
    Integer redPacketStatus;

    /**
     * 交易折扣
     */
    BigDecimal tradeDiscount;

    /**
     * 总积分
     */
    Integer totalIntegral;

    /**
     * 积分商品数
     */
    Integer integralGoods;

    /**
     * plushang商品单价
     */
    BigDecimal plusgoodsprice;

    /**
     * plushang商品总价
     */
    BigDecimal plusgoodstotal;

    /**
     * 追加订单数量
     */
    Integer  plusOrder;

    /**
     * 订单的直邮商品数（drop shipping）
     */
    Integer dropShipping;


    /**
     * 订单商品总金额（不包括优惠券和红包）
     */
    BigDecimal orderGoodsAmount;

    /**
     * 红包拆分累加金额
     */
    BigDecimal orderRptPrice;
    /**
     * 购物津贴拆分价格
     */
    BigDecimal platformVoucherSplitPrice;
    /**
     * 红包
     */
    BigDecimal redPacketSplitPrice;



    /**
     * 积分支付比例
     */
    private Double pointsRate;



    /**
     * 现金价格
     */
    private BigDecimal totalCashPrice;



}
