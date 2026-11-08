import test from 'node:test'
import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const projectDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const port = 43191
const origin = `http://127.0.0.1:${port}`

const waitForPage = async (process) => {
    const deadline = Date.now() + 90_000
    let lastError

    while (Date.now() < deadline) {
        if (process.exitCode != null) {
            throw new Error(`Nuxt exited before the test page was ready (exit ${process.exitCode})`)
        }
        try {
            const response = await fetch(origin)
            if (response.ok) return await response.text()
            lastError = new Error(`Unexpected HTTP ${response.status}`)
        } catch (error) {
            lastError = error
        }
        await new Promise((resolve) => setTimeout(resolve, 500))
    }

    throw lastError ?? new Error('Timed out waiting for Nuxt')
}

test('商城头部固定展示 AI 助手并将其放在购物车前', { timeout: 120_000 }, async () => {
    const nuxt = spawn(
        process.execPath,
        ['node_modules/nuxt/bin/nuxt.mjs', 'dev', '--host', '127.0.0.1', '--port', String(port)],
        {
            cwd: projectDir,
            detached: true,
            stdio: 'ignore'
        }
    )

    try {
        const html = await waitForPage(nuxt)
        const actionsIndex = html.indexOf('class="header-icons"')
        const aiIndex = html.indexOf('AI助手', actionsIndex)
        const cartIndex = html.indexOf('购物车', actionsIndex)
        const platformServiceIndex = html.indexOf('平台客服', actionsIndex)

        assert.ok(actionsIndex >= 0, '页面应渲染头部快捷入口')
        assert.ok(aiIndex >= 0, '头部应固定渲染 AI助手')
        assert.ok(cartIndex > aiIndex, 'AI助手应位于购物车之前')
        assert.equal(platformServiceIndex, -1, '头部不应再渲染平台客服')
    } finally {
        if (nuxt.pid) {
            try {
                process.kill(-nuxt.pid, 'SIGTERM')
            } catch {
                // Process already exited.
            }
        }
    }
})
