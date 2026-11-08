/**
 * 把安装/升级任务的失败态转成用户可读的一句话。
 * 优先用后端 errorMsg；再按 step 给阶段提示，避免只显示「未知原因」。
 *
 * ⚠️ 不要在这里把失败归因为「未上架」之类的商品状态：
 * 商品上架（hou_market_plugin.status=2）与安装失败没有因果关系。
 * 沙箱订单拿不到制品时后端返回 3002，若前端改写成「未上架」，
 * 用户会误以为商品没上架，反复刷新页面也解决不了。
 *
 * @param {Record<string, any>|null|undefined} status 轮询到的任务对象或 result
 * @returns {string}
 */
export function formatInstallFailure(status) {
  if (!status) return '安装失败：未返回任务状态，请查看服务端日志或稍后重试'
  const msg = String(status.errorMsg || status.result || status.message || '').trim()
  const step = String(status.step || status.data?.step || '').toUpperCase()
  const state = String(status.state || '').toUpperCase()

  const stepHint = {
    QUEUED: '任务排队中被中断',
    DOWNLOAD: '下载制品失败（网络、令牌过期或对象存储不可达）',
    VERIFY: '签名/摘要校验失败（制品被篡改或公钥未配置）',
    BACKUP: '升级预检/备份失败（请检查 upgradePreflight 脚本与磁盘空间）',
    START: '候选容器启动失败（镜像、环境变量或端口冲突）',
    ACTIVATE: '切换正式容器失败（已尝试回滚）',
    HEALTH: '健康检查未通过（进程起来了但探活/注册失败）',
    FAILED: '任务失败',
    INTERRUPTED: '任务被中断',
  }

  if (state === 'ROLLED_BACK') {
    return msg
      ? `安装未成功并已回滚：${msg}`
      : `安装未成功并已回滚${step ? `（阶段 ${step}${stepHint[step] ? ' · ' + stepHint[step] : ''}）` : ''}`
  }

  if (msg) {
    return step && stepHint[step] ? `安装失败（${step} · ${stepHint[step]}）：${msg}` : `安装失败：${msg}`
  }
  if (step && stepHint[step]) return `安装失败：${stepHint[step]}`
  return '安装失败：未知原因（请打开任务详情或服务端日志）'
}

/**
 * 把后端业务错误码翻译成「能照着做」的提示。
 *
 * 存在的理由：沙箱演示环境下，购买/支付/发证全部成功，
 * 但换取制品这一步会被 3002 拦下（沙箱订单不得白拿源码）。
 * 若只回显「只有正式购买对应产品后才能获取源码和服务制品」，
 * 用户会以为自己没买成功，反复重下订单；必须说清是沙箱限制。
 *
 * @param {string|number|null|undefined} code 后端业务码，如 3002
 * @param {string|number|null|undefined} msg 后端原始提示
 * @returns {string} 可直接展示给用户的说明
 */
export function explainArtifactError(code, msg) {
  const raw = String(msg || '').trim()
  const n = Number(code)
  if (n === 3002) {
    return '当前站点为沙箱演示环境：下单、支付与授权签发均已成功，' +
      '但沙箱订单按规则不得获取源码与运行制品，因此无法继续安装。' +
      '这是演示环境的既有限制，与商品上架状态无关；如需完整安装请切换到正式支付通道。'
  }
  if (n === 3003) {
    return '官方维护期已到期，无法获取新制品。当前已安装版本可继续运行；续费后可恢复下载与升级。'
  }
  if (n === 3009) {
    return '该版本缺少已签发的发行清单，制品不予放行。请联系平台确认该版本是否已正式发布。'
  }
  if (n === 3006 || n === 3005) {
    return '该版本未发布或已下架，请选择其他版本。'
  }
  return raw || '制品获取失败，请稍后重试'
}

/**
 * 维护期倒计时文案。
 * @param {string|Date|null|undefined} maintenanceUntil
 * @returns {{ text: string, level: 'ok'|'warn'|'danger'|'none' }}
 */
export function formatMaintenanceCountdown(maintenanceUntil) {
  if (!maintenanceUntil) return { text: '未开通官方维护', level: 'none' }
  const end = new Date(maintenanceUntil).getTime()
  if (Number.isNaN(end)) return { text: String(maintenanceUntil), level: 'none' }
  const days = Math.ceil((end - Date.now()) / 86400000)
  if (days < 0) return { text: `维护已到期 ${Math.abs(days)} 天`, level: 'danger' }
  if (days <= 30) return { text: `维护剩余 ${days} 天`, level: 'warn' }
  return { text: `维护剩余 ${days} 天（至 ${String(maintenanceUntil).slice(0, 10)}）`, level: 'ok' }
}
