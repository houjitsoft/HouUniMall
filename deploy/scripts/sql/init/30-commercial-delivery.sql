-- UniMall commercial dual delivery. Idempotent migration after core plugin artifact tables.
-- Existing paid licenses keep runtime access after maintenance expiry; the new column only gates future services.
SET @db = DATABASE();
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_market_plugin_license ADD COLUMN maintenance_until DATETIME NULL COMMENT ''官方维护权益截止；不影响已交付版本运行''', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_market_plugin_license' AND column_name='maintenance_until'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
CREATE TABLE IF NOT EXISTS hou_service_release (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, plugin_code VARCHAR(64) NOT NULL, version VARCHAR(32) NOT NULL,
 host_service VARCHAR(32) NOT NULL, envelope JSON NOT NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_service_release(plugin_code,version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='独立服务双制品签名发行清单';
-- The earlier hou_service_delivery bundle table is no longer used. Existing data is
-- retained for audit only; authorization requires actual per-artifact transfer receipts.
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_plugin_artifact ADD COLUMN artifact_kind VARCHAR(16) NOT NULL DEFAULT ''jar'' COMMENT ''jar/source/oci''', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_plugin_artifact' AND column_name='artifact_kind'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_plugin_artifact ADD COLUMN host_service VARCHAR(32) NULL COMMENT ''稳定服务名''', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_plugin_artifact' AND column_name='host_service'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_plugin_artifact ADD COLUMN runtime_artifact VARCHAR(512) NULL COMMENT ''OCI 镜像摘要''', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_plugin_artifact' AND column_name='runtime_artifact'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_plugin_artifact ADD COLUMN source_license VARCHAR(32) NULL COMMENT ''源码许可证''', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_plugin_artifact' AND column_name='source_license'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 仅由 base 在安装代理返回 SUCCESS 且携带完整签名摘要后写入；提交任务或健康检查失败不会成为业务可用状态。
CREATE TABLE IF NOT EXISTS hou_service_installation (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 instance_id VARCHAR(128) NOT NULL,
 plugin_code VARCHAR(64) NOT NULL,
 version VARCHAR(32) NOT NULL,
 image_id VARCHAR(80) NOT NULL,
 source_sha256 CHAR(64) NOT NULL,
 runtime_sha256 CHAR(64) NOT NULL,
 task_sequence BIGINT NOT NULL DEFAULT 0,
 status TINYINT NOT NULL DEFAULT 1 COMMENT '1=已通过健康与注册检查，0=已卸载',
 installed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_service_installation(instance_id,plugin_code),
 KEY idx_service_installation_status(instance_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实例绑定的独立商业服务安装状态';

SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_service_installation ADD COLUMN task_sequence BIGINT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_service_installation' AND column_name='task_sequence'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

CREATE TABLE IF NOT EXISTS hou_service_install_task (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 instance_id VARCHAR(128) NOT NULL, task_id VARCHAR(36) NOT NULL,
 plugin_code VARCHAR(64) NOT NULL, version VARCHAR(32) NOT NULL,
 state VARCHAR(16) NOT NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_service_task(instance_id,task_id), KEY idx_service_task_pending(instance_id,state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='base 提交的实例容器任务与回执';

CREATE TABLE IF NOT EXISTS hou_service_entitlement_lock (
 instance_id VARCHAR(128) NOT NULL, plugin_code VARCHAR(64) NOT NULL,
 PRIMARY KEY(instance_id,plugin_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购买与退款按站点产品串行计算维护权益';
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_market_plugin_license ADD COLUMN purchase_sandbox TINYINT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_market_plugin_license' AND column_name='purchase_sandbox'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
-- Payment closure columns are required on both upgrades and a fresh core initialization.
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_market_plugin_order ADD COLUMN plan_code VARCHAR(64) NULL', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_market_plugin_order' AND column_name='plan_code'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_market_plugin_order ADD COLUMN channel VARCHAR(32) NULL', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_market_plugin_order' AND column_name='channel'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_market_plugin_order ADD COLUMN sandbox TINYINT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_market_plugin_order' AND column_name='sandbox'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_market_plugin_order ADD COLUMN fail_reason VARCHAR(500) NULL', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_market_plugin_order' AND column_name='fail_reason'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_market_plugin_order ADD COLUMN notify_count INT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_market_plugin_order' AND column_name='notify_count'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_market_plugin_order ADD COLUMN notify_time DATETIME NULL', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_market_plugin_order' AND column_name='notify_time'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
-- Existing paid orders retain their original, evidenced maintenance cutoff. Do not reset the clock on migration.
UPDATE hou_market_plugin_license l JOIN hou_market_plugin_order o ON o.license_id=l.id
 SET l.source_order_no=COALESCE(NULLIF(l.source_order_no,''),o.order_no),
     l.purchase_sandbox=IF(o.sandbox=1 OR o.channel='MOCK',1,0),
     l.maintenance_until=IF(o.sandbox=1 OR o.channel='MOCK',NULL,COALESCE(l.maintenance_until,l.expire_time,DATE_ADD(o.paid_time,INTERVAL 12 MONTH)))
 WHERE l.license_type='PAID';
UPDATE hou_market_plugin_license SET maintenance_until=COALESCE(maintenance_until,expire_time,DATE_ADD(create_time,INTERVAL 12 MONTH))
 WHERE license_type='PAID' AND status=1 AND purchase_sandbox=0 AND (source_order_no IS NULL OR source_order_no='');

CREATE TABLE IF NOT EXISTS hou_service_artifact_delivery (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, instance_id VARCHAR(128) NOT NULL, plugin_code VARCHAR(64) NOT NULL,
 version VARCHAR(32) NOT NULL, artifact_kind VARCHAR(16) NOT NULL, sha256 CHAR(64) NOT NULL,
 operator_id BIGINT NULL, delivered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_artifact_delivery(instance_id,plugin_code,version,artifact_kind,sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分别记录实际交付完成的源码及运行制品';
CREATE TABLE IF NOT EXISTS hou_service_artifact_range (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, instance_id VARCHAR(128) NOT NULL, plugin_code VARCHAR(64) NOT NULL,
 version VARCHAR(32) NOT NULL, artifact_kind VARCHAR(16) NOT NULL, sha256 CHAR(64) NOT NULL,
 range_start BIGINT NOT NULL, range_end BIGINT NOT NULL,
 UNIQUE KEY uk_artifact_range(instance_id,plugin_code,version,artifact_kind,sha256,range_start,range_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='已成功传输的分段用于断点续传交付核对';

-- Required plugin-center catalog/config columns on older core installations.
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_addon_plugin ADD COLUMN cate_id INT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_addon_plugin' AND column_name='cate_id'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_market_plugin ADD COLUMN cate_id INT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_market_plugin' AND column_name='cate_id'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = (SELECT IF(COUNT(*)=0, 'ALTER TABLE hou_addon_cate ADD COLUMN sort INT NOT NULL DEFAULT 0', 'SELECT 1') FROM information_schema.columns WHERE table_schema=@db AND table_name='hou_addon_cate' AND column_name='sort'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
CREATE TABLE IF NOT EXISTS hou_market_pay_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    scope        VARCHAR(16)  NOT NULL                COMMENT 'GLOBAL=全局 / CHANNEL=渠道 / PLAN=价格套餐',
    scope_key    VARCHAR(64)  NOT NULL                COMMENT 'GLOBAL 固定 "*"；CHANNEL 为渠道码；PLAN 为插件编码',
    sub_key      VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT 'PLAN 时为套餐码；其余留空串',
    config_key   VARCHAR(64)  NOT NULL                COMMENT '配置键，见下方「可用键」清单',
    config_value TEXT         NULL                    COMMENT '配置值；secret=1 时只能是 ${ENV_NAME} 引用',
    value_type   VARCHAR(16)  NOT NULL DEFAULT 'STRING' COMMENT 'STRING / INT / DECIMAL / BOOL',
    secret       TINYINT      NOT NULL DEFAULT 0      COMMENT '1=密钥类：库里只存环境变量名，明文禁止入库',
    remark       VARCHAR(255) NULL                    COMMENT '备注（运营可见，会下发到配置页）',
    update_by    VARCHAR(64)  NULL                    COMMENT '最后修改人',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config (scope, scope_key, sub_key, config_key),
    KEY idx_scope (scope, scope_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件支付配置（DB 覆盖 Nacos/yml 基线；密钥仅存引用）';
