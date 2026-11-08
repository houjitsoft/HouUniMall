package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "userms-ucenter", contextId = "ucenterServiceFeign",
        url = "${feign.userms.url:http://127.0.0.1:8195}", path = "/user/ucenter",
        fallback = UcenterServiceFeignFallback.class)
public interface UcenterServiceFeign {
    @GetMapping("/bootstrap") ResponseResult<Object> bootstrap();
    @PostMapping("/page-access/check") ResponseResult<Object> checkPageAccess(@RequestBody Map<String, Object> request);
    @GetMapping("/space/members") ResponseResult<Object> members();
    @GetMapping("/space/audits") ResponseResult<Object> audits(@RequestParam("limit") int limit);
    @PostMapping("/invitations") ResponseResult<Object> invite(@RequestBody Map<String, Object> request);
    @GetMapping("/invitations/pending") ResponseResult<Object> pendingInvitations();
    @PostMapping("/invitations/{id}/accept") ResponseResult<Object> acceptInvitation(@PathVariable("id") Long id);
    @PostMapping("/invitations/{id}/reject") ResponseResult<Object> rejectInvitation(@PathVariable("id") Long id);
    @PostMapping("/invitations/{id}/cancel") ResponseResult<Object> cancelInvitation(@PathVariable("id") Long id);
    @PutMapping("/space/members/{memberId}/menus") ResponseResult<Object> replaceMemberMenus(
            @PathVariable("memberId") Long memberId, @RequestBody Map<String, Object> request);
    @DeleteMapping("/space/members/{memberId}") ResponseResult<Object> removeMember(@PathVariable("memberId") Long memberId);
    @PostMapping("/space/ownership/transfer") ResponseResult<Object> transferOwnership(@RequestBody Map<String, Object> request);
    @PostMapping("/space/leave") ResponseResult<Object> leaveSpace();
}
