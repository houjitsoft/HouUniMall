package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivityPiecingTogetherService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.activity.JoinPintuanParamVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName ActivityPiecingTogetherMarkController
 * @Description
 * @Author Amos
 * @Date 2023/6/27 14:58
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("piecingTogetherMark")
public class ActivityPiecingTogetherMarkController{
    @Autowired
    protected ActivityPiecingTogetherService activityPiecingTogetherService;


    /**
     * 查询拼团信息 feign
     */
    @PostMapping("/selectMarkInfo")
    public ResponseResult selectMarkInfo(@RequestBody Map<String,Object> param) {
        ResponseResult result = activityPiecingTogetherService.selectMarkInfo(param);
        return result;
    }

    /**
     * 修改拼团支付状态
     */
    @PostMapping("/updatePayStart")
    ResponseResult updatePinTuanPayStart(@RequestBody Map<String,Object> param) {
        ResponseResult result = activityPiecingTogetherService.updatePayStart(param);
        return result;
    }

    /**
     * 查询活动商品价格
     */
    @PostMapping("/query/goodsPrice")
    ResponseResult queryGoodsPrice(@RequestParam Integer goodsId) {
        ResponseResult result = activityPiecingTogetherService.queryGoodsPrice(goodsId);
        return result;
    }


    /**
     * 查询拼团详情
     *
     * @param param
     * @return
     */
    @PostMapping("/join/query/detail")
    public ResponseResult queryDetail(@RequestBody JoinPintuanParamVO param) {
        ResponseResult result = activityPiecingTogetherService.queryDetail(param);
        return result;
    }

    /**
     * 正在拼团的用户列表
     */
    @PostMapping("/query/list")
    public ResponseResult queryList(@RequestBody Map<String,Object>  param) {
        ResponseResult result=activityPiecingTogetherService.queryList(param);
        return result;
    }
}
