<template>
  <div class="live-preview">
    <article class="phone">
      <p class="phone-label">首页</p>
      <div class="phone-body" :style="shell">
        <div class="phone-nav" :style="{ background: navBg, color: navText }">
          <span class="dot" />
          <span class="nav-title">{{ name }}</span>
          <span class="cart">2</span>
        </div>
        <div class="phone-banner" :style="{ background: grad }" />
        <div class="phone-grid">
          <span v-for="i in 4" :key="i" class="tile">
            <i :style="{ background: i % 2 ? color : dark }" />
            <b :style="{ color }">¥{{ 29 + i * 10 }}</b>
          </span>
        </div>
        <div class="phone-tab" :style="{ borderTopColor: color }">
          <em :style="{ color }">首页</em>
          <span>分类</span>
          <span>购物车</span>
          <span>我的</span>
        </div>
      </div>
    </article>

    <article class="phone">
      <p class="phone-label">商品详情</p>
      <div class="phone-body" :style="shell">
        <div class="phone-nav" :style="{ background: navBg, color: navText }">
          <span class="back">‹</span>
          <span class="nav-title">商品详情</span>
          <span />
        </div>
        <div class="detail-hero" :style="{ background: light }" />
        <div class="detail-info">
          <span class="sku">棉麻衬衫 · 两色可选</span>
          <strong class="ticket" :style="{ background: color }">¥128.00</strong>
          <button class="buy" :style="{ background: color }">立即购买</button>
        </div>
      </div>
    </article>

    <article class="phone">
      <p class="phone-label">订单详情</p>
      <div class="phone-body" :style="shell">
        <div class="phone-nav" :style="{ background: navBg, color: navText }">
          <span class="back">‹</span>
          <span class="nav-title">订单详情</span>
          <span />
        </div>
        <div class="order-status" :style="{ color }">待发货</div>
        <div v-for="n in 2" :key="n" class="order-row">
          <i :style="{ background: n === 1 ? color : dark }" />
          <div>
            <b>商品 {{ n }}</b>
            <em>¥{{ 68 * n }}.00</em>
          </div>
        </div>
        <div class="order-total">合计 <b :style="{ color }">¥204.00</b></div>
      </div>
    </article>

    <article class="phone">
      <p class="phone-label">个人中心</p>
      <div class="phone-body" :style="shell">
        <div class="me-head" :style="{ background: grad }">
          <span class="avatar" />
          <div>
            <b>买家账号</b>
            <em>订单 · 收藏 · 钱包</em>
          </div>
        </div>
        <ul class="me-list">
          <li v-for="item in ['我的订单', '收货地址', '优惠券', '账号设置']" :key="item">
            {{ item }}
            <span>›</span>
          </li>
        </ul>
      </div>
    </article>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  color: { type: String, default: '#E0453C' },
  dark: { type: String, default: '#C7362E' },
  light: { type: String, default: '#FDF0EF' },
  navBg: { type: String, default: '#ffffff' },
  navText: { type: String, default: '#111827' },
  name: { type: String, default: '商城' },
})

const grad = computed(() => `linear-gradient(135deg, ${props.color}, ${props.dark})`)
const shell = computed(() => ({
  '--preview-color': props.color,
  background: '#f4f5f7',
}))
</script>

<style scoped>
.live-preview {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  width: 100%;
}

.phone-label {
  margin: 0 0 8px;
  font-size: 12px;
  color: #606266;
  text-align: center;
}

.phone-body {
  border: 1px solid #ebeef5;
  border-radius: 18px;
  overflow: hidden;
  min-height: 280px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
  background: #f4f5f7;
  display: flex;
  flex-direction: column;
}

.phone-nav {
  display: grid;
  grid-template-columns: 24px 1fr 24px;
  align-items: center;
  height: 36px;
  padding: 0 10px;
  font-size: 11px;
  font-weight: 600;
}

.nav-title {
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dot,
.cart {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.2;
  display: inline-block;
}

.cart {
  width: auto;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  font-size: 9px;
  line-height: 16px;
  text-align: center;
  opacity: 1;
  background: var(--preview-color, #e0453c);
  color: #fff;
  padding: 0 4px;
}

.back {
  font-size: 16px;
  line-height: 1;
}

.phone-banner {
  height: 72px;
  margin: 8px;
  border-radius: 8px;
}

.phone-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 0 8px;
  flex: 1;
}

.tile {
  background: #fff;
  border-radius: 8px;
  padding: 6px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tile i {
  display: block;
  height: 42px;
  border-radius: 6px;
  opacity: 0.55;
}

.tile b {
  font-size: 11px;
}

.phone-tab {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  font-size: 10px;
  text-align: center;
  padding: 8px 0;
  background: #fff;
  border-top: 2px solid #eee;
  color: #909399;
}

.phone-tab em {
  font-style: normal;
  font-weight: 700;
}

.detail-hero {
  height: 120px;
}

.detail-info {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sku {
  font-size: 12px;
  color: #303133;
}

.ticket {
  align-self: flex-start;
  color: #fff;
  font-size: 14px;
  padding: 4px 10px;
  clip-path: polygon(0 0, calc(100% - 8px) 0, 100% 50%, calc(100% - 8px) 100%, 0 100%);
}

.buy {
  border: 0;
  color: #fff;
  height: 32px;
  border-radius: 8px;
  font-size: 12px;
}

.order-status {
  padding: 12px 12px 4px;
  font-size: 13px;
  font-weight: 700;
}

.order-row {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  font-size: 11px;
}

.order-row i {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  flex-shrink: 0;
  opacity: 0.6;
}

.order-row b {
  display: block;
  color: #303133;
}

.order-row em {
  color: #909399;
  font-style: normal;
}

.order-total {
  margin-top: auto;
  padding: 10px 12px;
  font-size: 12px;
  background: #fff;
  text-align: right;
}

.me-head {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 16px 12px;
  color: #fff;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.85);
}

.me-head b,
.me-head em {
  display: block;
  font-size: 12px;
}

.me-head em {
  opacity: 0.85;
  font-style: normal;
  font-size: 10px;
  margin-top: 4px;
}

.me-list {
  list-style: none;
  margin: 8px;
  padding: 0;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.me-list li {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  font-size: 12px;
  color: #303133;
  border-bottom: 1px solid #f2f3f5;
}

.me-list li:last-child {
  border-bottom: 0;
}

@media (max-width: 1100px) {
  .live-preview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
