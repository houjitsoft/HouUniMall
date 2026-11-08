package com.houjiang.plugin.update.risk;

import com.houjiang.mall.service.plugin.ExtensionDescriptorScanner;
import com.houjiang.plugin.update.precheck.PluginCompatibilityPrecheckService;
import com.houjiang.plugin.update.precheck.PrecheckResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 升级前风险报告：强制向用户展示「将备份 / 可能不可逆迁移 / 二开冲突」。
 */
@Service
public class UpgradeRiskService {

    private final PluginCompatibilityPrecheckService precheckService;

    public UpgradeRiskService(PluginCompatibilityPrecheckService precheckService) {
        this.precheckService = Objects.requireNonNull(precheckService, "precheckService");
    }

    /**
     * @param pluginCode    插件编码（独立服务时等同 hostService）
     * @param targetVersion 目标版本，可空
     * @param hostService   宿主服务提示（可空，默认用 pluginCode）
     */
    public Map<String, Object> report(String pluginCode, String targetVersion, String hostService) {
        String code = pluginCode == null ? "" : pluginCode.trim();
        String host = StringUtils.hasText(hostService) ? hostService.trim() : code;
        PrecheckResult pre = precheckService.precheck(code, targetVersion);

        List<Map<String, Object>> extensions = new ArrayList<>();
        for (Map<String, String> row : ExtensionDescriptorScanner.discoverDetailed()) {
            String extHost = row.getOrDefault(ExtensionDescriptorScanner.KEY_HOST_SERVICE, "");
            String extCode = row.getOrDefault(ExtensionDescriptorScanner.KEY_CODE, "");
            if (!matchesHost(host, code, extHost, extCode)) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", extCode);
            item.put("version", row.getOrDefault(ExtensionDescriptorScanner.KEY_VERSION, "unknown"));
            item.put("hostService", extHost);
            item.put("contracts", row.getOrDefault(ExtensionDescriptorScanner.KEY_CONTRACTS, ""));
            item.put("note", "站点级二开已装入 classpath；升级官方插件/服务后请复核契约与升级差异报告");
            extensions.add(item);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pluginCode", code);
        out.put("targetVersion", targetVersion);
        out.put("hostService", host);
        out.put("willBackup", true);
        out.put("willBackupDetail", "升级前将自动备份当前版本目录；失败时优先回滚代码/容器，不保证业务数据原样恢复");
        out.put("irreversibleMigration", true);
        out.put("irreversibleDetail", "若目标版本含 schema 迁移，库表变更可能不可自动回滚（仅回滚制品与进程）");
        out.put("extensionConflicts", extensions);
        out.put("extensionConflictCount", extensions.size());
        out.put("precheckPass", pre.isPass());
        out.put("precheckReason", pre.getReason());
        out.put("precheckDetail", pre.getDetail());
        out.put("mustAcknowledge", List.of("backup", "migration", "extension"));
        out.put("blocking", !pre.isPass());
        return out;
    }

    private static boolean matchesHost(String host, String pluginCode, String extHost, String extCode) {
        String h = host.toLowerCase(Locale.ROOT);
        String p = pluginCode.toLowerCase(Locale.ROOT);
        String eh = extHost == null ? "" : extHost.toLowerCase(Locale.ROOT);
        String ec = extCode == null ? "" : extCode.toLowerCase(Locale.ROOT);
        if (eh.isEmpty()) {
            // 旧描述无 hostService：用 custom-<service> 命名约定兜底
            return ec.equals("custom-" + h) || ec.equals("custom-" + p) || ec.endsWith("-" + h);
        }
        return eh.equals(h) || eh.equals(p);
    }
}
