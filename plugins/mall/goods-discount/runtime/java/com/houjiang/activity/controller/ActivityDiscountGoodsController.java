package com.houjiang.activity.controller;


import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityDiscountGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.fastjson.JSON;
import com.houjiang.activity.dto.DiscountBaseDetailQueryDTO;
import com.houjiang.activity.dto.DiscountGoodsAddParamDTO;
import com.houjiang.commons.annnotation.Log;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.AppDiscountGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.CheckDiscountLimitResultVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName ActivityDiscountGoodsController
 * @Description 限时折扣商品表
 * @Author Amos
 * @Date 2023-05-25 14:10:08
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("discountGoods")
public class ActivityDiscountGoodsController {
    @Autowired
    protected ActivityDiscountGoodsService activityDiscountGoodsService;


    /**
     * 查询限时折扣活动信息 feign
     */
    @PostMapping("/selectByGoodsId")
    public AppDiscountGoodsQueryResultVO selectDiscountByGoodsId(@RequestParam("goodsId") Integer goodsId) {
        return activityDiscountGoodsService.selectDiscountByGoodsId(goodsId);
    }

    /**
     * 根据当前时间查询限时折扣活动信息 feign
     */
    @PostMapping("/selectDiscountByGoodsIdAndTime")
    public AppDiscountGoodsQueryResultVO selectDiscountByGoodsIdAndTime(@RequestParam("goodsId") Integer goodsId) {
        return activityDiscountGoodsService.selectDiscountByGoodsIdAndTime(goodsId);
    }


    /**
     * 下单根据goodsid 查询活动详情 feign
     */
    @PostMapping("/queryByGoodsId")
    public CheckDiscountLimitResultVO queryDiscountInfoByGoodsId(@RequestParam Integer goodsId){
        return activityDiscountGoodsService.queryDiscountInfoByGoodsId(goodsId);
    }

    /**
     * 查询限时折扣首页 随机查询
     *
     * @param param
     * @return
     */
    @PostMapping("/index/limitQuery")
    public ResponseResult queryDiscountLimitIndex(@RequestBody Map<String,Object> param) {

        return activityDiscountGoodsService.queryDiscountLimitIndex(param);
    }

    /**
     * 查看 限时折扣是否到购买上限
     * @param param
     * @return
     */
    @PostMapping("/check/limit")
    public ResponseResult checkDiscountLimit(@RequestBody Map<String,Object> param) {

        return activityDiscountGoodsService.checkDiscountLimit(param);
    }

    /**
     * 查看 限时折扣上限
     * @param param
     * @return
     */
    @PostMapping("/check/limit2")
    public ResponseResult checkDiscountLimit2(@RequestBody Map<String,Object> param) {

        return activityDiscountGoodsService.checkDiscountLimit2(param);
    }

    /**
     * 删除限时折扣活动
     *
     * @param param
     * @return
     */
    @Log("删除限时折扣活动")
    @PostMapping("/delete")
    public ResponseResult deleteDiscountBase(@RequestBody DiscountBaseDetailQueryDTO param) {
        return activityDiscountGoodsService.deleteDiscountBase(param);
    }

    /**
     * 删除限时折扣活动商品
     *
     * @param param
     * @return
     */
    @Log("删除限时折扣活动商品")
    @PostMapping("/deleteGoods")
    public ResponseResult deleteDiscountGoods(@RequestBody DiscountBaseDetailQueryDTO param) {
        return activityDiscountGoodsService.deleteDiscountGoods(param);
    }


    /**
     * 添加商品到限时折扣活动
     *
     * @param param
     * @return
     */
    @Log("添加商品到限时折扣活动")
    @PostMapping("/add")
    public ResponseResult addDiscountGoods(@RequestBody DiscountGoodsAddParamDTO param) {
        if (null == param.getDiscountId()) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),BizCodeEnume.MISSING_PARAMETER.getMsg() + "DiscountId为null：" + JSON.toJSONString(param));
        }
        return activityDiscountGoodsService.addDiscountGoods(param);
    }

    /**
     * 更新限时折扣价格
     *
     * @param param
     * @return
     */
    @Log("更新限时折扣价格")
    @PostMapping("/price/update")
    public ResponseResult updateDiscountGoodsPrice(@RequestBody DiscountGoodsAddParamDTO param) {
        return activityDiscountGoodsService.updateDiscountGoodsPrice(param);
    }

    /**
     * 查询活动规格的活动价格
     * @param param
     * @return
     */
    @PostMapping("/spec/query")
    public ResponseResult queryDiscountGoodsSpecList(@RequestBody DiscountBaseDetailQueryDTO param) {
        return activityDiscountGoodsService.queryDiscountGoodsSpecList(param);
    }

    /**
     * 查询活动商品价格
     */
    @PostMapping("/query/goodsPrice")
    public ResponseResult selectPriceByGoodsId(@RequestParam Integer goodsId) {
        return activityDiscountGoodsService.selectPriceByGoodsId(goodsId);
    }
    /**
     * 失效限时折扣活动
     *
     * @param param
     * @return
     */
    @PostMapping("/inactive")
    public ResponseResult inactiveDiscountBase(@RequestBody Map<String,Object> param) {
        ResponseResult result=activityDiscountGoodsService.inactiveDiscountBase(param);
        return result;
    }

    /**
     * 查询限时折扣活动数据
     *
     * @param param
     * @return
     */
    @PostMapping("/query/data")
    public ResponseResult queryDiscountBaseData(@RequestBody Map<String,Object> param) {
        ResponseResult result=activityDiscountGoodsService.queryDiscountBaseData(param);
        return result;
    }

    /**
     * 导出限时折扣活动数据
     *
     * @param param
     * @return
     */
    @PostMapping("/query/data/exp")
    public ResponseResult queryDiscountBaseDataExp(@RequestBody Map<String,Object> param) {
        ResponseResult result=activityDiscountGoodsService.queryDiscountBaseDataExp(param);
        return result;
    }

}

