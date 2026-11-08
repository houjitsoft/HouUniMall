import axios from 'axios'

const TOKEN_KEY = 'restart_admin_token'

function headers(token) {
  return { 'X-Admin-Token': token }
}

/**
 * 获取 Java 服务列表
 * @param {string} token 管理员令牌
 */
export function fetchServiceList(token) {
  return axios.get('/web/admin/services', {
    headers: headers(token),
    timeout: 120000
  })
}

/**
 * 启动单个服务
 * @param {string} token 令牌
 * @param {string} name 服务名
 */
export function startService(token, name) {
  return axios.post(`/web/admin/services/${encodeURIComponent(name)}/start`, {}, {
    headers: headers(token),
    timeout: 180000
  })
}

/**
 * 停止单个服务
 * @param {string} token 令牌
 * @param {string} name 服务名
 */
export function stopService(token, name) {
  return axios.post(`/web/admin/services/${encodeURIComponent(name)}/stop`, {}, {
    headers: headers(token),
    timeout: 120000
  })
}

/**
 * 重启单个服务
 * @param {string} token 令牌
 * @param {string} name 服务名
 */
export function restartService(token, name) {
  return axios.post(`/web/admin/services/${encodeURIComponent(name)}/restart`, {}, {
    headers: headers(token),
    timeout: 600000
  })
}

/**
 * 查询单个服务状态（轻量，供启动后轮询）
 * @param {string} token 令牌
 * @param {string} name 服务名
 */
export function fetchServiceStatus(token, name) {
  return axios.get(`/web/admin/services/${encodeURIComponent(name)}/status`, {
    headers: headers(token),
    timeout: 30000
  })
}

/**
 * 读取服务日志
 * @param {string} token 令牌
 * @param {string} name 服务名
 * @param {number} tail 行数
 */
export function fetchServiceLog(token, name, tail = 200) {
  return axios.get(`/web/admin/services/${encodeURIComponent(name)}/log`, {
    params: { tail },
    headers: headers(token),
    timeout: 60000
  })
}

/**
 * 启动全部服务
 * @param {string} token 令牌
 */
export function startAllServices(token) {
  return axios.post('/web/admin/services/start-all', {}, {
    headers: headers(token),
    timeout: 600000
  })
}

/**
 * 停止全部服务
 * @param {string} token 令牌
 */
export function stopAllServices(token) {
  return axios.post('/web/admin/services/stop-all', {}, {
    headers: headers(token),
    timeout: 300000
  })
}

export function saveAdminToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function loadAdminToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}
