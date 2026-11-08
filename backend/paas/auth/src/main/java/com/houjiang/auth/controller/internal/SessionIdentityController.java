package com.houjiang.auth.controller.internal;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

/** Returns only the identity of the supplied server-issued session; no caller-selected user. */
@RestController
public class SessionIdentityController {
    private final RedisTemplate<String,Object> redis;
    public SessionIdentityController(RedisTemplate<String,Object> redis) { this.redis = redis; }
    public record Identity(UserInfoVO user, List<String> permissions) {}
    @GetMapping("/internal/session/identity")
    public Identity identity(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        if (token.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Object stored = redis.opsForValue().get(HouConfig.HOU_USER_TOKEN_USER + token);
        if (stored == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        UserInfoDTO dto;
        try { dto = stored instanceof UserInfoDTO value ? value : JSON.parseObject(
                stored instanceof String value ? value : JSON.toJSONString(stored), UserInfoDTO.class); }
        catch (RuntimeException malformed) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED); }
        if (dto == null || dto.getUser() == null || dto.getUser().getUserId() == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return new Identity(dto.getUser(), dto.getPermissions() == null ? List.of() : List.copyOf(dto.getPermissions()));
    }
}
