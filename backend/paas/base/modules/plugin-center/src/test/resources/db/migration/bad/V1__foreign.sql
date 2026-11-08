-- 越权脚本（仅单测用）：触碰非 hou_ext_ 前缀的宿主表 sys_user，应在执行前被拦截
CREATE TABLE `hou_ext_bad_ok` (`id` bigint);
UPDATE `sys_user` SET `name` = 'x';
