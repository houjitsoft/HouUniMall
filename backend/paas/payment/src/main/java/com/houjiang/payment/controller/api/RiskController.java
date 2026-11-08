package com.houjiang.payment.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.risk.BlacklistAddRequest;
import com.houjiang.payment.dto.risk.BlacklistListRequest;
import com.houjiang.payment.dto.risk.RiskCheckRequest;
import com.houjiang.payment.dto.risk.RiskCheckResponse;
import com.houjiang.payment.dto.risk.RiskLogAuditRequest;
import com.houjiang.payment.dto.risk.RiskLogListRequest;
import com.houjiang.payment.dto.risk.RiskRuleCreateRequest;
import com.houjiang.payment.dto.risk.RiskRuleListRequest;
import com.houjiang.payment.dto.risk.RiskRuleUpdateRequest;
import com.houjiang.payment.entity.PayRiskBlacklistEntity;
import com.houjiang.payment.entity.PayRiskLogEntity;
import com.houjiang.payment.entity.PayRiskRuleEntity;
import com.houjiang.payment.exception.PaymentException;
import com.houjiang.payment.service.api.RiskBlacklistService;
import com.houjiang.payment.service.api.RiskCheckService;
import com.houjiang.payment.service.api.RiskLogService;
import com.houjiang.payment.service.api.RiskRuleService;
import com.houjiang.payment.util.ApiTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付风控管理 Controller（W2 P0 任务）
 *
 * 对标通企付 / 支付宝 / 微信支付 的风控能力
 * 接口清单（12 个）：
 *   规则：/rule/create /rule/update /rule/changeStatus /rule/delete /rule/list /rule/detail
 *   日志：/log/list /log/detail /log/audit
 *   名单：/blacklist/add /blacklist/list /blacklist/remove
 *   评估：/check （核心风控引擎）
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskRuleService ruleService;
    private final RiskLogService logService;
    private final RiskBlacklistService blacklistService;
    private final RiskCheckService checkService;

    // ============================ 规则 ============================

    /** 1. 创建规则 */
    @PostMapping("/rule/create")
    public ResponseResult<PayRiskRuleEntity> ruleCreate(@Valid @RequestBody RiskRuleCreateRequest req,
                                                        HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(ruleService.create(req, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 2. 更新规则 */
    @PostMapping("/rule/update")
    public ResponseResult<Boolean> ruleUpdate(@Valid @RequestBody RiskRuleUpdateRequest req,
                                              HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(ruleService.update(req, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 3. 变更规则启用/禁用 */
    @PostMapping("/rule/changeStatus")
    public ResponseResult<Boolean> ruleChangeStatus(@RequestBody Map<String, Object> body,
                                                    HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        String ruleNo = (String) body.get("ruleNo");
        Integer status = body.get("status") == null ? null : Integer.valueOf(body.get("status").toString());
        try {
            return ResponseResult.success(ruleService.changeStatus(ruleNo, status, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 4. 删除规则 */
    @PostMapping("/rule/delete")
    public ResponseResult<Boolean> ruleDelete(@RequestBody Map<String, Object> body,
                                              HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        String ruleNo = (String) body.get("ruleNo");
        try {
            return ResponseResult.success(ruleService.delete(ruleNo, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 5. 规则分页 */
    @PostMapping("/rule/list")
    public ResponseResult<IPage<PayRiskRuleEntity>> ruleList(@RequestBody(required = false) RiskRuleListRequest req) {
        if (req == null) req = new RiskRuleListRequest();
        return ResponseResult.success(ruleService.list(req));
    }

    /** 6. 规则详情 */
    @PostMapping("/rule/detail")
    public ResponseResult<PayRiskRuleEntity> ruleDetail(@RequestBody Map<String, Object> body) {
        String ruleNo = (String) body.get("ruleNo");
        if (!StringUtils.hasText(ruleNo)) return ResponseResult.fail(500, "ruleNo 不能为空");
        return ResponseResult.success(ruleService.detail(ruleNo));
    }

    // ============================ 日志 ============================

    /** 7. 日志分页 */
    @PostMapping("/log/list")
    public ResponseResult<IPage<PayRiskLogEntity>> logList(@RequestBody(required = false) RiskLogListRequest req) {
        if (req == null) req = new RiskLogListRequest();
        return ResponseResult.success(logService.list(req));
    }

    /** 8. 日志详情 */
    @PostMapping("/log/detail")
    public ResponseResult<PayRiskLogEntity> logDetail(@RequestBody Map<String, Object> body) {
        String logNo = (String) body.get("logNo");
        if (!StringUtils.hasText(logNo)) return ResponseResult.fail(500, "logNo 不能为空");
        return ResponseResult.success(logService.detail(logNo));
    }

    /** 9. 日志审核 */
    @PostMapping("/log/audit")
    public ResponseResult<Boolean> logAudit(@Valid @RequestBody RiskLogAuditRequest req,
                                            HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(logService.audit(req, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 9.1 W4 P0 提交人工审核（reviewStatus=0 待审） */
    @PostMapping("/log/submitReview")
    public ResponseResult<Boolean> logSubmitReview(@RequestBody Map<String, Object> body,
                                                   HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(logService.submitReview(body, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 9.2 W4 P0 人工审核通过/驳回（reviewStatus=1/2） */
    @PostMapping("/log/approve")
    public ResponseResult<Boolean> logApprove(@RequestBody Map<String, Object> body,
                                              HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(logService.approve(body, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    // ============================ 黑白名单 ============================

    /** 10. 名单新增 */
    @PostMapping("/blacklist/add")
    public ResponseResult<PayRiskBlacklistEntity> blacklistAdd(@Valid @RequestBody BlacklistAddRequest req,
                                                               HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        try {
            return ResponseResult.success(blacklistService.add(req, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    /** 11. 名单分页 */
    @PostMapping("/blacklist/list")
    public ResponseResult<IPage<PayRiskBlacklistEntity>> blacklistList(@RequestBody(required = false) BlacklistListRequest req) {
        if (req == null) req = new BlacklistListRequest();
        return ResponseResult.success(blacklistService.list(req));
    }

    /** 12. 名单移除 */
    @PostMapping("/blacklist/remove")
    public ResponseResult<Boolean> blacklistRemove(@RequestBody Map<String, Object> body,
                                                   HttpServletRequest http) {
        String uid = resolveUid(http);
        if (uid == null) return ResponseResult.fail(401, "用户未登录");
        Long id = body.get("id") == null ? null : Long.valueOf(body.get("id").toString());
        try {
            return ResponseResult.success(blacklistService.remove(id, uid));
        } catch (PaymentException ex) {
            return ResponseResult.fail(500, ex.getMessage());
        }
    }

    // ============================ 风控引擎 ============================

    /** 13. 风控评估（核心） */
    @PostMapping("/check")
    public ResponseResult<RiskCheckResponse> check(@Valid @RequestBody RiskCheckRequest req) {
        try {
            return ResponseResult.success(checkService.check(req));
        } catch (Exception ex) {
            log.error("[risk/check] error", ex);
            return ResponseResult.fail(500, "风控评估异常: " + ex.getMessage());
        }
    }

    /** ================================================================ */

    private String resolveUid(HttpServletRequest http) {
        String uid = ApiTokenUtils.resolveUserId(http);
        return StringUtils.hasText(uid) ? uid : null;
    }
}
