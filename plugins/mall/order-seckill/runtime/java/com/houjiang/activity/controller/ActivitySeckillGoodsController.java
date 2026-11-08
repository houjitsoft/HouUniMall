package com.houjiang.activity.controller;


import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivitySeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.PromotionIndexQueryParamDTO;
import com.houjiang.activity.dto.SeckillGoodsUpdateDTO;
import com.houjiang.activity.dto.SecondKillGoodsDTO;
import com.houjiang.commons.dto.goods.AppSeckillGoodsVO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.CheckSeckillLimitResultVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName ActivitySeckillGoodsController
 * @Description 限时秒杀商品表
 * @Author Amos
 * @Date 2023-05-25 13:58:28
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("seckillGoods")
public class ActivitySeckillGoodsController {
    @Autowired
    protected ActivitySeckillGoodsService activitySeckillGoodsService;



    /**
     * 查询秒杀活动信息 feign
     */
    @PostMapping("/selectSeckillByGoodsId")
    public AppSeckillGoodsQueryResultVO selectSeckillByGoodsId(@RequestBody Map<String, Object> map){
        AppSeckillGoodsQueryResultVO appSeckillGoodsQueryResultVO = activitySeckillGoodsService.selectSeckillByGoodsId(map);
        return appSeckillGoodsQueryResultVO;
    }
    /**
     * 查询秒杀活动信息 feign
     */
    @PostMapping("/selectSeckillByGoodsId2")
    public AppSeckillGoodsQueryResultVO selectSeckillByGoodsId2(@RequestParam("goodsId") Integer goodsId){
        AppSeckillGoodsQueryResultVO appSeckillGoodsQueryResultVO = activitySeckillGoodsService.selectSeckillByGoodsId2(goodsId);
        return appSeckillGoodsQueryResultVO;
    }

    /**
     * 查询秒杀活动信息 feign
     */
    @PostMapping("/selectByGoodsId")
    public AppSeckillGoodsVO selectByGoodsId(@RequestBody Map<String,Object> map){
        AppSeckillGoodsVO goodsId = activitySeckillGoodsService.selectByGoodsId((Integer) map.get("goodsId"));
        return goodsId;
    }
    /**
     * 查询秒杀商品的限购
     * @param param
     * @return
     */
    @PostMapping("/check/limit")
    public ResponseResult seckillCheckLimit(@RequestBody Map<String, Object> param){
        ResponseResult result = activitySeckillGoodsService.seckillCheckLimit(param);
        return result;
    }
    /**
     * 下单根据goodsid 查询活动详情 feign
     */
    @PostMapping("/querySeckillInfoByGoodsId")
    public CheckSeckillLimitResultVO querySeckillInfoByGoodsId(@RequestParam Integer goodsId){
        CheckSeckillLimitResultVO checkSeckillLimitResultVO = activitySeckillGoodsService.querySeckillInfoByGoodsId(goodsId);
        return checkSeckillLimitResultVO;
    }
    /**
     * 查询秒杀商品库存 feign
     */
    @PostMapping("/querySeckillNum")
    public Integer querySeckillNum(@RequestBody Map<String, Object> paramMap){
        Integer integer = activitySeckillGoodsService.querySeckillNum(paramMap);
        return integer;
    }
    /**
     * 更新秒杀商品库存
     * @param paramMap
     */
    @PostMapping("/updateSaleForSeckill")
    public ResponseResult updateSaleForSeckill(@RequestBody Map<String, Object> paramMap){
        ResponseResult result = activitySeckillGoodsService.updateSaleForSeckill(paramMap);
        return result;
    }
    /**
     * 查询秒杀首页
     *
     * @param param
     * @return
     */
    @PostMapping("/seckill/index/query")
    public ResponseResult querySeckillIndex(@RequestBody PromotionIndexQueryParamDTO param) {
        ResponseResult result = activitySeckillGoodsService.querySeckillIndex(param);
        return result;
    }

