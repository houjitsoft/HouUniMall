package com.houjiang.plugin.api.extension.activity;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * 红包发放扩展点。
 *
 * <p>供「某项权益发放一张红包」的调用方使用，例如抽奖中奖自动发红包、
 * 消费返现发红包等。红包是用户权益，数据归属 userms 宿主的红包插件；
 * 发放逻辑（模板校验、幂等、发红包流水）留在红包插件内部，不对调用方暴露。
 *
 * <p>原实现由抽奖插件直接注入 ActivityRedpacketBaseServiceImpl 写红包表，
 * 使两个插件在同一进程共享数据层；插件化后插件间不允许互相 import，
 * 故改为扩展点：抽奖插件只表达「要发一张红包」的意图，具体发放由红包插件执行。
 *
 * <p>约定：无实现时宿主按「本次不发放」处理并记录日志，**不得因缺少实现而中断抽奖**；
 * 发放失败时通过 {@link Context#errorMessage} 回传，由调用方决定是否回滚中奖记录。
 *
 * @since 1.0
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface ActivityRedpacketGrantExtension extends ExtensionPoint<ActivityRedpacketGrantExtension.Context> {

    /** 发放场景标识，如 spinwin（抽奖中奖）、rebate（返现）。 */
    String scene();

    /** 红包发放上下文。 */
    final class Context extends ExtensionContext {
        /** 店铺 ID。 */
        public Integer shopId;
        /** 发放场景。 */
        public String scene;
        /** 红包模板 ID。 */
        public Integer redpacketTemplateId;
        /** 扩展参数（如订单号、活动 ID、来源标识等）。 */
        public Map<String, String> params = new HashMap<>();
        /** 发放成功后回填的红包 ID。 */
        public Integer redpacketId;
    }

    class Noop implements ActivityRedpacketGrantExtension {
        @Override
        public String scene() {
            return "";
        }

        @Override
        public void execute(Context ctx) {
            /* 默认不发放 */
        }
    }
}
