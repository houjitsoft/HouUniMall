package com.houjiang.base.client;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenAiCompatibleClientTest {

    @Test
    void mapsDashscopeApiEndpointToOpenAiCompatibleEndpoint() {
        OpenAiCompatibleClient client = new OpenAiCompatibleClient();

        String url = ReflectionTestUtils.invokeMethod(client, "normalizeChatUrl",
                "https://dashscope.aliyuncs.com/api/v1");

        assertEquals("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", url);
    }

    @Test
    void extractsAmountAndDateWhenVisionModelReturnsPlainText() {
        OpenAiCompatibleClient client = new OpenAiCompatibleClient();

        JSONObject result = client.parseRecognizeJson(
                "这是收入凭证。金额：12,345.60元，发生日期：2026年9月3日。\n收款方：示例公司");

        assertEquals("document", result.getString("type"));
        assertEquals("12345.60", result.getJSONObject("fields").getString("amount"));
        assertEquals("2026年9月3日", result.getJSONObject("fields").getString("date"));
        assertEquals(true, result.getString("fullText").contains("收入凭证"));
    }

    @Test
    void enrichesStructuredJsonWithoutOverwritingExplicitFields() {
        OpenAiCompatibleClient client = new OpenAiCompatibleClient();

        JSONObject result = client.parseRecognizeJson(
                "{\"type\":\"receivable\",\"summary\":\"金额：999元，日期：2026-09-03\","
                        + "\"fields\":{\"amount\":\"88\"}}");

        assertEquals("receivable", result.getString("type"));
        assertEquals("88", result.getJSONObject("fields").getString("amount"));
        assertEquals("2026-09-03", result.getJSONObject("fields").getString("date"));
    }

    @Test
    void normalizesCurrencyPrefixInAmountField() {
        OpenAiCompatibleClient client = new OpenAiCompatibleClient();

        JSONObject result = client.parseRecognizeJson(
                "{\"type\":\"receivable\",\"fields\":{\"amount\":\"人民币: 32,000.00\","
                        + "\"date\":\"2026-09-03\"}}");

        assertEquals("32000.00", result.getJSONObject("fields").getString("amount"));
    }
}
