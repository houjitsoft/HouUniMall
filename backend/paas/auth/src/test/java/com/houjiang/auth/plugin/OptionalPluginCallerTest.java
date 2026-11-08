package com.houjiang.auth.plugin;

import com.houjiang.commons.util.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 可选插件调用守卫测试（闭坑清单 P0「Core 失去独立运行」）。
 *
 * <p>核心断言：插件缺失（抛异常）或返回空时，降级为明确失败应答，<b>绝不抛异常</b>；
 * 插件可用时结果原样透传，不改写业务语义。
 */
class OptionalPluginCallerTest {

    @Test
    @DisplayName("插件可用：结果原样透传，不改写")
    void passesThroughWhenPluginAvailable() {
        ResponseResult<String> upstream = ResponseResult.success("ok");
        ResponseResult<String> result =
                OptionalPluginCaller.call("webpos", "webPosLogin", () -> upstream);
        assertSame(upstream, result);
    }

    @Test
    @DisplayName("插件未安装（抛异常）：降级为失败应答，不抛出")
    void degradesWhenPluginMissing() {
        ResponseResult<String> result = assertDoesNotThrow(() ->
                OptionalPluginCaller.call("webpos", "webPosLogin", () -> {
                    throw new RuntimeException("No instances available for webpos");
                }));
        assertNotNull(result);
        assertTrue(result.getMessage().contains("webpos"),
                "提示应点名插件编码，实际=" + result.getMessage());
        assertTrue(result.getMessage().contains("未安装或不可用"),
                "提示应说明插件不可用，实际=" + result.getMessage());
    }

    @Test
    @DisplayName("远程返回 null：同样降级，不 NPE")
    void degradesWhenUpstreamReturnsNull() {
        ResponseResult<String> result =
                OptionalPluginCaller.call("delivery", "riderLogin", () -> null);
        assertNotNull(result);
        assertTrue(result.getMessage().contains("delivery"));
    }

    @Test
    @DisplayName("不同插件的提示互不混淆")
    void messageNamesThePlugin() {
        ResponseResult<String> webpos =
                OptionalPluginCaller.call("webpos", "webPosLogout", () -> {
                    throw new IllegalStateException("boom");
                });
        ResponseResult<String> rider =
                OptionalPluginCaller.call("delivery", "riderLogin", () -> {
                    throw new IllegalStateException("boom");
                });
        assertTrue(webpos.getMessage().contains("webpos"));
        assertTrue(rider.getMessage().contains("delivery"));
        assertEquals(webpos.getCode(), rider.getCode());
    }
}
