import request from '@/utils/request'

/** 查询平台开关（language_switch / auto_translate_switch 等） */
export function querySysSwitch(configKey: string) {
    return request.get(
        { url: `/base/sys-config/switch/query-language`, params: { configKey } },
        // 该配置是公开读接口。不要透传商家 token，避免过期登录态或认证服务
        // 短暂抖动拖住页面初始化，导致依赖配置的多语言 Tab 被误隐藏。
        { urlPrefix: 'web', withToken: false }
    )
}

export function isSwitchOn(raw: any): boolean {
    const data = raw?.data ?? raw
    const v = data?.configValue ?? data?.config_value
    return Number(v) === 1
}
