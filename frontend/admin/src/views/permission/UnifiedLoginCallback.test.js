import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'

const read = (path) => readFileSync(new URL(path, import.meta.url), 'utf8')

describe('统一登录公开回调页', () => {
  it('注册公开渠道回调路由', () => {
    const router = read('../../router/index.js')

    expect(router).toMatch(/path:\s*['"]\/plugin\/unilogin\/callback\/:channel['"]/) 
    expect(router).toMatch(/name:\s*['"]UnifiedLoginCallback['"]/) 
    expect(router).toMatch(/meta:\s*\{[^}]*public:\s*true/)
  })

  it('按渠道提交 code/state 并回传登录结果', () => {
    const page = read('./UnifiedLoginCallback.vue')

    expect(page).toMatch(/route\.params\?\.channel/)
    expect(page).toMatch(/\/web\/auth\/unilogin\/\$\{channel\.value\}\/callback/)
    expect(page).toMatch(/params:\s*\{\s*code,\s*state\s*\}/)
    expect(page).toMatch(/HOUCLOUD_UNILOGIN/)
  })
})

