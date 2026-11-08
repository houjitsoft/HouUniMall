import {
    joinBase as baserequest,
    joinBusiness as businessrequest,
    joinUserms as usermsrequest,
    joinGoods as goodsrequest,
    joinThird as thirdrequest,
    joinSupplier as mallSupplier
} from '@/utils/joinChannels'
// 获取OSS资源的预签名URL
export async function getPresignedUrl(data,newName) {
    const url = await getUploadUrl({
        name:newName,
        type: data.type,
        body: data,
    });
    return url;
}

// 获取货币符号（用于 main.js 初始化 currencySymbol / currencyRemark）
export function apiCurrencySymbol() {
  return baserequest({
    url: '/base/currencySymbol/open',
    method: 'get',
  })
}
function getUploadUrl({
    name, type, body
}) {
    return new Promise(async (resolve, reject) => {
        const res = await thirdrequest({
            url: '/web/oss/getPresignedUrl',
            method: 'post',
            data: {
                fileName: name,
            },
        });
        if (res && res.code === 1) {
            fetch(res.data.uploadUrl, {
                method: 'PUT',
                headers: {
                    'Content-Type': type,
                },
                body
            })
                .then((response) => {
                    if (!response.ok) {
                        throw new Error('上传失败，请稍后再试')
                    }
                    ;

                    resolve(res.data.fileUrl);
                })
                .catch((error) => {
                    console.error('发生错误:', error);
                    reject(error);
                })
        } else {
            reject('获取上传地址失败')
        }
    })
}