package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.dto.BehaviorAnalyticsQueryDTO;
import com.houjiang.userms.dto.BehaviorTrackDTO;
import com.houjiang.userms.service.BehaviorAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户行为与留存分析（真实数据）
 *
 * <p>行为 / 留存 / 活跃 / 转化四维度按时间范围实时聚合，另提供统一埋点上报入口。
 * 未采集维度由 dataQuality 显式标记，不做模拟与补数。</p>
 *
 * @author Amos
 */
@RestController
@RequestMapping("user/ms/behavior")
public class BehaviorAnalyticsController {

    @Autowired
    private BehaviorAnalyticsService behaviorAnalyticsService;

    /**
     * 四维度分析
     *
     * @param dto {"startDate":"2026-10-13","endDate":"2026-11-12"}，可空（默认近 30 天）
     */
    @PostMapping("/analytics")
    public ResponseResult analytics(@RequestBody(required = false) BehaviorAnalyticsQueryDTO dto) {
        return behaviorAnalyticsService.queryAnalytics(dto);
    }

    /**
     * 统一埋点上报
     *
     * @param dto userId / eventType / eventTime / source / refId / ext
     */
    @PostMapping("/track")
    public ResponseResult track(@RequestBody BehaviorTrackDTO dto) {
        return behaviorAnalyticsService.track(dto);
    }
}
