package com.houjiang.thirdparty.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录用户实体类
 * 用于Spring Security认证
 * 
 * @author Amos
 * @date 2026-01-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private UserInfoVO user;

    /** 存储权限信息 */
    private List<String> permissions;

    public LoginUser(UserInfoVO user, List<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    /** 存储SpringSecurity所需要的权限信息的集合 */
    @JSONField(serialize = false)
    private List<GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(authorities != null) {
            return authorities;
        }
        //把permissions中字符串类型的权限信息转换成GrantedAuthority对象存入authorities中
        if(CollectionUtils.isNotEmpty(permissions)) {
            authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        String password = user.getPassword() == null ? user.getUserPassword() : user.getPassword();
        return password;
    }

    @Override
    public String getUsername() {
        String userName = user.getPassword() == null ? user.getUserAccount() : user.getUserName();
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

