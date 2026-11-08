package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.AuthServiceFeign;
import com.houjiang.bffweb.feign.ThirdpartyServiceFeign;
import com.houjiang.bffweb.feign.UserDeptServiceFeign;
import com.houjiang.bffweb.feign.UserAddressServiceFeign;
import com.houjiang.bffweb.feign.UserServiceFeign;
import com.houjiang.bffweb.util.CommonStatusEnum;
import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.bffweb.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务Web控制器
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/web/user")
public class WebUserController {

    private static final Logger log = LoggerFactory.getLogger(WebUserController.class);

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private UserAddressServiceFeign userAddressServiceFeign;

    @Autowired
    private AuthServiceFeign authServiceFeign;

    @Autowired
    private ThirdpartyServiceFeign thirdpartyServiceFeign;

    /** userms 的 UserDeptController 路径为 /user/dept，需走独立 Feign (path=/user) 避免拼接成 /user/ms/dept 导致 404 */
    @Autowired
    private UserDeptServiceFeign userDeptServiceFeign;

    /**
     * 账号密码登录（auth 服务 /user/login 的 bff 别名，供 GEO 等使用）
     */
    @PostMapping("/login")
    public ResponseResult login(@RequestBody Map<String, Object> loginData) {
        return authServiceFeign.login(loginData);
    }

    /**
     * 短信验证码登录（auth 服务 /user/codelogin 的 bff 别名）
     */
    @PostMapping("/codelogin")
    public ResponseResult codelogin(@RequestBody Map<String, Object> loginData) {
        return authServiceFeign.codelogin(loginData);
    }

