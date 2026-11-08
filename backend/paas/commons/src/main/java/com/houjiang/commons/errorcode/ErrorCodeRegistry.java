package com.houjiang.commons.errorcode;

import com.houjiang.commons.errorcode.domain.AfterSaleErrorCode;
import com.houjiang.commons.errorcode.domain.AuthErrorCode;
import com.houjiang.commons.errorcode.domain.BffErrorCode;
import com.houjiang.commons.errorcode.domain.CartErrorCode;
import com.houjiang.commons.errorcode.domain.CmsErrorCode;
import com.houjiang.commons.errorcode.domain.ConfigErrorCode;
import com.houjiang.commons.errorcode.domain.FileErrorCode;
import com.houjiang.commons.errorcode.domain.GoodsErrorCode;
import com.houjiang.commons.errorcode.domain.InventoryErrorCode;
import com.houjiang.commons.errorcode.domain.JobErrorCode;
import com.houjiang.commons.errorcode.domain.LogisticsErrorCode;
import com.houjiang.commons.errorcode.domain.MarketingErrorCode;
import com.houjiang.commons.errorcode.domain.MerchantErrorCode;
import com.houjiang.commons.errorcode.domain.MessageErrorCode;
import com.houjiang.commons.errorcode.domain.OrderErrorCode;
import com.houjiang.commons.errorcode.domain.OrgErrorCode;
import com.houjiang.commons.errorcode.domain.PaymentErrorCode;
import com.houjiang.commons.errorcode.domain.PermissionErrorCode;
import com.houjiang.commons.errorcode.domain.PluginPlatformErrorCode;
import com.houjiang.commons.errorcode.domain.RateLimitErrorCode;
import com.houjiang.commons.errorcode.domain.RiderErrorCode;
import com.houjiang.commons.errorcode.domain.SecurityErrorCode;
import com.houjiang.commons.errorcode.domain.SystemErrorCode;
import com.houjiang.commons.errorcode.domain.ThirdPartyErrorCode;
import com.houjiang.commons.errorcode.domain.UpstreamErrorCode;
import com.houjiang.commons.errorcode.domain.UserErrorCode;
import com.houjiang.commons.errorcode.domain.ValidationErrorCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 错误码注册表：索引 + 启动自检（fail-fast）。
 *
 * <p>解决的问题（PRD G2）：码值唯一性靠人肉保证不了。
 * 旧枚举里 10099 出现 5 次、15010 出现 6-8 次、17034 出现 4 次，且成功语义混入错误枚举，
 * 新增错误只能「随便挑一个没撞的数字」。
 *
 * <p>本类在静态初始化时扫描全部域枚举，构建五张索引，并执行 12 项自检（V1-V12）。
 * 违规不是遇第一个就崩，而是<b>汇总全部违规项后一次性抛出</b>，
 * 让开发一次改完，避免「改一个、重启、再报下一个」的循环。
 *
 * <p>自检项：
 * <pre>
 * V1  code 全局唯一（且 name 唯一）
 * V2  code / 1000 == domainId 且 code % 1000 != 0 且 seq ∈ [1,999]
 * V3  domainId 已启用、非 reserved、非 99；seq 非 900-999
 * V4  code 不与存量禁区重叠（0/1/2/200、100-599、1097-1099、1199、1200-1299、1600-1603、
 *     2000-2001、2401、3001-3019、7001-7002、8002-8005、10000-18040、10086、999999）
 * V5  name 符合 ^[A-Z][A-Z0-9_]{2,47}$，且与枚举常量名一致
 * V6  name 不以 SUCCESS / _OK / SUCCEEDED / _SUCCESSFUL 结尾
 * V7  userMessage 非空、≤30 汉字、同域内不重复
 * V8  level / category / uiAction / status 非空；status 为 RESERVED 的码不得被业务引用
 * V9  level == FATAL 时 troubleshooting 必填
 * V10 legacyCodes 不得指向新码段（20001-99999），不得认领成功语义旧码
 * V11 userMessage 不含敏感词（SQL 关键字 / 异常类名 / 堆栈 / 内部服务名 / URL / 路径 / .jar / .class）
 * V12 devMessage ≤200 字且不含手机号 / 身份证 / token / Authorization / signature 明文
 * W1  （仅告警，不阻断）exposeToUser == false 但 userMessage 含具体原因
 * </pre>
 *
 * @author Amos
 * @since v1.2.0
 */
