# 数据库统一初始化包

面向从零安装的用户：一条命令安装**商城核心（Core）**——建库、建 Core 全部表结构、执行 Core 增量建表、写入初始数据。

> 🔴 **核心与插件分开装。** 本包只安装商城系统本身，**不创建任何插件表**。插件（ERP / OA / CRM / CMS / IM / 门店收银台 / 项目管理 / 餐饮等）是独立制品，必须在 Core 跑起来并登录后台之后，由用户按需在「插件中心」单独选择安装。插件建表脚本单独放在 `init/plugins/`，只在装对应插件时才执行。
>
> 🔴 **执行顺序的唯一权威清单是 `init/order.txt`**（`[schema]` 建表段 → `[data]` 种子段）。Docker 轨的 `deploy/mysql-init/00-init.sql` 由 `render-docker-init.sh` 从同一份清单渲染生成；改顺序/新增脚本 = 改 `order.txt` + 重跑渲染 + 连同生成物一起提交，`bash render-docker-init.sh --check` 可校验漂移。

## 文件结构

```
scripts/sql/
├── init_all.sh              # Linux / macOS 一键安装 Core
├── install_plugin_sql.sh    # 按需执行某个插件的建表脚本
├── render-docker-init.sh    # （维护者用）从 init/order.txt 渲染 deploy/mysql-init/00-init.sql
└── init/
    ├── 00-create-database.sql     # 建库 meoc（MySQL 8.0+，utf8mb4）
    ├── 10-meoc-schema.sql         # Core 全量表结构（666 张基表，仅结构、无数据；已剔除插件表）
    ├── 20~40 系列补丁              # Core 增量建表 / AI 中台等（均幂等或带存在性守卫）
    ├── 99-seed.sql                # 初始数据：菜单树 / 角色 / 初始超级管理员
    ├── 100~150 系列               # 分类导航、租户、插件中心菜单与市场目录等种子与规整
    ├── order.txt                  # 🔴 初始化唯一有序清单（两条执行链共用）
    └── plugins/                   # 插件建表脚本（初装流程不执行，装插件时才用，15 个文件 / 772 张表）
        ├── cms.sql  crm.sql  erp.sql  finance.sql  im.sql
        ├── mes.sql  oa.sql   orderfood.sql  pm.sql  webpos.sql …
        └── README.md
```

## 安装商城核心

前置：本机可用的 `mysql` 客户端，MySQL 8.0+ 服务端。

```bash
# Linux / macOS
export MYSQL_HOST=127.0.0.1 MYSQL_PORT=3306 MYSQL_USER=root MYSQL_PASSWORD=你的密码
export I_ACCEPT_EULA=yes        # 非交互必须：表示已阅读并同意开源 EULA
bash init_all.sh                # 全新安装（建库 + Core 全量表结构 + Core 增量迁移 + 初始数据）

# 只建表不灌种子（含演示租户跳过开关）
bash init_all.sh --schema-only
SKIP_DEMO_TENANT=1 bash init_all.sh
```

> 走 Docker 默认安装时无需手动执行：`deploy/install.sh` 会把本清单渲染成 `deploy/mysql-init/00-init.sql`，MySQL 容器首次启动自动执行（见 `deploy/README.md` 二点六节）。

## 安装插件的表

插件表不在 Core 初始化里。后台「插件中心」安装插件时由平台按 `tables.migrate` 自动建表（取插件包内 SQL，与本目录 `plugins/` 同源）。本脚本用于手工补表、排查结构差异、自动建表失败后的兜底：

```bash
bash install_plugin_sql.sh --list      # 列出可用的插件脚本
bash install_plugin_sql.sh erp         # 为 erp 插件建表（幂等，可重复执行）
```

## 验证

```bash
mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p -e \
  "select count(*) from information_schema.tables where table_schema='meoc'"
# Core 装完应约 670 张表（含少量控制面迁移表）；插件表随安装逐个增加
```

## 已初始化的库怎么升级

- 🔴 **不要对已初始化的库重跑整条 init 链**——会因表已存在报错（部分脚本带 `information_schema` 幂等守卫，但不承诺全部）。
- 存量库的增量改表 SQL 随版本发布说明提供，按说明单独执行。
