-- 用户行为与留存分析 · 统一埋点事件表（2026-11-12）
-- 全新安装由 init 链路建表；存量环境用 deploy/scripts/sql/migrations/20261112-user-behavior-event.sql
-- 幂等：重复执行安全。
-- 说明：登录 / 商品浏览 / 下单三类行为继续由既有真实表直接参与聚合（hou_log_login、
--       hou_goods_user_footprint、hou_order_base）；本表承接搜索 / 加购 / 分享等原先无源的埋点，
--       也允许各端把六类事件统一上报，避免重复计数时由 Service 侧按来源区分。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `hou_user_behavior_event` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(64) NOT NULL COMMENT '会员ID（统一按字符串存，兼容各端 user_id 类型）',
  `event_type` varchar(24) NOT NULL COMMENT '行为类型：login/view/search/cart/order/share',
  `event_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为发生时间',
  `source` varchar(32) DEFAULT NULL COMMENT '来源端：pc/h5/uniapp/miniapp/admin',
  `ref_id` varchar(64) DEFAULT NULL COMMENT '业务对象ID：商品/订单/搜索词等',
  `ext` varchar(500) DEFAULT NULL COMMENT '扩展信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '落库时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_event_time` (`user_id`, `event_time`),
  KEY `idx_event_type_time` (`event_type`, `event_time`),
  KEY `idx_event_time` (`event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户行为与留存分析-统一埋点事件';