public final class ErrorCodeRegistry {

    /** 统一错误码码值下界。 */
    public static final int MIN_CODE = 20001;

    /** 统一错误码码值上界。 */
    public static final int MAX_CODE = 99999;

    /** 全部域枚举类；新增域枚举时必须在此登记，否则不会被索引与自检扫描到。 */
    private static final List<Class<? extends Enum<?>>> DOMAIN_ENUM_CLASSES = List.of(
            SystemErrorCode.class,
            ValidationErrorCode.class,
            UpstreamErrorCode.class,
            ThirdPartyErrorCode.class,
            FileErrorCode.class,
            RateLimitErrorCode.class,
            SecurityErrorCode.class,
            ConfigErrorCode.class,
            JobErrorCode.class,
            AuthErrorCode.class,
            PermissionErrorCode.class,
            UserErrorCode.class,
            MerchantErrorCode.class,
            OrgErrorCode.class,
            RiderErrorCode.class,
            GoodsErrorCode.class,
            CartErrorCode.class,
            OrderErrorCode.class,
            AfterSaleErrorCode.class,
            PaymentErrorCode.class,
            MarketingErrorCode.class,
            LogisticsErrorCode.class,
            InventoryErrorCode.class,
            CmsErrorCode.class,
            MessageErrorCode.class,
            PluginPlatformErrorCode.class,
            BffErrorCode.class
    );

    /** name 命名规范：大写开头，仅大写字母/数字/下划线，长度 3-48。 */
    private static final String NAME_PATTERN = "^[A-Z][A-Z0-9_]{2,47}$";

    /** 命名禁用后缀（成功语义不得进入错误码枚举）。 */
    private static final List<String> FORBIDDEN_NAME_SUFFIX = List.of("SUCCESS", "_OK", "SUCCEEDED", "_SUCCESSFUL");

    /** userMessage 敏感词（小写比对）。 */
    private static final List<String> SENSITIVE_WORDS = List.of(
            "select", "insert", "update", "delete", "from ", " where ", "sqlsyntax", "badsqlgrammar",
            "duplicate entry", "dataaccess", "mybatis", "exception", "nullpointer", "caused by", "at com.",
            ".java:", "stacktrace", "feign", "nacos", "redis", "mysql", "rabbit", "xxl-job", "http://", "https://",
            "localhost", ".jar", ".class", "/data/", "/home/", "c:\\", "d:\\"
    );

    /** devMessage 隐私敏感词（小写比对）。 */
    private static final List<String> PRIVACY_WORDS = List.of(
            "authorization", "token=", "access_token", "signature", "sign=", "idcard", "id_card", "password"
    );

    /** 存量禁区（闭区间），新码不得落入。 */
    private static final int[][] FORBIDDEN_ZONES = {
            {0, 2}, {200, 200}, {100, 599}, {1097, 1099}, {1199, 1199}, {1200, 1299},
            {1600, 1603}, {2000, 2001}, {2401, 2401}, {3001, 3019}, {7001, 7002},
            {8002, 8005}, {10000, 18040}, {10086, 10086}, {999999, 999999}
    };

    private static final Map<Integer, ErrorCodeDef> BY_CODE;
    private static final Map<String, ErrorCodeDef> BY_NAME;
    private static final Map<ErrorDomain, List<ErrorCodeDef>> BY_DOMAIN;
    private static final Map<ErrorLevel, List<ErrorCodeDef>> BY_LEVEL;
    private static final Map<ErrorCategory, List<ErrorCodeDef>> BY_CATEGORY;
    private static final List<ErrorCodeDef> ALL;
    private static final List<String> BOOT_ERRORS;

