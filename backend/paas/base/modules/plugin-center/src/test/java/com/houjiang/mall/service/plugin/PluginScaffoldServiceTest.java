package com.houjiang.mall.service.plugin;

import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.AddonPlugin;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.AddonPluginMapper;
import com.houjiang.mall.mapper.plugin.AddonCateMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.houjiang.plugin.sdk.model.PluginManifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 创建插件脚手架测试：真实落盘到临时目录，验证文件生成、三层入库与失败回退。
 */
class PluginScaffoldServiceTest {

    @TempDir
    Path tempDir;

    private AddonPluginMapper addonPluginMapper;
    private MarketPluginMapper marketPluginMapper;
    private MarketPluginVersionMapper marketPluginVersionMapper;
    private PluginScaffoldService service;

    @BeforeEach
    void setUp() {
        addonPluginMapper = mock(AddonPluginMapper.class);
        marketPluginMapper = mock(MarketPluginMapper.class);
        marketPluginVersionMapper = mock(MarketPluginVersionMapper.class);
        when(addonPluginMapper.selectCount(any())).thenReturn(0L);
        when(marketPluginMapper.selectCount(any())).thenReturn(0L);
        service = new PluginScaffoldService(addonPluginMapper, marketPluginMapper,
                marketPluginVersionMapper, tempDir);
    }

    private Map<String, Object> validParams() {
        return Map.of("code", "visitor", "name", "访客管理", "description", "访客预约与登记",
                "licenseType", "PAID", "priceCents", 99000, "hostService", "oa");
    }

    @Test
    void standaloneCreatesMetadataAndFrontendWithinExistingService() throws Exception {
        Map<String, Object> result = service.createPlugin(validParams());
        Path dir = tempDir.resolve("oa/visitor");
        assertEquals(dir.toString(), result.get("pluginDir"));
        assertEquals("backend/paas/oa", result.get("implementationDir"));
        assertEquals(4, ((List<?>) result.get("files")).size());
        for (String file : List.of("plugin.yaml", "README.md", "frontend/src/index.js", "frontend/src/pages/Home.vue")) {
            assertTrue(Files.isRegularFile(dir.resolve(file)), file);
        }
        assertFalse(Files.exists(dir.resolve("pom.xml")));
        assertFalse(Files.exists(dir.resolve("src")));
        assertFalse(Files.exists(dir.resolve("deploy")));
        String yaml = Files.readString(dir.resolve("plugin.yaml"));
        assertTrue(yaml.contains("hostService: oa"));
        assertTrue(yaml.contains("runtime: standalone"));
        assertTrue(yaml.contains("license: PAID"));
        assertTrue(yaml.contains("entry: /plugin-assets/visitor/1.0.0/index.mjs"));
        assertFalse(yaml.contains("port:"));
        // 新框架必备字段：minFrameworkVersion 缺失会被发布校验拒绝；type 是 schema 里不存在的幽灵字段
        assertTrue(yaml.contains("minFrameworkVersion: 1.0.0"));
        assertTrue(yaml.contains("permissions:"));
        assertTrue(yaml.contains("- visitor:root"));
        assertTrue(yaml.contains("owner: official"));
        assertFalse(yaml.contains("type: SERVICE"));
        assertFalse(yaml.contains("type: EXAMPLE"));
        assertFalse(yaml.contains("routes:"));
        PluginManifest manifest = new ObjectMapper(new YAMLFactory()).readValue(yaml, PluginManifest.class);
        assertEquals("", manifest.getCategory());
        assertEquals(List.of("admin"), manifest.getTerminals());
        assertEquals("oa", manifest.getHostService());
        assertEquals("PAID", manifest.getLicense());
        assertTrue(Files.readString(dir.resolve("README.md")).contains("backend/paas/oa"));
    }

