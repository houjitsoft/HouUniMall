package com.houjiang.userms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class PlusMemberInfoVO {
    /**
     * 用户id
     */
    private String userId;

    /**
     * plus会员id
     */
    private Integer plusId;

    /**
     *套餐类型 1 月卡 2 季卡 3 年卡
     */
    private Integer thaliType;

    /**
     *剩余红包发放次数
     */
    private Integer redGrantnum;

    /**
     *套餐开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date thaliStarttime;

    /**
     *套餐结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date thaliEndtime;

    /**
     *当月已用额度
     */
    private Integer usedAmount;

    /**
     * 已优惠总额度
     */
    private Integer totalAmount;

    /**
     *创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date creatTime;

    /**
     *获取积分
     */
    private Integer earnPoints;

    /**
     *权益id
     */
    private String plusEquity;

    /**
     *用户名
     */
    private String userName;

    /**
     *套餐状态   0 关闭  1  启用
     */
    private Integer thaliStatus;

    /**
     *上次红包发放月份
     */
    private Integer redGrantmonth;

    /**
     *续费短信是否发送 0 未发送 1 已发送
     */
    private Integer expirationNotice;
}
