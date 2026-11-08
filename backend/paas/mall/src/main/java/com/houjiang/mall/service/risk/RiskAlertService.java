package com.houjiang.mall.service.risk;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.risk.RiskAlert;
import com.houjiang.mall.mapper.risk.RiskAlertMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 风控预警服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class RiskAlertService extends ServiceImpl<RiskAlertMapper, RiskAlert> {

    /**
     * 分页查询预警记录
     */
    public IPage<RiskAlert> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String alertNo = (String) params.get("alertNo");
        Integer alertType = params.get("alertType") != null ? Integer.parseInt(params.get("alertType").toString()) : null;
        Integer alertLevel = params.get("alertLevel") != null ? Integer.parseInt(params.get("alertLevel").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;

        LambdaQueryWrapper<RiskAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(alertNo), RiskAlert::getAlertNo, alertNo)
               .eq(alertType != null, RiskAlert::getAlertType, alertType)
               .eq(alertLevel != null, RiskAlert::getAlertLevel, alertLevel)
               .eq(status != null, RiskAlert::getStatus, status)
               .eq(shopId != null, RiskAlert::getShopId, shopId)
               .orderByDesc(RiskAlert::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 创建预警记录
     */
    public Boolean createAlert(RiskAlert alert) {
        alert.setAlertNo(generateAlertNo());
        // 待处理
        alert.setStatus(0);
        alert.setNotifyStatus(0);
        alert.setCreateTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());
        return save(alert);
    }

    /**
     * 处理预警
     */
    public Boolean handle(Long id, Long handlerId, String handlerName, String handleResult, String handleRemark) {
        RiskAlert alert = new RiskAlert();
        alert.setId(id);
        // 已处理
        alert.setStatus(2);
        alert.setHandlerId(handlerId);
        alert.setHandlerName(handlerName);
        alert.setHandleResult(handleResult);
        alert.setHandleRemark(handleRemark);
        alert.setHandleTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());
        return updateById(alert);
    }

    /**
     * 忽略预警
     */
    public Boolean ignore(Long id, String remark) {
        RiskAlert alert = new RiskAlert();
        alert.setId(id);
        // 已忽略
        alert.setStatus(3);
        alert.setHandleRemark(remark);
        alert.setUpdateTime(LocalDateTime.now());
        return updateById(alert);
    }

    /**
     * 获取待处理预警数量
     */
    public long getPendingCount(Long shopId) {
        return count(new LambdaQueryWrapper<RiskAlert>()
                .eq(RiskAlert::getStatus, 0)
                .eq(shopId != null, RiskAlert::getShopId, shopId));
    }

    private String generateAlertNo() {
        return "ALT" + System.currentTimeMillis();
    }
}
