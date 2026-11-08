-- =====================================================================
-- 插件 crm 的数据库建表脚本（共 40 张表）
-- 用途：安装「crm」插件后按需执行（也可由插件自身的迁移通道建表）。
-- 🔴 商城核心（Core）初始化不包含本文件；未安装该插件时不要执行。
-- 全部 CREATE TABLE 已统一补 IF NOT EXISTS（幂等，可重复执行，不会删数据）。
-- 执行期间临时关闭外键检查：插件之间存在跨插件外键（如 oa 引用 hou_pm_project），
-- 未同时安装被引用插件时允许该外键暂时悬空。
-- =====================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ---------- 基础表结构（结构基线） ----------
-- Table structure for table `hou_crm_achievement`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_achievement` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `config` tinyint NOT NULL DEFAULT '1' COMMENT '1销售（目标）2回款（目标）',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '2部门3员工',
  `type_id` int NOT NULL DEFAULT '0' COMMENT '对象ID',
  `year` int NOT NULL COMMENT '年',
  `january` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '一月',
  `february` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '二月',
  `march` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '三月',
  `april` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '四月',
  `may` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '五月',
  `june` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '六月',
  `july` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '七月',
  `august` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '八月',
  `september` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '九月',
  `october` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '十月',
  `november` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '十一月',
  `december` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '十二月',
  `yeartarget` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '年目标',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `config` (`config`,`type_id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业绩目标';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_business`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_business` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `customer_id` int NOT NULL COMMENT '客户ID',
  `type_id` int NOT NULL COMMENT '商机状态组',
  `status` int NOT NULL COMMENT '销售阶段',
  `status_time` int NOT NULL DEFAULT '0' COMMENT '阶段推进时间',
  `is_end` tinyint NOT NULL DEFAULT '0' COMMENT '0洽淡中，1成交2失败3无效',
  `next_time` int NOT NULL DEFAULT '0' COMMENT '下次联系时间',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '商机名称',
  `money` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '商机金额',
  `total_price` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '产品总金额',
  `deal_time` int DEFAULT NULL COMMENT '预计成交日期',
  `discount_rate` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '整单折扣',
  `remark` text COMMENT '备注',
  `create_user_id` bigint NOT NULL DEFAULT '0',
  `owner_user_id` bigint NOT NULL DEFAULT '0',
  `ro_user_id` varchar(500) NOT NULL DEFAULT '' COMMENT '只读权限',
  `rw_user_id` varchar(500) NOT NULL DEFAULT '' COMMENT '读写权限',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商机表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_business_contacts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_business_contacts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `contacts_id` int NOT NULL,
  `business_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商机联系人表表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_business_product`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_business_product` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `business_id` int DEFAULT NULL COMMENT '商机id',
  `product_id` int NOT NULL COMMENT '产品编号',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '单价',
  `nums` int DEFAULT '0' COMMENT '数量',
  `subtotal` decimal(10,2) DEFAULT NULL COMMENT '小计',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商机产品关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_cloudcall`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_cloudcall` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `admin_id` int NOT NULL,
  `from_exten` varchar(20) NOT NULL COMMENT '坐席工号',
  `exten_type` enum('gateway','sip','local') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'local' COMMENT '接听方式Local为“手机”',
  `cloud` varchar(20) DEFAULT 'tycc100' COMMENT '外呼引擎',
  `create_time` int DEFAULT NULL,
  `update_time` int DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1正常0关闭',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='云呼坐席管理';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_cloudcall_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_cloudcall_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL COMMENT '联系人名称',
  `source` varchar(50) DEFAULT NULL COMMENT '来源类型如customer，clues',
  `source_id` int DEFAULT NULL COMMENT '来源数据ID',
  `admin_id` int DEFAULT NULL,
  `cloud` varchar(20) DEFAULT NULL COMMENT '外呼引擎',
  `from_exten` varchar(20) DEFAULT NULL COMMENT '坐席工号',
  `create_time` int DEFAULT NULL,
  `update_time` int DEFAULT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0数据未回传-1未接通1已同步2同步失败',
  `call_no` varchar(20) DEFAULT NULL COMMENT '呼叫号码',
  `called_no` varchar(20) DEFAULT NULL COMMENT '被呼叫号码',
  `record_file` varchar(1000) DEFAULT NULL COMMENT '录音文件',
  `call_time_length` int DEFAULT NULL COMMENT '呼叫时长(秒)',
  `exten_type` varchar(20) DEFAULT NULL COMMENT '接听方式',
  `action_id` varchar(100) DEFAULT NULL COMMENT '呼叫事件ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='呼叫记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_clues`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_clues` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `name` varchar(50) NOT NULL COMMENT '线索',
  `mobile` varchar(50) NOT NULL DEFAULT '' COMMENT '手机',
  `telephone` varchar(50) NOT NULL DEFAULT '' COMMENT '电话',
  `status` int NOT NULL DEFAULT '0' COMMENT '状态1-无效线索，0未转客户,1转成客户',
  `customer_id` int NOT NULL DEFAULT '0' COMMENT '转成客户id',
  `change_time` int DEFAULT NULL COMMENT '转客户时间',
  `level` int NOT NULL DEFAULT '0' COMMENT '客户级别ID',
  `industry` int NOT NULL DEFAULT '0' COMMENT '客户行业ID',
  `tags` varchar(255) DEFAULT '' COMMENT ' 客户标签',
  `source` int NOT NULL DEFAULT '0' COMMENT '客户来源ID',
  `remark` text COMMENT '备注',
  `create_user_id` bigint NOT NULL DEFAULT '0',
  `owner_user_id` bigint NOT NULL DEFAULT '0',
  `user_id` int DEFAULT '0' COMMENT '前台用户ID',
  `ro_user_id` varchar(500) NOT NULL DEFAULT '' COMMENT '只读权限',
  `rw_user_id` varchar(500) NOT NULL DEFAULT '' COMMENT '读写权限',
  `province` int DEFAULT '0' COMMENT '省份id',
  `city` int DEFAULT '0' COMMENT '城市ID',
  `area` int DEFAULT '0' COMMENT '区域ID',
  `detail_address` varchar(255) NOT NULL DEFAULT '' COMMENT '详细地址',
  `next_time` int NOT NULL DEFAULT '0' COMMENT '下次联系时间',
  `follow_time` int DEFAULT NULL COMMENT '最后跟进时间',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `collect_time` int DEFAULT '0' COMMENT '领取时间',
  `counsds` varchar(255) DEFAULT '' COMMENT 'counsds',
  `follow_status` enum('1','2','3','4','5','0') DEFAULT '1' COMMENT '跟进状态',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='线索表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_contract_order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_contract_order` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `orderid` varchar(100) DEFAULT NULL COMMENT '订单ID',
  `user_id` int unsigned DEFAULT '0' COMMENT '会员ID',
  `amount` double(10,2) unsigned DEFAULT '0.00' COMMENT '订单金额',
  `payamount` double(10,2) unsigned DEFAULT '0.00' COMMENT '支付金额',
  `paytype` varchar(50) DEFAULT NULL COMMENT '支付类型',
  `paytime` int DEFAULT NULL COMMENT '支付时间',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `useragent` varchar(255) DEFAULT NULL COMMENT 'UserAgent',
  `memo` varchar(255) DEFAULT NULL COMMENT '备注',
  `createtime` int DEFAULT NULL COMMENT '添加时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `status` enum('created','paid','expired') DEFAULT 'created' COMMENT '状态',
  `receivables_id` int DEFAULT NULL COMMENT '回款ID',
  `contract_id` int DEFAULT NULL COMMENT '合同ID',
  `ordertype` enum('renew','pay') DEFAULT NULL COMMENT '订单类型',
  `renew_day` int DEFAULT '0' COMMENT '续费天数（总）',
  `number` int DEFAULT '1' COMMENT '下单数量',
  `org_id` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_contract_plan`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_contract_plan` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `customer_id` int NOT NULL COMMENT '客户 ID',
  `contract_id` int NOT NULL COMMENT '合同ID',
  `return_money` decimal(10,2) DEFAULT '0.00' COMMENT '已回款金额',
  `money` decimal(10,2) NOT NULL COMMENT '回款金额',
  `remind_day` tinyint DEFAULT '1' COMMENT '提前提醒天数',
  `remind` tinyint(1) DEFAULT '0' COMMENT '1已经提醒',
  `status` tinyint DEFAULT '0' COMMENT '-1到期未回款，0进行中，2已回款',
  `receivables_id` int DEFAULT NULL COMMENT '回款时关联的ID',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `plan_time` bigint DEFAULT NULL COMMENT '计划回款时间',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `account_id` int DEFAULT NULL COMMENT '收款账户ID',
  `admin_id` int DEFAULT NULL COMMENT '创建者',
  `name` varchar(50) DEFAULT NULL COMMENT '计划名称',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='回款计划';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_contract_product`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_contract_product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `contract_id` int NOT NULL COMMENT '合同ID',
  `product_id` int NOT NULL COMMENT '产品ID',
  `name` varchar(100) NOT NULL COMMENT '产品名称',
  `specification` varchar(80) NOT NULL COMMENT '规格',
  `sku` varchar(80) DEFAULT NULL COMMENT 'sku',
  `prop` text COMMENT '商品属性',
  `price` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '产品单价',
  `sales_price` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '销售价格',
  `nums` int NOT NULL DEFAULT '0' COMMENT '数量',
  `discount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '折扣',
  `subtotal` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '小计（折扣后价格）',
  `unit` varchar(50) NOT NULL DEFAULT '' COMMENT '单位',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` int DEFAULT NULL,
  `update_time` int DEFAULT NULL,
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同产品关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_contract_receivables`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_contract_receivables` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `number` varchar(100) NOT NULL DEFAULT '' COMMENT '回款编号',
  `customer_id` int NOT NULL COMMENT '客户ID',
  `contract_id` int NOT NULL COMMENT '合同ID',
  `return_time` int DEFAULT NULL COMMENT '回款日期',
  `account_id` int NOT NULL COMMENT '收款账户ID',
  `money` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '回款金额',
  `check_status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核、1审核中、2审核通过、3审核未通过',
  `flow_id` int NOT NULL DEFAULT '0' COMMENT '审核流程ID',
  `step_id` int NOT NULL DEFAULT '0' COMMENT '审核步骤排序ID',
  `check_admin_id` varchar(255) NOT NULL DEFAULT '' COMMENT '审批人IDs',
  `flow_admin_id` varchar(255) NOT NULL DEFAULT '' COMMENT '流程审批人ID',
  `remark` text COMMENT '备注',
  `create_user_id` int NOT NULL COMMENT '创建人ID',
  `owner_user_id` int NOT NULL DEFAULT '0' COMMENT '负责人ID',
  `order_admin_id` int DEFAULT NULL COMMENT '合同签约人',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `pay_status` tinyint DEFAULT '0' COMMENT '在线支付状态1已付款0未付款',
  `pay_type` tinyint DEFAULT '1' COMMENT '收款方式1默认2在线收款3续费',
  `pay_token` varchar(200) DEFAULT NULL COMMENT '访问支付的token',
  `plan_id` int NOT NULL DEFAULT '0' COMMENT '关联回款计划',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='回款表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_customer_audit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_customer_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `customer_id` int NOT NULL,
  `action` varchar(40) COLLATE utf8mb4_general_ci NOT NULL,
  `content` text COLLATE utf8mb4_general_ci,
  `operator_id` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_customer` (`org_id`,`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_customer_contacts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_customer_contacts` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `customer_id` int NOT NULL COMMENT '客户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '姓名',
  `mobile` varchar(50) NOT NULL DEFAULT '' COMMENT '手机',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `telephone` varchar(50) NOT NULL DEFAULT '' COMMENT '电话',
  `wechat` varchar(50) DEFAULT '' COMMENT '微信号',
  `email` varchar(255) NOT NULL DEFAULT '' COMMENT '电子邮箱',
  `decision` tinyint NOT NULL DEFAULT '-1' COMMENT '是否关键决策人1是0不是-1未知',
  `post` varchar(255) NOT NULL DEFAULT '' COMMENT '职务',
  `sex` tinyint NOT NULL DEFAULT '-1' COMMENT '性别0女1男-1未知',
  `detail_address` varchar(255) NOT NULL DEFAULT '' COMMENT '地址',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备注',
  `ro_user_id` varchar(500) NOT NULL DEFAULT '' COMMENT '只读权限',
  `rw_user_id` varchar(500) NOT NULL DEFAULT '' COMMENT '读写权限',
  `create_user_id` int NOT NULL COMMENT '创建人ID',
  `owner_user_id` int NOT NULL COMMENT '负责人ID',
  `next_time` int DEFAULT NULL COMMENT '下次联系时间',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='联系人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_customer_relation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_customer_relation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `customer_id` int NOT NULL,
  `related_customer_id` int NOT NULL,
  `relation_type` varchar(80) COLLATE utf8mb4_general_ci NOT NULL,
  `relation_strength` int NOT NULL DEFAULT '3',
  `description` text COLLATE utf8mb4_general_ci,
  `create_time` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_relation` (`org_id`,`customer_id`,`related_customer_id`,`relation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_fields`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_fields` (
  `id` smallint unsigned NOT NULL AUTO_INCREMENT,
  `source` varchar(30) DEFAULT '' COMMENT '来源',
  `name` char(30) DEFAULT '' COMMENT '名称',
  `type` varchar(30) DEFAULT '' COMMENT '类型',
  `title` varchar(30) DEFAULT '' COMMENT '标题',
  `content` text COMMENT '内容',
  `defaultvalue` varchar(100) DEFAULT '' COMMENT '默认值',
  `rule` varchar(100) DEFAULT '' COMMENT '验证规则',
  `msg` varchar(100) DEFAULT '0' COMMENT '错误消息',
  `ok` varchar(100) DEFAULT '0' COMMENT '成功消息',
  `tip` varchar(100) DEFAULT '' COMMENT '提示消息',
  `decimals` tinyint(1) DEFAULT NULL COMMENT '小数点',
  `length` mediumint DEFAULT NULL COMMENT '长度',
  `minimum` smallint DEFAULT NULL COMMENT '最小数量',
  `maximum` smallint unsigned NOT NULL DEFAULT '0' COMMENT '最大数量',
  `extend` varchar(255) DEFAULT '' COMMENT '扩展信息',
  `setting` varchar(1500) DEFAULT '' COMMENT '配置信息',
  `weigh` int NOT NULL DEFAULT '0' COMMENT '排序',
  `createtime` int DEFAULT NULL COMMENT '添加时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `isorder` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可排序',
  `iscontribute` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可投稿',
  `islist` tinyint(1) DEFAULT '0' COMMENT '是否显示在列表',
  `isfilter` tinyint(1) NOT NULL DEFAULT '0' COMMENT '筛选',
  `status` enum('normal','hidden') NOT NULL COMMENT '状态',
  PRIMARY KEY (`id`),
  KEY `model` (`source`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='自定义字段表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_finance_invoice`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_finance_invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) NOT NULL DEFAULT '1',
  `invoice_no` varchar(80) NOT NULL,
  `direction` varchar(16) NOT NULL DEFAULT 'output',
  `partner_name` varchar(200) DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `customer_name` varchar(200) DEFAULT NULL,
  `supplier_name` varchar(200) DEFAULT NULL,
  `type` varchar(16) NOT NULL DEFAULT '1',
  `amount` decimal(18,2) NOT NULL DEFAULT '0.00',
  `tax_rate` decimal(8,2) NOT NULL DEFAULT '0.00',
  `tax_amount` decimal(18,2) NOT NULL DEFAULT '0.00',
  `invoice_date` date NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT '0',
  `related_desc` varchar(500) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `print_status` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_finance_invoice_no_org` (`org_id`,`invoice_no`),
  KEY `idx_finance_invoice_org_date` (`org_id`,`invoice_date`),
  KEY `idx_finance_invoice_status` (`org_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_finance_tax`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_finance_tax` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) NOT NULL DEFAULT '1',
  `name` varchar(100) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_finance_tax_org` (`org_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_flow`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_flow` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '审批流名称',
  `config` tinyint NOT NULL DEFAULT '1' COMMENT '1固定审批0授权审批',
  `types` enum('contract','invoice','receivables') NOT NULL COMMENT '关联对象',
  `types_id` int NOT NULL DEFAULT '0' COMMENT '对象ID（如审批类型ID）',
  `group_ids` varchar(500) NOT NULL DEFAULT '' COMMENT '组别ID（0为全部）',
  `admin_ids` varchar(500) NOT NULL DEFAULT '' COMMENT '员工ID',
  `remark` varchar(500) NOT NULL DEFAULT '' COMMENT '流程说明',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1启用，0禁用',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `weigh` int DEFAULT '0' COMMENT '权重',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批流程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_flow_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_flow_log` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `flow_id` int NOT NULL COMMENT '审批流程ID',
  `types` enum('contract','invoice','receivables') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关联对象',
  `types_id` int NOT NULL DEFAULT '0' COMMENT '对象ID（如审批类型ID）',
  `admin_id` int unsigned NOT NULL DEFAULT '0' COMMENT '审核人',
  `nickname` varchar(30) DEFAULT '' COMMENT '审核人名称',
  `remark` varchar(200) DEFAULT '' COMMENT '审核意见',
  `is_end` tinyint NOT NULL DEFAULT '0' COMMENT '1审核结束',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0审核未通过,1审核通过',
  `create_time` int DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `name` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审核日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_flow_step`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_flow_step` (
  `id` int NOT NULL AUTO_INCREMENT,
  `flow_id` int NOT NULL COMMENT '审批流程ID',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1上级管理员，2指定用户（任意一人）',
  `admin_ids` varchar(500) NOT NULL DEFAULT '' COMMENT '审批人ID (使用逗号隔开) ,1,2,',
  `relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '审批流程关系（1并2或）',
  `create_time` int NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批步骤表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_invoice`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_invoice` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `contract_id` int NOT NULL COMMENT '合同ID',
  `customer_id` int NOT NULL COMMENT '客户ID',
  `invoice_body` varchar(50) DEFAULT NULL COMMENT '开票主体',
  `content` varchar(255) NOT NULL COMMENT '开票内容',
  `invoice_name` varchar(100) NOT NULL DEFAULT '' COMMENT '发票抬头',
  `invoice_issue` tinyint NOT NULL DEFAULT '1' COMMENT '抬头类型1企业2个人3事业单位',
  `invoice_type` tinyint NOT NULL DEFAULT '1' COMMENT '1增普2增专',
  `register_no` varchar(50) DEFAULT NULL COMMENT '统一社会信用代码',
  `bank_name` varchar(50) DEFAULT NULL COMMENT '开户行名称',
  `bank_no` varchar(50) DEFAULT NULL COMMENT '开户账号',
  `money` decimal(10,2) DEFAULT '0.00' COMMENT '发票税前金额',
  `tax_rate` int NOT NULL DEFAULT '0' COMMENT '税率',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `email` varchar(50) DEFAULT NULL COMMENT '接收发票邮箱',
  `user_name` varchar(20) DEFAULT NULL COMMENT '收件人',
  `user_phone` varchar(20) DEFAULT NULL COMMENT '收件手机',
  `user_address` varchar(200) DEFAULT NULL COMMENT '邮寄地址',
  `files` varchar(255) DEFAULT NULL COMMENT '开票附件',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `create_admin_id` int DEFAULT NULL COMMENT '创建人',
  `invoice_admin_id` int DEFAULT NULL COMMENT '开票人',
  `check_status` tinyint DEFAULT '0' COMMENT '0待审核、1审核中、2审核通过、3审核未通过',
  `flow_id` int DEFAULT NULL COMMENT '审核流程ID',
  `step_id` int DEFAULT NULL COMMENT '审核步骤ID',
  `check_admin_id` varchar(255) DEFAULT NULL COMMENT '已经审批人IDs',
  `flow_admin_id` varchar(255) DEFAULT NULL COMMENT '当前需要审批的人',
  `status` tinyint DEFAULT '0' COMMENT '状态0未开票1开票中2已开票3开票失败',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `contract_id` (`contract_id`),
  KEY `customer_i` (`customer_id`),
  KEY `invoice_admin_id` (`invoice_admin_id`),
  KEY `create_admin_id` (`create_admin_id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发票表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_notices`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_notices` (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int NOT NULL COMMENT '管理员ID',
  `user_id` int NOT NULL COMMENT '会员ID',
  `customer_id` int NOT NULL COMMENT '客户ID',
  `engine` varchar(20) DEFAULT NULL COMMENT '通知引擎',
  `status` tinyint DEFAULT '0' COMMENT '0失败1成功',
  `content` longtext NOT NULL COMMENT '内容',
  `create_time` int DEFAULT NULL,
  `update_time` int DEFAULT NULL,
  `msg` text COMMENT '信息',
  `event_id` varchar(100) DEFAULT NULL COMMENT '发送事件',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_operatelog`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_operatelog` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `admin_id` int unsigned NOT NULL DEFAULT '0' COMMENT '管理员ID',
  `customer_id` int NOT NULL DEFAULT '0' COMMENT '客户ID',
  `contract_id` int NOT NULL DEFAULT '0' COMMENT '合同ID',
  `receivable_id` int NOT NULL DEFAULT '0' COMMENT '回款ID',
  `nickname` varchar(50) DEFAULT NULL COMMENT '姓名',
  `username` varchar(50) DEFAULT '' COMMENT '管理员账号',
  `url` varchar(1500) DEFAULT '' COMMENT '操作页面',
  `title` varchar(255) DEFAULT '' COMMENT '日志标题',
  `content` longtext NOT NULL COMMENT '内容',
  `ip` varchar(50) DEFAULT '' COMMENT 'IP',
  `useragent` varchar(255) DEFAULT '' COMMENT 'User-Agent',
  `create_time` bigint DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `admin_id` (`admin_id`),
  KEY `customer_id` (`customer_id`),
  KEY `contract_id` (`contract_id`),
  KEY `receivable_id` (`receivable_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_product_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_product_type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `prop` mediumtext COMMENT '分类属性',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `pid` int unsigned NOT NULL DEFAULT '0' COMMENT '父id',
  `image` varbinary(100) DEFAULT NULL COMMENT '图片',
  `weigh` int NOT NULL DEFAULT '0' COMMENT '权重',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_product_unit`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_product_unit` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(50) NOT NULL COMMENT '单位名称',
  `create_time` int DEFAULT NULL COMMENT '创建时间',
  `update_time` int DEFAULT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品单位';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_qywx_contacts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_qywx_contacts` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `corp_id` varchar(120) NOT NULL COMMENT '企业ID',
  `external_userid` varchar(250) NOT NULL COMMENT '外部联系人的userid',
  `name` varchar(120) NOT NULL COMMENT '外部联系人的名称',
  `avatar` varchar(250) DEFAULT NULL COMMENT '外部联系人头像',
  `unionid` varchar(250) DEFAULT NULL COMMENT '微信unionid',
  `follow_user` text COMMENT '企业负责该联系人成员',
  `corp_full_name` varchar(100) DEFAULT NULL COMMENT '企业的主体名称',
  `corp_name` varchar(50) DEFAULT NULL COMMENT '企业的简称',
  `type` tinyint DEFAULT NULL COMMENT '类型，1微信用户，2企业微信用户',
  `gender` tinyint NOT NULL DEFAULT '0' COMMENT '性别 0-未知 1-男性 2-女性',
  `status` varchar(30) NOT NULL DEFAULT '' COMMENT '状态',
  `weigh` int NOT NULL DEFAULT '0' COMMENT '权重',
  `create_time` int NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  `customer_id` int DEFAULT NULL COMMENT '关联本地客户联系人ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业微信客户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_qywx_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_qywx_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `admin_id` int NOT NULL COMMENT '关联fa后台账户id',
  `corp_id` varchar(120) NOT NULL COMMENT '企业ID',
  `userid` varchar(250) NOT NULL COMMENT '成员userid',
  `name` varchar(250) NOT NULL COMMENT '成员名称',
  `mobile` varchar(20) DEFAULT NULL COMMENT '成员手机号码',
  `email` varchar(250) DEFAULT NULL COMMENT '成员邮箱',
  `avatar` varchar(250) DEFAULT NULL COMMENT '头像url',
  `gender` tinyint(1) NOT NULL DEFAULT '0' COMMENT '性别。0表示未定义，1表示男性，2表示女性',
  `position` varchar(250) DEFAULT NULL COMMENT '职务信息',
  `qr_code` varchar(250) DEFAULT NULL COMMENT '客户联系二维码',
  `status` varchar(30) NOT NULL DEFAULT '' COMMENT '激活状态: 1=已激活，2=已禁用，4=未激活，5=退出企业',
  `weigh` int NOT NULL DEFAULT '0' COMMENT '权重',
  `createtime` int NOT NULL DEFAULT '0' COMMENT '创建时间',
  `updatetime` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业微信员工';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `types` enum('business','clues','customer') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'customer' COMMENT '关联类型(customer跟进,business商机跟进,clues线索)',
  `types_id` int NOT NULL COMMENT '类型ID（根据types变动而变动）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '跟进内容',
  `record_type` int NOT NULL DEFAULT '0' COMMENT '跟进类型',
  `next_time` int NOT NULL DEFAULT '0' COMMENT '下次联系时间',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `create_user_id` bigint NOT NULL DEFAULT '0',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `lng` double(14,11) DEFAULT NULL COMMENT '经度',
  `lat` double(14,11) DEFAULT NULL COMMENT '维度',
  `follow_result` text,
  `follow_remark` text,
  `follow_images` text,
  `follow_collaborator` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `record_id` (`id`),
  KEY `types` (`types`),
  KEY `types_id` (`types_id`),
  KEY `record_type` (`record_type`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跟进记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_record_files`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_record_files` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `types` enum('business','clues','customer') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'customer' COMMENT '关联类型(customer客户,business商机,clues线索)',
  `types_id` int NOT NULL COMMENT '类型ID（根据types变动而变动）',
  `record_id` int NOT NULL COMMENT '跟进记录ID',
  `url` varchar(255) NOT NULL COMMENT '附件url',
  `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跟进文件记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_scene`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_scene` (
  `id` int NOT NULL AUTO_INCREMENT,
  `types` varchar(50) NOT NULL COMMENT '分类',
  `name` varchar(50) NOT NULL COMMENT '场景名称',
  `admin_id` int NOT NULL COMMENT '用户ID',
  `weigh` int NOT NULL DEFAULT '1' COMMENT '权重',
  `data` mediumtext COMMENT '属性值',
  `type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '1系统0自定义',
  `bydata` varchar(50) NOT NULL DEFAULT '' COMMENT '系统参数',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='场景';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_setting`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_setting` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `key` varchar(30) NOT NULL COMMENT '设置项标示',
  `describe` varchar(255) NOT NULL DEFAULT '' COMMENT '设置项描述',
  `values` longtext NOT NULL COMMENT '设置内容（json格式）',
  `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  `create_time` int NOT NULL COMMENT '创建时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '1正常0禁用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设置记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_tag`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_tag` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT '' COMMENT '标签名称',
  `type` varchar(20) DEFAULT 'customer' COMMENT '类型',
  `nums` int unsigned NOT NULL DEFAULT '0' COMMENT '数量',
  `createtime` int DEFAULT NULL COMMENT '添加时间',
  `updatetime` int DEFAULT NULL COMMENT '更新时间',
  `status` enum('normal','hidden') DEFAULT 'normal' COMMENT '状态',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`) USING BTREE,
  KEY `nums` (`nums`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hou_crm_workspace_record`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `hou_crm_workspace_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `module` varchar(80) COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `payload` json NOT NULL,
  `create_user_id` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` int NOT NULL,
  `update_time` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_module` (`org_id`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--

-- ---------- plugins/crm/migrations/crm_tables.sql ----------
-- CRM客户关系管理系统数据库表结构
-- Date: 2024-09-29

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for hou_crm_customer
-- ----------------------------
CREATE TABLE IF NOT EXISTS `hou_crm_customer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `customer_name` varchar(100) NOT NULL COMMENT '客户名称',
  `customer_type` tinyint NOT NULL DEFAULT '1' COMMENT '客户类型：1-个人客户，2-企业客户',
  `industry` varchar(50) DEFAULT NULL COMMENT '所属行业',
  `company_name` varchar(200) DEFAULT NULL COMMENT '公司名称',
  `company_scale` varchar(50) DEFAULT NULL COMMENT '公司规模',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `contact_address` varchar(500) DEFAULT NULL COMMENT '联系地址',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '客户状态：1-潜在客户，2-意向客户，3-成交客户，4-流失客户',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '客户等级：1-A级，2-B级，3-C级，4-D级',
  `source` varchar(50) DEFAULT NULL COMMENT '客户来源：线上推广，电话营销，展会，介绍等',
  `description` text COMMENT '客户描述',
  `tags` varchar(200) DEFAULT NULL COMMENT '客户标签，逗号分隔',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `assigned_to` varchar(50) DEFAULT NULL COMMENT '负责人',
  `team_id` bigint DEFAULT NULL COMMENT '所属团队ID',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_contact_time` datetime DEFAULT NULL COMMENT '最后联系时间',
  `next_contact_time` datetime DEFAULT NULL COMMENT '下次联系时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer_name` (`customer_name`),
  KEY `idx_contact_phone` (`contact_phone`),
  KEY `idx_status` (`status`),
  KEY `idx_assigned_to` (`assigned_to`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM客户信息表';

-- ----------------------------
-- Table structure for hou_crm_lead
-- ----------------------------
CREATE TABLE IF NOT EXISTS `hou_crm_lead` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '线索ID',
  `lead_name` varchar(100) NOT NULL COMMENT '线索名称',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `company_name` varchar(200) DEFAULT NULL COMMENT '公司名称',
  `industry` varchar(50) DEFAULT NULL COMMENT '所属行业',
  `source` varchar(50) DEFAULT NULL COMMENT '线索来源',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '线索状态：1-新线索，2-已联系，3-已转化，4-已关闭',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '优先级：1-高，2-中，3-低',
  `score` int DEFAULT '0' COMMENT '线索评分',
  `description` text COMMENT '线索描述',
  `assigned_to` varchar(50) DEFAULT NULL COMMENT '负责人',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `converted_time` datetime DEFAULT NULL COMMENT '转化时间',
  `customer_id` bigint DEFAULT NULL COMMENT '转化后的客户ID',
  PRIMARY KEY (`id`),
  KEY `idx_contact_phone` (`contact_phone`),
  KEY `idx_status` (`status`),
  KEY `idx_assigned_to` (`assigned_to`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM销售线索表';

-- ----------------------------
-- Table structure for hou_crm_opportunity
-- ----------------------------
CREATE TABLE IF NOT EXISTS `hou_crm_opportunity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商机ID',
  `opportunity_name` varchar(200) NOT NULL COMMENT '商机名称',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `customer_name` varchar(100) DEFAULT NULL COMMENT '客户名称',
  `amount` decimal(15,2) DEFAULT NULL COMMENT '预期金额',
  `probability` int DEFAULT '0' COMMENT '成功概率(%)',
  `stage` varchar(50) NOT NULL DEFAULT '初步接触' COMMENT '销售阶段',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '商机状态：1-进行中，2-已成交，3-已失败',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '优先级：1-高，2-中，3-低',
  `expected_close_date` date DEFAULT NULL COMMENT '预期成交日期',
  `actual_close_date` date DEFAULT NULL COMMENT '实际成交日期',
  `close_reason` varchar(200) DEFAULT NULL COMMENT '成交/失败原因',
  `description` text COMMENT '商机描述',
  `assigned_to` varchar(50) DEFAULT NULL COMMENT '负责人',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_assigned_to` (`assigned_to`),
  KEY `idx_expected_close_date` (`expected_close_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM销售商机表';

-- ----------------------------
-- Table structure for hou_crm_contact_record
-- ----------------------------
CREATE TABLE IF NOT EXISTS `hou_crm_contact_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `lead_id` bigint DEFAULT NULL COMMENT '线索ID',
  `opportunity_id` bigint DEFAULT NULL COMMENT '商机ID',
  `contact_type` tinyint NOT NULL COMMENT '联系类型：1-电话，2-邮件，3-微信，4-面访，5-其他',
  `contact_result` tinyint NOT NULL COMMENT '联系结果：1-成功，2-失败，3-待跟进',
  `content` text NOT NULL COMMENT '联系内容',
  `next_contact_time` datetime DEFAULT NULL COMMENT '下次联系时间',
  `attachments` varchar(500) DEFAULT NULL COMMENT '附件地址，多个用逗号分隔',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_lead_id` (`lead_id`),
  KEY `idx_opportunity_id` (`opportunity_id`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM联系记录表';

-- ----------------------------
-- Table structure for hou_crm_team
-- ----------------------------
CREATE TABLE IF NOT EXISTS `hou_crm_team` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '团队ID',
  `team_name` varchar(100) NOT NULL COMMENT '团队名称',
  `team_leader` varchar(50) DEFAULT NULL COMMENT '团队负责人',
  `description` varchar(500) DEFAULT NULL COMMENT '团队描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-正常，0-禁用',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_name` (`team_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM销售团队表';

-- ----------------------------
-- Table structure for hou_crm_product
-- ----------------------------
CREATE TABLE IF NOT EXISTS `hou_crm_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `product_name` varchar(200) NOT NULL COMMENT '产品名称',
  `product_code` varchar(50) DEFAULT NULL COMMENT '产品编码',
  `category` varchar(100) DEFAULT NULL COMMENT '产品分类',
  `price` decimal(15,2) DEFAULT NULL COMMENT '标准价格',
  `cost` decimal(15,2) DEFAULT NULL COMMENT '成本价格',
  `unit` varchar(20) DEFAULT '个' COMMENT '计量单位',
  `description` text COMMENT '产品描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-正常，0-停用',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM产品信息表';

-- ----------------------------
-- Table structure for hou_crm_contract
-- ----------------------------
CREATE TABLE IF NOT EXISTS `hou_crm_contract` (
  `id` int NOT NULL AUTO_INCREMENT,
  `org_id` varchar(100) DEFAULT '1' COMMENT '组织ID（支持逗号分隔）',
  `customer_id` int NOT NULL COMMENT '客户ID',
  `business_id` int NOT NULL COMMENT '商机ID',
  `contacts_id` int NOT NULL COMMENT '客户签约人（联系人ID）',
  `source_id` int DEFAULT '0' COMMENT '来源ID',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '合同名称',
  `number` varchar(255) NOT NULL DEFAULT '' COMMENT '合同编号',
  `order_time` int DEFAULT NULL COMMENT '下单时间',
  `money` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '合同金额',
  `total_price` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '产品总金额',
  `return_money` decimal(10,2) DEFAULT '0.00' COMMENT '已收到款项',
  `discount_rate` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '整单折扣',
  `check_status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核、1审核中、2审核通过、3审核未通过',
  `flow_id` int NOT NULL DEFAULT '0' COMMENT '审核流程ID',
  `step_id` int NOT NULL DEFAULT '0' COMMENT '审核步骤ID',
  `check_admin_id` varchar(255) NOT NULL DEFAULT '' COMMENT '已经审批人IDs',
  `flow_admin_id` varchar(255) NOT NULL DEFAULT '' COMMENT '当前需要审批的人',
  `start_time` int DEFAULT NULL COMMENT '开始时间',
  `end_time` int DEFAULT NULL COMMENT '结束时间',
  `order_admin_id` int DEFAULT NULL COMMENT '公司签约人',
  `remark` text COMMENT '备注',
  `create_user_id` bigint NOT NULL DEFAULT '0',
  `owner_user_id` bigint NOT NULL DEFAULT '0',
  `ro_user_id` varchar(500) NOT NULL DEFAULT '' COMMENT '只读权限',
  `rw_user_id` varchar(500) NOT NULL DEFAULT '' COMMENT '读写权限',
  `next_time` int NOT NULL DEFAULT '0' COMMENT '下次联系时间',
  `create_time` int NOT NULL COMMENT '创建时间',
  `update_time` int NOT NULL COMMENT '更新时间',
  `delete_time` int DEFAULT NULL COMMENT '删除时间',
  `expire_handle` tinyint DEFAULT '0' COMMENT '0合同过期未处理1已续签2不再合作',
  `invoice_money` decimal(10,2) DEFAULT '0.00' COMMENT '已开票金额',
  PRIMARY KEY (`id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同表';

SET FOREIGN_KEY_CHECKS = 1;
SET FOREIGN_KEY_CHECKS=1;
