package com.houjiang.thirdparty.service.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.thirdparty.entity.message.MessageSubscribe;
import com.houjiang.thirdparty.mapper.message.MessageSubscribeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户消息订阅设置服务类
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Service
public class MessageSubscribeService {

    @Autowired
    private MessageSubscribeMapper subscribeMapper;

    /**
     * 获取用户的订阅设置列表
     * 
     * @param userId 用户ID
     * @return 订阅设置列表
     */
    public List<MessageSubscribe> getUserSubscribes(String userId) {
        LambdaQueryWrapper<MessageSubscribe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageSubscribe::getUserId, userId);
        return subscribeMapper.selectList(wrapper);
    }

    /**
     * 获取用户指定业务类型的订阅设置
     * 
     * @param userId 用户ID
     * @param bizType 业务类型
     * @return 订阅设置
     */
    public MessageSubscribe getUserSubscribe(String userId, String bizType) {
        LambdaQueryWrapper<MessageSubscribe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageSubscribe::getUserId, userId)
               .eq(MessageSubscribe::getBizType, bizType);
        return subscribeMapper.selectOne(wrapper);
    }

    /**
     * 保存或更新订阅设置
     * 
     * @param subscribe 订阅设置
     * @return 是否成功
     */
    public boolean saveOrUpdate(MessageSubscribe subscribe) {
        // 检查是否已存在
        MessageSubscribe existing = getUserSubscribe(subscribe.getUserId(), subscribe.getBizType());
        
        if (existing != null) {
            subscribe.setId(existing.getId());
            subscribe.setUpdateTime((int)(System.currentTimeMillis() / 1000));
            return subscribeMapper.updateById(subscribe) > 0;
        } else {
            subscribe.setCreateTime((int)(System.currentTimeMillis() / 1000));
            return subscribeMapper.insert(subscribe) > 0;
        }
    }

    /**
     * 批量保存订阅设置
     * 
     * @param subscribes 订阅设置列表
     * @return 是否成功
     */
    public boolean batchSave(List<MessageSubscribe> subscribes) {
        for (MessageSubscribe subscribe : subscribes) {
            if (!saveOrUpdate(subscribe)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 删除订阅设置
     * 
     * @param id 订阅ID
     * @return 是否成功
     */
    public boolean deleteSubscribe(Integer id) {
        return subscribeMapper.deleteById(id) > 0;
    }

    /**
     * 检查用户是否订阅了指定渠道
     * 
     * @param userId 用户ID
     * @param bizType 业务类型
     * @param channel 渠道编码
     * @return 是否订阅
     */
    public boolean isSubscribed(String userId, String bizType, String channel) {
        MessageSubscribe subscribe = getUserSubscribe(userId, bizType);
        if (subscribe == null) {
            // 默认站内信开启
            return "INBOX".equals(channel);
        }
        
        switch (channel) {
            case "INBOX":
                return subscribe.getChannelInbox() != null && subscribe.getChannelInbox() == 1;
            case "EMAIL":
                return subscribe.getChannelEmail() != null && subscribe.getChannelEmail() == 1;
            case "SMS":
                return subscribe.getChannelSms() != null && subscribe.getChannelSms() == 1;
            case "DINGTALK":
                return subscribe.getChannelDingtalk() != null && subscribe.getChannelDingtalk() == 1;
            case "WECOM":
                return subscribe.getChannelWecom() != null && subscribe.getChannelWecom() == 1;
            case "WECHAT":
                return subscribe.getChannelWechat() != null && subscribe.getChannelWechat() == 1;
            case "FEISHU":
                return subscribe.getChannelFeishu() != null && subscribe.getChannelFeishu() == 1;
            default:
                return false;
        }
    }
}
