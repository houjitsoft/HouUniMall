package com.houjiang.merchant.service.ms;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.ms.Ms;
import com.houjiang.merchant.common.entity.ms.MsGoods;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.ms.MsSearchValidate;
import com.houjiang.merchant.vo.ms.GoodsMsVo;
import com.houjiang.merchant.vo.ms.MsListedVo;
import com.houjiang.merchant.vo.ms.MsOrderCountVo;
import com.houjiang.merchant.vo.ms.MsSkuListedVo;

import java.util.List;

/**
 *  秒杀活动商品接口类
 * @author Amos
 */
public interface IMsGoodsService {

    /**
     * 根据秒杀ID返回单价最小的记录
     * @param msId
     * @return
     */
    MsGoods getMinPriceByMsId(Integer msId);

    /**
     * 根据秒杀ID返回单价最大的记录
     * @param msId
     * @return
     */
    MsGoods getMaxPriceByMsId(Integer msId);

    /**
     * 根据秒杀id 返回SkuList
     * @param id
     * @return
     */
    List<MsSkuListedVo> getMsSkuList(Integer id);

    /**
     * 根据ID返回秒杀商品列表
     * @param msId
     * @return
     */
    List<MsGoods> getListByMsId(Integer msId);

    /**
     * 根据秒杀ID删除项
     * @param id
     */
    void destroyItemByMsId(Integer msId);
}
