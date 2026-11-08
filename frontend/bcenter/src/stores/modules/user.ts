import { defineStore } from 'pinia'
import cache from '@/utils/cache'
import type { RouteRecordRaw } from 'vue-router'
import { getUserInfo, login, logout } from '@/api/user'
import router, { filterAsyncRoutes } from '@/router'
import { TOKEN_KEY } from '@/enums/cacheEnums'
import { PageEnum } from '@/enums/pageEnum'
import { clearAuthInfo, getToken } from '@/utils/auth'
import { loadSellerPlugins, unloadSellerPlugins } from '@/plugins/runtime'
export interface UserState {
    token: string
    userInfo: Record<string, any>
    routes: RouteRecordRaw[]
    perms: string[]
}

const useUserStore = defineStore({
    id: 'user',
    state: (): UserState => ({
        token: getToken() || '',
        // 用户信息
        userInfo: {},
        // 路由
        routes: [],
        // 权限
        perms: []
    }),
    getters: {},
    actions: {
        resetState() {
            void unloadSellerPlugins()
            this.token = ''
            this.userInfo = {}
            this.perms = []
        },
        login(playload: any) {
            const { username, password } = playload
            return new Promise((resolve, reject) => {
                login({
                    username: username.trim(),
                    password: password
                })
                    .then((data) => {
                        this.token = data['shop-token'];
                        cache.set(TOKEN_KEY, data['shop-token'])
                        resolve(data)
                    })
                    .catch((error) => {
                        reject(error)
                    })
            })
        },
        logout() {
            return new Promise((resolve, reject) => {
                logout()
                    .then(async (data) => {
                        this.token = ''
                        await unloadSellerPlugins()
                        await router.push(PageEnum.LOGIN)
                        clearAuthInfo()
                        resolve(data)
                    })
                    .catch((error) => {
                        reject(error)
                    })
            })
        },
        getUserInfo() {
            return new Promise((resolve, reject) => {
                getUserInfo()
                    .then((data) => {
                        this.userInfo = data.account
                        this.perms = data.access
                        // 服务端权限树决定当前账号可见入口；插件中心清单再确认本站点已安装且启用。
                        return loadSellerPlugins(data.menu)
                            .then(() => data)
                    })
                    .then((data) => {
                        this.routes = filterAsyncRoutes(data.menu)
                        resolve(data)
                    })
                    .catch((error) => {
                        reject(error)
                    })
            })
        }
    }
})

export default useUserStore
