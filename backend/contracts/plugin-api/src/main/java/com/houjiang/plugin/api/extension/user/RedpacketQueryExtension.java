package com.houjiang.plugin.api.extension.user;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 红包查询扩展点：新人红包模板、红包模板批量查询、用户领取记录查询。
 *
 * <p><b>背景</b>：原先 Core 的 {@code userms}（{@code UserPlusInfoServiceImpl} /
 * {@code UserPlusEquityServiceImpl}）硬编码调用促销服务的红包接口
 * （{@code selectNewRedpacketTem} / {@code selectredtemplateId} / {@code selectuserred}）。
 * 插件缺失时用户权益查询会失败。
 *
 * <p><b>解耦方式</b>：宿主按 {@code queryType} 发起查询，插件写入 {@code result}；
 * 无插件注册时 {@code result} 为空列表，调用方按"查不到红包"处理即可。
 *
 * <p><b>注意</b>：本扩展点是只读查询，<b>不得</b>调用 {@link ExtensionContext#abort(String)}，
 * 也不得产生写操作。
 *
 * @author Amos
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface RedpacketQueryExtension extends ExtensionPoint<RedpacketQueryExtension.Context> {

    /** 查询新人红包模板（对应 selectNewRedpacketTem） */
    String QUERY_NEW_TEMPLATE = "NEW_TEMPLATE";

    /** 按 ID 批量查询红包模板（对应 selectredtemplateId / selectNewRedpacketTem） */
    String QUERY_TEMPLATE_BY_IDS = "TEMPLATE_BY_IDS";

    /** 查询用户已领取记录（对应 selectuserred） */
    String QUERY_USER_RECEIVED = "USER_RECEIVED";

    final class Context extends ExtensionContext {

        /** 查询类型，取值见本接口常量 */
        public String queryType;

        /** 用户 ID */
        public Long userId;

        /** 查询 ID 列表（模板 ID / 红包 ID，视 queryType 而定） */
        public List<Object> ids = new ArrayList<>();

        /** 查询结果（插件写入）；无插件时保持空列表 */
        public List<Map<String, Object>> result = new ArrayList<>();

        /** 透传参数 */
        public Map<String, Object> extra = new HashMap<>();
    }

    /**
     * 执行前钩子（红包查询）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(RedpacketQueryExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（红包查询）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(RedpacketQueryExtension.Context ctx) {
        /* 默认不做收尾 */
    }

    /**
     * 宿主询问实现是否关心本次上下文；返回 {@code false} 时宿主可快速跳过本实现。
     *
     * <p>默认返回 {@code true}（关心）。用于避免为无关业务场景加载重实现。
     *
     * @param ctx 扩展点上下文
     * @return 是否需要执行
     * @since 1.0
     */
    default boolean supports(RedpacketQueryExtension.Context ctx) {
        return true;
    }

    class Noop implements RedpacketQueryExtension {
        @Override
        public void execute(Context ctx) { /* 默认无红包数据 */
        }
    }
}
