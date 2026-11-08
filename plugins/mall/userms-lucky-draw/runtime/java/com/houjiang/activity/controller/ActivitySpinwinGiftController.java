package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivitySpinwinGiftService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ActivitySpinwinGiftController
 * @Description
 * @Author Amos
 * @Date 2023/7/11 10:19
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("spinwinGift")
public class ActivitySpinwinGiftController{
    @Autowired
    protected ActivitySpinwinGiftService activitySpinwinGiftService;


    /**
     * 删除活动礼品
     *
     * @param param
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteSpinwinGift(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinGiftService.deleteSpinwinGift(param);
        return result;
    }

    /**
     * 增加或更新幸运大转盘活动
     *
     * @param param
     * @return
     */
    @PostMapping("/update")
    public ResponseResult updateSpinwinGift(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinGiftService.updateSpinwinGift(param);
        return result;
    }

    /**
     * 查询幸运大转盘活动列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult querySpinwinGiftList(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinGiftService.querySpinwinGiftList(param);
        return result;
    }
    /**
     * 查询奖品
     *
     * @param param
     * @return
     */
    @PostMapping("/gift/query")
    public ResponseResult querySpinWinGift(@RequestBody ActivityExplainParamDTO param) {
        ResponseResult result = activitySpinwinGiftService.querySpinWinGift(param);
        return result;
    }
}
