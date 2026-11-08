<template>
	<view class="base-top" :class="['base-top--' + mode, { 'base-top--show': isShow }]" :style="wrapStyle"
		@click="goTop">
		<slot>
			<text class="iconfont icon-xiala base-top__icon" :style="iconInnerStyle"></text>
			<text v-if="text" class="base-top__text">{{ text }}</text>
		</slot>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onPageScroll } from '@dcloudio/uni-app'
import { publicVariable } from '@/utils/publicVariable.js'

defineOptions({
	name: 'BaseTop'
})

const props = defineProps({
	// 页面或 scroll-view 的滚动距离；不传时组件自行监听页面滚动
	scrollTop: {
		type: Number,
		default: undefined
	},
	// 滚动超过该距离后显示，单位 px
	threshold: {
		type: Number,
		default: 400
	},
	// 距底部距离，单位 px
	bottom: {
		type: [Number, String],
		default: 80
	},
	// 距右侧距离，单位 px
	right: {
		type: [Number, String],
		default: 16
	},
	// 回顶动画时长，单位 ms
	duration: {
		type: Number,
		default: 300
	},
	zIndex: {
		type: Number,
		default: 9
	},
	mode: {
		type: String,
		default: 'circle'
	},
	text: {
		type: String,
		default: ''
	},
	color: {
		type: String,
		default: ''
	}
})

const emit = defineEmits(['click'])

const innerScrollTop = ref(0)

const currentScrollTop = computed(() => {
	return typeof props.scrollTop === 'number' ? props.scrollTop : innerScrollTop.value
})

const isShow = computed(() => currentScrollTop.value >= props.threshold)

const iconColor = computed(() => {
	return props.color || publicVariable.themeData.contMxColor || '#333'
})

const wrapStyle = computed(() => ({
	bottom: addUnit(props.bottom),
	right: addUnit(props.right),
	zIndex: props.zIndex
}))

const iconInnerStyle = computed(() => ({
	color: iconColor.value
}))

onPageScroll((e) => {
	if (typeof props.scrollTop === 'number') {
		return
	}
	innerScrollTop.value = e.scrollTop
})

const addUnit = (value) => {
	if (typeof value === 'number') {
		return value + 'px'
	}
	return value
}

const goTop = () => {
	uni.pageScrollTo({
		scrollTop: 0,
		duration: props.duration
	})
	emit('click')
}
</script>

<style scoped>
.base-top {
	position: fixed;
	width: 80rpx;
	height: 80rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	background: #fff;
	box-shadow: none;
	opacity: 0;
	visibility: hidden;
	pointer-events: none;
	transform: translateY(20rpx);
	transition: opacity 0.25s ease, transform 0.25s ease, visibility 0.25s ease;
}

.base-top--circle {
	border-radius: 50%;
}

.base-top--square {
	border-radius: 12rpx;
}

.base-top--show {
	opacity: 1;
	visibility: visible;
	pointer-events: auto;
	transform: translateY(0);
}

.base-top__icon {
	font-size: 36rpx;
	line-height: 1;
	transform: rotate(180deg);
}

.base-top__text {
	margin-top: 2rpx;
	font-size: 20rpx;
	line-height: 1;
	color: #666;
	transform: scale(0.9);
}
</style>
