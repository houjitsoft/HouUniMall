package com.houjiang.merchant.common.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class UserWithdraw implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "提现单号")
    private String sn;

    @Schema(description = "提现批次")
    private String batchNo;

    @Schema(description = "类型 1钱包余额 2微信零钱 3银行卡 4微信收款码 5支付宝收款码")
    private Integer type;

    @Schema(description = "状态 0待提 1提现 2提现成 3提现失败")
    private Integer status;

    @Schema(description = "提现金额")
    private BigDecimal money;

    @Schema(description = "实际金额")
    private BigDecimal realMoney;

    @Schema(description = "手续费")
    private BigDecimal serviceMoney;

    @Schema(description = "银行")
    private String bankName;

    @Schema(description = "银行支行")
    private String bankSubName;

    @Schema(description = "银行卡账号")
    private String bankAccount;

    @Schema(description = "银行卡姓名")
    private String bankAccountName;

    @Schema(description = "微信账号")
    private String wechatAccount;

    @Schema(description = "微信真实姓名")
    private String wechatAccountName;

    @Schema(description = "微信收款码")
    private String wechatQrCode;

    @Schema(description = "微信转账零钱 支付信息")
    private String wechatPayMessage;

    @Schema(description = "微信转账零钱 结果信息")
    private String wechatPayResult;

    @Schema(description = "微信转账零钱 支付单号")
    private String wechatPayNo;

    @Schema(description = "微信转账零钱 支付时间")
    private Long wechatPayTime;

    @Schema(description = "支付宝账号")
    private String alipayAccount;

    @Schema(description = "支付宝真实姓名")
    private String alipayAccountName;

    @Schema(description = "支付宝收款码")
    private String alipayQrCode;

    @Schema(description = "提现备注")
    private String remark;

    @Schema(description = "审核备注")
    private String auditContent;

    @Schema(description = "转账凭证")
    private String transferVoucher;

    @Schema(description = "转账时间")
    private Long transferTime;

    @Schema(description = "转账备注")
    private String transferContent;

    @Schema(description = "发起微信转账时间")
    private Long wxTransferTime;

    @Schema(description = "微信转账失败原因")
    private String wxTransferFailReason;

    @Schema(description = "微信支付收款页的package信息")
    private String packageInfo;

    @Schema(description = "子状态: 0-无需处理 1-微信待用户收款 2. 微信用户收款操作完成,待微信回调 3.已收微信回调")
    private Integer subStatus;
}