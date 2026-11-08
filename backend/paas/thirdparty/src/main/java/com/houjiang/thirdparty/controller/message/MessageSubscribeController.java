package com.houjiang.thirdparty.controller.message;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.entity.message.MessageSubscribe;
import com.houjiang.thirdparty.service.message.MessageSubscribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户消息订阅设置控制器
 * 
 * @author Amos
 * @since 2025-12-26
 */
@RestController
@RequestMapping("/message/subscribe")
public class MessageSubscribeController {

    @Autowired
    private MessageSubscribeService subscribeService;

    /**
     * 获取用户的订阅设置列表
     * 
     * @param userId 用户ID
     * @return 订阅设置列表
     */
    @GetMapping("/list")
    public ResponseResult list(@RequestParam String userId) {
        List<MessageSubscribe> subscribes = subscribeService.getUserSubscribes(userId);
        return ResponseResult.success(subscribes);
    }

    /**
     * 获取用户指定业务类型的订阅设置
     * 
     * @param userId 用户ID
     * @param bizType 业务类型
     * @return 订阅设置
     */
    @GetMapping("/detail")
    public ResponseResult detail(@RequestParam String userId, @RequestParam String bizType) {
        MessageSubscribe subscribe = subscribeService.getUserSubscribe(userId, bizType);
        return ResponseResult.success(subscribe);
    }

    /**
     * 保存订阅设置
     * 
     * @param subscribe 订阅设置
     * @return 操作结果
     */
    @PostMapping("/save")
    public ResponseResult save(@RequestBody MessageSubscribe subscribe) {
        if (subscribe.getUserId() == null || subscribe.getBizType() == null) {
            return ResponseResult.fail(400, "用户ID和业务类型不能为空");
        }
        boolean result = subscribeService.saveOrUpdate(subscribe);
        if (result) {
            return ResponseResult.success("保存成功");
        }
        return ResponseResult.fail(500, "保存失败");
    }

    /**
     * 批量保存订阅设置
     * 
     * @param subscribes 订阅设置列表
     * @return 操作结果
     */
    @PostMapping("/batch-save")
    public ResponseResult batchSave(@RequestBody List<MessageSubscribe> subscribes) {
        boolean result = subscribeService.batchSave(subscribes);
        if (result) {
            return ResponseResult.success("批量保存成功");
        }
        return ResponseResult.fail(500, "批量保存失败");
    }

    /**
     * 删除订阅设置
     * 
     * @param id 订阅ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseResult delete(@PathVariable Integer id) {
        boolean result = subscribeService.deleteSubscribe(id);
        if (result) {
            return ResponseResult.success("删除成功");
        }
        return ResponseResult.fail(500, "删除失败");
    }
}
