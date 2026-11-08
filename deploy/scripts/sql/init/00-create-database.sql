-- =====================================================================
-- HouCloud 统一初始化 ①/④：建库
-- 全部微服务共用主库 meoc；workflow 服务为可选组件（需单独 workflow 库）。
-- 要求：MySQL 8.0+
-- 注意：本套脚本只安装商城核心（Core），不创建任何插件表。
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `meoc` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
