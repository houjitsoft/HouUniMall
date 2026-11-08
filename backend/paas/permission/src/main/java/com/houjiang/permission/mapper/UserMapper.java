package com.houjiang.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.permission.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM hou_user_info WHERE user_name = #{username} AND is_delete = 0 LIMIT 1")
    User selectByUsername(@Param("username") String username);
    
    /**
     * TODO 补充方法说明
     * @return TODO
     */
    @Select("SELECT * FROM hou_user_info WHERE user_mobile = #{mobile} AND is_delete = 0 LIMIT 1")
    User selectByMobile(@Param("mobile") String mobile);
}
