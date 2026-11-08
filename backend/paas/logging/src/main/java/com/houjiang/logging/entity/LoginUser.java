package com.houjiang.logging.entity;

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
 * 登录用户实体类，实现 Spring Security 的 UserDetails 接口
 *
 * <p>封装登录用户的基本信息和权限列表，由 JWT 过滤器从 Redis 中反序列化得到。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    /** 用户信息 VO */
    private UserInfoVO user;

    /** 权限信息字符串列表 */
    private List<String> permissions;

    /** Spring Security 权限集合（不序列化到 Redis） */
    @JSONField(serialize = false)
    private List<GrantedAuthority> authorities;

    /**
     * 构造函数
     *
     * @param user        用户信息 VO
     * @param permissions 权限字符串列表
     */
    public LoginUser(UserInfoVO user, List<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    /**
     * 获取 Spring Security 权限集合
     *
     * @return 权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities != null) {
            return authorities;
        }
        if (CollectionUtils.isNotEmpty(permissions)) {
            authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return authorities;
    }

    /**
     * 获取密码
     *
     * @return 用户密码
     */
    @Override
    public String getPassword() {
        return user.getPassword() == null ? user.getUserPassword() : user.getPassword();
    }

    /**
     * 获取用户名
     *
     * @return 用户名/账号
     */
    @Override
    public String getUsername() {
        return user.getPassword() == null ? user.getUserAccount() : user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
