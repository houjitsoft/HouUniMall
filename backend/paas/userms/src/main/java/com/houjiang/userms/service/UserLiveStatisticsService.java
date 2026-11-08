package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserLiveStatisticsEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface UserLiveStatisticsService extends IService<UserLiveStatisticsEntity> {


    /**
     * 更新用户日活跃
     * @param map
     * @return
     */
    ResponseResult saveLiveStatistics(Map<String, Object> map);


    /**
     * 查询各个端口日活详情
     * @param param
     * @return
     */
    ResponseResult queryUserNikkatsuDetailPort(Map<String, Object> param);


    /**
     * 查询各个端口日活
     * @param param
     * @return
     */
    ResponseResult queryUserNikkatsuPort(Map<String, Object> param);
}
