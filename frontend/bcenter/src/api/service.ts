import request from '@/utils/request'

export function getcustomerService() {
    return request.get({ url: '/config/get_service' })
}

export function setcustomerService(params: any) {
    return request.post({ url: '/config/set_service', params })
}
