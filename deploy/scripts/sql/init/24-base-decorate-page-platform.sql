-- ============================================================
-- hou_decorate_page → hou_base_decorate_page 改名 + page_platform 端类型
-- 目标库：meoc（生产库）
-- ============================================================

-- 1) 改表名（likeshop 遗留表归入 base 命名规范）
RENAME TABLE hou_decorate_page TO hou_base_decorate_page;

-- 2) 加端类型列（历史数据默认 mobile，H5/小程序行为不变）
ALTER TABLE hou_base_decorate_page
  ADD COLUMN page_platform varchar(10) NOT NULL DEFAULT 'mobile'
  COMMENT '页面所属端：mobile=移动端(H5/小程序) pc=PC端' AFTER meta;

-- 3) 索引：列表查询按 type + page_platform 过滤
ALTER TABLE hou_base_decorate_page
  ADD INDEX idx_type_platform (type, page_platform);