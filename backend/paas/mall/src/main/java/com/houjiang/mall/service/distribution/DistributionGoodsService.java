package com.houjiang.mall.service.distribution;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.mall.entity.distribution.DistributionGoods;
import com.houjiang.mall.mapper.distribution.DistributionGoodsMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分销商品服务类
 * 
 * @author Amos
 */
@Service
public class DistributionGoodsService extends ServiceImpl<DistributionGoodsMapper, DistributionGoods> {

    /**
     * 分页查询分销商品列表
     *
     * @param params 查询参数
     * @return 分页结果
     */
    public IPage<DistributionGoods> pageList(Map<String, Object> params) {
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer size = (Integer) params.getOrDefault("size", 10);
        Long shopId = params.get("shopId") != null ? Long.parseLong(params.get("shopId").toString()) : null;
        String goodsName = (String) params.get("goodsName");
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;

        LambdaQueryWrapper<DistributionGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, DistributionGoods::getShopId, shopId);
        wrapper.eq(status != null, DistributionGoods::getStatus, status);
        wrapper.like(StringUtils.hasText(goodsName), DistributionGoods::getGoodsName, goodsName);
        wrapper.orderByDesc(DistributionGoods::getCreateTime);

        return this.page(new Page<>(page, size), wrapper);
    }

    /**
     * 获取启用的分销商品列表
     *
     * @param shopId 店铺ID
     * @return 商品列表
     */
    public List<DistributionGoods> listEnabled(Long shopId) {
        LambdaQueryWrapper<DistributionGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(shopId != null, DistributionGoods::getShopId, shopId);
        wrapper.eq(DistributionGoods::getStatus, 1);
        wrapper.orderByDesc(DistributionGoods::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 创建分销商品
     *
     * @param goods 商品信息
     * @return 是否成功
     */
    public boolean createGoods(DistributionGoods goods) {
        goods.setCreateTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        return this.save(goods);
    }

    /**
     * 更新分销商品
     *
     * @param goods 商品信息
     * @return 是否成功
     */
    public boolean updateGoods(DistributionGoods goods) {
        goods.setUpdateTime(LocalDateTime.now());
        return this.updateById(goods);
    }

    /**
     * 更新分销商品状态
     *
     * @param id 商品ID
     * @param status 状态
     * @return 是否成功
     */
    public boolean updateStatus(Long id, Integer status) {
        DistributionGoods goods = new DistributionGoods();
        goods.setId(id);
        goods.setStatus(status);
        goods.setUpdateTime(LocalDateTime.now());
        return this.updateById(goods);
    }

    /**
     * 根据商品ID获取分销商品
     *
     * @param goodsId 商品ID
     * @return 分销商品
     */
    public DistributionGoods getByGoodsId(Long goodsId) {
        LambdaQueryWrapper<DistributionGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistributionGoods::getGoodsId, goodsId);
        wrapper.last("limit 1");
        return this.getOne(wrapper);
    }

    /**
     * 启用中的分销商品覆盖（按店铺 + 商品）
     */
    public DistributionGoods getEnabledByShopAndGoods(Long shopId, Long goodsId) {
        if (goodsId == null) {
            return null;
        }
        LambdaQueryWrapper<DistributionGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistributionGoods::getGoodsId, goodsId);
        wrapper.eq(DistributionGoods::getStatus, 1);
        if (shopId != null) {
            wrapper.eq(DistributionGoods::getShopId, shopId);
        }
        wrapper.last("limit 1");
        return this.getOne(wrapper);
    }
}
