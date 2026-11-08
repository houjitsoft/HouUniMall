package com.houjiang.merchant.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class FinanceRefundListVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Integer id;

    @Schema(description = "退款编号")
    private String sn;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "来源单号SN")
    private String orderSn;

    @Schema(description = "总应付款金额")
    private BigDecimal orderAmount;

    @Schema(description = "本次退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退款类型: 1=后台退款")
    private Integer refundType;

    @Schema(description = "退款状态: 0=退款中, 1=退款成功, 2=退款失败")
    private Integer refundStatus;

    private Integer refundWay;

    @Schema(description = "退款类型描述")
    private String refundTypeText;

    @Schema(description = "退款状态描述")
    private String refundStatusText;

    private String refundWayText;

    @Schema(description = "记录时间")
    private String createTime;

}
