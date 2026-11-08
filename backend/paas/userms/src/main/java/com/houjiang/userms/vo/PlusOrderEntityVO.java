package com.houjiang.userms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class PlusOrderEntityVO {

    /**
     *用户id
     */
    private String userId;

    /**
     *充值订单
     */
    private String plusOrderid;

    /**
     *充值金额
     */
    private BigDecimal payPrice;

    /**
     *支付方式 1-余额支付，2-购物卡，3-微信支付，4 支付宝 5 平台赠送
     */
    private Integer payType;

    /**
     *开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     *结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     *套餐类型 1 月卡 2 季卡 3 年卡
     */
    private Integer thaliType;

    /**
     *创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date creatTime;

    /**
     *用户名
     */
    private String userName;

    /**
     *用户手机号
     */
    private String userMobile;

    /**
     *状态 0 未生效 1 已生效
     */
    private Integer renewStatus;
}
