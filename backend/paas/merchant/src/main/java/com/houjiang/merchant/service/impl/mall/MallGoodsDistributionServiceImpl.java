package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallGoodsDistribution;
import com.houjiang.merchant.common.mapper.mall.MallGoodsDistributionMapper;
import com.houjiang.merchant.service.mall.IMallGoodsDistributionService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 商品分销实现类
 * @author Amos
 */
@Service
public class MallGoodsDistributionServiceImpl implements IMallGoodsDistributionService {

    @Resource
    MallGoodsDistributionMapper mallGoodsDistributionMapper;
    @Override
    public void delGoodsSet(Integer goodsId) {
        mallGoodsDistributionMapper.delete(new QueryWrapper<MallGoodsDistribution>().eq("goods_id", goodsId));
    }

    @Override
    public void delGoodsArraySet(List<Integer> goodsId) {
        mallGoodsDistributionMapper.delete(new QueryWrapper<MallGoodsDistribution>().in("goods_id", goodsId));
    }
}