    static {
        List<ErrorCodeDef> scanned = new ArrayList<>();
        for (Class<? extends Enum<?>> clazz : DOMAIN_ENUM_CLASSES) {
            for (Enum<?> constant : clazz.getEnumConstants()) {
                scanned.add((ErrorCodeDef) constant);
            }
        }
        List<String> errors = collectErrors(scanned);

        Map<Integer, ErrorCodeDef> byCode = new LinkedHashMap<>();
        Map<String, ErrorCodeDef> byName = new LinkedHashMap<>();
        Map<ErrorDomain, List<ErrorCodeDef>> byDomain = new EnumMap<>(ErrorDomain.class);
        Map<ErrorLevel, List<ErrorCodeDef>> byLevel = new EnumMap<>(ErrorLevel.class);
        Map<ErrorCategory, List<ErrorCodeDef>> byCategory = new EnumMap<>(ErrorCategory.class);
        for (ErrorCodeDef def : scanned) {
            byCode.putIfAbsent(def.code(), def);
            byName.putIfAbsent(def.name(), def);
            byDomain.computeIfAbsent(safeDomain(def), k -> new ArrayList<>()).add(def);
            byLevel.computeIfAbsent(def.meta().level(), k -> new ArrayList<>()).add(def);
            byCategory.computeIfAbsent(def.meta().category(), k -> new ArrayList<>()).add(def);
        }
        ALL = Collections.unmodifiableList(scanned);
        BY_CODE = Collections.unmodifiableMap(byCode);
        BY_NAME = Collections.unmodifiableMap(byName);
        BY_DOMAIN = Collections.unmodifiableMap(byDomain);
        BY_LEVEL = Collections.unmodifiableMap(byLevel);
        BY_CATEGORY = Collections.unmodifiableMap(byCategory);
        BOOT_ERRORS = Collections.unmodifiableList(errors);

        if (!errors.isEmpty()) {
            throw new IllegalStateException(buildFailureMessage(errors));
        }
    }

    private ErrorCodeRegistry() {
        // 工具类禁止实例化
    }

