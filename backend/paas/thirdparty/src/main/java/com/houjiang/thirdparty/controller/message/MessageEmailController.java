package com.houjiang.thirdparty.controller.message;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.service.message.EmailSettingsService;
import com.houjiang.thirdparty.service.message.sender.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件设置控制器：SMTP 配置读写与测试发送。
 * @author Amos
 */
@RestController
@RequestMapping("/message/email")
public class MessageEmailController {

    @Autowired
    private EmailSettingsService emailSettingsService;

    /**
     * 获取邮件配置。
     *
     * @return 邮件配置（密码脱敏）
     */
    @GetMapping("/settings")
    public ResponseResult getSettings() {
        return ResponseResult.success(emailSettingsService.getSettings());
    }

    /**
     * 保存邮件配置。
     *
     * @param body 配置内容
     * @return 操作结果
     */
    @PostMapping("/settings")
    public ResponseResult saveSettings(@RequestBody Map<String, Object> body) {
        boolean ok = emailSettingsService.saveSettings(body);
        return ok ? ResponseResult.success("保存成功") : ResponseResult.fail(500, "保存失败");
    }

    /**
     * 发送测试邮件。
     *
     * @param body 包含 testEmail 或 receiver 字段
     * @return 发送结果
     */
    @PostMapping("/test")
    public ResponseResult sendTest(@RequestBody Map<String, Object> body) {
        String receiver = body != null && body.get("testEmail") != null
                ? body.get("testEmail").toString()
                : (body != null && body.get("receiver") != null ? body.get("receiver").toString() : null);
        if (!StringUtils.hasText(receiver)) {
            return ResponseResult.fail(400, "请输入测试邮箱地址");
        }
        SendResult result = emailSettingsService.sendTestEmail(receiver.trim());
        if (result.isSuccess()) {
            Map<String, Object> data = new HashMap<>();
            data.put("messageId", result.getMessageId());
            data.put("queued", "queued".equals(result.getMessageId()));
            return ResponseResult.success("测试邮件发送成功", data);
        }
        return ResponseResult.fail(500, result.getErrorMessage() != null ? result.getErrorMessage() : "发送失败");
    }
}
