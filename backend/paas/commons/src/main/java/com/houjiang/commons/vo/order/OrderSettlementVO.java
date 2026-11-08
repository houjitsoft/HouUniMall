package com.houjiang.commons.vo.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * @ClassName OrderSettlementVO
 * @Description
 * @Author Amos
 * @Date 2023/7/18 16:12
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderSettlementVO {

    /**
     * 结算ID
     */
    private String  osId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 记录时间，使用@JsonFormat注解指定日期格式为"yyyy-MM-dd HH:mm:ss"，时区为"GMT+8"
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date recordTime;

    /**
     * 正数或负数，用于表示金额的增加或减少
     */
    private Integer plusOrMinus;

    /**
     * 记录金额
     */
    private BigDecimal recordMoney;

    /**
     * 消费记录ID
     */
    private Integer consumeRecordId;

    /**
     * 订单状态
     */
    private String osState;

    /**
     * 订单开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private java.util.Date osStartDate;

    /**
     * 订单结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private java.util.Date osEndDate;

    /**
     * 订单金额
     */
    private String osOrderAmount;

    /**
     * 运费金额
     */
    private String osShippingAmount;

    /**
     * 订单退款金额
     */
    private String osOrderReturnAmount;

    /**
     * 佣金金额
     */
    private String osCommisAmount;

    /**
     * 佣金退款金额
     */
    private String osCommisReturnAmount;

    /**
     * 店铺成本金额
     */
    private String osShopCostAmount;

    /**
     * 平台支付金额
     */
    private String osPlatformBearAmount;

    /**
     * 平台支付退款金额
     */
    private String osPlatformBearReturnAmount;

    /**
     * 总金额
     */
    private String osAmount;

    /**
     * 订单日期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private java.util.Date osDatetime;

    /**
     * 订单日期
     */
    private String osDate;

    /**
     * 支付日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private java.util.Date osPayDate;

    /**
     * 支付内容
     */
    private String osPayContent;

    /**
     * 店铺ID
     */
    private String shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 订单类型
     */
    private String osOrderType;

    /**
     * 红包金额
     */
    private String osRedpacketAmount;

    /**
     * 红包退款金额
     */
    private String osRedpacketReturnAmount;

    /**
     * 区域ID
     */
    private String districtId;

    /**
     * 直销商金额
     */
    private String osDirectsellerAmount;

    /**
     * 订单优惠金额
     */
    private String osOrderRanderectAmount;

    /**
     * 订单退款优惠金额
     */
    private String osOrderReturnRanderectAmount;

    /**
     * 订单砍价金额
     */
    private String osOrderBargainAmount;

    /**
     * 订单金额
     */
    private String osMoney;

    /**
     * 罚款金额
     */
    private String osFineAmount = "0.00";

    /**
     * 经销商佣金
     */
    private String osDealerCommission;

    /**
     * 经销商退款佣金
     */
    private String osDealerReturnCommission;

    /**
     * 供应商佣金
     */
    private String osSupplyCommission;

    /**
     * 供应商退款佣金
     */
    private String osSupplyReturnCommission;

    /**
     * 基础会员折扣
     */
    private String osBaseMemberDiscount = "0.00";

    /**
     * 退款会员折扣
     */
    private String osReturnMemberDiscount = "0.00";

    /**
     * 推广佣金
     */
    private String osTgCommission = "0.00";

    /**
     * 分销佣金
     */
    private String osFxCommission = "0.00";

    /**
     * 超级折扣费用
     */
    private String osSupperDiscountFee = "0.00";

    /**
     * 退款超级折扣费用
     */
    private String osReturnSupperDiscountFee = "0.00";

    /**
     * 供应商价格
     */
    private String osSupplyPrice = "0.00";

    /**
     * 退款供应商价格
     */
    private String osReturnSupplyPrice = "0.00";

}
