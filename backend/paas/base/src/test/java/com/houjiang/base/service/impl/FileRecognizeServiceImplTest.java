package com.houjiang.base.service.impl;

import com.houjiang.base.client.DifyApiClient;
import com.houjiang.base.client.OpenAiCompatibleClient;
import com.houjiang.base.mapper.BaseAiConfigMapper;
import com.houjiang.base.vo.BaseAiConfigVO;
import com.houjiang.commons.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import com.alibaba.fastjson.JSONObject;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.List;

class FileRecognizeServiceImplTest {

    @Test
    void intelligentImportRejectsBlankDescriptionBeforeCallingAi() {
        FileRecognizeServiceImpl service = new FileRecognizeServiceImpl(null, null, null);

        ResponseResult result = service.recognizeForImport(
                new MockMultipartFile("file", "customer.pdf", "application/pdf", new byte[]{1}),
                "crm", "   ", null);

        assertEquals(422, result.getCode());
        assertEquals("描述不能为空", result.getMessage());
    }

    @Test
    void intelligentImportConstrainsAiToSelectedSystemAndDropsUnknownFields() {
        BaseAiConfigMapper mapper = mock(BaseAiConfigMapper.class);
        BaseAiConfigVO model = config(8, "gpt-5.6-Sol", "https://hezu.ink/v1", 1, 20);
        when(mapper.getDefaultConfigList()).thenReturn(List.of(model));
        OpenAiCompatibleClient client = mock(OpenAiCompatibleClient.class);
        JSONObject fields = new JSONObject();
        fields.put("name", "华东客户");
        fields.put("mobile", "13800000000");
        fields.put("dropTable", "never");
        JSONObject ai = new JSONObject();
        ai.put("systemKey", "crm");
        ai.put("functionKey", "customer");
        ai.put("objectKey", "customer");
        ai.put("fields", fields);
        ai.put("summary", "识别为 CRM 客户");
        when(client.recognizeFile(eq(model), any(), any(), any())).thenReturn(ai);
        FileRecognizeServiceImpl service = new FileRecognizeServiceImpl(mapper, client, mock(DifyApiClient.class));

        ResponseResult result = service.recognizeForImport(
                new MockMultipartFile("file", "customer.pdf", "application/pdf", new byte[]{1}),
                "crm", "请录入客户资料", null);

        assertEquals(1, result.getCode());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals("crm", data.get("systemKey"));
        assertEquals("customer", data.get("objectKey"));
        assertEquals("hou_crm_customer", data.get("targetTable"));
        assertEquals(false, data.get("needsManualTarget"));
        Map<?, ?> normalizedFields = (Map<?, ?>) data.get("fields");
        assertEquals("华东客户", normalizedFields.get("name"));
        assertEquals(false, normalizedFields.containsKey("dropTable"));
        assertTrue(String.valueOf(data.get("validationErrors")).contains("dropTable"));

        ArgumentCaptor<String> instruction = ArgumentCaptor.forClass(String.class);
        verify(client).recognizeFile(eq(model), any(), eq("请录入客户资料"), instruction.capture());
        assertTrue(instruction.getValue().contains("crm.customer"));
        assertTrue(instruction.getValue().contains("crm.business"));
        assertFalse(instruction.getValue().contains("erp."));
        assertFalse(instruction.getValue().contains("hou_crm_customer"));
    }

    @Test
    void intelligentImportUsesManualTargetFallbackForUnknownAiObject() {
        BaseAiConfigMapper mapper = mock(BaseAiConfigMapper.class);
        BaseAiConfigVO model = config(9, "gpt-5.6-Sol", "https://hezu.ink/v1", 1, 20);
        when(mapper.getDefaultConfigList()).thenReturn(List.of(model));
        OpenAiCompatibleClient client = mock(OpenAiCompatibleClient.class);
        JSONObject ai = new JSONObject();
        ai.put("systemKey", "crm");
        ai.put("objectKey", "arbitrary_sql");
        ai.put("tableName", "user_supplied_table");
        ai.put("sql", "delete from anything");
        ai.put("fields", new JSONObject());
        when(client.recognizeFile(eq(model), any(), any(), any())).thenReturn(ai);
        FileRecognizeServiceImpl service = new FileRecognizeServiceImpl(mapper, client, mock(DifyApiClient.class));

        ResponseResult result = service.recognizeForImport(
                new MockMultipartFile("file", "unknown.pdf", "application/pdf", new byte[]{1}),
                "crm", "识别这份资料", null);

        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals(true, data.get("needsManualTarget"));
        assertEquals(null, data.get("objectKey"));
        assertFalse(data.containsKey("targetTable"));
        assertFalse(data.toString().contains("delete from anything"));
        assertFalse(data.toString().contains("user_supplied_table"));
    }

