-- =====================================================================
-- 插件开发者身份 + 第三方结算分账（缺失能力补齐，2026-10-27）
--
-- 背景：插件中心审计结论是「上传 Git → 部署 → 开发 → 发布销售 → 购买安装」
--   这条链路里有两块**从未实现**（不是缺陷）：① 插件开发者身份体系；
--   ② 第三方结算/分账。没有前者，发布永远只能由平台管理员做；没有后者，
--   平台收了钱也无法回答「该给哪个开发者结多少」。
--
-- 本脚本建四张表（无外键，与现有 hou_market_* 风格一致）：
--   hou_plugin_developer        开发者主体（一人一条，绑定平台 user_id）
--   hou_plugin_seller_account   收款账户（开发者提现/开票所需）
--   hou_plugin_revenue_share    分账流水（按订单逐笔记录，平台抽成在此固化）
--   hou_plugin_settlement_batch 结算批次（一期一批，可追溯凭证）
--
-- 幂等：CREATE TABLE IF NOT EXISTS + 菜单按 paths 判重。
-- =====================================================================
SET NAMES utf8mb4;

SET @dev_db = DATABASE();

-- 1) 开发者主体
CREATE TABLE IF NOT EXISTS `hou_plugin_developer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '平台用户ID（hou_auth_user.id），一个用户只能有一个开发者身份',
  `display_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '对外展示的开发者/工作室名称',
  `contact_email` VARCHAR(128) NOT NULL DEFAULT '',
  `contact_phone` VARCHAR(32) NOT NULL DEFAULT '',
  `company` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '公司主体（个人开发者为空）',
  `tax_no` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '纳税人识别号',
  `intro` VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '开发者简介，市场详情页展示',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待审核 1=正常 2=冻结 3=驳回',
  `quota_plugins` INT NOT NULL DEFAULT 5 COMMENT '可上架插件数上限',
  `quota_versions_monthly` INT NOT NULL DEFAULT 20 COMMENT '每月可上传版本数上限',
  `review_note` VARCHAR(500) NOT NULL DEFAULT '',
  `review_time` DATETIME NULL,
  `reviewer_id` BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_time` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_developer_user` (`user_id`),
  KEY `idx_developer_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件开发者主体';

-- 2) 收款账户
CREATE TABLE IF NOT EXISTS `hou_plugin_seller_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `developer_id` BIGINT NOT NULL,
  `settle_type` VARCHAR(16) NOT NULL DEFAULT 'BANK' COMMENT 'BANK / ALIPAY / WECHAT',
  `account_name` VARCHAR(128) NOT NULL DEFAULT '',
  `account_no` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '收款账号（生产环境应加密存储）',
  `bank_name` VARCHAR(128) NOT NULL DEFAULT '',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待审核 1=可用 2=停用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_seller_developer` (`developer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件开发者收款账户';

-- 3) 分账流水（订单支付成功即落一条，结算时按批次汇总）
CREATE TABLE IF NOT EXISTS `hou_plugin_revenue_share` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL,
  `plugin_code` VARCHAR(64) NOT NULL,
  `developer_id` BIGINT NOT NULL,
  `buyer_id` BIGINT NULL,
  `gross_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '订单实收金额',
  `currency` VARCHAR(8) NOT NULL DEFAULT 'CNY',
  `commission_rate` DECIMAL(6,4) NOT NULL DEFAULT 0.0000 COMMENT '平台抽成比例（0.3=30%）',
  `platform_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `developer_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待结算 1=已结算 2=已驳回（退款/风控）',
  `batch_no` VARCHAR(64) NOT NULL DEFAULT '',
  `settle_time` DATETIME NULL,
  `remark` VARCHAR(500) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_share_order` (`order_no`),
  KEY `idx_share_developer_status` (`developer_id`, `status`),
  KEY `idx_share_plugin` (`plugin_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件销售分账流水';

-- 4) 结算批次
CREATE TABLE IF NOT EXISTS `hou_plugin_settlement_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `batch_no` VARCHAR(64) NOT NULL,
  `developer_id` BIGINT NOT NULL,
  `period_start` DATE NULL,
  `period_end` DATE NULL,
  `share_count` INT NOT NULL DEFAULT 0,
  `gross_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `platform_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `developer_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `currency` VARCHAR(8) NOT NULL DEFAULT 'CNY',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待打款 1=已打款 2=已关闭',
  `voucher_url` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '打款凭证',
  `remark` VARCHAR(500) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `pay_time` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  KEY `idx_batch_developer` (`developer_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件结算批次';

-- 5) 菜单：开发者中心 / 结算分账（挂在插件中心 id=4 下，与发布/审核同层）
INSERT INTO `hou_permission_menu`
  (`pid`, `type`, `name`, `icon`, `sort`, `perms`, `paths`, `component`, `selected`, `params`,
   `is_cache`, `is_show`, `is_disable`, `create_time`, `update_time`, `display_pos`, `cate_id`, `plugin_code`)
SELECT 4, 'C', '开发者中心', 'User', 62, 'shell:addons:developer',
       '/platform/plugins/developer', 'platform/plugins/developer', '', '',
       0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 'hidden', 0, ''
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `paths` FROM `hou_permission_menu`) t
                    WHERE t.`paths` = '/platform/plugins/developer')
   AND EXISTS (SELECT 1 FROM (SELECT `id` FROM `hou_permission_menu`) p WHERE p.`id` = 4);

INSERT INTO `hou_permission_menu`
  (`pid`, `type`, `name`, `icon`, `sort`, `perms`, `paths`, `component`, `selected`, `params`,
   `is_cache`, `is_show`, `is_disable`, `create_time`, `update_time`, `display_pos`, `cate_id`, `plugin_code`)
SELECT 4, 'C', '结算分账', 'Money', 63, 'shell:addons:settlement',
       '/platform/plugins/settlement', 'platform/plugins/settlement', '', '',
       0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 'hidden', 0, ''
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `paths` FROM `hou_permission_menu`) t
                    WHERE t.`paths` = '/platform/plugins/settlement')
   AND EXISTS (SELECT 1 FROM (SELECT `id` FROM `hou_permission_menu`) p WHERE p.`id` = 4);

INSERT IGNORE INTO `hou_permission_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT r.`role_id`, m.`id`
  FROM `hou_permission_role_menu` r
  JOIN `hou_permission_menu` m
    ON m.`paths` IN ('/platform/plugins/developer', '/platform/plugins/settlement')
 WHERE r.`menu_id` = 4;

-- 6) 自检输出（人工核对用；不影响退出码）
SELECT 'plugin_developer_menus' AS check_item, COUNT(*) AS hit
  FROM `hou_permission_menu`
 WHERE `paths` IN ('/platform/plugins/developer', '/platform/plugins/settlement');
SELECT 'plugin_developer_tables' AS check_item, COUNT(*) AS hit
  FROM information_schema.tables
 WHERE table_schema = @dev_db
   AND table_name IN ('hou_plugin_developer', 'hou_plugin_seller_account',
                      'hou_plugin_revenue_share', 'hou_plugin_settlement_batch');
