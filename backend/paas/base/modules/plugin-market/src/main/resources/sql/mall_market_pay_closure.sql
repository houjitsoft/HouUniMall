-- ============================================================
-- 收费插件商业闭环（下单 → 支付 → 回调 → 发证 → 安装/卸载清算）
--
-- 执行方式：在业务库（meoc）执行本文件。
-- 重复执行：MySQL 不支持 ADD COLUMN IF NOT EXISTS，已存在的列会报
--           "Duplicate column name 'xxx'" —— 逐条确认后忽略该行即可，
--           建表语句带 IF NOT EXISTS，可安全重跑。
--
-- 【执行前必读】
--   1. 本脚本只加列 / 加表，不删数据、不改既有列类型，可在线执行；
--   2. hou_market_plugin_order 的历史行 status 语义不变（0待支付 1已支付 2已关闭 3已退款），
--      新增 4=支付失败；
--   3. 线上执行前先备份：
--      CREATE TABLE hou_market_plugin_order_bak_$(date +%Y%m%d) AS SELECT * FROM hou_market_plugin_order;
-- ============================================================

-- 1. 订单表扩展：套餐 / 渠道 / 沙箱标记 / 失败原因 / 回调计数
ALTER TABLE hou_market_plugin_order
    ADD COLUMN plan_code    VARCHAR(32)  NULL     COMMENT '价格套餐编码（pay.plans.<plugin>.<plan>）' AFTER license_key,
    ADD COLUMN channel      VARCHAR(32)  NULL     COMMENT '收款渠道：CASHIER / WECHAT_V3 / ALIPAY / MOCK' AFTER plan_code,
    ADD COLUMN sandbox      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否沙箱（模拟支付）订单：1=模拟支付，生产应为 0' AFTER channel,
    ADD COLUMN fail_reason  VARCHAR(500) NULL     COMMENT '支付失败 / 拒收原因' AFTER sandbox,
    ADD COLUMN notify_count INT          NOT NULL DEFAULT 0 COMMENT '已收到回调次数（含重复回调）' AFTER fail_reason,
    ADD COLUMN notify_time  DATETIME     NULL     COMMENT '首次收到有效回调的时间' AFTER notify_count;

-- status 枚举补充：4=支付失败（渠道通知失败 / 用户取消，可重新发起支付）
-- 说明：仅约定语义，列本身无需变更（TINYINT 已覆盖）。

-- 2. 支付回调流水：幂等去重 + 对账留痕
CREATE TABLE IF NOT EXISTS hou_market_plugin_pay_notify (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no    VARCHAR(64)  NULL     COMMENT '我方订单号（订单不存在时为 NULL，仅留痕）',
    channel     VARCHAR(32)  NULL     COMMENT '渠道编码',
    notify_id   VARCHAR(128) NULL     COMMENT '渠道通知唯一 ID，与 order_no 组成唯一键做幂等',
    trade_no    VARCHAR(128) NULL     COMMENT '渠道支付单号',
    result      VARCHAR(32)  NOT NULL COMMENT 'SUCCESS/FAILED/REFUNDED/REJECTED/DUPLICATED/LATE_PAID',
    raw_status  VARCHAR(64)  NULL     COMMENT '渠道原始状态串',
    amount      DECIMAL(12,2) NULL    COMMENT '回调金额（元），用于核对是否被篡改',
    raw         TEXT         NULL     COMMENT '原始报文（截断 4000）',
    message     VARCHAR(500) NULL     COMMENT '处理说明 / 拒绝原因',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_notify (order_no, notify_id),
    KEY idx_order (order_no),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件支付回调流水（幂等与对账）';

-- ============================================================
-- 验证语句（执行后自查）
--   SHOW COLUMNS FROM hou_market_plugin_order LIKE 'plan_code';
--   SHOW COLUMNS FROM hou_market_plugin_order LIKE 'sandbox';
--   SHOW TABLES LIKE 'hou_market_plugin_pay_notify';
--   SELECT order_no, status, sandbox, channel FROM hou_market_plugin_order ORDER BY id DESC LIMIT 5;
-- ============================================================
