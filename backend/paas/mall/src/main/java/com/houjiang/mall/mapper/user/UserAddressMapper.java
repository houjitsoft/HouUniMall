package com.houjiang.mall.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.mall.entity.user.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收货地址Mapper接口
 * 
 * @author Amos
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
