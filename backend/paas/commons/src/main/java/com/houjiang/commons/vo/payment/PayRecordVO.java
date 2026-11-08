package com.houjiang.commons.vo.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class PayRecordVO {
    /**
     * 充值记录ID
     */
    private Integer rechargeId;

    /**
     * 充值金额
     */
    private BigDecimal rechargeAmount;

    /**
     * 充值类型
     */
    private Integer rechargeType;

    /**
     * 充值状态
     */
    private Integer rechargeStatus;

    /**
     * 充值时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date rechargeTime;

    /**
     * 记录类型
     */
    private Integer rType;

    /**
     * 资金去向 1:账户余额 2:平台账户
     */
    private Integer fundDestination;

    /**
     * 收款人
     */
    private String payee;

    /**
     * 收款银行
     */
    private String receivingBank;

    /**
     * 收款账号
     */
    private String collectionAccount;

    /**
     * 支付凭证图片
     */
    private String payImg;

    /**
     * 支付凭证图片列表
     */
    private List payImgs;

    /**
     * 支付说明
     */
    private String payExplanation;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 用户ID
     */
    private String userId;
}
