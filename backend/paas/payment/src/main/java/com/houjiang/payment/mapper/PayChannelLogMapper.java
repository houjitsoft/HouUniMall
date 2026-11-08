package com.houjiang.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.payment.entity.PayChannelLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 渠道调用日志 Mapper
 * @author Amos
 */
@Mapper
public interface PayChannelLogMapper extends BaseMapper<PayChannelLogEntity> {
}
