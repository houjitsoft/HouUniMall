package com.houjiang.payment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName AssetInfoEntity
 * @Description 我的余额记录-实体类
 * @Author Amos
 * @Date 2022/8/10 16:10
 * @Version 1.0
 * @author Amos
 */
@Data
public class AssetInfoVO {

    /**
     * id
     */
    private Integer detailId;

    /**
     * 明细类型:0-默认;1-余额充值;2-订单退款;3-提现失败;4-余额提现;5-余额转账;6-购物消费;7-购物卡充值;8-余额扣款
     */
    private Integer detailType;

    /**
     * 金额
     */
    private BigDecimal assetAmount;

    /**
     * 来源/去向
     */
    private String assetDirection;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 订单状态 1.待付款 2.已付款 3.待发货 4.已发货
     * 5.已签收 6.确认收货 7.取消 8.退款中 9.已退款 11.待自提
     * 12 虚拟订单已过期
     */
    private Integer orderStatus;

    /**
     * 退款标识:0-默认；1-已退款；2-部分退款
     */
    private Integer returnFlag;

    /**
     * 资产类型:1-收入；2-支出
     */
    private Integer assetType;

    /**
     * 备注
     */
    private String detailComment;

    /**
     * 退货状态:0是无退货,1是退货中,2是退货完成，3退货不通过
     */
    private Integer returnStatus;

    /**
     * 退款状态：0：无退款 1：退款中 2：退款完成  3：商家拒绝退款
     */
    private Integer refundStatus;

    /**
     * 收货人名称
     */
    private String receiverName;

    /**
     * 收货人银行卡号
     */
    private String receiverBankNo;

    /**
     * 收货人银行名称
     */
    private String receiverBankName;

    /**
     * 0-待审核,1-进行中,2-打款完成,3-通过,4-不通过
     */
    private Integer auditStatus;
    /**
     * 审核状态中文描述
     */
    private String auditStatusDesc;

    /**
     * 审核备注
     */
    private String auditComment;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 1-实物；2-虚拟；3-门店
     */
    private Integer orderType;

    /**
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;

    /**
     * 是否核销0是1否是否核销0是1否
     */
    private Integer isCancel;

    /**
     * 支付单号
     */
    private String paymentNo;


}
