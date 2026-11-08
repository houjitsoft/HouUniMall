import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'
import { flattenWidgets, goodsItems, visibleItems, widgetStyle, loadDecorationRuntime, supportedWidgets, teaTheme, themedWidget, pageStyle, templateClass, templateDemoMap, swapDemoUrl, teaGoodsAssets } from '../src/components/decoration-rules.mjs'

test('奢侈品简单风格(tea)网站风格继承暖象牙，显式背景与无背景优先', () => {
    const website = { current: 'tea_theme', list: [teaTheme] }
    assert.equal(pageStyle({ title_type: 0 }, website).backgroundColor, '#f4f1ea')
    assert.equal(pageStyle({ title_type: 2, bg_color: '#123456' }, website).backgroundColor, '#123456')
    // title_type=1（沿用主题默认色）在 tea 风格下必须落到暖象牙，否则 tea 底色永远不生效
    assert.equal(pageStyle({ title_type: 1 }, website).backgroundColor, '#f4f1ea')
    // 页面套用 tea 模板时同样吃模板底色，显式自定义背景仍最高优先
    assert.equal(pageStyle({}, {}, { templateCode: 'tea' }).backgroundColor, '#f4f1ea')
    assert.equal(pageStyle({ title_type: 1 }, {}, { templateCode: 'tea' }).backgroundColor, '#f4f1ea')
    assert.equal(pageStyle({ title_type: 2, bg_color: '#123456' }, {}, { templateCode: 'tea' }).backgroundColor, '#123456')
    // 未套模板、非 tea 风格时保持原行为：title_type=1 不继承，回到默认灰白
    assert.equal(pageStyle({ title_type: 1 }, {}, {}).backgroundColor, '#f8f8f8')
})
test('奢侈品简单风格(tea)：版式以模板为准，内容仍以页面为准', () => {
    const website = { current: 'tea_theme' }
    // 页面里早先保存的版式（show_type=1 单列）必须被模板的双列接管，
    // 否则后台换模板前台看不出任何变化。
    const widget = { name: 'goodslists', content: { show_type: 1, goodsLists: { list: [{ id: 1 }] } }, styles: { padding: 0, color: '#123456' } }
    const before = JSON.stringify(widget)
    const result = themedWidget(widget, website)
    // 版式字段：模板赢
    assert.equal(result.content.show_type, 3)
    // tea 版式的留白由 .dc-tea-sec 统一给，组件自身不再挂内边距
    assert.equal(result.styles.padding, '0')
    // 内容字段：页面赢
    assert.deepEqual(result.content.goodsLists.list, [{ id: 1 }])
    // 配色不再由组件 styles 下发：tea 的文字色统一由 tea-template.css 控制，页面里的旧 color 原样保留
    assert.equal(result.styles.color, '#123456')
    // 不修改入参对象
    assert.equal(JSON.stringify(widget), before)

    const alias = themedWidget({ name: 'title', styles: { padding_top: 0, 'margin-bottom': 0 } }, website)
    assert.equal(alias.styles.padding, '0')
    assert.equal(alias.styles.paddingTop, '0px')
    assert.equal(alias.styles.marginBottom, '0px')

    assert.equal(themedWidget({ name: 'goodslists' }, website).content.show_type, 3)
    // tea 的导航是单行 5 列图标条（奢侈品质感），不是多行宫格
    assert.equal(themedWidget({ name: 'nav' }, website).content.type, 1)
    assert.equal(themedWidget({ name: 'nav' }, website).content.show_nums, 5)
    assert.equal(themedWidget({ name: 'nav' }, website).content.show_rows, 1)
    // 未知模板 code 不做任何干预（四套内置模板都会下发各自的默认版式）
    assert.equal(themedWidget(widget, {}, { templateCode: 'not-exist' }), widget)
    // 页面根节点 class 供样式表整页换肤
    assert.equal(templateClass(website), 'dc-tpl-tea')
    assert.equal(templateClass({}, {}), '')
})
test('tea 模板替换内置演示图，商家真实图不受影响', () => {
    const website = { current: 'tea_theme' }
    const map = templateDemoMap(website, {}, 'shoplists')
    // 键只取文件名，pc/demo 与 uploads/mall-demo 两种写法都要命中，且保留原域名
    // tea 的演示图已换成实拍池（photo-0X.jpg），这里按替换表里的实际值断言，避免再写死文件名
    assert.equal(swapDemoUrl('https://x.com/pc/demo/goods-tee.svg', map), `https://x.com${teaGoodsAssets['goods-tee.svg']}`)
    assert.equal(swapDemoUrl('/uploads/mall-demo/goods-dress.svg', map), teaGoodsAssets['goods-dress.svg'])
    // 商家自己上传的图不在替换表里，必须原样返回
    assert.equal(swapDemoUrl('https://x.com/uploads/2026/mine.jpg', map), 'https://x.com/uploads/2026/mine.jpg')
    // 未套模板时不下发替换表，前台保持原样
    assert.equal(templateDemoMap({}, {}, 'shoplists'), null)
    // 商品列表与店铺列表共用同一张演示图替换表
    assert.equal(templateDemoMap(website, {}, 'goodslists'), teaGoodsAssets)
    const widget = { name: 'goodslists', content: { goodsLists: { list: [{ id: 1, image: '/pc/demo/goods-tee.svg' }] } } }
    assert.equal(themedWidget(widget, website).content.goodsLists.list[0].image, teaGoodsAssets['goods-tee.svg'])
})

