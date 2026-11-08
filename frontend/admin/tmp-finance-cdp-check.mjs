import { writeFile } from 'node:fs/promises'
import { join } from 'node:path'
import { tmpdir } from 'node:os'

const port = 9227
const pages = await fetch(`http://127.0.0.1:${port}/json/list`).then((response) => response.json())
const target = pages.find((page) => page.type === 'page' && page.url.startsWith('http://127.0.0.1:13001/plugin/finance/'))
if (!target) throw new Error('No authenticated local Finance page on CDP port 9227')

const socket = new WebSocket(target.webSocketDebuggerUrl)
const pending = new Map()
let sequence = 0
let network = []
const requests = new Map()

await new Promise((resolve, reject) => {
  socket.addEventListener('open', resolve, { once: true })
  socket.addEventListener('error', reject, { once: true })
})

socket.addEventListener('message', (event) => {
  const message = JSON.parse(event.data)
  if (message.id) {
    const waiter = pending.get(message.id)
    if (!waiter) return
    pending.delete(message.id)
    if (message.error) waiter.reject(new Error(message.error.message))
    else waiter.resolve(message.result)
    return
  }
  if (message.method === 'Network.requestWillBeSent') {
    requests.set(message.params.requestId, {
      method: message.params.request.method,
      url: message.params.request.url
    })
  }
  if (message.method === 'Network.responseReceived') {
    const request = requests.get(message.params.requestId) || {}
    network.push({
      requestId: message.params.requestId,
      method: request.method,
      url: message.params.response.url,
      status: message.params.response.status
    })
  }
})

const call = (method, params = {}) => new Promise((resolve, reject) => {
  const id = ++sequence
  pending.set(id, { resolve, reject })
  socket.send(JSON.stringify({ id, method, params }))
})

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

await call('Page.enable')
await call('Runtime.enable')
await call('Network.enable')
await call('Network.setCacheDisabled', { cacheDisabled: true })
await call('Emulation.setDeviceMetricsOverride', {
  width: 1600,
  height: 900,
  deviceScaleFactor: 1,
  mobile: false
})

const evaluate = async (expression) => {
  const result = await call('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true })
  if (result.exceptionDetails) throw new Error(result.exceptionDetails.text || 'Runtime evaluation failed')
  return result.result.value
}

const navigate = async (path) => {
  network = []
  requests.clear()
  await call('Page.navigate', { url: `http://127.0.0.1:13001${path}` })
  await wait(9000)
}

const pageState = () => evaluate(`(() => ({
  title: document.title,
  url: location.href,
  body: document.body.innerText.slice(0, 4000),
  rows: document.querySelectorAll('.el-table__body tbody tr').length,
  visibleDialogs: [...document.querySelectorAll('.el-dialog')].filter((element) => {
    const style = getComputedStyle(element)
    return style.display !== 'none' && style.visibility !== 'hidden'
  }).length,
  viewport: {
    width: innerWidth,
    height: innerHeight,
    scrollWidth: document.documentElement.scrollWidth,
    scrollHeight: document.documentElement.scrollHeight
  }
}))()`)

const clickByText = async (selector, text) => {
  const box = await evaluate(`(() => {
    const element = [...document.querySelectorAll(${JSON.stringify(selector)})]
      .find((item) => item.textContent.trim().includes(${JSON.stringify(text)}))
    if (!element) return null
    const rect = element.getBoundingClientRect()
    return { x: rect.x, y: rect.y, width: rect.width, height: rect.height }
  })()`)
  if (!box) throw new Error(`Cannot find ${selector} containing ${text}`)
  const x = box.x + box.width / 2
  const y = box.y + box.height / 2
  await call('Input.dispatchMouseEvent', { type: 'mousePressed', x, y, button: 'left', clickCount: 1 })
  await call('Input.dispatchMouseEvent', { type: 'mouseReleased', x, y, button: 'left', clickCount: 1 })
}

const clickProjectSelect = async () => {
  const box = await evaluate(`(() => {
    const dialog = [...document.querySelectorAll('.el-dialog')].find((element) => {
      const style = getComputedStyle(element)
      return style.display !== 'none' && style.visibility !== 'hidden'
    })
    if (!dialog) return null
    const item = [...dialog.querySelectorAll('.el-form-item')]
      .find((element) => element.querySelector('.el-form-item__label')?.textContent.trim() === '项目')
    const wrapper = item?.querySelector('.el-select__wrapper') || item?.querySelector('.el-select')
    if (!wrapper) return null
    const rect = wrapper.getBoundingClientRect()
    return { x: rect.x, y: rect.y, width: rect.width, height: rect.height }
  })()`)
  if (!box) throw new Error('Cannot find project select in active dialog')
  const x = box.x + box.width / 2
  const y = box.y + box.height / 2
  await call('Input.dispatchMouseEvent', { type: 'mousePressed', x, y, button: 'left', clickCount: 1 })
  await call('Input.dispatchMouseEvent', { type: 'mouseReleased', x, y, button: 'left', clickCount: 1 })
}

const responseSummary = async (matcher) => {
  const entries = network.filter((entry) => matcher.test(entry.url))
  const summary = []
  for (const entry of entries) {
    let body = null
    try {
      const result = await call('Network.getResponseBody', { requestId: entry.requestId })
      body = result.base64Encoded
        ? Buffer.from(result.body, 'base64').toString('utf8')
        : result.body
    } catch {}
    let json = null
    try { json = JSON.parse(body) } catch {}
    summary.push({
      method: entry.method,
      url: entry.url,
      status: entry.status,
      code: json?.code,
      message: json?.message || json?.msg,
      listSize: json?.data?.list?.length ?? json?.data?.records?.length
    })
  }
  return summary
}

const screenshot = async (name) => {
  const result = await call('Page.captureScreenshot', { format: 'png', fromSurface: true })
  const path = join(tmpdir(), name)
  await writeFile(path, Buffer.from(result.data, 'base64'))
  return path
}

const results = {}

await navigate('/plugin/finance/income-book')
results.incomeInitial = {
  state: await pageState(),
  finance: await responseSummary(/\/web\/plugin\/finance\/(income\/list|income\/types|subjects\/all)/),
  pm: await responseSummary(/\/web\/(?:plugin\/)?pm\//),
  screenshot: await screenshot('houcloud-finance-income-book.png')
}

await clickByText('button', '新增收入')
await wait(1200)
await clickProjectSelect()
await wait(4500)
results.incomeProjectSelect = {
  state: await pageState(),
  pm: await responseSummary(/\/web\/(?:plugin\/)?pm\//),
  screenshot: await screenshot('houcloud-finance-income-project-select.png')
}

await navigate('/plugin/finance/spending-book')
results.spendingInitial = {
  state: await pageState(),
  finance: await responseSummary(/\/web\/plugin\/finance\/(spending\/list|spending\/types|subjects\/all)/),
  pm: await responseSummary(/\/web\/(?:plugin\/)?pm\//),
  screenshot: await screenshot('houcloud-finance-spending-book.png')
}

await clickByText('button', '新增支出')
await wait(1200)
await clickProjectSelect()
await wait(4500)
results.spendingProjectSelect = {
  state: await pageState(),
  pm: await responseSummary(/\/web\/(?:plugin\/)?pm\//)
}

console.log(JSON.stringify(results, null, 2))
socket.close()
