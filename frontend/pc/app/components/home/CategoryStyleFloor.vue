<template>
  <section class="cat-floor" :id="domId">
    <header class="cat-floor__head">
      <h2>
        <i class="cat-floor__bar" aria-hidden="true" />
        <span>{{ floorTitle }}</span>
      </h2>
      <nav class="cat-floor__tabs" aria-label="子分类">
        <button
          type="button"
          class="cat-floor__tab"
          :class="{ active: activeCatId === rootCatId }"
          @click="switchTab(rootCatId)"
        >
          {{ $t('热门商品') }}
        </button>
        <button
          v-for="tab in childTabs"
          :key="tab.id"
          type="button"
          class="cat-floor__tab"
          :class="{ active: activeCatId === tab.id }"
          @click="switchTab(tab.id)"
        >
          {{ tab.name }}
        </button>
        <NuxtLink class="cat-floor__more" :to="moreLink">{{ $t('更多') }} &gt;</NuxtLink>
      </nav>
    </header>

    <div class="cat-floor__body">
      <div v-if="loading" class="cat-floor__empty">{{ $t('加载中...') }}</div>
      <div v-else-if="!goods.length" class="cat-floor__empty">{{ $t('暂无商品') }}</div>
      <div v-else class="cat-floor__grid">
        <!-- 首列：主推商品 + 二级分类标签云（2×3） -->
        <div class="cat-floor__lead">
          <article
            v-if="featured"
            class="cat-floor__card cat-floor__card--lead"
            @click="goGoods(featured)"
          >
            <div class="thumb">
              <img v-if="featured.image" :src="featured.image" :alt="featured.name" loading="lazy" />
            </div>
            <div class="name">{{ featured.name }}</div>
            <div class="price">{{ formatPriceWithSymbolSafe(featured.price) }}</div>
          </article>
          <div v-if="tagList.length" class="cat-floor__tags">
            <button
              v-for="tag in tagList"
              :key="tag.id"
              type="button"
              class="cat-floor__tag"
              @click="goCategory(tag.id)"
            >
              {{ tag.name }}
            </button>
          </div>
        </div>

        <!-- 右侧固定 4 个商品卡 -->
        <article
          v-for="item in restGoods"
          :key="item.id"
          class="cat-floor__card"
          @click="goGoods(item)"
        >
          <div class="thumb">
            <img v-if="item.image" :src="item.image" :alt="item.name" loading="lazy" />
          </div>
          <div class="name">{{ item.name }}</div>
          <div class="price">{{ formatPriceWithSymbolSafe(item.price) }}</div>
          <div class="shop">{{ item.shopName || ' ' }}</div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'
import { getMallIndexCategory } from '~/services/base.service'
import { getFrontGoodsLists } from '~/services/goods.service'

type CatNode = { id: number; name: string; children?: CatNode[] }
type GoodsCard = {
  id: number
  name: string
  image: string
  price: number | string
  shopName?: string
}

const props = defineProps<{
  floor: Record<string, any>
  index: number
}>()

const loading = ref(false)
const goods = ref<GoodsCard[]>([])
const childTabs = ref<CatNode[]>([])
const activeCatId = ref(0)

const rootCatId = computed(() => Number(props.floor?.categoryId || props.floor?.catId || 0) || 0)
const floorTitle = computed(() => props.floor?.name || '分类楼层')
const domId = computed(() => `style-floor-${props.index}`)
const moreLink = computed(() =>
  rootCatId.value ? `/product/list?catId=${rootCatId.value}` : '/product/list'
)

const featured = computed(() => goods.value[0] || null)
/** 截图样式：右侧固定 4 卡 */
const restGoods = computed(() => goods.value.slice(1, 5))
const tagList = computed(() => childTabs.value.slice(0, 6))

