<template>
    <div v-if="preview || Number(content.enabled ?? 1) !== 0" class="dc-widget" :data-widget="widget.name" :style="widgetStyle(widget.styles)">
        <div v-if="unsupportedConfiguration" class="dc-unsupported" role="status">{{ unsupportedConfiguration }}，配置已保留</div>
        <!--
            搜索：tea 版式里收成一条细线搜索，位于 hero 之下，不占首屏。
            tea.houjit.com 本身没有搜索入口，但商城必须保留搜索能力，
            因此不删模块，只把它压成与编辑版式一致的细线。
        -->
        <div v-if="widget.name === 'search'" class="dc-search" :class="{ 'dc-search-tea': isTea }" @click="go('/general/searchpage/searchpage')"><div :style="{ justifyContent: isTea ? 'center' : (content.text_type == 2 ? 'center' : 'flex-start') }"><span class="dc-search-icon">⌕</span>{{ content.text }}</div></div>
        <!--
            首屏：tea 版式下 banner 变成整屏 hero（大图 + 暗渐变遮罩 + 眉标/标题/副题/双按钮 + 底部细字）。
            多页轮播：每张 banner 是一页，移动端一屏 1 张并露出下一张的边，桌面端一屏 3 张；
            支持左右滑动（原生 scroll-snap）、箭头翻页、5 秒自动轮播，文案随页切换。
        -->
        <template v-else-if="['banner', 'middle-banner'].includes(widget.name)">
            <div v-if="isTea && widget.name === 'banner'" class="dc-hero-rail" :class="{ 'is-multi': heroSlides.length > 1 }">
                <div ref="heroTrack" class="dc-hero-track" @scroll.passive="onHeroScroll">
                    <div v-for="(page, i) in heroSlides" :key="i" class="dc-hero dc-hero-page" :class="{ 'is-active': !heroScrollable || heroIndex === i }" @click="go(page.link)">
                        <img class="dc-hero-img" :src="page.image" />
                        <div class="dc-hero-mask"></div>
                        <div class="dc-hero-text">
                            <span class="dc-hero-eyebrow">{{ page.eyebrow }}</span>
                            <h2 class="dc-hero-title">{{ page.title }}</h2>
                            <span class="dc-hero-sub">{{ page.sub }}</span>
                            <div class="dc-hero-actions">
                                <span class="dc-hero-btn light" @click.stop="go(page.primary.link)">{{ page.primary.text }}<i aria-hidden="true">→</i></span>
                                <span class="dc-hero-btn" @click.stop="go(page.secondary.link)">{{ page.secondary.text }}<i aria-hidden="true">→</i></span>
                            </div>
                        </div>
                    </div>
                    <div v-if="!heroSlides.length" class="dc-hero dc-hero-page is-empty"><div class="dc-empty">暂无图片</div></div>
                </div>
                <div v-if="heroScrollable && heroSlides.length > 1" class="dc-hero-arrows">
                    <button type="button" class="dc-hero-arrow prev" :class="{ 'is-off': heroIndex === 0 }" @click.stop="heroGo(heroIndex - 1)" aria-label="上一页">←</button>
                    <button type="button" class="dc-hero-arrow next" :class="{ 'is-off': heroIndex >= heroSlides.length - 1 }" @click.stop="heroGo(heroIndex + 1)" aria-label="下一页">→</button>
                </div>
                <div class="dc-hero-bottom">
                    <span class="dc-hero-scroll">{{ hero.footLeft }}</span>
                    <span v-if="heroScrollable && heroSlides.length > 1" class="dc-hero-pager">{{ pager }}</span>
                    <span>{{ hero.footRight }}</span>
                </div>
            </div>
            <div v-else class="dc-banner" :class="{ 'dc-banner-tea': isTea }">
                <div v-if="images.length" class="dc-slide" @click="go(currentImage.link)"><img :src="currentImage.image" :style="{ height: content.image_type == 2 ? '550px' : 'auto' }" /></div>
                <div v-if="images.length > 1" class="dc-dots"><span role="button" v-for="(_, i) in images" :key="i" :class="{ active: slide === i }" @click="slide = i" :aria-label="`第${i + 1}张`"></span></div>
                <div v-if="!images.length" class="dc-empty">暂无图片</div>
            </div>
        </template>
        <!-- 公告：tea 版式下是一条居中细字带（上下细线），不做白卡片 -->
        <div v-else-if="widget.name === 'notice'" class="dc-notice" :class="{ 'dc-notice-tea': isTea }">
            <template v-if="isTea">
                <span class="dc-notice-tea-text" @click="go(notices[noticeIndex % Math.max(notices.length, 1)]?.link)">{{ notices[noticeIndex % Math.max(notices.length, 1)]?.title || '暂无公告' }}</span>
            </template>
            <template v-else>
                <span v-if="content.title_type == 1" class="dc-notice-title">{{ content.title }}</span><img v-else class="dc-title-image" :src="content.titile_img" />
                <span class="dc-divider">|</span><span class="dc-ellipsis" @click="go(notices[noticeIndex % Math.max(notices.length, 1)]?.link)">{{ notices[noticeIndex % Math.max(notices.length, 1)]?.title || '暂无公告' }}</span><span v-if="content.right_btn" class="dc-more">›</span>
            </template>
        </div>
        <div v-else-if="widget.name === 'nav'" class="dc-nav" :class="{ 'dc-nav-scroll': content.show_type == 2, 'dc-nav-tea': isTea }" :style="{ gridTemplateColumns: `repeat(${navColumns}, minmax(0, 1fr))` }">
            <div v-for="(item, i) in navItems" :key="i" @click="go(item.link)"><img v-if="content.type != 3" :src="item.image" /><span v-if="content.type != 2">{{ item.name }}</span></div>
        </div>
        <!-- 图立方：tea 版式下是深色影像区（两列错落 + 播放环），对应茶站的「以影像记录风物」 -->
        <template v-else-if="widget.name === 'cube'">
            <div v-if="isTea" class="dc-tea-sec dc-tea-sec-dark dc-tea-lookbook">
                <div class="dc-tea-head">
                    <span class="eyebrow">{{ sectionHead.eyebrow }}</span>
                    <h3 class="t-h1">{{ sectionHead.title }}</h3>
                    <p v-if="sectionHead.desc" class="t-body">{{ sectionHead.desc }}</p>
                </div>
                <div class="dc-cube dc-cube-tea">
                    <div v-for="(item, i) in cubeItems" :key="i" @click="go(item.link)"><img :src="item.image" /></div>
                </div>
            </div>
            <div v-else class="dc-cube" :class="`dc-cube-${content.image_type ?? 0}`">
                <div v-for="(item, i) in cubeItems" :key="i" @click="go(item.link)"><img :src="item.image" /></div>
            </div>
        </template>
        <div v-else-if="widget.name === 'title'" class="dc-title" :class="{ 'dc-title-center': content.style == 2, 'dc-tea-head': isTea }">
            <template v-if="isTea">
                <span v-if="content.subtitle" class="eyebrow">{{ content.subtitle }}</span>
                <h3 class="t-h1">{{ content.title }}</h3>
                <span v-if="content.right && content.right_title" class="dc-tea-more" @click="go(content.link)">{{ content.right_title }}</span>
            </template>
            <template v-else>
                <span :style="{ fontSize: `${content.title_size || 16}px`, color: content.title_color }" @click="go(content.link)">{{ content.title }}</span><span class="dc-subtitle" :style="{ fontSize: `${content.subtitle_size || 12}px`, color: content.subtitle_color }">{{ content.subtitle }}</span><span v-if="content.right && content.style != 2" class="dc-more" :style="{ color: content.right_color }" @click="go(content.link)">{{ content.right_title }}</span>
            </template>
        </div>
        <div v-else-if="widget.name === 'blank'" :style="{ height: `${content.height ?? 20}px`, background: content.bg_color || 'transparent' }"></div>
        <!-- 商品区：tea 版式下是「区块头（眉标/标题/说明居中）+ 双列竖图无卡片网格」 -->
        <template v-else-if="['goodslists', 'subsidy', 'seckill', 'team', 'choice'].includes(widget.name)">
            <!-- tea 版式：没有可展示商品时整块不渲染，否则会留一整屏空白（秒杀 / 拼团常缺数据） -->
            <div v-if="!isTea || products.length" :class="isTea ? 'dc-tea-sec' : ''">
                <div v-if="isTea" class="dc-tea-head">
                    <span class="eyebrow">{{ sectionHead.eyebrow }}</span>
                    <h3 class="t-h1">{{ sectionHead.title }}</h3>
                    <p v-if="sectionHead.desc" class="t-body">{{ sectionHead.desc }}</p>
                </div>
                <div v-if="widget.name === 'choice'" class="dc-tabs"><span role="button" v-for="(tab, i) in content.data || []" :key="i" :class="{ active: selectedTab === i }" @click="selectedTab = i"><span>{{ tab.title }}</span><small>{{ tab.sub_title }}</small></span></div>
                <div v-if="['seckill', 'team'].includes(widget.name)" class="dc-section-title"><span v-if="content.titleType == 1">{{ content.title }}</span><img v-else class="dc-title-image" :src="content.titile_img" /><span v-if="content.rightBtn" class="dc-more" :style="{ color: content.rightcolor }" @click="go(activityLink)">{{ content.rightTitle }}</span></div>
                <div v-if="widget.name === 'subsidy' && content.subsidy_tip" class="dc-tip">{{ content.subsidy_tip }}</div>
                <div class="dc-goods" :class="[`dc-layout-${layout}`, { 'dc-horizontal': layout === 5, 'dc-goods-tea': isTea }]">
                    <div v-for="(item, i) in products" :key="`${item.id}-${i}`" class="dc-card" @click="product(item)">
                        <img class="dc-product-image" :src="item.image" />
                        <div class="dc-card-body">
                            <div v-if="show(1)" class="dc-product-name">{{ item.name }}</div>
                            <div class="dc-price-row"><span v-if="show(2)" class="dc-price"><small>¥</small>{{ item.price ?? 0 }}</span><span v-if="show(3) && item.max_line_price != null" class="dc-market">¥{{ item.max_line_price }}</span></div>
                            <div v-if="show(4)" class="dc-sales">已售{{ item.sale_num || 0 }}</div>
                            <div v-if="show(5) && item.shopName" class="dc-shop-name" @click.stop="shop(item.shopId)">{{ item.shopName }}　进店</div>
                            <span role="button" v-if="content.cart_btn && widget.name === 'goodslists' && !isTea" class="dc-buy" @click.stop="product(item)"><img v-if="content.btn_type == 2 && content.cart_image" :src="content.cart_image" /><span v-else>{{ content.cart_style == 2 ? '购买' : '+' }}</span></span>
                            <span role="button" v-if="['seckill', 'team'].includes(widget.name)" class="dc-activity-buy" @click.stop="product(item)">{{ widget.name === 'team' ? '去拼团' : '去抢购' }}</span>
                        </div>
                    </div>
                </div>
                <div v-if="!products.length" class="dc-empty">{{ dataError || '暂无可展示商品' }}</div>
            </div>
        </template>
        <!-- 店铺区：tea 版式下是编辑式店铺行（细线分隔、衬线店名），不做白卡片 -->
        <template v-else-if="widget.name === 'shoplists'">
            <div :class="isTea ? 'dc-tea-sec' : ''">
                <div v-if="isTea" class="dc-tea-head">
                    <span class="eyebrow">{{ sectionHead.eyebrow }}</span>
                    <h3 class="t-h1">{{ sectionHead.title }}</h3>
                    <p v-if="sectionHead.desc" class="t-body">{{ sectionHead.desc }}</p>
                </div>
                <div v-else class="dc-section-title"><span v-if="content.title_type == 1">{{ content.title }}</span><img v-else class="dc-title-image" :src="content.titile_img" /><span v-if="content.right_btn" class="dc-more" :style="{ color: content.right_color }" @click="go('/general/shoplist/shoplist')">{{ content.right_title }}</span></div>
                <div v-for="item in stores" :key="item.id" class="dc-store" :class="{ 'dc-store-tea': isTea }">
                    <div class="dc-store-head" @click="shop(item.id)"><img :src="item.logo || item.image" /><div><div>{{ item.name }}</div><small v-if="item.goods_count != null">在售商品 {{ item.goods_count }} 件</small></div><span role="button" class="dc-store-button">进店</span></div>
                    <div v-if="content.show_type == 1" class="dc-store-goods"><div v-for="good in (item.goods || []).slice(0, 3)" :key="good.id" @click="product(good)"><img :src="good.image" /><span>¥{{ good.min_price ?? good.price }}</span></div></div>
                </div>
                <div v-if="!stores.length" class="dc-empty">{{ runtime.errors?.find(x => x.includes('店铺')) || '暂无可展示店铺' }}</div>
            </div>
        </template>
        <template v-else-if="widget.name === 'news' && !(isTea && !articles.length)">
            <div v-for="item in articles" :key="item.informationId" class="dc-article" @click="go(`/activities/informationDetails/informationDetails?id=${item.informationId}`)"><img v-if="item.informationCoverList?.[0]" :src="item.informationCoverList[0]" /><div><div>{{ item.informationTitle }}</div><small>{{ item.releaseTime }}</small></div></div>
            <div v-if="!articles.length" class="dc-empty">{{ dataError || '暂无资讯' }}</div>
        </template>
        <div v-else class="dc-unsupported" role="status">暂不支持组件：{{ widget.title || widget.name }}（{{ widget.name }}），配置已保留</div>
    </div>
