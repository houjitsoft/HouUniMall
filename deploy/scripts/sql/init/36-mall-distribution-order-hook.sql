-- 36 · 分销订单事件挂钩（order → mall）
--
-- 背景：订单服务在「确认收货（订单完成）」「退款/关闭」时通过
--       /internal/distribution/order/{finish,cancel} 把事件推给商城，
--       商城侧把订单落进 hou_mall_distribution_order 并按规则生成佣金。
--
-- 本文件只包含「让挂钩链路可用」所需的结构变更，业务配置数据（分销等级、
-- 分佣规则）由后台维护，不在此处写死。
--
-- 已在 ① 真库（meoc @ 宿主 3306）执行。

-- 1) commission_amount 补齐默认值
--    原列 NOT NULL 且无默认值，而下单事件登记时佣金尚未计算（金额为 0），
--    INSERT 不带该列会直接报
--      Field 'commission_amount' doesn't have a default value
--    导致整条分销订单登记失败。补默认值 0.00，语义正确（未完成时佣金为 0）。
ALTER TABLE hou_mall_distribution_order
    MODIFY COLUMN commission_amount decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '佣金金额（订单完成结算后回填）';

-- 2) 订单状态索引：结算链路按 order_status 扫描（generateCommissions 扫
--    order_status=3 的分销订单），补索引避免全表扫。
CREATE INDEX idx_hmdo_order_status ON hou_mall_distribution_order (order_status);
