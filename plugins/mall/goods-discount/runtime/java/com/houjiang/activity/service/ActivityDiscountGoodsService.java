package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.DiscountBaseDetailQueryDTO;
import com.houjiang.activity.dto.DiscountGoodsAddParamDTO;
import com.houjiang.activity.entity.ActivityDiscountGoodsEntity;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.AppDiscountGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.CheckDiscountLimitResultVO;

import java.util.Map;

/**
 * @ClassName ActivityDiscountGoodsService
 * @Description 限时折扣商品表
 * @Author Amos
 * @Date 2023-05-25 14:10:09
 * @Version 1.0
 * @author Amos
 */
public interface ActivityDiscountGoodsService extends IService<ActivityDiscountGoodsEntity> {
    /**
     * 查询限时折扣活动信息
     * @param goodsId
     * @return
     */
    AppDiscountGoodsQueryResultVO selectDiscountByGoodsId(Integer goodsId);

    /**
     * 下单根据goodsid 查询活动详情
     * @param goodsId
     * @return
     */
    CheckDiscountLimitResultVO queryDiscountInfoByGoodsId(Integer goodsId);


    /**
     * 根据当前时间查询限时折扣活动信息 feign
     * @param goodsId
     * @return
     */
    AppDiscountGoodsQueryResultVO selectDiscountByGoodsIdAndTime(Integer goodsId);

    /**
     * 查询限时折扣首页 随机查询
     * @param param
     * @return
     */
    ResponseResult queryDiscountLimitIndex(Map<String, Object> param);

    /**
     * 查看 限时折扣是否到购买上限
     * @param param
     * @return
     */
    ResponseResult checkDiscountLimit(Map<String, Object> param);

    /**
     * 查看 限时折扣上限
     * @param param
     * @return
     */
    ResponseResult checkDiscountLimit2(Map<String, Object> param);
    /**
     * 删除限时折扣活动
     *
     * @param param
     * @return
     */
    ResponseResult deleteDiscountBase(DiscountBaseDetailQueryDTO param);

    /**
     * 删除限时折扣活动商品
     *
     * @param param
     * @return
     */
    ResponseResult deleteDiscountGoods(DiscountBaseDetailQueryDTO param);

    /**
     * 添加商品到限时折扣活动
     * @param param
     * @return
     */
    ResponseResult addDiscountGoods(DiscountGoodsAddParamDTO param);

    /**
     * 更新限时折扣价格
     * @param param
     * @return
     */
    ResponseResult updateDiscountGoodsPrice(DiscountGoodsAddParamDTO param);

    /**
     * 查询活动规格的活动价格
     * @param param
     * @return
     */
    ResponseResult queryDiscountGoodsSpecList(DiscountBaseDetailQueryDTO param);

    /**
     * 失效限时折扣活动
     *
     * @param param
     * @return
     */
    ResponseResult inactiveDiscountBase(Map<String,Object> param);

    /**
     * 查询活动商品价格
     */
    ResponseResult selectPriceByGoodsId(Integer goodsId);


    /**
     * 查询限时折扣活动数据
     *
     * @param param
     * @return
     */
   ResponseResult queryDiscountBaseData(Map<String,Object> param);

    /**
     * 导出限时折扣活动数据
     *
     * @param param
     * @return
     */
    ResponseResult queryDiscountBaseDataExp(Map<String,Object> param);

}

