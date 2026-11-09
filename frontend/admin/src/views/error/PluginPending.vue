<template>
  <!-- 判定「这条路径不可能命中任何页面」后，直接在这里渲染 404 内容。
       为什么不 router.replace 到 NotFound 路由：路由匹配由 URL 驱动，`/plugin/**`
       永远先命中本组件挂的 `/plugin/:pluginCode(.*)`，replace 成同一个 URL 会被
       vue-router 判为重复导航直接丢弃，404 路由永远轮不到。 -->
  <NotFound v-if="notFound" />
  <div v-else class="plugin-pending">
    <el-result icon="info" title="正在加载应用" :sub-title="subtitle">
      <template #extra>
        <el-button type="primary" :loading="loading" @click="retry">重新加载</el-button>
        <el-button @click="$router.push('/dashboard')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
/**
 * 插件深链「待注册」占位页。
 *
 * <p><b>为什么需要它</b>：插件的前端路由是运行时动态注册（router.addRoute），
 * 冷启动直接打开 /plugin/{code}/** 深链时，路由表里还没有该路径，
 * 会先命中兜底的 404 路由 ⇒ 用户看到「页面不存在」，而实际插件只是还在下载。
 *
 * <p><b>为什么不用 404 页顶掉</b>：404 是终态语义（路由确实不存在），
 * 而这里是「还没注册完」的中间态，混用会让用户以为页面坏了。
 * 本组件在插件注册完成后自动跳转到真实路由（见下方 watch）。
 *
 * <p><b>兜底超时</b>：插件确实不可用（未安装 / 产物缺失 / 下载失败）时，
 * 等待超过 PLUGIN_PENDING_TIMEOUT_MS 后恢复成「页面不存在」提示，
 * 避免无限转圈。
 *
 * <p><b>提前落到 404（2026-10-29）</b>：本组件挂在 `/plugin/:pluginCode(.*)` 这条万能兜底上，
 * 意味着**任何**以 `/plugin/` 开头的路径都会命中它，包括根本不存在的那一层
 * （例：页面是 `/plugin/oa/salary/payslip`，误写成 `/plugin/oa/salary/payslip/page`）。
 * 那种情况下无论等多久都不会有路由注册进来，用户只会看到「正在下载前端产物」——
 * 把「地址写错」伪装成「正在加载」，比 404 更糟。
 * 因此满足下面任一条件就把 notFound 置真、**就地渲染 NotFound 的内容**（URL 保持原样），
 * 不再白等：
 * ① 插件清单里没有这个 code，或装了但未启用 / 没有前端入口（永远不会注册）；
 * ② 该 code 已在 loadedCodes 里（store 是在 addRoute 全部执行后才写入的），
 *   但目标路径仍解析到兜底路由 ⇒ 这条路就不在它的路由表里。
 * 插件尚未注册完成时**不**判定，避免把「还没加载」误判成「不存在」。
 */
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { usePluginStore } from '@/stores/plugin'
import NotFound from './NotFound.vue'

/** 等待插件注册完成的最长时间；超时后提示页面不存在。 */
const PLUGIN_PENDING_TIMEOUT_MS = 60000

const router = useRouter()
const pluginStore = usePluginStore()

const loading = ref(true)
const timedOut = ref(false)
/** 已确认这条深链不可能命中任何页面 —— 就地渲染 404，不再假装「正在加载」。 */
const notFound = ref(false)
let timer = null

/** 当前深链里的插件编码（/plugin/{code}/... → {code}）。 */
const pluginCode = computed(() => {
  const match = /^\/plugin\/([^/?]+)/.exec(router.currentRoute.value.path || '')
  return match ? match[1] : ''
})

const subtitle = computed(() => {
  if (timedOut.value) {
    return `应用 ${pluginCode.value} 未注册或不可用，请从右上角「插件应用」进入`
  }
  return `正在载入应用 ${pluginCode.value}，首次访问需要下载前端产物，请稍候`
})

/**
 * 目标路径是否已经「确定不存在」。
 *
 * 只有拿到确定信息才判——插件还在加载（code 不在 loadedCodes）时必须放它继续等，
 * 否则会把正常深链也误杀成 404。
 */
function isDefinitelyUnreachable(code) {
  if (!code) return true
  // ① 插件清单已到（installed 在 getPluginList 返回后立刻赋值，早于 loadedOnce）：
  //    清单里没有这个 code，或装了但未启用 / 没有前端入口 ⇒ 永远不会注册路由
  const installed = pluginStore.installed || []
  if (installed.length) {
    const item = installed.find((entry) => entry?.code === code)
    if (!item) return true
    // store 的 targets 会过滤掉未启用 / 无 frontendUrl 的插件，它们永远不会注册
    if (!item.enabled || !item.frontendUrl) return true
  }
  // ② 插件已注册完成，目标路径却仍解析到兜底 —— 说明这一层路径不在它的路由里
  return pluginStore.loadedCodes.includes(code)
}

/** 插件注册完成后，路由表里已存在真实路径 → 跳过去；确定不存在 → 就地渲染 404。 */
function jumpIfReady() {
  const target = router.currentRoute.value.fullPath
  if (!target) return
  const resolved = router.resolve(target)
  // 仍命中占位路由（PluginPending）或兜底 404 ⇒ 插件路由还没注册到这个路径。
  if (resolved.name === 'PluginPending' || resolved.name === 'NotFound') {
    if (isDefinitelyUnreachable(pluginCode.value)) notFound.value = true
    return
  }
  if (!resolved.matched.length) return
  // 之前判过 404，但插件后来注册上了（热装 / 清单晚到）—— 恢复并跳过去
  notFound.value = false
  router.replace(target).catch(() => {})
}

function retry() {
  timedOut.value = false
  loading.value = true
  jumpIfReady()
}

onMounted(() => {
  // 插件可能已经就绪（用户从菜单点进来时路由早已注册），直接跳。
  jumpIfReady()
  const stop = watch(
    // installed.length 也要跟：插件清单到达是最早的「可以判不存在」信号，
    // 只看 loadedOnce 会白等到当前插件 4MB 产物下载完（20-30 秒）。
    () => [pluginStore.loadedOnce, pluginStore.loadedCodes.length, (pluginStore.installed || []).length],
    () => {
      // 超时后也要继续响应插件注册完成：原来 timedOut 直接 return，导致后台队列
      // 晚到的插件注册成功后页面永久停在「正在加载应用」，必须手动刷新。
      jumpIfReady()
    }
  )
  timer = setTimeout(() => {
    timedOut.value = true
    loading.value = false
    jumpIfReady()
  }, PLUGIN_PENDING_TIMEOUT_MS)
  onBeforeUnmount(() => stop())
})

onBeforeUnmount(() => {
  if (timer) clearTimeout(timer)
})
</script>

<style scoped>
.plugin-pending {
  padding: 48px 24px;
}
</style>
