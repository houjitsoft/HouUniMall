package com.houjiang.mall.service.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.storage.StockInventory;
import com.houjiang.mall.mapper.storage.StockInventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 库存盘点服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class StockInventoryService extends ServiceImpl<StockInventoryMapper, StockInventory> {

    /**
     * 分页查询盘点单
     */
    public IPage<StockInventory> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String inventoryNo = (String) params.get("inventoryNo");
        Integer inventoryType = params.get("inventoryType") != null ? Integer.parseInt(params.get("inventoryType").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Long warehouseId = params.get("warehouseId") != null ? Long.parseLong(params.get("warehouseId").toString()) : null;

        LambdaQueryWrapper<StockInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(inventoryNo), StockInventory::getInventoryNo, inventoryNo)
               .eq(inventoryType != null, StockInventory::getInventoryType, inventoryType)
               .eq(status != null, StockInventory::getStatus, status)
               .eq(shopId != null, StockInventory::getShopId, shopId)
               .eq(warehouseId != null, StockInventory::getWarehouseId, warehouseId)
               .orderByDesc(StockInventory::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 创建盘点单
     */
    public Boolean createInventory(StockInventory inventory) {
        inventory.setInventoryNo(generateInventoryNo());
        // 待盘点
        inventory.setStatus(0);
        inventory.setCreateTime(LocalDateTime.now());
        inventory.setUpdateTime(LocalDateTime.now());
        return save(inventory);
    }

    /**
     * 开始盘点
     */
    public Boolean start(Long id) {
        StockInventory inventory = new StockInventory();
        inventory.setId(id);
        // 盘点中
        inventory.setStatus(1);
        inventory.setStartTime(LocalDateTime.now());
        inventory.setUpdateTime(LocalDateTime.now());
        return updateById(inventory);
    }

    /**
     * 提交盘点结果
     */
    public Boolean submit(Long id, Integer profitQuantity, Integer lossQuantity) {
        StockInventory inventory = new StockInventory();
        inventory.setId(id);
        // 待审核
        inventory.setStatus(2);
        inventory.setProfitQuantity(profitQuantity);
        inventory.setLossQuantity(lossQuantity);
        inventory.setEndTime(LocalDateTime.now());
        inventory.setUpdateTime(LocalDateTime.now());
        return updateById(inventory);
    }

    /**
     * 审核盘点
     */
    public Boolean audit(Long id, Integer status, Long auditorId, String auditorName, String auditRemark) {
        StockInventory inventory = new StockInventory();
        inventory.setId(id);
        inventory.setStatus(status);
        inventory.setAuditorId(auditorId);
        inventory.setAuditorName(auditorName);
        inventory.setAuditRemark(auditRemark);
        inventory.setAuditTime(LocalDateTime.now());
        inventory.setUpdateTime(LocalDateTime.now());
        return updateById(inventory);
    }

    private String generateInventoryNo() {
        return "IV" + System.currentTimeMillis();
    }
}
