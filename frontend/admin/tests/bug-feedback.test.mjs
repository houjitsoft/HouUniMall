import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { JSDOM } from 'jsdom'

const source = readFileSync(new URL('../../common/bug-report/bug-report.js', import.meta.url), 'utf8')

async function setup(response = { code: 1, data: { id: 'central-123' } }, httpOk = true) {
  const dom = new JSDOM('<!doctype html><title>测试菜单</title><body></body>', {
    url: 'https://tenant.example/admin/list?token=secret#access_token=secret', runScripts: 'outside-only'
  })
  const { window } = dom
  const calls = []
  const revoked = []
  let objectId = 0
  window.URL.createObjectURL = () => `blob:https://tenant.example/${++objectId}`
  window.URL.revokeObjectURL = url => revoked.push(url)
  window.localStorage.setItem('token', 'host-jwt')
  const writes = []
  window.Storage.prototype.setItem = (...args) => writes.push(args)
  window.fetch = async (url, options) => {
    calls.push({ url, options })
    if (response instanceof Error) throw response
    return { ok: httpOk, status: httpOk ? 200 : 500, json: async () => response }
  }
  window.eval(source)
  window.document.dispatchEvent(new window.Event('DOMContentLoaded'))
  const get = id => window.document.getElementById(`ubr-${id}`)
  get('bug').click()
  get('error-content').value = '错误正文\n第二行'
  get('reporter').value = '反馈人'
  get('privacy').checked = true
  const add = (sizes = [100], type = 'image/png') => {
    Object.defineProperty(get('file'), 'files', { configurable: true,
      value: sizes.map((size, index) => new window.File([new Uint8Array(size)], `private-${index}.png`, { type })) })
    get('file').dispatchEvent(new window.Event('change'))
  }
  const submit = async () => {
    get('form').dispatchEvent(new window.Event('submit', { cancelable: true }))
    await new Promise(resolve => setImmediate(resolve))
  }
  return { dom, window, calls, revoked, writes, get, add, submit }
}

test('知识库入口与Bug并存，复用宿主桥接，不写入渠道配置', async () => {
  const app = await setup()
  try {
    let opened = 0
    app.window.__HOU_OPEN_KNOWLEDGE__ = async () => { opened++; return true }
    app.get('main').click()
    assert.equal(app.get('knowledge').textContent.includes('知识库'), true)
    app.get('knowledge').click()
    await new Promise(resolve => setImmediate(resolve))
    assert.equal(opened, 1)
    assert.equal(app.get('knowledge').disabled, false)
    assert.equal(app.calls.length, 0)
    assert.equal(app.writes.length, 0)
  } finally { app.dom.window.close() }
})

test('宿主知识库未配置或打开异常时明确提示且保留Bug表单', async () => {
  for (const bridge of [undefined, async () => false, async () => { throw new Error('private-config') }]) {
    const app = await setup()
    try {
      app.window.__HOU_OPEN_KNOWLEDGE__ = bridge
      app.get('knowledge').click()
      await new Promise(resolve => setImmediate(resolve))
      assert.match(app.get('toast').textContent, /知识库/)
      assert.doesNotMatch(app.get('toast').textContent, /private-config/)
      assert.equal(app.get('error-content').value, '错误正文\n第二行')
      assert.equal(app.get('knowledge').disabled, false)
    } finally { app.dom.window.close() }
  }
})

test('成功必须有平台 code=1 与 id；单次 multipart、显式本域 JWT、无预上传/AI/本地台账', async () => {
  const app = await setup()
  try {
    app.add()
    assert.equal(app.calls.length, 0)
    await app.submit()
    assert.equal(app.calls.length, 1)
    const { url, options } = app.calls[0]
    assert.equal(url, 'https://tenant.example/web/bug-feedback/submit')
    assert.equal(options.headers.Authorization, 'Bearer host-jwt')
    assert.equal(options.mode, 'same-origin')
    assert.equal(options.redirect, 'error')
    assert.equal(options.credentials, 'same-origin')
    assert.equal(options.body.getAll('files').length, 1)
    assert.equal(options.body.getAll('files')[0].name, 'screenshot-1.png')
    const payload = JSON.parse(options.body.get('payload'))
    assert.deepEqual(Object.keys(payload).sort(), ['errorContent', 'serviceCode', 'menuName', 'pageUrl', 'apiUrl', 'reporterName', 'clientVersion', 'sourceSite', 'analysis'].sort())
    assert.equal(payload.pageUrl, 'https://tenant.example/admin/list')
    assert.equal(payload.sourceSite, 'https://tenant.example')
    assert.equal(payload.analysis, '')
    assert.equal(app.get('error-content').value, '')
    assert.equal(app.get('thumbs').children.length, 0)
    assert.equal(app.revoked.length, 1)
    assert.match(app.get('toast').textContent, /central-123/)
    assert.equal(app.writes.length, 0)
  } finally { app.dom.window.close() }
})

