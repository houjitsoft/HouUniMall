package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayChannelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付渠道配置 Mapper
 * @author Amos
 */
@Mapper
public interface PayChannelMapper extends BaseMapper<PayChannelEntity> {
}
