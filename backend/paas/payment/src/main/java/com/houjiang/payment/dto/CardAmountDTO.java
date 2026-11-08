package com.houjiang.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.Date;

/**
 * 卡金额
 * @author Amos
 */
@Data
public class CardAmountDTO extends PageHelper {

    /**
     * 商户订单id
     */
    private String orderId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 卡状态：0未激活,1激活未充值,2激活并充值
     */
    private Integer status;

    /**
     * 1,购物;2,转账;3,充值;4,提现;5,退款;10,佣金;11,结算;12,蜂卡充值;13,蜂卡收益
     */
    private Integer tradeTypeId;

    /**
     * 查询开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDate;

    /**
     * 查询结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;

    /**
     * 卡片账号
     */
    private String cardCode;

    /**
     * 昵称（手机号）
     */
    private String userNickname;

    /**
     * 标记1刷单0真实
     */
    private Integer parentId;

}
