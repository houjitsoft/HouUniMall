# Mall商城模块数据库建表说明

## 文件说明
- **文件名**: `mall_tables.sql`
- **插件市场目录与初始 SKU**: `mall_market_plugin_catalog.sql`
- **生成时间**: 2026-01-05
- **表总数**: 30张
- **文件行数**: 895行

## 模块清单

### 1. 分销模块 (6张表)
- `hou_mall_distributor` - 分销员表
- `hou_mall_distributor_level` - 分销等级表
- `hou_mall_commission_rule` - 佣金规则表
- `hou_mall_commission_record` - 佣金记录表
- `hou_mall_distribution_goods` - 分销商品表
- `hou_mall_distribution_order` - 分销订单表

### 2. 返利模块 (4张表)
- `hou_mall_rebate_rule` - 返利规则表
- `hou_mall_rebate_record` - 返利记录表
- `hou_mall_rebate_payment` - 返利发放表
- `hou_mall_rebate_adjustment` - 返利调整表

### 3. 风控模块 (3张表)
- `hou_mall_risk_rule` - 风控规则表
- `hou_mall_risk_monitor` - 风控监控表
- `hou_mall_risk_alert` - 风控预警表

### 4. 店铺模块 (7张表)
- `hou_mall_shop` - 店铺信息表
- `hou_mall_shop_level` - 店铺等级表
- `hou_mall_merchant_entry` - 商家入驻申请表
- `hou_mall_inquiry` - 询价单表
- `hou_mall_invoice` - 发票表
- `hou_mall_cash_on_delivery` - 货到付款订单表
- `hou_mall_quality_deposit` - 质保金表

### 5. 仓储模块 (6张表)
- `hou_mall_warehouse` - 仓库表
- `hou_mall_stock` - 库存表
- `hou_mall_stock_inbound` - 入库单表
- `hou_mall_stock_outbound` - 出库单表
- `hou_mall_stock_transfer` - 库存调拨单表
- `hou_mall_stock_inventory` - 库存盘点单表

### 6. 首页模块 (4张表)
- `hou_mall_banner` - Banner轮播图表
- `hou_mall_home_floor` - 首页楼层配置表
- `hou_mall_notice` - 公告快报表
- `hou_mall_recommend_goods` - 推荐商品表

## 执行说明

插件市场商品、定价和授权不是前端配置，也不从 `plugin.yaml` 反向覆盖。首次部署或初始化市场库时执行
`mall_market_plugin_catalog.sql`：脚本会幂等创建 `hou_market_plugin`、
`hou_market_plugin_version`、`hou_market_plugin_license` 三张基础表，并写入官方插件的初始上架与定价数据。

- `hou_market_plugin.price_cents` 保存价格，单位为分；当前官方业务插件初始价为 3000000（30000 元）。
- `hou_market_plugin_license` 保存购买/授权事实；支付成功或平台审批后才新增授权记录。
- 不要在脚本中写入面向所有站点的默认授权。验收环境如需“默认已购买”，应按实际 `buyer_id`、`instance_id` 执行一条授权数据迁移。
- 该脚本不是启动时自动执行的商品覆盖任务；重复执行时会保留已有行的收费类型、价格和试用天数，后续改价应走市场管理数据流程。

### 在本地执行
```bash
# 连接数据库
mysql -h localhost -u root -p

# 选择数据库
use your_database_name;

# 执行建表脚本
source /Users/amosliu/www/houcloud/backend/paas/mall/src/main/resources/sql/mall_tables.sql;
source /Users/amosliu/www/houcloud/backend/paas/mall/src/main/resources/sql/mall_market_plugin_catalog.sql;

# 查看创建的表
show tables like 'hou_mall_%';
```

### 在服务器上执行
```bash
# SSH连接服务器
ssh houjit@127.0.0.1 -p 2819

# 上传SQL文件到服务器（在本地执行）
scp -P 2819 /Users/amosliu/www/houcloud/backend/paas/mall/src/main/resources/sql/mall_tables.sql houjit@127.0.0.1:/www/wwwroot/houcloud_code/paas/mall/src/main/resources/sql/

# 在服务器上连接数据库并执行
mysql -h 数据库地址 -u 用户名 -p
use 数据库名;
source /www/wwwroot/houcloud_code/paas/mall/src/main/resources/sql/mall_tables.sql;
source /www/wwwroot/houcloud_code/paas/mall/src/main/resources/sql/mall_market_plugin_catalog.sql;
```

## 特性说明

### 1. 字段特性
- 所有表都有 `id` 主键，自增长
- 所有表都有 `create_time` 和 `update_time` 时间戳字段
- 所有表都有 `deleted` 逻辑删除标记(软删除)
- 金额字段统一使用 `DECIMAL(10,2)` 类型
- 比例字段统一使用 `DECIMAL(5,2)` 类型

### 2. 索引设计
- 主键索引: 所有表的 `id` 字段
- 唯一索引: 编号类字段(如 `order_no`, `record_no` 等)
- 普通索引: 外键字段、状态字段、时间字段等高频查询字段
- 联合索引: 组合查询场景(如 `status + sort`)

### 3. 状态枚举
各模块状态字段说明已在字段注释中详细标注,请参考SQL文件中的COMMENT说明。

## 注意事项

⚠️ **重要提醒**
1. 执行前请确认数据库连接信息正确
2. 建议先在测试环境执行,验证无误后再在生产环境执行
3. 如表已存在,脚本会先DROP再CREATE,请谨慎操作
4. 执行后建议检查表结构和索引是否创建成功

## 后续步骤

建表完成后，还需要：
1. ✅ **已完成**: 30个实体类定义
2. ✅ **已完成**: Mapper接口定义
3. ✅ **已完成**: Service层实现
4. ✅ **已完成**: Controller层实现
5. ⚠️ **待验证**: 数据库表创建
6. ⚠️ **待补充**: 核心电商功能(购物车、地址、收藏等)
