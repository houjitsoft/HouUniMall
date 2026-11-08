package com.houjiang.bffweb.service;

import com.alibaba.fastjson2.JSON;
import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.feign.UserServiceFeign;
import com.houjiang.bffweb.util.CommonStatusEnum;
import com.houjiang.bffweb.util.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册协议同意链路支撑服务（BFF 侧聚合）。
 *
 * <p>职责：把 base 的「协议定义 + 生效版本」与 userms 的「用户同意记录」聚合起来，
 * 供注册前必选协议校验、站内协议待同意计算、重新同意留痕三处复用。</p>
 *
 * @author Amos
 */
@Service
public class AgreementConsentService {

    private static final Logger log = LoggerFactory.getLogger(AgreementConsentService.class);

    /** 注册场景 */
    public static final String SCENE_REGISTER = "register";
    /** 重新同意场景 */
    public static final String SCENE_REAGREE = "reagree";
    /** 插件购买 */
    public static final String SCENE_PLUGIN_PURCHASE = "plugin_purchase";

    @Autowired
    private BaseServiceFeign baseServiceFeign;

    @Autowired
    private UserServiceFeign userServiceFeign;

    /**
     * 站点侧：把签署记录回传平台留痕（未配置平台地址时静默跳过）。
     */
    @Autowired(required = false)
    private PlatformConsentReporter platformConsentReporter;

    /**
     * 查询某场景下启用中的协议及其当前生效版本。
     *
     * @param scene 场景
     * @return 协议列表；依赖不可用时返回 null（调用方据此决定降级策略）
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listSceneAgreements(String scene) {
        try {
            Map<String, Object> param = new HashMap<>(2);
            param.put("scene", scene);
            ResponseResult result = baseServiceFeign.agreementList(param);
            if (result == null || result.getCode() != CommonStatusEnum.SUCCESS.getCode()
                    || !(result.getData() instanceof List)) {
                return null;
            }
            return (List<Map<String, Object>>) result.getData();
        } catch (Exception e) {
            log.warn("查询场景协议失败，scene={}, err={}", scene, e.getMessage());
            return null;
        }
    }

    /**
     * 注册前校验：客户端提交的协议勾选是否覆盖了全部必选协议，且版本号与当前生效版本一致。
     *
     * @param clientAgreements 客户端提交的勾选协议，元素形如 {code, versionNo}
     * @param scene            场景
     * @return 校验结果，包含 consents（用于落库的同意条目 JSON）与错误信息
     */
    @SuppressWarnings("unchecked")
    public ValidateResult validateAgreements(Object clientAgreements, String scene) {
        List<Map<String, Object>> agreements = listSceneAgreements(scene);
        if (agreements == null) {
            // 协议服务不可用：降级放行。注册页本身仍要求勾选，此处不因依赖故障阻断注册。
            log.warn("协议服务不可用，注册前协议校验降级放行，scene={}", scene);
            return ValidateResult.degraded();
        }
        return validateAgreementCatalog(clientAgreements, agreements);
    }

    private ValidateResult validateAgreementCatalog(Object clientAgreements,
                                                     List<Map<String, Object>> agreements) {
        List<Map<String, Object>> agreed = normalizeAgreements(clientAgreements);

        List<String> missing = new ArrayList<>();
        List<Map<String, Object>> consents = new ArrayList<>();
        for (Map<String, Object> agreement : agreements) {
            String code = str(agreement.get("code"));
            if (code.isEmpty()) {
                continue;
            }
            boolean required = asInt(agreement.get("required"), 0) == 1;
            String currentVersion = str(agreement.get("versionNo"));
            Map<String, Object> hit = null;
            for (Map<String, Object> item : agreed) {
                if (code.equals(str(item.get("code")))) {
                    hit = item;
                    break;
                }
            }
            if (hit == null) {
                if (required && !currentVersion.isEmpty()) {
                    missing.add(str(agreement.get("name")).isEmpty() ? code : str(agreement.get("name")));
                }
                continue;
            }
            if (currentVersion.isEmpty()) {
                // 协议尚无生效版本：无法要求用户同意，跳过（不阻断）
                log.warn("协议 {} 未配置生效版本，跳过校验", code);
                continue;
            }
            if (!currentVersion.equals(str(hit.get("versionNo")))) {
                return ValidateResult.versionExpired(code, str(agreement.get("name")), currentVersion);
            }
            Map<String, Object> consent = new LinkedHashMap<>(8);
            consent.put("code", code);
            consent.put("agreementId", agreement.get("id"));
            consent.put("agreementName", agreement.get("name"));
            consent.put("versionId", agreement.get("versionId"));
            consent.put("versionNo", currentVersion);
            consents.add(consent);
        }
        if (!missing.isEmpty()) {
            return ValidateResult.missing(missing);
        }
        ValidateResult result = new ValidateResult();
        result.setOk(true);
        result.setConsentsJson(consents.isEmpty() ? "" : JSON.toJSONString(consents));
        return result;
    }

