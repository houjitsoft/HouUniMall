import { defineStore } from 'pinia'

const useAppStore = defineStore('app', {
    state: () => ({
        config: {
            oss_domain: ''
        }
    }),
    actions: {
        getImageUrl(url) {
            if (!url) return ''
            if (url.startsWith('http')) return url
            return this.config.oss_domain + url
        }
    }
})

export default useAppStore
