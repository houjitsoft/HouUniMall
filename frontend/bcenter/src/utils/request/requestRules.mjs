export function resolveSameOriginHttpUrl(raw, base) {
    if (typeof raw !== 'string' || !raw.trim()) {
        return null
    }

    try {
        const baseUrl = new URL(base)
        const target = new URL(raw.trim(), baseUrl)
        if (
            !['http:', 'https:'].includes(baseUrl.protocol) ||
            !['http:', 'https:'].includes(target.protocol) ||
            target.origin !== baseUrl.origin ||
            target.username ||
            target.password
        ) {
            return null
        }
        return target.href
    } catch {
        return null
    }
}

export function getResponseFailureAction({
    code,
    method,
    isOpenRetry,
    retriesAttempted,
    retryLimit
}) {
    if (code === 'ERR_CANCELED') {
        return 'reject'
    }
    const retryable = ['ECONNABORTED', 'ETIMEDOUT', 'ERR_NETWORK'].includes(code)
    if (
        retryable &&
        isOpenRetry &&
        String(method).toUpperCase() !== 'POST' &&
        retriesAttempted < retryLimit
    ) {
        return 'retry'
    }
    return 'notify'
}

export function getResponseFailureActionForError(error) {
    const config = error?.config
    const requestOptions = config?.requestOptions
    return getResponseFailureAction({
        code: error?.code,
        method: config?.method,
        isOpenRetry: requestOptions?.isOpenRetry ?? false,
        retriesAttempted: config?.retryCount ?? 0,
        retryLimit: requestOptions?.retryCount ?? 0
    })
}

export function executeResponseFailure({ action, error, retry, notify }) {
    if (action === 'retry') {
        return retry()
    }
    if (action === 'notify') {
        notify(error)
    }
    return Promise.reject(error)
}

export function createInvalidSessionHandler({ clearAuth, isLoginPage, redirectToLogin }) {
    let invalidationHandled = false
    let redirectPending = false

    return {
        observeAuthenticatedSession(token) {
            if (typeof token === 'string' && token.trim()) {
                invalidationHandled = false
            }
        },
        handle() {
            if (!invalidationHandled) {
                invalidationHandled = true
                clearAuth()
            }
            if (redirectPending || isLoginPage()) {
                return Promise.resolve()
            }

            redirectPending = true
            let redirectResult
            try {
                redirectResult = redirectToLogin()
            } catch {
                redirectPending = false
                return Promise.resolve()
            }
            return Promise.resolve(redirectResult)
                .catch(() => undefined)
                .finally(() => {
                    redirectPending = false
                })
        }
    }
}
