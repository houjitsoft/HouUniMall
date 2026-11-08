import { defineStore } from 'pinia'

export interface PlusState {
    /** 平台是否开启 PLUS（接口 data===1） */
    platformPlusEnabled: boolean
    /** 是否已拉取平台开关 */
    platformLoaded: boolean
    /** 当前用户是否 PLUS 会员 */
    isPlusMember: boolean
    /** 会员等级名称 */
    memberName: string | null
    /** 是否已拉取用户会员信息（登录且平台开启时） */
    userMemberLoaded: boolean
}

export const usePlusStore = defineStore('plus', {
    state: (): PlusState => ({
        platformPlusEnabled: false,
        platformLoaded: false,
        isPlusMember: false,
        memberName: null,
        userMemberLoaded: false
    }),

    getters: {
        /** 平台已开启 PLUS（用于 Banner / 头部角标展示入口） */
        showPlusEntry: (state): boolean => state.platformLoaded && state.platformPlusEnabled
    },

    actions: {
        setPlatformPlusFromResponse(data: unknown, success: boolean) {
            if (!success) {
                this.platformPlusEnabled = false
                this.platformLoaded = true
                return
            }
            const v = data as number | string | undefined
            this.platformPlusEnabled = v === 1 || v === '1'
            this.platformLoaded = true
        },

        setUserMemberFromResponse(isPlus: boolean, memberName?: string | null) {
            this.isPlusMember = isPlus
            this.memberName = memberName ?? null
            this.userMemberLoaded = true
        },

        clearUserMember() {
            this.isPlusMember = false
            this.memberName = null
            this.userMemberLoaded = false
        },

        setPlatformLoadFailed() {
            this.platformPlusEnabled = false
            this.platformLoaded = true
        }
    }
})
