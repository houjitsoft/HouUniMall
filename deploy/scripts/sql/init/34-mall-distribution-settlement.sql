-- 34-mall-distribution-settlement.sql
-- 分销佣金结算闭环：佣金记录字段对齐 + 分销提现单
--
-- 背景：
--   1) hou_mall_commission_record 缺实体已定义的 4 列（record_no/goods_name/level/source_distributor_id），
--      直接插入会 Unknown column；level/source_distributor_id 是两级分佣留痕的必需字段。
--   2) 提现此前没有任何承载表（hou_mall_finance_withdraw 是会员提现，字段语义不同，不复用）。

-- 一、佣金记录字段对齐
ALTER TABLE hou_mall_commission_record
    ADD COLUMN record_no VARCHAR(32) NULL COMMENT '佣金流水号' AFTER id,
    ADD COLUMN goods_name VARCHAR(255) NULL COMMENT '商品名称快照' AFTER order_no,
    ADD COLUMN level TINYINT NOT NULL DEFAULT 1 COMMENT '分佣层级：1=一级 2=二级（合规上限两级）' AFTER commission_type,
    ADD COLUMN source_distributor_id BIGINT NULL COMMENT '来源分销员ID（二级分佣的上级）' AFTER level;

CREATE INDEX idx_hmcr_record_no ON hou_mall_commission_record (record_no);
CREATE INDEX idx_hmcr_level ON hou_mall_commission_record (level);

-- 二、分销提现单
CREATE TABLE IF NOT EXISTS hou_mall_distribution_withdraw (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    withdraw_no       VARCHAR(32)  NOT NULL COMMENT '提现单号',
    shop_id           BIGINT       NOT NULL DEFAULT 0 COMMENT '店铺ID（0=平台）',
    distributor_id    BIGINT       NOT NULL COMMENT '分销员ID',
    user_id           BIGINT       NOT NULL DEFAULT 0 COMMENT '关联用户ID',
    amount            DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '提现金额',
    fee               DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '手续费',
    actual_amount     DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际到账金额',
    pay_type          TINYINT      NOT NULL DEFAULT 1 COMMENT '到账方式：1=银行卡 2=微信 3=支付宝 4=余额',
    account_name      VARCHAR(50)  NULL COMMENT '收款人姓名',
    account_no        VARCHAR(64)  NULL COMMENT '收款账号',
    bank_name         VARCHAR(100) NULL COMMENT '开户行',
    status            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=待审核 1=审核通过待打款 2=已驳回 3=已打款',
    audit_time        DATETIME     NULL COMMENT '审核时间',
    auditor           VARCHAR(50)  NULL COMMENT '审核人',
    audit_remark      VARCHAR(255) NULL COMMENT '审核备注',
    remark            VARCHAR(255) NULL COMMENT '申请人备注',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_hmdw_no (withdraw_no),
    KEY idx_hmdw_distributor (distributor_id),
    KEY idx_hmdw_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销佣金提现单';
