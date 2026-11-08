package com.houjiang.activity.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 千客千价价格策略配置 DTO
 * @author Amos
 */
@Data
public class ThousandsGuestsPriceConfigDTO implements Serializable {

    private Long id;

    private Long shopId;

    private Long goodsId;

    private Long skuId;

    /**
     * 人群类型：0-全部用户，1-指定用户，2-会员等级，3-标签
     */
    private Integer segmentType;

    /**
     * 人群值：userId / 等级编码 / 标签编码
     */
    private String segmentValue;

    /**
     * 价格类型：1-指定价格，2-折扣百分比，3-减金额
     */
    private Integer priceType;

    /**
     * 价格值
     */
    private BigDecimal priceValue;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 生效开始时间（毫秒时间戳）
     */
    private Long startTime;

    /**
     * 生效结束时间（毫秒时间戳）
     */
    private Long endTime;

    /**
     * 备注
     */
    private String remark;
}
