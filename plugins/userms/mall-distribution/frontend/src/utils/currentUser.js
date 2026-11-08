/**
 * 读取当前登录用户（供审核类接口自动带 auditorId / operatorName）。
 *
 * admin 登录态把用户信息整份存在 localStorage.userInfo（见 stores/auth.js），
 * 插件产物拿不到宿主 pinia store，只能从这里读。audit 接口要求 auditorId 必填
 * （后端直接 Long.parseLong，缺了会 500），所以取不到时返回 0 但要让调用方看得见。
 */
export function currentUser() {
  try {
    const raw = localStorage.getItem('userInfo')
    if (!raw) return { id: 0, name: '' }
    const info = JSON.parse(raw)
    return {
      id: Number(info.userId ?? info.user_id ?? info.id ?? 0),
      name: info.realName || info.username || info.nickName || info.name || ''
    }
  } catch (error) {
    return { id: 0, name: '' }
  }
}
