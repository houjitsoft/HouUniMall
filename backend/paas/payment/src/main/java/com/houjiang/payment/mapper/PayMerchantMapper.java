package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayMerchantEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付商户主表 Mapper
 *
 * @author Amos
 */
@Mapper
public interface PayMerchantMapper extends BaseMapper<PayMerchantEntity> {
}