    /**
     * 发送短信验证码（与 /web/auth/smscode 等价）
     */
    @PostMapping("/smscode")
    public ResponseResult smscode(@RequestBody(required = false) Map<String, String> paraMap, HttpServletRequest request) {
        final Map<String, String> body = (paraMap == null || paraMap.isEmpty())
                ? new HashMap<>()
                : new HashMap<>(paraMap);
        try {
            String userMobile = body.get("userMobile");
            String sceneType = body.get("type");
            if (sceneType == null || sceneType.isBlank()) {
                sceneType = body.get("scene");
            }
            if (userMobile != null && !userMobile.isBlank()
                    && sceneType != null && "register".equalsIgnoreCase(sceneType.trim())) {
                Map<String, Object> checkMap = new HashMap<>();
                checkMap.put("userMobile", userMobile.trim());
                ResponseResult existResult = userServiceFeign.checkPhoneNumberExist(checkMap);
                if (existResult == null) {
                    return ResponseResult.fail("用户服务暂时不可用，请稍后重试");
                }
                if (existResult.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                    return existResult;
                }
            }
            body.put("clientIp", resolveClientIp(request));
            return thirdpartyServiceFeign.sendCode(body);
        } catch (Exception e) {
            log.error("发送短信验证码失败 userMobile={}", body.get("userMobile"), e);
            return ResponseResult.fail("短信发送失败，请稍后重试");
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseResult logout(@RequestBody(required = false) UserInfoVO user) {
        return authServiceFeign.logout(user != null ? user : new UserInfoVO());
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseResult register(@RequestBody Map<String, Object> paraMap) {
        return userServiceFeign.register(paraMap);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public ResponseResult refreshToken(@RequestBody Map<String, Object> paraMap) {
        return userServiceFeign.refreshToken(paraMap);
    }

    /**
     * 获取用户信息
     */
    @PostMapping("/queryUserInfo")
    public ResponseResult getUserInfo(@RequestBody Map<String, Object> paraMap) {
        return userServiceFeign.getUserInfo(paraMap);
    }

    @PostMapping("/user/ms/userSetting/getInfo")
    public ResponseResult getUcenterSetting(@RequestBody Map<String, Object> params) {
        return userServiceFeign.getUcenterSetting(params);
    }

    @PostMapping("/user/ms/information/getUserInfo")
    public ResponseResult getUcenterProfile(@RequestBody Map<String, Object> params) {
        return userServiceFeign.getUserInfoByToken(params);
    }

    @PostMapping("/user/ms/updateUserInfo")
    public ResponseResult updateUcenterProfile(@RequestBody Map<String, Object> params) {
        return userServiceFeign.updateUserInfo(params);
    }

    @PostMapping("/user/address/get")
    public ResponseResult getUcenterAddresses(@RequestBody Map<String, String> params) {
        return userAddressServiceFeign.getAddressList(params);
    }

    @PostMapping("/user/address/add")
    public ResponseResult addUcenterAddress(@RequestBody Map<String, String> params) {
        return userAddressServiceFeign.addAddress(params);
    }

    @PostMapping("/user/address/update")
    public ResponseResult updateUcenterAddress(@RequestBody Map<String, String> params) {
        return userAddressServiceFeign.updateAddress(params);
    }

    @PostMapping("/user/address/delete")
    public ResponseResult deleteUcenterAddress(@RequestBody Map<String, String> params) {
        return userAddressServiceFeign.deleteAddress(params);
    }

    /**
     * 检查手机号是否存在
     */
    @PostMapping("/check-phone")
    public ResponseResult checkPhoneNumberExist(@RequestBody Map<String, Object> paraMap) {
        if ((paraMap.get("userMobile") == null || String.valueOf(paraMap.get("userMobile")).isBlank())
                && paraMap.get("phone") != null) {
            paraMap.put("userMobile", String.valueOf(paraMap.get("phone")).trim());
        }
        return userServiceFeign.checkPhoneNumberExist(paraMap);
    }

    /**
     * 忘记密码
     */
    @PostMapping("/forgot-password")
    public ResponseResult forgotPassword(@RequestBody Map<String, Object> paraMap) {
        return userServiceFeign.forgotPassword(paraMap);
    }

    /**
     * 提交公司登记申请
     */
    @PostMapping("/company/register")
    public ResponseResult submitCompanyRegistration(@RequestBody Map<String, Object> companyData) {
        return userServiceFeign.submitCompanyRegistration(companyData);
    }

    // ================= 部门管理相关接口 =================

    /**
     * 获取当前登录用户的部门列表
     */
    @GetMapping("/dept/list")
    public ResponseResult getDeptListByCurrentUser() {
        return userDeptServiceFeign.getDeptListByCurrentUser();
    }

    /**
     * 根据组织ID获取部门列表
     */
    @GetMapping("/dept/list/{orgId}")
    public ResponseResult getDeptListByOrgId(@PathVariable("orgId") Long orgId) {
        return userDeptServiceFeign.getDeptListByOrgId(orgId);
    }

    /**
     * 获取当前登录用户的部门树结构
     */
    @GetMapping("/dept/tree")
    public ResponseResult getDeptTreeByCurrentUser() {
        return userDeptServiceFeign.getDeptTreeByCurrentUser();
    }

    /**
     * 根据组织ID获取部门树结构
     */
    @GetMapping("/dept/tree/{orgId}")
    public ResponseResult getDeptTreeByOrgId(@PathVariable("orgId") Long orgId) {
        return userDeptServiceFeign.getDeptTreeByOrgId(orgId);
    }

    /**
     * 根据部门ID获取部门详细信息
     */
    @GetMapping("/dept/detail/{deptId}")
    public ResponseResult getDeptDetailById(@PathVariable("deptId") Long deptId) {
        return userDeptServiceFeign.getDeptDetailById(deptId);
    }

    // ================= 组织管理相关接口 =================

    /**
     * 创建用户组织关联关系
     */
    @PostMapping("/organization/createUserOrg")
    public ResponseResult createUserOrganization(@RequestBody Map<String, Object> userOrgData) {
        return userServiceFeign.createUserOrganization(userOrgData);
    }

    /**
     * 根据用户ID查询用户的所有组织
     */
    @GetMapping("/organization/getUserOrganizations")
    public ResponseResult getUserOrganizations(@RequestParam("userId") String userId) {
        return userServiceFeign.getUserOrganizations(userId);
    }

    /**
     * 检查用户是否已关联指定组织
     */
    @GetMapping("/organization/checkUserOrgExists")
    public ResponseResult checkUserOrgExists(@RequestParam("userId") String userId,
                                            @RequestParam("orgId") String orgId) {
        return userServiceFeign.checkUserOrgExists(userId, orgId);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
