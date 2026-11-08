<template>
	<view class="lottery-btn">
		<view v-if="timer" @click="handleLottert">
			<image class="action-button-icon" :src="`${mallRequest.MALL_IMG}/liveImage/live-lottery.png`" mode="aspectFit" />
			<view class="">
				<text class="fz26 colFff">{{timeHtml}}</text>
			</view>
		</view>
	</view>


	<view class="bottom-drawer-lottery" v-if="isShowLiveLootery">
		<view class="drawer-lottery" @tap="close"></view>
		<view class="lottery-box" :class="{ 'drawer-open': isShowLiveLootery }" @click.stop>
			<view class="lottery-herder disFlex">
				<text>直播抽奖福利</text>
			</view>
			<view class="lottery-content">
				<view class="lottery-time disFlex">
					<text class="lottery-time-text">·</text>
					<text class="fz26 "> {{timeHtml}} 后开奖</text>
					<text class="lottery-time-text">·</text>
				</view>
				<WinningItems :liveID="liveID" :type="1" :lotteryInfo="lotteryInfo"></WinningItems>
				<view class="enterDrawButtom disFlex" v-if="!lotteryResults">
					<view class="enterDrawBtn disFlex">
						<text class="colFff" @click="handleEnterDrawChange">{{isEnterDraw ? '点击参与抽奖' : '已参与抽奖'}}</text>
					</view>
				</view>
			</view>
		</view>
	</view>

	<view class="winners-drawer" v-if="isShowWinners && !isEnterDraw">
		<view class="drawer-lottery" @tap="close"></view>
		<view class="winners-box" @click.stop>
			<view class="winners-header disFlex">
				<text v-if="lotteryUserIdList.includes(uni?.$userID)">{{ lotteryResults?.title || '恭喜您，中奖了~'}}</text>
				<text v-else>奖品与你擦肩而过~</text>
			</view>
			<WinningItems v-if="lotteryUserIdList.includes(uni?.$userID)" :liveID="liveID" :type="2"
				:lotteryInfo="lotteryInfo"></WinningItems>
			<view class="winners">
				<view class="winnersTitle">
					<text>————中奖名单————</text>
				</view>
				<scroll-view class="winnersList"
					:style="!lotteryUserIdList.includes(uni?.$userID) ? {height:'600rpx'} : {}" scroll-y="true">
					<view class="winnersListItem disFlex" v-for="(item,index) in lotteryUserList" :key="index">
						<image class="userImage" :src="item.userAvatar" mode=""></image>
						<text class="userName">{{item.userName || item.userNickname}}</text>
					</view>
				</scroll-view>
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
	import {
		apiTakeLottery,
		apiWinLottery,
		apiUpdateLottery,
		apiQueryLotteryUser
	} from "@/api/tuikit/index.js"
    import {
        mallRequest
    } from "@/utils/request.js"
	import WinningItems from "@/components/tuikit/WinningItems.vue"
	const props = defineProps({
		liveID: {
			type: String,
			default: ''
		}
	});
	const liveID = computed(() => props.liveID || uni?.$liveID);

	const lotteryInfo = ref({});
	const lotteryUserList = ref([]);
	const lotteryUserIdList = ref([]);
	let timer = ref(null);

	const isShowLiveLootery = ref(false);
	const isShowWinners = ref(false);
	const isEnterDraw = ref(true); // 是否参与抽奖
	const lotteryResults = ref(null); // 抽奖结果
	const TotalSeconds = ref(60); // 总秒
	const timeHtml = ref("00：00"); // 倒计时

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


	watch(messageList, async (newVal, oldVal) => {
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


			if (businessID === 'lottery') {
				console.log("抽奖自定义消息：", obj3);
				lotteryInfo.value = null;
				lotteryInfo.value = obj3;

				if (lotteryInfo.value) {
					startTime();
					isEnterDraw.value = true;
					lotteryResults.value = null;
				}
			} else if (businessID === 'win') {

				console.log("中奖自定义消息：", obj3);
				lotteryResults.value = obj3;
			}
		}
	}, {
		deep: true
	});
	// 启动倒计时
	const startTime = () => {
		if (timer.value) return // 防止重复启动
		let currentTime = new Date().getTime();
		TotalSeconds.value = parseInt((lotteryInfo.value?.endTime - currentTime) / 1000);
		timer.value = setInterval(() => {
			if (TotalSeconds.value > 0) {
				TotalSeconds.value--;
				countdownTime();
			} else {
				stopTime();
				startNotifyServerLottery(1);
				startNotifyServerLottery(2);
			}
		}, 1000)
	}


	// 停止倒计时
	const stopTime = () => {
		if (timer.value) {
			clearInterval(timer.value)
			timer.value = null;
			isShowLiveLootery.value = false;
		}
	}

	onUnmounted(() => {
		stopTime() // 页面卸载时清除定时器，防止内存泄漏
	})
	const handleLottert = () => {
		isShowLiveLootery.value = true
	}
	// 取消
	const close = () => {
		isShowLiveLootery.value = false;
		isShowWinners.value = false;
	};

	// 参与抽奖
	const handleEnterDrawChange = async () => {
		if (!isEnterDraw.value) {
			uni.showToast({
				title: "您已参与过抽奖了~",
				icon: 'none'
			})
			return;
		}
		try {
			const {
				code
			} = await apiTakeLottery({
				id: lotteryInfo.value?.lotterId,
				userId: uni?.$userID
			})
			if (code === 1) isEnterDraw.value = false;
			uni.showToast({
				title: code === 1 ? "参与成功" : '参与失败',
				icon: 'none'
			})
		} catch (error) {
			console.log("参与抽奖失败：", error);
		}
	}

	// type 1 开始抽奖  2 抽奖倒计时结束
	const startNotifyServerLottery = async (type) => {
		console.log("通知服务端更新数据：", type, lotteryInfo.value?.lotterId);
		let res;
		try {
			if (type === 1) {
				res = await apiWinLottery({
					id: lotteryInfo.value?.lotterId
				})
			} else if (type === 2) {
				res = await apiUpdateLottery({
					id: lotteryInfo.value?.lotterId
				})
			}
			await getLotteryUserList();
			console.log('已通知服务端更新数据：', res);
		} catch (error) {
			console.log("通知服务端失败：", error);
		}
	}
	async function getLotteryUserList() {
		try {
			const {
				code,
				data
			} = await apiQueryLotteryUser({
				id: lotteryInfo.value?.lotterId,
				winningStatus: 1
			});
			lotteryUserList.value = code === 1 ? (data.list || []) : [];
			lotteryUserIdList.value = code === 1 ? (data.list.map(item => item.userId) || []) : [];
			console.log('查询中奖用户', data);
			isShowWinners.value = true;
		} catch (error) {
			console.log("查询中奖用户失败：", error);
		}
	}
	// 倒计时
	function countdownTime() {
		const mins = Math.floor(TotalSeconds.value / 60)
		const secs = TotalSeconds.value % 60
		timeHtml.value = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`

		console.log("倒计时时间：", timeHtml.value);
	}
</script>

<style>
	.lottery-btn {
		position: fixed;
		left: 40rpx;
		top: 250rpx;
	}

	.action-button-icon {
		width: 60rpx;
		height: 60rpx;
		margin-bottom: 4rpx;
	}

	.winners-drawer,
	.bottom-drawer-lottery {
		width: 750rpx;
		position: fixed;
		bottom: 0;
		top: 0;
		left: 0;
		right: 0;
		z-index: 1000;
	}

	.drawer-lottery {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, 0.4);
	}

	.drawer-open {
		/* #ifdef APP */
		transform: translateY(0%);
		/* #endif */
		/* #ifndef APP */
		transform: translateY(100%);
		/* #endif */
	}

	.lottery-box {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: #fff;
		/* #ifdef APP */
		transform: translateY(100%);
		/* #endif */
		/* #ifndef APP */
		transform: translateY(0%);
		/* #endif */
		transition-property: transform;
		transition-duration: 0.3s;
		transition-timing-function: ease;
		border-top-left-radius: 32rpx;
		border-top-right-radius: 32rpx;
	}

	.colFff {
		color: #fff;
	}

	.winners-header,
	.lottery-herder {
		padding: 25rpx 20rpx 20rpx;
		border-bottom: 1rpx solid #e2e2e2;
		justify-content: center;
		color: #000;
	}

	.lottery-content {
		padding: 20rpx;
	}

	.lottery-time {
		justify-content: center;
		align-items: center;
		margin: 10rpx 0rpx 20rpx;
		color: #000;
	}

	.lottery-time .lottery-time-text {
		width: 10rpx;
		height: 10rpx;
		margin: 0rpx 20rpx;
		transform: rotate(45deg);
		background-color: #EC335F;
	}


	.enterDrawButtom {
		justify-content: center;
		margin: 60rpx 0rpx;
	}

	.enterDrawButtom .enterDrawBtn {
		width: 400rpx;
		height: 60rpx;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
		border-radius: 30rpx;
		background: var(--theme-color, #002FA7);
	}

	.winners {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-top: 60rpx;
		color: #000;
	}

	.winners .winnersList {
		width: 700rpx;
		max-height: 300rpx;
		margin-top: 20rpx;
		padding: 20rpx 0rpx;
	}

	.winnersListItem {
		width: 660rpx;
		align-items: center;
		padding: 20rpx;
		margin-top: 10rpx;
		margin-bottom: 10rpx;
		border-radius: 20rpx;
		box-shadow: none;
	}

	.winnersListItem .userImage {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50rpx;
		margin-right: 15rpx;
	}

	.winners-box {
		width: 700rpx;
		position: fixed;
		top: 400rpx;
		left: 25rpx;
		right: 25rpx;
		background: #fff;
		border-radius: 20rpx;
		padding: 10rpx 10rpx 30rpx;
		box-sizing: border-box;
	}

	/* 公共 */
	.disFlex {
		display: flex;
		flex-direction: row;
	}

	.alignCenter {
		align-items: center;
	}
</style>