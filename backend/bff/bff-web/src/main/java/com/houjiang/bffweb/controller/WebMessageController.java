package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.MessageServiceFeign;
import com.houjiang.bffweb.feign.MessageServiceFeignFallback;
import feign.FeignException;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息中心Web控制器
/**
 * 
 * @description 提供消息中心的渠道管理、模板管理、发送记录、收件箱、订阅设置等功能
 * @author Amos
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/web/message")
public class WebMessageController {

    @Autowired
    private MessageServiceFeign messageServiceFeign;

    @Autowired
    private MessageServiceFeignFallback messageServiceFeignFallback;

    @ExceptionHandler(FeignException.class)
    public ResponseResult handleMessageServiceFailure(FeignException cause) {
        return messageServiceFeignFallback.response(cause, "controller");
    }

    // ==================== 渠道管理 ====================

    /**
     * 获取渠道列表
     */
    @GetMapping("/channel/list")
    public ResponseResult getChannelList(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "20") Integer size) {
        return messageServiceFeign.getChannelList(page, size);
    }

    /**
     * 获取启用的渠道列表
     */
    @GetMapping("/channel/enabled")
    public ResponseResult getEnabledChannels() {
        return messageServiceFeign.getEnabledChannels();
    }

    /**
     * 获取渠道详情
     */
    @GetMapping("/channel/{id}")
    public ResponseResult getChannelById(@PathVariable("id") Integer id) {
        return messageServiceFeign.getChannelById(id);
    }

    /**
     * 创建渠道
     */
    @PostMapping("/channel/create")
    public ResponseResult createChannel(@RequestBody Map<String, Object> channel) {
        return messageServiceFeign.createChannel(channel);
    }

    /**
     * 更新渠道
     */
    @PostMapping("/channel/update")
    public ResponseResult updateChannel(@RequestBody Map<String, Object> channel) {
        return messageServiceFeign.updateChannel(channel);
    }

    /**
     * 更新渠道状态
     */
    @PostMapping("/channel/{id}/status")
    public ResponseResult updateChannelStatus(@PathVariable("id") Integer id, @RequestParam Integer status) {
        return messageServiceFeign.updateChannelStatus(id, status);
    }

    /**
     * 删除渠道
     */
    @DeleteMapping("/channel/{id}")
    public ResponseResult deleteChannel(@PathVariable("id") Integer id) {
        return messageServiceFeign.deleteChannel(id);
    }

    // ==================== 模板管理 ====================

    /**
     * 获取模板列表
     */
    @PostMapping("/template/list")
    public ResponseResult getTemplateList(@RequestBody(required = false) Map<String, Object> params) {
        try {
            // 如果参数为空，使用默认值
            if (params == null) {
                params = new java.util.HashMap<>();
            }
            // 确保 page 和 size 有默认值
            if (!params.containsKey("page") || params.get("page") == null) {
                params.put("page", 1);
            }
            if (!params.containsKey("size") || params.get("size") == null) {
                params.put("size", 20);
            }
            return messageServiceFeign.getTemplateList(params);
        } catch (FeignException.ServiceUnavailable e) {
            // 服务不可用，返回友好的错误信息
            return ResponseResult.error("消息服务暂时不可用，请检查 thirdparty 服务是否已启动并注册到 Nacos（命名空间可能不匹配）");
        } catch (FeignException e) {
            // 其他 Feign 异常
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Load balancer does not contain an instance")) {
                return ResponseResult.error("消息服务暂时不可用，请检查 thirdparty 服务是否已启动并注册到 Nacos（命名空间可能不匹配）");
            }
            return ResponseResult.error("调用消息服务失败: " + errorMsg);
        } catch (Exception e) {
            // 其他异常
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Load balancer does not contain an instance")) {
                return ResponseResult.error("消息服务暂时不可用，请检查 thirdparty 服务是否已启动并注册到 Nacos（命名空间可能不匹配）");
            }
            e.printStackTrace();
            return ResponseResult.error("获取模板列表失败: " + errorMsg);
        }
    }

    /**
     * 获取启用的模板列表
     */
    @GetMapping("/template/enabled")
    public ResponseResult getEnabledTemplates() {
        return messageServiceFeign.getEnabledTemplates();
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/template/{id}")
    public ResponseResult getTemplateById(@PathVariable("id") Integer id) {
        return messageServiceFeign.getTemplateById(id);
    }

    /**
     * 根据模板编码获取模板
     */
    @GetMapping("/template/code/{code}")
    public ResponseResult getTemplateByCode(@PathVariable("code") String code) {
        return messageServiceFeign.getTemplateByCode(code);
    }

    /**
     * 创建模板
     */
    @PostMapping("/template/create")
    public ResponseResult createTemplate(@RequestBody Map<String, Object> template) {
        return messageServiceFeign.createTemplate(template);
    }

    /**
     * 更新模板
     */
    @PostMapping("/template/update")
    public ResponseResult updateTemplate(@RequestBody Map<String, Object> template) {
        return messageServiceFeign.updateTemplate(template);
    }

    /**
     * 更新模板状态
     */
    @PostMapping("/template/{id}/status")
    public ResponseResult updateTemplateStatus(@PathVariable("id") Integer id, @RequestParam Integer status) {
        return messageServiceFeign.updateTemplateStatus(id, status);
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/template/{id}")
    public ResponseResult deleteTemplate(@PathVariable("id") Integer id) {
        return messageServiceFeign.deleteTemplate(id);
    }

    /**
     * 批量更新模板状态
     */
    @PostMapping("/template/batch/status")
    public ResponseResult batchUpdateTemplateStatus(@RequestBody Map<String, Object> params) {
        return messageServiceFeign.batchUpdateTemplateStatus(params);
    }

    // ==================== 发送记录 ====================

    /**
     * 获取发送记录列表
     */
    @GetMapping("/record/list")
    public ResponseResult getRecordList(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "20") Integer size,
                                        @RequestParam(required = false) String channel,
                                        @RequestParam(required = false) Integer sendStatus,
                                        @RequestParam(required = false) String bizType,
                                        @RequestParam(required = false) String eventType,
                                        @RequestParam(required = false) String receiver,
                                        @RequestParam(required = false) Integer startTime,
                                        @RequestParam(required = false) Integer endTime) {
        return messageServiceFeign.getRecordList(
                page, size, channel, sendStatus, bizType, eventType, receiver, startTime, endTime);
    }

    /**
     * 获取发送记录详情
     */
    @GetMapping("/record/{id}")
    public ResponseResult getRecordById(@PathVariable("id") Long id) {
        return messageServiceFeign.getRecordById(id);
    }

    /**
     * 获取发送记录统计
     */
    @GetMapping("/record/statistics")
    public ResponseResult getRecordStatistics(@RequestParam(required = false) String channel,
                                              @RequestParam(required = false) Integer sendStatus,
                                              @RequestParam(required = false) String bizType,
                                              @RequestParam(required = false) String eventType,
                                              @RequestParam(required = false) String receiver,
                                              @RequestParam(required = false) Integer startTime,
                                              @RequestParam(required = false) Integer endTime) {
        return messageServiceFeign.getRecordStatistics(
                channel, sendStatus, bizType, eventType, receiver, startTime, endTime);
    }

    /**
     * 重试发送消息
     */
    @PostMapping("/record/{id}/retry")
    public ResponseResult retryMessage(@PathVariable("id") Long id) {
        return messageServiceFeign.retryMessage(id);
    }

    // ==================== 收件箱 ====================

    /**
     * 获取用户收件箱列表
     */
    @GetMapping("/inbox/list")
    public ResponseResult getInboxList(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "20") Integer size,
                                       @RequestParam(required = false) Integer isRead,
                                       @RequestParam(required = false) Integer msgType) {
        return messageServiceFeign.getInboxList(page, size, isRead, msgType);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/inbox/unread-count")
    public ResponseResult getUnreadCount() {
        return messageServiceFeign.getUnreadCount();
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/inbox/{id}/read")
    public ResponseResult markAsRead(@PathVariable("id") Long id) {
        return messageServiceFeign.markAsRead(id);
    }

    /**
     * 批量标记为已读
     */
    @PostMapping("/inbox/read-all")
    public ResponseResult markAllAsRead() {
        return messageServiceFeign.markAllAsRead();
    }

    /**
     * 删除收件箱消息
     */
    @DeleteMapping("/inbox/{id}")
    public ResponseResult deleteInboxMessage(@PathVariable("id") Long id) {
        return messageServiceFeign.deleteInboxMessage(id);
    }

    // ==================== 订阅设置 ====================

    /**
     * 获取用户订阅设置
     */
    @GetMapping("/subscribe/user/{userId}")
    public ResponseResult getUserSubscribe(@PathVariable("userId") Integer userId) {
        return messageServiceFeign.getUserSubscribe(userId);
    }

    /**
     * 保存订阅设置
     */
    @PostMapping("/subscribe/save")
    public ResponseResult saveSubscribe(@RequestBody Map<String, Object> subscribe) {
        return messageServiceFeign.saveSubscribe(subscribe);
    }

    /**
     * 批量保存订阅设置
     */
    @PostMapping("/subscribe/batch")
    public ResponseResult batchSaveSubscribe(@RequestBody Map<String, Object> params) {
        return messageServiceFeign.batchSaveSubscribe(params);
    }

    // ==================== 邮件设置 ====================

    /**
     * 获取邮件 SMTP 配置
     */
    @GetMapping("/email/settings")
    public ResponseResult getEmailSettings() {
        return messageServiceFeign.getEmailSettings();
    }

    /**
     * 保存邮件 SMTP 配置
     */
    @PostMapping("/email/settings")
    public ResponseResult saveEmailSettings(@RequestBody Map<String, Object> body) {
        return messageServiceFeign.saveEmailSettings(body);
    }

    /**
     * 发送测试邮件
     */
    @PostMapping("/email/test")
    public ResponseResult sendTestEmail(@RequestBody Map<String, Object> body) {
        return messageServiceFeign.sendTestEmail(body);
    }

    // ==================== 极光推送 ====================

    /**
     * 获取极光推送配置
     */
    @GetMapping("/jpush/config")
    public ResponseResult getJPushConfig() {
        return messageServiceFeign.getJPushConfig();
    }

    /**
     * 保存极光推送配置
     */
    @PostMapping("/jpush/config/save")
    public ResponseResult saveJPushConfig(@RequestBody Map<String, Object> config) {
        return messageServiceFeign.saveJPushConfig(config);
    }

    /**
     * 测试极光推送连接
     */
    @PostMapping("/jpush/config/test")
    public ResponseResult testJPushConnection(@RequestBody Map<String, Object> params) {
        return messageServiceFeign.testJPushConnection(params);
    }

    /**
     * 发送极光推送消息
     */
    @PostMapping("/jpush/push/send")
    public ResponseResult sendJPushMessage(@RequestBody Map<String, Object> params) {
        return messageServiceFeign.sendJPushMessage(params);
    }

    /**
     * 获取极光推送历史列表
     */
    @GetMapping("/jpush/history/list")
    public ResponseResult getJPushHistoryList(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "20") Integer size,
                                              @RequestParam(required = false) String pushType,
                                              @RequestParam(required = false) Integer sendStatus) {
        return messageServiceFeign.getJPushHistoryList(page, size, pushType, sendStatus);
    }

    /**
     * 获取极光推送统计数据
     */
    @GetMapping("/jpush/statistics")
    public ResponseResult getJPushStatistics() {
        return messageServiceFeign.getJPushStatistics();
    }
}
