/**
 * 应用级 Meta 管理
 * 统一设置：viewport、标题、favicon、主题样式（响应式更新）、SEO 基础（Open Graph 等）
 * 仅在 getActivePinia() 存在时使用 mallStore，避免 SSR/插件阶段报错导致 500。
 */
import { getActivePinia } from 'pinia'
import { useMallStore } from '~/stores/modules/mall'

export const useAppMeta = () => {
    const pinia = getActivePinia()
    const mallStore = pinia ? useMallStore(pinia) : null
    const { generateThemeStyles, currentTheme } = useTheme()
    const config = useRuntimeConfig().public
    const route = useRoute()

    const defaultDescription = 'B2C商城，提供商品选购、店铺浏览、限时折扣、秒杀促销、积分兑换、在线支付等一站式购物服务。'
    const defaultKeywords = 'B2C商城,在线商城,商品购物,店铺,秒杀,限时折扣,积分商城,在线支付'
    const isProd = computed(() => `${config.appEnv || ''}`.toLowerCase() === 'production')

    const pageTitle = computed(() => {
        if (!mallStore) return 'B2C商城'
        mallStore.navLogoConfigs // 触发响应式依赖
        return mallStore.getConfigValueByKey('logo_copywriting_ico_pc') || 'B2C商城'
    })
    // 显式依赖 navLogoConfigs，确保配置异步加载后 favicon 能正确更新
    const faviconHref = computed(() => {
        if (!mallStore) return '/favicon.png'
        mallStore.navLogoConfigs // 触发响应式依赖
        return mallStore.getConfigValueByKey('logo_copywriting_ico') || '/favicon.png'
    })

    const siteUrl = (config.siteUrl as string) || ''
    const canonicalUrl = computed(() => (siteUrl ? `${siteUrl}${route.path}` : ''))

    useHead({
        htmlAttrs: { lang: 'zh-CN' },
        title: pageTitle,
        meta: [
            { key: 'viewport', name: 'viewport', content: 'width=device-width, initial-scale=1' },
            { key: 'description', name: 'description', content: defaultDescription },
            { key: 'keywords', name: 'keywords', content: defaultKeywords },
            {
                key: 'robots',
                name: 'robots',
                content: isProd.value ? 'index,follow,max-image-preview:large' : 'noindex,nofollow'
            },
            ...(isProd.value ? [] : [{ key: 'googlebot', name: 'googlebot', content: 'noindex,nofollow' }])
        ],
        link: computed(() => {
            const links: Array<{ rel: string; href: string; key?: string }> = [
                { rel: 'icon', href: faviconHref.value, key: 'favicon' }
            ]
            if (canonicalUrl.value) {
                links.push({ rel: 'canonical', href: canonicalUrl.value, key: 'canonical' })
            }
            return links
        }),
        style: computed(() => [{
            key: 'theme-styles',
            innerHTML: generateThemeStyles({
                primary: currentTheme.value.primary,
                background: currentTheme.value.background || '#ffffff',
                text: currentTheme.value.text || '#303133'
            }),
            tagPriority: 'high'
        }])
    })

    // Open Graph / 通用 SEO 基础（各页面可覆盖 description、ogTitle、ogImage 等）
    useSeoMeta({
        title: pageTitle,
        description: defaultDescription,
        ogSiteName: pageTitle,
        ogType: 'website',
        ogTitle: pageTitle,
        ogDescription: defaultDescription,
        twitterCard: 'summary_large_image',
        twitterTitle: pageTitle,
        twitterDescription: defaultDescription,
        ...(canonicalUrl.value ? { ogUrl: canonicalUrl } : {})
    })
}
