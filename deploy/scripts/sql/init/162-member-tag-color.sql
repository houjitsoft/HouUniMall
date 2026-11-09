-- 新装与存量一致：会员标签颜色扩展，幂等执行。
SET @tag_color_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hou_user_tag_info' AND COLUMN_NAME = 'tag_color') = 0,
    'ALTER TABLE hou_user_tag_info ADD COLUMN tag_color varchar(7) NOT NULL DEFAULT ''#409EFF'' COMMENT ''标签显示颜色''',
    'SELECT 1'
);
PREPARE tag_color_stmt FROM @tag_color_ddl;
EXECUTE tag_color_stmt;
DEALLOCATE PREPARE tag_color_stmt;
