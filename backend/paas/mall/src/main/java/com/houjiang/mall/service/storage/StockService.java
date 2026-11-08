package com.houjiang.mall.service.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.mall.entity.storage.Stock;
import com.houjiang.mall.mapper.storage.StockMapper;
import com.houjiang.commons.dto.mall.MallStockReservationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 库存Service
 * 
 * @author Amos
 */
@Slf4j
@Service
public class StockService {

    @Autowired
    private StockMapper stockMapper;

    @Deprecated
    public void reserveForOrder(List<MallStockReservationRequest> requests) {
        throw new UnsupportedOperationException(
                "saleable stock lifecycle moved to goods; call /goods/storage/stock/lifecycle/*");
    }

    

    @Deprecated
    public void commitOrder(String orderNo) {
        throw new UnsupportedOperationException(
                "saleable stock lifecycle moved to goods; call /goods/storage/stock/lifecycle/*");
    }

    

    @Deprecated
    public void releaseOrder(String orderNo) {
        throw new UnsupportedOperationException(
                "saleable stock lifecycle moved to goods; call /goods/storage/stock/lifecycle/*");
    }

    

    @Deprecated
    public void returnOrderToStock(String orderNo) {
        throw new UnsupportedOperationException(
                "saleable stock lifecycle moved to goods; call /goods/storage/stock/lifecycle/*");
    }

    /**
     * 分页查询库存
     *
     * @param param 查询参数
     * @return 分页结果
     */
    public IPage<Stock> getStockList(Map<String, Object> param) {
        int page = param.get("page") != null ? Integer.parseInt(param.get("page").toString()) : 1;
        int size = param.get("size") != null ? Integer.parseInt(param.get("size").toString()) : 10;

        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        
        if (param.get("warehouseId") != null) {
            wrapper.eq(Stock::getWarehouseId, Long.parseLong(param.get("warehouseId").toString()));
        }
        if (param.get("goodsId") != null) {
            wrapper.eq(Stock::getGoodsId, Long.parseLong(param.get("goodsId").toString()));
        }
        if (param.get("skuId") != null) {
            wrapper.eq(Stock::getSkuId, Long.parseLong(param.get("skuId").toString()));
        }
        if (param.get("goodsName") != null) {
            wrapper.like(Stock::getGoodsName, param.get("goodsName").toString());
        }
        // 库存预警筛选
        if (param.get("warning") != null && Boolean.parseBoolean(param.get("warning").toString())) {
            wrapper.apply("available_qty <= warning_qty");
        }
        
        wrapper.orderByDesc(Stock::getUpdateTime);

        return stockMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 根据ID获取库存详情
     *
     * @param id 库存ID
     * @return 库存详情
     */
    public Stock getStockById(Long id) {
        return stockMapper.selectById(id);
    }

    /**
     * 根据仓库ID和SKU ID获取库存
     *
     * @param warehouseId 仓库ID
     * @param skuId SKU ID
     * @return 库存记录
     */
    public Stock getStockByWarehouseAndSku(Long warehouseId, Long skuId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getWarehouseId, warehouseId)
               .eq(Stock::getSkuId, skuId);
        return stockMapper.selectOne(wrapper);
    }

    /**
     * 获取仓库库存列表
     *
     * @param warehouseId 仓库ID
     * @return 库存列表
     */
    public List<Stock> getStocksByWarehouse(Long warehouseId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getWarehouseId, warehouseId)
               .orderByAsc(Stock::getGoodsName);
        return stockMapper.selectList(wrapper);
    }

    /**
     * 获取库存预警列表
     *
     * @param warehouseId 仓库ID（可选）
     * @return 预警库存列表
     */
    public List<Stock> getWarningStocks(Long warehouseId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(Stock::getWarehouseId, warehouseId);
        }
        wrapper.apply("available_qty <= warning_qty")
               .orderByAsc(Stock::getAvailableQty);
        return stockMapper.selectList(wrapper);
    }

    /**
     * 创建库存记录
     *
     * @param stock 库存信息
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createStock(Stock stock) {
        // 检查是否已存在
        Stock existing = getStockByWarehouseAndSku(stock.getWarehouseId(), stock.getSkuId());
        if (existing != null) {
            log.warn("库存已存在: warehouseId={}, skuId={}", stock.getWarehouseId(), stock.getSkuId());
            return false;
        }
        
        // 初始化默认值
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

    /**
     * 更新库存
     *
     * @param stock 库存信息
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStock(Stock stock) {
        return stockMapper.updateById(stock) > 0;
    }

    /**
     * 增加库存
     *
     * @param warehouseId 仓库ID
     * @param skuId SKU ID
     * @param quantity 数量
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long warehouseId, Long skuId, Integer quantity) {
        Stock stock = getStockByWarehouseAndSku(warehouseId, skuId);
        if (stock == null) {
            log.error("库存不存在: warehouseId={}, skuId={}", warehouseId, skuId);
            return false;
        }
        
        stock.setQuantity(stock.getQuantity() + quantity);
        stock.setAvailableQty(stock.getAvailableQty() + quantity);
        
        return stockMapper.updateById(stock) > 0;
    }

    /**
     * 减少库存
     *
     * @param warehouseId 仓库ID
     * @param skuId SKU ID
     * @param quantity 数量
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long warehouseId, Long skuId, Integer quantity) {
        Stock stock = getStockByWarehouseAndSku(warehouseId, skuId);
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

    /**
     * 锁定库存
     *
     * @param warehouseId 仓库ID
     * @param skuId SKU ID
     * @param quantity 数量
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean lockStock(Long warehouseId, Long skuId, Integer quantity) {
        Stock stock = getStockByWarehouseAndSku(warehouseId, skuId);
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

    /**
     * 释放锁定库存
     *
     * @param warehouseId 仓库ID
     * @param skuId SKU ID
     * @param quantity 数量
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean unlockStock(Long warehouseId, Long skuId, Integer quantity) {
        Stock stock = getStockByWarehouseAndSku(warehouseId, skuId);
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

    /**
     * 删除库存
     *
     * @param id 库存ID
     * @return 删除结果
     */
    public boolean deleteStock(Long id) {
        return stockMapper.deleteById(id) > 0;
    }
}
