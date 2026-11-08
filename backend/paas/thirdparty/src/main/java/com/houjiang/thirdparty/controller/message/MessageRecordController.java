package com.houjiang.thirdparty.controller.message;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.dto.message.MessageRecordFilter;
import com.houjiang.thirdparty.service.message.MessageRecordService;
import com.houjiang.thirdparty.service.message.MessageRequestContext;
import com.houjiang.thirdparty.service.message.MessageRequestContextResolver;
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
@RequestMapping("/message/record")
public class MessageRecordController {

    private final MessageRecordService recordService;
    private final MessageRequestContextResolver contextResolver;

    public MessageRecordController(
            MessageRecordService recordService,
            MessageRequestContextResolver contextResolver) {
        this.recordService = recordService;
        this.contextResolver = contextResolver;
    }

    @GetMapping("/list")
    public ResponseResult list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) Integer sendStatus,
            @RequestParam(required = false) String receiver,
            @RequestParam(required = false) Integer startTime,
            @RequestParam(required = false) Integer endTime) {
        MessageRequestContext context = contextResolver.requireRecordAdminCurrent();
        return ResponseResult.success(recordService.list(
                context, page, size, filter(bizType, eventType, channel, sendStatus, receiver, startTime, endTime)));
    }

    @GetMapping("/statistics")
    public ResponseResult statistics(
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) Integer sendStatus,
            @RequestParam(required = false) String receiver,
            @RequestParam(required = false) Integer startTime,
            @RequestParam(required = false) Integer endTime) {
        MessageRequestContext context = contextResolver.requireRecordAdminCurrent();
        return ResponseResult.success(recordService.statistics(
                context, filter(bizType, eventType, channel, sendStatus, receiver, startTime, endTime)));
    }

    @GetMapping("/{id}")
    public ResponseResult detail(@PathVariable Long id) {
        return ResponseResult.success(recordService.detail(contextResolver.requireRecordAdminCurrent(), id));
    }

    @PostMapping("/{id}/retry")
    public ResponseResult retry(@PathVariable Long id) {
        return ResponseResult.success(recordService.retry(contextResolver.requireRecordAdminCurrent(), id));
    }

    private MessageRecordFilter filter(
            String bizType, String eventType, String channel, Integer sendStatus,
            String receiver, Integer startTime, Integer endTime) {
        return new MessageRecordFilter(
                bizType, eventType, channel, sendStatus, receiver, startTime, endTime);
    }
}
