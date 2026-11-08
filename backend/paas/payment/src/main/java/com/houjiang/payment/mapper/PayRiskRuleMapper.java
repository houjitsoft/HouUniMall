package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayRiskRuleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付风控规则表 Mapper
 *
 * @author Amos
 */
@Mapper
public interface PayRiskRuleMapper extends BaseMapper<PayRiskRuleEntity> {
}