test('tea 商品图与店铺图素材齐备', () => {
    const root = new URL('../../../public/', import.meta.url)
    for (const file of Object.values(teaGoodsAssets)) {
        // 实拍池是 jpg，占位图是 svg：只校验文件存在且非空，格式交给浏览器
        assert.ok(readFileSync(new URL('.' + file, root)).length > 1024, `缺少素材 ${file}`)
    }
})

test('奢侈品简单风格(tea)双端定义一致，PC模板选择按端查页面', () => {    const base = readFileSync(new URL('../../../backend/paas/base/src/main/java/com/houjiang/base/service/template/MobilePageTemplates.java', import.meta.url), 'utf8')
    const mall = readFileSync(new URL('../../../backend/paas/mall/src/main/java/com/houjiang/mall/decoration/MobileTemplateViews.java', import.meta.url), 'utf8')
    const definition = /new Template\("tea", "奢侈品简单风格", "([^"]+)",\s*"#b0805a", "tea-brand"\)/
    assert.equal(base.match(definition)?.[1], mall.match(definition)?.[1])
    assert.ok(base.match(definition))
    const select = readFileSync(new URL('../../admin/src/views/mall/decoration/pages/TemplateSelect.vue', import.meta.url), 'utf8')
    assert.match(select, /pagePlatform: isPc.value \? 'pc' : 'mobile'/)
})

test('网站风格保存完整序列化并保留PC模板字段', async () => {
    const source = readFileSync(new URL('../../admin/src/views/mall/decoration/pages/ThemeStyle.vue', import.meta.url), 'utf8')
    const body = source.slice(source.indexOf('const handleSave = async () => {'), source.indexOf('\nonMounted(async'))
    const theme = { current: 'tea_theme', list: [teaTheme], pc_template_code: 'tea', pc_floors: [{ _dragId: 'a', categoryId: 0, source: 'hotGoods', visible: true }] }
    let payload
    const save = new Function('theme', 'saving', 'level1Categories', 'setMallStyle', 'loadLevel1Categories', 'buildDefaultPcFloors', 'createPcFloorDragId', 'normalizeCategoryId', 'ElMessage', `${body}; return handleSave`)(theme, { value: false }, { value: [1] }, async data => { payload = data; return { code: 1 } }, async () => {}, () => [], () => 'id', Number, { success() {}, error(message) { throw Error(message) } })
    await save()
    assert.equal(payload.data.pc_template_code, 'tea')
    assert.equal(payload.data.current, 'tea_theme')
    assert.equal(payload.data.list[0].bg_color, '#f4f1ea')
    assert.equal(payload.data.pc_floors[0]._dragId, undefined)
})

