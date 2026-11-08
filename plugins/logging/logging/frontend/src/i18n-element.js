export const t = (key) => String(key)
export const useI18n = () => ({ t, locale: { value: 'zh-CN' } })
export const getLocale = () => 'zh-CN'
export const setLocale = () => true
export default { t, useI18n, getLocale, setLocale }
