package com.houjiang.auth.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * @Author Amos
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LoginUser implements UserDetails {

    private UserInfoVO user;

    /** 存储权限信息 */
    private List<String> permissions;


    public LoginUser(UserInfoVO user, List<String> permissions) {
        this.user = user;
        this.permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }

    /** 存储SpringSecurity所需要的权限信息的集合 */
    @JSONField(serialize = false)
    private List<GrantedAuthority> authorities;


    @Override
    public  Collection<? extends GrantedAuthority> getAuthorities() {
        if(authorities!=null){
            return authorities;
        }
        List<String> permissionCodes = permissions == null ? List.of() : permissions;
        authorities = permissionCodes.stream()
                .map(code -> (GrantedAuthority) new SimpleGrantedAuthority(code))
                .toList();
        return authorities;
    }


    @Override
    public String getPassword() {
        String password = user.getPassword();
        if (password == null || password.isEmpty()) {
            password = user.getUserPassword();
        }
        return password;
    }

    @Override
    public String getUsername() {
        String userName=user.getPassword()==null?user.getUserAccount():user.getUserName();
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