const home = readFileSync(new URL('../src/components/home/home.vue', import.meta.url), 'utf8')
const method = home.slice(home.indexOf('applyTemplateLayout(component) {'), home.indexOf('\n\t\t\thandleImgSearch(){'))
const apply = new Function(`return ({${method}}).applyTemplateLayout`)()
for (const template of ['double-column', 'single-column', 'banner-first', 'tea-brand']) {
    test(`${template} 不覆盖显式商品版式`, () => {
        const json = Object.freeze({ style: '3', items: Object.freeze([]) })
        assert.equal(apply.call({ templateView: { style: template } }, { moduleSign: 'moduleGoods', moduleJson: json }), json)
    })
}
test('模板仅为缺少版式的旧商品模块提供默认值', () => {
    const json = Object.freeze({ items: [] })
    assert.equal(apply.call({ templateView: { style: 'double-column' } }, { moduleSign: 'moduleGoods', moduleJson: json }).style, '2')
    assert.equal(json.style, undefined)
})
test('宽幅模板不覆盖任何显式间距', () => {
    const json = Object.freeze({ padding: '12px', padTop: '8px', padBom: '9px', pageMargin: '6px' })
    assert.deepEqual(apply.call({ templateView: { style: 'banner-first' } }, { moduleSign: 'moduleSwiper', moduleJson: json }), json)
})
test('共享widget与样式不会写回保存配置', () => {
    const input = [{ name: 'goodslists', content: { goodsLists: { list: [{ id: 1, price: 0 }] } }, styles: { font_size: 18, color: '#123456', margin_top: 0 } }]
    const before = JSON.stringify(input)
    assert.equal(flattenWidgets([input])[0], input[0])
    assert.deepEqual(widgetStyle(input[0].styles), { fontSize: '18px', color: '#123456', marginTop: '0px' })
    assert.equal(goodsItems(input[0].content.goodsLists.list)[0].price, 0)
    assert.equal(JSON.stringify(input), before)
})
test('条目隐藏和空选择不填入演示商品', () => {
    assert.equal(visibleItems([{ is_show: 0 }, { is_show: 2 }, { is_show: 1 }, {}]).length, 2)
    assert.deepEqual(goodsItems([{}]), [])
})
test('拒绝非展示属性和危险URL', () => {
    assert.deepEqual(widgetStyle({ onclick: 'alert(1)', background: 'url(javascript:alert(1))', position: 'fixed', fontSize: 16 }), { fontSize: '16px' })
})
test('真实动态数据加载失败会暴露错误而非伪造内容', async () => {
    const runtime = await loadDecorationRuntime(async () => { throw Error('offline') })
    assert.equal(runtime.errors.length, 2)
    assert.deepEqual(runtime.shops, [])
})
test('店铺和活动数据使用同一公开数据源', async () => {
    const runtime = await loadDecorationRuntime(async path => path.includes('legacyHome') ? { code: 1, data: { pageData: JSON.stringify({ moduleList: [{ moduleSign: 'moduleSecKill', moduleJson: { items: [1] } }] }) } } : { code: 1, data: { lists: [{ id: 1 }] } })
    assert.equal(runtime.modules.moduleSecKill.items[0], 1)
    assert.equal(runtime.shops[0].id, 1)
    assert.equal(runtime.errors.length, 0)
})
test('前后台引用同一渲染SFC，旧页面渲染保留', () => {
    const admin = readFileSync(new URL('../../admin/src/views/mall/decoration/components/editor/index-preview.vue', import.meta.url), 'utf8')
    const h5 = readFileSync(new URL('../src/components/homeIndex/homeIndex.vue', import.meta.url), 'utf8')
    assert.match(admin, /@decoration\/DecorationWidget.vue/)
    assert.match(h5, /\.\.\/DecorationWidget.vue/)
    assert.match(admin, /v-if="sharedRendering/)
    assert.match(h5, /v-else class="pb-30"/)
})
test('未知组件明确提示且不静默跳过', () => {
    const widget = readFileSync(new URL('../src/components/DecorationWidget.vue', import.meta.url), 'utf8')
    assert.match(widget, /暂不支持组件/)
    assert.match(widget, /配置已保留/)
    for (const name of ['shoplists', 'choice', 'seckill', 'team', 'subsidy']) assert.ok(supportedWidgets.includes(name))
})
