package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.entity.mall.MallGoodsSku;
import com.houjiang.merchant.vo.mall.MallGoodsSkuDetailVo;
import com.houjiang.merchant.vo.mall.MallGoodsSkuListedVo;
import com.houjiang.merchant.vo.mall.SkuNameListVo;

import java.util.List;

/**
 *  商品sku接口类
 * @author Amos
 */
public interface IMallGoodsSkuService {

    /**
     * TODO 补充方法说明
     * @param goodsId TODO
     * @return TODO
     */
    List<MallGoodsSkuListedVo> getListByGoods(Integer goodsId);

    /**
     * TODO 补充方法说明
     * @param goods TODO
     * @return TODO
     */
    List<SkuNameListVo> getSkuNameList(Integer goods);

    /**
     * 获取详情
     * @param id
     * @return
     */
    MallGoodsSkuDetailVo detail(Integer id);

    /**
     * 根据id 返回
     * @param id
     * @return
     */
    MallGoodsSku getById(Integer id);
}
