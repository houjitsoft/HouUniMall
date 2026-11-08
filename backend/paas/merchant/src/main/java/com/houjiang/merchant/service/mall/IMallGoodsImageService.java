package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.vo.mall.MallShopDetailVo;

import java.util.List;

/**
 *  图片接口类
 * @author Amos
 */
public interface IMallGoodsImageService {

    /**
     * 根据商品id获取商品图片列表
     * @param goodsId
     * @return
     */
    List<String> getListByGoodsId(Integer goodsId);
}
