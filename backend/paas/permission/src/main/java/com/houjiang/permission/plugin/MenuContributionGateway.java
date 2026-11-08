package com.houjiang.permission.plugin;

import com.houjiang.permission.vo.MenuVO;
import com.houjiang.plugin.api.extension.permission.MenuContributionExtension;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 菜单贡献扩展点的宿主薄网关（{@code before*} 语义，仅贡献数据）。
 *
 * <p><b>做什么</b>：让二开插件向宿主侧栏贡献菜单入口。插件把菜单节点写进
 * {@link MenuContributionExtension.Context#menus}，宿主在返回用户菜单前合并。
 *
 * <p><b>领域夹取与幂等</b>：
 * <ul>
 *   <li>契约里 {@code menus} 为 {@code List<Object>}（插件侧类型不可 ingress），网关只接受
 *       {@link MenuVO} 实例，其余类型记录告警后丢弃，绝不污染官方菜单树；</li>
 *   <li>按 {@code menuId} 去重：已存在于官方菜单树、或本轮重复贡献的同 id 节点只保留一份，
 *       因此<b>重复调用幂等</b>（官方菜单每次从库/缓存重建，贡献项挂在返回结果上，不写回缓存）。</li>
 * </ul>
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理；
 * MenuContribution 属故障 IGNORE 类（插件故障不影响侧栏）。
 *
 * @author Amos
 */
public class MenuContributionGateway {

    private static final Logger log = LoggerFactory.getLogger(MenuContributionGateway.class);

    private static final String POINT = "MenuContribution";

    private final ExtensionGatewaySupport support;

    public MenuContributionGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /**
     * 触发扩展点，收集插件贡献的原始菜单节点。
     *
     * @param pluginCode 宿主可用它做作用域提示，可为 {@code null}（插件自报身份）
     * @return 插件写入的原始节点列表；无插件时为空列表
     * @throws PluginAbortException 当插件显式中断时抛出
     */
    public List<Object> collect(String pluginCode) {
        MenuContributionExtension.Context ctx = new MenuContributionExtension.Context();
        ctx.pluginCode = pluginCode;

        support.invoke(MenuContributionExtension.class, ctx);

        if (ctx.isAborted()) {
            log.warn("[ext] point={} result=abort reason={}", POINT, ctx.getAbortReason());
            throw new PluginAbortException(POINT, ctx.getAbortReason());
        }
        return ctx.menus == null ? List.of() : ctx.menus;
    }

    /**
     * 领域夹取 + 幂等合并：把插件贡献的合法 {@link MenuVO} 追加到官方菜单结果。
     *
     * @param pluginCode 作用域提示，可为 {@code null}
     * @param official   官方菜单（可为 {@code null} 视作空）
     * @return 合并后的新列表（不修改入参）
     */
    public List<MenuVO> contributeAndMerge(String pluginCode, List<MenuVO> official) {
        List<MenuVO> merged = official == null ? new ArrayList<>() : new ArrayList<>(official);
        List<Object> contributed = collect(pluginCode);
        if (contributed.isEmpty()) {
            return merged;
        }
        Set<Integer> seenIds = new HashSet<>();
        for (MenuVO menu : merged) {
            if (menu != null && menu.getId() != null) {
                seenIds.add(menu.getId());
            }
        }
        for (Object node : contributed) {
            if (!(node instanceof MenuVO vo)) {
                log.warn("[ext] point={} 贡献了非 MenuVO 节点，已忽略：type={}",
                        POINT, node == null ? "null" : node.getClass().getName());
                continue;
            }
            Integer id = vo.getId();
            if (id != null && !seenIds.add(id)) {
                // 幂等：与官方菜单或前一次贡献重复的节点不再追加
                continue;
            }
            merged.add(vo);
        }
        return merged;
    }
}
