<template>
  <div class="home-container" :class="{ 'home-tea': mallStyle.pc_template_code === 'tea' }" :style="homeStyleVars">
    <header v-if="mallStyle.pc_template_code === 'tea'" class="tea-brand-heading">
      <small>奢侈品简单风格 · LUXURY</small><h1>一盏茶，慢下来</h1><p>以茶会友，品味日常</p>
    </header>
    <BannerBox />

    <div class="home-category-promo">
      <HomeCategoryPromoStrip :items="categoryPromoCards" />
    </div>

    <DiscountBox />

    <!-- 促销货架置顶：限时秒杀 / 精选好物等，优先出现在首屏 -->
    <div v-if="promoDecorateModules.length" class="home-promo-shelves">
      <HomeDecorateModules :modules="promoDecorateModules" />
    </div>

    <div class="home-floor-block">
      <Loading v-if="floorLoading" text="正在加载精选楼层..." />
      <template v-else>
        <div v-if="showClassicRank" id="floor-0">
          <SelectedRankBox :data-info="rankFloorData" />
        </div>
        <div v-if="showClassicRecommend" id="floor-1">
          <SelectedRecommendBox :data-info="recommendFloorData" />
        </div>
      </template>
    </div>

    <div v-if="visibleStyleFloors.length && mallStyle.pc_layout !== 'intl'" id="pc-style-floors" class="pc-style-floors">
      <MallStyleFloors :floors="visibleStyleFloors" :home-data="homeAggregate" />
    </div>

    <div v-if="legacyFloorModules.length" class="legacy-floor-block">
      <div v-for="(floor, index) in legacyFloorModules" :key="floor.key" :id="`floor-${index + 2}`" class="legacy-floor">
        <CategoryBox v-if="floor.kind === 'single'" :data-info="floor.data" />
        <LeftCarouselRightList v-else :data-info="floor.data" />
      </div>
    </div>

    <div v-if="restDecorateModules.length" class="floor-decoration">
      <Loading v-if="decorateLoading" text="正在加载首页内容..." />
      <HomeDecorateModules v-else :modules="restDecorateModules" />
    </div>

    <FloorNavigation :floorData="navFloorData" @scrollToFloor="scrollToFloor" />
    <HomePlatformRedpacket />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import HomePlatformRedpacket from '~/components/home/HomePlatformRedpacket.vue'
import HomeDecorateModules from '~/components/home/HomeDecorateModules.vue'
import HomeCategoryPromoStrip from '~/components/home/HomeCategoryPromoStrip.vue'
import SelectedRankBox from '~/components/home/SelectedRankBox.vue'
import SelectedRecommendBox from '~/components/home/SelectedRecommendBox.vue'
import FloorNavigation from '~/components/home/FloorNavigation.vue'
import { getMallIndexConfig, getMallIndexData, getMallIndexFloors, flattenMallIndexModules, getMallHomeAggregate } from '~/services/base.service'
import { getShopList } from '~/services/goods.service'
import { getUserInfo } from '~/services/userms.service'
import { runAgent } from '~/services/agent.service'
import { useUserStore } from '~/stores/modules/user'
import { getToken, deleteToken } from '~/utils/auth'
import type { FloorItem } from '~/types/home'
import CategoryBox from '~/components/home/CategoryBox.vue'
import LeftCarouselRightList from '~/components/home/LeftCarouselRightList.vue'
import MallStyleFloors from '~/components/home/MallStyleFloors.vue'
import { buildCategoryPromoCards } from '~/utils/categoryPromo'
import { resolveError, shouldRedirectLogin } from '~/utils/errorCode/index.js'

