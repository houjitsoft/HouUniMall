package com.houjiang.thirdparty.service.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.thirdparty.entity.message.MessageChannel;
import com.houjiang.thirdparty.mapper.message.MessageChannelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息渠道配置服务类
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Service
public class MessageChannelService {

    @Autowired
    private MessageChannelMapper channelMapper;

    /**
     * 获取渠道列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public Map<String, Object> getChannelList(Integer page, Integer size) {
        Page<MessageChannel> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<MessageChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(MessageChannel::getDeleteTime)
               .orderByAsc(MessageChannel::getSort);
        
        Page<MessageChannel> result = channelMapper.selectPage(pageInfo, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        return data;
    }

    /**
     * 获取所有启用的渠道
     * 
     * @return 渠道列表
     */
    public List<MessageChannel> getEnabledChannels() {
        LambdaQueryWrapper<MessageChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageChannel::getStatus, 1)
               .isNull(MessageChannel::getDeleteTime)
               .orderByAsc(MessageChannel::getSort);
        return channelMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取渠道
     * 
     * @param id 渠道ID
     * @return 渠道信息
     */
    public MessageChannel getChannelById(Integer id) {
        return channelMapper.selectById(id);
    }

    /**
     * 根据编码获取渠道
     * 
     * @param channelCode 渠道编码
     * @return 渠道信息
     */
    public MessageChannel getChannelByCode(String channelCode) {
        LambdaQueryWrapper<MessageChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageChannel::getChannelCode, channelCode)
               .isNull(MessageChannel::getDeleteTime);
        return channelMapper.selectOne(wrapper);
    }

    /**
     * 创建渠道
     * 
     * @param channel 渠道信息
     * @return 是否成功
     */
    public boolean createChannel(MessageChannel channel) {
        channel.setCreateTime((int)(System.currentTimeMillis() / 1000));
        return channelMapper.insert(channel) > 0;
    }

    /**
     * 更新渠道
     * 
     * @param channel 渠道信息
     * @return 是否成功
     */
    public boolean updateChannel(MessageChannel channel) {
        channel.setUpdateTime((int)(System.currentTimeMillis() / 1000));
        return channelMapper.updateById(channel) > 0;
    }

    /**
     * 更新渠道状态
     * 
     * @param id 渠道ID
     * @param status 状态
     * @return 是否成功
     */
    public boolean updateStatus(Integer id, Integer status) {
        MessageChannel channel = new MessageChannel();
        channel.setId(id);
        channel.setStatus(status);
        channel.setUpdateTime((int)(System.currentTimeMillis() / 1000));
        return channelMapper.updateById(channel) > 0;
    }

    /**
     * 删除渠道（软删除）
     * 
     * @param id 渠道ID
     * @return 是否成功
     */
    public boolean deleteChannel(Integer id) {
        MessageChannel channel = new MessageChannel();
        channel.setId(id);
        channel.setDeleteTime((int)(System.currentTimeMillis() / 1000));
        return channelMapper.updateById(channel) > 0;
    }
}
