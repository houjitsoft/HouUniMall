package com.houjiang.mall.controller.market;

import com.houjiang.commons.util.PluginManageGate;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.mall.entity.market.PluginDeveloper;
import com.houjiang.mall.service.market.PluginDeveloperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件开发者中心（{@code /base/plugin-center/developer/**}，对外经 BFF
 * {@code /web/plugin-center/developer/**} 暴露）。
 *
 * <p>端点分两类：
 * <ul>
 *   <li><b>自助</b>（{@code /apply}、{@code /mine}、{@code /account/save}）：
 *       当前登录用户操作自己的开发者身份，不需要插件管理权限；</li>
 *   <li><b>运营</b>（{@code /list}、{@code /review}、{@code /freeze}）：
 *       需要插件管理权限（{@link PluginManageGate}）。</li>
 * </ul>
 *
 * @author Amos
 * @since 2026-10-27
 */
@Slf4j
@RestController
@RequestMapping("/base/plugin-center/developer")
public class PluginDeveloperController {

    private final PluginDeveloperService developerService;

    @Value("${houcloud.plugin.require-manage-authority:true}")
    private boolean requireManageAuthority;

    public PluginDeveloperController(PluginDeveloperService developerService) {
        this.developerService = developerService;
    }

    /** 提交或更新开发者申请（自助） */
    @PostMapping("/apply")
    public ResponseResult apply(@RequestBody Map<String, Object> params) {
        try {
            Long userId = currentUserId();
            if (userId == null) {
                return ResponseResult.fail(401, "未登录，无法提交开发者申请");
            }
            PluginDeveloper developer = developerService.apply(userId, params);
            return ResponseResult.success(developerService.toView(developer));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("提交开发者申请失败", e);
            return ResponseResult.fail(500, "提交开发者申请失败: " + e.getMessage());
        }
    }

    /** 我的开发者身份 + 收款账户（自助） */
    @PostMapping("/mine")
    public ResponseResult mine() {
        try {
            Long userId = currentUserId();
            if (userId == null) {
                return ResponseResult.fail(401, "未登录");
            }
            PluginDeveloper developer = developerService.findByUser(userId);
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("developer", developerService.toView(developer));
            view.put("account", developer == null
                    ? Map.of()
                    : developerService.toAccountView(developerService.findAccount(developer.getId())));
            return ResponseResult.success(view);
        } catch (Exception e) {
            log.error("查询开发者身份失败", e);
            return ResponseResult.fail(500, "查询开发者身份失败: " + e.getMessage());
        }
    }

    /** 保存收款账户（自助） */
    @PostMapping("/account/save")
    public ResponseResult saveAccount(@RequestBody Map<String, Object> params) {
        try {
            Long userId = currentUserId();
            if (userId == null) {
                return ResponseResult.fail(401, "未登录");
            }
            PluginDeveloper developer = developerService.findByUser(userId);
            if (developer == null) {
                return ResponseResult.fail(400, "请先提交开发者申请，再填写收款账户");
            }
            return ResponseResult.success(
                    developerService.toAccountView(developerService.saveAccount(developer.getId(), params)));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("保存收款账户失败", e);
            return ResponseResult.fail(500, "保存收款账户失败: " + e.getMessage());
        }
    }

    /** 开发者列表（运营） */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> params) {
        ResponseResult denied = PluginManageGate.check(requireManageAuthority);
        if (denied != null) {
            return denied;
        }
        try {
            Integer status = params == null ? null : intOf(params.get("status"));
            List<Map<String, Object>> items = developerService.list(status).stream()
                    .map(developerService::toView)
                    .toList();
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("list", items);
            view.put("total", items.size());
            return ResponseResult.success(view);
        } catch (Exception e) {
            log.error("查询开发者列表失败", e);
            return ResponseResult.fail(500, "查询开发者列表失败: " + e.getMessage());
        }
    }

    /** 审核（运营）：approve=true 通过，false 驳回（原因必填） */
    @PostMapping("/review")
    public ResponseResult review(@RequestBody Map<String, Object> params) {
        ResponseResult denied = PluginManageGate.check(requireManageAuthority);
        if (denied != null) {
            return denied;
        }
        try {
            Long id = longOf(params.get("id"));
            if (id == null) {
                return ResponseResult.fail(400, "缺少开发者 ID");
            }
            boolean approve = Boolean.parseBoolean(String.valueOf(params.getOrDefault("approve", "true")));
            String note = params.get("note") == null ? "" : String.valueOf(params.get("note"));
            return ResponseResult.success(
                    developerService.toView(developerService.review(id, approve, note, currentUserId())));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("审核开发者失败", e);
            return ResponseResult.fail(500, "审核开发者失败: " + e.getMessage());
        }
    }

    /** 冻结 / 解冻（运营） */
    @PostMapping("/freeze")
    public ResponseResult freeze(@RequestBody Map<String, Object> params) {
        ResponseResult denied = PluginManageGate.check(requireManageAuthority);
        if (denied != null) {
            return denied;
        }
        try {
            Long id = longOf(params.get("id"));
            if (id == null) {
                return ResponseResult.fail(400, "缺少开发者 ID");
            }
            boolean frozen = Boolean.parseBoolean(String.valueOf(params.getOrDefault("frozen", "true")));
            String note = params.get("note") == null ? "" : String.valueOf(params.get("note"));
            return ResponseResult.success(developerService.toView(developerService.freeze(id, frozen, note)));
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("变更开发者状态失败", e);
            return ResponseResult.fail(500, "变更开发者状态失败: " + e.getMessage());
        }
    }

    /** 当前登录用户 ID（解析不出返回 null，由调用方按未登录处理） */
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
        if (value == null || "".equals(String.valueOf(value).trim())) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
