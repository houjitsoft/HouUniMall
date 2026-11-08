package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.entity.ActivitySpinwinGiftEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface ActivitySpinwinGiftService extends IService<ActivitySpinwinGiftEntity> {

    /**
     * 删除活动礼品
     *
     * @param param
     * @return
     */
    ResponseResult deleteSpinwinGift( Map<String, Object> param);

    /**
     * 增加或更新幸运大转盘活动
     *
     * @param param
     * @return
     */
   ResponseResult updateSpinwinGift(Map<String, Object> param);

    /**
     * 查询幸运大转盘活动列表
     *
     * @param param
     * @return
     */
    ResponseResult querySpinwinGiftList(Map<String, Object> param);

    /**
     * 查询奖品
     * @param param
     * @return
     */
    ResponseResult querySpinWinGift(ActivityExplainParamDTO param);
}
