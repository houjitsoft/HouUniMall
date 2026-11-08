package com.houjiang.activity.controller;

import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivitySpinwinPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ActivitySpinwinPersonController
 * @Description
 * @Author Amos
 * @Date 2023/7/11 9:47
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("spinwinPerson")
public class ActivitySpinwinPersonController{
    @Autowired
    protected ActivitySpinwinPersonService activitySpinwinPersonService;



    /**
     * 查询参与幸运大转盘活动的用户列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult querySpinwinPersonList(@RequestBody Map<String, Object> param) {
        ResponseResult result = activitySpinwinPersonService.querySpinwinPersonList(param);
        return result;
    }

    /**
     * 查询获奖记录
     *
     * @param param
     * @return
     */
    @PostMapping("/query")
    public ResponseResult queryPrizeRecord(@RequestBody ActivityExplainParamDTO param) {
        ResponseResult result = activitySpinwinPersonService.queryPrizeRecord(param);
        return result;
    }

}
