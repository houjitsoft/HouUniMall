package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallGather;
import com.houjiang.merchant.common.entity.mall.MallGatherGoods;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.gather.MallGatherCreateValidate;
import com.houjiang.merchant.validate.gather.MallGatherEditGoodsValidate;
import com.houjiang.merchant.validate.gather.MallGatherSaveGoodsValidate;
import com.houjiang.merchant.validate.gather.MallGatherSearchValidate;
import com.houjiang.merchant.vo.gather.MallGatherDetailVo;
import com.houjiang.merchant.vo.gather.MallGatherGoodsDetailVo;
import com.houjiang.merchant.vo.gather.MallGatherGoodsListedVo;
import com.houjiang.merchant.vo.gather.MallGatherListedVo;

import java.io.IOException;
import java.util.List;

/**
 * 采集商品接口类
 * @author Amos
 */
public interface IMallGatherGoodsService {

    /**
     * 获取还没处理的列表
     * @param gatherId
     * @return
     */
    List<MallGatherGoodsListedVo> goodsDealNo(Integer gatherId);

    /**
     * 获取处理的列表
     * @param gatherId
     * @return
     */
    List<MallGatherGoodsListedVo> goodsDealYes(Integer gatherId);

    /**
     * 获取采集商品详情
     * @param id
     * @return
     */
    MallGatherGoodsDetailVo detail(Integer id);

    /**
     * 编辑采集商品
     * @param editGoodsValidate
     */
    void editGoods(MallGatherEditGoodsValidate editGoodsValidate);

    /**
     * 采集商品 保存
     * @param saveValidate
     */
    void saveGoods(MallGatherSaveGoodsValidate saveValidate) throws IOException;
}
