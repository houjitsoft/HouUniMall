package com.houjiang.goods.service.storage;

import com.houjiang.commons.dto.mall.MallStockReservationRequest;
import com.houjiang.goods.entity.storage.SaleableStock;
import com.houjiang.goods.entity.storage.SaleableStockReservation;
import com.houjiang.goods.mapper.storage.SaleableStockMapper;
import com.houjiang.goods.mapper.storage.SaleableStockReservationMapper;
import com.houjiang.goods.vo.stock.ErpStockSyncRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ERP 物理仓同步接收端（超卖修复）
 *
 * <h3>职责边界</h3>
 * goods 只负责「可售量」—— 线上能卖多少。
 * ERP 管「物理量」—— 仓里还剩多少。ERP 出库过账后调本服务，
 * 把已出库的部分从可售口径中移除，避免商城继续卖已经发走的货。
 *
 * <h3>三条铁律</h3>
 * <ol>
 *   <li><b>单向</b>：goods 不知道 ERP 存在，绝不反向调回，避免服务环路</li>
 *   <li><b>幂等</b>：按 {@code erp_flow_no} 落 {@code hou_mall_stock_erp_sync}，
 *       唯一键 + INSERT IGNORE，重复调用直接跳过</li>
 *   <li><b>可容忍缺失</b>：ERP 仓未在本服务建库存行时先补一行，
 *       否则一次同步失败会导致该 SKU 之后全部同步失败</li>
 * </ol>
 *
 * @author Amos
 * @since 2026-10-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErpPhysicalStockService {

    private final SaleableStockMapper stockMapper;
    private final SaleableStockReservationMapper reservationMapper;
    private final JdbcTemplate jdbcTemplate;

    /** 普通 ERP 出库：只同步明确物理出库，不触碰订单锁定量。 */
    @Transactional(rollbackFor = Exception.class)
    public ErpStockSyncRequest.Result physicalOut(ErpStockSyncRequest request) {
        return apply(request, "physicalOut", 1);
    }

    /**
     * 物理入库：ERP 入库过账后调用。增加可售。
     */
    @Transactional(rollbackFor = Exception.class)
    public ErpStockSyncRequest.Result physicalIn(ErpStockSyncRequest request) {
        return apply(request, "physicalIn", -1);
    }

    /**
     * 调拨出库：转出仓扣减可售。
     */
    @Transactional(rollbackFor = Exception.class)
    public ErpStockSyncRequest.Result transferOut(ErpStockSyncRequest request) {
        return apply(request, "transferOut", -1);
    }

    /**
     * 统一处理。
     *
     * @param action      动作名（物理出/入库、转发 API）
     * @param signDirection 记录到幂等表的 direction：1=出，-1=入
     */
    private ErpStockSyncRequest.Result apply(ErpStockSyncRequest request, String action, int signDirection) {
        ErpStockSyncRequest.Result result = new ErpStockSyncRequest.Result();
        result.setSuccess(false);

        if (request == null || isBlank(request.getErpFlowNo())) {
            result.setMessage("幂等键 erpFlowNo 不能为空");
            return result;
        }
        if (request.getWarehouseId() == null || request.getWarehouseId() <= 0) {
            result.setMessage("仓库映射缺失（ERP 仓库未配置商城仓库）");
            return result;
        }
        List<ErpStockSyncRequest.Item> items = request.getItems();
        if (items == null || items.isEmpty()) {
            result.setSuccess(true);
            result.setMessage("无明细，视为已处理");
            return result;
        }

        int affected = 0;
        int skipped = 0;

        for (ErpStockSyncRequest.Item item : items) {
            if (item.getSkuId() == null || item.getQty() == null || item.getQty() <= 0) {
                continue;
            }

            // 幂等：先落幂等日志，命中唯一键说明这笔已处理
            int inserted;
            try {
                inserted = stockMapper.recordErpSync(
                        request.getErpFlowNo() + "#" + item.getSkuId(),
                        request.getDocNo(), request.getDocType(),
                        request.getWarehouseId(), item.getSkuId(), item.getQty(),
                        signDirection, item.getCost(), item.getBatchNo());
            } catch (DuplicateKeyException dup) {
                skipped++;
                continue;
            }
            if (inserted == 0) {
                skipped++;
                continue;
            }

            // 库存行不存在时先补一行（否则本次同步失败会导致该 SKU 后续全部失败）
            ensureStockRow(request.getWarehouseId(), item);

            int rows = switch (action) {
                case "physicalOut" -> stockMapper.physicalOut(
                        request.getWarehouseId(), item.getSkuId(), item.getQty());
                case "physicalIn" -> stockMapper.physicalIn(
                        request.getWarehouseId(), item.getSkuId(), item.getQty(), item.getCost());
                case "transferOut" -> stockMapper.transferOut(
                        request.getWarehouseId(), item.getSkuId(), item.getQty());
                default -> 0;
            };
            affected += rows;
            if (rows == 0) {
                throw new IllegalStateException("商城物理库存不足或库存行不可更新，拒绝假成功 skuId=" + item.getSkuId());
            }
        }

        result.setSuccess(true);
        result.setAffectedRows(affected);
        result.setMessage("同步完成：影响 " + affected + " 行，幂等跳过 " + skipped + " 行");
        log.info("ERP 物理库存同步 action={} docNo={} flowNo={} affected={} skipped={}",
                action, request.getDocNo(), request.getErpFlowNo(), affected, skipped);
        return result;
    }

    /**
     * 库存行不存在时补一行（ERP 侧已有货但商城没建库存行的情况）。
     */
    private void ensureStockRow(Long warehouseId, ErpStockSyncRequest.Item item) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM hou_mall_stock WHERE warehouse_id=? AND sku_id=? AND deleted=0",
                Integer.class, warehouseId, item.getSkuId());
        if (exists != null && exists > 0) {
            return;
        }

        String goodsName = "";
        String skuSpec = "";
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT goods_name, sku_spec FROM hou_mall_sku WHERE id=? LIMIT 1", item.getSkuId());
            goodsName = value(row.get("goods_name"));
            skuSpec = value(row.get("sku_spec"));
        } catch (Exception e) {
            // SKU 表名可能不同，取不到就留空，不阻断同步
            log.debug("读取 SKU 信息失败 skuId={}: {}", item.getSkuId(), e.getMessage());
        }

        jdbcTemplate.update(
                "INSERT IGNORE INTO hou_mall_stock " +
                "(warehouse_id, goods_id, sku_id, goods_name, sku_spec, quantity, available_qty, " +
                " locked_qty, warning_qty, cost_price, create_time, update_time, deleted) " +
                "VALUES(?,?,?,?,?,0,0,0,10,?,NOW(),NOW(),0)",
                warehouseId, 0L, item.getSkuId(), goodsName, skuSpec,
                item.getCost() == null ? BigDecimal.ZERO : item.getCost());
        log.info("ERP 同步补建商城库存行 warehouseId={} skuId={}", warehouseId, item.getSkuId());
    }

    private String value(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}