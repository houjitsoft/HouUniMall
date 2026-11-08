package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallShopWithdrawDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "提现单号")
    private String sn;

    @Schema(description = "提现金额")
    private BigDecimal money;

    @Schema(description = "实际金额")
    private BigDecimal realMoney;

    @Schema(description = "手续费")
    private BigDecimal serviceMoney;

    @Schema(description = "手续费比例")
    private BigDecimal serviceRatio;

    @Schema(description = "状态 0申请中 1处理中 2成功 3失败")
    private Integer status;

    @Schema(description = "状态 0申请中 1处理中 2成功 3失败")
    private String statusText;

    @Schema(description = "银行id")
    private Integer bankId;

    @Schema(description = "银行名称")
    private String bankName;

    @Schema(description = "支行名称")
    private String bankBranch;

    @Schema(description = "开户名")
    private String bankUsername;

    @Schema(description = "卡号")
    private String bankCardNumber;

    @Schema(description = "转账凭证")
    private String transferVoucher;

    @Schema(description = "转账时间")
    private Integer transferTime;

    @Schema(description = "转账说明")
    private String transferContent;

    @Schema(description = "商家")
    private MallShopSimplifyDetailVo shop;


}
