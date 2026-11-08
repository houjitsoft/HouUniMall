package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivitySpinwinLuckyDrawTimesService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ActivitySpinwinLuckyDrawTimesController
 * @Description
 * @Author Amos
 * @Date 2023/7/11 9:32
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("spinwinLuckyDrawTimes")
public class ActivitySpinwinLuckyDrawTimesController{
    @Autowired
    protected ActivitySpinwinLuckyDrawTimesService activitySpinwinLuckyDrawTimesService;



    /**
     * 查询参与幸运大转盘活动的用户抽奖次数列表
     *
     * @param param
     * @return
     */
    @PostMapping("/list")
    public ResponseResult queryLuckyDrawTimesList(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinLuckyDrawTimesService.queryLuckyDrawTimesList(param);
        return result;
    }

}
