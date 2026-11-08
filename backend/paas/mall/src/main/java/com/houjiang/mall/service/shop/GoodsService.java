package com.houjiang.mall.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.shop.Goods;
import com.houjiang.mall.mapper.shop.GoodsMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 商品管理 Service
 * 
 * @author Amos
 */
@Service
public class GoodsService extends ServiceImpl<GoodsMapper, Goods> {
    
    /**
     * 分页查询商品列表
     * @param params 查询参数
     * @return 分页结果
     */
    public IPage<Goods> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        String goodsName = (String) params.get("goodsName");
        Integer catId = params.get("catId") != null ? Integer.parseInt(params.get("catId").toString()) : null;
        Integer shopCategoryId = params.get("shopCategoryId") != null ? Integer.parseInt(params.get("shopCategoryId").toString()) : null;
        Integer type = params.get("type") != null ? Integer.parseInt(params.get("type").toString()) : null;
        
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        
        // 搜索商品名称
        wrapper.like(StringUtils.hasText(goodsName), Goods::getGoodsName, goodsName);
        
        // 搜索平台分类
        wrapper.eq(catId != null, Goods::getCatId, catId);
        
        // 搜索商品分类（店铺分类）
        // TODO: 如果有 shopCategoryId 字段，需要添加对应查询条件
        
        // 搜索类型：sale-销售中 audit-待审核 soldOut-已售罄 warehouse-仓库中 recycleBin-回收站
        if (type != null) {
            switch (type) {
                // sale - 销售中（上架状态且未删除）
                case 1:
                    wrapper.eq(Goods::getGoodsIsShelves, 1)
                           .eq(Goods::getIsDelete, 0);
                    break;
                // audit - 待审核
                case 2:
                    // TODO: 如果有审核状态字段，添加对应条件
                    break;
                // soldOut - 已售罄
                case 3:
                    wrapper.eq(Goods::getGoodsSalenum, 0);
                    break;
                // warehouse - 仓库中（下架状态）
                case 4:
                    wrapper.eq(Goods::getGoodsIsShelves, 2)
                           .eq(Goods::getIsDelete, 0);
                    break;
                // recycleBin - 回收站（已删除）
                case 5:
                    wrapper.eq(Goods::getIsDelete, 1);
                    break;
                default:
                    // 默认查询未删除的商品
                    wrapper.eq(Goods::getIsDelete, 0);
            }
        } else {
            // 默认只查询未删除的商品
            wrapper.eq(Goods::getIsDelete, 0);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Goods::getCreateTime);
        
        return this.page(new Page<>(page, size), wrapper);
    }
    
    /**
     * 根据 ID 查询商品详情
     * @param goodsId 商品 ID
     * @return 商品实体
     */
    public Goods getGoodsDetail(Integer goodsId) {
        Goods goods = this.getById(goodsId);
        if (goods != null && goods.getIsDelete() == 1) {
            // 已删除的商品返回 null
            return null;
        }
        return goods;
    }
    
    /**
     * 添加商品
     * @param goods 商品信息
     * @return 是否成功
     */
    public boolean addGoods(Goods goods) {
        // 初始化默认值
        if (goods.getGoodsIsShelves() == null) {
            // 默认上架
            goods.setGoodsIsShelves(1);
        }
        if (goods.getIsDelete() == null) {
            // 默认未删除
            goods.setIsDelete(0);
        }
        return this.save(goods);
    }
    
    /**
     * 编辑商品
     * @param goods 商品信息
     * @return 是否成功
     */
    public boolean editGoods(Goods goods) {
        return this.updateById(goods);
    }
    
    /**
     * 更新商品上下架状态
     * @param goodsId 商品 ID
     * @param status 状态：1-上架 2-下架
     * @return 是否成功
     */
    public boolean updateGoodsStatus(Integer goodsId, Integer status) {
        Goods goods = this.getById(goodsId);
        if (goods == null || goods.getIsDelete() == 1) {
            return false;
        }
        
        // 验证状态值：1-上架 2-下架
        if (!status.equals(1) && !status.equals(2)) {
            return false;
        }
        
        goods.setGoodsIsShelves(status);
        return this.updateById(goods);
    }
    
    /**
     * 删除商品（移到回收站）- 软删除
     * @param goodsId 商品 ID
     * @return 是否成功
     */
    public boolean deleteGoods(Integer goodsId) {
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            return false;
        }
        
        goods.setIsDelete(1);
        return this.updateById(goods);
    }
    
    /**
     * 批量删除商品
     * @param goodsIds 商品 ID 数组
     * @return 是否成功
     */
    public boolean batchDeleteGoods(Integer[] goodsIds) {
        for (Integer goodsId : goodsIds) {
            deleteGoods(goodsId);
        }
        return true;
    }
}
