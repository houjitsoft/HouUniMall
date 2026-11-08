package com.houjiang.merchant.common.mapper.distribution;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.distribution.DistributionOrder;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.ToolUtils;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分销订单Mapper
 * @author Amos
 */
@Mapper
public interface DistributionOrderMapper extends IBaseMapper<DistributionOrder> {
}
