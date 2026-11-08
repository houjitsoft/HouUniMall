package com.houjiang.payment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName WithdrawRecordVO
 * @Description 提现记录
 * @Author Amos
 * @Date 2023/7/18 15:46
 * @Version 1.0
 * @author Amos
 */
@Data
public class WithdrawRecordVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 描述
     */
    private String con;

    /**
     * 银行名称
     */
    private String bank;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date addTime;

    /**
     * 银行卡号
     */
    private String cardno;

    /**
     * 平台审核备注
     */
    private String remark;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 银行流水账号
     */
    private String bankflow;

    /**
     * 开户人姓名
     */
    private String cardname;

    /**
     * 提现金额
     */
    private BigDecimal amount;

    /**
     * 是否成功0 待审核,1 进行中,2 打款中,3通过,4 不通过
     */
    private Integer isSucceed;

    /**
     * 付款金额
     */
    private BigDecimal payAmount;

    /**
     * 佣金
     */
    private BigDecimal commission;

    /**
     * 结算单号
     */
    private String settlementOrderId;


    /**
     * 是否成功 字符
     */
    private String isSucceedStr;

    /**
     * 管理员
     */
    private String censor;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date checkTime;


    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 用户名
     */
    private String userName;


}
