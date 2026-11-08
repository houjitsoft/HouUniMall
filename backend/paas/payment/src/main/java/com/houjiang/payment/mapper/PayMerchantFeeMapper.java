package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayMerchantFeeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户费率配置 Mapper
 *
 * @author Amos
 */
@Mapper
public interface PayMerchantFeeMapper extends BaseMapper<PayMerchantFeeEntity> {
}
