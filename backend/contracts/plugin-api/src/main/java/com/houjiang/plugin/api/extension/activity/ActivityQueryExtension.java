package com.houjiang.plugin.api.extension.activity;

import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.util.List;
import java.util.Map;

/**
 * 活动列表查询扩展点：让每种活动由自己提供管理列表数据。
 *
 * <p><b>为什么有这个扩展点</b>：活动能力按业务归属分散在各宿主服务的插件里
 * （秒杀归order、红包归 userms、轮播归 cms…），而「活动管理」页面需要一个统一列表。
 * 若宿主实现直接注入全部活动 service，就会在order 进程里形成新的集中式门面，
 * 随活动种类增长不断膨胀。此扩展点改为由各活动插件自行贡献列表数据，
 * 宿主只做筛选、排序与分页编排，不感知具体活动实现。
 *
 * <p><b>契约约定</b>：
 * <ul>
 *   <li>{@link Context#activityType} 是稳定契约键，宿主按它做路由，不解析实现类。</li>
 *   <li>实现必须是幂等的：同一 {@code shopId + type} 重复查询不得改变宿主状态。</li>
 *   <li>实现只读，不得改写 {@link Context} 以外的宿主数据。</li>
 *   <li>未注册该 type 的实现时，宿主按空结果处理，不视为错误。</li>
 * </ul>
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface ActivityQueryExtension extends ExtensionPoint<ActivityQueryExtension.Context> {

    final class Context extends ExtensionContext {
        /** 当前店铺；实现必须按此隔离数据，不得跨店铺返回。 */
        public Integer shopId;
        /** 活动类型契约键，如 {@code seckill}、{@code redpacket}。 */
        public String activityType;
        /** 关键字模糊过滤；为空表示不过滤。 */
        public String keyword;
        /** 状态过滤；为空表示不过滤。 */
        public String status;
        /** 已累计查询到的条目数，实现可用于自行截断。 */
        public int offset;
        /** 本次最多返回条数，实现必须遵守。 */
        public int limit;

        /** 实现写入的结果列表；每项为展示用键值对，宿主会做统一排序与分页。 */
        public final List<Map<String, Object>> activities = new java.util.ArrayList<>();
        /** 实现可写入的该type 总数，用于宿主汇总分页总数。 */
        public int totalCount = 0;
    }

    /** 本扩展点为只读聚合型：execute 只向 {@code ctx.activities} 追加，不得 abort。 */
    /**
     * 执行前钩子（活动列表查询）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(ActivityQueryExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（活动列表查询）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(ActivityQueryExtension.Context ctx) {
        /* 默认不做收尾 */
    }

    class Noop implements ActivityQueryExtension {
        @Override
        public String activityType() { return "noop"; }

        @Override
        public void execute(Context ctx) { /* 默认不贡献活动 */ }
    }

    /**
     * 本实现贡献的活动类型契约键，如 {@code seckill}、{@code redpacket}。
     *
     * <p>宿主按此键路由，不解析实现类名；同一 type 只生效一个实现。
     * 新增活动类型时由对应插件新增实现，宿主无需改动。
     */
    String activityType();
}