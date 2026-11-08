package com.houjiang.userms.plugin;

import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import com.houjiang.plugin.api.extension.user.RedpacketQueryExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedpacketQueryGateway} 契约：<b>只读</b>、空上下文不抛异常、无插件回落官方、
 * 插件优先并做领域映射；误用 abort 也不得抛异常、不得中断。
 */
class RedpacketQueryGatewayTest {

    private static ExtensionGatewaySupport support(RedpacketQueryExtension... extensions) {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(0);
        ExtensionRegistry registry = new ExtensionRegistry();
        int priority = 0;
        for (RedpacketQueryExtension extension : extensions) {
            registry.register(RedpacketQueryExtension.class, extension, "custom-" + priority, priority, "official");
            priority += 10;
        }
        return new ExtensionGatewaySupport(new ExtensionInvoker(registry, properties));
    }

    private static RedTemplateEntityVO officialTemplate() {
        RedTemplateEntityVO vo = new RedTemplateEntityVO();
        vo.setRedpacketTId(1);
        vo.setRedpacketTTitle("官方券");
        return vo;
    }

    @Test
    void 无插件空上下文不抛异常() {
        List<Map<String, Object>> result = new RedpacketQueryGateway(support())
                .query(RedpacketQueryExtension.QUERY_TEMPLATE_BY_IDS, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void 无插件时回落官方结果() {
        List<RedTemplateEntityVO> official = List.of(officialTemplate());

        List<RedTemplateEntityVO> result = new RedpacketQueryGateway(support())
                .redTemplatesByIds(null, List.of(1), official);

        assertSame(official, result);
    }

    @Test
    void 插件结果优先并被映射为红包模板() {
        RedpacketQueryExtension ext = ctx -> ctx.result.add(Map.of(
                "redpacketTId", 7,
                "redpacketTTitle", "二开券",
                "redpacketTPrice", new BigDecimal("5.00"),
                "redTermofvalidity", 30));

        List<RedTemplateEntityVO> result = new RedpacketQueryGateway(support(ext))
                .redTemplatesByIds(null, List.of(7), List.of(officialTemplate()));

        assertEquals(1, result.size());
        assertEquals(7, result.get(0).getRedpacketTId());
        assertEquals("二开券", result.get(0).getRedpacketTTitle());
        assertEquals(0, new BigDecimal("5.00").compareTo(result.get(0).getRedpacketTPrice()));
        assertEquals(30, result.get(0).getRedTermofvalidity());
    }

    @Test
    void 用户领取记录计数_插件优先否则回落官方() {
        RedpacketQueryExtension ext = ctx -> {
            ctx.result.add(Map.of("id", 1));
            ctx.result.add(Map.of("id", 2));
        };

        Integer pluginCount = new RedpacketQueryGateway(support(ext))
                .countUserReceived(1L, List.of(1, 2), 0);
        Integer fallbackCount = new RedpacketQueryGateway(support())
                .countUserReceived(1L, List.of(1, 2), 3);

        assertEquals(2, pluginCount);
        assertEquals(3, fallbackCount);
    }

    @Test
    void 只读扩展点误用abort不抛异常() {
        RedpacketQueryExtension ext = ctx -> ctx.abort("不该中断");

        List<Map<String, Object>> result = new RedpacketQueryGateway(support(ext))
                .query(RedpacketQueryExtension.QUERY_USER_RECEIVED, 1L, List.of(1), null);

        assertTrue(result.isEmpty());
    }
}
