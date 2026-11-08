import assertModule from 'assert'

import {
    createInvalidSessionHandler,
    executeResponseFailure,
    getResponseFailureAction,
    getResponseFailureActionForError,
    resolveSameOriginHttpUrl
} from '../src/utils/request/requestRules.mjs'

const assert = assertModule.strict
const testCases = []

function test(name, run) {
    testCases.push({ name, run })
}

test('OPEN_NEW_PAGE accepts only same-origin http URLs', () => {
    const base = 'https://mall.example.com/admin/orders?tab=pending'

    assert.equal(
        resolveSameOriginHttpUrl('/orders/42?from=notice', base),
        'https://mall.example.com/orders/42?from=notice'
    )
    assert.equal(
        resolveSameOriginHttpUrl('https://mall.example.com/reports', base),
        'https://mall.example.com/reports'
    )

    for (const unsafe of [
        'javascript:alert(1)',
        'data:text/html,secret',
        'https://evil.example.com/phish',
        'https://mall.example.com.evil.test/phish',
        'https://user:password@mall.example.com/private',
        'http://[invalid'
    ]) {
        assert.equal(resolveSameOriginHttpUrl(unsafe, base), null, unsafe)
    }
})

test('GET retries finish before the error hook runs once', async () => {
    const events = []
    const error = new Error('network details')
    let retriesAttempted = 0

    const request = () => {
        events.push(`request:${retriesAttempted}`)
        const action = getResponseFailureAction({
            code: 'ERR_NETWORK',
            method: 'GET',
            isOpenRetry: true,
            retriesAttempted,
            retryLimit: 2
        })
        return executeResponseFailure({
            action,
            error,
            retry: () => {
                retriesAttempted += 1
                return request()
            },
            notify: () => events.push('hook')
        })
    }

    await assert.rejects(request(), error)
    assert.deepEqual(events, ['request:0', 'request:1', 'request:2', 'hook'])
})

test('canceled requests reject without retrying or notifying', async () => {
    const events = []
    const error = new Error('canceled')
    const action = getResponseFailureAction({
        code: 'ERR_CANCELED',
        method: 'GET',
        isOpenRetry: true,
        retriesAttempted: 0,
        retryLimit: 2
    })

    await assert.rejects(
        executeResponseFailure({
            action,
            error,
            retry: () => {
                events.push('retry')
                return Promise.resolve()
            },
            notify: () => events.push('hook')
        }),
        error
    )
    assert.deepEqual(events, [])
})

test('network errors without Axios config become terminal notifications', () => {
    assert.equal(getResponseFailureActionForError({ code: 'ERR_NETWORK' }), 'notify')
    assert.equal(getResponseFailureActionForError({ code: 'ERR_CANCELED' }), 'reject')
})

test('invalid sessions clear once on login and reset for a new authenticated session', async () => {
    let currentPath = '/login'
    let clearCount = 0
    let redirectCount = 0
    let finishRedirect
    const handler = createInvalidSessionHandler({
        clearAuth: () => {
            clearCount += 1
        },
        isLoginPage: () => currentPath === '/login',
        redirectToLogin: () => {
            redirectCount += 1
            return new Promise((resolve) => {
                finishRedirect = () => {
                    currentPath = '/login'
                    resolve()
                }
            })
        }
    })

    await Promise.all([handler.handle(), handler.handle()])
    assert.equal(clearCount, 1)
    assert.equal(redirectCount, 0)

    handler.observeAuthenticatedSession('new-shop-token')
    currentPath = '/orders'
    const first = handler.handle()
    const concurrent = handler.handle()
    assert.equal(clearCount, 2)
    assert.equal(redirectCount, 1)
    finishRedirect()
    await Promise.all([first, concurrent])
})

async function runTests() {
    for (const [index, testCase] of testCases.entries()) {
        try {
            await testCase.run()
            console.log(`ok ${index + 1} - ${testCase.name}`)
        } catch (error) {
            console.error(`not ok ${index + 1} - ${testCase.name}`)
            throw error
        }
    }
    console.log(`1..${testCases.length}`)
}

runTests().catch((error) => {
    console.error(error)
    process.exitCode = 1
})
