import type { WithDrawModal } from "./withdraw.d"
import request from '@/utils/request'

// 获取银行卡列表
export function getShopBankListss() {
    return request.get({ url: '/mall.shopBank/lists' })
}

// 编辑银行卡
export function shopBankEdit(params: WithDrawModal) {
    return request.post({ url: '/mall.shopBank/edit', params })
}

// 添加银行卡
export function shopBankAdd(params: WithDrawModal) {
    return request.post({ url: '/mall.shopBank/add', params })
}

// 删除银行卡
export function shopBankDelete(params: { id: string }) {
    return request.post({ url: '/mall.shopBank/delete', params })
}

// 银行卡详情
export function shopBankDetail(params: { id: string }) {
    return request.get({ url: '/mall.shopBank/detail', params })
}