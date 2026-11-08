-- =====================================================================
-- 抽奖转盘插件（userms-lucky-draw）建表脚本
-- 宿主：userms。表名与 mapper/entity 一致（activity_spinwin*）
-- 幂等：全部 CREATE TABLE IF NOT EXISTS，可重复执行，不删数据。
-- 说明：init 库里另有 hou_addon_activity_spinwin*（列名带 hou_ 前缀），
--       与本插件 mapper 不一致；本脚本按插件实际 SQL 落表。
-- =====================================================================

CREATE TABLE IF NOT EXISTS `activity_spinwin` (
  `activity_id` int NOT NULL AUTO_INCREMENT,
  `activity_title` varchar(255) NOT NULL DEFAULT '' COMMENT '活动标题',
  `activity_start_time` datetime NOT NULL COMMENT '活动开始时间',
  `activity_end_time` datetime NOT NULL COMMENT '活动结束时间',
  `partake_num_is_display` int NOT NULL DEFAULT '1' COMMENT '参与人数是否显示：1-显示，2-隐藏',
  `partake_num` int NOT NULL DEFAULT '0' COMMENT '参与人数',
  `partake_num_is_limit` int NOT NULL DEFAULT '1' COMMENT '参与人数限制：1-不限，2-限制',
  `partake_num_limit` int NOT NULL DEFAULT '0' COMMENT '参与人数限制值',
  `graphic_explanation` varchar(2000) NOT NULL DEFAULT '' COMMENT '图文说明',
  `first_screen_advertise` varchar(1000) NOT NULL DEFAULT '' COMMENT '首屏广告',
  `first_screen_is_display` int NOT NULL DEFAULT '1' COMMENT '首屏广告是否展示1-显示2-不显示',
  `is_round_award_information` int NOT NULL DEFAULT '1' COMMENT '是否轮播获奖信息：1-开启，2关闭',
  `activity_is_enable` int NOT NULL DEFAULT '1' COMMENT '活动是否可用1-可用2不可用3-删除',
  `update_by` varchar(32) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `total_sweepstakes_is_limit` int NOT NULL DEFAULT '1' COMMENT '总抽奖机会:是否限制：1-不限，2-限制',
  `total_sweepstakes_limit` int NOT NULL DEFAULT '0' COMMENT '总抽奖机会',
  `winning_times_per_person` int NOT NULL DEFAULT '0' COMMENT '每人每日抽奖次数',
  `winning_rate` decimal(10,2) DEFAULT NULL COMMENT '中奖率',
  `expected_number_of_participants` int NOT NULL DEFAULT '0' COMMENT '预计参与人数',
  PRIMARY KEY (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='幸运大转盘活动';

CREATE TABLE IF NOT EXISTS `activity_spinwin_gift` (
  `gift_id` int unsigned NOT NULL AUTO_INCREMENT,
  `gift_name` varchar(255) NOT NULL DEFAULT '' COMMENT '礼品名称',
  `redpacket_t_id` int NOT NULL DEFAULT '0' COMMENT '红包模版编号',
  `gift_is_redpacket` int NOT NULL DEFAULT '1' COMMENT '礼品是否是红包1-是,2-否',
  `gift_is_enable` int NOT NULL DEFAULT '1' COMMENT '礼品是否可用1-可用,2-不可用',
  `update_by` varchar(32) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `gift_image` varchar(1000) NOT NULL DEFAULT '' COMMENT '礼品图片',
  PRIMARY KEY (`gift_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大转盘活动礼品';

CREATE TABLE IF NOT EXISTS `activity_spinwin_prize` (
  `prize_id` int unsigned NOT NULL AUTO_INCREMENT,
  `prize_grade` varchar(255) NOT NULL DEFAULT '' COMMENT '奖项等级',
  `prize_name` varchar(255) NOT NULL DEFAULT '' COMMENT '奖项名称',
  `gift_id` int NOT NULL DEFAULT '0' COMMENT '礼品id',
  `activity_id` int NOT NULL DEFAULT '0' COMMENT '活动id',
  `gift_num` int NOT NULL DEFAULT '0' COMMENT '礼品数量',
  `update_by` varchar(32) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `give_out_gift_num` int NOT NULL DEFAULT '0' COMMENT '给出的礼品数量',
  `is_win` varchar(10) NOT NULL DEFAULT '1' COMMENT '是否中奖 1中奖2不中奖',
  `display_order` int NOT NULL DEFAULT '0' COMMENT '奖项排序',
  PRIMARY KEY (`prize_id`),
  KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大转盘活动奖项';

CREATE TABLE IF NOT EXISTS `activity_spinwin_lucky_draw_times` (
  `time_id` int unsigned NOT NULL AUTO_INCREMENT,
  `activity_id` int NOT NULL DEFAULT '0' COMMENT '活动id',
  `user_id` varchar(18) NOT NULL COMMENT '用户id',
  `lucky_draw_times` int NOT NULL DEFAULT '0' COMMENT '总抽奖次数',
  `surplus_draw_times` int NOT NULL DEFAULT '0' COMMENT '剩余抽奖次数',
  `update_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `winning_times` int NOT NULL DEFAULT '0' COMMENT '中奖次数',
  `number_of_daily_lottery` int NOT NULL DEFAULT '0' COMMENT '每日抽奖次数',
  `user_name` varchar(100) NOT NULL DEFAULT '' COMMENT '用户名',
  `num_sweepstakes_today` int NOT NULL DEFAULT '0' COMMENT '今日抽奖次数',
  PRIMARY KEY (`time_id`),
  KEY `idx_activity_user` (`activity_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大转盘用户抽奖次数';

CREATE TABLE IF NOT EXISTS `activity_spinwin_person` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `prize_id` int NOT NULL DEFAULT '0' COMMENT '奖项id',
  `activity_id` int NOT NULL DEFAULT '0' COMMENT '活动id',
  `user_id` varchar(18) NOT NULL COMMENT '用户id',
  `user_name` varchar(50) NOT NULL DEFAULT '' COMMENT '用户名',
  `user_avatar` varchar(500) NOT NULL DEFAULT '' COMMENT '用户头像',
  `update_date` datetime NOT NULL COMMENT '更新时间',
  `redpacket_id` int DEFAULT '0' COMMENT '红包id',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='幸运大转盘抽奖记录';
