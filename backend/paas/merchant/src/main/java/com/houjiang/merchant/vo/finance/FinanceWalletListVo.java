package com.houjiang.merchant.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class FinanceWalletListVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Integer id;

    @Schema(description = "用户编号")
    private String sn;

    private String account;

    private String mobile;

    private Integer action;

    @Schema(description = "u屏幕估计头皮屑")
    private String avatar;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "变动金额")
    private BigDecimal changeAmount;

    @Schema(description = "剩余金额")
    private BigDecimal leftAmount;

    @Schema(description = "变动类型")
    private String changeType;

    private String changeTypeDesc;

    @Schema(description = "来源单号")
    private String sourceSn;

    @Schema(description = "记录时间")
    private String createTime;

}
