package com.houjiang.userms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.userms.entity.UserLogActionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @Entity com.houjiang.userms.entity.UserLogAction
 * @author Amos
 */
@Mapper
public interface UserLogActionMapper extends BaseMapper<UserLogActionEntity> {

    /**
     * 分页查询用户日志
     *
     * @param page
     * @param paraMap
     * @return
     */
    IPage<Map<String, Object>> queryUserOperateLog(IPage<Map<String, Object>> page, @Param("param") Map<String, Object> paraMap);
}




