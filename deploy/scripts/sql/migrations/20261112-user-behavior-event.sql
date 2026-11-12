-- 用户行为与留存分析 · 统一埋点事件表（存量环境迁移，2026-11-12）
-- 幂等：CREATE TABLE IF NOT EXISTS，可在已安装环境重复执行。
-- 只新增一张事件表，不改动任何既有业务表；全新安装请同时保留 init/162-user-behavior-event.sql。
-- 数据来源说明：登录 / 浏览 / 下单由既有表实时聚合，本表用于承接搜索 / 加购 / 分享等埋点上报，
--               未接入埋点的维度在页面显式标注「未采集」，不补 0、不编数。
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
