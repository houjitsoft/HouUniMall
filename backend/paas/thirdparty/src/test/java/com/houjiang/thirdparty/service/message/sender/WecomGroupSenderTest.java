package com.houjiang.thirdparty.service.message.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WecomGroupSenderTest {

    @Mock
    private WecomSender wecomSender;
    @Mock
    private DingtalkSender dingtalkSender;
    @Mock
    private FeishuSender feishuSender;

    private WecomGroupSender sender;

    @BeforeEach
    void setUp() {
        sender = new WecomGroupSender(wecomSender, dingtalkSender, feishuSender);
    }

    @Test
    void delegatesRequestScopedConfigToTheSelectedGroupPlatform() {
        Map<String, Object> config = Map.of("webhook", "https://example.invalid/secret-hook");
        Map<String, Object> params = Map.of("groupPlatform", "wecom", "groupConfig", config);
        when(wecomSender.send("group-88", "标题", "内容", config, params))
                .thenReturn(SendResult.success("group-message-1"));

        SendResult result = sender.send("group-88", "标题", "内容", config, params);

        assertTrue(result.isSuccess());
        assertEquals("group-message-1", result.getMessageId());
        verify(wecomSender).send("group-88", "标题", "内容", config, params);
    }

    @Test
    void rejectsMissingOrUnsupportedPlatformWithStableErrors() {
        SendResult missing = sender.send("group-88", "标题", "内容", Map.of("webhook", "x"), Map.of());
        SendResult unsupported = sender.send(
                "group-88", "标题", "内容", Map.of("webhook", "x"),
                Map.of("groupPlatform", "unknown"));

        assertFalse(missing.isSuccess());
        assertEquals("GROUP_PLATFORM_REQUIRED", missing.getErrorCode());
        assertFalse(unsupported.isSuccess());
        assertEquals("GROUP_PLATFORM_UNSUPPORTED", unsupported.getErrorCode());
    }

    @Test
    void requiresNonEmptyRequestScopedConfig() {
        assertFalse(sender.validateConfig(Map.of()));
        assertTrue(sender.validateConfig(Map.of("webhook", "x")));
    }
}
