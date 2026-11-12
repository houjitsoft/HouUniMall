-- ============================================================
-- 商城 · 分销设置（2026-10-28）
--
-- 目标：分销功能的平台级开关与核心参数（是否开启、分佣层级数、申请方式、
--       绑定保护期、结算周期、提现门槛），后台「分销推广」插件可视化配置，
--       C 端分销中心按 enabled/maxLevel 控制入口显隐与层级权益。
--
-- 命名口径：对外统一叫「分销」，不用「微客 VK」（微客是 1 代历史实现，已冻结）。
--
-- ⚠️⚠️ 执行目标库：①【宿主 3306 的 meoc】，不是 fresh12-mysql 容器副本
--     ssh RH2288 "mysql -umeoc -p<凭据见Nacos-ai.yaml> -h127.0.0.1 -P3306 --default-character-set=utf8mb4 meoc" < 本文件
-- ============================================================

-- ---------------- 分销设置 ----------------
CREATE TABLE IF NOT EXISTS `hou_mall_distribution_setting` (
  `id`                 bigint unsigned  NOT NULL AUTO_INCREMENT,
  `shop_id`            bigint           NOT NULL DEFAULT 0     COMMENT '店铺ID，0=平台默认配置',
  `enabled`            tinyint          NOT NULL DEFAULT 0     COMMENT '分销总开关：1=开启 0=关闭',
  `max_level`          tinyint          NOT NULL DEFAULT 2     COMMENT '分佣层级数：1=一级 2=二级（合规上限两级，服务端再收敛）',
  `apply_mode`         tinyint          NOT NULL DEFAULT 2     COMMENT '成为分销员方式：1=无门槛 2=需审核 3=仅邀请',
  `bind_mode`          tinyint          NOT NULL DEFAULT 1     COMMENT '上下级绑定方式：1=永久绑定 2=保护期后可换绑',
  `bind_protect_days`  int              NOT NULL DEFAULT 15    COMMENT '绑定保护期（天），0=不设保护',
  `settle_days`        int              NOT NULL DEFAULT 7     COMMENT '佣金结算周期：订单完成后 T+N 天可提现',
  `min_withdraw_amount` decimal(10,2)   NOT NULL DEFAULT 10.00 COMMENT '单笔最低提现金额（元）',
  `auto_upgrade`       tinyint          NOT NULL DEFAULT 1     COMMENT '分销员等级自动升级：1=自动 0=仅手动',
  `auto_payout_balance` tinyint         NOT NULL DEFAULT 1     COMMENT '余额提现自动到账：1=审核通过后余额入账并完结 0=人工已打款',
  `create_time`        datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`        datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
  COMMENT='分销设置（平台级开关/层级/规则参数）';

-- 平台默认配置：默认关闭，后台显式开启后才生效（避免历史店铺被静默开通分佣）
INSERT INTO `hou_mall_distribution_setting`
  (`shop_id`, `enabled`, `max_level`, `apply_mode`, `bind_mode`, `bind_protect_days`,
   `settle_days`, `min_withdraw_amount`, `auto_upgrade`, `auto_payout_balance`)
VALUES (0, 0, 2, 2, 1, 15, 7, 10.00, 1, 1)
ON DUPLICATE KEY UPDATE `id` = `id`;
