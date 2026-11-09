package com.houjiang.userms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员画像聚合结果
 *
 * <p>字段分三类：
 * ① 原始事实——直接来自库表，未做任何加工；
 * ② 计算结果——由 {@code MemberPortraitService} 基于原始事实按公开规则算出（RFM / 分层 / 风险）；
 * ③ 数据可用性——标记该会员的某个维度是否真的有采集数据，避免把「空」误读成「零」。</p>
 *
 * @author Amos
 */
@Data
public class MemberPortraitVO {

    // ==================== ① 会员基础（hou_user_info） ====================

    private Long userId;
    private String nickname;
    private String mobile;
    /** 注册时间 */
    private String registerTime;
    /** 注册终端 */
    private String registerTerminal;
    /** 会员等级ID（hou_user_info.grade_id） */
    private Integer gradeId;
    /** 是否后台账号 */
    private Integer isAdmin;
    /** 累计登录次数（hou_user_info.user_count_login） */
    private Integer loginCount;
    /** 最后登录时间 */
    private String lastLoginTime;
    /** 最后登录IP */
    private String lastLoginIp;

    // ==================== ① 交易事实（hou_order_base） ====================

    /** 有效订单数（order_status NOT IN (1,7)） */
    private Integer orderCount;
    /** 累计支付金额 */
    private BigDecimal orderAmount;
    /** 最近一次下单时间 */
    private String lastOrderTime;
    /** 有下单的天数（按 order_date 去重） */
    private Integer orderDays;
    /** 客单价 */
    private BigDecimal avgOrderAmount;
    /** 全部订单数（含待付款/取消，仅作对照） */
    private Integer orderTotalCount;
    /** 全部订单应付金额（含待付款，不等于实际消费） */
    private BigDecimal orderTotalAmount;
    /** 待付款订单数（order_status = 1） */
    private Integer pendingPayCount;
    /** 待付款金额（未支付，不计入消费） */
    private BigDecimal pendingPayAmount;

    // ==================== ① 浏览事实（hou_goods_user_footprint） ====================

    /** 商品浏览足迹条数 */
    private Integer browseCount;
    /** 最近一次浏览时间 */
    private String lastBrowseTime;
    /** 浏览明细（TOP10，商品名 + 次数） */
    private List<String> browseGoods;

    // ==================== ① 手工标签（hou_base_tag_class + hou_user_tag_info） ====================

    /** 运营手工贴的标签名 */
    private List<String> manualTags;

    /** Mapper 聚合用的标签名串（逗号分隔），Service 转成 manualTags 后置空，不返回前端 */
    private String tagNamesStr;

    // ==================== ① 偏好（hou_order_goods） ====================

    /** 购买次数最多的商品 */
    private String favGoodsName;
    /** 下单最多的店铺 */
    private String favShopName;
    /** 购买过的商品件数 */
    private Integer boughtGoodsKinds;

    // ==================== ② 计算结果 ====================

    /** 分层：core / growth / edge */
    private String layer;
    private String layerLabel;
    /** R / F / M 分值（0-100） */
    private Integer rScore;
    private Integer fScore;
    private Integer mScore;
    /** 距最近一次活跃（下单或登录）的天数；无任何活跃记录时为 null */
    private Integer lastActiveDays;
    /** 流失风险分 0-100 */
    private Integer riskScore;
    private String riskLabel;
    /** 风险成因（逐条可解释） */
    private List<String> riskReasons;
    /** 运营建议（逐条，按真实数据生成） */
    private List<String> advice;

    // ==================== ③ 数据可用性 ====================

    /** 该会员是否有订单数据 */
    private Boolean hasOrderData;
    /** 该会员是否有浏览数据 */
    private Boolean hasBrowseData;
    /** 该会员是否被打过标签 */
    private Boolean hasManualTag;

    // ==================== ④ RFM 八分群（对标有赞/微盟 CRM 口径） ====================

    /** 会员等级名称（hou_base_membership_grade.grade_name，无等级时为 null） */
    private String gradeName;
    /** R/F/M 五分制打分（1-5，0 表示该维度无数据，不参与高/低判定） */
    private Integer rScore5;
    private Integer fScore5;
    private Integer mScore5;
    /** 八分群 key：important_value / important_develop / important_keep / important_retain / general_value / general_develop / general_keep / general_retain / never_bought */
    private String rfmSegment;
    /** 八分群中文名 */
    private String rfmLabel;
    /** 该分群对应的运营动作（按行业通行打法固化，可直接执行） */
    private String rfmAdvice;

    // ==================== ④ 生命周期 ====================

    /** 生命周期 key：newbie / growing / active / silent / churn / inactive */
    private String lifecycle;
    private String lifecycleLabel;

    // ==================== ④ 复购节奏 ====================

    /** 首次有效下单时间 */
    private String firstOrderTime;
    /** 平均复购间隔（天）：仅在有效订单 ≥2 时有意义，否则为 null */
    private Integer orderGapDays;
    /** 首单至今天数 */
    private Integer orderSpanDays;

    // ==================== ⑤ 单客 360 明细（仅 detail 接口返回） ====================

    /** 标签ID（用于打标回显） */
    private List<Integer> tagIds;
    /** 常购商品 TOP：goodsName / cnt */
    private List<java.util.Map<String, Object>> prefGoods;
    /** 常购店铺 TOP：shopName / cnt */
    private List<java.util.Map<String, Object>> prefShop;
    /** 价格带分布：band / cnt / amount */
    private List<java.util.Map<String, Object>> priceBands;
    /** 行为时间线：time / type / title / detail（登录 + 下单真实事件） */
    private List<java.util.Map<String, Object>> timeline;
    /** 品类维度是否可用（hou_order_goods.goods_class_id 是否落库） */
    private Boolean classAvailable;
}

