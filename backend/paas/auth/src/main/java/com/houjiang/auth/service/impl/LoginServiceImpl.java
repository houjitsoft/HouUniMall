package com.houjiang.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.auth.entity.LoginUser;
import com.houjiang.auth.feign.*;
import com.houjiang.auth.login.channel.ConfigurableOAuthChannelProvider;
import com.houjiang.auth.login.channel.LoginChannelDef;
import com.houjiang.auth.login.channel.LoginChannelException;
import com.houjiang.auth.login.channel.LoginChannelProvider;
import com.houjiang.auth.login.channel.LoginChannelRegistry;
import com.houjiang.auth.login.channel.LoginIdentity;
import com.houjiang.auth.plugin.LoginAfterGateway;
import com.houjiang.auth.plugin.OptionalPluginCaller;
import com.houjiang.auth.service.LoginServcie;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.goods.CommonShopStateDTO;
import com.houjiang.commons.dto.userms.BusinessUserInfoDTO;
import com.houjiang.commons.dto.userms.PlusEquityDTO;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.dto.webPos.ReceptionLoginDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.rocketmq.MqSendUtils;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.PayToolUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import feign.FeignException;
import feign.RetryableException;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransactionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Amos
 */
@Slf4j
@Service
public class LoginServiceImpl implements LoginServcie {

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);
    @Value("${domainName}")
    private String domainName;
    @Value("${shopAppId}")
    private String shopAppId;
    @Value("${shopAppIdSecret}")
    private String shopAppIdSecret;

    /**
     * 图形验证码开关（默认开启）。
     *
     * <p>仅在完全没有人机校验需求的内网/自动化环境才建议关掉；关掉等于后台登录
     * 只剩「账号 + 密码」，撞库成本极低。
     */
    @Value("${houcloud.auth.captcha.enabled:true}")
    private boolean captchaEnabled;

    // 第三方登录渠道的凭据不再由本类承载：统一由 LoginChannelRegistry 从
    // unilogin.channels.<渠道>.*（微信兼容旧的 wechat.open.*）实时读取，
    // 既支持 Nacos 热更新，也让新增渠道不需要改动本类。

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    @Autowired
    private BaseServiceClient baseServiceClient;

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;

    @Autowired
    private PayMentServiceClient payMentServiceClient;

    @Autowired
    private RiderServiceClient riderServiceClient;

    @Autowired
    private WebPosServiceClient webPosServiceClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SupplierServiceClient supplierServiceClient;

    @Autowired
    private LoginAfterGateway loginAfterGateway;

    /** 获取配置的辅助方法 */
    private String getDomainName() {
        return domainName;
    }
    
    private String getShopAppId() {
        return shopAppId;
    }
    
    private String getShopAppIdSecret() {
        return shopAppIdSecret;
    }

    /** 账号密码登录 */
    @Override
    @SneakyThrows
    public ResponseResult login(UserInfoVO user, HttpServletRequest request, HttpServletResponse response) {
        log.info("登录请求: mobile={}, platform={}", user.getUserMobile(), user.getPlatForm());
        
        //authenticationManager.authenticate进行用户认证
        String password = user.getPassword() == null ? user.getUserPassword() : user.getPassword();
        
        // RSA解密密码（兼容明文密码，解密失败则视为明文）
        try {
            password = JSEncrypt.decrypt(password);
            log.info("密码RSA解密成功");
        } catch (Exception e) {
            log.warn("密码非RSA加密格式，使用明文登录: {}", e.getMessage());
        }
        
        // 根据 platForm 设置 loginPlatform；Web 买家端只传 loginPlatform=1，不能被默认商家端覆盖。
        resolveLoginPlatform(user);
        
        // 确保userName和userMobile字段正确设置
        if (StrUtil.isEmpty(user.getUserName()) && !StrUtil.isEmpty(user.getUserMobile())) {
            user.setUserName(user.getUserMobile());
        }
        
        // 后台系统登录/商家登录/供应商登录 校验图形验证码
        // 🔴 2026-10-27 修 P1：原来这里挂了 `StrUtil.isEmpty(user.getPlatForm())` 的前提，
        //    请求体里带一个 platForm='2' 就能整段跳过验证码；validate() 里还留了万能码
        //    "unimall"。两处叠加等于后台/商家端完全不做人机校验，可无限撞库。
        //    现在：只要登录端是 BOSS/商家/供应商就必须过验证码（可用配置整体关闭，见 captchaEnabled）。
        if (null != user.getLoginPlatform()
                && (user.getLoginPlatform().equals(CommonType.loginPlatform.BOSS_LOGIN.getCode())
                || user.getLoginPlatform().equals(CommonType.loginPlatform.BUSINESS_LOGIN.getCode())
                || user.getLoginPlatform().equals(CommonType.loginPlatform.SUPPLIER_LOGIN.getCode()))) {
            if (captchaEnabled) {
                ResponseResult result = validate(user, user.getLoginPlatform());
                if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                    return result;
                }
            }
        }

        Map<String, Object> userParam = new HashMap<>();
        userParam.put("userMobile", user.getUserMobile());
        userParam.put("loginPlatform", user.getLoginPlatform());
        if (user.getUserName() != null) {
            userParam.put("userName", user.getUserName());
        }
        String jsonStr = JSON.toJSONString(userParam);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(jsonStr, password);
        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            log.error("认证失败: mobile={}, error={}", user.getUserMobile(), e.getMessage());
            String errorMsg = e.getMessage();
            // 发送登录失败日志
            try {
                Map<String, Object> loginLogData = new HashMap<>();
                loginLogData.put("userId", null);
                loginLogData.put("username", user.getUserMobile() != null ? user.getUserMobile() : user.getUserName());
                loginLogData.put("loginType", "password");
                loginLogData.put("loginStatus", 0);
                loginLogData.put("ip", PayToolUtil.getIpAddr(request));
                loginLogData.put("device", getDevice(request));
                loginLogData.put("browser", getBrowser(request));
                loginLogData.put("os", getOs(request));
                loginLogData.put("failReason", errorMsg);
                loginLogData.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                MqSendUtils.sendLogMessage("LOGIN", "auth", loginLogData);
            } catch (Exception mqEx) {
                log.warn("发送登录失败日志失败: {}", mqEx.getMessage());
            }
            return mapAuthenticationFailure(e);
        }

        //如果认证没通过,给出相应提示
        if (Objects.isNull(authenticate)) {
            return ResponseResult.fail(BizCodeEnume.USERNAME_PHONE_VALID_EXCEPTION);
        }

        String userMobile = user.getUserMobile() == null ? user.getUserName().trim() : user.getUserMobile().trim();
        //判断shopId是否为空
        String shopId = user.getShopId();
        Map<String, Object> returnMap = new HashMap<>();

        //如果认证通过,使用userid生成token,并返回
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        com.houjiang.commons.dto.userms.UserInfoDTO userInfoDTO = new com.houjiang.commons.dto.userms.UserInfoDTO();
        userInfoDTO.setUser(loginUser.getUser());
        userInfoDTO.setPermissions(loginUser.getPermissions());
        userInfoDTO.setAccountNonExpired(loginUser.isAccountNonExpired());
        userInfoDTO.setAccountNonLocked(loginUser.isAccountNonLocked());
        userInfoDTO.setCredentialsNonExpired(loginUser.isCredentialsNonExpired());
        userInfoDTO.setEnabled(loginUser.isEnabled());
        //当前用户信息
        UserInfoVO userInfo = loginUser.getUser();
        userInfo.setUserMobile(userMobile);
        //获取用户id
        String userId = loginUser.getUser().getUserId();
        
        //如果当前登录用户是后台系统登录
        if (null != user.getLoginPlatform() && user.getLoginPlatform().equals(CommonType.loginPlatform.BOSS_LOGIN.getCode())) {
            // 🔴 2026-10-27 修 P1：原来只要密码对就发 BOSS token，自助注册的普通会员
            //    在请求体里带 platForm=2 / loginPlatform=BOSS 就能拿到后台会话
            //    （权限码为空只是让它「点不动菜单」，会话本身是真的，能打所有只校验登录态的接口）。
            //    现在要求账号确属平台运营侧：超管（user_id=1，无 RBAC 角色，权限为通配）
            //    或持有任意平台权限码的账号，二者取一。
            if (!isPlatformOperator(userId, loginUser.getPermissions())) {
                log.warn("拒绝非平台管理员的后台登录: userId={} mobile={}", userId, userMobile);
                return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_PERMISSIONS.getCode(),
                        "该账号没有后台管理权限，请使用商家端或会员端登录");
            }
            String token = JwtUtil.createJWT(userId);
            userInfo.setUserPassword("");
            // 写入组织列表，供 finance 等服务按 org 隔离（勿默认落到组织 1）
            enrichUserOrganizations(userInfo);
            userInfoDTO.setUser(userInfo);
            // 统一使用HOU_USER_TOKEN_USER
            // 🔑 保存UserInfoDTO而不是LoginUser，确保与JWT过滤器一致
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(userInfoDTO));
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USER + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USERID + token, userId);
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USERID + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            returnMap.put("token", token);
            returnMap.put("userId", userId);
            returnMap.put("userMobile", userMobile);
            // 发送 BOSS 登录成功日志
            try {
                Map<String, Object> loginLogData = new HashMap<>();
                loginLogData.put("userId", userId);
                loginLogData.put("username", userMobile);
                loginLogData.put("loginType", "password");
                loginLogData.put("loginStatus", 1);
                loginLogData.put("ip", PayToolUtil.getIpAddr(request));
                loginLogData.put("device", getDevice(request));
                loginLogData.put("browser", getBrowser(request));
                loginLogData.put("os", getOs(request));
                loginLogData.put("failReason", null);
                loginLogData.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                MqSendUtils.sendLogMessage("LOGIN", "auth", loginLogData);
            } catch (Exception mqEx) {
                log.warn("发送登录成功日志失败: {}", mqEx.getMessage());
            }
            // after* 扩展点：后台(BOSS)登录成功后置处理，插件写入的 attrs 合并进响应。
            applyLoginAfterExtensions(returnMap, userId, (String) returnMap.get("token"));
            return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.LOGIN_SUCCEEDED.getMsg(), returnMap);
        }

        //如果当前登录用户是商家系统登录
        if (StrUtil.isEmpty(user.getPlatForm()) && null != user.getLoginPlatform()
                && (user.getLoginPlatform().equals(CommonType.loginPlatform.BUSINESS_LOGIN.getCode())
                || user.getLoginPlatform().equals(CommonType.loginPlatform.SUPPLIER_LOGIN.getCode()))) {
            //如果是商家,判断店铺是否关闭
            int isShop = userIsShop(userInfo, user.getLoginPlatform());
            if (isShop == CommonType.commonStatus.NO.getCode()) {
                return ResponseResult.fail(BizCodeEnume.THE_STORE_HAS_BEEN_CLOSED);
            }
        }
        //如果当前是商家app登录（仅限有店铺归属的账号）
        // 买家账号（shopId为空）不走商家店铺校验：hou_shop_base 可能存在 user_id 为空的脏数据，
        // 导致买家被误判为“非商家账号”而无法登录（商城PC登录 platForm=1 属于此场景）
        // 部署说明：本行改动需触发 auth 增量编译，若远端未编译请重新提交触发
        if (!StrUtil.isEmpty(user.getPlatForm()) && !StrUtil.isEmpty(user.getShopId())) {
            //如果是商家,判断店铺是否关闭
            ResponseResult shopResp = userIsShopResp(userInfo);
            if (shopResp.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return shopResp;
            }
        }


        if (shopId == null || shopId.isEmpty()) 
        {
            user.setUserState(CommonType.userState.CERTIFIED.getCode());
            user.setUserMobile(userMobile);
            user.setIsDelete(CommonType.commonStatus.YES.getCode());
//            ResponseResult countInteger = userServiceClient.queryUserInfo(user);
//            if (countInteger.getData() != null) {
//                //账号异常
//                return ResponseResult.fail(BizCodeEnume.ACCOUNT_ABNORMALITY);
//            }
            //判断用户是否是商家
            Integer isBusiness = userInfo.getIsBusiness() == null ? CommonType.commonStatus.NO.getCode() : userInfo.getIsBusiness();
            //若为企业用户，需要检查用户状态，若未审批通过则不允许登录
            Integer userState = userInfo.getUserState() == null ? CommonType.commonStatus.NO.getCode() : userInfo.getUserState();
            //如果是商家,判断商家审核状态,并返回商家信息
            ResponseResult result = BusinessMethod(isBusiness, userState, userInfo);
            if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return result;
            }
            BusinessUserInfoDTO bi = JSON.parseObject(JSON.toJSONString(result.getData()), BusinessUserInfoDTO.class);
            //判断账号是否被冻结
            if (userInfo.getIsDelete().equals(CommonType.commonStatus.YES.getCode())) {
                return ResponseResult.fail(BizCodeEnume.ACCOUNT_HAS_BEEN_FROZEN.getCode(), BizCodeEnume.ACCOUNT_HAS_BEEN_FROZEN.getMsg());
            }
            // 买家账号（无店铺归属）直接标记为非商家，不再远程查询 business/supplier 店铺状态，
            // 避免 supplier 服务内部调用 401 阻塞买家登录（商城PC登录场景）
            int isShop = StrUtil.isEmpty(userInfo.getShopId()) ? 0 : userIsShop(userInfo, user.getLoginPlatform());
            userInfo.setIsShop(isShop);

            // 处理商家账号逻辑（已移除business服务调用，仅使用用户信息中的shopId判断）
            if (null != userInfo.getShopId() && !userInfo.getShopId().trim().isEmpty()) {
                userInfo.setIsShop(1);
            } else {
                // 如果shopId为空，保持userIsShop方法设置的值
                if (userInfo.getIsShop() == null) {
                    userInfo.setIsShop(0);
                }
            }

            // 讲密码置空
            userInfo.setPassword("");

            // 默认验证帐号密码正确，创建code
            String token = JwtUtil.createJWT(userId);
            if (null == userInfo.getUserAvatar() || userInfo.getUserAvatar().isEmpty()) {
                userInfo.setUserAvatar(HouConfig.TOUXING);
            }
            userInfo.setToken(token);
            // 存储token到Redis（关键路径，必须同步）
            String redisKey = HouConfig.HOU_USER_TOKEN_USER + token;
            String userInfoJson = JSON.toJSONString(userInfoDTO);
            redisCache.setCacheObject(redisKey, userInfoJson);
            redisCache.expire(redisKey, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            redisCache.setCacheObject("login:" + userId, userInfoJson);
            redisCache.expire("login:" + userId, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId());
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USERID + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            returnMap.put("token", token);
            returnMap.put("userId", userId);
            returnMap.put("userMobile", userMobile);

            // 非关键操作异步执行，不阻塞登录响应
            final String asyncUserId = userId;
            final String asyncUserMobile = userMobile;
            final UserInfoVO asyncUserInfo = userInfo;
            CompletableFuture.runAsync(() -> {
                try {
                    // 修改用户登录次数和更新登录时间
                    Map<String, String> map = new HashMap<>();
                    map.put("userId", asyncUserId);
                    userServiceClient.updateUserInfo(map);
                    // 判断当前用户当天是否已经登录过，登录过不添加积分
                    memberPointsService(asyncUserInfo);
                    // 更新用户活跃度
                    Map<String, Object> liveMap = new HashMap<>();
                    liveMap.put("token", returnMap.get("token"));
                    userServiceClient.insertUserDayLive(liveMap);
                } catch (Exception e) {
                    log.warn("登录后异步操作失败: userId={}, error={}", asyncUserId, e.getMessage());
                }
            });
        } else {
            //走品牌店铺登录
            ResponseResult resultResp = brandLogin(loginUser);
            return resultResp;
        }
        // 发送登录成功日志（普通用户）
        try {
            Map<String, Object> loginLogData = new HashMap<>();
            loginLogData.put("userId", userId);
            loginLogData.put("username", userMobile);
            loginLogData.put("loginType", "password");
            loginLogData.put("loginStatus", 1);
            loginLogData.put("ip", PayToolUtil.getIpAddr(request));
            loginLogData.put("device", getDevice(request));
            loginLogData.put("browser", getBrowser(request));
            loginLogData.put("os", getOs(request));
            loginLogData.put("failReason", null);
            loginLogData.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            MqSendUtils.sendLogMessage("LOGIN", "auth", loginLogData);
        } catch (Exception mqEx) {
            log.warn("发送登录成功日志失败: {}", mqEx.getMessage());
        }
        // after* 扩展点：登录成功后置处理（打标签/风控/同步），插件写入的 attrs 合并进响应。
        applyLoginAfterExtensions(returnMap, userId, (String) returnMap.get("token"));
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.LOGIN_SUCCEEDED.getMsg(), returnMap);
    }

    /**
     * 登录成功后的 after* 扩展点调用：把二开插件写入的 attrs 合并进登录响应。
     *
     * <p>插件故障由 {@code ExtensionGatewaySupport} 隔离，不影响登录；插件显式 {@code abort}
     * 抛出的业务异常<b>不吞</b>（由调用方与全局异常处理对待），避免「静默按成功处理」。
     * userId 非数值时以 {@code null} 传递，插件应能容忍。
     */
    private void applyLoginAfterExtensions(Map<String, Object> returnMap, String userId, String token) {
        Long uid = null;
        if (StrUtil.isNotBlank(userId)) {
            try {
                uid = Long.valueOf(userId.trim());
            } catch (NumberFormatException ignore) {
                uid = null;
            }
        }
        Map<String, Object> attrs = loginAfterGateway.afterLogin(uid, token, null);
        if (attrs != null && !attrs.isEmpty()) {
            returnMap.putAll(attrs);
        }
    }

    /**
     * 解析登录平台。兼容旧客户端的 platForm 字段，同时保留新客户端显式传入的 loginPlatform（商城 PC=1）。
     */
    static void resolveLoginPlatform(UserInfoVO user) {
        if (!StrUtil.isEmpty(user.getPlatForm())) {
            try {
                user.setLoginPlatform(Integer.parseInt(user.getPlatForm()));
            } catch (NumberFormatException e) {
                user.setLoginPlatform(CommonType.loginPlatform.BUSINESS_LOGIN.getCode());
            }
        } else if (user.getLoginPlatform() == null) {
            // 商城买家登录以手机号为账号；旧客户端可能未传平台字段，默认按 PC 买家处理，
            // 避免被误判为商家后台并触发旧图形验证码（11030）。
            user.setLoginPlatform(StrUtil.isNotEmpty(user.getUserMobile())
                    ? CommonType.loginPlatform.PC_LOGIN.getCode()
                    : CommonType.loginPlatform.BUSINESS_LOGIN.getCode());
        }
    }

    /**
     * Spring Security 会把 UserDetailsService 的下游异常包装成认证异常。
     * 只有真正的凭据错误才应返回 11003，连接/超时/5xx 必须让前端明确提示服务暂时不可用。
     */
    private boolean isAuthenticationInfrastructureFailure(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof UserServiceLookupException
                    || current instanceof RetryableException
                    || current instanceof java.net.ConnectException
                    || current instanceof java.net.SocketTimeoutException) {
                return true;
            }
            if (current instanceof FeignException feignException) {
                int status = feignException.status();
                return status == -1 || status == 408 || status == 429 || status >= 500;
            }
            current = current.getCause();
        }
        return false;
    }

    ResponseResult mapAuthenticationFailure(Exception exception) {
        String errorMsg = exception.getMessage();
        if (isAuthenticationInfrastructureFailure(exception)
                || (errorMsg != null && (errorMsg.contains("Load balancer does not contain an instance")
                || errorMsg.contains("No servers available")
                || errorMsg.contains("503")))) {
            log.error("登录认证依赖暂时不可用", exception);
            return ResponseResult.fail(11009, "登录认证服务暂时不可用，请稍后重试");
        }
        if (BizCodeEnume.USER_DOES_NOT_EXIST.getMsg().equals(errorMsg)) {
            return ResponseResult.fail(BizCodeEnume.USER_DOES_NOT_EXIST);
        }
        if (isCredentialFailure(exception)) {
            return ResponseResult.fail(BizCodeEnume.USERNAME_PHONE_VALID_EXCEPTION);
        }

        log.error("登录认证处理异常", exception);
        return ResponseResult.fail(11009, "登录认证服务暂时不可用，请稍后重试");
    }

    private boolean isCredentialFailure(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof BadCredentialsException
                    || current instanceof UsernameNotFoundException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * 验证码登录查用户：与密码登录一样对短暂网络故障重试一次。
     */
    private UserInfoVO loadUserForCodeLoginWithRetry(String userMobile, String shopId) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                if (shopId == null || shopId.isEmpty()) {
                    return userServiceClient.getUserInfoByMobile(userMobile);
                }
                UserInfoVO userInfoVO = new UserInfoVO();
                userInfoVO.setUserMobile(userMobile);
                userInfoVO.setLoginType(CommonType.commonStatus.YES.getCode());
                userInfoVO.setShopId(shopId);
                ResponseResult userInfo = userServiceClient.querUserInfoByParams(userInfoVO);
                if (userInfo != null && userInfo.getData() != null) {
                    return JSON.parseObject(JSON.toJSONString(userInfo.getData()), UserInfoVO.class);
                }
                return null;
            } catch (RuntimeException e) {
                last = e;
                if (!isAuthenticationInfrastructureFailure(e) || attempt == 2) {
                    throw e;
                }
                log.warn("验证码登录查用户暂时失败，准备重试 {}/2: {}", attempt + 1, e.getMessage());
                try {
                    Thread.sleep(80L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw last;
    }



    /**
     * 品牌店铺用户登录
     * 非关键操作（更新登录时间、积分、活跃度）异步执行
     */
    public ResponseResult brandLogin(LoginUser loginUser) {
        UserInfoVO userInfo = loginUser.getUser();
        Map<String, Object> returnMap = new HashMap<>();

        Integer isBusiness = userInfo.getIsBusiness() != null ? userInfo.getIsBusiness() : 0;
        Integer userState = userInfo.getUserState() != null ? userInfo.getUserState() : 0;
        ResponseResult result = BusinessMethod(isBusiness, userState, userInfo);
        if (result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return result;
        }

        userInfo.setPassword("");
        String token = JwtUtil.createJWT(userInfo.getUserId());
        userInfo.setToken(token);
        if (null == userInfo.getUserAvatar() || userInfo.getUserAvatar().isEmpty()) {
            userInfo.setUserAvatar(HouConfig.TOUXING);
        }
        enrichUserOrganizations(userInfo);
        // 存储token到Redis（关键路径）
        redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(loginUser));
        redisCache.expire(HouConfig.HOU_USER_TOKEN_USER + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
        redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId());
        redisCache.expire(HouConfig.HOU_USER_TOKEN_USERID + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
        returnMap.put("token", token);
        returnMap.put("userId", userInfo.getUserId());
        returnMap.put("userMobile", userInfo.getUserMobile());

        // 非关键操作异步执行
        final UserInfoVO asyncUserInfo = userInfo;
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> map = new HashMap<>();
                map.put("userId", asyncUserInfo.getUserId());
                userServiceClient.updateUserInfo(map);
                memberPointsService(asyncUserInfo);
                Map<String, Object> liveMap = new HashMap<>();
                liveMap.put("token", token);
                userServiceClient.insertUserDayLive(liveMap);
            } catch (Exception e) {
                log.warn("品牌登录异步操作失败: userId={}, error={}", asyncUserInfo.getUserId(), e.getMessage());
            }
        });

        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.LOGIN_SUCCEEDED.getMsg(), returnMap);
    }


    /**
     * 登录成功后把组织列表写入 UserInfoVO，供 Redis Token 会话做多租户隔离。
     * 财务等服务依赖会话中的 orgList/orgId，禁止再默认落到组织 1。
     */
    private void enrichUserOrganizations(UserInfoVO userInfo) {
        if (userInfo == null || StrUtil.isEmpty(userInfo.getUserId())) {
            return;
        }
        if (userInfo.getOrgList() != null && !userInfo.getOrgList().isEmpty()) {
            if (StrUtil.isEmpty(userInfo.getOrgId())) {
                applyPrimaryOrgId(userInfo);
            }
            return;
        }
        try {
            ResponseResult result = userServiceClient.getUserOrganizations(userInfo.getUserId());
            if (result == null || (result.getCode() != 1 && result.getCode() != 200) || result.getData() == null) {
                log.warn("登录补全组织信息未成功 userId={}, code={}, message={}",
                        userInfo.getUserId(),
                        result == null ? null : result.getCode(),
                        result == null ? null : result.getMessage());
                return;
            }
            Object data = result.getData();
            if (!(data instanceof List<?> rawList)) {
                return;
            }
            List<Map<String, Object>> orgList = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> map) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getKey() != null) {
                            copy.put(entry.getKey().toString(), entry.getValue());
                        }
                    }
                    orgList.add(copy);
                }
            }
            userInfo.setOrgList(orgList);
            applyPrimaryOrgId(userInfo);
            log.info("登录会话已写入组织信息 userId={}, orgCount={}, orgId={}",
                    userInfo.getUserId(), orgList.size(), userInfo.getOrgId());
        } catch (Exception e) {
            log.warn("登录补全组织信息失败 userId={}: {}", userInfo.getUserId(), e.getMessage());
        }
    }

    private void applyPrimaryOrgId(UserInfoVO userInfo) {
        List<Map<String, Object>> orgList = userInfo.getOrgList();
        if (orgList == null || orgList.isEmpty()) {
            return;
        }
        for (Map<String, Object> org : orgList) {
            if (org == null) {
                continue;
            }
            Object isPrimary = org.get("is_primary");
            if (isPrimary != null && "1".equals(isPrimary.toString())) {
                Object orgId = org.get("org_id") != null ? org.get("org_id") : org.get("orgId");
                if (orgId != null) {
                    userInfo.setOrgId(orgId.toString());
                    return;
                }
            }
        }
        Object orgId = orgList.get(0).get("org_id");
        if (orgId == null) {
            orgId = orgList.get(0).get("orgId");
        }
        if (orgId != null) {
            userInfo.setOrgId(orgId.toString());
        }
    }

    /**
     * 是否平台运营侧账号（可以拿 BOSS 会话）。
     *
     * <p>判定口径与权限服务的既有事实保持一致：
     * <ul>
     *   <li>{@code user_id=1} 是内置超管，没有 RBAC 角色行，权限集是通配 —— 必须放行；</li>
     *   <li>其余账号只要名下挂着任意一条平台权限码就算运营侧；</li>
     *   <li>普通注册会员权限集为空 → 拒绝。</li>
     * </ul>
     */
    private boolean isPlatformOperator(String userId, java.util.List<String> permissions) {
        if ("1".equals(userId)) {
            return true;
        }
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        for (String permission : permissions) {
            if (permission != null && !permission.isBlank()) {
                return true;
            }
        }
        return false;
    }

    /** 验证码校验方法 */
    private ResponseResult validate(UserInfoVO userInfo, Integer loginPlatform) {

        String code = userInfo.getCode();
        String sign = userInfo.getSign();

        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(sign)) {
            return ResponseResult.fail(BizCodeEnume.VERIFICATION_CODE_CANNOT_BE_EMPTY);
        }

        if (loginPlatform.equals(CommonType.loginPlatform.BOSS_LOGIN.getCode())) {
            // 🔴 不再接受万能码 "unimall"：那是调试期留下的后门，等于验证码形同虚设
            String expected = redisCache.getCacheObject("PLATFORM_CODE_" + sign);
            if (expected == null || !code.equalsIgnoreCase(expected)) {
                return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
            }
            // 一次性使用，使用后删除
            redisCache.deleteObject("PLATFORM_CODE_" + sign);
        } else if (loginPlatform.equals(CommonType.loginPlatform.BUSINESS_LOGIN.getCode())) {
            String expected = redisCache.getCacheObject("USER_CODE_" + sign);
            if (expected == null || !code.equalsIgnoreCase(expected)) {
                return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
            }
            // 一次性使用，使用后删除
            redisCache.deleteObject("USER_CODE_" + sign);
        } else if (loginPlatform.equals(CommonType.loginPlatform.SUPPLIER_LOGIN.getCode())) {
            String expected = redisCache.getCacheObject("SUPPLIER_CODE_" + sign);
            if (expected == null) {
                expected = redisCache.getCacheObject("PLATFORM_CODE_" + sign);
            }
            if (expected == null || !code.equals(expected)) {
                return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
            }
            redisCache.deleteObject("SUPPLIER_CODE_" + sign);
            redisCache.deleteObject("PLATFORM_CODE_" + sign);
        }

        return ResponseResult.success();

    }

    /** 商家方法 */
    public ResponseResult BusinessMethod(Integer isBusiness, Integer userState, UserInfoVO userInfo) {
        BusinessUserInfoDTO bi = new BusinessUserInfoDTO();
        Map<String, String> businessMap = new HashMap<>();
        if (CommonType.commonStatus.YES.getCode().equals(isBusiness)) {
            businessMap.put("userId", userInfo.getUserId() + "");
            //获取企业用户实体类
            ResponseResult businessResult = userServiceClient.getBusinessInfoByMap(businessMap);
            if (businessResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
                return businessResult;
            }
            bi = JSON.parseObject(JSON.toJSONString(businessResult.getData()), BusinessUserInfoDTO.class);

            //账号审核未通过
            if (CommonType.commonStatus.NO.getCode().equals(userState)) {
                Map<String, String> rMap = new HashMap<>();
                rMap.put("taxId", bi.getTaxId());
                rMap.put("companyName", bi.getCompanyName());
                rMap.put("businessLicense", bi.getBusinessLicense());
                rMap.put("isApproved", bi.getIsApproved().toString());
                rMap.put("userId", userInfo.getUserId());
                rMap.put("isBusiness", isBusiness.toString());
                rMap.put("auditComment", bi.getAuditComment());
                //账号尚未审核通过，请耐心等待
                return ResponseResult.fail(BizCodeEnume.ACCOUNT_HAS_NOT_BEEN_APPROVED.getCode(),
                        BizCodeEnume.ACCOUNT_HAS_NOT_BEEN_APPROVED.getMsg(), rMap.toString());
            }
        }

        return ResponseResult.success(bi);
    }


    /** 用户会员,积分,等级方法 */
    public void memberPointsService(UserInfoVO userInfo) {
        //判断当前用户当天是否已经登录过，登录过不添加积分
        Map<String, String> param = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        param.put("startTime", sdf.format(DateUtils.getStartTime()));
        param.put("endTime", sdf.format(DateUtils.getEndTime()));
        param.put("userId", userInfo.getUserId());
        param.put("classId", "2");

        ResponseResult data = userServiceClient.queryLoginExit(param);


        if (data.getData() == null) {
            //查询积分规则
            ResponseResult resultResp = baseServiceClient.queryIntegralRules();
            Map<String, Integer> integralRules = (Map<String, Integer>) resultResp.getData();
            //修改会员积分和会员等级
            MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
            ArrayList<String> list = new ArrayList<>();
            list.add(userInfo.getUserId());
            updateMemberMessageListIntegral.setUserIdList(list);
            updateMemberMessageListIntegral.setIntegral(integralRules.get("ruleLogin"));
            //1是增加积分 0 是扣除积分
            updateMemberMessageListIntegral.setType(1);
            userServiceClient.updateMemberIntegral(updateMemberMessageListIntegral);

            //添加积分修改记录
            Map<String, Object> pointParam = new HashMap<>();
            pointParam.put("PointsLogType", CommonType.pointsLogType.GAIN.getCode());
            pointParam.put("ClassId", CommonType.pointType.LOGIN.getCode());
            pointParam.put("PointsLogTime", new Date());
            pointParam.put("PointsLogDesc", CommonType.pointType.LOGIN.getDesc());
            pointParam.put("PointsLogFlag", "reg");
            pointParam.put("UserId", userInfo.getUserId());
            pointParam.put("PointsLogPoints", integralRules.get("ruleLogin"));
            pointParam.put("PointsLogOperate", CommonType.pointType.LOGIN.getDesc());
            userServiceClient.insertPointLogByMap(pointParam);

            //更新最后登录时间 登录次数
            userServiceClient.updateLoginTimeCount(userInfo.getUserId());

        }
    }

    @Override
    public ResponseResult logout(UserInfoVO user) {
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(user.getToken());
        // 统一使用HOU_USER_TOKEN_USER
        redisCache.deleteObject(HouConfig.HOU_USER_TOKEN_USER + user.getToken());
        redisCache.deleteObject(HouConfig.HOU_USER_TOKEN_USERID + user.getToken());
        if (ui != null) {
            String userid = ui.getUserId();
            redisCache.deleteObject("login:" + userid);
            redisCache.deleteObject("login:" + userid);
            redisCache.deleteObject(HouConfig.RANDOM_TOKEN + userid);
            // 统一使用HOU_USER_TOKEN_USER
            redisCache.deleteObject(HouConfig.HOU_USER_TOKEN_USER + user.getToken());
        }
        return ResponseResult.success(BizCodeEnume.EXIT_SUCCESSFULLY.getMsg());
    }

    /**
     * 手机验证码登录
     *
     * @param paraMap 包含登录参数的映射，包括手机验证码、用户手机号和店铺ID
     * @return 登录结果，成功返回包含令牌和其他信息的映射，失败返回错误信息
     * <p>
     * 登录流程包括：
     * 1. 验证验证码是否正确；
     * 2. 根据店铺ID查询用户信息，可能是平台用户或品牌店铺用户；
     * 3. 处理企业用户审核状态；
     * 4. 查询用户是否为商家，并设置相关信息；
     * 5. 生成JWT令牌并返回用户信息。
     */
    /** 验证码登录 */
    @Override
    public ResponseResult codelogin(Map<String, String> paraMap) {
        // 从参数映射中获取手机验证码和用户手机号
        // 支持两种参数名：phoneCode/code, userMobile/phone
        String phoneCode = paraMap.get("phoneCode");
        if (phoneCode == null || phoneCode.isEmpty()) {
            phoneCode = paraMap.get("code");
        }
        String userMobile = paraMap.get("userMobile");
        if (userMobile == null || userMobile.isEmpty()) {
            userMobile = paraMap.get("phone");
        }
        
        // 参数验证
        if (phoneCode == null || phoneCode.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE.getCode(), "验证码不能为空");
        }
        if (userMobile == null || userMobile.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE.getCode(), "手机号不能为空");
        }
        
        // 短信验证码由 thirdparty 经 RedisUtil(Jedis 明文) 写入；优先同路径读取，避免 RedisCache(FastJson) 读不到
        phoneCode = phoneCode.trim();
        String redisPhoneCode = resolveSmsCodeFromRedis(userMobile);
        if (redisPhoneCode == null || !phoneCode.equals(redisPhoneCode)) {
            log.warn("验证码登录校验失败 mobile={}, redisHasCode={}", userMobile, redisPhoneCode != null);
            return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
        }

        // 获取当前 HTTP 请求（用于日志）
        HttpServletRequest currentRequest = null;
        try {
            currentRequest = ((jakarta.servlet.http.HttpServletRequest) ((org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest());
        } catch (Exception ignored) {
        }

        // 检查是否是合同签署场景（不验证用户是否存在）
        String loginType = paraMap.get("loginType");
        boolean isContractSign = "contract_sign".equals(loginType) || "contract".equals(loginType);

        // 根据是否有店铺ID来决定查询哪种用户信息（短暂 Feign 故障自动重试）
        UserInfoVO ucenterUserInfo = null;
        String shopId = paraMap.get("shopId");
        try {
            ucenterUserInfo = loadUserForCodeLoginWithRetry(userMobile, shopId);
        } catch (Exception e) {
            log.error("验证码登录查询用户失败 mobile={}", userMobile, e);
            return ResponseResult.fail(11009, "用户服务暂时不可用，请稍后重试");
        }

        // 合同签署场景：不验证用户是否存在，直接创建临时用户信息
        if (isContractSign) {
            if (ucenterUserInfo == null || ucenterUserInfo.getUserId() == null || ucenterUserInfo.getUserId().isEmpty()) {
                ucenterUserInfo = new UserInfoVO();
                ucenterUserInfo.setUserId("CONTRACT_SIGN_" + System.currentTimeMillis());
                ucenterUserInfo.setUserMobile(userMobile);
                ucenterUserInfo.setUserNickname("合同签署用户");
                ucenterUserInfo.setIsShop(0);
            }
        } else {
            if (ucenterUserInfo == null || ucenterUserInfo.getUserId() == null || ucenterUserInfo.getUserId().isEmpty()) {
                return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE.getCode(), "用户不存在，请先注册");
            }
        }

        // 校验通过后再删除验证码（Jedis + RedisCache 双删，兼容历史写入路径）
        try {
            RedisUtil.remove(userMobile);
        } catch (Exception e) {
            log.warn("RedisUtil 删除验证码失败 mobile={}: {}", userMobile, e.getMessage());
        }
        redisCache.deleteObject(userMobile);

        if (null != ucenterUserInfo.getShopId() && !ucenterUserInfo.getShopId().trim().isEmpty()) {
            ucenterUserInfo.setIsShop(1);
        } else {
            ucenterUserInfo.setIsShop(0);
        }
        ucenterUserInfo.setPassword("");

        Map<String, Object> returnMap = new HashMap<>();
        String token = JwtUtil.createJWT(ucenterUserInfo.getUserId());
        if (null == ucenterUserInfo.getUserAvatar() || ucenterUserInfo.getUserAvatar().isEmpty()) {
            ucenterUserInfo.setUserAvatar(HouConfig.TOUXING);
        }
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        enrichUserOrganizations(ucenterUserInfo);
        userInfoDTO.setUser(ucenterUserInfo);
        redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(userInfoDTO));
        redisCache.expire(HouConfig.HOU_USER_TOKEN_USER + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
        redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USERID + token, ucenterUserInfo.getUserId());
        redisCache.expire(HouConfig.HOU_USER_TOKEN_USERID + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);

        returnMap.put("token", token);
        returnMap.put("userId", ucenterUserInfo.getUserId());
        returnMap.put("userMobile", ucenterUserInfo.getUserMobile());

        // 与密码登录一致：积分/活跃度等非关键操作异步执行，避免 Feign 超时导致验证码登录失败
        final UserInfoVO asyncUserInfo = ucenterUserInfo;
        final String asyncToken = token;
        CompletableFuture.runAsync(() -> {
            try {
                memberPointsService(asyncUserInfo);
                Map<String, Object> liveMap = new HashMap<>();
                liveMap.put("token", asyncToken);
                liveMap.put("userId", asyncUserInfo.getUserId());
                liveMap.put("userMobile", asyncUserInfo.getUserMobile());
                userServiceClient.insertUserDayLive(liveMap);
            } catch (Exception e) {
                log.warn("验证码登录后异步操作失败: userId={}, error={}", asyncUserInfo.getUserId(), e.getMessage());
            }
        });

        try {
            Map<String, Object> loginLogData = new HashMap<>();
            loginLogData.put("userId", ucenterUserInfo.getUserId());
            loginLogData.put("username", userMobile);
            loginLogData.put("loginType", "sms");
            loginLogData.put("loginStatus", 1);
            loginLogData.put("ip", currentRequest != null ? PayToolUtil.getIpAddr(currentRequest) : null);
            loginLogData.put("device", currentRequest != null ? getDevice(currentRequest) : "Unknown");
            loginLogData.put("browser", currentRequest != null ? getBrowser(currentRequest) : "Unknown");
            loginLogData.put("os", currentRequest != null ? getOs(currentRequest) : "Unknown");
            loginLogData.put("failReason", null);
            loginLogData.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            MqSendUtils.sendLogMessage("LOGIN", "auth", loginLogData);
        } catch (Exception mqEx) {
            log.warn("发送验证码登录成功日志失败: {}", mqEx.getMessage());
        }

        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(),
                BizCodeEnume.LOGIN_SUCCEEDED.getMsg(), returnMap);
    }

    /**
     * 读取短信验证码：优先 RedisUtil（与 SendSmsServiceImpl 写入一致），再回退 RedisCache。
     */
    private String resolveSmsCodeFromRedis(String userMobile) {
        try {
            String code = RedisUtil.getNotExpire(userMobile);
            if (code != null && !code.trim().isEmpty()) {
                return code.trim();
            }
        } catch (Exception e) {
            log.warn("RedisUtil 读取验证码失败 mobile={}: {}", userMobile, e.getMessage());
        }
        try {
            if (!redisCache.hasKey(userMobile)) {
                return null;
            }
            Object cacheObj = redisCache.getCacheObject(userMobile);
            if (cacheObj == null) {
                return null;
            }
            if (cacheObj instanceof String) {
                return ((String) cacheObj).trim();
            }
            String str = cacheObj.toString();
            if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 1) {
                try {
                    String parsed = JSON.parseObject(str, String.class);
                    return parsed != null ? parsed.trim() : null;
                } catch (Exception ignored) {
                    return str.trim();
                }
            }
            return str.trim();
        } catch (Exception e) {
            log.warn("RedisCache 读取验证码失败 mobile={}: {}", userMobile, e.getMessage());
            return null;
        }
    }

    /**
     * 通过查询用户对应的店铺状态来确定用户是否是正常的商家。如果用户是供应商登录，则店铺类型为供应商店铺；否则，为商家店铺。
     * 如果店铺状态正常，直接返回确认结果。如果店铺状态不正常，则进一步检查用户是否是商家子账号或者是否有关联的门面店，以确定用户是否是商家。
     *
     * @param userInfo      用户信息，包含用户ID和店铺ID等关键信息。
     * @param loginPlatform 用户登录平台的类型，用于区分供应商登录和其他登录。
     * @return 返回一个整数代码，表示用户是否是商家。是商家返回YES的代码，否则返回NO的代码。
     */
    public Integer userIsShop(UserInfoVO userInfo, Integer loginPlatform) {
        // 初始化一个Map，用于存储查询店铺状态所需的参数。
        Map<String, String> shopMap = new HashMap<>();
        // 将用户ID和店铺ID转换为字符串形式，并添加到shopMap中。
        shopMap.put("userId", userInfo.getUserId());
        shopMap.put("shopId", userInfo.getShopId());
        // 设置店铺状态为成功开业。
        //开店成功
        shopMap.put("shopStatus", CommonType.storeStatus.SUCCESSFULLY.getCode() + "");
        // 根据登录平台类型，设置店铺类型。
        if (loginPlatform != null && loginPlatform.equals(CommonType.loginPlatform.SUPPLIER_LOGIN.getCode())) {
            //供应商店铺
            shopMap.put("shopType", CommonType.storeType.SUPPLIER_STORE.getCode() + "");
        } else {
            //商家店铺
            shopMap.put("shopType", CommonType.storeType.SELLER_STORE.getCode() + "");
        }
        // 调用业务服务客户端，查询店铺状态。
        //查询是否正常商家
        boolean supplierLogin = loginPlatform != null
                && loginPlatform.equals(CommonType.loginPlatform.SUPPLIER_LOGIN.getCode());
        ResponseResult shopResult;
        if (supplierLogin) {
            String serviceToken = java.util.UUID.randomUUID().toString();
            String key = "AUTH_SUPPLIER_QUERY:" + serviceToken;
            redisCache.setCacheObject(key, userInfo.getUserId());
            redisCache.expire(key, 30, TimeUnit.SECONDS);
            String identityToken = JwtUtil.createJWT(userInfo.getUserId(), 30000L);
            String identityKey = HouConfig.HOU_USER_TOKEN_USER + identityToken;
            com.houjiang.commons.dto.userms.UserInfoDTO identity = new com.houjiang.commons.dto.userms.UserInfoDTO();
            identity.setUser(userInfo);
            redisCache.setCacheObject(identityKey, JSON.toJSONString(identity));
            redisCache.expire(identityKey, 30, TimeUnit.SECONDS);
            try {
                shopResult = supplierServiceClient.queryShopStatus(shopMap, serviceToken, "Bearer " + identityToken);
            } finally {
                redisCache.deleteObject(key);
                redisCache.deleteObject(identityKey);
            }
        } else {
            shopResult = merchantServiceClient.queryShopStatus(shopMap);
        }
        if (shopResult != null && shopResult.getCode() == CommonStatusEnum.SUCCESS.getCode()
                && shopResult.getData() != null) {
            if (supplierLogin) {
                com.alibaba.fastjson.JSONObject shop = JSON.parseObject(JSON.toJSONString(shopResult.getData()));
                userInfo.setShopId(shop.getString("supplierShopId"));
                userInfo.setShopType(CommonType.storeType.SUPPLIER_STORE.getCode());
                userInfo.setIsShop(CommonType.commonStatus.YES.getCode());
            }
            return CommonType.commonStatus.YES.getCode();
        } else {
            if (supplierLogin) {
                return CommonType.commonStatus.NO.getCode();
            }
            // 如果店铺状态不正常，进一步检查用户是否是商家子账号。
            // 是否为商家子账号
            ResponseResult seller = userServiceClient.queryShopSellerBase(userInfo.getUserId());
            // 如果用户是商家子账号，返回确认结果。
            if (seller.getCode() == CommonStatusEnum.SUCCESS.getCode()) {
                return CommonType.commonStatus.YES.getCode();
            }
            // 如果用户不是商家子账号，检查用户信息中是否有shopId（已移除business服务调用）
            // 如果用户信息中有shopId，表示是商家
            if (userInfo.getShopId() != null && !userInfo.getShopId().trim().isEmpty()) {
                return CommonType.commonStatus.YES.getCode();
            }
            // 如果以上条件都不满足，表示用户不是商家，返回否定结果。
            return CommonType.commonStatus.NO.getCode();
        }
    }


    /**
     * 查询是否正常商家
     *
     * @param userInfo
     * @return
     */
    public ResponseResult userIsShopResp(UserInfoVO userInfo) {
        // 初始化一个Map用于传递查询店铺状态的参数
        Map<String, String> shopMap = new HashMap<>();
        // 将用户ID转换为字符串并添加到参数Map中
        shopMap.put("userId", userInfo.getUserId() + "");
        // 设置查询参数为卖家店铺类型
        //商家店铺
        shopMap.put("shopType", CommonType.storeType.SELLER_STORE.getCode() + "");
        // 调用业务服务客户端查询店铺状态
        //查询是否正常商家
        ResponseResult shopResult = merchantServiceClient.queryShopStatus(shopMap);

        // 如果查询结果不为空
        if (null != shopResult.getData()) {
            // 将查询结果转换为CommonShopStateDTO对象
            CommonShopStateDTO shopStateDTO = JSON.parseObject(JSON.toJSONString(shopResult.getData()), CommonShopStateDTO.class);
            // 如果店铺状态不为成功，则返回店铺已关闭的错误信息
            if (!shopStateDTO.getShopStatus().equals(CommonType.storeStatus.SUCCESSFULLY.getCode())) {
                return ResponseResult.fail(BizCodeEnume.THE_STORE_HAS_BEEN_CLOSED);
            }
            // 如果店铺状态正常，则返回成功信息
            return ResponseResult.success();
        } else {
            // 如果第一次查询结果为空，尝试按照供应商店铺类型查询
            // 查询是否为供应商店铺
            shopMap.put("shopType", CommonType.storeType.SUPPLIER_STORE.getCode() + "");
            shopResult = merchantServiceClient.queryShopStatus(shopMap);
            // 如果第二次查询结果不为空，则表示用户不是商家，返回相应的错误信息
            if (null != shopResult.getData()) {
                return ResponseResult.fail(BizCodeEnume.IS_NOT_SHOP_LOGIN);
            }
            // 如果第二次查询结果为空，尝试查询用户是否为商家子账号
            // 是否为商家子账号
            ResponseResult seller = userServiceClient.queryShopSellerBase(userInfo.getUserId());
            // 如果查询用户是否为商家子账号的结果不成功，则返回表示用户不是商家的错误信息
            if (seller.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return ResponseResult.fail(BizCodeEnume.IS_NOT_SHOP_LOGIN);
            }
            // 如果查询用户是否为商家子账号的结果成功，则表示用户是商家子账号，返回成功信息
            return ResponseResult.success();
        }
    }


    /**
     * 调用第三方服务发送验证代码。
     * <p>
     * 本方法封装了向第三方服务发送手机验证码的逻辑。通过传递包含必要信息的参数映射表，
     * 调用第三方服务的接口，以实现验证码的发送。这在注册、登录或其他需要验证用户身份的场景中广泛应用。
     *
     * @param paraMap 包含发送验证码所需参数的映射表。参数的具体内容和格式依赖于第三方服务的要求。
     *                通常可能包括手机号码等信息。
     * @return 返回第三方服务的响应结果。这个结果可能包含操作是否成功的标识、错误代码等信息。
     */
    @Override
    public ResponseResult sendVerificationCode(Map<String, String> paraMap) {
        return thirdPartyServiceClient.threeCheckPhoneAndSend(paraMap);
    }

    /**
     * 获取验证代码。
     * 该方法用于生成一个图形验证代码，并将其存储在Redis中，以供后续验证使用。
     * 验证代码包括一个数值代码和对应的图像，图像以Base64编码的形式返回。
     *
     * @return ResponseResult 包含验证代码信息和图像的响应结果。
     */
    @Override
    public ResponseResult getVerify() {
        Map<String, Object> resp = new HashMap<>();
        try {
            // 生成图形验证代码及其Base64编码的图像
            Map<String, String> stringStringMap = ValidateCodeUtils.generateValidateCode();
            // 提取图形验证代码的数值部分
            // 4位验证码
            String result = stringStringMap.get("codeValue");

            // 获取当前时间，用于设置验证代码的失效时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String signTime = df.format(new Date());
            Date date = df.parse(signTime);
            long signLong = date.getTime();
            // 将验证代码存储在Redis中，设置5分钟的失效时间
            //5 分钟过期
            redisCache.setCacheObject("PLATFORM_CODE_" + signLong / 1000, result);
            redisCache.expire("PLATFORM_CODE_" + signLong / 1000, RedisUtil.EXRP_FIVE_MIN, TimeUnit.SECONDS);
            // 将时间戳转换为秒级时间戳，并放入响应中
            resp.put("sign", signLong / 1000);
            // 将图像的Base64编码放入响应中
            resp.put("code", stringStringMap.get("imageBase64"));
            // 返回包含验证代码和图像的响应结果
            return ResponseResult.success(resp);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果发生异常，返回一个空的成功响应
        return ResponseResult.success(CommonStatusEnum.SUCCESS.getCode(), BizCodeEnume.LOGIN_SUCCEEDED.getMsg());
    }

    /**
     * 获取验证码的接口方法。
     * 该方法用于生成验证码并将其存储到Redis中，同时返回验证码的签名时间和图片Base64编码。
     *
     * @return ResponseResult 包含验证码信息的结果对象。
     */
    @Override
    public ResponseResult getVerify2() {
        Map<String, Object> resp = new HashMap<>();
        try {
            // 生成验证码及其图片的Base64编码
            Map<String, String> stringStringMap = ValidateCodeUtils.generateValidateCode();
            // 提取验证码值
            // 4位验证码
            String result = stringStringMap.get("codeValue");

            // 格式化当前时间作为签名时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String signTime = df.format(new Date());
            // 将签名时间转换为毫秒级时间戳
            Date date = df.parse(signTime);
            long signLong = date.getTime();
            // 将验证码存储到Redis中，有效期为5分钟
            redisCache.setCacheObject("USER_CODE_" + signLong / 1000, result);
            //5 分钟过期
            redisCache.expire("USER_CODE_" + signLong / 1000, 300, TimeUnit.SECONDS);
            // 将签名时间和验证码图片Base64编码放入响应中
            resp.put("sign", signLong / 1000);
            resp.put("code", stringStringMap.get("imageBase64"));
            // 返回成功响应，包含验证码信息
            return ResponseResult.success(BizCodeEnume.VERIFICATION_CODE_SUCCESS.getMsg(), resp);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果出现异常，返回失败响应
        return ResponseResult.fail();
    }

    /**
     * 生成店铺验证码并将其存储到Redis中。
     * 验证码为4位数字，有效时长为5分钟。
     *
     * @return ResponseResult 包含生成的验证码和签名时间的响应结果。
     */
    @Override
    public ResponseResult getShopVerify() {
        Map<String, Object> resp = new HashMap<>();
        try {
            // 生成4位随机数字验证码
            // 4位验证码
            StringBuilder result = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 4; i++) {
                result.append(random.nextInt(10));
            }

            // 获取当前时间并格式化为字符串
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String signTime = df.format(new Date());
            Date date = df.parse(signTime);
            long signLong = date.getTime();

            // 将验证码以当前时间作为键存储到Redis中，有效期为5分钟
            //5 分钟过期
            redisCache.setCacheObject("USER_CODE_" + signLong / 1000, result.toString());
            redisCache.expire("USER_CODE_" + signLong / 1000, 300, TimeUnit.SECONDS);

            // 将签名时间和验证码放入响应体中
            resp.put("sign", signLong / 1000);
            resp.put("code", result.toString());

            return ResponseResult.success(resp);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.fail();
    }

    /**
     * 获取供应商验证代码
     * 该方法用于生成一个4位数字的随机验证码，并将其存储到Redis中，以供后续验证使用。
     * 验证码的有效期为5分钟。
     *
     * @return ResponseResult 包含生成的验证码和时间戳的响应结果。
     */
    @Override
    public ResponseResult getSupplierVerify() {
        Map<String, Object> resp = new HashMap<>();
        try {
            // 生成一个4位随机验证码
            // 4位验证码
            StringBuilder result = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 4; i++) {
                result.append(random.nextInt(10));
            }

            // 获取当前时间并格式化为字符串
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String signTime = df.format(new Date());
            Date date = df.parse(signTime);
            long signLong = date.getTime();

            // 将验证码以当前时间戳为键存储到Redis中，有效期为5分钟
            //5 分钟过期
            redisCache.setCacheObject("SUPPLIER_CODE_" + signLong / 1000, result.toString());
            redisCache.expire("SUPPLIER_CODE_" + signLong / 1000, 300, TimeUnit.SECONDS);

            // 将时间戳和验证码放入响应数据中
            resp.put("sign", signLong / 1000);
            resp.put("code", result.toString());

            // 返回成功的响应结果，包含验证码和时间戳
            return ResponseResult.success(resp);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果发生异常，返回失败的响应结果
        return ResponseResult.fail();
    }

    /**
     * 根据地图验证手机验证码。
     * <p>
     * 本方法旨在通过映射参数验证用户的手机验证码。这通常用于用户注册或登录过程中，
     * 以确认用户提供的验证码是否正确，从而确保用户身份的安全验证。
     *
     * @param map 映射参数，包含验证所需的全部信息，如验证码、手机号等。
     * @return ResponseResult 返回验证结果的对象，其中可能包含成功与否的标志以及额外的信息。
     */
    @Override
    public ResponseResult verifiPhoneCode2(Map<String, Object> map) {
        // 调用第三方服务客户端来执行手机验证码的验证
        ResponseResult result = thirdPartyServiceClient.verifiPhoneCode2(map);
        // 返回验证结果
        return result;
    }

    /**
     * 初始化 小程序授权参数
     */
    public void initWXPayConfig(String shopId) {
        // 检查shopId是否为空，为空则初始化小程序支付配置，不为空则初始化H5支付配置
        //获取小程序配置
        if (shopId == null || shopId.isEmpty()) {
            // 初始化小程序支付配置的参数
            Map<String, Object> param = new HashMap<>();
            param.put("paymentMode", "02");
            // 调用支付服务查询支付配置
            ResponseResult resultResp = payMentServiceClient.selectPayConfig(param);
            // 将查询结果转换为Map，提取小程序的appID和appSecret
            Map<String, String> outMap = (Map<String, String>) resultResp.getData();
            WXPayConfig.Applet_APP_ID = outMap.get("appID");
            WXPayConfig.Applet_APP_SECRET = outMap.get("appSecret");

        } else {
            // 初始化H5支付配置的参数
            Map<String, Object> param = new HashMap<>();
            param.put("shopId", shopId);
            // 调用支付服务查询特定商店的H5支付配置
            ResponseResult resultResp = payMentServiceClient.queryShopAppletConfigFeign(param);
            // 将查询结果转换为Map，提取H5支付的appID和appSecret
            Map<String, String> outMap = (Map<String, String>) resultResp.getData();
            WXPayConfig.Applet_APP_ID = outMap.get("AUTHAPPID");
            WXPayConfig.Applet_APP_SECRET = outMap.get("AUTHSECRET");
        }
    }

    /**
     * 处理小程序登录请求。
     * 通过接收包含openid的参数映射，查询并返回用户登录相关信息。
     * 如果用户已绑定账号，生成并返回登录token及相关用户信息。
     * 如果用户未绑定账号，返回提示信息表示需要绑定账号。
     *
     * @param paraMap  包含登录参数的映射，其中openid是关键参数。
     * @param request  HTTP请求对象，用于获取请求相关信息。
     * @param response HTTP响应对象，用于设置登录cookie和返回登录结果。
     * @return 包含登录结果的映射，包括token、userId等信息。
     */
    @Override
    public Map<String, Object> uniappLogin(Map<String, String> paraMap, HttpServletRequest request, HttpServletResponse response) {
        // 初始化登录结果映射
        Map<String, Object> result = new HashMap<String, Object>();
        // 从参数映射中获取openid
        String openid = paraMap.get("openid");
        // 构造绑定的openid字符串
        String BindOpenid = "weixin_" + openid;

        // 根据构造的openid查询已绑定的用户ID
        String bindId = userServiceClient.selectUserIdByOpenid(BindOpenid);
        // 如果没有找到绑定的用户ID
        if (null == bindId || bindId.isEmpty()) {
            // 设置登录失败的结果，提示需要绑定账号
            result.put("code", 2);
            result.put("desc", null);
        } else {
            // 创建用户信息DTO对象
            UserInfoDTO dto = new UserInfoDTO();
            // 根据用户ID查询详细的用户信息
            UserInfoVO userInfo = userServiceClient.getUserInfoByUserId(bindId);
            // 生成JWT登录令牌
            // 默认验证帐号密码正确，创建code
            String token = JwtUtil.createJWT(userInfo.getUserId());
            // 设置用户信息到DTO
            dto.setUser(userInfo);
            // 将用户信息和token存储到Redis中，用于后续验证和快速查询
            // 统一使用HOU_USER_TOKEN_USER
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto));
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USER + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId());
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USERID + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);

            // 初始化返回给客户端的映射
            Map<String, Object> returnMap = new HashMap<String, Object>();
            // 设置登录token和其他用户相关信息
            returnMap.put("token", token);
            returnMap.put("userId", userInfo.getUserId());
            returnMap.put("userMobile", userInfo.getUserMobile());
            // 用户来源 1.pc;2.h5;3.小程序; 4: app
            returnMap.put("platForm", "4");

            // 更新用户活跃度信息
            // 更新用户活跃度
            userServiceClient.insertUserDayLive(returnMap);
            // 设置登录成功的结果，包括token、userId等信息
            result.put("code", 1);
            result.put("token", token);
            result.put("userId", userInfo.getUserId());
        }
        // 返回登录处理结果
        return result;
    }

    /**
     * 小程序用户注册接口。
     * 通过验证手机验证码，并使用小程序提供的用户信息进行注册。
     *
     * @param paraMap 包含注册所需信息的映射，如手机验证码、用户昵称、头像地址等。
     * @return 注册结果，成功或失败及原因。
     * @throws IOException          如果网络通信发生错误。
     * @throws TransactionException 如果交易过程中发生异常。
     */
    @Override
    public ResponseResult uniappRegister(Map<String, String> paraMap) throws IOException, TransactionException {
        // 验证手机验证码是否正确
        String phoneCode = paraMap.get("phoneCode");
        String sessionPhoneCode = (String) redisCache.getCacheObject(paraMap.get("userMobile"));
        if (!phoneCode.equals(sessionPhoneCode)) {
            // 验证码不匹配，返回失败结果
            return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE.getCode(), BizCodeEnume.INCORRECT_VERIFICATION_CODE.getMsg());
        }

        // 准备注册信息，包括openid、昵称、头像、手机号等，并构造绑定ID和来源平台
        Map<String, String> map = new HashMap<>();
        map.put("openid", paraMap.get("openid"));
        map.put("nickName", paraMap.get("nickname"));
        map.put("avatarUrl", paraMap.get("headimgurl"));
        map.put("userMobile", paraMap.get("userMobile"));
        map.put("bindId", "weixin_" + paraMap.get("openid"));
        // 绑定类型：微信
        map.put("bindType", "2");
        // 用户来源：APP
        map.put("platForm", "4");
        map.put("shareUserId", paraMap.get("shareUserId"));

        // 调用第三方注册接口进行注册
        ResponseResult result2 = userServiceClient.thirDregister(map);
        return result2;
    }


    /**
     * 将小程序的code转换为sessionKey和openid
     *
     * @param paraMap  包含小程序code和其他必要信息的参数映射
     * @param request  HTTP请求对象，用于获取请求相关数据
     * @param response HTTP响应对象，用于设置响应数据
     * @return 包含sessionKey、openid和其他相关信息的映射
     * @throws UnsupportedEncodingException 当编码转换不支持时抛出此异常
     */
    @Override
    public Map<String, Object> code2Session(Map<String, String> paraMap, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        // 打印参数映射用于调试
        System.out.println(paraMap);
        // 从参数映射中获取店铺ID
        String shopId = paraMap.get("shopId");
        // 初始化微信支付配置
        Map<String, Object> result = new HashMap<String, Object>();
        // 初始化微信支付配置
        initWXPayConfig(shopId);
        // 构造获取sessionKey和openid的URL
        String strUrl = String.format(WXPayConfig.getCODE_SESSION(), paraMap.get("code"));

        // 发起HTTP请求获取响应字符串
        String resultStr = restTemplate.getForObject(strUrl, String.class);
        // 对响应字符串进行编码转换
        resultStr = new String(resultStr.getBytes("ISO-8859-1"), "UTF-8");
        // 解析响应字符串为JSON对象
        JSONObject result1 = JSONObject.parseObject(resultStr);
        System.out.println(result1);
        // 提取openid和session_key并存入结果映射中
        String openid = result1.getString("openid");
        result.put("openid", result1.getString("openid"));
        String unionid = result1.getString("unionid");
        result.put("unionid", unionid);

        // 将openid和session_key存入Redis缓存
        redisCache.setCacheObject(HouConfig.HOU_USER_OPENID + openid, result1.getString("session_key"));
        redisCache.expire(HouConfig.HOU_USER_OPENID + openid, 240, TimeUnit.SECONDS);
        redisCache.setCacheObject(HouConfig.HOU_USER_UNIONID + openid, result1.getString("openid"));
        redisCache.expire(HouConfig.HOU_USER_UNIONID + openid, 240, TimeUnit.SECONDS);
        redisCache.setCacheObject("code_" + paraMap.get("code"), resultStr);
        redisCache.expire("code_" + paraMap.get("code"), 300, TimeUnit.SECONDS);

        // 如果openid为空或为空字符串，则返回错误信息
        if (null == openid || "".equals(openid)) {
            result.put("code", 1);
            result.put("desc", "");
            return result;
        }

        // 构造绑定的openid字符串
        String BindOpenid = "weixinsmall_" + result1.getString("openid");

        // 根据openid查询用户ID
        String bindId = userServiceClient.selectUserIdByOpenid(BindOpenid);
        // 如果未绑定，则返回错误信息
        //bindId 为空让用户走授权注册
        if (null == bindId || "".equals(bindId)) {
            result.put("code", 1);
            result.put("desc", result1.toString());
        } else {
            // 根据用户ID查询用户信息
            UserInfoVO userInfo = userServiceClient.getUserInfoByUserId(bindId);
            // 如果店铺ID不为空且用户信息为空，则返回错误信息
            if (shopId != null && shopId != "") {
                if (userInfo == null) {
                    result.put("code", 2);
                    result.put("desc", "该账号已被商家禁用，请联系商家");
                    return result;
                }
            }
            // 根据配置键查询用户头像URL
            ResponseResult avatorMap = baseServiceClient.selectByConfigKey("photo_user_logo");
            Map<String, Object> configMap = (Map<String, Object>) avatorMap.getData();
            String userAvator = configMap.get("configValue").toString();
            // 如果用户头像为空，则使用默认头像
            if (null == userInfo.getUserAvatar() || "".equals(userInfo.getUserAvatar())) {
                userInfo.setUserAvatar(userAvator);
            }
            // 创建用户DTO，设置用户信息和JWT令牌
            UserInfoDTO dto = new UserInfoDTO();
            String token = JwtUtil.createJWT(userInfo.getUserId());
            dto.setUser(userInfo);
            // 统一使用HOU_USER_TOKEN_USER
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto));
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USER + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId());
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USERID + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            // 构造返回的用户信息映射
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("token", token);
            returnMap.put("userId", userInfo.getUserId());
            returnMap.put("userMobile", userInfo.getUserMobile());
            //  用户来源 1.pc;2.h5;3.小程序; 4: app
            returnMap.put("platForm", "3");

            // 更新用户活跃度
            userServiceClient.insertUserDayLive(returnMap);
            // 判断当前用户当天是否已经登录过，登录过不添加积分
            memberPointsService(userInfo);


            // 更新用户自动登录状态
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("openid", result1.getString("openid"));
            //1 是允许自动登录 2 是不允许自动登录
            paramMap.put("start", 1);
            paramMap.put("unionid", result1.getString("unionid"));
            userServiceClient.updateAotuLogin(paramMap);

            // 返回成功信息，包括token等
            result.put("code", 4);
            result.put("desc", result1.toString());
            result.put("token", token);
        }
        return result;
    }


    /**
     * 微信登录代码换取用户信息。
     * 通过微信提供的openID和其他加密数据，解密获取用户手机号，并完成第三方注册。
     *
     * @param paraMap 包含微信登录相关参数的映射，如openID、加密的数据和加密数据的向量。
     * @return 返回第三方注册的结果。
     * @throws IOException          如果解密过程发生IO错误。
     * @throws TransactionException 如果数据库事务处理失败。
     */
    @Override
    public ResponseResult weixinCodelogin(Map<String, String> paraMap) throws IOException, TransactionException {
        // 记录参数映射的详细信息到日志。
        logger.info("paraMap{}", JSON.toJSONString(paraMap));

        // 从参数映射中提取openid、加密的数据和加密数据的向量。
        String openid = paraMap.get("openid");
        String encryptedDate = paraMap.get("encryptedDate");
        String iv = paraMap.get("iv");
        String code = paraMap.get("code");

        // 从Redis中获取与openid关联的sessionKey。
        String sessionKey = (String) redisCache.getCacheObject(HouConfig.HOU_USER_OPENID + openid);

        // 使用解密工具，基于sessionKey解密加密的数据，得到用户信息。
        String result = WechatDecryptDataUtil.decryptData(encryptedDate, sessionKey, iv);
        JSONObject result1 = JSONObject.parseObject(result);
        String userMobile = result1.getString("phoneNumber");
        String unionid = result1.getString("unionid");
        if (StringUtils.isEmpty(unionid)) {
            String codeStr = (String) redisCache.getCacheObject("code_" + code);
            if (codeStr != null) {
                JSONObject resultObject = JSONObject.parseObject(codeStr);
                if (null != resultObject) {
                    unionid = resultObject.getString("unionid");
                }
            }
        }

        // 更新参数映射，添加用户手机号、绑定类型、平台来源和绑定ID。
        paraMap.put("userMobile", userMobile);
        // 1 :mobile 2:weixin
        paraMap.put("bindType", "2");
        //  用户来源 1.pc;2.h5;3.小程序; 4: app
        paraMap.put("platForm", "3");
        paraMap.put("bindId", "weixinsmall_" + openid);
        paraMap.put("unionid", unionid);

        // 调用userServiceClient的thirDregister方法，完成第三方注册，并返回结果。
        return userServiceClient.thirDregister(paraMap);
    }


    /**
     * 根据小程序code换取用户的openid
     *
     * @param paraMap 包含小程序code和店铺ID的参数映射
     * @return 包含openid的成功响应结果
     * @throws IOException 如果网络通信发生错误
     */
    @Override
    public ResponseResult code2SessionopenId(Map<String, String> paraMap) throws IOException {
        // 打印参数映射，用于调试和日志记录
        System.out.println(paraMap);

        // 从参数映射中提取店铺ID
        String shopId = paraMap.get("shopId");
        // 根据店铺ID初始化微信支付配置
        // 初始化 小程序授权参数
        initWXPayConfig(shopId);

        // 构造获取openid的URL
        String strUrl = String.format(WXPayConfig.getCODE_SESSION(), paraMap.get("code"));

        // 发起HTTP请求，获取微信返回的openid信息
        String resultStr = restTemplate.getForObject(strUrl, String.class);
        // 处理微信返回的字符串编码问题
        resultStr = new String(resultStr.getBytes("ISO-8859-1"), "UTF-8");

        // 解析返回的JSON字符串，获取openid
        JSONObject result1 = JSONObject.parseObject(resultStr);
        String openid = result1.getString("openid");

        // 构建并返回包含openid的成功响应结果
        return ResponseResult.success(openid);
    }

    /**
     * 根据小程序code查询用户的openid，并判断是否允许自动登录。
     *
     * @param paraMap 包含shopId和code的参数映射，shopId为商家ID，code为小程序授权code。
     * @return 返回一个Map，包含code、autoLogin和desc字段。code为业务码，1为成功，其他为失败；
     * autoLogin表示是否允许自动登录，desc为返回的详细信息。
     * @throws UnsupportedEncodingException 如果编码转换失败。
     */
    @Override
    public Map<String, Object> queryAutoLogin(Map<String, String> paraMap) throws UnsupportedEncodingException {
        // 从参数映射中获取shopId
        String shopId = paraMap.get("shopId");
        Map<String, Object> result = new HashMap<>();
        // 初始化 小程序授权参数
        initWXPayConfig(shopId);
        // 根据code和shopId构造获取openid的URL
        String strUrl = String.format(WXPayConfig.getCODE_SESSION(), paraMap.get("code"));
        // 发起HTTP请求，获取微信返回的openid信息
        String resultStr = restTemplate.getForObject(strUrl, String.class);
        // 解决微信返回信息的编码问题
        resultStr = new String(resultStr.getBytes("ISO-8859-1"), "UTF-8");
        // 解析JSON字符串，获取openid
        JSONObject result1 = JSONObject.parseObject(resultStr);
        String openid = result1.getString("openid");
        // 如果openid为空或为空字符串，表示获取openid失败
        if (null == openid || "".equals(openid)) {
            // 设置业务码为1，表示失败，并返回详细的错误信息
            result.put("code", 1);
            result.put("desc", result1.toString());
            return result;
        }
        // 构造绑定的openid字符串，前缀为"weixinsmall_"，用于后续查询用户ID
        String BindOpenid = "weixinsmall_" + result1.getString("openid");
        // 根据构造的openid查询用户的ID
        // 查询小程序用户的openid 如果不存在就是当前用户是新用户 允许自动登录
        String bindId = userServiceClient.selectUserIdByOpenid(BindOpenid);
        // 如果用户ID为空或为空字符串，表示未找到绑定的用户，自动登录失败
        if (null == bindId || bindId.isEmpty()) {
            // 设置业务码为1，表示失败，并返回详细的错误信息
            result.put("code", 1);
            result.put("desc", result1.toString());
            return result;
        } else {
            // 查询该用户是否允许自动登录
            // 如果不是新用户查询他的是否允许自动登录
            Integer integer = userServiceClient.queryAutoLogin(openid);
            // 如果返回的自动登录标志为null或1，表示允许自动登录
            // 1 允许自动登录  2  不允许自动登录
            if (null == integer && 1 == integer) {
                integer = 1;
            }
            // 设置业务码为1，表示成功，并返回自动登录标志和详细信息
            result.put("code", 1);
            result.put("autoLogin", integer);
            result.put("desc", result1.toString());
            return result;
        }
    }


    /**
     * 处理支付授权页面的请求。
     * 该方法主要用于通过小程序的授权码换取用户的openid和session_key，并将这些信息保存到Redis中，
     * 以供后续支付和其他业务操作使用。
     *
     * @param paraMap 包含授权相关信息的参数映射，包括shopId（店铺ID）、token（用户授权token）和code（小程序授权码）。
     * @return 返回一个映射，包含操作结果代码和描述，以及openid和session_key等信息。
     * @throws UnsupportedEncodingException 如果处理结果字符串的编码转换失败。
     */
    @Override
    public Map<String, Object> paymentAuthorization(Map<String, String> paraMap) throws UnsupportedEncodingException {
        // 从参数映射中提取店铺ID和用户授权token
        String shopId = paraMap.get("shopId");
        String token = paraMap.get("token");
        // 初始化微信支付配置
        // 初始化 小程序授权参数
        initWXPayConfig(shopId);
        // 使用授权token获取用户信息
        UserInfoVO userInfoVO = ParseTokenUtils.getPlatformUser(token);
        // 如果用户信息为空，则返回错误结果
        if (null == userInfoVO) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", "2");
            result.put("desc", "token无效，请重新获取");
            return result;
        }
        // 根据授权码和微信支付配置获取openid和session_key
        String strUrl = String.format(WXPayConfig.getCODE_SESSION(), paraMap.get("code"));
        String resultStr = restTemplate.getForObject(strUrl, String.class);
        // 处理结果字符串的编码转换问题
        resultStr = new String(resultStr.getBytes("ISO-8859-1"), "UTF-8");
        JSONObject result1 = JSONObject.parseObject(resultStr);
        String openid = result1.getString("openid");
        // 将openid和session_key保存到结果映射中
        Map<String, Object> result = new HashMap<>();
        result.put("openid", openid);
        result.put("session_key", result1.getString("session_key"));
        // 更新用户信息中的openid，并将用户信息和用户ID保存到Redis中
        userInfoVO.setBindOpenid(openid);
        // 统一使用HOU_USER_TOKEN_USER
        redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(userInfoVO));
        redisCache.expire(HouConfig.HOU_USER_TOKEN_USER + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
                
        redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USERID + token, userInfoVO.getUserId());
        redisCache.expire(HouConfig.HOU_USER_TOKEN_USERID + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
                
        // 设置操作成功的结果代码和描述
        result.put("code", "1");
        result.put("desc", "");
        return result;
    }


    /**
     * 通过微信令牌获取用户信息并处理自动登录状态。
     *
     * @param paraMap 包含令牌的参数映射。
     * @return 返回处理结果。
     */
    @Override
    public ResponseResult weixinOut(Map<String, String> paraMap) {
        // 尝试根据令牌获取用户信息。
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));

        // 如果用户信息为空，则表示令牌无效，返回相应的错误响应。
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
        }

        // 如果用户信息存在且已绑定微信OpenID，则处理自动登录状态。
        if (null != userInfo.getBindOpenid()) {
            // 准备参数，设置用户自动登录状态为不允许。
            // 手动退出 当前登录微信小程序的用户 下次不会自动登录小程序
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("openid", userInfo.getBindOpenid());
            //1 是允许自动登录 2 是不允许自动登录
            paramMap.put("start", 2);

            // 调用用户服务，更新用户的自动登录状态。
            userServiceClient.updateAotuLogin(paramMap);

            // 从Redis中移除该用户的令牌，以终止自动登录。
            // 统一使用HOU_USER_TOKEN_USER
            redisCache.deleteObject(HouConfig.HOU_USER_TOKEN_USER + paraMap.get("token"));
        }

        // 如果一切处理正常，返回成功响应。
        return ResponseResult.success();
    }

    /**
     * 用户登出接口。
     * 该方法用于处理用户的登出请求，主要逻辑包括验证用户token的有效性、删除与用户相关的缓存信息及数据库信息。
     *
     * @param paraMap 包含用户token等参数的映射表。
     * @return 返回处理结果，成功或失败。
     * @throws TransactionException 如果交易异常，则抛出此异常。
     */
    @Override
    public ResponseResult userLogoff(Map<String, String> paraMap) throws TransactionException {
        // 从参数映射中获取用户token
        String token = paraMap.get("token");
        // 从Redis中根据token获取用户信息的JSON字符串
        // 统一使用HOU_USER_TOKEN_USER
        String json = (String) redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + token);
        // 如果用户信息不存在，则返回失败结果
        if (null == json || json.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
        try {
            // 解析token获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token);
            // 删除用户信息
            userServiceClient.delUserInfo(userInfo.getUserId());
            // 删除微客信息
            payMentServiceClient.delFxInfo(userInfo.getUserId());
            // 如果用户有店铺信息，则删除店铺信息
            //删除店铺信息
            if (null != userInfo.getShopId()) {
                Map<String, String> map = new HashMap<>();
                map.put("shopId", userInfo.getShopId());
                map.put("userId", userInfo.getUserId());
                merchantServiceClient.delShopInfo(map);
            }
            // 从Redis中移除用户token
            // 统一使用HOU_USER_TOKEN_USER
            redisCache.deleteObject(HouConfig.HOU_USER_TOKEN_USER + token);
            // 返回成功结果，注销成功
            return ResponseResult.success(1, "注销成功");
        } catch (Exception e) {
            e.printStackTrace();
            // 重新加载事务ID并回滚事务
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
        }
        // 如果出现异常，则返回失败结果，未知的用户信息
        return ResponseResult.fail(2, "未知的用户信息");
    }

    /**
     * 将小程序的code转换为sessionKey和openid，并完成用户登录或注册流程。
     *
     * @param paraMap  包含小程序code和shopId的参数映射。
     * @param request  HTTP请求对象，用于获取请求相关数据。
     * @param response HTTP响应对象，用于设置响应数据。
     * @return 包含登录结果和用户信息的映射。
     * @throws UnsupportedEncodingException 当字符编码不支持时抛出。
     */
    @Override
    public Map<String, Object> code2ShopAppSession(Map<String, String> paraMap, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        // 从参数映射中获取shopId和code
        String shopId = paraMap.get("shopId");
        Map<String, Object> result = new HashMap<>();

        // 根据code和预定义格式构造获取sessionKey和openid的URL
        // 初始化 小程序授权参数
        String strUrl = String.format(getCodeSession(), paraMap.get("code"));

        // 发起HTTP请求获取响应内容，并转为UTF-8编码
        String resultStr = restTemplate.getForObject(strUrl, String.class);
        resultStr = new String(resultStr.getBytes("ISO-8859-1"), "UTF-8");
        JSONObject result1 = JSONObject.parseObject(resultStr);
        String unionid = result1.getString("unionid");

        // 解析出openid和session_key，并存入结果映射中
        String openid = result1.getString("openid");
        result.put("openid", result1.getString("openid"));
        result.put("unionid", unionid);

        // 将openid和session_key存入Redis缓存，用于后续验证和会话管理
        redisCache.setCacheObject(HouConfig.HOU_USER_OPENID + openid, result1.getString("session_key"));
        redisCache.expire(HouConfig.HOU_USER_OPENID + openid, 240, TimeUnit.SECONDS);
        redisCache.setCacheObject(HouConfig.HOU_USER_UNIONID + openid, result1.getString("openid"));
        redisCache.expire(HouConfig.HOU_USER_UNIONID + openid, 240, TimeUnit.SECONDS);
        redisCache.setCacheObject("code_" + paraMap.get("code"), resultStr);
        redisCache.expire("code_" + paraMap.get("code"), 300, TimeUnit.SECONDS);

        // 如果openid为空或无效，则返回错误信息
        if (null == openid || openid.isEmpty()) {
            result.put("code", 1);
            result.put("desc", "");
            return result;
        }

        // 构造绑定的openid字符串，并尝试根据openid查询已绑定的用户ID
        String BindOpenid = "weixinsmall_" + result1.getString("openid");
        String bindId = userServiceClient.selectUserIdByOpenid(BindOpenid);

        // 如果未找到绑定的用户ID，则表示用户需要授权注册，返回相应的错误信息
        //bindId 为空让用户走授权注册
        if (null == bindId || bindId.isEmpty()) {
            result.put("code", 1);
            result.put("desc", result1.toString());
            // result.put("session_key", result1.getString("session_key"));
            return result;
        } else {
            // 根据用户ID查询用户信息
            UserInfoVO userInfo = userServiceClient.getUserInfoByUserId(bindId);
            // 如果shopId不为空且用户信息为空，则表示该用户已被禁用，返回错误信息
            if (shopId != null && !shopId.isEmpty()) {
                if (userInfo == null) {
                    result.put("code", 2);
                    result.put("desc", "该账号已被商家禁用，请联系商家");
                    return result;
                }
            }

            // 根据配置键查询用户头像URL，并更新用户头像信息
            ResponseResult avatorMap = baseServiceClient.selectByConfigKey("photo_user_logo");
            Map<String, Object> configMap = (Map<String, Object>) avatorMap.getData();
            String userAvator = configMap.get("configValue").toString();
            if (null == userInfo.getUserAvatar() || userInfo.getUserAvatar().isEmpty()) {
                userInfo.setUserAvatar(userAvator);
            }

            // 创建用户DTO，生成JWT，并将用户信息和JWT存入Redis缓存
            UserInfoDTO dto = new UserInfoDTO();
            String token = JwtUtil.createJWT(userInfo.getUserId());
            dto.setUser(userInfo);
            // 统一使用HOU_USER_TOKEN_USER
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto));
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USER + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId());
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USERID + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);

            // 构造返回的用户信息映射，包含token、用户ID、手机号和用户来源
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("token", token);
            returnMap.put("userId", userInfo.getUserId());
            returnMap.put("userMobile", userInfo.getUserMobile());
            //  用户来源 1.pc;2.h5;3.小程序; 4: app
            returnMap.put("platForm", "3");

            // 更新用户活跃度和添加用户积分
            userServiceClient.insertUserDayLive(returnMap);
            memberPointsService(userInfo);


            // 更新用户自动登录状态
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("openid", result1.getString("openid"));
            //1 是允许自动登录 2 是不允许自动登录
            paramMap.put("start", 1);
            paramMap.put("unionid", result1.getString("unionid"));
            userServiceClient.updateAotuLogin(paramMap);

            // 返回成功信息，包含token和用户信息
            result.put("code", 4);
            result.put("desc", result1.toString());
            result.put("token", token);
            return result;
        }
    }


    /**
     * 微信小程序登录接口。
     * 通过微信小程序提供的用户信息，完成用户登录或注册流程。
     *
     * @param paraMap 包含微信小程序用户登录相关参数的映射。
     * @return 返回登录或注册结果。
     * @throws IOException          如果与微信解密数据过程中发生IO错误。
     * @throws TransactionException 如果数据库事务处理过程中发生异常。
     */
    @Override
    public ResponseResult weixinShopAppCodelogin(Map<String, String> paraMap) throws IOException, TransactionException {
        // 日志记录输入参数
        logger.info("paraMap{}", JSON.toJSONString(paraMap));

        // 从参数映射中提取微信用户标识、加密的数据和加密算法的初始向量
        String openid = paraMap.get("openid");
        String encryptedDate = paraMap.get("encryptedDate");
        String iv = paraMap.get("iv");
        String code = paraMap.get("code");

        // 从Redis中获取用户的sessionKey
        String sessionKey = (String) redisCache.getCacheObject(HouConfig.HOU_USER_OPENID + openid);

        // 解密微信小程序提供的用户数据
        String result = WechatDecryptDataUtil.decryptData(encryptedDate, sessionKey, iv);
        JSONObject result1 = JSONObject.parseObject(result);
        String userMobile = result1.getString("phoneNumber");
        String unionid = result1.getString("unionid");
        if (StringUtils.isEmpty(unionid)) {
            String codeStr = (String) redisCache.getCacheObject("code_" + code);
            if (codeStr != null) {
                JSONObject resultObject = JSONObject.parseObject(codeStr);
                if (null != resultObject) {
                    unionid = resultObject.getString("unionid");
                }
            }
        }
        // 将解密得到的用户手机号添加到参数映射中
        paraMap.put("userMobile", userMobile);
        // 设置绑定类型为微信
        // 绑定类型 1 :mobile 2:weixin
        paraMap.put("bindType", "2");
        // 设置用户来源为小程序
        //  用户来源 1.pc;2.h5;3.小程序; 4: app
        paraMap.put("platForm", "3");
        // 构造绑定ID，格式为"weixinsmall_"加上微信用户标识
        paraMap.put("bindId", "weixinsmall_" + openid);
        paraMap.put("unionid", unionid);

        // 调用用户服务客户端，完成第三方注册
        return userServiceClient.thirDregister(paraMap);
    }


    /**
     * 根据小程序的code换取用户的openid，并将openid绑定到用户信息上。
     * 该方法主要用于处理小程序端的登录流程，通过小程序的code来获取用户的openid，
     * 并将openid与用户信息关联，以便后续的用户身份识别。
     *
     * @param paraMap 包含登录流程中所需参数的映射，包括token和code。
     *                token用于验证用户身份，code是小程序端通过OAuth流程获取的授权码。
     * @return 返回处理结果，如果token无效，则返回失败响应，否则返回包含openid的成功响应。
     * @throws IOException 如果在处理过程中发生IO异常。
     */
    @Override
    public ResponseResult code2ShopAppSessionopenId(Map<String, String> paraMap) throws IOException {
        // 根据token获取用户信息，验证token的有效性
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(paraMap.get("token"));

        // 如果用户信息为空，表示token无效，返回相应的错误响应
        if (null == userInfo) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID.getCode(), BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }

        // 构造请求URL，用于向小程序的API换取openid
        // 初始化 小程序授权参数
        String strUrl = String.format(getCodeSession(), paraMap.get("code"));

        // 发起HTTP请求，获取API的响应
        String resultStr = restTemplate.getForObject(strUrl, String.class);
        // 处理API响应的编码问题，确保响应内容能正确解析
        resultStr = new String(resultStr.getBytes("ISO-8859-1"), "UTF-8");
        JSONObject result1 = JSONObject.parseObject(resultStr);
        // 从API的响应中提取openid
        String openid = result1.getString("openid");

        // 将openid绑定到用户信息上，并更新到Redis中
        userInfo.setBindOpenid(openid);
        redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + paraMap.get("token"), JSON.toJSONString(userInfo));
        redisCache.expire(HouConfig.HOU_USER_TOKEN_USER + paraMap.get("token"), HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);

        // 返回包含openid的成功响应
        return ResponseResult.success(openid);
    }

    /**
     * 根据小程序的code查询店铺的自动登录信息。
     *
     * @param paraMap 包含shopId和code的参数映射。
     * @return 返回一个Map，包含自动登录的相关信息。
     * @throws UnsupportedEncodingException 如果编码不支持时抛出此异常。
     */
    @Override
    public Map<String, Object> queryShopAppAutoLogin(Map<String, String> paraMap) throws UnsupportedEncodingException {
        // 初始化结果映射
        Map<String, Object> result = new HashMap<>();
        // 根据小程序的code组装查询URL
        // 初始化 小程序授权参数
        String strUrl = String.format(getCodeSession(), paraMap.get("code"));

        // 通过REST客户端发送GET请求，获取授权信息
        String resultStr = restTemplate.getForObject(strUrl, String.class);
        // 解决字符编码问题
        resultStr = new String(resultStr.getBytes("ISO-8859-1"), "UTF-8");
        // 解析授权信息
        JSONObject result1 = JSONObject.parseObject(resultStr);
        // 从授权信息中提取openid
        String openid = result1.getString("openid");
        // 如果openid为空或为空字符串，则返回错误信息
        if (null == openid || openid.isEmpty()) {
            result.put("code", 1);
            result.put("desc", result1.toString());
            return result;
        }
        // 构造绑定的openid字符串
        String BindOpenid = "weixinsmall_" + result1.getString("openid");
        // 查询绑定的用户ID
        // 查询小程序用户的openid 如果不存在就是当前用户是新用户 允许自动登录
        String bindId = userServiceClient.selectUserIdByOpenid(BindOpenid);
        // 如果用户ID为空或为空字符串，则返回错误信息
        if (null == bindId || bindId.isEmpty()) {
            result.put("code", 1);
            result.put("desc", result1.toString());
            return result;

        } else {
            // 查询用户是否允许自动登录
            // 如果不是新用户查询他的是否允许自动登录
            Integer integer = userServiceClient.queryAutoLogin(openid);
            // 如果查询结果为null，则默认为允许自动登录
            // 1 允许自动登录  2  不允许自动登录
            if (null == integer && 1 == integer) {
                integer = 1;
            }
            // 构建结果映射，包含自动登录标志和授权信息
            result.put("code", 1);
            result.put("autoLogin", integer);
            result.put("desc", result1.toString());
            return result;
        }
    }


    /**
     * 实现骑手登录功能。
     * 本方法通过调用riderServiceClient的queryRiderInfo方法，传入骑手信息以查询并返回骑手的详细信息。
     * 主要用于在客户端进行骑手身份验证和信息获取。
     *
     * @param user 包含骑手登录信息的VO对象，如用户名、密码等。
     * @return 返回查询到的骑手信息，包括骑手ID、姓名、联系方式等。
     */
    @Override
    public ResponseResult riderLogin(UserInfoVO user) {
        // 骑手配送为可选商业插件：未安装时降级为明确失败应答而非抛异常（§1.4.4 / 闭坑清单 P0）
        return OptionalPluginCaller.call("delivery", "riderLogin",
                () -> riderServiceClient.queryRiderInfo(user));
    }

    /**
     * 骑手登出接口。
     * 该方法用于处理骑手登出操作，主要逻辑是验证token是否存在并删除相应的Redis缓存数据。
     *
     * @param token 骑手的登录令牌。用于唯一标识骑手身份。
     * @return 返回登出操作的结果。如果token不存在，返回提示信息；否则，返回成功登出的信息。
     */
    @Override
    public ResponseResult riderLogout(String token) {
        // 检查token在Redis中是否存在，以确定骑手是否已登录
        //判断token是否过期
        // 统一使用HOU_USER_TOKEN_USER
        boolean isExist = redisCache.hasKey(HouConfig.HOU_USER_TOKEN_USER + token);
        if (!isExist) {
            // 如果token不存在，记录错误日志并返回提示信息
            logger.error("暂无登录数据");
            return ResponseResult.success("暂无登录数据");
        }
        // 如果token存在，删除Redis中的相关数据，实现登出操作
        //清除redis数据
        redisCache.deleteObject(HouConfig.HOU_USER_TOKEN_USER + token);
        return ResponseResult.success();
    }


    /**
     * 实现了WebPOS终端的登录功能。
     * 该方法通过传递用户登录信息，调用webPosServiceClient的webPosLogin方法，实现WebPOS终端的登录操作。
     * WebPOS登录是会话开始的前提，用于验证用户身份和权限。
     *
     * @param user ReceptionLoginDTO类型的参数，包含了用户的登录信息。
     *             用户名、密码等关键信息需要通过这个参数传递给登录服务。
     * @return ResponseResult 该方法返回一个ResponseResult对象，其中包含了操作的结果信息。
     * 返回的结果可以用于判断登录操作是否成功，以及进一步处理可能的错误信息。
     */
    @Override
    public ResponseResult webPosLogin(ReceptionLoginDTO user) {
        // webpos 收银台为可选商业插件：未安装时降级为明确失败应答而非抛异常（§1.4.4 / 闭坑清单 P0）
        return OptionalPluginCaller.call("webpos", "webPosLogin",
                () -> webPosServiceClient.webPosLogin(user));
    }

    /**
     * 实现了WebPOS登出的功能。
     * 该方法通过调用webPosServiceClient的webPosLogout方法，实现WebPOS终端的登出操作。
     * 登出操作对于确保会话安全和数据一致性至关重要。
     *
     * @return ResponseResult 该方法返回一个ResponseResult对象，其中包含了操作的结果信息。
     * 返回的结果可以用于判断登出操作是否成功，以及进一步处理可能的错误信息。
     */
    @Override
    public ResponseResult webPosLogout() {
        // 同 webPosLogin：插件未安装时降级，不抛异常
        return OptionalPluginCaller.call("webpos", "webPosLogout",
                () -> webPosServiceClient.webPosLogout());
    }

    /**
     * 构造用于获取微信用户 session 的 URL 模板。
     * 这个方法用于构建一个包含必要参数的 URL，用于向微信 API 发起请求，以换取用户的 session_key 和其它相关信息。
     *
     * @return 返回一个字符串，该字符串是一个 URL 模板，包含 appid、secret、js_code 和 grant_type 参数。
     * 其中 js_code 参数是一个占位符，需要在实际使用时替换为具体的 JavaScript 标识码。
     */
    private String getCodeSession() {
        // 构建请求微信API的URL模板
        return "https://api.weixin.qq.com/sns/jscode2session?appid=" + getShopAppId()
                + "&secret=" + getShopAppIdSecret() + "&js_code=" + "%s" + "&grant_type=authorization_code";
    }

    // ==================================================================================
    // 统一登录（UniLogin）：渠道无关的扫码登录流程
    //
    // 本段不含任何厂商专有逻辑：渠道差异全部由 LoginChannelRegistry 按配置解析，
    // 新增渠道（企业微信/钉钉/QQ/飞书…）只需加 Nacos 配置，不需要改这里的代码。
    // ==================================================================================

    /** 扫码登录会话在 Redis 中的前缀，按渠道隔离，形如 unilogin_qrcode_wechat_xxx */
    private static final String LOGIN_QRCODE_KEY = "unilogin_qrcode_";
    /** 旧版微信登录遗留的 key 前缀，仅用于读取升级前尚未过期的会话 */
    private static final String LEGACY_WECHAT_QRCODE_KEY = "wechat_qrcode_";
    /** 扫码会话有效期（分钟） */
    private static final int LOGIN_QRCODE_TTL_MINUTES = 5;

    /** 默认渠道，旧接口 /auth/wechat/** 走它 */
    private static final String DEFAULT_LOGIN_CHANNEL = "wechat";

    @Autowired
    private LoginChannelRegistry loginChannelRegistry;

    @Autowired
    private org.springframework.core.env.Environment environment;

    /** 未指定应用保持后台登录口径，绝不读取渠道级自动注册开关。 */
    private Map<String, Object> applicationPolicy(String application, String channel) {
        Map<String, Object> policy = new HashMap<>();
        policy.put("application", "");
        policy.put("allowCreate", false);
        policy.put("registerTerminal", "houcloud");
        if (StrUtil.isBlank(application)) return policy;
        if (!application.matches("[a-z][a-z0-9-]{0,31}")) {
            throw new LoginChannelException("未登记的登录应用。");
        }
        String prefix = "unilogin.applications." + application + ".";
        String allowed = environment.getProperty(prefix + "allowed-channels");
        if (allowed == null) {
            allowed = org.springframework.boot.context.properties.bind.Binder.get(environment)
                    .bind(prefix + "allowed-channels", org.springframework.boot.context.properties.bind.Bindable.listOf(String.class))
                    .map(values -> String.join(",", values)).orElse(null);
        }
        if (StrUtil.isBlank(allowed)) throw new LoginChannelException("未登记的登录应用。");
        if (!Arrays.asList(allowed.split("\\s*,\\s*")).contains(channel)) {
            throw new LoginChannelException("该应用不允许使用此登录渠道。");
        }
        String terminal = environment.getProperty(prefix + "register-terminal");
        if (StrUtil.isBlank(terminal)) throw new LoginChannelException("登录应用未配置注册终端。");
        policy.put("application", application);
        policy.put("allowCreate", environment.getProperty(prefix + "auto-register", Boolean.class, false));
        policy.put("registerTerminal", terminal);
        return policy;
    }

    /** 渠道会话的 Redis key */
    private String qrCacheKey(String channel, String qrCodeId) {
        return LOGIN_QRCODE_KEY + channel + "_" + qrCodeId;
    }

    /** 取渠道执行器；编码为空或未知时统一按「不支持的渠道」处理 */
    private LoginChannelProvider resolveChannel(String channel) {
        String code = StrUtil.isBlank(channel) ? DEFAULT_LOGIN_CHANNEL : channel.trim().toLowerCase();
        LoginChannelProvider provider = loginChannelRegistry.get(code);
        if (provider == null) {
            throw new LoginChannelException("不支持的登录渠道。");
        }
        return provider;
    }

    /** 写入/覆盖扫码会话状态，保持 5 分钟有效期 */
    private void saveLoginQrState(String channel, String qrCodeId, String status, String message) {
        saveLoginQrState(channel, qrCodeId, status, message, null);
    }

    private void saveLoginQrState(String channel, String qrCodeId, String status, String message,
                                  Map<String, Object> extra) {
        JSONObject previous = readLoginQrState(channel, qrCodeId);
        Map<String, Object> data = previous == null ? new HashMap<>() : new HashMap<>(previous);
        if (extra != null) data.putAll(extra);
        data.put("status", status);
        if ("confirmed".equals(status)) data.put("completed", true);
        data.put("channel", channel);
        data.putIfAbsent("createTime", System.currentTimeMillis());
        if (StrUtil.isNotBlank(message)) {
            data.put("message", message);
        }
        redisCache.setCacheObject(qrCacheKey(channel, qrCodeId), JSON.toJSONString(data),
                LOGIN_QRCODE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /** 读取扫码会话；微信渠道额外回退旧前缀，兼容升级前仍在有效期内的会话 */
    private JSONObject readLoginQrState(String channel, String qrCodeId) {
        String cached = (String) redisCache.getCacheObject(qrCacheKey(channel, qrCodeId));
        if (cached == null && DEFAULT_LOGIN_CHANNEL.equals(channel)) {
            cached = (String) redisCache.getCacheObject(LEGACY_WECHAT_QRCODE_KEY + qrCodeId);
        }
        return cached == null ? null : JSON.parseObject(cached);
    }

    /**
     * 统一登录 - 获取扫码地址
     *
     * 返回第三方授权页地址（二维码内容）由前端内嵌展示；渠道未配置凭据时返回 enabled=false，
     * 前端据此隐藏入口或给出配置引导，避免出现扫不动的死二维码。
     *
     * @param channel 渠道编码，如 wechat / wecom / dingtalk / qq / feishu
     * @return ResponseResult 包含 enabled / channel / qrCodeId / qrCode
     */
    @Override
    public ResponseResult getLoginQrCode(String channel, String application) {
        LoginChannelProvider provider = null;
        try {
            provider = resolveChannel(channel);
            Map<String, Object> policy = applicationPolicy(application, provider.code());
            LoginChannelDef definition = channelDefinition(provider.code());
            policy.put("providerAppId", definition == null ? "" : StrUtil.blankToDefault(definition.getAppId(), ""));
            Map<String, Object> result = new HashMap<>();
            result.put("channel", provider.code());
            if (!provider.isConfigured()) {
                result.put("enabled", false);
                result.put("message", provider.notConfiguredMessage());
                return ResponseResult.success(result);
            }

            // state 即本次扫码的会话标识，同时承担 CSRF 校验作用
            String qrCodeId = UUID.randomUUID().toString().replace("-", "");
            saveLoginQrState(provider.code(), qrCodeId, "waiting", null, policy);

            result.put("enabled", true);
            result.put("qrCodeId", qrCodeId);
            result.put("qrCode", provider.buildAuthorizeUrl(qrCodeId));
            return ResponseResult.success(result);
        } catch (LoginChannelException e) {
            return ResponseResult.fail(500, e.getMessage());
        } catch (Exception e) {
            logger.error("获取登录二维码失败, channel={}", channel, e);
            return ResponseResult.fail(500, "获取登录二维码失败，请稍后重试。");
        }
    }

    /**
     * 统一登录 - 授权回调
     *
     * 用户扫码确认后第三方携带 code 与 state 回跳前端回调页，前端再调用本接口换取登录态。
     * 未绑定平台账号时不自动建号，返回 unbound 引导用户先登录再绑定。
     *
     * @param channel 渠道编码
     * @param code    第三方授权临时票据
     * @param state   会话标识（防 CSRF）
     * @return ResponseResult 包含 status 与登录信息（confirmed 时带 token）
     */
    @Override
    public ResponseResult loginCallback(String channel, String code, String state) {
        LoginChannelProvider provider;
        try {
            provider = resolveChannel(channel);
        } catch (LoginChannelException e) {
            return ResponseResult.fail(500, e.getMessage());
        }
        String code2 = provider.code();

        if (!provider.isConfigured()) {
            return ResponseResult.fail(500, provider.notConfiguredMessage());
        }
        if (StrUtil.isBlank(code) || StrUtil.isBlank(state)) {
            return ResponseResult.fail(500, provider.displayName() + "授权参数不完整，请重新扫码。");
        }

        JSONObject cached = readLoginQrState(code2, state);
        if (cached == null) {
            return ResponseResult.success(Map.of("status", "expired"));
        }

        if (cached.getBooleanValue("completed") || !"waiting".equals(cached.getString("status"))) {
            return callbackStateResult(code2, cached);
        }
        // 锁与 state 同寿命且不提前释放，避免慢回调锁过期后误删其他请求的锁。
        String lockKey = qrCacheKey(code2, state) + ":callback-lock";
        if (!Boolean.TRUE.equals(redisCache.redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOGIN_QRCODE_TTL_MINUTES, TimeUnit.MINUTES))) {
            JSONObject latest = readLoginQrState(code2, state);
            return callbackStateResult(code2, latest);
        }
        cached = readLoginQrState(code2, state);
        if (cached == null || !"waiting".equals(cached.getString("status"))) {
            return callbackStateResult(code2, cached);
        }
        saveLoginQrState(code2, state, "processing", null);
        try {
            // 1. 渠道无关的身份换取（token + 可选 userinfo + 字段映射）
            LoginIdentity identity = provider.exchange(code);
            String openid = identity.getProviderUserId();
            String unionid = identity.getUnionId();

            if (StrUtil.isBlank(openid)) throw new LoginChannelException("提供方未返回有效身份。");
            LoginChannelDef currentDef = channelDefinition(code2);
            String providerAppId = cached.getString("providerAppId");
            if (StrUtil.isBlank(providerAppId) && StrUtil.isBlank(cached.getString("application"))) {
                providerAppId = currentDef == null ? null : currentDef.getAppId();
            }
            if (currentDef == null || StrUtil.isBlank(providerAppId)
                    || !providerAppId.equals(currentDef.getAppId())) {
                throw new LoginChannelException("登录渠道配置已变更，请重新扫码。");
            }
            String internalToken = environment.getProperty("unilogin.internal-token");
            if (StrUtil.isBlank(internalToken)) throw new LoginChannelException("内部登录服务未配置。");
            Map<String, String> identityBody = new HashMap<>();
            identityBody.put("channel", code2);
            identityBody.put("providerAppId", providerAppId);
            identityBody.put("openid", openid);
            identityBody.put("unionid", StrUtil.blankToDefault(unionid, ""));
            identityBody.put("nickname", StrUtil.blankToDefault(identity.getNickname(), provider.displayName() + "用户"));
            identityBody.put("avatar", StrUtil.blankToDefault(identity.getAvatar(), ""));
            identityBody.put("registerTerminal", StrUtil.blankToDefault(cached.getString("registerTerminal"), "houcloud"));
            identityBody.put("allowCreate", String.valueOf(StrUtil.isNotBlank(cached.getString("application"))
                    && cached.getBooleanValue("allowCreate")));
            ResponseResult member = userServiceClient.getOrCreateOAuthMember(internalToken, identityBody);
            if (member == null) throw new LoginChannelException("内部登录服务暂时不可用。");
            String userId = null;
            if (member.getCode() == 1 && member.getData() != null) {
                userId = JSON.parseObject(JSON.toJSONString(member.getData())).getString("userId");
            }
            if (StrUtil.isBlank(userId)) {
                if (member.getCode() == 409 && "false".equals(identityBody.get("allowCreate"))
                        && "OAuth 身份尚未绑定会员".equals(member.getMessage())) {
                    Map<String, Object> extra = new HashMap<>();
                    extra.put("openid", openid);
                    saveLoginQrState(code2, state, "unbound",
                            "该" + provider.displayName() + "账号尚未绑定平台账号，请先用账号密码登录后完成绑定。", extra);

                    Map<String, Object> result = new HashMap<>();
                    result.put("status", "unbound");
                    result.put("channel", code2);
                    result.put("message", "该" + provider.displayName() + "账号尚未绑定平台账号，请先用账号密码登录后绑定。");
                    return ResponseResult.success(result);
                }
                throw new LoginChannelException("会员身份校验失败，请稍后重试或联系管理员。");
            }

            // 4. 已绑定：签发登录态（与 uniappLogin 一致）
            UserInfoVO userInfo = userServiceClient.getUserInfoByUserId(userId);
            if (userInfo == null) {
                saveLoginQrState(code2, state, "failed", null);
                return ResponseResult.fail(500, "账号信息不存在，请联系管理员。");
            }

            String token = JwtUtil.createJWT(userInfo.getUserId());
            UserInfoDTO dto = new UserInfoDTO();
            dto.setUser(userInfo);
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto));
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USER + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId());
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USERID + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);

            Map<String, Object> dayLive = new HashMap<>();
            dayLive.put("token", token);
            dayLive.put("userId", userInfo.getUserId());
            dayLive.put("userMobile", userInfo.getUserMobile());
            // 用户来源 1.pc;2.h5;3.小程序;4.app —— 管理后台扫码登录记 PC
            dayLive.put("platForm", "1");
            userServiceClient.insertUserDayLive(dayLive);

            Map<String, Object> extra = new HashMap<>();
            extra.put("token", token);
            extra.put("userId", userInfo.getUserId());
            extra.put("userMobile", userInfo.getUserMobile());
            extra.put("openid", openid);
            saveLoginQrState(code2, state, "confirmed", null, extra);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "confirmed");
            result.put("channel", code2);
            result.put("token", token);
            result.put("userId", userInfo.getUserId());
            result.put("userMobile", userInfo.getUserMobile());
            return ResponseResult.success(result);
        } catch (LoginChannelException e) {
            // 渠道侧的明确失败：记状态并原样回传可读文案
            saveLoginQrState(code2, state, "failed", null);
            logger.warn("统一登录回调失败, channel={}: {}", code2, e.getMessage());
            return ResponseResult.fail(500, e.getMessage());
        } catch (Exception e) {
            saveLoginQrState(code2, state, "failed", null);
            logger.error("统一登录回调处理失败, channel={}", code2, e);
            return ResponseResult.fail(500, "登录失败，请稍后重试。");
        }
    }

    /** 取渠道的绑定标识前缀；注册中心未命中时退回 weixin_，与历史微信绑定数据保持一致 */
    private String channelBindingPrefix(String channel) {
        try {
            LoginChannelProvider provider = loginChannelRegistry.get(channel);
            if (provider instanceof ConfigurableOAuthChannelProvider) {
                String prefix = ((ConfigurableOAuthChannelProvider) provider).definition().getBindingPrefix();
                if (StrUtil.isNotBlank(prefix)) {
                    return prefix;
                }
            }
        } catch (Exception e) {
            logger.warn("读取渠道绑定前缀失败, channel={}", channel, e);
        }
        return "weixin_";
    }

    /** 取渠道定义；渠道不是配置驱动实现时返回 null */
    private LoginChannelDef channelDefinition(String channel) {
        try {
            LoginChannelProvider provider = loginChannelRegistry.get(channel);
            if (provider instanceof ConfigurableOAuthChannelProvider) {
                return ((ConfigurableOAuthChannelProvider) provider).definition();
            }
        } catch (Exception e) {
            logger.warn("读取渠道定义失败, channel={}", channel, e);
        }
        return null;
    }

    private ResponseResult callbackStateResult(String channel, JSONObject cached) {
        Map<String, Object> result = new HashMap<>();
        result.put("channel", channel);
        if (cached == null) {
            result.put("status", "expired");
        } else if ("confirmed".equals(cached.getString("status"))) {
            result.put("status", "confirmed");
            result.put("token", cached.getString("token"));
            result.put("userId", cached.getString("userId"));
            result.put("userMobile", cached.getString("userMobile"));
        } else {
            String status = cached.getString("status");
            result.put("status", "waiting".equals(status) ? "processing" : status);
            if (cached.containsKey("message")) result.put("message", cached.getString("message"));
        }
        return ResponseResult.success(result);
    }

    /**
     * 统一登录 - 检查登录状态
     *
     * @param channel  渠道编码
     * @param qrCodeId 会话标识
     * @return ResponseResult 包含登录状态和token（如果已确认）
     */
    @Override
    public ResponseResult checkLoginStatus(String channel, String qrCodeId) {
        try {
            LoginChannelProvider provider = resolveChannel(channel);
            String code = provider.code();

            JSONObject qrCodeData = readLoginQrState(code, qrCodeId);
            if (qrCodeData == null) {
                return ResponseResult.success(Map.of("status", "expired"));
            }

            String status = qrCodeData.getString("status");
            Map<String, Object> result = new HashMap<>();
            result.put("channel", code);
            result.put("status", status);
            if (qrCodeData.getString("message") != null) {
                result.put("message", qrCodeData.getString("message"));
            }

            // 保留完成态直到会话过期，重复回调与轮询返回同一登录结果。
            if ("confirmed".equals(status)) {
                result.put("token", qrCodeData.getString("token"));
                result.put("userId", qrCodeData.getString("userId"));
                result.put("userMobile", qrCodeData.getString("userMobile"));
            }
            return ResponseResult.success(result);
        } catch (LoginChannelException e) {
            return ResponseResult.fail(500, e.getMessage());
        } catch (Exception e) {
            logger.error("检查登录状态失败, channel={}", channel, e);
            return ResponseResult.fail(500, "检查登录状态失败");
        }
    }

    /**
     * 统一登录 - 取消登录
     *
     * @param channel  渠道编码
     * @param qrCodeId 会话标识
     */
    @Override
    public ResponseResult cancelLogin(String channel, String qrCodeId) {
        try {
            LoginChannelProvider provider = resolveChannel(channel);
            redisCache.deleteObject(qrCacheKey(provider.code(), qrCodeId));
            redisCache.deleteObject(LEGACY_WECHAT_QRCODE_KEY + qrCodeId);
            return ResponseResult.success();
        } catch (LoginChannelException e) {
            return ResponseResult.fail(500, e.getMessage());
        } catch (Exception e) {
            logger.error("取消登录失败, channel={}", channel, e);
            return ResponseResult.fail(500, "取消登录失败");
        }
    }

    /**
     * 统一登录 - 渠道概况
     *
     * 只返回编码/名称/是否已配置，供设置页与排障使用；不返回任何凭据片段。
     */
    @Override
    public ResponseResult listLoginChannels() {
        try {
            return ResponseResult.success(loginChannelRegistry.describe());
        } catch (Exception e) {
            logger.error("获取登录渠道列表失败", e);
            return ResponseResult.fail(500, "获取登录渠道列表失败");
        }
    }

    /**
     * 统一登录 - 扫码后绑定（手机号未注册时自动建会员号）
     *
     * <p>商城前台（PC 买家平台）会员没有后台账号，扫码后必然是「未绑定」。
     * 这里让用户在弹窗内补一次手机号 + 短信验证码即可完成：
     * 已注册 → 直接登录并写入绑定；未注册 → 先自动建号再绑定。
     * 会员因此不需要先有账号，扫码即可开通。</p>
     */
    @Override
    public ResponseResult bindLogin(String channel, String qrCodeId, String userMobile, String phoneCode) {
        if (StrUtil.isBlank(userMobile) || StrUtil.isBlank(phoneCode)) {
            return ResponseResult.fail(500, "手机号与验证码不能为空");
        }
        LoginChannelProvider provider;
        try {
            provider = resolveChannel(channel);
        } catch (LoginChannelException e) {
            return ResponseResult.fail(500, e.getMessage());
        }
        String code = provider.code();

        JSONObject cached = readLoginQrState(code, qrCodeId);
        if (cached == null) {
            return ResponseResult.fail(500, "二维码已失效，请重新扫码");
        }
        String openid = cached.getString("openid");
        if (StrUtil.isBlank(openid)) {
            return ResponseResult.fail(500, "扫码会话无效，请重新扫码后再绑定");
        }

        String inputCode = phoneCode.trim();
        String redisCode = resolveSmsCodeFromRedis(userMobile);
        if (redisCode == null || !inputCode.equals(redisCode)) {
            return ResponseResult.fail(BizCodeEnume.INCORRECT_VERIFICATION_CODE);
        }

        String bindId = StrUtil.blankToDefault(channelBindingPrefix(code), "") + openid;
        try {
            String userId = userServiceClient.selectUserIdByOpenid(bindId);
            if (StrUtil.isBlank(userId)) {
                UserInfoVO existUser = userServiceClient.getUserInfoByMobile(userMobile);
                if (existUser != null && StrUtil.isNotBlank(existUser.getUserId())) {
                    userId = existUser.getUserId();
                    Map<String, Object> bindMap = new HashMap<>();
                    bindMap.put("bindId", bindId);
                    bindMap.put("bind_type", channelBindType(code));
                    bindMap.put("user_id", userId);
                    bindMap.put("bind_openid", openid);
                    bindMap.put("bind_unionid", StrUtil.blankToDefault(cached.getString("unionid"), ""));
                    bindMap.put("bind_nickname", provider.displayName() + "用户");
                    bindMap.put("bind_avator", "");
                    userServiceClient.insertUserThreeBindMap(bindMap);
                } else {
                    Map<String, String> registerMap = new HashMap<>();
                    registerMap.put("openid", openid);
                    registerMap.put("nickName", provider.displayName() + "用户");
                    registerMap.put("avatarUrl", "");
                    registerMap.put("userMobile", userMobile);
                    registerMap.put("bindId", bindId);
                    registerMap.put("bindType", String.valueOf(channelBindType(code)));
                    // 用户来源 1.pc;2.h5;3.小程序;4.app —— 商城前台记 PC 买家
                    registerMap.put("platForm", "1");
                    ResponseResult registerResult = userServiceClient.thirDregister(registerMap);
                    if (registerResult == null || registerResult.getCode() != 1) {
                        logger.error("扫码绑定自动注册失败, channel={}, message={}", code,
                                registerResult == null ? "null" : registerResult.getMessage());
                        return ResponseResult.fail(500, "注册失败，请稍后重试或改用手机号登录");
                    }
                    UserInfoVO newUser = userServiceClient.getUserInfoByMobile(userMobile);
                    if (newUser == null || StrUtil.isBlank(newUser.getUserId())) {
                        return ResponseResult.fail(500, "注册成功但账号未就绪，请稍后重试登录");
                    }
                    userId = newUser.getUserId();
                }
            }

            UserInfoVO userInfo = userServiceClient.getUserInfoByUserId(userId);
            if (userInfo == null) {
                return ResponseResult.fail(500, "账号信息不存在，请联系客服。");
            }

            String token = JwtUtil.createJWT(userInfo.getUserId());
            UserInfoDTO dto = new UserInfoDTO();
            dto.setUser(userInfo);
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USER + token, JSON.toJSONString(dto));
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USER + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);
            redisCache.setCacheObject(HouConfig.HOU_USER_TOKEN_USERID + token, userInfo.getUserId());
            redisCache.expire(HouConfig.HOU_USER_TOKEN_USERID + token, HouConfig.TOKEN_OUTTIME, TimeUnit.SECONDS);

            Map<String, Object> dayLive = new HashMap<>();
            dayLive.put("token", token);
            dayLive.put("userId", userInfo.getUserId());
            dayLive.put("userMobile", userInfo.getUserMobile());
            dayLive.put("platForm", "1");
            userServiceClient.insertUserDayLive(dayLive);

            Map<String, Object> extra = new HashMap<>();
            extra.put("token", token);
            extra.put("userId", userInfo.getUserId());
            extra.put("userMobile", userInfo.getUserMobile());
            extra.put("openid", openid);
            // 置为 confirmed，让仍在轮询扫码状态的页面也能直接拿到登录态
            saveLoginQrState(code, qrCodeId, "confirmed", null, extra);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "confirmed");
            result.put("channel", code);
            result.put("token", token);
            result.put("userId", userInfo.getUserId());
            result.put("userMobile", userInfo.getUserMobile());
            return ResponseResult.success(result);
        } catch (Exception e) {
            logger.error("扫码绑定登录失败, channel={}, mobile={}", code, userMobile, e);
            return ResponseResult.fail(500, "绑定失败，请稍后重试");
        }
    }

    /** 渠道 → 绑定类型（hou_user_bind_connect.bind_type） */
    private Integer channelBindType(String channel) {
        switch (StrUtil.blankToDefault(channel, "")) {
            case "qq":
                return CommonType.bindType.QQ.getCode();
            case "wecom":
                return CommonType.bindType.WECOM.getCode();
            case "dingtalk":
                return CommonType.bindType.DINGTALK.getCode();
            case "feishu":
                return CommonType.bindType.FEISHU.getCode();
            default:
                return CommonType.bindType.WEIXIN.getCode();
        }
    }

    // ==================== 旧版微信接口（保留为 wechat 渠道的委托，行为不变） ====================

    /**
     * @deprecated 改用渠道无关的 {@link #getLoginQrCode(String)}
     */
    @Deprecated
    @Override
    public ResponseResult getWechatQRCode() {
        return getLoginQrCode(DEFAULT_LOGIN_CHANNEL);
    }

    /**
     * @deprecated 改用渠道无关的 {@link #loginCallback(String, String, String)}
     */
    @Deprecated
    @Override
    public ResponseResult wechatCallback(String code, String state) {
        return loginCallback(DEFAULT_LOGIN_CHANNEL, code, state);
    }

    /**
     * @deprecated 改用渠道无关的 {@link #checkLoginStatus(String, String)}
     */
    @Deprecated
    @Override
    public ResponseResult checkWechatLoginStatus(String qrCodeId) {
        return checkLoginStatus(DEFAULT_LOGIN_CHANNEL, qrCodeId);
    }

    /**
     * @deprecated 改用渠道无关的 {@link #cancelLogin(String, String)}
     */
    @Deprecated
    @Override
    public ResponseResult cancelWechatLogin(String qrCodeId) {
        return cancelLogin(DEFAULT_LOGIN_CHANNEL, qrCodeId);
    }

    /**
     * 从 User-Agent 中解析设备类型
     *
     * @param request HTTP 请求对象
     * @return 设备类型：Mobile/Tablet/PC/Unknown
     */
    private String getDevice(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) return "Unknown";
        if (ua.contains("Mobile") || ua.contains("Android") || ua.contains("iPhone")) return "Mobile";
        if (ua.contains("iPad") || ua.contains("Tablet")) return "Tablet";
        return "PC";
    }

    /**
     * 从 User-Agent 中解析浏览器信息
     *
     * @param request HTTP 请求对象
     * @return 浏览器名称：Chrome/Firefox/Safari/Edge/Other/Unknown
     */
    private String getBrowser(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) return "Unknown";
        if (ua.contains("Chrome") && !ua.contains("Edg")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari") && !ua.contains("Chrome")) return "Safari";
        if (ua.contains("Edg")) return "Edge";
        return "Other";
    }

    /**
     * 从 User-Agent 中解析操作系统
     *
     * @param request HTTP 请求对象
     * @return 操作系统名称：Windows/macOS/Linux/Android/iOS/Other/Unknown
     */
    private String getOs(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) return "Unknown";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS")) return "macOS";
        if (ua.contains("Linux")) return "Linux";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        return "Other";
    }

}
