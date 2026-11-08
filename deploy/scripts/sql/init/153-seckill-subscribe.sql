-- 153 限时秒杀「开抢前预约」表（order-seckill 插件）
-- 用户针对某场次内某个秒杀商品预约，开抢时由 job 服务读取本表发站内信+短信提醒。
-- 幂等：CREATE TABLE IF NOT EXISTS。
-- 依赖：需在 10-meoc-schema.sql 建好 hou_addon_activity_seckill_base / _goods 之后执行。

CREATE TABLE IF NOT EXISTS `hou_addon_activity_seckill_subscribe` (
  `subscribe_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '预约编号',
  `seckill_id` int unsigned NOT NULL COMMENT '限时秒杀场次编号',
  `goods_id` int unsigned NOT NULL COMMENT '秒杀商品编号（规格级 goods_id）',
  `common_id` int NOT NULL DEFAULT '0' COMMENT '商品SPU编号',
  `shop_id` int unsigned NOT NULL DEFAULT '0' COMMENT '店铺编号',
  `user_id` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '预约用户编号',
  `user_mobile` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '预约时快照的手机号（供开抢短信提醒）',
  `goods_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品名称快照',
  `seckill_start_time` datetime NOT NULL COMMENT '场次开抢时间快照（供定时提醒调度）',
  `subscribe_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
  `remind_status` tinyint NOT NULL DEFAULT '0' COMMENT '提醒状态：0-未提醒 1-已提醒 2-提醒失败',
  `remind_time` datetime DEFAULT NULL COMMENT '提醒发送时间',
  PRIMARY KEY (`subscribe_id`) USING BTREE,
  UNIQUE KEY `uk_seckill_goods_user` (`seckill_id`, `goods_id`, `user_id`) USING BTREE,
  KEY `idx_start_remind` (`seckill_start_time`, `remind_status`) USING BTREE,
  KEY `idx_user` (`user_id`) USING BTREE,
  KEY `idx_shop` (`shop_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='限时秒杀开抢预约表';
