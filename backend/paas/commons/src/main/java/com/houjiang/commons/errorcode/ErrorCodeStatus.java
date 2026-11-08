package com.houjiang.commons.errorcode;

/**
 * 错误码生命周期状态。
 *
 * <p>解决的问题：码值一经分配不可回收，废弃只能改状态，否则历史日志里的码值会指向新语义。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum ErrorCodeStatus {

    /** 生效中，可被业务引用。 */
    ACTIVE,

    /** 已废弃：码值保留不复用，新代码不得再引用（自检 V8）。 */
    DEPRECATED,

    /** 预留占位：仅登记号段，禁止业务引用（自检 V8）。 */
    RESERVED
}
