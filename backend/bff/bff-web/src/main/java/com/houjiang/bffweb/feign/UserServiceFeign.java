package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户服务Feign客户端
/**
 *
 * @description 调用paas中台用户服务(userms)的接口
 * @note 默认直连本机 8195（单机部署）；多机可在 Nacos / 环境变量中设置 feign.userms.url 覆盖
 * @author Amos
 * @date 2024-01-01
 */
@FeignClient(
        name = "userms",
        url = "${feign.userms.url:http://127.0.0.1:8195}",
        path = "/user/ms",
        fallback = UserServiceFeignFallback.class
)
public interface UserServiceFeign {

    /**
     * 用户注册
/**
     *
     * @param paraMap 注册参数
     * @return ResponseResult
     */
    @PostMapping("/register")
    ResponseResult register(@RequestBody Map<String, Object> paraMap);

    /**
     * 刷新Token
/**
     *
     * @param paraMap 刷新参数
     * @return ResponseResult
     */
    @PostMapping("/refresh-token")
    ResponseResult refreshToken(@RequestBody Map<String, Object> paraMap);

    /**
     * 获取用户信息
/**
     *
     * @param paraMap 用户参数
     * @return ResponseResult
     */
    @PostMapping("/queryUserInfo")
    ResponseResult getUserInfo(@RequestBody Map<String, Object> paraMap);

    /**
     * 检查手机号是否存在
/**
     *
     * @param paraMap 手机号参数
     * @return ResponseResult
     */
    @PostMapping("/checkUserMobileExist")
    ResponseResult checkPhoneNumberExist(@RequestBody Map<String, Object> paraMap);

    /**
     * 忘记密码
/**
     *
     * @param paraMap 忘记密码参数
     * @return ResponseResult
     */
    @PostMapping("/forgot-password")
    ResponseResult forgotPassword(@RequestBody Map<String, Object> paraMap);

    /**
     * 提交公司登记申请
/**
     *
     * @param companyData 公司登记信息
     * @return ResponseResult
     */
    @PostMapping("/company/register")
    ResponseResult submitCompanyRegistration(@RequestBody Map<String, Object> companyData);

    // ================= 部门管理相关接口 =================

    /**
     * 获取当前登录用户的部门列表
/**
     *
     * @return ResponseResult
     */
    @GetMapping("/dept/list")
    ResponseResult getDeptListByCurrentUser();

    /**
     * 根据组织ID获取部门列表
/**
     *
     * @param orgId 组织ID
     * @return ResponseResult
     */
    @GetMapping("/dept/list/{orgId}")
    ResponseResult getDeptListByOrgId(@PathVariable("orgId") Long orgId);

    /**
     * 获取当前登录用户的部门树结构
/**
     *
     * @return ResponseResult
     */
    @GetMapping("/dept/tree")
    ResponseResult getDeptTreeByCurrentUser();

    /**
     * 根据组织ID获取部门树结构
/**
     *
     * @param orgId 组织ID
     * @return ResponseResult
     */
    @GetMapping("/dept/tree/{orgId}")
    ResponseResult getDeptTreeByOrgId(@PathVariable("orgId") Long orgId);

    /**
     * 根据部门ID获取部门详细信息
/**
     *
     * @param deptId 部门ID
     * @return ResponseResult
     */
    @GetMapping("/dept/detail/{deptId}")
    ResponseResult getDeptDetailById(@PathVariable("deptId") Long deptId);

    /**
     * Feign接口：根据组织ID获取部门列表
/**
     *
     * @param params 包含orgId的参数
     * @return ResponseResult
     */
    @PostMapping("/dept/feign/list")
    ResponseResult getDeptListByOrgIdFeign(@RequestBody Map<String, Object> params);

    /**
     * Feign接口：根据组织ID获取部门树结构
/**
     *
     * @param params 包含orgId的参数
     * @return ResponseResult
     */
    @PostMapping("/dept/feign/tree")
    ResponseResult getDeptTreeByOrgIdFeign(@RequestBody Map<String, Object> params);

    // ================= 前台用户认证接口 =================

    /**
     * 用户登录
/**
     *
     * @param paraMap 登录参数(username/phone, password)
     * @return ResponseResult
     */
    @PostMapping("/login")
    ResponseResult login(@RequestBody Map<String, Object> paraMap);

    /**
     * 修改用户信息
/**
     *
     * @param paraMap 用户信息
     * @return ResponseResult
     */
    @PostMapping("/updateUserInfo")
    ResponseResult updateUserInfo(@RequestBody Map<String, Object> paraMap);

    /**
     * 修改密码
/**
     *
     * @param paraMap 密码参数
     * @return ResponseResult
     */
    @PostMapping("/changePassword")
    ResponseResult changePassword(@RequestBody Map<String, Object> paraMap);

    /**
     * 退出登录
/**
     *
     * @param paraMap 参数
     * @return ResponseResult
     */
    @PostMapping("/logout")
    ResponseResult logout(@RequestBody Map<String, Object> paraMap);

    // ================= 组织管理相关接口 =================

