/**
 * Vue 全局属性类型扩展
 * 为插件挂载到 globalProperties 上的属性提供类型声明，供模板与 script 中直接使用
 */
declare module 'vue' {
    interface ComponentCustomProperties {
        /** 全局翻译方法：中文 key 转当前语言文案（由 translate 插件注入） */
        $t: (key: string) => string
    }
}

export {}
