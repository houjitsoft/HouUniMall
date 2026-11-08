/**
 * 商品名称展示：已本地化（无中文）直接显示；仍是中文则走 $t 机翻兜底。
 */
export function displayGoodsName(name: string | null | undefined, t: (k: string) => string, lang?: string): string {
    const text = name == null ? '' : String(name)
    if (!text) return ''
    const code = (lang || 'cn').toLowerCase()
    if (!code || code === 'cn' || code === 'zh' || code === 'zh-cn' || code === 'zh_cn') {
        return text
    }
    // 已无中日韩统一表意文字，视为后台已发布译文
    if (!/[\u4e00-\u9fff]/.test(text)) {
        return text
    }
    return t(text)
}
