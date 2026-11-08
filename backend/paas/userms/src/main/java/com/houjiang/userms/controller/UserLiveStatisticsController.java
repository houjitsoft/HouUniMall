package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName UserLiveStatisticsController
 * @Description 用户活跃统计
 * @Author Amos
 * @Date 2023/5/19 10:16
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/liveStatistics")
public class UserLiveStatisticsController extends  BaseController{

    /**
     * 更新用户日活跃
     *
     * @param map
     * @return
     */
    @PostMapping("/insertUserDayLive")
    public ResponseResult insertUserDayLive(@RequestBody Map<String, Object> map) {
        ResponseResult result = userLiveStatisticsService.saveLiveStatistics(map);
        return ResponseResult.success(result);
    }


    /**
     * 查询各个端口日活详情
     * @param param
     * @return
     */
    @PostMapping("/queryUserNikkatsuDetailPort")
    public ResponseResult queryUserNikkatsuDetailPort(@RequestBody Map<String, Object> param) {
        ResponseResult result = userLiveStatisticsService.queryUserNikkatsuDetailPort(param);
        return ResponseResult.success(result);
    }


    /**
     * 查询各个端口日活
     * @param param
     * @return
     */
    @PostMapping("/queryUserNikkatsuPort")
    public ResponseResult queryUserNikkatsuPort(@RequestBody Map<String, Object> param) {
        ResponseResult result = userLiveStatisticsService.queryUserNikkatsuPort(param);
        return ResponseResult.success(result);
    }
}
