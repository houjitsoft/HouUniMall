package com.houjiang.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.base.entity.BaseSystemMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @ClassName BaseSystemMessageMapper
 * @Description 系统化实现Mapper
 * @Author Amos
 * @Date 2023/7/19 15:59
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface BaseSystemMessageMapper extends BaseMapper<BaseSystemMessageEntity> {
    /**
     * 查询下单消息
     * @param page
     * @param param
     * @return
     */
    IPage<BaseSystemMessageEntity> querySysOrderMessage(IPage<BaseSystemMessageEntity> page, @Param("param") Map<String, Object> param);

    /**
     * 插入消息
     * @param param
     */
    void insertSysMessage(@Param("param") Map<String, Object> param);
}