    /**
     * 计算当前登录用户待重新同意的协议。
     *
     * @param userId 用户ID
     * @param scene  场景
     * @return 待同意协议列表（含正文），全部已同意时为空列表；
     *         <b>协议目录或同意记录查询失败时返回 {@code null}</b>（禁止当成「从未同意」）
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listPendingAgreements(String userId, String scene) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (userId == null || userId.isEmpty()) {
            return result;
        }
        List<Map<String, Object>> agreements = listSceneAgreements(scene);
        if (agreements == null) {
            // 依赖故障：必须与「清单为空 / 已全部同意」区分，否则会误弹补签窗
            return null;
        }
        if (agreements.isEmpty()) {
            return result;
        }
        Map<String, String> agreedVersions = listAgreedVersions(userId);
        if (agreedVersions == null) {
            return null;
        }

        for (Map<String, Object> agreement : agreements) {
            String code = str(agreement.get("code"));
            String currentVersion = str(agreement.get("versionNo"));
            if (code.isEmpty() || currentVersion.isEmpty()) {
                continue;
            }
            String agreedVersion = agreedVersions.get(code);
            if (currentVersion.equals(agreedVersion)) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>(agreement);
            item.put("agreedVersionNo", agreedVersion == null ? "" : agreedVersion);
            result.add(item);
        }
        return result;
    }

    /**
     * 登录后待同意清单：只包含站点运营者面向站内用户配置的协议。
     *
     * <p>开源 EULA 属于安装者与软件提供方之间的安装确认，不得进入站内用户登录流程。</p>
     */
    public List<Map<String, Object>> listPendingForLogin(String userId) {
        return listPendingAgreements(userId, SCENE_REGISTER);
    }

    /**
     * 校验并记录登录后的站内协议补签。
     */
    public ValidateResult validateAndRecordLoginPending(String userId, String username, Object clientAgreements,
                                                        String ip, String ua, String source) {
        return validateAndRecord(userId, username, clientAgreements, ip, ua, source,
                SCENE_REGISTER, SCENE_REAGREE);
    }

    /**
     * 购买场景校验并落库（fail-closed：协议服务不可用也拒绝下单）。
     */
    public ValidateResult validateAndRecordPluginPurchase(String userId, String username, Object clientAgreements,
                                                          String ip, String ua, String source) {
        List<Map<String, Object>> agreements = listSceneAgreements(SCENE_PLUGIN_PURCHASE);
        if (agreements == null) {
            ValidateResult failed = new ValidateResult();
            failed.setOk(false);
            failed.setMessage("协议服务不可用，无法完成购买前确认，请稍后重试");
            return failed;
        }
        ValidateResult validate = validateAgainstCatalog(clientAgreements, agreements, true);
        if (!validate.isOk()) {
            return validate;
        }
        if (validate.getConsentsJson().isEmpty()) {
            ValidateResult failed = new ValidateResult();
            failed.setOk(false);
            failed.setMessage("请勾选并同意《商业插件购买与维护协议》后再下单");
            return failed;
        }
        List<Map<String, Object>> consents = normalizeAgreements(JSON.parseArray(validate.getConsentsJson()));
        int count = recordConsents(userId, username, consents, ip, ua, source, SCENE_PLUGIN_PURCHASE);
        if (count < 0) {
            return failSave();
        }
        return validate;
    }

    /**
     * 依据 token 解析当前登录用户ID（供购买等链路复用）。
     */
    @SuppressWarnings("unchecked")
    public String resolveUserId(String token) {
        Map<String, Object> user = resolveUserMap(token);
        if (user == null) {
            return null;
        }
        Object id = user.get("userId");
        if (id == null) {
            id = user.get("user_id");
        }
        if (id == null) {
            id = user.get("id");
        }
        return id == null ? null : String.valueOf(id).trim();
    }

