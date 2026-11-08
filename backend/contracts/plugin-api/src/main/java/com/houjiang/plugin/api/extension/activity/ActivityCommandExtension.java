package com.houjiang.plugin.api.extension.activity;

import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.util.Map;

/**
 * 活动管理操作扩展点：让每种活动由自己承接管理端的写操作（创建 / 更新 / 删除 / 暂停 / 恢复 / 复制）。
 *
 * <p><b>为什么有这个扩展点</b>：管理端的「活动管理」是对所有活动的统一入口，但活动实现按业务归属
 * 分散在各宿主服务（秒杀归order、红包归 userms、轮播归 cms…）。若宿主实现直接按 type 写 switch
 * 并调用各域 service，则宿主会随活动种类增长不断膨胀，且迫使所有活动实现共处同一进程。
 *
 * <p><b>契约约定</b>：
 * <ul>
 *   <li>{@link #action()} + {@link Context#activityType} 共同决定路由；同一 type 只生效一个实现。</li>
 *   <li>实现必须按 {@link Context#shopId} 隔离数据，不得跨店铺读写。</li>
 *   <li>实现失败时置 {@link Context#errorMessage}，由宿主统一转换为错误响应，
 *       不得直接抛出宿主不识别的异常类型。</li>
 *   <li>删除、暂停、恢复等操作必须幂等：重复调用不得产生额外副作用。</li>
 *   <li>实现不得改写 {@link Context#result} 以外的宿主状态。</li>
 * </ul>
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface ActivityCommandExtension extends ExtensionPoint<ActivityCommandExtension.Context> {

    /** 操作类型常量。 */
    String ACTION_CREATE = "create";
    String ACTION_UPDATE = "update";
    String ACTION_DELETE = "delete";
    String ACTION_PAUSE = "pause";
    String ACTION_RESUME = "resume";
    String ACTION_COPY = "copy";
    String ACTION_DETAIL = "detail";

    final class Context extends ExtensionContext {
        /** 当前店铺；实现必须按此隔离数据。 */
        public Integer shopId;
        /** 活动类型契约键，如 {@code seckill}、{@code redpacket}。 */
        public String activityType;
        /** 操作类型，取本接口的 ACTION_* 常量。 */
        public String action;
        /** 活动主键；detail / delete / pause / resume / copy 使用。 */
        public Integer activityId;
        /** 创建参数载荷（宽松Map，由实现自行解析）；create 使用。 */
        public Map<String, Object> payload = new java.util.HashMap<>();
        /** 失败原因；实现置非空表示失败，宿主据此生成错误响应。 */
        public String errorMessage;
        /** 成功时的结果载荷，宿主原样透传给前端。 */
        public Map<String, Object> result = new java.util.HashMap<>();
    }

    /** 本扩展点为写操作型：实现通过 {@code errorMessage} 表达失败，不得 abort 主链路。 */
    /**
     * 执行前钩子（活动写操作（增删改/启停/复制））：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(ActivityCommandExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（活动写操作（增删改/启停/复制））：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(ActivityCommandExtension.Context ctx) {
        /* 默认不做收尾 */
    }

    class Noop implements ActivityCommandExtension {
        @Override
        public String activityType() { return "noop"; }

        @Override
        public String action() { return ACTION_DETAIL; }

        @Override
        public void execute(Context ctx) { /* 默认不承接任何操作 */ }
    }

    /** 本实现承接的活动类型契约键。 */
    String activityType();

    /**
     * 本实现承接的操作类型；宿主只路由到 action 匹配的实现。
     *
     * <p>同一 {@code (activityType, action)} 组合只生效一个实现；未匹配时宿主返回「不支持的活动类型」。
     */
    String action();
}