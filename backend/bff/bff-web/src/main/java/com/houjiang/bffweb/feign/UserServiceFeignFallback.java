package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户服务Feign降级处理
/**
 *
 * @author Amos
 * @date 2024-01-01
 */
@Component
public class UserServiceFeignFallback implements UserServiceFeign {

    @Override
    public ResponseResult register(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    @Override
    public ResponseResult refreshToken(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    @Override
    public ResponseResult getUserInfo(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    @Override
    public ResponseResult getUcenterSetting(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    @Override
    public ResponseResult checkPhoneNumberExist(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    @Override
    public ResponseResult forgotPassword(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    @Override
    public ResponseResult submitCompanyRegistration(Map<String, Object> companyData) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    /** ================= 部门管理降级处理 ================= */

    @Override
    public ResponseResult getDeptListByCurrentUser() {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptListByOrgId(Long orgId) {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptTreeByCurrentUser() {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptTreeByOrgId(Long orgId) {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptDetailById(Long deptId) {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptListByOrgIdFeign(Map<String, Object> params) {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptTreeByOrgIdFeign(Map<String, Object> params) {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    /** ================= 前台用户认证降级处理 ================= */

    @Override
    public ResponseResult login(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    @Override
    public ResponseResult updateUserInfo(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    @Override
    public ResponseResult changePassword(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    @Override
    public ResponseResult logout(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    /** ================= 组织管理降级处理 ================= */

    @Override
    public ResponseResult createUserOrganization(Map<String, Object> userOrgData) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }

    @Override
    public ResponseResult getUserOrganizations(String userId) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }

    @Override
    public ResponseResult checkUserOrgExists(String userId, String orgId) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }

    @Override
    public ResponseResult getUserOrgList(Map<String, Object> params) {
        return ResponseResult.fail("组织管理服务暂时不可用");
    }

    // ================= 用户协议同意记录 =================

    @Override
    public ResponseResult getUserInfoByToken(Map<String, Object> paraMap) {
        return ResponseResult.fail("用户服务暂时不可用");
    }

    @Override
    public ResponseResult agreementConsentRecord(Map<String, Object> params) {
        return ResponseResult.fail("协议同意记录服务暂时不可用");
    }

    @Override
    public ResponseResult agreementConsentListByUser(Map<String, Object> params) {
        return ResponseResult.fail("协议同意记录服务暂时不可用");
    }

    @Override
    public ResponseResult agreementConsentPage(Map<String, Object> params) {
        return ResponseResult.fail("协议同意记录服务暂时不可用");
    }

    @Override
    public ResponseResult agreementConsentExport(Map<String, Object> params) {
        return ResponseResult.fail("协议同意记录服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitPage(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitDetail(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitRfm(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitSegmentPreview(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitCrowdSave(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitCrowdList(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitCrowdDelete(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitTagList(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitTagSave(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitTagDelete(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitTagAttach(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberPortraitTagDetach(Map<String, Object> params) {
        return ResponseResult.fail("会员画像服务暂时不可用");
    }

    @Override
    public ResponseResult memberBehaviorAnalytics(Map<String, Object> params) {
        return ResponseResult.fail("用户行为分析服务暂时不可用");
    }

    @Override
    public ResponseResult memberBehaviorTrack(Map<String, Object> params) {
        return ResponseResult.fail("用户行为分析服务暂时不可用");
    }
}
