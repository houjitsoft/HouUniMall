-- 35-mall-distribution-schema-align.sql
-- 分销域实体与真库表结构对齐（既有代码在真库上必然 Unknown column，属遗留缺陷）
--
-- 背景：backend/paas/mall 的 distribution 实体是按「两级分佣」模型定义的，真库表是更早的
-- 「first_rate/second_rate」口径，缺实体的多处列，导致 DistributorService / DistributorLevelService /
-- CommissionRuleService 的任何读写都报 Unknown column。这里补齐实体所需列，旧列保留不动（历史数据不受影响）。

-- 一、分销员：补资金台账与申请信息列
ALTER TABLE hou_mall_distributor
    ADD COLUMN distributor_no     VARCHAR(32) NULL COMMENT '分销员编号' AFTER id,
    ADD COLUMN pending_commission DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '待结算佣金' AFTER available_commission,
    ADD COLUMN withdrawn_commission DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '已提现佣金' AFTER pending_commission,
    ADD COLUMN order_count        INT NOT NULL DEFAULT 0 COMMENT '分销订单数' AFTER withdrawn_commission,
    ADD COLUMN sub_count          INT NOT NULL DEFAULT 0 COMMENT '下级分销员数' AFTER order_count,
    ADD COLUMN apply_remark       VARCHAR(255) NULL COMMENT '申请备注' AFTER audit_remark;

CREATE INDEX idx_hmd_distributor_no ON hou_mall_distributor (distributor_no);

-- 二、分销等级：补两级费率与升级条件列（与 first_rate/second_rate 并存）
ALTER TABLE hou_mall_distributor_level
    ADD COLUMN level1_rate          DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '一级分佣比例(%)' AFTER second_rate,
    ADD COLUMN level2_rate          DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '二级分佣比例(%)' AFTER level1_rate,
    ADD COLUMN upgrade_amount       DECIMAL(12,2) NULL COMMENT '升级所需累计金额' AFTER level2_rate,
    ADD COLUMN upgrade_order_count  INT NULL COMMENT '升级所需订单数' AFTER upgrade_amount,
    ADD COLUMN upgrade_sub_count    INT NULL COMMENT '升级所需下级人数' AFTER upgrade_order_count;

-- 三、佣金规则：补规则类型/关联对象/两级费率与固定金额列
ALTER TABLE hou_mall_commission_rule
    ADD COLUMN type           TINYINT NOT NULL DEFAULT 1 COMMENT '规则类型：1=全店 2=分类 3=商品' AFTER name,
    ADD COLUMN related_id     BIGINT  NULL COMMENT '关联ID（分类/商品ID）' AFTER type,
    ADD COLUMN level1_rate    DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '一级分佣比例(%)' AFTER related_id,
    ADD COLUMN level2_rate    DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '二级分佣比例(%)' AFTER level1_rate,
    ADD COLUMN level1_amount  DECIMAL(12,2) NULL COMMENT '一级固定佣金金额' AFTER level2_rate,
    ADD COLUMN level2_amount  DECIMAL(12,2) NULL COMMENT '二级固定佣金金额' AFTER level1_amount,
    ADD COLUMN calc_type      TINYINT NOT NULL DEFAULT 1 COMMENT '计算方式：1=比例 2=固定金额' AFTER level2_amount;

CREATE INDEX idx_hmcr_type_related ON hou_mall_commission_rule (type, related_id);
