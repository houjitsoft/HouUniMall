package com.houjiang.commons.errorcode;

import com.houjiang.commons.errorcode.domain.SystemErrorCode;

import java.util.List;

/**
 * 统一错误码门面（对外唯一入口）。
 *
 * <p>解决的问题（PRD G1）：过去新增错误码没有入口，只能「随便挑一个没撞的数字」裸写在业务代码里。
 * 现在全局异常处理器、字典导出接口、离线生成器、前端映射层统一从这里取数。
 *
 * <p>契约：任何查询方法都<b>不返回 null</b>。未登记的码一律降级为
 * {@link SystemErrorCode#UNKNOWN_ERROR} 的元信息，保证调用方不需要判空。
 *
 * @author Amos
 * @since v1.2.0
 */
public final class ErrorCodes {

    private ErrorCodes() {
        // 工具类禁止实例化
    }

    /**
     * 按码值取元信息；未登记时返回 {@link SystemErrorCode#UNKNOWN_ERROR} 的元信息，绝不返回 null。
     *
     * @param code 错误码
     * @return 元信息
     */
    public static ErrorMeta metaOf(int code) {
        ErrorCodeDef def = ErrorCodeRegistry.of(code);
        return def == null ? SystemErrorCode.UNKNOWN_ERROR.meta() : def.meta();
    }

    /**
     * 按码值取用户文案；未登记时返回未知错误的兜底文案。
     *
     * @param code 错误码
     * @return 用户文案
     */
    public static String userMessage(int code) {
        return metaOf(code).userMessage();
    }

    /**
     * 按码值取开发/排障文案（只进日志，不出屏）。
     *
     * @param code 错误码
     * @return 排障文案
     */
    public static String devMessage(int code) {
        return metaOf(code).devMessage();
    }

    /**
     * 是否为已登记的统一错误码（20001-99999 且在册）。
     *
     * @param code 码值
     * @return true 表示统一错误码
     */
    public static boolean isUnifiedCode(int code) {
        return ErrorCodeRegistry.isUnifiedCode(code);
    }

    /**
     * 按码值取定义；未登记时 null。
     *
     * @param code 错误码
     * @return 定义
     */
    public static ErrorCodeDef of(int code) {
        return ErrorCodeRegistry.of(code);
    }

    /**
     * 按名称取定义；未登记时 null。
     *
     * @param name 英文常量名
     * @return 定义
     */
    public static ErrorCodeDef ofName(String name) {
        return ErrorCodeRegistry.ofName(name);
    }

    /**
     * @return 全部已登记错误码
     */
    public static List<ErrorCodeDef> all() {
        return ErrorCodeRegistry.all();
    }

    /**
     * @param domain 归属域
     * @return 该域下的错误码
     */
    public static List<ErrorCodeDef> byDomain(ErrorDomain domain) {
        return ErrorCodeRegistry.byDomain(domain);
    }

    /**
     * @param level 级别
     * @return 该级别下的错误码
     */
    public static List<ErrorCodeDef> byLevel(ErrorLevel level) {
        return ErrorCodeRegistry.byLevel(level);
    }

    /**
     * @param category 类别
     * @return 该类别下的错误码
     */
    public static List<ErrorCodeDef> byCategory(ErrorCategory category) {
        return ErrorCodeRegistry.byCategory(category);
    }

    /**
     * 是否成功码（成功判定只看码集合，不看 message）。
     *
     * @param code 响应码
     * @return true 表示成功
     */
    public static boolean isSuccess(int code) {
        return SuccessCode.isSuccess(code);
    }

    /**
     * 是否 FATAL 级别。
     *
     * @param code 错误码
     * @return true 表示需人工介入
     */
    public static boolean isFatal(int code) {
        return metaOf(code).level() == ErrorLevel.FATAL;
    }

    /**
     * 启动自检（Spring 侧由 ApplicationRunner 调用；非 Spring 环境可手工调用）。
     *
     * @throws IllegalStateException 存在违规项时抛出
     */
    public static void verify() {
        ErrorCodeRegistry.verify();
    }
}
