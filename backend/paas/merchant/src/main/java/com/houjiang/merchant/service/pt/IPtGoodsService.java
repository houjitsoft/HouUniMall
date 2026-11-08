package com.houjiang.merchant.service.pt;

import com.houjiang.merchant.common.entity.ms.MsGoods;
import com.houjiang.merchant.common.entity.pt.PtGoods;
import com.houjiang.merchant.vo.ms.MsSkuListedVo;
import com.houjiang.merchant.vo.pt.PtSkuListedVo;

import java.util.List;

/**
 *  拼团活动商品接口类
 * @author Amos
 */
public interface IPtGoodsService {

    /**
     * 根据拼团ID返回单价最小的记录
     * @param ptId
     * @return
     */
    PtGoods getMinPriceByPtId(Integer ptId);

    /**
     * 根据拼团ID返回单价最大的记录
     * @param ptId
     * @return
     */
    PtGoods getMaxPriceByPtId(Integer ptId);

    /**
     * 根据拼团id 返回SkuList
     * @param id
     * @return
     */
    List<PtSkuListedVo> getMsSkuList(Integer id);

    /**
     * 根据ID返回拼团商品列表
     * @param ptId
     * @return
     */
    List<PtGoods> getListByMsId(Integer ptId);

    /**
     * 根据拼团ID删除项
     * @param ptId
     */
    void destroyItemByPtId(Integer ptId);
}
