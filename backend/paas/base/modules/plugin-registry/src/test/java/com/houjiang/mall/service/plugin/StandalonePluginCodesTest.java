package com.houjiang.mall.service.plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 独立服务编码名单的契约测试。
 *
 * <p>防止再出现「插件已改成独立容器，但平台侧名单没跟上」的情况：
 * 名单一旦漏项，该插件的启停与安装会误走 PF4J 内嵌链路。
 */
class StandalonePluginCodesTest {

    @Test
    void aiGatewayIsStandalone() {
        assertTrue(StandalonePluginCodes.isStandalone("aigateway"),
                "AI 网关已改为独立容器（unimall-aigateway），必须按独立服务处理");
    }

    @Test
    void commercialStandaloneServicesRemainStandalone() {
        for (String code : new String[]{"crm", "cms", "erp", "finance", "geo", "im",
                "logging", "mes", "oa", "orderfood", "pm", "rider", "supplier", "webpos"}) {
            assertTrue(StandalonePluginCodes.isStandalone(code), code + " 应保持独立服务形态");
        }
    }

    @Test
    void embeddedExtensionsAreNotStandalone() {
        for (String code : new String[]{"activity", "ocr", "aicode", "unipay", "unilogin", "lowcode"}) {
            assertFalse(StandalonePluginCodes.isStandalone(code), code + " 是内嵌扩展，不应被判为独立服务");
        }
    }

    @Test
    void unknownOrBlankCodeIsNotStandalone() {
        assertFalse(StandalonePluginCodes.isStandalone(null));
        assertFalse(StandalonePluginCodes.isStandalone(""));
        assertFalse(StandalonePluginCodes.isStandalone("   "));
        assertFalse(StandalonePluginCodes.isStandalone("not-exist"));
    }

    @Test
    void surroundingWhitespaceIsTolerated() {
        assertTrue(StandalonePluginCodes.isStandalone(" aigateway "));
    }
}
