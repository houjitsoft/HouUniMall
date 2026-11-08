-- 交易管理二级收敛：库存 / 售后 / 物流下沉（2026-10-29）
--
-- 背景：左侧一级原有「库存管理 / 售后服务 / 物流管理」三项，与「交易管理」（订单）割裂了
-- 「下单 → 发货 → 售后 → 库存回补」的操作链路，一级数量也偏长。生产已把三者收敛为
-- 「交易管理(1003)」的二级项，本脚本把该口径回写初始化链路，保证新装即正确。
--
-- 依赖：140-menu-ironrule.sql 先把 1006/1011/1010 升为左侧 L1，本脚本在其后把三者下沉。
-- 幂等：可重复执行，结果恒定（只改 pid / sort / display_pos）。
-- 🔴 只动归属，不改 paths / perms / type —— 前端路由与 hou_permission_role_menu 授权无需重配。

-- 库存管理(1006) / 售后服务(1011) / 物流管理(1010) → 交易管理(1003) 二级，排在「订单列表」之后
UPDATE hou_permission_menu
   SET pid=1003, display_pos='left', sort=8000, update_time=UNIX_TIMESTAMP()
 WHERE id=1006;
UPDATE hou_permission_menu
   SET pid=1003, display_pos='left', sort=7999, update_time=UNIX_TIMESTAMP()
 WHERE id=1011;
UPDATE hou_permission_menu
   SET pid=1003, display_pos='left', sort=7998, update_time=UNIX_TIMESTAMP()
 WHERE id=1010;

-- 自检：交易管理(1003) 的二级清单（预期 7 个导航项 + 订单详情 A 类按钮）
-- 订单列表 / 库存管理 / 售后服务 / 物流管理 / 提醒发货 / 异常订单 / 售后订单 / 退款管理
SELECT id,pid,name,paths,sort FROM hou_permission_menu
 WHERE pid=1003 AND type<>'A' AND is_disable=0
 ORDER BY sort DESC;
