package com.houjiang.commons.errorcode;

import com.houjiang.commons.errorcode.domain.MerchantErrorCode;
import com.houjiang.commons.errorcode.domain.PermissionErrorCode;
import com.houjiang.commons.errorcode.domain.SystemErrorCode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 统一错误码自检单测。
 *
 * <p>解决的问题（PRD G2）：码值唯一性靠人肉保证不了。本单测锁住两类契约：
 * <ol>
 *   <li>正例：全量码表自检通过、码值域判定正确、5 个 cmsadmin 白名单旧码归属正确。</li>
 *   <li>负例：重复码（V1）、越界/保留域（V3）、同域文案重复（V7）、成功语义命名（V6）
 *       四类违规必须让 {@link ErrorCodeRegistry#verify(List)} 抛 {@link IllegalStateException}，
 *       即应用启动失败（fail-fast）。</li>
 * </ol>
 *
 * @author Amos
 * @since v1.2.0
 */
class ErrorCodeRegistryTest {

    /**
     * 构造一个不在任何域枚举里的合成错误码，用于负例校验。
     *
     * @param code 码值
     * @param name 名称
     * @param meta 元信息
     * @return 错误码定义
     */
    private static ErrorCodeDef def(int code, String name, ErrorMeta meta) {
        return new ErrorCodeDef() {
            @Override
            public int code() {
                return code;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public ErrorMeta meta() {
                return meta;
            }
        };
    }

    /**
     * @param code        码值
     * @param name        名称
     * @param userMessage 用户文案
     * @return 元信息
     */
    private static ErrorMeta meta(int code, String name, String userMessage) {
        return ErrorMeta.builder(code, userMessage).devMessage("单测用元信息：" + name).build();
    }

    @Test
    void 全量码表自检通过且规模达标() {
        ErrorCodes.verify();
        assertTrue(ErrorCodeRegistry.bootErrors().isEmpty(), "启动自检不应有违规项");
        assertTrue(ErrorCodes.all().size() >= 90, "本轮登记的错误码应不少于 90 个");
    }

    @Test
    void 码值域判定正确() {
        assertTrue(ErrorCodes.isUnifiedCode(SystemErrorCode.ROUTE_NOT_FOUND.code()));
        assertFalse(ErrorCodes.isUnifiedCode(11009), "旧码不属于统一错误码段");
        assertFalse(ErrorCodes.isUnifiedCode(999999), "BizCodeEnume 的 End 占位码不属于统一错误码段");
        assertFalse(ErrorCodes.isUnifiedCode(0));
    }

    @Test
    void 未登记的码降级为未知错误且绝不返回null() {
        assertFalse(ErrorCodes.isUnifiedCode(79999));
        assertEquals(SystemErrorCode.UNKNOWN_ERROR.meta().userMessage(), ErrorCodes.userMessage(79999));
    }

    @Test
    void cmsadmin白名单5码的新归属正确() {
        assertEquals(20007, SystemErrorCode.SYSTEM_QUERY_RESULT_EMPTY.code());
        assertEquals(31005, PermissionErrorCode.PERMISSION_ACCESS_DENIED.code());
        assertEquals(33001, MerchantErrorCode.MERCHANT_AUDIT_INFO_NOT_FOUND.code());
        assertEquals(33002, MerchantErrorCode.MERCHANT_CONTRACT_TEMPLATE_NOT_FOUND.code());
        assertEquals(43001, ErrorCodes.ofName("AFTER_SALE_RECORD_NOT_FOUND").code());

        // 4 个「查询为空」码：INFO + EMPTY_STATE，保持放行、不弹 Toast
        assertEmptyState(20007);
        assertEmptyState(33001);
        assertEmptyState(33002);
        assertEmptyState(43001);

        // 11007 权限不足：真错误，停止放行，出 Toast
        assertEquals(ErrorCategory.PERMISSION, PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().category());
        assertEquals(UiAction.TOAST, PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().uiAction());
        assertEquals(ErrorLevel.WARN, PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().level());
    }

    /**
     * 断言码值是「查询为空」语义。
     *
     * @param code 码值
     */
    private static void assertEmptyState(int code) {
        ErrorMeta meta = ErrorCodes.metaOf(code);
        assertEquals(ErrorLevel.INFO, meta.level(), "查询为空应为 INFO，不是故障");
        assertEquals(UiAction.EMPTY_STATE, meta.uiAction(), "查询为空应出空状态，不弹 Toast");
        assertEquals(ErrorCategory.NOT_FOUND, meta.category());
    }

    @Test
    void 旧码反向索引可用且歧义码不产出一对一映射() {
        // EXACT：唯一认领者，可安全做 1:1 映射
        assertEquals(List.of(31005), LegacyCodeIndex.candidatesOf(11007));
        assertEquals(List.of(40802), LegacyCodeIndex.candidatesOf(15030), "15030 真身是超出最大配送距离");
        assertEquals(List.of(30801), LegacyCodeIndex.candidatesOf(10086), "10086 登录语义归 AUTH 域 30801");
        assertEquals(List.of(30803), LegacyCodeIndex.candidatesOf(10016), "10016 token 失效归 AUTH 域 30803");
        assertFalse(LegacyCodeIndex.isAmbiguous(10016), "10016 是单一认证语义，应可 1:1 映射");
        // 10012「系统异常」与 10016 同处未授权数组，但语义与认证无关。
        // 第二阶段重构后它被统一码（SYSTEM 域）认领，这是对的；真正要锁的是
        // 「它落到的新码绝不能在 AUTH 域」—— 否则一次普通系统异常仍会把用户踢到登录页。
        List<Integer> targets10012 = allTargetsOf(10012);
        assertFalse(targets10012.isEmpty(), "10012（SYSTEM_ERROR）应已被统一码认领");
        for (int code : targets10012) {
            assertNotEquals(ErrorDomain.AUTH, ErrorCodeRegistry.of(code).domain(),
                    "10012 是系统异常，认领它的新码不得落在 AUTH 域：code=" + code);
        }

        // 已按模块拆分认领的跨枚举冲突码：刻意保持 AMBIGUOUS，绝不产出 1:1 映射
        assertTrue(LegacyCodeIndex.isAmbiguous(11002), "11002 跨枚举冲突，必须保持歧义");
        assertTrue(LegacyCodeIndex.isAmbiguous(11006), "11006 跨枚举冲突，必须保持歧义");
        assertTrue(LegacyCodeIndex.candidatesOf(11002).isEmpty(), "歧义旧码不得产出 1:1 映射");
        assertTrue(LegacyCodeIndex.candidatesOf(11006).isEmpty(), "歧义旧码不得产出 1:1 映射");
        // 重构后「同码多义」是普遍现象（旧枚举 519 项里同一个码值被多个语义复用），
        // 因此歧义集不再是固定的两个；契约收紧为：11002 / 11006 必须在歧义集内。
        assertTrue(LegacyCodeIndex.ambiguousAll().keySet().containsAll(Set.of(11002, 11006)),
                "11002 / 11006 必须仍在歧义集中，不得被收敛成 1:1 映射");
    }

    /**
     * 取某个旧码的全部落点（EXACT 候选 + 歧义候选）。
     *
     * @param legacy 旧码
     * @return 认领它的全部统一码，未认领时为空
     */
    private static List<Integer> allTargetsOf(int legacy) {
        List<Integer> targets = new ArrayList<>(LegacyCodeIndex.candidatesOf(legacy));
        List<Integer> ambiguous = LegacyCodeIndex.ambiguousAll().get(legacy);
        if (ambiguous != null) {
            targets.addAll(ambiguous);
        }
        return targets;
    }

    /**
     * 锁住「生成的前端映射表不得出现重复 key」这条契约。
     *
     * <p>生成器写 {@code LEGACY_TO_NEW} 时若把歧义旧码也写进去，同一个 key 会出现两次，
     * JS 对象字面量里后写的覆盖先写的，语义被静默翻转 —— admin 端的 11006 因此被判成
     * 骑手语义 35010（「只能提现到本人账户」，VALIDATION，不跳登录），
     * 而 admin 端 11006 的真身是「用户未登录」（30802，AUTH，必须跳登录）。
     * 该 bug 的防线是：歧义旧码的 {@code candidatesOf} 必须为空 ——
     * 生成器只输出 {@code candidatesOf} 非空（即 EXACT）的旧码，因此两者天然不相交。
     */
    @Test
    void 歧义旧码与前端唯一映射表的键集合必须互不相交() {
        Map<Integer, List<Integer>> ambiguous = LegacyCodeIndex.ambiguousAll();
        assertFalse(ambiguous.isEmpty(), "11002 / 11006 必须仍保持刻意歧义");
        for (Map.Entry<Integer, List<Integer>> entry : ambiguous.entrySet()) {
            int legacy = entry.getKey();
            assertTrue(legacy >= 0, "旧码不应为负");
            assertTrue(entry.getValue().size() >= 2,
                    "歧义旧码 " + legacy + " 至少被 2 个新码认领，实际：" + entry.getValue());
            assertTrue(LegacyCodeIndex.candidatesOf(legacy).isEmpty(),
                    "歧义旧码 " + legacy + " 的候选必须为空，否则生成器会写出重复 key");
        }
        // 11006 的两个候选必须都在，缺一个就说明某一侧语义被漏登记
        assertTrue(LegacyCodeIndex.ambiguousAll().get(11006).containsAll(List.of(30802, 35010)),
                "11006 的候选应同时含主站 30802 与骑手 35010");
        assertTrue(LegacyCodeIndex.ambiguousAll().get(11002).containsAll(List.of(32001, 32801)),
                "11002 的候选应同时含主站 32001 与骑手 32801");
    }

    @Test
    void 游离码认领后码值与映射落点正确() {
        assertEquals(40801, ErrorCodes.ofName("GOODS_OUT_OF_STOCK").code(), "无货商品");
        assertEquals(40802, ErrorCodes.ofName("DELIVERY_DISTANCE_EXCEEDED").code(), "15030 真身");
        assertEquals(30801, ErrorCodes.ofName("LOGIN_FAILED").code(), "10086 登录态不可用");
        assertEquals(30802, ErrorCodes.ofName("USER_NOT_LOGGED_IN").code(), "11006 主站语义");
        assertEquals(32801, ErrorCodes.ofName("USER_UNDER_REVIEW").code(), "11002 骑手侧语义");
        assertEquals(32001, ErrorCodes.ofName("USER_NOT_FOUND").code(), "11002 主站语义");
        assertEquals(35010, ErrorCodes.ofName("RIDER_WITHDRAW_ACCOUNT_INVALID").code(), "11006 骑手语义");
        assertEquals(30803, ErrorCodes.ofName("TOKEN_INVALID").code(), "10016 token 失效");

        // 新码全部落在域内扩展槽 800-899
        for (int code : new int[]{40801, 40802, 30801, 30802, 30803, 32801}) {
            assertTrue(code % 1000 >= 800 && code % 1000 <= 899, "新码必须落在 800-899 扩展槽：code=" + code);
        }

        // 10016 是唯一被认领为「认证失效」的 token 失效码，且必须触发跳登录
        assertEquals(ErrorCategory.AUTH, ErrorCodes.metaOf(30803).category());
        assertEquals(UiAction.REDIRECT_LOGIN, ErrorCodes.metaOf(30803).uiAction());

        // 认领后总量与映射条数同步增长
        assertTrue(ErrorCodes.all().size() >= 95, "游离码认领后错误码数量应增加");
        assertTrue(LegacyCodeIndex.mappedLegacyCount() >= 20, "新增 15030 / 10086 / 10016 / 11002 / 11006 五条映射");
    }

    @Test
    void 负例_V1_重复码值必须启动失败() {
        ErrorCodeDef a = def(20090, "DUP_ONE", meta(20090, "DUP_ONE", "单测重复码甲"));
        ErrorCodeDef b = def(20090, "DUP_TWO", meta(20090, "DUP_TWO", "单测重复码乙"));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> ErrorCodeRegistry.verify(List.of(a, b)));
        assertTrue(ex.getMessage().contains("V1 码值重复"), ex.getMessage());
    }

    @Test
    void 负例_V3_保留域与保留seq必须启动失败() {
        ErrorCodeDef reservedDomain = def(99001, "SELFTEST_PROBE", meta(99001, "SELFTEST_PROBE", "自检域禁止业务使用"));
        IllegalStateException ex1 = assertThrows(IllegalStateException.class,
                () -> ErrorCodeRegistry.verify(List.of(reservedDomain)));
        assertTrue(ex1.getMessage().contains("V3 使用了保留域"), ex1.getMessage());

        ErrorCodeDef reservedSeq = def(20999, "RESERVED_SEQ_PROBE", meta(20999, "RESERVED_SEQ_PROBE", "保留段禁止业务使用"));
        IllegalStateException ex2 = assertThrows(IllegalStateException.class,
                () -> ErrorCodeRegistry.verify(List.of(reservedSeq)));
        assertTrue(ex2.getMessage().contains("V3 seq 落在保留段"), ex2.getMessage());
    }

    @Test
    void 负例_V7_同域内用户文案重复必须启动失败() {
        ErrorCodeDef a = def(20091, "SAME_MSG_ONE", meta(20091, "SAME_MSG_ONE", "单测同文案"));
        ErrorCodeDef b = def(20092, "SAME_MSG_TWO", meta(20092, "SAME_MSG_TWO", "单测同文案"));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> ErrorCodeRegistry.verify(List.of(a, b)));
        assertTrue(ex.getMessage().contains("V7 同域内 userMessage 重复"), ex.getMessage());
    }

    @Test
    void 负例_V6_成功语义命名必须启动失败() {
        ErrorCodeDef def = def(20093, "OPERATION_SUCCESS", meta(20093, "OPERATION_SUCCESS", "单测成功语义命名"));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> ErrorCodeRegistry.verify(List.of(def)));
        assertTrue(ex.getMessage().contains("V6 名称含成功语义后缀"), ex.getMessage());
    }

    @Test
    void 元信息必填项缺失时构造器直接拒绝() {
        assertThrows(IllegalArgumentException.class, () -> ErrorMeta.builder(20094, ""));
        assertThrows(IllegalArgumentException.class, () -> ErrorMeta.builder(0, "单测非法码值"));
    }

    @Test
    void 新码不得认领成功语义旧码() {
        ErrorCodeDef claimSuccess = def(20095, "CLAIM_SUCCESS_LEGACY",
                ErrorMeta.builder(20095, "单测认领成功语义旧码").legacyCodes(11000).build());
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> ErrorCodeRegistry.verify(List.of(claimSuccess)));
        assertTrue(ex.getMessage().contains("V10 legacyCodes 不得认领成功语义旧码"), ex.getMessage());
    }
}
