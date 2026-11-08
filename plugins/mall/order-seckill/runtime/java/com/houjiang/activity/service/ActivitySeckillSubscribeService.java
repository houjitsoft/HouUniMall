package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.entity.ActivitySeckillSubscribeEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * 限时秒杀开抢预约 Service
 *
 * @author Amos
 */
public interface ActivitySeckillSubscribeService extends IService<ActivitySeckillSubscribeEntity> {

    /**
     * C 端预约某场次内某个秒杀商品（幂等，已预约直接返回成功）。
     *
     * @param param seckillId、goodsId、token
     */
    ResponseResult subscribe(Map<String, Object> param);

    /**
     * C 端取消预约。
     *
     * @param param seckillId、goodsId、token
     */
    ResponseResult cancel(Map<String, Object> param);

    /**
     * 查询当前用户在某场次内已预约的商品 goodsId 集合。
     *
     * @param param seckillId、token
     */
    ResponseResult mySubscribes(Map<String, Object> param);

    /**
     * 统计某场次各秒杀商品的预约人数（无需登录，供 C 端与后台展示）。
     *
     * @param param seckillId
     */
    ResponseResult countBySeckill(Map<String, Object> param);

    /**
     * 后台分页查询某场次的预约明细。
     *
     * @param param seckillId、page、limit
     */
    ResponseResult queryPage(Map<String, Object> param);
}
