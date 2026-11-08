package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.FinishTimeSecondkillDTO;
import com.houjiang.activity.dto.ManageSeckillBaseQueryDTO;
import com.houjiang.activity.dto.SecondKillGoodsDTO;
import com.houjiang.activity.entity.ActivitySeckillBaseEntity;
import com.houjiang.commons.dto.goods.ManageSeckillBaseUpdateDTO;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName ActivitySeckillBaseService
 * @Description  限时秒杀活动表
 * @Author Amos
 * @Date 2023-07-04 17:26:46
 * @Version 1.0
 * @author Amos
 */
public interface ActivitySeckillBaseService extends IService<ActivitySeckillBaseEntity> {

    /**
     * 组件化装修 限时秒杀活动列表
     * @param param
     * @return
     */
    ResponseResult getList(FinishTimeSecondkillDTO param);


    /**
     * 组件化装修 限时秒杀活动列表
     * @param param
     * @return
     */
    ResponseResult getByPage(FinishTimeSecondkillDTO param);

    /**
     * 限时秒杀活动删除
     * @param param
     * @return
     */
    ResponseResult deleteData(SecondKillGoodsDTO param);

    /**
     * 查询店铺秒杀活动详情
     * @param param
     * @return
     */
    ResponseResult queryShopSeckillBase(ManageSeckillBaseQueryDTO param);

    /**
     * 删除店铺秒杀活动详情
     *
     * @param param
     * @return
     */
    ResponseResult deleteShopSeckillBase(Map<String,Object> param);


    /**
     * 复制限时秒杀活动
     *
     * @param param
     * @return
     */
    ResponseResult queryShopSeckilCopy(Map<String, Object> param);



    /**
     * 查询店铺秒杀活动列表
     *
     * @param param
     * @return
     */
   ResponseResult queryShopSeckillBaseList(ManageSeckillBaseUpdateDTO param) throws IllegalAccessException;


    /**
     * 创建或更新店铺秒杀活动
     *
     * @param param
     * @return
     */
    ResponseResult addShopSeckillBase(ManageSeckillBaseUpdateDTO param);


}
