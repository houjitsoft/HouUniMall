-- =====================================================================
--  HouCloud 线上插件目录与分发 —— DDL（M0 地基 + M1 上传 + M3 补列与修键）
--  文件：plugin-artifact/src/main/resources/sql/plugin_artifact_tables.sql
--
--  ★幂等性说明（重要）：
--    本文件**可以反复执行**，每次执行结果一致，不会产生「列已存在 / 索引已存在」报错。
--    实现手法：MySQL 原生不支持 `ADD COLUMN IF NOT EXISTS` / `DROP INDEX IF EXISTS`，
--    因此统一用 information_schema 判断存在性，再拼动态 SQL 交给 PREPARE/EXECUTE 执行；
--    不存在需要变更时才执行 DDL，否则执行 `DO 0`（空操作）。
--    建表一律 `CREATE TABLE IF NOT EXISTS`。
--
--  执行方式：
--    1) 先 `USE <目标库>;`（脚本内所有判断都基于 `DATABASE()` 当前库），再整体执行本文件；
--    2) 生产执行前请备份涉及的表（hou_addon_plugin_migration / hou_market_plugin_version）；
--    3) 项目当前未引入 Flyway/Liquibase，本文件由人工在需要时执行，不做自动迁移。
--
--  ⚠️ 版本要求（仅第六段）：
--    第六段用「生成列 + 唯一索引」修 uk_idem，需要 MySQL 5.7+ / MariaDB 10.2+。
--    脚本会自行探测版本，不满足时保留旧键并打印告警，不会报错中断、也不会留下无互斥的半成品。
--
--  ⚠️ 部署顺序（M1 新增）：
--    本文件会为 `hou_market_plugin_version` 增加 12 列，而 `MarketPluginVersion` 实体
--    已同步新增这些字段。MyBatis-Plus 生成的 SELECT 会带上新列，
--    因此**必须先执行本文件再发布 mall**，否则 PluginMarketService 查询会因缺列报错。
-- =====================================================================

SET @hou_schema := DATABASE();


-- =====================================================================
--  一、改键：hou_addon_plugin_migration（已存在，改唯一键 + 扩 5 列）
--
--  原因：原唯一键 uk_plugin_version(plugin_code, version) 的语义是
--        「同一版本只允许一条成功记录」，无法表达三种不同动作：
--          install  : 空 -> 1.0.0
--          upgrade  : 1.0.0 -> 1.1.0
--          rollback : 1.1.0 -> 1.0.0
--        upgrade 与 rollback 会复用同一个 to_version（升上去再回滚回来），
--        旧唯一键会把第二次操作直接卡死。
--  做法：唯一键改为 uk_mig_op(plugin_code, version, operation, from_version)，
--        并补充 operation / from_version / operator_id / duration_ms / rollback_of 五列。
--
--  from_version 为什么是 NOT NULL DEFAULT ''：
--        MySQL 唯一键中 NULL 值互不冲突，若允许 NULL，则 operation=1(install)
--        的重复插入无法被 uk_mig_op 拦截，唯一键形同虚设。
--        因此 install 统一写空串，保证唯一键真正生效。
--        原建表语句位于 plugin-registry/.../plugin_addon_tables.sql，
--        为保证已有环境建表语义不被改动，这里不修改原文件，只追加幂等 ALTER。
-- =====================================================================

