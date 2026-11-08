package com.houjiang.activity.controller;

import com.houjiang.activity.service.ActivitySeckillSubscribeService;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 限时秒杀开抢预约控制器
 *
 * <p>C 端预约 / 取消 / 我的预约 / 预约人数统计，后台预约明细查询。
 *
 * @author Amos
 */
@RestController
@RequestMapping("seckillSubscribe")
public class ActivitySeckillSubscribeController {

    @Autowired
    private ActivitySeckillSubscribeService activitySeckillSubscribeService;

    /** C 端预约（seckillId、goodsId、token） */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Map<String, Object> param) {
        return activitySeckillSubscribeService.subscribe(param);
    }

    /** C 端取消预约（seckillId、goodsId、token） */
    @PostMapping("/cancel")
    public ResponseResult cancel(@RequestBody Map<String, Object> param) {
        return activitySeckillSubscribeService.cancel(param);
    }

    /** C 端查询当前用户在某场次内已预约的商品 goodsId 列表（seckillId、token） */
    @PostMapping("/my")
    public ResponseResult my(@RequestBody Map<String, Object> param) {
        return activitySeckillSubscribeService.mySubscribes(param);
    }

    /** 某场次各商品预约人数（seckillId） */
    @PostMapping("/count")
    public ResponseResult count(@RequestBody Map<String, Object> param) {
        return activitySeckillSubscribeService.countBySeckill(param);
    }

    /** 后台分页查询某场次预约明细（seckillId、page、limit） */
    @PostMapping("/query/list")
    public ResponseResult queryList(@RequestBody Map<String, Object> param) {
        return activitySeckillSubscribeService.queryPage(param);
    }
}
