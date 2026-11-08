package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.LogServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 运行错误告警 BFF 适配器
 *
 * <p>只做请求转发，不包含任何业务逻辑。对外暴露 {@code /web/log/alert/**} 路径，
 * 内部通过 {@link LogServiceFeign} 调用 logging 微服务。</p>
 *
 * <p>🔴 为什么不放在 {@link WebLogController} 里：那个类带
 * {@code @ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")}，
 * 而生产 Nacos 的 bff-web.yaml 并没有开这个开关（OA / CMS / 财务等 legacy 控制器
 * 全部走的是另一套 /web/plugin/** 泛化转发），整块控制器不会被装配 —— 表现就是
 * 告警接口一律「BFF 路由未命中 / 请求的资源不存在」。告警属于新能力且必须常开，
 * 因此单独成类，不带任何条件装配。</p>
 *
 * <p>认证方式：统一依赖 commons 中的 JWT Token 过滤器，不放开匿名访问。</p>
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/log/alert")
public class WebLogAlertController {

    @Autowired
    private LogServiceFeign logServiceFeign;

    /**
     * 运行告警分页查询
     *
     * @param params  查询条件，支持 status/module/errorType/startTime/endTime/keyword
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @PostMapping("/page")
    public ResponseResult queryAlerts(@RequestBody(required = false) Map<String, Object> params,
                                      @RequestParam(defaultValue = "1") Long current,
                                      @RequestParam(defaultValue = "20") Long size) {
        return logServiceFeign.queryAlerts(params == null ? new HashMap<>() : params, current, size);
    }

    /**
     * 未处理告警数量（后台顶栏红点）
     *
     * @return 未处理条数
     */
    @GetMapping("/unread-count")
    public ResponseResult alertUnreadCount() {
        return logServiceFeign.alertUnreadCount();
    }

    /**
     * 运行告警详情（含最近一次堆栈）
     *
     * @param id 告警ID
     * @return 告警详情
     */
    @GetMapping("/{id}")
    public ResponseResult alertDetail(@PathVariable Long id) {
        return logServiceFeign.alertDetail(id);
    }

    /**
     * 标记告警为已处理
     *
     * @param id     告警ID
     * @param remark 处理备注（修复原因）
     * @return 操作结果
     */
    @PostMapping("/{id}/handle")
    public ResponseResult handleAlert(@PathVariable Long id,
                                      @RequestParam(required = false) String remark) {
        return logServiceFeign.handleAlert(id, remark);
    }

    /**
     * 标记告警为已忽略
     *
     * @param id     告警ID
     * @param remark 忽略原因
     * @return 操作结果
     */
    @PostMapping("/{id}/ignore")
    public ResponseResult ignoreAlert(@PathVariable Long id,
                                      @RequestParam(required = false) String remark) {
        return logServiceFeign.ignoreAlert(id, remark);
    }
}
