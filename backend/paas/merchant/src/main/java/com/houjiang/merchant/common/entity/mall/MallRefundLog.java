package com.houjiang.merchant.common.entity.mall;

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
public class MallRefundLog implements Serializable {

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

    @Schema(description = "退款id")
    private Integer refundId;

    @Schema(description = "售后单号")
    private String sn;

    @Schema(description = "金额")
    private BigDecimal money;

    @Schema(description = "退款状态")
    private Integer refundStatus;
    @Schema(description = "记录内容")
    private String content;

    @Schema(description = "操作人")
    private Integer operatorId;
    @Schema(description = "操作人类型")
    private Integer operatorType;
}