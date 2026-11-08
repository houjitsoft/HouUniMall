-- 会员开户/绑卡落库与回调幂等（补齐 MemberServiceImpl 里被注释掉的持久化步骤）
-- 执行库：payment 服务所用库

CREATE TABLE IF NOT EXISTS `pay_member` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_user_id`       VARCHAR(64)  NOT NULL                COMMENT '渠道会员编号（渠道侧唯一）',
  `member_type`       VARCHAR(20)  NOT NULL                COMMENT '会员类型：ENTERPRISE 企业 / PERSON 个人',
  `mem_category`      VARCHAR(32)  DEFAULT NULL            COMMENT '会员类别（渠道侧分类）',
  `chan_no`           VARCHAR(32)  DEFAULT NULL            COMMENT '渠道号',
  `status`            TINYINT      NOT NULL DEFAULT 0      COMMENT '开户状态：0 处理中 1 成功 2 失败',
  `audit_result`      VARCHAR(8)   DEFAULT NULL            COMMENT '渠道审核结果：2 成功 3 失败',
  `phone`             VARCHAR(32)  DEFAULT NULL            COMMENT '绑定手机号',
  `bind_phone_status` TINYINT      NOT NULL DEFAULT 0      COMMENT '绑手机状态：0 未绑定 1 申请中 2 已绑定 3 失败',
  `ext`               VARCHAR(1000) DEFAULT NULL           COMMENT '补充资料 JSON（征信文件、OCR 等）',
  `remark`            VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_member_biz_user` (`biz_user_id`),
  KEY `idx_pay_member_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付会员（企业/个人）开户记录';

CREATE TABLE IF NOT EXISTS `pay_member_notify` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_user_id`  VARCHAR(64) NOT NULL                COMMENT '渠道会员编号',
  `notify_type`  VARCHAR(32) NOT NULL                COMMENT '通知类型：REG_ENTERPRISE / REG_SUPPLEMENT / BIND_PHONE / MEM_PROTOCOL',
  `notify_key`   VARCHAR(64) NOT NULL                COMMENT '幂等键：会员号+类型+渠道侧时间/流水，重复回调靠它拦掉',
  `audit_result` VARCHAR(8)  DEFAULT NULL            COMMENT '审核结果',
  `payload`      VARCHAR(2000) DEFAULT NULL          COMMENT '原始通知内容（排障用）',
  `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_member_notify` (`biz_user_id`, `notify_type`, `notify_key`),
  KEY `idx_pay_member_notify_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员回调通知流水（幂等去重 + 留痕）';
