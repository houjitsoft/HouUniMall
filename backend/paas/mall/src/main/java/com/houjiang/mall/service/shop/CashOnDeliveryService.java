package com.houjiang.mall.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.shop.CashOnDelivery;
import com.houjiang.mall.mapper.shop.CashOnDeliveryMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Amos
 */
@Service
public class CashOnDeliveryService extends ServiceImpl<CashOnDeliveryMapper, CashOnDelivery> {
    
    public IPage<CashOnDelivery> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Integer paymentStatus = params.get("paymentStatus") != null ? Integer.parseInt(params.get("paymentStatus").toString()) : null;
        
        LambdaQueryWrapper<CashOnDelivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(paymentStatus != null, CashOnDelivery::getPaymentStatus, paymentStatus);
        wrapper.orderByDesc(CashOnDelivery::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
}
