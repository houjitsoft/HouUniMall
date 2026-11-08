import { defineStore } from 'pinia'

export interface PlusPaymentContext {
    payPrice: number
    thaliEquity: string
    thaliType: number
}

interface PaymentState {
    plusPaymentContext: PlusPaymentContext | null
}

export const usePaymentStore = defineStore('payment', {
    state: (): PaymentState => ({
        plusPaymentContext: null
    }),
    getters: {
        getPlusPaymentContext: (state) => state.plusPaymentContext
    },
    actions: {
        setPlusPaymentContext(payload: PlusPaymentContext) {
            this.plusPaymentContext = payload
        },
        clearPlusPaymentContext() {
            this.plusPaymentContext = null
        }
    },
    persist: {
        key: 'payment-state',
        storage: typeof window !== 'undefined' ? window.sessionStorage : undefined,
        paths: ['plusPaymentContext']
    } as any
})
