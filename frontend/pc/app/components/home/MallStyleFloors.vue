<template>
  <template v-for="(floor, index) in visibleFloors" :key="`${floor.name}-${index}`">
    <CategoryStyleFloor
      v-if="isCategoryFloor(floor)"
      :floor="floor"
      :index="index"
    />
    <section
      v-else
      class="style-floor"
      :id="`style-floor-${index}`"
    >
      <header class="style-floor__head">
        <div>
          <h2>{{ floor.name }}</h2>
          <p v-if="floor.subtitle">{{ floor.subtitle }}</p>
        </div>
        <NuxtLink :to="floor.categoryId ? `/product/list?catId=${floor.categoryId}` : '/product/list'">更多 <span>→</span></NuxtLink>
      </header>
      <div class="style-floor__body" :class="{ 'style-floor__body--no-banner': !floor.bannerImage }">
        <a
          v-if="floor.bannerImage"
          class="style-floor__banner"
          :href="floor.bannerLink || '#'"
          @click="!floor.bannerLink && $event.preventDefault()"
        >
          <img :src="floor.bannerImage" :alt="floor.name" />
        </a>
        <div
          v-else
          class="style-floor__banner style-floor__banner--placeholder"
          :style="{ background: floor.tone }"
        >
          <strong>{{ floor.name }}</strong>
          <span>{{ floor.subtitle || '精选好物' }}</span>
        </div>
        <!-- 单行卡片，水平滚动 -->
        <div class="style-floor__products">
          <article
            v-for="(item, itemIndex) in floor.goods.slice(0, 20)"
            :key="item.goodsId || itemIndex"
            @click="item.goodsId && navigateTo(`/product/${item.goodsId}`)"
          >
            <div class="thumb">
              <img v-if="item.goodsImage" :src="item.goodsImage" :alt="item.goodsName" />
            </div>
            <div class="meta">
              <div class="name">{{ item.goodsName || '商品' }}</div>
              <div class="price">{{ formatPriceWithSymbolSafe(item.goodsPrice) }}</div>
            </div>
          </article>
          <div v-if="!floor.goods.length" class="style-floor__empty">暂无商品，请稍后再看</div>
        </div>
      </div>
    </section>
  </template>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'
import CategoryStyleFloor from '~/components/home/CategoryStyleFloor.vue'

const props = defineProps<{ floors: any[]; homeData?: any }>()
const tones = [
  'color-mix(in srgb, var(--color) 72%, #1a1a1a)',
  'color-mix(in srgb, var(--color) 58%, #17324d)',
  'color-mix(in srgb, var(--color) 46%, #2b2b2b)',
  'color-mix(in srgb, var(--color) 64%, #0a3d3b)',
  'color-mix(in srgb, var(--color) 40%, #17324d)'
]
const imageUrl = (value: any) => {
  if (!value || typeof value !== 'string') return ''
  if (/^https?:\/\//i.test(value)) return value
  return value.startsWith('/') ? value : `/${value}`
}

const isCategoryFloor = (floor: any) => String(floor?.source || '') === 'category'
const isFloorVisible = (floor: any) => {
  const value = floor?.visible
  if (value === false || value === 0) return false
  if (typeof value === 'string' && ['0', 'false'].includes(value.trim().toLowerCase())) return false
  return true
}

const visibleFloors = computed(() => (props.floors || []).filter((floor: any) => floor && isFloorVisible(floor)).map((floor: any, index: number) => {
  if (isCategoryFloor(floor)) {
    return {
      ...floor,
      categoryId: Number(floor.categoryId || floor.catId || 0) || 0
    }
  }
  const source = floor.source || 'hotGoods'
  const rawGoods = Array.isArray(floor.goods) ? floor.goods : (props.homeData?.[source] || [])
  const goods = (Array.isArray(rawGoods) ? rawGoods : []).map((item: any) => ({
    goodsId: item.goodsId || item.commonId || item.id,
    goodsName: item.goodsName || item.commonName || item.name || '',
    goodsImage: imageUrl(item.goodsImage || item.commonImage || item.image),
    goodsPrice: item.goodsPrice ?? item.commonPrice ?? item.price
  }))
  return {
    ...floor,
    tone: tones[index % tones.length],
    bannerImage: imageUrl(floor.bannerImage),
    goods
  }
}))
</script>

<style scoped lang="scss">
.style-floor {
  margin: 0 0 16px;
  padding: 16px;
  background: var(--bg-card, #fff);
  border-radius: var(--border-radius, 4px);
  scroll-margin-top: 100px;
  border: 1px solid var(--border-light, #eee);
}
.style-floor__head { display:flex; justify-content:space-between; align-items:end; margin-bottom:12px; }
.style-floor__head h2 { margin:0; font-size:18px; font-weight:600; color:var(--text-primary, #202a33); }
.style-floor__head p { margin:4px 0 0; color:var(--text-secondary, #8f8f8f); font-size:12px; }
.style-floor__head a { color:var(--text-regular, #5d6872); text-decoration:none; font-size:13px; }
.style-floor__head a:hover { color: var(--color); }
.style-floor__body { display:grid; grid-template-columns:200px minmax(0, 1fr); gap:16px; align-items:stretch; }
.style-floor__banner {
  display:flex;
  flex-direction:column;
  justify-content:flex-end;
  min-height: 280px;
  border-radius: var(--border-radius, 4px);
  overflow:hidden;
  background:#fafafa;
  color:#fff;
  text-decoration:none;
  padding: 16px;
  box-sizing: border-box;
}
.style-floor__banner img { width:100%; height:100%; object-fit:cover; display:block; }
.style-floor__banner--placeholder strong { font-size: 18px; line-height: 1.3; }
.style-floor__banner--placeholder span { margin-top: 8px; opacity: 0.9; font-size: 13px; }
.style-floor__products {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  align-items: stretch;
  gap: 12px;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 4px;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
}
.style-floor__products article {
  flex: 0 0 160px;
  width: 160px;
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 8px;
  background: #fff;
  border: 1px solid var(--border-light, #eee);
  border-radius: var(--border-radius, 4px);
  cursor: pointer;
  transition: border-color 0.15s ease;
  box-sizing: border-box;
}
.style-floor__products article:hover { border-color: var(--color); }
.style-floor__products .thumb {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: var(--border-radius, 4px);
  background: #f7f7f7;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.style-floor__products img { width: 100%; height: 100%; object-fit: cover; display: block; }
.style-floor__products .meta { min-width: 0; margin-top: 8px; }
.style-floor__empty {
  flex: 0 0 100%;
  padding: 32px 12px;
  text-align: center;
  color: var(--text-secondary, #8f8f8f);
  font-size: 14px;
}
.name {
  font-size: 13px;
  line-height: 1.4;
  color: var(--text-primary, #202a33);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8em;
}
.price { margin-top: 8px; color: var(--color-price, var(--color)); font-weight: 700; font-size: 16px; font-variant-numeric: tabular-nums; }
@media (max-width:768px) {
  .style-floor__body{grid-template-columns:1fr}
  .style-floor__banner{min-height:140px}
  .style-floor__products article { flex-basis: 132px; width: 132px; }
}
</style>
