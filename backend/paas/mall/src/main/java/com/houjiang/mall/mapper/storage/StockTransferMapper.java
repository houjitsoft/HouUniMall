package com.houjiang.mall.mapper.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.storage.StockTransfer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存调拨Mapper接口
 * 
 * @author Amos
 */
@Mapper
public interface StockTransferMapper extends BaseMapper<StockTransfer> {
}