-- 1.1 删除旧唯一键 uk_plugin_version（存在才删）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND index_name = 'uk_plugin_version');
SET @v_ddl := IF(@v_exists > 0,
                 'ALTER TABLE hou_addon_plugin_migration DROP INDEX uk_plugin_version',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.2 operation：1=install 2=upgrade 3=rollback
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND column_name = 'operation');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD COLUMN operation TINYINT NOT NULL DEFAULT 1 COMMENT ''1=install 2=upgrade 3=rollback''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.3 from_version：来源版本，install 时写空串（NOT NULL 才能被唯一键约束）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND column_name = 'from_version');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD COLUMN from_version VARCHAR(32) NOT NULL DEFAULT '''' COMMENT ''来源版本（install 时为空串）''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.4 operator_id：操作人
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND column_name = 'operator_id');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD COLUMN operator_id BIGINT NULL COMMENT ''操作人''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.5 duration_ms：迁移耗时，慢 DDL 排查
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND column_name = 'duration_ms');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD COLUMN duration_ms INT NULL COMMENT ''迁移耗时，慢 DDL 排查''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.6 rollback_of：回滚自哪次迁移 ID
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND column_name = 'rollback_of');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD COLUMN rollback_of BIGINT NULL COMMENT ''回滚自哪次迁移ID''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 1.7 存量数据回填：历史行 from_version 可能为 NULL，统一改成空串
UPDATE hou_addon_plugin_migration SET from_version = '' WHERE from_version IS NULL;

-- 1.8 新唯一键 uk_mig_op（不存在才建；必须在列补齐之后）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema
                    AND table_name = 'hou_addon_plugin_migration'
                    AND index_name = 'uk_mig_op');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_migration ADD UNIQUE KEY uk_mig_op (plugin_code, version, operation, from_version)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;


-- =====================================================================
--  二、扩列：hou_market_plugin_version（已存在，扩 12 列 —— 方案 §3.2）
--
--  这些列是 M1 上传链路的落点：storage_* 指向不可变存储身份（与可变的 jar_url 分离）、
--  jar_sha256 + signature_kid 支撑防篡改与密钥轮换、upload_status/upload_id 支撑上传幂等。
--  状态取值沿用现有代码：0=draft、1=published、3=pending、4=rejected、5=offline。
-- =====================================================================

-- 2.1 storage_type
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'storage_type');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN storage_type VARCHAR(16) NOT NULL DEFAULT ''LOCAL'' COMMENT ''LOCAL/OSS/COS''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.2 storage_key：不可变存储身份（jar_url 是可变网关地址，两者必须分开）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'storage_key');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN storage_key VARCHAR(512) NULL COMMENT ''存储对象键（与 jar_url 分离：jar_url 可变，key 才是不可变身份）''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.3 jar_sha256
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'jar_sha256');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN jar_sha256 VARCHAR(64) NULL COMMENT ''制品 SHA-256（md5 仅防误传，sha256+签名防篡改）''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.4 signature_kid：签名密钥 ID，密钥轮换必需
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'signature_kid');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN signature_kid VARCHAR(64) NULL COMMENT ''签名密钥ID，密钥轮换必需''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.5 manifest_sha256：plugin.yaml 摘要，防清单被掉包
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'manifest_sha256');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN manifest_sha256 VARCHAR(64) NULL COMMENT ''plugin.yaml 摘要，防清单被掉包''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.6 upload_status：0=上传中 1=完成 2=失败
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'upload_status');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN upload_status TINYINT NOT NULL DEFAULT 0 COMMENT ''0=上传中 1=完成 2=失败''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.7 upload_id：客户端幂等键 clientOpId
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'upload_id');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN upload_id VARCHAR(64) NULL COMMENT ''客户端幂等键 clientOpId''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.8 reviewer_id
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'reviewer_id');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN reviewer_id BIGINT NULL COMMENT ''审核人''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.9 submit_time
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'submit_time');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN submit_time DATETIME NULL COMMENT ''提交审核时间''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.10 offline_time
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'offline_time');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN offline_time DATETIME NULL COMMENT ''下架时间''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.11 offline_reason
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'offline_reason');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN offline_reason VARCHAR(255) NULL COMMENT ''下架原因''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.12 version_download_count：版本级下载埋点
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND column_name = 'version_download_count');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD COLUMN version_download_count INT NOT NULL DEFAULT 0 COMMENT ''版本级下载埋点''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.13 索引：idx_version_status（不存在才建）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND index_name = 'idx_version_status');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD KEY idx_version_status (plugin_code, status)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 2.14 唯一键：uk_version_upload（不存在才建；upload_id 为 NULL 的历史行不受影响）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_version'
                    AND index_name = 'uk_version_upload');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_version ADD UNIQUE KEY uk_version_upload (plugin_code, upload_id)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;


-- =====================================================================
--  三、新增表
-- =====================================================================

-- 3.6 hou_plugin_artifact —— 制品权威 + 上传幂等锁（新增）
-- artifact_id = sha256(code|version|md5) 的完整 64 位十六进制，是制品的不可变身份；
-- 早期方案只取 40 位截断，存在碰撞风险，字段长度本身成本为零，故改存完整摘要。
-- upload_id 为客户端幂等键，重复提交同一 upload_id 直接返回首次结果，避免重复写盘。
CREATE TABLE IF NOT EXISTS hou_plugin_artifact (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  artifact_id   VARCHAR(64)  NOT NULL COMMENT '制品ID = sha256(code|version|md5) 完整十六进制，不可变身份',
  plugin_code   VARCHAR(64)  NOT NULL COMMENT '插件编码',
  version       VARCHAR(32)  NOT NULL COMMENT '插件版本',
  storage_type  VARCHAR(16)  NOT NULL COMMENT '存储类型：local/cos',
  storage_key   VARCHAR(512) NOT NULL COMMENT '存储键 {code}/{version}/{file}',
  artifact_kind VARCHAR(16) NOT NULL DEFAULT 'jar' COMMENT 'jar/source/oci',
  host_service  VARCHAR(32) NULL COMMENT '稳定服务名',
  runtime_artifact VARCHAR(512) NULL COMMENT 'OCI 镜像摘要',
  source_license VARCHAR(32) NULL COMMENT 'Apache-2.0 源码许可证',
  md5           VARCHAR(64)  NOT NULL COMMENT '制品 md5（小写十六进制）',
  sha256        VARCHAR(64)  NOT NULL COMMENT '制品 sha256（小写十六进制）',
  size_bytes    BIGINT       NOT NULL COMMENT '制品字节数',
  upload_status TINYINT      NOT NULL DEFAULT 0 COMMENT '0=上传中 1=已完成 2=已作废',
  upload_id     VARCHAR(64)  NOT NULL COMMENT '客户端幂等键',
  created_by    BIGINT       NULL COMMENT '上传人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_artifact_id (artifact_id),
  UNIQUE KEY uk_upload_id (upload_id),
  KEY idx_code_version (plugin_code, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件制品元信息与上传幂等（新增）';

-- 3.7 hou_plugin_update_task —— 安装/升级/回滚任务（新增）
-- 幂等：uk_idem(instance_id, plugin_code, op_type, to_version, state)
--       重复点击不产生第二条任务；state 进入终态后同一组合才允许再建一条。
-- 断点：step 记录当前阶段，失败重启后从该阶段续做，不必从下载重来。
CREATE TABLE IF NOT EXISTS hou_plugin_update_task (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id      VARCHAR(40)  NOT NULL COMMENT '服务端生成',
  instance_id  VARCHAR(128) NOT NULL COMMENT '站点实例（执行主体）',
  plugin_code  VARCHAR(64)  NOT NULL COMMENT '插件编码',
  op_type      VARCHAR(16)  NOT NULL COMMENT 'INSTALL/UPGRADE/ROLLBACK',
  from_version VARCHAR(32)  NULL COMMENT '来源版本（INSTALL 时为 NULL）',
  to_version   VARCHAR(32)  NOT NULL COMMENT '目标版本',
  state        VARCHAR(16)  NOT NULL COMMENT 'PENDING/RUNNING/SUCCESS/ROLLED_BACK/FAILED',
  step         VARCHAR(32)  NULL COMMENT 'DOWNLOAD/VERIFY/COMPAT/BACKUP/STOP/SCHEMA/START/HEALTH',
  backup_path  VARCHAR(512) NULL COMMENT '备份版本目录，恢复用',
  error_code   VARCHAR(32)  NULL COMMENT '错误码（3001-3018 体系）',
  error_msg    VARCHAR(1024) NULL COMMENT '错误描述',
  operator_id  BIGINT       NULL COMMENT '操作人',
  start_time   DATETIME     NULL COMMENT '开始时间',
  end_time     DATETIME     NULL COMMENT '结束时间',
  UNIQUE KEY uk_idem (instance_id, plugin_code, op_type, to_version, state) COMMENT '幂等：重复点击不产生第二条任务',
  UNIQUE KEY uk_task_id (task_id),
  KEY idx_inst_state (instance_id, state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件安装/升级/回滚任务（新增）';

-- 3.8 hou_plugin_download_token —— 下载链接签发留痕（新增）
-- max_hits 设计成「限次」而非「一次性」：断点续传/网络重试会多次命中同一 token，
-- 一次性会直接把正常的重试打死。nonce 受 uk_nonce 唯一约束实现防重放，落库用于事后审计溯源。
-- 数据保留建议：本表只用于泄露后的溯源追责，不参与任何业务流程，
--   ★建议保留 90 天★（M3 不实现定期清理，数据量上涨后由运维按此策略归档/清理）。
CREATE TABLE IF NOT EXISTS hou_plugin_download_token (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  token_id    VARCHAR(40) NOT NULL COMMENT '令牌ID',
  nonce       VARCHAR(32) NOT NULL COMMENT '防重放随机串（Redis SETNX 原子占用）',
  instance_id VARCHAR(128) NOT NULL COMMENT '站点实例',
  user_id     BIGINT NULL COMMENT '下载用户',
  plugin_code VARCHAR(64) NOT NULL COMMENT '插件编码',
  version     VARCHAR(32) NOT NULL COMMENT '插件版本',
  artifact_id VARCHAR(64) NOT NULL COMMENT '制品ID（长度与 hou_plugin_artifact.artifact_id 对齐：完整 sha256 十六进制）',
  issued_at   DATETIME NOT NULL COMMENT '签发时间',
  expires_at  DATETIME NOT NULL COMMENT '过期时间',
  max_hits    INT NOT NULL DEFAULT 5 COMMENT '限次而非一次：兼容断点续传/重试',
  hits        INT NOT NULL DEFAULT 0 COMMENT '已命中次数',
  client_ip   VARCHAR(64) NULL COMMENT '客户端 IP，泄露溯源',
  user_agent  VARCHAR(255) NULL COMMENT '客户端 UA，泄露溯源',
  UNIQUE KEY uk_token_id (token_id),
  UNIQUE KEY uk_nonce (nonce),
  KEY idx_inst_code (instance_id, plugin_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载链接签发留痕（新增）';


-- =====================================================================
--  四、扩列：hou_market_plugin（已存在，扩 3 列 —— 方案 §3.1「插件目录」）
--
--  这三列把「插件商品」从「平台自营」扩展到「第三方可发布」：
--    visibility   —— 谁能看见（私有 / 公开 / 指定租户），目录页按它过滤；
--    publisher_id —— 发布者（第三方开发者账号），用于「我的发布」列表与责任溯源；
--    source_repo  —— 源码仓库地址，审核时人工复核用。
--
--  ⚠️ M2 只落列、不落 Java 字段：MarketPlugin 实体暂不新增这三个属性，
--     否则 MyBatis-Plus 的 SELECT 会带上新列，形成「先发版还是先执行 SQL」的部署顺序耦合。
--     等 M3 目录页真正要读写它们时再一次性补实体 + 门控。
-- =====================================================================

-- 4.1 visibility：0=私有 1=公开 2=指定租户
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin'
                    AND column_name = 'visibility');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin ADD COLUMN visibility TINYINT NOT NULL DEFAULT 1 COMMENT ''0=私有 1=公开 2=指定租户''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 4.2 publisher_id：发布者（第三方开发者账号）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin'
                    AND column_name = 'publisher_id');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin ADD COLUMN publisher_id BIGINT NULL COMMENT ''发布者（第三方开发者）''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 4.3 source_repo：源码仓库地址，审核时人工复核用
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin'
                    AND column_name = 'source_repo');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin ADD COLUMN source_repo VARCHAR(255) NULL COMMENT ''源码仓库地址''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 4.4 索引：idx_visibility（目录页按可见性过滤，不存在才建）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin'
                    AND index_name = 'idx_visibility');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin ADD KEY idx_visibility (visibility, status)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;


-- =====================================================================
--  五、扩列：hou_addon_plugin_install（已存在，扩 5 列 —— 方案 §3.5）
--
--  这五列是「插件安装态」从静态快照升级为「可编排」的落点：
--    update_status      —— 非终态互斥锁（0 空闲 / 1 安装中 / 2 升级中 / 3 回滚中 / 4 上次失败），
--                          M4 的并发互斥与「排空窗口」都读它；
--    update_started_at  —— 上锁时间，M4 的孤儿任务扫描靠它判断「锁了太久 = 进程死了」；
--    previous_version   —— 回滚锚点：升级成功后它指向升级前版本，回滚不是猜出来的；
--    upgradable_version —— 缓存「线上存在的最新更高版本」，避免目录页每次都回源算；
--    last_error         —— 最近一次失败原因，前端不必再查任务表就能展示「上次为什么失败」。
--
--  ⚠️ M3 只落列、不落 Java 字段：MallPluginInstall 实体暂不新增这五个属性，
--     否则 MyBatis-Plus 的 SELECT 会带上新列，形成「先发版还是先执行 SQL」的部署顺序耦合
--     （与第四节 hou_market_plugin 三列同样的取舍）。M4 实现升级/回滚时再一次性补实体。
-- =====================================================================

-- 5.1 update_status：0=空闲 1=安装中 2=升级中 3=回滚中 4=上次失败
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND column_name = 'update_status');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD COLUMN update_status TINYINT NOT NULL DEFAULT 0 COMMENT ''0=空闲 1=安装中 2=升级中 3=回滚中 4=上次失败''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 5.2 update_started_at：上锁时间，孤儿任务超时解锁用
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND column_name = 'update_started_at');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD COLUMN update_started_at DATETIME NULL COMMENT ''本轮更新开始时间，用于识别孤儿任务超时解锁''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 5.3 previous_version：升级前版本，回滚锚点
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND column_name = 'previous_version');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD COLUMN previous_version VARCHAR(32) NULL COMMENT ''升级前版本，回滚锚点''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 5.4 upgradable_version：缓存线上存在的最新更高版本
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND column_name = 'upgradable_version');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD COLUMN upgradable_version VARCHAR(32) NULL COMMENT ''缓存：线上存在的最新更高版本''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 5.5 last_error：最近一次失败原因
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND column_name = 'last_error');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD COLUMN last_error VARCHAR(1024) NULL COMMENT ''最近一次更新失败原因''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 5.6 索引：idx_update_status（M4 孤儿任务扫描按 update_status + update_started_at 扫，不存在才建）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_addon_plugin_install'
                    AND index_name = 'idx_update_status');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_addon_plugin_install ADD KEY idx_update_status (update_status, update_started_at)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;


-- =====================================================================
--  六、修键：hou_plugin_update_task 的 uk_idem —— 语义与设计意图相反
--
--  【原键】UNIQUE KEY uk_idem (instance_id, plugin_code, op_type, to_version, state)
--  【真实语义】MySQL 唯一键是「值全部相同才冲突」，state 进了键里，
--             于是真实约束变成「同一组合的**同一状态**只能有一条」：
--               · 非终态互斥：成立（PENDING / RUNNING 各自唯一，第二条插不进来 → 3016）；
--               · 终态历史：❌ 反了——第二次成功安装置 SUCCESS 时会撞上一次的 SUCCESS 行。
--  【后果】代码只能在 PluginUpdateService#finishTask 里用「合并进已存在的终态行」兜底，
--         历史因此永远只剩「最后一次」，第 2、3 次安装记录被抹掉。
--
--  【修法】函数式唯一键：加一个生成列 active_key
--             非终态（PENDING / RUNNING）→ CONCAT(instance_id,'|',plugin_code,'|',op_type,'|',to_version)
--             终态（SUCCESS / FAILED / ROLLED_BACK）→ NULL
--          MySQL / MariaDB 唯一索引中 NULL 互不冲突，于是：
--               · 非终态仍然严格互斥（并发 3016 语义不变）；
--               · 终态可以任意多条共存，完整历史得以保留，合并兜底不再被触发。
--          （MySQL 8.0.13+ 也可以用「函数索引」直接写表达式，但生成列兼容到 5.7，
--            且 information_schema 里看得见、SHOW CREATE TABLE 里读得懂，排查更省事。）
--
--  ⚠️ 前置条件：生成列需要 MySQL 5.7+ / MariaDB 10.2+（本文件其余部分无此要求）。
--     脚本会先探测版本：不满足时**保留旧键 uk_idem 不动**并打印告警，
--     绝不留「旧键已删、新键没建」这种没有互斥保护的半成品。
-- =====================================================================

-- 6.0 版本探测：@v_gen_ok=1 才允许改键
--     版本串形如 '8.0.33' / '5.7.44-log' / '10.11.2-MariaDB'，取前两段判断
SET @v_version := @@version;
SET @v_major := CAST(SUBSTRING_INDEX(@v_version, '.', 1) AS UNSIGNED);
SET @v_minor := CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(@v_version, '.', 2), '.', -1) AS UNSIGNED);
SET @v_mariadb := @v_version LIKE '%MariaDB%';
SET @v_gen_ok := IF(@v_mariadb,
                    (@v_major > 10 OR (@v_major = 10 AND @v_minor >= 2)),
                    (@v_major > 5 OR (@v_major = 5 AND @v_minor >= 7)));

-- 新键是否已存在（决定后面每一步是不是空操作）
SET @v_uk_active_exists := (SELECT COUNT(*) FROM information_schema.statistics
                            WHERE table_schema = @hou_schema AND table_name = 'hou_plugin_update_task'
                              AND index_name = 'uk_active');

-- 6.1 建新键前的必要清理：同一组合若存在多条非终态任务，ADD UNIQUE KEY 会直接报 Duplicate entry
--     旧 uk_idem 允许「一条 PENDING + 一条 RUNNING」共存（第二条 PENDING 与已有 RUNNING 状态不同，不冲突），
--     这两条在新键下 active_key 完全相同。这类行本质上是历史孤儿（服务只会跟踪其中一条），
--     保留 id 最大的那条，其余统一收敛为 FAILED/ORPHAN_TIMEOUT —— 与 M4 孤儿任务扫描的处理一致。
--     收敛后重跑本脚本不会再有重复组，因此这一步也是幂等的。
UPDATE hou_plugin_update_task
   SET state      = 'FAILED',
       step       = 'HEALTH',
       error_code = 'ORPHAN_TIMEOUT',
       error_msg  = '迁移 uk_active 前清理：同一组合存在多条非终态任务，仅保留最新一条',
       end_time   = NOW()
 WHERE @v_gen_ok = 1
   AND @v_uk_active_exists = 0
   AND state IN ('PENDING', 'RUNNING')
   AND id NOT IN (SELECT id FROM (SELECT MAX(id) AS id
                                    FROM hou_plugin_update_task
                                   WHERE state IN ('PENDING', 'RUNNING')
                                   GROUP BY instance_id, plugin_code, op_type, to_version) keep_newest);

SELECT IF(@v_gen_ok = 0,
          CONCAT('⚠️ 当前 MySQL/MariaDB 版本 ', @v_version, ' 不支持生成列，已跳过 uk_active 改造，旧键 uk_idem 保持不变'),
          IF(@v_uk_active_exists > 0,
             'uk_active 已存在，跳过本段',
             'uk_active 前置检查通过：同一组合最多一条非终态任务')) AS ddl_notice;

-- 6.2 生成列 active_key（不存在才加；终态为 NULL，唯一索引下 NULL 不冲突）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_plugin_update_task'
                    AND column_name = 'active_key');
SET @v_ddl := IF(@v_gen_ok = 1 AND @v_exists = 0,
                 'ALTER TABLE hou_plugin_update_task '
                 'ADD COLUMN active_key VARCHAR(255) GENERATED ALWAYS AS ('
                 'IF(state IN (''PENDING'',''RUNNING''), '
                 'CONCAT(instance_id,''|'',plugin_code,''|'',op_type,''|'',to_version), NULL)) VIRTUAL '
                 'COMMENT ''非终态互斥键：终态为 NULL，唯一索引下 NULL 不冲突''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 6.3 唯一键 uk_active（不存在才建；必须在旧键删除之前，全程保留互斥保护）
SET @v_ddl := IF(@v_gen_ok = 1 AND @v_uk_active_exists = 0,
                 'ALTER TABLE hou_plugin_update_task ADD UNIQUE KEY uk_active (active_key)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 6.4 删除旧键 uk_idem（新键已生效才删；版本不支持时保留旧键兜底）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_plugin_update_task'
                    AND index_name = 'uk_idem');
SET @v_ddl := IF(@v_gen_ok = 1 AND @v_exists > 0,
                 'ALTER TABLE hou_plugin_update_task DROP INDEX uk_idem',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- =====================================================================
--  七、扩列：hou_market_plugin_license（已存在，扩 6 列 —— 方案 §3.3，M5 试用期）
--
--  背景：授权原先只有「status（1 有效 / 0 吊销）+ expire_time」两态，
--        无法回答三个业务问题：
--          ① 这份授权走到生命周期哪一步了（签发 / 绑定 / 激活 / 过期 / 吊销 / 解绑）？
--          ② 试用期从哪天开始算？（原设计按签发时间算，会出现「买了没装却在倒计时」）
--          ③ 这份授权为什么被吊销？（退款 / 违规 / 紧急下架，无溯源字段）
--
--    state              —— 生命周期状态机（§7.2）。与 status 正交：
--                          status 是「能不能用」的粗开关，state 是「走到哪一步」；
--                          复用 status 表达生命周期会破坏既有 findValid 语义，因此另开一列。
--    bind_time          —— 首次绑定站点实例时间。
--    first_install_time —— ★试用期起算点：首次<b>成功安装</b>的时刻（不是签发、不是下载）。
--                          回填后 expire_time = first_install_time + trial_days。
--                          「卸载重装」不重置该列，否则等于无限续期试用。
--    source_order_no    —— 来源订单号，吊销溯源。
--    revoke_reason      —— 吊销原因（平台 / 退款 / 违规 / 紧急下架）。
--    last_heartbeat     —— 运行时最后一次授权心跳（M5 只落列，心跳任务未接）。
--
--  ⚠️ 部署顺序：MarketPluginLicense 实体已同步新增 state / bind_time /
--     first_install_time / source_order_no / revoke_reason / last_heartbeat 六个属性，
--     MyBatis-Plus 生成的 SELECT 会带上新列，**必须先执行本文件再发布 mall**，
--     否则授权查询会因缺列报错（与第四段 hou_market_plugin 的口径一致）。
--
--  ⚠️ 存量数据：state 一律回填 'ISSUED' 的默认值。已激活（装过）的历史授权
--     没有 first_install_time 可追，其 expire_time 保持原值不变——
--     不做「现在补一个起算点」的猜测，避免把老租户的过期时间改短。
-- =====================================================================

-- 7.1 state：生命周期状态（ISSUED/BOUND/ACTIVATED/EXPIRED/REVOKED/UNBOUND）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'state');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN state VARCHAR(16) NOT NULL DEFAULT ''ISSUED'' COMMENT ''ISSUED/BOUND/ACTIVATED/EXPIRED/REVOKED/UNBOUND''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.2 bind_time：首次绑定站点实例时间
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'bind_time');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN bind_time DATETIME NULL COMMENT ''首次绑定站点实例时间''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.3 first_install_time：★首次成功安装时间 = 试用期起算点(P1)
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'first_install_time');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN first_install_time DATETIME NULL COMMENT ''★首次成功安装时间 = 试用期起算点(P1)''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.4 source_order_no：来源订单号，吊销溯源
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'source_order_no');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN source_order_no VARCHAR(64) NULL COMMENT ''来源订单号，吊销溯源''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.5 revoke_reason：吊销原因（平台/退款/违规/紧急下架）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'revoke_reason');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN revoke_reason VARCHAR(255) NULL COMMENT ''吊销原因（平台/退款/违规/紧急下架）''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.6 last_heartbeat：运行时最后一次授权心跳(P1)
SET @v_exists := (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND column_name = 'last_heartbeat');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD COLUMN last_heartbeat DATETIME NULL COMMENT ''运行时最后一次授权心跳(P1)''',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

-- 7.7 索引：idx_license_state（过期迁移与心跳按 state + expire_time 扫，不存在才建）
SET @v_exists := (SELECT COUNT(*) FROM information_schema.statistics
                  WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                    AND index_name = 'idx_license_state');
SET @v_ddl := IF(@v_exists = 0,
                 'ALTER TABLE hou_market_plugin_license ADD KEY idx_license_state (state, expire_time)',
                 'DO 0');
PREPARE hou_stmt FROM @v_ddl; EXECUTE hou_stmt; DEALLOCATE PREPARE hou_stmt;

SELECT CONCAT('hou_market_plugin_license 扩列完成：state=',
              (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                  AND column_name = 'state'),
              ' first_install_time=',
              (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = @hou_schema AND table_name = 'hou_market_plugin_license'
                  AND column_name = 'first_install_time')) AS ddl_notice;
