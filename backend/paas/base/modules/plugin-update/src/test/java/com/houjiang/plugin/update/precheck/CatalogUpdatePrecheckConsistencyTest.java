package com.houjiang.plugin.update.precheck;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.market.CatalogUpdateItem;
import com.houjiang.mall.model.market.CatalogUpdateQuery;
import com.houjiang.mall.service.market.PluginCatalogUpdateService;
import com.houjiang.plugin.update.config.UpdateProperties;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 「有更新」判定与「升级前预检」的口径一致性（M5b）。
 *
 * <p><b>为什么这个测试放在 plugin-update</b>：plugin-update 依赖 plugin-market
 * （反向会成环），所以只有这里能同时构造两侧服务；换到 market 侧就需要新增依赖边，不值得。
 *
 * <p><b>它钉死的是什么</b>：同一个 {@code dependencies_json} + 同一份依赖安装态，
 * 目录判定（能不能升）与预检（能不能装）必须给出同一个结论。
 * 这两段此前各判各的——目录只看框架和授权，依赖留到预检——
 * 直接后果是「列表页提示可升级，用户点了却被 3015 打回」。
 *
 * @author Amos
 * @since M5b
 */
class CatalogUpdatePrecheckConsistencyTest {

    private static final String INSTANCE = "cloud.houjit.com";

    private static final String CODE = "demo";

    /**
     * 同一组输入喂给两侧，断言结论一致。
     *
     * @param dependenciesJson 依赖声明（可为 null / 非法）
     * @param dependency       被依赖插件 "erp" 的安装态，null 表示没装
     * @param expectPass       true 表示预期两侧都放行
     * @param scenario         场景名，失败时用于定位
     */
    private void assertSameVerdict(String dependenciesJson, MallPluginInstall dependency,
                                   boolean expectPass, String scenario) {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);

        MarketPlugin plugin = new MarketPlugin();
        plugin.setPluginCode(CODE);
        plugin.setName(CODE);
        plugin.setLicenseType("FREE");
        plugin.setStatus(2);
        plugin.setDependenciesJson(dependenciesJson);

        MarketPluginVersion latest = new MarketPluginVersion();
        latest.setPluginCode(CODE);
        latest.setVersion("1.1.0");
        latest.setStatus(1);

        MallPluginInstall self = new MallPluginInstall();
        self.setPluginCode(CODE);
        self.setInstalled(1);
        self.setEnabled(1);
        self.setVersion("1.0.0");

        when(pluginMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(plugin);
        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of(plugin));
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(latest);
        when(versionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(latest));
        // 预检按依赖编码逐条 selectOne
        when(installMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(dependency);
        // 目录侧两次 selectList：本次入参安装态 → 依赖批量
        when(installMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(self), dependency == null ? List.of() : List.of(dependency));

        UpdateProperties properties = new UpdateProperties();
        properties.setFrameworkVersion("9.9.9");
        PluginCompatibilityPrecheckService precheck =
                new PluginCompatibilityPrecheckService(pluginMapper, versionMapper, installMapper, properties);
        PluginCatalogUpdateService catalog =
                new PluginCatalogUpdateService(pluginMapper, versionMapper, installMapper, null, null, "9.9.9");

        PrecheckResult precheckResult = precheck.precheck(CODE, "1.1.0");
        CatalogUpdateItem item = catalog.catalogUpdates(INSTANCE,
                List.of(new CatalogUpdateQuery(CODE, "1.0.0"))).get(0);

        assertEquals(expectPass, precheckResult.isPass(), scenario + "：预检结论不符合预期");
        assertEquals(expectPass, item.isUpgradable(), scenario + "：目录结论与预检不一致");
        if (!expectPass) {
            assertEquals(PluginDistErrorCode.DEPENDENCY_MISSING, precheckResult.getReason(), scenario);
            assertEquals("DEPENDENCY_MISSING", item.getBlockReason(), scenario);
        } else {
            assertEquals("1.1.0", item.getLatestVersion(), scenario);
        }
    }

    private static MallPluginInstall erp(String version, int installed, int enabled) {
        MallPluginInstall row = new MallPluginInstall();
        row.setPluginCode("erp");
        row.setInstalled(installed);
        row.setEnabled(enabled);
        row.setVersion(version);
        return row;
    }

    @Test
    void missingDependencyIsBlockedByBothSides() {
        assertSameVerdict("[\"erp\"]", null, false, "依赖未安装");
    }

    @Test
    void disabledDependencyIsBlockedByBothSides() {
        assertSameVerdict("[\"erp\"]", erp("1.0.0", 1, 0), false, "依赖已装但已停用");
    }

    @Test
    void uninstalledRowIsBlockedByBothSides() {
        assertSameVerdict("[\"erp\"]", erp("1.0.0", 0, 1), false, "依赖安装记录 installed=0");
    }

    @Test
    void outdatedDependencyIsBlockedByBothSides() {
        assertSameVerdict("[\"erp@2.0.0\"]", erp("1.9.0", 1, 1), false, "已装版本低于声明");
    }

    @Test
    void satisfiedDependencyIsAllowedByBothSides() {
        assertSameVerdict("[\"erp@2.0.0\"]", erp("2.1.0", 1, 1), true, "已装版本高于声明");
        assertSameVerdict("[\"erp@2.0.0\"]", erp("2.0.0", 1, 1), true, "已装版本等于声明");
        assertSameVerdict("[\"erp\"]", erp("1.0.0", 1, 1), true, "只要求装了且启用");
    }

    @Test
    void lenientVersionCompareIsConsistentOnBothSides() {
        // 字符串比较会得出 "1.10.0" < "1.9.0"，两侧必须同为宽松比较，不能一个放行一个拦截
        assertSameVerdict("[\"erp@1.9.0\"]", erp("1.10.0", 1, 1), true, "1.10.0 满足 >=1.9.0");
        assertSameVerdict("[\"erp@1.10.0\"]", erp("1.9.0", 1, 1), false, "1.9.0 不满足 >=1.10.0");
    }

    @Test
    void noDeclarationIsAllowedByBothSides() {
        assertSameVerdict(null, null, true, "未声明依赖");
        assertSameVerdict("", null, true, "依赖声明为空串");
        assertSameVerdict("{坏掉的 JSON", null, true, "依赖声明非法（弱校验放行）");
    }

    /**
     * 一致性不能靠「两边写得很像」——那只是把分裂推迟到下次改动。
     * 这里把「任何一侧都不许有自己的依赖解析实现」钉成断言。
     */
    @Test
    void dependencyParsingIsNotDuplicatedInEitherModule() {
        boolean precheckHasOwnParser =
                Arrays.stream(PluginCompatibilityPrecheckService.class.getDeclaredMethods())
                        .anyMatch(method -> method.getName().toLowerCase().contains("parsedependencies"));
        assertFalse(precheckHasOwnParser, "预检侧不得保留私有的依赖解析实现，必须走 commons");

        boolean catalogHasOwnParser =
                Arrays.stream(PluginCatalogUpdateService.class.getDeclaredMethods())
                        .anyMatch(method -> method.getName().toLowerCase().contains("parse"));
        assertFalse(catalogHasOwnParser, "目录侧不得自己复制一份依赖解析，必须走 commons");
    }
}