const userStore = useUserStore()
const decorateLoading = ref(false)
const floorLoading = ref(false)
const floorModules = ref<any[]>([])
const legacyFloors = ref<any[]>([])
const mallStyle = ref<any>({})
const homeAggregate = ref<any>({})
/** 促销活动货架兜底（非分类楼层） */
const DEFAULT_PC_FLOORS = [
  { name: '热门推荐', subtitle: '大家都在买', source: 'hotGoods', visible: true, bannerImage: '', bannerLink: '' },
  { name: '新品上架', subtitle: '潮流好物抢先看', source: 'newGoods', visible: true, bannerImage: '', bannerLink: '' },
  { name: '猜你喜欢', subtitle: '为你精选好物', source: 'guessGoods', visible: true, bannerImage: '', bannerLink: '' }
]
const styleFloors = computed(() => {
  // 仅当字段缺失时用默认；空数组表示后台故意清空，不再强行回填
  if (!Object.prototype.hasOwnProperty.call(mallStyle.value || {}, 'pc_floors')) {
    return DEFAULT_PC_FLOORS
  }
  return Array.isArray(mallStyle.value.pc_floors) ? mallStyle.value.pc_floors : DEFAULT_PC_FLOORS
})
const isFloorVisible = (floor: any) => {
  const value = floor?.visible
  if (value === false || value === 0) return false
  if (typeof value === 'string' && ['0', 'false'].includes(value.trim().toLowerCase())) return false
  return true
}
/** 分类楼层在前（截图样式），促销货架在后，避免被三层促销顶出首屏 */
const visibleStyleFloors = computed(() => {
  const list = styleFloors.value.filter(
    (floor: any) => floor && isFloorVisible(floor)
  )
  const category = list.filter((floor: any) => String(floor?.source || '') === 'category')
  const promo = list.filter((floor: any) => String(floor?.source || '') !== 'category')
  return [...category, ...promo]
})
const showClassicRank = computed(() => mallStyle.value?.pc_show_rank !== false && mallStyle.value?.pc_show_rank !== 0 && mallStyle.value?.pc_show_rank !== '0')
const showClassicRecommend = computed(() => mallStyle.value?.pc_show_recommend !== false && mallStyle.value?.pc_show_recommend !== 0 && mallStyle.value?.pc_show_recommend !== '0')
const rankFloorData = ref<any[]>([])
const recommendFloorData = ref<any[]>([])

/** 四栏品类促销：挂在 Banner 下方、精选榜单上方 */
const categoryPromoCards = computed(() => {
  const cube = floorModules.value.find((item: any) => item?.name === 'cube')
  return buildCategoryPromoCards(cube)
})

/** 促销活动货架：应靠前展示，不要沉到页面底部 */
const PROMO_MODULE_NAMES = new Set(['seckill', 'goodslists', 'choice', 'discount'])
const isPromoDecorateModule = (item: any) => {
  if (!item || item.disabled === 1 || item.disabled === '1') return false
  const name = String(item.name || '')
  if (PROMO_MODULE_NAMES.has(name)) return true
  const title = String(item.content?.title || item.title || '')
  return /秒杀|折扣|精选好物|好物推荐|限时/.test(title)
}
const decorateVisibleBase = (item: any) =>
  item &&
  item.name !== 'banner' &&
  item.name !== 'search' &&
  item.name !== 'page-meta' &&
  item.name !== 'notice' &&
  item.name !== 'nav' &&
  item.name !== 'cube' &&
  item.disabled !== 1 &&
  item.disabled !== '1'

const promoDecorateModules = computed(() => {
  const order: Record<string, number> = { seckill: 0, discount: 1, goodslists: 2, choice: 3 }
  return (floorModules.value || [])
    .filter((item: any) => decorateVisibleBase(item) && isPromoDecorateModule(item))
    .slice()
    .sort((a: any, b: any) => (order[String(a.name)] ?? 50) - (order[String(b.name)] ?? 50))
})
const restDecorateModules = computed(() =>
  (floorModules.value || []).filter((item: any) => decorateVisibleBase(item) && !isPromoDecorateModule(item))
)

