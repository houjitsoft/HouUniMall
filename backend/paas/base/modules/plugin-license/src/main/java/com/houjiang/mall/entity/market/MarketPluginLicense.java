package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 插件市场-授权许可（hou_market_plugin_license）。
 *
 * <p>付费插件（PAID / TRIAL）的安装凭证：本站点必须持有「有效且未过期」的授权，
 * 才允许从市场安装。签发动作属于平台侧（线下收款后发证），租户侧只能激活。
 *
 * <p>表结构见 {@code public/docs/sql/mall.sql} 的「3. 授权许可」段。
 *
 * @author Amos
 */
@Data
@TableName("hou_market_plugin_license")
public class MarketPluginLicense implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String pluginCode;

    /** 购买方/站点 ID，平台自营为 1 */
    private Long buyerId;

    /** 绑定实例（域名/机器码），为空表示未绑定 */
    private String instanceId;

    /** 授权码，租户侧凭此激活 */
    private String licenseKey;

    /** FREE / PAID / TRIAL */
    private String licenseType;

    /** 最大实例数 */
    private Integer maxInstances;

    /** 过期时间，NULL = 永久 */
    private LocalDateTime expireTime;

    /** 官方维护权益截止时间；到期不影响已交付版本继续运行。 */
    private LocalDateTime maintenanceUntil;

    /**
     * 该笔购买折算的服务期限（天）。
     *
     * <p>空 = 按官方政策（每笔 +12 个月）累计维护期；非空则按本列天数累计。
     * 用来兑现价格套餐里的 {@code valid_days}——此前该字段在下单时被丢弃，
     * 导致「30 天版」与「永久版」拿到同样的授权（审计 2026-10-27 P2-2）。
     */
    private Integer termDays;

    /** 1 有效 0 失效（吊销） */
    private Integer status;

    private LocalDateTime createTime;

    /**
     * 授权状态机状态（M5，方案 §7.2）：ISSUED / BOUND / ACTIVATED / EXPIRED / REVOKED / UNBOUND。
     *
     * <p>为什么在 {@code status}（1 有效 / 0 吊销）之外再开一列：
     * {@code status} 是「能不能用」的粗粒度开关，已被 {@code findValid} 固化；
     * 而「签发 → 绑定 → 激活 → 过期」是生命周期，两者正交。
     * 复用 {@code status} 表达生命周期会破坏既有查询语义，因此新增一列。
     */
    private String state;

    /**
     * 首次绑定站点实例时间（M5）。
     */
    private LocalDateTime bindTime;

    /**
     * ★首次成功安装时间 = 试用期起算点（M5，方案 §7.3）。
     *
     * <p>试用期按「首次成功安装」而不是「签发 / 下载」计时：
     * 否则会出现「买了没装却在倒计时」这种典型投诉。
     */
    private LocalDateTime firstInstallTime;

    /**
     * 来源订单号，吊销溯源（M5）。
     */
    private String sourceOrderNo;

    /** Sandbox grants never deliver official artifacts or contribute to paid maintenance. */
    private Integer purchaseSandbox;

    /**
     * 吊销原因（平台 / 退款 / 违规 / 紧急下架）（M5）。
     */
    private String revokeReason;

    /**
     * 运行时最后一次授权心跳时间（M5 只落列，心跳任务未接）。
     */
    private LocalDateTime lastHeartbeat;
}
