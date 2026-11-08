-- 150：营销活动菜单收敛（2026-10-31）
-- ====================================================================
-- 背景：99-seed.sql 是生产库快照，带入 1004「营销活动」下 11 条二级菜单
--   （2017-2022 六条 shell:mall:marketing:* + 60110-60114 五条 mall:marketing:*），
--   它们指向的前端页面在仓库里**已全部不存在**（逐个核对 views/mall/marketing/coupon.vue
--   / views/marketing/seckill/index.vue 等 7 个 component 路径均为 MISS）——
--   点进去就是空白页，即「死链菜单」。
--
--   营销能力 2026-10-31 起已按业务拆到 order 域插件（order-seckill / order-voucher /
--   order-pintuan / order-reduction / order-combination），后台入口走插件位菜单
--   （display_pos='plugin'，插件安装时由 PluginMenuSyncPort 建出），不再占用 1004 的子级。
--
-- 本脚本做两件事（幂等，可重复执行）：
--   ① 删除 1004 下 11 条死链菜单 + 角色关联（rm JOIN 同删，不留悬挂引用）；
--   ② 把 1004 规整为「一级叶子页」：type=C / component=mall/marketing / display_pos=left，
--      指向 2026-10-31 新增的营销总览页 MarketingOverview.vue（跨插件只读聚合）。
--      sort 由 140-menu-ironrule.sql 统一给到 9500，本脚本不覆盖。
--
-- 🔴 刻意不做的事：**不往 init 里塞 5 个 order-* 插件位菜单**。
--   PluginRegistryReconciler 以「插件位菜单存在 ⇔ 本地已安装」判定（见其类注释），
--   init 阶段写死菜单 = 开源新装后这些插件默认「已安装」，直接违反
--   「开源包初始态所有插件未安装、站长从插件市场自己装」的铁律。
--   插件菜单由安装动作建，卸载动作删，这是设计内的唯一路径。
SET NAMES utf8mb4;

-- 一次性备份（重跑不覆盖）
CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_marketing_20261031 AS
 SELECT * FROM hou_permission_menu;

-- ① 死链二级菜单：角色关联先删，再删菜单本体
DELETE rm FROM hou_permission_role_menu rm
 WHERE rm.menu_id IN (2017,2018,2019,2020,2021,2022,60110,60111,60112,60113,60114);

DELETE FROM hou_permission_menu
 WHERE pid = 1004
   AND id IN (2017,2018,2019,2020,2021,2022,60110,60111,60112,60113,60114);

-- ② 1004 规整为一级叶子页（营销总览）。生产已是该口径，UPDATE 为 no-op。
UPDATE hou_permission_menu
   SET type='C',
       component='mall/marketing',
       paths='/mall/marketing',
       perms='mall:marketing',
       is_show=1,
       is_disable=0,
       update_time=UNIX_TIMESTAMP()
 WHERE id = 1004;

-- 自检：1004 应无子级；一级叶子页存在
SELECT '1004 子菜单残留（预期 0）' AS chk, COUNT(*) AS cnt
  FROM hou_permission_menu WHERE pid = 1004
UNION ALL
SELECT '1004 营销总览页', COUNT(*)
  FROM hou_permission_menu
 WHERE id = 1004 AND type='C' AND display_pos='left' AND component='mall/marketing';
