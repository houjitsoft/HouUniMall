package com.houjiang.commons.filter;

import com.houjiang.commons.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器 - 简化版本
 * BFF层只验证token有效性，不查询Redis
 * 适用于BFF层等不需要完整用户信息的场景
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Slf4j
public class SimplifiedJwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        log.info("==> JWT简化过滤器处理请求: {}", request.getRequestURI());
        
        // 获取token
        String token = request.getHeader("Authorization");
        log.info("==> Authorization header: {}", token != null ? (token.length() > 30 ? token.substring(0, 30) + "..." : token) : "null");
        
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            log.warn("==> 请求中没有token或格式不正确,将返回403: {}", token);
            // 没有token，放行让Spring Security处理
            filterChain.doFilter(request, response);
            return;
        }
        
        // 解析token
        token = token.substring(7);
        log.info("==> 提取的token: {}...", token.substring(0, Math.min(20, token.length())));
        
        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userid = claims.getSubject();
            log.info("==> Token解析成功，用户ID: {}", userid);
        } catch (Exception e) {
            log.error("==> Token解析失败，将返回403: {}", e.getMessage(), e);
            // token无效，放行让Spring Security返回401
            filterChain.doFilter(request, response);
            return;
        }
        
        // token有效，设置认证信息
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userid, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        
        log.info("==> 认证信息已设置，用户: {}、将放行请求", userid);
        
        // 放行
        filterChain.doFilter(request, response);
    }
}
