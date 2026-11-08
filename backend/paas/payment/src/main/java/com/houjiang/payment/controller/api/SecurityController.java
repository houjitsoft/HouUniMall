package com.houjiang.payment.controller.api;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.houjiang.commons.util.PasswordUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PaySecurityEntity;
import com.houjiang.payment.mapper.PaySecurityMapper;
import com.houjiang.payment.util.ApiTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付安全 Controller（支付密码 / 实名认证）
 *
 * 路径对应前端 api/pay.js:
 *   /api/payment/security/setPassword
 *   /api/payment/security/verifyPassword
 *   /api/payment/security/modifyPassword
 *   /api/payment/security/realName
 *   /api/payment/security/realNameStatus
 *
 * userId 从 Header Authorization 解析获得（FeignRequestInterceptor 透传）
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/security")
@RequiredArgsConstructor
public class SecurityController {

    private final PaySecurityMapper securityMapper;

    /**
     * 设置支付密码（首次）
     */
    @PostMapping("/setPassword")
    public ResponseResult<Boolean> setPassword(@RequestBody Map<String, String> body,
                                               HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        String password = body == null ? null : body.get("password");
        if (!StringUtils.hasText(password)) {
            return ResponseResult.fail(500, "password 必填");
        }
        PaySecurityEntity entity = securityMapper.selectOne(
                Wrappers.<PaySecurityEntity>lambdaQuery().eq(PaySecurityEntity::getUserId, userId));
        String hash = PasswordUtils.BCryptPasswordEncoder(password);
        if (entity == null) {
            entity = new PaySecurityEntity();
            entity.setUserId(userId);
            entity.setPayPassword(hash);
            entity.setPwdErrorCount(0);
            entity.setLastModifyTime(new Date());
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            securityMapper.insert(entity);
        } else {
            if (StringUtils.hasText(entity.getPayPassword())) {
                return ResponseResult.fail(500, "已设置过支付密码，请使用修改密码接口");
            }
            entity.setPayPassword(hash);
            entity.setLastModifyTime(new Date());
            entity.setUpdateTime(new Date());
            securityMapper.updateById(entity);
        }
        return ResponseResult.success(true);
    }

    /**
     * 校验支付密码
     */
    @PostMapping("/verifyPassword")
    public ResponseResult<Boolean> verifyPassword(@RequestBody Map<String, String> body,
                                                  HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        String password = body == null ? null : body.get("password");
        if (!StringUtils.hasText(password)) {
            return ResponseResult.fail(500, "password 必填");
        }
        PaySecurityEntity entity = securityMapper.selectOne(
                Wrappers.<PaySecurityEntity>lambdaQuery().eq(PaySecurityEntity::getUserId, userId));
        if (entity == null || !StringUtils.hasText(entity.getPayPassword())) {
            return ResponseResult.fail(500, "尚未设置支付密码");
        }
        if (entity.getPwdErrorCount() != null && entity.getPwdErrorCount() >= 5) {
            return ResponseResult.fail(500, "密码错误次数超限，请稍后再试");
        }
        boolean ok = Boolean.TRUE.equals(PasswordUtils.BCryptPasswordMatches(password, entity.getPayPassword()));
        if (!ok) {
            entity.setPwdErrorCount((entity.getPwdErrorCount() == null ? 0 : entity.getPwdErrorCount()) + 1);
            securityMapper.updateById(entity);
            return ResponseResult.fail(500, "支付密码错误");
        }
        entity.setPwdErrorCount(0);
        securityMapper.updateById(entity);
        return ResponseResult.success(true);
    }

    /**
     * 修改支付密码
     */
    @PostMapping("/modifyPassword")
    public ResponseResult<Boolean> modifyPassword(@RequestBody Map<String, String> body,
                                                  HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        String oldPwd = body == null ? null : body.get("oldPassword");
        String newPwd = body == null ? null : body.get("newPassword");
        if (!StringUtils.hasText(oldPwd) || !StringUtils.hasText(newPwd)) {
            return ResponseResult.fail(500, "oldPassword/newPassword 必填");
        }
        PaySecurityEntity entity = securityMapper.selectOne(
                Wrappers.<PaySecurityEntity>lambdaQuery().eq(PaySecurityEntity::getUserId, userId));
        if (entity == null || !StringUtils.hasText(entity.getPayPassword())) {
            return ResponseResult.fail(500, "尚未设置支付密码");
        }
        if (!Boolean.TRUE.equals(PasswordUtils.BCryptPasswordMatches(oldPwd, entity.getPayPassword()))) {
            return ResponseResult.fail(500, "原密码不正确");
        }
        entity.setPayPassword(PasswordUtils.BCryptPasswordEncoder(newPwd));
        entity.setPwdErrorCount(0);
        entity.setLastModifyTime(new Date());
        entity.setUpdateTime(new Date());
        securityMapper.updateById(entity);
        return ResponseResult.success(true);
    }

    /**
     * 实名认证提交（Task 0 仅落库，对接公安 / 运营商四要素见后续 Task）
     */
    @PostMapping("/realName")
    public ResponseResult<Boolean> realName(@RequestBody Map<String, String> body,
                                            HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        String name = body == null ? null : body.get("realName");
        String idCard = body == null ? null : body.get("idCard");
        if (!StringUtils.hasText(name) || !StringUtils.hasText(idCard)) {
            return ResponseResult.fail(500, "realName/idCard 必填");
        }
        PaySecurityEntity entity = securityMapper.selectOne(
                Wrappers.<PaySecurityEntity>lambdaQuery().eq(PaySecurityEntity::getUserId, userId));
        if (entity == null) {
            entity = new PaySecurityEntity();
            entity.setUserId(userId);
            entity.setCreateTime(new Date());
        }
        entity.setRealNameEncrypted(name);
        entity.setIdCardEncrypted(idCard);
        entity.setRealNameStatus(1);
        entity.setRealNameTime(new Date());
        entity.setUpdateTime(new Date());
        if (entity.getId() == null) {
            securityMapper.insert(entity);
        } else {
            securityMapper.updateById(entity);
        }
        return ResponseResult.success(true);
    }

    /**
     * 查询实名状态（POST：从 token 取 userId）
     */
    @PostMapping("/realNameStatus")
    public ResponseResult<Map<String, Object>> realNameStatus(HttpServletRequest http) {
        String userId = ApiTokenUtils.resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            return ResponseResult.fail(401, "用户未登录");
        }
        return ResponseResult.success(loadStatus(userId));
    }

    /**
     * 查询实名状态（GET by path：保留兼容）
     */
    @GetMapping("/realNameStatus/{userId}")
    public ResponseResult<Map<String, Object>> realNameStatusByPath(@PathVariable("userId") String userId) {
        return ResponseResult.success(loadStatus(userId));
    }

    private Map<String, Object> loadStatus(String userId) {
        PaySecurityEntity entity = securityMapper.selectOne(
                Wrappers.<PaySecurityEntity>lambdaQuery().eq(PaySecurityEntity::getUserId, userId));
        Map<String, Object> result = new HashMap<>();
        result.put("status", entity == null ? 0 : entity.getRealNameStatus());
        result.put("time", entity == null ? null : entity.getRealNameTime());
        result.put("hasPayPassword", entity != null && StringUtils.hasText(entity.getPayPassword()));
        return result;
    }
}
