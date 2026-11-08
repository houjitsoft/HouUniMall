package com.houjiang.mall.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.shop.Inquiry;
import com.houjiang.mall.mapper.shop.InquiryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class InquiryService extends ServiceImpl<InquiryMapper, Inquiry> {
    
    public IPage<Inquiry> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        
        LambdaQueryWrapper<Inquiry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Inquiry::getStatus, status);
        wrapper.orderByDesc(Inquiry::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
    
    public boolean quote(Inquiry inquiry) {
        inquiry.setQuoteTime(LocalDateTime.now());
        // 已报价
        inquiry.setStatus(1);
        return this.updateById(inquiry);
    }
}
