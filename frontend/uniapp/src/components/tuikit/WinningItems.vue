<template>
	<view class="LotteryProducts disFlex ">
		<image class="goodsImage" v-if="lotteryInfo?.giftType == 1" :src="lotteryInfo?.goodsImage"></image>
		<image class="goodsImage" v-if="lotteryInfo?.giftType == 2" :src="lotteryInfo?.voucherTCustomimg"></image>
		<image class="goodsImage" v-if="lotteryInfo?.giftType == 3" :src="lotteryInfo?.voucherTCustomimg"></image>
		<view class="LotteryProductsRight">
			<view class="goodsName disFlex">
				<text v-if="lotteryInfo?.giftType == 1">{{lotteryInfo?.goodsName}}</text>
				<text v-if="lotteryInfo?.giftType == 2">{{lotteryInfo?.voucherTTitle}}</text>
				<text v-if="lotteryInfo?.giftType == 3">{{lotteryInfo?.voucherTTitle}}</text>
			</view>
			<view class="winnerNum disFlex" v-if="type === 1">
				<text class="fz26 colFff">共{{lotteryInfo?.winnerNum}}份 | {{ peopleNum }} 人已参与 </text>
			</view>
			<view class="goodsPrice disFlex" v-if="lotteryInfo?.giftType == 1">
				<text class="price fz35">￥0</text>
				<text class="del fz26 ml20">￥{{lotteryInfo?.goodsPrice}}</text>
			</view>
			<view class="goodsPrice disFlex" v-else>
				<text class="c-666 fz26 ml20">{{lotteryInfo?.giftType == 2 ? '优惠券' : '兑换券'}}</text>
				<text class="c-666 fz26 ml20" v-if="type === 2">,可到我的奖品中进行查看。</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		reactive,
		watch,
		computed,
		onUnmounted,
		onMounted
	} from 'vue';
	const props = defineProps({
		lotteryInfo: {
			type: Object,
			default: () => {

			}
		},
		type: {
			type: Number,
			default: 1
		},
		liveID: {
			type: String,
			default: ''
		}
	});

	const liveID = computed(() => props.liveID || uni?.$liveID);
	const peopleNum = ref(0);

	// #ifdef APP
	import {
		useBarrageState
	} from '@/uni_modules/tuikit-atomic-x/state/BarrageState';

	const {
		messageList
	} = useBarrageState(liveID.value);
	// #endif

	// #ifndef APP
	import {
		useBarrageState
	} from 'tuikit-atomicx-vue3';
	const {
		messageList
	} = useBarrageState();
	// #endif

	watch(messageList, (newVal) => {
		console.log("newVal：",newVal);
		if (newVal && newVal.length > 0) {
			const obj = newVal[newVal.length - 1];

			if (!obj?.data) return

			const obj2 = JSON.parse(obj.data);

			const obj3 = obj2.msgBody[0].msgContent.data.Data;

			// #ifdef APP
			const businessID = newVal[newVal.length - 1].businessID;
			// #endif

			// #ifndef APP
			const businessID = newVal[newVal.length - 1].businessId;
			// #endif


			if (businessID === 'peopleNum') {

				console.log("参与人数自定义消息：", obj3);

				peopleNum.value = obj3?.takeUserNum;
			}
		}
	},{deep:true});
</script>

<style>
	/* 公共 */
	.disFlex {
		display: flex;
		flex-direction: row;
	}

	.alignCenter {
		align-items: center;
	}

	.colFff {
		color: #fff;
	}


	.LotteryProducts {
		width: 680rpx;
		padding: 15rpx;
		border-radius: 15rpx;
		background-color: #F1F1F1;
	}

	.LotteryProducts .goodsImage {
		width: 150rpx;
		height: 150rpx;
		border-radius: 10rpx;
		margin-right: 15rpx;
	}

	.LotteryProducts .LotteryProductsRight {
		display: flex;
		flex-direction: column;
	}

	.LotteryProductsRight .goodsName {
		color: #000;
		width: 490rpx;
		overflow: hidden;
		white-space: nowrap;
		text-overflow: ellipsis;
	}

	.LotteryProductsRight .winnerNum {
		width: 300rpx;
		padding: 4rpx;
		border-radius: 10rpx;
		border: 1rpx solid #e2e2e2;
		margin: 10rpx 0rpx;
		justify-content: center;
		background: var(--theme-color, #002FA7);
	}

	.LotteryProductsRight .goodsPrice {
		align-items: flex-end;
	}
</style>