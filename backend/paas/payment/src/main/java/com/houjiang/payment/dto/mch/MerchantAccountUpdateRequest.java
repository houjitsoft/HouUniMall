package com.houjiang.payment.dto.mch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 平台商户结算账户更新请求 DTO
 * @author Amos
 */
@Data
public class MerchantAccountUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "商户号不能为空")
    private String mchNo;

    /** 结算账户类型：1-对公 2-对私 */
    @NotNull(message = "结算账户类型不能为空")
    private Integer settleType;

    /** 结算银行编码 */
    private String settleBankCode;

    /** 结算银行名称 */
    @NotBlank(message = "结算银行名称不能为空")
    private String settleBankName;

    /** 开户支行 */
    private String settleBranchName;

    /** 结算户名 */
    @NotBlank(message = "结算户名不能为空")
    private String settleAccountName;

    /** 结算账号 */
    @NotBlank(message = "结算账号不能为空")
    private String settleAccountNo;

    /** 银行预留手机号 */
    private String settleReservedMobile;

    /** 结算周期：0-D0 1-T1 2-T2 3-周结 4-月结 */
    private Integer settleCycle;
}
