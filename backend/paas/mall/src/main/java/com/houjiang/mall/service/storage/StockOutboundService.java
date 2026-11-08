package com.houjiang.mall.service.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.storage.StockOutbound;
import com.houjiang.mall.mapper.storage.StockOutboundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 出库单服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class StockOutboundService extends ServiceImpl<StockOutboundMapper, StockOutbound> {

    /**
     * 分页查询出库单
     */
    public IPage<StockOutbound> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String outboundNo = (String) params.get("outboundNo");
        Integer outboundType = params.get("outboundType") != null ? Integer.parseInt(params.get("outboundType").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Long warehouseId = params.get("warehouseId") != null ? Long.parseLong(params.get("warehouseId").toString()) : null;

        LambdaQueryWrapper<StockOutbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(outboundNo), StockOutbound::getOutboundNo, outboundNo)
               .eq(outboundType != null, StockOutbound::getOutboundType, outboundType)
               .eq(status != null, StockOutbound::getStatus, status)
               .eq(shopId != null, StockOutbound::getShopId, shopId)
               .eq(warehouseId != null, StockOutbound::getWarehouseId, warehouseId)
               .orderByDesc(StockOutbound::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 创建出库单
     */
    public Boolean createOutbound(StockOutbound outbound) {
        outbound.setOutboundNo(generateOutboundNo());
        // 待出库
        outbound.setStatus(0);
        outbound.setCreateTime(LocalDateTime.now());
        outbound.setUpdateTime(LocalDateTime.now());
        return save(outbound);
    }

    /**
     * 确认出库
     */
    public Boolean confirm(Long id) {
        StockOutbound outbound = new StockOutbound();
        outbound.setId(id);
        // 已完成
        outbound.setStatus(2);
        outbound.setActualTime(LocalDateTime.now());
        outbound.setUpdateTime(LocalDateTime.now());
        return updateById(outbound);
    }

    /**
     * 取消出库
     */
    public Boolean cancel(Long id) {
        StockOutbound outbound = new StockOutbound();
        outbound.setId(id);
        // 已取消
        outbound.setStatus(3);
        outbound.setUpdateTime(LocalDateTime.now());
        return updateById(outbound);
    }

    private String generateOutboundNo() {
        return "OUT" + System.currentTimeMillis();
    }
}
