import { computed } from 'vue'
import { useRoute } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import useUserStore from '@/stores/modules/user'
import { getNormalPath, objectToQuery } from '@/utils/util'
import { isExternal } from '@/utils/validate'

/**
 * 外壳菜单：把后端下发的路由树整理成统一的 ShellNode 树。
 * 布局为「左侧边栏完整菜单树」，这里只做展示层的整理，不改动路由注册、权限与跳转逻辑。
 */
export interface ShellNode {
    fullPath: string
    title: string
    icon?: string
    activeMenu?: string
    isExternal: boolean
    children: ShellNode[]
    /** 带上 meta.query 后的最终跳转地址 */
    link: string
}

const buildNode = (route: RouteRecordRaw, parentPath = ''): ShellNode => {
    const rawPath = (route.path as string) ?? ''
    const outer = isExternal(rawPath)
    const fullPath = outer
        ? rawPath
        : parentPath
          ? getNormalPath(`${parentPath}/${rawPath}`)
          : getNormalPath(rawPath)

    const children = ((route.children ?? []) as RouteRecordRaw[])
        .filter((item) => !item.meta?.hidden)
        .map((item) => buildNode(item, fullPath))

    let queryStr = ''
    const query = route.meta?.query as string | undefined
    if (query) {
        try {
            queryStr = objectToQuery(JSON.parse(query))
        } catch (error) {
            queryStr = query
        }
    }

    return {
        fullPath,
        title: (route.meta?.title as string) ?? '',
        icon: route.meta?.icon as string | undefined,
        activeMenu: route.meta?.activeMenu as string | undefined,
        isExternal: outer,
        children,
        link: queryStr ? `${fullPath}?${queryStr}` : fullPath
    }
}

const collectLeaves = (nodes: ShellNode[], out: ShellNode[] = []): ShellNode[] => {
    nodes.forEach((node) => {
        if (node.children.length) {
            collectLeaves(node.children, out)
        } else {
            out.push(node)
        }
    })
    return out
}

export const useShellMenu = () => {
    const route = useRoute()
    const userStore = useUserStore()

    /** 当前需要高亮的地址（详情类页面通过 meta.activeMenu 指向列表菜单） */
    const highlightPath = computed(() => (route.meta?.activeMenu as string) || route.path)

    const nodes = computed<ShellNode[]>(() =>
        ((userStore.routes ?? []) as RouteRecordRaw[])
            .filter((item) => !item.meta?.hidden)
            .map((item) => buildNode(item))
    )

    const isActive = (node: ShellNode) => node.fullPath === highlightPath.value

    return {
        nodes,
        highlightPath,
        isActive,
        collectLeaves
    }
}
