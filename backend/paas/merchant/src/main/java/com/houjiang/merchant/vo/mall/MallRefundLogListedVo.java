package com.houjiang.merchant.vo.mall;

import com.houjiang.merchant.vo.user.UserVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallRefundLogListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "售后单号")
    private String sn;

    @Schema(description = "金额")
    private BigDecimal money;

    @Schema(description = "退款状态")
    private Integer refundStatus;
    @Schema(description = "退款状态")
    private String refundStatusText;
    @Schema(description = "记录内容")
    private String content;

    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "操作人")
    private Integer operatorId;
    @Schema(description = "操作人类型")
    private Integer operatorType;

}
