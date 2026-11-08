-- =====================================================================
-- 演示租户种子（缺失能力 ⑤：真实租户）
--
-- 背景：全新部署只有 admin（user_id=1，无组织归属）。插件授权、订单、安装态
--   全都以 instanceId = orgId 做站点隔离，没有组织就永远只能落在 u-1 这种
--   退化标识上，「按站点买授权 / 按站点看订单」这条最核心的多租户链路
--   在开箱状态下无可验证对象。
--
-- 本脚本落一个可登录的演示租户：
--   组织      hou_base_organization   org_id=1001「演示企业」（org_code=DEMO）
--   账号      hou_user_info           user_id=2  demo / 13900000001
--   角色      hou_permission_role 2「普通用户」
--   归属      hou_user_organization  user 2 → org 1001（主组织）
--
-- 默认口令与初始管理员一致（同一 BCrypt 串），首次登录后应立即修改；
-- 生产部署可用 init_all.sh --skip-demo-tenant 跳过本文件。
--
-- 幂等：全部按主键/唯一键判重。
-- =====================================================================
SET NAMES utf8mb4;

SET @demo_db = DATABASE();

-- 1) 演示组织
INSERT INTO `hou_base_organization`
  (`org_id`, `org_name`, `org_code`, `org_type`, `org_status`, `contact_phone`, `create_time`, `update_time`)
SELECT 1001, '演示企业', 'DEMO', 1, 1, '13900000001', NOW(), NOW()
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `org_id` FROM `hou_base_organization`) t WHERE t.`org_id` = 1001);

-- 2) 演示管理员（与初始管理员同一口令散列，登录后请立即改密）
INSERT IGNORE INTO `hou_user_info`
  (`user_id`, `user_mobile`, `password`, `user_name`, `user_state`,
   `user_nickname`, `create_user`, `create_time`, `update_user`, `update_time`,
   `is_delete`, `is_admin`)
VALUES
  (2, '13900000001', '$2a$10$ccObPNUUb9Y8NFLZKTy1EOBvbs1A1u5SLrDWQAmASRLE0P3WVMLxS',
   'demo', 2, '演示管理员', 'installer', NOW(), 'installer', NOW(), 0, 0);

-- 3) 角色绑定（普通用户角色 id=2，不是超级管理员）
INSERT IGNORE INTO `hou_permission_user_role` (`user_id`, `role_id`) VALUES (2, 2);

-- 4) 归属到演示组织（主组织）
--    id 必须显式给值：该表 id 是 varchar（UserOrganizationEntity 雪花 ASSIGN_ID），无 AUTO_INCREMENT，
--    不带 id 会报 "Field 'id' doesn't have a default value"（2026-10-28 表结构修正配套）。
INSERT IGNORE INTO `hou_user_organization` (`id`, `user_id`, `org_id`, `is_primary`, `create_time`)
VALUES ('1050000000000000001', '2', '1001', 1, NOW());

-- 5) 自检输出（人工核对用；不影响退出码）
SELECT 'demo_tenant' AS check_item,
       (SELECT COUNT(*) FROM `hou_base_organization` WHERE `org_id` = 1001) AS org_rows,
       (SELECT COUNT(*) FROM `hou_user_info` WHERE `user_id` = 2) AS user_rows,
       (SELECT COUNT(*) FROM `hou_user_organization` WHERE `user_id` = '2' AND `org_id` = '1001') AS membership_rows;
