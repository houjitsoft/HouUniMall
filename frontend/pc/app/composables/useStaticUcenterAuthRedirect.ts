import type { ConsumerAuthKind } from '~/utils/ucenterAuthUrl'
import { ucenterAuthUrl, ucenterAuthUrlFromSearch } from '~/utils/ucenterAuthUrl'

declare global {
    interface Window {
        __UCENTER_AUTH_SEARCH__?: string
    }
}

export const useStaticUcenterAuthRedirect = (kind: ConsumerAuthKind) => {
    const target = ref(ucenterAuthUrl(kind))

    useHead({
        script: [{
            key: 'capture-ucenter-auth-search',
            innerHTML: 'window.__UCENTER_AUTH_SEARCH__=window.location.search'
        }]
    })

    onMounted(() => {
        const search = window.__UCENTER_AUTH_SEARCH__ ?? window.location.search
        target.value = ucenterAuthUrlFromSearch(kind, search)
        window.location.replace(target.value)
    })

    return target
}
