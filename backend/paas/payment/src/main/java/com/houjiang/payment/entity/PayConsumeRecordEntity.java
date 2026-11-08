package com.houjiang.payment.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
 /**
 * @ClassName
 * @Description  交易明细表-账户收支明细-资金流水表-账户金额变化流水
 * @Author Amos
 * @Date 2023-05-31 15:36:55
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_pay_consume_record")
@Data
public class PayConsumeRecordEntity  implements Serializable {
    /**
     * 交易记录id
     */
    @TableId()
    private String consumeRecordId;
    /**
     * 商户订单id
     */
    private String orderId;
    /**
     * 所属用id
     */
    private String userId;
    /**
     * 昵称
     */
    private String userNickname;
    /**
     * 金额
     */
    private Double recordMoney;
    /**
     * 操作之后的金额
     */
    private Double recordAfterMoney;
    /**
     * 年-月-日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date recordDate;
    /**
     * 年
     */
    private Integer recordYear;
    /**
     * 月
     */
    private Integer recordMonth;
    /**
     * 日
     */
    private Integer recordDay;
    /**
     * 标题
     */
    private String recordTitle;
    /**
     * 描述
     */
    private String recordDesc;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recordTime;
    /**
     * 交易类型(详情pdb_wordbook_pay_trade_type)
     */
    private Integer tradeTypeId;
    /**
     * 1-收款方 2-付款方 3-合并订单
     */
    private Integer userType;
    /**
     * 付款状态
     */
    private Integer recordStatus;
    /**
     * 实际支付单号
     */
    private String recordPayorder;
    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recordPaytime;
    /**
     * 0未删除 1删除
     */
    private Integer recordDelete;
    /**
     * 白条剩余还款金额
     */
    private Double creditRemain;
    /**
     * 金额正负判断(1:+0:-)
     */
    private Integer plusOrMinus;
    /**
     * 是否提现0未提现 1已提现
     */
    private String recordWithdraw;
    /**
     * 佣金金额
     */
    private Double osCommisAmount;
    /**
     * 退款佣金金额
     */
    private Double osCommisReturnAmount;

    private String recordOperator;




     /**
      * 1-用家，2-批发
      */
     private Integer customerType;


     /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date ts;
    /**
     * 兑换积分商品所用的积分
     */
    private Integer totalIntegral;

     /**
      *1-充值;2-提现;3-购物消费;4-转账
      */
     @TableField(exist = false)
     private Integer detailType;

     /**
      * 订单状态
      */
     @TableField(exist = false)
     private Integer orderStatus;

     /**
      * 1-实物；2-虚拟；3-门店
      */
     @TableField(exist = false)
     private Integer orderType;

     /**
      *
      */
     @TableField(exist = false)
     private Integer pastDue;

     /**
      *
      */
     @TableField(exist = false)
     private Integer isCancel;

     /**
      *
      */
     @TableField(exist = false)
     private Integer returnStatus;

     /**
      *
      */
     @TableField(exist = false)
     private Integer refundStatus;

     /**
      *
      */
     @TableField(exist = false)
     private Integer returnFlag;


     /**
      * 金额
      */
     @TableField(exist = false)
     private String money;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
