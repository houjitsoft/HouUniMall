package com.houjiang.order.dto;

import com.houjiang.commons.vo.shop.ShippingListVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName PlaceOrderDTO
 * @Description 下单对象
 * @Author Amos
 * @Date 2023/7/3 14:13
 * @Version 1.0
 * @author Amos
 */
@Data
public class PlaceOrderDTO {

    /**
     * 是否选择门店自提 1是 2否
     */
    private Integer orderIsShopTake;

    /**
     * 支付方式
     */
    private Integer payMentCode;

    /**
     * 活动ID
     */
    private String liveId;

    /**
     * 账号id
     */
    private String accountId;

    /**
     * 支付用户id
     */
    private String primaryUserId;

    /**
     * 用户id
     */
    private String token;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 卖家店铺id
     */
    private Integer sellerShopId;

    /**
     * 用户信息
     */
    private UserInfoVO userinfo;

    /**
     * 上级店铺ID
     */
    private Integer parentShopId;

    /**
     * 邀请注册的城市分站ID
     */
    private Integer inviteSubsiteId;

    /**
     * 聚宝店铺id
     */
    private Integer treasureOrderShopId;

    /**
     * 收货人姓名
     */
    private String orderReceiverName;

    /**
     * 收货人地址
     */
    private String orderReceiverAddress;

    /**
     * 收货人联系方式
     */
    private String orderReceiverContact;

    /**
     * 收货人省Id
     */
    private Integer orderReceiverProvinceId;

    /**
     * 收货人市Id
     */
    private Integer orderReceiverCityId;

    /**
     * 收货人区Id
     */
    private Integer orderReceiverAreaId;

    /**
     * 0 是虚拟订单 1 不是虚拟订单
     */
    private Integer virtualGoods;

    /**
     * 有效期
     */
    private Date periodOfValidity;

    /**
     * 过期
     */
    private Integer pastDue;

    /**
     * 订单来源
     */
    private Integer orderFromNum;

    /**
     * 红包编码
     */
    private String redpacketId;

    /**
     * 平台优惠券code
     */
    private String platformVoucherCode;

    /**
     * 平台优惠券金额
     */
    private BigDecimal platformVoucherPrice;

    /**
     *  参与平台优惠券商品id
     */
    private List<Integer> avaliableGoods;

    /**
     * 获取实付总价
     */
    private BigDecimal totalPrice;

    /**
     * 优惠券id集合
     */
    private List<String> voucherIdList;

    /**
     * 获取红包金额
     */
    private BigDecimal redPacketPrice;

    /**
     * 优惠方式
     */
    private Integer orderDiscountType;

    /**
     * 获取总优惠券金额
     */
    private BigDecimal couponTotalPrice;

    /**
     * 运费集合
     */
    private List<ShippingListVO> shippingLists;

    /**
     * 获取满减优惠金额
     */
    private BigDecimal manjianDiscount;

    /**
     * 获取满折优惠金额
     */
    private BigDecimal manzheDiscount;

    /**
     * 赠品信息
     */
    private String giveawayInfo;

    /**
     * 红包状态
     */
    private Integer redPacketStatus;

    /**
     * 贸易折扣
     */
    private BigDecimal tradeDiscount;

    /**
     * 积分商品所需积分之和
     */
    private Integer totalIntegral;

    /**
     * 积分商品 0 是 1否
     */
    private Integer integralGoods;

    /**
     * 支付时间
     */
    private String paymentDate;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 商品价格
     */
    private BigDecimal goodPrice;

    /**
     * 折扣
     */
    private Integer discount;

    /**
     * 会员名称
     */
    private String gradeName;

    /**
     * 商品总价(不包含运费及优惠金额)
     */
    private BigDecimal orderGoodsAmount;

    /**
     * 红包拆分累加金额
     */
    private BigDecimal orderRptPrice;

    /**
     * 购物津贴拆分价格
     */
    private BigDecimal platformVoucherSplitPrice;

    /**
     * 红包
     */
    private BigDecimal redPacketSplitPrice;

    /**
     * 商户订单号
     */
    private String paymentNumber;

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 卖家id
     */
    private String sellerId;

    /**
     * 尺寸A
     */
    private Integer sizeA;

    /**
     * 尺寸B
     */
    private Integer sizeB;

    /**
     * 尺寸C
     */
    private Integer sizeC;

    /**
     * 商品税率
     */
    private Double goodsSl;

    /**
     * 组合活动类型 1固定组合 2搭配组合
     */
    private Integer combinationType;

    /**
     * 分佣比例
     */
    private BigDecimal catCommission;
}
