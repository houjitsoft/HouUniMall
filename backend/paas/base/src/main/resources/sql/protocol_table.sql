-- 协议管理表
CREATE TABLE IF NOT EXISTS `hou_base_protocol` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '协议名称',
  `type` varchar(50) NOT NULL COMMENT '协议类型：PERSONAL-个人入驻，ENTERPRISE-企业入驻，SUPPLIER-供应商入驻，COOPERATION-合作协议',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件URL',
  `file_name` varchar(200) DEFAULT NULL COMMENT '文件名称',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议管理表';

-- 插入默认数据
INSERT INTO `hou_base_protocol` (`id`, `name`, `type`, `file_url`, `file_name`, `sort`, `status`, `create_time`, `update_time`) 
VALUES 
(1, '个人入驻', 'PERSONAL', '384f645ff4dd44ed96822049bacd49fc.pdf', '384f645ff4dd44ed96822049bacd49fc.pdf', 1, 1, NOW(), NOW()),
(2, '企业入驻', 'ENTERPRISE', '5e9144c655554e4286cc76673e680552.pdf', '5e9144c655554e4286cc76673e680552.pdf', 2, 1, NOW(), NOW()),
(3, '供应商入驻', 'SUPPLIER', '2a023ecc103240f39d0be203163732b7.pdf', '2a023ecc103240f39d0be203163732b7.pdf', 3, 1, NOW(), NOW()),
(4, '合作协议', 'COOPERATION', '7d8499b00329423dada8999df33197c4.pdf', '7d8499b00329423dada8999df33197c4.pdf', 4, 1, NOW(), NOW());
