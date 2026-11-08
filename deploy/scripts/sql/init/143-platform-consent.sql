-- 平台侧：站点协议签署记录回传（2026-10-29）
--
-- 背景：站点（客户部署的站）里用户同意协议后，记录原本只落在站点自己的
-- hou_user_agreement_consent（"站内"）。但协议是平台（unimall.houjit.com）统一发布的，
-- 平台侧需要留存各站点、各用户的签署凭据用于合规举证与统计，因此站点在写完站内记录后，
-- 会把同一条签署记录回传到平台，落在本表。
--
-- 口径：本表只做「平台侧留痕」，不替代站内记录；站点侧回传失败不影响站内留痕（合规主记录
-- 仍在站内）。回传按 (site_code, user_id, agreement_code, version_no) 幂等，重复上报只更新
-- 最近一次时间与来源，不产生重复行。

CREATE TABLE IF NOT EXISTS hou_platform_site (
  id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  site_code         VARCHAR(64)  NOT NULL                COMMENT '站点唯一标识（站点配置 houcloud.platform.site-code）',
  site_name         VARCHAR(128)      DEFAULT NULL       COMMENT '站点名称（站点自带，便于平台识别）',
  first_report_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次回传时间',
  last_report_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次回传时间',
  report_count      BIGINT       NOT NULL DEFAULT 0      COMMENT '累计回传条数',
  PRIMARY KEY (id),
  UNIQUE KEY uk_site_code (site_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='平台侧：已接入协议回传的站点';

CREATE TABLE IF NOT EXISTS hou_platform_site_consent (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  site_code      VARCHAR(64)  NOT NULL                COMMENT '站点唯一标识（冗余，便于按站点查）',
  site_name      VARCHAR(128)      DEFAULT NULL       COMMENT '站点名称（冗余）',
  user_id        VARCHAR(64)       DEFAULT NULL       COMMENT '站内用户ID（平台侧不做用户体系关联，仅留痕）',
  username       VARCHAR(128)      DEFAULT NULL       COMMENT '登录名/手机号（便于人工核对）',
  agreement_code VARCHAR(64)  NOT NULL                COMMENT '协议编码，如 USER_SERVICE_AGREEMENT',
  agreement_name VARCHAR(128)      DEFAULT NULL       COMMENT '协议名称（冗余，平台侧不必回查协议定义）',
  version_no     VARCHAR(32)  NOT NULL                COMMENT '协议版本号，如 v1.0.0',
  scene          VARCHAR(32)       DEFAULT NULL       COMMENT '场景：register / reagree / plugin_purchase',
  source         VARCHAR(32)       DEFAULT NULL       COMMENT '来源端：web / h5 / app 等',
  ip             VARCHAR(64)       DEFAULT NULL       COMMENT '同意时客户端IP',
  user_agent     VARCHAR(512)      DEFAULT NULL       COMMENT '同意时 UA',
  consent_time   DATETIME          DEFAULT NULL       COMMENT '站内记录的同意时间',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '平台落库时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_site_user_agreement_version (site_code, user_id, agreement_code, version_no),
  KEY idx_site_code (site_code),
  KEY idx_agreement_code (agreement_code),
  KEY idx_consent_time (consent_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='平台侧：各站点回传的协议签署记录';
