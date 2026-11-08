package com.houjiang.logging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.logging.entity.LogAccessEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访问日志 Mapper 接口
 *
 * <p>继承 MyBatis-Plus 的 BaseMapper，提供 hou_log_access 表的基础 CRUD 操作。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Mapper
public interface LogAccessMapper extends BaseMapper<LogAccessEntity> {
}
