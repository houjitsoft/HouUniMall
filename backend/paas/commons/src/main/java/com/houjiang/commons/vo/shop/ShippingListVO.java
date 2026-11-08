package com.houjiang.commons.vo.shop;

import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 运费集合
 *
 * @author Amos
 */
@Data
public class ShippingListVO {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 每家店铺商品总数
     */
    private Integer goodsNo;

    /**
     * 店铺红包id
     */
    private String shopLimit;

    /**
     * 每家店铺运费
     */
    private BigDecimal shipping;

    /**
     * 参加多品红包商品数量
     */
    private Integer goodsQuantity;

    /**
     * 各店铺总价(不含运费与优惠价格)
     */
    private BigDecimal shopPrice;
    /**
     * 店铺金额 含运费 减优惠券
     */
    private BigDecimal shopPriceTotal;

    /**
     * 单品红包商品id
     */
    private String goodsCatLimit;

    /**
     * 红包类型，1-新人注册红包，2-普通红包，3-多品红包 4-店铺红包 5-单品多品红包 6-专属红包默认2
     */
    private Integer redpacketTType;

    /**
     * 商品id集合
     */
    private List<Integer> goodsIdList;

    /**
     * 单品红包所属shopid
     */
    private String itemShopId;

    /**
     * 购物津贴shopId
     */
    private String allowanceShopId;

    /**
     * 是否有权限开具发票
     */
    private Integer shopInvoices;

    /**
     * 优惠券价格
     */
    private BigDecimal couponPrice = BigDecimal.ZERO;

    /**
     * 优惠券使用状态
     */
    private Integer voucherStatus;

    /**
     * 发货时长
     */
    private BigDecimal receivingTime;

    /**
     * 参加红包活动店铺总价
     */
    private BigDecimal itemShopPrice;

    /**
     * 参加购物津贴活动店铺总价
     */
    private BigDecimal allowanceShopPrice;

    /**
     * 各店铺总价(去除优惠券价格)
     */
    private BigDecimal shopTotalPrice;

    /**
     * 店铺折扣金额
     */
    private BigDecimal shopDiscount;

    /**
     * 店铺商品信息
     */
    private List<WapShippingQueryResultVO> wsqrList;

    /**
     * 发票ID
     */
    private Integer invoiceId;

    /**
     * 买家留言
     */
    private String buyerMessage;

    /**
     * 分区ID
     */
    private Integer districtId;

    /**
     * 商品供应商店铺id
     */
    private Integer shopDistributorId;

    /**
     * 是否自营店铺
     */
    private Boolean shopSelfSupport;

    /**
     * 店铺满减优惠金额
     */
    private BigDecimal shopManjianDiscount;

    /**
     * 店铺满折优惠金额
     */
    private BigDecimal shopManzheDiscount;

    /**
     * 店铺赠品信息，存放赠品commonId,赠品名称commonName,赠品图片commonImage
     */
    private List<Map<String, String>> shopGiveawayList;

    /**
     * 货品价格集合
     */
    private List<BigDecimal> stepPriceList;

    /**
     * 价格集合
     */
    private List<BigDecimal> hyPriceList;

    /**
     * 商品价格集合
     */
    private List<BigDecimal> goodsPriceList;

    /**
     * 商品原本含税价集合
     */
    private List<BigDecimal> taxIncludedPriceList;

    /**
     * 商品折扣状态集合
     */
    private List<Integer> stepOrHyList;

    /**
     * 商品折扣状态集合
     */
    private List<Integer> promotionIdList;

    /**
     * 订单店铺总税费
     */
    private BigDecimal shopTaxPrice;

    /**
     * 订单店铺总未税价格
     */
    private BigDecimal shopWeiShuiPrice;

    /**
     * 订单店铺总会员折扣价格
     */
    private BigDecimal shopZheKouPrice;

    /**
     * 是否限额
     */
    private Integer accountIsLimit;

    /**
     * 可用金额
     */
    private BigDecimal availableAmount;

    /**
     * 商品小计
     */
    private List<BigDecimal> goodsAmountList;

    /**
     * 折扣比例
     */
    private BigDecimal scale;

    /**
     * 满减活动折扣金额
     */
    private List<BigDecimal> fullReductionDiscountList;

    /**
     * 满折活动折扣金额
     */
    private List<BigDecimal> fullFoldingDiscountList;

    /**
     * 经销折扣
     */
    private BigDecimal distributionDiscount;

    /**
     * 会员折扣集合
     */
    private List<BigDecimal> memberDiscountList;

    /**
     * plus折扣集合
     */
    private List<BigDecimal> plusDiscountList;

    /**
     * 满减折扣集合
     */
    private Map<Integer,BigDecimal> manjianMap;

    /**
     * 满折折扣集合
     */
    private Map<Integer,BigDecimal> manzheMap;

    /**
     * 优惠券Id
     */
    private String voucherId;

    /**
     * 优惠券Id
     */
    private String voucherTitle;

    /**
     * 店铺商品组合活动价
     */
    private BigDecimal goodsCombinationPrice;

    /**
     * 是否有自营店
     */
    private Boolean isHasSelfShop;


    /**
     * 积分支付比例
     */
    private Double pointsRate;



    /**
     * 现金价格
     */
    private BigDecimal totalCashPrice;

}
