/**
 * 客户端尽早预取平台开关，避免 Header onMounted 时被首页并发请求挤出连接池导致 TimeoutError。
 */
import { prefetchPlatformSwitches } from '~/composables/useIsKeySwitch'

export default defineNuxtPlugin(() => {
    void prefetchPlatformSwitches(['language_switch', 'aiComparison_switch'])
})
