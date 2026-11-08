package com.houjiang.mall.mapper.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.storage.StockInbound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单Mapper接口
 * 
 * @author Amos
 */
@Mapper
public interface StockInboundMapper extends BaseMapper<StockInbound> {
}
