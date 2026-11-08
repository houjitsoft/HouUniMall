package com.houjiang.merchant.validate.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class FinanceWalletSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词")
    private String user_info;

    @Schema(description = "类型")
    private Integer change_type;

    @Schema(description = "账户类型: balance=余额, commission=佣金")
    private String account_type;

    @Schema(description = "创建时间")
    private String start_time;

    @Schema(description = "结束时间")
    private String end_time;

}
