<template>
	<view v-if="visible" class="subsidy-badge">
		<text class="subsidy-badge__tag">国家补贴</text>
		<text class="subsidy-badge__text">{{ text }}</text>
	</view>
</template>

<script>
import { shouldShowSubsidy, subsidyLineAmount } from '@/utils/subsidy.js'

export default {
	name: 'SubsidyBadge',
	props: {
		// 后端试算结果；null 表示插件未安装/未命中
		quote: { type: Object, default: () => null },
		// 行级商品 ID：传入时只显示该行可减金额（购物车/详情列表用）
		goodsId: { type: [Number, String], default: null }
	},
	computed: {
		visible() {
			// 整单未命中则不显示；传入 goodsId 时，仅当该行确实有可减金额才显示，
			// 避免把「未命中类目」的商品也打上国补标。
			if (!shouldShowSubsidy(this.quote)) return false
			if (this.goodsId !== null && this.goodsId !== undefined) {
				return this.amount > 0
			}
			return true
		},
		amount() {
			return subsidyLineAmount(this.quote, this.goodsId)
		},
		text() {
			const v = this.amount > 0 ? this.amount.toFixed(2) : '0.00'
			return this.goodsId === null || this.goodsId === undefined
				? `预计可减 ¥${v}`
				: `本商品可减 ¥${v}`
		}
	}
}
</script>

<style scoped>
.subsidy-badge {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	margin-top: 8rpx;
	padding: 3rpx 10rpx;
	border: 1rpx solid #fecaca;
	border-radius: 4rpx;
	background: #fef2f2;
}
.subsidy-badge__tag {
	padding: 0 8rpx;
	border-radius: 3rpx;
	background: #dc2626;
	color: #fff;
	font-size: 20rpx;
	line-height: 30rpx;
	font-weight: 600;
}
.subsidy-badge__text {
	color: #b91c1c;
	font-size: 22rpx;
	line-height: 30rpx;
}
</style>
