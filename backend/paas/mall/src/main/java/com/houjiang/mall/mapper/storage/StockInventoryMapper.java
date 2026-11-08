package com.houjiang.mall.mapper.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.storage.StockInventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存盘点Mapper接口
 * 
 * @author Amos
 */
@Mapper
public interface StockInventoryMapper extends BaseMapper<StockInventory> {
}
