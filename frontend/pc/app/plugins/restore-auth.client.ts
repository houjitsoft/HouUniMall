/**
 * hydration 完成后再从 cookie 恢复登录态。
 * 若在 setup 阶段同步 restore，SSR（未登录）与客户端首帧（已登录）DOM 不一致，会触发 Hydration mismatch。
 */
export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.hook('app:mounted', () => {
    const userStore = useUserStore()
    const restored = userStore.restoreLoginState()
    if (restored) {
      void userStore.refreshUserInfo()
    }
  })
})
