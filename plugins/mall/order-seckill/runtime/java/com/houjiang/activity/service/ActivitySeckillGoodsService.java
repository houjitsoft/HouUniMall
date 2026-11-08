package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.PromotionIndexQueryParamDTO;
import com.houjiang.activity.dto.SeckillGoodsUpdateDTO;
import com.houjiang.activity.dto.SecondKillGoodsDTO;
import com.houjiang.activity.entity.ActivitySeckillGoodsEntity;
import com.houjiang.commons.dto.goods.AppSeckillGoodsVO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.CheckSeckillLimitResultVO;

import java.util.Map;

/**
 * @ClassName ActivitySeckillGoodsService
 * @Description 限时秒杀商品表
 * @Author Amos
 * @Date 2023-05-25 13:58:28
 * @Version 1.0
 * @author Amos
 */
public interface ActivitySeckillGoodsService extends IService<ActivitySeckillGoodsEntity> {
    /**
     * 查询秒杀活动信息
     *
     * @param map
     * @param
     * @return
     */
    AppSeckillGoodsQueryResultVO selectSeckillByGoodsId(Map<String, Object> map);

    /**
     * 查询秒杀活动信息
     *
     * @param goodsId
     * @return
     */
    AppSeckillGoodsQueryResultVO selectSeckillByGoodsId2(Integer goodsId);

    /**
     * 查询秒杀活动信息 feign
     */
    AppSeckillGoodsVO selectByGoodsId(Integer goodsId);

    /**
     * 查询秒杀商品的限购
     * @param param
     * @return
     */
    ResponseResult seckillCheckLimit(Map<String, Object> param);

    /**
     * 下单根据goodsid 查询活动详情
     * @param goodsId
     * @return
     */
    CheckSeckillLimitResultVO querySeckillInfoByGoodsId(Integer goodsId);

    /**
     * 查询秒杀商品库存
     * @param paramMap
     * @return
     */
    Integer querySeckillNum(Map<String, Object> paramMap);

    /**
     * 更新秒杀商品库存
     * @param paramMap
     * @return
     */
    ResponseResult updateSaleForSeckill(Map<String, Object> paramMap);

    /**
     * 查询秒杀首页
     * @param param
     * @return
     */
    ResponseResult querySeckillIndex(PromotionIndexQueryParamDTO param);
    /**
     * 查询 限时秒杀是否到购买上限
     * @param param
     * @return
     */
    ResponseResult checkSeckillLimit(Map<String, Object> param);
    /**
     * 失效店铺秒杀活动详情
     *
     * @param paramMap
     * @return
     */
    ResponseResult inactiveShopSeckillBase(Map<String, Object> paramMap);

    /**
     *查询活动商品价格
     */
    ResponseResult seckillGoodsPrice(Integer goodsId);

    /**
     * 限时秒杀商品列表查询
     * @param param
     * @return
     */
    ResponseResult querySecondKillGoods(SecondKillGoodsDTO param) throws IllegalAccessException;

    /**
     * 查询秒杀相差30%以上得商品
     * @param param
     * @return
     */
    ResponseResult querySeckillDiffer(PromotionIndexQueryParamDTO param);


    /**
     * 导出限时秒杀活动数据
     *
     * @param param
     * @return
     */
    ResponseResult queryDiscountBaseDataExp(Map<String,Object> param);

    /**
     * 查询限时秒杀活动数据
     *
     * @param param
     * @return
     */
    ResponseResult querySeckillBaseData(Map<String,Object> param);


    /**
     * 根据commonId查询秒杀的规格
     *
     * @param param
     * @return
     */
    ResponseResult querySeckillGoodsSpecList(Map<String,Object> param);




    /**
     * 删除限时秒杀商品
     *
     * @param map
     * @return
     */
    ResponseResult deleteShopSeckillGoods(Map<String, Object> map);


    /**
     * 查询限时秒杀商品
     *
     * @param param
     * @return
     */
    ResponseResult queryShopSeckillGoods(Map<String, Object> param);



    /**
     * 创建或更新限时秒杀商品
     *
     * @param param
     * @return
     */
    ResponseResult addShopSeckillGoods(SeckillGoodsUpdateDTO param);


    /**
     * 查询商品
     *
     * @param param
     * @return
     */
    ResponseResult queryGoodsList(Map<String, Object> param);

    /**
     * 查询某商品（commonId）下的全部规格。
     *
     * <p>秒杀商品以 goods_id（规格）为粒度落库、秒杀价即规格价，后台配置需要
     * 先拿到规格清单再逐条定价，故单独开一个取数口。
     *
     * @param param 至少包含 commonId
     * @return 规格行列表（goodsId / specName / goodsPrice / goodsStock）
     */
    ResponseResult queryCommonGoodsSpecs(Map<String, Object> param);
}