    /**
     * 用户名（登录名/手机号），用于同意记录冗余检索。
     */
    public String resolveUsername(String token) {
        Map<String, Object> user = resolveUserMap(token);
        if (user == null) {
            return "";
        }
        for (String key : new String[]{"userName", "user_name", "userMobile", "user_mobile"}) {
            Object value = user.get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveUserMap(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            // 必须用 /queryUserInfo（getUserInfo）：UserInfoVO 带 userId；
            // /information/getUserInfo 的 VO 没有 userId，会导致误判未登录。
            Map<String, Object> param = new HashMap<>(2);
            param.put("token", token);
            ResponseResult result = userServiceFeign.getUserInfo(param);
            if (result == null || result.getCode() != CommonStatusEnum.SUCCESS.getCode()
                    || !(result.getData() instanceof Map)) {
                return null;
            }
            return (Map<String, Object>) result.getData();
        } catch (Exception e) {
            log.warn("解析登录用户失败：{}", e.getMessage());
            return null;
        }
    }

    private ValidateResult failSave() {
        ValidateResult failed = new ValidateResult();
        failed.setOk(false);
        failed.setMessage("同意记录保存失败，请稍后重试");
        return failed;
    }

    /**
     * 按给定协议目录校验客户端勾选。
     *
     * @param requireAllRequired true=缺必选即失败；degrade 路径由调用方处理
     */
    @SuppressWarnings("unchecked")
    private ValidateResult validateAgainstCatalog(Object clientAgreements, List<Map<String, Object>> agreements,
                                                  boolean requireAllRequired) {
        List<Map<String, Object>> agreed = normalizeAgreements(clientAgreements);
        List<String> missing = new ArrayList<>();
        List<Map<String, Object>> consents = new ArrayList<>();
        for (Map<String, Object> agreement : agreements) {
            String code = str(agreement.get("code"));
            if (code.isEmpty()) {
                continue;
            }
            boolean required = asInt(agreement.get("required"), 0) == 1;
            String currentVersion = str(agreement.get("versionNo"));
            Map<String, Object> hit = null;
            for (Map<String, Object> item : agreed) {
                if (code.equals(str(item.get("code")))) {
                    hit = item;
                    break;
                }
            }
            if (hit == null) {
                if (required && !currentVersion.isEmpty() && requireAllRequired) {
                    missing.add(str(agreement.get("name")).isEmpty() ? code : str(agreement.get("name")));
                } else if (required && !currentVersion.isEmpty()) {
                    missing.add(str(agreement.get("name")).isEmpty() ? code : str(agreement.get("name")));
                }
                continue;
            }
            if (currentVersion.isEmpty()) {
                continue;
            }
            if (!currentVersion.equals(str(hit.get("versionNo")))) {
                return ValidateResult.versionExpired(code, str(agreement.get("name")), currentVersion);
            }
            Map<String, Object> consent = new LinkedHashMap<>(8);
            consent.put("code", code);
            consent.put("agreementId", agreement.get("id"));
            consent.put("agreementName", agreement.get("name"));
            consent.put("versionId", agreement.get("versionId"));
            consent.put("versionNo", currentVersion);
            consents.add(consent);
        }
        if (!missing.isEmpty()) {
            return ValidateResult.missing(missing);
        }
        ValidateResult result = new ValidateResult();
        result.setOk(true);
        result.setConsentsJson(consents.isEmpty() ? "" : JSON.toJSONString(consents));
        return result;
    }

    /**
     * 查询用户已有效的同意版本：协议编码 → 版本号。
     *
     * @return 同意映射；用户确实从未同意时为空 Map；
     *         <b>userms 不可用或响应异常时返回 {@code null}</b>（调用方不得当成「从未同意」）
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> listAgreedVersions(String userId) {
        Map<String, String> agreed = new HashMap<>(8);
        try {
            Map<String, Object> param = new HashMap<>(2);
            param.put("userId", userId);
            ResponseResult result = userServiceFeign.agreementConsentListByUser(param);
            if (result == null || result.getCode() != CommonStatusEnum.SUCCESS.getCode()
                    || !(result.getData() instanceof List)) {
                log.warn("查询用户同意记录失败或响应异常，userId={}, code={}",
                        userId, result == null ? null : result.getCode());
                return null;
            }
            for (Object item : (List<Object>) result.getData()) {
                if (!(item instanceof Map)) {
                    continue;
                }
                Map<String, Object> row = (Map<String, Object>) item;
                // Feign/Jackson 一般为 camelCase；兼容偶发 snake_case，避免已同意仍被判 pending
                String code = firstNonBlank(row.get("agreementCode"), row.get("agreement_code"));
                String version = firstNonBlank(row.get("versionNo"), row.get("version_no"));
                if (!code.isEmpty() && !version.isEmpty()) {
                    agreed.put(code, version);
                }
            }
            return agreed;
        } catch (Exception e) {
            log.warn("查询用户同意记录失败，userId={}, err={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 记录同意行为（注册后置 / 重新同意）。
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param consents 同意条目
     * @param ip       客户端IP
     * @param ua       User-Agent
     * @param source   来源端
     * @param scene    场景
     * @return 写入条数；失败返回 -1
     */
    @SuppressWarnings("unchecked")
    public int recordConsents(String userId, String username, List<Map<String, Object>> consents,
                              String ip, String ua, String source, String scene) {
        if (consents == null || consents.isEmpty()) {
            return 0;
        }
        try {
            Map<String, Object> param = new HashMap<>(16);
            param.put("userId", userId);
            param.put("username", username);
            param.put("consents", consents);
            param.put("ip", ip);
            param.put("ua", ua);
            param.put("source", source);
            param.put("scene", scene);
            ResponseResult result = userServiceFeign.agreementConsentRecord(param);
            if (result == null || result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
                return -1;
            }
            // 站内记录已落库：异步把同一份签署凭据回传平台留痕。
            // 回传是副本，失败既不改写返回值也不抛异常——合规主记录在站内，
            // 平台侧缺一条不应让「同意协议」这个动作失败。
            if (platformConsentReporter != null) {
                platformConsentReporter.reportAsync(userId, username, consents, scene, ip, ua, source);
            }
            return asInt(result.getData(), 0);
        } catch (Exception e) {
            log.error("记录协议同意行为失败，userId={}", userId, e);
            return -1;
        }
    }

    /**
     * 记录同意行为（按场景对客户端提交的勾选做一次完整校验后落库）。
     *
     * @param userId           用户ID
     * @param username         用户名
     * @param clientAgreements 客户端勾选协议 [{code, versionNo}]
     * @param ip               客户端IP
     * @param ua               User-Agent
     * @param source           来源端
     * @param scene            校验与落库共用场景（register / reagree）
     * @return 校验结果；校验通过且落库成功时 ok=true
     */
    public ValidateResult validateAndRecord(String userId, String username, Object clientAgreements,
                                            String ip, String ua, String source, String scene) {
        return validateAndRecord(userId, username, clientAgreements, ip, ua, source, scene, scene);
    }

    /**
     * 记录同意行为（校验场景与落库场景分离）。
     *
     * <p><b>为什么拆成两个场景：</b>协议定义表里的 scene 只有 register（见
     * scripts/sql/init/21-agreement-consent.sql），补签（reagree）时待同意清单按
     * register 场景计算，校验也必须按 register 查协议与生效版本；若按 "reagree"
     * 场景查会得到空清单，校验「通过」但 consents 为空，接口返回成功而
     * <b>同意记录一条不写</b>（2026-09-18 修复的生产 bug）。落库场景仍记 reagree，
     * 便于在同意记录里区分「注册同意」与「版本更新补签」。</p>
     *
     * @param validateScene 校验用的协议场景（必须与协议定义表里的 scene 一致，如 register）
     * @param recordScene   落库时记录的 scene
     */
    public ValidateResult validateAndRecord(String userId, String username, Object clientAgreements,
                                            String ip, String ua, String source,
                                            String validateScene, String recordScene) {
        // 补签弹窗只展示待同意项，不能再要求用户提交已同意且未升版的协议。
        // 注册仍校验完整目录；同意记录只保存本次明确勾选的条目。
        ValidateResult validate;
        if (SCENE_REAGREE.equals(recordScene)) {
            List<Map<String, Object>> pending = listPendingAgreements(userId, validateScene);
            if (pending == null) {
                ValidateResult failed = new ValidateResult();
                failed.setOk(false);
                failed.setMessage("协议服务暂时不可用，请稍后重试");
                return failed;
            }
            validate = validateAgreementCatalog(clientAgreements, pending);
            if (!validate.isOk()) {
                return validate;
            }
            // 客户端提交了勾选，但待同意清单已空：幂等成功（两次点击 / 多端已签）。
            // 若清单仍有项却 consents 为空，属于校验异常，禁止假成功。
            if (validate.getConsentsJson().isEmpty()) {
                if (!normalizeAgreements(clientAgreements).isEmpty() && !pending.isEmpty()) {
                    ValidateResult failed = new ValidateResult();
                    failed.setOk(false);
                    failed.setMessage("协议确认失败，请刷新页面后重试");
                    return failed;
                }
                return validate;
            }
        } else {
            validate = validateAgreements(clientAgreements, validateScene);
            if (!validate.isOk()) {
                return validate;
            }
            if (validate.getConsentsJson().isEmpty()) {
                return validate;
            }
        }
        List<Map<String, Object>> consents = normalizeAgreements(
                JSON.parseArray(validate.getConsentsJson()));
        int count = recordConsents(userId, username, consents, ip, ua, source, recordScene);
        if (count <= 0) {
            ValidateResult failed = new ValidateResult();
            failed.setOk(false);
            failed.setMessage("同意记录保存失败，请稍后重试");
            return failed;
        }
        return validate;
    }

    /**
     * 归一化协议勾选入参：兼容 List<Map> 与 JSON 字符串
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> normalizeAgreements(Object raw) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (raw == null) {
            return result;
        }
        if (raw instanceof List) {
            for (Object item : (List<Object>) raw) {
                if (item instanceof Map) {
                    result.add((Map<String, Object>) item);
                }
            }
            return result;
        }
        String text = String.valueOf(raw).trim();
        if (!text.startsWith("[")) {
            return result;
        }
        try {
            com.alibaba.fastjson2.JSONArray array = JSON.parseArray(text);
            for (int i = 0; i < array.size(); i++) {
                com.alibaba.fastjson2.JSONObject obj = array.getJSONObject(i);
                if (obj != null) {
                    result.add(obj);
                }
            }
        } catch (Exception e) {
            log.warn("解析协议勾选入参失败：{}", e.getMessage());
        }
        return result;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String firstNonBlank(Object primary, Object fallback) {
        String first = str(primary);
        return first.isEmpty() ? str(fallback) : first;
    }

    private int asInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 协议校验结果
     */
    public static class ValidateResult {

        /** 是否通过 */
        private boolean ok;

        /** 未勾选的必选协议名称 */
        private List<String> missing = new ArrayList<>();

        /** 协议已更新（客户端版本号过期） */
        private boolean versionExpired;

        /** 过期的协议编码 */
        private String expiredCode;

        /** 过期协议名称 */
        private String expiredName;

        /** 最新版本号 */
        private String latestVersion;

        /** 错误提示 */
        private String message;

        /** 用于落库的同意条目 JSON */
        private String consentsJson = "";

        static ValidateResult degraded() {
            // 依赖不可用：放行但不记录（ok=true 且 consentsJson 为空）
            ValidateResult result = new ValidateResult();
            result.setOk(true);
            return result;
        }

        static ValidateResult missing(List<String> missing) {
            ValidateResult result = new ValidateResult();
            result.setOk(false);
            result.setMissing(missing);
            result.setMessage("请勾选并同意以下协议后再提交：" + String.join("、", missing));
            return result;
        }

        static ValidateResult versionExpired(String code, String name, String latestVersion) {
            ValidateResult result = new ValidateResult();
            result.setOk(false);
            result.setVersionExpired(true);
            result.setExpiredCode(code);
            result.setExpiredName(name);
            result.setLatestVersion(latestVersion);
            result.setMessage("《" + name + "》已更新，请刷新页面重新阅读并同意");
            return result;
        }

        public boolean isOk() {
            return ok;
        }

        public void setOk(boolean ok) {
            this.ok = ok;
        }

        public List<String> getMissing() {
            return missing;
        }

        public void setMissing(List<String> missing) {
            this.missing = missing;
        }

        public boolean isVersionExpired() {
            return versionExpired;
        }

        public void setVersionExpired(boolean versionExpired) {
            this.versionExpired = versionExpired;
        }

        public String getExpiredCode() {
            return expiredCode;
        }

        public void setExpiredCode(String expiredCode) {
            this.expiredCode = expiredCode;
        }

        public String getExpiredName() {
            return expiredName;
        }

        public void setExpiredName(String expiredName) {
            this.expiredName = expiredName;
        }

        public String getLatestVersion() {
            return latestVersion;
        }

        public void setLatestVersion(String latestVersion) {
            this.latestVersion = latestVersion;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getConsentsJson() {
            return consentsJson;
        }

        public void setConsentsJson(String consentsJson) {
            this.consentsJson = consentsJson;
        }

    }
}
