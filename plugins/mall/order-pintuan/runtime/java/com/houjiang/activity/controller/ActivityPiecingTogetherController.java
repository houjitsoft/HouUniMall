package com.houjiang.activity.controller;


import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityPiecingTogetherService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.PintuanBaseAddDTO;
import com.houjiang.activity.dto.PintuanBaseListQueryDTO;
import com.houjiang.activity.dto.WebPinTuanParamDTO;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.dto.goods.ListPageQueryParamDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.AppPintuanGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.JoinPintuanParamVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName ActivityPiecingTogetherController
 * @Description 拼团主表
 * @Author Amos
 * @Date 2023-05-25 14:27:05
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("piecingTogether")
public class ActivityPiecingTogetherController {
    @Autowired
    protected ActivityPiecingTogetherService activityPiecingTogetherService;

    /**
     * 查询拼团活动信息 feign
     */
    @PostMapping("selectByGoodsId")
    public AppPintuanGoodsQueryResultVO selectByGoodsId(@RequestParam("commonId") Integer commonId,@RequestParam("goodsId") Integer goodsId) {
        return activityPiecingTogetherService.selectByGoodsId(commonId,goodsId);
    }

    /**
     * 查询拼团详情
     *
     * @return
     */
    @GetMapping("/surplus/num/query")
    public ResponseResult querySurplusNum(@RequestParam(required = false) String markId) {

        return activityPiecingTogetherService.querySurplusNum(markId);
    }
    /**
     * 发起拼团
     *
     * @param param
     * @return
     */
    @PostMapping("/join/pintuan/add")
    public ResponseResult addJoinPintuan(@RequestBody JoinPintuanParamVO param) {

        return activityPiecingTogetherService.addJoinPintuan(param);
    }

    /**
     * 支付成功后回写已付款并按已付款人数判定成团（供 order 支付回调调用）
     */
    @PostMapping("/join/pintuan/paid")
    public ResponseResult markPaidAndEvaluate(@RequestBody Map<String, Object> param) {
        return activityPiecingTogetherService.markPaidAndEvaluate(param);
    }
    /**
     * 查询所有 限时拼团商品
     *
     * @return
     */
    @PostMapping("/queryAll")
    public ResponseResult queryAll(@RequestBody ListPageQueryParamDTO param) {
        ResponseResult result = activityPiecingTogetherService.queryAll(param);
        return result;
    }

    /**
     * 取消订单 删除当前用户的拼团信息
     */
    @PostMapping("/delete")
    public ResponseResult deletePinTuanInfo(@RequestBody Map<String,Object> param){
        ResponseResult delete = activityPiecingTogetherService.delete(param);
        return delete;
    }

    /**
     * pc 查询拼团商品
     *
     * @return
     */
    @PostMapping("/queryGoods")
    public ResponseResult queryGoods(@RequestBody WebPinTuanParamDTO webPinTuanParam) {
        ResponseResult result = activityPiecingTogetherService.queryGoods(webPinTuanParam);
        return result;
    }

    /**
     * 组件化查询拼团活动列表
     *
     * @param param
     * @return
     */
    @PostMapping("/queryList")
    public ResponseResult queryList(@RequestBody PintuanBaseListQueryDTO param) {
        return activityPiecingTogetherService.queryList(param);
    }


    /**
     * 查询所有拼团商品
     * @param param
     * @return
     */
    @PostMapping("/queryGoodsAll")
    public ResponseResult queryGoodsAll(@RequestBody Map<String, Object>  param) {
        ResponseResult result = activityPiecingTogetherService.queryGoodsAll(param);
        return result;
    }


    /**
     * 更新权重
     * @param param
     * @return
     */
    @PostMapping("/disorder/update")
    public ResponseResult updateDisorder(@RequestBody Map<String, Object>  param) {
        ResponseResult result = activityPiecingTogetherService.updateDisorder(param);
        return result;
    }


    /**
     * 平台-查询拼团活动列表
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/query/list")
    public ResponseResult queryPintuanBaseList(@RequestBody  Map<String, Object> param) {
        ResponseResult result = activityPiecingTogetherService.queryPintuanBaseList(param);
        return result;
    }

    /**
     * 组件化查询拼团、 限时秒杀 、限时折扣 活动商品详情
     *
     * @param param
     * @return
     */
    @PostMapping("/query/byId")
    public ResponseResult modularizationQueryPintuanBasebyId(@RequestBody Map<String,Object> param) {
        ResponseResult result = activityPiecingTogetherService.modularizationQueryPintuanBasebyId(param);
        return result;
    }
    /**
     * 生成 h5 或 小程序 邀请好友拼团海报
     */
    @PostMapping("/getPoster")
    public ResponseResult getPinTuanPoster(@RequestBody Map<String,Object> param) {

        return activityPiecingTogetherService.getPinTuanPoster(param);
    }


    /**
     * 查询拼团活动详情
     *
     * @param param
     * @return
     */
    @PostMapping("/query/detail")
    public ResponseResult queryPinTuanBaseDetail(@RequestBody Map<String, Object>  param) {
        ResponseResult result = activityPiecingTogetherService.queryPinTuanBaseDetail(param);
        return result;
    }



    /**
     * 新增拼团活动
     *
     * @param param
     * @return
     */
    @SensitiveCheck(field = {"name"})
    @PostMapping("/base/add")
    public ResponseResult addPinTuanBase(@RequestBody PintuanBaseAddDTO param) {
        ResponseResult result = activityPiecingTogetherService.addPinTuanBase(param);
        return result;
    }


    /**
     * 删除拼团活动详情
     *
     * @param param
     * @return
     */
    @PostMapping("/base/delete")
    public ResponseResult deletePinTuanBase(@RequestBody Map<String, Object>  param) {
        ResponseResult result = activityPiecingTogetherService.deletePinTuanBase(param);
        return result;
    }

    /**
     * 启停拼团活动
     *
     * @param param pintuanId + status（1 启用 / 0 停用）
     * @return
     */
    @PostMapping("/base/status")
    public ResponseResult updatePinTuanBaseStatus(@RequestBody Map<String, Object> param) {
        return activityPiecingTogetherService.updatePinTuanBaseStatus(param);
    }

    /**
     * 查询拼团活动列表
     *
     * @param param
     * @return
     */
    @PostMapping("/base/query/list")
    public ResponseResult queryPinTuanBaseList(@RequestBody Map<String, Object> param) {
        ResponseResult result = activityPiecingTogetherService.queryPinTuanBaseList(param);
        return result;
    }

    /**
     * 通过扫小程序二维码 获取类型参数 根据参数从redis中获取具体参数
     */
    @PostMapping("/getParameterByToken")
    public ResponseResult getParameterByToken(@RequestBody Map<String,Object> param) {
        ResponseResult result = activityPiecingTogetherService.getParameterByToken(param);
        return result;
    }
}

