package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息中心服务Feign客户端
/**
 *
 * @description 调用paas中台第三方服务(thirdparty)的消息中心接口
 * @author Amos
 * @date 2025-01-01
 */
@FeignClient(
        name = "thirdparty",
        url = "${feign.thirdparty.url:http://127.0.0.1:8194}",
        path = "/message",
        contextId = "messageServiceFeign",
        fallbackFactory = MessageServiceFeignFallback.class
)
public interface MessageServiceFeign {

    // ==================== 渠道管理 ====================

    /**
     * 获取渠道列表
     */
    @GetMapping("/channel/list")
    ResponseResult getChannelList(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size);

    /**
     * 获取启用的渠道列表
     */
    @GetMapping("/channel/enabled")
    ResponseResult getEnabledChannels();

    /**
     * 获取渠道详情
     */
    @GetMapping("/channel/{id}")
    ResponseResult getChannelById(@PathVariable("id") Integer id);

    /**
     * 创建渠道
     */
    @PostMapping("/channel/create")
    ResponseResult createChannel(@RequestBody Map<String, Object> channel);

    /**
     * 更新渠道
     */
    @PostMapping("/channel/update")
    ResponseResult updateChannel(@RequestBody Map<String, Object> channel);

    /**
     * 更新渠道状态
     */
    @PostMapping("/channel/{id}/status")
    ResponseResult updateChannelStatus(@PathVariable("id") Integer id, @RequestParam Integer status);

    /**
     * 删除渠道
     */
    @DeleteMapping("/channel/{id}")
    ResponseResult deleteChannel(@PathVariable("id") Integer id);

    // ==================== 模板管理 ====================

    /**
     * 获取模板列表
     */
    @PostMapping("/template/list")
    ResponseResult getTemplateList(@RequestBody Map<String, Object> params);

    /**
     * 获取启用的模板列表
     */
    @GetMapping("/template/enabled")
    ResponseResult getEnabledTemplates();

    /**
     * 获取模板详情
     */
    @GetMapping("/template/{id}")
    ResponseResult getTemplateById(@PathVariable("id") Integer id);

    /**
     * 根据模板编码获取模板
     */
    @GetMapping("/template/code/{code}")
    ResponseResult getTemplateByCode(@PathVariable("code") String code);

    /**
     * 创建模板
     */
    @PostMapping("/template/create")
    ResponseResult createTemplate(@RequestBody Map<String, Object> template);

    /**
     * 更新模板
     */
    @PostMapping("/template/update")
    ResponseResult updateTemplate(@RequestBody Map<String, Object> template);

    /**
     * 更新模板状态
     */
    @PostMapping("/template/{id}/status")
    ResponseResult updateTemplateStatus(@PathVariable("id") Integer id, @RequestParam Integer status);

    /**
     * 删除模板
     */
    @DeleteMapping("/template/{id}")
    ResponseResult deleteTemplate(@PathVariable("id") Integer id);

    /**
     * 批量更新模板状态
     */
    @PostMapping("/template/batch/status")
    ResponseResult batchUpdateTemplateStatus(@RequestBody Map<String, Object> params);

    // ==================== 发送记录 ====================

