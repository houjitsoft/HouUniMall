-- ============================================================
-- 插件支付配置落库（hou_market_pay_config）
--
-- 背景：支付参数原先只能写在 Nacos mall.yaml / 环境变量里，运营改一次价格
--       就要改配置并重启服务。本脚本把「业务参数」搬到数据库，密钥仍留在
--       环境变量——库里只保存密钥的引用名（${HOU_PAY_XXX}），不存明文。
--
-- 执行方式：在业务库（meoc）执行本文件。建表带 IF NOT EXISTS，可安全重跑。
-- 兼容性：只加表；不建表时代码完全回落 Nacos/yml 基线，行为与之前一致。
-- ============================================================

CREATE TABLE IF NOT EXISTS hou_market_pay_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    scope        VARCHAR(16)  NOT NULL                COMMENT 'GLOBAL=全局 / CHANNEL=渠道 / PLAN=价格套餐',
    scope_key    VARCHAR(64)  NOT NULL                COMMENT 'GLOBAL 固定 "*"；CHANNEL 为渠道码；PLAN 为插件编码',
    sub_key      VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT 'PLAN 时为套餐码；其余留空串',
    config_key   VARCHAR(64)  NOT NULL                COMMENT '配置键，见下方「可用键」清单',
    config_value TEXT         NULL                    COMMENT '配置值；secret=1 时只能是 ${ENV_NAME} 引用',
    value_type   VARCHAR(16)  NOT NULL DEFAULT 'STRING' COMMENT 'STRING / INT / DECIMAL / BOOL',
    secret       TINYINT      NOT NULL DEFAULT 0      COMMENT '1=密钥类：库里只存环境变量名，明文禁止入库',
    remark       VARCHAR(255) NULL                    COMMENT '备注（运营可见，会下发到配置页）',
    update_by    VARCHAR(64)  NULL                    COMMENT '最后修改人',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config (scope, scope_key, sub_key, config_key),
    KEY idx_scope (scope, scope_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件支付配置（DB 覆盖 Nacos/yml 基线；密钥仅存引用）';

-- ============================================================
-- 可用配置键（写入时按此清单校验，未知键会被拒绝）
--
-- 【GLOBAL】scope='GLOBAL', scope_key='*', sub_key=''
--   enabled                 BOOL     是否开启在线购买（生产必须 true 才收钱）
--   channel                 STRING   默认渠道 CASHIER / WECHAT_V3 / ALIPAY / MOCK
--   sandbox                 BOOL     true=模拟支付（不碰真实资金）；生产必须 false
--   currency                STRING   默认币种 ISO 4217，如 CNY
--   pay-window-minutes      INT      订单支付窗口（分钟）
--   public-base-url         STRING   外网可访问基础地址，拼回调地址用
--   notify-path             STRING   回调路径，默认 /web/plugin-center/market/pay/notify
--   return-url              STRING   支付完成后跳转地址
--   notify-tolerance-minutes INT     回调时间戳容差（分钟），防重放
--
-- 【CHANNEL】scope='CHANNEL', scope_key=<渠道码>, sub_key=''
--   app-id          STRING  应用号（微信 appId / 支付宝 appId）
--   mch-id          STRING  商户号（微信 mchId / 支付宝 PID）
--   api-key         STRING  🔐 密钥：微信 APIv3 密钥 / 支付宝应用私钥 —— 库里只能存 ${ENV}
--   public-key      STRING  平台公钥（回调验签用）
--   cert-serial-no  STRING  商户证书序列号（微信 V3）
--   private-key     STRING  🔐 密钥：商户私钥（PEM 或路径）—— 库里只能存 ${ENV}
--   notify-url      STRING  该渠道单独的回调地址，留空用全局拼的
--   pay-methods     STRING  支持的支付方式，逗号分隔，留空不限制
--
-- 【PLAN】scope='PLAN', scope_key=<插件编码>, sub_key=<套餐码>
--   name        STRING  套餐展示名
--   price       DECIMAL 售价（元，两位小数）
--               ⚠️ PAID 类型必须 > 0：填 0 会在下单时被拒绝（否则等于白送正版授权）。
--                  免费发放请改用 license-type=TRIAL。TRIAL 允许 price=0。
--   currency    STRING  套餐币种，留空取全局
--   valid-days  INT     授权天数，>0 买断 N 天，<=0 或空为永久
--   license-type STRING PAID / TRIAL
--   enabled     BOOL    false 则该套餐不可选（下架某个价位）
--
-- 🔐 = secret=1，值必须是 ${环境变量名}，例如：
--     INSERT INTO hou_market_pay_config
--         (scope, scope_key, sub_key, config_key, config_value, value_type, secret, remark)
--     VALUES
--         ('CHANNEL','WECHAT_V3','','mch-id','1900000109','STRING',0,'商户号（非机密）'),
--         ('CHANNEL','WECHAT_V3','','api-key','${HOU_PAY_WECHAT_V3_API_KEY}','STRING',1,'APIv3 密钥，值取自环境变量'),
--         ('GLOBAL','*','','enabled','true','BOOL',0,'开启在线购买');
--     环境变量 HOU_PAY_WECHAT_V3_API_KEY 仍在 Nacos/容器 env 注入，库里永远不出现明文。
-- ============================================================

-- ============================================================
-- 验证语句
--   SHOW TABLES LIKE 'hou_market_pay_config';
--   SELECT scope, scope_key, sub_key, config_key, config_value, secret FROM hou_market_pay_config;
--   -- 安全检查：secret=1 的行，值必须形如 ${XXX}
--   SELECT * FROM hou_market_pay_config
--    WHERE secret = 1 AND (config_value IS NULL OR config_value NOT LIKE '${%}');
--   -- 上面这条应返回 0 行，返回非空说明有密钥明文入库，立即删除并改回引用写法。
-- ============================================================
