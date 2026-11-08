-- 二开 demo 第二个脚本：加索引（验证按文件名升序执行）
ALTER TABLE `hou_ext_order_demo` ADD INDEX `idx_order_demo_no` (`order_no`);
