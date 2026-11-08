package com.houjiang.mall.controller.market;

import com.houjiang.commons.util.PluginManageGate;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.mall.entity.market.PluginDeveloper;
import com.houjiang.mall.entity.market.PluginSettlementBatch;
import com.houjiang.mall.service.market.PluginDeveloperService;
import com.houjiang.mall.service.market.PluginSettlementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件结算 / 分账（{@code /base/plugin-center/settlement/**}）。
 *
 * <p><b>权限口径</b>：
 * <ul>
 *   <li>开发者只能看自己的流水与批次（{@code developerId} 由登录态反查，忽略请求体）；</li>
 *   <li>运营（插件管理权限）可以按任意 developerId 查询、生成批次、标记打款。</li>
 * </ul>
 * 这条区分很关键：分账数据是开发者的收入明细，横向看别人的收入是越权。
 *
 * @author Amos
 * @since 2026-10-27
 */
@Slf4j
@RestController
@RequestMapping("/base/plugin-center/settlement")
public class PluginSettlementController {

    private final PluginSettlementService settlementService;
    private final PluginDeveloperService developerService;

    @Value("${houcloud.plugin.require-manage-authority:true}")
    private boolean requireManageAuthority;

    public PluginSettlementController(PluginSettlementService settlementService,
                                      PluginDeveloperService developerService) {
        this.settlementService = settlementService;
        this.developerService = developerService;
    }

    /** 我的收入汇总（自助） */
    @PostMapping("/summary")
    public ResponseResult summary(@RequestBody(required = false) Map<String, Object> params) {
        try {
            Long developerId = resolveDeveloperId(params);
            if (developerId == null) {
                return ResponseResult.fail(400, "尚未登记开发者身份，暂无结算数据");
            }
            Map<String, Object> view = new LinkedHashMap<>(settlementService.summary(developerId));
            view.put("developerId", developerId);
            return ResponseResult.success(view);
        } catch (Exception e) {
            log.error("查询结算汇总失败", e);
            return ResponseResult.fail(500, "查询结算汇总失败: " + e.getMessage());
        }
    }

    /** 分账流水（自助 / 运营） */
    @PostMapping("/shares")
    public ResponseResult shares(@RequestBody(required = false) Map<String, Object> params) {
        try {
            Long developerId = resolveDeveloperId(params);
            if (developerId == null) {
                return ResponseResult.fail(400, "尚未登记开发者身份，暂无分账流水");
            }
            Integer status = intOf(params == null ? null : params.get("status"));
            int limit = params == null || params.get("limit") == null ? 100 : intOf(params.get("limit"), 100);
            List<Map<String, Object>> list = settlementService.listShares(developerId, status, limit);
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("list", list);
            view.put("total", list.size());
            view.put("summary", settlementService.summary(developerId));
            return ResponseResult.success(view);
        } catch (Exception e) {
            log.error("查询分账流水失败", e);
            return ResponseResult.fail(500, "查询分账流水失败: " + e.getMessage());
        }
    }

    /** 结算批次列表（自助 / 运营） */
    @PostMapping("/batch/list")
    public ResponseResult listBatches(@RequestBody(required = false) Map<String, Object> params) {
        try {
            Long developerId = resolveDeveloperId(params);
            Integer status = intOf(params == null ? null : params.get("status"));
            List<PluginSettlementBatch> batches = settlementService.listBatches(developerId, status);
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("list", batches);
            view.put("total", batches.size());
            return ResponseResult.success(view);
        } catch (Exception e) {
            log.error("查询结算批次失败", e);
            return ResponseResult.fail(500, "查询结算批次失败: " + e.getMessage());
        }
    }

    /** 生成结算批次（运营） */
    @PostMapping("/batch/create")
    public ResponseResult createBatch(@RequestBody Map<String, Object> params) {
        ResponseResult denied = PluginManageGate.check(requireManageAuthority);
        if (denied != null) {
            return denied;
        }
        try {
            Long developerId = longOf(params.get("developerId"));
            if (developerId == null) {
                return ResponseResult.fail(400, "缺少 developerId");
            }
            LocalDate from = dateOf(params.get("from"));
            LocalDate to = dateOf(params.get("to"));
            String remark = params.get("remark") == null ? "" : String.valueOf(params.get("remark"));
            return ResponseResult.success(settlementService.settle(developerId, from, to, remark));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("生成结算批次失败", e);
            return ResponseResult.fail(500, "生成结算批次失败: " + e.getMessage());
        }
    }

    /** 标记打款（运营） */
    @PostMapping("/batch/paid")
    public ResponseResult markPaid(@RequestBody Map<String, Object> params) {
        ResponseResult denied = PluginManageGate.check(requireManageAuthority);
        if (denied != null) {
            return denied;
        }
        try {
            Long id = longOf(params.get("id"));
            if (id == null) {
                return ResponseResult.fail(400, "缺少批次 ID");
            }
            String voucherUrl = params.get("voucherUrl") == null ? "" : String.valueOf(params.get("voucherUrl"));
            return ResponseResult.success(settlementService.markPaid(id, voucherUrl));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("标记打款失败", e);
            return ResponseResult.fail(500, "标记打款失败: " + e.getMessage());
        }
    }

    /** 驳回单笔流水（运营） */
    @PostMapping("/share/reject")
    public ResponseResult rejectShare(@RequestBody Map<String, Object> params) {
        ResponseResult denied = PluginManageGate.check(requireManageAuthority);
        if (denied != null) {
            return denied;
        }
        try {
            Long id = longOf(params.get("id"));
            if (id == null) {
                return ResponseResult.fail(400, "缺少流水 ID");
            }
            String remark = params.get("remark") == null ? "" : String.valueOf(params.get("remark"));
            return ResponseResult.success(settlementService.rejectShare(id, remark));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("驳回分账流水失败", e);
            return ResponseResult.fail(500, "驳回分账流水失败: " + e.getMessage());
        }
    }

    /**
     * 解析要查询的 developerId：
     * 有插件管理权限时允许按请求体的 developerId 查（运营对账）；
     * 否则一律用登录态反查自己的开发者身份——<b>忽略请求体</b>，避免横向越权看别人收入。
     */
    private Long resolveDeveloperId(Map<String, Object> params) {
        boolean operator = !requireManageAuthority || PluginManageGate.hasAuthority();
        Long requested = params == null ? null : longOf(params.get("developerId"));
        if (operator && requested != null) {
            return requested;
        }
        Long userId = currentUserId();
        if (userId == null) {
            return null;
        }
        PluginDeveloper developer = developerService.findByUser(userId);
        return developer == null ? null : developer.getId();
    }

    private Long currentUserId() {
        var user = SecurityContextUtils.getCurrentUserOrNull();
        if (user == null || user.getUserId() == null) {
            return null;
        }
        try {
            return Long.valueOf(user.getUserId().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long longOf(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer intOf(Object value) {
        return intOf(value, null);
    }

    private static Integer intOf(Object value, Integer fallback) {
        if (value == null || "".equals(String.valueOf(value).trim())) {
            return fallback;
        }
        try {
            return Integer.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static LocalDate dateOf(Object value) {
        if (value == null || "".equals(String.valueOf(value).trim())) {
            return null;
        }
        try {
            return LocalDate.parse(String.valueOf(value).trim());
        } catch (RuntimeException e) {
            return null;
        }
    }
}