    /**
     * 查询 限时秒杀是否到购买上限
     * @param param
     * @return
     */
    @PostMapping("/seckill/check/limit")
    public ResponseResult checkSeckillLimit(@RequestBody Map<String,Object> param) {
        ResponseResult result = activitySeckillGoodsService.checkSeckillLimit(param);
        return result;
    }
    /**
     * 失效店铺秒杀活动详情
     *
     * @param paramMap
     * @return
     */
    @PostMapping("/seckill/base/inactive")
    public ResponseResult inactiveShopSeckillBase(@RequestBody Map<String, Object> paramMap) {
        ResponseResult result=activitySeckillGoodsService.inactiveShopSeckillBase(paramMap);
        return result;
    }


    /**
     *查询活动商品价格
     */
    @PostMapping("/seckillGoodsPrice")
    ResponseResult seckillGoodsPrice(@RequestParam Integer goodsId){
        ResponseResult result = activitySeckillGoodsService.seckillGoodsPrice(goodsId);
        return result;
    }

    /**
     * 限时秒杀商品列表查询
     * @param param
     * @return
     */
    @PostMapping("/querySecondKillGoods")
    public ResponseResult querySecondKillGoods(@RequestBody(required = false) SecondKillGoodsDTO param) throws IllegalAccessException {
        ResponseResult result=activitySeckillGoodsService.querySecondKillGoods(param);
        return result;
    }
    /**
     * 查询秒杀相差30%以上得商品
     *
     * @param param
     * @return
     */
    @PostMapping("/seckill/index/differ")
    public ResponseResult querySeckillDiffer(@RequestBody PromotionIndexQueryParamDTO param) {
        ResponseResult result = activitySeckillGoodsService.querySeckillDiffer(param);
        return result;
    }


    /**
     * 导出限时秒杀活动数据
     *
     * @param param
     * @return
     */
    @PostMapping("/seckill/base/query/data/exp")
    public ResponseResult queryDiscountBaseDataExp(@RequestBody Map<String,Object> param) {
        ResponseResult result=activitySeckillGoodsService.queryDiscountBaseDataExp(param);
        return result;
    }
    /**
     * 查询限时秒杀活动数据
     *
     * @param param
     * @return
     */
    @PostMapping("/seckill/base/query/data")
    public ResponseResult querySeckillBaseData(@RequestBody Map<String,Object> param) {
        ResponseResult result=activitySeckillGoodsService.querySeckillBaseData(param);
        return result;
    }



    /**
     * 根据commonId查询秒杀的规格
     *
     * @param param
     * @return
     */
    @PostMapping("/spec/query")
    public ResponseResult querySeckillGoodsSpecList(@RequestBody Map<String,Object> param) {
        ResponseResult result=activitySeckillGoodsService.querySeckillGoodsSpecList(param);
        return result;
    }


    /**
     * 删除限时秒杀商品
     *
     * @param map
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteShopSeckillGoods(@RequestBody Map<String, Object> map) {
        ResponseResult result=activitySeckillGoodsService.deleteShopSeckillGoods(map);
        return result;
    }

    /**
     * 查询限时秒杀商品
     *
     * @param param
     * @return
     */
    @PostMapping("/query")
    public ResponseResult queryShopSeckillGoods(@RequestBody Map<String, Object> param) {
        ResponseResult result=activitySeckillGoodsService.queryShopSeckillGoods(param);
        return result;
    }


    /**
     * 创建或更新限时秒杀商品
     *
     * @param param
     * @return
     */
    @PostMapping("/add")
    public ResponseResult addShopSeckillGoods(@RequestBody SeckillGoodsUpdateDTO param) {
        ResponseResult result=activitySeckillGoodsService.addShopSeckillGoods(param);
        return result;
    }


    /**
     * 查询商品
     *
     * @param param
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult queryGoodsList(@RequestBody Map<String, Object> param) {
        ResponseResult result=activitySeckillGoodsService.queryGoodsList(param);
        return result;
    }

    /**
     * 查询某商品的全部规格（后台按规格定价取数）
     *
     * @param param commonId
     * @return 规格行列表
     */
    @PostMapping("/specOfCommon/query")
    public ResponseResult queryCommonGoodsSpecs(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySeckillGoodsService.queryCommonGoodsSpecs(param);
        return result;
    }
}

