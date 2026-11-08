package com.houjiang.mall.mapper.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.storage.StockOutbound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单Mapper接口
 * 
 * @author Amos
 */
@Mapper
public interface StockOutboundMapper extends BaseMapper<StockOutbound> {
}
