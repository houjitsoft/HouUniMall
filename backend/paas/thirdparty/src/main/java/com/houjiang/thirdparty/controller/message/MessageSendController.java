package com.houjiang.thirdparty.controller.message;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendRequest;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendResponse;
import com.houjiang.thirdparty.service.message.MessageSendService;
import com.houjiang.thirdparty.service.message.sender.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统一消息发送控制器。
 * @author Amos
 */
@RestController
@RequestMapping("/message/send")
public class MessageSendController {

    @Autowired
    private MessageSendService messageSendService;

    /**
     * 按模板发送消息。
     *
     * @param body 请求体，含 templateCode、receiver、params
     * @return 各渠道发送结果
     */
    @PostMapping("/by-template")
    public ResponseResult<List<SendResult>> sendByTemplate(@RequestBody Map<String, Object> body) {
        String templateCode = body.get("templateCode") != null ? body.get("templateCode").toString() : null;
        String receiver = body.get("receiver") != null ? body.get("receiver").toString() : null;
        @SuppressWarnings("unchecked")
        Map<String, Object> params = body.get("params") instanceof Map
                ? (Map<String, Object>) body.get("params")
                : null;
        if (templateCode == null || templateCode.isBlank()) {
            return ResponseResult.error("templateCode 不能为空");
        }
        if (receiver == null || receiver.isBlank()) {
            return ResponseResult.error("receiver 不能为空");
        }
        List<SendResult> results = messageSendService.sendByTemplate(templateCode, receiver, params);
        return ResponseResult.success(results);
    }

    /**
     * 按指定渠道发送消息。
     *
     * @param body 请求体，含 channelCode、receiver、title、content、params
     * @return 发送结果
     */
    @PostMapping("/by-channel")
    public ResponseResult<SendResult> sendByChannel(@RequestBody Map<String, Object> body) {
        String channelCode = body.get("channelCode") != null ? body.get("channelCode").toString() : null;
        String receiver = body.get("receiver") != null ? body.get("receiver").toString() : null;
        String title = body.get("title") != null ? body.get("title").toString() : "";
        String content = body.get("content") != null ? body.get("content").toString() : "";
        @SuppressWarnings("unchecked")
        Map<String, Object> params = body.get("params") instanceof Map
                ? (Map<String, Object>) body.get("params")
                : null;
        if (channelCode == null || channelCode.isBlank()) {
            return ResponseResult.error("channelCode 不能为空");
        }
        if (receiver == null || receiver.isBlank()) {
            return ResponseResult.error("receiver 不能为空");
        }
        String idempotencyKey = text(body.get("idempotencyKey"));
        if ((idempotencyKey == null || idempotencyKey.isBlank()) && params != null) {
            idempotencyKey = text(params.get("idempotencyKey"));
        }
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            IdempotentMessageSendResponse response = messageSendService.sendIdempotently(
                    new IdempotentMessageSendRequest(
                            idempotencyKey,
                            channelCode,
                            receiver,
                            text(body.get("receiverName")),
                            title,
                            content,
                            params));
            SendResult result = response.success()
                    ? SendResult.success(response.messageId())
                    : SendResult.fail(response.errorCode(), response.errorMessage());
            result.setIdempotentReplay(response.idempotentReplay());
            return ResponseResult.success(result);
        }
        SendResult result = messageSendService.sendByChannel(channelCode, receiver, title, content, params);
        if (result.isSuccess()) {
            return ResponseResult.success(result);
        }
        return ResponseResult.error(result.getErrorMessage());
    }

    private String text(Object value) {
        return value == null ? null : value.toString();
    }
}
