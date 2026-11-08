import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import test from 'node:test'

const read = (path) => readFileSync(new URL(path, import.meta.url), 'utf8')

test('商城端提供统一登录授权回调页并把 code/state 交给对应渠道', () => {
    const callbackPage = '../app/pages/plugin/unilogin/callback/[channel].vue'

    assert.ok(existsSync(new URL(callbackPage, import.meta.url)), '缺少统一登录授权回调页')

    const page = read(callbackPage)
    assert.match(page, /route\.params\.channel/)
    assert.match(page, /route\.query/)
    assert.match(page, /completeCallback\(channel, code, state\)/)
    assert.match(page, /登录成功/)
})

test('统一登录协议封装提供免登录的授权回调请求', () => {
    const composable = read('../app/composables/useUniLogin.ts')

    assert.match(
        composable,
        /\/web\/auth\/unilogin\/\$\{channel\}\/callback/
    )
    assert.match(composable, /params:\s*\{\s*code,\s*state\s*\}/)
    assert.match(composable, /skipToken:\s*true/)
    assert.match(composable, /completeCallback/)
})

