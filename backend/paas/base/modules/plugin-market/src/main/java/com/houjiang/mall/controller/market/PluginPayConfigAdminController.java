package com.houjiang.mall.controller.market;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.config.market.PluginPayConfigStore;
import com.houjiang.mall.entity.market.MarketPayConfig;
import com.houjiang.mall.mapper.market.MarketPayConfigMapper;
import com.houjiang.mall.pay.plugin.PluginPayChannelRouter;
import com.houjiang.mall.pay.plugin.PluginPaySigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件支付配置管理（{@code /base/plugin-center/market/pay/admin/**}）。
 *
 * <p><b>与 {@code /base/plugin-center/market/pay/config} 的区别</b>：后者是租户侧只读自检（只回「缺什么」），
 * 这里是平台运营侧读写接口，<b>必须带登录态</b>（不在 BFF 鉴权白名单里）。
 *
 * <p><b>密钥红线</b>：{@code api-key} / {@code private-key} 保存时值必须是 {@code ${环境变量名}}，
 * 明文一律拒绝落库并给出明确报错；读取时只回「是否已注入」的布尔量，任何密钥值都不会出现在响应里。
 * 库被拖走最多泄露「用的是哪个环境变量名」，泄露不了商户资金凭据。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/plugin-center/market/pay/admin")
public class PluginPayConfigAdminController {

    private final MarketPayConfigMapper configMapper;
    private final PluginPayConfigStore store;
    private final PluginPayChannelRouter channelRouter;

    /**
     * 运营侧管理令牌：改支付配置等于改「钱从哪个商户号收、什么价」，
     * 只靠「已登录」不够——任何一个登录用户都能把收款商户号改成自己的。
     *
     * <p><b>fail-closed</b>：未配置该令牌时，本控制器<b>全部接口一律 403</b>。
     * 宁可运营先用 SQL 或 Nacos 配，也不留一个「登录即可改收款账户」的口子。
     *
     * <p>配置方式（环境变量注入，禁止进 git）：
     * {@code HOU_PLUGIN_PAY_ADMIN_TOKEN=<随机长串>}。
     * 调用时在请求头 {@code X-Pay-Admin-Token} 或请求体 {@code adminToken} 带上。
     */
    /**
     * 令牌取值：优先读短名 {@code HOU_PLUGIN_PAY_ADMIN_TOKEN}（运维文档里一直写这个），
     * 再回落标准松弛绑定名 {@code houcloud.plugin.market.pay.admin-token}
     * （对应环境变量 {@code HOUCLOUD_PLUGIN_MARKET_PAY_ADMIN_TOKEN}）。
     *
     * <p>只写后者时，按运维文档注入短名会静默失效、接口全线 403（2026-09-19 实测踩到），
     * 因此这里显式兼容两个名字，避免「配了却没生效」。
     */
    @org.springframework.beans.factory.annotation.Value(
            "${HOU_PLUGIN_PAY_ADMIN_TOKEN:${houcloud.plugin.market.pay.admin-token:}}")
    private String adminToken;

    /**
     * 可用配置键清单：直接引用 Store 的白名单常量。
     *
     * <p>不在这里另写一份——两处各写一份必然分叉，前端按旧清单渲染出的键会被保存接口拒绝。
     */
    private static final Map<String, List<String>> KEYS = PluginPayConfigStore.ALLOWED_KEYS;

    /**
     * 配置总览：生效快照（脱敏）+ 库里原始行 + 待办问题 + 可用键清单。
     *
     * <p>参数：无。
     */
    @PostMapping("/view")
    public ResponseResult view(@RequestHeader(value = "X-Pay-Admin-Token", required = false)
                               String adminTokenHeader) {
        String deny = denyReason(null, adminTokenHeader);
        if (deny != null) {
            return ResponseResult.fail(403, deny);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("effective", store.describe());
        data.put("issues", channelRouter.describe());
        data.put("rows", listRows());
        data.put("availableKeys", KEYS);
        data.put("secretKeys", List.of("api-key", "private-key"));
        data.put("tableReady", store.isTableReady());
        if (!store.isTableReady()) {
            data.put("hint", "配置表 hou_market_pay_config 尚不可读，当前完全使用 Nacos/yml 基线。"
                    + "请先执行 backend/paas/base/modules/plugin-market/src/main/resources/sql/mall_market_pay_config.sql");
        }
        return ResponseResult.success(data);
    }

    /**
     * 新增或更新一行配置，写完立即刷新生效（无需重启）。
     *
     * <p>参数：{@code scope}（GLOBAL/CHANNEL/PLAN）、{@code scopeKey}、{@code subKey}、
     * {@code configKey}、{@code configValue}、{@code valueType}（STRING/INT/DECIMAL/BOOL）、
     * {@code remark}、{@code updateBy}。
     */
    @PostMapping("/save")
    public ResponseResult save(@RequestBody(required = false) Map<String, Object> params,
                               @RequestHeader(value = "X-Pay-Admin-Token", required = false)
                               String adminTokenHeader) {
        if (params == null) {
            return ResponseResult.fail(400, "缺少请求体");
        }
        String deny = denyReason(params, adminTokenHeader);
        if (deny != null) {
            return ResponseResult.fail(403, deny);
        }
        MarketPayConfig row = new MarketPayConfig();
        row.setScope(text(params.get("scope")).toUpperCase());
        row.setScopeKey(MarketPayConfig.SCOPE_GLOBAL.equals(row.getScope()) ? MarketPayConfig.GLOBAL_KEY
                : text(params.get("scopeKey")));
        row.setSubKey(text(params.get("subKey")));
        row.setConfigKey(text(params.get("configKey")));
        row.setConfigValue(params.get("configValue") == null ? null : String.valueOf(params.get("configValue")));
        String valueType = text(params.get("valueType")).toUpperCase();
        row.setValueType(valueType.isEmpty() ? "STRING" : valueType);
        row.setRemark(text(params.get("remark")));
        row.setUpdateBy(text(params.get("updateBy")));
        // 审计：改支付配置等于改钱怎么收，必须留得下「谁在什么时候改的」
        if (row.getUpdateBy() == null || row.getUpdateBy().isBlank()) {
            return ResponseResult.fail(400, "updateBy 必填（操作人标识，写入审计字段）");
        }

        String error = store.validate(row);
        if (error != null) {
            return ResponseResult.fail(400, error);
        }

        try {
            MarketPayConfig exist = find(row);
            if (exist != null) {
                row.setId(exist.getId());
                configMapper.updateById(row);
            } else {
                configMapper.insert(row);
            }
        } catch (Exception e) {
            log.error("保存支付配置失败: {}", e.getMessage());
            return ResponseResult.fail(500, "保存失败（配置表可能尚未创建）: " + e.getMessage());
        }
        store.reload();
        log.info("支付配置已变更: {}/{}/{}={} by {}", row.getScope(), row.getScopeKey(),
                row.getSubKey(), row.getConfigKey(), row.getUpdateBy());
        return ResponseResult.success(Map.of(
                "saved", row.getScope() + "/" + row.getScopeKey() + "/" + row.getSubKey() + "/" + row.getConfigKey(),
                "effective", store.describe()));
    }

    /**
     * 删除一行配置（回落到 Nacos/yml 基线），删完立即刷新。
     *
     * <p>参数：{@code scope}、{@code scopeKey}、{@code subKey}、{@code configKey}。
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody(required = false) Map<String, Object> params,
                                 @RequestHeader(value = "X-Pay-Admin-Token", required = false)
                                 String adminTokenHeader) {
        if (params == null) {
            return ResponseResult.fail(400, "缺少请求体");
        }
        String deny = denyReason(params, adminTokenHeader);
        if (deny != null) {
            return ResponseResult.fail(403, deny);
        }
        MarketPayConfig probe = new MarketPayConfig();
        probe.setScope(text(params.get("scope")).toUpperCase());
        probe.setScopeKey(MarketPayConfig.SCOPE_GLOBAL.equals(probe.getScope()) ? MarketPayConfig.GLOBAL_KEY
                : text(params.get("scopeKey")));
        probe.setSubKey(text(params.get("subKey")));
        probe.setConfigKey(text(params.get("configKey")));
        String updateBy = text(params.get("updateBy"));
        if (updateBy == null || updateBy.isBlank()) {
            return ResponseResult.fail(400, "updateBy 必填（操作人标识，写入审计日志）");
        }
        try {
            MarketPayConfig exist = find(probe);
            if (exist == null) {
                return ResponseResult.fail(404, "配置不存在");
            }
            configMapper.deleteById(exist.getId());
            store.reload();
            log.warn("支付配置已删除（回落基线）: {}/{}/{}={} by {}", probe.getScope(), probe.getScopeKey(),
                    probe.getSubKey(), probe.getConfigKey(), updateBy);
            return ResponseResult.success(Map.of("deleted", exist.getId(), "effective", store.describe()));
        } catch (Exception e) {
            log.error("删除支付配置失败: {}", e.getMessage());
            return ResponseResult.fail(500, "删除失败: " + e.getMessage());
        }
    }

    /** 重新加载配置（建表后首次使用、或手工同步时用）。 */
    @PostMapping("/reload")
    public ResponseResult reload(@RequestHeader(value = "X-Pay-Admin-Token", required = false)
                                 String adminTokenHeader) {
        String deny = denyReason(null, adminTokenHeader);
        if (deny != null) {
            return ResponseResult.fail(403, deny);
        }
        store.reload();
        return ResponseResult.success(Map.of(
                "tableReady", store.isTableReady(),
                "effective", store.describe()));
    }

    /**
     * 管理端点准入校验。
     *
     * @param params 请求体（令牌也可放这里，便于内部脚本调用）
     * @return 拒绝原因；null 表示通过
     */
    private String denyReason(Map<String, Object> params, String tokenHeader) {
        if (adminToken == null || adminToken.isBlank()) {
            return "未配置管理令牌，支付配置管理接口已停用。"
                    + "请注入环境变量 HOU_PLUGIN_PAY_ADMIN_TOKEN 后重试（该接口不向普通登录用户开放）";
        }
        String provided = tokenHeader != null && !tokenHeader.isBlank() ? tokenHeader.trim() : null;
        if (provided == null && params != null && params.get("adminToken") != null) {
            provided = String.valueOf(params.get("adminToken")).trim();
        }
        if (provided == null || provided.isBlank()) {
            return "缺少管理令牌：请在请求头 X-Pay-Admin-Token 中携带";
        }
        if (!PluginPaySigner.constantTimeEquals(adminToken.trim(), provided)) {
            log.warn("支付配置管理接口令牌校验失败（已拒绝）");
            return "管理令牌无效";
        }
        return null;
    }

    private MarketPayConfig find(MarketPayConfig probe) {
        return configMapper.selectOne(new LambdaQueryWrapper<MarketPayConfig>()
                .eq(MarketPayConfig::getScope, probe.getScope())
                .eq(MarketPayConfig::getScopeKey, probe.getScopeKey() == null ? "" : probe.getScopeKey())
                .eq(MarketPayConfig::getSubKey, probe.getSubKey() == null ? "" : probe.getSubKey())
                .eq(MarketPayConfig::getConfigKey, probe.getConfigKey())
                .last("LIMIT 1"));
    }

    /**
     * 库里原始行。密钥行的值本就是 {@code ${ENV}} 引用，可以安全回显；
     * 真值始终只在环境变量里。
     */
    private List<Map<String, Object>> listRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            List<MarketPayConfig> list = configMapper.selectList(
                    new LambdaQueryWrapper<MarketPayConfig>()
                            .orderByAsc(MarketPayConfig::getScope)
                            .orderByAsc(MarketPayConfig::getScopeKey)
                            .orderByAsc(MarketPayConfig::getSubKey)
                            .orderByAsc(MarketPayConfig::getConfigKey));
            for (MarketPayConfig row : list) {
                Map<String, Object> one = new LinkedHashMap<>();
                one.put("id", row.getId());
                one.put("scope", row.getScope());
                one.put("scopeKey", row.getScopeKey());
                one.put("subKey", row.getSubKey());
                one.put("configKey", row.getConfigKey());
                one.put("configValue", row.getConfigValue());
                one.put("valueType", row.getValueType());
                one.put("secret", row.getSecret());
                one.put("remark", row.getRemark());
                one.put("updateBy", row.getUpdateBy());
                one.put("updateTime", row.getUpdateTime() == null ? null : row.getUpdateTime().toString());
                rows.add(one);
            }
        } catch (Exception e) {
            log.warn("读取支付配置表失败（表可能未创建）: {}", e.getMessage());
        }
        return rows;
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
