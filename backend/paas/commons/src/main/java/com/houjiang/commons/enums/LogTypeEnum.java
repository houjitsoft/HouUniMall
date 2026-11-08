package com.houjiang.commons.enums;

/**
 * 日志类型枚举
 * <p>
 * 定义系统中所有日志类型，用于日志分类存储和查询
 * </p>
 *
 * @author Amos
 * @since 2026/04/13
 */
public enum LogTypeEnum {

    /**
     * 操作日志：记录用户对业务数据的增删改查操作
     */
    OPERATION("OPERATION", "操作日志"),

    /**
     * 业务日志：记录核心业务流程节点（如订单状态变更、审批流转等）
     */
    BUSINESS("BUSINESS", "业务日志"),

    /**
     * 系统日志：记录系统级别的事件（如定时任务、配置变更等）
     */
    SYSTEM("SYSTEM", "系统日志"),

    /**
     * 访问日志：记录所有 HTTP 请求的访问轨迹
     */
    ACCESS("ACCESS", "访问日志"),

    /**
     * 登录日志：记录用户登录、登出、认证相关事件
     */
    LOGIN("LOGIN", "登录日志"),

    /**
     * 错误日志：记录系统异常和错误信息
     */
    ERROR("ERROR", "错误日志");

    /**
     * 日志类型编码
     */
    private final String code;

    /**
     * 日志类型描述
     */
    private final String desc;

    /**
     * 构造器
     *
     * @param code 类型编码
     * @param desc 类型描述
     */
    LogTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取日志类型编码
     *
     * @return 类型编码字符串
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取日志类型描述
     *
     * @return 类型描述字符串
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 根据编码获取枚举，未找到时返回 OPERATION
     *
     * @param code 类型编码
     * @return 对应枚举，未匹配时返回 OPERATION
     */
    public static LogTypeEnum fromCode(String code) {
        if (code == null) {
            return OPERATION;
        }
        for (LogTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return OPERATION;
    }
}
