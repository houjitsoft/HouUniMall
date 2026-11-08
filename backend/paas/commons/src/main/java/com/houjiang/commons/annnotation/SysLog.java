package com.houjiang.commons.annnotation;

import java.lang.annotation.*;

/**
 * 系统日志注解
 * 用于标记需要记录操作日志的方法，支持日志类型和所属模块的配置
 *
 * @author Amos
 * @since 2023/6/14
 */
/** 注解放置的目标位置,METHOD是可注解在方法级别上 */
@Target(ElementType.METHOD)
/** 注解在哪个阶段执行 */
@Retention(RetentionPolicy.RUNTIME)
/** 生成文档 */
@Documented
public @interface SysLog {

    /**
     * 操作描述（向后兼容保留）
     *
     * @return 操作描述文本
     */
    String value();

    /**
     * 日志类型，默认为操作日志
     * 可选值：OPERATION（操作日志）、BUSINESS（业务日志）、SYSTEM（系统日志）
     *
     * @return 日志类型字符串
     */
    String logType() default "OPERATION";

    /**
     * 所属模块名称，如 pm、oa、crm 等
     * 便于日志分类统计和问题定位
     *
     * @return 模块名称
     */
    String module() default "";
}
