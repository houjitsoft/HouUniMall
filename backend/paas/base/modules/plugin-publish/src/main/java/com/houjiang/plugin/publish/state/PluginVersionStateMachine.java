package com.houjiang.plugin.publish.state;

import com.houjiang.commons.constant.PluginDistErrorCode;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 插件版本状态机（P0）。
 *
 * <p>允许的跃迁（方案 §3.2，<b>不增删任何一条</b>）：
 * <pre>
 *   draft(0)    → pending(3)
 *   pending(3)  → published(1)
 *   pending(3)  → rejected(4)
 *   rejected(4) → pending(3)
 *   published(1)→ offline(5)
 *   offline(5)  → published(1)
 * </pre>
 *
 * <p>两条硬约束：
 * <ol>
 *   <li>不在白名单里的跃迁一律拒绝，抛 {@link PluginStateTransitionException}；</li>
 *   <li>{@code published} / {@code offline} 状态的版本<b>不可删除、不可被同 code+version 覆盖</b>，
 *       这是「已安装租户永远能回滚」的前提。</li>
 * </ol>
 *
 * <p>本类为无状态工具类，所有方法静态，便于单测与在服务层复用。
 *
 * @author Amos
 * @since M1
 */
public final class PluginVersionStateMachine {

    /**
     * 允许的跃迁表。
     */
    private static final Map<PluginVersionStatus, Set<PluginVersionStatus>> ALLOWED =
            buildAllowedTransitions();

    /**
     * 私有构造，工具类不允许实例化。
     */
    private PluginVersionStateMachine() {
    }

    /**
     * 判断跃迁是否允许。
     *
     * @param from 源状态
     * @param to   目标状态
     * @return true 表示允许
     */
    public static boolean canTransition(PluginVersionStatus from, PluginVersionStatus to) {
        if (from == null || to == null) {
            return false;
        }
        Set<PluginVersionStatus> targets = ALLOWED.getOrDefault(from, Collections.emptySet());
        return targets.contains(to);
    }

    /**
     * 校验跃迁，不允许时抛异常。
     *
     * @param from 源状态
     * @param to   目标状态
     * @throws PluginStateTransitionException 跃迁不在白名单中
     */
    public static void assertTransition(PluginVersionStatus from, PluginVersionStatus to) {
        if (!canTransition(from, to)) {
            throw new PluginStateTransitionException(from, to);
        }
    }

    /**
     * 校验跃迁（入库值版本），不允许时抛异常。
     *
     * @param fromCode 源状态入库值
     * @param to       目标状态
     * @throws PluginStateTransitionException 跃迁不在白名单中
     */
    public static void assertTransition(Integer fromCode, PluginVersionStatus to) {
        assertTransition(PluginVersionStatus.fromCode(fromCode), to);
    }

    /**
     * 判断该状态的版本是否「不可删除 / 不可被同 code+version 覆盖」。
     *
     * @param status 版本状态
     * @return true 表示为终态，已发布或已下架
     */
    public static boolean isImmutable(PluginVersionStatus status) {
        return status != null && status.isImmutable();
    }

    /**
     * 判断该状态的版本是否允许重新上传制品（覆盖）。
     *
     * <p>只有 draft 允许：pending/rejected 表示已进入审核流程，
     * published/offline 表示已有租户在使用，都不允许换制品。
     *
     * @param status 版本状态
     * @return true 表示允许覆盖制品
     */
    public static boolean allowsArtifactOverwrite(PluginVersionStatus status) {
        return status == PluginVersionStatus.DRAFT;
    }

    private static Map<PluginVersionStatus, Set<PluginVersionStatus>> buildAllowedTransitions() {
        Map<PluginVersionStatus, Set<PluginVersionStatus>> map =
                new EnumMap<>(PluginVersionStatus.class);
        map.put(PluginVersionStatus.DRAFT, EnumSet.of(PluginVersionStatus.PENDING));
        map.put(PluginVersionStatus.PENDING,
                EnumSet.of(PluginVersionStatus.PUBLISHED, PluginVersionStatus.REJECTED));
        map.put(PluginVersionStatus.REJECTED, EnumSet.of(PluginVersionStatus.PENDING));
        map.put(PluginVersionStatus.PUBLISHED, EnumSet.of(PluginVersionStatus.OFFLINE));
        map.put(PluginVersionStatus.OFFLINE, EnumSet.of(PluginVersionStatus.PUBLISHED));
        return Collections.unmodifiableMap(map);
    }

    /**
     * 非法状态跃迁异常。
     *
     * <p>错误码沿用 {@link PluginDistErrorCode#VERSION_CONFLICT}（3012）：
     * 3001-3018 体系中，与「版本状态不允许这样改」最接近的就是版本冲突。
     */
    public static class PluginStateTransitionException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * 错误码。
         */
        private final int code = PluginDistErrorCode.VERSION_CONFLICT;

        /**
         * 构造异常。
         *
         * @param from 源状态
         * @param to   目标状态
         */
        public PluginStateTransitionException(PluginVersionStatus from, PluginVersionStatus to) {
            super("不允许的版本状态跃迁：" + describe(from) + " → " + describe(to));
        }

        /**
         * @return 错误码
         */
        public int getCode() {
            return code;
        }

        private static String describe(PluginVersionStatus status) {
            if (status == null) {
                return "未知";
            }
            return status.getDescription() + "(" + status.getCode() + ")";
        }
    }
}
