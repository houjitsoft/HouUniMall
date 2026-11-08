package com.houjiang.goods.service.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.mall.MallStockReservationRequest;
import com.houjiang.goods.entity.storage.SellableStock;
import com.houjiang.goods.entity.storage.SellableStockReservation;
import com.houjiang.goods.mapper.storage.SellableStockMapper;
import com.houjiang.goods.mapper.storage.SellableStockReservationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 可售库存唯一写入面（含订单预占生命周期）。仓库作业单据仍属 ERP。
 */
@Slf4j
@Service
public class SellableStockService {

    @Autowired
    private SellableStockMapper stockMapper;
    @Autowired
    private SellableStockReservationMapper reservationMapper;

    @Transactional(rollbackFor = Exception.class)
    public void reserveForOrder(List<MallStockReservationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("库存预占明细不能为空");
        }
        for (MallStockReservationRequest request : requests) {
            validateReservation(request);
            SellableStockReservation reservation = new SellableStockReservation();
            reservation.setOrderNo(request.getOrderNo());
            reservation.setWarehouseId(request.getWarehouseId());
            reservation.setSkuId(request.getSkuId());
            reservation.setQuantity(request.getQuantity());
            if (reservationMapper.insertIfAbsent(reservation) == 0) {
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
        for (SellableStockReservation reservation : reservationMapper.selectByOrderNo(requireOrderNo(orderNo))) {
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
        for (SellableStockReservation reservation : reservationMapper.selectByOrderNo(requireOrderNo(orderNo))) {
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
        for (SellableStockReservation reservation : reservationMapper.selectByOrderNo(requireOrderNo(orderNo))) {
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

    public IPage<SellableStock> getStockList(Map<String, Object> param) {
        int page = param.get("page") != null ? Integer.parseInt(param.get("page").toString()) : 1;
        int size = param.get("size") != null ? Integer.parseInt(param.get("size").toString()) : 10;

        LambdaQueryWrapper<SellableStock> wrapper = new LambdaQueryWrapper<>();
        if (param.get("warehouseId") != null) {
            wrapper.eq(SellableStock::getWarehouseId, Long.parseLong(param.get("warehouseId").toString()));
        }
        if (param.get("goodsId") != null) {
            wrapper.eq(SellableStock::getGoodsId, Long.parseLong(param.get("goodsId").toString()));
        }
        if (param.get("skuId") != null) {
            wrapper.eq(SellableStock::getSkuId, Long.parseLong(param.get("skuId").toString()));
        }
        if (param.get("goodsName") != null) {
            wrapper.like(SellableStock::getGoodsName, param.get("goodsName").toString());
        }
        if (param.get("warning") != null && Boolean.parseBoolean(param.get("warning").toString())) {
            wrapper.apply("available_qty <= warning_qty");
        }
        wrapper.orderByDesc(SellableStock::getUpdateTime);
        return stockMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public SellableStock getStockById(Long id) {
        return stockMapper.selectById(id);
    }

    public SellableStock getStockByWarehouseAndSku(Long warehouseId, Long skuId) {
        return stockMapper.selectOne(new LambdaQueryWrapper<SellableStock>()
                .eq(SellableStock::getWarehouseId, warehouseId)
                .eq(SellableStock::getSkuId, skuId));
    }

    public List<SellableStock> getStocksByWarehouse(Long warehouseId) {
        return stockMapper.selectList(new LambdaQueryWrapper<SellableStock>()
                .eq(SellableStock::getWarehouseId, warehouseId)
                .orderByAsc(SellableStock::getGoodsName));
    }

    public List<SellableStock> getWarningStocks(Long warehouseId) {
        LambdaQueryWrapper<SellableStock> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(SellableStock::getWarehouseId, warehouseId);
        }
        wrapper.apply("available_qty <= warning_qty")
                .orderByAsc(SellableStock::getAvailableQty);
        return stockMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean createStock(SellableStock stock) {
        SellableStock existing = getStockByWarehouseAndSku(stock.getWarehouseId(), stock.getSkuId());
        if (existing != null) {
            log.warn("库存已存在: warehouseId={}, skuId={}", stock.getWarehouseId(), stock.getSkuId());
            return false;
        }
        if (stock.getQuantity() == null) {
            stock.setQuantity(0);
        }
        if (stock.getAvailableQty() == null) {
            stock.setAvailableQty(stock.getQuantity());
        }
        if (stock.getLockedQty() == null) {
            stock.setLockedQty(0);
        }
        if (stock.getWarningQty() == null) {
            stock.setWarningQty(10);
        }
        return stockMapper.insert(stock) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateStock(SellableStock stock) {
        return stockMapper.updateById(stock) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long warehouseId, Long skuId, Integer quantity) {
        SellableStock stock = getStockByWarehouseAndSku(warehouseId, skuId);
        if (stock == null) {
            log.error("库存不存在: warehouseId={}, skuId={}", warehouseId, skuId);
            return false;
        }
        stock.setQuantity(stock.getQuantity() + quantity);
        stock.setAvailableQty(stock.getAvailableQty() + quantity);
        return stockMapper.updateById(stock) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long warehouseId, Long skuId, Integer quantity) {
        SellableStock stock = getStockByWarehouseAndSku(warehouseId, skuId);
        if (stock == null) {
            log.error("库存不存在: warehouseId={}, skuId={}", warehouseId, skuId);
            return false;
        }
        if (stock.getAvailableQty() < quantity) {
            log.error("库存不足: available={}, required={}", stock.getAvailableQty(), quantity);
            return false;
        }
        stock.setQuantity(stock.getQuantity() - quantity);
        stock.setAvailableQty(stock.getAvailableQty() - quantity);
        return stockMapper.updateById(stock) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean lockStock(Long warehouseId, Long skuId, Integer quantity) {
        SellableStock stock = getStockByWarehouseAndSku(warehouseId, skuId);
        if (stock == null) {
            log.error("库存不存在: warehouseId={}, skuId={}", warehouseId, skuId);
            return false;
        }
        if (stock.getAvailableQty() < quantity) {
            log.error("可用库存不足: available={}, required={}", stock.getAvailableQty(), quantity);
            return false;
        }
        stock.setAvailableQty(stock.getAvailableQty() - quantity);
        stock.setLockedQty(stock.getLockedQty() + quantity);
        return stockMapper.updateById(stock) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean unlockStock(Long warehouseId, Long skuId, Integer quantity) {
        SellableStock stock = getStockByWarehouseAndSku(warehouseId, skuId);
        if (stock == null) {
            log.error("库存不存在: warehouseId={}, skuId={}", warehouseId, skuId);
            return false;
        }
        if (stock.getLockedQty() < quantity) {
            log.error("锁定库存不足: locked={}, required={}", stock.getLockedQty(), quantity);
            return false;
        }
        stock.setAvailableQty(stock.getAvailableQty() + quantity);
        stock.setLockedQty(stock.getLockedQty() - quantity);
        return stockMapper.updateById(stock) > 0;
    }

    public boolean deleteStock(Long id) {
        return stockMapper.deleteById(id) > 0;
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
