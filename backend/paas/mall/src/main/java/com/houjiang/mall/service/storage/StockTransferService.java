package com.houjiang.mall.service.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.storage.StockTransfer;
import com.houjiang.mall.mapper.storage.StockTransferMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 库存调拨服务类
 * 
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class StockTransferService extends ServiceImpl<StockTransferMapper, StockTransfer> {

    /**
     * 分页查询调拨单
     */
    public IPage<StockTransfer> pageList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);
        String transferNo = (String) params.get("transferNo");
        Integer transferType = params.get("transferType") != null ? Integer.parseInt(params.get("transferType").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;

        LambdaQueryWrapper<StockTransfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(transferNo), StockTransfer::getTransferNo, transferNo)
               .eq(transferType != null, StockTransfer::getTransferType, transferType)
               .eq(status != null, StockTransfer::getStatus, status)
               .eq(shopId != null, StockTransfer::getShopId, shopId)
               .orderByDesc(StockTransfer::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 创建调拨单
     */
    public Boolean createTransfer(StockTransfer transfer) {
        transfer.setTransferNo(generateTransferNo());
        // 待审核
        transfer.setStatus(0);
        transfer.setCreateTime(LocalDateTime.now());
        transfer.setUpdateTime(LocalDateTime.now());
        return save(transfer);
    }

    /**
     * 审核调拨单
     */
    public Boolean audit(Long id, Integer status, Long auditorId, String auditorName, String auditRemark) {
        StockTransfer transfer = new StockTransfer();
        transfer.setId(id);
        transfer.setStatus(status);
        transfer.setAuditorId(auditorId);
        transfer.setAuditorName(auditorName);
        transfer.setAuditRemark(auditRemark);
        transfer.setAuditTime(LocalDateTime.now());
        transfer.setUpdateTime(LocalDateTime.now());
        return updateById(transfer);
    }

    /**
     * 完成调拨
     */
    public Boolean complete(Long id) {
        StockTransfer transfer = new StockTransfer();
        transfer.setId(id);
        // 已完成
        transfer.setStatus(3);
        transfer.setActualTime(LocalDateTime.now());
        transfer.setUpdateTime(LocalDateTime.now());
        return updateById(transfer);
    }

    private String generateTransferNo() {
        return "TF" + System.currentTimeMillis();
    }
}
