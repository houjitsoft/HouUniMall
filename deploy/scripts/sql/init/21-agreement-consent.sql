-- =====================================================================
-- HouCloud 统一初始化 ③/④ 增量迁移：注册协议与用户同意记录
-- 范围：协议可配置化（定义 + 版本 + 正文）、用户同意留痕与站点安装接受留痕
-- 幂等：建表与种子数据均可重复执行；安装接受记录由 init_all 在安装者确认后另行写入
-- 依赖：hou_permission_menu（系统菜单表，由 10-meoc-schema.sql 创建）
-- 说明：本文件属平台核心（Core），**不含任何插件**。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 协议定义表：注册场景中要求用户勾选的协议清单
--    与既有 hou_base_protocol（入驻协议，文件型）区分：本表面向注册场景、
--    存正文、带版本号，供注册页按 code 拉取并渲染。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hou_base_agreement` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(50) NOT NULL DEFAULT '' COMMENT '协议唯一编码：PRIVACY_POLICY / USER_SERVICE_AGREEMENT / COMPLIANCE_COMMITMENT / PLUGIN_COMMERCIAL_PURCHASE',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '协议展示名称',
  `scene` varchar(30) NOT NULL DEFAULT 'register' COMMENT '适用场景：register=注册 / plugin_purchase=插件购买 / login=登录 / shop_join=入驻 / other=其他',
  `required` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否必选：1=必选（不勾选不允许提交）0=可选',
  `default_checked` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否默认勾选：合规要求一律 0，须用户主动勾选',
  `sort` smallint unsigned NOT NULL DEFAULT '0' COMMENT '展示排序，小的在前',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态：1=启用 0=停用',
  `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_scene` (`code`, `scene`),
  KEY `idx_scene_status` (`scene`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议定义表（注册场景）';

-- ---------------------------------------------------------------------
-- 2. 协议版本表：协议正文按版本存储，支持版本发布与回滚
--    新版本发布后，未同意过该版本号的用户将被要求重新同意。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hou_base_agreement_version` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `agreement_id` int unsigned NOT NULL DEFAULT '0' COMMENT '协议ID（hou_base_agreement.id）',
  `agreement_code` varchar(50) NOT NULL DEFAULT '' COMMENT '协议编码（冗余，便于查询）',
  `version_no` varchar(20) NOT NULL DEFAULT '' COMMENT '版本号，格式 v1.0.0，同一协议内唯一',
  `title` varchar(200) NOT NULL DEFAULT '' COMMENT '版本标题（弹窗标题）',
  `content` longtext COMMENT '协议正文（HTML 或纯文本，前端原样渲染）',
  `change_note` varchar(500) NOT NULL DEFAULT '' COMMENT '本次变更说明（弹窗顶部展示）',
  `effective_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间（NULL=草稿未发布）',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态：1=已发布 0=草稿 2=已下线',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agreement_version` (`agreement_id`, `version_no`),
  KEY `idx_code_status` (`agreement_code`, `status`, `effective_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议版本表';

-- ---------------------------------------------------------------------
-- 3. 用户协议同意记录表：留痕用户每一次同意行为
--    同一用户对同一协议的同一版本只保留一条有效记录（唯一索引防重）。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hou_user_agreement_consent` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` varchar(64) NOT NULL DEFAULT '' COMMENT '用户ID',
  `username` varchar(100) NOT NULL DEFAULT '' COMMENT '用户名（冗余，便于后台检索）',
  `agreement_id` int unsigned NOT NULL DEFAULT '0' COMMENT '协议ID',
  `agreement_code` varchar(50) NOT NULL DEFAULT '' COMMENT '协议编码',
  `agreement_name` varchar(100) NOT NULL DEFAULT '' COMMENT '协议名称（同意时的快照）',
  `version_id` int unsigned NOT NULL DEFAULT '0' COMMENT '协议版本ID',
  `version_no` varchar(20) NOT NULL DEFAULT '' COMMENT '同意的协议版本号',
  `scene` varchar(30) NOT NULL DEFAULT 'register' COMMENT '同意场景：register=注册 / reagree=版本更新后重新同意 / other=其他',
  `consent_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '同意时间',
  `ip` varchar(50) NOT NULL DEFAULT '' COMMENT '客户端IP',
  `ip_location` varchar(200) NOT NULL DEFAULT '' COMMENT 'IP 归属地（异步解析，可为空）',
  `ua` varchar(500) NOT NULL DEFAULT '' COMMENT 'User-Agent 原文',
  `device` varchar(100) NOT NULL DEFAULT '' COMMENT '设备类型（PC / Mobile / Tablet）',
  `browser` varchar(100) NOT NULL DEFAULT '' COMMENT '浏览器名称及版本',
  `os` varchar(100) NOT NULL DEFAULT '' COMMENT '操作系统',
  `source` varchar(30) NOT NULL DEFAULT 'web' COMMENT '来源端：web=管理后台 / h5 / app / openapi',
  `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态：1=有效 0=已失效（版本更新后作废）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_code_version` (`user_id`, `agreement_code`, `version_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_code_time` (`agreement_code`, `consent_time`),
  KEY `idx_consent_time` (`consent_time`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户协议同意记录表';

-- ---------------------------------------------------------------------
-- 3.2 安装协议接受记录表：站点级安装审计，不关联任何站内用户
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hou_base_install_agreement_acceptance` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `agreement_code` varchar(50) NOT NULL DEFAULT '' COMMENT '协议编码',
  `version_no` varchar(20) NOT NULL DEFAULT '' COMMENT '安装者接受的协议版本号',
  `accepted_time` datetime NOT NULL COMMENT '安装者确认时间',
  `accepted_by` varchar(100) NOT NULL DEFAULT 'installer' COMMENT '确认执行者或安装入口',
  `source` varchar(30) NOT NULL DEFAULT 'installer' COMMENT '来源：installer / migration',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  PRIMARY KEY (`id`),
  KEY `idx_code_version_time` (`agreement_code`, `version_no`, `accepted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点安装协议接受记录表';

-- ---------------------------------------------------------------------
-- 4. 种子数据：注册场景三项协议（均为必选，默认不勾选）
--    幂等依据：uk_code_scene 唯一索引 + INSERT IGNORE
-- ---------------------------------------------------------------------
INSERT IGNORE INTO `hou_base_agreement` (`code`, `name`, `scene`, `required`, `default_checked`, `sort`, `status`, `remark`) VALUES
('USER_SERVICE_AGREEMENT', '用户服务协议', 'register', 1, 0, 10, 1, '服务关系与权利义务，注册必选'),
('PRIVACY_POLICY', '隐私政策', 'register', 1, 0, 20, 1, '个人信息处理规则，注册必选'),
('COMPLIANCE_COMMITMENT', '账号与内容合规使用承诺书', 'register', 1, 0, 30, 1, '账号行为规范与内容合规承诺，注册必选（本轮新增第三项）');

-- ---------------------------------------------------------------------
-- 5. 种子数据：三项协议的 v1.0.0 正文
--    说明：正文可在管理后台「系统设置 → 协议管理 → 协议版本」中维护，
--          发布新版本后，老版本用户会被要求重新同意。
-- ---------------------------------------------------------------------

-- 5.1 隐私政策 v1.0.0
INSERT IGNORE INTO `hou_base_agreement_version`
  (`agreement_id`, `agreement_code`, `version_no`, `title`, `content`, `change_note`, `effective_time`, `publish_time`, `status`, `create_by`)
SELECT a.`id`, a.`code`, 'v1.0.0', '隐私政策',
'一、适用范围\n本协议说明本平台如何收集、使用、存储、共享与保护你的个人信息。\n\n二、收集的信息\n1. 注册信息：手机号、邮箱、企业名称及联系人信息；\n2. 设备与日志信息：IP 地址、设备型号、操作系统、浏览器版本、操作日志；\n3. 业务信息：为完成服务所必需的订单、售后、结算等信息。\n\n三、使用目的\n用于账号注册与登录、身份核验、服务提供、故障排查、安全保障、合规审计与必要的服务通知。\n\n四、存储与保护\n信息存储于中华人民共和国境内，采取加密传输、访问控制、最小权限等措施保护。除法律另有规定外，仅在实现处理目的所必需的最短时间内保留。\n\n五、共享、转让与公开\n1. 仅在取得你的单独同意，或为实现服务所必需（如支付、物流、短信通道）时共享，共享方承担保密义务；\n2. 不对外公开你的个人信息，法律要求或你另行同意的除外；\n3. 涉及向第三方提供、公开或跨境提供的，将先行告知并取得单独同意。\n\n六、你的权利\n你有权查阅、复制、更正、补充、删除你的个人信息，有权撤回同意、注销账号。可通过平台设置或客服渠道行使。\n\n七、未成年人保护\n不满十四周岁未成年人的个人信息按敏感个人信息处理，并需取得监护人同意。\n\n八、政策更新\n本政策更新时，如涉及处理目的、方式、种类等重大变更，将再次征得你的同意。\n\n九、联系方式\n可通过平台公示的客服渠道就个人信息保护事项进行咨询与投诉。',
'首版发布', NOW(), NOW(), 1, 'system'
FROM `hou_base_agreement` a
WHERE a.`code` = 'PRIVACY_POLICY' AND a.`scene` = 'register';

-- 5.2 用户服务协议 v1.0.0（含现状提供、用户自担、责任上限、开源/付费边界、源码不可撤销）
INSERT IGNORE INTO `hou_base_agreement_version`
  (`agreement_id`, `agreement_code`, `version_no`, `title`, `content`, `change_note`, `effective_time`, `publish_time`, `status`, `create_by`)
SELECT a.`id`, a.`code`, 'v1.0.0', '用户服务协议',
'一、协议的订立\n本协议由你与本平台运营方（下称「平台」）订立。你勾选同意并完成注册即视为接受本协议；不同意请停止注册。本协议主要面向企业用户与开发者（B2B）；企业用户之间的付费交易以订单、发票抬头及商务约定为准。\n\n二、账号注册与使用\n1. 你应提供真实、准确、完整的资料，并及时更新；\n2. 账号所有权归平台，你取得使用权；账号仅限本人或本企业使用；\n3. 你应妥善保管账号与密码，因保管不善导致的损失由你自行承担。\n\n三、服务内容与产品边界\n1. 平台按「现状」（AS IS）向你提供软件与服务，具体功能以实际提供为准；平台有权对功能进行升级、调整、维护与下线；\n2. 开源免费核心：不附带服务水平、可用性、技术支持或官方升级承诺；\n3. 付费插件或独立商业服务：购买对价为交付权及订单载明维护期内的官方升级、安全补丁、技术支持与官方构建服务，不是终身质保，不是对业务结果或利润的保证；\n4. 维护期到期后，已安装版本可继续运行；你保留到期前已取得的源码包、运行制品及 Apache-2.0 权利，但不再获得新的官方升级、补丁、支持与构建服务，除非另行续费。\n\n四、服务变更与中止\n1. 因服务器维护、网络故障、不可抗力等原因导致服务中断的，平台不承担责任；\n2. 你存在违法、违约或危害平台安全行为的，平台有权中止或终止服务。\n\n五、费用\n平台收费项目以公示的资费标准及订单为准，调整将提前公示。已付费用的退还需符合公示的退款规则与订单约定。\n\n六、知识产权与已交付源码\n1. 平台软件、界面、文档、商标及其中包含的知识产权归平台或权利人所有，本协议不视为转让；\n2. 已合法交付且标明按 Apache License 2.0 提供的源码包，其许可权利不可撤销；维护期到期或本协议终止，均不构成收回、删除或禁止使用已交付源码的依据。\n\n七、免责、用户自担与责任上限\n1. 现状提供：平台不保证适销性、特定用途适用性、不间断、无错误、无病毒、与你二次开发或第三方软件的兼容性；\n2. 你自担：备份与容灾、日常运维、安全加固、二次开发、第三方依赖、自建或第三方插件、生产环境配置、密钥保管。平台不代运你的生产数据。任何升级、迁移、安装插件前，你应先完成可恢复的全量备份；未备份即操作导致的数据丢失、代码损坏、升级失败或环境异常，由你自行承担；\n3. 责任上限：在适用法律允许的最大范围内，因本软件或相关服务引起的全部赔偿责任，平台对你的累计赔偿不超过你就引发争议的同一产品实际已支付给平台的费用；若你未就该产品支付任何费用，累计赔偿上限为人民币零元。平台不对间接损失、利润损失、数据恢复费用、业务中断、商誉损失等承担责任，法律强制不得预先免除的除外；\n4. 因你自身操作失误、未备份、二开、自建插件、第三方软件或服务、网络故障、恶意攻击等不可归责于平台的原因造成的损失，平台不承担责任。\n\n八、协议变更\n平台可修订本协议并公示；涉及免责、责任上限、服务范围等重大变更的，可要求你重新勾选确认。继续使用服务且已完成确认的，视为接受修订后的协议。\n\n九、法律适用与争议解决\n适用中华人民共和国法律。若强制性规定不得预先排除部分责任，则以该规定为准，其余条款继续有效。协商不成的，提交平台运营方所在地有管辖权的人民法院诉讼解决。',
'首版发布（含免责、责任上限、开源/付费边界、源码不可撤销）', NOW(), NOW(), 1, 'system'
FROM `hou_base_agreement` a
WHERE a.`code` = 'USER_SERVICE_AGREEMENT' AND a.`scene` = 'register';

-- 5.4 商业插件购买与维护协议（购买场景，下单前必勾；与注册场景协议分开）
INSERT IGNORE INTO `hou_base_agreement` (`code`, `name`, `scene`, `required`, `default_checked`, `sort`, `status`, `remark`) VALUES
('PLUGIN_COMMERCIAL_PURCHASE', '商业插件购买与维护协议', 'plugin_purchase', 1, 0, 10, 1, '付费插件下单必选：交付权+维护期、责任上限、源码不可撤销');

INSERT IGNORE INTO `hou_base_agreement_version`
  (`agreement_id`, `agreement_code`, `version_no`, `title`, `content`, `change_note`, `effective_time`, `publish_time`, `status`, `create_by`)
SELECT a.`id`, a.`code`, 'v1.0.0', '商业插件购买与维护协议',
'第一条 协议性质与主体\n1.1 本协议适用于你通过官方插件中心购买付费插件或独立商业服务（下称「商业产品」）的行为，属于企业用户与平台之间的 B2B 交易条款。\n1.2 你勾选同意并完成下单支付，即视为接受本协议；不同意请停止购买。\n\n第二条 购买对价与交付范围\n2.1 你支付的费用对应的是：该商业产品约定版本的交付权（源码包与/或运行制品，以商品页与订单为准），以及首次维护期内的官方维护权益。\n2.2 购买对价不是终身质保，不是对你业务收入、利润或经营结果的保证，亦不是对与你二次开发无冲突的保证。\n2.3 购买某一商业产品仅开放该产品的源码与运行制品，不附赠其他未购买产品的源码或制品。\n\n第三条 维护期\n3.1 除非订单另有载明，首次购买含自交付完成起十二（12）个月的官方维护期。\n3.2 维护期内包含：官方升级（含兼容性预检）、安全补丁、技术支持、官方构建服务，以及已购产品对应源码包与运行制品在授权范围内的下载权。\n3.3 维护期到期后：已安装版本可继续运行；你保留到期前已取得的源码包、运行制品及 Apache-2.0 权利；但不再获得新的官方升级、安全补丁、技术支持与官方构建服务，除非续费。续费后自当前版本起做兼容性预检。\n\n第四条 已交付源码权利不可撤销\n4.1 已交付且标明按 Apache License 2.0 提供的源码包，其许可权利不可撤销。\n4.2 维护期到期、订单结束或账号停用，均不构成收回、删除或禁止使用已交付源码的依据。未续费仅影响官方维护与新制品获取权。\n\n第五条 强制备份与用户自担\n5.1 安装、升级、回滚或变更商业产品前，你必须完成可验证可回滚的全量备份（数据库、配置、重要文件）。\n5.2 你自担：备份与容灾、日常运维、安全加固、二次开发、第三方依赖、自建插件、生产环境配置。平台不代运你的生产环境。\n5.3 未备份或因二开/自建插件导致的数据丢失、代码损坏、升级失败或环境异常，由你自行承担。\n\n第六条 现状提供与责任上限\n6.1 商业产品按「现状」提供，不保证适销性、特定用途、不间断、无病毒、与你二开完全兼容。\n6.2 在适用法律允许的最大范围内，平台就该商业产品对你的累计赔偿责任，不超过你就该产品实际已支付给平台的费用；并排除间接损失、利润损失、数据恢复费用、业务中断等。法律强制不得预先免除的责任除外。\n\n第七条 法律适用\n适用中华人民共和国法律。企业用户争议按 B2B 合同与订单条款解释。协商不成的，提交平台运营方所在地有管辖权的人民法院诉讼解决。',
'首版：交付权+12个月维护、责任上限、源码不可撤销、强制备份', NOW(), NOW(), 1, 'system'
FROM `hou_base_agreement` a
WHERE a.`code` = 'PLUGIN_COMMERCIAL_PURCHASE' AND a.`scene` = 'plugin_purchase';

-- 5.5 开源 EULA（仅安装者在安装前确认；不进入站内用户登录流程）
INSERT IGNORE INTO `hou_base_agreement` (`code`, `name`, `scene`, `required`, `default_checked`, `sort`, `status`, `remark`) VALUES
('OPEN_SOURCE_EULA', '开源软件使用许可协议（EULA）', 'install', 1, 0, 10, 1, '仅安装者在安装前确认；含免责、责任上限、安装者自担');

INSERT IGNORE INTO `hou_base_agreement_version`
  (`agreement_id`, `agreement_code`, `version_no`, `title`, `content`, `change_note`, `effective_time`, `publish_time`, `status`, `create_by`)
SELECT a.`id`, a.`code`, 'v1.0.1', '开源软件使用许可协议（EULA）',
'一、协议定位\n本协议是 HouUniMall 开源软件的最终用户使用许可与免责声明（EULA）。主版权许可为 Apache License 2.0（见 LICENSE）；本协议补充免责、责任上限与运维责任边界，不缩减 Apache-2.0 已授予的权利。冲突时以 Apache-2.0 英文全文为准。完整中文说明见仓库 USAGE-AGREEMENT.md。\n\n二、现状提供（AS IS）\n软件按「现状」提供。不保证适销性、特定用途适用性、不间断运行、无错误、无病毒、与二次开发或第三方软件兼容。\n\n三、安装者自担\n安装者自行负责：备份与容灾、日常运维、安全加固、二次开发、第三方依赖、自建插件、生产环境配置与密钥保管。官方不代运安装者的生产数据。任何升级、迁移、安装插件前，必须先完成可恢复的全量备份；未备份即操作导致的数据丢失、代码损坏、升级失败或环境异常，由安装者自行承担。\n\n四、开源免费与付费分开\n开源免费核心不附带服务水平、技术支持或官方升级承诺。付费插件另受《商业插件购买与维护协议》约束：购买对价为交付权 + 维护期内官方升级/补丁/支持，不是终身质保。\n\n五、责任上限\n在适用法律允许的最大范围内，因本软件引起的全部赔偿责任，累计不超过安装者就引发争议的同一产品实际已支付给官方的费用；若未支付任何费用，上限为人民币零元。排除间接损失、利润损失、数据恢复费用、业务中断等。法律强制不得预先免除的除外。\n\n六、已交付源码权利不可撤销\n已按 Apache-2.0 交付的源码包许可权利不可撤销；维护期到期不收回源码。\n\n七、接受方式与记录\n安装者在安装脚本中勾选/确认「我已阅读并同意」后，本协议对安装者生效；版本号与接受时间写入本机安装标记、站点安装协议接受记录及站点配置 accepted_eula_version / accepted_eula_time。该记录不关联安装后的站内用户账号，也不写入站内用户协议同意记录。\n\n八、法律适用\n适用中华人民共和国法律。主要面向企业安装者与开发者（B2B）。',
'明确协议主体：仅安装者在安装前确认 EULA', NOW(), NOW(), 1, 'system'
FROM `hou_base_agreement` a
WHERE a.`code` = 'OPEN_SOURCE_EULA' AND a.`scene` = 'install';

INSERT IGNORE INTO `hou_base_sys_configs`
  (`config_key`, `config_value`, `config_type`, `config_comment`, `config_sort`, `op_type`, `op_value`, `config_enable`, `terminal_type`, `create_by`, `update_by`)
VALUES
('accepted_eula_version', '', '1', '安装者接受的开源 EULA 版本号', 0, '', '', 1, 1, 'system', 'system'),
('accepted_eula_time', '', '1', '安装者接受开源 EULA 的时间（yyyy-MM-dd HH:mm:ss）', 0, '', '', 1, 1, 'system', 'system');

-- 5.3 账号与内容合规使用承诺书 v1.0.0（本轮新增第三项）
INSERT IGNORE INTO `hou_base_agreement_version`
  (`agreement_id`, `agreement_code`, `version_no`, `title`, `content`, `change_note`, `effective_time`, `publish_time`, `status`, `create_by`)
SELECT a.`id`, a.`code`, 'v1.0.0', '账号与内容合规使用承诺书',
'第一条 承诺主体与生效\n1.1 本承诺书由注册用户（下称"承诺人"）向本平台运营方作出。\n1.2 承诺人勾选"我已阅读并同意"并完成注册时，本承诺书成立并生效，对用户服务协议、隐私政策构成补充；二者不一致的，就账号与内容合规事项以本承诺书为准。\n\n第二条 信息真实义务\n2.1 承诺人保证注册资料及后续提交的证照、授权文件真实、准确、完整、合法有效。\n2.2 资料发生变更的，应在十个工作日内更新；因资料不实或更新不及时导致的全部后果由承诺人承担。\n\n第三条 账号保管与禁止转让\n3.1 账号仅限承诺人本人或其所属企业使用，不得出租、出借、转让、出售、共享或以其他方式交由第三方使用。\n3.2 承诺人应采取必要措施保管账号、密码及验证码。账号下发生的一切行为，均视为承诺人本人行为，由承诺人承担相应责任。\n3.3 发现账号被盗用、冒用或存在安全风险的，应立即通知平台并采取补救措施。\n\n第四条 禁止用途\n承诺人不得利用账号或服务从事下列行为：\n4.1 违反宪法确定的基本原则，危害国家安全、泄露国家秘密、颠覆国家政权、破坏国家统一；\n4.2 制作、复制、发布、传播违法信息，或为违法活动提供工具、通道、推广与结算支持；\n4.3 销售或推广假冒伪劣商品、违禁品、管制物品，侵犯他人商标权、著作权、专利权、商业秘密；\n4.4 刷单炒信、虚假交易、虚假宣传、恶意评价、恶意索赔，或干扰平台排名、评价、统计秩序；\n4.5 以爬虫、抓包、批量注册、撞库、注入、DDoS 等技术手段非法获取平台数据或破坏平台正常运行；\n4.6 绕过、规避平台的技术保护措施、授权校验与计费机制；\n4.7 洗钱、非法集资、赌博、色情、传销、侵犯公民个人信息以及其他违反法律、行政法规强制性规定的活动。\n\n第五条 内容发布规范\n5.1 承诺人对其发布、上传、生成的文字、图片、音视频、商品信息及其他内容承担全部责任。\n5.2 承诺人保证对其发布的内容享有合法权利或已取得必要授权，并授予平台为提供服务、展示传播、安全审核、投诉处理之目的进行存储、复制、展示、传播的许可。\n5.3 平台有权对内容进行审核，对违法违规内容采取过滤、屏蔽、删除、下架等措施，并保留相关记录。\n\n第六条 数据与个人信息合规\n6.1 承诺人在使用服务过程中收集、处理个人信息，应遵守《中华人民共和国个人信息保护法》等规定，遵循合法、正当、必要和诚信原则。\n6.2 承诺人不得非法买卖、提供、泄露、篡改其获取的个人信息，不得超过授权范围访问、导出平台数据。\n\n第七条 违规处置\n承诺人违反本承诺书任一约定的，平台有权视情节采取下列一项或多项措施，并可视情况公示：\n7.1 警告、限期整改；\n7.2 限制或暂停部分或全部功能；\n7.3 删除违规内容、下架商品、终止交易；\n7.4 冻结或注销账号；\n7.5 依法向主管部门报告，或向司法机关报案。\n7.6 因违规处置给承诺人造成的不利后果由承诺人自行承担，已支付的费用原则上不予退还，给平台造成损失的应予赔偿。\n\n第八条 配合义务\n8.1 承诺人应配合平台及监管部门的调查，按要求提供真实资料与说明。\n8.2 平台有权依法保存承诺人的注册信息、操作日志、同意记录等数据，作为争议处理与合规审计的依据。\n\n第九条 责任承担\n9.1 因承诺人违反本承诺书导致平台被第三方索赔、被行政处罚或遭受其他损失的，承诺人应全额赔偿，包括平台因此支出的律师费、诉讼费、公证费及调查取证费用。\n9.2 承诺人不得以任何理由主张平台对其违规行为承担责任。\n\n第十条 变更与争议解决\n10.1 平台可依法修订本承诺书并公示；修订后继续使用的，视为接受修订后的内容。涉及重大变更的，平台可要求重新勾选确认。\n10.2 本承诺书的订立、效力、解释与争议解决适用中华人民共和国法律；协商不成的，提交平台运营方所在地有管辖权的人民法院诉讼解决。',
'本轮新增第三项注册协议：账号与内容合规使用承诺书，首版发布', NOW(), NOW(), 1, 'system'
FROM `hou_base_agreement` a
WHERE a.`code` = 'COMPLIANCE_COMMITMENT' AND a.`scene` = 'register';

-- ---------------------------------------------------------------------
-- 6. 菜单：系统设置 → 协议同意记录（幂等，按 paths 去重）
--    父级：hou_permission_menu 中 paths = /setting 且 type = M 的「系统设置」目录
-- ---------------------------------------------------------------------
INSERT INTO `hou_permission_menu`
  (`pid`, `type`, `name`, `icon`, `sort`, `perms`, `paths`, `component`, `selected`, `params`, `is_cache`, `is_show`, `is_disable`, `create_time`, `update_time`)
SELECT p.`id`, 'C', '协议同意记录', 'Document', 4100,
       'shell:setting:agreementConsent', '/setting/agreement-consent', 'setting/AgreementConsent',
       '/setting/agreement-consent', '', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM `hou_permission_menu` p
WHERE p.`paths` = '/setting' AND p.`type` = 'M'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `paths` FROM `hou_permission_menu`) m WHERE m.`paths` = '/setting/agreement-consent')
LIMIT 1;

-- 6.2 菜单：系统设置 → 协议管理（协议定义与版本维护入口）
INSERT INTO `hou_permission_menu`
  (`pid`, `type`, `name`, `icon`, `sort`, `perms`, `paths`, `component`, `selected`, `params`, `is_cache`, `is_show`, `is_disable`, `create_time`, `update_time`)
SELECT p.`id`, 'C', '协议管理', 'Tickets', 4050,
       'shell:setting:agreement', '/setting/agreement', 'setting/Agreement',
       '/setting/agreement', '', 0, 1, 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM `hou_permission_menu` p
WHERE p.`paths` = '/setting' AND p.`type` = 'M'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `paths` FROM `hou_permission_menu`) m WHERE m.`paths` = '/setting/agreement')
LIMIT 1;

-- ---------------------------------------------------------------------
-- 7. 角色授权：把新菜单授权给「管理员」角色（role_id=1），幂等
--    说明：hou_permission_role_menu 主键为 (role_id, menu_id)，无唯一冲突风险。
--          其它角色（普通用户 / 财务管理 / 交付组）默认不授权，按需在后台「角色管理」勾选。
-- ---------------------------------------------------------------------
INSERT IGNORE INTO `hou_permission_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.`id` FROM `hou_permission_menu` m
WHERE m.`paths` = '/setting/agreement-consent'
LIMIT 1;

-- 7.2 协议管理菜单授权给管理员角色
INSERT IGNORE INTO `hou_permission_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.`id` FROM `hou_permission_menu` m
WHERE m.`paths` = '/setting/agreement'
LIMIT 1;

-- 说明：若上条未命中（父级 /setting 目录不存在），请以实际父目录 ID 手工补录，
--      或执行以下语句挂到根目录：
-- INSERT INTO `hou_permission_menu` (`pid`,`type`,`name`,`icon`,`sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`)
-- VALUES (0,'C','协议同意记录','Document',4100,'shell:setting:agreementConsent','/setting/agreement-consent','setting/agreementConsent','/setting/agreement-consent','',0,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP());