    /**
     * 创建用户组织关联关系
/**
     *
     * @param userOrgData 用户组织关联数据
     * @return ResponseResult
     */
    @PostMapping("/organization/createUserOrg")
    ResponseResult createUserOrganization(@RequestBody Map<String, Object> userOrgData);

    /**
     * 根据用户ID查询用户的所有组织
/**
     *
     * @param userId 用户ID
     * @return ResponseResult
     */
    @GetMapping("/organization/getUserOrganizations")
    ResponseResult getUserOrganizations(@RequestParam("userId") String userId);

    /**
     * 检查用户是否已关联指定组织
/**
     *
     * @param userId 用户 ID
     * @param orgId 组织 ID
     * @return ResponseResult
     */
    @GetMapping("/organization/checkUserOrgExists")
    ResponseResult checkUserOrgExists(@RequestParam("userId") String userId,
                                     @RequestParam("orgId") String orgId);

    /**
     * 根据用户 ID 查询用户的组织列表
/**
     *
     * @param params 包含 userId 的参数
     * @return ResponseResult
     */
    @PostMapping("/getUserOrgList")
    ResponseResult getUserOrgList(@RequestBody Map<String, Object> params);

    // ================= 用户协议同意记录 =================

    /**
     * 依据登录 token 查询当前用户信息（userms 内部用 ParseTokenUtils 解析 token）
     *
     * @param paraMap 仅含 token
     * @return 用户信息
     */
    @PostMapping("/information/getUserInfo")
    ResponseResult getUserInfoByToken(@RequestBody Map<String, Object> paraMap);

    @PostMapping("/userSetting/getInfo")
    ResponseResult getUcenterSetting(@RequestBody Map<String, Object> paraMap);

    /**
     * 记录用户协议同意行为
     *
     * @param params userId / username / consents / ip / ua / source / scene
     * @return 写入条数
     */
    @PostMapping("/agreement/consent/record")
    ResponseResult agreementConsentRecord(@RequestBody Map<String, Object> params);

    /**
     * 查询某用户的同意记录
     *
     * @param params userId、includeInvalid
     * @return 记录列表
     */
    @PostMapping("/agreement/consent/listByUser")
    ResponseResult agreementConsentListByUser(@RequestBody Map<String, Object> params);

    /**
     * 后台：同意记录分页查询
     *
     * @param params page / limit / userId / username / agreementCode / beginTime / endTime / status
     * @return 分页结果
     */
    @PostMapping("/agreement/consent/admin/page")
    ResponseResult agreementConsentPage(@RequestBody Map<String, Object> params);

    /**
     * 后台：同意记录导出（全量筛选结果）
     *
     * @param params 同分页查询条件
     * @return 记录列表
     */
    @PostMapping("/agreement/consent/admin/export")
    ResponseResult agreementConsentExport(@RequestBody Map<String, Object> params);

    /**
     * 会员画像分页（真实数据聚合：会员/订单/登录/足迹/手工标签）
     *
     * @param params keyword / layer / minOrders / page / size
     * @return 分页结果 + 汇总 + 数据完整度
     */
    @PostMapping("/portrait/page")
    ResponseResult memberPortraitPage(@RequestBody Map<String, Object> params);

    /**
     * 单个会员画像详情
     *
     * @param params userId
     * @return 画像详情
     */
    @PostMapping("/portrait/detail")
    ResponseResult memberPortraitDetail(@RequestBody Map<String, Object> params);

    /**
     * RFM 八分群矩阵
     */
    @PostMapping("/portrait/rfm")
    ResponseResult memberPortraitRfm(@RequestBody Map<String, Object> params);

    /**
     * 智能分群圈人预览
     */
    @PostMapping("/portrait/segment/preview")
    ResponseResult memberPortraitSegmentPreview(@RequestBody Map<String, Object> params);

    /**
     * 保存人群包
     */
    @PostMapping("/portrait/crowd/save")
    ResponseResult memberPortraitCrowdSave(@RequestBody Map<String, Object> params);

    /**
     * 人群包列表
     */
    @PostMapping("/portrait/crowd/list")
    ResponseResult memberPortraitCrowdList(@RequestBody Map<String, Object> params);

    /**
     * 删除人群包
     */
    @PostMapping("/portrait/crowd/delete")
    ResponseResult memberPortraitCrowdDelete(@RequestBody Map<String, Object> params);

    /**
     * 标签字典
     */
    @PostMapping("/portrait/tag/list")
    ResponseResult memberPortraitTagList(@RequestBody Map<String, Object> params);

    /**
     * 新建 / 更新标签
     */
    @PostMapping("/portrait/tag/save")
    ResponseResult memberPortraitTagSave(@RequestBody Map<String, Object> params);

    /**
     * 删除标签
     */
    @PostMapping("/portrait/tag/delete")
    ResponseResult memberPortraitTagDelete(@RequestBody Map<String, Object> params);

    /**
     * 批量打标
     */
    @PostMapping("/portrait/tag/attach")
    ResponseResult memberPortraitTagAttach(@RequestBody Map<String, Object> params);

    /**
     * 批量取消打标
     */
    @PostMapping("/portrait/tag/detach")
    ResponseResult memberPortraitTagDetach(@RequestBody Map<String, Object> params);
}
