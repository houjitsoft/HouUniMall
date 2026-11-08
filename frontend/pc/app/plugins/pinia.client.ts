import { createPersistedState } from 'pinia-plugin-persistedstate'
import type { Pinia } from 'pinia'

/**
 * Pinia 持久化插件配置
 * 
 * 注意：此文件必须在 plugins 目录中，Nuxt 会自动加载
 */
export default defineNuxtPlugin((nuxtApp) => {
  const pinia = nuxtApp.$pinia as Pinia
  
  pinia.use(createPersistedState({
    storage: typeof window !== 'undefined' ? window.localStorage : undefined,
  }))
})
