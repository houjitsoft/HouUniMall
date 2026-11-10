-- 2026-10-28：菜单铁律结构对齐（开源初始化链路补课）。
-- 背景：2026-09-21 的菜单铁律改造（商城域能力各自升 L1、插件中心移出左侧）只改了
--       生产库数据，从未回写开源初始化链路。评估环境 initdb 后左侧一级菜单仍是
--       老结构（6 个：含「商城管理」壳与「插件中心」在左侧），与生产实况
--       （约 14 个 L1）明显不符。
-- 本迁移把基础菜单结构对齐生产实况（权威数据源：生产库 meoc，
--       2026-10-28 实查），全部幂等 UPDATE，可重复执行：
--   - id=1 商城管理壳 / id=4 插件中心：display_pos=hidden（不进左侧）
--   - id=60001 商城概览：隐藏（生产 is_show=0）
--   - 商城域 10 个能力（商品/订单/用户/营销/店铺/库存/售后/物流/装修/统计）升 pid=0
--   - id=18 系统设置 sort 对齐生产 6000（seed 原值 1000）
--   - 不动：id=15 支付中心（110 迁移已入 unipay 插件位）、id=60007 商城结算
--     （seed 已正确 pid=0/left/7500）、id=242（开源线为装修占位；生产 242=AI智能
--     属 AI 线菜单，开源包不含 aiagent，不引入）
-- 开源口径对齐后左侧一级 = 13 个（生产 14 个减 AI智能）。
SET NAMES utf8mb4;

-- 一次性备份（110 迁移同款风格，重跑不覆盖后续人工配置）
CREATE TABLE IF NOT EXISTS hou_permission_menu_bak_ironrule_20261028 AS
 SELECT * FROM hou_permission_menu;

-- 商城管理壳：移出左侧（商城域能力已各自升 L1，壳仅保留兼容历史授权）
UPDATE hou_permission_menu
   SET display_pos='hidden', sort=9000, update_time=UNIX_TIMESTAMP()
 WHERE id=1;

-- 插件中心：移出左侧，入口仅在右上角「插件应用」面板底部「前往插件中心」
UPDATE hou_permission_menu
   SET display_pos='hidden', sort=7000, update_time=UNIX_TIMESTAMP()
 WHERE id=4;

-- 商城概览：生产已隐藏（is_show=0 + hidden）
UPDATE hou_permission_menu
   SET pid=0, display_pos='hidden', is_show=0, sort=9900, update_time=UNIX_TIMESTAMP()
 WHERE id=60001;

-- 商城域能力升为一级（pid=0 + display_pos=left + 生产 sort 口径）
UPDATE hou_permission_menu SET pid=0, display_pos='left', sort=9800, update_time=UNIX_TIMESTAMP() WHERE id=1002; -- 商品管理
UPDATE hou_permission_menu SET pid=0, display_pos='left', sort=9700, update_time=UNIX_TIMESTAMP() WHERE id=1003; -- 订单管理
UPDATE hou_permission_menu SET pid=0, display_pos='left', sort=9600, update_time=UNIX_TIMESTAMP() WHERE id=1005; -- 用户管理
UPDATE hou_permission_menu SET pid=0, display_pos='left', sort=9500, update_time=UNIX_TIMESTAMP() WHERE id=1004; -- 营销活动
UPDATE hou_permission_menu SET pid=0, display_pos='left', sort=9400, update_time=UNIX_TIMESTAMP() WHERE id=1009; -- 店铺管理
UPDATE hou_permission_menu SET pid=0, display_pos='left', sort=9300, update_time=UNIX_TIMESTAMP() WHERE id=1006; -- 库存管理
UPDATE hou_permission_menu SET pid=0, display_pos='left', sort=9200, update_time=UNIX_TIMESTAMP() WHERE id=1011; -- 售后服务
UPDATE hou_permission_menu SET pid=0, display_pos='left', sort=9100, update_time=UNIX_TIMESTAMP() WHERE id=1010; -- 物流管理
UPDATE hou_permission_menu SET pid=0, display_pos='left', sort=9000, update_time=UNIX_TIMESTAMP() WHERE id=60010; -- 装修管理
UPDATE hou_permission_menu SET pid=0, display_pos='left', sort=8900, update_time=UNIX_TIMESTAMP() WHERE id=1008; -- 数据统计

-- 系统设置 sort 对齐生产
UPDATE hou_permission_menu
   SET sort=6000, update_time=UNIX_TIMESTAMP()
 WHERE id=18;

-- 自检：对齐后左侧一级清单（预期 13 个：仪表盘/商品/订单/用户/营销/店铺/库存/售后/物流/装修/统计/商城结算/系统设置）
SELECT id,name,perms,sort FROM hou_permission_menu
 WHERE pid=0 AND display_pos='left' AND type<>'A' AND is_show<>0 AND is_disable=0
 ORDER BY sort DESC;

-- 2026-11-10：补「店铺等级」后台入口。
--   /mall/store/level 静态路由早已存在，但菜单树缺这一项，左侧导航点不到；
--   该页维护 hou_shop_level —— 入驻页「店铺等级」下拉与「查看店铺权益和收费标准」
--   的唯一数据源。本段幂等，授权范围沿用「店铺列表」(2046) 已有的角色。
--   🔴 菜单树变更后必须同时递增 admin 的 MENU_CACHE_KEY。
INSERT INTO hou_permission_menu
(id,pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,is_cache,is_show,is_disable,create_time,update_time,display_pos,cate_id)
SELECT 2065,1009,'C','店铺等级','Star',8500,'shell:mall:store:level','/mall/store/level','mall/store/level','','','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP(),'left',0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM hou_permission_menu WHERE id=2065 OR paths='/mall/store/level');
INSERT IGNORE INTO hou_permission_role_menu(role_id,menu_id)
SELECT DISTINCT role_id,2065 FROM hou_permission_role_menu WHERE menu_id=2046;
