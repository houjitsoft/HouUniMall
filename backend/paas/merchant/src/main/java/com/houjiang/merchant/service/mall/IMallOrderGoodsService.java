package com.houjiang.merchant.service.mall;


import com.houjiang.merchant.common.entity.mall.MallOrderGoods;
import com.houjiang.merchant.validate.mall.MallGoodsCategorySearchValidate;
import com.houjiang.merchant.validate.mall.MallOrderGoodsSearchValidate;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryDetailVo;
import com.houjiang.merchant.vo.mall.MallGoodsCategoryListedVo;
import com.houjiang.merchant.vo.mall.MallOrderGoodsListedVo;
import com.houjiang.merchant.vo.mallorder.MallOrderSimpleGoodsVo;

import java.util.List;

/**
 * 订单商品接口类
 * @author Amos
 */
public interface IMallOrderGoodsService {

    /**
     * 订单商品列表
     * @author Amos
     * @param searchValidate 搜索参数
     * @return List<MallOrderGoodsListedVo>
     */
    List<MallOrderGoodsListedVo> getListByOrderId(MallOrderGoodsSearchValidate searchValidate);

    /**
     *  更新售后信息
     * @param ids
     * @param code
     */
    void updateAfterSaleBase(List<Integer> ids, Integer afterSale);

    /**
     * 取消回滚库存
     * @param id
     */
    void orderCancelIncStock(Integer orderId);

    /**
     * 根据ID返回
     * @param id
     * @return
     */
    MallOrderGoods getById(Integer id);

    /**
     * 根据ID返回
     * @param id
     * @return
     */
    MallOrderSimpleGoodsVo getSimpleById(Integer id);
}
