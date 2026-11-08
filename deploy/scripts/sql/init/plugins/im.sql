-- =====================================================================
-- 插件 im 的数据库建表脚本（共 9 张表）
-- 用途：安装「im」插件后按需执行（也可由插件自身的迁移通道建表）。
-- 🔴 商城核心（Core）初始化不包含本文件；未安装该插件时不要执行。
-- 全部 CREATE TABLE 已统一补 IF NOT EXISTS（幂等，可重复执行，不会删数据）。
-- 执行期间临时关闭外键检查：插件之间存在跨插件外键（如 oa 引用 hou_pm_project），
-- 未同时安装被引用插件时允许该外键暂时悬空。
-- =====================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ---------- 基础表结构（结构基线） ----------
-- Table structure for table `hou_im_automatic_reply`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_im_automatic_reply` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `is_shop` tinyint(1) DEFAULT '2' COMMENT '1 是店铺  2 是平台',
  `reply_start` tinyint(1) DEFAULT '1' COMMENT '1 启用 2 关闭',
  `reply_is_quick` tinyint(1) DEFAULT '1' COMMENT '是否设置为快捷问题 1 是 2 否',
  `reply_keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关键字',
  `reply_content_type` tinyint(1) DEFAULT NULL COMMENT '类型 1 文本 2 图片',
  `reply_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '内容',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='IM 自动回复语句';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_im_chat_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_im_chat_history` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '消息id',
  `send_hou_im_user_id` int DEFAULT NULL COMMENT '发送人id',
  `accept_hou_im_user_id` int DEFAULT NULL COMMENT '接收人id',
  `message` varchar(1500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '消息',
  `message_type` tinyint(1) DEFAULT NULL COMMENT '消息类型 0 文本  1 图片地址 3 视频地址 6：文件消息 11 商品 12 订单 13 自动回复 14 联系人工 15 提示消息 16 会话转接成功 100：自定义消息 （json）',
  `is_forward` tinyint(1) DEFAULT '2' COMMENT '是否是转发 1 是 2 否',
  `is_read` tinyint(1) DEFAULT '2' COMMENT '是否已读 1 是 2 否',
  `conversation_id` int DEFAULT NULL COMMENT '会话id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='IM  聊天记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_im_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_im_config` (
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配置值',
  `config_value2` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配置值',
  `config_enable` tinyint(1) DEFAULT '1' COMMENT '状态值，1可用，0不可用',
  `config_comment` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配置描述',
  PRIMARY KEY (`config_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='客服配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_im_conversation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_im_conversation` (
  `conversation_id` int NOT NULL AUTO_INCREMENT COMMENT '自增 会话id',
  `kf_user_id` int DEFAULT NULL COMMENT '客服用户id',
  `ordinary_user_id` int DEFAULT NULL COMMENT '普通用户id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `user_wy_conversation_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户-网易会话id',
  `kf_wy_conversation_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客服-网易会话id',
  `conversation_start` tinyint(1) DEFAULT NULL COMMENT '会话状态 1 正常 2 结束',
  `create_time` datetime DEFAULT NULL COMMENT '会话创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '会话更新时间',
  PRIMARY KEY (`conversation_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='IM  会话管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_im_customer_service`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_im_customer_service` (
  `kf_id` int NOT NULL AUTO_INCREMENT COMMENT '客服id',
  `hou_im_user_id` int DEFAULT NULL COMMENT 'im 用户id',
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `is_shop` tinyint(1) DEFAULT NULL COMMENT '1 是店铺  2 是平台',
  `kf_start` tinyint(1) DEFAULT NULL COMMENT '1 启用 2 关闭',
  `kf_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客服登录账号',
  `kf_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客服登录密码',
  PRIMARY KEY (`kf_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='IM 客服表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_im_quick_expressions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_im_quick_expressions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` int DEFAULT NULL COMMENT '店铺id',
  `is_shop` tinyint(1) DEFAULT '2' COMMENT '1 是店铺  2 是平台',
  `quick_start` tinyint(1) DEFAULT '1' COMMENT '1 启用 2 关闭',
  `quick_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '快捷用语',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='IM 快捷用语';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_im_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_im_user` (
  `hou_im_user_id` int NOT NULL AUTO_INCREMENT COMMENT 'im 用户id',
  `mall_user_id` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商城用户id',
  `user_mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号码',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
  `user_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户头像',
  `user_type` tinyint(1) DEFAULT NULL COMMENT '1 用户 2  客服',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `wy_account_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '网易云信账号',
  `wy_token` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '网易云信登录token',
  PRIMARY KEY (`hou_im_user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='IM 用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `im_cs_session_stats`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `im_cs_session_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` int NOT NULL,
  `kf_user_id` int DEFAULT NULL,
  `shop_id` int DEFAULT NULL,
  `ordinary_user_id` int DEFAULT NULL,
  `queue_start_time` datetime DEFAULT NULL,
  `queue_end_time` datetime DEFAULT NULL,
  `queue_wait_duration` int DEFAULT NULL,
  `first_request_time` datetime DEFAULT NULL,
  `conversation_start_time` datetime DEFAULT NULL,
  `first_response_time` datetime DEFAULT NULL,
  `first_response_duration` int DEFAULT NULL,
  `avg_response_duration` int DEFAULT NULL,
  `max_response_duration` int DEFAULT NULL,
  `conversation_end_time` datetime DEFAULT NULL,
  `conversation_duration` int DEFAULT NULL,
  `user_msg_count` int NOT NULL DEFAULT '0',
  `cs_msg_count` int NOT NULL DEFAULT '0',
  `end_type` tinyint DEFAULT NULL,
  `is_transferred` tinyint(1) NOT NULL DEFAULT '0',
  `transfer_count` int NOT NULL DEFAULT '0',
  `transfer_to_kf_user_id` int DEFAULT NULL,
  `evaluation_id` bigint DEFAULT NULL,
  `evaluation_score` int DEFAULT NULL,
  `stats_date` date NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `business_category_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_im_stats_conversation` (`conversation_id`),
  KEY `idx_im_stats_shop_date` (`shop_id`,`stats_date`),
  KEY `idx_im_stats_kf_date` (`kf_user_id`,`stats_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- ---------- plugins/im/migrations/客服评价表.sql ----------
CREATE TABLE IF NOT EXISTS `im_customer_service_evaluation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `im_kf_id` int DEFAULT NULL COMMENT '客服IM用户ID',
  `im_customer_id` int DEFAULT NULL COMMENT '客服记录ID',
  `mall_user_id` varchar(64) DEFAULT NULL COMMENT '商城用户ID',
  `im_mall_user_id` int DEFAULT NULL COMMENT '商城用户IM用户ID',
  `conversation_id` int NOT NULL COMMENT '会话ID',
  `score` tinyint NOT NULL COMMENT '评分1-5',
  `content` varchar(1000) DEFAULT NULL COMMENT '评价内容',
  `shop_id` int DEFAULT NULL COMMENT '店铺ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_id` (`conversation_id`),
  KEY `idx_im_kf_id` (`im_kf_id`),
  KEY `idx_shop_id_create_time` (`shop_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM客服评价';

SET FOREIGN_KEY_CHECKS=1;
