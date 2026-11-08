-- ============================================================
-- 2026-09-16 迁移：历史同名表冲突（生产库 meoc 实测）
-- 现象：POST /web/plugin-center/market/order/create 返回
--       SQLSyntaxErrorException: Unknown column 'license_type' in 'field list'
-- 根因：目标库已存在同名但字段完全不同的历史表（分账语义：
--       seller_id / amount_cents / platform_fee_cents / seller_income_cents / pay_status / pay_channel），
--       而本脚本用 CREATE TABLE IF NOT EXISTS，旧表存在时整段被静默跳过，
--       MarketPluginOrder 实体字段与旧表不匹配，insert 报未知列。
-- 处理：确认旧表无任何代码引用且行数为 0 后，先改名备份，再执行下方建表语句。
-- ============================================================
-- RENAME TABLE hou_market_plugin_order TO hou_market_plugin_order_legacy_bak_20260916;

-- 插件市场在线订单（hou_market_plugin_order）
-- 背景：此前收费链路为「线下收款 → 平台手工签发授权码 → 租户激活」，系统内不落订单。
-- 本表承载在线购买闭环：下单 → 调用已有收银台支付 → 支付成功自动签发并绑定授权。
--
-- 幂等约定：order_no 唯一，避免重复下单；cashier_no / pay_order_id 唯一，避免同一支付流水重复发证。

CREATE TABLE IF NOT EXISTS hou_market_plugin_order (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(64)  NOT NULL COMMENT '插件订单号（业务主键，唯一）',
    plugin_code     VARCHAR(64)  NOT NULL COMMENT '插件编码',
    version         VARCHAR(32)  NULL     COMMENT '购买目标版本，为空表示最新版本',
    license_type    VARCHAR(16)  NOT NULL COMMENT 'PAID 付费 / TRIAL 试用',
    valid_days      INT          NULL     COMMENT '授权有效天数，NULL 或 <=0 表示永久',
    amount          DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '应付金额（元）',
    currency        VARCHAR(8)   NOT NULL DEFAULT 'CNY' COMMENT '币种',
    buyer_id        BIGINT       NOT NULL DEFAULT 1 COMMENT '购买方站点 ID，平台自营为 1',
    instance_id     VARCHAR(128) NULL     COMMENT '站点实例（域名），支付成功后授权绑定到此实例',
    pay_method      VARCHAR(32)  NULL     COMMENT '支付方式：WXPAY / ALIPAY 等',
    terminal        VARCHAR(16)  NULL     COMMENT '终端：PC / H5 / APP',
    cashier_no      VARCHAR(64)  NULL     COMMENT '收银台流水号（order 侧）',
    pay_order_id    VARCHAR(64)  NULL     COMMENT '渠道支付单号',
    pay_data        TEXT         NULL     COMMENT '下单返回的支付参数（二维码/表单）JSON',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已关闭 3已退款',
    paid_time       DATETIME     NULL     COMMENT '支付完成时间',
    expire_time     DATETIME     NULL     COMMENT '支付截止时间（超时未付自动关闭）',
    license_id      BIGINT       NULL     COMMENT '支付成功后自动签发的授权 ID',
    license_key     VARCHAR(128) NULL     COMMENT '自动签发的授权码（冗余便于展示）',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_cashier_no (cashier_no),
    KEY idx_plugin_instance (plugin_code, instance_id),
    KEY idx_instance_status (instance_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件市场在线订单';
