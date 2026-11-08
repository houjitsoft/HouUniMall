package com.houjiang.commons.vo.shop;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 回参类型
 *
 * @author Amos
 */
@Data
public class ShopParaVO {

    /**
     * 红包编码
     */
    private String redpacketId;

    /**
     * 平台优惠券编码
     */
    private String platformVoucherCode;

    /**
     * 平台优惠券价格
     */
    private BigDecimal platformVoucherPrice = BigDecimal.ZERO;

    /**
     * 参与平台优惠券商品id
     */
    private List<Integer> avaliableGoods;

    /**
     * 总价(实付金额+运费)
     */
    private BigDecimal totalPrice;

    /**
     * 总运费(每家店铺运费和)
     */
    private BigDecimal totalShipping;

    /**
     * 红包价格
     */
    private BigDecimal redPacketPrice;

    /**
     * 红包使用状态
     */
    private Integer redPacketStatus;

    /**
     * 优惠方式
     */
    private Integer orderDiscountType;

    /**
     * 优惠券集合
     */
    private List<String> voucherIdList;

    /**
     * 优惠券总价
     */
    private BigDecimal couponTotalPrice;

    /**
     * 全部店铺价格(不含运费与优惠价格)
     */
    private BigDecimal allShopTotalPrice;

    /**
     * 总参加跨店满减商品价格
     */
    private BigDecimal allowancePrice;


    /**
     * 店铺折扣总金额
     */
    private BigDecimal shopTotalDiscount;

    /**
     * 运费信息集合
     */
    private List<ShippingListVO> shippingList;

    /**
     * 使用会员折扣的金额
     */
    private BigDecimal memberDiscountAmount;

    /**
     * 积分商品所需的积分
     */
    private Integer totalIntegral;

    /**
     * 爆品绑定组合折扣
     */
    private BigDecimal hotBindDiscount;

    /**
     * 满减优惠总价
     */
    private BigDecimal totalManjianDiscount;

    /**
     * 满折优惠总价
     */
    private BigDecimal totalManzheDiscount;

    /**
     * 所有赠品信息，存放shopId,commonId,commonName,conmmonImage
     */
    private List<Map<String, String>> giveawayList;

    /**
     * 商品价格
     */
    private BigDecimal hyGoodPrice;

    /**
     * 折扣商品价格
     */
    private BigDecimal stepGoodPrice;

    /**
     * 税费
     */
    private BigDecimal shuiFei;

    /**
     * 折扣成员
     */
    private Integer stepOrMember;

    /**
     * 等级名称
     */
    private List<String> gradeNames;

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
     * 订单未折扣含税价钱
     */
    private Double shopMoprice;

    /**
     * 是否授权账期 0，未授权 1，已授权 2，已冻结
     */
    private Integer isAccount;
    /**
     * 总参加红包商品价格
     */
    private BigDecimal allredPacketPrice;
    /**
     * plus会员折扣总额
     */
    private BigDecimal plusgoodstotal;
    /**
     * plus会员本月已用额度
     */
    private BigDecimal plususedamount;
    /**
     * 没有使用红包的金额 用于订单详情查询可用红包使用
     */
    private BigDecimal noRedpacketAmount;
    /**
     * 骑手配送费
     */
    private BigDecimal pick;

    /**
     * 组合活动类型 1固定组合 2搭配组合
     */
    private Integer combinationType;

    /**
     * 是否有微客商品
     */
    private boolean vkFlag;

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
