package com.houjiang.logging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.logging.entity.LogAlertEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 运行错误告警 Mapper 接口
 *
 * <p>继承 MyBatis-Plus 的 BaseMapper，提供 hou_log_alert 表的基础 CRUD 操作。</p>
 *
 * @author Amos
 * @since 2026-09-25
 */
@Mapper
public interface LogAlertMapper extends BaseMapper<LogAlertEntity> {
}
