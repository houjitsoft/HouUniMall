package com.houjiang.mall.service.risk;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.risk.RiskMonitor;
import com.houjiang.mall.mapper.risk.RiskMonitorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 风控监控服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class RiskMonitorService extends ServiceImpl<RiskMonitorMapper, RiskMonitor> {

    /**
     * 分页查询监控记录
     */
    public IPage<RiskMonitor> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        Integer monitorType = params.get("monitorType") != null ? Integer.parseInt(params.get("monitorType").toString()) : null;
        Integer monitorStatus = params.get("monitorStatus") != null ? Integer.parseInt(params.get("monitorStatus").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;

        LambdaQueryWrapper<RiskMonitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(monitorType != null, RiskMonitor::getMonitorType, monitorType)
               .eq(monitorStatus != null, RiskMonitor::getMonitorStatus, monitorStatus)
               .eq(shopId != null, RiskMonitor::getShopId, shopId)
               .orderByDesc(RiskMonitor::getLastCheckTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 创建或更新监控记录
     */
    public Boolean saveOrUpdateMonitor(RiskMonitor monitor) {
        monitor.setLastCheckTime(LocalDateTime.now());
        monitor.setUpdateTime(LocalDateTime.now());
        if (monitor.getId() == null) {
            monitor.setCreateTime(LocalDateTime.now());
            return save(monitor);
        }
        return updateById(monitor);
    }

    /**
     * 获取监控统计数据
     */
    public Map<String, Object> getStatistics(Long shopId) {
        long normalCount = count(new LambdaQueryWrapper<RiskMonitor>()
                .eq(RiskMonitor::getMonitorStatus, 0)
                .eq(shopId != null, RiskMonitor::getShopId, shopId));
        long warningCount = count(new LambdaQueryWrapper<RiskMonitor>()
                .eq(RiskMonitor::getMonitorStatus, 1)
                .eq(shopId != null, RiskMonitor::getShopId, shopId));
        long abnormalCount = count(new LambdaQueryWrapper<RiskMonitor>()
                .eq(RiskMonitor::getMonitorStatus, 2)
                .eq(shopId != null, RiskMonitor::getShopId, shopId));
        
        return Map.of(
            "normalCount", normalCount,
            "warningCount", warningCount,
            "abnormalCount", abnormalCount,
            "totalCount", normalCount + warningCount + abnormalCount
        );
    }
}
