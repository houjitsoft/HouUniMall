import request from '@/utils/request'

// 模版列表
export function gettemLists(params?: any) {
    return request.get({ url: '/printer.printerTemplate/lists', params })
}
// 模版删除
export function apitemDelete(params: { id: number }) {
    return request.post({ url: '/printer.printerTemplate/delete', params })
}
export function apiTemAdd(params: any) {
    return request.post({ url: '/printer.printerTemplate/add', params })
}
export function apiTemEdit(params: any) {
    return request.post({ url: '/printer.printerTemplate/edit', params })
}
export function getTemDetial(params: any) {
    return request.get({ url: '/printer.printerTemplate/detail', params })
}

// 打印机列表
export function getprinterists(params: any) {
    return request.get({ url: '/printer.printer/lists', params })
}
export function apiPrinterAdd(params: any) {
    return request.post({ url: '/printer.printer/add', params })
}
export function apiPrinterEdit(params: any) {
    return request.post({ url: '/printer.printer/edit', params })
}
export function getprinterDetial(params: any) {
    return request.get({ url: '/printer.printer/detail', params })
}
export function apiprinterDelete(params: { id: number }) {
    return request.post({ url: '/printer.printer/delete', params })
}
export function apiprint(params: { id: number }) {
    return request.post({ url: '/mall.order/print', params })
}
export function apitestprint(params: any) {
    return request.post({ url: '/printer.printer/test', params })
}
export function apistatusprint(params: any) {
    return request.post({ url: '/printer.printer/set_status', params })
}
