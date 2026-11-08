-- 银联商务企业订单支付插件表（前缀 hou_ums_）

CREATE TABLE IF NOT EXISTS hou_ums_corp_config (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  org_id        BIGINT       NOT NULL DEFAULT 0 COMMENT '组织/站点',
  env           VARCHAR(16)  NOT NULL DEFAULT 'test' COMMENT 'test|prod',
  group_id      VARCHAR(16)  NOT NULL DEFAULT '' COMMENT '集团号',
  mer_no        VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '企业用户号',
  channel_id    VARCHAR(8)   NOT NULL DEFAULT '043',
  ver_no        VARCHAR(8)   NOT NULL DEFAULT '100',
  base_url      VARCHAR(255) NOT NULL DEFAULT 'https://mobl-test.chinaums.com/channel/Business/UnifyMulti',
  mch_private_key TEXT       NULL COMMENT '商户私钥 PEM',
  ums_public_key  TEXT       NULL COMMENT '银商公钥 PEM',
  notify_url    VARCHAR(255) NOT NULL DEFAULT '',
  enabled       TINYINT      NOT NULL DEFAULT 1,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_org (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银联企业订单渠道配置';

CREATE TABLE IF NOT EXISTS hou_ums_corp_order (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  org_id          BIGINT       NOT NULL DEFAULT 0,
  mer_no          VARCHAR(32)  NOT NULL DEFAULT '',
  order_no        VARCHAR(64)  NOT NULL COMMENT '商户订单号',
  src_req_id      VARCHAR(64)  NOT NULL COMMENT '请求流水号',
  src_req_date    VARCHAR(8)   NOT NULL DEFAULT '',
  order_amt       BIGINT       NOT NULL DEFAULT 0 COMMENT '金额分',
  payer_no        VARCHAR(100) NOT NULL DEFAULT '',
  payer_name      VARCHAR(200) NOT NULL DEFAULT '',
  goods_name      VARCHAR(100) NOT NULL DEFAULT '',
  goods_num       VARCHAR(32)  NOT NULL DEFAULT '',
  order_state     VARCHAR(8)   NOT NULL DEFAULT '0',
  mark_no         VARCHAR(64)  NOT NULL DEFAULT '',
  mark_name       VARCHAR(128) NOT NULL DEFAULT '',
  bank_no         VARCHAR(32)  NOT NULL DEFAULT '',
  bank_name       VARCHAR(128) NOT NULL DEFAULT '',
  valid_date      VARCHAR(16)  NOT NULL DEFAULT '',
  order_pay_date  VARCHAR(16)  NOT NULL DEFAULT '',
  refunded_amt    BIGINT       NOT NULL DEFAULT 0,
  fail_reason     VARCHAR(255) NOT NULL DEFAULT '',
  raw_resp        MEDIUMTEXT   NULL,
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_order_no (org_id, order_no),
  KEY idx_src_req (org_id, src_req_id),
  KEY idx_state (org_id, order_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银联企业订单';

CREATE TABLE IF NOT EXISTS hou_ums_corp_op (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  org_id          BIGINT       NOT NULL DEFAULT 0,
  mer_no          VARCHAR(32)  NOT NULL DEFAULT '',
  trans_code      VARCHAR(16)  NOT NULL COMMENT '交易码',
  src_req_id      VARCHAR(64)  NOT NULL,
  src_req_date    VARCHAR(8)   NOT NULL DEFAULT '',
  order_no        VARCHAR(64)  NOT NULL DEFAULT '',
  pay_amt         BIGINT       NOT NULL DEFAULT 0 COMMENT '金额分',
  status          VARCHAR(8)   NOT NULL DEFAULT '0' COMMENT '0待处理1处理中2成功3失败…',
  order_type      VARCHAR(8)   NOT NULL DEFAULT '',
  resp_code       VARCHAR(16)  NOT NULL DEFAULT '',
  resp_msg        VARCHAR(255) NOT NULL DEFAULT '',
  req_body        MEDIUMTEXT   NULL,
  resp_body       MEDIUMTEXT   NULL,
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_req (org_id, src_req_date, src_req_id),
  KEY idx_order (org_id, order_no),
  KEY idx_code (org_id, trans_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银联企业订单操作记录';

CREATE TABLE IF NOT EXISTS hou_ums_corp_notify (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  org_id          BIGINT       NOT NULL DEFAULT 0,
  notify_id       VARCHAR(64)  NOT NULL DEFAULT '',
  notify_type     VARCHAR(64)  NOT NULL DEFAULT '',
  mer_order_no    VARCHAR(64)  NOT NULL DEFAULT '',
  order_status    VARCHAR(16)  NOT NULL DEFAULT '',
  payload         MEDIUMTEXT   NULL,
  processed       TINYINT      NOT NULL DEFAULT 0,
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_notify (org_id, notify_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银联企业订单异步通知';
