package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallGoodsDiscount;
import com.houjiang.merchant.common.mapper.mall.MallGoodsDiscountMapper;
import com.houjiang.merchant.service.mall.IMallGoodsDiscountService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 商品折扣实现类
 */
@Service
public class MallGoodsDiscountServiceImpl implements IMallGoodsDiscountService {

    @Resource
    MallGoodsDiscountMapper mallGoodsDiscountMapper;
    /**
     * @notes 删除商品设置
     * @return bool
     * @author Amos
     */
    @Override
    public void delGoodsSet(Integer goodsId) {
        mallGoodsDiscountMapper.delete(new QueryWrapper<MallGoodsDiscount>().eq("goods_id", goodsId));
    }

    @Override
    public void delGoodsArraySet(List<Integer> goodsIds) {
        mallGoodsDiscountMapper.delete(new QueryWrapper<MallGoodsDiscount>().in("goods_id", goodsIds));
    }

    @Override
    public MallGoodsDiscount getByGSLid(Integer goodsId, Integer goodsSkuId, Integer userLevelId) {
        return mallGoodsDiscountMapper.selectOne(new QueryWrapper<MallGoodsDiscount>().eq("goods_id", goodsId).eq("goods_sku_id", goodsSkuId).eq("user_level_id", userLevelId).eq("delete_time", 0).orderByDesc("id").last("limit 1"));
    }
}
