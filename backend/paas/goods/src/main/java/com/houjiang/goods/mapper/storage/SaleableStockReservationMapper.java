package com.houjiang.goods.mapper.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.storage.SaleableStockReservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 预占明细（从 mall StockReservationMapper 迁入；INSERT IGNORE 幂等保留）。
 */
@Mapper
public interface SaleableStockReservationMapper extends BaseMapper<SaleableStockReservation> {

    @Insert("INSERT IGNORE INTO hou_mall_stock_reservation " +
            "(order_no, warehouse_id, sku_id, quantity, status, create_time, update_time) " +
            "VALUES (#{r.orderNo}, #{r.warehouseId}, #{r.skuId}, #{r.quantity}, 'PENDING', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "r.id")
    int insertIfAbsent(@Param("r") SaleableStockReservation reservation);

    @Select("SELECT * FROM hou_mall_stock_reservation WHERE order_no = #{orderNo}")
    List<SaleableStockReservation> selectByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE hou_mall_stock_reservation SET status = #{toStatus}, update_time = NOW() " +
            "WHERE id = #{id} AND status = #{fromStatus}")
    int moveStatus(@Param("id") Long id, @Param("fromStatus") String fromStatus,
                   @Param("toStatus") String toStatus);
}
