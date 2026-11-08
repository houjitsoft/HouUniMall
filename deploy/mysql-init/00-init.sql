-- =====================================================================
-- Docker 轨：MySQL 容器首次启动时的数据库初始化（docker-entrypoint-initdb.d）
-- 只在数据卷为空时执行一次；改过数据后要重来请 `bash deploy/scripts/docker.sh reset`。
--
-- 🔴 本文件由 deploy/scripts/sql/render-docker-init.sh 从 deploy/scripts/sql/init/order.txt 生成，
--    请勿手改。改顺序改 order.txt，然后重跑生成脚本并一起提交。
--    校验：bash deploy/scripts/sql/render-docker-init.sh --check
--
-- 这里只装商城核心（Core）：建库 + 基表 + 增量迁移 + 菜单/角色/管理员。
-- 插件表不在这里建，插件是装完商城后在后台按需安装的独立制品。
--
-- 🔴 库名一律不写死。官方 entrypoint 会先按 MYSQL_DATABASE 建库，再以
--    `mysql --database="$MYSQL_DATABASE"` 执行本文件，所以这里既不需要
--    `CREATE DATABASE`，也不需要 `USE`。
--    2026-10-27 实测：这里原先写死 `USE \`meoc\``，把 MYSQL_DATABASE 配成别的名字时
--    compose 那行配置看起来生效、实际上所有表都落在 meoc 库，服务连新库直接报表不存在；
--    更糟的情形是 meoc 也不存在时，700 多条 DDL 逐条报「无库可用」而初始化进程还在跑，
--    表现为「装了很久没动静」。
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------- schema 阶段（建表 / 加列 / 改表） ----------
source /opt/houcloud-sql/10-meoc-schema.sql;
source /opt/houcloud-sql/20-core-migrations.sql;
source /opt/houcloud-sql/22-mobile-page-template.sql;
source /opt/houcloud-sql/24-base-decorate-page-platform.sql;
source /opt/houcloud-sql/15-plugin-nav-columns.sql;
source /opt/houcloud-sql/30-commercial-delivery.sql;
source /opt/houcloud-sql/31-plugin-license-term.sql;
source /opt/houcloud-sql/21-agreement-consent.sql;
source /opt/houcloud-sql/25-aiagent.sql;
source /opt/houcloud-sql/27-aiagent-schema-fix.sql;
source /opt/houcloud-sql/28-aiagent-platform-merge.sql;
source /opt/houcloud-sql/31-aiagent-agent.sql;
source /opt/houcloud-sql/37-aiagent-workflow.sql;
source /opt/houcloud-sql/38-aiagent-usage-page.sql;
source /opt/houcloud-sql/39-aiagent-display-pages.sql;
source /opt/houcloud-sql/33-mall-distribution-setting.sql;
source /opt/houcloud-sql/34-mall-distribution-settlement.sql;
source /opt/houcloud-sql/35-mall-distribution-schema-align.sql;

-- ---------- data 阶段（初始数据：菜单 / 角色 / 管理员） ----------
source /opt/houcloud-sql/99-seed.sql;
source /opt/houcloud-sql/21-agreement-consent.sql;
source /opt/houcloud-sql/22-mobile-page-template.sql;
source /opt/houcloud-sql/100-plugin-navigation-categories.sql;
source /opt/houcloud-sql/110-payment-to-unipay-plugin.sql;
source /opt/houcloud-sql/23-decoration-pc-mobile-split.sql;
source /opt/houcloud-sql/120-plugin-publish-menu.sql;
source /opt/houcloud-sql/105-tenant-seed.sql;
source /opt/houcloud-sql/130-plugin-developer-commerce.sql;
source /opt/houcloud-sql/26-ai-menu-fix.sql;
source /opt/houcloud-sql/29-aiagent-geo-cutover.sql;
source /opt/houcloud-sql/30-aiagent-front-menus.sql;
source /opt/houcloud-sql/32-aiagent-agent-menu.sql;
source /opt/houcloud-sql/140-menu-ironrule.sql;
source /opt/houcloud-sql/143-platform-consent.sql;
source /opt/houcloud-sql/144-trade-submenu-menus.sql;
source /opt/houcloud-sql/145-ai-knowledge-menu.sql;
source /opt/houcloud-sql/146-market-plugin-download.sql;
source /opt/houcloud-sql/147-drop-ai-admin-legacy-tables.sql;
source /opt/houcloud-sql/148-market-plugin-plans.sql;
source /opt/houcloud-sql/39-aiagent-cs-reply-workflow.sql;
source /opt/houcloud-sql/40-aiagent-workflow-menu.sql;
source /opt/houcloud-sql/141-oss-plugin-menu-trim.sql;
source /opt/houcloud-sql/152-payment-core-service.sql;
source /opt/houcloud-sql/145-oss-dead-app-trim.sql;
source /opt/houcloud-sql/150-marketing-menu-cleanup.sql;

SET FOREIGN_KEY_CHECKS = 1;
