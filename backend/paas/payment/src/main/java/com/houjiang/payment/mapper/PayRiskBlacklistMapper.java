package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayRiskBlacklistEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付风控黑白名单表 Mapper
 *
 * @author Amos
 */
@Mapper
public interface PayRiskBlacklistMapper extends BaseMapper<PayRiskBlacklistEntity> {
}
