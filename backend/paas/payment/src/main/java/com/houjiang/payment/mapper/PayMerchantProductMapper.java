package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayMerchantProductEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户产品开通记录 Mapper
 *
 * @author Amos
 */
@Mapper
public interface PayMerchantProductMapper extends BaseMapper<PayMerchantProductEntity> {
}
