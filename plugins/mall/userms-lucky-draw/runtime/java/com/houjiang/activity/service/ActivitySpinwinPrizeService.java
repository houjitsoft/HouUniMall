package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.entity.ActivitySpinwinPrizeEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface ActivitySpinwinPrizeService extends IService<ActivitySpinwinPrizeEntity> {

    /**
     * 更新抽奖限制
     *
     * @param param
     * @return
     */
    ResponseResult deleteSpinwinPrize(Map<String, Object> param);


    /**
     * 增加或更新幸运大转盘奖项
     *
     * @param param
     * @return
     */
    ResponseResult updateSpinwinPrize(Map<String, Object> param);


    /**
     * 查询幸运大转盘奖项列表
     *
     * @param param
     * @return
     */
    ResponseResult querySpinwinPrizeList(Map<String, Object> param);


}
