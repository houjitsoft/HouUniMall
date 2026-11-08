package com.houjiang.merchant.common.mapper.user;

import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.user.UserAuth;
import com.houjiang.merchant.common.entity.user.UserLevel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户授权Mapper
 * @author Amos
 */
@Mapper
public interface UserLevelMapper extends IBaseMapper<UserLevel> {
}
