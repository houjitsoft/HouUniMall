import { createRouter, createWebHashHistory, RouterView, type RouteRecordRaw } from 'vue-router'
import { MenuEnum } from '@/enums/appEnums'
import { isExternal } from '@/utils/validate'
import { constantRoutes, INDEX_ROUTE_NAME, LAYOUT } from './routes'
import useUserStore from '@/stores/modules/user'
import { isSellerRouteAvailable, loadSellerRouteView } from '@/plugins/runtime'

// 匹配views里面所有的.vue文件，动态引入
const modules = import.meta.glob('/src/views/**/*.vue')

//
export function getModulesKey() {
    return Object.keys(modules).map((item) => item.replace('/src/views/', '').replace('.vue', ''))
}

// 过滤路由所需要的数据
export function filterAsyncRoutes(routes: any[], firstRoute = true) {
    return routes.reduce<RouteRecordRaw[]>((records, route: any) => {
        if (route.type === MenuEnum.MENU && route.component?.startsWith('plugin:') &&
            !isSellerRouteAvailable(route.component)) {
            return records
        }
        const routeRecord = createRouteRecord(route, firstRoute)
        if (route.sub != null && route.sub && route.sub.length) {
            routeRecord.children = filterAsyncRoutes(route.sub, false)
        }
        records.push(routeRecord)
        return records
    }, [])
}

// 创建一条路由记录
export function createRouteRecord(route: any, firstRoute: boolean): RouteRecordRaw {
    //@ts-ignore
    const routeRecord: RouteRecordRaw = {
        path: isExternal(route.route) ? route.route : firstRoute ? `/${route.route}` : route.route,
        name: Symbol(route.route),
        meta: {
            hidden: !route.show_status,
            keepAlive: !!route.is_cache,
            title: route.name,
            perms: route.access,
            query: route.route_params,
            icon: route.icon,
            type: route.type,
            activeMenu: route.selected
        }
    }
    switch (route.type) {
        case MenuEnum.CATALOGUE:
            routeRecord.component = firstRoute ? LAYOUT : RouterView
            if (!route.sub) {
                routeRecord.component = LAYOUT
            }
            break
        case MenuEnum.MENU:
            routeRecord.component = loadRouteView(route.component)
            break
    }
    return routeRecord
}

// 动态加载组件
export function loadRouteView(component: string) {
    if (component?.startsWith('plugin:')) {
        const pluginView = loadSellerRouteView(component)
        if (pluginView) return pluginView
        console.error(`找不到已授权的商家插件组件${component}`)
        return RouterView
    }
    try {
        const key = Object.keys(modules).find((key) => {
            return key.includes(`${component}.vue`)
        })
        if (key) {
            return modules[key]
        }
        throw Error(`找不到组件${component}，请确保组件路径正确`)
    } catch (error) {
        console.error(error)
        return RouterView
    }
}

// 找到第一个有效的路由
export function findFirstValidRoute(routes: RouteRecordRaw[]): string | undefined {
    for (const route of routes) {
        if (route.meta?.type as unknown == MenuEnum.MENU && !route.meta?.hidden && !isExternal(route.path)) {
            return route.name as string
        }
        if (route.children) {
            const name = findFirstValidRoute(route.children)
            if (name) {
                return name
            }
        }
    }
}
//通过权限字符查询路由路径
export function getRoutePath(perms: string) {
    const routerObj = useRouter() || router
    return routerObj.getRoutes().find((item) => item.meta?.perms == perms)?.path || ''
}

// 重置路由
export function resetRouter() {
    router.removeRoute(INDEX_ROUTE_NAME)
    const { routes } = useUserStore()
    routes.forEach((route) => {
        const name = route.name
        if (name && router.hasRoute(name)) {
            router.removeRoute(name)
        }
    })
}

const router = createRouter({
    // 商城各入口统一使用 /bcenter/#/...。Hash 路由既与这些入口保持一致，
    // 也避免未登录跳转把地址错误改写成站点根目录的 /login。
    history: createWebHashHistory(),
    routes: constantRoutes
})

export default router
