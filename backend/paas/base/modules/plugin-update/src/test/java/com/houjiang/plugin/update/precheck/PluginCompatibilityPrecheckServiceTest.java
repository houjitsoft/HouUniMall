package com.houjiang.plugin.update.precheck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.plugin.update.config.UpdateProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 兼容性预检测试（P0-9）。
 *
 * <p>除了「该拦的是否拦住」，这里同样重要的是「<b>不该拦的是否放行</b>」：
 * 存量插件里 {@code min_framework_version} 为 null、{@code dependencies_json} 为空
 * 是普遍现象，声明缺失不能演变成「装不上」。
 *
 * @author Amos
 * @since M3
 */
class PluginCompatibilityPrecheckServiceTest {

    private MarketPluginMapper pluginMapper;

    private MarketPluginVersionMapper versionMapper;

    private MallPluginInstallMapper installMapper;

    private UpdateProperties properties;

    private PluginCompatibilityPrecheckService service;

    @BeforeEach
    void setUp() {
        pluginMapper = mock(MarketPluginMapper.class);
        versionMapper = mock(MarketPluginVersionMapper.class);
        installMapper = mock(MallPluginInstallMapper.class);
        properties = new UpdateProperties();
        properties.setFrameworkVersion("1.0.0");
        service = new PluginCompatibilityPrecheckService(pluginMapper, versionMapper, installMapper, properties);
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(version("1.0.0"));
    }

    @Test
    void 框架版本低于要求返回3014且detail含具体版本() {
        givenPlugin(plugin("demo", "2.0", null));
        PrecheckResult result = service.precheck("demo", "1.0.0");
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.INCOMPATIBLE_FRAMEWORK, result.getReason());
        assertTrue(result.getDetail().contains("1.0.0"), "detail 应包含当前框架版本");
        assertTrue(result.getDetail().contains("2.0"), "detail 应包含插件要求的最低版本");
    }

    @Test
    void 框架版本等于或高于要求放行() {
        givenPlugin(plugin("demo", "1.0.0", null));
        assertTrue(service.precheck("demo", "1.0.0").isPass());

        properties.setFrameworkVersion("2.1.0");
        givenPlugin(plugin("demo", "2.0", null));
        assertTrue(service.precheck("demo", "1.0.0").isPass());
    }

    @Test
    void 未声明最低框架版本放行() {
        givenPlugin(plugin("demo", null, null));
        assertTrue(service.precheck("demo", "1.0.0").isPass());

        givenPlugin(plugin("demo", "  ", null));
        assertTrue(service.precheck("demo", "1.0.0").isPass());
    }

    @Test
    void 依赖未安装返回3015() {
        givenPlugin(plugin("demo", null, "[\"erp\"]"));
        when(installMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        PrecheckResult result = service.precheck("demo", "1.0.0");
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.DEPENDENCY_MISSING, result.getReason());
        assertTrue(result.getDetail().contains("erp"));
        assertTrue(result.getDetail().contains("未安装"));
    }

    @Test
    void 依赖已安装但未启用返回3015() {
        givenPlugin(plugin("demo", null, "[\"erp\"]"));
        MallPluginInstall installed = install("erp", "1.0.0", 1, 0);
        when(installMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(installed);
        PrecheckResult result = service.precheck("demo", "1.0.0");
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.DEPENDENCY_MISSING, result.getReason());
        assertTrue(result.getDetail().contains("未启用"));
    }

    @Test
    void 依赖已安装且已启用放行() {
        givenPlugin(plugin("demo", null, "[\"erp\"]"));
        when(installMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(install("erp", "1.0.0", 1, 1));
        assertTrue(service.precheck("demo", "1.0.0").isPass());
    }

    @Test
    void 依赖声明版本高于已装版本返回3015() {
        givenPlugin(plugin("demo", null, "[\"erp@2.0.0\"]"));
        when(installMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(install("erp", "1.0.0", 1, 1));
        PrecheckResult result = service.precheck("demo", "1.0.0");
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.DEPENDENCY_MISSING, result.getReason());
        assertTrue(result.getDetail().contains("2.0.0"));
    }

    @Test
    void 依赖声明版本不高于已装版本放行() {
        givenPlugin(plugin("demo", null, "[\"erp@1.0.0\"]"));
        when(installMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(install("erp", "1.2.0", 1, 1));
        assertTrue(service.precheck("demo", "1.0.0").isPass());
    }

    @Test
    void 依赖段缺失或非法一律放行() {
        givenPlugin(plugin("demo", null, null));
        assertTrue(service.precheck("demo", "1.0.0").isPass());

        givenPlugin(plugin("demo", null, ""));
        assertTrue(service.precheck("demo", "1.0.0").isPass());

        givenPlugin(plugin("demo", null, "{不是数组"));
        assertTrue(service.precheck("demo", "1.0.0").isPass());
    }

    @Test
    void 框架版本与依赖同时满足放行() {
        givenPlugin(plugin("demo", "1.0.0", "[\"erp\",\"oa\"]"));
        when(installMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(install("erp", "1.0.0", 1, 1));
        assertTrue(service.precheck("demo", "1.0.0").isPass());
    }

    @Test
    void 版本不存在返回3009() {
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        PrecheckResult result = service.precheck("demo", "9.9.9");
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.ARTIFACT_NOT_FOUND, result.getReason());
    }

    @Test
    void 商品行缺失按弱校验放行() {
        givenPlugin(null);
        PrecheckResult result = service.precheck("demo", "1.0.0");
        assertTrue(result.isPass());
        assertNull(result.getReason());
    }

    @Test
    void 插件编码为空返回3009() {
        PrecheckResult result = service.precheck("  ", "1.0.0");
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.ARTIFACT_NOT_FOUND, result.getReason());
    }

    // ------------------------------------------------------------------
    // 测试数据
    // ------------------------------------------------------------------

    private void givenPlugin(MarketPlugin plugin) {
        when(pluginMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(plugin);
    }

    private static MarketPlugin plugin(String code, String minFrameworkVersion, String dependenciesJson) {
        MarketPlugin plugin = new MarketPlugin();
        plugin.setPluginCode(code);
        plugin.setMinFrameworkVersion(minFrameworkVersion);
        plugin.setDependenciesJson(dependenciesJson);
        return plugin;
    }

    private static MarketPluginVersion version(String version) {
        MarketPluginVersion row = new MarketPluginVersion();
        row.setPluginCode("demo");
        row.setVersion(version);
        row.setStatus(1);
        return row;
    }

    private static MallPluginInstall install(String code, String version, int installed, int enabled) {
        MallPluginInstall row = new MallPluginInstall();
        row.setPluginCode(code);
        row.setVersion(version);
        row.setInstalled(installed);
        row.setEnabled(enabled);
        return row;
    }
}