    @Test
    void fallsBackToRuleRecognitionWhenAiConfigLookupFails() {
        BaseAiConfigMapper mapper = mock(BaseAiConfigMapper.class);
        when(mapper.getDefaultConfigList()).thenThrow(new RuntimeException("Read timed out"));
        FileRecognizeServiceImpl service = new FileRecognizeServiceImpl(
                mapper, mock(OpenAiCompatibleClient.class), mock(DifyApiClient.class));

        ResponseResult result = service.recognize(
                new MockMultipartFile("file", "invoice.png", "image/png", new byte[]{1, 2, 3}),
                null, null);

        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertEquals("rule", ((java.util.Map<?, ?>) result.getData()).get("source"));
        String summary = String.valueOf(((java.util.Map<?, ?>) result.getData()).get("summary"));
        org.junit.jupiter.api.Assertions.assertFalse(summary.contains("OCR"));
        String warning = String.valueOf(((java.util.Map<?, ?>) result.getData()).get("recognitionWarning"));
        org.junit.jupiter.api.Assertions.assertTrue(warning.contains("读取 AI 模型配置失败"));
        org.junit.jupiter.api.Assertions.assertFalse(warning.contains("未配置可用的视觉"));
    }

    @Test
    void usesEnabledVisionModelWhenConfiguredModelIsNotMarkedDefault() {
        BaseAiConfigMapper mapper = mock(BaseAiConfigMapper.class);
        BaseAiConfigVO textDefault = config(1, "deepseek-v4-flash", "https://api.deepseek.com/v1", 0, 5);
        BaseAiConfigVO openAiVision = config(2, "gpt-4o", "https://api.openai.com/v1", 1, 10);
        when(mapper.getDefaultConfigList()).thenReturn(List.of(textDefault));
        when(mapper.queryEnabledConfigs()).thenReturn(List.of(textDefault, openAiVision));

        OpenAiCompatibleClient client = mock(OpenAiCompatibleClient.class);
        JSONObject ai = new JSONObject();
        ai.put("type", "receivable");
        ai.put("summary", "收入 22000 元");
        when(client.recognizeFile(eq(openAiVision), any(), any())).thenReturn(ai);

        FileRecognizeServiceImpl service = new FileRecognizeServiceImpl(mapper, client, mock(DifyApiClient.class));
        ResponseResult result = service.recognize(
                new MockMultipartFile("file", "receipt.png", "image/png", new byte[]{1, 2, 3}),
                "收入账簿凭证", null);

        assertEquals("system-ai", ((java.util.Map<?, ?>) result.getData()).get("source"));
        assertEquals("gpt-4o", ((java.util.Map<?, ?>) result.getData()).get("aiModel"));
        verify(client).recognizeFile(eq(openAiVision), any(), eq("收入账簿凭证"));
    }

    @Test
    void usesGpt56SolAsVisionModelForImageRecognition() {
        BaseAiConfigMapper mapper = mock(BaseAiConfigMapper.class);
        BaseAiConfigVO gpt56Sol = config(3, "gpt-5.6-Sol", "https://hezu.ink/v1", 1, 20);
        when(mapper.getDefaultConfigList()).thenReturn(List.of(gpt56Sol));
        when(mapper.queryEnabledConfigs()).thenReturn(List.of(gpt56Sol));

        OpenAiCompatibleClient client = mock(OpenAiCompatibleClient.class);
        JSONObject ai = new JSONObject();
        ai.put("type", "unknown");
        ai.put("summary", "识别完成");
        when(client.recognizeFile(eq(gpt56Sol), any(), any())).thenReturn(ai);

        FileRecognizeServiceImpl service = new FileRecognizeServiceImpl(mapper, client, mock(DifyApiClient.class));
        ResponseResult result = service.recognize(
                new MockMultipartFile("file", "企业微信截图.png", "image/png", new byte[]{1, 2, 3}),
                null, null);

        assertEquals("system-ai", ((java.util.Map<?, ?>) result.getData()).get("source"));
        assertEquals("gpt-5.6-Sol", ((java.util.Map<?, ?>) result.getData()).get("aiModel"));
        verify(client).recognizeFile(eq(gpt56Sol), any(), eq(null));
    }