</template>
<script setup>
import { computed, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { goodsItems, visibleItems, widgetStyle, swapDemoUrl, teaContent } from './decoration-rules.mjs'
const props = defineProps({
    widget: { type: Object, required: true },
    runtime: { type: Object, default: () => ({ modules: {}, shops: [], errors: [] }) },
    preview: Boolean,
    /**
     * 模板演示素材替换表。店铺货品图来自 /web/mall/front/shop/list 的运行时数据，
     * 不经过 themedWidget，只能在这里按表换掉内置演示图。
     */
    demoMap: { type: Object, default: null },
    /**
     * 当前生效的页面模板 code（tea / simple / vivid / premium）。
     * tea 走编辑式版式分支：整屏 hero、居中区块头、双列无卡片网格、深色影像区，
     * 与 tea.houjit.com 的首页一一对应；其余模板沿用标准装修组件版式。
     */
    templateCode: { type: String, default: '' },
    /**
     * 模板的整张演示素材表（{ banner: {...}, goodslists: {...}, cube: {...} }）。
     *
     * <p>goodslists / seckill / team 的商品来自运行时接口（moduleGoods / moduleSeckill），
     * 不走 themedWidget，也就换不到模板素材，页面上会混进标品的彩色占位图。
     * 这里把整张表下发，组件按自己的 name 取对应那一段。
     */
    demoAssets: { type: Object, default: null }
})
const emit = defineEmits(['navigate'])
const content = computed(() => props.widget.content || {})
const slide = ref(0)
const noticeIndex = ref(0)
const selectedTab = ref(0)
const images = computed(() => visibleItems(content.value.data))
const currentImage = computed(() => images.value[slide.value % Math.max(images.value.length, 1)] || {})
const notices = computed(() => content.value.lists || [])
const navColumns = computed(() => [3, 4, 5][Number(content.value.show_nums || 3) - 1] || 5)
const navItems = computed(() => visibleItems(content.value.data).slice(0, content.value.show_type == 2 ? undefined : navColumns.value * Number(content.value.show_rows || 2)))
const cubeItems = computed(() => visibleItems(content.value.data).slice(0, [1, 2, 3, 3, 4, 3][Number(content.value.image_type || 0)] || 4))
/** 当前组件对应的演示素材表：优先按组件名取整表里那一段，其次用旧的 demoMap */
const swapMap = computed(() => props.demoAssets?.[props.widget.name] || props.demoMap || null)
const isTea = computed(() => props.templateCode === 'tea')
/** tea 版式首屏文案（眉标 / 大标题 / 副题 / 两个引导按钮 / 底部细字） */
const hero = computed(() => teaContent.hero)
/**
 * 首屏多页轮播的每一页：一张 banner 一页，文案按 teaContent.hero.slides 顺序取。
 * 图片比文案多时复用最后一组文案，保证每页都有眉标/标题，不会出现半空的页。
 */
const heroSlides = computed(() => {
    const copy = hero.value.slides || []
    return images.value.map((item, i) => {
        const c = copy[i] || copy[copy.length - 1] || {}
        return {
            image: item.image,
            link: item.link,
            eyebrow: c.eyebrow || hero.value.eyebrow,
            title: c.title || hero.value.title,
            sub: c.sub || hero.value.sub,
            primary: c.primary || hero.value.primary,
            secondary: c.secondary || hero.value.secondary
        }
    })
})
const heroIndex = ref(0)
const heroTrack = ref(null)
const pager = computed(() => `${String(heroIndex.value + 1).padStart(2, '0')} / ${String(Math.max(heroSlides.value.length, 1)).padStart(2, '0')}`)
/**
 * 区块头文案：装修页写了 content.head 以页面为准，否则取模板预设。
 * 商品 / 影像 / 店铺这些模块本身只存图片和数据，没有「眉标 + 标题 + 说明」三个文字位，
 * 缺了这段版式就会退化成一排商品，看不出编辑感。
 */
const sectionHead = computed(() => {
    const own = content.value.head || {}
    const preset = teaContent.sections[props.widget.name] || {}
    return { eyebrow: own.eyebrow || preset.eyebrow || '', title: own.title || preset.title || '', desc: own.desc ?? preset.desc ?? '' }
})
const layout = computed(() => {
    if (props.widget.name === 'choice') return content.value.show_type == 1 ? 1 : 3
    if (['team', 'seckill'].includes(props.widget.name)) return [2, 3, 4, 5][Number(content.value.showType || 1) - 1] || 2
    return Number(content.value.show_type || 1)
})
const unsupportedConfiguration = computed(() => {
    const c = content.value
    if (['goodslists', 'subsidy'].includes(props.widget.name) && Number(c.get_type) === 2) return '指定分类实时取数暂未接通，仅展示已保存商品'
    if (props.widget.name === 'shoplists' && Number(c.get_type) === 1 && Number(c.sort_type) === 2) return '店铺自定义排序暂未接通，当前使用服务端默认排序'
    if (['seckill', 'team'].includes(props.widget.name) && (Number(c.getType) > 1 || ['visit_desc', 'sale_change'].includes(String(c.sort || '').trim()))) return '该活动取数或排序配置暂未接通，当前使用服务端默认排序'
    return ''
})
const products = computed(() => {
    const c = content.value
    const map = swapMap.value
    // 运行时接口的商品图不会经过模板素材替换，在这里统一过一遍（商家真实图不含演示键，不受影响）
    const themed = items => (map ? items.map(x => ({ ...x, image: swapDemoUrl(x.image, map) })) : items)
    if (props.widget.name === 'choice') return themed(goodsItems(c.data?.[selectedTab.value]?.goodsLists?.list))
    if (props.widget.name === 'seckill') return themed(sortProducts(goodsItems((props.runtime.modules?.moduleSecKill?.items || []).flatMap(section => (section.items || []).map(x => ({ ...x, id: x.commonId, price: x.seckillPrice })))), c.sort).slice(0, Number(c.showNums || 3)))
    if (props.widget.name === 'team') return themed(sortProducts(goodsItems((props.runtime.modules?.modulePintuan?.items || []).map(x => x.items)), c.sort).slice(0, Number(c.showNums || 3)))
    const selected = goodsItems(c.goodsLists?.list)
    if (selected.length) return themed(selected)
    if (props.widget.name === 'goodslists' && Number(c.get_type) === 1) return themed(sortProducts(goodsItems(props.runtime.modules?.moduleGoods?.items), c.sort).slice(0, Number(c.goods_num || 10)))
    if (props.widget.name === 'subsidy' && Number(c.get_type) === 1) return themed(sortProducts(goodsItems((props.runtime.modules?.moduleDiscount?.items || []).flatMap(section => section.items || [])), c.sort))
    return []
})
function sortProducts(items, sort) {
    const result = [...items]
    if (sort === 'sale_desc') return result.sort((a, b) => Number(b.sale_num || 0) - Number(a.sale_num || 0))
    if (sort === 'price_desc') return result.sort((a, b) => Number(b.price || 0) - Number(a.price || 0))
    if (sort === 'price_asc') return result.sort((a, b) => Number(a.price || 0) - Number(b.price || 0))
    return result
}
const stores = computed(() => {
    const selected = (content.value.storelists || []).filter(x => x.id || x.name)
    const list = (selected.length ? selected : props.runtime.shops || []).slice(0, Number(content.value.showNums || 3))
    const map = swapMap.value
    if (!map) return list
    // 只替换内置演示图：店铺头像与货品图都按同一张表处理
    return list.map(shop => ({
        ...shop,
        logo: swapDemoUrl(shop.logo, map),
        image: swapDemoUrl(shop.image, map),
        goods: (shop.goods || []).map(good => ({ ...good, image: swapDemoUrl(good.image, map) }))
    }))
})
const articles = computed(() => props.runtime.modules?.moduleArticle?.items || [])
const dataError = computed(() => props.runtime.errors?.find(x => !x.includes('店铺')))
const activityLink = computed(() => props.widget.name === 'team' ? '/activities/pintuan_index/pintuan_index' : '/activities/secondKillGoods/secondKillGoods')
const show = n => !content.value.show_content || content.value.show_content.map(Number).includes(n)
function go(link) { if (!props.preview && link) emit('navigate', link) }
/**
 * 所有页都已落在视口内（桌面三张并排）时不再翻页：
 * 三栏各显示自己的文案，箭头和页码一并隐藏，避免「箭头点了没反应」。
 */
const heroScrollable = ref(true)
function heroMetrics() {
    const el = heroTrack.value
    if (!el || !el.children.length) return null
    const gap = parseFloat(getComputedStyle(el).columnGap || getComputedStyle(el).gap || 0) || 0
    const pageWidth = el.children[0].getBoundingClientRect().width
    return { el, gap, pageWidth, max: el.scrollWidth - el.clientWidth }
}
function measureHeroOverflow() {
    const m = heroMetrics()
    if (!m) return
    heroScrollable.value = m.max > 4
}
/** 翻到第 i 页（越界循环）：按页居中滚动，两侧同时露出相邻页 */
function heroGo(i) {
    const m = heroMetrics()
    const total = heroSlides.value.length
    if (!m || !total) return
    const idx = ((i % total) + total) % total
    heroIndex.value = idx
    const child = m.el.children[idx]
    if (!child) return
    const box = m.el.getBoundingClientRect()
    const pageLeft = child.getBoundingClientRect().left - box.left + m.el.scrollLeft
    const target = Math.max(0, Math.min(m.max, pageLeft - (m.el.clientWidth - m.pageWidth) / 2))
    m.el.scrollTo({ left: target, behavior: 'smooth' })
}
let heroFrame = 0
/** 手指/滚轮拖动后同步页码：取离视口中心最近的一页 */
function onHeroScroll() {
    if (heroFrame) return
    heroFrame = requestAnimationFrame(() => {
        heroFrame = 0
        const m = heroMetrics()
        if (!m) return
        const box = m.el.getBoundingClientRect()
        const center = box.left + m.el.clientWidth / 2
        let best = 0
        let bestDistance = Infinity
        ;[...m.el.children].forEach((child, i) => {
            const rect = child.getBoundingClientRect()
            const distance = Math.abs(rect.left + rect.width / 2 - center)
            if (distance < bestDistance) { bestDistance = distance; best = i }
        })
        heroIndex.value = Math.min(best, Math.max(heroSlides.value.length - 1, 0))
    })
}
function product(item) { if (item.id || item.commonId) go(`/pages/description/descrption?goodsId=${encodeURIComponent(item.goodsId || item.id || item.commonId)}&commonId=${encodeURIComponent(item.commonId || item.id)}`) }
function shop(id) { if (id) go(`/store/index/index?shopId=${encodeURIComponent(id)}`) }
let timer
onMounted(() => {
    nextTick(measureHeroOverflow)
    if (typeof window !== 'undefined') window.addEventListener('resize', measureHeroOverflow)
    timer = setInterval(() => {
        // 首屏多页轮播：tea 下 banner 自己翻页，其余组件继续走原来的单张轮换
        if (isTea.value && props.widget.name === 'banner' && heroScrollable.value && heroSlides.value.length > 1) heroGo(heroIndex.value + 1)
        else slide.value = (slide.value + 1) % Math.max(images.value.length, 1)
        noticeIndex.value++
    }, 5000)
})
onBeforeUnmount(() => {
    clearInterval(timer)
    if (heroFrame) cancelAnimationFrame(heroFrame)
    if (typeof window !== 'undefined') window.removeEventListener('resize', measureHeroOverflow)
})
</script>
<style>
.dc-widget{font-family:Arial,"PingFang SC","Microsoft YaHei",sans-serif;font-size:14px;line-height:1.5;color:#333;font-weight:400;text-align:left;box-sizing:border-box;min-width:0;overflow:hidden}
.dc-widget *{box-sizing:border-box}.dc-widget img{display:block;max-width:100%;object-fit:cover}.dc-widget [role=button]{font:inherit;line-height:inherit;margin:0;border:0;padding:0;border-radius:0;min-height:0;background:none;color:inherit;cursor:pointer;text-align:center;display:inline-block}.dc-widget [role=button]:after{border:0}.dc-widget small{font-size:10px}.dc-search{background:#fff;padding:7px 12px}.dc-search>div{display:flex;align-items:center;gap:5px;height:36px;border-radius:36px;background:#f4f4f4;color:#999;padding:0 15px}.dc-search-icon{font-size:23px}.dc-banner{position:relative}.dc-slide img{width:100%}.dc-dots{position:absolute;bottom:8px;left:0;right:0;display:flex;justify-content:center;gap:5px}.dc-dots [role=button]{height:5px;width:5px;border-radius:50%;background:#fff9}.dc-dots [role=button].active{background:#333}.dc-notice{margin:0 8px;padding:8px;display:flex;align-items:center;background:#fff;border-radius:8px;gap:6px;font-size:12px}.dc-notice-title{flex-shrink:0;color:#333}.dc-divider{color:#ddd}.dc-ellipsis{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.dc-more{margin-left:auto;flex-shrink:0;font-size:12px;color:#888}.dc-title-image{width:56px;height:20px}.dc-nav{display:grid;background:#fff;padding:15px 0 8px;row-gap:15px}.dc-nav>div{display:flex;flex-direction:column;align-items:center;min-width:0;text-align:center}.dc-nav img{width:41px;height:41px}.dc-nav span{margin-top:7px}.dc-nav-scroll{display:flex;overflow-x:auto}.dc-nav-scroll>div{flex:0 0 20%}.dc-cube{display:grid;gap:2px}.dc-cube img{width:100%;height:100%}.dc-cube-0{grid-template-columns:1fr}.dc-cube-0 img{height:auto}.dc-cube-1,.dc-cube-4{grid-template-columns:repeat(2,minmax(0,1fr))}.dc-cube-2{grid-template-columns:repeat(3,minmax(0,1fr))}.dc-cube-3{grid-template-columns:repeat(2,minmax(0,1fr));grid-template-rows:repeat(2,100px)}.dc-cube-3>div:first-child{grid-row:span 2}.dc-cube-5{grid-template-columns:repeat(2,minmax(0,1fr))}.dc-cube-5>div:first-child{grid-column:span 2}.dc-title{margin:8px;padding:8px;background:#fff;border-radius:8px;display:flex;align-items:baseline;gap:4px}.dc-title-center{display:flex;flex-direction:column;align-items:center;text-align:center}.dc-subtitle{color:#666}.dc-section-title{display:flex;align-items:center;margin:8px 8px 0;padding:8px;background:#fff;border-radius:8px 8px 0 0;min-height:37px}.dc-tip{padding:8px;font-size:12px;color:#b67b2e}.dc-goods{display:grid;padding:8px;gap:8px}.dc-layout-1,.dc-layout-2{grid-template-columns:minmax(0,1fr)}.dc-layout-3{grid-template-columns:repeat(2,minmax(0,1fr))}.dc-layout-4{grid-template-columns:repeat(3,minmax(0,1fr))}.dc-horizontal{display:flex;overflow-x:auto;align-items:stretch;scrollbar-width:none}.dc-horizontal::-webkit-scrollbar{display:none}.dc-horizontal>.dc-card{flex:0 0 32%;min-width:0}.dc-card{background:#fff;border-radius:8px;overflow:hidden;min-width:0}.dc-product-image{width:100%;height:200px}.dc-layout-1 .dc-product-image{height:300px}.dc-layout-2 .dc-card{display:flex;height:116px}.dc-layout-2 .dc-product-image{width:100px;height:116px;flex-shrink:0}.dc-layout-4 .dc-product-image,.dc-layout-5 .dc-product-image{height:130px}.dc-card-body{padding:8px;min-width:0;position:relative;flex:1}.dc-product-name{overflow:hidden;text-overflow:ellipsis;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;line-height:20px;min-height:20px}.dc-price-row{display:flex;align-items:baseline;gap:6px;flex-wrap:wrap}.dc-price{color:#e44343;font-size:16px}.dc-market{text-decoration:line-through;color:#999;font-size:10px}.dc-sales,.dc-shop-name{font-size:10px;color:#999}.dc-widget .dc-buy{position:absolute;right:8px;bottom:8px;background:#b67b2e;color:white;border-radius:50%;width:22px;height:22px;text-align:center;font-size:12px}.dc-buy img{width:22px;height:22px}.dc-widget .dc-activity-buy{float:right;font-size:12px;border-radius:20px;padding:1px 12px;background:#b67b2e;color:#fff}.dc-tabs{display:flex;margin:8px 8px 0;background:#fff;border-radius:8px}.dc-tabs [role=button]{flex:1;text-align:center;padding:8px;min-width:0}.dc-tabs span,.dc-tabs small{display:block}.dc-tabs .active small{border-radius:8px;background:#7c4d30;color:#fff}.dc-store{background:#fff;padding:8px;margin:0 8px 8px;border-radius:0 0 8px 8px}.dc-store-head{display:flex;align-items:center;gap:8px}.dc-store-head>img{height:44px;width:44px;border-radius:8px}.dc-store-head small{color:#999}.dc-widget .dc-store-button{margin-left:auto;background:#b67b2e1a;color:#b67b2e;width:70px;line-height:30px;border-radius:20px}.dc-store-goods{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:8px;margin-top:8px;text-align:center}.dc-store-goods img{width:100%;height:100px}.dc-article{display:flex;padding:8px;margin:8px;background:#fff;gap:8px}.dc-article>img{width:100px;height:70px;flex-shrink:0}.dc-article small{color:#999}.dc-empty,.dc-unsupported{padding:16px 8px;text-align:center;color:#888;font-size:12px}.dc-unsupported{background:#fff4e5;color:#975a16;border:1px dashed #e6a23c}
.dc-tpl-tea .dc-search{background:transparent}
.dc-tpl-tea .dc-search>div{background:transparent;border:1px solid #ddd3c2;border-radius:0;color:#8b8375}
.dc-tpl-tea .dc-notice{background:transparent;border-radius:0;border-bottom:1px solid #e2d9c8;margin:0 16px;padding:12px 0;color:#4b453a}
.dc-tpl-tea .dc-nav{background:transparent;padding:20px 0 14px;row-gap:14px}
.dc-tpl-tea .dc-nav img{width:38px;height:38px}
.dc-tpl-tea .dc-nav span{font-family:Georgia,"Songti SC",serif;font-size:12px;letter-spacing:1px;color:#3b352c;margin-top:8px}
.dc-tpl-tea .dc-cube{gap:6px}
.dc-tpl-tea .dc-title{background:transparent;border-radius:0;margin:0;padding:30px 16px 12px;justify-content:center}
.dc-tpl-tea .dc-title span:first-child{font-family:Georgia,"Songti SC",serif;letter-spacing:4px}
.dc-tpl-tea .dc-title .dc-subtitle{letter-spacing:2px;font-size:11px}
.dc-tpl-tea .dc-goods{padding:4px 16px 36px;gap:20px 12px}
.dc-tpl-tea .dc-card{background:transparent;border-radius:0}
.dc-tpl-tea .dc-product-name{font-family:Georgia,"Songti SC",serif;font-size:13px;line-height:22px;color:#221e17}
.dc-tpl-tea .dc-price{color:#b0805a;font-family:Georgia,serif}
.dc-tpl-tea .dc-market{color:#b3aa9a}
.dc-tpl-tea .dc-buy,.dc-tpl-tea .dc-activity-buy{background:#b0805a}
.dc-tpl-tea .dc-store{background:transparent;border-radius:0;margin:0 16px 8px;padding:16px 0 0;border-top:1px solid #e2d9c8}
.dc-tpl-tea .dc-store-button{background:transparent;border:1px solid #b0805a;color:#b0805a;border-radius:0}
.dc-tpl-tea .dc-tabs,.dc-tpl-tea .dc-section-title,.dc-tpl-tea .dc-article{background:transparent;border-radius:0}
.dc-tpl-tea .dc-tabs .active small{background:#b0805a;border-radius:0}
.dc-tpl-tea .dc-more{color:#b0805a}
.dc-tpl-tea .dc-banner{overflow:hidden}
.dc-tpl-tea .dc-dots [role=button]{background:#b0805a66;width:4px;height:4px}
.dc-tpl-tea .dc-dots [role=button].active{background:#b0805a;width:14px;border-radius:2px}
.dc-tpl-tea .dc-store-goods{grid-template-columns:repeat(2,minmax(0,1fr));gap:14px}
.dc-tpl-tea .dc-store-head small{color:#8b8375}
.dc-tpl-tea .dc-empty,.dc-tpl-tea .dc-unsupported{color:#a39a8b}
</style>
