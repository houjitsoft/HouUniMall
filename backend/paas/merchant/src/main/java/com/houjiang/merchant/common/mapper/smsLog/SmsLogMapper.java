package com.houjiang.merchant.common.mapper.smsLog;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.Config;
import com.houjiang.merchant.common.entity.smsLog.SmsLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信记录
 * @author Amos
 */
@Mapper
public interface SmsLogMapper extends IBaseMapper<SmsLog> {
}
