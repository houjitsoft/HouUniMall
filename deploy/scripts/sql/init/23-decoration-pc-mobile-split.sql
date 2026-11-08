-- ============================================================
-- PC / 移动 装修菜单重构 + 页面端类型字段
-- 目标库：meoc（线上服务实际连接的生产库）
-- 原则：优先复用【占位】菜单，占位用光才新增
-- ============================================================

-- 1) 给页面表加端类型字段（新列不动现有列，历史数据默认 mobile 保证 H5 行为不变）
--    幂等守卫：schema 段 22-mobile-page-template.sql 已给 hou_base_modularizationd 建过
--    page_platform（定义与下方一致），裸 ALTER 重跑会 ERROR 1060 中断整条 data 链
--    （2026-10-31 rehearsal4 实测：从零链必炸）。
SET @dd_sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'hou_base_modularizationd'
        AND COLUMN_NAME = 'page_platform') > 0,
    'DO 0',
    'ALTER TABLE hou_base_modularizationd
       ADD COLUMN page_platform varchar(10) NOT NULL DEFAULT ''mobile''
       COMMENT ''页面所属端：mobile=移动端(H5/小程序) pc=PC端'''
  )
);
PREPARE dd_stmt FROM @dd_sql; EXECUTE dd_stmt; DEALLOCATE PREPARE dd_stmt;

-- 2) 占位菜单改造为两端目录
--    242占位(/ai) -> PC装修目录
UPDATE hou_permission_menu SET
  name = 'PC装修',
  type = 'M',
  paths = '/mall/decoration/pc',
  component = '',
  perms = 'mall:decoration:pc',
  icon = 'Monitor',
  sort = 90,
  is_show = 1,
  is_disable = 0
WHERE id = 242;

--    61199 占位(空) -> 移动装修目录
UPDATE hou_permission_menu SET
  name = '移动装修',
  type = 'M',
  paths = '/mall/decoration/mobile',
  component = '',
  perms = 'mall:decoration:mobile',
  icon = 'Iphone',
  sort = 100,
  is_show = 1,
  is_disable = 0
WHERE id = 61199;

-- 3) 现有两个叶子菜单改成"主题风格"，分别挂到两端目录下
--    60136 (原PC装修) -> PC装修/主题风格
UPDATE hou_permission_menu SET
  name = '主题风格', paths = '/mall/decoration/pc_style',
  component = 'mall/decoration/theme', pid = 242, sort = 10, icon = 'Brush'
WHERE id = 60136;

--    60251 (原移动装修) -> 移动装修/主题风格
UPDATE hou_permission_menu SET
  name = '主题风格', paths = '/mall/decoration/mobile_style',
  component = 'mall/decoration/theme', pid = 61199, sort = 10, icon = 'Brush'
WHERE id = 60251;

-- 4) 移动端"页面装修"目录：复用原页面装修 60134，挂到移动装修下
UPDATE hou_permission_menu SET
  name = '页面装修', paths = '/mall/decoration/pages',
  pid = 61199, sort = 20, icon = 'Document'
WHERE id = 60134;

-- 5) PC 端新增 7 个菜单（占位已用光：242/61199 都变成目录了）
--    5.1 PC 页面装修目录
SET @pc_pages := (SELECT MAX(id)+1 FROM hou_permission_menu);
INSERT INTO hou_permission_menu
  (id,pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,
   is_cache,is_show,is_disable,create_time,update_time,display_pos,cate_id)
VALUES
  (@pc_pages,242,'M','页面装修','Document',20,'mall:decoration:pc:pages',
   '/mall/decoration/pc/pages','','','','',1,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP(),'left',0);

--    5.2 PC 页面装修下 6 个子菜单（与移动端对称）
SET @pc_tpl := (SELECT MAX(id)+1 FROM hou_permission_menu);
INSERT INTO hou_permission_menu
  (id,pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,
   is_cache,is_show,is_disable,create_time,update_time,display_pos,cate_id)
VALUES
  (@pc_tpl,@pc_pages,'C','模板选择','Brush',10,'mall:decoration:pc:template',
   '/mall/decoration/pc/template','mall/decoration/pages/template','','','',1,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP(),'left',0);

SET @pc_home := (SELECT MAX(id)+1 FROM hou_permission_menu);
INSERT INTO hou_permission_menu
  (id,pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,
   is_cache,is_show,is_disable,create_time,update_time,display_pos,cate_id)
VALUES
  (@pc_home,@pc_pages,'C','首页装修','Monitor',20,'mall:decoration:pc:home',
   '/mall/decoration/pc/pages','mall/decoration/pages/pages','','','',1,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP(),'left',0);

SET @pc_goods := (SELECT MAX(id)+1 FROM hou_permission_menu);
INSERT INTO hou_permission_menu
  (id,pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,
   is_cache,is_show,is_disable,create_time,update_time,display_pos,cate_id)
VALUES
  (@pc_goods,@pc_pages,'C','商品详情','ShoppingBag',30,'mall:decoration:pc:goods',
   '/mall/decoration/pc/goods_detiail','mall/decoration/pages/goods_detiail','','','',1,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP(),'left',0);

SET @pc_ucenter := (SELECT MAX(id)+1 FROM hou_permission_menu);
INSERT INTO hou_permission_menu
  (id,pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,
   is_cache,is_show,is_disable,create_time,update_time,display_pos,cate_id)
VALUES
  (@pc_ucenter,@pc_pages,'C','个人中心','User',40,'mall:decoration:pc:usercenter',
   '/mall/decoration/pc/user_center','mall/decoration/pages/user_center','','','',1,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP(),'left',0);

SET @pc_data := (SELECT MAX(id)+1 FROM hou_permission_menu);
INSERT INTO hou_permission_menu
  (id,pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,
   is_cache,is_show,is_disable,create_time,update_time,display_pos,cate_id)
VALUES
  (@pc_data,@pc_pages,'C','数据配置','DataAnalysis',50,'mall:decoration:pc:data',
   '/mall/decoration/pc/data_allocation','mall/decoration/pages/data_allocation','','','',1,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP(),'left',0);

SET @pc_edit := (SELECT MAX(id)+1 FROM hou_permission_menu);
INSERT INTO hou_permission_menu
  (id,pid,type,name,icon,sort,perms,paths,component,selected,params,plugin_code,
   is_cache,is_show,is_disable,create_time,update_time,display_pos,cate_id)
VALUES
  (@pc_edit,@pc_pages,'C','首页编辑','Edit',0,'mall:decoration:pc:edit',
   '/mall/decoration/pc/index','mall/decoration/pages/index','','','',1,0,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP(),'left',0);

-- 6) 授权：给所有原本有 装修管理(60010) 权限的角色，补发两端新菜单
INSERT IGNORE INTO hou_permission_role_menu (role_id, menu_id)
SELECT DISTINCT r.role_id, m.id
FROM hou_permission_role_menu r
JOIN hou_permission_menu pm ON pm.id = r.menu_id AND pm.id = 60010
CROSS JOIN hou_permission_menu m
WHERE m.id IN (242, 61199, 60136, 60251, 60134, @pc_pages, @pc_tpl, @pc_home, @pc_goods, @pc_ucenter, @pc_data, @pc_edit);

-- 7) 移动端页面装修目录下的子菜单，统一限定为移动端语义（改名区分）
UPDATE hou_permission_menu SET name = '模板选择' WHERE id = 103526;