package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.dto.ActivityExplainParamDTO;
import com.houjiang.activity.entity.ActivitySpinwinEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface ActivitySpinwinService extends IService<ActivitySpinwinEntity> {

    /**
     * 更新抽奖限制
     *
     * @param param
     * @return
     */
    ResponseResult updateSpinwinLimit(Map<String, Object> param);

    /**
     * 查询抽奖限制
     *
     * @param param
     * @return
     */
    ResponseResult querySpinwinLimit(Map<String, Object> param);


    /**
     * 删除幸运大转盘活动
     *
     * @param param
     * @return
     */
   ResponseResult deleteSpinwinActivity(Map<String, Object> param);

    /**
     * 查询幸运大转盘活动详情
     *
     * @param param
     * @return
     */
   ResponseResult querySpinwinActivityDetail(Map<String, Object> param);

    /**
     * 查询幸运大转盘活动列表
     *
     * @param param
     * @return
     */
    ResponseResult querySpinwinActivityList(Map<String, Object> param);

    /**
     * 增加或更新幸运大转盘活动
     *
     * @param param
     * @return
     */
    ResponseResult updateSpinwinActivity(Map<String, Object> param);

    /**
     * 查询用户可抽奖次数和总的参与人数
     * @param param
     * @return
     */
    ResponseResult querySpinWinDrawTimes(ActivityExplainParamDTO param);

    /**
     * 查询活动说明
     * @param param
     * @return
     */
    ResponseResult queryActivityExplain(ActivityExplainParamDTO param);

    /**
     * 用户抽奖
     * @param param
     * @return
     */
    ResponseResult updateLotteryDraw(ActivityExplainParamDTO param);
}
