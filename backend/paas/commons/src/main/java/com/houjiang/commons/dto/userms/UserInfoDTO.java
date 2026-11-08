package com.houjiang.commons.dto.userms;

import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class UserInfoDTO {

    /**
     * 用户
     */
    private UserInfoVO user;

    /**
     * 权限
     */
    private List<String> permissions;

    /**
     * 账户是否过期
     */
    private boolean accountNonExpired;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账号是否锁定
     */
    private boolean accountNonLocked;

    /**
     * 资格证书是否过期
     */
    private boolean credentialsNonExpired;

    /**
     * 已注销
     */
    private boolean enabled;

}
