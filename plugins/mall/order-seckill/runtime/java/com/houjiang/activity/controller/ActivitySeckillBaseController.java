package com.houjiang.activity.controller;


import com.houjiang.activity.support.ActivitySupport;
import com.houjiang.activity.service.ActivitySeckillBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import com.houjiang.activity.dto.FinishTimeSecondkillDTO;
import com.houjiang.activity.dto.ManageSeckillBaseQueryDTO;
import com.houjiang.activity.dto.SecondKillGoodsDTO;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.dto.goods.ManageSeckillBaseUpdateDTO;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ActivitySeckillBaseController
 * @Description  限时秒杀活动表
 * @Author Amos
 * @Date 2023-07-04 17:26:45
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("SeckillBase")
public class ActivitySeckillBaseController{
    @Autowired
    protected ActivitySeckillBaseService activitySeckillBaseService;


    /**
     * 组件化装修 限时秒杀活动列表
     * @param param
     * @return
     */
    @PostMapping("/get/list")
    public ResponseResult getList(@RequestBody(required = false) FinishTimeSecondkillDTO param) {
        ResponseResult result = activitySeckillBaseService.getList(param);
        return result;
    }

    /**
     * 组件化装修 限时秒杀活动列表
     * @param param
     * @return
     */
    @PostMapping("/get/ByPage")
    public ResponseResult getByPage(@RequestBody(required = false) FinishTimeSecondkillDTO param) {
        return activitySeckillBaseService.getByPage(param);
    }


    /**
     * 限时秒杀活动删除
     * @param param
     * @return
     */
    @PostMapping("/delete/data")
    public ResponseResult deleteData(@RequestBody(required = false) SecondKillGoodsDTO param) {
        ResponseResult result=activitySeckillBaseService.deleteData(param);
        return result;
    }


    /**
     * 删除店铺秒杀活动详情
     *
     * @param param
     * @return
     */
    @PostMapping("/delete")
    public ResponseResult deleteShopSeckillBase(@RequestBody Map<String,Object> param) {
        ResponseResult result=activitySeckillBaseService.deleteShopSeckillBase(param);
        return result;
    }

    /**
     * 复制限时秒杀活动
     *
     * @param param
     * @return
     */
    @PostMapping("/copy")
    public ResponseResult queryShopSeckilCopy(@RequestBody Map<String, Object> param) {
        ResponseResult result=activitySeckillBaseService.queryShopSeckilCopy(param);
        return result;
    }


    /**
     * 查询店铺秒杀活动列表
     *
     * @param param
     * @return
     */
    @PostMapping("/query/list")
    public ResponseResult queryShopSeckillBaseList(@RequestBody ManageSeckillBaseUpdateDTO param) throws IllegalAccessException {
        ResponseResult result=activitySeckillBaseService.queryShopSeckillBaseList(param);
        return result;
    }

    /**
     * 查询店铺秒杀活动详情
     *
     * @param param
     * @return
     */
    @PostMapping("/base/query")
    public ResponseResult queryShopSeckillBase(@RequestBody ManageSeckillBaseQueryDTO param) {
        ResponseResult result = activitySeckillBaseService.queryShopSeckillBase(param);
        return result;
    }



    /**
     * 创建或更新店铺秒杀活动
     *
     * @param param
     * @return
     */
    @SensitiveCheck(field = {"seckillName"})
    @PostMapping("/add")
    public ResponseResult addShopSeckillBase(@RequestBody ManageSeckillBaseUpdateDTO param) {
        ResponseResult result = activitySeckillBaseService.addShopSeckillBase(param);
        return result;
    }
}