const absUrl = (value: any) => {
  if (!value || typeof value !== 'string') return ''
  if (/^https?:\/\//i.test(value)) return value
  return value.startsWith('/') ? value : `/${value}`
}

const mapGoods = (list: any[]): GoodsCard[] =>
  (Array.isArray(list) ? list : []).map((item: any, idx: number) => ({
    id: Number(item.goodsId || item.id || item.commonId || idx),
    name: item.name || item.goodsName || item.commonName || '商品',
    image: absUrl(item.image || item.goodsImage || item.commonImage),
    price: item.minPrice ?? item.goodsPrice ?? item.commonPrice ?? item.price ?? item.priceText ?? 0,
    shopName: item.shopName || item.shop_name || ''
  }))

const loadChildren = async () => {
  const rootId = rootCatId.value
  if (!rootId) {
    childTabs.value = []
    return
  }
  try {
    const res = await getMallIndexCategory()
    const list = res?.data?.category_lists || res?.data?.lists || res?.data?.list || []
    const hit = (Array.isArray(list) ? list : []).find(
      (item: any) => Number(item.id ?? item.catId) === rootId
    )
    const sons = hit?.sons || hit?.children || []
    childTabs.value = (Array.isArray(sons) ? sons : [])
      .map((item: any) => ({
        id: Number(item.id ?? item.catId),
        name: item.name || item.catName || ''
      }))
      .filter((item: CatNode) => item.id && item.name)
  } catch {
    childTabs.value = []
  }
}

const loadGoods = async (catId: number) => {
  if (!catId) {
    goods.value = []
    return
  }
  loading.value = true
  try {
    const res = await getFrontGoodsLists({ page: 1, limit: 8, categoryId: catId })
    const list = res?.data?.lists || res?.data?.list || res?.data || []
    goods.value = mapGoods(list)
  } catch {
    goods.value = []
  } finally {
    loading.value = false
  }
}

const switchTab = async (catId: number) => {
  const id = Number(catId) || rootCatId.value
  activeCatId.value = id
  await loadGoods(id)
}

const goGoods = (item: GoodsCard) => {
  if (item?.id) navigateTo(`/product/${item.id}`)
}

const goCategory = (catId: number) => {
  if (catId) navigateTo(`/product/list?catId=${catId}`)
}

const bootstrap = async () => {
  activeCatId.value = rootCatId.value
  await Promise.all([loadChildren(), loadGoods(rootCatId.value)])
}

watch(rootCatId, () => {
  bootstrap()
})

onMounted(() => {
  bootstrap()
})
</script>

<style scoped lang="scss">
.cat-floor {
  margin: 0 0 20px;
  padding: 18px 22px 22px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  scroll-margin-top: 100px;
}

.cat-floor__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;

  h2 {
    margin: 0;
    display: inline-flex;
    align-items: center;
    gap: 10px;
    font-size: 22px;
    font-weight: 700;
    color: var(--color, #ef1d2d);
    line-height: 1.2;
  }
}

.cat-floor__bar {
  width: 4px;
  height: 20px;
  border-radius: 2px;
  background: var(--color, #ef1d2d);
  flex-shrink: 0;
}

.cat-floor__tabs {
  display: flex;
  align-items: center;
  gap: 22px;
  flex-wrap: wrap;
  min-width: 0;
}

.cat-floor__tab {
  border: 0;
  background: transparent;
  padding: 0 0 8px;
  margin: 0;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  line-height: 1.2;
  position: relative;

  &.active {
    color: #111;
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      left: 0;
      right: 0;
      bottom: 0;
      height: 3px;
      border-radius: 2px;
      background: var(--color, #ef1d2d);
    }
  }

  &:hover {
    color: var(--color, #ef1d2d);
  }
}

.cat-floor__more {
  color: #666;
  font-size: 13px;
  text-decoration: none;
  white-space: nowrap;
  padding-bottom: 8px;

  &:hover {
    color: var(--color, #ef1d2d);
  }
}

.cat-floor__grid {
  display: grid;
  grid-template-columns: minmax(180px, 1.05fr) repeat(4, minmax(0, 1fr));
  gap: 14px;
  align-items: stretch;
}

.cat-floor__lead {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.cat-floor__card {
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 12px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  cursor: pointer;
  box-sizing: border-box;
  transition: box-shadow 0.15s ease, border-color 0.15s ease;

  &:hover {
    border-color: #e8e8e8;
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
  }

  &--lead {
    border: 0;
    padding: 0;
    box-shadow: none;

    &:hover {
      box-shadow: none;
    }
  }

  .thumb {
    width: 100%;
    aspect-ratio: 1 / 1;
    border-radius: 4px;
    background: #f7f7f7;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
                    object-fit: cover;
      display: block;
    }
  }

  .name {
    margin-top: 10px;
    font-size: 13px;
    line-height: 1.45;
    color: #333;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 2.9em;
  }

  .price {
    margin-top: 8px;
    color: var(--color, #ef1d2d);
    font-size: 18px;
    font-weight: 700;
    line-height: 1.2;
  }

  .shop {
    margin-top: 6px;
    font-size: 12px;
    color: #999;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    min-height: 1.2em;
  }
}

.cat-floor__card--lead .name {
  margin-top: 8px;
}

.cat-floor__card--lead .price {
  margin-top: 6px;
  font-size: 16px;
}

.cat-floor__tags {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.cat-floor__tag {
  border: 0;
  border-radius: 4px;
  background: #f2f2f2;
  color: #555;
  font-size: 12px;
  line-height: 1.2;
  padding: 9px 6px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;

  &:hover {
    background: color-mix(in srgb, var(--color, #ef1d2d) 10%, #fff);
    color: var(--color, #ef1d2d);
  }
}

.cat-floor__empty {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
  background: #fafafa;
  border-radius: 8px;
}

@media (max-width: 1100px) {
  .cat-floor__grid {
    grid-template-columns: minmax(160px, 1fr) repeat(3, minmax(0, 1fr));

    .cat-floor__card:nth-child(5) {
      display: none;
    }
  }
}

@media (max-width: 768px) {
  .cat-floor__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .cat-floor__lead {
    grid-column: 1 / -1;
  }

  .cat-floor__head h2 {
    font-size: 18px;
  }

  .cat-floor__tabs {
    gap: 14px;
  }
}
</style>
