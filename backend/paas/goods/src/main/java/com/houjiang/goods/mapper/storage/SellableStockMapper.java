package com.houjiang.goods.mapper.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.storage.SellableStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SellableStockMapper extends BaseMapper<SellableStock> {

    @Update("UPDATE hou_mall_stock SET available_qty = available_qty - #{quantity}, " +
            "locked_qty = locked_qty + #{quantity}, update_time = NOW() " +
            "WHERE warehouse_id = #{warehouseId} AND sku_id = #{skuId} AND deleted = 0 " +
            "AND available_qty >= #{quantity}")
    int reserve(@Param("warehouseId") Long warehouseId, @Param("skuId") Long skuId,
                @Param("quantity") Integer quantity);

    @Update("UPDATE hou_mall_stock SET quantity = quantity - #{quantity}, " +
            "locked_qty = locked_qty - #{quantity}, update_time = NOW() " +
            "WHERE warehouse_id = #{warehouseId} AND sku_id = #{skuId} AND deleted = 0 " +
            "AND locked_qty >= #{quantity} AND quantity >= #{quantity}")
    int commit(@Param("warehouseId") Long warehouseId, @Param("skuId") Long skuId,
               @Param("quantity") Integer quantity);

    @Update("UPDATE hou_mall_stock SET available_qty = available_qty + #{quantity}, " +
            "locked_qty = locked_qty - #{quantity}, update_time = NOW() " +
            "WHERE warehouse_id = #{warehouseId} AND sku_id = #{skuId} AND deleted = 0 " +
            "AND locked_qty >= #{quantity}")
    int release(@Param("warehouseId") Long warehouseId, @Param("skuId") Long skuId,
                @Param("quantity") Integer quantity);

    @Update("UPDATE hou_mall_stock SET quantity = quantity + #{quantity}, " +
            "available_qty = available_qty + #{quantity}, update_time = NOW() " +
            "WHERE warehouse_id = #{warehouseId} AND sku_id = #{skuId} AND deleted = 0")
    int returnToStock(@Param("warehouseId") Long warehouseId, @Param("skuId") Long skuId,
                      @Param("quantity") Integer quantity);
}
