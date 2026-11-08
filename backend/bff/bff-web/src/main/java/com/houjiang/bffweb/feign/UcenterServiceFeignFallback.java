package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.commons.errorcode.domain.UpstreamErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UcenterServiceFeignFallback implements UcenterServiceFeign {
    private ResponseResult<Object> unavailable() {
        return ResponseResult.fail(UpstreamErrorCode.SERVICE_UNREACHABLE.code(),
                UpstreamErrorCode.SERVICE_UNREACHABLE.userMessage());
    }
    @Override public ResponseResult<Object> bootstrap() { return unavailable(); }
    @Override public ResponseResult<Object> checkPageAccess(Map<String, Object> request) { return unavailable(); }
    @Override public ResponseResult<Object> members() { return unavailable(); }
    @Override public ResponseResult<Object> audits(int limit) { return unavailable(); }
    @Override public ResponseResult<Object> invite(Map<String, Object> request) { return unavailable(); }
    @Override public ResponseResult<Object> pendingInvitations() { return unavailable(); }
    @Override public ResponseResult<Object> acceptInvitation(Long id) { return unavailable(); }
    @Override public ResponseResult<Object> rejectInvitation(Long id) { return unavailable(); }
    @Override public ResponseResult<Object> cancelInvitation(Long id) { return unavailable(); }
    @Override public ResponseResult<Object> replaceMemberMenus(Long memberId, Map<String, Object> request) { return unavailable(); }
    @Override public ResponseResult<Object> removeMember(Long memberId) { return unavailable(); }
    @Override public ResponseResult<Object> transferOwnership(Map<String, Object> request) { return unavailable(); }
    @Override public ResponseResult<Object> leaveSpace() { return unavailable(); }
}
