import {
    joinBase as baserequest,
    joinBusiness as businessrequest,
    joinUserms as usermsrequest,
    joinGoods as goodsrequest,
    joinThird as thirdrequest,
    joinSupplier as mallSupplier
} from '@/utils/joinChannels'
// 获取OSS资源的预签名URL
export async function getPresignedUrl(data, newName) {
    return getUploadUrl({
        name: newName,
        type: data.type,
        body: data,
    });
}

// 获取货币符号（用于 main.js 初始化 currencySymbol / currencyRemark）
export function apiCurrencySymbol() {
  return baserequest({
    url: '/base/currencySymbol/open',
    method: 'get',
  })
}
/**
 * 获取 OSS 预签名地址并直传文件到对象存储。
 * 改为普通 async/await 实现，避免 new Promise(async) 反模式导致异常泄漏为
 * 未捕获 Promise（表现为浏览器控制台 “Uncaught (in promise) Object” 且上传静默失败）。
 */
async function getUploadUrl({
    name, type, body
}) {
    let res
    try {
        res = await thirdrequest({
            url: '/web/oss/getPresignedUrl',
            method: 'post',
            data: {
                fileName: name,
            },
        })
    } catch (e) {
        console.error('获取上传地址请求失败:', e)
        throw new Error('获取上传地址失败，请稍后再试')
    }
    if (!res || res.code !== 1) {
        throw new Error((res && res.message) || '获取上传地址失败')
    }
    try {
        const response = await fetch(res.data.uploadUrl, {
            method: 'PUT',
            headers: {
                'Content-Type': type,
            },
            body,
        })
        if (!response.ok) {
            throw new Error('上传失败，请稍后再试')
        }
        return res.data.fileUrl
    } catch (e) {
        console.error('直传到 OSS 失败:', e)
        throw new Error('上传失败，请稍后再试')
    }
}