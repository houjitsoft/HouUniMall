package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayRiskLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付风控拦截日志表 Mapper
 *
 * @author Amos
 */
@Mapper
public interface PayRiskLogMapper extends BaseMapper<PayRiskLogEntity> {
}
