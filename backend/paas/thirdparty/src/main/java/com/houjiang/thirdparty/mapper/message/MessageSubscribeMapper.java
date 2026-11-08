package com.houjiang.thirdparty.mapper.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.thirdparty.entity.message.MessageSubscribe;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户消息订阅设置Mapper接口
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Mapper
public interface MessageSubscribeMapper extends BaseMapper<MessageSubscribe> {
}
