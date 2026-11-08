package com.houjiang.thirdparty.controller.message;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.entity.message.MessageChannel;
import com.houjiang.thirdparty.service.message.MessageChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息渠道配置控制器
 * 
 * @author Amos
 * @since 2025-12-26
 */
@RestController
@RequestMapping("/message/channel")
public class MessageChannelController {

    @Autowired
    private MessageChannelService channelService;

    /**
     * 获取渠道列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 渠道列表
     */
    @GetMapping("/list")
    public ResponseResult list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Map<String, Object> data = channelService.getChannelList(page, size);
        return ResponseResult.success(data);
    }

    /**
     * 获取所有启用的渠道
     * 
     * @return 渠道列表
     */
    @GetMapping("/enabled")
    public ResponseResult enabled() {
        return ResponseResult.success(channelService.getEnabledChannels());
    }

    /**
     * 获取渠道详情
     * 
     * @param id 渠道ID
     * @return 渠道信息
     */
    @GetMapping("/{id}")
    public ResponseResult detail(@PathVariable Integer id) {
        MessageChannel channel = channelService.getChannelById(id);
        if (channel == null) {
            return ResponseResult.fail(404, "渠道不存在");
        }
        return ResponseResult.success(channel);
    }

    /**
     * 创建渠道
     * 
     * @param channel 渠道信息
     * @return 操作结果
     */
    @PostMapping("/create")
    public ResponseResult create(@RequestBody MessageChannel channel) {
        boolean result = channelService.createChannel(channel);
        if (result) {
            return ResponseResult.success("创建成功");
        }
        return ResponseResult.fail(500, "创建失败");
    }

    /**
     * 更新渠道
     * 
     * @param channel 渠道信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody MessageChannel channel) {
        if (channel.getId() == null) {
            return ResponseResult.fail(400, "渠道ID不能为空");
        }
        boolean result = channelService.updateChannel(channel);
        if (result) {
            return ResponseResult.success("更新成功");
        }
        return ResponseResult.fail(500, "更新失败");
    }

    /**
     * 更新渠道状态
     * 
     * @param id 渠道ID
     * @param status 状态
     * @return 操作结果
     */
    @PostMapping("/{id}/status")
    public ResponseResult updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        boolean result = channelService.updateStatus(id, status);
        if (result) {
            return ResponseResult.success("状态更新成功");
        }
        return ResponseResult.fail(500, "状态更新失败");
    }

    /**
     * 删除渠道
     * 
     * @param id 渠道ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseResult delete(@PathVariable Integer id) {
        boolean result = channelService.deleteChannel(id);
        if (result) {
            return ResponseResult.success("删除成功");
        }
        return ResponseResult.fail(500, "删除失败");
    }
}
