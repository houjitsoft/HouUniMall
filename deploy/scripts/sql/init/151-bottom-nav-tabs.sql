-- =====================================================================
-- 移动端底部导航默认种子（2026-10-31）
--
-- 背景：底部导航读的是 `hou_base_modularizationd` 里 `page_is_index = 3` 的那一行，
--       而初始化链路里从来没有插入过这行 —— 生产库之所以有，是后台装修模块写过一次。
--       结果是全新安装（开源源码 / 评估站）装完 H5 根本没有底部菜单，
--       一级页面之间无法互相跳转。
--
-- 口径：五项底部导航，每项都配置语义图标（首页/及时购/视频/瀹集/我的）
--       首页 → /pages/index/index
--       及时购 → /pages/search/search（商品目录）
--       视频 → /pages/video/video（视频号入口）
--       瀹集 → /pages/activity/activity（瀹集会山河，活动/邀请）
--       我的 → /pages/personal/personal
--       底色 #F7F5F0、未选 #8A8578、选中 #B8976A。items[].icon 用的是前后端同源的字体图标类名。
--       showIcon 仅作「一项图标都没配时」的全局兜底，这里配了逐项图标，H5 会直接显示图标。
--
-- 结构（后台「装修管理 → 底部导航」可改，与 H5 读的是同一条记录）：
--   color        选中色
--   defaultColor 未选色
--   background   栏底色，留空跟主题
--   showIcon     一项图标都没配时的兜底开关（false=纯文字；已配逐项图标时该开关不生效）
--   items[]      { navTitle, navUrl, navUrlSign, icon, selectedIcon, show, navIndex }
--
-- 幂等：只在「没有底部导航行」时插入；已有配置（后台改过的）一律不动，
--       避免升级脚本把站长自定义的导航覆盖回默认值。
-- =====================================================================

INSERT INTO `hou_base_modularizationd`
  (`page_data`, `page_type`, `page_start`, `page_default`, `page_is_index`, `create_time`)
SELECT
  '{"color":"#B8976A","defaultColor":"#8A8578","background":"#F7F5F0","showIcon":true,"items":[{"icon":"icon-home-s1","navTitle":"首页","navUrlSign":"首页","navUrl":"/pages/index/index","show":true,"navIndex":0},{"icon":"icon-class-s1","navTitle":"及时购","navUrlSign":"及时购","navUrl":"/pages/search/search","show":true,"navIndex":1},{"icon":"icon-content-s1","navTitle":"视频","navUrlSign":"视频","navUrl":"/pages/video/video","show":true,"navIndex":2},{"icon":"icon-activity-s1","navTitle":"瀹集","navUrlSign":"瀹集","navUrl":"/pages/activity/activity","show":true,"navIndex":3},{"icon":"icon-user-s1","navTitle":"我的","navUrlSign":"我的","navUrl":"/pages/personal/personal","show":true,"navIndex":4}]}',
  1, 1, 0, 3, NOW()
  FROM DUAL
 WHERE NOT EXISTS (
        SELECT 1 FROM `hou_base_modularizationd` WHERE `page_is_index` = 3
      );
