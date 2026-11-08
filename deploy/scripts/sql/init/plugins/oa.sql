-- =====================================================================
-- 插件 oa 的数据库建表脚本（共 100 张表）
-- 用途：安装「oa」插件后按需执行（也可由插件自身的迁移通道建表）。
-- 🔴 商城核心（Core）初始化不包含本文件；未安装该插件时不要执行。
-- 全部 CREATE TABLE 已统一补 IF NOT EXISTS（幂等，可重复执行，不会删数据）。
-- 执行期间临时关闭外键检查：插件之间存在跨插件外键（如 oa 引用 hou_pm_project），
-- 未同时安装被引用插件时允许该外键暂时悬空。
-- =====================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ---------- 基础表结构（结构基线） ----------
-- Table structure for table `hou_oa_asset`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `asset_no` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资产编号',
  `asset_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '资产名称',
  `asset_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资产类型: 电子设备/办公家具/车辆/房产/其他',
  `asset_category` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资产分类',
  `brand` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '品牌',
  `model` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '型号',
  `specification` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格参数',
  `serial_number` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '序列号/出厂编号',
  `purchase_date` date DEFAULT NULL COMMENT '采购日期',
  `purchase_price` decimal(12,2) DEFAULT NULL COMMENT '采购价格',
  `supplier` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '供应商',
  `warranty_start_date` date DEFAULT NULL COMMENT '保修开始日期',
  `warranty_end_date` date DEFAULT NULL COMMENT '保修结束日期',
  `useful_life` int DEFAULT NULL COMMENT '使用年限(年)',
  `depreciation_method` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '折旧方式: 直线法/年数总和法/双倍余额递减法',
  `current_value` decimal(12,2) DEFAULT NULL COMMENT '当前价值',
  `location` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '存放位置',
  `department_id` bigint DEFAULT NULL COMMENT '使用部门ID',
  `department_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '使用部门名称',
  `user_employee_id` bigint DEFAULT NULL COMMENT '使用人员工ID',
  `user_employee_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '使用人姓名',
  `asset_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT '闲置' COMMENT '资产状态: 在用/闲置/维修中/已报废/已出售',
  `allocation_date` date DEFAULT NULL COMMENT '分配日期',
  `last_maintenance_date` date DEFAULT NULL COMMENT '最后维护日期',
  `next_maintenance_date` date DEFAULT NULL COMMENT '下次维护日期',
  `maintenance_cost` decimal(12,2) DEFAULT '0.00' COMMENT '累计维护费用',
  `qr_code` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '二维码内容',
  `image_urls` text COLLATE utf8mb4_general_ci COMMENT '资产图片URL(多个用逗号分隔)',
  `description` text COLLATE utf8mb4_general_ci COMMENT '资产描述',
  `remarks` text COLLATE utf8mb4_general_ci COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` int DEFAULT '0' COMMENT '删除标志 0-正常 1-删除',
  PRIMARY KEY (`id`),
  KEY `idx_asset_no` (`asset_no`),
  KEY `idx_asset_type` (`asset_type`),
  KEY `idx_asset_status` (`asset_status`),
  KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OA资产管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_attendance_dingtalk`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_attendance_dingtalk` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `org_id` varchar(32) NOT NULL DEFAULT '1' COMMENT '组织ID',
  `ding_user_id` varchar(64) NOT NULL COMMENT '钉钉成员 userId',
  `employee_id` int DEFAULT NULL COMMENT '关联 OA 员工 hou_oa_base.id',
  `employee_name` varchar(64) DEFAULT NULL COMMENT '姓名(同步快照)',
  `employee_no` varchar(32) DEFAULT NULL COMMENT '工号',
  `ding_dept_name` varchar(128) DEFAULT NULL COMMENT '钉钉部门名称(同步快照)',
  `attendance_date` date NOT NULL COMMENT '考勤日期',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `work_hours` decimal(6,2) DEFAULT NULL COMMENT '实际工作时长(小时)',
  `overtime_hours` decimal(6,2) DEFAULT '0.00' COMMENT '加班时长(小时)',
  `late_minutes` int DEFAULT '0' COMMENT '迟到分钟',
  `early_leave_minutes` int DEFAULT '0' COMMENT '早退分钟',
  `attendance_status` varchar(32) DEFAULT NULL COMMENT '考勤状态',
  `check_in_address` varchar(255) DEFAULT NULL COMMENT '签到地点',
  `check_out_address` varchar(255) DEFAULT NULL COMMENT '签退地点',
  `sync_month` varchar(7) DEFAULT NULL COMMENT '最近同步月份 YYYY-MM',
  `ext_json` json DEFAULT NULL COMMENT '钉钉扩展(班次/审批/原始报表等)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_ding_user_date` (`org_id`,`ding_user_id`,`attendance_date`),
  KEY `idx_org_date` (`org_id`,`attendance_date`),
  KEY `idx_employee_month` (`org_id`,`employee_id`,`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钉钉考勤日汇总(同步落库)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_attendance_feishu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_attendance_feishu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `org_id` varchar(32) NOT NULL DEFAULT '1' COMMENT '组织ID',
  `lark_user_id` varchar(64) NOT NULL COMMENT '飞书成员 user_id',
  `employee_id` int DEFAULT NULL COMMENT '关联 OA 员工 hou_oa_base.id',
  `employee_name` varchar(64) DEFAULT NULL COMMENT '姓名(同步快照)',
  `employee_no` varchar(32) DEFAULT NULL COMMENT '工号',
  `feishu_dept_name` varchar(128) DEFAULT NULL COMMENT '飞书部门名称(同步快照)',
  `attendance_date` date NOT NULL COMMENT '考勤日期',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `work_hours` decimal(6,2) DEFAULT NULL COMMENT '实际工作时长(小时)',
  `overtime_hours` decimal(6,2) DEFAULT '0.00' COMMENT '加班时长(小时)',
  `late_minutes` int DEFAULT '0' COMMENT '迟到分钟',
  `early_leave_minutes` int DEFAULT '0' COMMENT '早退分钟',
  `attendance_status` varchar(32) DEFAULT NULL COMMENT '考勤状态',
  `check_in_address` varchar(255) DEFAULT NULL COMMENT '签到地点',
  `check_out_address` varchar(255) DEFAULT NULL COMMENT '签退地点',
  `sync_month` varchar(7) DEFAULT NULL COMMENT '最近同步月份 YYYY-MM',
  `ext_json` json DEFAULT NULL COMMENT '飞书扩展字段',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_lark_user_date` (`org_id`,`lark_user_id`,`attendance_date`),
  KEY `idx_org_date` (`org_id`,`attendance_date`),
  KEY `idx_employee_month` (`org_id`,`employee_id`,`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='飞书考勤日汇总(同步落库)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_attendance_local`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_attendance_local` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `org_id` varchar(32) NOT NULL DEFAULT '1' COMMENT '组织ID',
  `employee_id` int NOT NULL COMMENT '员工ID(hou_oa_base.id)',
  `employee_name` varchar(64) DEFAULT NULL COMMENT '姓名快照',
  `employee_no` varchar(32) DEFAULT NULL COMMENT '工号快照',
  `department_id` int DEFAULT NULL COMMENT '部门ID快照',
  `department_name` varchar(128) DEFAULT NULL COMMENT '部门名称快照',
  `attendance_date` date NOT NULL COMMENT '考勤日期',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `scheduled_start_time` time DEFAULT '09:00:00' COMMENT '计划上班时间',
  `scheduled_end_time` time DEFAULT '18:00:00' COMMENT '计划下班时间',
  `work_hours` decimal(6,2) DEFAULT NULL COMMENT '实际工作时长(小时)',
  `overtime_hours` decimal(6,2) DEFAULT '0.00' COMMENT '加班时长(小时)',
  `late_minutes` int DEFAULT '0' COMMENT '迟到分钟',
  `early_leave_minutes` int DEFAULT '0' COMMENT '早退分钟',
  `attendance_status` varchar(32) DEFAULT '正常' COMMENT '正常/迟到/早退/旷工/请假等',
  `check_in_address` varchar(255) DEFAULT NULL COMMENT '签到地址',
  `check_out_address` varchar(255) DEFAULT NULL COMMENT '签退地址',
  `check_in_latitude` decimal(10,7) DEFAULT NULL COMMENT '签到纬度',
  `check_in_longitude` decimal(10,7) DEFAULT NULL COMMENT '签到经度',
  `check_out_latitude` decimal(10,7) DEFAULT NULL COMMENT '签退纬度',
  `check_out_longitude` decimal(10,7) DEFAULT NULL COMMENT '签退经度',
  `punch_channel` varchar(32) DEFAULT 'app' COMMENT '打卡渠道: app/web/mini',
  `device_info` varchar(128) DEFAULT NULL COMMENT '设备信息',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注或补签说明',
  `exception_type` varchar(64) DEFAULT NULL COMMENT '异常类型: 忘记打卡/补签/调班',
  `exception_reason` varchar(512) DEFAULT NULL COMMENT '异常原因',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `approval_status` varchar(32) DEFAULT NULL COMMENT '审批状态',
  `approval_comment` varchar(512) DEFAULT NULL COMMENT '审批意见',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_employee_date` (`org_id`,`employee_id`,`attendance_date`),
  KEY `idx_org_date` (`org_id`,`attendance_date`),
  KEY `idx_employee_month` (`org_id`,`employee_id`,`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='本平台本地打卡记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_attendance_wecom`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_attendance_wecom` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `org_id` varchar(32) NOT NULL DEFAULT '1' COMMENT '组织ID',
  `userid` varchar(64) NOT NULL COMMENT '企微成员 userid',
  `employee_id` int DEFAULT NULL COMMENT '关联 OA 员工 hou_oa_base.id',
  `employee_name` varchar(64) DEFAULT NULL COMMENT '姓名(同步时快照)',
  `employee_no` varchar(32) DEFAULT NULL COMMENT '工号',
  `wecom_dept_name` varchar(128) DEFAULT NULL COMMENT '企微部门名称(同步快照)',
  `attendance_date` date NOT NULL COMMENT '考勤日期',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '签退时间',
  `work_hours` decimal(6,2) DEFAULT NULL COMMENT '实际工作时长(小时)',
  `overtime_hours` decimal(6,2) DEFAULT '0.00' COMMENT '加班时长(小时)',
  `late_minutes` int DEFAULT '0' COMMENT '迟到分钟',
  `early_leave_minutes` int DEFAULT '0' COMMENT '早退分钟',
  `attendance_status` varchar(32) DEFAULT NULL COMMENT '企微汇总状态: 正常/异常等',
  `check_in_address` varchar(255) DEFAULT NULL COMMENT '签到地点',
  `check_out_address` varchar(255) DEFAULT NULL COMMENT '签退地点',
  `sync_month` varchar(7) DEFAULT NULL COMMENT '最近同步月份 YYYY-MM',
  `ext_json` json DEFAULT NULL COMMENT '企微扩展字段(规则/班次/原始统计等)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_userid_date` (`org_id`,`userid`,`attendance_date`),
  KEY `idx_org_date` (`org_id`,`attendance_date`),
  KEY `idx_employee_month` (`org_id`,`employee_id`,`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微考勤日汇总(同步落库)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_attendance_whitelist`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_attendance_whitelist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `org_id` varchar(50) NOT NULL COMMENT '组织ID',
  `source` varchar(32) NOT NULL COMMENT '考勤来源 wechat-work/dingtalk/feishu/platform',
  `external_user_id` varchar(128) NOT NULL COMMENT '平台用户ID（列表行 externalUserId）',
  `employee_id` int DEFAULT NULL COMMENT 'OA员工ID（可空，薪资结算辅助匹配）',
  `employee_name` varchar(100) DEFAULT NULL COMMENT '姓名快照',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '1放行满勤 0关闭',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_org_source_ext` (`org_id`,`source`,`external_user_id`),
  KEY `idx_org_employee` (`org_id`,`employee_id`),
  KEY `idx_org_enabled` (`org_id`,`enabled`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤满勤白名单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_bank`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_bank` (
  `id` int NOT NULL,
  `deposit_bank` varchar(255) DEFAULT '' COMMENT '开户行',
  `bank_branch` varchar(255) DEFAULT '' COMMENT '开户支行',
  `bank_card_number` varchar(255) DEFAULT '' COMMENT '银行卡号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表-银行卡信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_base`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_base` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组织ID',
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '会员表user_id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '姓名',
  `gender` enum('1','2','3') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '性别',
  `mobile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '手机号',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '邮箱',
  `id_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '证件姓名',
  `id_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '证件号码',
  `id_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '证件类型',
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `constellation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '星座',
  `blood_type` enum('1','2','3','4','5') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '血型',
  `marital` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '婚姻状况',
  `party_time` date DEFAULT NULL COMMENT '入党时间',
  `politic_countenance` enum('1','2','3','4','5','6') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '政治面貌',
  `edu` enum('1','2','3','4','5','6','7','8','9','10','11','12') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '最高学历',
  `flower_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '花名',
  `en_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '英文名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '头像',
  `wechat` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '微信号',
  `qq` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'QQ',
  `id_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '证件地址',
  `id_validity` date DEFAULT NULL COMMENT '证件有效期',
  `age` int DEFAULT NULL COMMENT '年龄',
  `height` int DEFAULT NULL COMMENT '身高（m）',
  `weight` int DEFAULT NULL COMMENT '体重（kg）',
  `ch_zodiac` enum('1','2','3','4','5','6','7','8','9','10','11','12') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '属相',
  `fertility_state` enum('1','2','3') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '生育状况',
  `health_state` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '健康状况',
  `residence_type` enum('1','2','3','4','5') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '户籍类型',
  `residence_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '户籍所在地',
  `residence_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '户籍地址',
  `current_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '现居地址',
  `current_city` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '现居城市',
  `hobby` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '特长爱好',
  `id_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '证件照',
  `self_evaluation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '自我评价',
  `native_place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '籍贯',
  `nation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '民族',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='员工表-基本信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_certificate`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_certificate` (
  `id` int NOT NULL,
  `id_copy` varchar(255) DEFAULT '' COMMENT '身份证复印件',
  `bank_card_copy` varchar(255) DEFAULT '' COMMENT '银行卡复印件',
  `academic_certificate` varchar(255) DEFAULT '' COMMENT '学历证明',
  `leaving_certificate` varchar(255) DEFAULT '' COMMENT '离职证明',
  `resume` varchar(255) DEFAULT '' COMMENT '入职简历',
  `physical` varchar(255) DEFAULT '' COMMENT '入职体检表',
  `id_image` varchar(255) DEFAULT '' COMMENT '个人证件照',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表-证件材料';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contact`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contact` (
  `id` int NOT NULL,
  `relation` enum('1','2','3','4','5','6','7','8','9','10','11','12','13','14','15') DEFAULT '1' COMMENT '关系',
  `relation_mobile` varchar(255) DEFAULT '' COMMENT '电话',
  `relation_username` varchar(255) DEFAULT '' COMMENT '紧急联系人姓名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表-紧急联系人';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组织ID',
  `contract_number` varchar(50) NOT NULL COMMENT '合同编号',
  `contract_name` varchar(100) NOT NULL COMMENT '合同名称',
  `category_id` int DEFAULT NULL COMMENT '合同分类ID:1销售收入合同2服务商合同3员工合同4销售支出合同',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '合同分类名称：1销售收入合同2服务商合同3员工合同4销售支出合同',
  `customer_name` varchar(100) NOT NULL COMMENT '客户名称',
  `customer_contact` varchar(50) DEFAULT NULL COMMENT '客户联系人',
  `customer_phone` varchar(20) DEFAULT NULL COMMENT '客户联系电话',
  `customer_email` varchar(100) DEFAULT NULL COMMENT '客户邮箱',
  `party_a_name` varchar(200) DEFAULT NULL COMMENT '甲方名称',
  `party_b_name` varchar(200) DEFAULT NULL COMMENT '乙方名称',
  `contract_amount` decimal(15,2) DEFAULT '0.00' COMMENT '合同金额',
  `status` int DEFAULT '0' COMMENT '合同状态：0草稿 1待签署 2已中止 3执行中 4已完成 5已终止',
  `direction` varchar(10) DEFAULT NULL COMMENT '合同方向：out(出)、in(入)',
  `sign_date` datetime DEFAULT NULL COMMENT '合同签署日期',
  `effective_date` datetime DEFAULT NULL COMMENT '合同生效日期',
  `expiry_date` datetime DEFAULT NULL COMMENT '合同到期日期',
  `contract_content` longtext COMMENT '合同内容',
  `payment_method` varchar(50) DEFAULT NULL COMMENT '付款方式',
  `project_id` bigint DEFAULT NULL COMMENT '项目ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',
  `del_flag` int DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `version` int DEFAULT '1' COMMENT '版本号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_number` (`contract_number`),
  KEY `idx_customer_name` (`customer_name`),
  KEY `idx_status` (`status`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_org_status_time` (`org_id`,`status`,`create_time`),
  KEY `idx_org_category_time` (`org_id`,`category_id`,`create_time`),
  KEY `idx_update_time` (`update_time`),
  KEY `idx_contract_number` (`contract_number`),
  KEY `idx_org_del_createtime` (`org_id`,`del_flag`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_approval`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_approval` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '审批ID',
  `contract_id` int NOT NULL COMMENT '合同ID',
  `approval_title` varchar(100) NOT NULL COMMENT '审批标题',
  `workflow_id` int DEFAULT NULL COMMENT '审批流程ID',
  `process_instance_id` int DEFAULT NULL COMMENT '审批流程实例ID',
  `status` int DEFAULT '0' COMMENT '审批状态：0待审批 1已通过 2已拒绝 3已撤回',
  `current_node` varchar(50) DEFAULT NULL COMMENT '当前审批节点',
  `current_approver_id` int DEFAULT NULL COMMENT '当前审批人ID',
  `current_approver_name` varchar(50) DEFAULT NULL COMMENT '当前审批人名称',
  `apply_reason` varchar(500) DEFAULT NULL COMMENT '申请理由',
  `approval_comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `approval_time` datetime DEFAULT NULL COMMENT '审批完成时间',
  `attachment_path` varchar(255) DEFAULT NULL COMMENT '附件路径',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` int DEFAULT NULL COMMENT '申请人ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '申请人名称',
  `update_user_id` int DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',
  `del_flag` int DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `org_id` varchar(255) DEFAULT NULL COMMENT '组织ID',
  PRIMARY KEY (`id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_status` (`status`),
  KEY `idx_approver_id` (`current_approver_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OA合同审批表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `category_code` varchar(20) DEFAULT NULL COMMENT '分类编码',
  `parent_id` int DEFAULT '0' COMMENT '父级分类ID，0表示顶级分类',
  `level_path` varchar(255) DEFAULT NULL COMMENT '层级路径，如：0,1,2',
  `level` int DEFAULT '1' COMMENT '层级，从1开始',
  `sort_order` int DEFAULT '0' COMMENT '排序值，数值越小越靠前',
  `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
  `status` int DEFAULT '1' COMMENT '状态：0禁用 1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `update_user_id` int DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',
  `del_flag` int DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_org_del_status_sort` (`org_id`,`del_flag`,`status`,`sort_order`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OA合同分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_employee`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_employee` (
  `id` int NOT NULL,
  `contract_company` varchar(255) DEFAULT '' COMMENT '合同公司',
  `contract_start_date` date DEFAULT NULL COMMENT '首次合同起始日',
  `contract_end_date` date DEFAULT NULL COMMENT '首次合同到期日',
  `current_contract_start_date` date DEFAULT NULL COMMENT '现合同起始日',
  `current_contract_end_date` date DEFAULT NULL COMMENT '现合同到期日',
  `contract_type` varchar(255) DEFAULT '' COMMENT '合同类型',
  `contract_period` varchar(255) DEFAULT '' COMMENT '合同期限',
  `contract_status` varchar(255) DEFAULT '' COMMENT '合同状态',
  `renewal_times` varchar(255) DEFAULT '' COMMENT '续签次数',
  `contract_file` varchar(255) DEFAULT '' COMMENT '合同文件',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表-合同信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_export_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_export_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '导出历史ID',
  `template_id` bigint DEFAULT NULL COMMENT '使用的模板ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `export_type` varchar(50) NOT NULL COMMENT '导出类型：excel,pdf,csv',
  `file_name` varchar(255) NOT NULL COMMENT '导出文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小',
  `record_count` int NOT NULL COMMENT '导出记录数',
  `export_user_id` bigint NOT NULL COMMENT '导出用户ID',
  `export_user_name` varchar(100) NOT NULL COMMENT '导出用户姓名',
  `export_status` varchar(20) NOT NULL DEFAULT 'processing' COMMENT '导出状态：processing-处理中，success-成功，failed-失败',
  `error_message` text COMMENT '错误信息',
  `export_time` datetime NOT NULL COMMENT '导出时间',
  `download_count` int NOT NULL DEFAULT '0' COMMENT '下载次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_export_user_id` (`export_user_id`),
  KEY `idx_export_time` (`export_time`),
  KEY `idx_export_status` (`export_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同导出历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_export_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_export_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_type` varchar(50) NOT NULL COMMENT '模板类型：excel,pdf,csv',
  `selected_fields` text NOT NULL COMMENT '选择的字段JSON',
  `sort_fields` text COMMENT '排序字段JSON',
  `filter_conditions` text COMMENT '筛选条件JSON',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认模板 0-否 1-是',
  `is_public` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否公共模板 0-否 1-是',
  `create_user_id` bigint NOT NULL COMMENT '创建用户ID',
  `create_user_name` varchar(100) NOT NULL COMMENT '创建用户姓名',
  `use_count` int NOT NULL DEFAULT '0' COMMENT '使用次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_user_id` (`create_user_id`),
  KEY `idx_template_type` (`template_type`),
  KEY `idx_is_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同导出模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_file`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(100) NOT NULL COMMENT '文件类型',
  `file_hash` varchar(64) NOT NULL COMMENT '文件hash值',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `version` int NOT NULL DEFAULT '1' COMMENT '文件版本号',
  `description` text COMMENT '文件描述',
  `download_count` int NOT NULL DEFAULT '0' COMMENT '下载次数',
  `upload_user_id` bigint NOT NULL COMMENT '上传用户ID',
  `upload_user_name` varchar(100) NOT NULL COMMENT '上传用户姓名',
  `upload_time` datetime NOT NULL COMMENT '上传时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_upload_user_id` (`upload_user_id`),
  KEY `idx_upload_time` (`upload_time`),
  KEY `idx_file_hash` (`file_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同文件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_notification`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `notification_type` varchar(50) NOT NULL COMMENT '通知类型：create-创建，update-更新，approve-审批，expire-到期',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `recipient_user_id` bigint NOT NULL COMMENT '接收用户ID',
  `recipient_user_name` varchar(100) NOT NULL COMMENT '接收用户姓名',
  `sender_user_id` bigint NOT NULL COMMENT '发送用户ID',
  `sender_user_name` varchar(100) NOT NULL COMMENT '发送用户姓名',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读 0-未读 1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `priority` varchar(20) NOT NULL DEFAULT 'normal' COMMENT '优先级：low-低，normal-普通，high-高，urgent-紧急',
  `send_time` datetime NOT NULL COMMENT '发送时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_recipient_user_id` (`recipient_user_id`),
  KEY `idx_sender_user_id` (`sender_user_id`),
  KEY `idx_send_time` (`send_time`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_payment_plan`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_payment_plan` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '付款计划ID',
  `org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织ID',
  `project_id` int NOT NULL DEFAULT '0' COMMENT '项目ID',
  `contract_id` int NOT NULL COMMENT '合同ID',
  `period_number` int NOT NULL COMMENT '付款期数（第几期）',
  `payment_amount` decimal(15,2) NOT NULL COMMENT '付款金额',
  `payment_date` date DEFAULT NULL COMMENT '付款日期',
  `payment_status` int DEFAULT '0' COMMENT '付款状态：0未付款 1已付款 2已逾期',
  `payment_time` datetime DEFAULT NULL COMMENT '实际付款时间',
  `payment_voucher` varchar(500) DEFAULT NULL COMMENT '付款凭证',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',
  `del_flag` int DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_payment_date` (`payment_date`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_contract_status` (`contract_id`,`payment_status`),
  KEY `idx_contract_del_status` (`contract_id`,`del_flag`,`payment_status`),
  KEY `idx_plan_contract_del_status` (`contract_id`,`del_flag`,`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同付款计划表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_permission`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `department_id` bigint DEFAULT NULL COMMENT '部门ID',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID',
  `permission_type` varchar(50) NOT NULL COMMENT '权限类型：data-数据权限，operation-操作权限，field-字段权限',
  `permission_scope` varchar(100) NOT NULL COMMENT '权限范围：all-全部，department-部门，personal-个人',
  `allowed_operations` varchar(500) DEFAULT NULL COMMENT '允许的操作：view,edit,delete,approve',
  `allowed_fields` text COMMENT '允许访问的字段',
  `forbidden_fields` text COMMENT '禁止访问的字段',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否激活 0-否 1-是',
  `create_user_id` bigint NOT NULL COMMENT '创建用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `org_id` varchar(64) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission` (`user_id`,`department_id`,`role_id`,`permission_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_contract_permission_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_pm`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_pm` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `contract_number` varchar(50) NOT NULL COMMENT '合同编号',
  `contract_name` varchar(100) NOT NULL COMMENT '合同名称',
  `project_id` int DEFAULT NULL COMMENT '项目ID',
  `customer_name` varchar(100) NOT NULL COMMENT '客户名称',
  `customer_contact` varchar(50) DEFAULT NULL COMMENT '客户联系人',
  `customer_phone` varchar(20) DEFAULT NULL COMMENT '客户联系电话',
  `customer_email` varchar(100) DEFAULT NULL COMMENT '客户邮箱',
  `contract_amount` decimal(15,2) DEFAULT '0.00' COMMENT '合同金额',
  `status` int DEFAULT '0' COMMENT '合同状态：0草稿 1待签署 2已签署 3执行中 4已完成 5已终止',
  `sign_date` datetime DEFAULT NULL COMMENT '合同签署日期',
  `effective_date` datetime DEFAULT NULL COMMENT '合同生效日期',
  `expiry_date` datetime DEFAULT NULL COMMENT '合同到期日期',
  `contract_content` text COMMENT '合同内容',
  `payment_method` varchar(50) DEFAULT NULL COMMENT '付款方式',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `update_user_id` int DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',
  `del_flag` int DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `version` int DEFAULT '1' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_number` (`contract_number`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_customer_name` (`customer_name`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_pm_contract_project` FOREIGN KEY (`project_id`) REFERENCES `hou_pm_project` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同附件-项目合同';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_provider`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_provider` (
  `contract_id` int NOT NULL COMMENT '合同id',
  `provider_id` int DEFAULT NULL COMMENT '服务商id',
  PRIMARY KEY (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='合同附表-服务商';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_contract_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_contract_template` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_code` varchar(20) DEFAULT NULL COMMENT '模板编码',
  `category_id` int DEFAULT NULL COMMENT '合同分类ID',
  `category_name` varchar(50) DEFAULT NULL COMMENT '合同分类名称',
  `template_content` longtext NOT NULL COMMENT '模板内容',
  `description` varchar(200) DEFAULT NULL COMMENT '模板描述',
  `variables` text COMMENT '模板变量配置，JSON格式',
  `status` int DEFAULT '1' COMMENT '状态：0禁用 1启用',
  `sort_order` int DEFAULT '0' COMMENT '排序值，数值越小越靠前',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `update_user_id` int DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',
  `del_flag` int DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  `org_id` varchar(255) DEFAULT NULL COMMENT '组织ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OA合同模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_daily_report`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_daily_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(32) DEFAULT NULL COMMENT '组织ID(多租户隔离)',
  `report_date` date DEFAULT NULL COMMENT '汇报日期',
  `report_start_time` datetime DEFAULT NULL COMMENT '汇报开始时间',
  `report_end_time` datetime DEFAULT NULL COMMENT '汇报结束时间',
  `receiver_ids` varchar(1024) DEFAULT '' COMMENT '汇报给员工ID列表(逗号分隔)',
  `receiver_names` varchar(1024) DEFAULT '' COMMENT '汇报给员工姓名(逗号分隔,冗余展示用)',
  `project_no` varchar(512) DEFAULT '' COMMENT '项目编号(必填,多个逗号分隔)',
  `task_assigned` text COMMENT '今日分配任务数(实际主管分配任务,文本描述)',
  `task_completed` text COMMENT '今日完成任务数(实际完成数量,文本描述)',
  `zentao_ids` varchar(512) DEFAULT '' COMMENT '禅道ID(必填,多个英文逗号分隔)',
  `coordination_work` text COMMENT '需协调工作',
  `attachment_urls` varchar(2048) DEFAULT '' COMMENT '附件URL列表(逗号分隔,最多9个)',
  `attachment_names` varchar(2048) DEFAULT '' COMMENT '附件名列表(逗号分隔,与 URL 一一对应)',
  `send_to_group_chat` tinyint DEFAULT '0' COMMENT '是否同时发送到群聊 0-否 1-是',
  `reporter_id` bigint DEFAULT NULL COMMENT '汇报人员工ID',
  `reporter_name` varchar(64) DEFAULT '' COMMENT '汇报人姓名(冗余展示)',
  `status` tinyint DEFAULT '1' COMMENT '状态 0-草稿 1-已提交',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志 0-正常 1-删除',
  PRIMARY KEY (`id`),
  KEY `idx_org_reporter_date` (`org_id`,`reporter_id`,`report_date`),
  KEY `idx_report_date` (`report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OA-日报';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_default`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_default` (
  `id` int NOT NULL,
  `num` varchar(255) DEFAULT '' COMMENT '序号',
  `operator_id` varchar(255) DEFAULT '' COMMENT '操作员',
  `import_time` datetime DEFAULT NULL COMMENT '导入时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='默认分组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_education`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_education` (
  `id` int NOT NULL,
  `work_edu` enum('1','2','3','4','5','6','7','8','9','10','11','12') DEFAULT '1' COMMENT '学历',
  `work_school` varchar(255) DEFAULT '' COMMENT '毕业院校',
  `work_graduation_time` date DEFAULT NULL COMMENT '毕业时间',
  `work_major` varchar(255) DEFAULT '' COMMENT '所学专业',
  `teaching_type` enum('1','2','3','4','5','6','7') DEFAULT '1' COMMENT '教学方式',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表-学历信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_employee_dingtalk`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_employee_dingtalk` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID',
  `ding_user_id` varchar(128) NOT NULL COMMENT '钉钉用户ID',
  `name` varchar(100) DEFAULT NULL COMMENT '姓名',
  `mobile` varchar(32) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像',
  `department_ids` varchar(512) DEFAULT NULL COMMENT '部门ID列表',
  `department_name` varchar(256) DEFAULT NULL COMMENT '部门名称',
  `position` varchar(128) DEFAULT NULL COMMENT '职位',
  `status` tinyint DEFAULT '1' COMMENT '1在职 0离职',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_ding_user` (`org_id`,`ding_user_id`),
  KEY `idx_org_mobile` (`org_id`,`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='钉钉员工分表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_employee_feishu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_employee_feishu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID',
  `lark_user_id` varchar(128) NOT NULL COMMENT '飞书用户ID',
  `name` varchar(100) DEFAULT NULL COMMENT '姓名',
  `mobile` varchar(32) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像',
  `department_ids` varchar(512) DEFAULT NULL COMMENT '部门ID列表',
  `department_name` varchar(256) DEFAULT NULL COMMENT '部门名称',
  `position` varchar(128) DEFAULT NULL COMMENT '职位',
  `status` tinyint DEFAULT '1' COMMENT '1在职 0离职',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_lark_user` (`org_id`,`lark_user_id`),
  KEY `idx_org_mobile` (`org_id`,`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='飞书员工分表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_employee_platform`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_employee_platform` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(50) NOT NULL COMMENT '组织ID',
  `employee_id` int NOT NULL COMMENT '主表 hou_oa_base.id',
  `platform` varchar(32) NOT NULL COMMENT 'wechat-work/dingtalk/feishu/platform',
  `platform_user_id` varchar(128) NOT NULL COMMENT '第三方用户ID',
  `platform_staff_id` bigint DEFAULT NULL COMMENT '分表记录ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_platform_user` (`org_id`,`platform`,`platform_user_id`),
  KEY `idx_employee` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工主表与第三方分表关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_employee_salary`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_employee_salary` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` int NOT NULL COMMENT '员工ID',
  `employee_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '员工编号',
  `employee_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '员工姓名',
  `department_id` int DEFAULT NULL COMMENT '部门ID',
  `position_id` int DEFAULT NULL COMMENT '职位ID',
  `salary_standard` text COLLATE utf8mb4_unicode_ci COMMENT '薪资标准，JSON格式存储薪资项目及金额',
  `base_salary` decimal(12,2) DEFAULT '0.00' COMMENT '基本工资',
  `position_salary` decimal(12,2) DEFAULT '0.00' COMMENT '岗位工资',
  `performance_salary` decimal(12,2) DEFAULT '0.00' COMMENT '绩效工资',
  `allowance` decimal(12,2) DEFAULT '0.00' COMMENT '津贴补贴',
  `secrecy` decimal(12,2) DEFAULT '0.00' COMMENT '保密费',
  `post_allowance` decimal(12,2) DEFAULT '0.00' COMMENT '岗位津贴',
  `meal_subsidy` decimal(12,2) DEFAULT '0.00' COMMENT '餐补',
  `other_subsidy` decimal(12,2) DEFAULT '0.00' COMMENT '其他补贴',
  `hourly_ot_rate` decimal(10,2) DEFAULT '0.00' COMMENT '加班小时单价',
  `social_insurance_base` decimal(12,2) DEFAULT '0.00' COMMENT '社保基数',
  `personal_pension` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '个人养老保险',
  `personal_medical` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '个人医疗保险',
  `personal_unemployment` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '个人失业保险',
  `personal_social_total` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '个人社保月合计（工资代扣）',
  `company_pension` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '公司养老保险',
  `company_medical` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '公司医疗保险',
  `company_unemployment` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '公司失业保险',
  `company_work_injury` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '公司工伤保险',
  `company_social_total` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '公司社保月合计（不从工资扣除）',
  `housing_fund_base` decimal(12,2) DEFAULT '0.00' COMMENT '公积金基数',
  `tax_deduction` decimal(12,2) DEFAULT '0.00' COMMENT '个税专项附加扣除',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `status` tinyint DEFAULT '1' COMMENT '状态：0=禁用，1=启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_position_id` (`position_id`),
  KEY `idx_status_effective` (`status`,`effective_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工薪资档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_expense_application`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_expense_application` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '报销申请ID',
  `application_no` varchar(50) NOT NULL COMMENT '申请编号',
  `applicant_id` int NOT NULL COMMENT '申请人ID',
  `applicant_name` varchar(50) NOT NULL COMMENT '申请人姓名',
  `department_id` int DEFAULT NULL COMMENT '部门ID',
  `department_name` varchar(50) DEFAULT NULL COMMENT '部门名称',
  `category_id` int NOT NULL COMMENT '费用分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '费用分类名称',
  `expense_reason` varchar(500) DEFAULT NULL COMMENT '报销事由',
  `total_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '总金额',
  `currency` varchar(10) DEFAULT 'CNY' COMMENT '币种',
  `status` int DEFAULT '0' COMMENT '申请状态：0草稿 1待审批 2审批中 3已通过 4已拒绝 5已撤回 6已完成',
  `process_instance_id` varchar(50) DEFAULT NULL COMMENT '流程实例ID',
  `current_approver_id` int DEFAULT NULL COMMENT '当前审批人ID',
  `current_approver_name` varchar(50) DEFAULT NULL COMMENT '当前审批人姓名',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `approval_time` datetime DEFAULT NULL COMMENT '审批完成时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `attachment_paths` text COMMENT '附件路径，JSON格式',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `update_user_id` int DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',
  `del_flag` int DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_apply_time` (`apply_time`),
  KEY `idx_process_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OA费用报销申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_expense_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_expense_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '费用分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `category_code` varchar(20) DEFAULT NULL COMMENT '分类编码',
  `parent_id` int DEFAULT '0' COMMENT '父级分类ID，0表示顶级分类',
  `level_path` varchar(255) DEFAULT NULL COMMENT '层级路径，如：0,1,2',
  `level` int DEFAULT '1' COMMENT '层级，从1开始',
  `sort_order` int DEFAULT '0' COMMENT '排序值，数值越小越靠前',
  `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
  `status` int DEFAULT '1' COMMENT '状态：0禁用 1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `update_user_id` int DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',
  `del_flag` int DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OA费用分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_expense_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_expense_detail` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '报销明细ID',
  `application_id` int NOT NULL COMMENT '报销申请ID',
  `expense_date` date NOT NULL COMMENT '费用发生日期',
  `expense_type` varchar(50) NOT NULL COMMENT '费用类型',
  `expense_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '费用金额',
  `currency` varchar(10) DEFAULT 'CNY' COMMENT '币种',
  `description` varchar(500) DEFAULT NULL COMMENT '费用描述',
  `attachment_paths` text COMMENT '附件路径，JSON格式',
  `sort_order` int DEFAULT '0' COMMENT '排序值',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `update_user_id` int DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',
  `del_flag` int DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_expense_date` (`expense_date`),
  KEY `idx_expense_type` (`expense_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OA费用报销明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_expense_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_expense_template` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_code` varchar(20) DEFAULT NULL COMMENT '模板编码',
  `category_id` int DEFAULT NULL COMMENT '费用分类ID',
  `category_name` varchar(50) DEFAULT NULL COMMENT '费用分类名称',
  `description` varchar(200) DEFAULT NULL COMMENT '模板描述',
  `required_fields` text COMMENT '必填字段配置，JSON格式',
  `default_approvers` text COMMENT '默认审批人配置，JSON格式',
  `status` int DEFAULT '1' COMMENT '状态：0禁用 1启用',
  `sort_order` int DEFAULT '0' COMMENT '排序值，数值越小越靠前',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `update_user_id` int DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',
  `del_flag` int DEFAULT '0' COMMENT '删除标记：0正常 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OA费用报销模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_family`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_family` (
  `id` int NOT NULL,
  `family_relation` enum('1','2','3','4','5','6','7','8','9','10','11','12','13') DEFAULT '1' COMMENT '关系（家人）',
  `family_mobile` varchar(255) DEFAULT '' COMMENT '电话（家人）',
  `family_username` varchar(255) DEFAULT '' COMMENT '姓名（家人）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表-家庭信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_fieldgroup`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_fieldgroup` (
  `id` smallint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT '' COMMENT '模型名称',
  `table` varchar(255) DEFAULT '' COMMENT '表名',
  `fields` text COMMENT '字段列表',
  `createtime` bigint DEFAULT NULL COMMENT '创建时间',
  `updatetime` bigint DEFAULT NULL COMMENT '更新时间',
  `setting` text COMMENT '模型配置',
  `weigh` int DEFAULT '0' COMMENT '权重',
  `issystem` tinyint(1) DEFAULT '0' COMMENT '是否内置组',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容模型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_fields`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_fields` (
  `id` smallint unsigned NOT NULL AUTO_INCREMENT,
  `group_id` int NOT NULL DEFAULT '0' COMMENT '分组ID',
  `name` char(30) DEFAULT '' COMMENT '名称',
  `type` varchar(30) DEFAULT '' COMMENT '类型',
  `title` varchar(255) DEFAULT '' COMMENT '标题',
  `content` text COMMENT '内容',
  `filterlist` text COMMENT '筛选列表',
  `defaultvalue` varchar(100) DEFAULT '' COMMENT '默认值',
  `rule` varchar(100) DEFAULT '' COMMENT '验证规则',
  `msg` varchar(100) DEFAULT '' COMMENT '错误消息',
  `ok` varchar(100) DEFAULT '' COMMENT '成功消息',
  `tip` varchar(100) DEFAULT '' COMMENT '提示消息',
  `decimals` tinyint(1) DEFAULT NULL COMMENT '小数点',
  `length` mediumint DEFAULT NULL COMMENT '长度',
  `minimum` smallint DEFAULT NULL COMMENT '最小数量',
  `maximum` smallint unsigned NOT NULL DEFAULT '0' COMMENT '最大数量',
  `extend` varchar(255) DEFAULT '' COMMENT '扩展信息',
  `setting` varchar(1500) DEFAULT '' COMMENT '配置信息',
  `weigh` int NOT NULL DEFAULT '0' COMMENT '排序',
  `createtime` bigint DEFAULT NULL COMMENT '添加时间',
  `updatetime` bigint DEFAULT NULL COMMENT '更新时间',
  `isorder` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可排序',
  `isfilter` tinyint(1) NOT NULL DEFAULT '0' COMMENT '筛选',
  `issystem` tinyint(1) DEFAULT '0' COMMENT '是否内置',
  `status` enum('normal','hidden') NOT NULL COMMENT '状态',
  PRIMARY KEY (`id`),
  KEY `group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模型字段表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_fund`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_fund` (
  `id` int NOT NULL,
  `fund_account` varchar(255) DEFAULT '' COMMENT '个人公积金账号',
  `security_account` varchar(255) DEFAULT '' COMMENT '个人社保账号',
  `security_status` enum('1','2','3') DEFAULT '1' COMMENT '社保账号状态',
  `fund_status` enum('1','2','3','4') DEFAULT '1' COMMENT '公积金状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社保信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_holiday_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_holiday_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID（NULL 表示全平台通用）',
  `holiday_date` date NOT NULL COMMENT '节假日/调休日期',
  `holiday_name` varchar(100) NOT NULL COMMENT '节假日名称',
  `holiday_type` varchar(16) NOT NULL DEFAULT 'legal' COMMENT '类型：legal=法定节假日 / adjust=调休放假 / makeup=补班',
  `is_working` tinyint NOT NULL DEFAULT '0' COMMENT '是否上班：0=假日(应交日剔除) 1=补班工作日(计入应交日)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `delete_time` datetime DEFAULT NULL COMMENT '软删除时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_org_date` (`org_id`,`holiday_date`,`delete_time`) COMMENT '同组织同日期唯一',
  KEY `idx_date` (`holiday_date`),
  KEY `idx_type` (`holiday_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='法定节假日配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_induction`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_induction` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL COMMENT '员工',
  `status` tinyint(1) DEFAULT '1' COMMENT '入职表提交状态:1=待提交,2=已提交',
  `state` tinyint(1) DEFAULT '1' COMMENT '状态:0=隐藏,1=正常',
  `createtime` bigint DEFAULT NULL COMMENT '邀请时间',
  `updatetime` bigint DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='入职管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_job`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_job` (
  `id` int NOT NULL,
  `number` varchar(255) DEFAULT '' COMMENT '工号',
  `type` enum('1','2','3','4','5','6','7') DEFAULT '1' COMMENT '员工类型',
  `join_date` date DEFAULT NULL COMMENT '入职日期',
  `first_working_date` date DEFAULT NULL COMMENT '首次参加工作日期',
  `state` tinyint(1) DEFAULT NULL COMMENT '员工状态',
  `confirmation_date` date DEFAULT NULL COMMENT '转正日期',
  `probation_period` enum('1','2','3','4','5','6','12','0') DEFAULT '3' COMMENT '试用期',
  `contract_exp_date` date DEFAULT NULL COMMENT '当前合同到期时间',
  `resignation_date` date DEFAULT NULL COMMENT '离职日期',
  `base_pay` varchar(255) DEFAULT '' COMMENT '基本工资',
  `resignation_type` enum('1','2','3','4') DEFAULT '1' COMMENT '离职类型',
  `work_email` varchar(255) DEFAULT '' COMMENT '工作邮箱',
  `work_address` varchar(255) DEFAULT '' COMMENT '工作地点',
  `work_city` varchar(255) DEFAULT '' COMMENT '工作城市',
  `secretary_years` int DEFAULT NULL COMMENT '司龄',
  `work_years` int DEFAULT NULL COMMENT '工龄',
  `work_status` varchar(255) DEFAULT '' COMMENT '工作状态',
  `resignation_reason` varchar(255) DEFAULT '' COMMENT '离职原因',
  `recruitment_channels` enum('1','2','3','4','5') DEFAULT '1' COMMENT '招聘渠道',
  `referrer` varchar(255) DEFAULT '' COMMENT '推荐人',
  `department_id` varchar(255) DEFAULT '' COMMENT '部门',
  `job_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '职位',
  `org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公司组织',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_leave_apply`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_leave_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID',
  `employee_id` int NOT NULL COMMENT '员工ID',
  `employee_name` varchar(100) DEFAULT NULL COMMENT '员工姓名（冗余）',
  `department_id` int DEFAULT NULL COMMENT '部门ID（快照）',
  `department_name` varchar(100) DEFAULT NULL COMMENT '部门名称（快照）',
  `leave_type` varchar(32) NOT NULL COMMENT '请假类型：personal/sick/annual/marriage/maternity/bereavement/other',
  `leave_start` datetime NOT NULL COMMENT '请假开始时间',
  `leave_end` datetime NOT NULL COMMENT '请假结束时间',
  `leave_days` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '请假天数（含小数，半天=0.5）',
  `reason` varchar(500) DEFAULT NULL COMMENT '请假事由',
  `attachment_url` varchar(512) DEFAULT NULL COMMENT '附件URL（病假证明等）',
  `status` varchar(16) NOT NULL DEFAULT 'pending' COMMENT '状态：pending=待审批 / approved=已批准 / rejected=已驳回 / cancelled=已撤销',
  `approver_id` int DEFAULT NULL COMMENT '审批人ID',
  `approver_name` varchar(100) DEFAULT NULL COMMENT '审批人姓名',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) DEFAULT NULL COMMENT '审批备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `delete_time` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_emp` (`org_id`,`employee_id`),
  KEY `idx_leave_range` (`employee_id`,`leave_start`,`leave_end`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工请假申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_overtime_apply`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_overtime_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID',
  `employee_id` int NOT NULL COMMENT '员工ID',
  `employee_name` varchar(100) DEFAULT NULL COMMENT '员工姓名（冗余）',
  `department_id` int DEFAULT NULL COMMENT '部门ID（快照）',
  `department_name` varchar(100) DEFAULT NULL COMMENT '部门名称（快照）',
  `overtime_date` date NOT NULL COMMENT '加班日期',
  `start_time` varchar(16) NOT NULL COMMENT '开始时间 HH:mm',
  `end_time` varchar(16) NOT NULL COMMENT '结束时间 HH:mm',
  `overtime_hours` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '加班时长（小时）',
  `overtime_type` varchar(32) DEFAULT 'workday' COMMENT 'workday/weekend/holiday',
  `reason` varchar(500) DEFAULT NULL COMMENT '加班事由',
  `status` varchar(16) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected/cancelled/completed',
  `approver_id` int DEFAULT NULL COMMENT '审批人ID',
  `approver_name` varchar(100) DEFAULT NULL COMMENT '审批人姓名',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) DEFAULT NULL COMMENT '审批备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `delete_time` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ot_org_emp` (`org_id`,`employee_id`),
  KEY `idx_ot_date` (`org_id`,`overtime_date`),
  KEY `idx_ot_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工加班申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_payslip_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_payslip_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `value` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_payslip_paylog`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_payslip_paylog` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(255) DEFAULT NULL COMMENT '组织ID',
  `salary_month` varchar(7) DEFAULT NULL COMMENT '工资月份 YYYY-MM',
  `payroll_id` bigint DEFAULT NULL COMMENT '关联 hou_oa_salary_payroll.id',
  `createtime` bigint DEFAULT NULL COMMENT '创建时间',
  `notice` varchar(255) DEFAULT NULL COMMENT '公告',
  `timingtime` varchar(255) DEFAULT NULL,
  `nowdate` varchar(255) DEFAULT NULL COMMENT '时间',
  `type` varchar(255) DEFAULT NULL COMMENT '发送方式：1=微信，2=邮件，3=企业微信，4=短信',
  `pid` int DEFAULT NULL COMMENT '上级id',
  `status` enum('0','1','2') NOT NULL DEFAULT '0' COMMENT '状态:0=失败,1=成功,2=撤回',
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `phone` varchar(255) DEFAULT NULL COMMENT '电话',
  `price` varchar(255) NOT NULL DEFAULT '0',
  `updatetime` bigint DEFAULT NULL COMMENT '更新时间',
  `company` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `user_id` int DEFAULT NULL,
  `errs` varchar(255) DEFAULT NULL COMMENT '错误',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否签收 0否 1是',
  `read_time` bigint DEFAULT NULL COMMENT '签收时间戳(秒)',
  `send_time` bigint DEFAULT NULL COMMENT '发送时间戳(秒)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_paylog_org_month` (`org_id`,`salary_month`),
  KEY `idx_paylog_payroll` (`payroll_id`),
  KEY `idx_paylog_user_month` (`user_id`,`salary_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='工资条记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_payslip_paylog_auth`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_payslip_paylog_auth` (
  `id` int NOT NULL AUTO_INCREMENT,
  `paylog_id` int DEFAULT NULL COMMENT '上传记录id',
  `paylog_rule_id` int DEFAULT NULL COMMENT '上传字段id',
  `content` varchar(255) DEFAULT NULL COMMENT '内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='上传字段关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_payslip_paylog_rule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_payslip_paylog_rule` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(50) DEFAULT '1' COMMENT '组织ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(50) NOT NULL COMMENT '规则类型：attendance=考勤扣款，performance=绩效奖金，reward=奖惩规则，insurance=社保公积金，tax=个税计算',
  `rule_category` varchar(50) DEFAULT NULL COMMENT '规则分类：late=迟到，early_leave=早退，absent=旷工，leave=请假，bonus=奖金，reward=奖励，punishment=惩罚，social=社保，housing=公积金，tax=个税',
  `rule_value` decimal(10,2) DEFAULT NULL COMMENT '规则数值',
  `rule_unit` varchar(20) DEFAULT NULL COMMENT '规则单位：元/次，元/天，元，%',
  `rule_formula` varchar(200) DEFAULT NULL COMMENT '计算公式或规则描述',
  `rule_param` varchar(16) DEFAULT NULL COMMENT '扩展参数：餐补=下班门槛HH:mm，none=不限制',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用：0=禁用，1=启用',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_id` (`org_id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='薪资计算规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_payslip_paytype`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_payslip_paytype` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '类型名称',
  `is_enabled` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否启用:1启动2禁用',
  `org_id` int DEFAULT NULL COMMENT '组织ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='发薪类型';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_payslip_setlog`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_payslip_setlog` (
  `id` int NOT NULL AUTO_INCREMENT,
  `times` varchar(255) DEFAULT '' COMMENT '发薪时间',
  `createtime` bigint DEFAULT NULL COMMENT '创建时间',
  `timing` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL COMMENT '发薪类型',
  `static` int NOT NULL DEFAULT '3' COMMENT '定时发送状态:1=待发送,2=发送中,3=发送完成',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='发送记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_payslip_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_payslip_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '姓名',
  `phone` varchar(20) NOT NULL DEFAULT '' COMMENT '手机',
  `openid` varchar(255) DEFAULT NULL COMMENT 'openid',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `createtime` bigint DEFAULT NULL COMMENT '创建时间',
  `userid` varchar(255) DEFAULT NULL COMMENT '企业微信id',
  `nickname` varchar(255) DEFAULT NULL,
  `avator` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_quick_note`
--
-- ⚠️ 已废弃（2026-09-21）：临时记事本已并入 hou_oa_todo，速记统一以
-- category='quicknote' + status='note' 存储。本表仅作历史数据归档，应用层不再读写。
-- 保留建表语句是为了让 20260921-merge-quicknote-into-todo.sql 在任何环境都能安全重跑
-- （新装环境是一张空表，迁移影响 0 行），迁移完成后可按需手工 DROP。
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_quick_note` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `owner_id` bigint NOT NULL COMMENT '笔记所有人ID（仅本人可见）',
  `owner_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '笔记所有人名称',
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文本内容（记忆/随手记）',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '图片URL列表(JSON数组)',
  `attachments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '附件列表(JSON数组: [{url, name, size}])',
  `org_id` bigint DEFAULT NULL COMMENT '组织ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志: 0-正常 1-删除',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='首页临时记事本便签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_regular`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_regular` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` int NOT NULL COMMENT '员工',
  `confirmation_date` date DEFAULT NULL COMMENT '转正日期',
  `base_pay` float DEFAULT NULL COMMENT '工资',
  `operator_id` int DEFAULT NULL COMMENT '操作人',
  `createtime` bigint DEFAULT NULL COMMENT '创建时间',
  `updatetime` bigint DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='转正管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_report_deduction`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_report_deduction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID',
  `salary_month` varchar(7) NOT NULL COMMENT '薪资月份 YYYY-MM',
  `employee_id` int NOT NULL COMMENT '员工ID',
  `employee_name` varchar(100) DEFAULT NULL COMMENT '员工姓名（冗余）',
  `department_id` int DEFAULT NULL COMMENT '部门ID（快照）',
  `department_name` varchar(100) DEFAULT NULL COMMENT '部门名称（快照）',
  `should_submit_days` int NOT NULL DEFAULT '0' COMMENT '当月应交天数（剔除周末/节假日/假期后）',
  `actual_submit_days` int NOT NULL DEFAULT '0' COMMENT '当月实际有效提交天数',
  `miss_count` int NOT NULL DEFAULT '0' COMMENT '当月未交次数 = should - actual',
  `continuous_n` int NOT NULL DEFAULT '0' COMMENT '连续扣款段长度（以本月为段末，含本月）',
  `tier` varchar(16) DEFAULT NULL COMMENT '档位：T1_first(首月20) / T1(×20) / T2(×40) / T3(×100)',
  `unit_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '档位单价：20/40/100',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '最终扣款金额',
  `appeal_status` tinyint NOT NULL DEFAULT '0' COMMENT '申诉状态：0=无申诉 1=申诉中 2=申诉通过(已重算) 3=申诉驳回',
  `appeal_approval_id` varchar(64) DEFAULT NULL COMMENT '申诉审批单号',
  `locked` tinyint NOT NULL DEFAULT '0' COMMENT '是否锁定：0=可重算 1=已写入薪资条不可改',
  `payroll_detail_id` bigint DEFAULT NULL COMMENT '关联 hou_oa_salary_payroll_detail.id',
  `compute_time` datetime DEFAULT NULL COMMENT '最后计算时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `delete_time` datetime DEFAULT NULL COMMENT '软删除时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_org_month_emp` (`org_id`,`salary_month`,`employee_id`,`delete_time`) COMMENT '同员工同月唯一',
  KEY `idx_org_month` (`org_id`,`salary_month`),
  KEY `idx_employee` (`employee_id`),
  KEY `idx_appeal` (`appeal_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日报扣款月度结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_report_submit_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_report_submit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID',
  `employee_id` int NOT NULL COMMENT '员工ID（关联 hou_emp_user.id）',
  `employee_name` varchar(100) DEFAULT NULL COMMENT '员工姓名（冗余）',
  `submit_date` date NOT NULL COMMENT '应交日期',
  `submit_time` datetime DEFAULT NULL COMMENT '实际提交时间，NULL 表示未交',
  `status` varchar(32) NOT NULL DEFAULT 'miss' COMMENT '状态：submitted=按时提交 / late=迟交 / miss=未交 / appeal_passed=申诉通过视为正常',
  `daily_report_id` bigint DEFAULT NULL COMMENT '关联 hou_oa_daily_report.id（若员工已在工作日报模块提交）',
  `appeal_approval_id` varchar(64) DEFAULT NULL COMMENT '申诉审批单号（hou_oa_approval.id）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `delete_time` datetime DEFAULT NULL COMMENT '软删除时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_org_emp_date` (`org_id`,`employee_id`,`submit_date`,`delete_time`) COMMENT '同员工同日期唯一（软删除后可重建）',
  KEY `idx_org_date` (`org_id`,`submit_date`),
  KEY `idx_status` (`status`),
  KEY `idx_daily_report` (`daily_report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日报提交流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_resignation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_resignation` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL COMMENT '员工',
  `department_id` int DEFAULT NULL COMMENT '离职前部门',
  `position_id` int DEFAULT NULL COMMENT '离职前职位',
  `state` tinyint(1) DEFAULT NULL COMMENT '离职前状态',
  `resignation_type` tinyint(1) DEFAULT NULL COMMENT '离职类型',
  `resignation_date` date DEFAULT NULL COMMENT '离职日期',
  `resignation_reason` varchar(255) DEFAULT NULL COMMENT '离职原因',
  `operator_id` int DEFAULT NULL COMMENT '操作人',
  `createtime` bigint DEFAULT NULL COMMENT '创建时间',
  `updatetime` bigint DEFAULT NULL COMMENT '更新时间',
  `status` tinyint(1) DEFAULT NULL COMMENT '状态:1=待离职,2=已离职,3=已撤回',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='离职纪录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_salary_import_job`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_salary_import_job` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `org_id` varchar(64) NOT NULL COMMENT '组织ID',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(512) NOT NULL COMMENT '本地存储路径',
  `salary_month` varchar(16) DEFAULT NULL COMMENT '指定月份 YYYY-MM，可空',
  `overwrite_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否覆盖同月数据',
  `status` varchar(32) NOT NULL DEFAULT 'queued' COMMENT 'queued/processing/need_confirm/completed/failed',
  `message` varchar(1000) DEFAULT NULL COMMENT '状态说明或错误信息',
  `result_json` mediumtext COMMENT '导入结果 JSON',
  `create_by` varchar(64) DEFAULT NULL COMMENT '提交人 userId',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_org_status` (`org_id`,`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资 Excel 导入异步任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_salary_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_salary_item` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(50) DEFAULT '1' COMMENT '组织ID',
  `item_code` varchar(50) NOT NULL COMMENT '项目编码',
  `item_name` varchar(100) NOT NULL COMMENT '项目名称',
  `item_type` int DEFAULT NULL COMMENT '项目类型：1=基本工资，2=绩效工资，3=津贴补贴，4=扣款项，5=社保公积金，6=个税',
  `category` varchar(20) DEFAULT 'addition' COMMENT '分类：addition=加项,deduction=扣项,stat=统计项,info=信息项',
  `calculation_type` int DEFAULT '3' COMMENT '计算类型：1=固定值，2=公式计算，3=手动输入',
  `formula` varchar(200) DEFAULT NULL COMMENT '计算公式',
  `default_value` varchar(50) DEFAULT NULL COMMENT '默认值',
  `is_taxable` int DEFAULT '0' COMMENT '是否计税：0=否,1=是',
  `is_display` int DEFAULT '1' COMMENT '是否显示：0=否,1=是',
  `display_order` int DEFAULT '0',
  `status` int DEFAULT '1' COMMENT '状态：0=禁用,1=启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(50) DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_org_code` (`org_id`,`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='薪资项目字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_salary_no_acceptance_tier`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_salary_no_acceptance_tier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) NOT NULL COMMENT '组织ID',
  `rule_id` int NOT NULL COMMENT '薪资规则ID',
  `month_no` int NOT NULL COMMENT '连续无项目验收月数（1-6）',
  `performance_deduction_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '绩效工资扣除比例（%）',
  `position_deduction_rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '岗位工资扣除比例（%）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_org_rule_month` (`org_id`,`rule_id`,`month_no`),
  KEY `idx_org_rule` (`org_id`,`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='连续无项目验收阶梯扣款配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_salary_payment_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_salary_payment_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `payroll_id` bigint NOT NULL,
  `employee_id` int DEFAULT NULL,
  `employee_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `salary_month` varchar(7) COLLATE utf8mb4_general_ci NOT NULL,
  `payment_amount` decimal(18,2) NOT NULL,
  `payment_time` datetime NOT NULL,
  `operator_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `operator_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_salary_payment_org_payroll` (`org_id`,`payroll_id`),
  KEY `idx_salary_payment_org_month_employee` (`org_id`,`salary_month`,`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_salary_payroll`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_salary_payroll` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID',
  `salary_month` varchar(10) NOT NULL COMMENT '工资月份 YYYY-MM',
  `employee_id` int DEFAULT NULL COMMENT '员工ID',
  `employee_name` varchar(100) DEFAULT NULL COMMENT '员工姓名',
  `unit_name` varchar(100) DEFAULT NULL COMMENT '交付单元',
  `position_text` varchar(100) DEFAULT NULL COMMENT '职位',
  `bank_account` varchar(100) DEFAULT NULL COMMENT '银行账号',
  `bank_name` varchar(100) DEFAULT NULL COMMENT '开户行',
  `id_card` varchar(50) DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
  `hired_date` varchar(50) DEFAULT NULL COMMENT '入职时间',
  `attendance_days` decimal(5,1) DEFAULT NULL COMMENT '出勤天数',
  `salary_subtotal` decimal(12,2) DEFAULT '0.00' COMMENT '工资小计',
  `sub_subtotal` decimal(12,2) DEFAULT '0.00' COMMENT '补助小计',
  `deduction_subtotal` decimal(12,2) DEFAULT '0.00' COMMENT '扣款小计',
  `gross_salary` decimal(12,2) DEFAULT '0.00' COMMENT '应发工资',
  `social_insurance` decimal(12,2) DEFAULT '0.00' COMMENT '代扣个人社保',
  `housing_fund` decimal(12,2) DEFAULT '0.00' COMMENT '代扣个人公积金',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '合计（应发-社保-公积金）',
  `personal_tax` decimal(12,2) DEFAULT '0.00' COMMENT '个税',
  `net_salary` decimal(12,2) DEFAULT '0.00' COMMENT '实发工资',
  `status` varchar(20) DEFAULT 'draft' COMMENT '状态：draft=草稿,pending=待审核,published=已发放',
  `source` varchar(20) DEFAULT 'manual' COMMENT '来源：manual=手工,excel=Excel导入,auto=自动生成',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `delete_time` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_org_month_emp` (`org_id`,`salary_month`,`employee_name`,`delete_time`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_salary_month` (`salary_month`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_employee_name` (`employee_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='月度工资条主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_salary_payroll_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_salary_payroll_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payroll_id` bigint NOT NULL COMMENT '工资条ID（hou_oa_salary_payroll.id）',
  `item_id` int DEFAULT NULL COMMENT '薪资项目ID',
  `item_code` varchar(50) DEFAULT NULL COMMENT '项目编码',
  `item_name` varchar(100) DEFAULT NULL COMMENT '项目名称',
  `category` varchar(20) DEFAULT NULL COMMENT '分类：addition=加项,deduction=扣项,stat=统计项,info=信息项',
  `amount` decimal(12,2) DEFAULT '0.00' COMMENT '金额',
  `display_order` int DEFAULT '0' COMMENT '显示顺序',
  `calculation_detail` json DEFAULT NULL COMMENT '计算快照',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_payroll_id` (`payroll_id`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='月度工资条明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_salary_blackwhite`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_salary_blackwhite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) NOT NULL COMMENT '组织ID',
  `employee_id` int NOT NULL COMMENT '员工ID（关联 hou_oa_base.id）',
  `employee_name` varchar(100) DEFAULT NULL COMMENT '员工姓名（冗余展示）',
  `list_type` varchar(10) NOT NULL DEFAULT 'white' COMMENT '名单类型：white=白名单，black=黑名单',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注（切换原因等）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `delete_time` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_org_employee` (`org_id`, `employee_id`),
  KEY `idx_org_type` (`org_id`, `list_type`),
  KEY `idx_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='薪资计算黑白名单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_salary_structure`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_salary_structure` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID',
  `employee_id` int DEFAULT NULL COMMENT '员工ID（关联 hou_emp_user.id）',
  `employee_name` varchar(100) DEFAULT NULL COMMENT '员工姓名（冗余，便于 Excel 导入时姓名匹配）',
  `unit_name` varchar(100) DEFAULT NULL COMMENT '交付单元',
  `position_text` varchar(100) DEFAULT NULL COMMENT '职位描述',
  `item_id` int NOT NULL COMMENT '薪资项目ID（关联 hou_oa_salary_item.id）',
  `item_code` varchar(50) DEFAULT NULL COMMENT '项目编码（冗余）',
  `item_name` varchar(100) DEFAULT NULL COMMENT '项目名称（冗余）',
  `default_amount` decimal(12,2) DEFAULT '0.00' COMMENT '默认金额',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0=禁用，1=启用',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `delete_time` datetime DEFAULT NULL COMMENT '软删除时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_id` (`org_id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_employee_name` (`employee_name`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='员工薪资结构(模板)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_service_provider`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_service_provider` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组织ID',
  `provider_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务商名称',
  `provider_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '服务商编码',
  `provider_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '服务商类型（供应商/服务商/合作伙伴）',
  `contact_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人邮箱',
  `company_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公司地址',
  `business_scope` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经营范围',
  `credit_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '信用等级',
  `cooperation_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '合作状态（1:合作中 2:暂停 3:已终止）',
  `cooperation_start_date` date DEFAULT NULL COMMENT '合作开始日期',
  `cooperation_end_date` date DEFAULT NULL COMMENT '合作结束日期',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `create_user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人ID',
  `create_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人ID',
  `update_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人姓名',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志（0:未删除 1:已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_provider_code` (`provider_code`),
  KEY `idx_provider_type` (`provider_type`),
  KEY `idx_cooperation_status` (`cooperation_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OA服务商管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_supplier_payment_approval`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_supplier_payment_approval` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '审批ID',
  `spending_id` int NOT NULL COMMENT '财务支出记录ID(hou_finance_bookkeeping_spending_log.id)',
  `supplier_id` int DEFAULT NULL COMMENT '服务商ID',
  `supplier_name` varchar(100) DEFAULT NULL COMMENT '服务商名称',
  `amount` decimal(12,2) DEFAULT NULL COMMENT '打款金额',
  `approval_title` varchar(200) NOT NULL COMMENT '审批标题',
  `apply_reason` varchar(500) DEFAULT NULL COMMENT '申请原因',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审批 1已通过 2已拒绝 3已撤回 4审批中',
  `current_node_index` int NOT NULL DEFAULT '0' COMMENT '当前节点序号(从0开始)',
  `current_node_name` varchar(100) DEFAULT NULL COMMENT '当前节点名称',
  `current_approver_user_id` varchar(64) DEFAULT NULL COMMENT '当前审批人用户ID',
  `current_approver_name` varchar(50) DEFAULT NULL COMMENT '当前审批人姓名',
  `total_nodes` int NOT NULL DEFAULT '0' COMMENT '审批节点总数',
  `org_id` varchar(64) DEFAULT NULL COMMENT '组织ID',
  `create_user_id` varchar(64) DEFAULT NULL COMMENT '申请人用户ID',
  `create_user_name` varchar(50) DEFAULT NULL COMMENT '申请人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_spending_id` (`spending_id`),
  KEY `idx_current_approver` (`current_approver_user_id`,`status`),
  KEY `idx_org_status` (`org_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务商打款审批主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_supplier_payment_approval_node`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_supplier_payment_approval_node` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `approval_id` int NOT NULL COMMENT '审批主表ID',
  `node_index` int NOT NULL COMMENT '节点序号',
  `node_name` varchar(100) NOT NULL COMMENT '节点名称',
  `approver_user_id` varchar(64) DEFAULT NULL COMMENT '审批人用户ID',
  `approver_employee_id` int DEFAULT NULL COMMENT '审批人员工ID',
  `approver_name` varchar(50) DEFAULT NULL COMMENT '审批人姓名',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审批 1已通过 2已拒绝 3已跳过',
  `approval_comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `approval_time` datetime DEFAULT NULL COMMENT '审批时间',
  `org_id` varchar(64) DEFAULT NULL COMMENT '组织ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_approval_id` (`approval_id`),
  KEY `idx_approver` (`approver_user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务商打款审批节点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_system_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_system_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `operation_type` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `operation_desc` varchar(500) DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` text COMMENT '请求参数',
  `response_result` text COMMENT '响应结果',
  `execution_time` bigint DEFAULT NULL COMMENT '执行时间(ms)',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `exception_info` text COMMENT '异常信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_operation_type` (`operation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_todo`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_todo` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '待办标题（长 SQL 粘贴不被截断；2026-10-19 由 varchar(255) 放宽，存量库见 scripts/sql/migrations/20261019-todo-title-text.sql）',
  `description` text COLLATE utf8mb4_general_ci COMMENT '待办描述',
  `owner_id` bigint NOT NULL COMMENT '待办所有人ID',
  `owner_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '待办所有人名称',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `creator_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人名称',
  `category` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类',
  `priority` tinyint DEFAULT '0' COMMENT '优先级: 0-普通 1-高 2-紧急',
  `status` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待处理 done-已完成 archived-已归档',
  `is_starred` tinyint DEFAULT '0' COMMENT '是否星标: 0-否 1-是',
  `is_later` tinyint DEFAULT '0' COMMENT '是否稍后处理: 0-否 1-是',
  `related_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关联类型: approval/workflow/task等',
  `related_id` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关联业务ID',
  `due_date` datetime DEFAULT NULL COMMENT '截止日期',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `remark` text COLLATE utf8mb4_general_ci COMMENT '备注',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '图片URL列表(JSON数组)',
  `attachments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '附件列表(JSON数组: [{url, name, size}])',
  `org_id` bigint DEFAULT NULL COMMENT '组织ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志: 0-正常 1-删除',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_status` (`status`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='待办事项表（含 category=quicknote 的随手记/速记）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_transfer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_transfer` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` int DEFAULT NULL COMMENT '员工',
  `position_type` tinyint(1) DEFAULT '1' COMMENT '调岗类型:1=平级调动,2=升级,3=降级',
  `salary_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '调薪类型:1=无,2=涨薪,3=降薪',
  `base_pay` float DEFAULT '0' COMMENT '基本工资',
  `effect_time` bigint DEFAULT NULL COMMENT '调动生效日期',
  `before` text NOT NULL COMMENT '调岗前部门/职位',
  `after` text NOT NULL COMMENT '调岗后部门/职位',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态:1=待生效,2=已生效,3=已撤销',
  `createtime` bigint DEFAULT NULL COMMENT '创建时间',
  `updatetime` bigint DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='调动管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_travel_apply`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_travel_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `org_id` varchar(50) DEFAULT NULL COMMENT '组织ID',
  `employee_id` int NOT NULL COMMENT '员工ID',
  `employee_name` varchar(100) DEFAULT NULL COMMENT '员工姓名（冗余）',
  `department_id` int DEFAULT NULL COMMENT '部门ID（快照）',
  `department_name` varchar(100) DEFAULT NULL COMMENT '部门名称（快照）',
  `destination` varchar(200) NOT NULL COMMENT '出差地点',
  `start_time` datetime NOT NULL COMMENT '出差开始时间',
  `end_time` datetime NOT NULL COMMENT '出差结束时间',
  `travel_days` decimal(6,2) DEFAULT '0.00' COMMENT '出差天数',
  `purpose` varchar(500) DEFAULT NULL COMMENT '出差事由',
  `transport` varchar(32) DEFAULT NULL COMMENT 'plane/train/car/other',
  `budget` decimal(12,2) DEFAULT '0.00' COMMENT '预算金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` varchar(16) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected/cancelled/ongoing/completed',
  `approver_id` int DEFAULT NULL COMMENT '审批人ID',
  `approver_name` varchar(100) DEFAULT NULL COMMENT '审批人姓名',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) DEFAULT NULL COMMENT '审批备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `delete_time` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_travel_org_emp` (`org_id`,`employee_id`),
  KEY `idx_travel_range` (`org_id`,`start_time`,`end_time`),
  KEY `idx_travel_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工出差申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_batch_customer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_batch_customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `mobile` varchar(32) NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `tag_names` varchar(512) DEFAULT NULL,
  `follow_userid` varchar(64) DEFAULT NULL,
  `follow_user_name` varchar(64) DEFAULT NULL,
  `status` varchar(32) DEFAULT 'pending' COMMENT 'pending/added/wait_pass',
  `assign_count` int DEFAULT '1',
  `add_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_status` (`org_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微批量添加客户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_bot`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_bot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `group_id` bigint DEFAULT NULL,
  `group_name` varchar(128) DEFAULT NULL,
  `name` varchar(128) NOT NULL,
  `webhook_url` varchar(512) DEFAULT NULL,
  `remark` varchar(512) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0禁用 1正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_group` (`org_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微机器人';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_bot_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_bot_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `name` varchar(128) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微机器人分组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_channel_code`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_channel_code` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `group_id` bigint DEFAULT NULL,
  `group_name` varchar(128) DEFAULT NULL,
  `activity_name` varchar(255) NOT NULL,
  `config_id` varchar(128) DEFAULT NULL,
  `qrcode_url` varchar(512) DEFAULT NULL,
  `recruit_count` int DEFAULT '0',
  `churn_count` int DEFAULT '0',
  `staff_count` int DEFAULT '0',
  `code_type` varchar(32) DEFAULT 'single' COMMENT 'single/multi',
  `scene` varchar(128) DEFAULT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_group` (`org_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微渠道活码';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_channel_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_channel_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `name` varchar(128) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微渠道码分组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_customer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `external_userid` varchar(64) NOT NULL,
  `name` varchar(128) DEFAULT NULL,
  `avatar` varchar(512) DEFAULT NULL,
  `customer_type` varchar(32) DEFAULT NULL COMMENT '微信用户/企业微信用户',
  `gender` tinyint DEFAULT '0',
  `follow_userid` varchar(64) DEFAULT NULL,
  `follow_user_name` varchar(64) DEFAULT NULL,
  `remark` varchar(512) DEFAULT NULL,
  `mobile_remark` varchar(64) DEFAULT NULL,
  `source` varchar(128) DEFAULT NULL,
  `tag_names` varchar(512) DEFAULT NULL,
  `add_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_ext` (`org_id`,`external_userid`),
  KEY `idx_follow` (`org_id`,`follow_userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微客户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_customer_churn`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_customer_churn` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `external_userid` varchar(64) DEFAULT NULL,
  `customer_name` varchar(128) DEFAULT NULL,
  `avatar` varchar(512) DEFAULT NULL,
  `customer_type` varchar(32) DEFAULT NULL,
  `gender` tinyint DEFAULT '0',
  `follow_userid` varchar(64) DEFAULT NULL,
  `follow_user_name` varchar(64) DEFAULT NULL,
  `remark` varchar(512) DEFAULT NULL,
  `mobile_remark` varchar(64) DEFAULT NULL,
  `source` varchar(128) DEFAULT NULL,
  `churn_type` varchar(32) DEFAULT NULL COMMENT 'employee_delete/customer_delete',
  `add_time` datetime DEFAULT NULL,
  `churn_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_churn` (`org_id`,`churn_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微流失客户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_department`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_department` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `wecom_dept_id` int NOT NULL COMMENT '企微部门ID',
  `parent_wecom_dept_id` int DEFAULT '0',
  `name` varchar(128) NOT NULL,
  `name_en` varchar(128) DEFAULT NULL,
  `order_num` int DEFAULT '0' COMMENT '权重/排序',
  `leader_userid` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_wecom_dept` (`org_id`,`wecom_dept_id`),
  KEY `idx_org_parent` (`org_id`,`parent_wecom_dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微部门';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_group_chat`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_group_chat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `chat_id` varchar(64) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL,
  `owner_userid` varchar(64) DEFAULT NULL,
  `owner_name` varchar(64) DEFAULT NULL,
  `notice` text,
  `member_count` int DEFAULT '0',
  `admin_count` int DEFAULT '0',
  `follow_status` varchar(32) DEFAULT NULL COMMENT '跟进人正常/离职等',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_owner` (`org_id`,`owner_userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微客户群';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_inherit_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_inherit_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `inherit_mode` varchar(32) NOT NULL COMMENT 'resign/onjob',
  `task_name` varchar(255) DEFAULT NULL,
  `transfer_type` varchar(32) NOT NULL COMMENT 'customer/group',
  `from_userid` varchar(64) DEFAULT NULL,
  `from_user_name` varchar(64) DEFAULT NULL,
  `from_dept_name` varchar(128) DEFAULT NULL,
  `to_userid` varchar(64) DEFAULT NULL,
  `to_user_name` varchar(64) DEFAULT NULL,
  `to_dept_name` varchar(128) DEFAULT NULL,
  `exec_status` varchar(32) DEFAULT 'pending',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_mode` (`org_id`,`inherit_mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微继承任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_kf_account`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_kf_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `open_kfid` varchar(64) DEFAULT NULL COMMENT '企微客服账号ID',
  `name` varchar(128) NOT NULL,
  `avatar` varchar(512) DEFAULT NULL,
  `kf_link` varchar(512) DEFAULT NULL,
  `has_api_permission` tinyint NOT NULL DEFAULT '0' COMMENT '0无 1有',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_kfid` (`org_id`,`open_kfid`),
  KEY `idx_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微客服账号';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_kf_servicer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_kf_servicer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `open_kfid` varchar(64) DEFAULT NULL,
  `kf_account_name` varchar(128) DEFAULT NULL,
  `userid` varchar(64) DEFAULT NULL,
  `servicer_name` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_kfid` (`org_id`,`open_kfid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微客服接待人员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_marketing_task`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_marketing_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `task_type` varchar(32) NOT NULL COMMENT 'welcome/broadcast/customer_marketing/moments',
  `task_name` varchar(255) NOT NULL,
  `material_id` bigint DEFAULT NULL,
  `material_name` varchar(255) DEFAULT NULL,
  `staff_ids` varchar(512) DEFAULT NULL,
  `staff_names` varchar(512) DEFAULT NULL,
  `dept_ids` varchar(255) DEFAULT NULL,
  `store_ids` varchar(255) DEFAULT NULL,
  `audience_type` varchar(64) DEFAULT NULL,
  `exec_status` varchar(32) DEFAULT 'pending' COMMENT 'pending/executed',
  `creator` varchar(64) DEFAULT NULL,
  `config_json` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_type` (`org_id`,`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微营销任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_material`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_material` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `group_id` bigint DEFAULT NULL,
  `group_name` varchar(128) DEFAULT NULL,
  `material_type` varchar(32) NOT NULL DEFAULT 'text' COMMENT 'image/link/video/miniprogram/text',
  `name` varchar(255) NOT NULL,
  `content_json` text COMMENT '素材内容JSON',
  `image_url` varchar(512) DEFAULT NULL,
  `remark` varchar(512) DEFAULT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_group` (`org_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微素材';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_material_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_material_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `name` varchar(128) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微素材分组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_message_notification`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_message_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `name` varchar(128) NOT NULL,
  `template_id` bigint DEFAULT NULL,
  `template_name` varchar(128) DEFAULT NULL,
  `send_type` varchar(32) DEFAULT 'immediate' COMMENT 'immediate/scheduled',
  `scheduled_at` datetime DEFAULT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `exec_status` varchar(32) DEFAULT 'pending',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_type` (`org_id`,`send_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微消息通知';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_message_template`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_message_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `name` varchar(128) NOT NULL,
  `message_type` varchar(32) DEFAULT 'text' COMMENT 'text/markdown/image',
  `template_code` varchar(128) DEFAULT NULL,
  `content` text,
  `status` tinyint NOT NULL DEFAULT '1',
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微消息模版';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_msg_audit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_msg_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `msgid` varchar(128) DEFAULT NULL,
  `from_userid` varchar(64) DEFAULT NULL,
  `from_name` varchar(64) DEFAULT NULL,
  `to_userid` varchar(64) DEFAULT NULL,
  `to_name` varchar(64) DEFAULT NULL,
  `chat_type` varchar(32) DEFAULT 'customer' COMMENT 'customer/colleague/group',
  `content` text,
  `msg_type` varchar(32) DEFAULT 'text',
  `msg_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_from` (`org_id`,`from_userid`),
  KEY `idx_org_to` (`org_id`,`to_userid`),
  KEY `idx_msg_time` (`msg_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微会话存档';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_notification_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_notification_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `notification_name` varchar(128) DEFAULT NULL,
  `bot_id` bigint DEFAULT NULL,
  `bot_name` varchar(128) DEFAULT NULL,
  `admin_id` int DEFAULT NULL,
  `admin_name` varchar(64) DEFAULT NULL,
  `ip` varchar(64) DEFAULT NULL,
  `status` varchar(32) DEFAULT 'pending' COMMENT 'pending/success/failed',
  `message` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_status` (`org_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微通知日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_reply_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_reply_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `name` varchar(128) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微话术分组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_reply_script`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_reply_script` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `group_id` bigint DEFAULT NULL,
  `group_name` varchar(128) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `script_type` varchar(32) DEFAULT 'text',
  `content` text,
  `weight` int DEFAULT '0',
  `share_count` int DEFAULT '0',
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_group` (`org_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微话术';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_sensitive_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_sensitive_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `name` varchar(128) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微敏感词分组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_sensitive_word`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_sensitive_word` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `group_id` bigint DEFAULT NULL,
  `group_name` varchar(128) DEFAULT NULL,
  `word` varchar(255) NOT NULL,
  `rule_name` varchar(128) DEFAULT NULL,
  `scope_type` varchar(32) DEFAULT 'employee',
  `intercept_mode` varchar(64) DEFAULT '警告并拦截发送',
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_group` (`org_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微敏感词';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_staff`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_staff` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `userid` varchar(64) NOT NULL,
  `name` varchar(64) DEFAULT NULL,
  `mobile` varchar(32) DEFAULT NULL,
  `gender` tinyint DEFAULT '0' COMMENT '0未知 1男 2女',
  `email` varchar(128) DEFAULT NULL,
  `biz_mail` varchar(128) DEFAULT NULL,
  `avatar` varchar(512) DEFAULT NULL,
  `department_ids` varchar(255) DEFAULT NULL COMMENT '逗号分隔企微部门ID',
  `department_name` varchar(255) DEFAULT NULL,
  `position` varchar(128) DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1已激活 2已禁用 4未激活 5退出企业',
  `store_id` bigint DEFAULT NULL,
  `store_name` varchar(128) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_userid` (`org_id`,`userid`),
  KEY `idx_org_store` (`org_id`,`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微员工';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_stat_daily`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_stat_daily` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `stat_date` date NOT NULL,
  `stat_type` varchar(32) NOT NULL COMMENT 'customer/group/group_chat/customer_chat',
  `metric_key` varchar(64) NOT NULL,
  `metric_value` decimal(18,2) DEFAULT '0.00',
  `dimension_key` varchar(64) DEFAULT NULL COMMENT '员工userid等',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat` (`org_id`,`stat_date`,`stat_type`,`metric_key`,`dimension_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微运营统计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_store`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_store` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `name` varchar(128) NOT NULL,
  `phone` varchar(32) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0已关店 1营业中',
  `store_code` varchar(64) DEFAULT NULL,
  `region` varchar(255) DEFAULT NULL,
  `address` varchar(512) DEFAULT NULL,
  `remark` varchar(512) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_status` (`org_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微门店';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_tag`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `tag_id` varchar(64) DEFAULT NULL,
  `name` varchar(128) NOT NULL,
  `group_id` bigint DEFAULT NULL,
  `group_name` varchar(128) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org_group` (`org_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微客户标签';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_wecom_tag_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_wecom_tag_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(32) NOT NULL DEFAULT '1',
  `group_id` varchar(64) DEFAULT NULL COMMENT '企微标签组ID',
  `name` varchar(128) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企微客户标签组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_oa_work`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_oa_work` (
  `id` int NOT NULL,
  `work_start_date` date DEFAULT NULL COMMENT '开始日期',
  `work_end_date` date DEFAULT NULL COMMENT '结束日期',
  `enterprise_name` varchar(255) DEFAULT '' COMMENT '企业名称',
  `main_work` varchar(255) DEFAULT '' COMMENT '主要工作',
  `witness` varchar(255) DEFAULT '' COMMENT '证明人',
  `witness_mobile` varchar(255) DEFAULT '' COMMENT '证明人电话',
  `resignation_reason_desc` varchar(255) DEFAULT '' COMMENT '离职原因(描述)',
  `position_desc` varchar(255) DEFAULT '' COMMENT '职位（描述）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作经历';
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- ---------- plugins/oa/runtime/resources/sql/employee_table.sql ----------
-- 员工表
CREATE TABLE IF NOT EXISTS `hou_oa_employee` (
    `id` bigint(20) NOT NULL COMMENT '员工ID',
    `employee_no` varchar(50) NOT NULL COMMENT '员工编号',
    `name` varchar(100) NOT NULL COMMENT '姓名',
    `gender` tinyint(1) NOT NULL DEFAULT '1' COMMENT '性别：1-男，2-女',
    `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `department_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
    `position_id` bigint(20) DEFAULT NULL COMMENT '职位ID',
    `entry_date` datetime DEFAULT NULL COMMENT '入职时间',
    `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '员工状态：active-在职，inactive-离职，probation-试用期',
    `org_id` bigint(20) NOT NULL COMMENT '组织ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`, `del_flag`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_position_id` (`position_id`),
    KEY `idx_org_id` (`org_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 插入测试数据

-- ---------- plugins/oa/runtime/resources/sql/position_table.sql ----------
-- 职位表
CREATE TABLE IF NOT EXISTS `hou_base_position` (
    `id` bigint(20) NOT NULL COMMENT '职位ID',
    `position_code` varchar(50) NOT NULL COMMENT '职位编码',
    `position_name` varchar(100) NOT NULL COMMENT '职位名称',
    `description` text COMMENT '职位描述',
    `position_level` int(11) DEFAULT '1' COMMENT '职位级别',
    `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '职位状态：active-启用，inactive-禁用',
    `org_id` bigint(20) NOT NULL COMMENT '组织ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_position_code` (`position_code`, `org_id`, `del_flag`),
    KEY `idx_org_id` (`org_id`),
    KEY `idx_status` (`status`),
    KEY `idx_position_level` (`position_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位表';

-- 插入测试数据

SET FOREIGN_KEY_CHECKS=1;
