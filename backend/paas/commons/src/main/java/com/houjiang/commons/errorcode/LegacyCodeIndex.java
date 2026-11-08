package com.houjiang.commons.errorcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 旧码 → 新码反向索引。
 *
 * <p>解决的问题（PRD P0-5 / 架构 D3）：519 项旧枚举既有重复码又有跨枚举冲突
 * （如 15010 在 BizCodeEnume 里被 6-8 个语义占用），无法自动 1:1 映射。
 *
 * <p>设计取舍：<b>不另建映射表</b>，而是由各新码在自己的 {@code legacyCodes} 里反向声明，
 * 启动时反向构建索引 —— 单一真源，杜绝「枚举改了、映射表没改」的双向漂移。
 *
 * <p>三种解析结果：
 * <ul>
 *   <li>{@code EXACT}：恰好 1 个新码认领该旧码。</li>
 *   <li>{@code AMBIGUOUS}：多于 1 个新码认领（如 15010）。本轮<b>不为它们建新码</b>，
 *       而是登记在手册《附录 B：AMBIGUOUS 旧码清单》，由业务方逐接口认领后再补 legacyCodes；
 *       运行期 {@link #resolve(int)} 返回空，前端按 category 兜底。</li>
 *   <li>{@code UNMAPPED}：无人认领，不 fail-fast，进手册「待认领」清单。</li>
 * </ul>
 *
 * @author Amos
 * @since v1.2.0
 */
public final class LegacyCodeIndex {

    private static final Map<Integer, List<ErrorCodeDef>> BY_LEGACY;
    private static final Map<Integer, List<Integer>> AMBIGUOUS;

    static {
        Map<Integer, List<ErrorCodeDef>> byLegacy = new LinkedHashMap<>();
        Map<Integer, List<Integer>> ambiguous = new LinkedHashMap<>();
        for (ErrorCodeDef def : ErrorCodeRegistry.all()) {
            for (int legacy : def.meta().legacyCodes()) {
                byLegacy.computeIfAbsent(legacy, k -> new ArrayList<>()).add(def);
            }
        }
        for (Map.Entry<Integer, List<ErrorCodeDef>> entry : byLegacy.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<Integer> candidates = new ArrayList<>();
                for (ErrorCodeDef def : entry.getValue()) {
                    candidates.add(def.code());
                }
                ambiguous.put(entry.getKey(), Collections.unmodifiableList(candidates));
            }
        }
        BY_LEGACY = Collections.unmodifiableMap(byLegacy);
        AMBIGUOUS = Collections.unmodifiableMap(ambiguous);
    }

    private LegacyCodeIndex() {
        // 工具类禁止实例化
    }

    /**
     * 解析旧码对应的新码集合。
     *
     * @param legacyCode 旧码（BizCodeEnume / GlobalExceptionEnum / 裸码等）
     * @return 新码列表；AMBIGUOUS 与 UNMAPPED 均返回空列表
     */
    public static List<ErrorCodeDef> resolve(int legacyCode) {
        List<ErrorCodeDef> defs = BY_LEGACY.get(legacyCode);
        if (defs == null || defs.size() != 1) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(defs);
    }

    /**
     * 该旧码是否被多个新码认领（歧义码）。
     *
     * @param legacyCode 旧码
     * @return true 表示歧义
     */
    public static boolean isAmbiguous(int legacyCode) {
        return AMBIGUOUS.containsKey(legacyCode);
    }

    /**
     * 该旧码的全部候选新码（含歧义场景）。
     *
     * @param legacyCode 旧码
     * @return 候选新码码值列表，无人认领时为空
     */
    public static List<Integer> candidatesOf(int legacyCode) {
        if (isAmbiguous(legacyCode)) {
            return Collections.emptyList();
        }
        List<ErrorCodeDef> defs = BY_LEGACY.get(legacyCode);
        if (defs == null) {
            return Collections.emptyList();
        }
        List<Integer> codes = new ArrayList<>();
        for (ErrorCodeDef def : defs) {
            codes.add(def.code());
        }
        return Collections.unmodifiableList(codes);
    }

    /**
     * @return 全部歧义旧码 → 候选新码 的映射
     */
    public static Map<Integer, List<Integer>> ambiguousAll() {
        return AMBIGUOUS;
    }

    /**
     * 取旧码对应的唯一新码；歧义或未映射时返回 {@code fallback}。
     *
     * @param legacyCode 旧码
     * @param fallback   兜底错误码
     * @return 新码定义
     */
    public static ErrorCodeDef firstOf(int legacyCode, ErrorCodeDef fallback) {
        List<ErrorCodeDef> defs = resolve(legacyCode);
        return defs.isEmpty() ? fallback : defs.get(0);
    }

    /**
     * @return 已登记映射的旧码数量
     */
    public static int mappedLegacyCount() {
        return BY_LEGACY.size();
    }
}
