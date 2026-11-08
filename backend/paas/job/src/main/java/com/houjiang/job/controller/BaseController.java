package com.houjiang.job.controller;

import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import com.houjiang.job.service.JobPlatformService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName BaseController
 * @Description 基础controller
 * @Author Amos
 * @Date 2023/5/18 17:39
 * @Version 1.0
 * @author Amos
 */

public class BaseController {
    /**
     * 自动注入作业平台服务。
     */
    @Autowired
    protected JobPlatformService jobPlatformService;

    /**
     * 从请求头中获取token
     *
     * @return
     */
    public String getTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                return authorization.substring(7);
            }
        }
        return null;
    }

    /**
     * 验证token并获取用户信息
     *
     * @return
     */
    public UserInfoVO validateTokenAndGetUser() {
        String token = getTokenFromRequest();
        if (token == null) {
            throw new RuntimeException("Token不能为空");
        }
        return ParseTokenUtils.getUserInfoByToken(token);
    }

}
