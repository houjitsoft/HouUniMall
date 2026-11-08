package com.houjiang.auth.login.channel;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置驱动的 OAuth2 渠道执行器（UniLogin 默认实现）
 *
 * <p>把「标准 OAuth2 授权码流程」抽象成四步，全部由 {@link LoginChannelDef} 描述，无任何厂商分支：</p>
 * <ol>
 *   <li>按模板拼授权页地址（二维码内容）</li>
 *   <li>用 code 换 token</li>
 *   <li>（可选）用 token 再拉一次 userinfo —— 覆盖企业微信/QQ/钉钉/飞书这类
 *       「token 响应里没有用户标识」的渠道</li>
 *   <li>按字段映射取出身份标识与展示信息</li>
 * </ol>
 *
 * @author Amos
 */
@Slf4j
public class ConfigurableOAuthChannelProvider implements LoginChannelProvider {

    private final LoginChannelDef def;
    private final RestTemplate restTemplate;

    public ConfigurableOAuthChannelProvider(LoginChannelDef def, RestTemplate restTemplate) {
        this.def = def;
        this.restTemplate = restTemplate;
    }

    public LoginChannelDef definition() {
        return def;
    }

    @Override
    public String code() {
        return def.getCode();
    }

    @Override
    public String displayName() {
        return def.displayName();
    }

    @Override
    public boolean isConfigured() {
        return def.isConfigured();
    }

    @Override
    public String notConfiguredMessage() {
        return def.notConfiguredMessage();
    }

    // ==================== 1. 授权页地址 ====================

    @Override
    public String buildAuthorizeUrl(String state) {
        Map<String, String> vars = baseVars();
        // 授权地址里的回调地址必须编码，否则其中的 ? & 会被当成授权端点自己的参数
        vars.put("redirectUri", URLEncoder.encode(StrUtil.blankToDefault(def.getRedirectUri(), ""), StandardCharsets.UTF_8));
        vars.put("state", state);
        String url = render(def.getAuthorizeUrl(), vars);
        if (StrUtil.isNotBlank(def.getAuthorizeExtraParams())) {
            url = url + (url.contains("?") ? "&" : "?") + def.getAuthorizeExtraParams().trim();
        }
        if (StrUtil.isNotBlank(def.getAuthorizeAnchor())) {
            url = url + def.getAuthorizeAnchor().trim();
        }
        return url;
    }

    // ==================== 2~4. 换取身份 ====================

    @Override
    public LoginIdentity exchange(String code) {
        Map<String, String> vars = baseVars();
        vars.put("code", code);
        vars.put("redirectUri", StrUtil.blankToDefault(def.getRedirectUri(), ""));

        // ---- 2. code 换 token ----
        String tokenUrl = render(def.getTokenUrl(), vars);
        String tokenResp = requestToken(tokenUrl, vars);
        log.info("[unilogin:{}] 换 token 响应: {}", def.getCode(), mask(tokenResp));
        JSONObject tokenJson = parse(tokenResp, def.getTokenResponseType());
        assertSuccess(tokenJson, "换取访问令牌");

        JSONObject tokenPayload = root(tokenJson, def.getTokenRootPath());
        String accessToken = firstNonBlank(tokenPayload, def.getAccessTokenField());
        if (StrUtil.isBlank(accessToken)) {
            throw new LoginChannelException(def.displayName() + "未返回访问令牌，请重新扫码。");
        }

        // ---- 3. 可选 userinfo ----
        JSONObject payload = tokenPayload;
        String rawForLog = tokenResp;
        if (StrUtil.isNotBlank(def.getUserInfoUrl())) {
            vars.put("accessToken", accessToken);
            vars.put("openId", firstNonBlank(tokenPayload, def.getUserIdField()));
            String userInfoUrl = render(def.getUserInfoUrl(), vars);
            String userInfoResp = requestUserInfo(userInfoUrl, accessToken);
            log.info("[unilogin:{}] userinfo 响应: {}", def.getCode(), mask(userInfoResp));
            rawForLog = userInfoResp;
            JSONObject userInfoJson = parse(userInfoResp, def.getTokenResponseType());
            assertSuccess(userInfoJson, "获取用户信息");
            payload = root(userInfoJson, def.getUserInfoRootPath());
        }

        // ---- 4. 字段映射 ----
        LoginIdentity identity = new LoginIdentity();
        identity.setProviderUserId(firstNonBlank(payload, def.getUserIdField()));
        identity.setUnionId(firstNonBlank(payload, def.getUnionIdField()));
        identity.setNickname(firstNonBlank(payload, def.getNicknameField()));
        identity.setAvatar(firstNonBlank(payload, def.getAvatarField()));
        identity.setRaw(rawForLog);
        identity.setAttributes(new HashMap<>(payload));
        if (!identity.hasProviderUserId()) {
            throw new LoginChannelException(def.displayName()
                    + "未返回用户标识（字段 " + def.getUserIdField() + "），请检查渠道字段映射配置。");
        }
        return identity;
    }

