package com.houjiang.thirdparty.mapper.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.thirdparty.entity.message.JPushHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 极光推送历史Mapper
 * @author Amos
 */
@Mapper
public interface JPushHistoryMapper extends BaseMapper<JPushHistoryEntity> {
}
