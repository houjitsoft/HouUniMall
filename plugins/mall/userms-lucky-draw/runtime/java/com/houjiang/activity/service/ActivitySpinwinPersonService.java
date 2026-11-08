package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.entity.ActivitySpinwinPersonEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface ActivitySpinwinPersonService extends IService<ActivitySpinwinPersonEntity> {

    /**
     * 查询参与幸运大转盘活动的用户列表
     *
     * @param param
     * @return
     */
    ResponseResult querySpinwinPersonList(Map<String, Object> param);

    /**
     * 查询获奖记录
     * @param param
     * @return
     */
    ResponseResult queryPrizeRecord(ActivityExplainParamDTO param);
}
