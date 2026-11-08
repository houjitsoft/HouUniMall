-- =============================================================================
-- 插件市场：下载次数真实存储（明细表 + 计数回写 + 历史回填）
--
-- 背景（2026-10-29）：下载次数此前只是 hou_market_plugin.download_count 一个自增字段，
-- 谁下载的、哪个版本、来自哪个站点全无留痕；站点重装一次就虚增一次，别的安装人
-- 也无从读取「全网真实次数」。本迁移把口径改为：
--   1) hou_market_plugin_download 明细表是唯一权威，一行 = 一个站点的一次真实下载；
--   2) 唯一键 (site_code, plugin_code, version)：同站点同版本重复上报不刷量；
--   3) hou_market_plugin.download_count 由明细聚合回写（由服务端重算，不做自增）；
--   4) 历史累计值物化为 legacy-* 明细行，保证回写后总数不倒退。
--
-- 幂等：可重复执行（CREATE TABLE IF NOT EXISTS / INSERT IGNORE / 回写为覆盖式 UPDATE）。
-- =============================================================================

CREATE TABLE IF NOT EXISTS `hou_market_plugin_download` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `plugin_code` varchar(64) NOT NULL COMMENT '插件编码',
  `version` varchar(32) NOT NULL DEFAULT '' COMMENT '版本号，空串=未知',
  `site_code` varchar(64) NOT NULL DEFAULT '' COMMENT '下载方站点标识（platform=平台自身）',
  `site_name` varchar(128) NOT NULL DEFAULT '' COMMENT '站点名称',
  `source` varchar(32) NOT NULL DEFAULT 'install' COMMENT '来源：install/download/platform/legacy',
  `client_ip` varchar(64) NOT NULL DEFAULT '' COMMENT '客户端IP（服务端取）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_site_plugin_version` (`site_code`,`plugin_code`,`version`),
  KEY `idx_plugin` (`plugin_code`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件市场-下载/安装明细（下载次数权威来源）';

-- 历史累计值物化：把存量 download_count 拆成 legacy-1..N 明细行，
-- 这样「计数 = 明细行数」从第一天起就成立，回写不会把已有数字打回 0。
INSERT IGNORE INTO `hou_market_plugin_download`
  (`plugin_code`, `version`, `site_code`, `site_name`, `source`, `client_ip`)
SELECT p.`plugin_code`, '', CONCAT('legacy-', seq.n), '历史累计（2026-10-29 迁移回填）', 'legacy', ''
FROM `hou_market_plugin` p
JOIN (
  SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
  UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
  UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
  UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
  UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25
  UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
  UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35
  UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
  UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45
  UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
) seq ON seq.n <= COALESCE(p.`download_count`, 0)
WHERE COALESCE(p.`download_count`, 0) > 0;

-- 计数回写：download_count = 该插件的明细行数（幂等，覆盖式）
UPDATE `hou_market_plugin` p
SET p.`download_count` = (
  SELECT COUNT(*) FROM `hou_market_plugin_download` d WHERE d.`plugin_code` = p.`plugin_code`
)
WHERE p.`download_count` <> (
  SELECT COUNT(*) FROM `hou_market_plugin_download` d WHERE d.`plugin_code` = p.`plugin_code`
);
