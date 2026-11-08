package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.base.entity.BaseKd100ConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 快递100配置Mapper
 * @author Amos
 */
@Mapper
public interface BaseKd100ConfigMapper extends BaseMapper<BaseKd100ConfigEntity> {
    
    /**
     * 更新快递100配置
     * @param param 配置参数
     */
    void updateConfig(@Param("param") Map<String, Object> param);
}
