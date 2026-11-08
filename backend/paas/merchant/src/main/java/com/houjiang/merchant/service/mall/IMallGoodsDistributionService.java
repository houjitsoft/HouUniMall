package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.vo.ms.GoodsMsVo;

import java.util.List;

/**
 *  商品分销接口类
 */
public interface IMallGoodsDistributionService {

    /**
     * @notes 删除商品设置
     * @param $goods_id
     * @return bool
     * @author Amos
     */
    void delGoodsSet(Integer goodsId);

    /**
     * @notes 删除商品设置
     * @param $goods_id
     * @return bool
     * @author Amos
     */
    void delGoodsArraySet(List<Integer> goodsId);
}
