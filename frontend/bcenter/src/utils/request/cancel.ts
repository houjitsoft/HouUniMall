import axios, { type AxiosRequestConfig, type Canceler } from 'axios'
import { buildRequestKey } from './cancelRules.mjs'

const cancelerMap = new Map<string, Canceler>()

export class AxiosCancel {
    private static instance?: AxiosCancel

    static createInstance() {
        return this.instance ?? (this.instance = new AxiosCancel())
    }
    add(config: AxiosRequestConfig) {
        const requestKey = buildRequestKey(config)
        config.requestCancelKey = requestKey
        this.remove(requestKey)
        config.cancelToken = new axios.CancelToken((cancel) => {
            if (!cancelerMap.has(requestKey)) {
                cancelerMap.set(requestKey, cancel)
            }
        })
    }
    remove(requestKey: string) {
        if (cancelerMap.has(requestKey)) {
            const cancel = cancelerMap.get(requestKey)
            cancel && cancel(requestKey)
            cancelerMap.delete(requestKey)
        }
    }

    removeByConfig(config: AxiosRequestConfig) {
        this.remove(config.requestCancelKey || buildRequestKey(config))
    }
}

const axiosCancel = AxiosCancel.createInstance()

export default axiosCancel
