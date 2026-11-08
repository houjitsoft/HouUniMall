package com.houjiang.commons.vo.shop;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 店铺详情
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShopDetailVO {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 供应商店铺id
     */
    private Integer supplierShopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商家用户名
     */
    private String userName;

    /**
     * 店铺logo
     */
    private String shopLogo;

    /**
     * 商品数量
     */
    private Integer shopCount;

    /**
     * 粉丝数
     */
    private Integer shopCollect;

    /**
     * 店铺logo
     */
    private String wapShopLogo;

    /**
     * 总体得分
     */
    private Double overallScore;

    /**
     * 是否开具增值税发票 1是，0否
     */
    private Integer shopInvoices;

    /**
     * 买家电话
     */
    private String shopTel;

    /**
     * 1:已缴纳2:尚未缴纳3审核中
     */
    private Integer depositStatus;

    /**
     * 是否自营
     */
    private Boolean shopSelfSupport;

    /**
     * 发票
     */
    private Integer invoiceActive;

    /**
     * 是否开启业务员模式
     */
    private Integer isSalesman;

    /**
     * 店铺属性
     */
    private Integer shopGradeId;
    /**
     * 投诉金额
     */
    private BigDecimal amount;

    /**
     * 投诉单号
     */
    private String complaintsId;

    /**
     * 投诉原因编码
     * -1: 充值问题
     * -2: 提现问题
     * -3: 补缴问题
     * 1: 未按时发货
     * 2: 虚假发货
     * 3: 违规出售商品
     * 4: 骚扰辱骂
     * 5: 售后退款
     */
    private Integer reason;

    /**
     * 投诉原因描述
     */
    private String complaintsReason;

    /**
     * 关联订单ID
     */
    private String orderId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date failureTime;

    /**
     * 资金去向
     * 1: 账户余额
     * 2: 平台账户
     */
    private Integer fundDestination;

    /**
     * 充值/支付类型
     * 0: 质保金余额
     * 1: 余额支付
     * 2: 对公转账
     */
    private Integer rechargeType;

    /**
     * 投诉状态
     */
    private Integer complaintsStatus;

    public ShopDetailVO(Integer shopId, String shopName, String shopLogo, Integer shopCount, Integer shopCollect,
                        String wapShopLogo) {
        super();
        this.shopId = shopId;
        this.shopName = shopName;
        this.shopLogo = shopLogo;
        this.shopCount = shopCount;
        this.shopCollect = shopCollect;
        this.wapShopLogo = wapShopLogo;
    }

}
