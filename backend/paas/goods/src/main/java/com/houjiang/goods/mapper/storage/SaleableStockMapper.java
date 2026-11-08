package com.houjiang.goods.mapper.storage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.goods.entity.storage.SaleableStock;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 可售库存原子更新 + ERP 物理仓同步（从 mall StockMapper 迁入，SQL 保持一致）。
 */
@Mapper
public interface SaleableStockMapper extends BaseMapper<SaleableStock> {

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

    // =================================================================
    // ERP 物理仓同步（2026-10-16 新增）
    // 修复缺陷 D12：ERP 出库不通知商城 → 线上超卖
    // =================================================================

    /**
     * 物理入库：货进到可发仓，可售库存增加。
     * <p>
     * 与 {@link #returnToStock} 的区别：returnToStock 用于退货（货原先已扣过，
     * 这里只回补 available），而物理入库是货第一次进入本仓，
     * quantity 与 available_qty 都要增加。
     */
    @Update("UPDATE hou_mall_stock SET quantity = quantity + #{quantity}, " +
            "available_qty = available_qty + #{quantity}, " +
            "cost_price = CASE WHEN #{cost} IS NOT NULL AND #{cost} > 0 THEN #{cost} ELSE cost_price END, " +
            "update_time = NOW() " +
            "WHERE warehouse_id = #{warehouseId} AND sku_id = #{skuId} AND deleted = 0")
    int physicalIn(@Param("warehouseId") Long warehouseId, @Param("skuId") Long skuId,
                   @Param("quantity") Integer quantity, @Param("cost") java.math.BigDecimal cost);

    /** 普通 ERP 出库：物理量与可售量同步减少，不释放订单预占。 */
    @Update("UPDATE hou_mall_stock SET quantity = quantity - #{quantity}, " +
            "available_qty = available_qty - #{quantity}, update_time = NOW() " +
            "WHERE warehouse_id = #{warehouseId} AND sku_id = #{skuId} AND deleted = 0 " +
            "AND quantity >= #{quantity} AND available_qty >= #{quantity}")
    int physicalOut(@Param("warehouseId") Long warehouseId, @Param("skuId") Long skuId,
                    @Param("quantity") Integer quantity);

    /**
     * 调拨出库：从本仓扣减可售（货已运往目标仓，本仓不能再卖）。
     */
    @Update("UPDATE hou_mall_stock SET quantity = GREATEST(quantity - #{quantity}, 0), " +
            "available_qty = GREATEST(available_qty - #{quantity}, 0), update_time = NOW() " +
            "WHERE warehouse_id = #{warehouseId} AND sku_id = #{skuId} AND deleted = 0")
    int transferOut(@Param("warehouseId") Long warehouseId, @Param("skuId") Long skuId,
                    @Param("quantity") Integer quantity);

    /**
     * 记录 ERP 同步幂等日志。返回 0 表示已处理过（重复调用）。
     * <p>
     * 幂等靠唯一键 {@code uk_flow_no} + INSERT IGNORE 实现，
     * 否则网络重试会导致库存被重复加减。
     */
    @Insert("INSERT IGNORE INTO hou_mall_stock_erp_sync " +
            "(erp_flow_no, doc_no, doc_type, warehouse_id, sku_id, quantity, direction, cost, batch_no, " +
            " occur_time, create_time) " +
            "VALUES(#{erpFlowNo}, #{docNo}, #{docType}, #{warehouseId}, #{skuId}, #{quantity}, " +
            " #{direction}, #{cost}, #{batchNo}, NOW(), NOW())")
    int recordErpSync(@Param("erpFlowNo") String erpFlowNo, @Param("docNo") String docNo,
                      @Param("docType") String docType, @Param("warehouseId") Long warehouseId,
                      @Param("skuId") Long skuId, @Param("quantity") Integer quantity,
                      @Param("direction") Integer direction,
                      @Param("cost") java.math.BigDecimal cost, @Param("batchNo") String batchNo);
}
