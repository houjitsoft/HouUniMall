-- ============================================================
-- AI 智能菜单修正（2026-10-27）
--
-- 背景：菜单树其实已经建好（父 242 + 6 个子页 61203~61208，已授权给角色 1/2/5），
--       但父菜单名是建结构时留下的「占位」，左侧菜单直接显示成「占位」，看不出是 AI。
--       子菜单的 paths / component 与前端 router/modules/ai 完全一致，无需改动。
--
-- 幂等：只按 id 更新，可重复执行
--
-- 2026-10-29 补 pid=0：seed 里 242 的 pid 是 2，而父级 2 在生产已被删除，
-- 新装后 242 成了孤儿（collectNavItems 只把 pid=0 当一级），左侧看不到 AI智能。
-- 生产实查 242 = pid 0 / display_pos left / sort 300，以生产为准对齐。
-- ============================================================

UPDATE `hou_permission_menu`
SET `pid`         = 0,
    `name`        = 'AI智能',
    `icon`        = 'MagicStick',
    `sort`        = 300,
    `is_show`     = 1,
    `is_disable`  = 0,
    `display_pos` = 'left',
    `update_time` = UNIX_TIMESTAMP()
WHERE `id` = 242;

-- 兜底：若子菜单被误关，统一恢复显示
UPDATE `hou_permission_menu`
SET `is_show`     = 1,
    `is_disable`  = 0,
    `update_time` = UNIX_TIMESTAMP()
WHERE `pid` = 242
  AND `perms` IN ('shell:ai:dashboard', 'shell:ai:chat', 'shell:ai:generation',
                  'shell:ai:models', 'shell:ai:prompts', 'shell:ai:settings');
