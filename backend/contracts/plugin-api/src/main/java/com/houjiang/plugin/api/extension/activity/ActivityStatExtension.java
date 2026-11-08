package com.houjiang.plugin.api.extension.activity;

import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 活动统计扩展点：让每种活动自行提供管理端概览所需的计数。
 *
 * <p><b>为什么有这个扩展点</b>：概览页需要「各类型活动各有多少个」。若宿主直接对每种活动的表
 * 逐个count，则宿主必须依赖全部活动实体，随活动种类增长不断膨胀。改为由各活动插件自行提供计数，
 * 宿主只汇总已注册的类型。
 *
 * <p><b>契约约定</b>：
 * <ul>
 *   <li>实现只读，不得写入业务表。</li>
 *   <li>实现必须按 {@link Context#shopId} 隔离统计范围。</li>
 *   <li>未注册该类型时，宿主不展示该类型，不得以 0 冒充（0 表示已注册但确实没有）。</li>
 *   <li>实现失败时置 {@link Context#errorMessage}，宿主跳过该类型，不阻断整体概览。</li>
 * </ul>
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface ActivityStatExtension extends ExtensionPoint<ActivityStatExtension.Context> {

    final class Context extends ExtensionContext {
        /** 当前店铺；实现必须按此隔离统计范围。 */
        public Integer shopId;
        /** 本实现对应的活动类型契约键，如 {@code seckill}。 */
        public String activityType;
        /** 本类型的活动总数。 */
        public int totalCount = 0;
        /** 按状态计数，键为状态契约值（pending / active / paused / ended）。 */
        public final Map<String, Integer> statusCounts = new LinkedHashMap<>();
        /** 失败原因；非空时宿主跳过本类型。 */
        public String errorMessage;
    }

    /** 本扩展点为只读聚合型：execute 只回填计数，不得 abort。 */
    /**
     * 执行前钩子（活动统计汇总）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(ActivityStatExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（活动统计汇总）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(ActivityStatExtension.Context ctx) {
        /* 默认不做收尾 */
    }

    class Noop implements ActivityStatExtension {
        @Override
        public String activityType() { return "noop"; }

        @Override
        public void execute(Context ctx) { /* 默认不贡献统计 */ }
    }

    /** 本实现贡献统计的活动类型契约键。 */
    String activityType();
}