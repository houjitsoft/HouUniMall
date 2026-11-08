package com.houjiang.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.activity.entity.ActivitySpinwinLuckyDrawTimesEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @author Amos
 */
public interface ActivitySpinwinLuckyDrawTimesService extends IService<ActivitySpinwinLuckyDrawTimesEntity> {

    /**
     * 查询参与幸运大转盘活动的用户抽奖次数列表
     *
     * @param param
     * @return
     */

    /**
     * TODO 补充方法说明
     * @param param TODO
     * @return TODO
     */
    ResponseResult queryLuckyDrawTimesList(Map<String, Object> param);
}
