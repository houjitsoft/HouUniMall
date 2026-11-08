package com.houjiang.userms.plugin;

import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import com.houjiang.plugin.api.extension.user.RedpacketQueryExtension;
import com.houjiang.plugin.runtime.gateway.ExtensionGatewaySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 红包查询扩展点的宿主薄网关（<b>只读</b>语义，<b>不得</b>中断）。
 *
 * <p><b>做什么</b>：替代 Core 侧对促销服务红包接口（{@code selectNewRedpacketTem} /
 * {@code selectredtemplateId} / {@code selectuserred}）的硬编码 {@code Feign} 调用。
 * 二开插件可接管红包模板 / 领取记录的查询，宿主按 {@link RedpacketQueryExtension#QUERY_NEW_TEMPLATE}
 * 等 {@code queryType} 发起查询，插件把结果写进 {@code ctx.result}。
 *
 * <p><b>只读约束</b>：本扩展点<b>不得</b>调用 {@code abort}，也不得产生写操作。网关即使收到
 * {@code abort} 也只记日志、<b>不抛异常</b>、不中断查询链路，按「无数据」处理。
 *
 * <p><b>回写语义澄清（对设计措辞的修正）</b>：本点虽名为「查询」，但其结果回写属
 * <b>{@code after*} 语义——回写替换，而非附加</b>：插件返回非空时<b>直接替换</b>宿主官方结果
 * （{@code replace}），未返回时才回落官方。设计文档中「return override / 附加」的措辞不精确，
 * 以本注释为准。after* 点的本义就是「结果回写」，此处与之一致。
 *
 * <p><b>向后兼容</b>：契约原文为「无插件注册时 {@code result} 为空列表，调用方按查不到红包处理」。
 * 但 Core 现有官方 {@code Feign} 链路必须保持可用（关闭二开 profile 时同一套断言全绿），
 * 故宿主接入采用<b>插件优先、无插件回落官方</b>：插件返回非空即以插件为准，否则沿用官方结果。
 * 该策略是本网关配套的宿主集成决策（见交付说明），不改变扩展点自身契约。
 *
 * <p>超时 / 熔断 / 降级 / 可观测由 {@link ExtensionGatewaySupport} 统一处理；
 * 本扩展点属故障 IGNORE 类。
 *
 * @author Amos
 */
public class RedpacketQueryGateway {

    private static final Logger log = LoggerFactory.getLogger(RedpacketQueryGateway.class);

    private static final String POINT = "RedpacketQuery";

    private final ExtensionGatewaySupport support;

    public RedpacketQueryGateway(ExtensionGatewaySupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    /**
     * 只读查询：触发扩展点并取回插件写入的结果。
     *
     * @param queryType 查询类型，取 {@link RedpacketQueryExtension} 常量
     * @param userId    用户 ID，可为 {@code null}
     * @param ids       查询 ID 列表（模板 ID / 红包 ID），可为 {@code null}
     * @param extra     透传参数，可为 {@code null}
     * @return 插件结果；无插件 / 空上下文 / 误 abort 时为空列表（绝不抛异常）
     */
    public List<Map<String, Object>> query(String queryType,
                                           Long userId,
                                           List<Integer> ids,
                                           Map<String, Object> extra) {
        RedpacketQueryExtension.Context ctx = new RedpacketQueryExtension.Context();
        ctx.queryType = queryType;
        ctx.userId = userId;
        if (ids != null) {
            ctx.ids = new ArrayList<>(ids);
        }
        if (extra != null) {
            ctx.extra = new HashMap<>(extra);
        }

        support.invoke(RedpacketQueryExtension.class, ctx);

        // 只读：契约禁止 abort；即便插件误用也只告警、不抛、不中断。
        if (ctx.isAborted()) {
            log.warn("[ext] point={} 为只读扩展点，忽略误用的 abort：queryType={} reason={}",
                    POINT, queryType, ctx.getAbortReason());
        }
        return ctx.result == null ? List.of() : ctx.result;
    }

    /**
     * 按 ID 批量查询红包模板（{@code TEMPLATE_BY_IDS}）：插件优先，无插件回落官方结果。
     *
     * @param userId   用户 ID（可空）
     * @param ids      模板 ID 列表
     * @param official 官方 {@code Feign} 结果（无插件时返回）
     * @return 红包模板列表
     */
    public List<RedTemplateEntityVO> redTemplatesByIds(Long userId,
                                                       List<Integer> ids,
                                                       List<RedTemplateEntityVO> official) {
        List<Map<String, Object>> pluginResult =
                query(RedpacketQueryExtension.QUERY_TEMPLATE_BY_IDS, userId, ids, null);
        if (pluginResult.isEmpty()) {
            return official;
        }
        List<RedTemplateEntityVO> mapped = new ArrayList<>(pluginResult.size());
        for (Map<String, Object> row : pluginResult) {
            mapped.add(toRedTemplate(row));
        }
        return mapped;
    }

    /**
     * 查询用户已领取记录条数（{@code USER_RECEIVED}）：插件优先，无插件回落官方计数。
     *
     * @param userId   用户 ID
     * @param ids      红包 ID 列表
     * @param official 官方 {@code Feign} 计数（无插件时返回）
     * @return 已领取条数
     */
    public Integer countUserReceived(Long userId, List<Integer> ids, Integer official) {
        List<Map<String, Object>> pluginResult =
                query(RedpacketQueryExtension.QUERY_USER_RECEIVED, userId, ids, null);
        if (pluginResult.isEmpty()) {
            return official;
        }
        return pluginResult.size();
    }

    /**
     * 领域映射：插件结果 Map → {@code RedTemplateEntityVO}（容错缺失/异类型字段）。
     */
    private RedTemplateEntityVO toRedTemplate(Map<String, Object> row) {
        RedTemplateEntityVO vo = new RedTemplateEntityVO();
        if (row == null) {
            return vo;
        }
        vo.setRedpacketTId(asInteger(row.get("redpacketTId")));
        vo.setRedpacketTTitle(asString(row.get("redpacketTTitle")));
        vo.setRedpacketTPrice(asBigDecimal(row.get("redpacketTPrice")));
        vo.setRedpacketTOrderlimit(asBigDecimal(row.get("redpacketTOrderlimit")));
        vo.setRedTermofvalidity(asInteger(row.get("redTermofvalidity")));
        return vo;
    }

    private static Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
