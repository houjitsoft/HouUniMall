<template>
  <div class="decorate-modules">
    <section
      v-for="(module, index) in visibleModules"
      :key="`${module.name}-${index}`"
      class="decorate-module"
    >
      <template v-if="module.name === 'middle-banner' && imageItems(module).length">
        <div class="image-rail">
          <a
            v-for="(item, itemIndex) in imageItems(module)"
            :key="itemIndex"
            href="#"
            @click.prevent="goLink(item.link)"
          >
            <img :src="item.image" :alt="item.name || ''" />
          </a>
        </div>
      </template>

      <template v-else-if="productItems(module).length">
        <div class="module-heading">
          <h2>{{ module.content?.title || module.title || module.name }}</h2>
          <NuxtLink :to="moreLinkOf(module)">
            更多
          </NuxtLink>
        </div>
        <!-- 单行卡片，水平滚动 -->
        <div class="product-list">
          <article
            v-for="(item, itemIndex) in productItems(module).slice(0, 20)"
            :key="itemIndex"
            class="product-card"
            @click="goProduct(item)"
          >
            <div class="thumb">
              <img v-if="productImage(item)" :src="productImage(item)" :alt="productName(item)" />
            </div>
            <div class="meta">
              <div class="product-name">{{ productName(item) }}</div>
              <div class="product-price">{{ productPrice(item) }}</div>
            </div>
          </article>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ modules: any[] }>()

/** 头条/导航已下线；cube 改到 Banner 与精选榜单之间单独挂载 */
const visibleModules = computed(() =>
  (props.modules || []).filter(
    (item: any) =>
      item &&
      item.name !== 'banner' &&
      item.name !== 'search' &&
      item.name !== 'page-meta' &&
      item.name !== 'notice' &&
      item.name !== 'nav' &&
      item.name !== 'cube' &&
      item.disabled !== 1 &&
      item.disabled !== '1'
  )
)

const resolvePath = (link: any) => {
  if (!link) return ''
  if (typeof link === 'string') return link
  if (link.path) return link.path
  if (link.url) return link.url
  if (link.query?.id && (link.type === 'goods' || link.type === 'product')) return `/product/${link.query.id}`
  if (link.query?.id && link.type === 'article') return `/information/${link.query.id}`
  if (link.query?.catId || link.query?.id) return `/product/list?catId=${link.query.catId || link.query.id}`
  return ''
}

const imageItems = (module: any) =>
  Array.isArray(module?.content?.data) ? module.content.data.filter((item: any) => item?.image) : []

const productItems = (module: any) => {
  const list = module?.content?.data || module?.content?.goodsLists?.list || module?.content?.storelists || []
  return Array.isArray(list)
    ? list.filter(
        (item: any) =>
          item && (item.goodsId || item.commonId || item.id || item.image || item.goodsImage || item.commonImage)
      )
    : []
}
const productImage = (item: any) => item.goodsImage || item.commonImage || item.image || item.images || ''
const productName = (item: any) => item.goodsName || item.commonName || item.name || '商品'
const productPrice = (item: any) => {
  const value = item.goodsPrice ?? item.commonPrice ?? item.price
  return value === undefined || value === null || value === '' ? '￥—' : `￥${Number(value).toFixed(2)}`
}
const moreLinkOf = (module: any) => {
  const name = String(module?.name || '')
  if (name === 'seckill') return '/activity/seckill'
  if (name === 'discount') return '/activity/discount'
  if (module?.content?.category_id) return `/product/list?catId=${module.content.category_id}`
  return '/product/list'
}
const goProduct = (item: any) => {
  const id = item.goodsId || item.commonId || item.id
  if (id) navigateTo(`/product/${id}`)
}

const goLink = (link: any) => {
  const path = resolvePath(link)
  if (!path) return
  if (/^https?:\/\//i.test(path)) window.open(path, '_blank', 'noopener,noreferrer')
  else navigateTo(path)
}
</script>

<style scoped lang="scss">
.decorate-modules {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 8px 24px;
}

.decorate-module {
  background: #fff;
  border-radius: 10px;
  padding: 18px;
}

.image-rail {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;

  img {
    width: 100%;
    height: 150px;
    object-fit: cover;
    border-radius: 8px;
    display: block;
  }
}

.module-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;

  h2 {
    margin: 0;
    font-size: 20px;
    color: var(--color, #ef1d2d);
  }

  a {
    color: #888;
    font-size: 13px;
  }
}

.product-list {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  gap: 12px;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
}

.product-card {
  flex: 0 0 160px;
  width: 160px;
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 10px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s ease;
  box-sizing: border-box;

  &:hover {
    background: #f3f5f7;
  }
}

.thumb {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

.meta {
  min-width: 0;
  margin-top: 10px;
}

.product-name {
  font-size: 14px;
  line-height: 1.4;
  color: #333;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 2.8em;
}

.product-price {
  color: var(--color, #ef1d2d);
  font-weight: 700;
  margin-top: 8px;
  font-size: 15px;
}

@media (max-width: 640px) {
  .product-card {
    flex-basis: 132px;
    width: 132px;
  }
}
</style>