    /**
     * 获取发送记录列表
     */
    @GetMapping("/record/list")
    ResponseResult getRecordList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                  @RequestParam(value = "size", defaultValue = "20") Integer size,
                                  @RequestParam(value = "channel", required = false) String channel,
                                  @RequestParam(value = "sendStatus", required = false) Integer sendStatus,
                                  @RequestParam(value = "bizType", required = false) String bizType,
                                  @RequestParam(value = "eventType", required = false) String eventType,
                                  @RequestParam(value = "receiver", required = false) String receiver,
                                  @RequestParam(value = "startTime", required = false) Integer startTime,
                                  @RequestParam(value = "endTime", required = false) Integer endTime);

    /**
     * 获取发送记录详情
     */
    @GetMapping("/record/{id}")
    ResponseResult getRecordById(@PathVariable("id") Long id);

    /**
     * 获取发送记录统计
     */
    @GetMapping("/record/statistics")
    ResponseResult getRecordStatistics(
            @RequestParam(value = "channel", required = false) String channel,
            @RequestParam(value = "sendStatus", required = false) Integer sendStatus,
            @RequestParam(value = "bizType", required = false) String bizType,
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "receiver", required = false) String receiver,
            @RequestParam(value = "startTime", required = false) Integer startTime,
            @RequestParam(value = "endTime", required = false) Integer endTime);

    /**
     * 重试发送消息
     */
    @PostMapping("/record/{id}/retry")
    ResponseResult retryMessage(@PathVariable("id") Long id);

    // ==================== 收件箱 ====================

    /**
     * 获取用户收件箱列表
     */
    @GetMapping("/inbox/list")
    ResponseResult getInboxList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "size", defaultValue = "20") Integer size,
                                 @RequestParam(value = "isRead", required = false) Integer isRead,
                                 @RequestParam(value = "msgType", required = false) Integer msgType);

    /**
     * 获取未读消息数量
     */
    @GetMapping("/inbox/unread-count")
    ResponseResult getUnreadCount();

    /**
     * 标记消息为已读
     */
    @PostMapping("/inbox/{id}/read")
    ResponseResult markAsRead(@PathVariable("id") Long id);

    /**
     * 批量标记为已读
     */
    @PostMapping("/inbox/read-all")
    ResponseResult markAllAsRead();

    /**
     * 删除收件箱消息
     */
    @DeleteMapping("/inbox/{id}")
    ResponseResult deleteInboxMessage(@PathVariable("id") Long id);

    // ==================== 订阅设置 ====================

    /**
     * 获取用户订阅设置
     */
    @GetMapping("/subscribe/user/{userId}")
    ResponseResult getUserSubscribe(@PathVariable("userId") Integer userId);

    /**
     * 保存订阅设置
     */
    @PostMapping("/subscribe/save")
    ResponseResult saveSubscribe(@RequestBody Map<String, Object> subscribe);

    /**
     * 批量保存订阅设置
     */
    @PostMapping("/subscribe/batch")
    ResponseResult batchSaveSubscribe(@RequestBody Map<String, Object> params);

    // ==================== 邮件设置 ====================

    /**
     * 获取邮件 SMTP 配置
     */
    @GetMapping("/email/settings")
    ResponseResult getEmailSettings();

    /**
     * 保存邮件 SMTP 配置
     */
    @PostMapping("/email/settings")
    ResponseResult saveEmailSettings(@RequestBody Map<String, Object> body);

    /**
     * 发送测试邮件
     */
    @PostMapping("/email/test")
    ResponseResult sendTestEmail(@RequestBody Map<String, Object> body);

    // ==================== 极光推送 ====================

    /**
     * 获取极光推送配置
     */
    @GetMapping("/jpush/config")
    ResponseResult getJPushConfig();

    /**
     * 保存极光推送配置
     */
    @PostMapping("/jpush/config/save")
    ResponseResult saveJPushConfig(@RequestBody Map<String, Object> config);

    /**
     * 测试极光推送连接
     */
    @PostMapping("/jpush/config/test")
    ResponseResult testJPushConnection(@RequestBody Map<String, Object> params);

    /**
     * 发送极光推送消息
     */
    @PostMapping("/jpush/push/send")
    ResponseResult sendJPushMessage(@RequestBody Map<String, Object> params);

    /**
     * 获取极光推送历史列表
     */
    @GetMapping("/jpush/history/list")
    ResponseResult getJPushHistoryList(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "20") Integer size,
                                      @RequestParam(value = "pushType", required = false) String pushType,
                                      @RequestParam(value = "sendStatus", required = false) Integer sendStatus);

    /**
     * 获取极光推送统计数据
     */
    @GetMapping("/jpush/statistics")
    ResponseResult getJPushStatistics();
}
