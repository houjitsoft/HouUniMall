package com.houjiang.thirdparty.mapper.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.thirdparty.entity.message.JPushConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 极光推送配置Mapper
 * @author Amos
 */
@Mapper
public interface JPushConfigMapper extends BaseMapper<JPushConfigEntity> {
}
