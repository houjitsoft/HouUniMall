package com.houjiang.commons.errorcode;

/**
 * 统一错误码定义接口。
 *
 * <p>解决的问题：过去一个错误码只是「一个数字 + 一句中文」，
 * 且裸码散落在 113 个 Java 文件、1216 处调用点里，没有归属域、没有级别、没有展示动作，
 * 前端只能靠 <code>res.message.includes('未登录')</code> 这类字符串判断做分支，后端改一个字就失效。
 *
 * <p>本接口把「码值 + 名称 + 元信息 + 归属域」绑成一个编译期常量，
 * 由 {@link ErrorCodeRegistry} 在启动时统一登记并做 12 项自检（见 {@link ErrorCodeRegistry#verify()}），
 * 手册、前端映射表、字典接口全部由它派生，杜绝第二份真源。
 *
 * <p>实现方为各业务域枚举（如 {@code SystemErrorCode}），每个域一个文件，域 owner 各改各的，
 * 避免所有人挤在一个几千行的巨型枚举里产生 merge 冲突。
 *
 * @author Amos
 * @since v1.2.0
 */
public interface ErrorCodeDef {

    /**
     * 错误码值，形如 {@code domainId * 1000 + seq}，当前码值域 20001-99999。
     *
     * @return 全局唯一的错误码
     */
    int code();

    /**
     * 英文常量名，UPPER_SNAKE_CASE，必须与枚举常量名严格一致（自检 V5）。
     *
     * @return 错误码名称
     */
    String name();

    /**
     * 错误码元信息（用户文案、级别、类别、展示动作、旧码映射等）。
     *
     * @return 元信息，不可为 null
     */
    ErrorMeta meta();

    /**
     * 由码值反查归属域，避免「码值在一个域、域填成另一个域」的错填（自检 V2）。
     *
     * @return 归属域
     */
    default ErrorDomain domain() {
        return ErrorDomain.of(code());
    }

    /**
     * 可直接出屏的用户文案。
     *
     * @return 用户文案
     */
    default String userMessage() {
        return meta().userMessage();
    }

    /**
     * 是否为致命错误（依赖故障 / 数据异常，需人工介入）。
     *
     * @return true 表示 FATAL
     */
    default boolean isFatal() {
        return meta().level() == ErrorLevel.FATAL;
    }
}
