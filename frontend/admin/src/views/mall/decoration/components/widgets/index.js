const widgets = import.meta.glob('./**/index.js', { eager: true })
const exportWidgets = {}
Object.keys(widgets).forEach((key) => {
    const widgetName = key.replace(/^\.\/([\w-]+).*/gi, '$1')
    exportWidgets[widgetName] = widgets[key]?.default
})

export default exportWidgets
