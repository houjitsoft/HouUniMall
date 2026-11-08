package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.entity.mall.MallGoodsDiscount;
import com.houjiang.merchant.vo.ms.GoodsMsVo;

import java.util.List;

/**
 *  商品折扣接口类
 */
public interface IMallGoodsDiscountService {
    /**
     * @notes 删除商品设置
     * @return bool
     * @author Amos
     */
    void delGoodsSet(Integer goodsId);

    /**
     * TODO 补充方法说明
     * @param goodsIds TODO
     */
    void delGoodsArraySet(List<Integer> goodsIds);


    /**
     * 根据商品ID， skuId， 用户ID返回折扣信息
     * @param goodsId
     * @param goodsSkuId
     * @param userLevelId
     * @return
     */
    MallGoodsDiscount getByGSLid(Integer goodsId, Integer goodsSkuId, Integer userLevelId);
}
