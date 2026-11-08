package com.houjiang.thirdparty.controller.message;

import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.entity.message.JPushConfigEntity;
import com.houjiang.thirdparty.entity.message.JPushHistoryEntity;
import com.houjiang.thirdparty.service.message.JPushConfigService;
import com.houjiang.thirdparty.service.message.JPushHistoryService;
import com.houjiang.thirdparty.service.message.MessageRequestContextResolver;
import com.houjiang.thirdparty.service.message.sender.JPushSender;
import com.houjiang.thirdparty.service.message.sender.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 极光推送管理控制器
 * <p>
 * 提供极光推送的配置管理、推送发送、历史记录、统计监控等功能
 *
 * @author Amos
 * @since 2025-07-02
 */
@Slf4j
@RestController
@RequestMapping("/message/jpush")
public class JPushController {

    @Autowired
    private JPushConfigService jPushConfigService;

    @Autowired
    private JPushHistoryService jPushHistoryService;

    @Autowired
    private JPushSender jPushSender;

    @Autowired
    private MessageRequestContextResolver contextResolver;

    // ==================== 配置管理 ====================

    /**
     * 获取极光推送配置
     */
    @GetMapping("/config")
    public ResponseResult getConfig(@RequestParam(required = false) String orgId) {
        orgId = contextResolver.resolveCurrent().orgId();
        JPushConfigEntity config = jPushConfigService.getConfigByOrgId(orgId);
        if (config == null) {
            // 返回空配置模板
            JPushConfigEntity empty = new JPushConfigEntity();
            empty.setOrgId(orgId);
            empty.setStatus(0);
            empty.setApnsEnabled(0);
            empty.setApnsProduction(0);
            empty.setChannel("all");
            return ResponseResult.success(empty);
        }
        return ResponseResult.success(config);
    }

    /**
     * 保存极光推送配置
     */
    @PostMapping("/config/save")
    @SysLog(value = "保存极光推送配置", module = "message")
    public ResponseResult saveConfig(@RequestBody JPushConfigEntity config) {
        if (config == null) {
            return ResponseResult.fail(400, "配置不能为空");
        }
        config.setOrgId(contextResolver.resolveCurrent().orgId());
        boolean result = jPushConfigService.saveOrUpdateConfig(config);
        if (result) {
            return ResponseResult.success("保存成功");
        }
        return ResponseResult.fail(500, "保存失败");
    }

    /**
     * 测试极光推送连接
     */
    @PostMapping("/config/test")
    @SysLog(value = "测试极光推送连接", module = "message")
    public ResponseResult testConnection(@RequestBody Map<String, String> params) {
        String orgId = contextResolver.resolveCurrent().orgId();
        Map<String, Object> result = jPushConfigService.testConnection(orgId);
        return ResponseResult.success(result);
    }

    // ==================== 推送发送 ====================

    /**
     * 发送推送消息
     */
    @PostMapping("/push/send")
    @SysLog(value = "发送极光推送消息", module = "message")
    public ResponseResult sendPush(@RequestBody Map<String, Object> params) {
        String orgId = contextResolver.resolveCurrent().orgId();
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        String pushType = (String) params.get("pushType");
        String targetType = (String) params.get("targetType");
        String targetValue = params.get("targetValue") != null ? params.get("targetValue").toString() : "";
        String platform = params.get("platform") != null ? params.get("platform").toString() : "all";
        String msgType = params.get("msgType") != null ? params.get("msgType").toString() : "notification";
        String extras = params.get("extras") != null ? params.get("extras").toString() : null;

        if (title == null || title.isEmpty()) {
            return ResponseResult.fail(400, "推送标题不能为空");
        }
        if (content == null || content.isEmpty()) {
            return ResponseResult.fail(400, "推送内容不能为空");
        }

        // 获取完整配置
        JPushConfigEntity config = jPushConfigService.getFullConfigByOrgId(orgId);
        if (config == null || config.getStatus() == null || config.getStatus() != 1) {
            return ResponseResult.fail(400, "极光推送未配置或未启用");
        }

        // 构建发送参数
        Map<String, Object> sendParams = new HashMap<>();
        sendParams.put("targetType", targetType != null ? targetType : "all");
        sendParams.put("msgType", msgType);
        sendParams.put("platform", platform);
        if (extras != null) {
            sendParams.put("extras", extras);
        }

        // 先创建历史记录
        JPushHistoryEntity history = new JPushHistoryEntity();
        history.setOrgId(orgId);
        history.setPushType(pushType != null ? pushType : "broadcast");
        history.setTitle(title);
        history.setContent(content);
        history.setTargetType(targetType);
        history.setTargetValue(targetValue);
        history.setPlatform(platform);
        history.setMsgType(msgType);
        history.setExtras(extras);
        // 待发送
        history.setSendStatus(0);
        jPushHistoryService.saveHistory(history);

        // 发送推送
        Map<String, Object> channelConfig = new HashMap<>();
        channelConfig.put("appKey", config.getAppKey());
        channelConfig.put("masterSecret", config.getMasterSecret());

        SendResult result = jPushSender.send(targetValue, title, content, channelConfig, sendParams);

        // 更新历史记录
        if (result.isSuccess()) {
            jPushHistoryService.updateSendStatus(orgId, history.getId(), 1, result.getMessageId(),
                    1, 0, null);
        } else {
            jPushHistoryService.updateSendStatus(orgId, history.getId(), 2, null,
                    0, 1, result.getErrorMessage());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("success", result.isSuccess());
        data.put("messageId", result.getMessageId());
        data.put("errorMessage", result.getErrorMessage());
        return ResponseResult.success(data);
    }

    // ==================== 历史记录 ====================

    /**
     * 获取推送历史列表
     */
    @GetMapping("/history/list")
    public ResponseResult getHistoryList(@RequestParam String orgId,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "20") Integer size,
                                         @RequestParam(required = false) String pushType,
                                         @RequestParam(required = false) Integer sendStatus) {
        orgId = contextResolver.resolveCurrent().orgId();
        Map<String, Object> data = jPushHistoryService.getHistoryPage(orgId, page, size, pushType, sendStatus);
        return ResponseResult.success(data);
    }

    /**
     * 获取推送历史详情
     *
     * @param id 记录ID
     * @return 推送历史详情
     */
    @GetMapping("/history/{id}")
    public ResponseResult getHistoryDetail(@PathVariable Long id) {
        String orgId = contextResolver.resolveCurrent().orgId();
        JPushHistoryEntity entity = jPushHistoryService.getById(orgId, id);
        if (entity == null) {
            return ResponseResult.fail(404, "记录不存在");
        }
        return ResponseResult.success(entity);
    }

    // ==================== 统计监控 ====================

    /**
     * 获取推送统计数据
     */
    @GetMapping("/statistics")
    public ResponseResult getStatistics(@RequestParam String orgId) {
        orgId = contextResolver.resolveCurrent().orgId();
        Map<String, Object> stats = jPushHistoryService.getStatistics(orgId);
        return ResponseResult.success(stats);
    }
}
