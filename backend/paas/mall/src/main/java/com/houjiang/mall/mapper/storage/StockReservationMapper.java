package com.houjiang.mall.mapper.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.storage.StockReservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Amos
 */
@Mapper
public interface StockReservationMapper extends BaseMapper<StockReservation> {

    @Insert("INSERT IGNORE INTO hou_mall_stock_reservation " +
            "(order_no, warehouse_id, sku_id, quantity, status, create_time, update_time) " +
            "VALUES (#{r.orderNo}, #{r.warehouseId}, #{r.skuId}, #{r.quantity}, 'PENDING', NOW(), NOW())")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Options(useGeneratedKeys = true, keyProperty = "r.id")
    int insertIfAbsent(@Param("r") StockReservation reservation);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_mall_stock_reservation WHERE order_no = #{orderNo}")
    List<StockReservation> selectByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE hou_mall_stock_reservation SET status = #{toStatus}, update_time = NOW() " +
            "WHERE id = #{id} AND status = #{fromStatus}")
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int moveStatus(@Param("id") Long id, @Param("fromStatus") String fromStatus,
                   @Param("toStatus") String toStatus);
}
