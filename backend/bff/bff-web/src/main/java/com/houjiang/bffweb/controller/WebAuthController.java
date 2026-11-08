package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.AuthServiceFeign;
import com.houjiang.bffweb.feign.UserServiceFeign;
import com.houjiang.bffweb.feign.ThirdpartyServiceFeign;
import com.houjiang.bffweb.feign.PermissionServiceFeign;
import com.houjiang.bffweb.service.AgreementConsentService;
import com.houjiang.bffweb.util.CommonStatusEnum;
import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.bffweb.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.HashMap;

/**
 * Web端认证控制器
/**
 *
 * @description 处理Web端的认证相关请求，转发到对应的PaaS服务
 * @author Amos
 * @date 2024-01-01
 */
@Tag(name = "Web端认证管理", description = "Web端用户认证、登录、注册等接口")
@RestController
@RequestMapping("/web/auth")
public class WebAuthController {

    private static final Logger log = LoggerFactory.getLogger(WebAuthController.class);

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private AuthServiceFeign authServiceFeign;

    @Autowired
    private ThirdpartyServiceFeign thirdpartyServiceFeign;

    @Autowired
    private PermissionServiceFeign permissionServiceFeign;

    /** 注册协议校验与同意留痕聚合服务 */
    @Autowired
    private AgreementConsentService agreementConsentService;

