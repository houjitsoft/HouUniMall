package com.houjiang.mall.service.decoration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.decoration.DecoratePage;
import com.houjiang.mall.mapper.decoration.DecoratePageMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 店铺装修 Service
 * 
 * @author Amos
 */
@Service
public class DecoratePageService extends ServiceImpl<DecoratePageMapper, DecoratePage> {
    
    /**
     * 分页查询装修页面列表
     * @param params 查询参数
     * @return 分页结果
     */
    public IPage<DecoratePage> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        Integer pageType = params.get("pageType") != null ? Integer.parseInt(params.get("pageType").toString()) : null;
        
        LambdaQueryWrapper<DecoratePage> wrapper = new LambdaQueryWrapper<>();
        
        // 按店铺 ID 筛选
        wrapper.eq(shopId != null, DecoratePage::getShopId, shopId);
        
        // 按页面类型筛选
        wrapper.eq(pageType != null, DecoratePage::getPageType, pageType);
        
        // 默认查询启用的页面
        wrapper.eq(DecoratePage::getStatus, 1);
        
        // 按创建时间降序排序
        wrapper.orderByDesc(DecoratePage::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
    
    /**
     * 根据 ID 查询页面详情
     * @param pageId 页面 ID
     * @return 装修页面实体
     */
    public DecoratePage getPageDetail(Long pageId) {
        return this.getById(pageId);
    }
    
    /**
     * 保存页面装修配置
     * @param decoratePage 装修页面对象
     * @return 是否成功
     */
    public boolean savePageConfig(DecoratePage decoratePage) {
        if (decoratePage.getPageId() == null) {
            // 新增
            // 默认启用
            decoratePage.setStatus(1);
            return this.save(decoratePage);
        } else {
            // 更新
            return this.updateById(decoratePage);
        }
    }
    
    /**
     * 删除装修页面
     * @param pageId 页面 ID
     * @return 是否成功
     */
    public boolean deletePage(Long pageId) {
        return this.removeById(pageId);
    }
    
    /**
     * 获取首页装修列表
     * @param params 查询参数
     * @return 首页列表
     */
    public List<DecoratePage> getHomeList(Map<String, Object> params) {
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        
        LambdaQueryWrapper<DecoratePage> wrapper = new LambdaQueryWrapper<>();
        
        // 查询首页类型的页面
        wrapper.eq(DecoratePage::getPageType, 1);
        
        // 按店铺 ID 筛选
        wrapper.eq(shopId != null, DecoratePage::getShopId, shopId);
        
        // 默认查询启用的页面
        wrapper.eq(DecoratePage::getStatus, 1);
        
        // 按是否设为首页排序（置顶优先）
        wrapper.orderByDesc(DecoratePage::getIsHome)
               .orderByDesc(DecoratePage::getCreateTime);
        
        return this.list(wrapper);
    }
    
    /**
     * 设为首页
     * @param shopId 店铺 ID
     * @param pageId 页面 ID
     * @return 是否成功
     */
    public boolean setHomePage(Long shopId, Long pageId) {
        // 1. 取消该店铺所有页面的设为首页标记
        LambdaQueryWrapper<DecoratePage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DecoratePage::getShopId, shopId);
        List<DecoratePage> pages = this.list(wrapper);
        
        for (DecoratePage page : pages) {
            page.setIsHome(0);
        }
        this.updateBatchById(pages);
        
        // 2. 设置指定页面为首页
        DecoratePage homePage = this.getById(pageId);
        if (homePage != null && homePage.getShopId().equals(shopId)) {
            homePage.setIsHome(1);
            return this.updateById(homePage);
        }
        
        return false;
    }
}
