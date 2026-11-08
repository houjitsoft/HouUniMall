package com.houjiang.merchant.common.mapper.system;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.system.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统操作日志
 * @author Amos
 */
@Mapper
public interface SystemLogOperateMapper extends IBaseMapper<OperationLog> {
}
