package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 退款订单详情
 * @author Amos
 */
@Data
public class ReturnOrderDetailVO {

    /**
     * 退货编号
     */
    private String returnCode;

    /**
     * 退款金额
     */
    private Double returnCash = 0.00;

    /**
     * 供货价
     */
    private Double orderSupplyAmount = 0.00;

    /**
     * 退还佣金(暂时废弃)
     */
    private String returnCommisionFee;

    /**
     * 退款理由
     */
    private String returnReason;

    /**
     * 收货人联系电话
     */
    private String orderReceiverContact;

    /**
     * 收货人地址
     */
    private String orderReceiverAddress;

    /**
     * 添加时间
     */
    private String returnAddTime;

    /**
     * 商家备注
     */
    private String returnShopMessage;

    /**
     * 商家处理时间
     */
    private String returnShopTime;

    /**
     * 商户订单id
     */
    private String orderId;

    /**
     * 买家账号
     */
    private String buyerUserAccount;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 订单商品名
     */
    private String orderGoodsName;

    /**
     * 退货数量
     */
    private String orderGoodsNum;

    /**
     * 退款状态 1未退款，2已退款
     */
    private String returnState;

    /**
     * 支付方式名称
     */
    private String paymentName;

    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 总付款额度
     */
    private String orderPaymentAmount;

    /**
     * 平台留言
     */
    private String returnPlatformMessage;

    /**
     * 商品列表
     */
    private List<GoodsDetailsVO> goodsDetails;

    /**
     * 退还红包
     */
    private String osRedpacketReturnAmount;

    /**
     * 商家处理结果
     * 1-待处理
     * 2-卖家审核通过
     * 3-卖家审核不通过(暂废弃，状态同return_state)
     */
    private Integer returnShopHandle;

    /**
     * 退还红包
     */
    private String returnRptCash;

    /**
     * 支付时间
     */
    private String paymentTime;

    /**
     * 操作人
     */
    private String orderOperator;

    /**
     * 操作时间
     */
    private String orderOperatorTime;

    /**
     * 退款完成时间
     */
    private String returnFinishTime;

    /**
     * 退还会员折扣
     */
    private Double returnMemberDiscount = 0.00;

    /**
     * 退还 供应商折扣等级 的折扣金额
     */
    private Double returnSupperDiscountFee = 0.00;

    /**
     * 退还 供货价
     */
    private Double orderReturnPrice = 0.00;

    /**
     * 退货凭证
     */
    private String returnVoucher;

    /**
     * 退货凭证
     */
    private List<String> returnVouchers;

    /**
     * 订单商品详情
     */
    private List<GoodsDetailsListVO> orderGoodsDetail;

    /**
     * 平台介入状态
     */
    private Integer interventionState;

    /**
     * 说明
     */
    private String returnMessage;

    /**
     * 退款原因id
     */
    private Integer returnReasonId;

    /**
     * 退款方式
     */
    private Integer returnMethodStatus;

    /**
     * 提交次数
     */
    private Integer returnChangeNum;

    /**
     * 撤销次数
     */
    private Integer revokeNum;

    /**
     * 历史状态
     */
    private String historicalReturnStatus;

    /**
     * 提交数量
     */
    private Integer resubmissionsNumber;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expirationTime;

    /**
     * 快递名称
     */
    private String returnShippingCodeName;

    /**
     * 商家拒绝退款次数是否超过上限1:是,0:否
     */
    private Integer numberOfTimesMerchantsRefuseRefunds;

    /**
     *退货物流单号
     */
    private String returnShippingCode;

    /**
     *联系方式
     */
    private String returnMobile;

    /**
     *收货地址
     */
    private String returnAddrName;

    /**
     *退款完成
     */
    private String closeTime;
    /**
     * 商家填写收货地址
     */
    private String returnAddr;
    /**
     * 是否自营
     */
    private String shopSelfSupport;
    /**
     * 收货人姓名
     */
    private String returnRealName;
    /**
     * 收货人手机号
     */
    private String returnNumber;
    /**
     * 1.线上支付  2.线下支付 3.货到付款 4.账期支付
     */
    private Integer paymentCode;

    /**
     * 用户线下支付付款凭证
     */
    private List<String> orderPaymentVoucherList;

    /**
     * 平台退款凭证
     */
    private String returnPaymentVoucher;

    /**
     * 平台退款凭证
     */
    private List<String> returnPaymentVoucherList;

    /**
     * 换货物流信息
     */
    private List<BarterShippingInfoVO> barterShippingInfoList;

    /**
     * 换货信息
     */
    private OrderReturnPlatformVO orderReturnPlatform;
    /**
     * 订单来源:0:自营订单,1:代发平台供应链订单,2:代发怡亚通供应链订单 3 京东VOP
     */
    private Integer orderSource;
}
