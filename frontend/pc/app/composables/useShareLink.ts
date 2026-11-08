/**
 * 分享链接 Composable
 *
 * 封装商品分享链接生成逻辑，支持微信二维码、QQ空间、脸书、ins 等平台
 */

export interface ShareLinkParams {
    /** 商品 commonId */
    commonId?: string | number
    /** 商品 goodsId */
    goodsId?: string | number
    /** 分享标题（商品名称） */
    title?: string
    /** 封面图片 URL（支持相对路径，会自动补全为完整 URL） */
    imageUrl?: string
}

/** 下载APP路径模板 */
const DOWNLOAD_APP_PATH = 'h5/#/setting/downloadApp/downloadApp';

/** 分享路径模板 */
const SHARE_PATH = 'h5/#/pages/description/descrption'

/**
 * 构建下载APP链接
 * 格式：当前域名 + h5/#/setting/downloadApp/downloadApp
 */
export function buildDownloadAppLink(): string {
    if (typeof window === 'undefined') return ''
    return `${window.location.origin}/${DOWNLOAD_APP_PATH}`
}

/**
 * 构建商品分享链接
 * 格式：当前域名 + h5/#/pagesA/description/descrption?commonId=&goodsId=
 */
export function buildShareLink(params: ShareLinkParams): string {
    if (typeof window === 'undefined') return ''
    const { commonId = '', goodsId = '' } = params
    const base = window.location.origin
    return `${base}/${SHARE_PATH}?commonId=${commonId}&goodsId=${goodsId}`
}

/**
 * 将相对路径转为完整 URL
 */
export function toAbsoluteUrl(url: string): string {
    if (!url || typeof window === 'undefined') return ''
    return url.startsWith('http') ? url : `${window.location.origin}${url.startsWith('/') ? url : `/${url}`}`
}

/**
 * 构建 QQ 空间分享链接
 * @see https://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey
 */
export function buildQzoneShareUrl(params: ShareLinkParams & { shareLink: string }): string {
    const { shareLink, title = '商品分享', imageUrl } = params
    if (!shareLink || typeof window === 'undefined') return ''
    const titleStr = String(title).slice(0, 50)
    const summary = titleStr
    const pics = imageUrl ? toAbsoluteUrl(imageUrl) : ''
    const searchParams = new URLSearchParams({
        url: shareLink,
        title: titleStr,
        summary,
        ...(pics && { pics })
    })
    return `https://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey?${searchParams.toString()}`
}

/**
 * 获取各平台分享 URL
 */
export function getShareUrlByPlatform(
    platform: 'qq' | 'facebook' | 'ins',
    shareLink: string,
    qzoneShareUrl: string
): string {
    const encodedUrl = shareLink ? encodeURIComponent(shareLink) : ''
    switch (platform) {
        case 'qq':
            return qzoneShareUrl || `https://connect.qq.com/widget/shareqq/index.html?url=${encodedUrl}`
        case 'facebook':
            return `https://www.facebook.com/sharer/sharer.php?u=${encodedUrl}`
        case 'ins':
            return 'https://www.instagram.com'
        default:
            return shareLink
    }
}

/**
 * 分享链接 Composable
 *
 * @param params 分享参数（支持 ref/computed，会自动解包）
 */
export function useShareLink(params: ShareLinkParams | (() => ShareLinkParams)) {
    const getParams = (): ShareLinkParams =>
        typeof params === 'function' ? params() : params

    const shareLink = computed(() => buildShareLink(getParams()))

    const qzoneShareUrl = computed(() =>
        buildQzoneShareUrl({
            ...getParams(),
            shareLink: shareLink.value
        })
    )

    const openShare = (platform: 'qq' | 'facebook' | 'ins') => {
        const url = getShareUrlByPlatform(platform, shareLink.value, qzoneShareUrl.value)
        window.open(url, '_blank', 'noopener,noreferrer')
    }

    return {
        shareLink,
        qzoneShareUrl,
        openShare,
        buildShareLink,
        buildQzoneShareUrl,
        getShareUrlByPlatform
    }
}