    @Test
    void embeddedCreatesLoadableExtensionWithoutIndependentServer() throws Exception {
        service.createPlugin(Map.of("code", "visitor-log", "name", "访客流水", "hostService", "oa", "runtime", "embedded"));
        Path dir = tempDir.resolve("oa/visitor-log");
        // 源码必须落 runtime/java：Maven 默认的 src/main/java 不被插件构建脚本识别
        String source = Files.readString(dir.resolve("runtime/java/com/houjiang/plugin/visitor_log/VisitorLogPlugin.java"));
        assertTrue(source.contains("extends Plugin"));
        assertFalse(source.contains("SpringApplication"));
        assertFalse(Files.exists(dir.resolve("src/main/java")));
        String pom = Files.readString(dir.resolve("pom.xml"));
        assertTrue(pom.contains("<Plugin-Class>com.houjiang.plugin.visitor_log.VisitorLogPlugin</Plugin-Class>"));
        assertFalse(pom.contains("spring-boot"));
        // pom 必须声明 runtime/java 源目录，并把 plugin.yaml 与 frontend 打进包
        assertTrue(pom.contains("<sourceDirectory>${project.basedir}/runtime/java</sourceDirectory>"));
        assertTrue(pom.contains("<include>plugin.yaml</include>"));
        assertTrue(pom.contains("node_modules/**"));
        assertFalse(Files.exists(dir.resolve("deploy")));
        String yaml = Files.readString(dir.resolve("plugin.yaml"));
        assertTrue(yaml.contains("runtime: embedded"));
        assertTrue(yaml.contains("hostService: oa"));
        assertTrue(yaml.contains("prefix: hou_plugin_visitor_log_"));
        assertTrue(yaml.contains("minFrameworkVersion: 1.0.0"));
        assertTrue(yaml.contains("- visitor-log:root"));
    }

    @Test
    void createPluginInsertsAddonMarketAndVersionRows() {
        service.createPlugin(validParams());

        org.mockito.ArgumentCaptor<AddonPlugin> addonCap = org.mockito.ArgumentCaptor.forClass(AddonPlugin.class);
        verify(addonPluginMapper).insert(addonCap.capture());
        AddonPlugin addon = addonCap.getValue();
        assertEquals("visitor", addon.getPluginCode());
        assertEquals("oa", addon.getServiceCode());
        assertEquals(1, addon.getStatus(), "status=1 才会出现在插件中心列表");
        assertEquals("/plugin/visitor", addon.getEntryPath());

        org.mockito.ArgumentCaptor<MarketPlugin> marketCap = org.mockito.ArgumentCaptor.forClass(MarketPlugin.class);
        verify(marketPluginMapper).insert(marketCap.capture());
        MarketPlugin market = marketCap.getValue();
        assertEquals("PAID", market.getLicenseType());
        assertEquals(99000, market.getPriceCents());
        assertEquals(0, market.getStatus(), "新建插件为草稿，不直接上架市场");

        org.mockito.ArgumentCaptor<MarketPluginVersion> versionCap = org.mockito.ArgumentCaptor.forClass(MarketPluginVersion.class);
        verify(marketPluginVersionMapper).insert(versionCap.capture());
        MarketPluginVersion version = versionCap.getValue();
        assertEquals("1.0.0", version.getVersion());
        assertTrue(version.getPluginYaml().contains("code: visitor"), "版本行必须存 plugin.yaml 全文");
        assertEquals(0, version.getStatus());
    }

    @Test
    void runtimeParameterIsRequiredAndStrict() {
        assertEquals("standalone", service.parseSpec(Map.of("code", "visitor", "name", "x",
                "hostService", "oa", "runtime", "standalone")).runtime());
        assertEquals("embedded", service.parseSpec(Map.of("code", "visitor", "name", "x",
                "hostService", "oa", "runtime", "embedded")).runtime());
        assertThrows(IllegalArgumentException.class, () -> service.parseSpec(Map.of(
                "code", "visitor", "name", "x", "hostService", "oa")));
        assertThrows(IllegalArgumentException.class, () -> service.parseSpec(Map.of(
                "code", "visitor", "name", "x", "hostService", "oa", "type", "EXAMPLE")));
        assertThrows(IllegalArgumentException.class, () -> service.parseSpec(Map.of(
                "code", "visitor", "name", "x", "hostService", "oa", "runtime", "UNKNOWN")));
    }

