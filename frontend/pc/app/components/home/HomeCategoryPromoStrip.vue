<template>
  <section v-if="cards.length" class="category-promo-strip" aria-label="品类推荐">
    <button
      v-for="(card, index) in cards"
      :key="card.key || index"
      type="button"
      class="promo-card"
      :style="{ background: card.background }"
      @click="go(card)"
    >
      <span class="promo-card__glow" aria-hidden="true" />
      <span class="promo-card__body">
        <span class="promo-card__eyebrow">{{ $t(card.eyebrow) }}</span>
        <span class="promo-card__slogan">{{ $t(card.slogan) }}</span>
        <span class="promo-card__cta">{{ $t(card.cta || '立即进入') }}</span>
      </span>
      <span class="promo-card__media">
        <img v-if="card.image" :src="card.image" :alt="card.slogan || card.eyebrow || ''" loading="lazy" />
      </span>
    </button>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CategoryPromoCard } from '~/utils/categoryPromo'

const props = defineProps<{
  items?: CategoryPromoCard[]
}>()

const cards = computed(() =>
  (Array.isArray(props.items) ? props.items : []).filter((item) => item?.eyebrow || item?.slogan || item?.image)
)

const go = (card: CategoryPromoCard) => {
  const path = card.path || card.link?.path || card.link?.url
  if (!path) {
    navigateTo('/product/list')
    return
  }
  if (/^https?:\/\//i.test(path)) {
    window.open(path, '_blank', 'noopener,noreferrer')
    return
  }
  navigateTo(path)
}
</script>

<style scoped lang="scss">
.category-promo-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  overflow: visible;
  min-height: 152px;
}

.promo-card {
  position: relative;
  min-width: 0;
  min-height: 152px;
  border: 1px solid rgba(23, 50, 77, 0.06);
  border-radius: 12px;
  padding: 16px 18px 14px;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(88px, 0.95fr);
  align-items: stretch;
  gap: 8px;
  cursor: pointer;
  text-align: left;
  color: #fff;
  overflow: hidden;
  box-shadow: 0 5px 16px rgba(23, 50, 77, 0.06);
}

.promo-card__glow {
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(
      -32deg,
      rgba(255, 255, 255, 0.12) 0 10px,
      rgba(255, 255, 255, 0) 10px 22px
    );
  pointer-events: none;
  opacity: 0.22;
}

.promo-card__body {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}

.promo-card__eyebrow {
  font-size: 12px;
  line-height: 1.3;
  opacity: 0.95;
}

.promo-card__slogan {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.25;
  letter-spacing: 0.5px;
}

.promo-card__cta {
  margin-top: auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 12px;
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 2px;
  font-size: 12px;
  line-height: 1;
  background: transparent;
}

.promo-card__media {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  min-width: 0;

  img {
    width: 100%;
    max-width: 126px;
    max-height: 132px;
    object-fit: contain;
    filter: drop-shadow(0 8px 14px rgba(0, 0, 0, 0.18));
    transition: transform 0.25s ease;
  }
}

.promo-card:hover .promo-card__media img {
  transform: translateY(-3px) scale(1.03);
}

@media (max-width: 1100px) {
  .category-promo-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .category-promo-strip {
    grid-template-columns: 1fr;
  }

  .promo-card__slogan {
    font-size: 18px;
  }
}
</style>
