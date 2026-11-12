package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.UserServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户行为与留存分析 BFF 适配器
 *
 * <p>只做转发：{@code /web/member/behavior/**} → userms {@code /user/ms/behavior/**}。
 * 与会员画像一致，刻意不加 {@code @ConditionalOnProperty(houcloud.legacy-bff.enabled)}，
 * 生产 Nacos 未开启该开关时带条件的控制器不会被装配。</p>
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/member/behavior")
public class WebBehaviorAnalyticsController {

    @Autowired
    private UserServiceFeign userServiceFeign;

    /** 四维度分析：行为 / 留存 / 活跃 / 转化 + 趋势图与表格数据 */
    @PostMapping("/analytics")
    public ResponseResult analytics(@RequestBody(required = false) Map<String, Object> params) {
        return userServiceFeign.memberBehaviorAnalytics(params == null ? new HashMap<>() : params);
    }

    /** 统一埋点上报 */
    @PostMapping("/track")
    public ResponseResult track(@RequestBody(required = false) Map<String, Object> params) {
        return userServiceFeign.memberBehaviorTrack(params == null ? new HashMap<>() : params);
    }
}
