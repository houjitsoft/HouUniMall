package com.houjiang.commons.errorcode;

import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.GlobalExceptionEnum;
import com.houjiang.commons.exception.RiderCodeEnume;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 防回退锁：三个历史枚举（{@link BizCodeEnume} / {@link RiderCodeEnume} / {@link GlobalExceptionEnum}）
 * 在第二阶段重构后已经<b>不是第二套码源</b>，而是统一码的引用入口 ——
 * 每个存活项的 code / msg 都直接取自 {@code com.houjiang.commons.errorcode.domain} 下的统一码枚举。
 *
 * <p>本测试确保这条约束不会被悄悄破坏：
 * <ol>
 *   <li>每个错误语义项的码值必须能在 {@link ErrorCodeRegistry} 里查到（死项复活、手抄数字抄错都会红）；</li>
 *   <li>每个错误语义项的 msg 必须<b>等于</b>对应统一码的 {@code userMessage}
 *       （防止有人把码值改成引用、却把文案改回历史文本）；</li>
 *   <li>不进错误码体系的项（成功语义 + 历史哨兵）必须显式登记在
 *       {@link #NON_ERROR_LEGACY} 白名单里，且白名单不得比实际多出或少于任何一项
 *       —— 新增一个「保留历史码值」的常量而不说明理由，这里会直接红。</li>
 * </ol>
 *
 * @author Amos
 * @since v1.3.0
 */
class LegacyEnumBindingTest {

    /**
     * 明确不进错误码体系的项（成功语义 + 历史哨兵），键为「枚举简名.常量名」。
     *
     * <p>成功语义的码值必须保持历史值：前端 {@code isSuccessCode} 认 0/1/200，
     * 把它们编成 5 位统一码会把成功判成失败。
     */
    private static final Set<String> NON_ERROR_LEGACY = Set.of(
            // —— BizCodeEnume：成功语义 ——
            "BizCodeEnume.OPERATION_SUCCESSFUL",
            "BizCodeEnume.REGISTRATION_SUCCESS",
            "BizCodeEnume.SENDING_SUCCEEDED",
            "BizCodeEnume.SMS_SUCCESS",
            "BizCodeEnume.UPDATE_SUCCEEDED",
            "BizCodeEnume.OSS_SUCCESS",
            "BizCodeEnume.UPLOAD_SUCCESS",
            "BizCodeEnume.ADD_SUCCESS",
            "BizCodeEnume.DELETE_SUCCESS",
            "BizCodeEnume.OSS_STACT_SUCCESS",
            "BizCodeEnume.VERIFICATION_SUCCESSFUL",
            "BizCodeEnume.LOGIN_SUCCEEDED",
            "BizCodeEnume.EXIT_SUCCESSFULLY",
            "BizCodeEnume.PASSWORD_UPDATED_SUCCESSFULLY",
            "BizCodeEnume.VERIFICATION_CODE_SUCCESS",
            "BizCodeEnume.SUCCESSFULLY_QUERIED_FAVORITE_PRODUCTS",
            "BizCodeEnume.SUCCESSFULLY_VERIFIED_THE_OLD_PHONE_NUMBER",
            "BizCodeEnume.SUCCESSFULLY_CHANGED_THE_PHONE_NUMBER",
            "BizCodeEnume.DEFAULT_RECEIPT_SUCCEEDED",
            "BizCodeEnume.GOODS_RECEIVED_SUCCESSFULLY",
            "BizCodeEnume.APPLICATION_SUCCESS",
            "BizCodeEnume.REVOKE_COMPLAINTS_SUCCESS",
            "BizCodeEnume.ADD_GOODS_RELEASED",
            "BizCodeEnume.ADD_GOODS_SUCCESS",
            "BizCodeEnume.ADD_SELF_GOODS_SUCCESS",
            "BizCodeEnume.UPDATE_SELF_GOODS_SUCCESS",
            "BizCodeEnume.SUCCESSFULLY_QUERIED_FAVORITE_STORES",
            "BizCodeEnume.RECHARGED_SUCCESSFULLY",
            "BizCodeEnume.TRANSFER_SUCCEEDED",
            "BizCodeEnume.PAYMENT_SUCCESSFUL",
            "BizCodeEnume.ACTIVITY_NO_PURCHASE_LIMIT",
            "BizCodeEnume.VOUCHER_TEMPLATE_SUCCESS",
            "BizCodeEnume.INSERT_VOUCHER_TEMPLATE_SUCCESS",
            // —— 历史哨兵：原枚举用 End 标记常量列表结束，保留以兼容反射式遍历 ——
            "BizCodeEnume.End",
            "RiderCodeEnume.End"
    );

    /** 一条待校验的枚举项。 */
    private record Item(String key, int code, String msg) {
    }

    /**
     * 收集三个历史枚举的全部待校验项。
     *
     * @return 全部项
     */
    private static List<Item> collectItems() {
        List<Item> items = new ArrayList<>();
        for (BizCodeEnume e : BizCodeEnume.values()) {
            items.add(new Item("BizCodeEnume." + e.name(), e.getCode(), e.getMsg()));
        }
        for (RiderCodeEnume e : RiderCodeEnume.values()) {
            items.add(new Item("RiderCodeEnume." + e.name(), e.getCode(), e.getMsg()));
        }
        for (GlobalExceptionEnum e : GlobalExceptionEnum.values()) {
            items.add(new Item("GlobalExceptionEnum." + e.name(), e.getErrorCode(), e.getErrorMsg()));
        }
        return items;
    }

    /**
     * 错误语义项：码值必须已登记为统一码，且文案必须等于该统一码的 userMessage。
     */
    @Test
    void 错误语义项的码值与文案必须来自统一码() {
        List<String> errors = new ArrayList<>();
        int checked = 0;
        for (Item item : collectItems()) {
            if (NON_ERROR_LEGACY.contains(item.key())) {
                continue;
            }
            checked++;
            if (!ErrorCodeRegistry.isUnifiedCode(item.code())) {
                errors.add(item.key() + " 码值 " + item.code() + " 未登记为统一错误码（疑似手抄数字或死项复活）");
                continue;
            }
            ErrorCodeDef def = ErrorCodeRegistry.of(item.code());
            assertNotNull(def, item.key() + " 注册表返回 null");
            String expected = def.meta().userMessage();
            if (!expected.equals(item.msg())) {
                errors.add(item.key() + " 文案被改写：期望「" + expected + "」，实际「" + item.msg() + "」");
            }
        }
        assertTrue(errors.isEmpty(),
                "历史枚举必须全部引用统一码，发现 " + errors.size() + " 处违规：\n" + String.join("\n", errors));
        assertTrue(checked > 0, "至少应校验一个错误语义项");
    }

    /**
     * 白名单锁：不进错误码体系的项必须且只能是白名单里的那些，一个不多一个不少。
     */
    @Test
    void 非错误码白名单必须与枚举现状完全一致() {
        List<String> actual = new ArrayList<>();
        for (Item item : collectItems()) {
            if (!ErrorCodeRegistry.isUnifiedCode(item.code())) {
                actual.add(item.key());
            }
        }
        List<String> extra = new ArrayList<>(actual);
        extra.removeAll(NON_ERROR_LEGACY);
        List<String> missing = new ArrayList<>(NON_ERROR_LEGACY);
        missing.removeAll(actual);
        assertTrue(extra.isEmpty(),
                "以下项保留了历史码值却未登记进白名单（必须说明理由后才能加）：" + extra);
        assertTrue(missing.isEmpty(),
                "白名单里的项实际已改为引用统一码，请把它们从白名单移除：" + missing);
        assertEquals(NON_ERROR_LEGACY.size(), actual.size(), "白名单数量应与实际保留历史码值的项数一致");
    }

    /**
     * wire 上不再出现旧码：错误语义项的码值必须全部落在统一码区间。
     */
    @Test
    void 错误语义项不得再出现旧码值() {
        List<String> legacyOnWire = new ArrayList<>();
        for (Item item : collectItems()) {
            if (NON_ERROR_LEGACY.contains(item.key())) {
                continue;
            }
            if (item.code() < ErrorCodeRegistry.MIN_CODE || item.code() > ErrorCodeRegistry.MAX_CODE) {
                legacyOnWire.add(item.key() + "=" + item.code());
            }
        }
        assertTrue(legacyOnWire.isEmpty(),
                "错误语义项仍在使用旧码值，wire 上会出现历史码：" + legacyOnWire);
    }
}
