package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.ActivityServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall商城活动接口控制器
/**
 * 面向PC前台用户的活动相关接口
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/activity")
public class MallActivityController {
    
    @Autowired
    private ActivityServiceFeign activityServiceFeign;
    
    /**
     * 获取秒杀活动
     */
    @PostMapping("/seckill")
    public ResponseResult getSeckillActivity(@RequestBody Map<String, Object> param) {
        param.put("type", "seckill");
        return activityServiceFeign.queryActivityList(param);
    }
    
    /**
     * 获取拼团活动
     */
    @PostMapping("/pintuan")
    public ResponseResult getPintuanActivity(@RequestBody Map<String, Object> param) {
        param.put("type", "pintuan");
        return activityServiceFeign.queryActivityList(param);
    }
    
    /**
     * 获取限时折扣
     */
    @PostMapping("/discount")
    public ResponseResult getDiscountActivity(@RequestBody Map<String, Object> param) {
        param.put("type", "discount");
        return activityServiceFeign.queryActivityList(param);
    }
    
    /**
     * 获取满减活动
     */
    @PostMapping("/full-discount")
    public ResponseResult getFullDiscountActivity(@RequestBody Map<String, Object> param) {
        param.put("type", "full-discount");
        return activityServiceFeign.queryActivityList(param);
    }
    
    /**
     * 获取活动详情
     */
    @PostMapping("/detail")
    public ResponseResult getActivityDetail(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.getActivityDetail(param);
    }
    
    /**
     * 获取活动列表
     */
    @PostMapping("/list")
    public ResponseResult getActivityList(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryActivityList(param);
    }
}
