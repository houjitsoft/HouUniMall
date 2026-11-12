package com.houjiang.userms.service;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.BehaviorAnalyticsQueryDTO;
import com.houjiang.userms.dto.BehaviorTrackDTO;

/**
 * 用户行为与留存分析
 *
 * <p>行为 / 留存 / 活跃 / 转化四维度，全部按所选时间范围从真实库表实时聚合。
 * 统一埋点事件表承接搜索 / 加购 / 分享等无源事件，登录 / 浏览 / 下单沿用既有真实表；
 * 未采集的维度由 dataQuality 显式标记，不补 0、不编数。</p>
 *
 * @author Amos
 */
public interface BehaviorAnalyticsService {

    /** 四维度分析（趋势图 + 表格数据源） */
    ResponseResult queryAnalytics(BehaviorAnalyticsQueryDTO dto);

    /** 统一埋点上报 */
    ResponseResult track(BehaviorTrackDTO dto);
}
