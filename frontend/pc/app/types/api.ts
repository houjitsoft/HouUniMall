/**
 * API 响应相关类型定义
 */

/** 空请求参数（无 body 的 POST 或可选 params 的 GET） */
export type EmptyRequestParams = Record<string, never> | object

/** 可选 API 请求参数（用于 params 可选的服务方法） */
export type ApiParams = Record<string, any>

/**
 * API 响应数据接口
 *
 * 定义了后端返回的数据格式
 */
export interface ApiResponse<T = unknown> {
    code: number        // 响应码，0 表示成功，其他值表示错误
    message: string     // 响应信息，成功时通常为 'success'
    data: T             // 响应数据，类型可泛化
}
