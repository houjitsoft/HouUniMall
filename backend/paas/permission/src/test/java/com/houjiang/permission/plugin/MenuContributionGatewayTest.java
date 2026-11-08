package com.houjiang.permission.plugin;

import com.houjiang.permission.vo.MenuVO;
import com.houjiang.plugin.api.extension.permission.MenuContributionExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.PluginAbortException;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MenuContributionGateway} 契约：只接受 {@link MenuVO}、按 id 去重、<b>重复调用幂等</b>，
 * 无插件时官方菜单不变，abort → 业务异常。
 */
class MenuContributionGatewayTest {

    private static ExtensionGatewaySupport support(MenuContributionExtension... extensions) {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(0);
        ExtensionRegistry registry = new ExtensionRegistry();
        int priority = 0;
        for (MenuContributionExtension extension : extensions) {
            registry.register(MenuContributionExtension.class, extension, "custom-" + priority, priority, "official");
            priority += 10;
        }
        return new ExtensionGatewaySupport(new ExtensionInvoker(registry, properties));
    }

    private static MenuVO menu(Integer id, String name) {
        MenuVO vo = new MenuVO();
        vo.setId(id);
        vo.setPid(0);
        vo.setName(name);
        return vo;
    }

    @Test
    void 无插件时官方菜单原样返回() {
        List<MenuVO> official = List.of(menu(1, "系统"), menu(2, "订单"));

        List<MenuVO> merged = new MenuContributionGateway(support())
                .contributeAndMerge("demo", official);

        assertEquals(2, merged.size());
        assertEquals("系统", merged.get(0).getName());
    }

    @Test
    void 插件贡献的菜单被追加() {
        MenuContributionExtension ext = ctx -> ctx.menus.add(menu(900, "二开入口"));

        List<MenuVO> merged = new MenuContributionGateway(support(ext))
                .contributeAndMerge("demo", List.of(menu(1, "系统")));

        assertEquals(2, merged.size());
        assertEquals(900, merged.get(1).getId());
    }

    @Test
    void 非MenuVO节点被丢弃不污染菜单树() {
        MenuContributionExtension ext = ctx -> {
            ctx.menus.add("非法节点");
            ctx.menus.add(menu(901, "合法节点"));
        };

        List<MenuVO> merged = new MenuContributionGateway(support(ext))
                .contributeAndMerge("demo", new ArrayList<>());

        assertEquals(1, merged.size());
        assertEquals("合法节点", merged.get(0).getName());
    }

    @Test
    void 重复调用幂等且去重() {
        MenuContributionExtension ext = ctx -> {
            ctx.menus.add(menu(1, "与官方重复"));
            ctx.menus.add(menu(902, "新增"));
        };
        MenuContributionGateway gateway = new MenuContributionGateway(support(ext));
        List<MenuVO> official = List.of(menu(1, "系统"));

        List<MenuVO> first = gateway.contributeAndMerge("demo", official);
        List<MenuVO> second = gateway.contributeAndMerge("demo", official);

        assertEquals(2, first.size(), "官方 1 + 新增 1（与官方同 id 的贡献被去重）");
        assertEquals(first.size(), second.size(), "重复调用结果幂等");
        assertEquals(902, first.get(1).getId());
        assertEquals(902, second.get(1).getId());
    }

    @Test
    void 插件中断时抛可识别的业务异常() {
        MenuContributionExtension ext = ctx -> ctx.abort("菜单贡献被禁用");

        PluginAbortException ex = assertThrows(PluginAbortException.class,
                () -> new MenuContributionGateway(support(ext)).contributeAndMerge("demo", List.of()));

        assertEquals("MenuContribution", ex.getPoint());
    }

    @Test
    void 官方菜单为空时仅返回贡献项() {
        MenuContributionExtension ext = ctx -> ctx.menus.add(menu(903, "仅二开"));

        List<MenuVO> merged = new MenuContributionGateway(support(ext)).contributeAndMerge("demo", null);

        assertTrue(merged.size() == 1);
        assertEquals(903, merged.get(0).getId());
    }
}
