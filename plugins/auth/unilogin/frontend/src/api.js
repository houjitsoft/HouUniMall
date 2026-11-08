import { callHost } from './context.js'

function unwrap(response) {
  // 宿主 request 拦截器已返回业务体；不同通道可能再包一层 data
  if (response && typeof response === 'object' && 'data' in response && !('status' in response)) {
    return response.data || {}
  }
  return response || {}
}

/**
 * 按渠道创建扫码登录接口
 *
 * 通道形如 /web/auth/unilogin/{channel}/** —— 渠道编码即路径参数，
 * 后端由 LoginChannelRegistry 按配置解析，新增渠道不需要后端加接口。
 * @param {object} channel channels.js 中的渠道描述
 */
export function createAuthApi(channel) {
  const base = `web/auth/unilogin/${channel.code}`
  return {
    /** 获取扫码页地址；enabled !== true 表示服务端尚未配置该渠道凭据 */
    qrcode() {
      return callHost(`${base}/qrcode`, {}, { method: 'post' }).then(unwrap)
    },

    /** 轮询登录状态：waiting/scanned/confirmed/unbound/expired */
    status(qrCodeId) {
      return callHost(`${base}/login-status/${qrCodeId}`, {}, { method: 'get' }).then(unwrap)
    },

    /** 取消本次扫码登录 */
    cancel(qrCodeId) {
      return callHost(`${base}/cancel-login/${qrCodeId}`, {}, { method: 'post' }).then(unwrap)
    },

    /** 授权回调：用 code 换取登录态 */
    callback(code, state) {
      return callHost(`${base}/callback`, { code, state }, { method: 'get' }).then(unwrap)
    }
  }
}

/**
 * 查询各渠道开通状态（服务端只返回编码/名称/是否已配置，不含凭据）
 * 失败时抛出异常，由调用方决定兜底展示策略。
 */
export function fetchChannelStatus() {
  return callHost('web/auth/unilogin/channels', {}, { method: 'get' }).then(unwrap)
}

/** 同一页面多次取用只发一次请求；失败后允许重试（不缓存失败结果） */
let channelStatusPromise = null
export function fetchChannelStatusCached() {
  if (!channelStatusPromise) {
    channelStatusPromise = fetchChannelStatus().catch((error) => {
      channelStatusPromise = null
      throw error
    })
  }
  return channelStatusPromise
}

/**
 * 按渠道创建第三方集成配置接口（宿主设置中心，platform = channel.configPlatform）
 * @param {object} channel channels.js 中的渠道描述
 */
export function createConfigApi(channel) {
  const path = `web/setting/third-party/${channel.configPlatform}`
  return {
    get() {
      return callHost(path, {}, { method: 'get' }).then(unwrap)
    },
    save(payload) {
      return callHost(path, payload, { method: 'post' }).then(unwrap)
    }
  }
}

export default createAuthApi
