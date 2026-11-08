package com.houjiang.commons.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 版本号比较测试（宽松口径）。
 *
 * <p>重点覆盖两类容易出错的判定：
 * <ol>
 *   <li><b>字典序陷阱</b>：{@code 1.10} 必须大于 {@code 1.9}，字符串比较会得出相反结论；</li>
 *   <li><b>正式版 &gt; 预发布版</b>：补位逻辑不能把 {@code 1.0.0} 判成低于 {@code 1.0.0-rc.1}。</li>
 * </ol>
 *
 * @author Amos
 * @since M3
 */
class VersionComparisonTest {

    @Test
    void 数字段按数值比较而不是字典序() {
        assertTrue(VersionComparison.compare("1.10.0", "1.9.0") > 0);
        assertTrue(VersionComparison.compare("1.9.0", "1.10.0") < 0);
        assertEquals(0, VersionComparison.compare("1.0.0", "1.0.0"));
    }

    @Test
    void 段数不同时缺位补零() {
        assertEquals(0, VersionComparison.compare("1.8", "1.8.0"));
        assertTrue(VersionComparison.compare("1.8", "1.8.1") < 0);
        assertTrue(VersionComparison.compare("2.0", "1.9.9") > 0);
    }

    @Test
    void 正式版高于预发布版() {
        assertTrue(VersionComparison.compare("1.0.0", "1.0.0-rc.1") > 0);
        assertTrue(VersionComparison.compare("1.0.0-rc.1", "1.0.0") < 0);
        assertTrue(VersionComparison.compare("1.0.0-rc.2", "1.0.0-rc.10") < 0);
    }

    @Test
    void atLeast在声明缺失时放行() {
        assertTrue(VersionComparison.atLeast("1.0.0", null));
        assertTrue(VersionComparison.atLeast("1.0.0", ""));
        assertTrue(VersionComparison.atLeast("1.0.0", "   "));
    }

    @Test
    void atLeast按最低要求判定() {
        assertTrue(VersionComparison.atLeast("2.0.0", "2.0.0"));
        assertTrue(VersionComparison.atLeast("2.1.0", "2.0.0"));
        assertFalse(VersionComparison.atLeast("1.8.0", "2.0.0"));
    }

    @Test
    void 宿主版本未知时按不兼容处理() {
        // fail-closed：宿主版本拿不到就无法自证兼容，不能默认放行
        assertFalse(VersionComparison.atLeast(null, "1.0.0"));
        assertFalse(VersionComparison.atLeast("", "1.0.0"));
    }
}
