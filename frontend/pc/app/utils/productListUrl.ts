/**
 * 商品列表 URL 参数处理工具函数
 */

/**
 * 解析属性名称字符串为对象数组
 * 格式: "groupName1:value1,value2;groupName2:value3,value4"
 * @param str 属性名称字符串
 * @returns 对象数组
 */
export function parseAttrNameString(str: string): Array<{ groupName: string; nameList: string[] }> {
    if (!str) return []

    const groups = str.split(';').filter(Boolean)
    const result: Array<{ groupName: string; nameList: string[] }> = []

    groups.forEach(group => {
        const parts = group.split(':')
        if (parts.length === 2 && parts[0] && parts[1]) {
            const groupName = parts[0]
            const items = parts[1].split(',').filter(Boolean)
            result.push({ groupName, nameList: items })
        }
    })

    return result
}

/**
 * 将对象数组转换为属性名称字符串
 * @param array 对象数组
 * @returns 属性名称字符串
 */
export function convertAttrNameToString(array: Array<{ groupName: string; nameList: string[] }>): string {
    if (!array || array.length === 0) return ''

    return array.map(item => {
        return `${item.groupName}:${item.nameList.join(',')}`
    }).join(';')
}

/**
 * 更新URL参数（使用 pushState）
 * @param params 参数对象
 */
export function updateUrlParams(params: Record<string, string | number | null | undefined>) {
    if (typeof window === 'undefined') return

    const router = useRouter()
    const route = useRoute()

    const query: Record<string, string> = {}

    // 保留现有参数
    Object.keys(route.query).forEach(key => {
        const value = route.query[key]
        if (value) {
            const stringValue = Array.isArray(value) ? value[0] : value
            if (stringValue) {
                query[key] = String(stringValue)
            }
        }
    })

    // 更新新参数
    Object.keys(params).forEach(key => {
        const value = params[key]
        if (value !== null && value !== undefined && value !== '') {
            query[key] = String(value)
        } else {
            delete query[key]
        }
    })

    router.push({ query })
}
