package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivitySpinwinPrizeService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ActivitySpinwinPrizeController
 * @Description
 * @Author Amos
 * @Date 2023/7/11 10:02
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("spinwinPrize")
public class ActivitySpinwinPrizeController{
    @Autowired
    protected ActivitySpinwinPrizeService activitySpinwinPrizeService;



    /**
     * 更新抽奖限制
     *
     * @param param
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteSpinwinPrize(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinPrizeService.deleteSpinwinPrize(param);
        return result;
    }


    /**
     * 增加或更新幸运大转盘奖项
     *
     * @param param
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateSpinwinPrize(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinPrizeService.updateSpinwinPrize(param);
        return result;
    }

    /**
     * 查询幸运大转盘奖项列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult querySpinwinPrizeList(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinPrizeService.querySpinwinPrizeList(param);
        return result;
    }


}
