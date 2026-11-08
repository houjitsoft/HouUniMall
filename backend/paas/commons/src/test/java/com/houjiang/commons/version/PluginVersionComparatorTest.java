package com.houjiang.commons.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link PluginVersionComparator} 单测。
 *
 * <p>重点：版本号必须按 SemVer 比较，字符串比较会得出 {@code "1.10.0" < "1.9.0"} 的错误结论，
 * 直接导致「有更新」判定失效、客户端反复提示升级到旧版本。
 *
 * @author Amos
 * @since M1
 */
class PluginVersionComparatorTest {

    @Test
    @DisplayName("1.10.0 必须大于 1.9.0（字符串比较会得出相反结论）")
    void compareMinorTenAgainstNine() {
        assertTrue(PluginVersionComparator.compare("1.10.0", "1.9.0") > 0);
        assertTrue(PluginVersionComparator.isUpgrade("1.9.0", "1.10.0"));
        assertFalse(PluginVersionComparator.isUpgrade("1.10.0", "1.9.0"));

        // 反证：字符串比较在这个用例上确实是错的，说明为什么必须做 SemVer 比较
        assertTrue("1.10.0".compareTo("1.9.0") < 0);
    }

    @Test
    @DisplayName("主/次/修订号逐级比较")
    void compareBySegments() {
        assertTrue(PluginVersionComparator.compare("2.0.0", "1.99.99") > 0);
        assertTrue(PluginVersionComparator.compare("1.0.1", "1.0.0") > 0);
        assertEquals(0, PluginVersionComparator.compare("1.2.3", "1.2.3"));
        assertTrue(PluginVersionComparator.compare("1.2.3", "1.2.4") < 0);
    }

    @Test
    @DisplayName("正式版高于预发布版")
    void releaseGreaterThanPreRelease() {
        assertTrue(PluginVersionComparator.compare("1.0.0", "1.0.0-rc.1") > 0);
        assertTrue(PluginVersionComparator.isUpgrade("1.0.0-rc.1", "1.0.0"));
        assertFalse(PluginVersionComparator.isUpgrade("1.0.0", "1.0.0-rc.1"));
    }

    @Test
    @DisplayName("预发布段按数字段比数值：rc.10 > rc.2")
    void comparePreReleaseNumerically() {
        assertTrue(PluginVersionComparator.compare("1.0.0-rc.10", "1.0.0-rc.2") > 0);
        assertTrue(PluginVersionComparator.compare("1.0.0-alpha.1", "1.0.0-alpha.2") < 0);
        assertTrue(PluginVersionComparator.compare("1.0.0-beta", "1.0.0-alpha") > 0);
    }

    @Test
    @DisplayName("合法性判定与解析")
    void validateSemVer() {
        assertTrue(PluginVersionComparator.isValid("1.0.0"));
        assertTrue(PluginVersionComparator.isValid("1.0.0-rc.1"));
        assertFalse(PluginVersionComparator.isValid("v1.0.0"));
        assertFalse(PluginVersionComparator.isValid("1.0"));
        assertFalse(PluginVersionComparator.isValid(""));
        assertFalse(PluginVersionComparator.isValid(null));

        PluginVersionComparator.SemVer semVer = PluginVersionComparator.parse("1.0.0");
        assertEquals(1, semVer.getMajor());
        assertEquals(0, semVer.getMinor());
        assertEquals(0, semVer.getPatch());
        assertTrue(semVer.isRelease());
        assertThrows(IllegalArgumentException.class, () -> PluginVersionComparator.parse("nope"));
    }

    @Test
    @DisplayName("未安装（current 为空）时任何合法版本都算升级")
    void upgradeFromNothing() {
        assertTrue(PluginVersionComparator.isUpgrade(null, "1.0.0"));
        assertTrue(PluginVersionComparator.isUpgrade("  ", "1.0.0"));
        assertFalse(PluginVersionComparator.isUpgrade(null, "1.0"));
    }

    @Test
    @DisplayName("maxOf 按 SemVer 取最大，非法版本跳过而不是抛异常")
    void maxOfPicksSemVerMax() {
        assertEquals("1.10.0", PluginVersionComparator.maxOf(java.util.List.of("1.9.0", "1.10.0", "1.2.0")));
        assertEquals("2.0.0", PluginVersionComparator.maxOf(java.util.List.of("2.0.0", "1.99.99")));
        assertEquals("1.0.0", PluginVersionComparator.maxOf(java.util.List.of("v1.2", "1.0.0")),
                "脏版本必须跳过，不能让一条历史数据打断刷新");
        assertNull(PluginVersionComparator.maxOf(java.util.List.of("v1.2", "1.0")));
        assertNull(PluginVersionComparator.maxOf(java.util.List.of()));
        assertNull(PluginVersionComparator.maxOf(null));
    }
}
