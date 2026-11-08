package com.houjiang.mall.service.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.storage.Warehouse;
import com.houjiang.mall.mapper.storage.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 仓库服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class WarehouseService extends ServiceImpl<WarehouseMapper, Warehouse> {

    /**
     * 分页查询仓库
     */
    public IPage<Warehouse> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String warehouseName = (String) params.get("warehouseName");
        Integer warehouseType = params.get("warehouseType") != null ? Integer.parseInt(params.get("warehouseType").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;

        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(warehouseName), Warehouse::getWarehouseName, warehouseName)
               .eq(warehouseType != null, Warehouse::getWarehouseType, warehouseType)
               .eq(status != null, Warehouse::getStatus, status)
               .eq(shopId != null, Warehouse::getShopId, shopId)
               .orderByDesc(Warehouse::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 获取所有启用的仓库
     */
    public List<Warehouse> listEnabled(Long shopId) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getStatus, 1)
               .eq(shopId != null, Warehouse::getShopId, shopId)
               .orderByDesc(Warehouse::getIsDefault);
        return list(wrapper);
    }

    /**
     * 创建仓库
     */
    public Boolean createWarehouse(Warehouse warehouse) {
        warehouse.setWarehouseCode(generateWarehouseCode());
        warehouse.setCreateTime(LocalDateTime.now());
        warehouse.setUpdateTime(LocalDateTime.now());
        return save(warehouse);
    }

    /**
     * 更新仓库
     */
    public Boolean updateWarehouse(Warehouse warehouse) {
        warehouse.setUpdateTime(LocalDateTime.now());
        return updateById(warehouse);
    }

    /**
     * 设置默认仓库
     */
    public Boolean setDefault(Long id, Long shopId) {
        // 先取消其他默认
        update().set("is_default", 0).eq("shop_id", shopId).update();
        // 设置当前为默认
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        warehouse.setIsDefault(1);
        warehouse.setUpdateTime(LocalDateTime.now());
        return updateById(warehouse);
    }

    private String generateWarehouseCode() {
        return "WH" + System.currentTimeMillis();
    }
}
