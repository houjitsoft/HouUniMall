package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.UcenterServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/web/ucenter")
public class WebUcenterController {
    /** 与 frontend/ucenter AUTH_CODES 对齐：鉴权失败时抬升 HTTP 401，便于前端清 cookie 回登录页 */
    private static final Set<Integer> AUTH_CODES = Set.of(30002, 30003, 30004, 30801, 30802, 30803);

    private final UcenterServiceFeign ucenterService;

    public WebUcenterController(UcenterServiceFeign ucenterService) {
        this.ucenterService = ucenterService;
    }

    @GetMapping("/bootstrap")
    public ResponseEntity<ResponseResult<Object>> bootstrap() {
        ResponseResult<Object> result = ucenterService.bootstrap();
        if (result != null && AUTH_CODES.contains(result.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        return ResponseEntity.ok(result);
    }
    @PostMapping("/page-access/check")
    public ResponseResult<Object> checkPageAccess(@RequestBody AccessCheckRequest request) {
        return ucenterService.checkPageAccess(Map.of("permissionCode", request.permissionCode()));
    }
    @GetMapping("/space/members") public ResponseResult<Object> members() { return ucenterService.members(); }
    @GetMapping("/space/audits") public ResponseResult<Object> audits(@RequestParam(defaultValue = "50") int limit) { return ucenterService.audits(limit); }
    @PostMapping("/invitations")
    public ResponseResult<Object> invite(@RequestBody InviteRequest request) {
        return ucenterService.invite(Map.of("mobile", request.mobile(), "menuCodes", request.menuCodes()));
    }
    @GetMapping("/invitations/pending") public ResponseResult<Object> pendingInvitations() { return ucenterService.pendingInvitations(); }
    @PostMapping("/invitations/{id}/accept") public ResponseResult<Object> acceptInvitation(@PathVariable Long id) { return ucenterService.acceptInvitation(id); }
    @PostMapping("/invitations/{id}/reject") public ResponseResult<Object> rejectInvitation(@PathVariable Long id) { return ucenterService.rejectInvitation(id); }
    @PostMapping("/invitations/{id}/cancel") public ResponseResult<Object> cancelInvitation(@PathVariable Long id) { return ucenterService.cancelInvitation(id); }
    @PutMapping("/space/members/{memberId}/menus")
    public ResponseResult<Object> replaceMemberMenus(@PathVariable Long memberId,
                                                      @RequestBody MenuGrantRequest request) {
        return ucenterService.replaceMemberMenus(memberId,
                Map.of("menuCodes", request.menuCodes()));
    }
    @DeleteMapping("/space/members/{memberId}") public ResponseResult<Object> removeMember(@PathVariable Long memberId) { return ucenterService.removeMember(memberId); }
    @PostMapping("/space/ownership/transfer")
    public ResponseResult<Object> transferOwnership(@RequestBody TransferOwnershipRequest request) {
        return ucenterService.transferOwnership(Map.of("memberId", request.memberId()));
    }
    @PostMapping("/space/leave") public ResponseResult<Object> leaveSpace() { return ucenterService.leaveSpace(); }

    public record AccessCheckRequest(String permissionCode) {}
    public record InviteRequest(String mobile, Set<String> menuCodes) {
        public InviteRequest {
            menuCodes = menuCodes == null ? Set.of() : Set.copyOf(menuCodes);
        }
    }
    public record MenuGrantRequest(List<String> menuCodes) {
        public MenuGrantRequest {
            menuCodes = menuCodes == null ? List.of() : List.copyOf(menuCodes);
        }
    }
    public record TransferOwnershipRequest(Long memberId) {}
}
