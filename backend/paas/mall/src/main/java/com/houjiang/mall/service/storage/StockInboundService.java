package com.houjiang.mall.service.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.storage.StockInbound;
import com.houjiang.mall.mapper.storage.StockInboundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 入库单服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class StockInboundService extends ServiceImpl<StockInboundMapper, StockInbound> {

    /**
     * 分页查询入库单
     */
    public IPage<StockInbound> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String inboundNo = (String) params.get("inboundNo");
        Integer inboundType = params.get("inboundType") != null ? Integer.parseInt(params.get("inboundType").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Long warehouseId = params.get("warehouseId") != null ? Long.parseLong(params.get("warehouseId").toString()) : null;

        LambdaQueryWrapper<StockInbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(inboundNo), StockInbound::getInboundNo, inboundNo)
               .eq(inboundType != null, StockInbound::getInboundType, inboundType)
               .eq(status != null, StockInbound::getStatus, status)
               .eq(shopId != null, StockInbound::getShopId, shopId)
               .eq(warehouseId != null, StockInbound::getWarehouseId, warehouseId)
               .orderByDesc(StockInbound::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 创建入库单
     */
    public Boolean createInbound(StockInbound inbound) {
        inbound.setInboundNo(generateInboundNo());
        // 待入库
        inbound.setStatus(0);
        inbound.setCreateTime(LocalDateTime.now());
        inbound.setUpdateTime(LocalDateTime.now());
        return save(inbound);
    }

    /**
     * 确认入库
     */
    public Boolean confirm(Long id) {
        StockInbound inbound = new StockInbound();
        inbound.setId(id);
        // 已完成
        inbound.setStatus(2);
        inbound.setActualTime(LocalDateTime.now());
        inbound.setUpdateTime(LocalDateTime.now());
        return updateById(inbound);
    }

    /**
     * 取消入库
     */
    public Boolean cancel(Long id) {
        StockInbound inbound = new StockInbound();
        inbound.setId(id);
        // 已取消
        inbound.setStatus(3);
        inbound.setUpdateTime(LocalDateTime.now());
        return updateById(inbound);
    }

    private String generateInboundNo() {
        return "IN" + System.currentTimeMillis();
    }
}
