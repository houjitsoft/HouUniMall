<template>
	<view
		v-if="propsobj.badgeType == '1'"
		class="goods-badge"
		:class="'badge-style-' + (propsobj.badgeStyle || '1')"
	>
		<text class="badge-text">{{ propsobj.badgeText || '热卖' }}</text>
	</view>
	<image
		v-else-if="propsobj.badgeType == '2' && propsobj.badgeImg"
		class="goods-badge-custom"
		:src="propsobj.badgeImg"
		mode="aspectFit"
	/>
</template>

<script>
export default {
	name: 'goodsBadge',
	props: {
		propsobj: {
			type: Object,
			default() {
				return {}
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.goods-badge {
	position: absolute;
	left: 0;
	top: 0;
	z-index: 2;
	display: flex;
	align-items: center;
	justify-content: center;
	pointer-events: none;
	box-sizing: border-box;

	.badge-text {
		color: #fff;
		font-size: 20rpx;
		line-height: 1.1;
		font-weight: 500;
		white-space: nowrap;
	}
}

.badge-style-1 {
	width: 56rpx;
	height: 60rpx;
	padding-bottom: 10rpx;
	padding-right: 8rpx;
	background: #C92A2A;
	clip-path: polygon(0 0, 86% 0, 86% 100%, 50% 82%, 0 100%);
}

.badge-style-1::after {
	content: '';
	position: absolute;
	right: 0;
	top: 0;
	width: 8rpx;
	height: 6rpx;
	background: #d71616;
	clip-path: polygon(0 0, 100% 100%, 0 100%);
}

.badge-style-2 {
	width: 52rpx;
	height: 56rpx;
	padding-bottom: 8rpx;
	background: #C92A2A;
	border-radius: 0 0 26rpx 26rpx;
}

.badge-style-3 {
	width: 64rpx;
	height: 44rpx;
	background: #C92A2A;
	border-radius: 0 22rpx 22rpx 0;
}

.goods-badge-custom {
	position: absolute;
	left: 0;
	top: 0;
	z-index: 2;
	width: 76rpx;
	height: 76rpx;
	pointer-events: none;
}
</style>
