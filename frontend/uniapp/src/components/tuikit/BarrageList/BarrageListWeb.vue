<template>
	<view class="barrage-container">
		<!-- 聊天消息列表 -->
		<scroll-view id="scrollView" class="chat-list" scroll-y :show-scrollbar="false"
			:style="{ bottom: bottomPx + 'px' }" :scroll-top="scrollTop">
			<view class="chat-item" v-if="mixMessageList.length > 0" v-for="(message) in mixMessageList"
				:key="message?.sequence">
				<view class="message-content-wrapper" v-if="message?.textContent">
					<view class="nickname-content-gift" v-if="message?.gift">
						<text class="chat-nickname"
							numberOfLines="1">{{ message?.sender?.userName || message?.sender?.userID }}：</text>
						<view class="gift-row">
							<text class="gift-prefix">{{ giftPrefix }}</text>
							<text class="gift-recipient">{{ giftReceiverName }}</text>
							<text class="gift-name">{{ message?.textContent || '' }}</text>
							<image class="gift-icon" :src="message?.gift?.iconURL || ''" alt="" />
							<text class="gift-count" style="font-size: 26rpx;">x{{ message?.count || '1' }}</text>
						</view>
					</view>
					<view class="nickname-content-wrapper" v-if="!message?.gift">
						<view class="nickname-content-wrapper">
							<text class="chat-nickname"
								numberOfLines="1">{{ message?.sender?.userName || message?.sender?.userID }}：</text>
							<text class="chat-content">{{ message?.textContent || '' }}</text>
						</view>
					</view>
				</view>
			</view>
			<view ref="ListBottom" id="ListBottom" style="height: 50rpx;"></view>
		</scroll-view>
	</view>
</template>

<script setup>
	import {
		ref,
		watch,
		computed,
		onMounted,
		onUnmounted,
		nextTick,
		getCurrentInstance
	} from 'vue';
	import {
		useBarrageState
	} from 'tuikit-atomicx-vue3';
	const props = defineProps({
		bottomPx: {
			type: Number,
			default: 0
		},
		liveID: {
			type: String,
			default: 0
		},
	});
	const {
		messageList
	} = useBarrageState();
	const proxy = getCurrentInstance();
	const mixMessageList = ref([]);
	const ListBottom = ref('ListBottom');
	const scrollTop = ref(0);
	
	// 监听数据变化
	watch(messageList, (newValue) => {
		mixMessageList.value = newValue;
		scrollToBottom()
	}, {
		deep: true,
		immediate: true
	});

	function scrollToBottom() {
		nextTick(() => {
			const query = uni.createSelectorQuery().in(proxy)
			// 先获取 scroll-view 内容总高度（即 scrollHeight）
			query.select('#scrollView').fields({
				scrollOffset: true
			}, res => {
				if (res) {
					scrollTop.value = res.scrollHeight // 设置 scroll-top 为内容总高度
				}
			}).exec()
		})
	}
</script>

<style>
	.barrage-container {
		position: relative;
	}

	.chat-item {
		border-radius: 32rpx;
		/* padding: 6rpx; */
		max-width: 500rpx;
	}

	.message-content-wrapper {
		flex-direction: row;
		align-items: flex-start;
		min-width: 0;
		max-width: 500rpx;
		width: fit-content;
		background-color: rgba(0, 0, 0, 0.25);
		border-radius: 999rpx;
		padding: 2rpx 15rpx;
		margin-top: 10rpx;
	}

	.nickname-content-wrapper {
		flex: 1;
		flex-direction: row;
		align-items: flex-start;
		justify-content: center;
		min-width: 0;
	}

	.nickname-content-gift {
		flex-direction: row;
		align-items: flex-start;
		min-width: 0;
		max-width: 500rpx;
		width: fit-content;
	}

	.chat-nickname {
		color: #80BEF6;
		font-size: 24rpx;
		line-height: 24rpx;
		margin-right: 8rpx;
		padding: 5rpx 0;
		flex-shrink: 0;
		max-width: 200rpx;
		lines: 1;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.gift-row {
		display: flex;
		flex-direction: row;
		align-items: center;
		max-width: 300rpx;
		flex: 1;
		justify-content: flex-start;
		word-wrap: break-word;
		word-break: break-all;
		white-space: normal;
		flex-wrap: wrap;
		padding-top: 2rpx;
	}

	.gift-icon {
		width: 24rpx;
		height: 24rpx;
		flex-shrink: 0;
		margin: 0;
	}

	.gift-recipient,
	.gift-name {
		color: #ffffff;
		font-size: 24rpx;
		line-height: 24rpx;
		font-weight: 500;
		z-index: 999;
		padding: 2rpx 0;
		max-width: 300rpx;
		word-wrap: break-word;
		word-break: break-all;
		white-space: normal;
		text-align: left;
		min-width: 0;
	}

	.gift-prefix,
	.gift-count {
		color: #ffffff;
		font-size: 24rpx;
		line-height: 24rpx;
		font-weight: 500;
		z-index: 999;
	}

	.chat-content {
		color: #ffffff;
		font-size: 24rpx;
		line-height: 24rpx;
		font-weight: 500;
		z-index: 999;
		padding: 5rpx 0;
		word-wrap: break-word;
		word-break: break-all;
		white-space: normal;
		flex: 1;
		min-width: 0;
		text-indent: 0;
		padding-left: 0;
	}
</style>