    @Test
    void runtimeParameterRejectsUnknownValues() {
        assertThrows(IllegalArgumentException.class, () -> service.parseSpec(
                Map.of("code", "visitor", "name", "x", "hostService", "oa", "runtime", "UNKNOWN")));
    }

    @Test
    void generatedManifestPassesPublishValidatorRequirements() throws Exception {
        service.createPlugin(Map.of("code", "visitor-log", "name", "访客流水",
                "hostService", "oa", "runtime", "embedded"));
        String yaml = Files.readString(tempDir.resolve("oa/visitor-log/plugin.yaml"));
        PluginManifest manifest = new ObjectMapper(new YAMLFactory()).readValue(yaml, PluginManifest.class);
        // PluginManifestValidator 的硬性要求：code/version/minFrameworkVersion 非空
        assertEquals("visitor-log", manifest.getCode());
        assertEquals("1.0.0", manifest.getVersion());
        assertEquals("1.0.0", manifest.getMinFrameworkVersion());
        assertEquals("embedded", manifest.getRuntime());
        assertEquals("oa", manifest.getHostService());
        assertEquals(List.of("visitor-log:root"), manifest.getPermissions());
    }

    @Test
    void duplicateCodeInAddonCatalogIsRejectedWithoutWritingFiles() {
        when(addonPluginMapper.selectCount(any())).thenReturn(1L);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.createPlugin(validParams()));
        assertTrue(error.getMessage().contains("visitor"));
        assertFalse(Files.exists(tempDir.resolve("oa/visitor")), "拒绝时不得写盘");
        verify(marketPluginMapper, never()).insert(any(MarketPlugin.class));
    }

    @Test
    void duplicateCodeInMarketCatalogIsRejected() {
        when(marketPluginMapper.selectCount(any())).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.createPlugin(validParams()));
        assertFalse(Files.exists(tempDir.resolve("oa/visitor")));
    }

    @Test
    void existingDirectoryIsRejectedInsteadOfOverwritten() throws Exception {
        Files.createDirectories(tempDir.resolve("oa/visitor"));
        Files.writeString(tempDir.resolve("oa/visitor/keep.txt"), "开发者已有内容");

        assertThrows(IllegalStateException.class, () -> service.createPlugin(validParams()));
        assertTrue(Files.exists(tempDir.resolve("oa/visitor/keep.txt")), "既有目录内容不得被覆盖");
    }

    @Test
    void dbFailureRollsBackGeneratedDirectory() {
        when(marketPluginVersionMapper.insert(any(MarketPluginVersion.class)))
                .thenThrow(new RuntimeException("db down"));

        assertThrows(RuntimeException.class, () -> service.createPlugin(validParams()));
        assertFalse(Files.exists(tempDir.resolve("oa/visitor")),
                "库写入失败时脚手架目录必须清理，不留半成品");
    }

    @Test
    void invalidCodeIsRejected() {
        for (String bad : List.of("Visitor", "1visitor", "-visitor", "visitor_", "a", "visitor--x")) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.parseSpec(Map.of("code", bad, "name", "x")),
                    "编码 " + bad + " 应被拒绝");
        }
    }

    @Test
    void reservedCodesAreRejected() {
        for (String reserved : List.of("erp", "webpos", "demo-coupon", "mall")) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.parseSpec(Map.of("code", reserved, "name", "x")),
                    "保留编码 " + reserved + " 应被拒绝");
        }
    }

    @Test
    void missingUnknownAndPathTraversalHostServicesAreRejected() {
        for (String host : List.of("", "mall", "visitor", "../oa", "OA")) {
            assertThrows(IllegalArgumentException.class, () -> service.parseSpec(
                    Map.of("code", "visitor", "name", "x", "hostService", host)));
        }
        assertThrows(IllegalArgumentException.class,
                () -> service.parseSpec(Map.of("code", "visitor", "name", "x")));
        assertThrows(IllegalArgumentException.class,
                () -> service.parseSpec(Map.of("code", "visitor", "name", "x", "hostService", "oa", "runtime", "UNKNOWN")));
    }

    @Test
    void duplicateCodeInAnotherServiceDirectoryIsRejected() throws Exception {
        Files.createDirectories(tempDir.resolve("base/visitor"));
        assertThrows(IllegalStateException.class, () -> service.createPlugin(validParams()));
        assertFalse(Files.exists(tempDir.resolve("oa/visitor")));
    }

    @Test
    void legacyFlatDirectoryIsNotShadowed() throws Exception {
        Files.createDirectories(tempDir.resolve("visitor"));
        assertThrows(IllegalStateException.class, () -> service.createPlugin(validParams()));
        assertFalse(Files.exists(tempDir.resolve("oa/visitor")));
    }

    @Test
    void customManifestMetadataSurvivesParsing() throws Exception {
        Map<String, Object> params = new java.util.HashMap<>(validParams());
        var categories = mock(AddonCateMapper.class);
        ReflectionTestUtils.setField(service, "addonCateMapper", categories);
        when(categories.selectAll()).thenReturn(List.of(Map.of("id", 77, "name", "客户办公", "sort", 3)));
        params.put("category", "客户办公");
        params.put("terminals", List.of("admin", "seller"));
        service.createPlugin(params);
        PluginManifest manifest = new ObjectMapper(new YAMLFactory()).readValue(
                Files.readString(tempDir.resolve("oa/visitor/plugin.yaml")), PluginManifest.class);
        assertEquals("客户办公", manifest.getCategory());
        assertEquals(List.of("admin", "seller"), manifest.getTerminals());
    }

    @Test
    void defaultMenuPathFollowsPluginRouteConvention() {
        PluginScaffoldService.ScaffoldSpec spec = service.parseSpec(Map.of("code", "visitor", "name", "访客管理", "hostService", "oa"));
        assertEquals("/plugin/visitor", spec.menuPath());
        assertEquals("访客管理", spec.menuTitle());
        assertEquals("hou_plugin_visitor_", spec.tablePrefix());
    }
    @Test
    void selectedCategoryIdControlsBothCatalogRowsAndManifestName() {
        var categories = mock(AddonCateMapper.class);
        ReflectionTestUtils.setField(service, "addonCateMapper", categories);
        when(categories.selectAll()).thenReturn(List.of(Map.of("id", 999, "name", "客户分类", "sort", 5)));
        var params = new java.util.HashMap<>(validParams()); params.put("cateId", 999); params.put("category", "伪造文案");
        var result = service.createPlugin(params);
        var addon = org.mockito.ArgumentCaptor.forClass(AddonPlugin.class);
        var market = org.mockito.ArgumentCaptor.forClass(MarketPlugin.class);
        verify(addonPluginMapper).insert(addon.capture()); verify(marketPluginMapper).insert(market.capture());
        assertEquals(999, addon.getValue().getCateId()); assertEquals("客户分类", addon.getValue().getCategory());
        assertEquals(999, market.getValue().getCateId()); assertEquals("客户分类", market.getValue().getCategory());
        assertTrue(result.get("pluginYaml").toString().contains("客户分类"));
    }

    @Test
    void unknownCategoryRejectedBeforeWritingFilesOrRows() {
        var categories = mock(AddonCateMapper.class);
        ReflectionTestUtils.setField(service, "addonCateMapper", categories);
        when(categories.selectAll()).thenReturn(List.of());
        var params = new java.util.HashMap<>(validParams()); params.put("cateId", 999);
        assertThrows(IllegalArgumentException.class, () -> service.createPlugin(params));
        assertFalse(Files.exists(tempDir.resolve("oa/visitor")));
        verify(addonPluginMapper, never()).insert(any(AddonPlugin.class));
    }

}
