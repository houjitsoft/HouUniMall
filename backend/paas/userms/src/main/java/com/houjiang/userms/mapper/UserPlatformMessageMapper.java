package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.userms.dto.UserPlatformMessageDTO;
import com.houjiang.userms.entity.UserPlatformMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Entity com.houjiang.userms.entity.UserPlatformMessage
 * @author Amos
 */
@Mapper
public interface UserPlatformMessageMapper extends BaseMapper<UserPlatformMessageEntity> {
    /**
     * 站内信通知
     * @param map
     */
    void insertmessage(@Param("param") Map map);


    /**
     * 新增站内信息
     *
     * @param record
     * @return
     */
    int insertSelective(UserPlatformMessageDTO record);
}




