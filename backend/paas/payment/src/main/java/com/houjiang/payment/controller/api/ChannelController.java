package com.houjiang.payment.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.channel.ChannelListRequest;
import com.houjiang.payment.dto.channel.ChannelLogListRequest;
import com.houjiang.payment.dto.channel.ChannelResultReport;
import com.houjiang.payment.dto.channel.ChannelSaveRequest;
import com.houjiang.payment.dto.channel.CompensateListRequest;
import com.houjiang.payment.dto.channel.CompensateRegisterRequest;
import com.houjiang.payment.dto.channel.RouteListRequest;
import com.houjiang.payment.dto.channel.RouteSaveRequest;
import com.houjiang.payment.dto.channel.RouteSelectRequest;
import com.houjiang.payment.dto.channel.RouteSelectResponse;
import com.houjiang.payment.entity.PayChannelCompensateEntity;
import com.houjiang.payment.entity.PayChannelEntity;
import com.houjiang.payment.entity.PayChannelLogEntity;
import com.houjiang.payment.entity.PayChannelRouteEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.api.ChannelRouteEngineService;
import com.houjiang.payment.service.api.ChannelRouteService;
import com.houjiang.payment.service.api.ChannelService;
import com.houjiang.payment.service.api.CompensateService;
import com.houjiang.payment.util.ApiTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付渠道路由管理 Controller（W3 P0 任务）
 *
 * 对标通企付 / 支付宝 / 微信支付 的渠道路由 + 降级 + 熔断 + 补偿能力
 * 接口清单（20 个）：
 *   渠道配置：/channel/save /channel/changeStatus /channel/delete /channel/list /channel/detail /channel/getByCode
 *   路由规则：/route/save /route/changeStatus /route/delete /route/list /route/detail
 *   路由引擎：/route/select /route/report /log/list /log/detail
 *   补偿查询：/compensate/register /compensate/executeOne /compensate/executeDue /compensate/list /compensate/detail /compensate/giveUp
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/channel")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;
    private final ChannelRouteService routeService;
    private final ChannelRouteEngineService engineService;
    private final CompensateService compensateService;

    /** ============================ 渠道配置 ============================ */

    @PostMapping("/channel/save")
    public ResponseResult<Long> channelSave(@Valid @RequestBody ChannelSaveRequest req, HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(channelService.save(req, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    @PostMapping("/channel/changeStatus")
    public ResponseResult<Boolean> channelChangeStatus(@RequestBody Map<String, Object> body, HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        Integer status = body.get("status") == null ? null : Integer.valueOf(body.get("status").toString());
        try {
            return ResponseResult.success(channelService.changeStatus(id, status, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    @PostMapping("/channel/delete")
    public ResponseResult<Boolean> channelDelete(@RequestBody Map<String, Object> body, HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        return ResponseResult.success(channelService.delete(id));
    }

    @PostMapping("/channel/list")
    public ResponseResult<IPage<PayChannelEntity>> channelList(@RequestBody(required = false) ChannelListRequest req) {
        if (req == null) req = new ChannelListRequest();
        return ResponseResult.success(channelService.page(req));
    }

    @PostMapping("/channel/detail")
    public ResponseResult<PayChannelEntity> channelDetail(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        if (id == null) return ResponseResult.fail(500, "id 不能为空");
        return ResponseResult.success(channelService.detail(id));
    }

    @PostMapping("/channel/getByCode")
    public ResponseResult<PayChannelEntity> channelGetByCode(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("channelCode");
        if (!StringUtils.hasText(code)) return ResponseResult.fail(500, "channelCode 不能为空");
        return ResponseResult.success(channelService.getByCode(code));
    }

    // ============================ 凭证管理 (W5 新增) ============================

    /** 凭证列表（脱敏） */
    @PostMapping("/channel/config/list")
    public ResponseResult<IPage<PayChannelEntity>> channelConfigList(@RequestBody(required = false) ChannelListRequest req) {
        if (req == null) req = new ChannelListRequest();
        return ResponseResult.success(channelService.pageDesensitized(req));
    }

    /** 凭证详情（解密返回明文） */
    @PostMapping("/channel/config/get")
    public ResponseResult<Map<String, Object>> channelConfigGet(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        if (id == null) return ResponseResult.fail(500, "id 不能为空");
        try {
            return ResponseResult.success(channelService.getCredentialDecrypted(id));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 保存凭证（自动加密） */
    @PostMapping("/channel/config/save")
    @SuppressWarnings("unchecked")
    public ResponseResult<Long> channelConfigSave(@RequestBody Map<String, Object> body, HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        try {
            ChannelSaveRequest req = new ChannelSaveRequest();
            if (body.get("id") != null) req.setId(Long.valueOf(body.get("id").toString()));
            req.setChannelCode((String) body.get("channelCode"));
            req.setChannelName((String) body.get("channelName"));
            req.setProvider((String) body.get("provider"));
            req.setPayMethod((String) body.get("payMethod"));
            req.setMerchantNo((String) body.get("merchantNo"));
            req.setRemark((String) body.get("remark"));
            if (body.get("status") != null) req.setStatus(Integer.valueOf(body.get("status").toString()));
            Map<String, Object> configMap = body.get("configMap") instanceof Map
                    ? (Map<String, Object>) body.get("configMap")
                    : null;
            return ResponseResult.success(channelService.saveCredential(req, configMap, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 启用/停用 */
    @PostMapping("/channel/config/toggle")
    public ResponseResult<Boolean> channelConfigToggle(@RequestBody Map<String, Object> body, HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        Integer status = body.get("status") == null ? null : Integer.valueOf(body.get("status").toString());
        try {
            return ResponseResult.success(channelService.changeStatus(id, status, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 测试连通：无凭证返回 CHANNEL_NOT_CONFIGURED */
    @PostMapping("/channel/config/testConnect")
    public ResponseResult<Map<String, Object>> channelConfigTestConnect(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        if (id == null) return ResponseResult.fail(500, "id 不能为空");
        try {
            return ResponseResult.success(channelService.testConnect(id));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /**
     * 公开版渠道配置（C 端无需登录鉴权也可读，仅返回启用 + 公开字段）
     * payMethod 可选过滤：WECHAT/ALIPAY/UNIONPAY/...
     */
    @PostMapping("/channel/config/public")
    public ResponseResult<java.util.List<Map<String, Object>>> channelConfigPublic(@RequestBody(required = false) Map<String, Object> body) {
        String payMethod = body == null ? null : (String) body.get("payMethod");
        return ResponseResult.success(channelService.listEnabledPublic(payMethod));
    }

    /** ============================ 路由规则 ============================ */

    @PostMapping("/route/save")
    public ResponseResult<Long> routeSave(@Valid @RequestBody RouteSaveRequest req, HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(routeService.save(req, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    @PostMapping("/route/changeStatus")
    public ResponseResult<Boolean> routeChangeStatus(@RequestBody Map<String, Object> body, HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        Integer status = body.get("status") == null ? null : Integer.valueOf(body.get("status").toString());
        try {
            return ResponseResult.success(routeService.changeStatus(id, status, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    @PostMapping("/route/delete")
    public ResponseResult<Boolean> routeDelete(@RequestBody Map<String, Object> body, HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        return ResponseResult.success(routeService.delete(id));
    }

    @PostMapping("/route/list")
    public ResponseResult<IPage<PayChannelRouteEntity>> routeList(@RequestBody(required = false) RouteListRequest req) {
        if (req == null) req = new RouteListRequest();
        return ResponseResult.success(routeService.page(req));
    }

    @PostMapping("/route/detail")
    public ResponseResult<PayChannelRouteEntity> routeDetail(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        if (id == null) return ResponseResult.fail(500, "id 不能为空");
        return ResponseResult.success(routeService.detail(id));
    }

    // ============================ 路由引擎 ============================

    /** 路由选择（核心） */
    @PostMapping("/route/select")
    public ResponseResult<RouteSelectResponse> routeSelect(@Valid @RequestBody RouteSelectRequest req) {
        try {
            return ResponseResult.success(engineService.select(req));
        } catch (Exception ex) {
            log.error("[channel/route/select] error", ex);
            return ResponseResult.fail(500, "路由选择异常: " + ex.getMessage());
        }
    }

    /**
     * 退款路由选择（W5+ P2-1）
     * <p>与 /route/select 差异：bizType 强制=2，需 originChannelCode，带原路退回硬约束</p>
     */
    @PostMapping("/route/selectRefund")
    public ResponseResult<RouteSelectResponse> routeSelectRefund(@Valid @RequestBody RouteSelectRequest req) {
        try {
            return ResponseResult.success(engineService.selectRefund(req));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        } catch (Exception ex) {
            log.error("[channel/route/selectRefund] error", ex);
            return ResponseResult.fail(500, "退款路由选择异常: " + ex.getMessage());
        }
    }

    /** 上报渠道调用结果 */
    @PostMapping("/route/report")
    public ResponseResult<Boolean> routeReport(@RequestBody ChannelResultReport req) {
        try {
            return ResponseResult.success(engineService.report(req));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    @PostMapping("/log/list")
    public ResponseResult<IPage<PayChannelLogEntity>> logList(@RequestBody(required = false) ChannelLogListRequest req) {
        if (req == null) req = new ChannelLogListRequest();
        return ResponseResult.success(engineService.logPage(req));
    }

    @PostMapping("/log/detail")
    public ResponseResult<PayChannelLogEntity> logDetail(@RequestBody Map<String, Object> body) {
        String logNo = (String) body.get("logNo");
        if (!StringUtils.hasText(logNo)) return ResponseResult.fail(500, "logNo 不能为空");
        return ResponseResult.success(engineService.logDetail(logNo));
    }

    /** ============================ 补偿查询 ============================ */

    @PostMapping("/compensate/register")
    public ResponseResult<String> compensateRegister(@Valid @RequestBody CompensateRegisterRequest req) {
        try {
            return ResponseResult.success(compensateService.register(req));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    @PostMapping("/compensate/executeOne")
    public ResponseResult<Boolean> compensateExecuteOne(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        if (id == null) return ResponseResult.fail(500, "id 不能为空");
        try {
            return ResponseResult.success(compensateService.executeOne(id));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    @PostMapping("/compensate/executeDue")
    public ResponseResult<Integer> compensateExecuteDue(@RequestBody(required = false) Map<String, Object> body) {
        int limit = 50;
        if (body != null && body.get("limit") != null) {
            limit = Integer.parseInt(body.get("limit").toString());
        }
        return ResponseResult.success(compensateService.executeDue(limit));
    }

    @PostMapping("/compensate/list")
    public ResponseResult<IPage<PayChannelCompensateEntity>> compensateList(@RequestBody(required = false) CompensateListRequest req) {
        if (req == null) req = new CompensateListRequest();
        return ResponseResult.success(compensateService.page(req));
    }

    @PostMapping("/compensate/detail")
    public ResponseResult<PayChannelCompensateEntity> compensateDetail(@RequestBody Map<String, Object> body) {
        String taskNo = (String) body.get("taskNo");
        if (!StringUtils.hasText(taskNo)) return ResponseResult.fail(500, "taskNo 不能为空");
        return ResponseResult.success(compensateService.detail(taskNo));
    }

    @PostMapping("/compensate/giveUp")
    public ResponseResult<Boolean> compensateGiveUp(@RequestBody Map<String, Object> body) {
        String taskNo = (String) body.get("taskNo");
        String reason = (String) body.get("reason");
        if (!StringUtils.hasText(taskNo)) return ResponseResult.fail(500, "taskNo 不能为空");
        try {
            return ResponseResult.success(compensateService.giveUp(taskNo, reason));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** ================================================================ */

    private String resolveUid(HttpServletRequest http) {
        String uid = ApiTokenUtils.resolveUserId(http);
        return StringUtils.hasText(uid) ? uid : null;
    }
}