    @Test
    void reportsConfiguredModelFailureInsteadOfMissingModel() {
        BaseAiConfigMapper mapper = mock(BaseAiConfigMapper.class);
        BaseAiConfigVO vision = config(4, "gpt-4o", "https://api.openai.com/v1", 1, 10);
        when(mapper.getDefaultConfigList()).thenReturn(List.of(vision));
        when(mapper.queryEnabledConfigs()).thenReturn(List.of(vision));
        OpenAiCompatibleClient client = mock(OpenAiCompatibleClient.class);
        when(client.recognizeFile(eq(vision), any(), any()))
                .thenThrow(new IllegalStateException("默认模型调用失败: HTTP 503"));

        FileRecognizeServiceImpl service = new FileRecognizeServiceImpl(mapper, client, mock(DifyApiClient.class));
        ResponseResult result = service.recognize(
                new MockMultipartFile("file", "receipt.png", "image/png", new byte[]{1, 2, 3}),
                "收入账簿凭证", null);

        java.util.Map<?, ?> data = (java.util.Map<?, ?>) result.getData();
        assertEquals("rule", data.get("source"));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(data.get("recognitionWarning")).contains("gpt-4o"));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(data.get("recognitionWarning")).contains("全部调用失败"));
        org.junit.jupiter.api.Assertions.assertFalse(String.valueOf(data.get("recognitionWarning")).contains("未配置"));
    }

    @Test
    void switchesToNextVisionModelWhenPrimaryFails() {
        BaseAiConfigMapper mapper = mock(BaseAiConfigMapper.class);
        BaseAiConfigVO primary = config(977, "gpt-4o", "https://hezubus.cc/v1", 1, 10);
        BaseAiConfigVO backup = config(37, "qwen-vl-plus", "https://dashscope.aliyuncs.com/compatible-mode/v1", 1, 8);
        when(mapper.getDefaultConfigList()).thenReturn(List.of(primary));
        when(mapper.queryEnabledConfigs()).thenReturn(List.of(primary, backup));

        OpenAiCompatibleClient client = mock(OpenAiCompatibleClient.class);
        when(client.recognizeFile(eq(primary), any(), any()))
                .thenThrow(new IllegalStateException("默认模型调用失败: HTTP 503"));
        JSONObject ai = new JSONObject();
        ai.put("type", "receivable");
        ai.put("summary", "金额 22000");
        ai.put("fullText", "收入凭证 金额:22000");
        when(client.recognizeFile(eq(backup), any(), any())).thenReturn(ai);

        FileRecognizeServiceImpl service = new FileRecognizeServiceImpl(mapper, client, mock(DifyApiClient.class));
        ResponseResult result = service.recognize(
                new MockMultipartFile("file", "企业微信截图.png", "image/png", new byte[]{1, 2, 3}),
                "收入账簿凭证", null);

        java.util.Map<?, ?> data = (java.util.Map<?, ?>) result.getData();
        assertEquals("system-ai", data.get("source"));
        assertEquals("qwen-vl-plus", data.get("aiModel"));
        assertEquals("金额 22000", data.get("summary"));
        verify(client, times(1)).recognizeFile(eq(primary), any(), any());
        verify(client, times(1)).recognizeFile(eq(backup), any(), any());
    }

    private static BaseAiConfigVO config(int id, String model, String endpoint, int supportVision, int priority) {
        BaseAiConfigVO config = new BaseAiConfigVO();
        config.setId(id);
        config.setModelName(model);
        config.setApiEndpoint(endpoint);
        config.setApiKey("test-key");
        config.setIsEnabled(1);
        config.setStatus(1);
        config.setPriority(priority);
        config.setSupportVision(supportVision);
        return config;
    }
}
