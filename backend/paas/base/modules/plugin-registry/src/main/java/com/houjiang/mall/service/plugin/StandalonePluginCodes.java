package com.houjiang.mall.service.plugin;

import java.util.Set;

/**
 * 独立服务型插件编码的单一事实来源（插件中心、插件更新、安装代理守卫共用）。
 *
 * <p><b>为什么收口到这里</b>：独立服务名单原先在 {@code PluginCenterService}、
 * {@code PluginCenterController}、{@code PluginUpdateController} 各写一份，
 * 三份手工同步的编码集合迟早漂移 —— 2026-10-27 AI 网关改为独立容器
 * （{@code plugins/aigateway}，{@code runtime: standalone}）后，
 * 三处名单都没带上它，于是该插件的启停与安装会误走 PF4J 内嵌链路。
 *
 * <p><b>为什么不读 {@code architecture/services.json}</b>：base 运行期以数据库目录
 * （{@code hou_addon_plugin}）为准，仓库根的那份清单是发布期契约；两者可能短暂不一致，
 * 而运行期判定必须稳定。独立服务名单是<b>发布形态</b>而非业务归属，
 * 与 {@code hostService}（业务归属，可能是 thirdparty 集成边界）无关，两者不可混用。
 *
 * <p>新增独立服务插件时，只需在此登记一次。
 *
 * @author Amos
 */
public final class StandalonePluginCodes {

    /** 独立服务型插件：独立容器运行，启停/安装/卸载一律由容器安装代理负责。 */
    public static final Set<String> CODES = Set.of(
            "crm", "cms", "erp", "finance", "geo", "im", "logging", "mes",
            "oa", "orderfood", "pm", "rider", "supplier", "webpos",
            // AI 网关：FreeLLMAPI 独立容器（unimall-aigateway），不加载进任何宿主进程。
            "aigateway"
    );

    private StandalonePluginCodes() {
    }

    /** 该插件是否为独立服务型（独立容器运行）。 */
    public static boolean isStandalone(String code) {
        return code != null && CODES.contains(code.trim());
    }
}
