package com.houjiang.merchant.common.entity;

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
public class RefundLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "编号")
    private String sn;

    @Schema(description = "退款记录ID")
    private Integer recordId;

    @Schema(description = "关联用户ID")
    private Integer userId;

    @Schema(description = "处理管理ID")
    private Integer handleId;

    @Schema(description = "总应付的金额")
    private BigDecimal orderAmount;

    @Schema(description = "本次退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退款状态: [0=退款中, 1=退款成功, 2=退款失败]")
    private Integer refundStatus;

    @Schema(description = "退款信息")
    private String refundMsg;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

}
