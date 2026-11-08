package com.houjiang.commons.version;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析插件依赖声明 {@code dependencies_json}（M5b）。
 *
 * <p><b>弱校验口径（与 M1 清单校验、预检既有实现一致）</b>：
 * 声明缺失、不是数组、JSON 非法，一律<b>按「无依赖」放行</b>而不是报错拦截。
 * 理由：存量商品里 {@code dependencies_json} 为 null / 空串 / 手填脏数据很常见，
 * 「声明写错了」和「真的缺依赖」是两件事，前者不该让已上架的插件整体升不了级。
 * 但<b>声明了就必须满足</b>——那部分判定在 {@link PluginDependencyExpression#satisfiedBy(String)}。
 *
 * <p><b>为什么解析失败只 warn 不抛</b>：调用方一个是「目录列表页」（一次几十个插件），
 * 一个是「安装前预检」。前者抛异常会让整页失效，后者抛异常会把「没声明依赖」的插件全拦在门外，
 * 都比「少校验一项」严重。真正的兜底在预检——它仍是安装流程的强制前置步骤。
 *
 * <p><b>⚠️ 已知限制</b>：依赖声明是<b>商品级</b>的（挂在 {@code hou_market_plugin} 上），
 * 不是<b>版本级</b>的。因此某个 v2.0 新增的依赖会影响 v1.x 用户看到的升级提示——
 * 目录判定用的是「当前商品行上的依赖声明」，而不是「目标版本当时声明的依赖」。
 * 要彻底解决需要把 dependencies 下沉到版本行，那是一次涉及发布侧、预检侧、目录侧的
 * 数据模型改造，本次不做。
 *
 * @author Amos
 * @since M5b
 */
@Slf4j
public final class PluginDependencyParser {

    /**
     * 私有构造，工具类不允许实例化。
     */
    private PluginDependencyParser() {
    }

    /**
     * 解析 {@code dependencies_json}。
     *
     * <p>接受两种形态：JSON 字符串数组（{@code ["erp","oa@1.2.0"]}）或单个字符串
     * （{@code "erp"}）；其它形态（对象、数字、非法 JSON、空串）一律返回空列表。
     *
     * <p>数组元素中的空白项会被跳过；{@code code} 为空的声明（如 {@code "@1.0.0"}）
     * 同样跳过——编码为空无从查证，按弱校验丢弃而不是按「缺失依赖」拦截。
     *
     * @param json 原始 JSON，可为 null
     * @return 依赖声明列表；解析不出任何有效声明时返回空列表
     */
    public static List<PluginDependencyExpression> parse(String json) {
        List<String> declarations = rawDeclarations(json);
        List<PluginDependencyExpression> result = new ArrayList<>();
        for (String declaration : declarations) {
            PluginDependencyExpression expression = parseOne(declaration);
            if (expression != null) {
                result.add(expression);
            }
        }
        return result;
    }

    /**
     * 解析单条声明：{@code code} 或 {@code code@minVersion}。
     *
     * <p>分隔符取<b>第一个</b> {@code @} 且要求它不在首位（{@code indexOf > 0}）——
     * 与下沉前预检的实现保持逐字一致。
     *
     * <p><b>⚠️ 遗留怪癖（刻意保留，未顺手修）</b>：因此 {@code "@1.0.0"} 这种写法
     * <b>不会</b>被拆成「编码为空 + 版本 1.0.0」，而是整体当作一个编码 {@code "@1.0.0"}，
     * 结果必然查不到安装记录、被判为「缺依赖」。按弱校验口径它本该被丢弃，
     * 但改掉会翻转线上既有脏数据的判定结果（从「拦下」变成「放行」），
     * 超出本次「补依赖校验」的范围，故保留原语义，仅在此标注待议。
     *
     * @param declaration 单条声明
     * @return 依赖声明；编码为空返回 null
     */
    private static PluginDependencyExpression parseOne(String declaration) {
        String code = declaration;
        String minVersion = null;
        int separator = declaration.indexOf(PluginDependencyExpression.VERSION_SEPARATOR);
        if (separator > 0) {
            code = declaration.substring(0, separator).trim();
            minVersion = declaration.substring(separator + 1).trim();
        }
        if (code.isEmpty()) {
            return null;
        }
        return PluginDependencyExpression.of(code, minVersion);
    }

    /**
     * 从 JSON 里取出声明字符串列表（只做「取出来」，不做语义拆分）。
     *
     * @param json 原始 JSON
     * @return 声明字符串列表；任何异常或非法形态都返回空列表
     */
    private static List<String> rawDeclarations(String json) {
        List<String> result = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return result;
        }
        Object parsed;
        try {
            parsed = JSON.parse(json.trim());
        } catch (RuntimeException e) {
            log.warn("依赖声明解析失败，按无依赖处理: json={} err={}", json, e.getMessage());
            return result;
        }
        if (parsed instanceof List<?> list) {
            for (Object item : list) {
                if (item == null) {
                    continue;
                }
                String text = item.toString().trim();
                if (!text.isEmpty()) {
                    result.add(text);
                }
            }
        } else if (parsed instanceof String single) {
            String text = single.trim();
            if (!text.isEmpty()) {
                result.add(text);
            }
        }
        return result;
    }
}
