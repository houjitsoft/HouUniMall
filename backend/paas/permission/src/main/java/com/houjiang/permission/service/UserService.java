package com.houjiang.permission.service;

import com.houjiang.permission.dto.PageRequest;
import com.houjiang.permission.dto.UserCreateRequest;
import com.houjiang.permission.dto.UserSearchRequest;
import com.houjiang.permission.dto.UserUpdateRequest;
import com.houjiang.permission.vo.PageResult;
import com.houjiang.permission.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 * 
 * @author Amos
 * @since 2025-01-10
 */
public interface UserService {
    
    /**
     * 分页获取用户列表
     * 
     * @param searchRequest 搜索请求
     * @return 分页结果
     */
    PageResult<UserVO> getUserList(UserSearchRequest searchRequest);
    
    /**
     * 获取用户详情
     * 
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getUserDetail(String id);
    
    /**
     * 创建用户
     * 
     * @param request 创建请求
     */
    void createUser(UserCreateRequest request);
    
    /**
     * 更新用户
     * 
     * @param request 更新请求
     */
    void updateUser(UserUpdateRequest request);
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     */
    void deleteUser(String id);
    
    /**
     * 重置用户密码
     * 
     * @param id 用户ID
     * @param newPassword 新密码
     */
    void resetPassword(String id, String newPassword);
    
    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param status 状态
     */
    void updateUserStatus(String id, Integer status);
}
