package com.houjiang.commons.errorcode;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 成功码集合 —— 与错误码严格分离。
 *
 * <p>解决的问题（PRD 痛点 2）：成功/失败码值语义在两端反转。
 * 后端 {@code CommonStatusEnum} 定义 SUCCESS=1 / FAIL=0，
 * 但 {@code frontend/admin} 判 <code>code===200 || code===1</code> 为成功，
 * {@code frontend/cmsadmin} 却把 0 以及 5 个错误码（10060/11007/14052/10047/16012）
 * 塞进 VALID_CODES 白名单当成功放行 —— 同一个 code=0 在 admin 是报错弹窗、在 cmsadmin 是静默放行。
 *
 * <p>本类把「成功判定」收敛成三个方法，前端映射层统一调用，不再各端自己写魔法数字。
 * <b>成功语义禁止进入错误码枚举</b>（自检 V6 拦截），新码段 20001-99999 内永不出现成功码。
 *
 * @author Amos
 * @since v1.2.0
 */
public final class SuccessCode {

    /**
     * 唯一规范成功码，对齐 {@code CommonStatusEnum.SUCCESS=1}
     * 与多数前端（dcenter / imchat / cms / ai 判 1）。
     */
    public static final int CANONICAL = 1;

    /**
     * 历史成功兼容集：对齐 {@code frontend/admin} 的 <code>code===200 || code===1</code>。
     * P1 之后收敛为只剩 {@link #CANONICAL}。
     */
    public static final Set<Integer> COMPAT_SUCCESS;

    /**
     * 失败码，对齐 {@code CommonStatusEnum.FAIL=0} 与 {@code frontend/admin}。
     *
     * <p>注意：cmsadmin 判 0 放行是历史 bug，靠 {@code compatZeroAsSuccess} 开关兼容，
     * 开关关闭后 0 一律按失败处理。
     */
    public static final int FAIL = 0;

    /**
     * 旧码中具有「成功语义」的码值，禁止被新码认领为 legacyCodes（自检 V10）。
     *
     * <p>来源：BizCodeEnume 的 OPERATION_SUCCESSFUL(1)、REGISTRATION_SUCCESS(1)、
     * LOGIN_SUCCEEDED(11000)、EXIT_SUCCESSFULLY(11001)、SENDING_SUCCEEDED(10004)、
     * SMS_SUCCESS(10006) 等数十项成功语义混入错误枚举的历史遗留。
     */
    public static final Set<Integer> LEGACY_SUCCESS_SEMANTIC_CODES;

    static {
        Set<Integer> compat = new LinkedHashSet<>();
        compat.add(CANONICAL);
        compat.add(200);
        COMPAT_SUCCESS = Collections.unmodifiableSet(compat);

        Set<Integer> legacySuccess = new LinkedHashSet<>();
        legacySuccess.add(0);
        legacySuccess.add(1);
        legacySuccess.add(200);
        legacySuccess.add(10004);
        legacySuccess.add(10006);
        legacySuccess.add(11000);
        legacySuccess.add(11001);
        LEGACY_SUCCESS_SEMANTIC_CODES = Collections.unmodifiableSet(legacySuccess);
    }

    private SuccessCode() {
        // 工具类禁止实例化
    }

    /**
     * 是否为唯一规范成功码。
     *
     * @param code 响应码
     * @return true 表示 code == 1
     */
    public static boolean isCanonicalSuccess(int code) {
        return code == CANONICAL;
    }

    /**
     * 是否落在历史兼容成功集 {1, 200} 内。
     *
     * @param code 响应码
     * @return true 表示兼容期内视为成功
     */
    public static boolean isCompatSuccess(int code) {
        return COMPAT_SUCCESS.contains(code);
    }

    /**
     * 统一成功判定入口（默认走兼容集）。
     *
     * @param code 响应码
     * @return true 表示成功
     */
    public static boolean isSuccess(int code) {
        return isCompatSuccess(code);
    }

    /**
     * 是否为明确失败码。
     *
     * @param code 响应码
     * @return true 表示 code == 0
     */
    public static boolean isFail(int code) {
        return code == FAIL;
    }
}
