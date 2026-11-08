/**
 * 添加单位
 * @param {string|number} value - 数值
 * @param {string} unit - 单位，默认为 px
 * @returns {string} 带单位的值
 */
export function addUnit(value, unit = 'px') {
    if (value == null || value === '') return ''
    if (typeof value === 'string' && (value.endsWith('px') || value.endsWith('%') || value.endsWith('em') || value.endsWith('rem') || value.endsWith('vh') || value.endsWith('vw'))) {
        return value
    }
    return `${value}${unit}`
}
