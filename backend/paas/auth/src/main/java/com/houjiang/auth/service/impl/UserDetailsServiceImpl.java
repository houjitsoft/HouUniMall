package com.houjiang.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.houjiang.auth.entity.LoginUser;
import com.houjiang.auth.feign.UserServiceClient;
import com.houjiang.auth.mapper.AuthPermissionMapper;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * @Author Amos
 * @author Amos
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    /** 直连 userms 后偶发抖动仍重试；次数不宜过大，避免叠加超时拖垮 BFF 登录 */
    private static final int USER_LOOKUP_MAX_ATTEMPTS = 3;
    private static final long USER_LOOKUP_RETRY_DELAY_MILLIS = 80L;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AuthPermissionMapper authPermissionMapper;

    /**
     * 根据用户名加载用户详细信息。
     * 此方法根据传入的用户名或手机号，从统一用户表加载用户信息。
     *
     * @param usernameJson 包含用户名和手机号的JSON字符串。
     * @return UserDetails 用户的详细信息，封装在LoginUser对象中。
     * @throws UsernameNotFoundException 如果用户不存在，抛出此异常。
     */
    @Override
    public UserDetails loadUserByUsername(String usernameJson) throws UsernameNotFoundException 
    {
        Map<String, Object> userInfoMap = JsonUtils.string2Obj(usernameJson, Map.class);

        UserInfoVO userInfoVO = new UserInfoVO();
        Object userNameObj = userInfoMap.get("userName");
        Object userMobileObj = userInfoMap.get("userMobile");

        String userName = userNameObj == null ? null : userNameObj.toString().trim();
        String userMobile = userMobileObj == null ? null : userMobileObj.toString().trim();
        if ((userName == null || userName.isEmpty()) && (userMobile == null || userMobile.isEmpty())) {
            throw new RuntimeException("用户名或手机号不能为空");
        }

        // 所有账号统一查询 hou_user_info，仅按 user_name 或 user_mobile 匹配。
        userInfoVO.setUserName(userName == null || userName.isEmpty() ? userMobile : userName);
        userInfoVO.setUserMobile(userMobile == null || userMobile.isEmpty() ? userName : userMobile);
        userInfoVO.setLoginType(CommonType.commonStatus.NO.getCode());
        ResponseResult userInfo = queryUserInfoWithRetry(userInfoVO);
        if (userInfo == null || userInfo.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            String message = userInfo == null || userInfo.getMessage() == null
                    ? "未知错误"
                    : userInfo.getMessage();
            throw new UserServiceLookupException("用户服务查询失败: " + message);
        }
        if(Objects.isNull(userInfo.getData())){
            throw new RuntimeException(BizCodeEnume.USER_DOES_NOT_EXIST.getMsg());
        }
        UserInfoVO user = JSON.parseObject(JSON.toJSONString(userInfo.getData()), UserInfoVO.class);
        Long userId;
        try {
            userId = Long.valueOf(user.getUserId());
        } catch (RuntimeException exception) {
            throw new UserServiceLookupException("用户服务返回无效用户ID");
        }
        List<String> permissions = authPermissionMapper.selectPermissionCodes(userId);
        List<String> normalizedPermissions = permissions == null
                ? List.<String>of()
                : permissions.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .distinct()
                    .toList();
        // 平台约定：超管（is_admin=1）的会话权限集含字面 "*"（生产 ① 既有形态，
        // 无 RBAC 角色时 permissions 就是 ["*"]）。基线升级重构时丢失过一次，
        // 表现是超管登录后被所有精确比对的鉴权 403。
        if (normalizedPermissions.isEmpty() && Integer.valueOf(1).equals(authPermissionMapper.selectIsAdmin(userId))) {
            normalizedPermissions = List.of("*");
        }
        return new LoginUser(user, normalizedPermissions);
    }

    /**
     * 登录时用户信息是密码校验的前置条件。仅对短暂的网络/5xx故障重试，
     * 不会对查无用户或密码不匹配进行重试。
     */
    private ResponseResult queryUserInfoWithRetry(UserInfoVO userInfoVO) {
        RuntimeException lastException = null;
        for (int attempt = 1; attempt <= USER_LOOKUP_MAX_ATTEMPTS; attempt++) {
            try {
                return userServiceClient.querUserInfoByParams(userInfoVO);
            } catch (RuntimeException exception) {
                lastException = exception;
                if (!isRetryableUserServiceFailure(exception) || attempt == USER_LOOKUP_MAX_ATTEMPTS) {
                    throw exception;
                }

                log.warn("登录用户查询暂时失败，准备第 {}/{} 次重试: {}",
                        attempt + 1, USER_LOOKUP_MAX_ATTEMPTS, exception.getMessage());
                try {
                    Thread.sleep(USER_LOOKUP_RETRY_DELAY_MILLIS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw exception;
                }
            }
        }
        throw lastException;
    }

    private boolean isRetryableUserServiceFailure(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof RetryableException
                    || current instanceof ConnectException
                    || current instanceof SocketTimeoutException) {
                return true;
            }
            if (current instanceof FeignException feignException) {
                int status = feignException.status();
                return status == -1 || status == 408 || status == 429 || status >= 500;
            }
            current = current.getCause();
        }
        return false;
    }

}
