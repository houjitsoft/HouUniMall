-- 二开 demo 建表（仅单测用，不进入任何真实业务库；表名符合 hou_ext_ 前缀约定）
CREATE TABLE IF NOT EXISTS `hou_ext_order_demo` (
    `id`       bigint      NOT NULL,
    `order_no` varchar(64) NOT NULL,
    PRIMARY KEY (`id`)
);
