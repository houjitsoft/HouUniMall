package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayCallbackLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付回调流水 Mapper
 *
 * @author Amos
 */
@Mapper
public interface PayCallbackLogMapper extends BaseMapper<PayCallbackLogEntity> {
}
