import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { test } from 'node:test'
import vm from 'node:vm'

const api = await readFile(new URL('../src/api/join.js', import.meta.url), 'utf8')
const page = await readFile(new URL('../src/views/join/step3.vue', import.meta.url), 'utf8')

test('入驻验证码使用已导入的第三方请求通道', () => {
  const alias = api.match(/joinThird\s+as\s+(\w+)/)[1]
  let request
  const context = vm.createContext({ [alias]: (config) => { request = config; return Promise.resolve({ code: 1 }) } })
  const fn = api.match(/export function sendPhoneCode\(data\) \{[\s\S]*?\n\}/)[0].replace('export ', '')
  vm.runInContext(fn, context)
  context.sendPhoneCode({ userMobile: 'test@example.com' })
  assert.equal(request.url, '/sms/verifiPhoneCode2')
  assert.equal(request.method, 'post')
  assert.equal(request.data.userMobile, 'test@example.com')
})

function setup(overrides = {}) {
  const callbacks = {}
  const state = {
    canClick: { value: true }, totalTime: { value: 60 }, content: { value: '获取验证码' },
    originType: { value: 0 }, ruleForm: { contactPhone: ' test@example.com ', internationalDialingCode: '+86' },
    formshopName: {}, addMobile: {},
    phonecheck: async () => ({ data: 1 }), sendPhoneCode: async () => ({ code: 1 }),
    ElMessage: { warning() {}, error() {} }, ElMessageBox: { confirm: async () => {} },
    safeOpenUrl() {}, baseUrl: 'https://example.com',
    window: { setInterval(fn) { callbacks.tick = fn; return 42 }, clearInterval(id) { callbacks.cleared = id } },
    onBeforeUnmount(fn) { callbacks.unmount = fn }, ...overrides,
  }
  const context = vm.createContext(state)
  const handler = page.slice(page.indexOf('async function handleGetCode()'), page.indexOf('//店铺所在地址'))
  vm.runInContext(handler, context)
  return { state, callbacks, run: () => context.handleGetCode() }
}

test('空账号不锁死按钮也不发送验证码', async () => {
  const { state, run } = setup({ ruleForm: { contactPhone: ' ' }, phonecheck: () => { throw Error('不应发送') } })
  await run()
  assert.equal(state.canClick.value, true)
})

for (const stage of ['phonecheck', 'sendPhoneCode']) {
  test(`${stage} 拒绝后按钮恢复且不会冒出未处理拒绝`, async () => {
    const { state, run } = setup({ [stage]: async () => { throw Error('网络错误') } })
    await run()
    assert.equal(state.canClick.value, true)
    assert.equal(state.content.value, '获取验证码')
  })
}

test('发送成功才倒计时，完成后可重发，卸载清理定时器', async () => {
  const { state, callbacks, run } = setup()
  await run()
  assert.equal(state.addMobile.userMobile, 'test@example.com')
  assert.equal(state.canClick.value, false)
  assert.equal(state.content.value, '60s后重新发送')
  for (let i = 0; i < 60; i++) callbacks.tick()
  assert.equal(state.canClick.value, true)
  assert.equal(state.content.value, '重新发送验证码')
  assert.equal(callbacks.cleared, 42)
  await run()
  callbacks.unmount()
  assert.equal(callbacks.cleared, 42)
})

test('已注册账号在新注册入口取消登录引导后可重新填写', async () => {
  const { state, run } = setup({ originType: { value: 1 }, ElMessageBox: { confirm: async () => { throw 'cancel' } } })
  await run()
  assert.equal(state.canClick.value, true)
})