    // ==================== HTTP ====================

    private String requestToken(String url, Map<String, String> vars) {
        String method = StrUtil.blankToDefault(def.getTokenMethod(), "GET").trim().toUpperCase();
        try {
            if (!"POST".equals(method)) {
                return restTemplate.getForObject(url, String.class);
            }
            String bodyType = StrUtil.blankToDefault(def.getTokenBodyType(), "form").trim().toLowerCase();
            if ("json".equals(bodyType)) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                String body = render(def.getTokenBodyTemplate(), vars);
                return restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
            }
            // 表单提交：请求体由 token-extra-params 以 k=v&k2=v2 形式给出
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.putAll(parsePairs(def.getTokenExtraParams()));
            form.putAll(parsePairs(def.getCustomParams()));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            return restTemplate.postForObject(url, new HttpEntity<>(form, headers), String.class);
        } catch (Exception e) {
            log.error("[unilogin:{}] 换取 token 失败: {}", def.getCode(), e.getMessage());
            throw new LoginChannelException(def.displayName() + "授权失败，请重新扫码或稍后重试。", e);
        }
    }

    private String requestUserInfo(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        if (StrUtil.isNotBlank(def.getUserInfoTokenHeader())) {
            String prefix = StrUtil.blankToDefault(def.getUserInfoTokenHeaderPrefix(), "").trim();
            headers.set(def.getUserInfoTokenHeader().trim(),
                    StrUtil.isBlank(prefix) ? accessToken : prefix + " " + accessToken);
        }
        String method = StrUtil.blankToDefault(def.getUserInfoMethod(), "GET").trim().toUpperCase();
        try {
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.valueOf(method),
                    new HttpEntity<>(headers), String.class);
            return resp.getBody();
        } catch (Exception e) {
            log.error("[unilogin:{}] 拉取用户信息失败: {}", def.getCode(), e.getMessage());
            throw new LoginChannelException(def.displayName() + "获取用户信息失败，请重新扫码。", e);
        }
    }

    // ==================== 解析与校验 ====================

    private JSONObject parse(String body, String responseType) {
        if (StrUtil.isBlank(body)) {
            throw new LoginChannelException(def.displayName() + "未返回数据，请重新扫码。");
        }
        try {
            if ("form".equalsIgnoreCase(StrUtil.blankToDefault(responseType, "json").trim())) {
                JSONObject json = new JSONObject();
                parsePairs(body.replace("\n", "&")).forEach(json::put);
                return json;
            }
            return JSONObject.parseObject(body.trim());
        } catch (Exception e) {
            log.error("[unilogin:{}] 响应解析失败: {}", def.getCode(), mask(body));
            throw new LoginChannelException(def.displayName() + "返回数据无法解析，请稍后重试。", e);
        }
    }

    /** 校验业务错误码；字段缺失视为成功，取值命中 error-success-values 也视为成功 */
    private void assertSuccess(JSONObject json, String action) {
        String codeField = def.getErrorCodeField();
        if (StrUtil.isBlank(codeField) || json == null) {
            return;
        }
        Object value = json.get(codeField.trim());
        if (value == null) {
            return;
        }
        String codeValue = String.valueOf(value).trim();
        if (Arrays.stream(StrUtil.blankToDefault(def.getErrorSuccessValues(), "0,200").split(","))
                .map(String::trim)
                .anyMatch(codeValue::equals)) {
            return;
        }
        String message = firstNonBlank(json, def.getErrorMessageField());
        log.error("[unilogin:{}] {}失败: code={} message={}", def.getCode(), action, codeValue, message);
        throw new LoginChannelException(def.displayName() + "授权失败（" + codeValue
                + (StrUtil.isNotBlank(message) ? "：" + message : "") + "），请重新扫码。");
    }

    /** 取业务数据根节点；rootPath 为空时返回原对象 */
    private JSONObject root(JSONObject json, String rootPath) {
        if (json == null) {
            return new JSONObject();
        }
        if (StrUtil.isBlank(rootPath)) {
            return json;
        }
        JSONObject current = json;
        for (String segment : rootPath.trim().split("\\.")) {
            JSONObject next = current.getJSONObject(segment.trim());
            if (next == null) {
                // 根路径不存在时退回原对象，避免把「本就平的响应」误判成无数据
                return json;
            }
            current = next;
        }
        return current;
    }

    /** 按逗号分隔的候选字段名依次取值，兼容不同渠道/不同用户类型的字段差异 */
    private String firstNonBlank(JSONObject json, String fieldNames) {
        if (json == null || StrUtil.isBlank(fieldNames)) {
            return null;
        }
        for (String field : fieldNames.split(",")) {
            Object value = json.get(field.trim());
            if (value != null && StrUtil.isNotBlank(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    // ==================== 模板渲染 ====================

    private Map<String, String> baseVars() {
        Map<String, String> vars = new HashMap<>();
        vars.put("appId", StrUtil.blankToDefault(def.getAppId(), ""));
        vars.put("appSecret", StrUtil.blankToDefault(def.getAppSecret(), ""));
        vars.put("redirectUri", StrUtil.blankToDefault(def.getRedirectUri(), ""));
        // 自定义参数（如企微 agentId）作为模板变量注入；同名时以标准变量优先
        parsePairs(def.getCustomParams()).forEach((key, values) -> {
            if (!values.isEmpty() && !vars.containsKey(key)) {
                vars.put(key, values.get(0));
            }
        });
        return vars;
    }

    /** 把模板中的 {k} 全部替换为 vars 中的值；无对应变量时替换为空串 */
    private String render(String template, Map<String, String> vars) {
        if (StrUtil.isBlank(template)) {
            return "";
        }
        String result = template;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", StrUtil.blankToDefault(entry.getValue(), ""));
        }
        return result.replaceAll("\\{[A-Za-z0-9_]+}", "");
    }

    /** 解析 k=v&k2=v2 形式的参数串 */
    private MultiValueMap<String, String> parsePairs(String pairs) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (StrUtil.isBlank(pairs)) {
            return map;
        }
        for (String pair : pairs.trim().split("[&,;]")) {
            if (StrUtil.isBlank(pair) || !pair.contains("=")) {
                continue;
            }
            int idx = pair.indexOf('=');
            String key = pair.substring(0, idx).trim();
            String value = pair.substring(idx + 1).trim();
            if (StrUtil.isNotBlank(key)) {
                map.add(key, value);
            }
        }
        return map;
    }

    /** 日志脱敏：第三方响应里可能带 access_token，落日志前截断并抹掉令牌值 */
    private String mask(String body) {
        if (body == null) {
            return null;
        }
        String masked = body.replaceAll("(\"(?:access_token|accessToken|user_ticket|refresh_token|refreshToken)\"\\s*:\\s*\")[^\"]+", "$1***");
        masked = masked.replaceAll("((?:access_token|accessToken)=)[^&\\s]+", "$1***");
        masked = masked.replaceAll("(\"code\"\\s*:\\s*\")[^\"]{8,}", "$1***");
        return masked.length() > 500 ? masked.substring(0, 500) + "…" : masked;
    }

    /** 仅测试/排障用：暴露字段映射的解析结果 */
    public String describeMapping() {
        return Arrays.stream(new String[]{def.getUserIdField(), def.getUnionIdField(),
                        def.getBindingPrefix(), def.getAuthorizeUrl(), def.getTokenUrl()})
                .collect(Collectors.joining(" | "));
    }
}
