package com.houjiang.userms.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员画像圈人条件（智能分群 / 人群包共用）
 *
 * <p>全部条件与列表页展示的真实字段一一对应，不做隐藏换算：看到的即筛的。
 * 任意条件留空表示不限制。</p>
 *
 * @author Amos
 */
@Data
public class PortraitSegmentDTO {

    /** 昵称 / 手机号 / 用户ID */
    private String keyword;

    /** 价值分层：core / growth / edge */
    private String layer;

    /** RFM 八分群 key */
    private String rfmSegment;

    /** 生命周期 key */
    private String lifecycle;

    /** 有效订单数范围 */
    private Integer minOrders;
    private Integer maxOrders;

    /** 累计有效消费范围 */
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    /** 待付款笔数下限（催付场景用，>0 即「有单未付」） */
    private Integer minPending;

    /** 距最近一次登录/活跃的天数下限（沉默/流失场景用） */
    private Integer inactiveDays;

    /** true 只看下过单的，false 只看从未下单的，null 不限 */
    private Boolean hasOrder;

    /** true 只看已打标的，false 只看未打标的，null 不限 */
    private Boolean hasTag;

    /** 必须命中的标签ID（可多选） */
    private List<Integer> tagIds;

    /** 页码，从 1 开始 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 20;
}
