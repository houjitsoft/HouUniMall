package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 插件结算批次（hou_plugin_settlement_batch）。
 *
 * <p>一期一批：把某开发者在一段时间内「待结算」的分账流水汇总成一张批次单，
 * 打款后回填凭证与时间。<b>批次是财务凭据，不随流水变动</b>——
 * 流水之后被驳回（退款）也不改批次金额，而是另起一笔冲抵，保证账实相符。
 *
 * @author Amos
 * @since 2026-10-27
 */
@Data
@TableName("hou_plugin_settlement_batch")
public class PluginSettlementBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 0=待打款 1=已打款 2=已关闭 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_CLOSED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchNo;

    private Long developerId;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private Integer shareCount;

    private BigDecimal grossAmount;

    private BigDecimal platformAmount;

    private BigDecimal developerAmount;

    private String currency;

    /** 0=待打款 1=已打款 2=已关闭 */
    private Integer status;

    private String voucherUrl;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime payTime;
}
