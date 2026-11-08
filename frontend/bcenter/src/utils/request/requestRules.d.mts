export function resolveSameOriginHttpUrl(raw: unknown, base: string): string | null

export type ResponseFailureAction = 'retry' | 'notify' | 'reject'

export function getResponseFailureAction(options: {
    code?: string
    method?: string
    isOpenRetry: boolean
    retriesAttempted: number
    retryLimit: number
}): ResponseFailureAction

export function getResponseFailureActionForError(error: {
    code?: string
    config?: {
        method?: string
        retryCount?: number
        requestOptions?: {
            isOpenRetry?: boolean
            retryCount?: number
        }
    }
}): ResponseFailureAction

export function executeResponseFailure<T>(options: {
    action: ResponseFailureAction
    error: unknown
    retry: () => Promise<T>
    notify: (error: unknown) => void
}): Promise<T>

export function createInvalidSessionHandler(options: {
    clearAuth: () => void
    isLoginPage: () => boolean
    redirectToLogin: () => unknown | Promise<unknown>
}): {
    observeAuthenticatedSession: (token: unknown) => void
    handle: () => Promise<void>
}