for (const [label, response, ok] of [
  ['HTTP失败', { code: 1, data: { id: 'not-success' } }, false],
  ['缺少编号', { code: 1, data: {} }, true],
  ['code200不接受', { code: 200, data: { id: 'x' } }, true],
  ['code0不接受', { code: 0, data: { id: 'x' } }, true],
  ['网络异常', new Error('network'), true]
]) {
  test(`${label}保留表单、图片及隐私确认，可重试`, async () => {
    const app = await setup(response, ok)
    try {
      app.add()
      await app.submit()
      assert.equal(app.get('error-content').value, '错误正文\n第二行')
      assert.equal(app.get('reporter').value, '反馈人')
      assert.equal(app.get('privacy').checked, true)
      assert.equal(app.get('thumbs').children.length, 1)
      assert.equal(app.get('submit').disabled, false)
      assert.equal(app.revoked.length, 0)
      assert.match(app.get('toast').textContent, /已保留/)
      assert.equal(app.writes.length, 0)
    } finally { app.dom.window.close() }
  })
}

test('页面与失败接口去 query/fragment/URL 用户凭据，采集不记录异常正文', async () => {
  const app = await setup()
  try {
    await app.window.fetch('https://username:password@api.example/order?jwt=secret#secret')
    app.calls.length = 0
    await app.submit()
    const payload = JSON.parse(app.calls[0].options.body.get('payload'))
    assert.equal(payload.apiUrl, 'https://api.example/order')
    assert.doesNotMatch(JSON.stringify(payload), /secret|password|username|access_token/)
  } finally { app.dom.window.close() }
})

test('重复注入仅一个根节点、样式、监听与网络包装，不覆盖知识库 runtime', async () => {
  const app = await setup()
  try {
    const runtime = { token: 'runtime-only' }
    app.window.WeKnora = runtime
    const fetch = app.window.fetch
    app.window.eval(source)
    app.window.document.dispatchEvent(new app.window.Event('DOMContentLoaded'))
    assert.equal(app.window.document.querySelectorAll('#unimall-bug-report-root').length, 1)
    assert.equal(app.window.document.querySelectorAll('#unimall-bug-report-style').length, 1)
    assert.equal(app.window.fetch, fetch)
    assert.equal(app.window.WeKnora, runtime)
    assert.equal(app.window.document.querySelector('script'), null)
    await app.submit()
    assert.equal(app.calls.length, 1)
  } finally { app.dom.window.close() }
})

test('图片4张/4MB/总12MB约束；禁止SVG；移除释放 objectURL；隐私未确认不提交', async () => {
  const app = await setup()
  try {
    app.add([4 * 1024 * 1024 + 1])
    app.add([10], 'image/svg+xml')
    assert.equal(app.get('thumbs').children.length, 0)
    app.add([4 * 1024 * 1024, 4 * 1024 * 1024, 4 * 1024 * 1024, 1])
    assert.equal(app.get('thumbs').children.length, 3)
    app.get('thumbs').querySelector('[data-del="0"]').click()
    assert.equal(app.revoked.length, 1)
    app.add([1, 1, 1])
    assert.equal(app.get('thumbs').children.length, 4)
    app.get('privacy').checked = false
    await app.submit()
    assert.equal(app.calls.length, 0)
  } finally { app.dom.window.close() }
})

test('进行中重复提交/增删图片不改变请求快照', async () => {
  const app = await setup()
  try {
    let resolve
    app.window.fetch = () => new Promise(done => { resolve = done })
    app.add()
    app.get('form').dispatchEvent(new app.window.Event('submit', { cancelable: true }))
    app.get('form').dispatchEvent(new app.window.Event('submit', { cancelable: true }))
    app.get('thumbs').querySelector('[data-del="0"]').click()
    assert.equal(app.get('thumbs').children.length, 1)
    resolve({ ok: true, json: async () => ({ code: 1, data: { id: 'ok' } }) })
    await new Promise(done => setImmediate(done))
    assert.equal(app.get('error-content').value, '')
  } finally { app.dom.window.close() }
})
