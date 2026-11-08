package com.houjiang.mall.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.shop.Invoice;
import com.houjiang.mall.mapper.shop.InvoiceMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class InvoiceService extends ServiceImpl<InvoiceMapper, Invoice> {
    
    public IPage<Invoice> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Invoice::getStatus, status);
        wrapper.orderByDesc(Invoice::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
    
    public boolean issue(Invoice invoice) {
        invoice.setIssueTime(LocalDateTime.now());
        // 已开具
        invoice.setStatus(1);
        return this.updateById(invoice);
    }
}
