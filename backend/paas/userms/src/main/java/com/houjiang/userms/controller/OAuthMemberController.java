package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.service.OAuthMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OAuthMemberController {
    private final OAuthMemberService service;

    @PostMapping("/user/ms/feign/oauth/get-or-create")
    public ResponseResult<Map<String, Object>> getOrCreate(@RequestBody Map<String, String> body) {
        try {
            return ResponseResult.success(service.getOrCreate(body));
        } catch (IllegalArgumentException e) {
            return ResponseResult.<Map<String, Object>>fail().setCode(400).setMessage(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseResult.<Map<String, Object>>fail().setCode(409).setMessage(e.getMessage());
        }
    }
}