const navFloorData = computed<FloorItem[]>(() => [
  ...(showClassicRank.value ? [{
    pageType: 2,
    data: [{ pageTitle: '精选榜单', pageStart: 1, floorStatus: 1 }]
  } as FloorItem] : []),
  ...(showClassicRecommend.value ? [{
    pageType: 3,
    data: [{ pageTitle: '精选推荐', pageStart: 1, floorStatus: 1 }]
  } as FloorItem] : []),
  ...(mallStyle.value.pc_layout === 'intl' ? [] : visibleStyleFloors.value.map((floor: any, index: number) => ({
    pageType: 9,
    navId: `style-floor-${index}`,
    data: [{ name: floor.name || `PC楼层${index + 1}`, visible: true }]
  }))) as FloorItem[],
  ...legacyFloorModules.value.map((floor: any, index: number) => ({
    pageType: floor.kind === 'multi' ? 6 : 5,
    navId: `floor-${index + 2}`,
    data: floor.data
  })) as FloorItem[]
])

const homeStyleVars = computed(() => {
  const style = mallStyle.value || {}
  const container = String(style.pc_container || '1280')
  const radiusMap: Record<string, string> = { sm: '6px', md: '10px', lg: '16px' }
  return {
    '--home-max-width': `${/^\d+$/.test(container) ? container : '1280'}px`,
    '--home-radius': radiusMap[String(style.pc_radius || 'md')] || '10px',
    ...(style.pc_template_code === 'tea' ? {
      backgroundColor: style.pc_bg_color || '#f4f1ea',
      '--tea-green': '#1d2420', '--tea-gold': '#b0805a'
    } : {})
  }
})

const normalizeFloorList = (raw: any): any[] => {
  const value = raw?.data ?? raw
  const list = Array.isArray(value)
    ? value
    : Object.values(value || {}).flatMap((item: any) => Array.isArray(item) ? item : (item?.data || []))
  return list.filter((item: any) => item && (item.floorStatus === undefined || Number(item.floorStatus) === 1))
    .sort((a: any, b: any) => Number(a.floorAdpageOrder || 0) - Number(b.floorAdpageOrder || 0))
}

