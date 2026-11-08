package com.houjiang.mall.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.shop.MerchantEntry;
import com.houjiang.mall.mapper.shop.MerchantEntryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class MerchantEntryService extends ServiceImpl<MerchantEntryMapper, MerchantEntry> {
    
    public IPage<MerchantEntry> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        
        LambdaQueryWrapper<MerchantEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, MerchantEntry::getStatus, status);
        wrapper.orderByDesc(MerchantEntry::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
    
    public boolean audit(Long id, Integer status, String remark) {
        MerchantEntry entry = this.getById(id);
        entry.setStatus(status);
        entry.setAuditRemark(remark);
        entry.setAuditTime(LocalDateTime.now());
        return this.updateById(entry);
    }
}
