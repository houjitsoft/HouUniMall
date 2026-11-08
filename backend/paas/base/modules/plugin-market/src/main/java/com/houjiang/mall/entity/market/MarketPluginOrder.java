package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 插件市场在线订单（hou_market_plugin_order）。
 *
 * <p>承载在线购买闭环：下单 → 调用已有收银台支付 → 支付成功<b>自动签发并绑定授权</b>，
 * 取代原先「线下收款 → 平台手工发证 → 租户激活」的人工链路。
 *
 * <p>表结构见 {@code backend/paas/mall/src/main/resources/sql/mall_market_order_tables.sql}。
 *
 * @author Amos
 */
@Data
@TableName("hou_market_plugin_order")
public class MarketPluginOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 待支付 */
    public static final int STATUS_PENDING = 0;

    /** 已支付（已自动发证） */
    public static final int STATUS_PAID = 1;

    /** 已关闭（超时或主动取消） */
    public static final int STATUS_CLOSED = 2;

    /** 已退款（授权同时吊销） */
    public static final int STATUS_REFUNDED = 3;

    /** 支付失败（渠道明确返回失败 / 用户取消），可重新发起支付 */
    public static final int STATUS_PAY_FAILED = 4;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号（业务主键） */
    private String orderNo;

    private String pluginCode;

    /** 购买目标版本，为空表示最新版本 */
    private String version;

    /** PAID / TRIAL */
    private String licenseType;

    /** 授权有效天数，null 或 <=0 表示永久 */
    private Integer validDays;

    /** 应付金额（元） */
    private BigDecimal amount;

    private String currency;

    /** 购买方站点 ID */
    private Long buyerId;

    /** 站点实例（域名），支付成功后授权绑定到此实例 */
    private String instanceId;

    /** 支付方式：WXPAY / ALIPAY */
    private String payMethod;

    /** 终端：PC / H5 / APP */
    private String terminal;

    /** 收银台流水号 */
    private String cashierNo;

    /** 渠道支付单号 */
    private String payOrderId;

    /** 下单返回的支付参数 JSON */
    private String payData;

    private Integer status;

    private LocalDateTime paidTime;

    private LocalDateTime expireTime;

    /** 自动签发的授权 ID */
    private Long licenseId;

    /** 自动签发的授权码 */
    private String licenseKey;

    /**
     * 价格套餐编码（对应 {@code pay.plans.<pluginCode>.<planCode>}）。
     * 为空表示按商品表标价下单。
     */
    private String planCode;

    /** 收款渠道：CASHIER / WECHAT_V3 / ALIPAY / MOCK */
    private String channel;

    /** 是否沙箱（模拟支付）订单：沙箱订单支付的授权不得用于生产环境 */
    private Integer sandbox;

    /** 支付失败 / 拒绝原因（供前端与对账展示） */
    private String failReason;

    /** 已收到的回调次数（含重复回调），对账与排查重复通知用 */
    private Integer notifyCount;

    /** 首次收到有效回调的时间 */
    private LocalDateTime notifyTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
