package com.houjiang.plugin.api.extension.permission;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

/**
 * 菜单贡献扩展点：插件向宿主侧栏贡献菜单入口。before* 语义（仅贡献数据）。
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface MenuContributionExtension extends ExtensionPoint<MenuContributionExtension.Context> {

    final class Context extends ExtensionContext {
        /** 插件贡献的菜单节点（写入即可） */
        public java.util.List<Object> menus = new java.util.ArrayList<>();
        public String pluginCode;
    }

    /**
     * 执行前钩子（菜单贡献）：宿主在调用 {@link #execute(Context)} 之前触发。
     *
     * <p>默认空实现。实现可在此读取上下文、写入审计信息，或调用
     * {@link com.houjiang.plugin.sdk.extension.ExtensionContext#abort(String)} 中断主链路。
     *
     * <p>契约：新增能力一律走本钩子，<b>不得改动 {@code execute} 或 {@code Context} 已有字段</b>。
     *
     * @param ctx 扩展点上下文，只读
     * @since 1.0
     */
    default void onPrepare(MenuContributionExtension.Context ctx) {
        /* 默认不干预 */
    }

    /**
     * 执行后钩子（菜单贡献）：宿主在 {@link #execute(Context)} 返回后触发。
     *
     * <p>默认空实现。{@code after*} 类扩展点可在此读取最终结果做埋点、审计或对账。
     *
     * @param ctx 扩展点上下文，可读最终值
     * @since 1.0
     */
    default void onComplete(MenuContributionExtension.Context ctx) {
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
    default boolean supports(MenuContributionExtension.Context ctx) {
        return true;
    }

    class Noop implements MenuContributionExtension {
        @Override
        public void execute(Context ctx) { /* 默认不贡献菜单 */
        }
    }
}
