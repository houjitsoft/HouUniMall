import request from '@/utils/request'

// 获取结算设置
export function getSettlementConfig() {
    return request.get({ url: '/mall.orderSetting/settlement_get' })
}

type SettlementConf = {
    settle_cycle: number	           // integer 是	结算周期 0 日结 1 周结 2月结
    settle_cycle_week_day: number	   // integer 是	周结星期几 settle_cycle = 1时需要
    settle_cycle_month_day: number	   // integer 是	月结第几天 settle_cycle = 2时需要
}

export type SettlementForm = {
    [index: string]: any
    settlement: SettlementConf,
    enum_week: any
    enum_month: any
}
