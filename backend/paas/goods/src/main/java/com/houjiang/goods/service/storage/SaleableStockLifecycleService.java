package com.houjiang.goods.service.storage;

import com.houjiang.commons.dto.mall.MallStockReservationRequest;
import com.houjiang.goods.entity.storage.SaleableStockReservation;
import com.houjiang.goods.mapper.storage.SaleableStockMapper;
import com.houjiang.goods.mapper.storage.SaleableStockReservationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 可售库存订单生命周期唯一写方（从 mall StockService 迁入）。
 * reserve → commit / release；commit → return（退货回补）。
 * 幂等：uk(order_no,warehouse_id,sku_id) + INSERT IGNORE；状态机 CAS moveStatus。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleableStockLifecycleService {

    private final SaleableStockMapper stockMapper;
    private final SaleableStockReservationMapper reservationMapper;

    @Transactional(rollbackFor = Exception.class)
    public void reserveForOrder(List<MallStockReservationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("库存预占明细不能为空");
        }
        for (MallStockReservationRequest request : requests) {
            validateReservation(request);
            SaleableStockReservation reservation = new SaleableStockReservation();
            reservation.setOrderNo(request.getOrderNo());
            reservation.setWarehouseId(request.getWarehouseId());
            reservation.setSkuId(request.getSkuId());
            reservation.setQuantity(request.getQuantity());
            if (reservationMapper.insertIfAbsent(reservation) == 0) {
                // already present for this (order, warehouse, sku) — idempotent skip
                continue;
            }
            if (stockMapper.reserve(request.getWarehouseId(), request.getSkuId(), request.getQuantity()) != 1) {
                throw new IllegalStateException("SKU库存不足: " + request.getSkuId());
            }
            if (reservationMapper.moveStatus(reservation.getId(), "PENDING", "RESERVED") != 1) {
                throw new IllegalStateException("库存预占状态更新失败");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void commitOrder(String orderNo) {
        for (SaleableStockReservation reservation : reservationMapper.selectByOrderNo(requireOrderNo(orderNo))) {
            if (!"RESERVED".equals(reservation.getStatus())) {
                continue;
            }
            if (stockMapper.commit(reservation.getWarehouseId(), reservation.getSkuId(),
                    reservation.getQuantity()) != 1) {
                throw new IllegalStateException("库存实扣失败: " + reservation.getSkuId());
            }
            if (reservationMapper.moveStatus(reservation.getId(), "RESERVED", "COMMITTED") != 1) {
                throw new IllegalStateException("库存实扣状态更新失败");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseOrder(String orderNo) {
        for (SaleableStockReservation reservation : reservationMapper.selectByOrderNo(requireOrderNo(orderNo))) {
            if (!"RESERVED".equals(reservation.getStatus())) {
                continue;
            }
            if (stockMapper.release(reservation.getWarehouseId(), reservation.getSkuId(),
                    reservation.getQuantity()) != 1) {
                throw new IllegalStateException("库存释放失败: " + reservation.getSkuId());
            }
            if (reservationMapper.moveStatus(reservation.getId(), "RESERVED", "RELEASED") != 1) {
                throw new IllegalStateException("库存释放状态更新失败");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnOrderToStock(String orderNo) {
        for (SaleableStockReservation reservation : reservationMapper.selectByOrderNo(requireOrderNo(orderNo))) {
            if (!"COMMITTED".equals(reservation.getStatus())) {
                continue;
            }
            if (stockMapper.returnToStock(reservation.getWarehouseId(), reservation.getSkuId(),
                    reservation.getQuantity()) != 1) {
                throw new IllegalStateException("退货库存回补失败: " + reservation.getSkuId());
            }
            if (reservationMapper.moveStatus(reservation.getId(), "COMMITTED", "RETURNED") != 1) {
                throw new IllegalStateException("退货库存状态更新失败");
            }
        }
    }

    private void validateReservation(MallStockReservationRequest request) {
        if (request == null || request.getOrderNo() == null || request.getOrderNo().isBlank()
                || request.getWarehouseId() == null || request.getSkuId() == null
                || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("库存预占参数不完整");
        }
    }

    private String requireOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        return orderNo;
    }
}
