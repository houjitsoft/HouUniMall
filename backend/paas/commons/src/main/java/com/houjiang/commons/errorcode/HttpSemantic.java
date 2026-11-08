package com.houjiang.commons.errorcode;

/**
 * HTTP 语义标注（仅元数据）。
 *
 * <p>解决的问题：HouCloud 现状是 HTTP 恒 200、业务结果靠 body 的 <code>code</code> 判断，
 * 本轮明确不改这个策略。本枚举只做语义留痕，供网关、监控与未来协议演进使用，
 * <b>不会影响实际 HTTP 状态码</b>，也不进响应体。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum HttpSemantic {

    /** 200 OK。 */
    OK_200,

    /** 400 Bad Request：参数不合法（不含「方法不支持」，那是 405）。 */
    BAD_REQUEST_400,

    /** 405 Method Not Allowed：请求方法与 Mapping 不匹配。 */
    METHOD_NOT_ALLOWED_405,

    /** 401 Unauthorized：未登录或凭证失效。 */
    UNAUTHORIZED_401,

    /** 403 Forbidden：已登录但无权限。 */
    FORBIDDEN_403,

    /** 404 Not Found：资源或路由不存在。 */
    NOT_FOUND_404,

    /** 409 Conflict：重复键/并发冲突。 */
    CONFLICT_409,

    /** 429 Too Many Requests：限流。 */
    TOO_MANY_REQUESTS_429,

    /** 500 Internal Server Error：系统未知异常。 */
    INTERNAL_500,

    /** 503 Service Unavailable：下游不可达。 */
    UNAVAILABLE_503
}
