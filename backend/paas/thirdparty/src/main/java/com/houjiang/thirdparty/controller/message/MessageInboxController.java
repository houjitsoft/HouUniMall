package com.houjiang.thirdparty.controller.message;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.service.message.MessageInboxService;
import com.houjiang.thirdparty.service.message.MessageRequestContext;
import com.houjiang.thirdparty.service.message.MessageRequestContextResolver;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/message/inbox")
public class MessageInboxController {

    private final MessageInboxService inboxService;
    private final MessageRequestContextResolver contextResolver;

    public MessageInboxController(
            MessageInboxService inboxService,
            MessageRequestContextResolver contextResolver) {
        this.inboxService = inboxService;
        this.contextResolver = contextResolver;
    }

    @GetMapping("/list")
    public ResponseResult list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(required = false) Integer msgType) {
        MessageRequestContext context = contextResolver.resolveCurrent();
        return ResponseResult.success(inboxService.list(context, page, size, isRead, msgType));
    }

    @GetMapping("/unread-count")
    public ResponseResult unreadCount() {
        return ResponseResult.success(inboxService.unreadCount(contextResolver.resolveCurrent()));
    }

    @GetMapping("/{id}")
    public ResponseResult detail(@PathVariable Long id) {
        return ResponseResult.success(inboxService.detail(contextResolver.resolveCurrent(), id));
    }

    @PostMapping("/{id}/read")
    public ResponseResult markAsRead(@PathVariable Long id) {
        inboxService.markAsRead(contextResolver.resolveCurrent(), id);
        return ResponseResult.success("标记成功");
    }

    @PostMapping("/read-all")
    public ResponseResult markAllAsRead() {
        int count = inboxService.markAllAsRead(contextResolver.resolveCurrent());
        return ResponseResult.success("已标记" + count + "条消息为已读");
    }

    @DeleteMapping("/{id}")
    public ResponseResult delete(@PathVariable Long id) {
        inboxService.delete(contextResolver.resolveCurrent(), id);
        return ResponseResult.success("删除成功");
    }
}
