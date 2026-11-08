package com.houjiang.mall.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.shop.QualityDeposit;
import com.houjiang.mall.mapper.shop.QualityDepositMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class QualityDepositService extends ServiceImpl<QualityDepositMapper, QualityDeposit> {
    
    public IPage<QualityDeposit> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        
        LambdaQueryWrapper<QualityDeposit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, QualityDeposit::getStatus, status);
        wrapper.orderByDesc(QualityDeposit::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
    
    public boolean refund(Long id) {
        QualityDeposit deposit = this.getById(id);
        // 已退还
        deposit.setStatus(3);
        deposit.setRefundTime(LocalDateTime.now());
        return this.updateById(deposit);
    }
}
