package com.houjiang.merchant.service.admin;

import java.util.List;

/**
 * 权限功能类
 */
public interface IAuthService {

    /**
     * @notes 获取当前管理员角色按钮权限
     * @return mixed
     * @author Amos
     * @date 2022/7/1 16:10
     */
    List<String> getBtnAuthByRoleId(Integer adminId);
}
