package com.houjiang.thirdparty.mapper.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.thirdparty.entity.message.MessageChannel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息渠道配置Mapper接口
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Mapper
public interface MessageChannelMapper extends BaseMapper<MessageChannel> {
}
