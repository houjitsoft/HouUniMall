package com.houjiang.auth.service.impl;

import com.houjiang.auth.entity.LoginUser;
import com.houjiang.auth.feign.UserServiceClient;
import com.houjiang.auth.mapper.AuthPermissionMapper;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private AuthPermissionMapper authPermissionMapper;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldQueryHouUserInfoByMobileAndNameWhenLoginPayloadContainsBoth() {
        UserInfoVO user = new UserInfoVO();
        user.setUserId("790708000000000001");
        user.setUserName("geo-user");
        user.setPassword("encoded-password");
        when(userServiceClient.querUserInfoByParams(any(UserInfoVO.class)))
                .thenReturn(ResponseResult.success(user));
        when(authPermissionMapper.selectPermissionCodes(790708000000000001L)).thenReturn(List.of());

        userDetailsService.loadUserByUsername("{\"loginPlatform\":2,\"userName\":\"13800138000\",\"userMobile\":\"13800138000\"}");

        ArgumentCaptor<UserInfoVO> request = ArgumentCaptor.forClass(UserInfoVO.class);
        verify(userServiceClient).querUserInfoByParams(request.capture());
        assertEquals("13800138000", request.getValue().getUserName());
        assertEquals("13800138000", request.getValue().getUserMobile());
    }

    @Test
    void shouldIgnoreShopIdAndAlwaysUseUnifiedHouUserInfoLookup() {
        UserInfoVO user = new UserInfoVO();
        user.setUserId("790708000000000001");
        user.setUserName("geo-user");
        user.setPassword("encoded-password");
        when(userServiceClient.querUserInfoByParams(any(UserInfoVO.class)))
                .thenReturn(ResponseResult.success(user));
        when(authPermissionMapper.selectPermissionCodes(790708000000000001L)).thenReturn(List.of());

        userDetailsService.loadUserByUsername("{\"loginPlatform\":2,\"shopId\":99,\"userName\":\"geo-user\",\"userMobile\":\"13800138000\"}");

        ArgumentCaptor<UserInfoVO> request = ArgumentCaptor.forClass(UserInfoVO.class);
        verify(userServiceClient).querUserInfoByParams(request.capture());
        assertEquals("geo-user", request.getValue().getUserName());
        assertEquals("13800138000", request.getValue().getUserMobile());
        assertNull(request.getValue().getShopId());
    }

    @Test
    void shouldNotReportUserMissingWhenUserServiceReturnsFailure() {
        when(userServiceClient.querUserInfoByParams(any(UserInfoVO.class)))
                .thenReturn(ResponseResult.fail(11009, "数据库查询失败"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userDetailsService.loadUserByUsername(
                        "{\"userName\":\"17612115819\",\"userMobile\":\"17612115819\"}"));

        assertEquals("用户服务查询失败: 数据库查询失败", exception.getMessage());
    }

    @Test
    void shouldLoadDistinctNonBlankPermissionsIntoLoginUser() {
        UserInfoVO user = new UserInfoVO();
        user.setUserId("790708000000000001");
        user.setUserName("permission-user");
        user.setPassword("encoded-password");
        when(userServiceClient.querUserInfoByParams(any(UserInfoVO.class)))
                .thenReturn(ResponseResult.success(user));
        when(authPermissionMapper.selectPermissionCodes(790708000000000001L))
                .thenReturn(List.of(
                        " shell:setting:permission ",
                        "",
                        "shell:setting:permission",
                        "shell:setting:permission:menu"));

        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(
                "{\"userName\":\"permission-user\"}");

        assertEquals(List.of(
                "shell:setting:permission",
                "shell:setting:permission:menu"), loginUser.getPermissions());
        assertEquals(loginUser.getPermissions(), loginUser.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList());
    }

    @Test
    void shouldReturnNonNullEmptyAuthoritiesWhenUserHasNoPermission() {
        UserInfoVO user = new UserInfoVO();
        user.setUserId("790708000000000001");
        user.setUserName("no-permission-user");
        user.setPassword("encoded-password");
        when(userServiceClient.querUserInfoByParams(any(UserInfoVO.class)))
                .thenReturn(ResponseResult.success(user));
        when(authPermissionMapper.selectPermissionCodes(790708000000000001L)).thenReturn(List.of());

        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(
                "{\"userName\":\"no-permission-user\"}");

        assertNotNull(loginUser.getAuthorities());
        assertEquals(0, loginUser.getAuthorities().size());
    }

    @Test
    void shouldFailLoginWhenPermissionLookupFails() {
        UserInfoVO user = new UserInfoVO();
        user.setUserId("790708000000000001");
        user.setUserName("permission-user");
        user.setPassword("encoded-password");
        when(userServiceClient.querUserInfoByParams(any(UserInfoVO.class)))
                .thenReturn(ResponseResult.success(user));
        when(authPermissionMapper.selectPermissionCodes(790708000000000001L))
                .thenThrow(new IllegalStateException("permission database unavailable"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userDetailsService.loadUserByUsername("{\"userName\":\"permission-user\"}"));

        assertEquals("permission database unavailable", exception.getMessage());
    }
}
