package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * SYSTEM（域 20）系统通用错误码，号段 20001-20999。
 *
 * <p>解决的问题：过去「系统级异常」在 commons 的 GlobalExceptionHandler 里是一串裸数字
 * （11009 / 11003 / 10000 / 10001 / 404 / 5000），且这些数字在 BizCodeEnume 里各是另一件事
 * （11009=账号尚未审核通过、11003=账号或密码错误、10000=系统未知异常、10001=参数格式异常），
 * 客服拿到截图里的数字无法反查语义。本域把系统级异常重新登记为独立号段，并保留旧码映射。
 *
 * <p>子模块段位：20001-20099 框架通用（路由 / 序列化 / 线程池 / 配置）；
 * 20601-20699 数据访问（DB / SQL / MyBatis）。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum SystemErrorCode implements ErrorCodeDef {

    /**
     * 路由或资源不存在（NoResourceFoundException、接口未加载、bff-web 未重启到最新版本）。
     *
     * <p>触发：请求路径没有匹配的 HandlerMapping，或访问了尚未加载的 Controller。
     * 用户看到「请求的资源不存在，请确认后重试」。
     *
     * <p>排查：确认请求路径与 bff-web / paas 服务是否已重启到最新版本；确认网关转发规则正确。
     * 旧码 404 在两个 handler 里各自定义过，本码统一收口。
     */
    ROUTE_NOT_FOUND(20001, ErrorMeta.builder(20001, "请求的资源不存在，请确认后重试")
            .devMessage("NoResourceFoundException / HandlerMapping 未命中")
            .level(ErrorLevel.ERROR).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(404)
            .since("v1.2.0")
            .remark("旧码 404 在 commons 与 bff-web 两个 handler 里各自定义，本码统一收口")
            .build()),

    /**
     * 系统层参数不合法（请求体缺失、JSON 结构错误导致的通用参数问题）。
     *
     * <p>触发：框架层在绑定参数阶段就失败，且没有落到具体业务域的校验器。
     * 用户看到「请求参数不合法，请检查后重试」。
     *
     * <p>排查：先看请求体与 Content-Type；具体字段级问题应优先使用 VALIDATION 域（21）的码。
     */
    SYSTEM_PARAM_INVALID(20002, ErrorMeta.builder(20002, "提交的数据不合法，请检查后重试")
            .devMessage("框架层参数绑定/转换失败，未落到具体业务校验器")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 数据重复（唯一索引冲突、重复提交同一业务单号）。
     *
     * <p>触发：写入时命中数据库唯一约束。用户看到「数据已存在，请勿重复提交」。
     *
     * <p>排查：确认业务幂等键（订单号 / 编号）是否重复提交；重复键详情只在日志里看，不出屏。
     */
    SYSTEM_DUPLICATE_KEY(20004, ErrorMeta.builder(20004, "数据已存在，请勿重复提交")
            .devMessage("DuplicateKeyException：命中数据库唯一约束")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(8005, 10039, 10042, 10084, 10087)
            .since("v1.2.0")
            .remark("替代 commons handler 里 GlobalExceptionEnum.MYSQL_EXCEPTION_DATA_DUPLICATION 的裸码出口")
            .build()),

    /**
     * 查询为空（列表/详情查不到数据）。
     *
     * <p>触发：业务查询返回空集合或空对象，且语义上「没有数据」不是故障。
     * 用户看到空状态占位，<b>不弹 Toast</b>。
     *
     * <p>排查：这是 INFO 级别，正常业务结果，不需要排障。旧码 10060（数据查询为空）
     * 在 cmsadmin 曾被视为合法放行，本码继续按 EMPTY_STATE 放行，但改为显式声明。
     */
    SYSTEM_QUERY_RESULT_EMPTY(20007, ErrorMeta.builder(20007, "没有查询到相关数据")
            .devMessage("查询结果为空集合/空对象，非故障")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .legacyCodes(10026, 10060)
            .since("v1.2.0")
            .remark("cmsadmin 白名单码 10060 的新归属；保持放行行为，禁止弹 Toast")
            .build()),

    /**
     * 上游依赖调用失败（框架层捕获的通用下游异常）。
     *
     * <p>触发：调用其它微服务失败且未落到具体上游域。用户看到「依赖服务调用失败，请稍后重试」。
     *
     * <p>排查：优先看 Feign 超时配置与目标服务实例健康；具体通道请用 UPSTREAM 域（22）的码。
     */
    SYSTEM_UPSTREAM_CALL_FAILED(20008, ErrorMeta.builder(20008, "依赖服务调用失败，请稍后重试")
            .devMessage("框架层捕获的通用下游调用异常")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 系统配置缺失或未初始化（缺少必要的启动配置、字典未加载）。
     *
     * <p>触发：读取关键配置为空或配置项未初始化。用户看到「系统配置缺失，请联系管理员」。
     *
     * <p>排查：检查 Nacos 对应命名空间的配置项是否下发；确认 base 模块的配置字典已加载。
     * FATAL 级别，需人工介入。
     */
    SYSTEM_CONFIG_MISSING(20009, ErrorMeta.builder(20009, "系统配置缺失，请联系管理员")
            .devMessage("关键配置项为空或未初始化")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 检查 Nacos 对应命名空间的配置是否已下发；2. 确认 base 模块配置字典已加载；"
                    + "3. 若为新环境，联系运维补齐配置项后重启服务。")
            .since("v1.2.0")
            .build()),

    /**
     * 未知系统异常（兜底码，绝不返回 null 的出口）。
     *
     * <p>触发：任何未被具体分支识别的异常。用户看到「操作失败，请稍后重试」。
     *
     * <p>排查：后端只把 userMessage 给用户，根因（devMessage + 堆栈）只进日志。
     * 旧码 5000（bff-web 的「服务端异常：」拼根因）统一收敛到本码，禁止回显异常原文。
     */
    UNKNOWN_ERROR(20010, ErrorMeta.builder(20010, "操作失败，请稍后重试")
            .devMessage("未分类的系统异常，根因见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
              .troubleshooting("按请求路径 + 发生时间检索服务端日志，定位 handler 捕获点的原始堆栈。")
              .legacyCodes(2, 500, 5000, 10000, 10012, 10052)
              .since("v1.2.0")
              .remark("ErrorCodes.metaOf 的降级出口；旧码 5000 曾把异常根因拼进响应，已停止；"
                      + "HTTP 裸码 500 一并收口到本码")
            .build()),

    /**
     * 序列化/反序列化失败（JSON 解析、Feign 解码、RPC 编解码）。
     *
     * <p>触发：请求体或响应体无法解析。用户看到「数据解析失败，请稍后重试」。
     *
     * <p>排查：对比前后端字段类型与时间格式；确认上下游 DTO 版本一致。
     */
    SYSTEM_SERIALIZE_FAILED(20011, ErrorMeta.builder(20011, "数据解析失败，请稍后重试")
            .devMessage("序列化/反序列化失败（JSON 解析或 RPC 编解码异常）")
            .level(ErrorLevel.ERROR).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(16041)
            .since("v1.2.0")
            .build()),

    /**
     * 线程池拒绝（任务提交被拒，队列已满）。
     *
     * <p>触发：异步任务提交时线程池饱和。用户看到「系统繁忙，请稍后重试」。
     *
     * <p>排查：看线程池队列与拒绝策略配置；确认是否有慢任务堆积。FATAL 级别，需人工介入。
     */
    SYSTEM_THREAD_POOL_REJECTED(20012, ErrorMeta.builder(20012, "系统繁忙，请稍后重试")
            .devMessage("RejectedExecutionException：线程池队列已满")
            .level(ErrorLevel.FATAL).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .troubleshooting("1. 查看线程池队列深度与拒绝策略；2. 排查是否有慢任务堆积；"
                    + "3. 必要时扩容核心线程数或拆分任务类型。")
            .since("v1.2.0")
            .build()),

    /**
     * 数据库访问失败（DataAccessException，写入/读取都算）。
     *
     * <p>触发：commons handler 捕获到 DataAccessException。用户看到「数据保存失败，请稍后重试」。
     *
     * <p>排查：旧码 11009 在 BizCodeEnume 里其实是「账号尚未审核通过」，handler 复用它是历史错误；
     * 本码独立登记。检查数据库连接池与慢 SQL，FATAL 级别需人工介入。
     */
    DB_ACCESS_FAILED(20601, ErrorMeta.builder(20601, "数据保存失败，请稍后重试")
            .devMessage("DataAccessException：数据库连接或写入失败")
            .level(ErrorLevel.FATAL).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 检查数据库连接池是否打满；2. 查看慢 SQL 与锁等待；"
                    + "3. 确认 Seata 全局事务未悬挂。")
            .legacyCodes(10024, 10025, 10045, 11009, 11020)
            .since("v1.2.0")
            .remark("旧码 11009 在 BizCodeEnume 中是「账号尚未审核通过」，handler 复用它是历史错误")
            .build()),

    /**
     * SQL 语法/结构错误（BadSqlGrammarException、SQLSyntaxErrorException）。
     *
     * <p>触发：handler 捕获到 try to proceed invocation error 且根因是 SQL 语法异常。
     * 用户看到「数据查询异常，请稍后重试」。
     *
     * <p>排查：旧码 11003 在 BizCodeEnume 里是「账号或者密码错误」，handler 用它表示 syntax error
     * 属于错用；<b>SQL 原文绝不出屏</b>，只在日志里看。FATAL 级别需人工介入。
     */
    SQL_GRAMMAR_ERROR(20602, ErrorMeta.builder(20602, "数据查询异常，请稍后重试")
            .devMessage("SQL 语法或结构错误，SQL 原文见服务端日志")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 从服务端日志取 SQL 原文与 Mapper 全限定名；2. 核对表结构与字段是否随版本变更；"
                    + "3. 确认 MyBatis-Plus 版本与 mybatis 版本匹配。")
            .legacyCodes(8002, 11003)
            .since("v1.2.0")
            .remark("旧码 11003 在 BizCodeEnume 中是「账号或者密码错误」，handler 用它表示语法错误属于错用")
            .build()),

    /**
     * 查询返回多条记录（预期单条却返回多条）。
     *
     * <p>触发：Mapper 用 selectOne 但命中多条。用户看到「查询到多条记录，请联系管理员」。
     *
     * <p>排查：旧码 10000 在 BizCodeEnume 里是「系统未知异常」，语义不明；
     * 本码明确为 TooManyResultsException。检查唯一性约束是否被破坏。
     */
    TOO_MANY_RESULTS(20603, ErrorMeta.builder(20603, "查询到多条记录，请联系管理员")
            .devMessage("TooManyResultsException：预期单条但命中多条")
            .level(ErrorLevel.ERROR).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 定位报出的 Mapper 方法；2. 检查对应业务唯一性约束是否被破坏；3. 清理重复数据。")
            .legacyCodes(10000)
            .since("v1.2.0")
            .remark("旧码 10000 在 BizCodeEnume 中是「系统未知异常」，语义不明")
            .build()),

    /**
     * MyBatis 框架异常（MyBatisSystemException）。
     *
     * <p>触发：handler 捕获到 MyBatis 自身异常。用户看到「数据访问异常，请稍后重试」。
     *
     * <p>排查：旧码 10001 在 BizCodeEnume 里是「参数格式异常」，属于错用；
     * 本码明确为 MyBatis 框架层问题。FATAL 级别需人工介入。
     */
    MYBATIS_ERROR(20604, ErrorMeta.builder(20604, "数据访问异常，请稍后重试")
            .devMessage("MyBatisSystemException：持久层框架异常，堆栈见服务端日志")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 从日志取原始堆栈；2. 核对 Mapper XML 与接口签名是否一致；"
                    + "3. 确认 mybatis 与 mybatis-plus 版本匹配（当前锁定 3.5.16 / 3.5.7）。")
            .legacyCodes(10001, 10008)
            .since("v1.2.0")
            .remark("旧码 10001 在 BizCodeEnume 中是「参数格式异常」，属于错用")
            .build()),
    /**
     * 申请提交失败，请稍后重试。
     *
     * <p>触发/排查：业务申请类操作持久化失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    SYSTEM_APPLY_FAILED(20101, ErrorMeta.builder(20101, "申请提交失败，请稍后重试")
            .devMessage("业务申请类操作持久化失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(10032, 14011)
            .since("v1.3.0")
            .build()),

    /**
     * 审核失败，请稍后重试。
     *
     * <p>触发/排查：审批/审核流程执行失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    SYSTEM_AUDIT_FAILED(20102, ErrorMeta.builder(20102, "审核失败，请稍后重试")
            .devMessage("审批/审核流程执行失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(10009, 10053)
            .since("v1.3.0")
            .build()),

    /**
     * 导出失败，数据量过大或生成失败。
     *
     * <p>触发/排查：导出任务超限或生成失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    SYSTEM_EXPORT_FAILED(20103, ErrorMeta.builder(20103, "导出失败，数据量过大或生成失败")
            .devMessage("导出任务超限或生成失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(10067, 14056)
            .since("v1.3.0")
            .build()),

    /**
     * 资源被占用，无法执行该操作。
     *
     * <p>触发/排查：待操作资源仍被其它数据引用。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    SYSTEM_RESOURCE_IN_USE(20104, ErrorMeta.builder(20104, "资源被占用，无法执行该操作")
            .devMessage("待操作资源仍被其它数据引用")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(10038, 10093, 18011)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    SystemErrorCode(int code, ErrorMeta meta) {
        this.code = code;
        this.meta = meta;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public ErrorMeta meta() {
        return meta;
    }
}