const normalizeFloorRecord = (item: any) => {
  const parseFloorName = (value: any) => {
    if (value && typeof value === 'object') return value
    if (typeof value === 'string' && value.trim()) {
      try {
        const parsed = JSON.parse(value)
        if (parsed && typeof parsed === 'object') return parsed
      } catch {
        return { catName: value.trim() }
      }
    }
    return {}
  }
  const name = parseFloorName(item.floorAdpageNameNew || item.floorName || item.floorAdpageName)
  const abs = (url: any) => {
    if (!url || typeof url !== 'string') return ''
    if (/^https?:\/\//i.test(url)) return url
    return url.startsWith('/') ? url : `/${url}`
  }
  const goods = (list: any[]) => (Array.isArray(list) ? list : []).filter(Boolean).map((g: any) => ({
    ...g,
    goodsId: g.goodsId || g.commonId || g.id,
    goodsName: g.goodsName || g.commonName || g.name || '',
    goodsImage: abs(g.goodsImage || g.commonImage || g.image),
    goodsPrice: g.goodsPrice ?? g.commonPrice ?? g.price
  }))
  return {
    ...item,
    floorAdpageNameNew: {
      catId: Number(name.catId ?? name.cat_id ?? name.categoryId ?? 0),
      catName: name.catName || name.name || item.floorTitle || '商品分类'
    },
    adpageBannerList: goods(item.adpageBannerList),
    rightRecGoodsList: goods(item.rightRecGoodsList),
    leftAdpageNavList: item.leftAdpageNavList || [],
    rightAdpageNavList: item.rightAdpageNavList || [],
    rightRec: item.rightRec || {}
  }
}

const legacyFloorModules = computed(() => {
  const grouped = new Map<string, any[]>()
  legacyFloors.value.forEach((raw: any) => {
    const item = normalizeFloorRecord(raw)
    const pageType = Number(item.pageType || item.floorType || 5)
    const key = pageType === 6 ? 'multi' : `single-${item.floorAdpageId || item.floorAdpageOrder || grouped.size}`
    if (!grouped.has(key)) grouped.set(key, [])
    grouped.get(key)!.push(item)
  })
  return Array.from(grouped.entries()).map(([key, data]) => ({ key, kind: key.startsWith('single') ? 'single' : 'multi', data }))
})

const scrollToFloor = (floorId: string) => {
  const el = document.getElementById(floorId)
  if (!el) return
  window.scrollTo({ top: el.getBoundingClientRect().top + window.pageYOffset - 100, behavior: 'smooth' })
}

const checkTokenValid = async () => {
  const token = userStore.token || getToken()
  if (!token) return
  try {
    const res = await getUserInfo(token)
    // 只有明确的认证失败才清除登录态。用户资料接口可能因店铺资料为空、
    // 下游超时或临时 5000 异常返回失败，这些情况不代表 token 失效。
    // 过去是裸码数组 [10002, 10086, 11002]，其中 11002 是跨枚举冲突码（骑手端为「正在审核」），
    // 拿它清登录态会误伤；现在只看错误码语义：category === 'AUTH'。
    if (shouldRedirectLogin(resolveError(Number(res.code), { backendMessage: res.message }))) {
      userStore.clearUserInfo()
      deleteToken()
    }
  } catch {
    /* 首页无需因用户校验失败而阻塞 */
  }
}

const absImage = (url = '') => {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return url.startsWith('/') ? url : `/${url}`
}

const toGoods = (list: any[] = []) =>
  (Array.isArray(list) ? list : []).map((item: any) => ({
    goodsId: item.goodsId || item.id || item.commonId,
    commonId: item.commonId || item.goodsId || item.id,
    commonName: item.goodsName || item.commonName || item.name || '',
    commonImage: absImage(item.goodsImage || item.commonImage || item.image || ''),
    commonPrice: item.goodsPrice ?? item.commonPrice ?? item.price ?? '',
    goodsPrice: item.goodsPrice ?? item.commonPrice ?? item.price ?? '',
    commonSalenum: item.commonSalenum ?? item.saleNum ?? item.sale_num ?? 0,
    commonCollect: item.commonCollect ?? item.collectNum ?? item.collect_num ?? 0,
    preferentialPrice: item.preferentialPrice
  }))

const toShops = (list: any[] = []) =>
  (Array.isArray(list) ? list : []).map((item: any, index: number) => ({
    shopId: item.id || item.shopId || item.shop_id || index + 1,
    shopName: item.name || item.shopName || item.shop_name || `店铺${index + 1}`,
    shopCollect: item.visit_num || item.visitNum || item.shopCollect || item.fans || 0,
    commonImage: absImage(item.logo || item.commonImage || item.shopLogo || '')
  }))

const uniqById = (list: any[]) => {
  const seen = new Set<string>()
  const out: any[] = []
  for (const item of list) {
    const key = String(item.goodsId || item.commonId || item.commonImage || item.commonName || '')
    if (!key || seen.has(key)) continue
    seen.add(key)
    out.push(item)
  }
  return out
}

/** 装修楼层缺商品时，用首页聚合推荐补齐，避免空白模块 */
const enrichFloorModules = (modules: any[], homeData: any) => {
  if (!Array.isArray(modules) || !homeData) return modules
  const hot = toGoods(homeData.hotGoods || [])
  const neu = toGoods(homeData.newGoods || [])
  const guess = toGoods(homeData.guessGoods || hot)
  return modules.map((mod: any) => {
    if (!mod || typeof mod !== 'object') return mod
    const content = { ...(mod.content || {}) }
    const name = mod.name
    const hasGoods = (list: any) =>
      Array.isArray(list) && list.some((row: any) => row && (row.goodsId || row.id || row.image || row.goodsImage || row.commonImage))
    if (name === 'seckill' && !hasGoods(content.data) && !hasGoods(content.goodsLists?.list)) {
      content.data = hot.slice(0, 6)
      content.title = content.title || '限时秒杀'
    }
    if (name === 'goodslists' && !hasGoods(content.goodsLists?.list) && !hasGoods(content.data)) {
      content.goodsLists = { list: neu.slice(0, 10) }
      content.title = content.title || '精选好物'
    }
    if (name === 'choice' && Array.isArray(content.data)) {
      content.data = content.data.map((tab: any, idx: number) => {
        const tabList = tab?.goodsLists?.list
        if (hasGoods(tabList)) return tab
        return {
          ...tab,
          title: tab?.title || (idx === 0 ? '口碑推荐' : '好货专区'),
          goodsLists: { list: (idx === 0 ? hot : guess).slice(0, 10) }
        }
      })
    }
    if (name === 'cube' && Array.isArray(content.data)) {
      const promoDefaults = [
        {
          name: '电脑办公',
          slogan: '搜罗爆款好货',
          image: '/uploads/mall-demo/promo-office.png',
          background: 'linear-gradient(135deg, #f6c93d 0%, #e9a61a 100%)',
          link: { path: '/product/list?kws=电脑办公' }
        },
        {
          name: '捕捉光影瞬间',
          slogan: '好物天天低价',
          image: '/uploads/mall-demo/promo-camera.png',
          background: 'linear-gradient(135deg, #d9b48a 0%, #c79a6b 100%)',
          link: { path: '/product/list?kws=数码相机' }
        },
        {
          name: '牛奶乳品',
          slogan: '冬季福利购',
          image: '/uploads/mall-demo/promo-milk.png',
          background: 'linear-gradient(135deg, #e8a0a8 0%, #d57c8a 100%)',
          link: { path: '/product/list?kws=牛奶乳品' }
        },
        {
          name: '妆出彩',
          slogan: '搜罗全球美妆',
          image: '/uploads/mall-demo/promo-lipstick.png',
          background: 'linear-gradient(135deg, #4eb6b0 0%, #2f9a94 100%)',
          link: { path: '/product/list?kws=美妆' }
        }
      ]
      content.data = promoDefaults.map((fallback, idx) => {
        const item = content.data[idx] || {}
        const weak =
          !item?.image ||
          String(item.image).includes('/uploads/mall/') ||
          String(item.image).includes('banner-0') ||
          String(item.image).includes('goods-01.svg') ||
          String(item.image).endsWith('.html')
        return {
          ...fallback,
          ...item,
          name: item?.name || item?.title || fallback.name,
          slogan: item?.slogan || item?.desc || fallback.slogan,
          image: weak ? fallback.image : item.image,
          background: item?.background || item?.bgColor || fallback.background,
          link: item?.link?.path || item?.link?.url ? item.link : fallback.link
        }
      })
    }
    if (name === 'nav' && Array.isArray(content.data)) {
      const iconMap: Record<string, string> = {
        资讯中心: '/uploads/mall-demo/nav-news.svg',
        个人设置: '/uploads/mall-demo/nav-settings.svg',
        我的收藏: '/uploads/mall-demo/nav-fav.svg',
        关于我们: '/uploads/mall-demo/nav-about.svg',
        联系客服: '/uploads/mall-demo/nav-service.svg'
      }
      content.data = content.data.map((item: any) => {
        const key = item?.name || item?.link?.name || ''
        const broken = !item?.image || String(item.image).includes('/uploads/mall/') || String(item.image).includes('17799')
        return broken && iconMap[key] ? { ...item, image: iconMap[key] } : item
      })
    }
    if (name === 'notice') {
      const lists = content.lists || []
      const valid = Array.isArray(lists) && lists.some((row: any) => row?.title || row?.informationTitle)
      if (!valid) {
        content.lists = (homeData.notices || []).map((n: any) => ({
          title: n.title,
          informationTitle: n.title,
          informationId: n.id,
          link: { id: n.id, type: 'article', query: { id: n.id }, path: `/information/${n.id}` }
        }))
      }
    }
    return { ...mod, content }
  })
}

const buildClassicFloors = (homeData: any, shops: any[]) => {
  const hot = uniqById(toGoods(homeData?.hotGoods || []))
  const neu = uniqById(toGoods(homeData?.newGoods || []))
  const popular = uniqById(toGoods(homeData?.popularGoods || []))
  const guess = uniqById(toGoods(homeData?.guessGoods || []))
  const pool = uniqById([...neu, ...hot, ...popular, ...guess])

  const findList = (hot.length ? hot : pool).slice(0, 3)
  const rankSource = popular.length ? popular : pool
  const rankList = rankSource.slice(0, 3)
  // 榜单尽量与「发现好货」错开；不足时回退到前 3
  const rankAlt = rankSource.slice(3, 6)
  const rankGoods = rankAlt.length >= 2 ? rankAlt : rankList

  rankFloorData.value = [
    { pageTitle: '新品首发', goods: (neu.length ? neu : pool).slice(0, 3) },
    { pageTitle: '发现好货', goods: findList },
    { pageTitle: '精选好店', shops: shops.slice(0, 3) },
    { pageTitle: '推荐榜单', goods: rankGoods }
  ]

  recommendFloorData.value = [
    {
      pageId: 1,
      pageTitle: '天天特价',
      pageTwoTitle: '限时低价好物',
      goods: (hot.length ? hot : pool).slice(0, 6)
    },
    {
      pageId: 2,
      pageTitle: '精选商品',
      pageTwoTitle: '口碑精选推荐',
      goods: (guess.length ? guess : pool).slice(0, 6)
    }
  ]
}

/**
 * 首页个性化推荐（智能体 home_personal）
 *
 * 只在后台启用该智能体并绑定模型后生效：把候选商品交给智能体，
 * 用返回的推荐语替换「精选商品」副标题。未启用 / 调用失败一律保持原样。
 * 异步执行且不阻塞首屏——楼层先按默认规则渲染，智能体结果到达后再更新。
 */
const personalizeRecommendFloor = async () => {
    try {
        const floor = recommendFloorData.value.find(item => item?.pageId === 2)
        const goods = floor?.goods || []
        if (!goods.length) return

        const titles = goods
            .map(item => item?.goods_name || item?.goodsName || item?.name || '')
            .filter(Boolean)
            .slice(0, 6)
            .join('、')
        if (!titles) return

        const result = await runAgent<string>('home_personal', `候选商品：${titles}`, {
            bizId: 'home',
            subjectType: 'home_recommend',
            // 兜底：智能体不可用时保持原副标题
            fallback: () => ''
        })
        if (!result.byAgent || !result.data) return

        const text = String(result.data).trim()
        // 只接受简短单行文案，避免模型长篇输出撑坏楼层标题
        if (!text || text.length > 20 || text.includes('\n')) return
        floor.pageTwoTitle = text
    } catch {
        /* 智能体异常不影响首页楼层展示 */
    }
}

const getClassicFloorData = async () => {
  floorLoading.value = true
  try {
    const [homeRes, shopRes] = await Promise.all([
      getMallHomeAggregate().catch(() => null),
      getShopList({ page: 1, limit: 6 }).catch(() => null)
    ])
    const homeData = (homeRes as any)?.data || {}
    homeAggregate.value = homeData
    const shopLists = (shopRes as any)?.data?.lists || (shopRes as any)?.data?.list || []
    buildClassicFloors(homeData, toShops(shopLists))
    // 智能体个性化：异步应用，失败不影响楼层展示
    void personalizeRecommendFloor()
  } catch (error) {
    console.error('获取精选楼层失败', error)
    rankFloorData.value = []
    recommendFloorData.value = []
  } finally {
    floorLoading.value = false
  }
}

const getDecorateFloorData = async () => {
  decorateLoading.value = true
  try {
    const [res, homeRes] = await Promise.all([
      getMallIndexData(),
      getMallHomeAggregate().catch(() => null)
    ])
    if (res?.code === 1) {
      const modules = flattenMallIndexModules(res)
      floorModules.value = enrichFloorModules(modules, (homeRes as any)?.data)
    }
  } catch (error) {
    console.error('获取首页装修数据失败', error)
    floorModules.value = []
  } finally {
    decorateLoading.value = false
  }
}

const getMallStyleAndLegacyFloors = async () => {
  const [styleRes, floorRes] = await Promise.all([
    getMallIndexConfig().catch(() => null),
    getMallIndexFloors().catch(() => null)
  ])
  const style = (styleRes as any)?.data?.mall_style
  if (style && typeof style === 'object') {
    mallStyle.value = style
  }
  legacyFloors.value = normalizeFloorList(floorRes)
}

/** SSR/首屏即拉取楼层配置，避免分类楼层等客户端 onMounted 才出现 */
const { data: bootHomeStyle } = await useAsyncData('home-mall-style-floors', async () => {
  const [styleRes, floorRes] = await Promise.all([
    getMallIndexConfig().catch(() => null),
    getMallIndexFloors().catch(() => null)
  ])
  return {
    mallStyle: (styleRes as any)?.data?.mall_style || {},
    legacyFloors: normalizeFloorList(floorRes)
  }
})
if (bootHomeStyle.value?.mallStyle && Object.keys(bootHomeStyle.value.mallStyle).length) {
  mallStyle.value = bootHomeStyle.value.mallStyle
}
if (bootHomeStyle.value?.legacyFloors) {
  legacyFloors.value = bootHomeStyle.value.legacyFloors
}

onMounted(() => {
  checkTokenValid()
  // 客户端再刷新一次，避免 SSR 缓存过旧
  getMallStyleAndLegacyFloors()
  getClassicFloorData()
  getDecorateFloorData()
})
</script>

<style scoped lang="scss">
.tea-brand-heading { max-width: var(--home-max-width); margin: 0 auto; padding: 48px 24px 32px; color: #221e17; }
.tea-brand-heading small { color: var(--tea-gold); letter-spacing: 6px; }
.tea-brand-heading h1 { margin: 16px 0; font-family: serif; font-size: 38px; font-weight: 400; }
.tea-brand-heading p { color: #504f49; }
.home-tea { padding-bottom: 48px; }
.home-tea :deep(.banner-box-bg) { background-color: transparent; }
.home-tea :deep(.banner-content) { max-width: var(--home-max-width); }
.home-tea :deep(.main-stage) { flex: 1; min-width: 0; }
.home-tea :deep(.stage-top) { min-height: 360px; }
.home-tea .home-category-promo { margin-top: 32px; margin-bottom: 32px; }
.home-tea :deep(.style-floor__head h2) { color: #221e17; font-family: serif; }
.home-tea :deep(.style-floor__head p) { color: var(--tea-gold); }
.home-tea :deep(.style-floor__products) { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 24px; }
.home-tea :deep(.style-floor__products > article) { width: auto; min-width: 0; }
.home-tea :deep(.style-floor) { margin-bottom: 40px; }

.home-container {
  --home-page: #f5f6f8;
  --home-surface: #ffffff;
  --home-surface-soft: #f7f9fb;
  --home-text: #202a33;
  --home-text-secondary: #66727d;
  --home-text-muted: #8b96a0;
  --home-border: #e2e8ed;
  --home-border-strong: #d5dee5;
  --home-card-radius: 12px;
  --home-card-shadow: 0 2px 10px rgba(23, 50, 77, 0.05);
  width: 100%;
  max-width: var(--home-max-width, 1280px);
  margin: 0 auto;
  padding: 0;
  position: relative;
  box-sizing: border-box;
}

.home-category-promo {
  margin: 14px 0 18px;
}

.home-promo-shelves {
  margin: 12px 0 8px;
}

.pc-style-floors {
  margin: 0 0 18px;
}

.home-floor-block,
.floor-decoration,
.legacy-floor-block {
  min-height: 80px;
}
.legacy-floor { scroll-margin-top: 100px; }
</style>
