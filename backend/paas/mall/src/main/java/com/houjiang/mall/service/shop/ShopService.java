package com.houjiang.mall.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.shop.Shop;
import com.houjiang.mall.mapper.shop.ShopMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 店铺管理服务层
 *
 * @author Amos
 */
@Service
public class ShopService extends ServiceImpl<ShopMapper, Shop> {

    /**
     * 分页查询店铺列表
     *
     * @param params 查询参数：page,size,shopName,status,shopType
     * @return 分页结果
     */
    public IPage<Shop> pageList(Map<String, Object> params) {
        Integer page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        Integer size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        String shopName = (String) params.get("shopName");
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        Integer shopType = params.get("shopType") != null ? Integer.parseInt(params.get("shopType").toString()) : null;

        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(shopName), Shop::getShopName, shopName);
        wrapper.eq(status != null, Shop::getStatus, status);
        wrapper.eq(shopType != null, Shop::getShopType, shopType);
        wrapper.orderByDesc(Shop::getCreateTime);

        return this.page(new Page<>(page, size), wrapper);
    }

    /**
     * 更新店铺状态
     *
     * @param id     店铺 ID
     * @param status 新状态: 0-待审核 1-正常 2-冻结 3-关闭
     * @return 是否成功
     */
    public boolean updateStatus(Long id, Integer status) {
        Shop shop = this.getById(id);
        if (shop == null) return false;
        shop.setStatus(status);
        return this.updateById(shop);
    }

    /**
     * 获取店铺统计概览
     *
     * @return Map，包含total/pending/normal/frozen/closed店铺数量
     */
    public Map<String, Object> overview() {
        long total = this.count();
        long pending = this.count(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 0));
        long normal = this.count(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 1));
        long frozen = this.count(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 2));
        long closed = this.count(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 3));

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pending", pending);
        result.put("normal", normal);
        result.put("frozen", frozen);
        result.put("closed", closed);
        return result;
    }
}