    /**
     * 用户登录
/**
     *
     * @param loginData 登录数据
     * @return ResponseResult
     */
    @Operation(
        summary = "用户登录",
        description = "用户账号密码登录接口，支持手机号/邮箱/用户名登录"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "登录成功",
            content = @Content(schema = @Schema(implementation = ResponseResult.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "参数错误",
            content = @Content(schema = @Schema(implementation = ResponseResult.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "用户名或密码错误",
            content = @Content(schema = @Schema(implementation = ResponseResult.class))
        )
    })
    @PostMapping("/login")
    public ResponseResult login(
        @Parameter(
            description = "登录数据",
            required = true
        )
        @RequestBody Map<String, Object> loginData
    ) {
        return authServiceFeign.login(loginData);
    }


    @PostMapping("/codelogin")
    public ResponseResult codelogin(
        @Parameter(
            description = "登录数据",
            required = true
        )
        @RequestBody Map<String, Object> loginData
    ) {
        return authServiceFeign.codelogin(loginData);
    }

    /**
     * 解析短信场景：优先 scene；仅当 type 为 register/login/forgot 等场景名时才回退到 type。
     * 注意：前端注册发短信会传 type=1/2（手机/邮箱通道），不能把数字 type 当成场景。
     */
    private static String resolveSmsScene(Map<String, String> body) {
        if (body == null) {
            return "login";
        }
        String scene = body.get("scene");
        if (scene != null && !scene.isBlank()) {
            return scene.trim();
        }
        String type = body.get("type");
        if (type != null && !type.isBlank()) {
            String trimmed = type.trim();
            if ("register".equalsIgnoreCase(trimmed)
                    || "login".equalsIgnoreCase(trimmed)
                    || "forgot".equalsIgnoreCase(trimmed)
                    || "reset".equalsIgnoreCase(trimmed)) {
                return trimmed;
            }
        }
        return "login";
    }

    /**
     * 注册场景：手机号已存在则返回失败（不发短信）。
     */
    private ResponseResult assertPhoneAvailableForRegister(String userMobile) {
        if (userMobile == null || userMobile.isBlank()) {
            return null;
        }
        Map<String, Object> checkMap = new HashMap<>();
        checkMap.put("userMobile", userMobile.trim());
        ResponseResult existResult = userServiceFeign.checkPhoneNumberExist(checkMap);
        if (existResult == null) {
            return ResponseResult.fail("用户服务暂时不可用，请稍后重试");
        }
        if (existResult.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return existResult;
        }
        return null;
    }

    /**
     * 发送手机验证码
/**
     *
     * @param paraMap 发送参数（含 userMobile；type 或 scene 为 register 时先校验手机号未注册再发短信）
     * @param request HTTP请求（用于获取客户端IP）
     * @return ResponseResult 成功则已触发发送；注册场景若手机号已存在返回失败、不发短信
     */
    @PostMapping("/smscode")
    public ResponseResult smscode(@RequestBody(required = false) Map<String, String> paraMap, HttpServletRequest request) {
        // 请求体 Map 可能不可变，后续 put clientIp 会抛 UnsupportedOperationException → 网关侧 500
        final Map<String, String> body = (paraMap == null || paraMap.isEmpty())
                ? new HashMap<>()
                : new HashMap<>(paraMap);
        try {
            String userMobile = body.get("userMobile");
            String sceneType = resolveSmsScene(body);
            if ("register".equalsIgnoreCase(sceneType)) {
                ResponseResult blocked = assertPhoneAvailableForRegister(userMobile);
                if (blocked != null) {
                    return blocked;
                }
            }
            body.put("clientIp", getClientIp(request));
            return thirdpartyServiceFeign.sendCode(body);
        } catch (Exception e) {
            // 暴露异常类型 + 根因消息，便于 Kibana 区分：
            //  - FeignException$ServiceUnavailable → thirdparty 服务未注册/未启动
            //  - FeignException$NotFound          → thirdparty 路由不存在
            //  - RetryableException / SocketTimeoutException → 连接或读取超时
            //  - IllegalStateException: "No instances available" → Nacos 无实例
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            log.error("发送短信验证码失败 userMobile={}, scene={}, errType={}, errMsg={}, rootType={}, rootMsg={}",
                    body.get("userMobile"), body.get("type"),
                    e.getClass().getName(), e.getMessage(),
                    root.getClass().getName(), root.getMessage(), e);

            // 根据异常类型返回更精确的用户可见文案，避免所有异常被统一映射为
            // “短信服务暂时不可用”导致一线排查时无法区分 thirdparty 挂掉 / 超时 / 路由没注册
            String className = e.getClass().getName();
            String errMsg = e.getMessage() == null ? "" : e.getMessage();
            String frontMsg;
            if (className.contains("FeignException$NotFound")) {
                frontMsg = "短信接口未找到，请确认 thirdparty 服务是否部署最新版本";
            } else if (className.contains("FeignException$ServiceUnavailable")
                    || errMsg.contains("No instances available")) {
                frontMsg = "短信服务未在线（Nacos 无实例），请启动 thirdparty 服务";
            } else if (className.contains("RetryableException")
                    || className.contains("SocketTimeoutException")
                    || className.contains("ConnectException")) {
                frontMsg = "短信服务连接超时，请稍后重试";
            } else {
                frontMsg = "短信发送失败：" + root.getClass().getSimpleName()
                        + (root.getMessage() != null ? " - " + root.getMessage() : "");
            }
            return ResponseResult.fail(frontMsg);
        }
    }

    /**
     * 用户注册
/**
     *
     * @param paraMap 注册参数
     * @return ResponseResult
     */
    @PostMapping("/register")
    public ResponseResult register(@RequestBody Map<String, Object> paraMap, HttpServletRequest request) {
        // 请求体可能不可变，复制一份后再补充网关侧字段
        Map<String, Object> body = (paraMap == null) ? new HashMap<>() : new HashMap<>(paraMap);
        try {
            // 注册前协议校验：全部必选协议须已勾选，且提交的版本号与当前生效版本一致
            AgreementConsentService.ValidateResult validate = agreementConsentService.validateAgreements(
                    body.get("agreements"), AgreementConsentService.SCENE_REGISTER);
            if (!validate.isOk()) {
                return ResponseResult.error(validate.getMessage());
            }
            if (!validate.getConsentsJson().isEmpty()) {
                body.put("agreementConsents", validate.getConsentsJson());
            }
            // 客户端 IP / UA 必须由网关侧注入：paas 侧 IpUtils 取到的是本机网卡地址
            body.put("clientIp", WebAgreementController.getClientIp(request));
            body.put("clientUa", request.getHeader("User-Agent"));
            body.put("agreementSource", "web");
        } catch (Exception e) {
            // 协议服务异常不阻断注册（注册页仍强制勾选，此处属防御性校验）
            log.error("注册前协议校验异常，降级放行", e);
        }
        return userServiceFeign.register(body);
    }

    /**
     * 退出登录
/**
     *
     * @param user 用户信息
     * @return ResponseResult
     */
    @PostMapping("/logout")
    public ResponseResult logout(@RequestBody UserInfoVO user) {
        return authServiceFeign.logout(user);
    }

    /**
     * 刷新Token
/**
     *
     * @param paraMap 刷新参数
     * @return ResponseResult
     */
    @PostMapping("/refresh-token")
    public ResponseResult refreshToken(@RequestBody Map<String, Object> paraMap) {
        return userServiceFeign.refreshToken(paraMap);
    }

    /**
     * 获取用户信息
/**
     *
     * @param paraMap 用户参数
     * @return ResponseResult
     */
    @PostMapping("/userinfo")
    public ResponseResult getUserInfo(@RequestBody Map<String, Object> paraMap) {
        // 获取用户基本信息
        ResponseResult userInfoResult = userServiceFeign.getUserInfo(paraMap);
        
        // 如果获取用户信息失败，直接返回错误
        if (userInfoResult.getCode() != 200 || userInfoResult.getData() == null) {
            return userInfoResult;
        }
        
        // 从用户信息中提取 userId
        Map<String, Object> userInfo = (Map<String, Object>) userInfoResult.getData();
        String userId = null;
        
        if (userInfo.containsKey("userId")) {
            userId = (String) userInfo.get("userId");
        } else if (userInfo.containsKey("user_id")) {
            userId = (String) userInfo.get("user_id");
        }
        
        // 如果有 userId，查询用户的组织信息
        if (userId != null && !userId.isEmpty()) {
            try {
                Map<String, Object> orgParams = new HashMap<>();
                orgParams.put("userId", userId);
                ResponseResult orgResult = userServiceFeign.getUserOrgList(orgParams);
                
                if (orgResult.getCode() == 200 && orgResult.getData() != null) {
                    java.util.List<Map<String, Object>> orgList = 
                        (java.util.List<Map<String, Object>>) orgResult.getData();
                    
                    // 如果有组织信息，添加到返回数据中
                    if (!orgList.isEmpty()) {
                        // 主组织信息（第一个，按 is_primary 降序排列）
                        Map<String, Object> primaryOrg = orgList.get(0);
                        userInfo.put("orgId", primaryOrg.get("org_id"));
                        userInfo.put("orgName", primaryOrg.get("org_name"));
                        userInfo.put("organizationList", orgList);
                    }
                }
            } catch (Exception e) {
                // 查询组织失败不影响返回用户基本信息
                log.warn("查询用户组织信息失败 userId={}, err={}", userId, e.getMessage());
            }

            // ============= 商城后台整合 B2-2：聚合菜单 + 权限码 =============
            // 前端 stores/modules/user.ts#getUserInfo 期望 data.menu / data.permissions
            // 调 paas/permission 已有的 /permission/user/menus、/permission/user/permissions
            try {
                Map<String, Object> menuReq = new HashMap<>();
                menuReq.put("userId", userId);
                ResponseResult menuResult = permissionServiceFeign.getUserMenus(menuReq);
                if (menuResult != null && menuResult.getCode() == 200 && menuResult.getData() != null) {
                    userInfo.put("menu", menuResult.getData());
                }
            } catch (Exception e) {
                log.warn("聚合用户菜单失败 userId={}, err={}", userId, e.getMessage());
            }

            try {
                Map<String, Object> permReq = new HashMap<>();
                permReq.put("userId", userId);
                ResponseResult permResult = permissionServiceFeign.getUserPermissions(permReq);
                if (permResult != null && permResult.getCode() == 200 && permResult.getData() != null) {
                    userInfo.put("permissions", permResult.getData());
                }
            } catch (Exception e) {
                log.warn("聚合用户权限码失败 userId={}, err={}", userId, e.getMessage());
            }
        }
        
        return userInfoResult;
    }

    /**
     * 检查手机号是否存在
/**
     *
     * @param paraMap 手机号参数
     * @return ResponseResult
     */
    @PostMapping("/check-phone")
    public ResponseResult checkPhone(@RequestBody Map<String, Object> paraMap) {
        if ((paraMap.get("userMobile") == null || String.valueOf(paraMap.get("userMobile")).isBlank())
                && paraMap.get("phone") != null) {
            paraMap.put("userMobile", String.valueOf(paraMap.get("phone")).trim());
        }
        return userServiceFeign.checkPhoneNumberExist(paraMap);
    }

    /**
     * 发送验证码
/**
     *
     * @param paraMap 发送参数（scene=register 时先校验手机号未注册）
     * @param request HTTP请求（用于获取客户端IP）
     * @return ResponseResult
     */
    @PostMapping("/send-code")
    public ResponseResult sendCode(@RequestBody Map<String, String> paraMap, HttpServletRequest request) {
        Map<String, String> body = paraMap == null ? new HashMap<>() : new HashMap<>(paraMap);
        String sceneType = resolveSmsScene(body);
        if ("register".equalsIgnoreCase(sceneType)) {
            ResponseResult blocked = assertPhoneAvailableForRegister(body.get("userMobile"));
            if (blocked != null) {
                return blocked;
            }
        }
        body.put("clientIp", getClientIp(request));
        return thirdpartyServiceFeign.sendCode(body);
    }

    /**
     * 忘记密码
/**
     *
     * @param paraMap 忘记密码参数
     * @return ResponseResult
     */
    @PostMapping("/forgot-password")
    public ResponseResult forgotPassword(@RequestBody Map<String, Object> paraMap) {
        return userServiceFeign.forgotPassword(paraMap);
    }

    /**
     * 获取图片验证码
/**
     *
     * @return ResponseResult
     */
    @PostMapping("/get-verify")
    public ResponseResult getVerify() {
        return authServiceFeign.getVerify();
    }

    /**
     * 生成滑动拼图验证码（登录验证2，匿名访问）
/**
     *
     * @return ResponseResult data 包含 bgImage、imgToken、targetX、targetY
     */
    @RequestMapping(value = "/generate", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult generateSlideCaptcha() {
        return authServiceFeign.generateSlideCaptcha();
    }

    /**
     * 校验滑动拼图验证码（登录验证2，匿名访问）
/**
     *
     * @param paraMap 包含 imgToken、targetX、targetY
     * @return ResponseResult data 为 true/false
     */
    @PostMapping("/verifyGenerateCaptcha")
    public ResponseResult verifyGenerateCaptcha(@RequestBody Map<String, Object> paraMap) {
        return authServiceFeign.verifyGenerateCaptcha(paraMap);
    }

    /**
     * 提交公司登记申请
/**
     * @param companyData 公司登记信息
     * @return 提交结果
     */
    @PostMapping("/company/register")
    public ResponseResult submitCompanyRegistration(@RequestBody Map<String, Object> companyData) {
        return userServiceFeign.submitCompanyRegistration(companyData);
    }

    // ==================== 微信登录相关接口 ====================

    /**
     * 获取微信登录二维码
/**
     * 
     * @return ResponseResult 包含二维码URL和二维码ID
     */
    @RequestMapping(value = "/wechat/qrcode", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getWechatQRCode(@RequestParam(value = "application", required = false) String application) {
        return authServiceFeign.getWechatQRCode(application);
    }

    /**
     * 检查微信登录状态
/**
     * 
     * @param qrCodeId 二维码ID
     * @return ResponseResult 包含登录状态（waiting/scanned/confirmed/expired）
     */
    @GetMapping("/wechat/login-status/{qrCodeId}")
    public ResponseResult checkWechatLoginStatus(@PathVariable String qrCodeId) {
        return authServiceFeign.checkWechatLoginStatus(qrCodeId);
    }

    /**
     * 取消微信登录
/**
     * 
     * @param qrCodeId 二维码ID
     * @return ResponseResult
     */
    @PostMapping("/wechat/cancel-login/{qrCodeId}")
    public ResponseResult cancelWechatLogin(@PathVariable String qrCodeId) {
        return authServiceFeign.cancelWechatLogin(qrCodeId);
    }

    /**
     * 微信扫码登录 - 授权回调
/**
     *
     * @param code  微信下发的临时票据
     * @param state 二维码ID
     * @return ResponseResult 包含登录状态（confirmed/unbound/expired/failed），confirmed 时带 token
     */
    @RequestMapping(value = "/wechat/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult wechatCallback(@RequestParam("code") String code,
                                         @RequestParam("state") String state) {
        return authServiceFeign.wechatCallback(code, state);
    }

    // ==================== 统一登录（UniLogin）：渠道无关的扫码登录 ====================
    // 登录页在未登录态即可调用，因此全部放行（见 SecurityConfig），
    // 安全性由「state 会话校验 + 必须已绑定平台账号」两道关保证。

    /**
     * 统一登录 - 获取扫码地址
     *
     * @param channel 渠道编码：wechat / wecom / dingtalk / qq / feishu
     */
    @RequestMapping(value = "/unilogin/{channel}/qrcode", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getLoginQrCode(@PathVariable("channel") String channel,
                                         @RequestParam(value = "application", required = false) String application) {
        return authServiceFeign.getLoginQrCode(channel, application);
    }

    /**
     * 统一登录 - 检查登录状态
     */
    @GetMapping("/unilogin/{channel}/login-status/{qrCodeId}")
    public ResponseResult checkLoginStatus(@PathVariable("channel") String channel,
                                           @PathVariable("qrCodeId") String qrCodeId) {
        return authServiceFeign.checkLoginStatus(channel, qrCodeId);
    }

    /**
     * 统一登录 - 取消登录
     */
    @PostMapping("/unilogin/{channel}/cancel-login/{qrCodeId}")
    public ResponseResult cancelLogin(@PathVariable("channel") String channel,
                                      @PathVariable("qrCodeId") String qrCodeId) {
        return authServiceFeign.cancelLogin(channel, qrCodeId);
    }

    /**
     * 统一登录 - 授权回调
     */
    @RequestMapping(value = "/unilogin/{channel}/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult loginCallback(@PathVariable("channel") String channel,
                                        @RequestParam("code") String code,
                                        @RequestParam("state") String state) {
        return authServiceFeign.loginCallback(channel, code, state);
    }

    /**
     * 统一登录 - 渠道概况（编码/名称/是否已配置，不含凭据）
     */
    @GetMapping("/unilogin/channels")
    public ResponseResult listLoginChannels() {
        return authServiceFeign.listLoginChannels();
    }

    /**
     * 统一登录 - 扫码后绑定（手机号未注册时自动建会员号）
     *
     * <p>商城前台会员没有后台账号，扫码后必然是「未绑定」；这里让用户在弹窗内补一次
     * 手机号 + 短信验证码完成绑定（无账号则自动开通），会员因此不需要先有账号。</p>
     */
    @PostMapping("/unilogin/{channel}/bind-login")
    public ResponseResult bindLogin(@PathVariable("channel") String channel,
                                    @RequestBody(required = false) Map<String, Object> param) {
        return authServiceFeign.bindLogin(channel, param == null ? new java.util.HashMap<>() : param);
    }

    /**
     * 获取客户端真实IP地址（支持代理场景）
/**
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多级代理时取第一个真实IP
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