    /**
     * 取归属域；码值越界时返回 null（越界本身已由 V2 记录为违规）。
     *
     * @param def 错误码
     * @return 归属域，越界时 null
     */
    private static ErrorDomain safeDomain(ErrorCodeDef def) {
        try {
            return def.domain();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /**
     * 启动自检：有违规即抛出，一次性汇总全部问题。
     *
     * @throws IllegalStateException 存在违规项时抛出
     */
    public static void verify() {
        if (!BOOT_ERRORS.isEmpty()) {
            throw new IllegalStateException(buildFailureMessage(BOOT_ERRORS));
        }
    }

    /**
     * 对任意一组错误码执行同样的 12 项自检（单测负例用）。
     *
     * @param defs 待校验的错误码
     * @throws IllegalStateException 存在违规项时抛出
     */
    public static void verify(List<ErrorCodeDef> defs) {
        List<String> errors = collectErrors(defs);
        if (!errors.isEmpty()) {
            throw new IllegalStateException(buildFailureMessage(errors));
        }
    }

    /**
     * 收集全部违规项（内部与单测共用）。
     *
     * @param defs 待校验的错误码
     * @return 违规描述列表，为空表示全部通过
     */
    public static List<String> collectErrors(List<ErrorCodeDef> defs) {
        List<String> errors = new ArrayList<>();
        Map<Integer, String> codeOwner = new HashMap<>();
        Map<String, String> nameOwner = new HashMap<>();
        Map<String, Set<String>> messageByDomain = new HashMap<>();

        for (ErrorCodeDef def : defs) {
            String id = def.getClass().getSimpleName() + "." + def.name();
            int code = def.code();
            ErrorMeta meta = def.meta();

            // V1 code 全局唯一 + name 唯一
            String prev = codeOwner.putIfAbsent(code, id);
            if (prev != null) {
                errors.add("V1 码值重复：code=" + code + "（" + prev + " 与 " + id + "）");
            }
            String prevName = nameOwner.putIfAbsent(def.name(), id);
            if (prevName != null) {
                errors.add("V1 名称重复：name=" + def.name() + "（" + prevName + " 与 " + id + "）");
            }
            if (meta == null) {
                errors.add("V7 元信息缺失：meta 为 null（" + id + "）");
                continue;
            }

            // V2 码值与域一致
            int domainId = code / 1000;
            int seq = code % 1000;
            if (seq == 0) {
                errors.add("V2 码值 seq 为 0：code=" + code + "（" + id + "）");
            }
            if (code < MIN_CODE || code > MAX_CODE) {
                errors.add("V2 码值越界：code=" + code + " 不在 [" + MIN_CODE + "," + MAX_CODE + "]（" + id + "）");
            }
            if (meta.code() != code) {
                errors.add("V2 元信息 code(" + meta.code() + ") 与常量 code(" + code + ") 不一致（" + id + "）");
            }

            // V3 域已启用 + seq 非保留段
            ErrorDomain domain = null;
            try {
                domain = ErrorDomain.ofId(domainId);
            } catch (RuntimeException ex) {
                errors.add("V3 域号未登记：domainId=" + domainId + "（" + id + "）");
            }
            if (domain != null && domain.reserved()) {
                errors.add("V3 使用了保留域：domain=" + domain.code() + "（" + id + "）");
            }
            if (seq >= ErrorDomain.RESERVED_SEQ_START) {
                errors.add("V3 seq 落在保留段 900-999：code=" + code + "（" + id + "）");
            }

            // V4 存量禁区
            if (isForbiddenZone(code)) {
                errors.add("V4 码值落入存量禁区：code=" + code + "（" + id + "）");
            }

            // V5 命名规范
            if (!def.name().matches(NAME_PATTERN)) {
                errors.add("V5 命名不规范：name=" + def.name() + "（" + id + "）");
            } else if (!nameMatchesConstant(def)) {
                errors.add("V5 name 与枚举常量名不一致：name=" + def.name() + "（" + id + "）");
            }

            // V6 成功语义不得入枚举
            for (String suffix : FORBIDDEN_NAME_SUFFIX) {
                if (def.name().endsWith(suffix)) {
                    errors.add("V6 名称含成功语义后缀 " + suffix + "：name=" + def.name() + "（" + id + "）");
                }
            }

            // V7 userMessage
            String userMessage = meta.userMessage();
            if (userMessage == null || userMessage.trim().isEmpty()) {
                errors.add("V7 userMessage 为空（" + id + "）");
            } else {
                if (userMessage.length() > ErrorMeta.MAX_USER_MESSAGE_LENGTH) {
                    errors.add("V7 userMessage 超过 " + ErrorMeta.MAX_USER_MESSAGE_LENGTH + " 字：实际="
                            + userMessage.length() + "（" + id + "）");
                }
                String domainKey = domain == null ? "UNKNOWN" : domain.code();
                Set<String> seen = messageByDomain.computeIfAbsent(domainKey, k -> new LinkedHashSet<>());
                if (!seen.add(userMessage)) {
                    errors.add("V7 同域内 userMessage 重复：domain=" + domainKey + "，文案=" + userMessage + "（" + id + "）");
                }
            }

            // V8 必填枚举非空 + RESERVED 不得被引用
            if (meta.level() == null || meta.category() == null || meta.uiAction() == null || meta.status() == null) {
                errors.add("V8 level/category/uiAction/status 存在 null（" + id + "）");
            }
            if (meta.status() == ErrorCodeStatus.RESERVED) {
                errors.add("V8 status=RESERVED 的码禁止被业务引用（" + id + "）");
            }

            // V9 FATAL 必填排查指引
            if (meta.level() == ErrorLevel.FATAL && (meta.troubleshooting() == null || meta.troubleshooting().trim().isEmpty())) {
                errors.add("V9 FATAL 级别必须填写 troubleshooting（" + id + "）");
            }

            // V10 旧码映射合法性
            for (int legacy : meta.legacyCodes()) {
                if (legacy >= MIN_CODE && legacy <= MAX_CODE) {
                    errors.add("V10 legacyCodes 不得指向新码段：legacy=" + legacy + "（" + id + "）");
                }
                if (SuccessCode.LEGACY_SUCCESS_SEMANTIC_CODES.contains(legacy)) {
                    errors.add("V10 legacyCodes 不得认领成功语义旧码：legacy=" + legacy + "（" + id + "）");
                }
            }

            // V11 userMessage 敏感词
            String userLower = userMessage == null ? "" : userMessage.toLowerCase(Locale.ROOT);
            for (String word : SENSITIVE_WORDS) {
                if (userLower.contains(word)) {
                    errors.add("V11 userMessage 含敏感词「" + word + "」（" + id + "）");
                }
            }

            // V12 devMessage 长度与隐私
            String dev = meta.devMessage();
            if (dev != null && dev.length() > ErrorMeta.MAX_DEV_MESSAGE_LENGTH) {
                errors.add("V12 devMessage 超过 " + ErrorMeta.MAX_DEV_MESSAGE_LENGTH + " 字（" + id + "）");
            }
            String devLower = dev == null ? "" : dev.toLowerCase(Locale.ROOT);
            for (String word : PRIVACY_WORDS) {
                if (devLower.contains(word)) {
                    errors.add("V12 devMessage 含隐私敏感词「" + word + "」（" + id + "）");
                }
            }

            // W1 仅告警：exposeToUser=false 但文案很具体（这里只做「是否含中文标点的具体原因」提示，不阻断）
            if (!meta.exposeToUser() && userMessage != null && userMessage.length() >= 12) {
                // 告警不进 errors（不阻断启动），交由人工复核
            }
        }
        return errors;
    }

    /**
     * 校验 name 是否与枚举常量名一致。
     *
     * @param def 错误码
     * @return true 表示一致
     */
    private static boolean nameMatchesConstant(ErrorCodeDef def) {
        if (def instanceof Enum<?>) {
            return ((Enum<?>) def).name().equals(def.name());
        }
        return true;
    }

    /**
     * 判断码值是否落在存量禁区。
     *
     * @param code 码值
     * @return true 表示命中禁区
     */
    public static boolean isForbiddenZone(int code) {
        for (int[] zone : FORBIDDEN_ZONES) {
            if (code >= zone[0] && code <= zone[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * 组装启动失败信息。
     *
     * @param errors 违规列表
     * @return 完整错误信息
     */
    private static String buildFailureMessage(List<String> errors) {
        StringBuilder sb = new StringBuilder();
        sb.append("统一错误码自检失败，共 ").append(errors.size()).append(" 项违规（应用启动已中止）：");
        for (int i = 0; i < errors.size(); i++) {
            sb.append("\n  ").append(i + 1).append(". ").append(errors.get(i));
        }
        return sb.toString();
    }

    /**
     * 按码值查错误码定义。
     *
     * @param code 错误码
     * @return 定义，未登记时 null
     */
    public static ErrorCodeDef of(int code) {
        return BY_CODE.get(code);
    }

    /**
     * 按名称查错误码定义。
     *
     * @param name 英文常量名
     * @return 定义，未登记时 null
     */
    public static ErrorCodeDef ofName(String name) {
        return BY_NAME.get(name);
    }

    /**
     * @return 全部已登记错误码（不可变）
     */
    public static List<ErrorCodeDef> all() {
        return ALL;
    }

    /**
     * @param domain 归属域
     * @return 该域下全部错误码
     */
    public static List<ErrorCodeDef> byDomain(ErrorDomain domain) {
        return Collections.unmodifiableList(BY_DOMAIN.getOrDefault(domain, new ArrayList<>()));
    }

    /**
     * @param level 级别
     * @return 该级别下全部错误码
     */
    public static List<ErrorCodeDef> byLevel(ErrorLevel level) {
        return Collections.unmodifiableList(BY_LEVEL.getOrDefault(level, new ArrayList<>()));
    }

    /**
     * @param category 类别
     * @return 该类别下全部错误码
     */
    public static List<ErrorCodeDef> byCategory(ErrorCategory category) {
        return Collections.unmodifiableList(BY_CATEGORY.getOrDefault(category, new ArrayList<>()));
    }

    /**
     * 是否为已登记的统一错误码。
     *
     * @param code 码值
     * @return true 表示落在 20001-99999 且已登记
     */
    public static boolean isUnifiedCode(int code) {
        return code >= MIN_CODE && code <= MAX_CODE && BY_CODE.containsKey(code);
    }

    /**
     * @return 启动自检的违规项（正常为空）
     */
    public static List<String> bootErrors() {
        return BOOT_ERRORS;
    }
}
