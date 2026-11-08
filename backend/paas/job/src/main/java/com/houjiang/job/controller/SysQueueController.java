package com.houjiang.job.controller;

import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.service.SysQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统队列管理
 * @author Amos
 */
@RestController
@RequestMapping("/queue")
public class SysQueueController {

    @Autowired
    private SysQueueService sysQueueService;

    @GetMapping("/stats")
    public ResponseResult stats() {
        return sysQueueService.stats();
    }

    @GetMapping("/list")
    public ResponseResult list(@RequestParam Map<String, Object> params) {
        return sysQueueService.list(params);
    }

    @PostMapping("/cancel/{id}")
    @SysLog(value = "取消队列任务", module = "job")
    public ResponseResult cancel(@PathVariable("id") Long id) {
        return sysQueueService.cancel(id);
    }

    @PostMapping("/retry/{id}")
    @SysLog(value = "重试队列任务", module = "job")
    public ResponseResult retry(@PathVariable("id") Long id) {
        return sysQueueService.retry(id);
    }

    @PostMapping("/execute/{id}")
    @SysLog(value = "立即执行队列任务", module = "job")
    public ResponseResult execute(@PathVariable("id") Long id) {
        return sysQueueService.executeNow(id);
    }

    @PostMapping("/batchCancel")
    @SysLog(value = "批量取消队列任务", module = "job")
    public ResponseResult batchCancel(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        return sysQueueService.batchCancel(ids);
    }
}
